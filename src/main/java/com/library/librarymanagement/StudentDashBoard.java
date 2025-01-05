package com.library.librarymanagement;

import com.library.librarymanagement.Enity.Book;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class StudentDashBoard implements Initializable {

    @FXML
    private GridPane bookGrid;
    @FXML
    private Label studentID;

    public void setID(String id){
        studentID.setText(id);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadAvailableBooks();
    }

    public void loadAvailableBooks() {

        bookGrid.getChildren().clear();
        List<Book> books = getAvailableBooksFromDatabase();

        int column = 0;
        int row = 0;

        for (Book book : books) {
            VBox bookCard = createBookCard(book);

            bookGrid.add(bookCard, column, row);
            column++;

            if (column == 3) { // 3 columns per row
                column = 0;
                row++;
            }
        }
    }

    private VBox createBookCard(Book book) {
        VBox bookCard = new VBox();
        bookCard.setSpacing(20); // Increased spacing for better readability
        bookCard.setStyle("-fx-padding: 20; -fx-background-color: #f9f9f9; "
                + "-fx-border-color: #ccc; -fx-border-radius: 10; "
                + "-fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");

        // Book cover
        ImageView bookCover = new ImageView();
        bookCover.setFitWidth(300); // Increased width for larger displays
        bookCover.setFitHeight(400); // Increased height for larger displays
        if (book.getCoverImage() != null) {
            bookCover.setImage(new Image(book.getCoverImage()));
        }

        // Book title
        Text title = new Text(book.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-fill: #333;");

        // Book author
        Text author = new Text("by " + book.getAuthor());
        author.setStyle("-fx-font-style: italic; -fx-font-size: 16px; -fx-fill: #666;");

        // Book stock
        Text stock = new Text("Available Copies: " + book.getStock());
        stock.setStyle("-fx-font-size: 14px; -fx-fill: #444;");

        // Borrow button
        Button borrowButton = new Button("Borrow");
        borrowButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20; "
                + "-fx-font-size: 16px; -fx-border-radius: 5; -fx-background-radius: 5;");

// Hover effect: Change color and scale on hover
        borrowButton.setOnMouseEntered(event -> {
            // Change color to a darker shade
            borrowButton.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-padding: 10 20; "
                    + "-fx-font-size: 16px; -fx-border-radius: 5; -fx-background-radius: 5;");

            // Animate scale to make the button appear slightly larger
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(150), borrowButton);
            scaleTransition.setToX(1.1);  // Scale up slightly
            scaleTransition.setToY(1.1);  // Scale up slightly
            scaleTransition.play();
        });

// Hover out effect: Reset to normal size and color
        borrowButton.setOnMouseExited(event -> {
            // Reset color to original
            borrowButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20; "
                    + "-fx-font-size: 16px; -fx-border-radius: 5; -fx-background-radius: 5;");

            // Animate scale to bring the button back to normal size
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(150), borrowButton);
            scaleTransition.setToX(1);  // Reset to normal size
            scaleTransition.setToY(1);  // Reset to normal size
            scaleTransition.play();
        });

// Press effect: Slightly press the button down when clicked
        borrowButton.setOnMousePressed(event -> {
            // Apply translation to simulate pressing effect
            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(100), borrowButton);
            translateTransition.setByY(5);  // Move the button 5 pixels down
            translateTransition.play();
        });

// Release effect: Reset position after pressing
        borrowButton.setOnMouseReleased(event -> {
            // Reset the button position back to normal
            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(100), borrowButton);
            translateTransition.setByY(-5);  // Move the button back up
            translateTransition.play();
        });

// Event handler for borrowing
        borrowButton.setOnAction(event -> {
            try {
                borrowBook(book);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


        // Center-align all components
        bookCard.setAlignment(Pos.CENTER);
        bookCard.getChildren().addAll(bookCover, title, author, stock,borrowButton);

        return bookCard;
    }
    private void borrowBook(Book book) throws IOException {
        openBorrowBookDetails(book);

    }

    private void openBorrowBookDetails(Book book) throws IOException {
        Stage currentStage = (Stage) bookGrid.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("BorrowingBook.fxml"));
        Parent root = loader.load();

        BorrowingBook borrowingBook  = loader.getController();
        borrowingBook.setTitleAndAuthor(book.getTitle(),book.getAuthor(),studentID.getText());

        Scene scene = new Scene(root);
        Stage newStage = new Stage();
        newStage.setScene(scene);
        newStage.setResizable(false);
        newStage.getIcons().add(new Image(getClass().getResourceAsStream("/Images/LibraryManagement.png")));
        newStage.initOwner(currentStage);
        newStage.initModality(Modality.WINDOW_MODAL);
        newStage.setTitle("Secure Library");

        // Pass the current controller to BorrowingBook
        borrowingBook.setParentController(this);

        newStage.show();
    }


    private List<Book> getAvailableBooksFromDatabase() {
        List<Book> books = new ArrayList<>();

        String query = "SELECT title, author, book_cover, stock FROM books WHERE availability = 1"; // 1 means "Available"

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/secureLibrary", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String title = rs.getString("title");
                String author = rs.getString("author");
                InputStream coverImage = rs.getBinaryStream("book_cover");
                int stock = rs.getInt("stock");

                books.add(new Book(title, author, coverImage, stock));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return books;
    }


    private void showAlert(String Title, String Header, String Message, Alert.AlertType alertType){
        Alert alert = new Alert(alertType);
        alert.setTitle(Title);
        alert.setHeaderText(Header);
        alert.setContentText(Message);
        alert.showAndWait();
    }



}
