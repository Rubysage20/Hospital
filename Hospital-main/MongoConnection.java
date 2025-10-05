import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public final class MongoConnection {
    private static volatile MongoClient client;
    private MongoConnection() {}
    public static MongoDatabase db(String dbName) {
        if (client == null) {
            synchronized (MongoConnection.class) {
                if (client == null) {
                    String uri = System.getenv().getOrDefault("MONGODB_URI", "mongodb://localhost:27017");
                    client = MongoClients.create(uri);
                }
            }
        }
        return client.getDatabase(dbName);
    }
}
