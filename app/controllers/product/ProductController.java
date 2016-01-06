package controllers.product;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import controllers.admin.AdminAuthenticated;
import controllers.admin.BaseAdminController;
import forms.ProductManageForm;
import models.Parcels;
import models.admin.AdminUser;
import models.product.AdminProduct;
import models.product.Category;
import models.product.ChannelMouldPro;
import models.product.Currency;
import models.product.Fromsite;
import models.product.Product;
import models.product.ProductDetail;
import models.product.ProductDetailPram;
import models.product.ProductGroup;
import models.product.ProductImages;
import models.product.ProductUnion;
import models.subject.SubjectMouldPro;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.twirl.api.Html;
import services.OperateLogService;
import services.ServiceFactory;
import services.admin.AdminUserService;
import services.product.CategoryService;
import services.product.ChannelService;
import services.product.ProductGroupService;
import services.product.ProductService;
import services.subject.SubjectService;
import utils.AjaxHelper;
import utils.BeanUtils;
import utils.Constants;
import utils.Constants.DepositType;
import utils.Constants.Lovelydistinct;
import utils.Constants.NationalFlag;
import utils.Constants.ProductStatus;
import utils.Constants.TypsProduct;
import utils.Constants.Wayremark;
import utils.Numbers;
import utils.OSSUtils;
import utils.StringUtil;

/**
 * 
 * <p>Title: ProductController.java</p> 
 * <p>Description: 商品信息管理</p> 
 * <p>Company: higegou</p> 
 * @author  
 * date  2015年7月28日  下午2:12:23
 * @version
 */
@Named
@Singleton
public class ProductController extends BaseAdminController {
	private static final Logger.ALogger logger = Logger.of(ProductController.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
    private final Form<ProductManageForm> productManageForm = Form.form(ProductManageForm.class);
	
	private final ProductService productService;
	private final ChannelService channelService;
	private final SubjectService subjectService;
	private final CategoryService categoryService;
	private final ProductGroupService productGroupService;
	private final OperateLogService operateLogService;
	private final AdminUserService adminUserService;
	
    @Inject
    public ProductController(final ProductService productService,final CategoryService categoryService,final ChannelService channelService,final SubjectService subjectService,final ProductGroupService productGroupService,final OperateLogService operateLogService,final AdminUserService adminUserService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.subjectService = subjectService;
        this.channelService = channelService;
        this.productGroupService = productGroupService;
        this.operateLogService = operateLogService;
        this.adminUserService = adminUserService;
    }
	/**
	 * 商品管理
	 * @return
	 */
    @AdminAuthenticated()
	public Result productManage(){
    	AdminUser adminUser = getCurrentAdminUser();
		Form<ProductManageForm> form = productManageForm.bindFromRequest();
		ProductManageForm formPage = new ProductManageForm();
		if (!form.hasErrors()) {
        	formPage = form.get();
        	formPage.ishot=0;
        }
		if("2".equals(adminUser.getAdminType())){
			formPage.adminUserId = adminUser.getId();
		}
		Page<Product> productPage = productService.queryProductPage(formPage);
		List<Category> categoryList = categoryService.categoryList(0,1);
		List<Fromsite> fromsiteList  = productService.queryAllFromsite();
		List<AdminUser> adminUserList  = adminUserService.queryAllLyAdminUser();		//查找出所有的联营商户
		for(Product product : productPage){
			ServiceFactory.getCacheService().setObject(Constants.product_KEY+product.getPid(), product,0 );
			
			if("1".equals(product.getTyp())){//代下单商品
				if(product.getIslockprice()!=1){//自动计算价格
					Currency currency = productService.queryCurrencyById(product.getCurrency());
					product.setSymbol(currency.getSymbol()); 
					BigDecimal b = new BigDecimal(Double.toString(0.01));
					BigDecimal b1 = new BigDecimal(Double.toString(product.getPrice()));
					BigDecimal b2 = new BigDecimal(Double.toString(product.getList_price()));
					Double price = b.multiply(b1).doubleValue();
					Double list_price = b.multiply(b2).doubleValue();
					product.setHiPrice(currency.getSymbol()+price);//嗨购价
					product.setChaListPrice(currency.getSymbol()+list_price);//市场价
				}
			}else{
				product.setHiPrice("¥"+product.getRmbprice());//嗨购价
				product.setChaListPrice("¥"+product.getChinaprice());//市场价
			}
			if(product.getTyp()!=null){
				if("2".equals(product.getTyp())||formPage.type==3){
					List<AdminUser> lyAdminUsers = adminUserService.queryAdminUserByPid(product.getPid(),formPage.adminid);
					if(lyAdminUsers!=null&&lyAdminUsers.size()>0){
						if(formPage.adminid==-1){
							if(lyAdminUsers.size()>1){
								product.setTypeName("多家联营");
							}else{
								product.setTypeName("联营-"+lyAdminUsers.get(0).getRealname());
							}
						}else{
							product.setTypeName("联营-"+lyAdminUsers.get(0).getRealname());
						}
					}else{
						product.setTypeName(Constants.TypsProduct.typs2Message(Integer.parseInt(product.getTyp())));
					}
				}else{
					product.setTypeName(Constants.TypsProduct.typs2Message(Integer.parseInt(product.getTyp())));
				}
			}
		}
	    
		return ok(views.html.product.productManage.render(
				Html.apply(TypsProduct.typs2HTML(formPage.type)),
				Html.apply(ProductService.fromsiteList2Html(fromsiteList,formPage.fromsite)),
				Html.apply(ProductStatus.status2HTML(formPage.status)),
				Html.apply(CategoryService.categoryList2Html(categoryList,formPage.category)),
				Html.apply(AdminUserService.adminUserList2Html(adminUserList,formPage.adminid)),
				productPage,getCurrentAdminUser()));
	}
	/**
	 * 跳转至新增增商品页面
	 * @return
	 */
    @AdminAuthenticated()
	public Result newOrUpdateProduct(){
		Long pid = Numbers.parseLong(Form.form().bindFromRequest().get("pid"), Long.valueOf(0));
		String activeNum=Form.form().bindFromRequest().get("activeNum")==null?"1":Form.form().bindFromRequest().get("activeNum");
		List<Category> categoryFirstList = categoryService.categoryList(0,1);
		List<Currency> currencyList = productService.currencyList();
		Html categoryFirstHtml = Html.apply(CategoryService.categoryList2Html(categoryFirstList,-1));//一级品类
		Html currencyListHtml = Html.apply(ProductService.currencyList2Html(currencyList,1));//默认人民币
		Html wayremarks = Html.apply(Wayremark.wayRemark2HTML(""));
		List<Fromsite> fromsiteList  = productService.queryAllFromsite();
		Html fromsites = Html.apply(ProductService.fromsiteList2Html(fromsiteList, -1));
		Html distincts = Html.apply(Lovelydistinct.distinct2HTML(0));
		Html depositTypes = Html.apply(DepositType.typs2HTML(0));
		Html pics = Html.apply(NationalFlag.pic2HTML(""));
		List<ProductDetail> detailList = new ArrayList<ProductDetail>();
		List<Product> productList = new ArrayList<Product>();
		List<ProductImages> productImagesList = new ArrayList<ProductImages>();
		Product product = productService.findProductById(pid);
		ProductUnion productUnion = null;
		if(product!=null){
			String categoryName=categoryService.category2Categoryname(product.getCategory(),"");
			if(categoryName.length()>1){
				categoryName =categoryName.substring(0, categoryName.length()-1);
			}
			product.setCategoryName(categoryName);
			categoryFirstHtml = Html.apply(CategoryService.categoryList2Html(categoryFirstList,product.getCategory()));
			wayremarks  = Html.apply(Wayremark.wayRemark2HTML(product.getWayremark()));
			fromsites = Html.apply(ProductService.fromsiteList2Html(fromsiteList, product.getFromsite()));
			Double loveLyDistinct = (product.getLovelydistinct()*10);
			distincts = Html.apply(Lovelydistinct.distinct2HTML(loveLyDistinct.intValue()));
			pics = Html.apply(NationalFlag.pic2HTML(product.getNationalFlag()));
			detailList = productService.findDetailByPid(pid);
			productImagesList = productService.findProductImagesByPid(pid);
			if("1".equals(product.getTyp())){//代下单商品
				if(product.getIslockprice()!=1){//自动计算价格
					Currency currency = productService.queryCurrencyById(product.getCurrency());
					product.setSymbol(currency.getSymbol()); 
					BigDecimal b = new BigDecimal(Double.toString(0.01));
					BigDecimal b1 = new BigDecimal(Double.toString(product.getPrice()));
					BigDecimal b2 = new BigDecimal(Double.toString(product.getList_price()));
					Double price = b.multiply(b1).doubleValue();
					Double list_price = b.multiply(b2).doubleValue();
					product.setPrice(price);//嗨购价
					product.setList_price(list_price);//市场价
				}
			}
			if(product.getPpid()==null||product.getPpid().longValue()==0){
				productList.add(product);
			}else{
				productList = productService.findProductListByPpId(product.getPpid());
			}
			for(Product productTemp : productList){
				if("1".equals(productTemp.getTyp())){//代下单商品
					if(productTemp.getIslockprice()!=1){//自动计算价格
						Currency currency = productService.queryCurrencyById(productTemp.getCurrency());
						product.setSymbol(currency.getSymbol()); 
						BigDecimal b = new BigDecimal(Double.toString(0.01));
						BigDecimal b1 = new BigDecimal(Double.toString(productTemp.getPrice()));
						BigDecimal b2 = new BigDecimal(Double.toString(productTemp.getList_price()));
						Double price = b.multiply(b1).doubleValue();
						Double list_price = b.multiply(b2).doubleValue();
						productTemp.setHiPrice(currency.getSymbol()+price);//嗨购价
						productTemp.setChaListPrice(currency.getSymbol()+list_price);//市场价
					}
				}else{
					productTemp.setHiPrice("¥"+product.getRmbprice());//嗨购价
					productTemp.setChaListPrice("¥"+product.getChinaprice());//市场价
				}
			}
			currencyListHtml = Html.apply(ProductService.currencyList2Html(currencyList,product.getCurrency()));//默认人民币
			depositTypes = Html.apply(DepositType.typs2HTML(product.getStage()));
			productUnion = productService.findProductUnionByPid(product.getPid());
		}
		String kjtFlag = judgeProductTypeWithKjt(product);
		return ok(views.html.product.newOrUpdateProduct.render(categoryFirstHtml,wayremarks,fromsites,currencyListHtml,depositTypes,distincts,pics,pid,product,productUnion,productImagesList,detailList,productList,activeNum,getCurrentAdminUser(),kjtFlag));
	}
	
    /**
     * 
     * <p>Title: judgeProductTypeWithKjt</p> 
     * <p>Description: </p> 
     * @param product
     * @return
     */
	public String judgeProductTypeWithKjt(Product product) {
		String flag = "";
		if(product!=null&&product.getTyp()!=null){
			if("2".equals(product.getTyp())){
				List<AdminUser> lyAdminUsers = adminUserService.queryAdminUserByPid(product.getPid(),-1);
				if(lyAdminUsers!=null&&lyAdminUsers.size()>0){
					if("跨境通".equals(lyAdminUsers.get(0).getRealname())){
						flag = "kjt";
					}
				}
			}
		}
		return flag;
	}
	/**
	 * 保存商品基本详情
	 * @return
	 */
    @AdminAuthenticated()
	public Result saveProduct(){
    	AdminUser adminUser = getCurrentAdminUser();
		Long pid = Numbers.parseLong(Form.form().bindFromRequest().get("pidProduct"), Long.valueOf(0));
		Product product = productService.findProductById(pid);
		String typesRadios = Form.form().bindFromRequest().get("typesRadios");//类型
		String rmbpriceTemp = "";
		if(product==null){
			product = new Product();
			product.setStatus(1);//新品待审
			product.setTyp(typesRadios);
		}else{
			rmbpriceTemp = String.valueOf(product.getRmbprice());
			typesRadios = product.getTyp();
		}
		if(pid.longValue()==0){
			product.setEndorsementPrice(0.0);
			product.setDate_add(new Date());
			product.setIsSyncErp(2);//是否同步ERP
		}
		product.setChinaprice(0.0);
		if("1".equals(typesRadios)){//代下单
			String fromsites = Form.form().bindFromRequest().get("fromsites");//来源网站
			product.setFromsite(Numbers.parseInt(fromsites, 0));
			String extcode = Form.form().bindFromRequest().get("extcode");//原站SKU号
			product.setExtcode(extcode);
			String exturl = Form.form().bindFromRequest().get("exturl");//外部链接
			product.setExturl(exturl);
			String currency = Form.form().bindFromRequest().get("currency");//币种
			product.setCurrency(Numbers.parseInt(currency, 1));
			
			String islockprice = Form.form().bindFromRequest().get("islockprice");//是否锁定价格
			product.setIslockprice(Numbers.parseInt(islockprice, 0));
			product.setFreight((double) 30);
			if("1".equals(islockprice)){//如果锁定价格
				String rmbprice = Form.form().bindFromRequest().get("rmbprice");//锁定后的价格
				if(!Strings.isNullOrEmpty(rmbpriceTemp) && !rmbprice.equals(rmbpriceTemp)){//修改嗨购售价
					operateLogService.saveProductHigouPriceLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), pid, Double.parseDouble(rmbpriceTemp), Double.parseDouble(rmbprice));
				}
				product.setRmbprice(Numbers.parseDouble(rmbprice, 0.0));
				String chinaprice = Form.form().bindFromRequest().get("chinaprice");//国内售价
				product.setChinaprice(Numbers.parseDouble(chinaprice, 0.0));
			}else{
				String price = Form.form().bindFromRequest().get("price");//现价
				String list_price = Form.form().bindFromRequest().get("list_price");//原价
				String dxdchinaprice = Form.form().bindFromRequest().get("dxdchinaprice");//原价
				BigDecimal b = new BigDecimal(Double.toString(100));
				BigDecimal b1 = new BigDecimal(price);
				BigDecimal b2 = new BigDecimal(list_price);
				Double priceDouble = b.multiply(b1).doubleValue();
				Double list_priceDouble = b.multiply(b2).doubleValue();
				Double priceTemp = product.getPrice();
				if(priceTemp.doubleValue()!=priceDouble){
					operateLogService.saveProductHigouPriceLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), pid, priceTemp, priceDouble);
				}
				product.setPrice(priceDouble);
				product.setList_price(list_priceDouble);
				product.setChinaprice(Numbers.parseDouble(dxdchinaprice, 0.0));
			}
			String nstock_autoupd = Form.form().bindFromRequest().get("nstock_autoupd");//是否自动更新
			product.setNstock_autoupd(Numbers.parseInt(nstock_autoupd, 0));
		}else{
			String chinaprice = Form.form().bindFromRequest().get("chinaprice");//国内售价
			product.setChinaprice(Numbers.parseDouble(chinaprice, 0.0));
			String rmbprice = Form.form().bindFromRequest().get("rmbprice");//嗨购售价
			if(!Strings.isNullOrEmpty(rmbpriceTemp) && !rmbprice.equals(rmbpriceTemp)){//修改嗨购售价
				operateLogService.saveProductHigouPriceLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), pid, Double.parseDouble(rmbpriceTemp), Double.parseDouble(rmbprice));
			}
			product.setRmbprice(Numbers.parseDouble(rmbprice, 0.0));
			//针对自营商品，代言价修改
			if(Numbers.parseDouble(rmbprice, 0.0)>1){
				product.setEndorsementPrice(Numbers.parseDouble(rmbprice, 0.0)-1);
			}
			BigDecimal b = new BigDecimal(Double.toString(100));
			BigDecimal b1 = new BigDecimal(rmbprice);
			Double priceDouble = b.multiply(b1).doubleValue();
			product.setPrice(priceDouble);
			product.setCurrency(1);
			product.setIslockprice(1);//锁定价格
			product.setFreight((double) 0);
			String isSyncErp = Form.form().bindFromRequest().get("isSyncErp");
			if("2".equals(adminUser.getAdminType())){
				product.setIsSyncErp(9);
			}else{
				product.setIsSyncErp(Numbers.parseInt(isSyncErp, 2));
			}
			
		}
		String preProduct = Form.form().bindFromRequest().get("preProduct");//是否是预售 1普通 2撒娇 3预售 4定时开抢
		String lovelyProduct = Form.form().bindFromRequest().get("lovelyProduct");//是否是撒娇 1普通 2撒娇 3预售 4定时开抢
		String timProduct = Form.form().bindFromRequest().get("timProduct");//是否是定时开抢 1普通 2撒娇 3预售 4定时开抢
		product.setMancnt(0);//映射的是int 默认值暂时不起作用
		product.setDeposit(0.0);
		
		product.setRtitle("");
		product.setPreselltoast("");
		if("1".equals(preProduct)){//预售商品
			product.setPtyp("3");//标识是预售
			String deposit = Form.form().bindFromRequest().get("deposit");//订金金额
			product.setDeposit(Numbers.parseDouble(deposit, 0.0));
			String mancnt = Form.form().bindFromRequest().get("mancnt");//参与人数
			product.setMancnt(Numbers.parseInt(mancnt, 0));
			String stage = Form.form().bindFromRequest().get("stage");//当前阶段
			product.setStage(Numbers.parseInt(stage, 0));
			if(product!=null){
				productService.doJobWithStage(Numbers.parseInt(stage, 0),product.getPid());
			}
			String paytim = Form.form().bindFromRequest().get("paytim");//预计尾款支付时间
			product.setPaytim(paytim);
			String payStim = Form.form().bindFromRequest().get("payStim");//预售开始时间
			product.setBtim(payStim);
			String payEtim = Form.form().bindFromRequest().get("payEtim");//预售结束时间
			product.setEtim(payEtim);
			
			String preselltoast = Form.form().bindFromRequest().get("preselltoast");//预售preselltoast
			product.setPreselltoast(preselltoast);
			String rtitle = Form.form().bindFromRequest().get("rtitle");//预售rtitle
			product.setRtitle(rtitle);
			
			product.setIslovely("0");//非撒娇商品
			product.setLovelydistinct(10.0);
			
		}else if("1".equals(lovelyProduct)){//撒娇商品
			product.setPtyp("2");//标识是撒娇
			product.setIslovely("1");//撒娇商品
			String lovelydistinct = Form.form().bindFromRequest().get("lovelydistinct");//撒娇折扣
			product.setLovelydistinct(Numbers.parseDouble(lovelydistinct, 0.0)/10);
		}else if("1".equals(timProduct)){//定时开抢商品
			product.setPtyp("4");//标识是定时开抢
			String btim = Form.form().bindFromRequest().get("btim");//开抢开始时间
			product.setBtim(btim);
			String etim = Form.form().bindFromRequest().get("etim");//开抢开始时间
			product.setEtim(etim);
			
			product.setLovelydistinct(10.0);
			product.setIslovely("0");//非撒娇商品
		}else{
			product.setPtyp("1");
			product.setLovelydistinct(10.0);
			product.setIslovely("0");//普通商品
			product.setBtim("");
			product.setEtim("");
		}
		String newMantypeRadios = Form.form().bindFromRequest().get("newMantypeRadios");//1首购商品 2仅一次商品 0普通（不做处理）3、0元商品 
		product.setNewMantype(newMantypeRadios);//1首购商品 2仅一次商品 0普通（不做处理）3、0元商品
		String category = Form.form().bindFromRequest().get("categorys");//品类
		product.setCategory(Numbers.parseInt(category, 1));
		String title = Form.form().bindFromRequest().get("title");//商品名称
		product.setTitle(title);
		String subtitle = Form.form().bindFromRequest().get("subtitle");//商品介绍
		product.setSubtitle(subtitle);
		String adstr1 = Form.form().bindFromRequest().get("adstr1");//推荐理由
		product.setAdstr1(adstr1);
		String nstock = Form.form().bindFromRequest().get("nstock");//库存
		Long nstockTemp = product.getNstock();
		product.setNstock(Numbers.parseLong(nstock, 0L));
		String limitcount = Form.form().bindFromRequest().get("limitcount");//限购数量
		product.setLimitcount(Numbers.parseInt(limitcount, 0));
		
		String weight = Form.form().bindFromRequest().get("weight");//重量
		product.setWeight(Numbers.parseDouble(weight, 0.0));
		String wayremark = Form.form().bindFromRequest().get("wayremark");//发货地
		product.setWayremark(Constants.Wayremark.wayRemark2Message(Numbers.parseInt(wayremark, 1)));
		String nlikes = Form.form().bindFromRequest().get("nlikes");//喜欢数
		product.setNlikes(Numbers.parseInt(nlikes, 0));
		
		String isopenid = Form.form().bindFromRequest().get("isopenid");//是否需要身份证信息
		product.setIsopenid(Numbers.parseInt(isopenid, 0));
		String isopenidimg = Form.form().bindFromRequest().get("isopenidimg");//是否需要身份证图片信息
		product.setIsopenidimg(Numbers.parseInt(isopenidimg, 0));
		String national = Form.form().bindFromRequest().get("national");//是否展示国旗
		if("1".equals(national)){
			String nationalFlag = Form.form().bindFromRequest().get("nationalFlag");//国旗标志
			product.setNationalFlag(nationalFlag);
		}else{
			product.setNationalFlag("");
		}
		product.setDate_upd(new Date());
		product = productService.saveProduct(product);
		product.setSkucode("1000"+product.getPid());//设置Skucode
		if(StringUtils.isBlank(product.getNewSku())){
			String newSku = productService.getNewSkuByCategory(product.getCategory());
			product.setNewSku(newSku);
		}
		if(pid.longValue()==0||product.getPpid()==null){
			product.setPpid(product.getPid());
		}
		product = productService.saveProduct(product);
		
		String taxRate = Form.form().bindFromRequest().get("taxRate");//税率
		String buyNow = Form.form().bindFromRequest().get("buyNow");//是否单独购买（立即购买）
		ProductUnion productUnion = productService.findProductUnionByPid(product.getPid());
		if(productUnion==null){
			productUnion = new ProductUnion();
			productUnion.setPid(product.getPid());
			productUnion.setDateAdd(new Date());
		}
		productUnion.setBuyNowFlag(buyNow);
		productUnion.setTaxRate(Numbers.parseDouble(taxRate, 0.0));
		productUnion.setDateUpd(new Date());
		productUnion = productService.saveProductUnion(productUnion);
		logger.info(taxRate+"++++++++++");
		logger.info(buyNow+"-------------");
		if(pid.longValue()==0){//如果是新增，需要初始化两个详情
			operateLogService.saveProductLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "新建商品,创建后的商品ID为:"+product.getPid());
			ProductDetail productDetail=new ProductDetail();
			productDetail.setChname("商品详情");
			productDetail.setEnname("Details");
			productDetail.setDetail("");
			productDetail.setDate_add(new Date());
			productDetail.setPid(product.getPid());
			productDetail.setNsort(0);
			productDetail = productService.saveDetail(productDetail);
			ProductDetail productDetail2=new ProductDetail();
			productDetail2.setChname("商品参数");
			productDetail2.setEnname("Parameters");
			productDetail2.setDetail("");
			productDetail2.setDate_add(new Date());
			productDetail2.setPid(product.getPid());
			productDetail2.setNsort(0);
			productDetail2 = productService.saveDetail(productDetail2);
		}else{
			if(nstockTemp!=null && nstockTemp.longValue()!=Numbers.parseLong(nstock, 0L)){//修改库存管理
				operateLogService.saveProductNstockLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), pid, nstockTemp, product.getNstock());
			}else{//编辑除修改库存外的操作
				operateLogService.saveProductLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "编辑商品,编辑的商品ID为:"+product.getPid());
			}
		}
		if("2".equals(getCurrentAdminUser().getAdminType())){//联营商品
			List<AdminProduct> adminProductList = productService.findAdminProductByPidAndAdminid(product.getPid(), getCurrentAdminUser().getId());
			if(adminProductList==null || adminProductList.isEmpty()){
				AdminProduct adminProduct = new AdminProduct();
				adminProduct.setAdminid(getCurrentAdminUser().getId());
				adminProduct.setDate_add(new Date());
				adminProduct.setPid(product.getPid());
				productService.saveAdminProduct(adminProduct); 
			}
		}
		return redirect("/product/newOrUpdateProduct?pid="+product.getPid()+"&activeNum="+2);
	}
	
	
	
	/**
	 * 上传列表图片
	 * @return
	 */
    @AdminAuthenticated()
	public Result uploadListPic(){
		MultipartFormData body = request().body().asMultipartFormData();
		Long pid = Numbers.parseLong(Form.form().bindFromRequest().get("pidListpic"), Long.valueOf(0));
		Product product = productService.findProductById(pid);
		FilePart listPic = body.getFile("listPic");
        if (product!=null && listPic != null) {
        	String path=Configuration.root().getString("oss.upload.product.listpic", "upload/endorsement/");//上传路径
        	String BUCKET_NAME=StringUtil.getBUCKET_NAME();
    		if (listPic != null && listPic.getFile() != null) {
    			String fileName = listPic.getFilename();
    			File file = listPic.getFile();//获取到该文件
    			int p = fileName.lastIndexOf('.');
    			String type = fileName.substring(p, fileName.length()).toLowerCase();
    			
    			if (".jpg".equals(type)||".gif".equals(type)||".png".equals(type)||".jpeg".equals(type)||".bmp".equals(type)) {
    				// 检查文件后缀格式
    				String fileNameLast = UUID.randomUUID().toString().replaceAll("-", "")+type;//最终的文件名称
    				String endfilestr = OSSUtils.uploadFile(file,path,fileNameLast, type,BUCKET_NAME);	
    				product.setListpic(endfilestr);
    				productService.saveProduct(product);
    				List<ChannelMouldPro> chmouldProList = channelService.findChMoPrByPId(product.getPid());
    				for(ChannelMouldPro channelMouldPro:chmouldProList){
    					channelMouldPro.setImgurl(endfilestr);
    					channelService.saveChannelMouldPro(channelMouldPro, "", channelMouldPro.getCmid());//此处channelId传入的是空，是因为肯定是新图片，不需要再传入channelid了
    				}
    				List<SubjectMouldPro> subjectMouldProList = subjectService.findSuMoPrByPId(product.getPid());
    				for(SubjectMouldPro subjectMouldPro:subjectMouldProList){
    					subjectMouldPro.setImgurl(endfilestr);
    					subjectService.saveSubjectMouldPro(subjectMouldPro,"",subjectMouldPro.getId());
    				}
    			}
    		}
        }
		return redirect("/product/newOrUpdateProduct?pid="+pid+"&activeNum="+2);
	}
	
	/**
	 * 上传商品图片
	 * @return
	 */
    @AdminAuthenticated()
	public Result uploadProductImages(){
		Long pid = Numbers.parseLong(Form.form().bindFromRequest().get("pidImages"), Long.valueOf(0));
		Product product = productService.findProductById(pid);
		MultipartFormData body = request().body().asMultipartFormData();
        List<FilePart> imageParts = body.getFiles();
        if (product!=null && imageParts != null) {
        	String path=Configuration.root().getString("oss.upload.product.image", "upload/endorsement/");//上传路径
        	String BUCKET_NAME=StringUtil.getBUCKET_NAME();
    		for(FilePart imagePart:imageParts){
    			String fileName = imagePart.getFilename();
    			File file = imagePart.getFile();//获取到该文件
    			int p = fileName.lastIndexOf('.');
    			String type = fileName.substring(p, fileName.length()).toLowerCase();
    			
    			if (".jpg".equals(type)||".gif".equals(type)||".png".equals(type)||".jpeg".equals(type)||".bmp".equals(type)) {
    				// 检查文件后缀格式
    				String fileNameLast = UUID.randomUUID().toString().replaceAll("-", "")+type;//最终的文件名称
    				String endfilestr = OSSUtils.uploadFile(file,path,fileNameLast, type,BUCKET_NAME);		
    				logger.info("========="+endfilestr);//=/upload/product/images/fbbbeeb1-2a96-4634-a77b-4566dc68dff0.jpg
    				ProductImages productImage=new ProductImages();
    				productImage.setDate_add(new Date());
    				productImage.setFilename(endfilestr);
    				productImage.setPid(pid);
    				productImage.setPcode(product.getSkucode());
    				productService.saveProductImages(productImage);
    			}
    		}
        }
        
        return ok(Json.toJson(""));
	}
	/**
	 * 修改商品状态
	 * @param detailId
	 * @return
	 */
    @AdminAuthenticated()
	public Result updateProductState(Long pid,int status){
    	AdminUser adminUser = getCurrentAdminUser();
    	ObjectNode result=Json.newObject();
    	Product product=productService.findProductById(pid);
		boolean flag = true;
		if(product.getIshot()==1&&status==10){
			//针对组合商品，查看子商品数量是否>1
			List<ProductGroup> productGroups = productGroupService.findProductGroupListByPgId(product.getPid());
			int sum = 0;
			for (ProductGroup productGroup : productGroups) {
				/* 用于记录组合商品下的子商品是否需要要求在线状态
				 * Product productTemp = productService.findProductById(productGroup.getPid());
				if(productTemp.getStatus()!=10){
					flag = false;
					break;
				}*/
				if(productGroup.getNum()==0L){
					flag = false;
					break;
				}
				sum += productGroup.getNum();
			}
			if(sum <= 1){
				flag = false;
			}
		}
		if(status==20){
			//针对组合商品，优先去除组合商品
			List<ProductGroup> productGroups = productGroupService.getProductGroupByPid(product.getPid());
			for (ProductGroup productGroup : productGroups) {
				Product productTemp = productService.findProductById(productGroup.getPgid());
				operateLogService.saveProductStateLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), pid, productTemp.getStatus(), status);
				productTemp.setStatus(status);
				productService.saveProduct(productTemp);
			}
		}
		if(flag){
			if(status==10){
				product.setDate_upd(new Date());
			}
			operateLogService.saveProductStateLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), pid, product.getStatus(), status);
			product.setStatus(status);
			productService.saveProduct(product);
			result.put("status", 1);
		}else{
			result.put("status", 0);
		}
		return ok(Json.toJson(result));
	}
	
    /**
     * 批量上架商品
     * @return
     */
    @AdminAuthenticated()
    public Result updateProductsToGrouding(){
    	AdminUser adminUser = getCurrentAdminUser();
    	String productIds = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "products");
    	List<Long> ids = Lists.newArrayList();
    	JsonNode json = Json.parse(productIds);
    	for(JsonNode temp : json){
    		ids.add(temp.asLong());
    	}
    	productService.updateProductStateBatch(ids,ProductStatus.ONSALE.getStatus());
    	operateLogService.saveProductLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "批量修改商品状态，将商品上架，ID列表为："+productIds);
    	return ok(Json.toJson("1"));
    }
	/**
	 * 修改商品代言状态
	 * @param detailId
	 * @return
	 */
    @AdminAuthenticated()
	public Result updateProductEndorsement(Long pid,int status){
		Product product=productService.findProductById(pid);
		product.setMaxEndorsementCount(Numbers.parseInt(Form.form().bindFromRequest().get("maxEndorsementCount"), 99999));
		product.setEndorsementPrice(Numbers.parseDouble(Form.form().bindFromRequest().get("endorsementPrice"), 0.0));
		product.setCommision(Numbers.parseDouble(Form.form().bindFromRequest().get("commision"), 0.0));
		product.setCommisionTyp(Numbers.parseInt(Form.form().bindFromRequest().get("commisionTyp"), 1));
		product.setIsEndorsement(status);
		productService.saveProduct(product);
		ObjectNode result=Json.newObject();
		result.put("status", 1);
		return ok(Json.toJson(result));
	}
	/**
	 * 删除商品图片
	 * @param detailId
	 * @return
	 */
    @AdminAuthenticated()
	public Result deleteProductImage(Long imageid){
		productService.deleteProductImageById(imageid);
		ObjectNode result=Json.newObject();
		result.put("status", 1);
		return ok(Json.toJson(result));
	}
	/**
	 * 保存商品详情
	 * @param pid
	 * @param pidnow
	 * @return
	 */
    @AdminAuthenticated()
	public Result saveProductDetail(){
		Long pid = Numbers.parseLong(Form.form().bindFromRequest().get("pidDetail"), Long.valueOf(0));
		Long detailId = Numbers.parseLong(Form.form().bindFromRequest().get("detailid"), Long.valueOf(0));
		String enname = Form.form().bindFromRequest().get("enname");
		String chname = Form.form().bindFromRequest().get("chname");
		String content = Form.form().bindFromRequest().get("content");
		String detailSort = Form.form().bindFromRequest().get("detailSort");
		ProductDetail productDetail = productService.findProductDetailById(detailId);
		if(productDetail==null)
			productDetail=new ProductDetail();
		productDetail.setChname(chname);
		productDetail.setEnname(enname);
		productDetail.setDetail(content);
		productDetail.setDate_add(new Date());
		productDetail.setPid(pid);
		productDetail.setNsort(Numbers.parseInt(detailSort, 0));
		productDetail = productService.saveDetail(productDetail);
		ObjectNode result=Json.newObject();
		result.put("status", 1);
		return ok(Json.toJson(result));
	}
	
	/**
	 * 根据商品详情ID获取该详情
	 * @param pid
	 * @param pidnow
	 * @return
	 */
    @AdminAuthenticated()
	public Result getProductDetail(Long detailId){
		ProductDetail productDetail = productService.findProductDetailById(detailId);
		return ok(Json.toJson(productDetail));
	}
	
	/**
	 * 删除商品详情
	 * @param detailId
	 * @return
	 */
    @AdminAuthenticated()
	public Result deleteProductDetail(Long detailId){
		productService.deleteProductDetail(detailId);
		ObjectNode result=Json.newObject();
		result.put("status", 1);
		return ok(Json.toJson(result));
	}
	
	
	/**
	 * 根据商品详情ID获取该详情下的参数列表
	 * @param pid
	 * @param pidnow
	 * @return
	 */
    @AdminAuthenticated()
	public Result getProductDetailParams(Long detailId){
		List<ProductDetailPram> ProductDetailPramList = productService.findDetailParamsByPdid(detailId);
		return ok(Json.toJson(ProductDetailPramList));
	}
	/**
	 * 保存详情参数
	 * @param pid
	 * @param pidnow
	 * @return
	 */
    @AdminAuthenticated()
	public Result saveDetailInfo(){
		Long detailId = Numbers.parseLong(Form.form().bindFromRequest().get("detailIdWithInfo"), Long.valueOf(0));
		String detailInfoKey = Form.form().bindFromRequest().get("detailInfoKey");
		String detailInfoVal = Form.form().bindFromRequest().get("detailInfoVal");
		String detailInfoNsort = Form.form().bindFromRequest().get("detailInfoNsort");
		ProductDetailPram productDetailPram = new ProductDetailPram();
		productDetailPram.setDate_add(new Date());
		productDetailPram.setKey(detailInfoKey);
		productDetailPram.setVal(detailInfoVal);
		productDetailPram.setPdid(detailId);
		productDetailPram.setNsort(Numbers.parseInt(detailInfoNsort, 0));
		productDetailPram = productService.saveProductDetailPram(productDetailPram);
		ObjectNode result=Json.newObject();
		result.put("status", 1);
		return ok(Json.toJson(result));
	}
	/**
	 * 删除详情参数
	 * @param detailId
	 * @return
	 */
    @AdminAuthenticated()
	public Result deleteParamById(Long paramId){
		productService.deleteDetailParamById(paramId);
		ObjectNode result=Json.newObject();
		result.put("status", 1);
		return ok(Json.toJson(result));
	}
	
	/**
	 * 上传规格图片
	 * @return
	 */
    @AdminAuthenticated()
	public Result uploadSpecPic(){
		MultipartFormData body = request().body().asMultipartFormData();
		Long pidnow = Numbers.parseLong(Form.form().bindFromRequest().get("pidnow"), Long.valueOf(0));
		Long pid = Numbers.parseLong(Form.form().bindFromRequest().get("pidSpecpic"), Long.valueOf(0));
		String specifications = Form.form().bindFromRequest().get("specifications");
		if(StringUtils.isBlank(specifications)){
			specifications = "";
		}
		String specSort = Form.form().bindFromRequest().get("specSort");
		Product product = productService.findProductById(pid);
		if(product!=null){
			FilePart specpic = body.getFile("specpic");
			if (specpic != null) {
				String path=Configuration.root().getString("oss.upload.specpic.image", "upload/specpic/images/");//上传路径
				String BUCKET_NAME=StringUtil.getBUCKET_NAME();
				if (specpic != null && specpic.getFile() != null) {
					String fileName = specpic.getFilename();
					File file = specpic.getFile();//获取到该文件
					int p = fileName.lastIndexOf('.');
					String type = fileName.substring(p, fileName.length()).toLowerCase();
					
					if (".jpg".equals(type)||".gif".equals(type)||".png".equals(type)||".jpeg".equals(type)||".bmp".equals(type)) {
						// 检查文件后缀格式
						String fileNameLast = UUID.randomUUID().toString().replaceAll("-", "")+type;//最终的文件名称
						String endfilestr = OSSUtils.uploadFile(file,path,fileNameLast, type,BUCKET_NAME);	
						product.setSpecpic(endfilestr);
					}
				}
			}
			product.setSpecifications(specifications);
			product.setSort(Numbers.parseInt(specSort, 0));
			productService.saveProduct(product);
		}
		return redirect("/product/newOrUpdateProduct?pid="+pidnow+"&activeNum="+4);
	}
	
	/**
	 * 删除规格
	 * @param pid
	 * @return
	 */
    @AdminAuthenticated()
	public Result deleteSpec(Long pid){
		int rows = productService.deleteSpec(pid);
		ObjectNode result=Json.newObject();
		result.put("status", 1);
		result.put("rows", rows);
		return ok(Json.toJson(result));
	}
	/**
	 * 多规格中选取现有商品
	 * @return
	 */
    @AdminAuthenticated()
	public Result addSpecProduct(){
    	AdminUser adminUser = getCurrentAdminUser();
		Long pid = Numbers.parseLong(AjaxHelper.getHttpParam(request(), "pid"), 0L);
		Long ppid = Numbers.parseLong(AjaxHelper.getHttpParam(request(), "ppid"), 0L);
		if(pid.equals(ppid)){//选取自己本身，则复制此商品，PID不一样即可
			Product proudct = productService.findProductById(pid);
			Product productNew = new Product();
			String ignor[] = {"pid","skucode","newSku"};
			BeanUtils.copyProperties(proudct, productNew, ignor);
			productNew = productService.saveProduct(productNew);
			productNew.setSkucode("100000"+productNew.getPid());//设置Skucode
			if(StringUtils.isBlank(productNew.getNewSku())){
				String newSku = productService.getNewSkuByCategory(productNew.getCategory());
				productNew.setNewSku(newSku);
			}
			productNew = productService.saveProduct(productNew);
			if("2".equals(getCurrentAdminUser().getAdminType())){
				AdminProduct adminProduct = new AdminProduct();
				adminProduct.setAdminid(getCurrentAdminUser().getId());
				adminProduct.setDate_add(new Date());
				adminProduct.setPid(productNew.getPid());
				productService.saveAdminProduct(adminProduct); 
			}
			operateLogService.saveProductLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "新建商品(多规格中选取商品本身),创建后的商品ID为:"+productNew.getPid());
		}else{
			productService.updateProductPpid(pid,ppid);
		}
		ObjectNode result=Json.newObject();
		result.put("status", 1);
		return ok(Json.toJson(result));
	}
	/**
	 * 商品列表返回JSON
	 * @return
	 */
    @AdminAuthenticated()
	public Result productListForJson(){
		String isEndorsement = AjaxHelper.getHttpParam(request(), "isEndorsement");
		ProductManageForm productManageForm = new ProductManageForm();
		productManageForm.pidOrNewSku=AjaxHelper.getHttpParam(request(), "pidForSearch");
		productManageForm.type=Numbers.parseInt(AjaxHelper.getHttpParam(request(), "typ"), -1);
		productManageForm.keyWords = AjaxHelper.getHttpParam(request(), "pNameForSearch");
		productManageForm.isEndorsement = Numbers.parseInt(isEndorsement, -1);
		productManageForm.ishot = Numbers.parseInt(AjaxHelper.getHttpParam(request(), "ishot"), 0);
		if (Strings.isNullOrEmpty(productManageForm.pidOrNewSku) && StringUtils.isBlank(productManageForm.keyWords)) {
			return ok(views.html.product.products.render(new ArrayList<Product>()));
		}
		
		AdminUser adminUser = getCurrentAdminUser();
		if("2".equals(adminUser.getAdminType())){
			productManageForm.adminUserId = adminUser.getId();
		}
		
		List<Product> productList = productService.queryProduct(productManageForm);
		for(Product product : productList){
			if(product!=null){
				if("1".equals(product.getTyp())){//代下单商品
					if(product.getIslockprice()!=1){//自动计算价格
						Currency currency = productService.queryCurrencyById(product.getCurrency());
						product.setSymbol(currency.getSymbol()); 
						BigDecimal b = new BigDecimal(Double.toString(0.01));
						BigDecimal b1 = new BigDecimal(Double.toString(product.getPrice()));
						BigDecimal b2 = new BigDecimal(Double.toString(product.getList_price()));
						Double price = b.multiply(b1).doubleValue();
						Double list_price = b.multiply(b2).doubleValue();
						product.setHiPrice(currency.getSymbol()+price);//嗨购价
						product.setChaListPrice(currency.getSymbol()+list_price);//市场价
					}
				}else{
					product.setHiPrice("¥"+product.getRmbprice());//嗨购价
					product.setChaListPrice("¥"+product.getChinaprice());//市场价
				}
				product = dealProductWithTyp(product);
			}               
		}
		return ok(Json.toJson(productList));
	}
	/**
	 * 商品列表返回JSON(根据pids)
	 * @return
	 */
    @AdminAuthenticated()
	public Result productListWithPidsForJson(){
		String pIds = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "products");
		List<Product> productList = new ArrayList<Product>();
		JsonNode json = Json.parse(pIds);
		for(JsonNode temp : json){
			Product product = productService.findProductById(temp.asLong());
			if(product!=null){
				if("1".equals(product.getTyp())){//代下单商品
					if(product.getIslockprice()!=1){//自动计算价格
						Currency currency = productService.queryCurrencyById(product.getCurrency());
						product.setSymbol(currency.getSymbol()); 
						BigDecimal b = new BigDecimal(Double.toString(0.01));
						BigDecimal b1 = new BigDecimal(Double.toString(product.getPrice()));
						BigDecimal b2 = new BigDecimal(Double.toString(product.getList_price()));
						Double price = b.multiply(b1).doubleValue();
						Double list_price = b.multiply(b2).doubleValue();
						product.setHiPrice(currency.getSymbol()+price);//嗨购价
						product.setChaListPrice(currency.getSymbol()+list_price);//市场价
					}
				}else{
					product.setHiPrice("¥"+product.getRmbprice());//嗨购价
					product.setChaListPrice("¥"+product.getChinaprice());//市场价
				}
				productList.add(product);
			}
		}
		return ok(Json.toJson(productList));
	}
	/**
	 * 商品列表(弹窗)
	 * @return
	 */
    @AdminAuthenticated()
	public Result products(){
		String isEndorsement = AjaxHelper.getHttpParam(request(), "isEndorsement");
		ProductManageForm productManageForm = new ProductManageForm();
		productManageForm.pidOrNewSku=AjaxHelper.getHttpParam(request(), "pidForSearch");
		productManageForm.keyWords = AjaxHelper.getHttpParam(request(), "pNameForSearch");
		productManageForm.isEndorsement = Numbers.parseInt(isEndorsement, -1);
		if (Strings.isNullOrEmpty(productManageForm.pidOrNewSku) && StringUtils.isBlank(productManageForm.keyWords)) {
			return ok(views.html.product.products.render(new ArrayList<Product>()));
		}
		
		AdminUser adminUser = getCurrentAdminUser();
		if("2".equals(adminUser.getAdminType())){
			productManageForm.adminUserId = adminUser.getId();
		}
		List<Product> productList = productService.queryProduct(productManageForm);
		for(Product product : productList){
			if("1".equals(product.getTyp())){//代下单商品
				if(product.getIslockprice()!=1){//自动计算价格
					Currency currency = productService.queryCurrencyById(product.getCurrency());
					product.setSymbol(currency.getSymbol()); 
					BigDecimal b = new BigDecimal(Double.toString(0.01));
					BigDecimal b1 = new BigDecimal(Double.toString(product.getPrice()));
					BigDecimal b2 = new BigDecimal(Double.toString(product.getList_price()));
					Double price = b.multiply(b1).doubleValue();
					Double list_price = b.multiply(b2).doubleValue();
					product.setHiPrice(currency.getSymbol()+price);//嗨购价
					product.setChaListPrice(currency.getSymbol()+list_price);//市场价
				}
			}else{
				product.setHiPrice("¥"+product.getRmbprice());//嗨购价
				product.setChaListPrice("¥"+product.getChinaprice());//市场价
			}           
			if(product.getNstock()<=-99){
				long nstock = productService.dealNstockWithProduct(product.getPid());
				product.setNstock(nstock);
			}
			product = dealProductWithTyp(product);
		}
		return ok(views.html.product.products.render(productList));
	}
	/**
	 * 商品列表(批量添加)(弹窗)
	 * @return
	 */
    @AdminAuthenticated()
	public Result productsBatch(){
		String isEndorsement = AjaxHelper.getHttpParam(request(), "isEndorsement");
		ProductManageForm productManageForm = new ProductManageForm();
		productManageForm.pidOrNewSku=AjaxHelper.getHttpParam(request(), "pidForSearch");
		productManageForm.keyWords = AjaxHelper.getHttpParam(request(), "pNameForSearch");
		productManageForm.isEndorsement = Numbers.parseInt(isEndorsement, -1);
		if (Strings.isNullOrEmpty(productManageForm.pidOrNewSku) && StringUtils.isBlank(productManageForm.keyWords)) {
			return ok(views.html.product.products.render(new ArrayList<Product>()));
		}
		AdminUser adminUser = getCurrentAdminUser();
		if("2".equals(adminUser.getAdminType())){
			productManageForm.adminUserId = adminUser.getId();
		}
		List<Product> productList = productService.queryProduct(productManageForm);
		for(Product product : productList){
			if("1".equals(product.getTyp())){//代下单商品
				if(product.getIslockprice()!=1){//自动计算价格
					Currency currency = productService.queryCurrencyById(product.getCurrency());
					product.setSymbol(currency.getSymbol()); 
					BigDecimal b = new BigDecimal(Double.toString(0.01));
					BigDecimal b1 = new BigDecimal(Double.toString(product.getPrice()));
					BigDecimal b2 = new BigDecimal(Double.toString(product.getList_price()));
					Double price = b.multiply(b1).doubleValue();
					Double list_price = b.multiply(b2).doubleValue();
					product.setHiPrice(currency.getSymbol()+price);//嗨购价
					product.setChaListPrice(currency.getSymbol()+list_price);//市场价
				}
			}else{
				product.setHiPrice("¥"+product.getRmbprice());//嗨购价
				product.setChaListPrice("¥"+product.getChinaprice());//市场价
			}
			if(product.getNstock()<=-99){
				long nstock = productService.dealNstockWithProduct(product.getPid());
				product.setNstock(nstock);
			}
			product = dealProductWithTyp(product);
		}
		return ok(views.html.product.productsBatch.render(productList));
	}
	
    /**
     * 
     * <p>Title: dealProductWithTyp</p> 
     * <p>Description: 处理商品类型展示内容</p> 
     * @param product
     * @return
     */
	private Product dealProductWithTyp(Product product) {
		if(product!=null&&product.getTyp()!=null){
			if("2".equals(product.getTyp())){
				List<AdminUser> lyAdminUsers = adminUserService.queryAdminUserByPid(product.getPid(),-1);
				if(lyAdminUsers!=null&&lyAdminUsers.size()>0){
					if(lyAdminUsers.size()>1){
						product.setTypeName("多家联营");
					}else{
						product.setTypeName("联营-"+lyAdminUsers.get(0).getRealname());
					}
				}else{
					product.setTypeName(Constants.TypsProduct.typs2Message(Integer.parseInt(product.getTyp())));
				}
			}else{
				product.setTypeName(Constants.TypsProduct.typs2Message(Integer.parseInt(product.getTyp())));
			}
		}
		return product;
	}
	/**
	 * 根据parentid与level获取该level的下级分类列表
	 * @return
	 */
    @AdminAuthenticated()
	public Result getCategoryListByParentIdAndLevel(){
		String categorys = AjaxHelper.getHttpParam(request(), "categorys");
		String level = AjaxHelper.getHttpParam(request(), "level");
		List<Category> categoryList = categoryService.categoryList(Numbers.parseInt(categorys,0),Numbers.parseInt(level,0));
		return ok(Html.apply(CategoryService.categoryList2Html(categoryList,-1)));
	}
	
    public Result getAllCategoryList(){
    	String pids = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "pids");
    	String[] tempPids = pids.split(",");
    	StringBuffer sb = new StringBuffer();
    	for (String pid : tempPids) {
			Product product = productService.findProductById(Numbers.parseLong(pid, 0l));
			String categoryName=categoryService.category2Categoryname(product.getCategory(),"");
			if(categoryName.length()>1){
				categoryName =categoryName.substring(0, categoryName.length()-1);
			}	
			sb.append(pid).append("\t").append(categoryName).append("\r\n");
		}
    	return ok(sb.toString());
    }
    
	
}
