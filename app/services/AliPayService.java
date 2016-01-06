package services;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Named;
import javax.inject.Singleton;

import models.ShoppingOrder;
import models.ShoppingOrderPay;
import play.Logger;
import play.libs.Json;
import utils.StringUtil;
import utils.WSUtils;
import utils.alipay.AlipayConfig;
import utils.alipay.AlipaySubmit;
import utils.alipay.UtilDate;

import com.fasterxml.jackson.databind.JsonNode;

import forms.order.RefundForm;

/**
 * 阿里支付Service
 * 
 * @author luobotao
 * 
 */
@Named
@Singleton
public class AliPayService extends Thread {
	private static final Logger.ALogger logger = Logger.of(AliPayService.class);
	// 服务器异步通知页面路径
	private String notify_url = "http://182.92.227.140:9003/admin/alipay/alipayNotify/";
	// 需http://格式的完整路径，不能加?id=123这类自定义参数
	// 页面跳转同步通知页面路径
	private String call_back_url = "http://182.92.227.140:9003/admin/alipay/alipayReturn/";
	// 需http://格式的完整路径，不能加?id=123这类自定义参数，不能写成http://localhost/
	// 服务器异步通知页面路径
	private String notify_refund_url = "http://oms.higegou.com/refund/alipayNotifyRefund";

	private static AliPayService instance = new AliPayService();
	private Executor executor = Executors.newSingleThreadExecutor();
	private LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
	private static int batchNo=10000;
	/* 私有构造方法，防止被实例化 */
	private AliPayService() {
		this.start();
	}

	public void run() {
		logger.info("start AliPayService service ");
		Runnable r;
		try {
			while ((r = tasks.take()) != null) {
				executor.execute(r);
			}
		} catch (InterruptedException e) {
			logger.error("InterruptedException in AliPayService service", e);
		}
	}

	public static AliPayService getInstance() {
		return instance;
	}

	/**
	 * @param out_trade_no
	 *            订单号
	 * @param price
	 *            待付款金额
	 * @return
	 */
	public JsonNode sendAliPay(String out_trade_no, long price) {
		// 支付宝网关地址
		String ALIPAY_GATEWAY_NEW = "http://wappaygw.alipay.com/service/rest.htm?";
		// //////////////////////////////////调用授权接口alipay.wap.trade.create.direct获取授权码token//////////////////////////////////////
		// 返回格式
		String format = "xml";
		// 必填，不需要修改
		String v = "2.0";// 请求号
		String req_id = UtilDate.getOrderNum();// 必填，须保证每次请求都是唯一

		// 操作中断返回地址
		String merchant_url = "http://182.92.227.140:9003/admin/alipay/alipayNotify/";
		// 用户付款中途退出返回商户的地址。需http://格式的完整路径，不允许加?id=123这类自定义参数

		// 订单名称
		String subject = "嗨购商品";// 必填
		String total_fee = String.valueOf(price);// 必填

		// 请求业务参数详细
		String req_dataToken = "<direct_trade_create_req><notify_url>"
				+ notify_url + "</notify_url><call_back_url>" + call_back_url
				+ "</call_back_url><seller_account_name>"
				+ AlipayConfig.seller_email
				+ "</seller_account_name><out_trade_no>" + out_trade_no
				+ "</out_trade_no><subject>" + subject
				+ "</subject><total_fee>" + total_fee
				+ "</total_fee><merchant_url>" + merchant_url
				+ "</merchant_url></direct_trade_create_req>";
		// 必填

		// ////////////////////////////////////////////////////////////////////////////////

		// 把请求参数打包成数组
		Map<String, String> sParaTempToken = new HashMap<String, String>();
		sParaTempToken.put("service", "alipay.wap.trade.create.direct");
		sParaTempToken.put("partner", AlipayConfig.partner);
		sParaTempToken.put("_input_charset", AlipayConfig.input_charset);
		sParaTempToken.put("sec_id", AlipayConfig.sign_type);
		sParaTempToken.put("format", format);
		sParaTempToken.put("v", v);
		sParaTempToken.put("req_id", req_id);
		sParaTempToken.put("req_data", req_dataToken);

		// 建立请求
		String sHtmlTextToken = "";
		String request_token = "";
		try {
			sHtmlTextToken = AlipaySubmit.buildRequest(ALIPAY_GATEWAY_NEW, "",
					"", sParaTempToken);
			// URLDECODE返回的信息
			sHtmlTextToken = URLDecoder.decode(sHtmlTextToken,
					AlipayConfig.input_charset);
			// 获取token
			request_token = AlipaySubmit.getRequestToken(sHtmlTextToken);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 根据授权码token调用交易接口alipay.wap.auth.authAndExecute
		// 业务详细
		String req_data = "<auth_and_execute_req><request_token>"
				+ request_token + "</request_token></auth_and_execute_req>";
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "alipay.wap.auth.authAndExecute");
		sParaTemp.put("partner", AlipayConfig.partner);
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("sec_id", AlipayConfig.sign_type);
		sParaTemp.put("format", format);
		sParaTemp.put("v", v);
		sParaTemp.put("req_data", req_data);

		sParaTemp = AlipaySubmit.buildRequestPara(sParaTemp);

		return Json.toJson(sParaTemp);
	}

	public String sendWebAliPay(String out_trade_no, Double price) {
		// 支付类型
		String payment_type = "1";// 必填，不能修改
		String subject = "myorder";// 订单名称 必填;
		// 付款金额
		String total_fee = String.valueOf(price);// 必填;
		String body = "myorder";// 订单描述
		
		String show_url = "";// 商品展示地址需以http://开头的完整路径，例如：http://www.商户网址.com/myorder.html
		
		String anti_phishing_key = "";// 防钓鱼时间戳
		try {
			anti_phishing_key = AlipaySubmit.query_timestamp();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 客户端的IP地址
		String exter_invoke_ip = "";// 非局域网的外网IP地址，如：221.0.0.1

		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "create_direct_pay_by_user");
		sParaTemp.put("partner", AlipayConfig.partner);
		sParaTemp.put("seller_email", AlipayConfig.seller_email);
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("payment_type", payment_type);
		sParaTemp.put("notify_url", notify_url);
		sParaTemp.put("return_url", call_back_url);
		sParaTemp.put("out_trade_no", out_trade_no);
		sParaTemp.put("subject", subject);
		sParaTemp.put("total_fee", total_fee);
		sParaTemp.put("body", body);
		sParaTemp.put("show_url", show_url);
		sParaTemp.put("anti_phishing_key", anti_phishing_key);
		sParaTemp.put("exter_invoke_ip", exter_invoke_ip);
		// 建立请求
		return buildUrl("https://mapi.alipay.com/gateway.do?", sParaTemp);
	}

	private String buildUrl(String ALIPAY_GATEWAY_NEW,
			Map<String, String> sParaTemp) {
		// 待请求参数数组
		Map<String, String> sPara = AlipaySubmit.buildRequestPara(sParaTemp);
		List<String> keys = new ArrayList<String>(sPara.keySet());

		StringBuffer sbHtml = new StringBuffer();
		sbHtml.append(ALIPAY_GATEWAY_NEW + "_input_charset="+ AlipayConfig.input_charset);
		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) sPara.get(name);
			sbHtml.append("&" + name + "=" + value);
		}
		return sbHtml.toString();
	}

	/**
	 * 
	 * <p>Title: refundFastPay</p> 
	 * <p>Description: 支付宝快速退款</p>
	 */
	public RefundForm refundFastPay(ShoppingOrder so,double refundFee,RefundForm formPage) {
		//退款当天日期
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		SimpleDateFormat sdfNyr = new SimpleDateFormat("yyyyMMddHHmmss");
		String refund_date = sdf.format(new Date());
		//必填，格式：年[4位]-月[2位]-日[2位] 小时[2位 24小时制]:分[2位]:秒[2位]，如：2007-10-01 13:13:13
		//批次号
		String batch_no = sdfNyr.format(new Date())+so.getOrderCode();
		//必填，格式：当天日期[8位]+序列号[3至24位]，如：201008010000001 日期+订单号
		//退款笔数
		String batch_num = "1";
		//必填，参数detail_data的值中，“#”字符出现的数量加1，最大支持1000笔（即“#”字符出现的数量999个）
	
		//退款详细数据 交易退款数据集的格式为：原付款支付宝交易号^退款总金额^退款理由；
		String detail_data = so.getTradeno()+"^"+refundFee+"^"+formPage.memo;	//"2015110400001000640061538295^0.1^test";
		logger.info("detail_data:"+detail_data);
		formPage.tradeNo=so.getTradeno();
		formPage.batchNum = batch_num;
		formPage.batchNo=batch_no;
		formPage.detailData=detail_data;
		
		//必填，具体格式请参见接口技术文档
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "refund_fastpay_by_platform_pwd");		//接口名称
		sParaTemp.put("partner", AlipayConfig.partner);					//合作者身份ID
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);	//参数编码字符集
		sParaTemp.put("notify_url", notify_refund_url);					//服务器异步通知页面路径
		sParaTemp.put("seller_email", AlipayConfig.seller_email);		//卖家支付宝账号
		sParaTemp.put("seller_user_id", AlipayConfig.partner);			//卖家用户ID
		sParaTemp.put("refund_date", refund_date);						//退款请求时间
		sParaTemp.put("batch_no", batch_no);							//退款批次号
		sParaTemp.put("batch_num", batch_num);							//总笔数
		sParaTemp.put("detail_data", detail_data);						//单笔数据集	交易退款数据集的格式为：原付款支付宝交易号^退款总金额^退款理由；
		// 建立请求
		String url =  buildUrl("https://mapi.alipay.com/gateway.do?", sParaTemp);
		formPage.url=url;
		return formPage;
	}

}
