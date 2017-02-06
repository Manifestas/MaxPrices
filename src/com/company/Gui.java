package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

public class Gui implements ActionListener, PropertyChangeListener {

    private JButton loadButton;
    private JButton duplicateButton;
    private JButton processButton;
    private JButton showButton;
    private JButton showInstruction;
    private JButton exitButton;

    private JProgressBar progressBar;
    private File tableFile;
    private JTextArea textArea;
    private ExcelFile excelFile;


    private TextAreaLog textAreaLog;

    public Gui() {
        textAreaLog = new TextAreaLog();

    }

    public void initUi() {
        JFrame frame = new JFrame("Max price");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        initButtons(panel);
        panel.setLayout(new GridLayout(10, 1, 5, 15));

        initTextArea();
        writeInstruction(textArea);

        progressBar = new JProgressBar(0, 100);
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        frame.getContentPane().add(BorderLayout.CENTER, scrollPane);
        frame.getContentPane().add(BorderLayout.EAST, panel);
        frame.getContentPane().add(BorderLayout.NORTH, progressBar);

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

    private void initTextArea() {
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(5, 5, 5, 5));
        textArea.setEditable(false);
        textArea.setFont(textArea.getFont().deriveFont(12f)); // will only change size to 12pt
    }

    private void writeInstruction(JTextArea textArea) {
        String instruction = "1. В \"этикетках\" загрузить нужную переоценку.\n" +
                "2. Выбрать не нулевые позиции через фильтр в столбце\"Кол-во\".\n" +
                "3. Скопировать полученную таблицу в новый файл Excel, запомнив ее местонахождение.\n" +
                "4. Нажать кнопку \"Загрузить таблицу\".\n" +
                "5. Выбрать ранее сохранненую таблицу.\n" +
                "6. Нажать кнопку \"Удалить дубликаты\".\n" +
                "7. В TradeX \"Экспортировать из Excel\" ранее сохраненную таблицу.\n" +
                "8. В TradeX нажать \"Поставить цены из указанного объекта\".\n" +
                "9. Нажать кнопку \"Поставить цены\".\n" +
                "10. Нажать кнопку \"Показать таблицу\".\n" +
                "11. Скопировать ячейки с ценами.\n" +
                "12. Вставить в TradeX в столбец \"Старые цены\".\n\n\n";
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
            excelFile = new ExcelFile(tableFile, getTextAreaLog());
            textAreaLog.textAppend("Выбран файл " + tableFile.getName());
        } else {
            textAreaLog.textAppend("Выбор файла прерван.");
        }
    }

    private void removeDuplicates() {
        if (excelFile == null) {
            textAreaLog.textAppend("Сначала необходимо загрузить таблицу.");
        } else {
            progressBar.setVisible(true);
            progressBar.setValue(0);
            try {
                FormatTableTask formatTableTask = new FormatTableTask(this);
                formatTableTask.addPropertyChangeListener(this);
                formatTableTask.execute();
            } catch (Exception e) {
                textAreaLog.textAppend("Невозможно записать файл, возможно он открыт другой программой");
            }
        }

    }

    private void process() {
        if (excelFile == null) {
            textAreaLog.textAppend("Сначала необходимо загрузить таблицу.");
        } else {
            progressBar.setVisible(true);
            progressBar.setValue(0);
            try {
                MaxPriceTableTask maxPriceTableTask = new MaxPriceTableTask(this);
                maxPriceTableTask.addPropertyChangeListener(this);
                maxPriceTableTask.execute();
            } catch (Exception e) {
                textAreaLog.textAppend("Невозможно записать файл, возможно он открыт другой программой");
            }
        }
    }

    private void openFile() {
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(tableFile);
        } catch (NullPointerException e) {
            textAreaLog.textAppend("Необходимо сначала выбрать файл.");
        } catch (IllegalArgumentException e) {
            textAreaLog.textAppend("Файл не существует, возможно он удален или перемещен.");
        } catch (UnsupportedOperationException e) {
            textAreaLog.textAppend("Данная платформа не поддерживает открытие файла отсюда.");
        } catch (IOException e) {
            textAreaLog.textAppend("Файл таблицы не ассоциирован с какой либо программой для открытия" +
                    " или программа не смогла запуститься.");
        } catch (SecurityException e) {
            textAreaLog.textAppend("Доступ к файлу запрещен.");
        }
    }

    public ExcelFile getExcelFile() {
        return excelFile;
    }

    public TextAreaLog getTextAreaLog() {
        return textAreaLog;
    }

    public void makeProgressBarInvisible() {
        progressBar.setVisible(false);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equalsIgnoreCase("progress")) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        }
    }

    public class TextAreaLog {

        public void textAppend(String text) {
            textArea.append(text + "\n");
        }
    }
}
