import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory Patient repository (thread-safe).
 * Keeps API identical to what a future Mongo repository would expose.
 * Milestone II: adds a secondary index for Last-name -> set of patient IDs.
 * Average case lookups  because O(1) to fetch ID set + O(k) to materialize entities. 
 */
public class InMemoryPatientRepository implements PatientRepository {
	
	/** Primary store keyed by immutable uniqueId. */
    private final Map<String, Patient> store = new ConcurrentHashMap<>();
    
    /** 
     * Secondary index for fast Lookup by Last name.
     * key: LastName. toLowerCase(Locale.ROOT)
     * val: set of patient uniqueIds with that last name
     */
    private final Map<String, Set<String>> indexByLastName = new ConcurrentHashMap<>();
    /**
     * Upsert a patient and maintain the secondary index.
     * Sychronized to keep store and index updates atomic per save.
     */
    @Override 
    public synchronized Patient save(Patient p) {
        Objects.requireNonNull(p, "patient must not be null");
        
        // Remove from old index bucket if this is an update and last name changed
        Patient prev = store.get(p.getUniqueId());
        if (prev != null && !prev.getLastName().equalsIgnoreCase(p.getLastName())) {
        	String oldKey = prev.getLastName().toLowerCase(Locale.ROOT);
        	Set <String> bucket = indexByLastName.get(oldKey);
        	if (bucket != null) {
        		bucket.remove(p.getUniqueId());
        		if (bucket.isEmpty()) {
        			indexByLastName.remove(oldKey);
        	}
        }
    }
        // Put/update in main store 
        store.put(p.getUniqueId(), p);
        
        // Add/update in the secondary index
        String key = p.getLastName().toLowerCase(Locale.ROOT);
        indexByLastName.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(p.getUniqueId());
        return p;
    }

    @Override 
    public Optional<Patient> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }
    /**
     * Fast exact match by Last name(return any one match as Optional).
     * Average complexity: O(1) to Locate ID set + O(1) to map first ID to entity.
     */

    @Override 
    public Optional<Patient> findByLastName(String lastName) {
        if (lastName == null) return Optional.empty();
        String key = lastName.toLowerCase(Locale.ROOT);
        Set<String> ids = indexByLastName.getOrDefault(key, Set.of());
        return ids.stream().findFirst().map(store::get);
       
    }
    /**
     * Return all Patients with an exact last-name match, sorted by first name.
     * Average complexity: O(1) to Locate ID set + O(k) to materialize + O(k log k) to sort. 
     */
    		
    @Override 
    public List<Patient> findAllByLastName(String lastName) {
        if (lastName == null) return List.of();
        String key = lastName.toLowerCase(Locale.ROOT);
        Set<String> ids = indexByLastName.getOrDefault(key, Set.of());

        List<Patient> result = new ArrayList<>(ids.size());
        for (String id : ids) {
            Patient p = store.get(id);
            if (p != null) {
                result.add(p);
            }
        }
        result.sort(Comparator.comparing(Patient::getFirstName));
        return result;
    }
    /**
     * Snapshot list of all patients, sorted by last name then first name.
     */
    @Override 
    public List<Patient> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(Patient::getLastName).thenComparing(Patient::getFirstName))
                .collect(Collectors.toList());
    }
    /**
     * Delete by id and update the secondary index accordingly.
     * Synchronized to keep store and index consistent per delete.
     */

    @Override
    public synchronized void deleteById(String id) {
        if (id == null) return;
        Patient removed = store.remove(id);
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
