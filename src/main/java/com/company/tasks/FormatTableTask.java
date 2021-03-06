package com.company.tasks;

import com.company.ExcelFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import javax.swing.*;

public class FormatTableTask extends SwingWorker<Void, Void> {

    private ExcelFile file;

    public FormatTableTask(ExcelFile excelFile) {
        file = excelFile;
    }

    @Override
    protected Void doInBackground() {
        file.formatTable();
        setProgress(10);
        Sheet sheet = file.getSheet();
        int lastRowNum = sheet.getLastRowNum();
        for (int i = 1; i <= lastRowNum; i++) {
            if (isCancelled()) {
                return null;
            }
            String currentArticle = file.getCellValue(i, 0);
            String currentColor = file.getCellValue(i, 1);
            String previousArticle = file.getCellValue(i - 1, 0);
            String previousColor = file.getCellValue(i - 1, 1);
            // Если артикул и цвет равны - удалить строку
            if (currentArticle.equals(previousArticle) && currentColor.equals(previousColor)) {
                file.deleteRow(i);
                lastRowNum--;
                i--;
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
}
