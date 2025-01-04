module com.library.librarymanagement {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires java.desktop;
    requires javafx.swing;

    opens com.library.librarymanagement to javafx.fxml;
    exports com.library.librarymanagement;
}