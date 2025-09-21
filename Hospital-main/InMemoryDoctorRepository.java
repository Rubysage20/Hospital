import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory Doctor repository (thread-safe).
 * Swap this with a Mongo-backed implementation later without changing callers.
 */
public class InMemoryDoctorRepository implements DoctorRepository {

    /** Primary store keyed by uniqueId. */
    private final Map<String, Doctor> store = new ConcurrentHashMap<>();

    @Override public Doctor save(Doctor d) {
        Objects.requireNonNull(d, "doctor must not be null");
        store.put(d.getUniqueId(), d);
        return d;
    }

    @Override public Optional<Doctor> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override public Optional<Doctor> findByLastName(String lastName) {
        if (lastName == null) return Optional.empty();
        final String needle = lastName.trim();
        return store.values().stream()
                .filter(x -> x.getLastName().equalsIgnoreCase(needle))
                .findFirst();
    }

    @Override public List<Doctor> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(Doctor::getLastName).thenComparing(Doctor::getFirstName))
                .collect(Collectors.toList());
    }

    @Override public void deleteById(String id) {
        if (id == null) return;
        store.remove(id);
    }
}
