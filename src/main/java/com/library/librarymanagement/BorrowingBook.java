package com.library.librarymanagement;

import com.library.librarymanagement.DB.Database;
import com.library.librarymanagement.SecurityUtils.SecurityUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Base64;

public class BorrowingBook {
    public SecurityUtils securityUtils = new SecurityUtils();

    @FXML
    private TextField bookTitleField;
    @FXML
    private TextField bookAuthorField;
    @FXML
    private TextField quantityField;
    @FXML
    private DatePicker duedate;
    @FXML
    private Label userId;


    private Database database = new Database();

    public void setTitleAndAuthor(String title, String author, String id){
        bookTitleField.setText(title);
        bookAuthorField.setText(author);
        userId.setText(id);
    }

    private StudentDashBoard parentController;

    public void setParentController(StudentDashBoard parentController) {
        this.parentController = parentController;
    }


    @FXML
    private void handleConfirmBorrow(ActionEvent event) throws SQLException {
        String bookTitle = bookTitleField.getText();
        String bookAuthor = bookAuthorField.getText();
        String quantity = quantityField.getText();

        int currentStock = Integer.parseInt(database.getValue("SELECT stock FROM books WHERE title = '" + bookTitle + "'"));

        if (bookTitle.isEmpty() || bookAuthor.isEmpty() || quantity.isEmpty() || duedate.getValue() == null) {
            showAlert("Error", null, "Please fill out all fields.", Alert.AlertType.ERROR);
            return;
        }

        String studentID = userId.getText();
        String bookID = database.getValue("SELECT id FROM books WHERE title = '" + bookTitle + "'");

        int quantity1 = Integer.parseInt(quantity);
        if (quantity1 > currentStock) {
            showAlert("Invalid quantity", null, "Current number of copies: " + currentStock, Alert.AlertType.INFORMATION);
            return;
        }

        LocalDate currentDate = LocalDate.now();

        if (!duedate.getValue().isAfter(currentDate)) {
            showAlert("Error", null, "Please choose a date after the current date", Alert.AlertType.INFORMATION);
            return;
        }

        String queryForUpdateBook = "UPDATE books " +
                "SET stock = stock - ?, " +
                "availability = CASE WHEN stock = 0 THEN 0 ELSE availability END " +
                "WHERE title = ? AND stock > 0";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/secureLibrary", "root", "");
             PreparedStatement pstmt = conn.prepareStatement(queryForUpdateBook)) {
            pstmt.setInt(1, quantity1);
            pstmt.setString(2, bookTitle);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", null, "An error occurred while updating the book stock.", Alert.AlertType.ERROR);
            return;
        }

        String queryForInsertBorrow = "INSERT INTO borrow_records (book_id, user_id, borrow_date, quantity, due_date, en_key) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/secureLibrary", "root", "");
             PreparedStatement preparedStatement = connection.prepareStatement(queryForInsertBorrow)) {

            // Generate AES key for encrypting sensitive data
            SecretKey aesKey = SecurityUtils.generateAESKey();

            // Encode the AES key as Base64 string (optional, for debugging or future purposes)
            String aesKeyBase64 = SecurityUtils.keyToString(aesKey);

            // Derive the KEK (Key Encryption Key) using a password and salt
            String kekPassword = "Hello123"; // Use a secure password
            byte[] kekSalt = "hedged".getBytes(StandardCharsets.UTF_8); // Use a unique and consistent salt
            SecretKey kek = SecurityUtils.deriveKEK(kekPassword, kekSalt);

            // Wrap the AES key using the derived KEK
            String wrappedKey = SecurityUtils.wrapKey(aesKey, kek);

            // Encrypt sensitive data using the AES key
            String encryptedBookID = SecurityUtils.encryptAES(bookID, aesKey);
            String encryptedStudentID = SecurityUtils.encryptAES(studentID, aesKey);

            // Set parameters in the query
            preparedStatement.setString(1, encryptedBookID);
            preparedStatement.setString(2, encryptedStudentID);
            preparedStatement.setString(3, currentDate.toString());
            preparedStatement.setInt(4, quantity1);
            preparedStatement.setString(5, duedate.getValue().toString());
            preparedStatement.setString(6, wrappedKey);

            // Execute the query
            preparedStatement.executeUpdate();
            showAlert("Success", null, "Book borrowed successfully!", Alert.AlertType.INFORMATION);

            if (parentController != null) {
                parentController.loadAvailableBooks();
            }

            // Close the current stage
            Stage currentStage = (Stage) userId.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", null, "An error occurred while borrowing the book.", Alert.AlertType.ERROR);
        }

    }



    // This method is called when the "Cancel" button is clicked
    @FXML
    private void handleCloseDialog(ActionEvent event) {
        // Call the reloadRecords method in the parent controller
        if (parentController != null) {
            parentController.loadAvailableBooks();
        }
        Stage currentStage = (Stage) userId.getScene().getWindow();
        currentStage.close();


    }

    // Utility method to show alerts
    private void showAlert(String title, String header, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
