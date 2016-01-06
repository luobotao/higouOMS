package controllers.parcels;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;

import controllers.admin.AdminAuthenticated;
import controllers.admin.BaseAdminController;
import forms.parcels.ParcelsForm;
import models.KdCompany;
import models.Parcels;
import models.ParcelsPro;
import models.ParcelsWaybill;
import models.ParcelsWaybillInfo;
import models.admin.AdminUser;
import models.product.Currency;
import models.product.Product;
import models.product.ProductGroup;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.twirl.api.Html;
import services.KdCompanyService;
import services.OperateLogService;
import services.admin.AdminUserService;
import services.admin.SmsService;
import services.order.OrderProductService;
import services.order.OrderService;
import services.parcels.ParcelsProService;
import services.parcels.ParcelsService;
import services.parcels.ParcelsWaybillInfoService;
import services.parcels.ParcelsWaybillService;
import services.product.CurrencyService;
import services.product.ProductGroupService;
import services.product.ProductService;
import utils.AjaxHelper;
import utils.Constants;
import utils.Htmls;
import utils.Numbers;
import utils.StringUtil;
import vo.ParcelsVO;

/**
 * 
 * <p>Title: ParcelsController.java</p> 
 * <p>Description: 包裹物流处理</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年7月31日  下午6:21:28
 * @version
 */
@Named
@Singleton
public class ParcelsController extends BaseAdminController {
	private static final Logger.ALogger logger = Logger.of(ParcelsController.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final SimpleDateFormat CHINESE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final ProductService productService;
    private final OrderService orderService;
    private final OrderProductService orderProductService;
    private final ParcelsService parcelsService;
    private final ParcelsProService parcelsProService;
    private final ParcelsWaybillService parcelsWaybillService;
    private final ParcelsWaybillInfoService parcelsWaybillInfoService;
    private final KdCompanyService kdCompanyService;
    private final SmsService smsService;
    private final CurrencyService currencyService;
    private final ProductGroupService productGroupService;
    private final OperateLogService operateLogService;
    private final AdminUserService adminUserService;
    
    private final Form<ParcelsForm> parcelsForm = Form.form(ParcelsForm.class);
    @Inject
    public ParcelsController(final ProductService productService,final OrderService orderService,
    		final OrderProductService orderProductService,final ParcelsService parcelsService,final ParcelsProService parcelsProService,
    		final ParcelsWaybillService parcelsWaybillService,final KdCompanyService kdCompanyService,final SmsService smsService,final CurrencyService currencyService,
    		final ParcelsWaybillInfoService parcelsWaybillInfoService,final ProductGroupService productGroupService,final OperateLogService operateLogService,final AdminUserService adminUserService) {
        this.productService = productService;
        this.orderService = orderService;
        this.orderProductService=orderProductService;
        this.parcelsService = parcelsService;
        this.parcelsProService = parcelsProService;
        this.parcelsWaybillService = parcelsWaybillService;
        this.kdCompanyService = kdCompanyService;
        this.smsService = smsService;
        this.currencyService = currencyService;
        this.parcelsWaybillInfoService = parcelsWaybillInfoService;
        this.productGroupService = productGroupService;
        this.operateLogService = operateLogService;
        this.adminUserService = adminUserService;
    }
	
    /**
     * 
     * <p>Title: orderManage</p> 
     * <p>Description: 获取订单信息</p> 
     * @return
     */
    @AdminAuthenticated()
	public Result parcelsManage(){
    	AdminUser adminUser =getCurrentAdminUser();
		Form<ParcelsForm> form = parcelsForm.bindFromRequest();
		ParcelsForm formPage = new ParcelsForm();
        if (!form.hasErrors()) {
        	formPage = form.get();
        }
        if(!Strings.isNullOrEmpty(formPage.keyword)){
        	formPage = dealFormWithKeyword(formPage);
        }
        if(!Strings.isNullOrEmpty(formPage.orderCode)){
        	formPage = dealFormWithOrderCode(formPage);
        }
        if("2".equals(adminUser.getAdminType())){//管理员类型 1普通用户 2外部用户 3商铺管理员
        	formPage.adminid = adminUser.getId();
		}
     	//根据现有条件获取订单总量
        Long totals = parcelsProService.getTotalsWithForm(formPage);
        //根据现有条件获取订单集合,分页结果
        List<ParcelsVO> parcelsVoList = parcelsProService.queryParcelsProIdList(formPage);
        List<AdminUser> adminUserList  = adminUserService.queryAllLyAdminUser();		//查找出所有的联营商户
        int totalPage = (int) (totals%formPage.size==0?totals/formPage.size:totals/formPage.size+1);
        String kdCompanys = dealCommonWithKdCompany2Html("");
		return ok(views.html.parcels.parcelsManage.render(adminUser,parcelsVoList,totals,formPage.page,totalPage,
				Html.apply(Constants.Srcs.srcs2HTML(Integer.parseInt(formPage.src))),
				Html.apply(Constants.WaybillStatus.waybill2HTML(formPage.status)),
				Html.apply(Constants.WaybillStatus.waybill2HTMLDxd(formPage.status)),
				Html.apply(Constants.WaybillStatus.waybill2HTMLZyOrHkd(formPage.status)),Html.apply(kdCompanys),
				Html.apply(AdminUserService.adminUserList2Html(adminUserList,formPage.adminId))));
	}
    /**
     * 联营包裹
     * luobotao
     * @return
     */
    @AdminAuthenticated()
    public Result unionParcelsManage(){
    	AdminUser adminUser =getCurrentAdminUser();
    	Form<ParcelsForm> form = parcelsForm.bindFromRequest();
    	ParcelsForm formPage = new ParcelsForm();
    	if (!form.hasErrors()) {
    		formPage = form.get();
    	}
    	if(!Strings.isNullOrEmpty(formPage.keyword)){
    		formPage = dealFormWithKeyword(formPage);
    	}
    	if(!Strings.isNullOrEmpty(formPage.orderCode)){
    		formPage = dealFormWithOrderCode(formPage);
    	}
    	if("2".equals(adminUser.getAdminType())){//管理员类型 1普通用户 2外部用户 3商铺管理员
    		formPage.adminid = adminUser.getId();
    	}
    	formPage.size=100;
    	//根据现有条件获取订单总量
    	Long totals = parcelsProService.getTotalsWithForm(formPage);
    	//根据现有条件获取订单集合,分页结果
    	List<ParcelsVO> parcelsVoList = parcelsProService.queryParcelsProIdList(formPage);
    	List<AdminUser> adminUserList  = adminUserService.queryAllLyAdminUser();		//查找出所有的联营商户
    	int totalPage = (int) (totals%formPage.size==0?totals/formPage.size:totals/formPage.size+1);
    	String kdCompanys = dealCommonWithKdCompany2Html("");
    	return ok(views.html.parcels.unionParcelsManage.render(adminUser,parcelsVoList,totals,formPage.page,totalPage,
    			Html.apply(Constants.Srcs.srcs2HTML(Integer.parseInt(formPage.src))),
    			Html.apply(Constants.WaybillStatus.waybill2HTML(formPage.status)),
    			Html.apply(Constants.WaybillStatus.waybill2HTMLDxd(formPage.status)),
    			Html.apply(Constants.WaybillStatus.waybill2HTMLZyOrHkd(formPage.status)),Html.apply(kdCompanys),
    			Html.apply(Constants.ParcelCheckStatus.checkStatus2HTML(formPage.checkStatus)),
    			Html.apply(AdminUserService.adminUserList2Html(adminUserList,formPage.adminId))));
    }

    /**
     * 批量进行申请结算
     * @return
     */
    @AdminAuthenticated()
    public Result applyCheck(){
    	AdminUser adminUser = getCurrentAdminUser();
    	String parIds = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "parIds");
    	JsonNode json = Json.parse(parIds);
    	for(JsonNode temp : json){
    		Parcels parcels = parcelsService.queryParcelsById(temp.asLong());
    		if(parcels!=null){
    			parcels.setCheckStatus(Constants.ParcelCheckStatus.Submited.getStatus());
    			parcels = parcelsService.saveParcels(parcels);
    			operateLogService.saveParcelsLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "包裹进行申请结算，包裹号："+parcels.getParcelCode());
    		}
    	}
    	return ok(Json.toJson("1"));
    }
    /**
     * 批量进行确认结算
     * @return
     */
    @AdminAuthenticated()
    public Result setChecked(){
    	AdminUser adminUser = getCurrentAdminUser();
    	String parIds = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "parIds");
    	JsonNode json = Json.parse(parIds);
    	for(JsonNode temp : json){
    		Parcels parcels = parcelsService.queryParcelsById(temp.asLong());
    		if(parcels!=null){
    			parcels.setCheckStatus(Constants.ParcelCheckStatus.Checked.getStatus());
    			parcelsService.saveParcels(parcels);
    			operateLogService.saveParcelsLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "包裹进行确认结算，包裹号："+parcels.getParcelCode());
    		}
    	}
    	return ok(Json.toJson("1"));
    }
	/**
	 * 导出包裹
	 * @return
	 */
	@AdminAuthenticated()
	public Result exportParcelsManage() {
		AdminUser adminUser = getCurrentAdminUser();
		Form<ParcelsForm> form = parcelsForm.bindFromRequest();
		ParcelsForm formPage = new ParcelsForm();
		if (!form.hasErrors()) {
			formPage = form.get();
		}
		if (!Strings.isNullOrEmpty(formPage.keyword)) {
			formPage = dealFormWithKeyword(formPage);
		}
		if (!Strings.isNullOrEmpty(formPage.orderCode)) {
			formPage = dealFormWithOrderCode(formPage);
		}
		if ("2".equals(adminUser.getAdminType())) {// 管理员类型 1普通用户 2外部用户 3商铺管理员
			formPage.adminid = adminUser.getId();
		}
		formPage.page=0;
		formPage.size=999999999;
		// 根据现有条件获取订单集合,分页结果
		List<ParcelsVO> parcelsVoList = parcelsProService.queryParcelsProIdList(formPage);
		File downFile = parcelsProService.exportFile(parcelsVoList);
		String fileNameChine = "包裹数据表"+CHINESE_DATE_FORMAT.format(new Date())+".xls";
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
	 * 导入运单信息（只针对外部商户管理员）
	 * @return
	 */
	@AdminAuthenticated()
	public Result importParcelsMail() {
		AdminUser adminUser = getCurrentAdminUser();
		String kdCompanyId = Form.form().bindFromRequest().get("kdCompany");
		KdCompany kdCompany = kdCompanyService.getkdCompanyById(Numbers.parseInt(kdCompanyId, 0));
		MultipartFormData body = request().body().asMultipartFormData();
        FilePart waybillFile = body.getFile("waybillFile");
        if (waybillFile != null) {
            String fileName = waybillFile.getFilename();
            if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                File file = waybillFile.getFile();
                boolean flag = parcelsProService.extractMailInfo(file, kdCompany);
				if (flag)
					return ok(Json.toJson("1"));
				else
					return ok(Json.toJson("0"));
            } else {
            	return ok(Json.toJson("0"));
            }
        } else {
        	return ok(Json.toJson("0"));
        }
	}
	/**
	 * 
	 * <p>Title: dealFormWithOrderCode</p> 
	 * <p>Description: 对查询条件orderCode进行处理</p> 
	 * @param formPage
	 * @return
	 */
    @AdminAuthenticated()
	private ParcelsForm dealFormWithOrderCode(ParcelsForm form) {
		//处理订单号和嘿客号
		 if(!Strings.isNullOrEmpty(form.orderCode)){
			 String src = form.src;
			 if("90".equals(src)||"-1".equals(src)){
				 form.sfcode = form.orderCode;
			 }
		 }
		 return form;
	}

	/**
	 * 
	 * <p>Title: dealFormWithKeyword</p> 
	 * <p>Description: 对查询条件keyword进行处理</p> 
	 * @param formPage
	 * @return
	 */
    @AdminAuthenticated()
	private ParcelsForm dealFormWithKeyword(ParcelsForm form) {
		 if (!Strings.isNullOrEmpty(form.keyword)) {
         	if(StringUtil.isMobileNO(form.keyword)){
         		form.phone=form.keyword;
         	}else if(StringUtil.isNumeric(form.keyword)){
         		form.pId=form.keyword;
         		form.newSku = form.keyword;
         	}else {
         		form.name=form.keyword;
         		form.title=form.keyword;
         		form.newSku = form.keyword;
         	}
         }
		 return form;
	}
	
	/**
	 * 
	 * <p>Title: viewParcels</p> 
	 * <p>Description: 查看包裹详情</p> 
	 * @return
	 */
    @AdminAuthenticated()
	public Result viewParcels(){
		String parId = AjaxHelper.getHttpParam(request(), "parId");
		Parcels parcels  = new Parcels ();
		List<ParcelsPro> parcelsPros = new ArrayList<ParcelsPro>();
		List<ProductGroup> productGroups = new ArrayList<ProductGroup>();
		if(!Strings.isNullOrEmpty(parId)){
			parcels = parcelsService.queryParcelsById(Long.parseLong(parId));
			if(parcels!=null&&!Strings.isNullOrEmpty(parcels.getCurrency())){
				Currency currency = currencyService.queryCurrencyById(parcels.getCurrency());
				parcels.setCurrencyName(currency.getName());
			}
			parcelsPros = parcelsProService.getAllByParcelsId(Long.parseLong(parId));
			if(parcelsPros!=null&&parcelsPros.size()>0){
				for (ParcelsPro parcelsPro : parcelsPros) {
					Product product = productService.findProductById(parcelsPro.getpId());
					product.setFromsiteName(productService.queryFromsiteById(product.getFromsite()).getName());
					parcelsPro.setProduct(product);
					if(product.getIshot()==1){
						List<ProductGroup> productGroup = productGroupService.findProductGroupListByPgId(product.getPid());
						for (ProductGroup productGroup2 : productGroup) {
							productGroup2.setProduct(productService.findProductById(productGroup2.getPid()));
							productGroup2.setBuyNum(parcelsPro.getCounts()*productGroup2.getNum());
							productGroups.add(productGroup2);
						}
					}
				}
			}
		}
		return ok(views.html.parcels.viewParcels.render(parcels,parcelsPros,productGroups,parId));
	}
	
	/**
	 * 
	 * <p>Title: backOrder</p> 
	 * <p>Description: 退回成订单</p> 
	 * @return
	 */
    @AdminAuthenticated()
	public Result backOrder(){
		String parId = AjaxHelper.getHttpParam(request(), "parId");
		String ppId = AjaxHelper.getHttpParam(request(), "ppId");
		Parcels parcels = parcelsService.queryParcelsById(Long.parseLong(parId));
		AdminUser adminUser = getCurrentAdminUser();
		if(parcels!=null){
			//获取同一包裹下的所有商品
			List<ParcelsPro> pps = parcelsProService.getAllByParcelsId(Long.parseLong(parId));
			//更改订单的处理状态为未处理
			if(!Strings.isNullOrEmpty(ppId)){
				for (ParcelsPro pp : pps) {
					orderProductService.backOrder(pp.getShopProId());
        			operateLogService.saveOrderLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "包裹退回成订单，包裹号："+parcels.getParcelCode()+",订单商品ID："+pp.getShopProId());
					//删除相应的包裹商品信息
					parcelsProService.del(pp);
					operateLogService.saveParcelsLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "包裹退回成订单，清除包裹商品ID："+pp.getId());
				}
			}
			//删除相应的包裹信息
			parcelsService.del(parcels);
			operateLogService.saveParcelsLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "包裹退回成订单，清除包裹ID："+parcels.getId());
		}
		return redirect("/parcels/parcelsManage");
	}
	/**
	 * 
	 * <p>Title: waybillManage</p> 
	 * <p>Description: 查看包裹的物流信息</p> 
	 * @return
	 */
    @AdminAuthenticated()
	public Result waybillManage(){
		String parId = AjaxHelper.getHttpParam(request(), "parId");
		String kdCompanys = dealCommonWithKdCompany2Html("");
		Parcels parcels = parcelsService.queryParcelsById(Long.parseLong(parId));
		String currentDate = CHINESE_DATE_TIME_FORMAT.format(new Date());
		List<ParcelsWaybill> pwbs = parcelsWaybillService.getAllWaybillInfoWithParcelsId(parcels.getId());
		return ok(views.html.parcels.waybillManage.render(parcels,pwbs,currentDate,Html.apply(kdCompanys)));
	}
	
	/**
	 * 
	 * <p>Title: dealCommonWithAccount2Html</p> 
	 * <p>Description: 处理下单账号信息，用于前段选择，格式为id : name</p> 
	 * @param value
	 * @return
	 */
    @AdminAuthenticated()
	public String dealCommonWithKdCompany2Html(String value) {
		List<KdCompany> kdCompanys = kdCompanyService.findAll();
		StringBuilder sb = new StringBuilder();
        for (KdCompany m : kdCompanys) {
            if (value!=null&&value.equals(m.getId()+"")) {
                sb.append(Htmls.generateSelectedOption(m.getId(),
                        m.getCnName()));
            } else {
                sb.append(Htmls.generateOption(m.getId(), m.getCnName()));
            }
        }
        return sb.toString();
	}
	
	/**
	 * 
	 * <p>Title: changeParcelsStatus</p> 
	 * <p>Description: 更改包裹的物流状态</p> 
	 * @return
	 */
    @AdminAuthenticated()
	public Result changeParcelsStatus(){
		String status = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "status");
		String parId = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "parId");
		Parcels parcels = parcelsService.queryParcelsById(Long.parseLong(parId));
		int statusTemp = parcels.getStatus();
		if(parcels!=null){
			//更新物流状态
			parcels.setStatus(Integer.parseInt(status));	
			Parcels tempParcels = parcelsService.saveParcels(parcels);
			String args = "";
			String tpl_id = "";
			String fromsite = "";
			//如果已送达，则需要反查订单，看所有商品是否均已送达且不包含未处理，满足条件更改订单的状态为3，已完成
			if("5".equals(status)||"12".equals(status)){
				orderService.dealOrderWithStatus(parcels);
			}else{
				//生产一条新的物流记录
				switch(Integer.parseInt(status)){
					case 2:
						args = "#code#="+parcels.getOrderCode();
						tpl_id = "786697";
						break;
					case 3:
						args = "#code#="+parcels.getOrderCode();
						tpl_id = "786679";
						break;
					case 4:
						args = "#address#="+parcels.getOrderId()+"&#code#="+parcels.getOrderCode();
						tpl_id = "776397";
						break;
					case 11:
						fromsite = orderService.getFromsietWithOrderCode(parcels.getOrderId());
						args = "#address#="+fromsite+"&#code#="+parcels.getOrderCode();
						tpl_id = "776397";
						break;
					default:
						;
				}
			}
			AdminUser adminUser = getCurrentAdminUser();
			operateLogService.saveParcelsLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "更新包裹"+parcels.getParcelCode()+"物流状态，物流状态由："+statusTemp+"更新为："+tempParcels.getStatus());
			if(!StringUtils.isBlank(args)&&!StringUtils.isBlank(tpl_id)){
				//获取到包裹对应的物流信息
				smsService.saveSmsInfo(args, parcels.getPhone(), tpl_id, "1");
			}
		}
		return ok(Json.toJson(""));
	}

	/**
	 * 
	 * <p>Title: saveWayBill</p> 
	 * <p>Description: 添加物流运单</p> 
	 * @return
	 */
    @AdminAuthenticated()
	public Result saveWayBill(){
		Form<ParcelsForm> form = parcelsForm.bindFromRequest();
		ParcelsForm formPage = new ParcelsForm();
        if (!form.hasErrors()) {
        	formPage = form.get();
        }
        //添加物流信息，pardels_Waybill
        ParcelsWaybill pwb = parcelsWaybillService.saveWithWaybillForm(formPage);
        if(!Strings.isNullOrEmpty(formPage.waybillCode)){
        	Parcels parcels = parcelsService.queryParcelsById(formPage.parId);
        	parcels.setMailnum(formPage.waybillCode);//将包裹里的运单号写入
        	parcelsService.saveParcels(parcels);
        }
        AdminUser adminUser = getCurrentAdminUser();
		operateLogService.saveWaybillLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "新增物流运单，物流运单ID:"+pwb.getId());
		return redirect("/parcels/waybillManage?parId="+formPage.parId);
	}
	
	/**
	 * 
	 * <p>Title: delWaybill</p> 
	 * <p>Description: 删除相应的物流运单</p> 
	 * @return
	 */
    @AdminAuthenticated()
	public Result delWaybill(){
		String wbId = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "wbId");
		if(!StringUtils.isBlank(wbId)){
			parcelsWaybillService.delWayBill(Long.parseLong(wbId));
			AdminUser adminUser = getCurrentAdminUser();
			operateLogService.saveWaybillLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "删除物流运单，物流运单ID:"+wbId);
			return ok(Json.toJson("ok"));
		}else{
			return ok(Json.toJson("fail"));
		}
	}
	/**
	 * 
	 * <p>Title: changeNsort</p> 
	 * <p>Description: 更改运单信息的排序</p> 
	 * @return
	 */
    @AdminAuthenticated()
	public Result changeNsort(){
		String wbId = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "wbId");
		String nsort = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "nsort");
		if(!StringUtils.isBlank(wbId)&&!StringUtils.isBlank(nsort)){
			parcelsWaybillService.updateWayBill(Long.parseLong(wbId),Integer.parseInt(nsort));
			return ok(Json.toJson("ok"));
		}else{
			return ok(Json.toJson("fail"));
		}
	}
	/**
	 * 
	 * <p>Title: getWayBillInfo</p> 
	 * <p>Description: 获得指定waybill下的info信息集合</p> 
	 * @return
	 */
    @AdminAuthenticated()
	public Result getWayBillInfo(){
		String wayBillId = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "wayBillId");
		if(!StringUtils.isBlank(wayBillId)){
			List<ParcelsWaybillInfo> parcelsWaybillInfos = parcelsWaybillInfoService.getAllByWayBillId(Long.parseLong(wayBillId));
			return ok(Json.toJson(parcelsWaybillInfos));
		}else{
			return ok(Json.toJson("fail"));
		}
	}
	
}
