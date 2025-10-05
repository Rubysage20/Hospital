/**
 * Name: Valerie Dawson
 *
 * Enhancement (CS-499, Software Design/Engineering):
 * Centralized validation helpers to improve maintainability and prevent invalid state.
 */
import java.time.LocalDateTime;
import java.util.Objects;

public final class ValidationUtils {
    private ValidationUtils() {}

    /** Ensure a string is not null/blank; returns the trimmed value. */
    public static String requireNonBlank(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }

    /** Ensure an object ref is not null; returns the same ref. */
    public static <T> T requireNonNull(T value, String field) {
        return Objects.requireNonNull(value, field + " must not be null");
    }

    /** Ensure a double is >= 0. */
    public static double requireNonNegative(double value, String field) {
        if (value < 0.0) throw new IllegalArgumentException(field + " must be >= 0");
        return value;
    }

    /** Ensure an int is >= 0. */
    public static int requireNonNegative(int value, String field) {
        if (value < 0) throw new IllegalArgumentException(field + " must be >= 0");
        return value;
    }

    /** Ensure start is strictly before end. */
    public static void requireStartBeforeEnd(LocalDateTime start, LocalDateTime end) {
        requireNonNull(start, "start");
        requireNonNull(end, "end");
        if (!start.isBefore(end)) throw new IllegalArgumentException("start must be before end");
    }

    /** Ensure time is not in the past (useful for scheduling). */
    public static LocalDateTime requireNotInPast(LocalDateTime time, String field) {
        requireNonNull(time, field);
        if (time.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(field + " must not be in the past");
        }
        return time;
    }
}

