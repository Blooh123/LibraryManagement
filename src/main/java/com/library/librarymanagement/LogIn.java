package com.library.librarymanagement;

import com.library.librarymanagement.DB.Database;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
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
    private TextField emailField,codeTextField,codeTextField1;
    @FXML
    private Label randomCode;
    @FXML
    private PasswordField passwordField;
    private Stage stage;
    ////
    Database database = new Database();
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
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT role FROM users WHERE (username = ? AND password = SHA2(?,256))")){

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
    private void openDashboard(String role) throws IOException, SQLException {
        Stage currentStage = (Stage) closeIcon.getScene().getWindow();

        if (role.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("Admin")){
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
    private void proceed(ActionEvent event) throws IOException, SQLException {

        if (codeTextField.getText().equals(randomCode.getText())){
            verificationContainer.setVisible(false);
            showAlert("Second verification", null, "A 6 digit code has been set to your email.", Alert.AlertType.INFORMATION);
            String emailForUsers = database.getValue("SELECT email FROM users WHERE username = '" + emailField.getText() + "'");
            new EmailSender(emailForUsers);
            verificationContainer1.setVisible(true);



 //

        }
    }

    //verify action button
    @FXML
    private void verifyAction() throws SQLException, IOException {
        String email = database.getValue("SELECT email FROM users WHERE username = '" + emailField.getText() + "'");//mag kuha sa email sa user based sa iyang userName (so ma hulog na unique ang each username)
        String getVerificationCode = database.getValue("SELECT code FROM confirmation WHERE email = '" + email + "'");//mag kuha sa verification code sa confirmation table sa database
        String entered_code = codeTextField1.getText();//mag kuha sa ge enter ni user na code

        //check if nag match bah ang verification code
        if (entered_code.equals(getVerificationCode)){
            database.deleteCode(email, getVerificationCode);
            showAlert("Log In Successful", null, "Log in successfully as " + role, Alert.AlertType.INFORMATION);
            openNewStage("Admin.fxml","Admin DashBoard");
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
    private void openNewStage(String fxml, String title) throws IOException, SQLException {
        Stage currentStage = (Stage) closeIcon.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
        Parent root = fxmlLoader.load();

        String id = database.getValue("SELECT id FROM users WHERE username = '" + emailField.getText() + "'");
        String email = database.getValue("SELECT email FROM users WHERE id = " + id);
        String password = database.getValue("SELECT password FROM users WHERE id = " + id);

        if (role.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("Admin")){
            Admin admin = fxmlLoader.getController();
            admin.setRoleAndUsername(role,emailField.getText(), id,email,password);
        } else if (role.equalsIgnoreCase("Student")) {
            String idStudent = database.getValue("SELECT id FROM users WHERE username = '" +emailField.getText() + "'" );
            StudentDashBoard studentDashBoard = fxmlLoader.getController();
            studentDashBoard.setID(idStudent);
        }else if (role.equalsIgnoreCase("Librarian")){
            Librarian librarian = fxmlLoader.getController();
            librarian.setNameRole(emailField.getText(),role);
        }

        Scene scene = new Scene(root);
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
