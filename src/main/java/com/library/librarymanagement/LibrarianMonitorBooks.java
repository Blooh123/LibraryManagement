package com.library.librarymanagement;

import com.library.librarymanagement.DB.Database;
import com.library.librarymanagement.Enity.BorrowRecord;
import com.library.librarymanagement.SecurityUtils.SecurityUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import javax.crypto.SecretKey;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class LibrarianMonitorBooks implements Initializable {
    //DB stuff
    private final String DB_URL = "jdbc:mysql://localhost/securelibrary";
    private final String USER = "root";
    private final String PASS = "";
    Database database = new Database();
    ////

    SecurityUtils securityUtils = new SecurityUtils();
    @FXML
    private AnchorPane mainContainer;

    @FXML
    private ScrollPane scrollPane;
    private String query = "SELECT * FROM borrow_records";

    private void loadAllRecords(String query) {
        new Thread(() -> {
            boolean check = false;
            List<BorrowRecord> borrowRecords = new ArrayList<>();

            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
                 PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    // Retrieve the KEK (Key Encryption Key) for unwrapping the AES key
                    SecretKey kek = securityUtils.generateAESKey();  // This should be your KEK (Key Encryption Key)
                    check = true;

                    // Retrieve the encrypted AES key from the database
                    String encryptedKey = resultSet.getString("en_key");
                    System.out.println("Encrypted AES Key: " + encryptedKey);

                    // Unwrap the encrypted AES key using the KEK
                    SecretKey unwrappedKey = securityUtils.unwrapKey(encryptedKey, kek);
                    System.out.println("Unwrapped key: " + unwrappedKey);

                    // Decrypt the fields using the unwrapped AES key
                    String book_id = securityUtils.decryptAES(resultSet.getString("book_id"), unwrappedKey);
                    String user_id = securityUtils.decryptAES(resultSet.getString("user_id"), unwrappedKey);
                    String borrowDate = resultSet.getString("borrow_date");
                    String dueDate = resultSet.getString("due_date");
                    String returnDate = resultSet.getString("return_date");
                    String fine = resultSet.getString("fine");

                    // Print decrypted data for testing
                    System.out.println("Decrypted Book ID: " + book_id);
                    System.out.println("Decrypted User ID: " + user_id);
                    System.out.println("Borrow Date: " + borrowDate);
                    System.out.println("Due Date: " + dueDate);
                    System.out.println("Return Date: " + returnDate);
                    System.out.println("Fine: " + fine);

                    // Add the decrypted record to your borrowRecords list (for further use)
                  //  borrowRecords.add(new BorrowRecord(book_id, user_id, borrowDate, dueDate, returnDate, fine));
                }
            } catch (SQLException e) {
                Platform.runLater(() -> showAlert("Error", null, "Something went wrong while loading data", Alert.AlertType.ERROR));
            } catch (Exception e) {
                throw new RuntimeException("Error decrypting data: " + e.getMessage());
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
                    // Iterate over the borrowRecords list and create UI elements
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
//            try {
//               // deleteAction(id);
//            } catch (SQLException e) {
//                showAlert("Error", null ,"Something went wrong!", Alert.AlertType.ERROR);
//            }
        });

        editButton.setOnAction(event -> {
//            try {
//                //editAction(id);
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
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
    private void showAlert(String Title, String Header, String Message, Alert.AlertType alertType){
        Alert alert = new Alert(alertType);
        alert.setTitle(Title);
        alert.setHeaderText(Header);
        alert.setContentText(Message);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadAllRecords(query);
    }
}
