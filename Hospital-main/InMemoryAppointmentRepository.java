import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory Appointment repository (thread-safe).
 * Supports common queries you'll need for scheduling/conflict checks.
 */
public class InMemoryAppointmentRepository implements AppointmentRepository {

    /** Primary store keyed by appointment id. */
    private final Map<String, Appointment> store = new ConcurrentHashMap<>();

    @Override public Appointment save(Appointment a) {
        Objects.requireNonNull(a, "appointment must not be null");
        store.put(a.getId(), a);
        return a;
    }

    @Override public Optional<Appointment> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override public List<Appointment> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(Appointment::getStart))
                .collect(Collectors.toList());
    }

    @Override public List<Appointment> findByDoctor(String doctorId) {
        if (doctorId == null) return List.of();
        return store.values().stream()
                .filter(a -> doctorId.equals(a.getDoctor().getUniqueId()))
                .sorted(Comparator.comparing(Appointment::getStart))
                .collect(Collectors.toList());
    }

    @Override public List<Appointment> findByPatient(String patientId) {
        if (patientId == null) return List.of();
        return store.values().stream()
                .filter(a -> patientId.equals(a.getPatient().getUniqueId()))
                .sorted(Comparator.comparing(Appointment::getStart))
                .collect(Collectors.toList());
    }

    @Override public List<Appointment> findBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) return List.of();
        return store.values().stream()
                .filter(a -> !a.getStart().isAfter(to) && !a.getEnd().isBefore(from))
                .sorted(Comparator.comparing(Appointment::getStart))
                .collect(Collectors.toList());
    }

    @Override public void deleteById(String id) {
        if (id == null) return;
        store.remove(id);
    }
}
