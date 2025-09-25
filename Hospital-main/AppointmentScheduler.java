import java.time.LocalDateTime;
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
     */
    public static final class SchedulingRequest {
        public final String patientId;
        public final String doctorId;
        public final LocalDateTime start;
        public final LocalDateTime end;
        public final int priority; // 0=LOW, 1=NORMAL, 2=HIGH, 3=CRITICAL

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
     * @param priority 0=LOW, 1=NORMAL, 2=HIGH, 3=CRITICAL
     */
    public void request(String patientId,
                        String doctorId,
                        LocalDateTime start,
                        LocalDateTime end,
                        int priority) {
        queue.add(new SchedulingRequest(patientId, doctorId, start, end, priority));
    }

    /**
     * Convenience overload: default priority NORMAL (1).
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

            var doctor  = doctorRepo.findById(req.doctorId).orElse(null);
            var patient = patientRepo.findById(req.patientId).orElse(null);
            if (doctor == null || patient == null) {
                rejected.add(req);
                continue;
            }

            // Persist the appointment using your existing constructor (no urgency required).
            Appointment appt = new Appointment(
                    UUID.randomUUID().toString(),
                    patient,
                    doctor,
                    req.start,
                    req.end
            );
            apptRepo.save(appt);
            scheduled.add(req);
        }

        return new Result(scheduled, rejected);
    }

    // -----------------------------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------------------------

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

