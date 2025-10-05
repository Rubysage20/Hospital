import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;

import java.util.*;

/**
 * MongoEmployeeRepository
 * -----------------------
 * MongoDB-backed implementation of EmployeeRepository.
 * Persists Employee data in the "employees" collection.
 *
 * Notes:
 * - Numeric reads use Number.doubleValue()/intValue() to be tolerant of Mongo’s numeric types.
 * - Indexes on id (unique) and lastName for common lookups.
 */
public class MongoEmployeeRepository implements EmployeeRepository {
    private final MongoCollection<Document> col; // "employees" collection handle

    /**
     * Open a connection to the "employees" collection and ensure indexes.
     * @param dbName database name (e.g., "gms_db")
     */
    public MongoEmployeeRepository(String dbName) {
        MongoDatabase db = MongoConnection.db(dbName);
        this.col = db.getCollection("employees");
        col.createIndex(Indexes.ascending("id"), new IndexOptions().unique(true));
        col.createIndex(Indexes.ascending("lastName"));
    }

    /**
     * Upsert (save or update) an Employee by id.
     * @param e employee to persist
     * @return same employee (for chaining)
     */
    @Override
    public Employee save(Employee e) {
        // Domain → BSON
        Document doc = new Document()
                .append("id", e.getUniqueId())
                .append("firstName", e.getFirstName())
                .append("lastName", e.getLastName())
                .append("address", e.getAddress())
                .append("monthlyWorkingHours", e.getMonthlyWorkingHours())
                .append("hourlyRate", e.getBaseSalary());

        // Upsert by id
        col.updateOne(Filters.eq("id", e.getUniqueId()),
                new Document("$set", doc),
                new UpdateOptions().upsert(true));
        return e;
    }

    /**
     * Find an employee by unique id.
     */
    @Override
    public Optional<Employee> findById(String id) {
        Document d = col.find(Filters.eq("id", id)).first();
        return Optional.ofNullable(fromDoc(d));
    }

    /**
     * Find first employee by last name (sorted by firstName).
     */
   
    public Optional<Employee> findByLastName(String lastName) {
        Document d = col.find(Filters.eq("lastName", lastName))
                        .sort(Sorts.ascending("firstName"))
                        .first();
        return Optional.ofNullable(fromDoc(d));
    }

    /**
     * Return all employees sorted by lastName, then firstName.
     */
    @Override
    public List<Employee> findAll() {
        List<Employee> out = new ArrayList<>();
        for (Document d : col.find().sort(Sorts.ascending("lastName", "firstName"))) {
            Employee e = fromDoc(d);
            if (e != null) out.add(e);
        }
        return out;
    }

    /**
     * Return all employees with a matching last name, sorted by firstName.
     */
   
    public List<Employee> findAllByLastName(String lastName) {
        List<Employee> out = new ArrayList<>();
        for (Document d : col.find(Filters.eq("lastName", lastName)).sort(Sorts.ascending("firstName"))) {
            Employee e = fromDoc(d);
            if (e != null) out.add(e);
        }
        return out;
    }

    /**
     * Delete an employee by id.
     */
    @Override
    public void deleteById(String id) {
        col.deleteOne(Filters.eq("id", id));
    }

    // ---------------------------------------------------------------------
    // Mapping helpers (numeric-safe)
    // ---------------------------------------------------------------------

    /**
     * Read a numeric field as double, accepting Int32/Int64/Double/String.
     */
    private static double readDouble(Document d, String key, double defVal) {
        Object v = d.get(key);
        if (v instanceof Number n) return n.doubleValue();
        if (v instanceof String s) {
            try { return Double.parseDouble(s); } catch (NumberFormatException ignore) {}
        }
        return defVal;
    }

    /**
     * Read a numeric field as int, accepting Int32/Int64/Double/String.
     */
    private static int readInt(Document d, String key, int defVal) {
        Object v = d.get(key);
        if (v instanceof Number n) return n.intValue();
        if (v instanceof String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException ignore) {}
        }
        return defVal;
    }

    /**
     * Convert BSON Document → Employee domain object.
     */
    private Employee fromDoc(Document d) {
        if (d == null) return null;

        int monthlyHours = readInt(d, "monthlyWorkingHours", 0);
        double hourly    = readDouble(d, "hourlyRate", 0.0);

        return new Employee(
                d.getString("id"),
                d.getString("firstName"),
                d.getString("lastName"),
                d.getString("address"),
                monthlyHours,
                hourly
        );
        // Additional setters would go here if your Employee had more mutable fields
    }
}

