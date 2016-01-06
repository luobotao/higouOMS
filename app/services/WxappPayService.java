package services;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.RefundInfo;
import models.ShoppingOrder;

import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import play.Configuration;
import play.Logger;
import play.libs.Json;
import services.refund.RefundInfoService;
import utils.Constants;
import utils.HttpClientUtil;
import utils.Numbers;
import utils.WSUtils;
import utils.wxapppay.ClientCustomSSL;
import utils.wxapppay.ClientResponseHandler;
import utils.wxapppay.MD5Util;
import utils.wxapppay.RequestHandler;
import utils.wxapppay.Sha1Util;
import utils.wxapppay.TenpayHttpClient;
import utils.wxwebpay.WeixinUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;

import forms.order.RefundForm;


/**
 * 微信支付Service
 * @author luobotao
 *
 */
@Named
@Singleton
public class WxappPayService extends Thread{
    private static final Logger.ALogger logger = Logger.of(WxappPayService.class);
	@Inject
    private RefundInfoService refundInfoService;
    @Inject
    private OperateLogService operateLogService;
	//收款方
	private static final String spname = "微信支付";                                           

	//商户号
	private static final String partner = "1234290501";

	//密钥
	private static final String partner_key = "97d12b655c585625710e31309d2ee3c4";

	//appid
	private static final String app_id="wx73bdf02facab9ca2";

	private static final String app_secret = "f17f13bbac8893946a267566c24628fb";

	//appkey
	private static final String app_key="CBTFJjMoeCoIweHgZfmuPg4nCwVrYy5QjqEWGvgghVvj44iDy9eToInNIi4DizrUYvMYSUkQnGvqqck9xKqAd1BrRPEKZeFo6fWDkQbJ93xniBbrGbkH2EC1VpGRVNec";
//	private static final String spname = "微信支付";                                           
//	
//	//商户号
//	private static final String partner = "1225432101";
//	
//	//密钥
//	private static final String partner_key = "4959df8260f776b0d427bd88b5806863";
//	
//	//appid
//	private static final String app_id="wx82db1655019828db";
//	
//	private static final String app_secret = "672d7ce5183deb6cea63c7cb512bc13d";
//	
//	//appkey
//	private static final String app_key="Nbm5sk2ago8peudS19tgXc7O9tQ9dgxhCBLv1iznEIIk1ObBIPTTcvm3ilLvZgAacwfZ3x7j2D85kxsAsSRsWLuT5iFCBNCJIHSm0uWPabUyk9wjJlaVlEuVrz1Bngle";

	
	private static WxappPayService instance = new WxappPayService();
    private Executor executor = Executors.newSingleThreadExecutor();
    private LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
	 /* 私有构造方法，防止被实例化 */
	private WxappPayService(){
		this.start();
	}
	public void run(){
		logger.info("start WxappPayService service ");
		Runnable r;
		try {
			while((r = tasks.take()) != null){
				executor.execute(r);
			}
		} catch (InterruptedException e) {
			logger.error("InterruptedException in WxappPayService service",e);
		}
	}
	public static WxappPayService getInstance(){
		return instance;
	}

	/**
	 * 
	 * <p>Title: refundAppPay</p> 
	 * <p>Description: 微信app支付退款</p> 
	 * @param result 
	 * @param shoppingOrder
	 * @param refundFee
	 * @param formPage
	 * @return
	 * @throws Exception 
	 */
	public ObjectNode refundAppPay(ObjectNode result, ShoppingOrder shoppingOrder, double refundFee,
			RefundForm formPage) throws Exception {
		 //创建查询请求对象
	    RequestHandler reqHandler = new RequestHandler(null, null);
	    //通信对象
	    TenpayHttpClient httpClient = new TenpayHttpClient();
	    //应答对象
	    ClientResponseHandler resHandler = new ClientResponseHandler();
	  //-----------------------------
	    //设置请求参数
	    //-----------------------------
	    reqHandler.init();
	    reqHandler.setKey(partner_key);
	    reqHandler.setGateUrl("https://mch.tenpay.com/refundapi/gateway/refund.xml");
	    
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		//位置退款信息组装
		reqHandler.setParameter("service_version", "1.1");					//接口版本
		reqHandler.setParameter("partner",Constants.wxapp_partner);					//商户号
		reqHandler.setParameter("out_trade_no",shoppingOrder.getOrderCode());			//商户订单号
		reqHandler.setParameter("transaction_id", shoppingOrder.getTradeno());	//微信订单号
		reqHandler.setParameter("out_refund_no", sdf.format(new Date())+shoppingOrder.getOrderCode());		//商户退款单号
		reqHandler.setParameter("total_fee", String.valueOf(Numbers.parseInt(String.valueOf(100*shoppingOrder.getTotalFee()), 0)));		//总金额
		reqHandler.setParameter("refund_fee", String.valueOf(Numbers.parseInt(String.valueOf(100*refundFee), 0)));			//退款金额
		reqHandler.setParameter("op_user_id", Constants.wxapp_partner);	//操作员
		reqHandler.setParameter("op_user_passwd", MD5Util.MD5Encode(Constants.CFTPASSWD, "GBK"));	//操作员
		//-----------------------------
	    //设置通信参数
	    //-----------------------------
	    //设置请求返回的等待时间
		//设置请求返回的等待时间
	    httpClient.setTimeOut(5);	
	    String caInfoPath = Configuration.root().getString("wxapppay.ca.path", "conf/cacert.pem");
	    String cerInfoPath = Configuration.root().getString("wxapppay.cer.path", "conf/1234290501_20150317144746.pfx");
	    //设置ca证书
	    httpClient.setCaInfo(new File(caInfoPath));
	    //设置个人(商户)证书
	    httpClient.setCertInfo(new File(cerInfoPath), Constants.waapp_passwd);
	    //设置发送类型POST
	    httpClient.setMethod("POST");     
	    //设置请求内容
	    String requestUrl = reqHandler.getRequestURL();
	    logger.info("[wxapp refund]requestUrl："+requestUrl);
	    httpClient.setReqContent(requestUrl);
	    String rescontent = "null";
	    //后台调用
	    if(httpClient.call()) {
	    	//设置结果参数
	    	rescontent = httpClient.getResContent();
	    	resHandler.setContent(rescontent);
	    	resHandler.setKey(partner_key);
	    	//获取返回参数
	    	String retcode = resHandler.getParameter("retcode");
	    	//判断签名及结果
	    	if(resHandler.isTenpaySign()&& "0".equals(retcode)) {
		    	/*退款状态	refund_status	
					4，10：退款成功。
					3，5，6：退款失败。
					8，9，11:退款处理中。
					1，2: 未确定，需要商户原退款单号重新发起。
					7：转入代发，退款到银行发现用户的卡作废或者冻结了，导致原路退款银行卡失败，资金回流到商户的现金帐号，需要商户人工干预，通过线下或者财付通转账的方式进行退款。
					*/
		    	String refund_status=resHandler.getParameter("refund_status");
		    	String out_trade_no=resHandler.getParameter("out_trade_no");
		    	String out_refund_no=resHandler.getParameter("out_refund_no");
		    	String refund_id=resHandler.getParameter("refund_id");			//微信退款单号
		    	String refund_channel=resHandler.getParameter("refund_channel");//退款渠道
		    	String refund_fee=resHandler.getParameter("refund_fee");		//现金退款金额
				//变更业务信息
				logger.info("微信退款申请已提交，微信退款单号："+refund_channel+",现金退款金额"+refund_fee);
				//更新refundInfo ,退款信息记录
				result = dealRefundInfoWithWxapp(result,formPage,out_refund_no,refund_status,refund_fee,refund_id,refund_channel);
				
	    	} else {
	    		result.put("status", "[wxapp refund errcode]"+retcode);
	        	result.put("msg", "[wxapp refund errInfo]"+resHandler.getParameter("retmsg"));
	    		//错误时，返回结果未签名，记录retcode、retmsg看失败详情。
	    		logger.info("[wxapp refund]验证签名失败或业务错误");
	    		logger.info("[wxapp refund]retcode:" + resHandler.getParameter("retcode")+
	    	    	                    " retmsg:" + resHandler.getParameter("retmsg"));
	    	}	
	    } else {
	    	result.put("status", "0");
        	result.put("msg", "[wxapp refund]后台调用通信失败！"+httpClient.getErrInfo());
	    	logger.info("后台调用通信失败");   	
	    	logger.info("[wxapp refund]"+httpClient.getResponseCode()+"");
	    	logger.info("[wxapp refund]"+httpClient.getErrInfo());
	    	//有可能因为网络原因，请求已经处理，但未收到应答。
	    }
	    //获取debug信息,建议把请求、应答内容、debug信息，通信返回码写入日志，方便定位问题
	    logger.info("[wxapp refund]http res:" + httpClient.getResponseCode() + "," + httpClient.getErrInfo());
	    logger.info("[wxapp refund]req url:" + requestUrl);
	    logger.info("[wxapp refund]req debug:" + reqHandler.getDebugInfo());
	    logger.info("[wxapp refund]res content:" + rescontent);
	    logger.info("[wxapp refund]res debug:" + resHandler.getDebugInfo());
	    return result;
	}
	/**
	 * 
	 * <p>Title: dealRefundInfoWithWxapp</p> 
	 * <p>Description: 更新退款相关信息</p> 
	 * @param formPage 
	 * @param result 
	 * @param refund_status 
	 * 退款状态	refund_status	
					4，10：退款成功。
					3，5，6：退款失败。
					8，9，11:退款处理中。
					1，2: 未确定，需要商户原退款单号重新发起。
					7：转入代发，退款到银行发现用户的卡作废或者冻结了，导致原路退款银行卡失败，资金回流到商户的现金帐号，需要商户人工干预，通过线下或者财付通转账的方式进行退款。
	 * @param refund_fee
	 * @param refund_id
	 * @param refund_channel
	 * @param refund_channel2 
	 * @return
	 */
	private ObjectNode dealRefundInfoWithWxapp(ObjectNode result, RefundForm formPage, String out_refund_no, String refund_status,
			String refund_fee, String refund_id, String refund_channel) {
		if("4".equals(refund_status)||"10".equals(refund_status)){
			RefundInfo refundInfo = refundInfoService.updateRefundInfoWithWxPayInfo(formPage, refund_id, refund_channel,refund_fee,"[微信app退款]退款成功");
			if("1".equals(refundInfo.getStatus())){
				//更新本次退款所涉及到的业务信息
				refundInfoService.updateRefundOrderInfo(refundInfo);
			}
			//记录订单操作日志，退款操作
			operateLogService.saveOrderLog(1l, refundInfo.getOperator(), "127.0.0.1", "[微信app退款]订单"+refundInfo.getOrderCode()+"退款商品"+refundInfo.getSpid()+"操作成功，退款成功");
	    	logger.info("[wxapp refund]商户退款单号"+out_refund_no+"的退款状态是："+refund_status);
	    	result.put("status", "1");
        	result.put("msg", "[wxapp refund]商户退款单号"+out_refund_no+"成功！");
    	}else if("3".equals(refund_status)||"5".equals(refund_status)||"6".equals(refund_status)){
    		RefundInfo refundInfo = refundInfoService.updateRefundInfoWithWxPayInfo(formPage, refund_id, refund_channel,refund_fee,"[微信app退款]退款失败");
			//记录订单操作日志，退款操作
			operateLogService.saveOrderLog(1l, refundInfo.getOperator(), "127.0.0.1", "[微信app退款]退款失败！商户退款单号"+out_refund_no+"的退款状态是："+refund_status);
	    	logger.info("[wxapp refund]商户退款单号"+out_refund_no+"的退款状态是："+refund_status);
    		result.put("status", "0");
    		result.put("msg", "[wxapp refund]商户退款单号"+out_refund_no+"退款失败！");
    	}else if("8".equals(refund_status)||"9".equals(refund_status)||"11".equals(refund_status)){
    		RefundInfo refundInfo = refundInfoService.updateRefundInfoWithWxPayInfo(formPage, refund_id, refund_channel,refund_fee,"[微信app退款]退款处理中");
    		if("1".equals(refundInfo.getStatus())){
	    		//更新本次退款所涉及到的业务信息
				refundInfoService.updateRefundOrderInfo(refundInfo);
    		}
			//记录订单操作日志，退款操作
			operateLogService.saveOrderLog(1l, refundInfo.getOperator(), "127.0.0.1", "[微信app退款]退款处理中。。！商户退款单号"+out_refund_no+"的退款状态是："+refund_status);
	    	logger.info("[wxapp refund]商户退款单号"+out_refund_no+"的退款状态是："+refund_status);
    		result.put("status", "1");
    		result.put("msg", "[wxapp refund]商户退款单号"+out_refund_no+"退款处理中");
    	}else if("1".equals(refund_status)||"2".equals(refund_status)){
    		RefundInfo refundInfo = refundInfoService.updateRefundInfoWithWxPayInfo(formPage, refund_id, refund_channel,refund_fee,"[微信app退款]未确定，需要商户原退款单号重新发起");
			//记录订单操作日志，退款操作
			operateLogService.saveOrderLog(1l, refundInfo.getOperator(), "127.0.0.1", "[微信app退款]失败，未确定，需要商户原退款单号重新发起！商户退款单号"+out_refund_no+"的退款状态是："+refund_status);
	    	logger.info("[wxapp refund]商户退款单号"+out_refund_no+"的退款状态是："+refund_status);
    		result.put("status", "0");
    		result.put("msg", "[wxapp refund]商户退款单号"+out_refund_no+"退款失败，未确定，需要商户原退款单号重新发起！");
    	}else if("7".equals(refund_status)){
    		RefundInfo refundInfo = refundInfoService.updateRefundInfoWithWxPayInfo(formPage, refund_id, refund_channel,refund_fee,"[微信app退款]转入代发，退款到银行发现用户的卡作废或者冻结了，导致原路退款银行卡失败，资金回流到商户的现金帐号，需要商户人工干预，通过线下或者财付通转账的方式进行退款");
			//记录订单操作日志，退款操作
			operateLogService.saveOrderLog(1l, refundInfo.getOperator(), "127.0.0.1", "[微信app退款]失败，转入代发，退款到银行发现用户的卡作废或者冻结了，导致原路退款银行卡失败，资金回流到商户的现金帐号，需要商户人工干预，通过线下或者财付通转账的方式进行退款！商户退款单号"+out_refund_no+"的退款状态是："+refund_status);
	    	logger.info("[wxapp refund]商户退款单号"+out_refund_no+"的退款状态是："+refund_status);
    		result.put("status", "0");
    		result.put("msg", "[wxapp refund]商户退款单号"+out_refund_no+"退款失败，转入代发，退款到银行发现用户的卡作废或者冻结了，导致原路退款银行卡失败，资金回流到商户的现金帐号，需要商户人工干预，通过线下或者财付通转账的方式进行退款！");
    	}
		return result;
	}

}
