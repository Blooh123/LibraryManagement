package com.library.librarymanagement;

import com.library.librarymanagement.DB.Database;
import com.library.librarymanagement.SecurityUtils.SecurityUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

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

        if (bookTitle.isEmpty() || bookAuthor.isEmpty() || quantity.isEmpty()||duedate.getValue() == null) {
            showAlert("Error", null, "Please fill out all fields.", Alert.AlertType.ERROR);
            return;
        }
        String studentID = userId.getText();
        String bookID = database.getValue("SELECT id FROM books WHERE title = '" + bookTitle + "'");

        //String dueDate = duedate.getValue().toString();
        int quantity1 = Integer.parseInt(quantity);
        if (quantity1 > currentStock){
            showAlert("Invalid quantity", null, "Current number of copies: " + currentStock, Alert.AlertType.INFORMATION);
            return;
        }

        LocalDate currentDate = LocalDate.now();

        if (!duedate.getValue().isAfter(currentDate)){
            showAlert("Error", null, "Please choose a date after the current date", Alert.AlertType.INFORMATION);
            return;
        } else if (duedate.getValue().isEqual(currentDate)) {
            showAlert("Error", null, "Please choose a date after the current date", Alert.AlertType.INFORMATION);
            return;
        }


        String queryForUpdateBook = "UPDATE books " +
                "SET stock = stock - " + quantity1 + ", " +
                "availability = CASE WHEN stock = 0 THEN 0 ELSE availability END " +  // Only update availability when stock hits 0
                "WHERE title = ? AND stock > 0";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/secureLibrary", "root", "");
             PreparedStatement pstmt = conn.prepareStatement(queryForUpdateBook)) {
            pstmt.setString(1, bookTitleField.getText());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", null, "An error occurred while borrowing the book.", Alert.AlertType.ERROR);
        }

        String queryForInsertBorrow = "INSERT INTO `borrow_records`(`book_id`, `user_id`, `borrow_date`, `quantity`, `due_date`, `en_key`) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/secureLibrary", "root", "");
             PreparedStatement preparedStatement = connection.prepareStatement(queryForInsertBorrow, PreparedStatement.RETURN_GENERATED_KEYS)) {

            // Generate reusable keys
            SecretKey aesKey = securityUtils.generateAESKey(); // Generate AES key once for consistency
            String wrappedKey = securityUtils.wrapKey(aesKey, securityUtils.generateKEKKey()); // Wrap the AES key

            // Set parameters in the query
            preparedStatement.setString(1, securityUtils.encryptAES(bookID, aesKey)); // Encrypt book ID
            preparedStatement.setString(2, securityUtils.encryptAES(studentID, aesKey)); // Encrypt student ID
            preparedStatement.setString(3, currentDate.toString()); // Set current date
            preparedStatement.setInt(4, quantity1);
            preparedStatement.setString(5, duedate.getValue().toString()); // Set due date
            preparedStatement.setString(6, wrappedKey); // Set wrapped AES key

            preparedStatement.executeUpdate();
            showAlert("Success", null,"Borrowed Successfully!", Alert.AlertType.INFORMATION);

            if (parentController != null) {
                parentController.loadAvailableBooks();
            }

            Stage currentStage = (Stage) userId.getScene().getWindow();
            currentStage.close();

            System.out.println("BORROWED SUCCESSFULLY");
        } catch (Exception e) {
            throw new RuntimeException("Error inserting data: " + e.getMessage());
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
