package com.library.librarymanagement;

import com.library.librarymanagement.DB.Database;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class Admin implements Initializable {
    @FXML
    private BorderPane borderPane;
    private Map<String, Pane> loadedScenes = new HashMap<>();
    @FXML
    private Label usernameLabel,roleLabel,idLabel,emailLabel,passlabel;


    private String userRole;
    private String userName;

    public void setRoleAndUsername(String role,String userName, String id, String email, String pass){
        this.userRole = role;
        this.userName= userName;
        usernameLabel.setText(userName);
        roleLabel.setText(role);
        idLabel.setText(id);
        emailLabel.setText(email);
        passlabel.setText(pass);

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
        // Stop the timeline before navigating
        if (timeline != null) {
            timeline.stop();  // Stop the timeline
        }
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
            String username = usernameLabel.getText();
            System.out.println(username + "  " + usernameLabel.getText());
            if (controller instanceof AdminUserManagement){
                ((AdminUserManagement)controller).setRoleAndUsername(usernameLabel.getText(),roleLabel.getText());
            }
            loadedScenes.put(fxml, pane);
        }
        borderPane.setCenter(pane);
    }
    private Database database = new Database();
    @FXML
    private void editAdminProfiles() throws IOException, SQLException {
        Stage currentStage = (Stage) usernameLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("EditAdminDetails.fxml"));
        Parent root = loader.load();

        EditAdminDetails editAdminDetails = loader.getController();
        String username = usernameLabel.getText();
        String email = database.getValue("SELECT email FROM users WHERE username = '" + username + "'");
        String id = database.getValue("SELECT id FROM users WHERE username = '"  + username +"'");

        editAdminDetails.setFields(username,email,id);

        Scene scene = new Scene(root);
        Stage newStage = new Stage();
        newStage.setScene(scene);
        newStage.setResizable(false);
        newStage.initOwner(currentStage);
        newStage.initModality(Modality.WINDOW_MODAL);
        newStage.setTitle("Edit details");
        newStage.getIcons().add(new Image(getClass().getResourceAsStream("/Images/LibraryManagement.png")));
        newStage.show();


    }
    private Timeline timeline;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Create a Timeline to check for changes every second
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            try {
                checkForAdminChanges();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE); // Run the check indefinitely
        timeline.play(); // Start the timeline
    }
    private void closeStage() {
        // Stop the timeline before closing the stage
        if (timeline != null) {
            timeline.stop();  // Stop the timeline
        }

        Stage currentStage = (Stage) usernameLabel.getScene().getWindow();
        currentStage.close();
    }


    /**
     * Checks for changes in the admin details in the database.
     */
    private void checkForAdminChanges() throws SQLException {
        String OriginalUsername = usernameLabel.getText();
        String OriginalEmail = emailLabel.getText();
        String OriginalPassword = passlabel.getText();
        try {
            // Fetch current admin details from the database
            String currentUsername = database.getValue("SELECT username FROM users WHERE id = " + idLabel.getText());
            String currentEmail = database.getValue("SELECT email FROM users WHERE id = "  +idLabel.getText());
            String currentPassword = database.getValue("SELECT password FROM users WHERE id = "  +idLabel.getText());

            // Compare fetched details with the original ones
            if (!OriginalUsername.equals(currentUsername) || !OriginalEmail.equals(currentEmail) || !OriginalPassword.equals(currentPassword)) {
                System.out.println("Admin details have changed!");

                // Update local variables and reflect changes in the UI
                OriginalUsername = currentUsername;
                OriginalEmail = currentEmail;

                Platform.runLater(() -> {
                        usernameLabel.setText(currentUsername);
                        closeStage();
                        // After closing, navigate to the login page
                        try {
                            backToLogInPage();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
