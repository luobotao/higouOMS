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
import vo.ImportSfExpressVO;

/**
 * 商品相关Service
 * @author luobotao
 * Date: 2015年4月17日 下午2:26:14
 */
@Named
@Singleton
public class SfExpressService {

    private static final Logger.ALogger logger = Logger.of(SfExpressService.class);
	
    public static Row rowStatic = null;
	public void run_SfOrder(ImportSfExpressVO vo) {
		String sql = "{CALL run_SFOrder('"+vo.orderNum+"','"+vo.datePay+"','"+vo.mechantNum+"',"+vo.counts+",'"+vo.receiver+"','"+vo.parentsAddress+"','"+vo.subAddress+"','"+vo.phone+"','"+vo.cardId+"','"+vo.totalFee+"','"+vo.price+"','"+vo.expenses+"','"+vo.dateOrder+"')}";//SQL语句  //调用存储过程
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
	public void run_SFOrder_act() {
		String sql = "{CALL `run_SFOrder_act`()}";//SQL语句  //调用存储过程
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
	
	public List<ImportSfExpressVO> extractSfInfo(File file) {
		
		List<ImportSfExpressVO> sfExpress = new LinkedList<>();
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
				sfExpress = null;
				return sfExpress;
			}
			// 从第1行开始（不算标题）
			for (int i = 1; i <= lastRowNumber; i++) {
				Row row = sheet.getRow(i);
				if (row == null) {
	                continue;
	            }
				ImportSfExpressVO vo = convert(row);
	            if(vo!=null && "待发货".equals(vo.status)){
//	            if(vo!=null){
	            	sfExpress.add(vo);
	            }
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			sfExpress = null;
		}
        return sfExpress;
    }
	
	private ImportSfExpressVO convert(Row row) {
        ImportSfExpressVO vo = new ImportSfExpressVO();
        vo.orderNum = row.getCell(0, Row.RETURN_NULL_AND_BLANK)==null?"":row.getCell(0, Row.RETURN_NULL_AND_BLANK).toString();//订单号
        vo.mechantNum = row.getCell(8, Row.RETURN_NULL_AND_BLANK)==null?"":row.getCell(8, Row.RETURN_NULL_AND_BLANK).toString();//商户商品编号
        vo.counts = Numbers.parseInt(row.getCell(11, Row.RETURN_NULL_AND_BLANK)==null?"":row.getCell(11, Row.RETURN_NULL_AND_BLANK).toString(), 0);//商品数量
        vo.price = row.getCell(12, Row.RETURN_NULL_AND_BLANK)==null?"":row.getCell(12, Row.RETURN_NULL_AND_BLANK).toString();//商品 单价
        
        if(StringUtils.isBlank(vo.orderNum)){
        	vo.orderNum = rowStatic.getCell(0, Row.RETURN_NULL_AND_BLANK).toString();//订单号
        }else{
        	rowStatic = row;
        }
        
        vo.status = rowStatic.getCell(2, Row.RETURN_NULL_AND_BLANK).toString();//状态 待发货
        vo.totalFee = rowStatic.getCell(15, Row.RETURN_NULL_AND_BLANK).toString();//总金额(不含运费)
        vo.dateOrder = rowStatic.getCell(16, Row.RETURN_NULL_AND_BLANK).toString();//订单日期 2015-04-21 17:57
        vo.expenses = rowStatic.getCell(17, Row.RETURN_NULL_AND_BLANK).toString();//运费
        vo.datePay = rowStatic.getCell(18, Row.RETURN_NULL_AND_BLANK).toString();//支付日期 2015-04-21 17:57
        vo.receiver = rowStatic.getCell(19, Row.RETURN_NULL_AND_BLANK).toString();//收货人
        String address = rowStatic.getCell(20, Row.RETURN_NULL_AND_BLANK).toString();
        String addressArray[]=address.split("/");
        int arrayLength = addressArray.length;
        if(arrayLength>1){
        	vo.subAddress = addressArray[arrayLength-1];//收货人地址（北城新区齐鲁园西门，Excel文件中收货地址的前三截之后的地址）
        	vo.parentsAddress = address.replace(vo.subAddress, "").replaceAll("/", "");//收货人地址（北京北京市朝阳区，Excel文件中收货地址的前三截）
        }else{
        	int cityNum = address.indexOf("市");
        	if(cityNum>0){
        		cityNum++;
        		vo.parentsAddress = address.substring(0, cityNum).replaceAll("/", "");//收货人地址（北京北京市朝阳区，Excel文件中收货地址的前三截）
        		vo.subAddress = address.replace(vo.parentsAddress, "").replaceAll("/", "");//收货人地址（北城新区齐鲁园西门，Excel文件中收货地址的前三截之后的地址）
        	}else{
        		int provinceNum = address.indexOf("省");
        		if(provinceNum>0){
        			provinceNum++;
            		vo.parentsAddress = address.substring(0, provinceNum).replaceAll("/", "");//收货人地址（北京北京市朝阳区，Excel文件中收货地址的前三截）
            		vo.subAddress = address.replace(vo.parentsAddress, "").replaceAll("/", "");//收货人地址（北城新区齐鲁园西门，Excel文件中收货地址的前三截之后的地址）
            	}else{
            		vo.parentsAddress = "";//收货人地址（北京北京市朝阳区，Excel文件中收货地址的前三截）
            		vo.subAddress =  address;
            	}
        	}
        }
        vo.phone = rowStatic.getCell(21, Row.RETURN_NULL_AND_BLANK).toString();//收货人电话
        //vo.userName = rowStatic.getCell(29, Row.RETURN_NULL_AND_BLANK).toString();//客户姓名
        vo.cardId = rowStatic.getCell(32, Row.RETURN_NULL_AND_BLANK).toString();//身份证号
        
        
        return vo;
    }
	
	
	public File exportSfFile(String start,String end) {
		
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
		String sql = "call get_sfMail('"+start+"','"+end+"')";//SQL语句  //调用存储过程
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
			helper.createStringCell(row, colIndex++, orderCode);// 姓名
			helper.createStringCell(row, colIndex++, mailNum);//部门
			helper.createStringCell(row, colIndex++, remark);//职位
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
		String fileName = path + "sfExpress" + System.currentTimeMillis() + ".xls";
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
