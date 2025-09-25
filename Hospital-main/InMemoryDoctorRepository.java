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
    
    /**
     * Secondary index for fast lookup by last name.
     * key: LastName.toLowerCase(Locale.ROOT)
     * val: set of doctor uniqueIds with that Last name
     */
    private final Map<String, Set<String>> indexByLastName = new ConcurrentHashMap<>();
    /**
     * Upsert a doctor and maintain the secondary index.
     * Synchronized to keep store and index updates atomic per save.
     */
    @Override 
    public synchronized Doctor save(Doctor d) {
        Objects.requireNonNull(d, "doctor must not be null");
        
        // If updating and last name changed, remove from the old index bucket.
        Doctor prev = store.get(d.getUniqueId());
        if (prev != null && !prev.getLastName().equalsIgnoreCase(d.getLastName())) {
            String oldKey = prev.getLastName().toLowerCase(Locale.ROOT);
            Set<String> bucket = indexByLastName.get(oldKey);
            if (bucket != null) {
                bucket.remove(d.getUniqueId());
                if (bucket.isEmpty()) {
                    indexByLastName.remove(oldKey);
                }
            }
        }
        // Put/update in the main store.
        store.put(d.getUniqueId(), d);
        
        //Add/update in the secondary index.
        String key = d.getLastName().toLowerCase(Locale.ROOT);
        indexByLastName .computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()) .add(d.getUniqueId());
        
        return d;
    }

    @Override 
    public Optional<Doctor> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }
    /**
     * Fast exact match by last name (returns any one match as Optional).
     * Average complexity: O(1) to locate ID set + O(1) to map first ID to entity.
     */
    @Override 
    public Optional<Doctor> findByLastName(String lastName) {
        if (lastName == null) return Optional.empty();
        String key = lastName.toLowerCase(Locale.ROOT);
        Set<String> ids = indexByLastName.getOrDefault(key, Set.of());
        return ids.stream().findFirst().map(store::get);
    }
    /**
     * New: return all Doctors with an exact last-name match, sorted by first name.
     * Average complexity: O(1) to locate ID set + O(k) to materialize + O(k log k) to sort.
     */
    @Override
    public List<Doctor> findAllByLastName(String lastName) {
        if (lastName == null) return List.of();
        String key = lastName.toLowerCase(Locale.ROOT);
        Set<String> ids = indexByLastName.getOrDefault(key, Set.of());

        List<Doctor> result = new ArrayList<>(ids.size());
        for (String id : ids) {
            Doctor d = store.get(id);
            if (d != null) {
                result.add(d);
            }
        }
        result.sort(Comparator.comparing(Doctor::getFirstName));
        return result;
    }
    /**
     * Snapshot list of all doctors, sorted by last name then first name.
     */
    @Override 
    public List<Doctor> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(Doctor::getLastName).thenComparing(Doctor::getFirstName))
                .collect(Collectors.toList());
    }
    /**
     * Delete by id and update the secondary index accordingly.
     * Synchronized to keep store and index consistent per delete.
     */

    @Override
    public synchronized void deleteById(String id) {
        if (id == null) return;
        Doctor removed = store.remove(id);
        if (removed != null) {
            String key = removed.getLastName().toLowerCase(Locale.ROOT);
            Set<String> bucket = indexByLastName.get(key);
            if (bucket != null) {
                bucket.remove(id);
                if (bucket.isEmpty()) {
                    indexByLastName.remove(key);
                }
            }
        }
    }
}
