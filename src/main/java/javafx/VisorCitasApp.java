package javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class VisorCitasApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        File fxmlFile = new File("src/main/resources/fxml/visorCitas.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
        Scene scene = new Scene(loader.load());

        stage.setScene(scene);
        stage.show();
    }

    public static void launchApp(){
        launch();
    }
}
