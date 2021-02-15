package com.cory.util.excel;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * https://blog.csdn.net/gxx_csdn/article/details/79085713
 */
public class ExcelReader {

    public enum ExcelType {
        XLS("xls"),
        XLSX("xlsx");

        private String extension;
        ExcelType(String extension) {
            this.extension = extension;
        }

        public static ExcelType parse(String extension) {
            if (XLS.extension.equals(extension)) {
                return XLS;
            } else if (XLSX.extension.equals(extension)) {
                return XLSX;
            } else {
                throw new RuntimeException("not supported file type, just support .xls or .xlsx");
            }
        }
    }

    private Workbook workbook;

    public ExcelReader(String fileName) {
        this(new File(fileName));
    }

    public ExcelReader(File file) {
        try {
            String ext = parseExtension(file.getName());
            ExcelType type = ExcelType.parse(ext);
            FileInputStream fis = new FileInputStream(file);
            init(fis, type);
        } catch (IOException e) {
            ex("read file error, please check if the file exists.");
        }
    }

    public ExcelReader(InputStream inputStream, ExcelType type) {
        init(inputStream, type);
    }

    private void init(InputStream inputStream, ExcelType type) {
        try {
            //根据文件后缀（xls/xlsx）进行判断
            if (ExcelType.XLS.equals(type)) {
                workbook = new HSSFWorkbook(inputStream);
            }else if (ExcelType.XLSX.equals(type)){
                workbook = new XSSFWorkbook(inputStream);
            }else {
                ex("not xls/xlsx file!");
            }
        } catch (IOException e) {
            ex("read file error, please check if the file exists.");
        }
    }


    private void ex(String msg) {
        throw new RuntimeException(msg);
    }

    private String parseExtension(String fileName) {
        if (StringUtils.isBlank(fileName) || fileName.indexOf('.') < 0) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    /**
     * read the first sheet
     *
     * @param row -- start from 1
     * @param col -- start from 1
     * @return
     */
    public String read(int row, int col) {
        return read(workbook.getSheetAt(0), row, col);
    }

    /**
     * read the cell from specified sheet
     *
     * @param sheetName
     * @param row -- start from 1
     * @param col -- start from 1
     * @return
     */
    public String read(String sheetName, int row, int col) {
        return read(workbook.getSheet(sheetName), row, col);
    }

    private String read(Sheet sheet, int rowNum, int colNum) {
        if (null == sheet) {
            return null;
        }
        int maxRowNumber = sheet.getLastRowNum() + 1;
        if (rowNum > maxRowNumber) {
            return null;
        }
        if (rowNum < 1) {
            rowNum = 1;
        }
        if (colNum < 1) {
            colNum = 1;
        }
        Row row = sheet.getRow(rowNum - 1);
        Cell cell = row.getCell(colNum - 1);
        return null == cell ? null : cell.toString();
    }

    public static void main(String[] args) {
        String fileName = ExcelReader.class.getClassLoader().getResource("test.xlsx").getFile();
        ExcelReader reader = new ExcelReader(fileName);

        System.out.println("sheet 1, row 3, col 4: " + reader.read(3, 4));
        System.out.println("sheet test2Sheet, row 3, col 4: " + reader.read("test2Sheet", 3, 4));
    }
}