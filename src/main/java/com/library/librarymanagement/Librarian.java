package com.library.librarymanagement;

import com.library.librarymanagement.LogIn;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class Librarian implements Initializable {

    private Map<String, Pane> loadedScenes = new HashMap<>();
    @FXML
    private BorderPane borderPane;
    @FXML
    private Button logOutBtn,monitorBooks,bookInventoryBtn;
    @FXML
    private AnchorPane menuContainer;
    @FXML
    void bookInventoryAction(ActionEvent event) throws IOException {
        setCenteredPane("AdminBookInventory.fxml");
    }

    @FXML
    void logOutAction(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation to Log out");
        alert.setContentText("Are you sure you want to log out?");
        Optional<ButtonType> result = alert.showAndWait();


        if (result.isPresent() && result.get() == ButtonType.OK) {
            backToLogInPage();
        }
    }
    private void backToLogInPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LogIn.fxml"));
        Parent root = loader.load();
        Stage currentStage = (Stage) borderPane.getScene().getWindow();

        currentStage.close();
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        LogIn logIn = loader.getController();
        logIn.setStage(stage);
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/Images/LibraryManagement.png")));
        scene.setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();
    }

    @FXML
    void monitorBooksAction(ActionEvent event) throws IOException {
        setCenteredPane("LibrarianMonitorBooks.fxml");
    }

    private void setCenteredPane(String fxml) throws IOException {
        Pane pane = loadedScenes.get(fxml);
        if (pane == null) {  // Load and cache if not already loaded
            pane = FXMLLoader.load(getClass().getResource(fxml));
            loadedScenes.put(fxml, pane);
        }
        borderPane.setCenter(pane);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            setCenteredPane("AdminBookInventory.fxml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
