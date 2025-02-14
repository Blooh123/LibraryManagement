package com.library.librarymanagement;

import com.library.librarymanagement.DB.Database;
import com.library.librarymanagement.Enity.UserData;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class AdminUserManagement implements Initializable {
    //DB stuff
    private final String DB_URL = "jdbc:mysql://localhost/securelibrary";
    private final String USER = "root";
    private final String PASS = "";
    Database database = new Database();
    ////


    @FXML
    private Button searchBtn,addBtn,refreshBtn;
    @FXML
    private AnchorPane mainContainer,addUserPane,editUserPane;
    @FXML
    private TextField searchField,usernameFIeld,passwordFieldAdd,confirmPasswordField,usernameFIeld1,passwordFieldAdd1,confirmPasswordField1,emailField1,emailField;
    @FXML
    private ComboBox<String> rolesCombo,rolesCombo1;
    @FXML
    private Label usernameLabel,roleLabel;

    private String userName;
    private String userRole;


    public void setRoleAndUsername(String username, String userRole){
        System.out.println(username + " user management");
        this.userName = username;
        this.userRole = userRole;
        usernameLabel.setText(username);
        roleLabel.setText(userRole);
    }

    private String loadRecordsQuery = "SELECT * FROM users";
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setButtonIcon(searchBtn, "/Icons/SearchIcon.png", 35, 35);
        setButtonIcon(addBtn, "/Icons/PlusIcon.png", 35, 35);
        setButtonIcon(refreshBtn, "/Icons/ReloadIcon.png", 35, 35);

//        rolesCombo.getItems().addAll("Admin", "Librarian", "Student");
//        rolesCombo1.getItems().addAll("Admin", "Librarian", "Student");

        addUserPane.setLayoutY(-800);
        editUserPane.setLayoutY(-800);

        loadAllRecords(loadRecordsQuery);
    }
    @FXML
    private void refreshPage(ActionEvent event){
       // clearDataPane();
        loadAllRecords(loadRecordsQuery);
    }
    @FXML
    private void addUserAction(ActionEvent event){
        mainContainer.setDisable(true);
        addBtn.setDisable(true);
        refreshBtn.setDisable(true);
        searchBtn.setDisable(true);
        searchField.setDisable(true);
        animatePane(addUserPane,0,900,200);



        if (roleLabel.getText().equalsIgnoreCase("Admin")){
            rolesCombo.getItems().setAll("Librarian", "Student");
        }else {
            rolesCombo.getItems().setAll("Admin", "Librarian", "Student");
        }
    }
    @FXML
    private void closeAdduserPane(MouseEvent event){
        animatePane(addUserPane,0,-900,200);
        mainContainer.setDisable(false);
        addBtn.setDisable(false);
        refreshBtn.setDisable(false);
        searchBtn.setDisable(false);
        searchField.setDisable(false);
        usernameFIeld.clear();
        passwordFieldAdd.clear();
        confirmPasswordField.clear();
    }
    @FXML
    private void closeEditUser(MouseEvent event){
        animatePane(editUserPane,0,-900,200);
        mainContainer.setDisable(false);
        addBtn.setDisable(false);
        refreshBtn.setDisable(false);
        searchBtn.setDisable(false);
        searchField.setDisable(false);
        usernameFIeld1.clear();
        passwordFieldAdd1.clear();
        confirmPasswordField1.clear();
        emailField1.clear();
    }

    @FXML
    private void SaveUser(ActionEvent event) throws SQLException {

        String username = usernameFIeld.getText();
        String password = new String(passwordFieldAdd.getText());
        String role = rolesCombo.getValue();
        String email = emailField.getText();
        String confirmPassword = new String(confirmPasswordField.getText());
        if ((username.isEmpty() || username.isBlank()) || (password.isEmpty() || password.isBlank()) || (confirmPassword.isEmpty() || confirmPassword.isBlank()) || (email.isEmpty() || email.isBlank())){
            showAlert("Unable to proceed", null, "Please fill out all the fields!", Alert.AlertType.INFORMATION);
            return;
        }
        // Validate the password
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
        if (role == null){
            showAlert("Unable to proceed",null, "Please select a role", Alert.AlertType.INFORMATION);
            return;
        }

        if (database.checkIfUsernameExists(username)){
            showAlert("Unable to proceed", null, "Username already exists!", Alert.AlertType.INFORMATION);
            return;
        }

        if (database.checkIfEmailExists(email)){
            showAlert("Unable to proceed", null, "Email already exists!", Alert.AlertType.INFORMATION);
            return;
        }

        //check if email is existing or active


        if (!checkIfEmailIsActive(email)){
            showAlert("Unable to proceed", null, "Email not found!", Alert.AlertType.INFORMATION);
            return;
        }
        database.addUser(username,password,role, email);
        database.addToActivityLog(usernameLabel.getText(),roleLabel.getText(),"Added a new user: " +username + " - Role: " +  role);
        showAlert("Success",null, "Added successfully!", Alert.AlertType.INFORMATION);
        usernameFIeld.clear();
        passwordFieldAdd.clear();
        confirmPasswordField.clear();
        emailField.clear();
        animatePane(addUserPane,0,-900,400);
        mainContainer.setDisable(false);
        addBtn.setDisable(false);
        refreshBtn.setDisable(false);
        searchBtn.setDisable(false);
        searchField.setDisable(false);
        clearDataPane();
        loadAllRecords(loadRecordsQuery);
    }

    private boolean checkIfEmailIsActive(String email) {
        EmailSender sender = new EmailSender();
        final boolean[] isActive = {false};
        CountDownLatch latch = new CountDownLatch(1);

        sender.sentWelcomeMessage(email, new EmailCallback() {
            @Override
            public void onSuccess() {
                isActive[0] = true;
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                isActive[0] = false;
                latch.countDown();
            }
        });

        try {
            latch.await(); // Wait until email sending completes
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return isActive[0];
    }


    private int ID;
    private String OriginalUsername;
    private String OriginalEmail;
    @FXML
    private void saveEditedUser(ActionEvent event) throws SQLException {
        String username = usernameFIeld1.getText();
        String password = new String(passwordFieldAdd1.getText());
        String role = rolesCombo1.getValue();
        String email = emailField1.getText();
        String confirmPassword = new String(confirmPasswordField1.getText());
        if ((username.isEmpty() || username.isBlank()) || (email.isEmpty() || email.isBlank())){
            showAlert("Unable to proceed", null, "Please fill out all the fields!", Alert.AlertType.INFORMATION);
            return;
        }


        if (database.checkIfActualUser(ID,username,email)){
            if (!password.isEmpty()){
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
                database.updateUser(ID,username,password,role,email);
                database.addToActivityLog(usernameLabel.getText(),roleLabel.getText(),usernameLabel.getText()  +" Updated a user: " + OriginalUsername);
                showAlert("Success",null, "Saved successfully!", Alert.AlertType.INFORMATION);
                usernameFIeld1.clear();
                passwordFieldAdd1.clear();
                confirmPasswordField1.clear();
                animatePane(editUserPane,0,-900,400);
                mainContainer.setDisable(false);
                addBtn.setDisable(false);
                refreshBtn.setDisable(false);
                searchBtn.setDisable(false);
                searchField.setDisable(false);
                clearDataPane();
                loadAllRecords(loadRecordsQuery);
                return;
            }else {
                database.updateUser(ID,username,role,email);
                database.addToActivityLog(usernameLabel.getText(),roleLabel.getText(),"Update a user: " + OriginalUsername + " New details has been change! (Username,Role, Email)");
                showAlert("Success",null, "Saved successfully!", Alert.AlertType.INFORMATION);
                usernameFIeld1.clear();
                passwordFieldAdd1.clear();
                confirmPasswordField1.clear();
                animatePane(editUserPane,0,-900,400);
                mainContainer.setDisable(false);
                addBtn.setDisable(false);
                refreshBtn.setDisable(false);
                searchBtn.setDisable(false);
                searchField.setDisable(false);
                clearDataPane();
                loadAllRecords(loadRecordsQuery);
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
            database.updateUser(ID,username,role, email);
            showAlert("Success",null, "Saved successfully!", Alert.AlertType.INFORMATION);
            animatePane(editUserPane,0,-900,400);
            return;
        }
        database.updateUser(ID,username,password,role, email);
        database.addToActivityLog(usernameLabel.getText(),roleLabel.getText(),"Update a user: " + OriginalUsername + " New details has been change! (Username,Password,Role, Email)");
        showAlert("Success",null, "Saved successfully!", Alert.AlertType.INFORMATION);
        usernameFIeld1.clear();
        passwordFieldAdd1.clear();
        confirmPasswordField1.clear();
        emailField1.clear();
        animatePane(editUserPane,0,-900,400);
        mainContainer.setDisable(false);
        addBtn.setDisable(false);
        refreshBtn.setDisable(false);
        searchBtn.setDisable(false);
        searchField.setDisable(false);
        clearDataPane();
        loadAllRecords(loadRecordsQuery);
    }

    /**
     * Validates the password based on the required criteria.
     *
     * @param password The password to validate.
     * @return true if the password meets the criteria, false otherwise.
     */
    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,16}$";
        return password.matches(passwordPattern);
    }
    @FXML
    private void searchAction(ActionEvent event){
        String queryField = searchField.getText();
        loadAllRecords("SELECT * FROM users WHERE username = '" + queryField + "' OR role = '" + queryField + "'");
    }

    private void animatePane(Node node, double x, double y, double millis){
        TranslateTransition translateTransition = new TranslateTransition( Duration.millis(millis),node);
        translateTransition.setByY(y);
        translateTransition.setByX(x);
        translateTransition.play();
    }
    private void loadAllRecords(String query) {
        new Thread(() -> {
            //String query = "SELECT * FROM users";
            boolean check = false;
            List<UserData> userDataList = new ArrayList<>();

            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
                 PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    check = true;
                    if(!resultSet.getString("role").equalsIgnoreCase("Super Admin")){
                        if (!resultSet.getString("username").equalsIgnoreCase(usernameLabel.getText())){
                            userDataList.add(new UserData(
                                    resultSet.getInt("id"),
                                    resultSet.getString("username"),
                                    resultSet.getString("role")
                            ));
                        }

                    }

                }
            } catch (SQLException e) {
                Platform.runLater(() -> showAlert("Error", null, "Something went wrong while loading data", Alert.AlertType.ERROR));
                return;
            }

            boolean finalCheck = check;

            Platform.runLater(() -> {
                clearDataPane(); // Clear previous data
                if (!finalCheck) {
                    Label noDataLabel = createStyledLabel("No Data to show", 500, 100, 200);
                    noDataLabel.setOpacity(0.50);
                    mainContainer.getChildren().add(noDataLabel);
                } else {
                    // Create panes for each record
                    for (UserData userData : userDataList) {
                        createDataPane(userData.id, userData.userName, userData.role);
                    }
                }
            });
        }).start();
    }

    private void clearDataPane() {
        mainContainer.getChildren().clear();
        mainContainer.setPrefHeight(0); // Reset height
    }

    private void createDataPane(int id, String userName, String role) {
        AnchorPane dataPane = createStyledDataPane(id);
        Label ID = createStyledLabel(String.valueOf(id), 97, 23, 291);
        Label UserName = createStyledLabel(userName, 330, 23, 291);
        Label Role = createStyledLabel(role, 579, 23, 291);

        Button editButton = createButton(id, "/Icons/EditIcon.png");
        editButton.setLayoutX(950);
        editButton.setLayoutY(15);
        Button deleteButton = createButton(id, "/Icons/DeleteIcon.png");
        deleteButton.setLayoutY(15);
        deleteButton.setLayoutX(1000);

        deleteButton.setOnAction(event -> {
            try {
                deleteAction(id);
            } catch (SQLException e) {
                showAlert("Error", null ,"Something went wrong!", Alert.AlertType.ERROR);
            }
        });

        editButton.setOnAction(event -> {
            try {
                editAction(id);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        dataPane.setOnMousePressed(event -> {
            // Uncomment and implement any click handling logic for the row
            // handleEditButtonAction(id, userName, role);
        });
        dataPane.getChildren().addAll(ID, UserName, Role,editButton,deleteButton);
        int index = mainContainer.getChildren().size();
        dataPane.setLayoutY(index * 90);
        mainContainer.getChildren().add(dataPane);
        mainContainer.setPrefHeight((index + 1) * 90);
    }
    private Button createButton(int ID, String IconPath) {
        Button button = new Button();
        button.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #6a89cc; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 5 10; " +
                        "-fx-cursor: hand;"
        );

        // Load the icon image
        ImageView iconView = new ImageView();
        try {
            Image iconImage = new Image(getClass().getResourceAsStream(IconPath)); // Ensure IconPath is a valid path or URL
            iconView.setImage(iconImage);
            iconView.setFitWidth(30); // Set desired width
            iconView.setFitHeight(30); // Set desired height
            iconView.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Error loading icon image: " + e.getMessage());
        }

        // Add the icon to the button
        button.setGraphic(iconView);

        // Add scaling effect when pressed
        button.setOnMousePressed(event -> {
            button.setScaleX(0.9); // Slightly reduce button size
            button.setScaleY(0.9);
        });

        button.setOnMouseReleased(event -> {
            button.setScaleX(1.0); // Reset to original size
            button.setScaleY(1.0);
        });

        // Add button action (replace with your actual logic)
        button.setOnAction(event -> {
            // Your action here
            System.out.println("Button clicked with ID: " + ID);
        });
        return button;
    }
    private void deleteAction(int id) throws SQLException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation to Delete");
        alert.setContentText("Are you sure you want to delete?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            database.deleteUser(id);
            loadAllRecords(loadRecordsQuery);
            showAlert("Success",null,"Delete successfully!", Alert.AlertType.INFORMATION);
        }
    }
    private void editAction(int id) throws SQLException {
        animatePane(editUserPane,0,900,200);
        // Fetch data from the database
        String userName = database.getValue("SELECT username FROM users WHERE id = " + id).trim();
        String Role = database.getValue("SELECT role FROM users WHERE id = " + id).trim();
        ID = id;
        String email = database.getValue("SELECT email FROM users WHERE id = " + id).trim();
        String role = roleLabel.getText();

        OriginalUsername = userName;
        OriginalEmail = email;

        if (role.equalsIgnoreCase("Admin")){
            rolesCombo1.getItems().setAll("Librarian", "Student");
        }else {
            rolesCombo1.getItems().setAll("Admin", "Librarian", "Student");
        }


        usernameFIeld1.setText(userName);
        rolesCombo1.setValue(Role);
        emailField1.setText(email);
    }
    private AnchorPane createStyledDataPane(int ID) {
        AnchorPane dataPane = new AnchorPane();
        dataPane.setPrefSize(1093, 68);
        dataPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 6, 0, 2, 2);");
        dataPane.setOnMouseEntered(event -> dataPane.setStyle("-fx-background-color: #f0f0f0; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 8, 0, 4, 4);"));
        dataPane.setOnMouseExited(event -> dataPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 6, 0, 2, 2);"));
        //dataPane.setOnMousePressed(event -> handleEditButtonAction(airportCode));
        return dataPane;
    }
    private Label createStyledLabel(String text, double x, double y, double width) {
        Label label = new Label(text);
        label.setLayoutX(x);
        label.setLayoutY(y);
        label.setPrefWidth(width);
        label.setStyle("-fx-font-size: 16px; -fx-font-family: 'Arial'; -fx-text-fill: #333333;");
        return label;
    }




    private void setButtonIcon(Button button, String iconPath, double width, double height) {
        // Load the icon as an Image
        Image icon = new Image(getClass().getResourceAsStream(iconPath));

        // Create an ImageView with the specified dimensions
        ImageView imageView = new ImageView(icon);
        imageView.setFitWidth(width);  // Set the desired width
        imageView.setFitHeight(height);  // Set the desired height
        imageView.setPreserveRatio(true);  // Preserve the aspect ratio

        // Set the ImageView as the button's graphic
        button.setGraphic(imageView);
    }
    private void showAlert(String Title, String Header, String Message, Alert.AlertType alertType){
        Alert alert = new Alert(alertType);
        alert.setTitle(Title);
        alert.setHeaderText(Header);
        alert.setContentText(Message);
        alert.showAndWait();
    }

}
