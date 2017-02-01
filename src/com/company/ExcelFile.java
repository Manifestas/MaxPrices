package com.company;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

/**
 * Created by tv.unimol on 01.02.2017.
 */
public class ExcelFile {

    File excelFile;
    Sheet sheet;

    public ExcelFile(File file) throws IOException{
        excelFile = file;
        loadSheet();
    }

    // TODO: разобраться с поимкой исключений
    private void loadSheet() throws FileNotFoundException, IOException {
        InputStream inputStream = new FileInputStream(excelFile);
        // TODO: проверку на расширение(если xls, то исп. HSSFWorkbook
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        sheet = workbook.getSheetAt(0);
    }

    public void deleteRow(int rowNo) {
        if (sheet == null) {
            return;
        }
        int lastRowNum = sheet.getLastRowNum();
        if (rowNo >= 0 && rowNo < lastRowNum) {
            sheet.shiftRows(rowNo + 1, lastRowNum, -1);
        }
        if (rowNo == lastRowNum) {
            Row removingRow = sheet.getRow(rowNo);
            if (removingRow != null) {
                sheet.removeRow(removingRow);
            }
        }
    }

    public void removeDuplicates() {
        int lastRow = sheet.getLastRowNum();
        System.out.println(lastRow);
    }
}
