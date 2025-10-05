/**
 * Name: Valerie Dawson
 * Enhancement (CS-499, Software Design/Engineering):
 * - Encapsulation + validation
 * - PCP + last-visited as LAST-NAME strings (matches Clinic flow)
 * - markVisited() hook for Appointment integration later
 */
import java.time.LocalDateTime;
import java.util.Objects;

public class Patient extends Person {

    private String primaryCarePhysician; // PCP last name
    private String lastVisitedDoctor;    // last visited doctor last name
    private LocalDateTime lastVisitedAt; // when the last visit happened
    
    public Patient(String firstName, String lastName, String address, String pcpLastName) {
        super(firstName, lastName, address);  // UUID auto-generated
        this.primaryCarePhysician = ValidationUtils.requireNonBlank(pcpLastName, "primaryCarePhysician");
    }

    public Patient(String id, String firstName, String lastName, String address, String pcpLastName) {
        super(id, firstName, lastName, address);
        this.primaryCarePhysician = ValidationUtils.requireNonBlank(pcpLastName, "primaryCarePhysician");
    }

    // getters
    public String getPrimaryCarePhysician() { return primaryCarePhysician; }
    public String getLastVisitedDoctor() { return lastVisitedDoctor; }
    public LocalDateTime getLastVisitedAt() { return lastVisitedAt; }

    // setters
    public void setPrimaryCarePhysician(String pcpLastName) {
        this.primaryCarePhysician = ValidationUtils.requireNonBlank(pcpLastName, "primaryCarePhysician");
    }

    /** Clear last-visited fields (useful if data resets). */
    public void clearLastVisited() {
        this.lastVisitedDoctor = null;
        this.lastVisitedAt = null;
    }

    /** Called when a visit completes (e.g., Appointment.complete()). */
    public void markVisited(Doctor doctor, LocalDateTime when) {
        Objects.requireNonNull(doctor, "doctor must not be null");
        this.lastVisitedDoctor = ValidationUtils.requireNonBlank(doctor.getLastName(), "doctor.lastName");
        this.lastVisitedAt = ValidationUtils.requireNonNull(when, "visitedAt");
    }

    @Override public String toString() {
        return "Patient{" +
               "uniqueId='" + getUniqueId() + '\'' +
               ", name='" + getFirstName() + " " + getLastName() + '\'' +
               ", pcpLastName='" + primaryCarePhysician + '\'' +
               ", lastVisitedDoctor='" + lastVisitedDoctor + '\'' +
               ", lastVisitedAt=" + lastVisitedAt +
               '}';
    }

}
