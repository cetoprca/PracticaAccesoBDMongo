package app.javafx;

import app.GestorDB.MongoDB;
import app.Launcher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.objects.Appoint;
import app.objects.Patient;
import app.objects.Specialty;
import com.mongodb.client.MongoCollection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;


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
    Button deleteAppointBt;
    @FXML
    Button modifyAppointBt;
    @FXML
    Button newAppointBt;

    ObjectMapper JSON_MAPPER = new ObjectMapper();

    MongoDB mongoDB = MongoDB.getInstance();

    List<Specialty> specialties = new ArrayList<>();

    List<String> specialtyNames = new ArrayList<>();

    Map<String, Integer> specialtyNameToID = new HashMap<>();
    Map<Integer, String> specialtyIdToName = new HashMap<>();

    ObservableList<Appoint> appointList = FXCollections.observableList(new ArrayList<>());

    @FXML
    private void initialize(){
        loadUserData(Launcher.usuarioLoggeado.dni);

        loadSpecialties();

        specialtyCb.setItems(FXCollections.observableList(specialtyNames));

        patientAppointsTv.setItems(appointList);

        appointNumTc.setCellValueFactory(data -> data.getValue().numCita.asString());
        dateTc.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().fecha.get()
                        .toInstant()
                        .atZone(
                                ZoneId.systemDefault()
                        )
                        .toLocalDate().format(
                                DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        )
                )
        );
        specialtyTc.setCellValueFactory(data -> new SimpleStringProperty(
                specialtyIdToName.get(
                        data.getValue().idEspecialidad.get()
                ))
        );
    }
    @FXML
    private void seePatientAppoints(){
        updateAppointList();
    }
    @FXML
    private void modifyAppoint(){
        int numCita = patientAppointsTv.getFocusModel().getFocusedItem().numCita.get();

        deleteAppoint(numCita);
        newAppoint(numCita);

        seePatientAppoints();
    }
    @FXML
    private void newAppoint(){
        int numCita;
        if (appointNumTf.getText().isEmpty() || appointNumTf.getText().isBlank()){
            numCita = getMaxNumCita() + 1;
        }else{
            try {
                numCita = Integer.parseInt(appointNumTf.getText());
            } catch (NumberFormatException e) {
                throw new RuntimeException(e);
            }
        }

        newAppoint(numCita);

        seePatientAppoints();
    }
    @FXML
    private void deleteAppoint(){
        int numCita = patientAppointsTv.getFocusModel().getFocusedItem().numCita.get();

        deleteAppoint(numCita);

        updateAppointList();
    }
    @FXML
    private void checkKey(KeyEvent event){
        if (event.getCode().equals(KeyCode.ENTER) || event.getCode().equals(KeyCode.TAB)){
            loadUserData(dniTf.getText());
        }
    }

    @FXML
    private void tableViewClicked(){
        MongoDB mongodb = MongoDB.getInstance();
        MongoCollection<Document> collection = MongoDB.getDatabase().getCollection("cita");

        int numCita = patientAppointsTv.getFocusModel().getFocusedItem().numCita.get();

        String json = mongodb.find(collection, Document.parse("{'numCita' : '" + numCita + "'}")).getFirst().toJson();
        Appoint selectedAppoint;
        try {
            selectedAppoint = JSON_MAPPER.readValue(json, Appoint.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        appointNumTf.setText("" + selectedAppoint.numCita.get());
        specialtyCb.getSelectionModel().select(specialtyIdToName.get(selectedAppoint.idEspecialidad.get()));
        datePicker.setValue(selectedAppoint.fecha.get().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    private void loadUserData(String dni){
        dniTf.setText(dni);


        String json = mongoDB.find(MongoDB.getDatabase().getCollection("paciente"), Document.parse("{'dni':'" + dni + "'}")).getFirst().toJson();

        Patient patient;

        try {
            patient = JSON_MAPPER.readValue(json, JSON_MAPPER.constructType(Patient.class));
        } catch (JsonProcessingException e) {
            System.out.println("ERROR: Ha ocurrido un error parseando una cita de json a clase Appoint");
            throw new RuntimeException(e);
        }

        addressTf.setText(patient.direccion);
        telephoneTf.setText(patient.telefono);
        nameTf.setText(patient.nombre);
    }

    private void loadSpecialties(){
        File specialtyJson = new File("src/main/resources/json/especialidades.json");
        try {
            specialties = JSON_MAPPER.readValue(specialtyJson, JSON_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, Specialty.class));
        } catch (IOException _) {}

        getSpecialtyNames();
    }

    private void getSpecialtyNames(){
        for (Specialty specialty : specialties){
            specialtyNames.add(specialty.especialidad.get());
            specialtyNameToID.put(specialty.especialidad.get(), specialty.id.get());
            specialtyIdToName.put(specialty.id.get(), specialty.especialidad.get());
        }
    }

    private int getMaxNumCita(){
        int maxNumCita = -1;

        List<Document> appoints = mongoDB.find(MongoDB.getDatabase().getCollection("cita"), Document.parse("{}"));
        for (Document appoint : appoints){
            int numCita = Integer.parseInt(appoint.getString("numCita"));
            if (numCita > maxNumCita){
                maxNumCita = numCita;
            }
        }

        return maxNumCita;
    }

    private Patient getPatient(String dni){
        try {
            MongoCollection<Document> collection = MongoDB.getDatabase().getCollection("paciente");
            String json = mongoDB.find(collection, Document.parse("{'dni':'" + dni + "'}")).getFirst().toJson();
            return JSON_MAPPER.readValue(json, JSON_MAPPER.constructType(Patient.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateAppointList(){
        appointList.clear();

        Patient patient = getPatient(dniTf.getText());

        List<Document> patientAppoints = mongoDB.find(MongoDB.getDatabase().getCollection("cita") , Document.parse("{'idPaciente':'" + patient.id + "'}"));
        for (Document appoint : patientAppoints){
            try {
                appointList.add(JSON_MAPPER.readValue(appoint.toJson(), JSON_MAPPER.constructType(Appoint.class)));
            }catch (JsonProcessingException e){
                System.out.println("ERROR: Ha ocurrido un error parseando una cita de json a clase Appoint");
                throw new RuntimeException(e);
            }
        }
    }

    private void deleteAppoint(int numCita){
        MongoCollection<Document> collection = MongoDB.getDatabase().getCollection("cita");

        mongoDB.remove(collection, Document.parse("{'numCita':'" + numCita + "'}"));
    }

    private void newAppoint(int numCita){
        String json = mongoDB.find(MongoDB.getDatabase().getCollection("paciente"), Document.parse("{'dni':'" + dniTf.getText() + "'}")).getFirst().toJson();

        Patient patient;
        try {
            patient = JSON_MAPPER.readValue(json, JSON_MAPPER.constructType(Patient.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        int idEspecialidad = specialtyNameToID.get(specialtyCb.getValue());
        LocalDate fecha = datePicker.getValue();

        mongoDB.insertOne(MongoDB.getDatabase().getCollection("cita"), Document.parse("{'numCita':'" + numCita + "','idPaciente':'" + patient.id + "','idEspecialidad':'" + idEspecialidad + "','fecha':'" + fecha + "'}"));

    }
}
