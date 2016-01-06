package controllers.kjt;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.AutoSyncTask;
import models.AutoSyncTaskLog;
import models.Parcels;
import models.ShoppingOrder;
import models.admin.AdminUser;
import models.product.Product;
import play.Logger;
import play.Logger.ALogger;
import play.libs.Json;
import play.mvc.Result;
import services.AutoSyncTaskService;
import services.OperateLogService;
import services.admin.AdminUserService;
import services.kjt.KjtService;
import services.parcels.ParcelsService;
import services.product.ProductService;
import utils.AjaxHelper;
import utils.Dates;
import utils.Numbers;
import utils.StringUtil;
import utils.kjt.KjtApiProtocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.ibm.icu.text.SimpleDateFormat;

import controllers.admin.BaseAdminController;

/**
 * 
 * <p>Title: KJTApiController.java</p> 
 * <p>Description:跨境通API 工具类 </p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年9月17日  上午11:32:08
 * @version
 */
@Named
@Singleton
public class KJTApiController extends BaseAdminController{
	private static final ALogger logger = Logger.of(KJTApiController.class);
	public final static String APPID = "seller325";
	public final static String APPSECRET = "kjt@325";
	public final static Integer DEFLIMITROWS = 5;
	
	private final OperateLogService operateLogService;
	private final KjtService kjtService;
	private final AutoSyncTaskService autoSyncTaskService;
	private final ParcelsService parcelsService;
	private final ProductService productService;
	private final AdminUserService adminUserService;
	
	
	@Inject
	public KJTApiController(final OperateLogService operateLogService,final KjtService kjtService,final AutoSyncTaskService autoSyncTaskService,final ParcelsService parcelsService,final ProductService productService,final AdminUserService adminUserService){
		this.operateLogService = operateLogService;
		this.kjtService = kjtService;
		this.autoSyncTaskService = autoSyncTaskService;
		this.parcelsService = parcelsService;
		this.productService = productService;
		this.adminUserService = adminUserService;
	}
	
	/**
	 * 
	 * <p>Title: productIdGetQuery</p> 
	 * <p>Description: /**
		时间段内信息变化的商品ID列表获取
	 * 1.4.2
	 * @param 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public Result doJobWithProductIdGetQuery() throws UnsupportedEncodingException  {
		response().setContentType("application/json;charset=utf-8");
		ObjectNode result = Json.newObject();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		AutoSyncTask autoSyncTask = autoSyncTaskService.getTaskWithCond("kjt", "productIdGetQuery");
		String changedDateBegin=sdf.format(autoSyncTask.getLasttime());
		Date currentDate = new Date();
		String changedDateEnd=sdf.format(currentDate);
		int limitRows=Numbers.parseInt(AjaxHelper.getHttpParam(request(), "limitRows"),DEFLIMITROWS);
		if(limitRows == 0){
			limitRows = 5;
		}
		int startRow=Numbers.parseInt(AjaxHelper.getHttpParam(request(), "startRow"),1);
		//更新auto_sync_task记录表
		autoSyncTask.setLasttime(currentDate);
		autoSyncTaskService.save(autoSyncTask);
		List<String> productIds = kjtService.productIdGetQuery(changedDateBegin,changedDateEnd,limitRows,startRow);
		logger.info("[kjt]doJobWithProductIdGetQuery :"+StringUtil.getStringFromList(productIds));
		if(productIds!=null&&productIds.size()>0){
			List<String> strs = new ArrayList<String>();
			for (int i = 0 ; i < productIds.size(); i++) {
				strs.add(productIds.get(i));
				if(i==productIds.size()-1){
					result = doProductInfoBatchGet(strs.toArray(), result);
					break;
				}
				//控制每次最多传5个以下的个数
				if((i+1)%limitRows==0){
					//每limitRows个数就获取一次
					result = doProductInfoBatchGet(strs.toArray(), result);
					strs = new ArrayList<String>();
				}
			}
			logger.info("[kjt]productIdGetQuery success");
		}else{
			logger.info("[kjt]productIdGetQuery null");
		}
		return ok("ok");
	}
	
	/**
	 * 
	 * <p>Title: productInfoBatchGet</p> 
	 * <p>Description: /**
	 * 根据商品ID批量获取商品详细信息
	 * 1.4.3
	 * @param productIds
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public Result productInfoBatchGet() throws UnsupportedEncodingException  {
		response().setContentType("application/json;charset=utf-8");
		ObjectNode result = Json.newObject();
		String productIdsStr=AjaxHelper.getHttpParam(request(), "productIds");
		logger.info("[kjt] productInfoBatchGet request ids:"+productIdsStr);
		if(!Strings.isNullOrEmpty(productIdsStr)){
			List<String> productIds = Arrays.asList(productIdsStr.split(","));
			List<String> strs = new ArrayList<String>();
			for (int i = 0 ; i < productIds.size(); i++) {
				strs.add(productIds.get(i));
				if(i==productIds.size()-1){
					result = doProductInfoBatchGet(strs.toArray(), result);
					break;
				}
				//控制每次最多传5个以下的个数
				if((i+1)%DEFLIMITROWS==0){
					//每limitRows个数就获取一次
					result = doProductInfoBatchGet(strs.toArray(), result);
					strs = new ArrayList<String>();
				}
			}
		}
		else{
			logger.info("[kjt]productInfoBatchGet failed");
		}
        return ok("ok");
	}
	
	/**
	 * 
	 * <p>Title: productPriceChangeIDQuery</p> 
	 * <p>Description: /**
		时间段内价格变化的商品ID列表获取
	 * 1.4.4
	 * @param 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public Result doJobWithProductPriceChangeIDQuery() throws UnsupportedEncodingException  {
		response().setContentType("application/json;charset=utf-8");
		ObjectNode result = Json.newObject();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		AutoSyncTask autoSyncTask = autoSyncTaskService.getTaskWithCond("kjt", "productPriceChangeIDQuery");
		String changedDateBegin=sdf.format(autoSyncTask.getLasttime());
		Date currentDate = new Date();
		String changedDateEnd=sdf.format(currentDate);
		int limitRows=Numbers.parseInt(AjaxHelper.getHttpParam(request(), "limitRows"),5);
		if(limitRows == 0){
			limitRows = 5;
		}
		int startRow=Numbers.parseInt(AjaxHelper.getHttpParam(request(), "startRow"),1);
		//更新auto_sync_task记录表
		autoSyncTask.setLasttime(currentDate);
		autoSyncTaskService.save(autoSyncTask);
		boolean flag = kjtService.productPriceChangeIDQuery(changedDateBegin,changedDateEnd,limitRows,startRow);
		if(flag){
			result.put("status", "productPriceChangeIDQuery success");
		}else{
			result.put("status", "productPriceChangeIDQuery null");
		}
		return ok(Json.toJson(result));
	}
	
	/**
	 * 
	 * <p>Title: productPriceBatchGet</p> 
	 * <p>Description: /**
	 * 根据商品ID批量获取商品价格信息
	 * 1.4.5
	 * @param productIds
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public Result productPriceBatchGet() throws UnsupportedEncodingException  {
		response().setContentType("application/json;charset=utf-8");
		ObjectNode result = Json.newObject();
		String productIds=AjaxHelper.getHttpParam(request(), "productIds");
		if(!Strings.isNullOrEmpty(productIds)){
			boolean flag = kjtService.productPriceBatchGet(productIds.split(","));
			if(flag){
				result.put("status", "productInfoBatchGet success");
			}else{
				result.put("status", "productInfoBatchGet failed");
			}
		}else{
			result.put("status", "failed get productIds");
		}
        return ok(Json.toJson(result));
	}
	/**
	 * 
	 * <p>Title: doProductInfoBatchGet</p> 
	 * <p>Description: 批量获取商品信息操作</p> 
	 * @param result
	 * @return
	 */
	public ObjectNode doProductInfoBatchGet(Object[] productIds, ObjectNode result){
		boolean flag = kjtService.productInfoBatchGet(productIds);
		if(flag){
			result.put("status", "productInfoBatchGet success");
		}else{
			result.put("status", "productInfoBatchGet failed");
		}
		return result;
	}
	
	/**
	 * 
	 * <p>Title: channelq4sbatchget</p> 
	 * <p>Description: 商品分销渠道库存批量获取</p> 
	 * 1.5.3
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public Result channelQ4SBatchGet() throws UnsupportedEncodingException  {
		response().setContentType("application/json;charset=utf-8");
		ObjectNode result = Json.newObject();
		String productIds=AjaxHelper.getHttpParam(request(), "productIds");
		if(!Strings.isNullOrEmpty(productIds)){
			boolean flag = kjtService.channelQ4SBatchGet(productIds);
			if(flag){
				result.put("status", "channelq4sbatchget success");
			}else{
				result.put("status", "channelq4sbatchget failed");
			}
		}else{
			result.put("status", "channelq4sbatchget, failed get orderIds");
		}
		return ok(Json.toJson(result));
	}
	
	/**
	 * 
	 * <p>Title: channelq4sadjustrequest</p> 
	 * <p>Description: kjt通知分销渠道库存调整，商户提供api</p> 
	 * 1.5.4
	 * @return
	 */
	public Result channelQ4SAdjustRequest() {
		response().setContentType("application/json;charset=utf-8");
		String data = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "data");
		String method = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "method");
		String version = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "version");
		String appid = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "appid");
		String format = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "format");
		String timestamp = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "timestamp");
		String nonce = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "nonce");
		String secretkey = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "secretkey");
		String sign = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "sign");
		HashMap<String, String> params = new HashMap<String,String>();
		params.put("data", data);
		params.put("method", method);
		params.put("version", version);
		params.put("appid", appid);
		params.put("format", format);
		params.put("timestamp", timestamp);
		params.put("nonce", nonce);
		params.put("secretkey", secretkey);
		String mySign = KjtApiProtocol.sign(APPSECRET, params);
		logger.info("[kjt]soOutputCustoms request data:"+data);
		if(Strings.isNullOrEmpty(sign)||!mySign.equals(sign)){
			return ok("FAILURE");
		}
		JsonNode jsonData = Json.parse(data);
		String sysNo = jsonData.get("SysNo").asText();
		String saleChannelSysNo = jsonData.get("SalechannelSysNo").asText();
		JsonNode resultJson = jsonData.get("Items");
		logger.info("channelQ4SAdjustRequest items:"+resultJson);
		if(resultJson!=null){
			AdminUser adminUser = adminUserService.findByUsername("kjt");
			for(int i = 0; i < resultJson.size(); i++){
				String productId = resultJson.get(i).get("ProductID").asText();
				long qty = resultJson.get(i).get("Qty").asLong();
				Product product = productService.findProductByExtcode(productId);
				long nstock = product.getNstock();
				long tempNstock = nstock+qty;
				if(tempNstock<0){
					tempNstock = 0;
				}
				product.setNstock(tempNstock);
				productService.saveProduct(product);
				operateLogService.saveProductNstockLog(adminUser.getId(), adminUser.getUsername(), "127.0.0.1", product.getPid(), nstock,tempNstock);
				logger.info("跨境通分销渠道库存调整，商品ID："+resultJson.get(i).get("ProductID").asText()+"，库存增减数量："+resultJson.get(i).get("Qty").asDouble()+"，出库仓仓库编号："+resultJson.get(i).get("WareHouseID").asDouble());
			}
		}
		logger.info(sysNo+":"+saleChannelSysNo);
		return ok("SUCCESS");
	}

	/**
	 * 
	 * <p>Title: soCreate</p> 
	 * <p>Description: /**
		分销渠道订单创建接收接口
	 * 1.6.1
	 * @param 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public Result doJobWithSoCreate() throws UnsupportedEncodingException  {
		response().setContentType("application/json;charset=utf-8");
		ObjectNode result = Json.newObject();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		AutoSyncTask autoSyncTask = autoSyncTaskService.getTaskWithCond("kjt", "soCreate");
		AdminUser adminUser = adminUserService.findByUsername("kjt");
		List<Parcels> parcels = parcelsService.findParcelsWithAdminAndDateAdd(adminUser.getId(),autoSyncTask.getLasttime());
		String changedDateBegin=sdf.format(autoSyncTask.getLasttime());
		Date currentDate = new Date();
		//更新auto_sync_task记录表
		autoSyncTask.setLasttime(currentDate);
		autoSyncTaskService.save(autoSyncTask);
		//查询指定时间段后生成的包裹信息
		for (Parcels parcel : parcels) {
			JsonNode jsonNode = kjtService.soCreate(parcel);
		}
		return ok(Json.toJson(result));
	}
	
	/**
	 * 
	 * <p>Title: doJobWithSoCreateErr</p> 
	 * <p>Description: 定时执行将前一天失败的包裹同步到kjt</p> 
	 * 1.6.1 补充
	 * @return
	 */
	public Result doJobWithSoCreateErr(){
		kjtService.doJobWithSoCreateErr();
		return ok("doJobWithSoCreateErr ok");
	}
	
	
	/**
	 * kjt通知分销渠道订单已出关区，商户提供的api
	 * 1.6.2
	 * @param id
	 * @return
	 */
	public Result soOutputCustoms() {
		response().setContentType("application/json;charset=utf-8");
		String data = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "data");
		String method = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "method");
		String version = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "version");
		String appid = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "appid");
		String format = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "format");
		String timestamp = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "timestamp");
		String nonce = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "nonce");
		String secretkey = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "secretkey");
		String sign = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "sign");
		HashMap<String, String> params = new HashMap<String,String>();
		params.put("data", data);
		params.put("method", method);
		params.put("version", version);
		params.put("appid", appid);
		params.put("format", format);
		params.put("timestamp", timestamp);
		params.put("nonce", nonce);
		params.put("secretkey", secretkey);
		String mySign = KjtApiProtocol.sign(APPSECRET, params);
		logger.info("[kjt]soOutputCustoms request data:"+data);
		if(Strings.isNullOrEmpty(sign)||!mySign.equals(sign)){
			return ok("FAILURE");
		}
		JsonNode jsonData = Json.parse(data);
		String merchantOrderID = jsonData.get("MerchantOrderID").asText();
		String status = jsonData.get("Status").asText();
		String shipTypeID = jsonData.get("ShipTypeID").asText();
		String trackingNumber = jsonData.get("TrackingNumber").asText();
		String commitTime = jsonData.get("CommitTime").asText();
		String message = jsonData.get("Message").asText();
		logger.info(merchantOrderID+":"+status+":"+shipTypeID+":"+trackingNumber+":"+commitTime+":"+message);
		//保存订单出关相关物流信息
		boolean flag = kjtService.soOutputCustoms(merchantOrderID,status,shipTypeID,trackingNumber,commitTime,message);
		if(flag){
		}
		return ok("SUCCESS");
	}
	
	/**
	 * 
	 * <p>Title: soTrace</p> 
	 * <p>Description: /**
		订单状态追踪
	 * 1.6.3
	 * @param 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public Result doJobWithSoTrace() throws UnsupportedEncodingException  {
		response().setContentType("application/json;charset=utf-8");
		ObjectNode result = Json.newObject();
		AdminUser adminUser = adminUserService.findByUsername("kjt");
		//获取到跨境通用户所有未发货的包裹集合，查询包裹状态
		List<Parcels> orderIds = parcelsService.findOrderIdsWithDfh(adminUser.getId());
		if(orderIds!=null&&orderIds.size()>0){
			boolean flag = kjtService.soTrace(orderIds);
			if(flag){
				result.put("status", "soTrace success");
			}else{
				result.put("status", "soTrace failed");
			}
		}else{
			result.put("status", "soTrace, failed get orderIds");
		}
        return ok(Json.toJson(result));
	}
	
	/**
	 * 
	 * <p>Title: soVoid</p> 
	 * <p>Description: /**
		分销渠道订单作废
	 * 1.6.4
	 * @param 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public Result soVoid() throws UnsupportedEncodingException  {
		response().setContentType("application/json;charset=utf-8");
		ObjectNode result = Json.newObject();
		String orderIds=AjaxHelper.getHttpParam(request(), "orderIds");
		if(!Strings.isNullOrEmpty(orderIds)){
			boolean flag = kjtService.soVoid(orderIds.split(","));
			if(flag){
				result.put("status", "soVoid success");
			}else{
				result.put("status", "soVoid failed");
			}
		}else{
			result.put("status", "soVoid, failed get orderIds");
		}
		return ok(Json.toJson(result));
	}
	
	/**
	 * 
	 * <p>Title: fepBillPost</p> 
	 * <p>Description: 待购汇账单接收</p>
	 *  1.7.1
	 * @return
	 */
	public Result doJobWithFepBillPost() throws UnsupportedEncodingException  {
		response().setContentType("application/json;charset=utf-8");
		ObjectNode result = Json.newObject();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		AutoSyncTask autoSyncTask = autoSyncTaskService.getTaskWithCond("kjt", "fepBillPost");
		Date changedDate=autoSyncTask.getLasttime();
		Date currentDate = new Date();
		//更新auto_sync_task记录表
		autoSyncTask.setLasttime(currentDate);
		autoSyncTaskService.save(autoSyncTask);
		//获得到满足条件的订单列表，保存订单编号集合
		List<Parcels> orderIds = parcelsService.findOrderIdsWithChangedDate(changedDate);
		if(orderIds!=null&&orderIds.size()>0){
			boolean flag = kjtService.fepBillPost(orderIds);
			if(flag){
				result.put("status", "fepBillPost success");
			}else{
				result.put("status", "fepBillPost failed");
			}
		}
		return ok(Json.toJson(result));
	}
	
	
	
}
