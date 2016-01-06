package services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.product.Fromsite;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import play.Configuration;
import play.Logger;
import repositories.OrderProductRepository;
import repositories.ShoppingOrderRepository;
import repositories.parcels.ParcelsRepository;
import repositories.product.CategoryRepository;
import repositories.product.ProductRepository;
import services.product.ProductService;
import services.user.UserService;
import utils.Constants;
import utils.ExcelGenerateHelper;
import utils.JdbcOper;
import utils.Numbers;
import vo.CloseTaskVO.Item;
import vo.NotSendOrderVO;
import vo.ScanCodeVO;

/**
 * 
 * <p>Title: JdbcService.java</p> 
 * <p>Description: JDBC操作db查询</p> 
 * <p>Company: changyou</p> 
 * @author  somebody
 * date  2015年7月13日  下午12:20:12
 * @version
 */
@Named
@Singleton
public class JdbcService {

    private static final Logger.ALogger logger = Logger.of(JdbcService.class);

    @Inject
    private ProductRepository productRepository;
    @Inject
    private ShoppingOrderRepository shoppingOrderRepository;
    @Inject
    private ParcelsRepository pardelsRepository;
    @Inject
    private OrderProductRepository orderProductRepository;
    @Inject
    private CategoryRepository categoryRepository;
    @Inject
    private ProductService productService;
    @Inject
    private UserService userService;
    
    /**
     * 
     * <p>Title: getHotWordWithUserLog</p> 
     * <p>Description: 获取搜索热词排行榜</p> 
     * @param start		搜索时间范围
     * @param end
     * @param greater	搜索次数大于等于
     * @return
     */
	public List<Item> getHotWordWithUserLog(String start, String end,
			Integer count) {
		if(count==null){
			count=0;
		}
		List<Item> result = new ArrayList<Item>();
		String sql = "SELECT content, COUNT(id) AS cnum FROM user_log WHERE `atype`=3 AND `date_add` BETWEEN '"+start+"' AND '"+end+"' GROUP BY content HAVING cnum>="+count+" ORDER BY cnum DESC";// SQL语句
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				String content = (String) rs.getString(1);
				int cnum = (int) rs.getInt(2);
				Item item = new Item();
				item.setName(content);
				item.setStatus(cnum);
				result.add(item);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
		
	}

	public List<NotSendOrderVO> getNotSendOrders(String start, String end,
			Integer typ) {
		List<NotSendOrderVO> result = new ArrayList<NotSendOrderVO>();
		String sql = "{call get_prdsales_stat3 ('"+start+"','"+end+"',"+typ+")}";// SQL语句// //调用存储过程
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				NotSendOrderVO notSendOrderVO = new NotSendOrderVO();
				notSendOrderVO.dateTxt=rs.getString(1);
				notSendOrderVO.orderCode=rs.getString(2);
				notSendOrderVO.pardelCode=rs.getString(3);
				notSendOrderVO.pid=rs.getInt(4);
				Fromsite fromsite = productService.queryFromsiteById(rs.getInt(5));
				notSendOrderVO.fromsite=fromsite==null?"":fromsite.getName();
				notSendOrderVO.newSku=rs.getString(7);
				notSendOrderVO.title=rs.getString(8);
				notSendOrderVO.count=rs.getInt(9);
				notSendOrderVO.price=Numbers.formatDouble(rs.getDouble(10), "#0.00");
				notSendOrderVO.status=Constants.ProStatus.proStatus2Message(rs.getInt(11));
				result.add(notSendOrderVO);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	/**
	 * 
	 * <p>Title: exportNotSendOrderFile</p> 
	 * <p>Description: 导出未发货订单商品信息</p> 
	 * @param start
	 * @param end
	 * @param typ
	 * @return
	 */
	public File exportNotSendOrderFile(String start, String end, Integer typ) {
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("sheet1");
		sheet.setColumnWidth(0, 6000);
		sheet.setColumnWidth(1, 5000);
		sheet.setColumnWidth(2, 5000);
		sheet.setColumnWidth(3, 5000);
		sheet.setColumnWidth(4, 5000);
		sheet.setColumnWidth(5, 5000);
		sheet.setColumnWidth(6, 5000);
		sheet.setColumnWidth(7, 10000);
		sheet.setColumnWidth(8, 5000);
		sheet.setColumnWidth(9, 5000);
		ExcelGenerateHelper helper = ExcelGenerateHelper.getInstance(wb);

		// 获取行索引
		int rowIndex = 0;
		int colIndex = 0;

		// 表头
		String[] titles = { "日期", "订单号" ,"包裹号" ,"来源网站" ,"商品ID", "NEWSKU","标题" ,"数量" ,"价格" ,"状态"};

		// 生成表头
		helper.setRowIndex(rowIndex++);
		helper.generateHeader(sheet, titles, 0, StringUtils.EMPTY,
				StringUtils.EMPTY);

		// 循环生成数据行
		String sql = "{call get_prdsales_stat3 ('"+start+"','"+end+"',"+typ+")}";// SQL语句// //调用存储过程
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				colIndex = 0;
				Row row = sheet.createRow(rowIndex++);
				helper.createStringCell(row, colIndex++, rs.getString(1));// 
				helper.createStringCell(row, colIndex++, rs.getString(2));//
				helper.createStringCell(row, colIndex++, rs.getString(3));//
				Fromsite fromsite = productService.queryFromsiteById(rs.getInt(5));
				helper.createStringCell(row, colIndex++, fromsite==null?"":fromsite.getName());//
				helper.createStringCell(row, colIndex++, rs.getString(4));//
				helper.createStringCell(row, colIndex++, rs.getString(7));//
				helper.createStringCell(row, colIndex++, rs.getString(8));//
				helper.createStringCell(row, colIndex++, rs.getString(9));//
				helper.createStringCell(row, colIndex++, Numbers.formatDouble(rs.getDouble(10), "#0.00"));//
				helper.createStringCell(row, colIndex++, Constants.ProStatus.proStatus2Message(rs.getInt(11)));//
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		//在application.conf中配置的路径
		String path = Configuration.root().getString("export.path");
		File file = new File(path);
		file.mkdir();//判断文件夹是否存在，不存在就创建
		
		FileOutputStream out = null;
		String fileName = path + "未发货订单（" + start+"-" + end + "）.xls";
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

	/**
	 * 
	 * <p>Title: ShopO2O</p> 
	 * <p>Description: 获取指定日期的指定商户的统计信息</p> 
	 * @param start
	 * @param end
	 * @return
	 */
	public List<ScanCodeVO> getShopO2OStatics(String start, String end, long uid) {
		List<ScanCodeVO> scanCodeVos = new ArrayList<ScanCodeVO>();
		String sql = "{CALL get_shops_statTime('"+start+"','"+end+"',"+uid+")}";// SQL语句// //调用存储过程
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				ScanCodeVO scanCodeVO = new ScanCodeVO();
				scanCodeVO.dateTxt = rs.getString(3);
				User user = userService.findUserById(uid);
				scanCodeVO.uid = user.getNickname();
				int productDetailPv = rs.getInt(30);
				int productDetailUv = rs.getInt(31);
				int mobileLoginSuccessUsers = rs.getInt(42);
				int orderSuccessNums = rs.getInt(36);
				double orderSuccessTotalFee = rs.getDouble(37);
				int newAddUsers = rs.getInt(46);
				int newBuyUsers = rs.getInt(47);
				int buyUsers = rs.getInt(48);
				int rebuy30dayUsers = rs.getInt(49);
				double rebuystat = rs.getDouble(51);
				logger.info("订单成功数："+orderSuccessNums);
				//要求设置商品详情页PV,商品详情页UV,成功手机登录用户数,成功支付订单数,订单金额
				scanCodeVO.productDetailPv = productDetailPv;
				scanCodeVO.productDetailUv = productDetailUv;
				scanCodeVO.mobileLoginSuccessUsers = mobileLoginSuccessUsers;
				scanCodeVO.newAddUsers = newAddUsers;
				scanCodeVO.newBuyUsers = newBuyUsers;
				scanCodeVO.bugUsers = buyUsers;
				scanCodeVO.orderSuccessNums = orderSuccessNums;
				scanCodeVO.orderSuccessTotalFee = Numbers.parseDouble(Numbers.formatDouble(orderSuccessTotalFee, "0.0"),0.0);
				scanCodeVO.reBuy30DayUser = rebuy30dayUsers;
				scanCodeVO.reBuy30DayStat = Numbers.parseDouble(Numbers.formatDouble(rebuystat*100.0, "0.00"),0.0);;
				scanCodeVos.add(scanCodeVO);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return scanCodeVos;
	}

	/**
	 * 
	 * <p>Title: dealSum30A</p> 
	 * <p>Description: 获得30天A的总和</p> 
	 * @return
	 */
	private int dealSum30A() {
		int sum = 0;
		for(int i = 0 ; i < 30; i++){
			sum += reduceRandomNum(10, 20);
		}
		return sum;
	}

	/**
	 * 
	 * <p>Title: reduceRandomNum</p> 
	 * <p>Description: 生成指定范围内的随机数</p> 
	 * @param d
	 * @param e
	 * @return
	 */
	public static double reduceRandomNum(double min, double max) {
		int a = (int) (min*100);
		int b=  (int) (max*100);
        Random random = new Random();
        int s = random.nextInt(b)%(b-a+1) + a;
        //System.out.println(s/100.0);
		return s/100.0;
	}

	public static void main(String[] args) {
		JdbcService.reduceRandomNum(4.5, 5.8);
	}

	/**
	 * 
	 * <p>Title: getScanCodeStatics</p> 
	 * <p>Description: 店铺统计数据</p> 
	 * @param currDate
	 * @param uid
	 * @return
	 */
	public ScanCodeVO getScanCodeStatics(String currDate, long uid) {
		ScanCodeVO scanCodeVO = new ScanCodeVO();
		String sql = "{CALL get_shops_stat('"+currDate+"',"+uid+")}";// 
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 
			while(rs.next()){
				scanCodeVO.dateTxt = currDate;
				User user = userService.findUserById(uid);
				scanCodeVO.uid = user.getNickname();
				scanCodeVO.saomaPv=rs.getInt(1);
				scanCodeVO.saomaUv = rs.getInt(2);
				scanCodeVO.productDetailPv = rs.getInt(3);
				scanCodeVO.productDetailUv = rs.getInt(4);
				scanCodeVO.clickBuyPv = rs.getInt(5);
				scanCodeVO.clickBuyUv = rs.getInt(6);
				scanCodeVO.jiesuanPagePv = rs.getInt(7);
				scanCodeVO.jiesuanPageUv = rs.getInt(8);
				scanCodeVO.zhifuBtnPv = rs.getInt(9);
				scanCodeVO.zhifuBtnUv = rs.getInt(10);
				scanCodeVO.orderSuccessNums = rs.getInt(11);
				scanCodeVO.orderSuccessTotalFee = rs.getDouble(12);
				scanCodeVO.orderSuccessUsers = rs.getInt(13);
				scanCodeVO.wxJianquanUser = rs.getInt(14);
				scanCodeVO.mobileLoginPv = rs.getInt(15);
				scanCodeVO.mobileLoginUv = rs.getInt(16);
				scanCodeVO.mobileLoginSuccessUsers = rs.getInt(17);
				scanCodeVO.orderFinishClickDownPv = rs.getInt(18);
				scanCodeVO.orderFinishClickDownUv = rs.getInt(19);
				scanCodeVO.changeAppUsers = rs.getInt(20);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return scanCodeVO;
	}
}
