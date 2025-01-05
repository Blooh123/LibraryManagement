package com.library.librarymanagement;

import com.library.librarymanagement.DB.Database;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class BorrowingBook {

    @FXML
    private TextField bookTitleField;
    @FXML
    private TextField bookAuthorField;
    @FXML
    private TextField quantityField;
    @FXML
    private DatePicker duedate;


    private Database database = new Database();

    public void setTitleAndAuthor(String title, String author){
        bookTitleField.setText(title);
        bookAuthorField.setText(author);
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
                "SET stock = stock - 1, " +
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

    }

    // This method is called when the "Cancel" button is clicked
    @FXML
    private void handleCloseDialog(ActionEvent event) {
        Stage currentStage = (Stage) bookTitleField.getScene().getWindow();
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
