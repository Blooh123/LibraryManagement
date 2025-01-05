package com.library.librarymanagement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class BorrowingBook {

    @FXML
    private TextField bookTitleField;
    @FXML
    private TextField bookAuthorField;
    @FXML
    private TextField quantityField;
    @FXML
    private DatePicker duedate;
    @FXML
    private void handleConfirmBorrow(ActionEvent event) {

        String bookTitle = bookTitleField.getText();
        String bookAuthor = bookAuthorField.getText();
        String quantity = quantityField.getText();
        String dueDate = duedate.getValue().toString();

        if (bookTitle.isEmpty() || bookAuthor.isEmpty() || quantity.isEmpty()||dueDate.isEmpty()) {
            showAlert("Error", null, "Please fill out all fields.", Alert.AlertType.ERROR);
            return;
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
