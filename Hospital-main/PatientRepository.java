import java.util.*;

/** Storage-agnostic Patient repository.
 * Adds fast last-name search.
  */
public interface PatientRepository {
    Patient save(Patient p);
    Optional<Patient> findById(String id);
    Optional<Patient> findByLastName(String lastName);
    List<Patient> findAll();
    
    /** Fast path for exact last-name searches. */
    List<Patient> findAllByLastName(String lastName);
    
    void deleteById(String id);
}
