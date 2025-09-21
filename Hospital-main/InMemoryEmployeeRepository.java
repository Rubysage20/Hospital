import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory Employee repository (thread-safe).
 * A drop-in stand-in for a future Mongo repo.
 */
public class InMemoryEmployeeRepository implements EmployeeRepository {

    private final Map<String, Employee> store = new ConcurrentHashMap<>();

    @Override public Employee save(Employee e) {
        Objects.requireNonNull(e, "employee must not be null");
        store.put(e.getUniqueId(), e);
        return e;
    }

    @Override public Optional<Employee> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override public List<Employee> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(Employee::getLastName).thenComparing(Employee::getFirstName))
                .collect(Collectors.toList());
    }

    @Override public void deleteById(String id) {
        if (id == null) return;
        store.remove(id);
    }
}
