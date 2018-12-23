package com.company.view;

import com.company.exceptions.FileChoosingInterruptedException;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class View {

    private JButton loadTableButton;
    private JButton removeDuplicatesButton;
    private JButton processButton;
    private JButton showTableButton;
    private JButton showInstructionButton;
    private JButton exitButton;
    private List<JButton> allButtons = new ArrayList<>();

    private JProgressBar progressBar;
    private JTextArea textArea;

    public View() {
        setupUi();
    }

    private void setupUi() {
        JFrame frame = new JFrame("Max price");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        setupButtons(panel);
        panel.setLayout(new GridLayout(10, 1, 5, 15));

        setupTextArea();
        writeInstruction();

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

    private void setupButtons(JPanel panel) {
        loadTableButton = new JButton("Загрузить таблицу");
        allButtons.add(loadTableButton);
        removeDuplicatesButton = new JButton("Удалить дубликаты");
        allButtons.add(removeDuplicatesButton);
        processButton = new JButton("Поставить цены");
        allButtons.add(processButton);
        showTableButton = new JButton("Показать таблицу");
        allButtons.add(showTableButton);
        showInstructionButton = new JButton(("Инструкция"));
        allButtons.add(showInstructionButton);
        exitButton = new JButton("Выход");
        allButtons.add(exitButton);

        for (JButton each : allButtons) {
            panel.add(each);
        }
    }

    private void setupTextArea() {
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(5, 5, 5, 5));
        textArea.setEditable(false);
        textArea.setFont(textArea.getFont().deriveFont(12f)); // will only change size to 12pt
    }

    public void addTextToTextArea(String text) {
        textArea.append(text + '\n');
    }

    public void writeInstruction() {
        String instruction =
                "1. В \"этикетках\" загрузить нужную переоценку.\n" +
                "2. Выбрать не нулевые позиции через фильтр в столбце\"Кол-во\".\n" +
                "3. Скопировать полученную таблицу в новый файл Excel, закрыть, запомнив его местонахождение.\n" +
                "4. Нажать кнопку \"Загрузить таблицу\".\n" +
                "5. Выбрать ранее сохранненую таблицу.\n" +
                "6. Нажать кнопку \"Удалить дубликаты\".\n" +
                "7. В TradeX \"Экспортировать из Excel\" ранее сохраненную таблицу.\n" +
                "8. В TradeX нажать \"Поставить цены из указанного объекта\".\n" +
                "9. Нажать кнопку \"Поставить цены\".\n" +
                "10. Нажать кнопку \"Показать таблицу\".\n" +
                "11. Скопировать ячейки с ценами.\n" +
                "12. Вставить в TradeX в столбец \"Старые цены\".\n\n\n";
        addTextToTextArea(instruction);
    }

    public File showFileChooser() throws FileChoosingInterruptedException{
        JFileChooser fileChooser = new JFileChooser();
        FileFilter excelFilter = new FileNameExtensionFilter("Excel file", "xls", "xlsx");
        fileChooser.setFileFilter(excelFilter);
        int retValue = fileChooser.showDialog(null, "Открыть файл");
        if (retValue == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        } else {
            throw new FileChoosingInterruptedException();
        }
    }

    public void hideProgressBar() {
        progressBar.setVisible(false);
    }

    public void showProgressBar() {
        progressBar.setVisible(true);
    }

    public void setProgressBarValue(int n) {
        progressBar.setValue(n);
    }

    public JButton getLoadTableButton() {
        return loadTableButton;
    }

    public JButton getRemoveDuplicatesButton() {
        return removeDuplicatesButton;
    }

    public JButton getProcessButton() {
        return processButton;
    }

    public JButton getShowTableButton() {
        return showTableButton;
    }

    public JButton getShowInstructionButton() {
        return showInstructionButton;
    }

    public JButton getExitButton() {
        return exitButton;
    }

    public List<JButton> getAllButtons() {
        return allButtons;
    }
}
