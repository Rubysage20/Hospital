import java.util.*;

/** Storage-agnostic Patient repository. */
public interface PatientRepository {
    Patient save(Patient p);
    Optional<Patient> findById(String id);
    Optional<Patient> findByLastName(String lastName);
    List<Patient> findAll();
    void deleteById(String id);
}
