import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoTest {
    public static void main(String[] args) {
        try (var client = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase db = client.getDatabase("gms_db");
            System.out.println("✅ Connected to: " + db.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

