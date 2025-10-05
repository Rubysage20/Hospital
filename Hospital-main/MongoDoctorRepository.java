import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;

import java.util.*;

/**
 * MongoDoctorRepository
 * ---------------------
 * MongoDB-backed implementation of DoctorRepository.
 * Stores Doctor records in the "doctors" collection and maps Documents to Doctor objects.
 *
 * Notes:
 * - Numeric reads are type-tolerant (Number → doubleValue/intValue) to avoid
 *   ClassCastException when Mongo stored Int32/Int64 instead of Double.
 * - Creates helpful indexes on id (unique) and lastName.
 */
public class MongoDoctorRepository implements DoctorRepository {
    private final MongoCollection<Document> col; // "doctors" collection handle

    /**
     * Open a connection to the "doctors" collection and ensure indexes.
     * @param dbName name of the database (e.g., "gms_db")
     */
    public MongoDoctorRepository(String dbName) {
        MongoDatabase db = MongoConnection.db(dbName);
        this.col = db.getCollection("doctors");
        // Unique id for lookups, lastName for quick filtering
        col.createIndex(Indexes.ascending("id"), new IndexOptions().unique(true));
        col.createIndex(Indexes.ascending("lastName"));
    }

    /**
     * Upsert (save or update) a Doctor by id.
     * @param d doctor to persist
     * @return same doctor (for chaining)
     */
    @Override
    public Doctor save(Doctor d) {
        // Convert domain → BSON document (fields match your Doctor model)
        Document doc = new Document()
                .append("id", d.getUniqueId())
                .append("firstName", d.getFirstName())
                .append("lastName", d.getLastName())
                .append("address", d.getAddress())
                .append("speciality", d.getSpeciality())
                // Store numeric fields as-is; Mongo may choose Int32/Int64/Double
                .append("officeVisitFee", d.getOfficeVisitFee())
                .append("numVisitedPatients", d.getnumVisitedPatients())
                .append("monthlyWorkingHours", d.getMonthlyWorkingHours());

        // Upsert by id
        col.updateOne(Filters.eq("id", d.getUniqueId()),
                new Document("$set", doc),
                new UpdateOptions().upsert(true));

        return d;
    }

    /**
     * Find a doctor by unique id.
     */
    @Override
    public Optional<Doctor> findById(String id) {
        Document d = col.find(Filters.eq("id", id)).first();
        return Optional.ofNullable(fromDoc(d));
    }

    /**
     * Find a doctor by last name (first match, sorted by first name).
     */
    @Override
    public Optional<Doctor> findByLastName(String lastName) {
        Document d = col.find(Filters.eq("lastName", lastName))
                        .sort(Sorts.ascending("firstName"))
                        .first();
        return Optional.ofNullable(fromDoc(d));
    }

    /**
     * Return all doctors sorted by lastName, then firstName.
     */
    @Override
    public List<Doctor> findAll() {
        List<Doctor> out = new ArrayList<>();
        for (Document d : col.find().sort(Sorts.ascending("lastName", "firstName"))) {
            Doctor doc = fromDoc(d);
            if (doc != null) out.add(doc);
        }
        return out;
    }

    /**
     * Return all doctors with a matching last name, sorted by firstName.
     */
    @Override
    public List<Doctor> findAllByLastName(String lastName) {
        List<Doctor> out = new ArrayList<>();
        for (Document d : col.find(Filters.eq("lastName", lastName)).sort(Sorts.ascending("firstName"))) {
            Doctor doc = fromDoc(d);
            if (doc != null) out.add(doc);
        }
        return out;
    }

    /**
     * Delete a doctor by id.
     */
    @Override
    public void deleteById(String id) {
        col.deleteOne(Filters.eq("id", id));
    }

    // ---------------------------------------------------------------------
    // Mapping helpers (numeric-safe)
    // ---------------------------------------------------------------------

    /**
     * Read a numeric field as double, accepting Int32/Int64/Double/String.
     */
    private static double readDouble(Document d, String key, double defVal) {
        Object v = d.get(key);
        if (v instanceof Number n) return n.doubleValue();      // safe for Int32/Int64/Double
        if (v instanceof String s) {
            try { return Double.parseDouble(s); } catch (NumberFormatException ignore) {}
        }
        return defVal;
    }

    /**
     * Read a numeric field as int, accepting Int32/Int64/Double/String.
     */
    private static int readInt(Document d, String key, int defVal) {
        Object v = d.get(key);
        if (v instanceof Number n) return n.intValue();
        if (v instanceof String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException ignore) {}
        }
        return defVal;
    }

    /**
     * Convert BSON Document → Doctor domain object.
     * Fields not present use safe defaults to avoid NPEs.
     */
    private Doctor fromDoc(Document d) {
        if (d == null) return null;

        // Tolerant reads for numeric fields (avoid ClassCastException)
        double fee          = readDouble(d, "officeVisitFee", 0.0);
        int visitedPatients = readInt(d, "numVisitedPatients", 0);
        int monthlyHours    = readInt(d, "monthlyWorkingHours", 0);

        // Build Doctor (uses your existing constructor signature)
        Doctor doc = new Doctor(
                d.getString("id"),
                d.getString("firstName"),
                d.getString("lastName"),
                d.getString("address"),
                d.getString("speciality"),
                fee,
                visitedPatients,
                0.0 // keep existing last-arg behavior from your model usage
        );
        // Setters for any fields not in constructor
        doc.setMonthlyWorkingHours(monthlyHours);
        return doc;
    }
}

