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
    private MessageListener messageListener;

    public ExcelFile(File file) {
        excelFile = file;
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

            // если здесь нет строки - переходить к следующей
            if (row == null)
                continue;

            // если в строке столбцов меньше - переход к следующей
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
        // отрегулировать ширину столбца
        for (int c = 0; c < maxColumn; c++) {
            sheet.setColumnWidth(c, sheet.getColumnWidth(c + 1));
        }
    }

    /**
     * Берет все данные и формулы из старой ячейки и переносит в новую
     */
    private static void cloneCell(Cell newCell, Cell oldCell) {
        newCell.setCellComment(oldCell.getCellComment());
        newCell.setCellStyle(oldCell.getCellStyle());

        switch (newCell.getCellType()) {
            case BOOLEAN: {
                newCell.setCellValue(oldCell.getBooleanCellValue());
                break;
            }
            case NUMERIC: {
                newCell.setCellValue(oldCell.getNumericCellValue());
                break;
            }
            case STRING: {
                newCell.setCellValue(oldCell.getStringCellValue());
                break;
            }
            case ERROR: {
                newCell.setCellValue(oldCell.getErrorCellValue());
                break;
            }
            case FORMULA: {
                newCell.setCellFormula(oldCell.getCellFormula());
                break;
            }
        }
    }

    public void setMessageListener(MessageListener listener) {
        messageListener = listener;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public boolean loadSheet() throws IOException {
        InputStream inputStream = new FileInputStream(excelFile);
        if (getFileExtension(excelFile).equalsIgnoreCase("xls")) {
            workbook = new HSSFWorkbook(inputStream);
        } else if (getFileExtension(excelFile).equalsIgnoreCase("xlsx")) {
            workbook = new XSSFWorkbook(inputStream);
        } else {
            return false;
        }
        // таблица на первом листе
        sheet = workbook.getSheetAt(0);
        return true;
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
        messageListener.onMessage("Начало преобразования таблицы.");
        // если в таблице меньше трех строк
        if (sheet.getLastRowNum() < 3) {
            messageListener.onMessage("В таблице отсутствуют данные");
            return;
        }
        // если значение в четвертой колонке пустое- таблица скорее всего уже отформатирована
        // счет столбцов начинается с единицы
        if (sheet.getRow(0).getLastCellNum() <= 3) {
            messageListener.onMessage("Таблица уже прошла обработку");
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
     * Сохраняет(перезаписывает) таблицу на жесткий диск.
     */
    public void closeTable() {
        try {
            FileOutputStream fileOut = new FileOutputStream(excelFile);
            workbook.write(fileOut);
            fileOut.close();
        } catch (FileNotFoundException e) {
            messageListener.onMessage("По некоторым причинам файл не может быть открыт " + e);
        } catch (IOException e) {
            messageListener.onMessage("Файл не может быть сохранен." + e);
        }
        messageListener.onMessage("Сохранение таблицы.");
    }

    private String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        else
            return "";
    }

    public File getExcelFile() {
        return excelFile;
    }
}
