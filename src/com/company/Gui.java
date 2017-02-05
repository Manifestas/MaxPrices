package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class Gui implements ActionListener{

    private JButton loadButton;
    private JButton duplicateButton;
    private JButton processButton;
    private JButton showButton;
    private JButton showInstruction;
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
        textArea.setMargin(new Insets(5, 5, 5, 5));
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
        duplicateButton = new JButton("Удалить дубликаты");
        processButton = new JButton("Поставить цены");
        showButton = new JButton("Показать таблицу");
        showInstruction = new JButton(("Инструкция"));
        exitButton = new JButton("Выход");

        loadButton.addActionListener(this);
        duplicateButton.addActionListener(this);
        processButton.addActionListener(this);
        showButton.addActionListener(this);
        showInstruction.addActionListener(this);
        exitButton.addActionListener(this);

        panel.add(loadButton);
        panel.add(duplicateButton);
        panel.add(processButton);
        panel.add(showButton);
        panel.add(showInstruction);
        panel.add(exitButton);
    }

    private void writeInstruction(JTextArea textArea) {
        String instruction = "1. В \"этикетках\" загрузить нужную переоценку.\n" +
                "2. Выбрать не нулевые позиции через фильтр в столбце\"Кол-во\".\n" +
                "3. Скопировать полученную таблицу в таблицу Excel, запомнив ее местонахождение.\n" +
                "4. Нажать кнопку \"Загрузить таблицу\".\n" +
                "5. Выбрать ранее сохранненую таблицу.\n" +
                "6. Нажать кнопку \"Удалить дубликаты\".\n" +
                "7. В TradeX \"Экспортировать из Excel\" ранее сохраненную таблицу.\n" +
                "8. В TradeX нажать \"Поставить цены из указанного объекта\".\n" +
                "9. Нажать кнопку \"Поставить цены\".\n" +
                "10. Нажать кнопку \"Показать таблицу\".\n" +
                "11. Скопировать ячейки с ценами.\n" +
                "12. Скопировать ячейки с ценами.\n" +
                "13. Вставить в TradeX в столбец \"Старые цены\".\n" +
                "14. Готово!\n";
        textArea.append(instruction);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == loadButton) {
            loadTable();
        } else if (actionEvent.getSource() == duplicateButton) {
            removeDuplicates();
        } else if (actionEvent.getSource() == processButton) {
            process();
        } else if (actionEvent.getSource() == showButton) {
            openFile();
        } else if (actionEvent.getSource() == showInstruction) {
            writeInstruction(textArea);
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

    private void removeDuplicates() {
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

    private void process() {
        if (excelFile == null) {
            log("Сначала необходимо загрузить таблицу.");
        } else {
            try {
                excelFile.putPrices();
            } catch (IOException e) {
                log("Невозможно записать файл, возможно он открыт другой программой");
            }
        }
    }

    private void openFile() {
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(tableFile);
        } catch (NullPointerException e) {
            log("Необходимо сначала открыть таблицу и поставить цены.");
        } catch (IllegalArgumentException e) {
            log("Файл не существует, возможно он удален или перемещен.");
        } catch (UnsupportedOperationException e) {
            log("Данная платформа не поддерживает открытие файла отсюда.");
        } catch (IOException e) {
            log("Файл таблицы не ассоциирован с какой либо программой для открытия" +
            " или программа не смогла запуститься.");
        } catch (SecurityException e) {
            log("Доступ к файлу запрещен.");
        }
    }

    public void log(String msg) {
        textArea.append(msg + "\n");
    }

    public File getTableFile() {
        return tableFile;
    }
}
