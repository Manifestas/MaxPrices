package com.company;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

public class ExcelFile {

    private File excelFile;
    private Sheet sheet;
    private XSSFWorkbook workbook;

    public ExcelFile(File file) throws IOException {
        excelFile = file;
        loadSheet();
    }

    // TODO: разобраться с поимкой исключений
    private void loadSheet() throws FileNotFoundException, IOException {
        InputStream inputStream = new FileInputStream(excelFile);
        // TODO: проверку на расширение(если xls, то исп. HSSFWorkbook
        workbook = new XSSFWorkbook(inputStream);
        sheet = workbook.getSheetAt(0);
    }

    private void deleteRow(int rowNo) {
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

    // TODO: убирать строки с нулями в количестве и менять кол-во больше чем 1 на 1
    public void removeDuplicates() throws IOException {
        String tempValue = "";
        for (int i = 1; i <= sheet.getLastRowNum(); ++i) {
            String cellValue = sheet.getRow(i).getCell(0).toString();
            if (cellValue.equals(tempValue)) {
                deleteRow(i);
                --i;
            } else {
                tempValue = cellValue;
                Cell cell8 = sheet.getRow(i).getCell(8);
                cell8.setCellValue(QueryUtils.fetchMaxPrice(cellValue));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        closeTable();
        System.out.println("Подстановка цен завершена.");
    }


    private void closeTable() throws IOException {
        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(excelFile);
        workbook.write(fileOut);
        fileOut.close();
    }
}
