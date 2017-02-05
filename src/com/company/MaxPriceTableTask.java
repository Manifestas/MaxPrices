package com.company;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import javax.swing.*;

public class MaxPriceTableTask extends SwingWorker<Void, Void> {

    private Gui gui;
    private ExcelFile file;

    public MaxPriceTableTask(Gui gui) {
        this.gui = gui;
        file = gui.getExcelFile();
    }

    @Override
    protected Void doInBackground() throws Exception {
        Sheet sheet = file.getSheet();
        int lastRowNum = sheet.getLastRowNum();
        for (int i = 1; i <= lastRowNum; ++i) {
            String modelCell = sheet.getRow(i).getCell(0).toString();
            Cell priceCell = sheet.getRow(i).createCell(4);
            priceCell.setCellValue(QueryUtils.fetchMaxPrice(modelCell));
            setProgress(i * 95 / lastRowNum);
            try {
                // не знаю, как сервер отреагирует, на всякий случай
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
    }
}
