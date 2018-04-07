package com.company;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import javax.swing.*;

public class MaxPriceTableTask extends SwingWorker<Void, Void> {

    private Gui gui;
    private ExcelFile file;
    private QueryUtils queryUtils;

    public MaxPriceTableTask(Gui gui) {
        this.gui = gui;
        file = gui.getExcelFile();
        queryUtils = new QueryUtils(gui.getTextAreaLog());
    }

    @Override
    protected Void doInBackground() throws Exception {
        Sheet sheet = file.getSheet();
        int lastRowNum = sheet.getLastRowNum();
        // перебрать все строки в таблице, кроме первой
        for (int i = 1; i <= lastRowNum; ++i) {
            // взять название модели
            String modelCell = sheet.getRow(i).getCell(0).getStringCellValue();
            Cell priceCell = sheet.getRow(i).createCell(4);
            // получить с сайта значение макс. цены и поставить в ячейку
            priceCell.setCellValue(queryUtils.fetchMaxPrice(modelCell));
            setProgress(i * 95 / lastRowNum);
            try {
                // без паузы были проблемы с ответом
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        setProgress(95);
        file.closeTable();
        return null;
    }

    @Override
    protected void done() {
        super.done();
        gui.makeProgressBarInvisible();
        gui.getTextAreaLog().textAppend("Постановка цен закончена.");
    }
}
