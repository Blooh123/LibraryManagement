package com.library.librarymanagement.Enity;

public class ActivityLog {
    private int id;
    private String user;
    private String role;
    private String activity;

    public ActivityLog(int id, String user, String role, String activity) {
        this.id = id;
        this.user = user;
        this.role = role;
        this.activity = activity;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public String getRole() {
        return role;
    }

    public String getActivity() {
        return activity;
    }
}

