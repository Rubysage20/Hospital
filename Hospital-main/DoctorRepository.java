import java.util.*;

/**
 * Storage-agnostic Doctor repository.
 * Implemented in-memory now; swap to Mongo later without changing callers.
 * adds fast last-name search
 */
public interface DoctorRepository {
    Doctor save(Doctor d);
    Optional<Doctor> findById(String id);
    Optional<Doctor> findByLastName(String lastName);
    List<Doctor> findAll();
    
    /** Fast path for exact last-name searches. */
    List<Doctor> findAllByLastName(String lastName);
    
    void deleteById(String id);
}
