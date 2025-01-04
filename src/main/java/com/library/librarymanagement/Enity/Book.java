package com.library.librarymanagement.Enity;

import java.io.InputStream;

public class Book {
    private String title;
    private String author;
    private InputStream coverImage;
    private int stock;
    public Book(String title, String author, InputStream coverImage, int stock) {
        this.title = title;
        this.author = author;
        this.coverImage = coverImage;
        this.stock = stock;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public InputStream getCoverImage() {
        return coverImage;
    }
    public int getStock(){
        return stock;
    }
}
