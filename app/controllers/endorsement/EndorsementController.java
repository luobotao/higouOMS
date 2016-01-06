package controllers.endorsement;

import java.io.File;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import assets.CdnAssets;
import controllers.admin.AdminAuthenticated;
import controllers.admin.BaseAdminController;
import forms.ProductManageForm;
import forms.UserQueryForm;
import forms.admin.ChannelQueryForm;
import forms.admin.SubjectQueryForm;
import models.Bbtaddress;
import models.admin.AdminUser;
import models.admin.OperateLog;
import models.endorsement.Endorsement;
import models.product.Category;
import models.product.Currency;
import models.product.Fromsite;
import models.product.Mould;
import models.product.PadChannel;
import models.product.PadChannelPro;
import models.product.PadChannelProView;
import models.product.Product;
import models.subject.Subject;
import models.subject.SubjectMould;
import models.subject.SubjectMouldPro;
import models.user.User;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.twirl.api.Html;
import services.ServiceFactory;
import services.admin.AdminUserService;
import services.endorsement.EndorsementService;
import services.product.CategoryService;
import services.product.ChannelService;
import services.product.ProductService;
import services.subject.SubjectService;
import services.user.UserService;
import utils.AjaxHelper;
import utils.Constants;
import utils.Constants.ProductStatus;
import utils.Constants.ShowFlag;
import utils.Constants.ShowTypes;
import utils.Constants.TypsProduct;
import utils.Dates;
import utils.JdbcOper;
import utils.MatrixToImageWriter;
import utils.Numbers;
import utils.OSSUtils;
import utils.StringUtil;
import vo.SubjectMouldProVO;
import vo.SubjectMouldVO;

/**
 * 代言管理模块
 * @author luobotao
 * @Date 2015年8月29日
 */
@Named
@Singleton
public class EndorsementController extends BaseAdminController {
	private static final Logger.ALogger logger = Logger.of(EndorsementController.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
    private final Form<ProductManageForm> productManageForm = Form.form(ProductManageForm.class);
	
	private final EndorsementService endorsementService;
	private final UserService userService;
	private final ProductService productService;
	private final ChannelService channelService;
	private final AdminUserService adminUserService;
	private final CategoryService categoryService;
	
    @Inject
    public EndorsementController(final EndorsementService endorsementService,final UserService userService,final ProductService productService,final CategoryService categoryService,final ChannelService channelService,final AdminUserService adminUserService) {
        this.endorsementService = endorsementService;
        this.userService = userService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.adminUserService = adminUserService;
        this.channelService = channelService;
    }
    
    /**
     * 商户列表
     * @return
     */
    @AdminAuthenticated()
    public Result companyManage() {
    	Form<UserQueryForm> userQueryForm = Form.form(UserQueryForm.class);
    	Form<UserQueryForm> form = userQueryForm.bindFromRequest();
    	UserQueryForm userForm = new UserQueryForm();
		if (!form.hasErrors()) {
			userForm = form.get();
        }
		List<Bbtaddress> provinceList = endorsementService.findByParentid(0);
		Html province = Html.apply(EndorsementService.provinceList2Html(provinceList, userForm.province));
		userForm.danyanFlag="1";
    	Page<User> userList = userService.findUserPage(userForm);
    	return ok(views.html.endorsement.companyManage.render(province,userList,userForm));
    }
    /**
     * 新建或编辑商户
     * @return
     */
    @AdminAuthenticated()
    public Result newOrUpdateCompany() {
    	String uid = Form.form().bindFromRequest().get("uid");//手机
    	User user = userService.findUserById(Numbers.parseLong(uid, 0L));
    	List<Bbtaddress> provinceList = endorsementService.findByParentid(0);
    	Html province =null;
    	Html city =null;
    	if(user!=null){
    		province = Html.apply(EndorsementService.provinceList2Html(provinceList, user.getProvince()));
    		List<Bbtaddress> cityList = endorsementService.findByParentid(user.getProvince());
    		city = Html.apply(EndorsementService.cityList2Html(cityList, user.getCity()));
    	}else{
    		province = Html.apply(EndorsementService.provinceList2Html(provinceList, -1));
    		city = Html.apply("<option value=\"-1\">城市</option>");
    	}
    	
    	return ok(views.html.endorsement.newOrUpdateCompany.render(province,city,user));
    }
    /**
     * 保存商户
     * @return
     */
    @AdminAuthenticated()
    public Result saveCompany() {
    	String phone = Form.form().bindFromRequest().get("phone");//手机
    	String nickname = Form.form().bindFromRequest().get("nickname");//昵称
    	String province = Form.form().bindFromRequest().get("province");//省份
    	String city = Form.form().bindFromRequest().get("city");//城市
    	String address = Form.form().bindFromRequest().get("address");//地址
    	String contactPerson = Form.form().bindFromRequest().get("contactPerson");//联系人
    	String contactPhone = Form.form().bindFromRequest().get("contactPhone");//联系电话
    	
    	List<User> userList = userService.findByPhone(phone);
    	String userIdForSave = Form.form().bindFromRequest().get("userIdForSave");//编辑时的用户ID
    	if(!userList.isEmpty()){
    		boolean flag = false;
    		for(User userTemp:userList){
    			if(userTemp.getUid().longValue()==Numbers.parseLong(userIdForSave, 0L).longValue()){
    				flag = true;
    				break;
    			}
    		}
    		if(!flag){
    			return ok(Json.toJson("手机号码已注册！"));
    		}
    	}
    	User user;
    	if(StringUtils.isBlank(userIdForSave)){
    		user =new User();
    		user.setGid(4L);//1普通用户组2新用户组3旧用户组4商户组
    		user.setPasswords(StringUtil.getMD5("123456"));
    		user.setActive(1);
        	user.setIsEndorsement(1);//可代言
        	user.setDate_add(new Date());
    	}else{
    		user = userService.findUserById(Numbers.parseLong(userIdForSave, 0L));
    	}
    	user.setPhone(phone);
    	user.setNickname(nickname);
    	user.setProvince(Numbers.parseInt(province, 0));
    	user.setCity(Numbers.parseInt(city, 0));
    	user.setAddress(address);
    	user.setContactPerson(contactPerson);
    	user.setContactPhone(contactPhone);
    	
    	user.setDateUpd(new Date());
    	user = userService.saveUser(user);
    	if(StringUtils.isBlank(userIdForSave)){//新建商户时需要初始化此商户的后台管理账户
    		AdminUser adminUser = adminUserService.findByUsername(phone);
        	adminUser.setUsername(phone);
        	adminUser.setPasswd(StringUtil.getMD5("123456"));
        	adminUser.setRealname(nickname);
        	adminUser.setActive(1);
        	adminUser.setDate_add(new Date());
        	adminUser.setLastip(request().remoteAddress());
        	adminUser.setDate_login(new Date());
        	adminUser.setLogin_count(0);
        	adminUser.setHeadIcon("/pimgs/comment/tximage/headIcon.png");
        	adminUser.setAdminType("3");
        	adminUser.setUid(user.getUid());
        	adminUser.setRoleId(0);
        	adminUserService.updateAdminUser(adminUser);
        	
        	File file = new File("/data/defaultPadChannelAndPro.xls");
			Boolean flag = endorsementService.importFile(file,user.getUid(),user.getGid());//新建商户时默认生成频道与频道商品
    	}
    	
    	return ok(Json.toJson("1"));
    }
    
    /**
     * 修改商户状态 停用或启用
     * @param userid
     * @param stateFlag
     * @return
     */
    @AdminAuthenticated()
    public Result updateCompanyState(Long userid,Integer stateFlag){
    	User user = userService.findUserById(userid);
    	if(user!=null){
    		user.setState(stateFlag);
    		userService.saveUser(user);
    		return ok(Json.toJson("1"));
    	}else{
    		return ok(Json.toJson("0"));
    	}
    }
    /**
	 * 商户频道管理
	 * @return
	 */
    @AdminAuthenticated()
	public Result channelManage(){
		Form<ChannelQueryForm> form = Form.form(ChannelQueryForm.class).bindFromRequest();
		ChannelQueryForm channelQueryForm = new ChannelQueryForm();
		if (!form.hasErrors()) {
			channelQueryForm = form.get();
        }
		AdminUser adminUser = getCurrentAdminUser();
		if("3".equals(adminUser.getAdminType())){
			channelQueryForm.userid = adminUser.getUid();
		}
    	Page<PadChannel> channelPage = channelService.queryPadChannel(channelQueryForm);
    	for(PadChannel padChannel:channelPage){
    		User user =userService.findUserById(padChannel.getUserid());
			if(user!=null){
				padChannel.setUser(user);
			}
    	}
    	return ok(views.html.endorsement.channelManage.render(channelPage,adminUser));
	}
	/**
	 * 新建商户频道
	 * @return
	 */
    @AdminAuthenticated()
	public Result newChannel(){
		Long userId=Numbers.parseLong(AjaxHelper.getHttpParam(request(), "userid"), -1L);
		AdminUser adminUser = getCurrentAdminUser();
		if("3".equals(adminUser.getAdminType()) && userId.longValue()==-1){
			userId = adminUser.getUid();
		}
		
		String cid=AjaxHelper.getHttpParam(request(), "cid");
		String cname=AjaxHelper.getHttpParam(request(), "cname");
		String nsort=AjaxHelper.getHttpParam(request(), "nsort");
		PadChannel padChannel = new PadChannel();
		if(!StringUtils.isBlank(cid) && Numbers.parseLong(cid, 0L).longValue()!=0){
			padChannel = channelService.findPadChannelById(Numbers.parseLong(cid, 0L));
		}else{//新建频道
			padChannel.setSta("0");//默认不显示
			padChannel.setUserid(userId);
			padChannel.setDate_add(new Date());
		}
		padChannel.setCname(cname);
		padChannel.setNsort(Numbers.parseInt(nsort, 0));
		padChannel = channelService.savePadChannel(padChannel);
		return ok(Json.toJson("1"));
	}
	
	/**
	 * 根据ID获取该频道
	 * @return
	 */
    @AdminAuthenticated()
	public Result getChannelById(Long channelId){
		PadChannel channel = channelService.findPadChannelById(channelId);
		return ok(Json.toJson(channel));
	}
	/**
	 * 根据ID删除该频道
	 * @return
	 */
    @AdminAuthenticated()
	public Result deletechannelById(Long channelId){
		channelService.deletePadchannelById(channelId);
		return ok(Json.toJson("1"));
	}
	/**
	 * 修改频道显示或隐藏
	 * @return
	 */
    @AdminAuthenticated()
	public Result changeChannelSta(){
		Long channelId = Numbers.parseLong(AjaxHelper.getHttpParam(request(), "channelId"), 0L);
		String channelsta = AjaxHelper.getHttpParam(request(), "channelsta");
		channelService.updatePadChannelSta(channelId,channelsta);
		return ok(Json.toJson("1"));
	}
	
	
	/**
	 * 根据频道ID获取下面的频道商品
	 * @return
	 */
    @AdminAuthenticated()
	public Result getChannelProByCid(Long userId,Long channelId){
		Form<ChannelQueryForm> form = Form.form(ChannelQueryForm.class).bindFromRequest();
		ChannelQueryForm channelQueryForm = new ChannelQueryForm();
		if (!form.hasErrors()) {
			channelQueryForm = form.get();
        }
		channelQueryForm.channelId=channelId;
		User user = userService.findUserById(userId);
		if(6==user.getGid()){
			Page<PadChannelPro> padChannelProPage = channelService.queryPadChannelProPage(channelQueryForm);
			for(PadChannelPro padChannelPro : padChannelProPage.getContent()){
				Endorsement endorsement = endorsementService.findEndorsementById(padChannelPro.getEid());
				if(endorsement!=null)
				{
					Product product =productService.findProductById(endorsement.getProductId());
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
						endorsement.setProducinfo(product);
					}
					padChannelPro.setEndorsement(endorsement);
				}
			}
			ChannelQueryForm bannerForm = new ChannelQueryForm();
			bannerForm.typFlag="1";
			bannerForm.channelId=channelId;
			List<PadChannelPro> padChannelBannerList = channelService.queryPadChannelProPage(bannerForm).getContent();
			bannerForm.gid = user.getGid();
			return ok(views.html.endorsement.padContentManage.render(userId,bannerForm.gid,channelId,padChannelProPage,padChannelBannerList));
		}else if(4 == user.getGid()){
			Page<PadChannelPro> padChannelProPage = channelService.queryPadChannelProPage(channelQueryForm);
			for(PadChannelPro padChannelPro : padChannelProPage.getContent()){
				Endorsement endorsement = endorsementService.findEndorsementById(padChannelPro.getEid());
				if(endorsement!=null)
				{
					Product product =productService.findProductById(endorsement.getProductId());
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
						endorsement.setProducinfo(product);
					}
					padChannelPro.setEndorsement(endorsement);
				}
			}
			ChannelQueryForm bannerForm = new ChannelQueryForm();
			bannerForm.typFlag="1";
			bannerForm.channelId=channelId;
			List<PadChannelPro> padChannelBannerList = channelService.queryPadChannelProPage(bannerForm).getContent();
			bannerForm.gid = user.getGid();
			return ok(views.html.endorsement.padChannelProList.render(userId,bannerForm.gid,channelId,padChannelProPage,padChannelBannerList));
		}else{
			return ok("非商户用户");
		}
	}
	
	/**
	 * 将商品加入到商户频道
	 * @return
	 */
    @AdminAuthenticated()
	public Result productListInChannel(){
		String pIds = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "products");
		Long cid = Numbers.parseLong(AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "cid"), -1L);
		PadChannel channel = channelService.findPadChannelById(cid);
		if(channel!=null){
			User user = userService.findUserById(channel.getUserid());
			if(user==null){
				return ok(Json.toJson("0")); 
			}
	    	String BUCKET_NAME=StringUtil.getBUCKET_NAME();
	    	String exportPath=Configuration.root().getString("oss.upload.dimensional.image", "/");//上传路径
			JsonNode json = Json.parse(pIds);
			for(JsonNode pIdTemp : json){
				//根据pid与用户id去检查此用户是否已代言过此商品,且此代言处于未删除状态
				ProductManageForm form = new ProductManageForm();
				form.pid=pIdTemp.asLong();
				form.userId = channel.getUserid();
				Endorsement endorsement=null;
				List<Endorsement> endorsementList = endorsementService.queryEndorsementList(form);
				if(endorsementList.isEmpty()){
					Product product = productService.findProductById(pIdTemp.asLong());
					if(product!=null ){
						endorsement = new Endorsement();
						if(user!=null){
							endorsement.setUserId(channel.getUserid());
							endorsement.setGid(user.getGid());
						}
						//针对嗨购商城做的处理
						if(StringUtil.getConfigUid().equals(String.valueOf(user.getUid()))){
							endorsement.setCommisionTyp(2);//佣金类型
							endorsement.setCommision(10.0);
							BigDecimal b = new BigDecimal(Double.toString(0.01));
							BigDecimal b1 = new BigDecimal(Double.toString(product.getPrice()));
							Double price = b.multiply(b1).doubleValue();					//初始化默认代言价等于嗨购价
							endorsement.setEndorsementPrice(price);
	    	    		}else{
	    	    			endorsement.setCommisionTyp(product.getCommisionTyp());//佣金类型
	    	    			endorsement.setCommision(product.getCommision());
	    	    			endorsement.setEndorsementPrice(product.getEndorsementPrice());
	    	    		}
						endorsement.setProductId(pIdTemp.asLong());
						endorsement.setRemark("");
						endorsement.setStatus(0);//0用户上传未审核，1正常（审核通过），2后台审核未通过,3删除
						endorsement.setBannerimg(StringUtil.getSheSaidIcon());
						endorsement.setCreateTime(new Date());
						endorsement.setCount(0);
						endorsement = endorsementService.saveOrUpdateEnorsement(endorsement);
	    	    		//针对海购商城不再生成二维码
	    	    		if(!StringUtil.getConfigUid().equals(String.valueOf(user.getUid()))){
	    	    			//生成二维码
		        			try {
		        				String url=StringUtil.getAPIDomain()+"/sheSaid/show?uid="+channel.getUserid()+"&pid="+pIdTemp.asLong()+"&daiyanid="+endorsement.getEid()+"&wx=0";
		        				
		        				MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
		        				Map hints = new HashMap();
		        				hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		        				BitMatrix bitMatrix = multiFormatWriter.encode(url,BarcodeFormat.QR_CODE, 400, 400, hints);
		        				String fileNameLast = UUID.randomUUID().toString().replaceAll("-", "")+".jpg";//最终的文件名称
		        				String tempPath =Configuration.root().getString("temp.path", "/");//本地上传临时路径
		        				File file1 = new File(tempPath, fileNameLast);
		        				file1 = MatrixToImageWriter.writeToFile(bitMatrix, "jpg", file1);
		        				String endfilestr = OSSUtils.uploadFile(file1,exportPath,fileNameLast, ".jpg",BUCKET_NAME);
		        				endorsement.setDimensionalimg(endfilestr);//二维码路径
		        				file1.delete();
		        			} catch (Exception e) {
		        				e.printStackTrace();
		        			}
		        			endorsement = endorsementService.saveOrUpdateEnorsement(endorsement);
	    	    		}
					}
				}else{
					endorsement  = endorsementList.get(0);
				}
				Long eid = endorsement.getEid();
				//根据eid与cid去padChannelPro表里检查是否已存在此商品
				Long count = channelService.countPadChannelByEidAndCid(eid,cid);
				if(count==null || count.longValue()==0){
					PadChannelPro padChannelPro = new PadChannelPro();
					padChannelPro.setCid(cid);
					padChannelPro.setEid(eid);
					padChannelPro.setDate_add(new Date());
					padChannelPro.setPid(pIdTemp.asLong());
					padChannelPro.setTyp("0");//显示与否
					padChannelPro.setNsort(0);
					padChannelPro.setTypFlag("0");
					padChannelPro = channelService.savePadChannelPro(padChannelPro);
				}
			}
		}else{
			return ok(Json.toJson("0")); 
		}
		return ok(Json.toJson("1"));
	}
	
	/**
	 * 根据代言ID获取该代言
	 * @param eid
	 * @return
	 */
    @AdminAuthenticated()
	public Result getEndorsementById(Long eid){
		Endorsement endorsement = endorsementService.findEndorsementById(eid);
		if(endorsement!=null){
			return ok(Json.toJson(endorsement));
		}else{
			return ok(Json.toJson("0"));
		}
	}
	
	/**
	 * 保存修改后的商户代言
	 */
    @AdminAuthenticated()
	public Result saveEndorsement(){
		String eIdForUpdate = Form.form().bindFromRequest().get("eIdForUpdate");//
		String nsortForUpdate = Form.form().bindFromRequest().get("nsortForUpdate");//channelPro里的排序
		String channelProIdForUpdate = Form.form().bindFromRequest().get("channelProIdForUpdate");//channelPro id
		if(!StringUtils.isBlank(eIdForUpdate)){
			Endorsement endorsement = endorsementService.findEndorsementById(Numbers.parseLong(eIdForUpdate, 0L));
			String endorsementPrice = Form.form().bindFromRequest().get("endorsementPrice");//代言价
			endorsement.setEndorsementPrice(Numbers.parseDouble(endorsementPrice, 0.0));
			String commisionTyp = Form.form().bindFromRequest().get("commisionTyp");//代言价
			endorsement.setCommisionTyp(Numbers.parseInt(commisionTyp, 0));//佣金类型
			String commision = Form.form().bindFromRequest().get("commision");//佣金
			endorsement.setCommision(Numbers.parseDouble(commision, 0.0));
			endorsement = endorsementService.saveOrUpdateEnorsement(endorsement);
		}
		if(!StringUtils.isBlank(channelProIdForUpdate)){//修改频道商品的排序
			channelService.updatePadChannelProNsort(Numbers.parseLong(channelProIdForUpdate, 0L),Numbers.parseInt(nsortForUpdate, 0));
		}
		return ok(Json.toJson("1"));
	}
	
	
	/**
	 * 修改频道里的商户商品显示或隐藏
	 * @return
	 */
    @AdminAuthenticated()
	public Result changeChannelProSta(Long id,String type){
		PadChannelPro padChannelPro = channelService.findPadChannelProById(id);
		if(padChannelPro!=null){
			if("1".equals(padChannelPro.getTypFlag())||"2".equals(padChannelPro.getTypFlag())){//Banner
				channelService.updatePadChannelProSta(id,type);
				return ok(Json.toJson("1"));
			}else if("0".equals(padChannelPro.getTypFlag())){
				Endorsement endorsement = endorsementService.findEndorsementById(padChannelPro.getEid());
				if(endorsement!=null && endorsement.getEndorsementPrice()>0){
					channelService.updatePadChannelProSta(id,type);
					return ok(Json.toJson("1"));
				}else{
					return ok(Json.toJson("代言价格必须大于零"));
				}
			}
		}
		return ok(Json.toJson("不存在此商品"));
	}
	
	/**
	 * 根据代言商品ID删除代言商品
	 * @param id
	 * @return
	 */
    @AdminAuthenticated()
	public Result deleteChannelPro(Long id){
		channelService.deletePadChannelProById(id);
		return ok(Json.toJson("ok"));
	}
	
	
	/**
	 * 代言商品管理
	 * @return
	 */
    @AdminAuthenticated()
	public Result productsManage(){
		Form<ProductManageForm> form = productManageForm.bindFromRequest();
		ProductManageForm formPage = new ProductManageForm();
		if (!form.hasErrors()) {
        	formPage = form.get();
        }
		formPage.isEndorsement=1;//标识是代言商品列表
		Page<Product> productPage = productService.queryProductPage(formPage);
		List<Category> categoryList = categoryService.categoryList(0,1);
		List<Fromsite> fromsiteList  = productService.queryAllFromsite();
		for(Product product : productPage){
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
		}
		return ok(views.html.endorsement.productManage.render(
				Html.apply(TypsProduct.typs2HTML(formPage.type)),
				Html.apply(ProductService.fromsiteList2Html(fromsiteList,formPage.fromsite)),
				Html.apply(ProductStatus.status2HTML(formPage.status)),
				Html.apply(CategoryService.categoryList2Html(categoryList,formPage.category)),
				productPage));
	}
	/**
	 * 商户代言列表
	 * @return
	 */
    @AdminAuthenticated()
	public Result endorsementList(){
		Form<ProductManageForm> form = productManageForm.bindFromRequest();
		ProductManageForm formPage = new ProductManageForm();
		if (!form.hasErrors()) {
			formPage = form.get();
		}
		Page<Endorsement> endorsementPage = endorsementService.queryEndorsementPage(formPage);
		for(Endorsement endorsement : endorsementPage){
			Product product =productService.findProductById(endorsement.getProductId());
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
				endorsement.setProducinfo(product);
			}
			User user =userService.findUserById(endorsement.getUserId());
			if(user!=null){
				endorsement.setUser(user);
			}
		}
		return ok(views.html.endorsement.endorsementList.render(endorsementPage));
	}
	/**
	 * 根据类型不同跳转至不同的商户代言商品管理页面（新建或者编辑）
	 * @param type
	 * @return
	 */
    @AdminAuthenticated()
	public Result newendorsement(String type){
		if("1".equals(type)){//编辑商户代言
			Long eid = Numbers.parseLong(AjaxHelper.getHttpParam(request(), "eid"), 0L);
			Endorsement endorsement = endorsementService.findEndorsementById(eid);
			if(endorsement!=null){
				endorsement.setEndorsImgList(endorsementService.findEndorsementImageByEid(eid));
			}
			return ok(views.html.endorsement.updateCompanyEndorsement.render(endorsement));
		}else{//新建商铺代言
			return ok(views.html.endorsement.newCompanyEndorsement.render());
		}
	}
	
	/**
	 * 根据图片ID删除代言图片
	 * @param id
	 * @return
	 */
    @AdminAuthenticated()
	public Result deleteEndorsementImage(Long id){
		endorsementService.deleteEndorsementImageById(id);
		return ok(Json.toJson("ok"));
	}
	/**
	 * 修改代言状态
	 * @param id
	 * @return
	 */
    @AdminAuthenticated()
	public Result updateEndorsementState(){
		String eid = Form.form().bindFromRequest().get("eid");//
		String status = Form.form().bindFromRequest().get("status");//
		Endorsement endorsement =  endorsementService.findEndorsementById(Numbers.parseLong(eid, 0L));
		if(endorsement!=null){
			endorsement.setStatus(Numbers.parseInt(status, 0));
			endorsementService.saveOrUpdateEnorsement(endorsement);
		}
		return redirect(routes.EndorsementController.endorsementList());
	}
	
	/**
	 * 商品列表(弹窗)
	 * @return
	 */
    @AdminAuthenticated()
	public Result products(){
		String isEndorsement = AjaxHelper.getHttpParam(request(), "isEndorsement");
		ProductManageForm productManageForm = new ProductManageForm();
		productManageForm.pid=Numbers.parseLong(AjaxHelper.getHttpParam(request(), "pidForSearch"), 0L);
		productManageForm.keyWords = AjaxHelper.getHttpParam(request(), "pNameForSearch");
		productManageForm.isEndorsement = Numbers.parseInt(isEndorsement, -1);
		if (productManageForm.pid.longValue()==0 && StringUtils.isBlank(productManageForm.keyWords)) {
			return ok(views.html.endorsement.products.render(new ArrayList<Product>()));
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
		}
		return ok(views.html.endorsement.products.render(productList));
	}
	
	
	/**
	 * 商品列表(批量添加)(弹窗)
	 * @return
	 */
    @AdminAuthenticated()
	public Result productsBatch(){
		String isEndorsement = AjaxHelper.getHttpParam(request(), "isEndorsement");
		ProductManageForm productManageForm = new ProductManageForm();
		productManageForm.pids=AjaxHelper.getHttpParam(request(), "pidForSearch");
		productManageForm.keyWords = AjaxHelper.getHttpParam(request(), "pNameForSearch");
		productManageForm.newSkus = AjaxHelper.getHttpParam(request(), "newSkuForSearch");
		productManageForm.isEndorsement = -1;//去掉代言条件
		productManageForm.type = 2;//自营商品
		//productManageForm.containsUnion = false;//不搜索联营商品
		productManageForm.isfromshop=true;//是否来自商城的查询，如果是则默认要求商品在线
		if (Strings.isNullOrEmpty(productManageForm.pids) && StringUtils.isBlank(productManageForm.keyWords) && StringUtils.isBlank(productManageForm.newSkus)) {
			return ok(views.html.endorsement.productsBatch.render(new ArrayList<Product>()));
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
		}
		return ok(views.html.endorsement.productsBatch.render(productList));
	}
	
	/**
	 * 根据省份id获取下面的城市
	 * @param parentid
	 * @param cityId
	 * @return
	 */
    @AdminAuthenticated()
	public Result findBbtCityByParentid(Integer parentid,Integer cityId){
		List<Bbtaddress> cityList = endorsementService.findByParentid(parentid);
		return ok(Html.apply(EndorsementService.cityList2Html(cityList, cityId)));
	}
    
    /**
	 * 导入商户频道与商品
	 * 
	 * @return
	 */
	@AdminAuthenticated()
	public Result importProductFile() {
		String uidForImport = Form.form().bindFromRequest().get("uidForImport");
		logger.info(uidForImport);
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart companyFile = body.getFile("companyFile");
		if (companyFile != null) {
			String fileName = companyFile.getFilename();
			if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
				File file = companyFile.getFile();
				Long userid= Numbers.parseLong(uidForImport, 0L);
				User user = userService.findUserById(userid);
				if(user==null){
					return ok(Json.toJson("0")); 
				}
				Boolean flag = endorsementService.importFile(file,userid,user.getGid());
			} else {
			}
		} else {
		}
		return ok();
	}
	
	
	/**
	 * 商户商品频道创建banner
	 * 
	 * @return
	 */
	@AdminAuthenticated()
	public Result padChannelNewBanner() {
		String nsort = Form.form().bindFromRequest().get("nsort");//排序
		String exturl = Form.form().bindFromRequest().get("exturl");//链接
		String cid = Form.form().bindFromRequest().get("cid");//频道ID
		String newcid = Form.form().bindFromRequest().get("newcid");//选取跳转的频道ID
		String pid = Form.form().bindFromRequest().get("pid");//商品ID
		String eid = Form.form().bindFromRequest().get("eid");//代言ID
		String checkedval = Form.form().bindFromRequest().get("checkedval");//0，商品详情 3频道
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart picFile = body.getFile("picFile");
		if (picFile != null) {
			String path=Configuration.root().getString("oss.upload.endorsement", "upload/endorsement/");//上传路径
        	String BUCKET_NAME=StringUtil.getBUCKET_NAME();
			String fileName = picFile.getFilename();
			File file = picFile.getFile();//获取到该文件
			int p = fileName.lastIndexOf('.');
			String type = fileName.substring(p, fileName.length()).toLowerCase();
			
			if (".jpg".equals(type)||".gif".equals(type)||".png".equals(type)||".jpeg".equals(type)||".bmp".equals(type)) {
				// 检查文件后缀格式
				String fileNameLast = UUID.randomUUID().toString().replaceAll("-", "")+type;//最终的文件名称
				String endfilestr = OSSUtils.uploadFile(file,path,fileNameLast, type,BUCKET_NAME);	
				PadChannelPro padChannelPro = new PadChannelPro();
				padChannelPro.setCid(Numbers.parseLong(cid, 0L));
				padChannelPro.setEid(Numbers.parseLong(eid, 0L));
				padChannelPro.setDate_add(new Date());
				padChannelPro.setPid(Numbers.parseLong(pid, 0L));
				padChannelPro.setTyp("0");//显示与否
				padChannelPro.setTypFlag("1");//标识是Banner
				padChannelPro.setLinkurl(exturl);
				padChannelPro.setImgurl(endfilestr);
				padChannelPro.setNsort(Numbers.parseInt(nsort, 0));
				padChannelPro = channelService.savePadChannelPro(padChannelPro);
			}
		
		} else {
		}
		return ok();
	}
	
	/**
	 * 频道列表
	 * @return
	 */
    @AdminAuthenticated()
	public Result channelList(){
    	//获取该商户下的频道列表
    	String userId = AjaxHelper.getHttpParam(request(), "userId");
    	String cid = AjaxHelper.getHttpParam(request(), "cid");
    	//如果频道id不为空
    	List<PadChannel> padChennelList = new ArrayList<PadChannel>();
    	if(!Strings.isNullOrEmpty(userId)){
    		//获取该用户下的所有cid集合
    		padChennelList = channelService.findPadchannelByUserid(Numbers.parseLong(userId, 0L));
    	}
		return ok(views.html.endorsement.channelList.render(padChennelList));
	}
	
	/**
	 * 
	 * <p>Title: newOrUpdatePadChannelPro</p> 
	 * <p>Description: </p> 
	 * @return
	 */
	public Result newOrUpdatePadChannelPro(){
		//跳转到新增卡片内容页，主要增加的内容包裹类型（商品，banner,频道，多图），排序，是否显示
		Long cid = Numbers.parseLong(Form.form().bindFromRequest().get("cid"), 0L);//pad channel id
		PadChannelPro padChannelPro = new PadChannelPro();
		Long cpid = Numbers.parseLong(Form.form().bindFromRequest().get("cpid"), 0L);//pad channel pro id
		Long userId=Numbers.parseLong(AjaxHelper.getHttpParam(request(), "userId"), -1L);
		if(cpid.longValue()!=0){
			padChannelPro=endorsementService.findPadChannelProById(cpid);
		}
		List<PadChannelProView> padChannelProViewList = channelService.findPadChannelProViewListByCpId(cpid);
		for (PadChannelProView padChannelProView : padChannelProViewList) {
			Endorsement endorsement = endorsementService.findEndorsementById(padChannelProView.getEid());
			if(endorsement!=null)
			{
				Product product =productService.findProductById(endorsement.getProductId());
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
					endorsement.setProducinfo(product);
				}
				padChannelProView.setEndorsement(endorsement);
			}
		}
		return ok(views.html.endorsement.newOrUpdatePadChannelProView.render(userId,cid,cpid,padChannelPro,padChannelProViewList,
				Html.apply(Constants.PadChannelProCounts.typs2HTML(padChannelPro.getTypCount())),
				Html.apply(Constants.ShowFlag.flag2HTML(padChannelPro.getTyp()))
				));
	}
	
	/**
	 * 
	 * <p>Title: savePadChannelPro</p> 
	 * <p>Description: 保存信息</p> 
	 * @return
	 */
	public Result savePadChannelPro(){
		//AdminUser adminUser = getCurrentAdminUser();
		PadChannelPro padChannelPro=new PadChannelPro();
		Long cpid = Numbers.parseLong(Form.form().bindFromRequest().get("cpid"), 0L);//频道ID
		Long cid = Numbers.parseLong(Form.form().bindFromRequest().get("cid"), 0L);//频道ID
		Long userId=Numbers.parseLong(Form.form().bindFromRequest().get("userId"), 0L);
		padChannelPro.setCid(cid);
		if(cpid.longValue()!=0){
			padChannelPro = endorsementService.findPadChannelProById(cpid);
		}
		Integer nsort =  Numbers.parseInt(Form.form().bindFromRequest().get("nsort"),0);
		padChannelPro.setNsort(nsort);
		String typ =  Form.form().bindFromRequest().get("typ");
		padChannelPro.setTyp(typ);
		String typCount =  Form.form().bindFromRequest().get("typCount");
		padChannelPro.setTypFlag("2");
		padChannelPro.setTypCount(Numbers.parseInt(typCount, 0));
		padChannelPro.setPid(0L);
		padChannelPro.setEid(0L);
		padChannelPro.setDate_add(new Date());
		padChannelPro = endorsementService.savePadChannelPro(padChannelPro);
		return redirect("/endorsement/newOrUpdatePadChannelPro?cid="+cid+"&cpid="+padChannelPro.getId()+"&userId="+userId);
	}
	
	/**
	 * 商户商品频道创建banner
	 * 
	 * @return
	 */
	@AdminAuthenticated()
	public Result padChannelNewChannel() {
		String channelnsort = Form.form().bindFromRequest().get("channelnsort");//排序
		String channelexturl = Form.form().bindFromRequest().get("channelexturl");//链接
		String cid = Form.form().bindFromRequest().get("channelcid");//频道ID
		String eid = Form.form().bindFromRequest().get("eid");//代言ID
		String pid = Form.form().bindFromRequest().get("pid");//商品ID
		String channelnewcid = Form.form().bindFromRequest().get("channelnewcid");//选取跳转的频道ID
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart picFile = body.getFile("channelpicFile");
		if (picFile != null) {
			String path=Configuration.root().getString("oss.upload.endorsement", "upload/endorsement/");//上传路径
        	String BUCKET_NAME=StringUtil.getBUCKET_NAME();
			String fileName = picFile.getFilename();
			File file = picFile.getFile();//获取到该文件
			int p = fileName.lastIndexOf('.');
			String type = fileName.substring(p, fileName.length()).toLowerCase();
			
			if (".jpg".equals(type)||".gif".equals(type)||".png".equals(type)||".jpeg".equals(type)||".bmp".equals(type)) {
				// 检查文件后缀格式
				String fileNameLast = UUID.randomUUID().toString().replaceAll("-", "")+type;//最终的文件名称
				String endfilestr = OSSUtils.uploadFile(file,path,fileNameLast, type,BUCKET_NAME);	
				PadChannelPro padChannelPro = new PadChannelPro();
				padChannelPro.setCid(Numbers.parseLong(cid, 0L));
				padChannelPro.setEid(Numbers.parseLong(eid, 0L));
				padChannelPro.setDate_add(new Date());
				padChannelPro.setPid(Numbers.parseLong(pid, 0L));
				padChannelPro.setTyp("0");//显示与否
				padChannelPro.setTypFlag("2");//标识是N图
				padChannelPro.setLinkurl(channelexturl);
				padChannelPro.setImgurl(endfilestr);
				padChannelPro.setNsort(Numbers.parseInt(channelnsort, 0));
				padChannelPro = channelService.savePadChannelPro(padChannelPro);
			}
		
		} else {
		}
		return ok();
	}
	
	/**
	 * 
	 * <p>Title: savePadChannelProView</p> 
	 * <p>Description: 保存4图详细信息</p> 
	 * @return
	 */
	public Result savePadChannelProView(){
		String nsort = Form.form().bindFromRequest().get("viewnsort");//排序
		String exturl = Form.form().bindFromRequest().get("exturl");//链接
		String cid = Form.form().bindFromRequest().get("cid");//频道ID
		String cpid = Form.form().bindFromRequest().get("cpid");//1*4图ID
		String newcid = Form.form().bindFromRequest().get("newcid");//选取跳转的频道ID
		String pid = Form.form().bindFromRequest().get("pid");//商品ID
		String eid = Form.form().bindFromRequest().get("eid");//代言ID
		String checkedval = Form.form().bindFromRequest().get("checkedval");//0，商品详情3频道
		Long userId=Numbers.parseLong(Form.form().bindFromRequest().get("userId"), 0L);
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart picFile = body.getFile("picFile");
		if (picFile != null) {
			String path=Configuration.root().getString("oss.upload.endorsement", "upload/endorsement/");//上传路径
        	String BUCKET_NAME=StringUtil.getBUCKET_NAME();
			String fileName = picFile.getFilename();
			File file = picFile.getFile();//获取到该文件
			int p = fileName.lastIndexOf('.');
			String type = fileName.substring(p, fileName.length()).toLowerCase();
			
			if (".jpg".equals(type)||".gif".equals(type)||".png".equals(type)||".jpeg".equals(type)||".bmp".equals(type)) {
				// 检查文件后缀格式
				String fileNameLast = UUID.randomUUID().toString().replaceAll("-", "")+type;//最终的文件名称
				String endfilestr = OSSUtils.uploadFile(file,path,fileNameLast, type,BUCKET_NAME);	
				PadChannelProView padChannelProView = new PadChannelProView();
				padChannelProView.setCpid(Numbers.parseLong(cpid, 0L));
				padChannelProView.setEid(Numbers.parseLong(eid, 0L));
				padChannelProView.setDate_add(new Date());
				padChannelProView.setPid(Numbers.parseLong(pid, 0L));
				padChannelProView.setTyp("0");//显示与否
				padChannelProView.setTypFlag(checkedval);//标识是0 商品 1频道
				padChannelProView.setLinkurl(exturl);
				padChannelProView.setImgurl(endfilestr);
				padChannelProView.setNsort(Numbers.parseInt(nsort, 0));
				padChannelProView = channelService.savePadChannelProView(padChannelProView);
			}
		} 
		return ok();
	}
	
	/**
	 * 修改频道里的商户商品显示或隐藏
	 * @return
	 */
    @AdminAuthenticated()
	public Result changeChannelProViewSta(Long id,String type){
		PadChannelProView padChannelProView = channelService.findPadChannelProViewById(id);
		String rstInfo = "1";
		if(padChannelProView!=null){
			if("1".equals(padChannelProView.getTypFlag())){//频道
				channelService.updatePadChannelProViewSta(id,type);
			}else{//商品
				Endorsement endorsement = endorsementService.findEndorsementById(padChannelProView.getEid());
				if(endorsement!=null && endorsement.getEndorsementPrice()>0){
					channelService.updatePadChannelProViewSta(id,type);
				}else{
					rstInfo = "代言价格必须大于零";
				}
			}
			//根据view获取padChannelPro下现实的数量，如果=typcount,则显示
			Long cpid = padChannelProView.getCpid();
			PadChannelPro padChannelPro = channelService.findPadChannelProById(cpid);
			int showCount = channelService.findPadChannelProViewByCpid(cpid);
			if(showCount==padChannelPro.getTypCount()){
				logger.info("1*N图可展示，设置是否显示为显示状态");
				channelService.updatePadChannelProSta(cpid, "1");
			}else{
				logger.info("1*N图不可展示，设置是否显示为隐藏状态");
				channelService.updatePadChannelProSta(cpid, "0");
			}
			return ok(Json.toJson(rstInfo));
		}
		return ok(Json.toJson("不存在此商品"));
	}
    
    @AdminAuthenticated()
   	public Result deleteChannelProView(Long id){
   		channelService.deletePadChannelProViewById(id);
   		return ok(Json.toJson("ok"));
   	}
    /**
	 * 
	 * <p>Title: syncTemplate</p> 
	 * <p>Description: 同步模板操作</p> 
	 * @return
	 */
	public Result syncTemplate(){
		String adminId = AjaxHelper.getHttpParam(request(), "adminId");
		String userId = AjaxHelper.getHttpParam(request(), "userId");
		String resultStr = "同步操作正在进行，请耐心等待~";
		//判断模板是否为嗨购商城模板
		if(userId.equals(StringUtil.getConfigUid())){
			boolean flag = channelService.syncTemplate();
			if(flag){
				resultStr = "同步成功";
			}
			logger.info(Dates.formatDateTime(new Date())+"  ->用户:"+adminId+"：进行同步嗨购商城模板操作！");
		}else{
			resultStr = "同步的模板不匹配，停止同步操作";
		}
		return ok(Json.toJson(resultStr));
	}
	
    @AdminAuthenticated
    public Result syncHgTemplate(Long userid){
    	logger.info("同步更新快递员商户："+userid+"操作开始");
   		//channelService.syncHgTemplate(userid);
   		logger.info("同步更新快递员商户："+userid+"操作完成");
   		return ok(Json.toJson("ok"));
    }
    
    /**
     * 
     * <p>Title: refreshSyncInfo</p> 
     * <p>Description: 获取更新同步返回的进度</p> 
     * @return
     */
    public Result refreshSyncInfo(){
    	String refreshsyncinfo = (String) ServiceFactory.getCacheService().getObject("refreshsyncinfo");
    	if(refreshsyncinfo==null){
    		refreshsyncinfo = "";
    	}
    	if(refreshsyncinfo.indexOf("ok")>-1){
    		ServiceFactory.getCacheService().set("refreshsyncinfo", "");
    	}
    	return ok(Json.toJson(refreshsyncinfo));
    }
}
