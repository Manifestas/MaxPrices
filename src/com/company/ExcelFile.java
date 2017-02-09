package com.company;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

public class ExcelFile {

    private File excelFile;
    private Sheet sheet;
    private Workbook workbook;
    private Gui.TextAreaLog textAreaLog;

    public ExcelFile(File file, Gui.TextAreaLog textAreaLog) {
        excelFile = file;
        this.textAreaLog = textAreaLog;
        loadSheet();
    }

    public Sheet getSheet() {
        return sheet;
    }

    private void loadSheet() {
        try {
            InputStream inputStream = new FileInputStream(excelFile);
            if (getFileExtension(excelFile).equalsIgnoreCase("xls")) {
                workbook = new HSSFWorkbook(inputStream);
            } else if (getFileExtension(excelFile).equalsIgnoreCase("xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                textAreaLog.textAppend("Файл должен быть с расширением xls или xlsx");
                return;
            }
            sheet = workbook.getSheetAt(0);
        } catch (FileNotFoundException e) {
            textAreaLog.textAppend("Файл не найден. " + e);
        } catch (IOException e) {
            textAreaLog.textAppend("Невозможно прочесть файл.");
        }
    }

    /**
     * Remove(shift) row.
     *
     * @param rowNo row number to be deleted
     */
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

    /**
     * Возвращает строку, находящуюся на пересечении номера строки и столбца
     */
    public String getCellValue(int rowNumber, int cellNumber) {
        return sheet.getRow(rowNumber).getCell(cellNumber).toString();
    }

    /**
     * Удаляет столбцы "Название", "Цена", "Ск%", "Цена со ск.", "Ст.цена",
     * меняет название стобца "Кол-во" на "Количество"
     */
    public void formatTable() {
        textAreaLog.textAppend("Начало преобразования таблицы.");
        deleteColumn(sheet, 3);
        //удалить 4-7 столбцы(они смещаются налево, поэтому один номер
        for (int i = 0; i < 4; ++i) {
            deleteColumn(sheet, 4);
        }
        Cell count = sheet.getRow(0).getCell(3);
        count.setCellValue("Количество");
        textAreaLog.textAppend("Преобразование окончено.");

    }

    /**
     * Given a sheet, this method deletes a column from a sheet and moves
     * all the columns to the right of it to the left one cell.
     * <p>
     * Note, this method will not update any formula references.
     */
    private static void deleteColumn(Sheet sheet, int columnToDelete) {
        int maxColumn = 0;
        for (int r = 0; r < sheet.getLastRowNum() + 1; r++) {
            Row row = sheet.getRow(r);

            // if no row exists here; then nothing to do; next!
            if (row == null)
                continue;

            // if the row doesn't have this many columns then we are good; next!
            int lastColumn = row.getLastCellNum();
            if (lastColumn > maxColumn)
                maxColumn = lastColumn;

            if (lastColumn < columnToDelete)
                continue;

            for (int x = columnToDelete + 1; x < lastColumn + 1; x++) {
                Cell oldCell = row.getCell(x - 1);
                if (oldCell != null)
                    row.removeCell(oldCell);

                Cell nextCell = row.getCell(x);
                if (nextCell != null) {
                    Cell newCell = row.createCell(x - 1, nextCell.getCellType());
                    cloneCell(newCell, nextCell);
                }
            }
        }
        // Adjust the column widths
        for (int c = 0; c < maxColumn; c++) {
            sheet.setColumnWidth(c, sheet.getColumnWidth(c + 1));
        }
    }

    /**
     * Takes an existing Cell and merges all the styles and formula
     * into the new one
     */
    private static void cloneCell(Cell cNew, Cell cOld) {
        cNew.setCellComment(cOld.getCellComment());
        cNew.setCellStyle(cOld.getCellStyle());

        switch (cNew.getCellTypeEnum()) {
            case BOOLEAN: {
                cNew.setCellValue(cOld.getBooleanCellValue());
                break;
            }
            case NUMERIC: {
                cNew.setCellValue(cOld.getNumericCellValue());
                break;
            }
            case STRING: {
                cNew.setCellValue(cOld.getStringCellValue());
                break;
            }
            case ERROR: {
                cNew.setCellValue(cOld.getErrorCellValue());
                break;
            }
            case FORMULA: {
                cNew.setCellFormula(cOld.getCellFormula());
                break;
            }
        }
    }

    public void closeTable() {
        try {
            // Write the output to a file
            FileOutputStream fileOut = new FileOutputStream(excelFile);
            workbook.write(fileOut);
            fileOut.close();
        } catch (FileNotFoundException e) {
            textAreaLog.textAppend("По некоторым причинам файл не может быть открыт " + e);
        } catch (IOException e) {
            textAreaLog.textAppend("Файл не может быть сохранен." + e);
        }
        textAreaLog.textAppend("Сохранение таблицы.");
    }

    private String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        else return "";
    }
}
