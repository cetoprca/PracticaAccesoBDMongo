package app;

import app.GestorDB.MongoDB;
import app.javafx.MainMenuApp;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import app.objects.Patient;
import org.bson.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Launcher {

    public static Patient usuarioLoggeado = null;

    public static void main(String[] args) {
        //Desactivar logs de mongoDB para evitar que se llene la consola
        Logger.getLogger("org.mongodb.driver").setLevel(Level.OFF);

        Scanner scanner = new Scanner(System.in);

        System.out.println("¿Quiere inicializar la base de datos a los valores de prueba por defecto? S/n");
        if (scanner.nextLine().equalsIgnoreCase("s")){
            initDB();
        }

        initDB();

        MainMenuApp.launchApp();
    }

    public static void initDB(){

        dropDatabae("practica");

        MongoCollection<Document> patients = MongoDB.getDatabase().getCollection("paciente");
        MongoCollection<Document> appoints = MongoDB.getDatabase().getCollection("cita");

        initFile(patients, "initPacientes");
        initFile(appoints, "initCitas");

    }

    private static void dropDatabae(String databaseName){
        MongoDB mongoDB = MongoDB.getInstance();
        MongoClient client = mongoDB.getClient();
        client.dropDatabase(databaseName);
    }

    private static void initFile(MongoCollection<Document> collection, String filename){
        MongoDB mongoDB = MongoDB.getInstance();

        List<String> lineas;
        try {
            lineas = Files.readAllLines(Path.of("src/main/resources/json/" + filename + ".txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Document> instructions = new ArrayList<>();

        for (String linea : lineas){
            instructions.add(Document.parse(linea));
        }

        mongoDB.insertMany(collection, instructions);
    }

}
