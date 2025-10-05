import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * AppointmentScheduler
 * --------------------
 * Milestone II (Algorithms & Data Structures):
 * - Uses a PriorityQueue to process higher-priority requests first, then earlier start time.
 * - Performs conflict detection for each doctor to prevent overlapping appointments.
 * - Works with the existing Appointment/Repository APIs (no changes needed to Appointment.java).
 *
 * * Enhancements Milestone III
 * - Enforces 1-hour slots: floors start to the top of the hour; end is start + 1h.
 * - Validates doctor/patient existence before enqueueing to avoid orphan appointments.
 * - Adds an interactive console flow (date and time prompted separately).
 * 
 * Complexity (per batch):
 * - Enqueue: O(log n) per request (priority queue insert).
 * - Processing: for each request, conflict check over the doctor's existing appointments.
 *   With naive scan it's O(m) per doctor; can be reduced by keeping per-doctor time-sorted lists.
 */
public class AppointmentScheduler {

    private final AppointmentRepository apptRepo;
    private final DoctorRepository doctorRepo;
    private final PatientRepository patientRepo;

    /**
     * Internal request type held in the priority queue.
     * priority: higher number means higher priority (0..3 recommended).
     * start/end are normalized to a single 1-hour window at enqueue time.
     */
    public static final class SchedulingRequest {
        public final String patientId;
        public final String doctorId;
        public final LocalDateTime start; // normalized to top-of-hour
        public final LocalDateTime end;   // start + 1 hour
        public final int priority;        // 0=LOW, 1=NORMAL, 2=HIGH, 3=CRITICAL

        public SchedulingRequest(String patientId,
                                 String doctorId,
                                 LocalDateTime start,
                                 LocalDateTime end,
                                 int priority) {
            this.patientId = ValidationUtils.requireNonBlank(patientId, "patientId");
            this.doctorId  = ValidationUtils.requireNonBlank(doctorId, "doctorId");
            ValidationUtils.requireStartBeforeEnd(start, end);
            this.start = start;
            this.end   = end;
            this.priority = Math.max(0, Math.min(priority, 3)); // clamp into 0..3
        }
    }

    /**
     * Queue orders by: higher priority first, then earlier start time.
     */
    private final PriorityBlockingQueue<SchedulingRequest> queue =
            new PriorityBlockingQueue<>(11,
                    Comparator.<SchedulingRequest>comparingInt(r -> -r.priority)
                              .thenComparing(r -> r.start));

    public AppointmentScheduler(AppointmentRepository apptRepo,
                                DoctorRepository doctorRepo,
                                PatientRepository patientRepo) {
        this.apptRepo = apptRepo;
        this.doctorRepo = doctorRepo;
        this.patientRepo = patientRepo;
    }

    // -----------------------------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------------------------

    /**
     * Enqueue a scheduling request with the given priority.
     * Enforces 1-hour slots (floor to hour; end = start + 1h) and rejects
     * if doctor/patient do not exist.
     * @param priority 0=LOW, 1=NORMAL, 2=HIGH, 3=CRITICAL
     */
    public void request(String patientId,
                        String doctorId,
                        LocalDateTime start,
                        LocalDateTime end,
                        int priority) {
        // normalize to a 1-hour window at enqueue time
        LocalDateTime normStart = floorToHour(start);
        LocalDateTime normEnd   = normStart.plusHours(1);

        // validate existence to prevent orphan requests sitting in the queue
        if (doctorRepo.findById(doctorId).isEmpty()) {
            throw new IllegalArgumentException("Unknown doctorId: " + doctorId);
        }
        if (patientRepo.findById(patientId).isEmpty()) {
            throw new IllegalArgumentException("Unknown patientId: " + patientId);
        }

        queue.add(new SchedulingRequest(patientId, doctorId, normStart, normEnd, priority));
    }

    /**
     * Convenience overload: default priority NORMAL (1).
     * Still applies 1-hour normalization and FK checks.
     */
    public void request(String patientId,
                        String doctorId,
                        LocalDateTime start,
                        LocalDateTime end) {
        request(patientId, doctorId, start, end, 1);
    }

    /**
     * Drain and attempt to schedule every queued request.
     * Conflicting requests (overlap with existing non-canceled appts) are rejected.
     *
     * Also updates Doctor counters on success so payroll reflects the booking.
     *
     * @return Result containing lists of scheduled and rejected requests.
     */
    public Result processAll() {
        List<SchedulingRequest> scheduled = new ArrayList<>();
        List<SchedulingRequest> rejected  = new ArrayList<>();

        SchedulingRequest req;
        while ((req = queue.poll()) != null) {
            if (!canSchedule(req.doctorId, req.start, req.end)) {
                rejected.add(req);
                continue;
            }

            var doctorOpt  = doctorRepo.findById(req.doctorId);
            var patientOpt = patientRepo.findById(req.patientId);
            if (doctorOpt.isEmpty() || patientOpt.isEmpty()) {
                // defensive: should not happen due to request() checks
                rejected.add(req);
                continue;
            }

            Doctor doctor   = doctorOpt.get();
            Patient patient = patientOpt.get();

            // Persist the appointment using your existing constructor.
            Appointment appt = new Appointment(
                    UUID.randomUUID().toString(),
                    patient,
                    doctor,
                    req.start,
                    req.end
            );
            apptRepo.save(appt);

            // ---- Bookkeeping: update doctor so payroll sees the work immediately ----
            int hours = (int) java.time.Duration.between(req.start, req.end).toHours();
            if (hours < 1) hours = 1; // slots are 1h; guard against zero/negatives

            // increment hours + visit count (method names match your model)
            doctor.setMonthlyWorkingHours(doctor.getMonthlyWorkingHours() + hours);
            doctor.setnumVisitedPatients(doctor.getnumVisitedPatients() + 1);

            // persist updated doctor back to Mongo
            doctorRepo.save(doctor);
            // ------------------------------------------------------------------------

            scheduled.add(req);
        }

        return new Result(scheduled, rejected);
    }

    // -----------------------------------------------------------------------------------------
    // Interactive console flow (date and time in separate prompts)
    // -----------------------------------------------------------------------------------------

    /**
     * Interactive scheduling flow (date + time separated):
     * - Date: yyyy-MM-dd (e.g., 2025-10-05)
     * - Time: hour only (0..23, accepts "9", "09", or "09:00"; minutes ignored)
     * - Floors to top-of-hour and sets end = start + 1h
     * - Validates doctor/patient IDs
     * - Enqueues and processes immediately; prints summary with separate date/time.
     */
    public void scheduleInteractive() {
        final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Schedule Appointment (interactive) ===");

        // doctor
        System.out.print("Doctor ID: ");
        String did = scanner.nextLine().trim();
        if (doctorRepo.findById(did).isEmpty()) {
            System.out.println("Could not schedule: doctor not found: " + did);
            return;
        }

        // patient
        System.out.print("Patient ID: ");
        String pid = scanner.nextLine().trim();
        if (patientRepo.findById(pid).isEmpty()) {
            System.out.println("Could not schedule: patient not found: " + pid);
            return;
        }

        // date (strict yyyy-MM-dd)
        System.out.print("Date (yyyy-MM-dd): ");
        LocalDate date;
        try {
            date = parseDateStrict(scanner.nextLine());
        } catch (Exception e) {
            System.out.println("Could not schedule: invalid date. Expected yyyy-MM-dd.");
            return;
        }

        // time (hour only, flexible input)
        System.out.print("Hour (0-23, accepts 9, 09, or 09:00): ");
        Integer hour = parseHourLenient(scanner.nextLine());
        if (hour == null) {
            System.out.println("Could not schedule: invalid hour. Enter 0..23 (e.g., 9 or 09).");
            return;
        }

        // build normalized 1-hour window
        LocalDateTime normStart = LocalDateTime.of(date, LocalTime.of(hour, 0));
        LocalDateTime normEnd   = normStart.plusHours(1);

        // priority
        System.out.print("Priority [0..3, default=1]: ");
        String pIn = scanner.nextLine().trim();
        int priority = 1;
        if (!pIn.isBlank()) {
            try { priority = Integer.parseInt(pIn); } catch (NumberFormatException ignore) {}
            if (priority < 0) priority = 0;
            if (priority > 3) priority = 3;
        }

        // confirm (date and time shown separately)
        System.out.println("Will schedule 1-hour slot:");
        System.out.println("  Doctor:  " + did);
        System.out.println("  Patient: " + pid);
        System.out.println("  Date:    " + DATE_FMT.format(normStart));
        System.out.println("  Time:    " + TIME_FMT.format(normStart) + " - " + TIME_FMT.format(normEnd));
        System.out.print("Proceed? [y/N]: ");
        String ok = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
        if (!ok.equals("y") && !ok.equals("yes")) {
            System.out.println("Canceled.");
            return;
        }

        // enqueue + process
        try {
            request(pid, did, normStart, normEnd, priority);
        } catch (IllegalArgumentException e) {
            System.out.println("Could not enqueue: " + e.getMessage());
            return;
        }

        Result res = processAll();

        // summary (date/time shown separately)
        System.out.println();
        System.out.println("=== Result ===");
        System.out.println("Scheduled: " + res.scheduled.size());
        for (SchedulingRequest r : res.scheduled) {
            System.out.println("  • " + r.doctorId + " with " + r.patientId
                    + " @ " + DATE_FMT.format(r.start)
                    + " " + TIME_FMT.format(r.start) + "-" + TIME_FMT.format(r.end)
                    + " (p=" + r.priority + ")");
        }
        System.out.println("Rejected:  " + res.rejected.size());
        for (SchedulingRequest r : res.rejected) {
            System.out.println("  • " + r.doctorId + " with " + r.patientId
                    + " @ " + DATE_FMT.format(r.start)
                    + " " + TIME_FMT.format(r.start) + "-" + TIME_FMT.format(r.end)
                    + " (conflict or invalid)");
        }
    }

    // -----------------------------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------------------------

    /** Normalize to the top of the hour (e.g., 09:23 -> 09:00). */
    private static LocalDateTime floorToHour(LocalDateTime dt) {
        return dt.truncatedTo(ChronoUnit.HOURS);
    }

    /** Strict yyyy-MM-dd date parser. */
    private static LocalDate parseDateStrict(String input) {
        final DateTimeFormatter D = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(input.trim(), D);
    }

    /**
     * Lenient hour parser:
     * - accepts "9", "09", "9:00", "09:00"
     * - returns 0..23 or null if invalid
     */
    private static Integer parseHourLenient(String input) {
        if (input == null) return null;
        String s = input.trim();
        if (s.isEmpty()) return null;

        // strip optional ":mm" (we ignore minutes for 1h slots)
        int colon = s.indexOf(':');
        if (colon >= 0) s = s.substring(0, colon);

        // now expect a number 0..23 (with or without leading zero)
        try {
            int h = Integer.parseInt(s);
            if (h < 0 || h > 23) return null;
            return h;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Overlap if newStart < existingEnd AND newEnd > existingStart. */
    private boolean overlaps(LocalDateTime s1, LocalDateTime e1,
                             LocalDateTime s2, LocalDateTime e2) {
        return s1.isBefore(e2) && e1.isAfter(s2);
    }

    /** Check doctor conflicts against persisted appointments. */
    private boolean canSchedule(String doctorId, LocalDateTime start, LocalDateTime end) {
        List<Appointment> existing = apptRepo.findByDoctor(doctorId);
        for (Appointment a : existing) {
            // skip canceled, but block SCHEDULED and COMPLETED windows
            if (a.getStatus() == Appointment.Status.CANCELED) continue;
            if (overlaps(start, end, a.getStart(), a.getEnd())) return false;
        }
        return true;
    }

    // -----------------------------------------------------------------------------------------
    // Result
    // -----------------------------------------------------------------------------------------

    /** Batch result with scheduled vs rejected for reporting or console printing. */
    public static final class Result {
        public final List<SchedulingRequest> scheduled;
        public final List<SchedulingRequest> rejected;

        public Result(List<SchedulingRequest> scheduled, List<SchedulingRequest> rejected) {
            this.scheduled = Collections.unmodifiableList(scheduled);
            this.rejected  = Collections.unmodifiableList(rejected);
        }
    }
}