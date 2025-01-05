package com.library.librarymanagement;

import com.library.librarymanagement.DB.Database;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class EditAdminDetails implements Initializable {

    @FXML
    private PasswordField confirmPasswordField1,passwordFieldAdd1;

    @FXML
    private TextField emailField1,usernameFIeld1;
    @FXML
    private Label id;

    @FXML
    private Button saveBtn1;

    public void setFields(String username, String email, String id){
        this.usernameFIeld1.setText(username);
        this.emailField1.setText(email);
        this.id.setText(id);

    }
    private Database database = new Database();
    @FXML
    private void saveEditedUser() throws SQLException {
        String username = usernameFIeld1.getText();
        String password = new String(passwordFieldAdd1.getText());
        String email = emailField1.getText();
        String confirmPassword = new String(confirmPasswordField1.getText());
        int ID = Integer.parseInt(id.getText());

        if ((username.isEmpty() || username.isBlank()) || (email.isEmpty() || email.isBlank())){
            showAlert("Unable to proceed", null, "Please fill out all the fields!", Alert.AlertType.INFORMATION);
            return;
        }
        if (database.checkIfActualUser(ID,username, email)){
            if (password.isEmpty()){
                database.updateAdmin(ID,username,email);
                showAlert("Success",null, "Saved successfully!", Alert.AlertType.INFORMATION);
                closeStage();
                return;
            }else {
                if (!isValidPassword(password)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Password");
                    alert.setHeaderText("Password does not meet the requirements");
                    alert.setContentText("Password must be 8-16 characters long and contain at least one letter and one number.");
                    alert.showAndWait();
                    return; // Stop further execution if validation fails
                }
                if (!password.equals(confirmPassword)){
                    showAlert("Unable to proceed", null, "Password did not match!", Alert.AlertType.INFORMATION);
                    return;
                }
                database.updateAdmin(ID,username,password,email);
                showAlert("Success",null, "Saved successfully!", Alert.AlertType.INFORMATION);
                closeStage();
                return;
            }

        }
        if (!username.equalsIgnoreCase(OriginalUsername)){
            if (database.checkIfUsernameExists(username)){
                showAlert("Unable to proceed", null, "Username already exists!", Alert.AlertType.INFORMATION);
                return;
            }
        }
        if (!email.equalsIgnoreCase(OriginalEmail)){
            if (database.checkIfEmailExists(email)){
                showAlert("Unable to proceed", null, "Email already exists!", Alert.AlertType.INFORMATION);
                return;
            }
        }

        if (password.isEmpty() || password.isBlank()){
            database.updateAdmin(ID,username,email);
            showAlert("Success",null, "Saved successfully!", Alert.AlertType.INFORMATION);
            closeStage();
            return;
        }
        database.updateAdmin(ID,username,password,email);
        showAlert("Success",null, "Saved successfully!", Alert.AlertType.INFORMATION);
        closeStage();


    }
    private void closeStage(){
        Stage currentStage = (Stage) id.getScene().getWindow();
        currentStage.close();
    }
    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,16}$";
        return password.matches(passwordPattern);
    }
    private void showAlert(String Title, String Header, String Message, Alert.AlertType alertType){
        Alert alert = new Alert(alertType);
        alert.setTitle(Title);
        alert.setHeaderText(Header);
        alert.setContentText(Message);
        alert.showAndWait();
    }
    private String OriginalUsername;
    private String OriginalEmail;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //e delay ang pag execute
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> {
            OriginalUsername = usernameFIeld1.getText();
            OriginalEmail = emailField1.getText();
            System.out.println(OriginalUsername);
            System.out.println(OriginalEmail);
        });
        delay.play();
    }
}
