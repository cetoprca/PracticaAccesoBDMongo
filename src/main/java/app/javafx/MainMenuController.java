package app.javafx;

import app.GestorDB.MongoDB;
import app.Launcher;
import app.objects.Patient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.bson.Document;

import java.util.List;

public class MainMenuController {
    @FXML
    TextField emailTf;
    @FXML
    TextField passwordTf;
    @FXML
    PasswordField passwordPf;

    @FXML
    Button logInButt;

    @FXML
    RadioButton showPasswordRbut;

    @FXML
    private void initialize(){
        passwordTf.setVisible(false);
        passwordTf.setManaged(false);

        passwordTf.textProperty().bindBidirectional(passwordPf.textProperty());
    }

    @FXML
    private void logIn(){
        try {
            String email = emailTf.getText();
            String password = passwordPf.getText();
            boolean correct;

            MongoDB mongoDB = MongoDB.getInstance();
            MongoCollection<Document> usuarios = MongoDB.getDatabase().getCollection("paciente");
            List<Document> users = mongoDB.find(usuarios, Document.parse("{'email':'" + email + "', 'password':'" + password + "'}"));
            String json = mongoDB.find(usuarios, Document.parse("{'email':'" + email + "', 'password':'" + password + "'}")).getFirst().toJson();

            ObjectMapper JSON_MAPPER = new ObjectMapper();

            correct = !users.isEmpty();

            if (correct){
                Stage stage = ((Stage) passwordPf.getScene().getWindow());
                stage.close();

                VisorCitasApp visorCitasApp = new VisorCitasApp();

                try {
                    Launcher.usuarioLoggeado = JSON_MAPPER.readValue(json, JSON_MAPPER.constructType(Patient.class));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                try {
                    visorCitasApp.start(new Stage());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Something went wrong");
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void showPassword(){
        if (showPasswordRbut.isSelected()){
            passwordPf.setManaged(false);
            passwordPf.setVisible(false);
            passwordTf.setManaged(true);
            passwordTf.setVisible(true);

        }else {
            passwordPf.setManaged(true);
            passwordPf.setVisible(true);
            passwordTf.setManaged(false);
            passwordTf.setVisible(false);
        }
    }

    @FXML
    private void checkKey(KeyEvent event){
        if (event.getCode().equals(KeyCode.ENTER) || event.getCode().equals(KeyCode.TAB)){
            logIn();
        }
    }
}
