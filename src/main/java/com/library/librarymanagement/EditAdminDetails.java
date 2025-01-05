package com.library.librarymanagement;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class EditAdminDetails {

    @FXML
    private PasswordField confirmPasswordField1,passwordFieldAdd1;

    @FXML
    private TextField emailField1,usernameFIeld1;

    @FXML
    private Button saveBtn1;

    public void setFields(String username, String email){
        this.usernameFIeld1.setText(username);
        this.emailField1.setText(email);

    }
    @FXML
    private void saveEditedUser(){

    }
}
