package main;/**
 * Created by hendro-sinaga on 21/08/16.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static Stage mainStage;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("maindoc.fxml"));
        primaryStage.setTitle("MedPla");
        primaryStage.setScene(new Scene(root, 622.0, 625.0));
        primaryStage.centerOnScreen();
        Main.mainStage = primaryStage;
        Main.mainStage.show();
    }
}
