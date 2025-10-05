/**
 * Name: Valerie Dawson
 *
 * Enhancement (CS-499, Software Design/Engineering):
 * - Adds an Appointment domain model with lifecycle and validation.
 * - Integrates with Doctor.visit() and Patient.markVisited(...) on completion.
 * - Provides strong invariants (start < end, non-null refs).
 */
import java.time.LocalDateTime;

public class Appointment {

    /** Lifecycle of the appointment. */
    public enum Status { SCHEDULED, COMPLETED, CANCELED }

    /** Immutable id (e.g., a UUID string). */
    private final String id;

    /** Participant references. */
    private final Patient patient;
    private final Doctor doctor;

    /** Appointment window (must satisfy start < end). */
    private LocalDateTime start;
    private LocalDateTime end;

    /** Current lifecycle status. */
    private Status status;

    public Appointment(String id, Patient patient, Doctor doctor, LocalDateTime start, LocalDateTime end) {
        this.id = ValidationUtils.requireNonBlank(id, "id");
        this.patient = ValidationUtils.requireNonNull(patient, "patient");
        this.doctor  = ValidationUtils.requireNonNull(doctor, "doctor");
        ValidationUtils.requireStartBeforeEnd(start, end);
        this.start = start;
        this.end = end;
        this.status = Status.SCHEDULED;
    }

    /** Mark completed, increment doctor visits, update patient's last-visit info. */
    public void complete() {
        if (status != Status.SCHEDULED) throw new IllegalStateException("Only SCHEDULED appointments can be completed");
        status = Status.COMPLETED;
        doctor.visit();
        patient.markVisited(doctor, LocalDateTime.now());
    }

    /** Cancel a scheduled appointment. */
    public void cancel() {
        if (status != Status.SCHEDULED) throw new IllegalStateException("Only SCHEDULED appointments can be canceled");
        status = Status.CANCELED;
    }

    /** Reschedule within valid time window. */
    public void reschedule(LocalDateTime newStart, LocalDateTime newEnd) {
        if (status != Status.SCHEDULED) throw new IllegalStateException("Only SCHEDULED appointments can be rescheduled");
        ValidationUtils.requireStartBeforeEnd(newStart, newEnd);
        this.start = newStart;
        this.end = newEnd;
    }

    // Getters
    public String getId() { return id; }
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
    public Status getStatus() { return status; }

    @Override public String toString() {
        return "Appointment{" +
               "id='" + id + '\'' +
               ", patient=" + patient.getLastName() +
               ", doctor=" + doctor.getLastName() +
               ", start=" + start +
               ", end=" + end +
               ", status=" + status +
               '}';
    }
}
