package com.library.librarymanagement;

import com.library.librarymanagement.DB.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.SQLException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("LogIn.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        LogIn logIn = fxmlLoader.getController();
        logIn.setStage(stage);

        scene.setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/Images/LibraryManagement.png")));
        stage.setTitle("Log In");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) throws SQLException {
        Database database = new Database();
        String user = database.getValue("SELECT * FROM users");
        if (user == null){
            database.addDefaultAdmin();
            System.out.println("Default Admin Added");
        }
        launch(args);
    }
}
