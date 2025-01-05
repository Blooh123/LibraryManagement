package com.library.librarymanagement;

import com.library.librarymanagement.DB.Database;

import com.library.librarymanagement.Enity.ActivityLog;
import com.library.librarymanagement.Enity.BorrowRecord;
import com.library.librarymanagement.SecurityUtils.SecurityUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.util.Base64;
import java.util.ResourceBundle;

public class ReturnBooks implements Initializable {

    private final String DB_URL = "jdbc:mysql://localhost/securelibrary";
    private final String USER = "root";
    private final String PASS = "";


    @FXML
    private TextField bookTitleField;
    @FXML
    private TextField usernamefield;
    @FXML
    private TextField quantityField,fineFIeld;
    @FXML
    private Button proceed, confirmReceived;


    private String recordID;
    private int BookQuan;
    private String BookID;
    private String BookTitle;
    private Database database = new Database();

    private LibrarianMonitorBooks parentController;

    public void setParentController(LibrarianMonitorBooks parentController) {
        this.parentController = parentController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
    @FXML
    void handleCloseDialog(ActionEvent event) {
        Stage currentStage = (Stage) quantityField.getScene().getWindow();
        currentStage.close();
    }
    @FXML
    void proceed() throws SQLException {
        try {
            String username = usernamefield.getText();
            String bookTitle = bookTitleField.getText();
            String quantityT = quantityField.getText();

            String OriginalBookID = database.getValue("SELECT id FROM books WHERE title = '" + bookTitleField.getText() + "'");
            String OriginalUserID = database.getValue("SELECT id FROM users WHERE username = '" + usernamefield.getText() + "'");

            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM borrow_records");
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                boolean checker = false;
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

                        if (OriginalBookID.equalsIgnoreCase(book_id) && OriginalUserID.equalsIgnoreCase(user_id) && String.valueOf(quantity).equalsIgnoreCase(quantityT)){
                            recordID = String.valueOf(id);
                            BookQuan = quantity;
                            BookID = book_id;
                            BookTitle = bookTitle;
                            checker = true;
                            showAlert("Record found!", null, "Record found!", Alert.AlertType.INFORMATION);
                            confirmReceived.setDisable(false);
                            fineFIeld.setText(fine);
                            return;
                        }


                    } catch (Exception e) {
                        System.err.println("Error decrypting data: " + e.getMessage());
                    }
                }
                if (!checker){
                    showAlert("No record found !", null, "No record found!", Alert.AlertType.INFORMATION);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Error", null, "Something went wrong while loading data", Alert.AlertType.ERROR));
            }
        }catch (Exception e){
            e.printStackTrace();
            showAlert("Invalid", null, "Failed to proceed", Alert.AlertType.INFORMATION);
        }

    }
    @FXML
    void handleConfirmReturned(ActionEvent event) {
        try(Connection connection = DriverManager.getConnection(DB_URL,USER,PASS);
            PreparedStatement preparedStatement  = connection.prepareStatement("DELETE FROM borrow_records WHERE id = ?");
            PreparedStatement preparedStatement1 = connection.prepareStatement("UPDATE books SET stock =+ ?, availability = 1 WHERE id = ?")){
            preparedStatement.setString(1,recordID);
            preparedStatement.executeUpdate();


            preparedStatement1.setInt(1,BookQuan);
            preparedStatement1.setString(2, BookID);
            preparedStatement1.executeUpdate();
            showAlert("Success", null, "Receive successfully", Alert.AlertType.INFORMATION);
            if (parentController != null) {
                parentController.loadAllRecords("SELECT * FROM borrow_records");
            }
            database.addToActivityLog("","",BookQuan + " copies of " + BookTitle + " returned");

            Stage currentStage = (Stage) bookTitleField.getScene().getWindow();
            currentStage.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void showAlert(String title, String header, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
