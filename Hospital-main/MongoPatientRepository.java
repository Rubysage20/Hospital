import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;

import java.util.*;

public class MongoPatientRepository implements PatientRepository {
    private final MongoCollection<Document> col;

    public MongoPatientRepository(String dbName) {
        MongoDatabase db = MongoConnection.db(dbName);
        this.col = db.getCollection("patients");
        col.createIndex(Indexes.ascending("id"), new IndexOptions().unique(true));
        col.createIndex(Indexes.ascending("lastName"));
    }

    @Override
    public Patient save(Patient p) {
        Document doc = new Document()
                .append("id", p.getUniqueId())
                .append("firstName", p.getFirstName())
                .append("lastName", p.getLastName())
                .append("address", p.getAddress())
                .append("primaryCarePhysician", p.getPrimaryCarePhysician())
                .append("lastVisitedDoctor", p.getLastVisitedDoctor())
                .append("lastVisitedAt", p.getLastVisitedAt() == null ? null : p.getLastVisitedAt().toString());
        col.updateOne(Filters.eq("id", p.getUniqueId()), new Document("$set", doc), new UpdateOptions().upsert(true));
        return p;
    }

    @Override
    public Optional<Patient> findById(String id) {
        Document d = col.find(Filters.eq("id", id)).first();
        return Optional.ofNullable(fromDoc(d));
    }

    @Override
    public Optional<Patient> findByLastName(String lastName) {
        Document d = col.find(Filters.eq("lastName", lastName))
                        .sort(Sorts.ascending("firstName"))
                        .first();
        return Optional.ofNullable(fromDoc(d));
    }

    @Override
    public List<Patient> findAll() {
        List<Patient> out = new ArrayList<>();
        for (Document d : col.find().sort(Sorts.ascending("lastName", "firstName"))) out.add(fromDoc(d));
        return out;
    }

    @Override
    public List<Patient> findAllByLastName(String lastName) {
        List<Patient> out = new ArrayList<>();
        for (Document d : col.find(Filters.eq("lastName", lastName)).sort(Sorts.ascending("firstName")))
            out.add(fromDoc(d));
        return out;
    }

    @Override
    public void deleteById(String id) {
        col.deleteOne(Filters.eq("id", id));
    }

    private Patient fromDoc(org.bson.Document d) {
        if (d == null) return null;
        return new Patient(
            d.getString("id"),
            d.getString("firstName"),
            d.getString("lastName"),
            d.getString("address"),
            d.getString("primaryCarePhysician")
        );
    }
}

   
