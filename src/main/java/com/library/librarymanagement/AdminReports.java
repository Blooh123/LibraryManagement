package com.library.librarymanagement;

import com.library.librarymanagement.DB.Database;
import com.library.librarymanagement.Enity.ActivityLog;
import com.library.librarymanagement.Enity.BorrowRecord;
import com.library.librarymanagement.Enity.UserData;
import com.library.librarymanagement.SecurityUtils.SecurityUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import javax.crypto.SecretKey;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminReports implements Initializable {
    //DB stuff
    private final String DB_URL = "jdbc:mysql://localhost/securelibrary";
    private final String USER = "root";
    private final String PASS = "";
    Database database = new Database();
    ////
    @FXML
    private AnchorPane mainContainer,mainContainer1;
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
                    try {
                        // Retrieve and decode the encrypted AES key
                        String encryptedKey = resultSet.getString("en_key");
                        if (encryptedKey == null || encryptedKey.isEmpty()) {
                            throw new IllegalArgumentException("Encrypted key is null or empty.");
                        }

                        // Derive a consistent KEK (Key Encryption Key)
                        String kekPassword = "Hello123"; // Use a secure password
                        byte[] kekSalt = "hedged".getBytes(StandardCharsets.UTF_8); // Consistent salt value
                        SecretKey kek = SecurityUtils.deriveKEK(kekPassword, kekSalt);

                        // Unwrap the AES key using the derived KEK
                        SecretKey aesKey = SecurityUtils.unwrapKey(encryptedKey, kek);

                        // Decrypt sensitive fields
                        int id = resultSet.getInt("id");
                        String book_id = SecurityUtils.decryptAES(resultSet.getString("book_id"), aesKey);
                        String user_id = SecurityUtils.decryptAES(resultSet.getString("user_id"), aesKey);
                        String borrowDate = resultSet.getString("borrow_date");
                        String dueDate = resultSet.getString("due_date");
                        int quantity = resultSet.getInt("quantity");
                        String returnDate = resultSet.getString("return_date");
                        String fine = resultSet.getString("fine");

                        // Log decrypted data (optional for debugging)
                        System.out.println("Decrypted Book ID: " + book_id);
                        System.out.println("Decrypted User ID: " + user_id);

                        // Add the decrypted record to the list
                        borrowRecords.add(new BorrowRecord(id,Integer.parseInt(book_id), Integer.parseInt(user_id), borrowDate,quantity, dueDate, returnDate, Double.parseDouble(fine)));
                        check = true;

                    } catch (Exception e) {
                        System.err.println("Error decrypting data: " + e.getMessage());
                    }
                }
            } catch (SQLException e) {
                Platform.runLater(() -> showAlert("Error", null, "Something went wrong while loading data", Alert.AlertType.ERROR));
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }

            boolean finalCheck = check;

            Platform.runLater(() -> {
                clearDataPane(); // Clear previous data
                if (!finalCheck) {
                    Label noDataLabel = createStyledLabel("No Data to show", 500, 100, 200);
                    noDataLabel.setOpacity(0.50);
                    mainContainer.getChildren().add(noDataLabel);
                } else {
                    // Iterate over the records and create UI elements
                    for (BorrowRecord record : borrowRecords) {
                        try {
                            createDataPane(record.getId(),record.getUserId(),record.getBookId(),record.getQuantity(),record.getBorrowDate(),record.getReturnDate()
                                    ,record.getDueDate(),record.getFine());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        }).start();
    }
    private void clearDataPane() {
        mainContainer.getChildren().clear();
        mainContainer.setPrefHeight(0); // Reset height
    }
    private void createDataPane(int id, int userID, int bookID, int quantity, String borrowDate, String returnDate, String dueDate, double fine) throws SQLException {
        AnchorPane dataPane = createStyledDataPane(id);

        // Fetching user name and book title from the database
        String username = database.getValue("SELECT username FROM users WHERE id = " + userID);
        String book = database.getValue("SELECT title FROM books WHERE id = " + bookID);

        // Fetching the book cover as a Blob
        Blob bookCoverBlob = database.getBlobValue("SELECT book_cover FROM books WHERE id = " + bookID);
        Image bookCoverImage = null;
        if (bookCoverBlob != null) {
            try (InputStream is = bookCoverBlob.getBinaryStream()) {
                bookCoverImage = new Image(is);
            } catch (Exception e) {
                System.out.println("Error loading book cover image: " + e.getMessage());
            }
        }

        // Creating and styling the labels
        Label studentName = createStyledLabel("User: " + username, 10, 10, 300);
        Label bookTitle = createStyledLabel("Book: " + book, 10, 50, 300);
        Label bookQuantity = createStyledLabel("Quantity: " + quantity, 10, 90, 300);
        Label borrowDateLabel = createStyledLabel("Borrow Date: " + borrowDate, 10, 130, 300);
        Label dueDateLabel = createStyledLabel("Due Date: " + dueDate, 10, 170, 300);

        // Handling the return date (if empty, set it to "Not Yet Returned")
        String returnDateText = (returnDate == null || returnDate.isEmpty()) ? "Not Yet Returned" : returnDate;
        Label returnDateLabel = createStyledLabel("Return Date: " + returnDateText, 10, 210, 300);
        Label fineLabel = createStyledLabel("Fine: " + String.format("%.2f", fine), 10, 250, 300);

        // Adding the book cover as an ImageView (if available)
        ImageView bookCoverView = new ImageView();
        if (bookCoverImage != null) {
            bookCoverView.setImage(bookCoverImage);
            bookCoverView.setFitHeight(150);
            bookCoverView.setFitWidth(120);
            bookCoverView.setLayoutX(10);
            bookCoverView.setLayoutY(10);
        } else {
            System.out.println("No book cover available for bookID: " + bookID);
        }

        // Creating the "Add Fine" button
        Button addFineButton = new Button("Add Fine");
        addFineButton.setStyle(
                "-fx-font-size: 12px; " +
                        "-fx-padding: 5px 10px; " +
                        "-fx-background-color: #4CAF50; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-font-weight: bold;"
        );
        addFineButton.setLayoutX(10);
        addFineButton.setLayoutY(290); // Positioning the button below the fine label
        final double[] Fine = {fine}; // Store the fine as an array to allow mutation

        addFineButton.setOnAction(event -> {
            // Handle adding fine logic
            Fine[0] += 5.0; // Add 5 to the fine
            // Update the fine label with the new fine value
            fineLabel.setText("Fine: " + String.format("%.2f", Fine[0])); // Use Fine[0] to display the updated fine
            // Update the fine in the database
            database.update("UPDATE borrow_records SET fine = " + Fine[0] + " WHERE id = " + id); // Use Fine[0] to update the database
            EmailSender emailSender = new EmailSender();
            try {
                String email = database.getValue("SELECT email FROM users WHERE id = " + userID);
                emailSender.notifyStudentAboutFine(email,dueDate,String.valueOf(fine),book);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        });

        // Adjusting layout and adding all components
        VBox contentBox = new VBox(10); // 10px spacing between elements
        contentBox.getChildren().addAll(studentName, bookTitle, bookQuantity, borrowDateLabel, dueDateLabel, returnDateLabel, fineLabel);

        // Adding elements to the dataPane
        contentBox.setLayoutX(150); // Offset to the right for the book cover
        contentBox.setLayoutY(10);

        dataPane.getChildren().add(contentBox);
        if (bookCoverImage != null) {
            dataPane.getChildren().add(bookCoverView);
        }

        // Adding the button to the dataPane
        dataPane.getChildren().add(addFineButton);

        // Adding the dataPane to the main container
        int index = mainContainer.getChildren().size();
        dataPane.setLayoutY(index * 350); // Set Y position based on the current index
        mainContainer.getChildren().add(dataPane);
        mainContainer.setPrefHeight((index + 1) * 400); // Adjust container height
    }
    private AnchorPane createStyledDataPane(int ID) {
        AnchorPane dataPane = new AnchorPane();
        dataPane.setPrefSize(489, 68); // Set pane size
        dataPane.setStyle(
                "-fx-background-color: #ffffff; " + // White background
                        "-fx-border-top-color: #cccccc; " + // Border only on top
                        "-fx-border-top-width: 5px; " + // Slightly thicker top border
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: gray;"

        );

//        // Adding hover effect for dataPane
//        dataPane.setOnMouseEntered(event -> dataPane.setStyle(
//                "-fx-background-color: #f0f0f0; " + // Slightly darker background on hover
//                        "-fx-border-top-color: #cccccc; " + // Border remains only on top
//                        "-fx-border-top-width: 2px; " + // Same width border on hover
//                        "-fx-border-radius: 10; " +
//                        "-fx-background-radius: 10; " +
//                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 8, 0, 4, 4);" // Stronger shadow effect on hover
//        ));
//        dataPane.setOnMouseExited(event -> dataPane.setStyle(
//                "-fx-background-color: #ffffff; " + // White background after hover
//                        "-fx-border-top-color: #cccccc; " + // Light border on top
//                        "-fx-border-top-width: 2px; " + // Same border width as before
//                        "-fx-border-radius: 10; " +
//                        "-fx-background-radius: 10; " +
//                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 6, 0, 2, 2);" // Default shadow effect
//        ));

        return dataPane;
    }
    private Label createStyledLabel(String text, double layoutX, double layoutY, double width) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-font-family: Arial; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #333333; " + // Dark gray text
                        "-fx-padding: 8px 16px; " + // Padding for a nice look
                        "-fx-border-radius: 10px; " + // Rounded corners
                        "-fx-background-radius: 10px; " + // Rounded corners
                        "-fx-border-color: white; " + // Light border color
                        "-fx-border-width: 1px; " + // Thin border
                        "-fx-background-color: transparent;" // Transparent background for the label
        );
        label.setLayoutX(layoutX);
        label.setLayoutY(layoutY);
        label.setPrefWidth(width);
        return label;
    }
    private void showAlert(String Title, String Header, String Message, Alert.AlertType alertType){
        Alert alert = new Alert(alertType);
        alert.setTitle(Title);
        alert.setHeaderText(Header);
        alert.setContentText(Message);
        alert.showAndWait();
    }





    private void loadAllRecords1(String query) {
        clearDataPane1();
        new Thread(() -> {
            //String query = "SELECT * FROM users";
            boolean check = false;
            List<ActivityLog> activityLogs = new ArrayList<>();

            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
                 PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String user = resultSet.getString("user");
                    String role = resultSet.getString("role");
                    String activity  = resultSet.getString("activity");
                    activityLogs.add(new ActivityLog(id,user,role,activity));

                    check = true;

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
                    mainContainer1.getChildren().add(noDataLabel);
                } else {
                    // Create panes for each record
                    for (ActivityLog activityLog : activityLogs) {
                       createDataPane(activityLog.getId(),activityLog.getUser(),activityLog.getRole(),activityLog.getActivity());
                    }
                }
            });
        }).start();
    }

    private void clearDataPane1() {
        mainContainer1.getChildren().clear();
        mainContainer1.setPrefHeight(0); // Reset height
    }

    private void createDataPane(int id, String userName, String role, String activity) {
        AnchorPane dataPane = createStyledDataPane1(id);
        Label ID = createStyledLabel1(String.valueOf(id), 15, 23, 291);
        Label UserName = createStyledLabel1(userName, 97, 23, 291);
        Label Role = createStyledLabel1(role, 250, 23, 291);
        Label activityL = createStyledLabel1(activity,400,23,400);

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
        dataPane.getChildren().addAll(ID, UserName, Role,activityL);
        int index = mainContainer1.getChildren().size();
        dataPane.setLayoutY(index * 90);
        mainContainer1.getChildren().add(dataPane);
        mainContainer1.setPrefHeight((index + 1) * 90);
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
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//        alert.setTitle("Confirmation to Delete");
//        alert.setContentText("Are you sure you want to delete?");
//        Optional<ButtonType> result = alert.showAndWait();
//
//        if (result.isPresent() && result.get() == ButtonType.OK) {
//            database.deleteUser(id);
//            loadAllRecords(loadRecordsQuery);
//            showAlert("Success",null,"Delete successfully!", Alert.AlertType.INFORMATION);
//        }
    }
    private void editAction(int id) throws SQLException {
//        animatePane(editUserPane,0,900,200);
//        // Fetch data from the database
//        String userName = database.getValue("SELECT username FROM users WHERE id = " + id).trim();
//        String Role = database.getValue("SELECT role FROM users WHERE id = " + id).trim();
//        ID = id;
//        String email = database.getValue("SELECT email FROM users WHERE id = " + id).trim();
//        String role = roleLabel.getText();
//
//        OriginalUsername = userName;
//        OriginalEmail = email;
//
//        if (role.equalsIgnoreCase("Admin")){
//            rolesCombo1.getItems().setAll("Librarian", "Student");
//        }else {
//            rolesCombo1.getItems().setAll("Admin", "Librarian", "Student");
//        }
//
//
//        usernameFIeld1.setText(userName);
//        rolesCombo1.setValue(Role);
//        emailField1.setText(email);
    }
    private AnchorPane createStyledDataPane1(int ID) {
        AnchorPane dataPane = new AnchorPane();
        dataPane.setPrefSize(700, 68);
        dataPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 6, 0, 2, 2);");
        dataPane.setOnMouseEntered(event -> dataPane.setStyle("-fx-background-color: #f0f0f0; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 8, 0, 4, 4);"));
        dataPane.setOnMouseExited(event -> dataPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 6, 0, 2, 2);"));
        //dataPane.setOnMousePressed(event -> handleEditButtonAction(airportCode));
        return dataPane;
    }
    private Label createStyledLabel1(String text, double x, double y, double width) {
        Label label = new Label(text);
        label.setLayoutX(x);
        label.setLayoutY(y);
        label.setPrefWidth(width);
        label.setStyle("-fx-font-size: 16px; -fx-font-family: 'Arial'; -fx-text-fill: #333333;");
        return label;
    }





    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadAllRecords(query);
        loadAllRecords1("SELECT * FROM activity_logs");
    }
}
