package controllers.statistics;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.admin.AdminUser;
import models.product.Category;
import models.product.Fromsite;
import models.product.Product;
import models.user.User;

import org.apache.commons.codec.binary.Base64;

import play.Logger;
import play.data.Form;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.twirl.api.Html;
import services.JdbcService;
import services.admin.AdminUserService;
import services.product.ProductService;
import services.user.UserService;
import utils.Constants;
import utils.Constants.Devices;
import utils.Dates;
import utils.Htmls;
import vo.CloseTaskVO.Item;
import vo.ImportAddStockVO;
import vo.NotSendOrderVO;
import vo.OrderStaticsVO;
import vo.ProductSellTopVO;
import vo.ScanCodeVO;
import controllers.BaseController;
import controllers.routes;
import controllers.admin.AdminAuthenticated;
import forms.DateSimpleBetween;
import forms.admin.FreshTaskQueryForm;

/**
 * 阿里支付Controller
 * 
 * @author luobotao
 * 
 */
@Named
@Singleton
public class OperationController extends BaseController {
	private static final Logger.ALogger logger = Logger.of(OperationController.class);
	private static final int GIDVAL = 4;
	
	private final String error = "addStock";
	private final Form<FreshTaskQueryForm> freshTaskQueryForm = Form.form(FreshTaskQueryForm.class);
	
    private final ProductService productService;
    private final JdbcService jdbcService;
    private final UserService userService;
    private final AdminUserService adminUserService;
    @Inject
    public OperationController(final ProductService productService, final JdbcService jdbcService,final UserService userService, final AdminUserService adminUserService) {
        this.productService = productService;
        this.jdbcService = jdbcService;
        this.userService = userService;
        this.adminUserService = adminUserService;
    }
	/**
	 * 跳转至增加库存页面
	 * @return
	 */
    @AdminAuthenticated()
	public Result addStock() {
		return ok(views.html.statistics.addStock.render(flash(error)));
	}
    //处理修改库存
    @AdminAuthenticated()
    public Result addStockHandle() {
    	String stockRadioValue =Form.form().bindFromRequest().get("stockRadio");
    	MultipartFormData body = request().body().asMultipartFormData();
        FilePart stockFile = body.getFile("stockFile");
        if (stockFile != null) {
            String fileName = stockFile.getFilename();
            if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                File stockExcel = stockFile.getFile();
                List<ImportAddStockVO> importAddStockVOList = productService.extractAddStockInfo(stockExcel);
                if(importAddStockVOList==null){
                	 flash(error, "文件内容非法");
                     return redirect(routes.Application.sfImportManage());
                }
                for(ImportAddStockVO importAddStockVO:importAddStockVOList){
                	logger.info(importAddStockVO.pid+"===="+importAddStockVO.stock);
                	Product product = productService.findProductById(importAddStockVO.pid);
                	if(product!=null){
                		Integer stockNow = product.getStock();
                		if("sub".equals(stockRadioValue)){
                			stockNow = product.getStock()-importAddStockVO.stock;
                		}else if("add".equals(stockRadioValue)){
                			stockNow = product.getStock()+importAddStockVO.stock;
                		}
                		productService.updateProductStock(stockNow,importAddStockVO.pid);
                	}
                }
                flash(error, "导入成功");
                return redirect("/statistics/operation/addStock");
            } else {
                flash(error, "文件格式错误");
                return redirect("/statistics/operation/addStock");
            }
        } else {
            flash(error, "上传的excel文件不存在");
            return redirect("/statistics/operation/addStock");
        }
    }
	/**
	 * 商品访购
	 * @return
	 */
    @AdminAuthenticated()
	public Result vistManage() {
		
		Form<FreshTaskQueryForm> formRequest = freshTaskQueryForm.bindFromRequest();
		FreshTaskQueryForm form = new FreshTaskQueryForm();
		if (!formRequest.hasErrors()) {
			form = formRequest.get();
		}
		String start ="";
		String end ="";
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
		if(form.simpleBetween==null){
			end = DATE_FORMAT.format(new Date());
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.DAY_OF_MONTH,-7);
			start = DATE_FORMAT.format(cal.getTime());
			form.simpleBetween = new DateSimpleBetween();
			form.simpleBetween.start=cal.getTime();
			form.simpleBetween.end=new Date();
		}else{
			start = DATE_FORMAT.format(form.simpleBetween.start);
			end = DATE_FORMAT.format(form.simpleBetween.end);
		}
		List<ProductSellTopVO> productSellTopVOList = productService.getProductVistTopInfo(start,end,form.typ,form.fromsite,form.proInfo);
		List<Fromsite> fromsiteList  = productService.queryAllFromsite();
		return ok(views.html.statistics.vistManage.render(productSellTopVOList,form,Html.apply(Constants.Devices.devices2HTML(form.device)),Html.apply(Constants.Typs.typs2HTML(form.typ)),Html.apply(ProductService.fromsiteList2Html(fromsiteList,form.fromsite))));
	}
	
	/**
	 * 商品销量统计
	 * @return
	 */
    @AdminAuthenticated()
	public Result productsSellManage() {
		
		Form<FreshTaskQueryForm> formRequest = freshTaskQueryForm.bindFromRequest();
		FreshTaskQueryForm form = new FreshTaskQueryForm();
		if (!formRequest.hasErrors()) {
			form = formRequest.get();
		}
		String start ="";
		String end ="";
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
		List<AdminUser> adminUserList  = adminUserService.queryAllLyAdminUser();		//查找出所有的联营商户
		if(form.simpleBetween==null){
			start = DATE_FORMAT.format(new Date());
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.DAY_OF_MONTH, 1);
			end = DATE_FORMAT.format(cal.getTime());
			form.simpleBetween = new DateSimpleBetween();
			form.simpleBetween.start=new Date();
			form.simpleBetween.end=cal.getTime();
		}else{
			start = DATE_FORMAT.format(form.simpleBetween.start);
			end = DATE_FORMAT.format(form.simpleBetween.end);
		}
		List<ProductSellTopVO> productSellTopVOList = productService.getProductSellTopInfo(start,end,form.greater,form.proInfo,form.typ,form.countTyp,form.adminid);
		return ok(views.html.statistics.productsSellManage.render(productSellTopVOList,form,Html.apply(getCategoryHTML(form.categoryId)),Html.apply(Constants.Typs.typs2HTML(form.typ)),Html.apply(Constants.CountTyps.countTyps2HTML(form.countTyp)),Html.apply(AdminUserService.adminUserList2Html(adminUserList,form.adminid))));
	}
    
    /**
     * 
     * <p>Title: hotWordManage</p> 
     * <p>Description: 搜索热词</p> 
     * @return
     * @throws ParseException 
     */
    @AdminAuthenticated()
	public Result hotWordManage() throws ParseException {
		
		Form<FreshTaskQueryForm> formRequest = freshTaskQueryForm.bindFromRequest();
		FreshTaskQueryForm form = new FreshTaskQueryForm();
		if (!formRequest.hasErrors()) {
			form = formRequest.get();
		}
		String start ="";
		String end ="";
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
		if(form.simpleBetween==null){
			start = DATE_FORMAT.format(new Date());
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.DAY_OF_MONTH, 1);
			end = DATE_FORMAT.format(cal.getTime());
			form.simpleBetween = new DateSimpleBetween();
			form.simpleBetween.start=new Date();
			form.simpleBetween.end=cal.getTime();
		}else{
			start = DATE_FORMAT.format(form.simpleBetween.start);
			end = DATE_FORMAT.format(form.simpleBetween.end);
		}
		List<Item> itemList = jdbcService.getHotWordWithUserLog(start,end,form.greater);
		return ok(views.html.statistics.hotWordManage.render(itemList,form));
	
    }
    
	/**
	 * 订单数据
	 * @return
	 */
    @AdminAuthenticated()
	public Result orderStatics() {
		
		Form<FreshTaskQueryForm> formRequest = freshTaskQueryForm.bindFromRequest();
		FreshTaskQueryForm form = new FreshTaskQueryForm();
		if (!formRequest.hasErrors()) {
			form = formRequest.get();
		}
		String start ="";
		String end ="";
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
		if(form.simpleBetween==null){
			end = DATE_FORMAT.format(new Date());
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.DAY_OF_MONTH, -7);
			start = DATE_FORMAT.format(cal.getTime());
			
			form.simpleBetween = new DateSimpleBetween();
			form.simpleBetween.start=cal.getTime();
			form.simpleBetween.end=new Date();;
		}else{
			start = DATE_FORMAT.format(form.simpleBetween.start);
			end = DATE_FORMAT.format(form.simpleBetween.end);
		}
		List<OrderStaticsVO> orderStaticsVOList = productService.getOrderStatics(start,end);
		List<OrderStaticsVO> dealOrderStaticsVOList = new ArrayList<OrderStaticsVO>();
		if(orderStaticsVOList.size() > 2){
			int length = orderStaticsVOList.size();
			dealOrderStaticsVOList.add(orderStaticsVOList.get(length-2));
			dealOrderStaticsVOList.add(orderStaticsVOList.get(length-1));
			orderStaticsVOList.remove(length-1);
			orderStaticsVOList.remove(length-2);
		}
		return ok(views.html.statistics.orderStatics.render(orderStaticsVOList,dealOrderStaticsVOList,form,Html.apply(getCategoryHTML(form.categoryId)),Html.apply(Constants.Devices.devices2HTML(form.device))));
	}
    /**
     * 未发货订单数据
     * @return
     * @throws ParseException 
     */
    @AdminAuthenticated()
    public Result notSendOrderManage() throws ParseException {
    	
    	Form<FreshTaskQueryForm> formRequest = freshTaskQueryForm.bindFromRequest();
    	FreshTaskQueryForm form = new FreshTaskQueryForm();
    	if (!formRequest.hasErrors()) {
    		form = formRequest.get();
    	}
    	String start ="";
    	String end ="";
    	SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    	if(form.simpleBetween==null){
    		Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.DAY_OF_MONTH, 1);
			end = DATE_FORMAT.format(cal.getTime());
    		start = "2014-01-01";
    		if(form.typ==0){
    			form.typ=1;
    		}
    		form.simpleBetween = new DateSimpleBetween();
			form.simpleBetween.start=DATE_FORMAT.parse(start);
			form.simpleBetween.end=cal.getTime();
    	}else{
    		start = DATE_FORMAT.format(form.simpleBetween.start);
    		end = DATE_FORMAT.format(form.simpleBetween.end);
    	}
    	List<NotSendOrderVO> notSendOrderVOList = jdbcService.getNotSendOrders(start,end,form.typ);
    	return ok(views.html.statistics.notSendOrderManage.render(notSendOrderVOList,form,Html.apply(Constants.Typs.typs2HTML(form.typ))));
    }
	
    @AdminAuthenticated()
	public String getCategoryHTML(Integer id){
		//获取品类
		List<Category> categoryList = productService.getCategoryList();
		StringBuilder sb = new StringBuilder();
        Devices[] devices = Devices.values();
        sb.append(Htmls.generateOption(-1, "默认全部"));
        for (Category category : categoryList) {
            if (id.equals(category.getId())) {
                sb.append(Htmls.generateSelectedOption(category.getId(),
                		category.getName()));
            } else {
            	 sb.append(Htmls.generateOption(category.getId(),
                 		category.getName()));
            }
        }
        return sb.toString();
	}
	
	/**
	 * 没有发货的订单导出Excel文件
	 * @return
	 */
	@AdminAuthenticated()
	public Result notSendOrderExportFile(){
		FreshTaskQueryForm form =  Form.form(FreshTaskQueryForm.class).bindFromRequest(request()).get();
		SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd");
		String start = formate.format(form.simpleBetween.start);
		String end = formate.format(form.simpleBetween.end);
		File downFile = jdbcService.exportNotSendOrderFile(start,end,form.typ);
		String fileNameChine = "未发货订单（"+start+"-"+end+"）.xls";
		try {
			if (request().getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
				fileNameChine = "=?UTF-8?B?" + (new String(Base64.encodeBase64(fileNameChine.getBytes("UTF-8")))) + "?=";    
	        } else{
	        	fileNameChine = java.net.URLEncoder.encode(fileNameChine, "UTF-8");
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		response().setHeader("Content-disposition","attachment;filename=" + fileNameChine);
		response().setContentType("application/msexcel");
		return ok(downFile);
	}

	/**
	 * 店铺统计
	 * @return
	 */
    @AdminAuthenticated()
	public Result scanCodeStatics() {
		
		Form<FreshTaskQueryForm> formRequest = freshTaskQueryForm.bindFromRequest();
		FreshTaskQueryForm form = new FreshTaskQueryForm();
		long uid = 0L;
		if (!formRequest.hasErrors()) {
			form = formRequest.get();
			uid = form.uid;
		}
		AdminUser adminUser = getCurrentAdminUser();
		if("3".equals(adminUser.getAdminType())){
			uid = adminUser.getUid();
		}
		
		List<ScanCodeVO> scanCodeVOList = new ArrayList<ScanCodeVO>();
		List<User> userList  = userService.findByGid(GIDVAL);
		if(uid!=0L){
			String start ="";
			String end ="";
			SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
			if(form.simpleBetween==null){
				end = DATE_FORMAT.format(new Date());
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				cal.add(Calendar.DAY_OF_MONTH, -1);
				start = DATE_FORMAT.format(cal.getTime());
				
				form.simpleBetween = new DateSimpleBetween();
				form.simpleBetween.start=cal.getTime();
				form.simpleBetween.end=new Date();;
			}else{
				start = DATE_FORMAT.format(form.simpleBetween.start);
				end = DATE_FORMAT.format(form.simpleBetween.end);
			}
			try {
				List<String> dateList = Dates.getBetweenDays(DATE_FORMAT.parse(start), DATE_FORMAT.parse(end));
				for (String currDate : dateList) {
					ScanCodeVO scanCodeVO = jdbcService.getScanCodeStatics(currDate,uid);
					scanCodeVOList.add(scanCodeVO);
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return ok(views.html.statistics.scanCodeManage.render(scanCodeVOList,form,Html.apply(UserService.userList2Html(userList,form.uid)),adminUser));
	}
    
    /**
     * 
     * <p>Title: shopO2OStatics</p> 
     * <p>Description: 店铺O2O数据</p> 
     * @return
     */
    @AdminAuthenticated()
    public Result shopO2OStatics() {
    	
    	Form<FreshTaskQueryForm> formRequest = freshTaskQueryForm.bindFromRequest();
    	FreshTaskQueryForm form = new FreshTaskQueryForm();
    	long uid = 0L;
    	if (!formRequest.hasErrors()) {
    		form = formRequest.get();
    		uid = form.uid;
    	}
    	List<ScanCodeVO> scanCodeVOList = new ArrayList<ScanCodeVO>();
    	List<User> userList  = userService.findByGid(GIDVAL);
    	if(uid!=0L){
    		String start ="";
    		String end ="";
    		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    		if(form.simpleBetween!=null){
    			start = DATE_FORMAT.format(form.simpleBetween.start);
    			end = DATE_FORMAT.format(form.simpleBetween.end);
    			scanCodeVOList = jdbcService.getShopO2OStatics(start,end,uid);
    		}
    	}
    	return ok(views.html.statistics.shopO2OStatics.render(scanCodeVOList,form,Html.apply(UserService.userList2Html(userList,form.uid))));
    }
}
