package com.company.controller;

import com.company.ExcelFile;
import com.company.MessageListener;
import com.company.QueryUtils;
import com.company.exceptions.FileChoosingInterruptedException;
import com.company.tasks.FormatTableTask;
import com.company.tasks.MaxPriceTableTask;
import com.company.view.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class Controller implements ActionListener, PropertyChangeListener, MessageListener {

    private View view;
    private ExcelFile excelFile;
    private FormatTableTask formatTableTask;
    private MaxPriceTableTask maxPriceTableTask;
    private StateValue state = StateValue.PENDING;

    public Controller(View view) {
        this.view = view;
        setOnclickListenerForButtons();
    }

    private void setOnclickListenerForButtons() {
        List<JButton> allButtons = view.getAllButtons();
        for (JButton each : allButtons) {
            each.addActionListener(this);
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Object button = actionEvent.getSource();
        if (button == view.getLoadTableButton()) {
            loadTableFile();
        } else if (button == view.getRemoveDuplicatesButton()) {
            if (state == StateValue.PENDING) {
                removeDuplicates();
            } else if (state == StateValue.REMOVING_DUPLICATES && formatTableTask != null) {
                formatTableTask.cancel(true);
            }
        } else if (button == view.getProcessButton()) {
            if (state == StateValue.PENDING) {
                process();
            } else if (state == StateValue.PROCESSING && maxPriceTableTask != null) {
                maxPriceTableTask.cancel(true);
            }
        } else if (button == view.getShowTableButton()) {
            openFile();
        } else if (button == view.getShowInstructionButton()) {
            view.writeInstruction();
        } else if (button == view.getExitButton()) {
            System.exit(0);
        }
    }

    private void loadTableFile() {
        try {
            state = StateValue.LOADING_FILE;
            view.disableAllButtonsExcept(null);

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
        state = StateValue.PENDING;
        view.enableAllButtons();
    }

    private void removeDuplicates() {
        if (excelFile == null) {
            view.addTextToTextArea("Сначала необходимо загрузить таблицу.");
        } else {
            state = StateValue.REMOVING_DUPLICATES;
            JButton removeDuplicates = view.getRemoveDuplicatesButton();
            removeDuplicates.setText("Остановить");
            view.disableAllButtonsExcept(removeDuplicates);
            view.showProgressBar();
            view.setProgressBarValue(0);

            formatTableTask = new FormatTableTask(excelFile);
            formatTableTask.addPropertyChangeListener(this);
            formatTableTask.execute();
        }
    }

    private void process() {
        if (excelFile == null) {
            view.addTextToTextArea("Сначала необходимо загрузить таблицу.");
        } else {
            state = StateValue.PROCESSING;
            JButton processButton = view.getProcessButton();
            processButton.setText("Остановить");
            view.disableAllButtonsExcept(processButton);
            view.showProgressBar();
            view.setProgressBarValue(0);

            QueryUtils queryUtils = new QueryUtils(this);
            maxPriceTableTask = new MaxPriceTableTask(excelFile, queryUtils);
            maxPriceTableTask.addPropertyChangeListener(this);
            maxPriceTableTask.execute();
        }
    }

    private void openFile() {
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(excelFile.getExcelFile());
        } catch (NullPointerException e) {
            view.addTextToTextArea("Необходимо сначала выбрать файл.");
        } catch (IllegalArgumentException e) {
            view.addTextToTextArea("Файл не существует, возможно он удален или перемещен.");
        } catch (UnsupportedOperationException e) {
            view.addTextToTextArea("Данная платформа не поддерживает открытие файла отсюда.");
        } catch (IOException e) {
            view.addTextToTextArea("Файл таблицы не ассоциирован с какой либо программой для открытия" +
                    " или программа не смогла запуститься.");
        } catch (SecurityException e) {
            view.addTextToTextArea("Доступ к файлу запрещен.");
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
                view.getRemoveDuplicatesButton().setText("Удалить дубликаты");
                view.enableAllButtons();
                state = StateValue.PENDING;
            }
        }
        if (evt.getSource() instanceof MaxPriceTableTask) {
            if ("state".equals(evt.getPropertyName()) && SwingWorker.StateValue.DONE == evt.getNewValue()) {
                view.addTextToTextArea("Постановка цен закончена.");
                view.hideProgressBar();
                view.getProcessButton().setText("Поставить цены");
                view.enableAllButtons();
                state = StateValue.PENDING;
            }
        }
    }

    @Override
    public void onMessage(String s) {
        SwingUtilities.invokeLater(() -> view.addTextToTextArea(s));
    }

    public enum StateValue {

        /** Initial Controller state. */
        PENDING,
        /** When starts loading ExcelFile in memory. */
        LOADING_FILE,
        /** Removing duplicates from file. */
        REMOVING_DUPLICATES,
        /** Requesting prices from internet and saving to file. */
        PROCESSING
    }

}
