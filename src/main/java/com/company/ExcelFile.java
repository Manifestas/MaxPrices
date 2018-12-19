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
            // таблица на первом листе
            sheet = workbook.getSheetAt(0);
        } catch (FileNotFoundException e) {
            textAreaLog.textAppend("Файл не найден. " + e);
        } catch (IOException e) {
            textAreaLog.textAppend("Невозможно прочесть файл.");
        }
    }

    /**
     * Удалить строку
     *
     * @param rowNo номер строки, которую нужно удалить
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
     * Удаляет столбцы "Название", "Цена", "Ск%", "Цена со ск.", "Ст.цена", "Выгода"
     * меняет название стобца "Кол-во" на "Количество" (необходимо для корректного
     * импортирования в TradeX
     */
    public void formatTable() {
        textAreaLog.textAppend("Начало преобразования таблицы.");
        // если в таблице меньше трех строк
        if (sheet.getLastRowNum() < 3) {
            textAreaLog.textAppend("В таблице отсутствуют данные");
            return;
        }
        // если значение в четвертой колонке пустое- таблица скорее всего уже отформатирована
        // счет столбцов начинается с единицы
        if (sheet.getRow(0).getLastCellNum() <= 3) {
            textAreaLog.textAppend("Таблица уже прошла обработку");
            return;
        }

        //удалить столбец "Название"
        deleteColumn(sheet, 3);
        //удалить 4-8 столбцы(они смещаются налево, поэтому один номер
        for (int i = 0; i < 5; ++i) {
            deleteColumn(sheet, 4);
        }
        Cell count = sheet.getRow(0).getCell(3);
        count.setCellValue("Количество");

    }

    /**
     * Удаляет в указанной таблице столбец с указанным номером,
     * перемещая все данные из левого столбца на место удаленного.
     * <p>
     * Примечание: не обновляет формулы
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
     * Берет все данные и формулы из старой ячейки и переносит в новую
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

    /**
     * Сохраняет(перезаписывает) таблицу на жесткий диск.
     */
    public void closeTable() {
        try {
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
        else
            return "";
    }
}
