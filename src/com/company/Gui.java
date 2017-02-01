package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class Gui implements ActionListener{

    private JButton loadButton;
    private JButton processButton;
    private JButton showButton;
    private JButton exitButton;

    private File tableFile;
    private JTextArea textArea;
    private ExcelFile excelFile;

    public void initUi() {
        JFrame frame = new JFrame("Max price");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        initButtons(panel);
        panel.setLayout(new GridLayout(10, 1, 5, 15));

        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        writeInstruction(textArea);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        frame.getContentPane().add(BorderLayout.CENTER, scrollPane);
        frame.getContentPane().add(BorderLayout.EAST, panel);

        frame.setSize(640, 480);
        frame.setVisible(true);
    }

    private void initButtons(JPanel panel) {
        loadButton = new JButton("Загрузить таблицу");
        processButton = new JButton("Поставить цены");
        showButton = new JButton("Показать таблицу");
        exitButton = new JButton("Выход");

        loadButton.addActionListener(this);
        processButton.addActionListener(this);
        showButton.addActionListener(this);
        exitButton.addActionListener(this);

        panel.add(loadButton);
        panel.add(processButton);
        panel.add(showButton);
        panel.add(exitButton);
    }

    private void writeInstruction(JTextArea textArea) {
        StringBuilder builder = new StringBuilder();
        builder.append("1. В \"этикетках\" загрузить нужную переоценку.\n");
        builder.append("2. .\n");
        builder.append("3. Экспортировать полученное в таблицу Excel.\n");
        builder.append("4. Нажать кнопку \"Загрузить таблицу\".\n");
        builder.append("5. Выбрать ранее сохранненую таблицу.\n\n\n");
        textArea.setText(builder.toString());
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == loadButton) {
            loadTable();
        } else if (actionEvent.getSource() == processButton) {
            process();
        } else if (actionEvent.getSource() == showButton) {

        } else if (actionEvent.getSource() == exitButton) {
            System.exit(0);
        }
    }

    private void loadTable() {
        JFileChooser fileChooser = new JFileChooser();
        int retValue = fileChooser.showDialog(null, "Открыть файл");
        if (retValue == JFileChooser.APPROVE_OPTION) {
            tableFile = fileChooser.getSelectedFile();
            try {
                excelFile = new ExcelFile(tableFile);
            } catch (IOException e) {
                log("Невозможно открыть файл.");
            }
            log("Выбран файл " + tableFile.getName());
        } else {
            log("Выбор файла прерван.");
        }
    }

    private void process() {
        if (excelFile == null) {
            log("Сначала необходимо загрузить таблицу.");
        } else {
            try {
                excelFile.removeDuplicates();
            } catch (IOException e) {
                log("Невозможно записать файл, возможно он открыт другой программой");
            }
        }
    }

    public void log(String msg) {
        textArea.append(msg + "\n");
    }

    public File getTableFile() {
        return tableFile;
    }
}
