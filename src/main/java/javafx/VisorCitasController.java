package javafx;

import GestorDB.MongoDB;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import objects.Appoint;
import objects.Patient;
import objects.Specialty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.bson.Document;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class VisorCitasController {
    @FXML
    TextField appointNumTf;
    @FXML
    TextField dniTf;
    @FXML
    TextField nameTf;
    @FXML
    TextField addressTf;
    @FXML
    TextField telephoneTf;

    @FXML
    DatePicker datePicker;

    @FXML
    ChoiceBox<String> specialtyCb;

    @FXML
    TableView<Appoint> patientAppointsTv;

    @FXML
    TableColumn<Appoint, String> appointNumTc;
    @FXML
    TableColumn<Appoint, String> dateTc;
    @FXML
    TableColumn<Appoint, String> specialtyTc;

    @FXML
    Button seePatientAppointsBt;

    @FXML
    Button uploadAppointBt;
    @FXML
    Button deleteAppointBt;
    @FXML
    Button modifyAppointBt;
    @FXML
    Button newAppointBt;

    MongoDB mongoDB = MongoDB.getInstance();

    ObjectMapper JSON_MAPPER = new ObjectMapper();

    List<Specialty> specialties = getSpecialties();

    HashMap<String, Integer> specialtyIds = new HashMap<>();

    List<Appoint> appoints = new ArrayList<>();

    ObservableList<Appoint> appointsTable = FXCollections.observableList(appoints);

    @FXML
    private void initialize(){
        ObservableList<String> specialtyNames = FXCollections.observableList(new ArrayList<>());

        for (Specialty specialty : specialties){
            specialtyNames.add(specialty.especialidad.get());
            specialtyIds.put(specialty.especialidad.get(), specialty.id.get());
        }

        specialtyCb.setItems(specialtyNames);

        patientAppointsTv.setItems(appointsTable);
        appointNumTc.setCellValueFactory(data -> data.getValue().numCita.asString());
        specialtyTc.setCellValueFactory(data -> {
            for (Specialty specialty : specialties){
                if (specialty.id.get() == data.getValue().idEspecialidad.get()){
                    return specialty.especialidad;
                }
            }

            return new SimpleStringProperty("Especialidad no encontrada");
        });
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        dateTc.setCellValueFactory(data -> new SimpleStringProperty(dateFormat.format(data.getValue().fecha.get())));

    }
    @FXML
    private void seePatientAppoints(){
        String dni = dniTf.getText();
        appoints = getPatientAppoints(dni);
        appointsTable.setAll(appoints);
    }
    @FXML
    private void modifyAppoint(){

    }
    @FXML
    private void newAppoint(){
        int appointNum = getMaxAppointNum();

        appointNumTf.setText(String.valueOf(appointNum+1));
    }
    @FXML
    private void deleteAppoint(){
        int index = patientAppointsTv.getFocusModel().getFocusedItem().numCita.get();

        MongoCollection<Document> collection = MongoDB.getDatabase().getCollection("cita");

        mongoDB.remove(collection, Document.parse("{'num':'" + index + "'}"));

        seePatientAppoints();
    }
    @FXML
    private void uploadAppoint(){

        String dni = dniTf.getText();
        Patient patient = getPatient(dni);
        Specialty specialty = null;
        if (specialtyCb.getValue() != null){
            specialty = new Specialty(specialtyIds.get(specialtyCb.getValue()), specialtyCb.getValue());
        }

        int appointNum;
        try {
            appointNum = Integer.parseInt(appointNumTf.getText());
        }catch (Exception _){
            return;
        }

        if (patient != null && specialty != null && datePicker.getValue() != null){
            MongoCollection<Document> collection = MongoDB.getDatabase().getCollection("cita");
            System.out.println(datePicker.getValue());
            mongoDB.insertOne(collection, Document.parse("{'numCita' : '" + appointNum + "', 'idPaciente' : '" + patient.id + "', 'idEspecialidad' : '" + specialty.id.get() + "', 'fecha' : '" + datePicker.getValue() + "'}"));
        }
    }

    @FXML
    private void checkKey(KeyEvent event){
        if (event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.ENTER){
            setPatientData();
        }
    }

    private int getMaxAppointNum(){
        int appointNum = 0;

        MongoCollection<Document> collection = MongoDB.getDatabase().getCollection("cita");

        List<Document> docAppoints = mongoDB.find(collection, Document.parse("{}"));

        for (Document doc : docAppoints){
            if (Integer.parseInt(doc.get("numCita").toString()) > appointNum){
                appointNum = Integer.parseInt(doc.get("numCita").toString());
            }
        }

        return appointNum;
    }

    private void setPatientData(){
        String dni = dniTf.getText();

        Patient patient = getPatient(dni);

        nameTf.setText(patient.nombre);
        addressTf.setText(patient.direccion);
        telephoneTf.setText(patient.telefono);
    }

    private Patient getPatient(String dni){
        Patient patient;

        MongoCollection<Document> collection = MongoDB.getDatabase().getCollection("paciente");
        Document patientResult = mongoDB.find(collection, Document.parse("{'dni':'" + dni + "'}")).getFirst();

        try {
            patient = JSON_MAPPER.readValue(patientResult.toJson(), Patient.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return patient;
    }

    private List<Appoint> getPatientAppoints(String dni){
        List<Appoint> appointList;

        Patient patient = getPatient(dni);

        MongoCollection<Document> appoints = MongoDB.getDatabase().getCollection("cita");
        List<Document> docAppoints = mongoDB.find(appoints, Document.parse("{'idPaciente':'" + patient.id + "'}"));

        StringBuilder json = new StringBuilder();

        json.append("[");
        for (int i = 0; i < docAppoints.size(); i++) {
            json.append(docAppoints.get(i).toJson()).append(i < docAppoints.size() - 1 ? "," : "");
        }
        json.append("]");


        try {
            appointList = JSON_MAPPER.readValue(json.toString(), JSON_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, Appoint.class));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        return appointList;
    }

    private List<Specialty> getSpecialties(){
        List<Specialty> specialtyList = new ArrayList<>();

        try {
            File jsonFile = new File("src/main/resources/json/especialidades.json");
            specialtyList = JSON_MAPPER.readValue(jsonFile, JSON_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, Specialty.class));
        }catch (Exception _){}

        return specialtyList;
    }

}
