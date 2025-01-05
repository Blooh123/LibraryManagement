package com.library.librarymanagement;

public interface EmailCallback {
    void onSuccess();
    void onFailure(Exception e);
}
