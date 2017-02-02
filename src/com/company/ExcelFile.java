package com.company;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
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

    /**
     * Remove(shift) row.
     * @param rowNo row number to be deleted
     */
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

    /**
     * Remove rows with duplicate models.
     * and format table for import in tradex
     * @throws IOException when file can't be saved.
     */
    public void removeDuplicates() throws IOException {
        formatTable();
        for (int i = 1; i <= sheet.getLastRowNum(); ++i) {
            // Если артикул и цвет равны - удалить строку
            if (getCellValue(i, 0).equals(getCellValue(i - 1, 0))
                    && getCellValue(i, 1).equals(getCellValue(i - 1, 1))) {
                deleteRow(i);
                --i;
            } else {
                Cell cell3 = sheet.getRow(i).getCell(3);
                //поставить "кол-во" - 1
                cell3.setCellValue(1);
            }
        }
        closeTable();
        System.out.println("Преобразование тыблицы завершено.");
    }

    /**
     * Возвращает строку, находящуюся на пересечении номера строки и столбца
     */
    private String getCellValue(int rowNumber, int cellNumber) {
        return sheet.getRow(rowNumber).getCell(cellNumber).toString();
    }

    /**
     * Проставляет цену для артикула(первая ячейка в строке) в пятой колонке таблицы.
     *
     */
    public void putPrices() throws IOException{
        for (int i = 1; i <= sheet.getLastRowNum(); ++i) {
            String modelCell = sheet.getRow(i).getCell(0).toString();
            Cell priceCell = sheet.getRow(i).createCell(4);
            priceCell.setCellValue(QueryUtils.fetchMaxPrice(modelCell));
            try {
                // не знаю, как сервер отреагирует, на всякий случай
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        closeTable();
    }

    /** Удаляет столбцы "Название", "Цена", "Ск%", "Цена со ск.", "Ст.цена",
     *  меняет название стобца "Кол-во" на "Количество"
     */
     private void formatTable() {
         deleteColumn(sheet, 3);
         //удалить 4-7 столбцы(они смещаются налево, поэтому один номер
         for (int i = 0; i < 4; ++i) {
             deleteColumn(sheet, 4);
         }
         Cell count = sheet.getRow(0).getCell(3);
         count.setCellValue("Количество");

    }

    /**
     * Given a sheet, this method deletes a column from a sheet and moves
     * all the columns to the right of it to the left one cell.
     *
     * Note, this method will not update any formula references.
     */
    public static void deleteColumn(Sheet sheet, int columnToDelete){
        int maxColumn = 0;
        for ( int r=0; r < sheet.getLastRowNum()+1; r++ ){
            Row row = sheet.getRow( r );

            // if no row exists here; then nothing to do; next!
            if ( row == null )
                continue;

            // if the row doesn't have this many columns then we are good; next!
            int lastColumn = row.getLastCellNum();
            if ( lastColumn > maxColumn )
                maxColumn = lastColumn;

            if ( lastColumn < columnToDelete )
                continue;

            for ( int x=columnToDelete+1; x < lastColumn + 1; x++ ){
                Cell oldCell    = row.getCell(x-1);
                if ( oldCell != null )
                    row.removeCell( oldCell );

                Cell nextCell   = row.getCell( x );
                if ( nextCell != null ){
                    Cell newCell    = row.createCell( x-1, nextCell.getCellType() );
                    cloneCell(newCell, nextCell);
                }
            }
        }
        // Adjust the column widths
        for ( int c=0; c < maxColumn; c++ ){
            sheet.setColumnWidth( c, sheet.getColumnWidth(c+1) );
        }
    }

    /**
     * Takes an existing Cell and merges all the styles and forumla
     * into the new one
     */
    private static void cloneCell(Cell cNew, Cell cOld){
        cNew.setCellComment(cOld.getCellComment());
        cNew.setCellStyle(cOld.getCellStyle());

        switch ( cNew.getCellTypeEnum() ){
            case BOOLEAN:{
                cNew.setCellValue( cOld.getBooleanCellValue() );
                break;
            }
            case NUMERIC:{
                cNew.setCellValue( cOld.getNumericCellValue() );
                break;
            }
            case STRING:{
                cNew.setCellValue( cOld.getStringCellValue() );
                break;
            }
            case ERROR:{
                cNew.setCellValue( cOld.getErrorCellValue() );
                break;
            }
            case FORMULA:{
                cNew.setCellFormula( cOld.getCellFormula() );
                break;
            }
        }
    }

    private void closeTable() throws IOException {
        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(excelFile);
        workbook.write(fileOut);
        fileOut.close();
    }
}
