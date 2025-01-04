package com.library.librarymanagement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Admin implements Initializable {
    @FXML
    private BorderPane borderPane;
    private Map<String, Pane> loadedScenes = new HashMap<>();
    @FXML
    private Label usernameLabel,roleLabel;


    private String userRole;
    private String userName;

    public void setRoleAndUsername(String role,String userName){
        this.userRole = role;
        this.userName= userName;
        usernameLabel.setText(userName);
        roleLabel.setText(role);

    }

    @FXML
    private void userManagementAction(ActionEvent event) throws IOException {
        setCenteredPane("AdminUserManagement.fxml");
    }
    @FXML
    private void bookInventoryAction(ActionEvent event) throws  IOException{
        setCenteredPane("AdminBookInventory.fxml");
    }
    @FXML
    private void reportsAction(ActionEvent event) throws IOException {
        setCenteredPane("AdminReports.fxml");
    }

    @FXML
    private void logOutAction() throws IOException {
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

        //  LogInForAdmin logInForAdmin = loader.getController();
        //  loadingContainer.setVisible(true);

        currentStage.close();
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        LogIn logIn = loader.getController();
        logIn.setStage(stage);
        //   logInForAdmin.setStage(stage);
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/Images/LibraryManagement.png")));
        scene.setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();
    }
    private void setCenteredPane(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Pane pane = loadedScenes.get(fxml);



        if (pane == null) {  // Load and cache if not already loaded
            pane = loader.load();



            Object controller = loader.getController();
            if (controller instanceof AdminUserManagement){
                ((AdminUserManagement)controller).setRoleAndUsername(userName,userRole);
            }


            loadedScenes.put(fxml, pane);
        }
        borderPane.setCenter(pane);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            setCenteredPane("AdminUserManagement.fxml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
