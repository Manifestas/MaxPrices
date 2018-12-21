package com.company.exceptions;

public class FileChoosingInterruptedException extends Exception{

    public FileChoosingInterruptedException() {
    }

    public FileChoosingInterruptedException(String message) {
        super(message);
    }
}
