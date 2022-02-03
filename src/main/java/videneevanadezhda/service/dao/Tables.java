package videneevanadezhda.service.dao;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.javatuples.Pair;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class Tables {
    /**
     * Creates table, values given as pairs
     *
     * @param value       data to fill in the table
     * @param fileName
     * @param columnNames
     * @throws IOException
     */
    public <T, S> void createTableFromPair(ArrayList<Pair<T, S>> value,
                                           String fileName, ArrayList<String> columnNames)
            throws IOException {
        try (XSSFWorkbook table = new XSSFWorkbook()) {
            XSSFSheet sheetExcel = table.createSheet(fileName);

            CellStyle style = table.createCellStyle();
            Font font = table.createFont();
            font.setBold(true);
            style.setFont(font);
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setLocked(true);

            XSSFRow firstRow = sheetExcel.createRow(0);
            Cell currentCell = firstRow.createCell(0);
            currentCell.setCellValue(columnNames.get(0));
            currentCell.setCellStyle(style);
            currentCell = firstRow.createCell(1);
            currentCell.setCellValue(columnNames.get(1));
            currentCell.setCellStyle(style);

            for (int i = 0; i < value.size(); ++i) {
                firstRow = sheetExcel.createRow(i + 1);
                if (value.get(i).getValue0() instanceof String) {
                    firstRow.createCell(0).setCellValue((String) value.get(i).getValue0());
                } else if (value.get(i).getValue0() instanceof Integer) {
                    firstRow.createCell(0).setCellValue((Integer) value.get(i).getValue0());
                }
                if (value.get(i).getValue1() instanceof String) {
                    firstRow.createCell(1).setCellValue((String) value.get(i).getValue1());
                } else if (value.get(i).getValue1() instanceof Integer) {
                    firstRow.createCell(1).setCellValue((Integer) value.get(i).getValue1());
                }
            }

            for (int x = 0; x < sheetExcel.getRow(0).getPhysicalNumberOfCells(); x++) {
                sheetExcel.autoSizeColumn(x);
            }

            table.write(new FileOutputStream(fileName + ".xlsx"));
        }
    }

    /**
     * Creates table, values given as array of strings
     *
     * @param value       data to fill in the table
     * @param fileName
     * @param columnNames
     * @throws IOException
     */
    public void createTableFromArray(ArrayList<ArrayList<String>> value,
                                     String fileName, ArrayList<String> columnNames)
            throws IOException {
        try (XSSFWorkbook table = new XSSFWorkbook()) {
            XSSFSheet sheetExcel = table.createSheet(fileName);

            CellStyle style = table.createCellStyle();
            Font font = table.createFont();
            font.setBold(true);
            style.setFont(font);
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setLocked(true);

            XSSFRow firstRow = sheetExcel.createRow(0);
            for (int i = 0; i < columnNames.size(); ++i) {
                Cell currentCell = firstRow.createCell(i);
                currentCell.setCellValue(columnNames.get(i));
                currentCell.setCellStyle(style);
            }

            for (int i = 0; i < value.size(); ++i) {
                Row row = sheetExcel.createRow(i + 1);
                for (int j = 0; j < value.get(i).size(); ++j) {
                    row.createCell(j).setCellValue(value.get(i).get(j));
                }
            }

            for (int x = 0; x < sheetExcel.getRow(0).getPhysicalNumberOfCells(); x++) {
                sheetExcel.autoSizeColumn(x);
            }

            table.write(new FileOutputStream(fileName + ".xlsx"));
        }
    }
}
