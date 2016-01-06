package services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import play.Configuration;
import play.Logger;
import utils.ExcelGenerateHelper;
import utils.JdbcOper;
import utils.JdbcOperWithClose;
import utils.Numbers;
import vo.ImportNeolixVO;

/**
 * 
 * <p>Title: NeolixService.java</p> 
 * <p>Description: 新石器手机</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年7月22日  上午11:27:51
 * @version
 */
@Named
@Singleton
public class NeolixService {

    private static final Logger.ALogger logger = Logger.of(NeolixService.class);
	
    public static Row rowStatic = null;
	public void run_NeolixOrder(ImportNeolixVO vo) {
		String sql = "{CALL run_NeolixOrder('"+vo.orderNum+"','"+vo.datePay+"','"+vo.mechantNum+"',"+vo.counts+",'"+vo.receiver+"','"+vo.parentsAddress+"','"+vo.subAddress+"','"+vo.phone+"','','"+vo.totalFee+"','"+vo.price+"','"+vo.expenses+"','"+vo.dateOrder+"')}";//SQL语句  //调用存储过程
		logger.debug(sql);
		JdbcOper  db = JdbcOper.getInstance();//创建DBHelper对象  
        try {  
       	 db.getPrepareStateDao(sql);
       	 db.pst.executeQuery();//执行语句，得到结果集  
        } catch (Exception e) {  
            e.printStackTrace();  
        }finally{
       	 db.close();
        }  
    }
	
	public List<ImportNeolixVO> extractNeolixInfo(File file) {
		
		List<ImportNeolixVO> neolix = new LinkedList<>();
		// 解析文件
		Workbook workBook = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("没有找到对应的文件", e);
		}
		try {
			workBook = WorkbookFactory.create(fis);
			Sheet sheet = workBook.getSheetAt(0);
			int lastRowNumber = sheet.getLastRowNum();
			Row rowTitle = sheet.getRow(0);
			if (rowTitle == null) {
				neolix = null;
				return neolix;
			}
			// 从第1行开始（不算标题）
			for (int i = 1; i <= lastRowNumber; i++) {
				Row row = sheet.getRow(i);
				if (row == null) {
	                continue;
	            }
				ImportNeolixVO vo = convert(row);
	            neolix.add(vo);
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			neolix = null;
		}
        return neolix;
    }
	
	private ImportNeolixVO convert(Row row) {
        ImportNeolixVO vo = new ImportNeolixVO();
        vo.orderNum = row.getCell(0, Row.RETURN_NULL_AND_BLANK)==null?"":row.getCell(0, Row.RETURN_NULL_AND_BLANK).toString();//订单号
        vo.mechantNum = row.getCell(1, Row.RETURN_NULL_AND_BLANK)==null?"":row.getCell(1, Row.RETURN_NULL_AND_BLANK).toString();//商户商品编号
        vo.counts = Numbers.parseInt(row.getCell(3, Row.RETURN_NULL_AND_BLANK)==null?"":row.getCell(3, Row.RETURN_NULL_AND_BLANK).toString(), 0);//商品数量
        vo.price = row.getCell(4, Row.RETURN_NULL_AND_BLANK)==null?"":row.getCell(4, Row.RETURN_NULL_AND_BLANK).toString();//商品 单价
        
        if(StringUtils.isBlank(vo.orderNum)){
        	vo.orderNum = rowStatic.getCell(0, Row.RETURN_NULL_AND_BLANK).toString();//订单号
        }else{
        	rowStatic = row;
        }
        
        vo.totalFee = rowStatic.getCell(5, Row.RETURN_NULL_AND_BLANK).toString();//总金额(不含运费)
        vo.dateOrder = rowStatic.getCell(6, Row.RETURN_NULL_AND_BLANK).toString();//订单日期 2015-04-21 17:57
        vo.expenses = rowStatic.getCell(7, Row.RETURN_NULL_AND_BLANK).toString();//运费
        vo.datePay = rowStatic.getCell(8, Row.RETURN_NULL_AND_BLANK).toString();//支付日期 2015-04-21 17:57
        vo.receiver = rowStatic.getCell(9, Row.RETURN_NULL_AND_BLANK).toString();//收货人
        vo.parentsAddress = rowStatic.getCell(10, Row.RETURN_NULL_AND_BLANK).toString();//收货人所在省
        vo.subAddress = rowStatic.getCell(11, Row.RETURN_NULL_AND_BLANK).toString();//收货人所在市
        
        vo.phone = rowStatic.getCell(12, Row.RETURN_NULL_AND_BLANK).toString();//收货人电话
        
        return vo;
    }
	
	
	public File exportNeolixFile(String start,String end) {
		
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("sheet1");
		sheet.setColumnWidth(0, 6000);
		sheet.setColumnWidth(1, 5000);
		sheet.setColumnWidth(2, 5000);
		ExcelGenerateHelper helper = ExcelGenerateHelper.getInstance(wb);

		// 获取行索引
		int rowIndex = 0;
		int colIndex = 0;

		// 表头
		String[] titles = { "订单编号", "运单号", "发货备注" };

		// 生成表头
		helper.setRowIndex(rowIndex++);
		helper.generateHeader(sheet, titles, 0, StringUtils.EMPTY,
				StringUtils.EMPTY);

		// 循环生成数据行
		String sql = "call get_NeolixMail('"+start+"','"+end+"')";//SQL语句  //调用存储过程
		logger.info(sql);
		JdbcOperWithClose  db = JdbcOperWithClose.getInstance();//创建DBHelper对象  
        try {  
       	 db.getPrepareStateDao(sql);
       	 ResultSet rs = db.pst.executeQuery();//执行语句，得到结果集  
       	 while(rs.next()){
       		 String orderCode = rs.getString("orderCode");
       		 String mailNum = rs.getString("mailnum");
       		 String remark = rs.getString("remark");
       		colIndex = 0;
			Row row = sheet.createRow(rowIndex++);
			helper.createStringCell(row, colIndex++, orderCode);// 订单号
			helper.createStringCell(row, colIndex++, mailNum);//运单号
			helper.createStringCell(row, colIndex++, remark);//备注
       	 }
        } catch (SQLException e) {  
            e.printStackTrace();  
        }finally{
       	 db.close();
        }  
		//在application.conf中配置的路径
		String path = Configuration.root().getString("export.path");
		File file = new File(path);
		file.mkdir();//判断文件夹是否存在，不存在就创建
		
		FileOutputStream out = null;
		String fileName = path + "neolix" + System.currentTimeMillis() + ".xls";
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
