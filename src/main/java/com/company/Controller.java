package com.company;

import com.company.exceptions.FileChoosingInterruptedException;
import com.company.view.View;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class Controller implements ActionListener {

    private View view;
    private File tableFile;

    public Controller(View view) {
        this.view = view;
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
            loadTable();

        } else if (button == view.getRemoveDuplicatesButton()) {

        } else if (button == view.getProcessButton()) {

        } else if (button == view.getShowTableButton()) {

        } else if (button == view.getShowInstructionButton()) {

        } else if (button == view.getExitButton()) {

        }
    }

    private void loadTable() {
        try {
            tableFile = view.showFileChooser();
            view.addTextToTextArea("Выбран файл: " + tableFile.getName());
        } catch (FileChoosingInterruptedException e) {
            view.addTextToTextArea("Выбор файла прерван.");
        }
    }
}
