package com.company.controller;

import com.company.ExcelFile;
import com.company.MessageListener;
import com.company.QueryUtils;
import com.company.exceptions.FileChoosingInterruptedException;
import com.company.tasks.FormatTableTask;
import com.company.tasks.MaxPriceTableTask;
import com.company.view.View;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Controller implements ActionListener, PropertyChangeListener, MessageListener {

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
            removeDuplicates();
        } else if (button == view.getProcessButton()) {
            process();
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
                excelFile = null;
            } else {
                excelFile.setMessageListener(this);
            }
        } catch (FileChoosingInterruptedException e) {
            view.addTextToTextArea("Выбор файла прерван.");
        } catch (FileNotFoundException e) {
            view.addTextToTextArea("Файл не найден. " + e);
        } catch (IOException e) {
            view.addTextToTextArea("Невозможно прочесть файл.");
        }
    }

    private void removeDuplicates() {
        if (excelFile == null) {
            view.addTextToTextArea("Сначала необходимо загрузить таблицу.");
        } else {
            view.showProgressBar();
            view.setProgressBarValue(0);
            FormatTableTask formatTableTask = new FormatTableTask(excelFile);
            formatTableTask.addPropertyChangeListener(this);
            formatTableTask.execute();
        }
    }

    private void process() {
        if (excelFile == null) {
            view.addTextToTextArea("Сначала необходимо загрузить таблицу.");
        } else {
            view.showProgressBar();
            view.setProgressBarValue(0);
            QueryUtils queryUtils = new QueryUtils(this);
            MaxPriceTableTask maxPriceTableTask = new MaxPriceTableTask(excelFile, queryUtils);
            maxPriceTableTask.addPropertyChangeListener(this);
            maxPriceTableTask.execute();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equalsIgnoreCase("progress")) {
            int progress = (Integer) evt.getNewValue();
            view.setProgressBarValue(progress);
        }
        if (evt.getSource() instanceof FormatTableTask) {
            if ("state".equals(evt.getPropertyName()) && SwingWorker.StateValue.DONE == evt.getNewValue()) {
                view.addTextToTextArea("Преобразование таблицы окончено.");
                view.hideProgressBar();
            }
        }
        if (evt.getSource() instanceof MaxPriceTableTask) {
            if ("state".equals(evt.getPropertyName()) && SwingWorker.StateValue.DONE == evt.getNewValue()) {
                view.addTextToTextArea("Постановка цен закончена.");
                view.hideProgressBar();
            }
        }
    }

    @Override
    public void onMessage(String s) {
        SwingUtilities.invokeLater(() -> view.addTextToTextArea(s));
    }

}
