import GestorDB.MongoDB;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import javafx.MainMenuApp;
import javafx.VisorCitasApp;
import org.bson.Document;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Launcher {

    public static void main(String[] args) {
        //Desactivar logs de mongoDB para evitar que se llene la consola
        Logger.getLogger("org.mongodb.driver").setLevel(Level.OFF);

        initDB();

        VisorCitasApp.launchApp();
    }

    public static void initDB(){
        try {
            MongoDB mongoDB = MongoDB.getInstance();
            ObjectMapper mapper = new ObjectMapper();

            MongoClient client = mongoDB.getClient();

            client.dropDatabase("practica");

            MongoCollection<Document> collection = MongoDB.getDatabase().getCollection("paciente");

            // 1️⃣ Leer JSON a lista de Map
            List<Map<String, Object>> listMap = mapper.readValue(
                    new File("src/main/resources/json/initPacientes.json"),
                    new TypeReference<List<Map<String,Object>>>() {}
            );

            // 2️⃣ Convertir cada Map a Document
            List<Document> documents = listMap.stream()
                    .map(Document::new)
                    .collect(Collectors.toList());

            mongoDB.insertMany(collection, documents);

            collection = MongoDB.getDatabase().getCollection("cita");

            // 1️⃣ Leer JSON a lista de Map
            listMap = mapper.readValue(
                    new File("src/main/resources/json/initCitas.json"),
                    new TypeReference<List<Map<String,Object>>>() {}
            );

            // 2️⃣ Convertir cada Map a Document
            documents = listMap.stream()
                    .map(Document::new)
                    .collect(Collectors.toList());

            mongoDB.insertMany(collection, documents);

            collection = MongoDB.getDatabase().getCollection("usuario");

            // 1️⃣ Leer JSON a lista de Map
            listMap = mapper.readValue(
                    new File("src/main/resources/json/initUsuarios.json"),
                    new TypeReference<List<Map<String,Object>>>() {}
            );

            // 2️⃣ Convertir cada Map a Document
            documents = listMap.stream()
                    .map(Document::new)
                    .collect(Collectors.toList());

            mongoDB.insertMany(collection, documents);

        }catch (Exception _){}
    }

}
