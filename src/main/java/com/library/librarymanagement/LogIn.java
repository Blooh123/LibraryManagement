package com.library.librarymanagement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.xml.transform.Result;
import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.sql.*;
import java.util.ResourceBundle;

public class LogIn implements Initializable {
    //DB stuff
    private final String DB_URL = "jdbc:mysql://localhost/securelibrary";
    private final String USER = "root";
    private final String PASS = "";
    ////


    //FXML stuff
    @FXML
    private AnchorPane coverPane,verificationContainer,verificationContainer1;
    @FXML
    private ImageView closeIcon;
    @FXML
    private TextField emailField,codeTextField;
    @FXML
    private Label randomCode;
    @FXML
    private PasswordField passwordField;
    private Stage stage;
    ////

    //Global variable stuff
    private double y;
    private double x;
    private String role;
    ///
    public void setStage(Stage stage){
        this.stage = stage;
    }

    @FXML
    private void close(MouseEvent event){
        Stage currentStage = (Stage) closeIcon.getScene().getWindow();
        currentStage.close();
    }
    @FXML
    private void logInAction(ActionEvent event){
        String userName = emailField.getText();
        String password = new String(passwordField.getText());
        if ((userName.isEmpty() || userName.isBlank()) || (password.isBlank() || password.isBlank())){
            showAlert("Invalid", null,"Please fill out the fields", Alert.AlertType.INFORMATION);
            return;
        }
        //System.out.println("Bilat");
        try (Connection connection = DriverManager.getConnection(DB_URL,USER,PASS);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT role FROM users WHERE (username = ? AND password = SHA1(?))")){

            preparedStatement.setString(1, userName);
            preparedStatement.setString(2,password);

            try (ResultSet resultSet = preparedStatement.executeQuery()){
                if (resultSet.next()){
                    String role = resultSet.getString("role");
                    this.role = role;
                    openDashboard(role);
                }else {
                    showAlert("Invalid", null,"Invalid username or password.", Alert.AlertType.INFORMATION);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            showAlert("Error", null, "Something went wrong!", Alert.AlertType.ERROR);
        }
    }
    private void openDashboard(String role) throws IOException {
        Stage currentStage = (Stage) closeIcon.getScene().getWindow();

        if (role.equalsIgnoreCase("Admin")){

                verificationContainer.setVisible(true);
                randomCode.setText(generateRandomCode());

        }else if (role.equalsIgnoreCase("Librarian")){
            showAlert("Success", null, "Login successfully! Role: " + role, Alert.AlertType.INFORMATION);
            openNewStage("Librarian.fxml","Librarian Dashboard");
        } else if (role.equalsIgnoreCase("Student")) {
            showAlert("Success", null, "Login successfully! Role: " + role, Alert.AlertType.INFORMATION);
            openNewStage("StudentDashBoard.fxml","Student Dashboard");
        }
    }
    @FXML
    private void proceed(ActionEvent event) throws IOException {
        openNewStage("Admin.fxml","Admin DashBoard");
        if (codeTextField.getText().equals(randomCode.getText())){
//
//            verificationContainer.setVisible(false);
//            verificationContainer1.setVisible(true);
//            showAlert("Second verification", null, "A 6 digit code has been set to your email.", Alert.AlertType.INFORMATION);



        }
    }


    private String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            code.append((char) (random.nextInt(26) + 'A'));
        }
        return code.toString();
    }
    private void openNewStage(String fxml, String title) throws IOException {
        Stage currentStage = (Stage) closeIcon.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage1 = new Stage();
        stage1.setTitle(title);
        stage1.getIcons().add(new Image(getClass().getResourceAsStream("/Images/LibraryManagement.png")));
        stage1.setScene(scene);
        stage1.show();
        currentStage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        coverPane.setOnMousePressed(mouseEvent -> {
            x = mouseEvent.getSceneX();
            y = mouseEvent.getSceneY();
        });
        coverPane.setOnMouseDragged(mouseEvent -> {
            stage.setX(mouseEvent.getScreenX() - x);
            stage.setY(mouseEvent.getScreenY() - y);
        });
    }

    private void showAlert(String Title, String Header, String Message, Alert.AlertType alertType){
        Alert alert = new Alert(alertType);
        alert.setTitle(Title);
        alert.setHeaderText(Header);
        alert.setContentText(Message);
        alert.showAndWait();
    }
}
