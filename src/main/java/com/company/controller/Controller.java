package com.company.controller;

import com.company.ExcelFile;
import com.company.exceptions.FileChoosingInterruptedException;
import com.company.view.View;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Controller implements ActionListener {

    private View view;
    private ExcelFile excelFile;

    public Controller(View view) {
        this.view = view;
        setOnclickListenerForButtons();
    }

    private void setOnclickListenerForButtons() {
        view.getLoadTableButton().addActionListener(this);
        view.getRemoveDuplicatesButton().addActionListener(this);
        view.getProcessButton().addActionListener(this);
        view.getShowTableButton().addActionListener(this);
        view.getShowInstructionButton().addActionListener(this);
        view.getExitButton().addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Object button = actionEvent.getSource();
        if (button == view.getLoadTableButton()) {
            loadTableFile();
        } else if (button == view.getRemoveDuplicatesButton()) {

        } else if (button == view.getProcessButton()) {

        } else if (button == view.getShowTableButton()) {

        } else if (button == view.getShowInstructionButton()) {

        } else if (button == view.getExitButton()) {

        }
    }

    private void loadTableFile() {
        try {
            File tableFile = view.showFileChooser();
            view.addTextToTextArea("Выбран файл: " + tableFile.getName());
            excelFile = new ExcelFile(tableFile);
            boolean isLoaded = excelFile.loadSheet();
            if (!isLoaded) {
                view.addTextToTextArea("Файл должен быть с расширением xls или xlsx");
            }
        } catch (FileChoosingInterruptedException e) {
            view.addTextToTextArea("Выбор файла прерван.");
        } catch (FileNotFoundException e) {
            view.addTextToTextArea("Файл не найден. " + e);
        } catch (IOException e) {
            view.addTextToTextArea("Невозможно прочесть файл.");
        }
    }
}
