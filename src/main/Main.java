package main;
/**
 * Created by hendro-sinaga on 21/08/16.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import kelas.AlertInfo;

public class Main extends Application {
    public static Stage mainStage;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("maindoc.fxml"));
        primaryStage.setTitle("MedPla - Media Player (version 0.2 build 22)");
        primaryStage.setScene(new Scene(root, 622.0, 625.0));
        primaryStage.centerOnScreen();
        primaryStage.setResizable(false);
        Main.mainStage = primaryStage;

        try {
            Main.mainStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        } catch (Exception e) {
            AlertInfo.showAlertErrorMessage(
                    "Informasi Aplikasi",
                    "Inisialisasi Icon Aplikasi",
                    "Icon aplikasi tidak ditemukan",
                    ButtonType.OK
            );
        }

        Main.mainStage.show();
    }

}
