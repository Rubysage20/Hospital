import java.time.LocalDateTime;
import java.util.*;

/**
 * Storage-agnostic Appointment repository.
 * Useful for scheduling; keeps callers independent from storage choice.
 */
public interface AppointmentRepository {
    Appointment save(Appointment a);
    Optional<Appointment> findById(String id);
    List<Appointment> findAll();
    List<Appointment> findByDoctor(String doctorId);
    List<Appointment> findByPatient(String patientId);
    List<Appointment> findBetween(LocalDateTime from, LocalDateTime to);
    void deleteById(String id);
}
