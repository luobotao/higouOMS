package utils;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import play.Configuration;


/**
 * export data to excel UTIL
 * @autho
 *
 */
public class ComponentExcel {

    /**
     * export data to excel
     * @param data
     * @return
     */
    public static File toExcel(List<List<String>> data) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Workbook wb = new HSSFWorkbook();
        ExcelGenerateHelper helper = ExcelGenerateHelper.getInstance(wb);
        
        Sheet sheet = wb.createSheet("sheet1");
        
        int rowIndex = 0;
        
        helper.setRowIndex(rowIndex++);
        String[] titles = new String[data.get(0).size()];
        for (int j = 0; j < data.get(0).size(); ++j) {
            titles[j]=(data.get(0).get(j));
        }
        helper.generateHeader(sheet, titles, 0, StringUtils.EMPTY,
        		StringUtils.EMPTY);
        int len = data.size(), leninner;
		// 表头
        for (int i = 1; i < len; i++) {
            leninner = data.get(i).size();
            int colIndex = 0;
            Row row = sheet.createRow(rowIndex++);
            for (int j = 0; j < leninner; ++j) {
            	helper.createStringCell(row, colIndex++, data.get(i).get(j));// 姓名
            }
        }
        String path = Configuration.root().getString("export.path");
        File file = new File(path);
		file.mkdir();//判断文件夹是否存在，不存在就创建
		
		FileOutputStream out = null;
		String fileName = path + "export" + System.currentTimeMillis() + ".xls";
		try {
			out = new FileOutputStream(fileName);
			wb.write(out);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new File(fileName);
    }
}