package GestorDB;

import com.mongodb.MongoClient;
import org.bson.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class MongoDB implements DataBase<Document, Document>{

    private static MongoDB instance;
    private final MongoClient client;

    private MongoDB(){
        Properties properties = new Properties();

        try {
            properties.load(Files.newInputStream(Path.of("src/main/resources/properties/database.properties")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String host = properties.getProperty("mongo.host");
        int port = Integer.parseInt(properties.getProperty("mongo.port"));
        String user = properties.getProperty("mongo.user");
        String pwd = properties.getProperty("mongo.pwd");
        String db = properties.getProperty("mongo.db");

        client = new MongoClient("mongodb://" + user + ":" + pwd + "@" + host + ":" + port + "/" + db + "?authSource=admin");
    }

    public MongoClient getClient() {
        return client;
    }

    public static MongoDB getInstance(){
        if (instance != null){
            return instance;
        }
        instance = new MongoDB();
        return getInstance();
    }

    @Override
    public boolean insertOne(Document data) {
        return false;
    }

    @Override
    public boolean insertMany(Document data) {
        return false;
    }

    @Override
    public boolean removeOne(Document data) {
        return false;
    }

    @Override
    public boolean removeMany(Document data) {
        return false;
    }

    @Override
    public Document find(Document query) {
        return null;
    }
}
