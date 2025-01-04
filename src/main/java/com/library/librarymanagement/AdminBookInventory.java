package com.library.librarymanagement;

import com.library.librarymanagement.DB.Database;
import com.library.librarymanagement.Enity.BookData;
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
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
//import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils; // For converting between JavaFX Image and BufferedImage
import java.awt.image.BufferedImage;    // BufferedImage class
import javax.imageio.ImageIO;           // For writing the image to a stream
import java.io.ByteArrayOutputStream;   // For converting the image to a byte array
import java.io.IOException;


public class AdminBookInventory implements Initializable {
    //DB stuff
    private final String DB_URL = "jdbc:mysql://localhost/secureLibrary";
    private final String USER = "root";
    private final String PASS = "";
    Database database = new Database();
    private String selectQuery = "SELECT * FROM books";
    private int bookID;
    ////

    @FXML
    private Button refreshBtn,searchBtn,addBtn;
    @FXML
    private ImageView coverImage,coverImage1;
    @FXML
    private AnchorPane addBookPane,editBookPane;
    @FXML
    private ComboBox<String> availabilityCombo,availabilityCombo1;
    @FXML
    private TextField searchField,bookTitleField,bookAuthorField,bookGenreField,bookTitleField1,bookAuthorField1,bookGenreField1,bookStock1,bookStock;
    @FXML
    private AnchorPane mainContainer;

    @FXML
    void addBookAction(ActionEvent event) {
        mainContainer.setDisable(true);
        addBtn.setDisable(true);
        refreshBtn.setDisable(true);
        searchBtn.setDisable(true);
        searchField.setDisable(true);
        animatePane(addBookPane,0,900,200);
    }
    @FXML
    void closeAddbookPane(MouseEvent event){
        animatePane(addBookPane,0,-900,200);
        mainContainer.setDisable(false);
        addBtn.setDisable(false);
        refreshBtn.setDisable(false);
        searchBtn.setDisable(false);
        searchField.setDisable(false);

    }
    @FXML
    void closeEditPane(MouseEvent event){
        animatePane(editBookPane,0,-900,200);
        mainContainer.setDisable(false);
        addBtn.setDisable(false);
        refreshBtn.setDisable(false);
        searchBtn.setDisable(false);
        searchField.setDisable(false);
    }

    @FXML
    void refreshPage(ActionEvent event) {
        loadAllRecords(selectQuery);
    }

    @FXML
    void searchAction(ActionEvent event) {
        String queryField = searchField.getText();
        loadAllRecords("SELECT * FROM books WHERE title = '" + queryField + "' OR genre = '" + queryField + "' OR author = '" + queryField + "'");
    }

    @FXML
    private void SaveBook(ActionEvent event) {
        try {
            // Validate input fields
            if (!validateBookFields(bookTitleField.getText(), bookAuthorField.getText(), bookGenreField.getText(), availabilityCombo.getValue(),bookStock.getText())) {
                return;
            }

            // Determine availability
            // Determine availability
            boolean isAvailable = Integer.parseInt(bookStock.getText()) == 0 ? false : true;

            if (isAvailable) {availabilityCombo.setValue("Available");}else availabilityCombo.setValue("Not available");

            // Convert image to byte array
            byte[] bookCover = processCoverImage(coverImage.getImage());
            if (bookCover == null) return; // If image size check fails, return early

            // Add book to the database
            database.addBook(bookTitleField.getText(), bookAuthorField.getText(), bookGenreField.getText(), isAvailable, bookCover,Integer.parseInt(bookStock.getText()));

            // Show success message and reset fields
            showAlert("Success", null, "New book added successfully!", Alert.AlertType.INFORMATION);
            resetAddBookForm();

        } catch (Exception e) {
            showAlert("Error", null, "Failed to save the book.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void saveEditedBook(ActionEvent event) {
            try {
                // Validate input fields
                if (!validateBookFields(bookTitleField1.getText(), bookAuthorField1.getText(), bookGenreField1.getText(), availabilityCombo1.getValue(),bookStock1.getText())) {
                    return;
                }
                // Determine availability
                boolean isAvailable = Integer.parseInt(bookStock1.getText()) == 0 ? false : true;

                if (isAvailable) {availabilityCombo1.setValue("Available");}else availabilityCombo1.setValue("Not available");

                // Convert image to byte array
                byte[] bookCover = processCoverImage(coverImage1.getImage());
                if (bookCover == null) return; // If image size check fails, return early

                // Update book in the database
                database.updateBook(bookID, bookTitleField1.getText(), bookAuthorField1.getText(), bookGenreField1.getText(), isAvailable, bookCover,Integer.parseInt(bookStock1.getText()));

                // Show success message and reset fields
                showAlert("Success", null, "Book updated successfully!", Alert.AlertType.INFORMATION);
                resetEditBookForm();

            } catch (Exception e) {
                showAlert("Error", null, "Failed to update the book.", Alert.AlertType.ERROR);
                e.printStackTrace();
            }
    }

    /**
     * Validates the input fields for book details.
     */
    private boolean validateBookFields(String title, String author, String genre, String availability, String stock) {
        if (title.isBlank() || author.isBlank() || genre.isBlank() || availability == null || stock.isBlank()) {
            showAlert("Unable to proceed", null, "Please fill out all the fields.", Alert.AlertType.INFORMATION);
            return false;
        }
        if (!isInteger(stock)){
            showAlert("Unable to proceed", null,"Please enter stock.", Alert.AlertType.INFORMATION);
            return false;
        }
        return true;
    }
    public boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }


    /**
     * Converts the given JavaFX image to a byte array, checks its size, and returns it.
     */
    private byte[] processCoverImage(Image image) {
        if (image == null) return null;

        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] imageData = baos.toByteArray();

            return imageData;

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", null, "Failed to process the cover image.", Alert.AlertType.ERROR);
            return null;
        }
    }

    /**
     * Resets the add book form fields.
     */
    private void resetAddBookForm() {
        bookTitleField.clear();
        bookAuthorField.clear();
        bookGenreField.clear();
        bookStock.clear();

        coverImage.setImage(null);
        animatePane(addBookPane, 0, -900, 400);
        mainContainer.setDisable(false);
        addBtn.setDisable(false);
        refreshBtn.setDisable(false);
        searchBtn.setDisable(false);
        searchField.setDisable(false);
        clearDataPane();
        loadAllRecords(selectQuery);
    }

    /**
     * Resets the edit book form fields.
     */
    private void resetEditBookForm() {
        bookTitleField1.clear();
        bookAuthorField1.clear();
        bookGenreField1.clear();
        bookStock1.clear();
        coverImage1.setImage(null);
        animatePane(editBookPane, 0, -900, 400);
        mainContainer.setDisable(false);
        addBtn.setDisable(false);
        refreshBtn.setDisable(false);
        searchBtn.setDisable(false);
        searchField.setDisable(false);
        clearDataPane();
        loadAllRecords(selectQuery);
    }


    @FXML
    private void uploadImage(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Cover Image");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(coverImage.getScene().getWindow());

        if (selectedFile != null) {
            try {

                Image image = new Image(selectedFile.toURI().toString());// Set the image to the ImageView
                coverImage.setImage(image);

            } catch (Exception e) {
                showAlert("Error", null, "Could not load the selected image!", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Info", null, "No image was selected.", Alert.AlertType.INFORMATION);
        }
    }
    @FXML
    private void uploadImage1(ActionEvent event){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Cover Image");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(coverImage1.getScene().getWindow());

        if (selectedFile != null) {
            try {

                Image image = new Image(selectedFile.toURI().toString());// Set the image to the ImageView
                coverImage1.setImage(image);

            } catch (Exception e) {
                showAlert("Error", null, "Could not load the selected image!", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Info", null, "No image was selected.", Alert.AlertType.INFORMATION);
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setButtonIcon(searchBtn, "/Icons/SearchIcon.png", 35, 35);
        setButtonIcon(addBtn, "/Icons/PlusIcon.png", 35, 35);
        setButtonIcon(refreshBtn, "/Icons/ReloadIcon.png", 35, 35);

        availabilityCombo.getItems().addAll("Available","Not Available");
        availabilityCombo1.getItems().addAll("Available","Not Available");

        addBookPane.setLayoutY(-800);
        editBookPane.setLayoutY(-800);

        loadAllRecords(selectQuery);
    }


    private void loadAllRecords(String query) {
        new Thread(() -> {
            //String query = "SELECT * FROM users";
            boolean check = false;
            List<BookData> bookData = new ArrayList<>();

            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
                 PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    check = true;
                    boolean availability = resultSet.getInt("stock") == 0 ? false : true;
                    bookData.add(new BookData(resultSet.getInt("id"), resultSet.getString("title"),resultSet.getString("author"),resultSet.getString("genre"),availability));

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
                    for (BookData BookData : bookData) {
                        createDataPane(BookData.id, BookData.title, BookData.author,BookData.genre,BookData.availability);
                    }
                }
            });
        }).start();
    }
    private void createDataPane(int id, String title, String author, String genre, boolean availability) {
        AnchorPane dataPane = createStyledDataPane(id);
        Label ID = createStyledLabel(String.valueOf(id), 20, 23, 291);
        Label Title = createStyledLabel(title, 230, 23, 291);
        Label Author = createStyledLabel(author, 479, 23, 291);
        Label Genre = createStyledLabel(genre,700,23,291);

        Button editButton = createButton(id, "/Icons/EditIcon.png");
        editButton.setLayoutX(950);
        editButton.setLayoutY(15);
        Button deleteButton = createButton(id, "/Icons/DeleteIcon.png");
        deleteButton.setLayoutY(15);
        deleteButton.setLayoutX(1000);

        deleteButton.setOnAction(event -> {
            deleteAction(id);
        });

        editButton.setOnAction(event -> {
            try {
                editAction(id);
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        });

        dataPane.setOnMousePressed(event -> {
            // Uncomment and implement any click handling logic for the row
            // handleEditButtonAction(id, userName, role);
        });
        dataPane.getChildren().addAll(ID, Title, Author,Genre,editButton,deleteButton);
        int index = mainContainer.getChildren().size();
        dataPane.setLayoutY(index * 90);
        mainContainer.getChildren().add(dataPane);
        mainContainer.setPrefHeight((index + 1) * 90);
    }
    private void editAction(int id) throws SQLException, IOException {
        mainContainer.setDisable(true);
        addBtn.setDisable(true);
        refreshBtn.setDisable(true);
        searchBtn.setDisable(true);
        searchField.setDisable(true);
        animatePane(editBookPane, 0, 900, 400);

        bookID = id;

        // Fetch book details from the database
        String bookAuthor = database.getValue("SELECT author FROM books WHERE id = " + id);
        String bookTitle = database.getValue("SELECT title FROM books WHERE id = " + id);
        String bookGenre = database.getValue("SELECT genre FROM books WHERE id = " + id);
        String bookAvailability = database.getValue("SELECT availability FROM books WHERE id = " + id); // Expecting "1" or "0"
        Blob bookCover = database.getImage("SELECT book_cover FROM books WHERE id = " + id);
        String stock = database.getValue("SELECT stock FROM books WHERE id = " + id);

        // Handle book cover image
        if (bookCover != null) {
            try (InputStream inputStream = bookCover.getBinaryStream()) {
                // Load the image with its original size
                Image image = new Image(inputStream);
                coverImage1.setImage(image);

                // Ensure the ImageView scales the image correctly
                coverImage1.setFitWidth(200); // Set your desired width
                coverImage1.setFitHeight(286); // Set your desired height
                coverImage1.setPreserveRatio(true); // Maintain aspect ratio
                coverImage1.setSmooth(true); // Enable smooth scaling
            } catch (SQLException | IOException e) {
                e.printStackTrace();
                showAlert("Error", null, "Failed to load the book cover image.", Alert.AlertType.ERROR);
            }
        } else {
            coverImage1.setImage(null); // Clear the ImageView if no image is found
        }

        // Populate fields
        bookTitleField1.setText(bookTitle);
        bookAuthorField1.setText(bookAuthor);
        bookGenreField1.setText(bookGenre);

        // Update availability logic
        availabilityCombo1.setValue(
                bookAvailability.equalsIgnoreCase("1") ? "Available" : "Not Available"
        );

        // Update stock field
        bookStock1.setText(stock);

    }

    private void deleteAction(int id){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation to Delete");
        alert.setContentText("Are you sure you want to delete?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            database.deleteBook(id);
            showAlert("Success", null, "Deleted successfully!", Alert.AlertType.INFORMATION);
            loadAllRecords(selectQuery);
        }

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
    private Label createStyledLabel(String text, double x, double y, double width) {
        Label label = new Label(text);
        label.setLayoutX(x);
        label.setLayoutY(y);
        label.setPrefWidth(width);
        label.setStyle("-fx-font-size: 16px; -fx-font-family: 'Arial'; -fx-text-fill: #333333;");
        return label;
    }
    private void clearDataPane() {
        mainContainer.getChildren().clear();
        mainContainer.setPrefHeight(0); // Reset height
    }
    private void animatePane(Node node, double x, double y, double millis){
        TranslateTransition translateTransition = new TranslateTransition( Duration.millis(millis),node);
        translateTransition.setByY(y);
        translateTransition.setByX(x);
        translateTransition.play();
    }
    private void showAlert(String Title, String Header, String Message, Alert.AlertType alertType){
        Alert alert = new Alert(alertType);
        alert.setTitle(Title);
        alert.setHeaderText(Header);
        alert.setContentText(Message);
        alert.showAndWait();
    }
}
