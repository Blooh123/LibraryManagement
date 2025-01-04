package com.library.librarymanagement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class BorrowingBook {

    @FXML
    private TextField bookTitleField,dueDateTextField;
    @FXML
    private TextField bookAuthorField;
    @FXML
    private TextField quantityField;

    @FXML
    private void handleConfirmBorrow(ActionEvent event) {
        System.out.println("Hello");
        String bookTitle = bookTitleField.getText();
        String bookAuthor = bookAuthorField.getText();
        String quantity = quantityField.getText();

        if (bookTitle.isEmpty() || bookAuthor.isEmpty() || quantity.isEmpty()) {
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
