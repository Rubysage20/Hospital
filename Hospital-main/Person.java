/**
 * Name: Valerie Dawson
 * Enhancement (CS-499, Software Design/Engineering):
 * - Encapsulation with validation
 * - Immutable uniqueId (auto-generated if omitted)
 * - equals/hashCode by uniqueId; helpful toString
 */
import java.util.Objects;
import java.util.UUID;

public abstract class Person {

    /** Immutable identity key. */
    private final String uniqueId;

    /** Core identity fields. */
    private String firstName;
    private String lastName;
    private String address;

    /**
     * Canonical constructor. If uniqueId is null/blank, generates a UUID.
     */
    protected Person(String uniqueId, String firstName, String lastName, String address) {
        this.uniqueId = (uniqueId == null || uniqueId.isBlank())
                ? UUID.randomUUID().toString()
                : ValidationUtils.requireNonBlank(uniqueId, "uniqueId");
        this.firstName = ValidationUtils.requireNonBlank(firstName, "firstName");
        this.lastName  = ValidationUtils.requireNonBlank(lastName,  "lastName");
        this.address   = ValidationUtils.requireNonBlank(address,   "address");
    }

    /** Convenience constructor that always generates a uniqueId. */
    protected Person(String firstName, String lastName, String address) {
        this(UUID.randomUUID().toString(), firstName, lastName, address);
    }

    // Getters (uniqueId is read-only)
    public String getUniqueId() { return uniqueId; }
    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName; }
    public String getAddress()   { return address; }

    // Validated setters
    public void setFirstName(String firstName) { this.firstName = ValidationUtils.requireNonBlank(firstName, "firstName"); }
    public void setLastName(String lastName)   { this.lastName  = ValidationUtils.requireNonBlank(lastName,  "lastName"); }
    public void setAddress(String address)     { this.address   = ValidationUtils.requireNonBlank(address,   "address"); }

    // Identity and debug printing
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        return uniqueId.equals(((Person) o).uniqueId);
    }
    @Override public int hashCode() { return Objects.hash(uniqueId); }

    @Override public String toString() {
        return getClass().getSimpleName() + "{" +
               "uniqueId='" + uniqueId + '\'' +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", address='" + address + '\'' +
               '}';
    }
}
