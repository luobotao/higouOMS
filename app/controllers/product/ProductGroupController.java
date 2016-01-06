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

import models.admin.AdminUser;
import models.product.Category;
import models.product.ChannelMouldPro;
import models.product.Currency;
import models.product.Fromsite;
import models.product.Product;
import models.product.ProductDetail;
import models.product.ProductDetailPram;
import models.product.ProductGroup;
import models.product.ProductImages;
import models.subject.SubjectMouldPro;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;

import controllers.admin.AdminAuthenticated;
import controllers.admin.BaseAdminController;
import forms.ProductManageForm;

/**
 * 
 * <p>Title: ProductGroupController.java</p> 
 * <p>Description: 组合商品管理</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年9月8日  下午3:22:48
 * @version
 */
@Named
@Singleton
public class ProductGroupController extends BaseAdminController {
	private static final Logger.ALogger logger = Logger.of(ProductGroupController.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private final String newOrupdateError = "productNewOrupdateError";
    private final Form<ProductManageForm> productManageForm = Form.form(ProductManageForm.class);
	
	private final ProductService productService;
	private final ChannelService channelService;
	private final SubjectService subjectService;
	private final CategoryService categoryService;
	private final ProductGroupService productGroupService;
	private final OperateLogService operateLogService;
    @Inject
    public ProductGroupController(final ProductService productService,final CategoryService categoryService,final ChannelService channelService,final SubjectService subjectService,final ProductGroupService productGroupService,final OperateLogService operateLogService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.subjectService = subjectService;
        this.channelService = channelService;
        this.productGroupService = productGroupService;
        this.operateLogService = operateLogService;
    }
	/**
	 * 商品管理
	 * @return
	 */
	public Result productGroupManage(){
		Form<ProductManageForm> form = productManageForm.bindFromRequest();
		ProductManageForm formPage = new ProductManageForm();
		if (!form.hasErrors()) {
        	formPage = form.get();
        	formPage.ishot=1;
        }
		Page<Product> productPage = productService.queryProductPage(formPage);
		List<Category> categoryList = categoryService.categoryList(0,1);
		List<Fromsite> fromsiteList  = productService.queryAllFromsite();
		for(Product product : productPage){
			ServiceFactory.getCacheService().setObject(Constants.product_KEY+product.getPid(), product,0 );
			
			product.setHiPrice("¥"+product.getRmbprice());//嗨购价
			product.setChaListPrice("¥"+product.getChinaprice());//市场价
			if(product.getNstock()<=-99){
				long nstock = dealNstockWithProduct(product.getPid());
				product.setNstock(nstock);
			}
		}
	    
		return ok(views.html.product.productGroupManage.render(
				Html.apply(TypsProduct.typs2HTML(formPage.type)),
				Html.apply(ProductService.fromsiteList2Html(fromsiteList,formPage.fromsite)),
				Html.apply(ProductStatus.status2HTML(formPage.status)),
				Html.apply(CategoryService.categoryList2Html(categoryList,formPage.category)),
				productPage));
	}

	/**
	 * 
	 * <p>Title: dealNstockWithProduct</p> 
	 * <p>Description: 获取到指定组合商品的商品库存</p> 
	 * @param product
	 * @return
	 */
	private long dealNstockWithProduct(Long pgid) {
		List<ProductGroup> productGroups = productGroupService.findProductGroupListByPgId(pgid);
		long nstock = 9999L;
		for (ProductGroup productGroup : productGroups) {
			Product product = productService.findProductById(productGroup.getPid());
			if(product.getNstock()==0L||product.getNstock()<productGroup.getNum()){
				nstock = 0L;
				break;
			}
			long tempNstock = product.getNstock() / productGroup.getNum();
			if(tempNstock < nstock&&tempNstock>0){
				nstock = tempNstock;
			}
		}
		return nstock==9999L?0:nstock;
	}
	/**
	 * 跳转至新增组合商品页面
	 * @return
	 */
	public Result newOrUpdateProductGroup(){
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
		List<ProductGroup> productGroupList = new ArrayList<ProductGroup>();
		List<ProductImages> productImagesList = new ArrayList<ProductImages>();
		Product product = productService.findProductById(pid);
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
			if(product.getPid()!=null&&product.getPid().longValue()!=0){
				productGroupList = productGroupService.findProductGroupListByPgId(product.getPid());
			}
			for(ProductGroup productGroupTemp : productGroupList){
				Product productTemp = productService.findProductById(productGroupTemp.getPid());
				if(productTemp!=null){
					productTemp.setHiPrice("¥"+productTemp.getRmbprice());//嗨购价
					productTemp.setChaListPrice("¥"+productTemp.getChinaprice());//市场价
					productGroupTemp.setProduct(productTemp);
				}
			}
			currencyListHtml = Html.apply(ProductService.currencyList2Html(currencyList,product.getCurrency()));//默认人民币
			depositTypes = Html.apply(DepositType.typs2HTML(product.getStage()));
//			
		}
		return ok(views.html.product.newOrUpdateProductGroup.render(categoryFirstHtml,wayremarks,fromsites,currencyListHtml,depositTypes,distincts,pics,pid,product,productImagesList,detailList,productGroupList,activeNum));
	}
	
	/**
	 * 保存商品基本详情
	 * @return
	 */
	@AdminAuthenticated()
	public Result saveProductGroup(){
		Long pid = Numbers.parseLong(Form.form().bindFromRequest().get("pidProduct"), Long.valueOf(0));
		AdminUser adminUser = getCurrentAdminUser();
		Product product = productService.findProductById(pid);
		String typesRadios = "2";//类型
		Long nstockTemp = 0L;
		if(product==null){
			product = new Product();
			product.setStatus(1);//新品待审
			product.setTyp(typesRadios);
		}else{
			nstockTemp = product.getNstock();
		}
		if(pid.longValue()==0){
			product.setEndorsementPrice(0.0);
			product.setDate_add(new Date());
			product.setIsSyncErp(9);//是否同步ERP
			product.setWeight(0.0);//商品重量默认为0.0
		}
		String chinaprice = Form.form().bindFromRequest().get("chinaprice");//国内售价
		product.setChinaprice(Numbers.parseDouble(chinaprice, 0.0));
		String rmbprice = Form.form().bindFromRequest().get("rmbprice");//嗨购售价
		product.setRmbprice(Numbers.parseDouble(rmbprice, 0.0));
		BigDecimal b = new BigDecimal(Double.toString(100));
		BigDecimal b1 = new BigDecimal(rmbprice);
		Double priceDouble = b.multiply(b1).doubleValue();
		product.setPrice(priceDouble);
		product.setCurrency(1);
		product.setIslockprice(1);//锁定价格
		product.setFreight((double) 0);
		String isSyncErp = Form.form().bindFromRequest().get("isSyncErp");
		product.setIsSyncErp(Numbers.parseInt(isSyncErp, 9));
		String preProduct = Form.form().bindFromRequest().get("preProduct");//是否是预售 1普通 2撒娇 3预售 4定时开抢
		String lovelyProduct = Form.form().bindFromRequest().get("lovelyProduct");//是否是撒娇 1普通 2撒娇 3预售 4定时开抢
		String timProduct = Form.form().bindFromRequest().get("timProduct");//是否是定时开抢 1普通 2撒娇 3预售 4定时开抢
		product.setMancnt(0);//映射的是int 默认值暂时不起作用
		product.setDeposit(0.0);
		product.setRtitle("");
		product.setPreselltoast("");
		if("1".equals(lovelyProduct)){//撒娇商品
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
			product.setPtyp("1");		//是否预售。。。
			product.setLovelydistinct(10.0);
			product.setIslovely("0");//普通商品
			product.setBtim("");
			product.setEtim("");
		}
		String newMantypeRadios = "0";//1首购商品 2仅一次商品 0普通（不做处理）3、0元商品  p2类型
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
		product.setNstock(Numbers.parseLong(nstock, -99L));
		String limitcount = Form.form().bindFromRequest().get("limitcount");//限购数量
		product.setLimitcount(Numbers.parseInt(limitcount, 0));
		
		product.setWayremark(Constants.Wayremark.wayRemark2Message(1));//默认发货地为嗨个购
		String nlikes = Form.form().bindFromRequest().get("nlikes");//喜欢数
		product.setNlikes(Numbers.parseInt(nlikes, 0));
		
		product.setIsopenid(0);//是否需要身份证信息 默认不需要
		product.setIsopenidimg(0);//是否需要身份证图片信息  默认不需要
		String national = Form.form().bindFromRequest().get("national");//是否展示国旗
		if("1".equals(national)){
			String nationalFlag = Form.form().bindFromRequest().get("nationalFlag");//国旗标志
			product.setNationalFlag(nationalFlag);
		}else{
			product.setNationalFlag("");
		}
		product.setDate_upd(new Date());
		product.setIshot(1);
		//库存
		//重量
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
		if(pid.longValue()==0){//如果是新增，需要初始化两个详情
			operateLogService.saveProductGroupLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "新建组合商品,创建后的组合商品ID为:"+product.getPid());
			ProductDetail productDetail=new ProductDetail();
			productDetail.setChname("Details");
			productDetail.setEnname("商品详情");
			productDetail.setDetail("");
			productDetail.setDate_add(new Date());
			productDetail.setPid(product.getPid());
			productDetail.setNsort(0);
			productDetail = productService.saveDetail(productDetail);
			ProductDetail productDetail2=new ProductDetail();
			productDetail2.setChname("Parameters");
			productDetail2.setEnname("商品参数");
			productDetail2.setDetail("");
			productDetail2.setDate_add(new Date());
			productDetail2.setPid(product.getPid());
			productDetail2.setNsort(0);
			productDetail2 = productService.saveDetail(productDetail2);
		}else{
			if(nstockTemp!=null && nstockTemp.longValue()!=Numbers.parseLong(nstock, -99L)){//修改库存管理
				operateLogService.saveProductGroupLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "编辑组合商品PID:"+product.getPid()+"的库存由"+nstockTemp+"变更为"+nstock);
			}//编辑除修改库存外的操作
			operateLogService.saveProductGroupLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "编辑组合商品,编辑的商品ID为:"+product.getPid());
		}
		return redirect("/product/newOrUpdateProductGroup?pid="+product.getPid()+"&activeNum="+2);
	}
	
	/**
	 * 上传列表图片
	 * @return
	 */
	public Result uploadListPicWithProductGroup(){
		response().setContentType("application/json;charset=utf-8");
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
		return redirect("/product/newOrUpdateProductGroup?pid="+pid+"&activeNum="+2);
	}
	
	/**
	 * 上传商品图片
	 * @return
	 */
	public Result uploadProductImagesWithProductGroup(){
		response().setContentType("application/json;charset=utf-8");
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
	 * 
	 * <p>Title: searchProductGroup</p> 
	 * <p>Description: 添加组合商品</p> 
	 * @return
	 */
	public Result searchProductGroup(){
		response().setContentType("application/json;charset=utf-8");
		Long pid = Numbers.parseLong(Form.form().bindFromRequest().get("pid"), Long.valueOf(0));
		Long pgid = Numbers.parseLong(Form.form().bindFromRequest().get("pgid"), Long.valueOf(0));
		ObjectNode result=Json.newObject();
		//判断商品是否存在
		if(pgid>0&&pid>0){
			//根据商品ID和PGid获得组合信息，如果为空，则添加，如果不为空，则修改数量和时间
			Product product = productService.findProductById(pid);
			return ok(Json.toJson(product));
		}else{
			result.put("status", 0);
		}
		return ok(Json.toJson(result));
	}
	
	/**
	 * 
	 * <p>Title: addProductGroup</p> 
	 * <p>Description: 添加组合商品</p> 
	 * @return
	 */
	@AdminAuthenticated()
	public Result addProductGroups(){
		response().setContentType("application/json;charset=utf-8");
		String attr = Form.form().bindFromRequest().get("attr");
		AdminUser adminUser = getCurrentAdminUser();
		if(!Strings.isNullOrEmpty(attr)){
			String[] attrs = attr.split(";");
			Long pgid = Numbers.parseLong(attr.split(",")[0], 0L);
			productGroupService.deleteAllWithPgid(pgid);
			double weight = 0.0;
			for (String string : attrs) {
				String[] ss = string.split(",");
				Long pid = Numbers.parseLong(ss[1], 0L);
				int num = Numbers.parseInt(ss[2], 0);
				//判断组合商品是否存在
				ProductGroup productGroup = productGroupService.queryProductGroup(pgid,pid);
				if(productGroup == null){
					productGroup = new ProductGroup();
					productGroup.setPgid(pgid);
					productGroup.setPid(pid);
					productGroup.setDate_add(new Date());
				}
				Product product = productService.findProductById(pid);
				productGroup.setNum(num);
				weight += num*product.getWeight();
				productGroup.setDate_upd(new Date());
				productGroup = productGroupService.save(productGroup);
				operateLogService.saveProductGroupLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "保存组合商品ID:"+pgid+"下子商品信息完成，子商品ID:"+productGroup.getId()+",子商品数量："+productGroup.getNum());
			}
			operateLogService.saveProductGroupLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "保存组合商品子商品信息完成，组合商品ID:"+pgid);
			Product product = productService.findProductById(pgid);
			product.setWeight(weight);
			productService.saveProduct(product);
		}
		ObjectNode result=Json.newObject();
		result.put("status", 1);
		return ok(Json.toJson(result));
	}
	
	/**
	 * 
	 * <p>Title: getProductGroupByPid</p> 
	 * <p>Description: 根据子商品ID获取到组合商品列表</p> 
	 * @param pid
	 * @return
	 */
    @AdminAuthenticated()
	public Result getProductGroupByPid(Long pid){
		ObjectNode result=Json.newObject();
		StringBuffer sb = new StringBuffer();
		List<ProductGroup> productGroups = productGroupService.getProductGroupByPid(pid);
		for (int i = 0; i < productGroups.size(); i++) {
			sb.append(productGroups.get(i).getPgid());
			if(i < productGroups.size()-1){
				sb.append(",");
			}
		}
		String groupStr = sb.toString();
		result.put("status", groupStr);
		return ok(Json.toJson(result));
	}
}
