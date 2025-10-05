import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;

import java.lang.reflect.Method;
import java.time.*;
import java.util.*;

import static com.mongodb.client.model.Filters.*;

/**
 * MongoAppointmentRepository
 * --------------------------
 * MongoDB-backed repository for Appointment entities.
 * - Writes legacy ISO strings ("start","end") and epoch millis ("startTime","endTime").
 * - Implements all AppointmentRepository methods.
 * - No hard dependency on getPriority()/setPriority or status setters.
 */
public class MongoAppointmentRepository implements AppointmentRepository {

    // Mongo collection for appointments
    private final MongoCollection<Document> col;

    // Repositories to resolve foreign keys
    private final MongoDoctorRepository doctorRepo;
    private final MongoPatientRepository patientRepo;

    // System time zone for epoch <-> LocalDateTime conversion
    private final ZoneId zone = ZoneId.systemDefault();

    /**
     * Initialize repository with DB name and ensure indexes.
     */
    public MongoAppointmentRepository(String dbName) {
        MongoDatabase db = MongoConnection.db(dbName);
        this.col = db.getCollection("appointments");
        this.doctorRepo = new MongoDoctorRepository(dbName);
        this.patientRepo = new MongoPatientRepository(dbName);

        // Indexes: unique id; lookups by doctor/patient; sort/range by startTime
        col.createIndex(Indexes.ascending("id"), new IndexOptions().unique(true));
        col.createIndex(Indexes.ascending("doctorId"));
        col.createIndex(Indexes.ascending("patientId"));
        col.createIndex(Indexes.ascending("startTime"));
    }

    // ---------------------------------------------------------------------
    // CRUD + Queries
    // ---------------------------------------------------------------------

    /** Upsert by id. */
    @Override
    public Appointment save(Appointment a) {
        Objects.requireNonNull(a, "appointment");
        Objects.requireNonNull(a.getId(), "appointment.id");
        Objects.requireNonNull(a.getDoctor(), "appointment.doctor");
        Objects.requireNonNull(a.getPatient(), "appointment.patient");
        Objects.requireNonNull(a.getStart(), "appointment.start");
        Objects.requireNonNull(a.getEnd(), "appointment.end");

        // --- ensure foreign keys exist in our system ---
        String did = a.getDoctor().getUniqueId();
        String pid = a.getPatient().getUniqueId();

        if (doctorRepo.findById(did).isEmpty()) {
            throw new IllegalArgumentException("Unknown doctorId: " + did);
        }
        if (patientRepo.findById(pid).isEmpty()) {
            throw new IllegalArgumentException("Unknown patientId: " + pid);
        }

        long startMs = toEpochMillis(a.getStart());
        long endMs   = toEpochMillis(a.getEnd());
        Integer priority = extractPriority(a); // optional

        Document doc = new Document()
                .append("id", a.getId())
                .append("doctorId", a.getDoctor().getUniqueId())
                .append("patientId", a.getPatient().getUniqueId())
                // legacy string fields (compat with old data)
                .append("start", a.getStart().toString())
                .append("end", a.getEnd().toString())
                // epoch millis for fast sorts/range queries
                .append("startTime", startMs)
                .append("endTime", endMs)
                .append("status", a.getStatus().toString());
        if (priority != null) doc.append("priority", priority);

        col.updateOne(eq("id", a.getId()), new Document("$set", doc), new UpdateOptions().upsert(true));
        return a;
    }

    @Override
    public Optional<Appointment> findById(String id) {
        Document d = col.find(eq("id", id)).first();
        return Optional.ofNullable(fromDoc(d));
    }

    @Override
    public List<Appointment> findAll() {
        List<Appointment> out = new ArrayList<>();
        for (Document d : col.find().sort(Sorts.ascending("startTime", "start"))) {
            Appointment a = fromDoc(d);
            if (a != null) out.add(a);
        }
        return out;
    }

    @Override
    public List<Appointment> findByDoctor(String doctorId) {
        List<Appointment> results = new ArrayList<>();
        for (Document d : col.find(eq("doctorId", doctorId)).sort(Sorts.ascending("startTime", "start"))) {
            Appointment a = fromDoc(d);
            if (a != null) results.add(a);
        }
        return results;
    }

    @Override
    public List<Appointment> findByPatient(String patientId) {
        List<Appointment> results = new ArrayList<>();
        for (Document d : col.find(eq("patientId", patientId)).sort(Sorts.ascending("startTime", "start"))) {
            Appointment a = fromDoc(d);
            if (a != null) results.add(a);
        }
        return results;
    }

    @Override
    public List<Appointment> findBetween(LocalDateTime from, LocalDateTime to) {
        List<Appointment> results = new ArrayList<>();
        long fromMs = toEpochMillis(from);
        long toMs   = toEpochMillis(to);

        for (Document d : col.find(and(gte("startTime", fromMs), lt("endTime", toMs)))
                              .sort(Sorts.ascending("startTime"))) {
            Appointment a = fromDoc(d);
            if (a != null) results.add(a);
        }
        return results;
    }

    @Override
    public void deleteById(String id) {
        col.deleteOne(eq("id", id));
    }

    // ---------------------------------------------------------------------
    // Mapping
    // ---------------------------------------------------------------------

    /** Convert Document -> Appointment using your 5-arg constructor. */
    private Appointment fromDoc(Document d) {
        if (d == null) return null;

        String doctorId  = d.getString("doctorId");
        String patientId = d.getString("patientId");

        Optional<Doctor> docOpt = doctorRepo.findById(doctorId);
        Optional<Patient> patOpt = patientRepo.findById(patientId);
        if (docOpt.isEmpty() || patOpt.isEmpty()) return null;

        LocalDateTime start = readDateTimeFlexible(d.get("startTime"), d.get("start"));
        LocalDateTime end   = readDateTimeFlexible(d.get("endTime"), d.get("end"));
        if (start == null || end == null) return null; // invalid record

        Integer priority = toIntSafe(d.get("priority")); // read if present (ignored by model)
        // Status is read for completeness; your Appointment has no setter,
        // and calling complete()/cancel() here would cause side effects.
        Appointment.Status status = safeStatus(d.getString("status"));

        // Try several constructors, preferring your 5-arg
        Appointment built = tryConstruct(d.getString("id"),
                                         patOpt.get(), docOpt.get(),
                                         start, end, priority, status);
        if (built != null) return built;

        // Fallback to no-arg + setters if available
        try {
            Appointment a = Appointment.class.getDeclaredConstructor().newInstance();
            invokeSetterIfPresent(a, "setId", String.class, d.getString("id"));
            invokeSetterIfPresent(a, "setPatient", Patient.class, patOpt.get());
            invokeSetterIfPresent(a, "setDoctor", Doctor.class, docOpt.get());
            invokeSetterIfPresent(a, "setStart", LocalDateTime.class, start);
            invokeSetterIfPresent(a, "setEnd", LocalDateTime.class, end);
            if (priority != null) invokeSetterIfPresent(a, "setPriority", Integer.class, priority);
            // no status setter in your model; skip
            return a;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Try multiple constructors in descending preference:
     *  1) (String, Patient, Doctor, LocalDateTime, LocalDateTime) <-- my model
     *  2) (String, Patient, Doctor, LocalDateTime, LocalDateTime, Appointment.Status)
     *  3) (String, Patient, Doctor, LocalDateTime, LocalDateTime, int, Appointment.Status)
     */
    private Appointment tryConstruct(String id, Patient p, Doctor d,
                                     LocalDateTime start, LocalDateTime end,
                                     Integer priority, Appointment.Status status) {
        try {
            return Appointment.class
                    .getConstructor(String.class, Patient.class, Doctor.class,
                                    LocalDateTime.class, LocalDateTime.class)
                    .newInstance(id, p, d, start, end);
        } catch (Exception ignored) { }
        try {
            return Appointment.class
                    .getConstructor(String.class, Patient.class, Doctor.class,
                                    LocalDateTime.class, LocalDateTime.class, Appointment.Status.class)
                    .newInstance(id, p, d, start, end, status);
        } catch (Exception ignored) { }
        try {
            return Appointment.class
                    .getConstructor(String.class, Patient.class, Doctor.class,
                                    LocalDateTime.class, LocalDateTime.class, int.class, Appointment.Status.class)
                    .newInstance(id, p, d, start, end, priority == null ? 1 : priority, status);
        } catch (Exception ignored) { }
        return null;
    }

    // ---------------------------------------------------------------------
    // Safe conversions / reflection helpers
    // ---------------------------------------------------------------------

    /** Convert LocalDateTime -> epoch millis in system zone. */
    private long toEpochMillis(LocalDateTime ldt) {
        return ldt.atZone(zone).toInstant().toEpochMilli();
    }

    /** Convert epoch millis -> LocalDateTime in system zone. */
    private LocalDateTime fromEpochMillis(long ms) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), zone);
    }

    /** Read time from millis if available, else parse ISO string. */
    private LocalDateTime readDateTimeFlexible(Object millisOrNull, Object isoOrNull) {
        if (millisOrNull instanceof Number) return fromEpochMillis(((Number) millisOrNull).longValue());
        if (isoOrNull instanceof String) {
            try { return LocalDateTime.parse((String) isoOrNull); } catch (Exception ignored) { }
        }
        return null;
    }

    /** Optional priority getter via reflection; returns null if absent. */
    private Integer extractPriority(Object target) {
        if (target == null) return null;
        try {
            Method m = target.getClass().getMethod("getPriority");
            Object val = m.invoke(target);
            if (val instanceof Number) return ((Number) val).intValue();
            if (val != null) return Integer.parseInt(String.valueOf(val));
        } catch (Exception ignored) { }
        return null;
    }

    /** Safely coerce an Object to Integer if possible. */
    private Integer toIntSafe(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(String.valueOf(value)); } catch (Exception e) { return null; }
    }

    /** Convert status string to enum; default to SCHEDULED if null/invalid. */
    private Appointment.Status safeStatus(String s) {
        if (s == null) return Appointment.Status.SCHEDULED;
        try { return Appointment.Status.valueOf(s); } catch (Exception e) { return Appointment.Status.SCHEDULED; }
    }

    /** Call a setter if it exists (used only on fallback path). */
    private void invokeSetterIfPresent(Object target, String setter, Class<?> paramType, Object value) {
        try {
            Method m = target.getClass().getMethod(setter, paramType);
            m.invoke(target, value);
        } catch (Exception ignored) { }
    }
}
