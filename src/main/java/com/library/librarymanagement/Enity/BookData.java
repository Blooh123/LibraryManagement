package com.library.librarymanagement.Enity;

public class BookData {
    public int id;
    public String title;
    public String author;
    public String genre;
    public boolean availability;

    public BookData(int id, String title, String author, String genre, boolean availability){
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.availability = availability;
    }
}
