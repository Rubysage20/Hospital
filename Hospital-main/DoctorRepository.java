import java.util.*;

/**
 * Storage-agnostic Doctor repository.
 * Implemented in-memory now; swap to Mongo later without changing callers.
 */
public interface DoctorRepository {
    Doctor save(Doctor d);
    Optional<Doctor> findById(String id);
    Optional<Doctor> findByLastName(String lastName);
    List<Doctor> findAll();
    void deleteById(String id);
}
