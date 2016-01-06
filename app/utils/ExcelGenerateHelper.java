package utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * 数据导出到Excel工具类
 * 
 * @author luo
 * 
 */
public class ExcelGenerateHelper {
	/** 标题单元格格式 */
	public final static String CELL_STYLE_HEADER = "headerCell";
	/** 字符单元格格式 */
	public final static String CELL_STYLE_STRING = "stringCell";
	/** 数字单元格格式 */
	public final static String CELL_STYLE_NUMBER = "numberCell";
	/** 日期单元格格式 */
	public final static String CELL_STYLE_DATE = "dateCell";

	public Map<String, CellStyle> styles;
	private int rowIndex = 0;

	public static ExcelGenerateHelper getInstance(Workbook wb) {
		return new ExcelGenerateHelper(wb);
	}

	private ExcelGenerateHelper(Workbook wb) {
		createCellStyles(wb);
	}

	/**
	 * 生成表头
	 * 
	 * @param sheet
	 * @param titleArray
	 */
	public void generateHeader(Sheet sheet, String[] titleArray) {
		generateHeader(sheet, titleArray, 0, org.apache.commons.lang3.StringUtils.EMPTY,
				org.apache.commons.lang3.StringUtils.EMPTY);
	}

	/**
	 * 生成表头
	 * 
	 * @param sheet
	 * @param titleArray
	 * @param colStartIndex
	 *            指定列的开始索引
	 * @param valuePrefix
	 *            单元格值的前缀
	 * @param valueSuffix
	 *            单元格值的后缀
	 */
	public void generateHeader(Sheet sheet, String[] titleArray,
			int colStartIndex, String valuePrefix, String valueSuffix) {
		Row row = sheet.createRow(rowIndex++);
		row.setHeightInPoints(30);
		CellStyle headerStyle = styles.get(CELL_STYLE_HEADER);

		int arrayLength = titleArray.length;
		for (int i = 0; i < arrayLength; i++) {
			Cell c = row.createCell(i + colStartIndex);
			c.setCellStyle(headerStyle);
			c.setCellValue(valuePrefix + titleArray[i] + valueSuffix);
			setBorder(headerStyle);
		}
	}

	/**
	 * 创建所有的单元格格式
	 * 
	 * @param wb
	 * @return
	 */
	private Map<String, CellStyle> createCellStyles(Workbook wb) {
		styles = new HashMap<String, CellStyle>();
		DataFormat df = wb.createDataFormat();

		// --字体设定 --//

		// 普通字体
		Font normalFont = wb.createFont();
		normalFont.setFontHeightInPoints((short) 10);

		// 加粗字体
		Font boldFont = wb.createFont();
		boldFont.setFontHeightInPoints((short) 10);
		boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

		// 蓝色加粗字体
		Font blueBoldFont = wb.createFont();
		blueBoldFont.setFontHeightInPoints((short) 10);
		blueBoldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		blueBoldFont.setColor(IndexedColors.BLUE.getIndex());
		
		// 黑色加粗字体
		Font blackBoldFont = wb.createFont();
		blackBoldFont.setFontHeightInPoints((short) 10);
		blackBoldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		blackBoldFont.setColor(IndexedColors.BLACK.getIndex());	

		// --Cell Style设定-- //

		// 标题格式
		CellStyle headerStyle = wb.createCellStyle();
		headerStyle.setFont(blackBoldFont);
		headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
		headerStyle.setVerticalAlignment(CellStyle.VERTICAL_BOTTOM);
		styles.put(CELL_STYLE_HEADER, headerStyle);

		// 字符串格式
		CellStyle stringCellStyle = wb.createCellStyle();
		stringCellStyle.setFont(normalFont);
		stringCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		stringCellStyle.setVerticalAlignment(CellStyle.VERTICAL_BOTTOM);
		setBorder(stringCellStyle);
		styles.put(CELL_STYLE_STRING, stringCellStyle);

		// 数字格式
		CellStyle numberCellStyle = wb.createCellStyle();
		numberCellStyle.setFont(normalFont);
		setBorder(numberCellStyle);
		styles.put(CELL_STYLE_NUMBER, numberCellStyle);

		// 日期格式
		CellStyle dateCellStyle = wb.createCellStyle();
		dateCellStyle.setFont(normalFont);
		dateCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		dateCellStyle.setVerticalAlignment(CellStyle.VERTICAL_BOTTOM);
		dateCellStyle.setDataFormat(df.getFormat("yyyy-MM-dd"));
		setBorder(dateCellStyle);
		styles.put(CELL_STYLE_DATE, dateCellStyle);

		return styles;
	}

	/**
	 * 为单元格样式设置边框
	 * 
	 * @param cellStyle
	 * @return
	 */
	private CellStyle setBorder(CellStyle cellStyle) {
		// 上边框
		cellStyle.setBorderTop(CellStyle.BORDER_THIN);
		cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());

		// 下边框
		cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
		cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());

		// 左边框
		cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
		cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());

		// 右边框
		cellStyle.setBorderRight(CellStyle.BORDER_THIN);
		cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());

		return cellStyle;
	}

	/**
	 * 创建字符串单元格
	 * 
	 * @param row
	 * @param colIndex
	 * @param cellStyle
	 * @param stringValue
	 * @return
	 */
	public Cell createStringCell(Row row, int colIndex, String stringValue) {
		CellStyle stringCellStyle = styles.get(CELL_STYLE_STRING);
		return createCell(row, colIndex, stringCellStyle, stringValue, null,
				null);

	}
	/**
	 * 去除边框
	 * @param cell
	 */
	public void removeBorder(Cell cell){
		cell.getCellStyle().setBorderTop((short)0);
		cell.getCellStyle().setBorderBottom((short)0);
		cell.getCellStyle().setBorderLeft((short)0);
		cell.getCellStyle().setBorderRight((short)0);
	}

	/**
	 * 创建数字单元格
	 * 
	 * @param row
	 * @param colIndex
	 * @param doubleValue
	 * @return
	 */
	public Cell createDoubleCell(Row row, int colIndex, Double doubleValue) {
		CellStyle numberCellStyle = styles.get(CELL_STYLE_NUMBER);
		return createCell(row, colIndex, numberCellStyle, org.apache.commons.lang3.StringUtils.EMPTY,
				doubleValue, null);

	}

	/**
	 * 创建日期单元格
	 * 
	 * @param row
	 * @param colIndex
	 * @param dateValue
	 * @return
	 */
	public Cell createDateCell(Row row, int colIndex, Date dateValue) {
		CellStyle dateCellStyle = styles.get(CELL_STYLE_DATE);
		return createCell(row, colIndex, dateCellStyle, org.apache.commons.lang3.StringUtils.EMPTY,
				null, dateValue);

	}

	/**
	 * 创建单元格
	 * 
	 * @param row
	 * @param colIndex
	 * @param cellStyle
	 * @param stringValue
	 * @param doubleValue
	 * @param dateValue
	 * @return
	 */
	private Cell createCell(Row row, int colIndex, CellStyle cellStyle,
			String stringValue, Double doubleValue, Date dateValue) {
		Cell cell = row.createCell(colIndex);
		cell.setCellStyle(cellStyle);

		if (org.apache.commons.lang3.StringUtils.isNotBlank(stringValue)) {
			cell.setCellValue(stringValue);
		} else if (doubleValue != null) {
			cell.setCellValue(doubleValue);
		} else if (dateValue != null) {
			cell.setCellValue(dateValue);
		}

		return cell;
	}

	public int getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

}
