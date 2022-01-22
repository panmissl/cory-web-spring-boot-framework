package com.cory.util.excel;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * how to use:
 * <li>for single sheet: ExcelWriter.write(Table, OS)</li>
 * <li>for multi sheet: ExcelWriter.write(Map<String, Table>, OS)</li>
 */
public class ExcelWriter {

    private Workbook workbook;
    private Map<String, Sheet> sheetMap;
    private List<Sheet> sheetList;

    private ExcelWriter() {
        workbook = new XSSFWorkbook();
        sheetMap = new HashMap<>();
        sheetList = new ArrayList<>();
    }

    /**
     * write to output stream for single sheet
     * @param table row: start from 1, col: start from 1, value: value
     * @param os output stream, e.g. FileOutpuStream
     */
    public static void write(Table<Integer, Integer, String> table, OutputStream os) {
        if (null == table) {
            throw new RuntimeException("excel content is null");
        }
        if (null == os) {
            throw new RuntimeException("output is null");
        }

        ExcelWriter writer = new ExcelWriter();
        writer.createSheet(null);
        table.rowMap().entrySet().forEach(entry -> {
            int row = entry.getKey();
            Map<Integer, String> colMap = entry.getValue();
            colMap.entrySet().forEach(colEntry -> writer.write(row, colEntry.getKey(), colEntry.getValue()));
        });

        writer.writeToOutput(os);
    }

    /**
     * write to output stream for multi sheet
     * @param map key: sheetName, table: row: start from 1, col: start from 1, value: value
     * @param os output stream, e.g. FileOutpuStream
     */
    public static void write(Map<String, Table<Integer, Integer, String>> map, OutputStream os) {
        if (null == map) {
            throw new RuntimeException("excel content is null");
        }
        if (null == os) {
            throw new RuntimeException("output is null");
        }

        ExcelWriter writer = new ExcelWriter();

        map.keySet().forEach(sheetName -> writer.createSheet(sheetName));

        map.entrySet().forEach(entry -> {
            String sheetName = entry.getKey();
            Table<Integer, Integer, String> table = entry.getValue();
            table.rowMap().entrySet().forEach(rowEntry -> {
                int row = rowEntry.getKey();
                Map<Integer, String> colMap = rowEntry.getValue();
                colMap.entrySet().forEach(colEntry -> writer.write(sheetName, row, colEntry.getKey(), colEntry.getValue()));
            });
        });

        writer.writeToOutput(os);
    }

    private Sheet createSheet(String name) {
        Sheet sheet;
        if (StringUtils.isBlank(name)) {
            sheet = workbook.createSheet();
        } else {
            sheet = workbook.createSheet(name);
        }
        sheetList.add(sheet);
        sheetMap.put(sheet.getSheetName(), sheet);
        return sheet;
    }

    /**
     * write to first sheet, specified row and col
     * @param row start from 1
     * @param col start from 1
     * @param value value
     */
    private void write(int row, int col, String value) {
        if (this.sheetList.size() == 0) {
            this.createSheet(null);
        }
        write(sheetList.get(0), row, col, value);
    }

    /**
     * write to specified sheet, row and col
     * @param sheetName sheet name
     * @param row start from 1
     * @param col start from 1
     * @param value value
     */
    private void write(String sheetName, int row, int col, String value) {
        if (null == this.sheetMap.get(sheetName)) {
            this.createSheet(sheetName);
        }
        write(this.sheetMap.get(sheetName), row, col, value);
    }

    private void writeToOutput(OutputStream os) {
        try {
            workbook.write(os);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * write a colNum
     * @param sheet sheet
     * @param rowNum start from 1
     * @param colNum start from 1
     * @param value value
     */
    private void write(Sheet sheet, int rowNum, int colNum, String value) {
        if (null == sheet) {
            return;
        }
        if (null == value) {
            value = "";
        }
        if (rowNum < 1) {
            rowNum = 1;
        }
        if (colNum < 1) {
            colNum = 1;
        }
        Row row = sheet.getRow(rowNum - 1);
        if (null == row) {
            row = sheet.createRow(rowNum - 1);
        }
        Cell cell = row.getCell(colNum - 1);
        if (null == cell) {
            cell = row.createCell(colNum - 1);
        }
        cell.setCellValue(value);
    }

    public static void main(String[] args) {
        try {
            Table<Integer, Integer, String> table = HashBasedTable.create();
            table.put(1, 1, "姓名");
            table.put(1, 2, "成绩");
            table.put(2, 1, "张三");
            table.put(2, 2, "98");
            table.put(3, 1, "李四");
            table.put(3, 2, "95");
            FileOutputStream os = new FileOutputStream(new File("D:/test.xlsx"));
            ExcelWriter.write(table, os);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}