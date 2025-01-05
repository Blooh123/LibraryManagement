package com.library.librarymanagement.DB;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.sql.*;

public class Database {
    static Connection connection;
    static Statement statement;
    static String DB_NAME = "secureLibrary";
    static String DB_URL = "jdbc:mysql://localhost/";
    static String USER = "root";
    static String PASS = "";
    static String value;
    static Object[][] list;
    static Object[][] view;

    public void display(SQLException e) {
        System.out.println("SQLEXCEPTION " + e.getMessage());
        System.out.println("SQLSTATE " + e.getSQLState());
        System.out.println("VENDORERROR " + e.getErrorCode());
    }
    public void addDefaultAdmin(){
        connectToDB();
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users(username, password, role,email) VALUES('AdminDef', SHA2('12345', 256), 'Super Admin', 'ddtiongson00006@usep.edu.ph')", PreparedStatement.RETURN_GENERATED_KEYS)){
            preparedStatement.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public Database() {
        try {
            createDB();
        } catch (Exception e) {
            System.err.println("Unable to find Load");
            System.exit(1);
        }
    }

    public void connectToDB() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/secureLibrary", "root", "");
            statement = connection.createStatement();
        } catch (SQLException e) {
            display(e);
        }
    }

    public void createDB() {
       // connectToDB();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
            String sql = "CREATE DATABASE if not exists secureLibrary";
            stmt.executeUpdate(sql);
            System.out.println("Database created successfully...");
            // Increase max_allowed_packet to 16 MB (16 * 1024 * 1024)
                String increaseMaxPacketSizeSQL = "SET GLOBAL max_allowed_packet = 16777216"; // 16 MB
                stmt.executeUpdate(increaseMaxPacketSizeSQL);
                System.out.println("max_allowed_packet size increased to 16 MB");

                conn.close();
                stmt.close();
            createTables();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTables() {
        String URL = "jdbc:mysql://localhost/secureLibrary";
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            Statement stmt = conn.createStatement();

            // User table
            String users = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(255) NOT NULL UNIQUE," +
                    "password VARCHAR(255) NOT NULL," +
                    "role VARCHAR(50) NOT NULL," +
                    "email VARCHAR(255)" +
                    ")";
            stmt.executeUpdate(users);

            // Books table
            String books = "CREATE TABLE IF NOT EXISTS books (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "title VARCHAR(255) NOT NULL," +
                    "author VARCHAR(255) NOT NULL," +
                    "genre VARCHAR(100)," +
                    "availability BOOLEAN DEFAULT TRUE," +
                    "book_cover longblob," +
                    "stock int(11)" +
                    ")";
            stmt.executeUpdate(books);

            // Borrow Records table
            String borrowRecords = "CREATE TABLE IF NOT EXISTS borrow_records (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "book_id varchar (255)," +
                    "user_id varchar (255)," +
                    "borrow_date DATE NOT NULL," +
                    "quantity int(11)," +
                    "due_date DATE NOT NULL," +
                    "return_date DATE," +
                    "fine DECIMAL(10, 2) DEFAULT 0," +
                    "en_key varchar(255)" +
                    ")";

            stmt.executeUpdate(borrowRecords);

            String confirmation = "CREATE TABLE IF NOT EXISTS confirmation (" +
                    "email VARCHAR(255)," +
                    "code varchar(11))";

            stmt.executeUpdate(confirmation);
            conn.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getRowCount(ResultSet resultSet) throws SQLException {
        int rowCount;
        int currentRow = resultSet.getRow();
        resultSet.last();
        rowCount = resultSet.getRow();
        if (currentRow == 0) {
            resultSet.beforeFirst();
        } else {
            resultSet.absolute(currentRow);
        }
        return rowCount;
    }

    public static Object[][] getList(String query) {
        try {
            ResultSet rs = statement.executeQuery(query);
            int numRows = getRowCount(rs);
            int numColumns = rs.getMetaData().getColumnCount();
            list = new Object[numRows][numColumns];
            int rowIndex = 0;
            while (rs.next()) {
                for (int colIndex = 0; colIndex < numColumns; colIndex++) {
                    list[rowIndex][colIndex] = rs.getObject(colIndex + 1);
                }
                rowIndex++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public String getValue(String query) throws SQLException {
        connectToDB();
        try {
            ResultSet rs = statement.executeQuery(query);
            if (rs.next()) {
                value = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            connection.close();
            statement.close();
        }
        return value;
    }


    //mao ni mag delete if ma verify ang user or Admin
    public void deleteCode(String email, String code) throws SQLException {
        connectToDB();
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM confirmation WHERE email = ? AND code = ?")){
            preparedStatement.setString(1,email);
            preparedStatement.setString(2,code);
            preparedStatement.executeUpdate();
            System.out.println("Verification Code delete in database!");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connection.close();
        }
    }

    public Blob getImage(String query) throws SQLException {
        connectToDB();
        Blob image = null;
        try {
            ResultSet rs = statement.executeQuery(query);
            if (rs.next()) {
                image = rs.getBlob("book_cover");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            connection.close();
            statement.close();
        }
        return image;
    }

    public static void prepare(String query, String... params) throws SQLException {
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                pstmt.setString(i + 1, params[i]);
            }
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            connection.close();
        }
    }

    public void addBook(String title, String author, String genre, boolean availability, byte[] bookCover, int stock) throws SQLException {
        connectToDB();
        String query = "INSERT INTO books (title, author, genre, availability, book_cover, stock) VALUES (?, ?, ?, ?, ?,?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, author);
            preparedStatement.setString(3, genre);
            preparedStatement.setBoolean(4, availability);
            preparedStatement.setBytes(5, bookCover); // Add the image as a byte array
            preparedStatement.setInt(6,stock);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }
    public void updateBook(int id, String title, String author, String genre, boolean availability, byte[] bookCover, int stock){
        connectToDB();
        System.out.println("STOCK: " + stock);
        String query = "UPDATE books SET title = ?, author = ?, genre = ?, availability = ?, book_cover = ?, stock = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setString(1,title);
            preparedStatement.setString(2,author);
            preparedStatement.setString(3,genre);
            preparedStatement.setBoolean(4,availability);
            preparedStatement.setBytes(5,bookCover);
            preparedStatement.setInt(7,id);
            preparedStatement.setInt(6,stock);

            preparedStatement.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static Object[][] viewBooks() {
        String query = "SELECT * FROM books";
        return getList(query);
    }

    public void deleteBook(int bookId) {
        connectToDB();
        String query = "DELETE FROM books WHERE id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, bookId);
            pstmt.executeUpdate();
            System.out.println("Book deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void deleteUser(int id) throws SQLException {
        connectToDB();
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM users WHERE id = ?")){
            preparedStatement.setInt(1,id);
            preparedStatement.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connection.close();
        }
    }
    public  void addUser(String userName, String password, String role, String email) throws SQLException {
        connectToDB();
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users(username, password, role, email) VALUES(?,SHA2(?,256),?,?)",PreparedStatement.RETURN_GENERATED_KEYS)){
            preparedStatement.setString(1,userName);
            preparedStatement.setString(2,password);
            preparedStatement.setString(3,role);
            preparedStatement.setString(4,email);
            preparedStatement.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
            Platform.runLater(()->{
                Alert alert =new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Something went wrong!");
                alert.showAndWait();
            });
        }finally {
            connection.close();
        }
    }
    public void updateUser(int id, String userName, String password, String role, String email) throws SQLException{
        connectToDB();
        try(PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET username = ?, password = SHA2(?,256), role = ?, email = ? WHERE id = ?")) {
            preparedStatement.setString(1,userName);
            preparedStatement.setString(2,password);
            preparedStatement.setString(3,role);
            preparedStatement.setString(4,email);
            preparedStatement.setInt(5,id);
            preparedStatement.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connection.close();
        }
    }
    public void updateAdmin(int id, String userName, String password, String email) throws SQLException{
        connectToDB();
        try(PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET username = ?, password = SHA2(?,256), email = ? WHERE id = ?")) {
            preparedStatement.setString(1,userName);
            preparedStatement.setString(2,password);
            preparedStatement.setString(3,email);
            preparedStatement.setInt(4,id);
            preparedStatement.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connection.close();
        }
    }
    public void updateAdmin(int id, String userName, String email) throws SQLException{
        connectToDB();
        try(PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET username = ?, email = ? WHERE id = ?")) {
            preparedStatement.setString(1,userName);
            preparedStatement.setString(2,email);
            preparedStatement.setInt(3,id);
            preparedStatement.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connection.close();
        }
    }

    public void updateUser(int id, String username, String role, String email) throws SQLException {
        connectToDB();
        try(PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET username = ?,role = ?, email = ? WHERE id = ?")) {
            preparedStatement.setString(1,username);
            preparedStatement.setString(2,role);
            preparedStatement.setString(3,email);
            preparedStatement.setInt(4,id);
            preparedStatement.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connection.close();
        }
    }

    public boolean checkIfUsernameExists(String username) throws SQLException{
        connectToDB();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ?")){
            preparedStatement.setString(1, username);
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()){
                    return true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connection.close();
        }
        return false;
    }

    public boolean checkIfEmailExists(String email) throws SQLException {
        connectToDB();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE email = ?")){
            preparedStatement.setString(1, email);
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()){
                    return true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connection.close();
        }
        return false;
    }
    public boolean checkIfActualUser(int id, String username, String email) throws SQLException{
        connectToDB();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE id = ? AND username = ? AND email = ?")){
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2,username);
            preparedStatement.setString(3,email);
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()){
                    return true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connection.close();
        }
        return false;
    }
    public boolean checkIfUserActualEmail(int id, String email) throws SQLException {
        connectToDB();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE id = ? AND email = ?")){
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2,email);
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()){
                    return true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connection.close();
        }
        return false;
    }

}
