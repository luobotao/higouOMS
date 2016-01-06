package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.net.ssl.SSLContext;

import models.RefundInfo;
import models.ShoppingOrder;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.h2.util.StringUtils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;

import play.Logger;
import repositories.Order.RefundInfoRepository;
import services.refund.RefundInfoService;
import utils.Constants;
import utils.MD5Util;
import utils.Numbers;
import utils.StringUtil;
import utils.wxwebpay.ClientCustomSSL;
import utils.wxwebpay.RequestHandler;
import utils.wxwebpay.WeixinUtil;
import forms.order.RefundForm;

/**
 * 
 * <p>Title: WxwebPayService.java</p> 
 * <p>Description: 微信网页支付，公众号</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年11月14日  上午10:14:43
 * @version
 */
@Named
@Singleton
public class WxwebPayService extends Thread {
	private static final Logger.ALogger logger = Logger.of(WxwebPayService.class);
	@Inject
    private RefundInfoService refundInfoService;
    @Inject
    private OperateLogService operateLogService;
	
	private static WxwebPayService instance = new WxwebPayService();
	private Executor executor = Executors.newSingleThreadExecutor();
	private LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
	
	/* 私有构造方法，防止被实例化 */
	private WxwebPayService() {
		this.start();
	}

	public void run() {
		logger.info("start WxPayService service ");
		Runnable r;
		try {
			while ((r = tasks.take()) != null) {
				executor.execute(r);
			}
		} catch (InterruptedException e) {
			logger.error("InterruptedException in WxPayService service", e);
		}
	}

	public static WxwebPayService getInstance() {
		return instance;
	}

	/*
	 * 微信退款接口
	 */
	public ObjectNode refundWebPay(ObjectNode result, ShoppingOrder shoppingOrder, double refundFee, RefundForm formPage) throws Exception{
		String nonce_str=RandomStringUtils.randomAlphanumeric(32);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		//位置退款信息组装
		SortedMap<String,String> signmap=new TreeMap<String,String>();
		signmap.put("appid",Constants.WXappID);			//公众账号ID
		signmap.put("mch_id",Constants.WXMCID);			//商户号
		signmap.put("nonce_str", nonce_str);			//随机字符串
		signmap.put("out_trade_no",shoppingOrder.getOrderCode());			//商户订单号
		signmap.put("transaction_id", shoppingOrder.getTradeno());	//微信订单号
		signmap.put("out_refund_no", sdf.format(new Date())+shoppingOrder.getOrderCode());		//商户退款单号
		signmap.put("total_fee", String.valueOf(Numbers.parseInt(String.valueOf(100*shoppingOrder.getTotalFee()), 0)));		//总金额
		signmap.put("refund_fee", String.valueOf(Numbers.parseInt(String.valueOf(100*refundFee), 0)));			//退款金额
		signmap.put("refund_fee_type", "CNY");			//货币总类默认人民币
		signmap.put("op_user_id", Constants.WXMCID);	//操作员
		
		RequestHandler reqHandler = new RequestHandler(null, null);
		reqHandler.init(Constants.WXappID, Constants.WXappsecret, Constants.WXappsecretpay);
		
		//签名
		String sign = reqHandler.createSign(signmap);
		signmap.put("sign", sign);
		//退款请求地址
		String url="https://api.mch.weixin.qq.com/secapi/pay/refund";
		//组装提交参数
	    String arrayToXml = WeixinUtil.ArrayToXml(signmap);
	    Map call = ClientCustomSSL.call(url, arrayToXml);
	    if(call!=null){
	    	//返回状态码
			String return_code=(String) call.get("return_code");
			//微信退款记录成功
			if(!Strings.isNullOrEmpty(return_code) && return_code.equals("SUCCESS")){
				//退款申请提交成功，业务结果
				String result_code=(String) call.get("result_code");
				if(!Strings.isNullOrEmpty(result_code) && result_code.equals("SUCCESS")){
					String out_refund_no=(String) call.get("out_refund_no");				//商户退款单号
					String refund_id=(String) call.get("refund_id");				//微信退款单号
					String refund_channel=(String) call.get("refund_channel");		//退款渠道
					String cash_fee=(String) call.get("cash_fee");					//现金支付金额
					String cash_refund_fee=(String) call.get("cash_refund_fee");	//现金退款金额
					String coupon_refund_fee=(String) call.get("coupon_refund_fee");//代金券或立减优惠退款金额
					String coupon_refund_count=(String) call.get("coupon_refund_count");//代金券或立减优惠使用数量
					String coupon_refund_id=(String) call.get("coupon_refund_id");	//代金券或立减优惠ID
					RefundInfo refundInfo = refundInfoService.updateRefundInfoWithWxPayInfo(formPage, refund_id, refund_channel,cash_refund_fee,"[微信web退款]退款成功");
					if("1".equals(refundInfo.getStatus())){
						//更新本次退款所涉及到的业务信息
						refundInfoService.updateRefundOrderInfo(refundInfo);
						//更新本次退款所涉及到的业务信息
						operateLogService.saveOrderLog(1l, refundInfo.getOperator(), "127.0.0.1", "[微信web退款]订单"+refundInfo.getOrderCode()+"退款商品"+refundInfo.getSpid()+"操作成功，退款成功");
					}
			    	logger.info("[wxweb refund]商户退款单号"+out_refund_no+"退款成功");
			    	result.put("status", "1");
		        	result.put("msg", "[wxweb refund]商户退款单号"+out_refund_no+"退款成功");
				}else{
					String err_code=(String) call.get("err_code");
					String err_code_des=(String) call.get("err_code_des");
					RefundInfo refundInfo = refundInfoService.updateRefundInfoWithWxPayInfo(formPage, "", "","","[微信web退款]退款失败，"+err_code_des);
					//记录订单操作日志，退款操作
					operateLogService.saveOrderLog(1l, refundInfo.getOperator(), "127.0.0.1", "[微信web退款]订单"+refundInfo.getOrderCode()+"退款商品"+refundInfo.getSpid()+"操作成功，退款失败");
			    	logger.info("[wxweb refund]失败，原因："+err_code);

					result.put("status", "0");
		        	result.put("msg", "[wxweb refund]商户退款单号"+formPage.orderCode+"退款失败！错误原因："+err_code_des);
				}
				logger.info("订单退款已进入微信退款记录");
			}else{
				String return_msg=(String) call.get("return_msg");
				logger.info("退款失败");
				result.put("status", "0");
	        	result.put("msg", "[wxweb refund]商户退款单号"+formPage.orderCode+"退款失败！");
			}
	    }
	    System.out.print(call);
	    return result;
	}

}
