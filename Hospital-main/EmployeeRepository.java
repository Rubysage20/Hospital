import java.util.*;

/** Storage-agnostic Employee repository. */
public interface EmployeeRepository {
    Employee save(Employee e);
    Optional<Employee> findById(String id);
    List<Employee> findAll();
    void deleteById(String id);
}
