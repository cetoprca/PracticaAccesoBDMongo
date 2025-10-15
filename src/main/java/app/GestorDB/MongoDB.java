package app.GestorDB;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import org.bson.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MongoDB implements DataBase<List<Document>, Document, MongoCollection<Document>>{

    private static MongoDB instance;
    private final MongoClient client;
    private static MongoDatabase database;

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

        client = new MongoClient(new MongoClientURI("mongodb://" + user + ":" + pwd + "@" + host + ":" + port + "/?authSource=admin"));
        database = client.getDatabase(db);
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

    public static MongoDatabase getDatabase(){
        return database;
    }

    @Override
    public boolean insertOne(MongoCollection<Document> collection, Document data) {
        try {
            collection.insertOne(data);
            return true;
        }catch (Exception _){
            return false;
        }
    }

    @Override
    public boolean insertMany(MongoCollection<Document> collection, List<Document> data) {
        try {
            collection.insertMany(data);
            return true;
        }catch (Exception _){
            return false;
        }
    }

    @Override
    public boolean remove(MongoCollection<Document> collection, Document data) {
        try {
            collection.deleteMany(data);
            return true;
        }catch (Exception _){
            return false;
        }
    }

    @Override
    public boolean modify(MongoCollection<Document> location, Document query, Document data) {
        try {
            remove(location, query);
            insertOne(location, data);
        }catch (Exception _){
            return false;
        }
        return true;
    }

    @Override
    public List<Document> find(MongoCollection<Document> collection, Document query) {
        FindIterable<Document> queryResult = collection.find(query).projection(Projections.exclude("_id"));

        List<Document> documents = new ArrayList<>();

        for (Document doc : queryResult){
            documents.add(doc);
        }

        return documents;
    }
}
