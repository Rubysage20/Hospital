import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory Patient repository (thread-safe).
 * Keeps API identical to what a future Mongo repository would expose.
 */
public class InMemoryPatientRepository implements PatientRepository {

    private final Map<String, Patient> store = new ConcurrentHashMap<>();

    @Override public Patient save(Patient p) {
        Objects.requireNonNull(p, "patient must not be null");
        store.put(p.getUniqueId(), p);
        return p;
    }

    @Override public Optional<Patient> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override public Optional<Patient> findByLastName(String lastName) {
        if (lastName == null) return Optional.empty();
        final String needle = lastName.trim();
        return store.values().stream()
                .filter(x -> x.getLastName().equalsIgnoreCase(needle))
                .findFirst();
    }

    @Override public List<Patient> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(Patient::getLastName).thenComparing(Patient::getFirstName))
                .collect(Collectors.toList());
    }

    @Override public void deleteById(String id) {
        if (id == null) return;
        store.remove(id);
    }
}
