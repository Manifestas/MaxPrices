package com.company;

import com.company.controller.Controller;
import com.company.view.View;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        try {
            // set look and feel to system dependent
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new Controller(new View()));
    }
}
