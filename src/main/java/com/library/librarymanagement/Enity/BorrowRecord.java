package com.library.librarymanagement.Enity;

public class BorrowRecord {
    private int id;
    private int book_id;
    private int user_id;
    private String borrow_date;
    private int quantity;
    private String due_date;
    private String return_date;
    private double fine;

    // Constructor
    public BorrowRecord(int id, int book_id, int user_id, String borrow_date, int quantity, String due_date, String return_date, double fine) {
        this.id = id;
        this.book_id = book_id;
        this.user_id = user_id;
        this.borrow_date = borrow_date;
        this.quantity = quantity;
        this.due_date = due_date;
        this.return_date = return_date;
        this.fine = fine;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getBookId() {
        return book_id;
    }

    public int getUserId() {
        return user_id;
    }

    public String getBorrowDate() {
        return borrow_date;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getDueDate() {
        return due_date;
    }

    public String getReturnDate() {
        return return_date;
    }

    public double getFine() {
        return fine;
    }
}

