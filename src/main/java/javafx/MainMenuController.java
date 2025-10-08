package javafx;

import GestorDB.MongoDB;
import com.mongodb.client.MongoCollection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
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
        String email = emailTf.getText();
        String password = passwordPf.getText();
        boolean correct;

        MongoDB mongoDB = MongoDB.getInstance();
        MongoCollection<Document> usuarios = MongoDB.getDatabase().getCollection("usuario");
        System.out.println("{'user':'" + email + "', 'password':'" + password + "'}");
        List<Document> users = mongoDB.find(usuarios, Document.parse("{'user':'" + email + "', 'password':'" + password + "'}"));
        System.out.println(users);
        correct = !users.isEmpty();

        if (correct){
            Stage stage = ((Stage) passwordPf.getScene().getWindow());
            stage.close();

            VisorCitasApp visorCitasApp = new VisorCitasApp();
            try {
                visorCitasApp.start(new Stage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
}
