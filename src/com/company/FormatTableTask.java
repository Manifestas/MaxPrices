package com.company;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import javax.swing.*;
import java.io.IOException;

public class FormatTableTask extends SwingWorker<Void, Void> {

    private Gui gui;
    private ExcelFile file;

    public FormatTableTask(Gui gui) {
        this.gui = gui;
        file = gui.getExcelFile();
    }
    @Override
    protected Void doInBackground() throws IOException {
        file.formatTable();
        setProgress(10);
        Sheet sheet = file.getSheet();
        int lastRowNum = sheet.getLastRowNum();
        for (int i = 1; i <= lastRowNum; ++i) {
            // Если артикул и цвет равны - удалить строку
            if (file.getCellValue(i, 0).equals(file.getCellValue(i - 1, 0))
                    && file.getCellValue(i, 1).equals(file.getCellValue(i - 1, 1))) {
                file.deleteRow(i);
                lastRowNum = sheet.getLastRowNum();
                --i;
                setProgress(i * 80 / lastRowNum + 10);
            } else {
                Cell cell3 = sheet.getRow(i).getCell(3);
                //поставить "кол-во" - 1
                cell3.setCellValue(1);
            }
        }
        setProgress(90);
        file.closeTable();
        setProgress(100);
        return null;
    }

    @Override
    protected void done() {
        super.done();
        gui.makeProgressBarInvisible();
        gui.getTextAreaLog().textAppend("Преобразование окончено.");
    }
}
