package controllers.admin;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.ShoppingOrder;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import services.AliPayService;
import services.product.ProductService;
import utils.Constants.OrderStatus;
import utils.Constants.PayMethod;
import utils.Dates;
import utils.JdbcOper;
import utils.Numbers;
import utils.alipay.AlipayNotify;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.BaseController;

/**
 * 阿里支付Controller
 * 
 * @author luobotao
 * 
 */
@Named
@Singleton
public class AliPayAPIController extends BaseController {
	private static final Logger.ALogger logger = Logger.of(AliPayAPIController.class);
	
	private final ProductService productService;
    @Inject
    public AliPayAPIController(final ProductService productService) {
        this.productService = productService;
    }
	//获取阿里支付的必填参数
	public static Result sendAliPay() {
		String out_trade_no = "123123";
		long price = 1;
		JsonNode reslut = AliPayService.getInstance().sendAliPay(out_trade_no,price);
		// String url =
		// "http://wappaygw.alipay.com/service/rest.htm?_input_charset=utf-8"+"&sign=LuCVNbWaTvbVLlWpIo8uVfVzqI/MUfwhdC2LtvlHvFztvGtUltrGK3984wP/Y0X7ieAmHgYMIT3mIQjl97H3wRmw3g8CMT0wiwnTJrEl7JOHAlU7ERfLO/JKlsPv1QYmiIHWk0CggBNW2KXunJ9NWyKtdqOYUWRGIaxwS/Xcjac=&sec_id=0001&v=2.0&_input_charset=utf-8&req_data=<auth_and_execute_req><request_token>201504102a3e8a50b1f7ee599a237cf5a59cc960</request_token></auth_and_execute_req>&service=alipay.wap.auth.authAndExecute&partner=2088711832091925&format=xml";
		// return redirect(reslut);
//		String sign="ZVB/3FJA62lBqQeMC1oTB+J2fTkCgXXVhaVnV2+NFUfse9qOz5Xz6pWTzPGRGPIDWnAb13rxqIBLNJvjCWAHu6J3/ingqL1TIr+BhzP5o2h/9vnq1vh9QA7ZxwNJBZUbCW6b2egMjg1zoA6WdbZhi82JrU7kPE/2GK9WUarK/ps=";
//		String req_data="<auth_and_execute_req><request_token>20150416595536d605687ed62d260dde0c542bd9</request_token></auth_and_execute_req>";
//		Logger.info(id+"===========");
//		
//		String temp = "?body=234234&subject=1&sign_type=MD5&notify_url=http%3A%2F%2F商户网关地址%2Fcreate_direct_pay_by_user-JAVA-UTF-8%2Fnotify_url.jsp&out_trade_no=22222&return_url=http%3A%2F%2F商户网关地址%2Fcreate_direct_pay_by_user-JAVA-UTF-8%2Freturn_url.jsp&sign=8a5352de1b56f2af1398ceeb890d37c8&_input_charset=utf-8&total_fee=2&service=create_direct_pay_by_user&partner=2088811656917752&anti_phishing_key=KP3HyfAezESk5PyHIQ%3D%3D&seller_email=yangtao%40neolix.cn&payment_type=1&show_url=34";
//		 String url =
//		 "https://mapi.alipay.com/gateway.do?_input_charset=utf-8"+"&sign="+sign+"&sec_id=0001&v=2.0&_input_charset=utf-8&req_data="+req_data+"&service=alipay.wap.auth.authAndExecute&partner=2088711832091925&format=xml";
//		
		response().setContentType("application/json;charset=utf-8");
		return ok(Json.toJson(reslut));
	}
	/**
	 * 根据订单编号去获取订单并跳转支付页面
	 * @param id
	 * @return
	 */
	public Result checkOutOrder(String id) {
		ShoppingOrder shoppingOrder = productService.getShoppingOrderById(Numbers.parseLong(id, 0L));
		if(shoppingOrder!=null){
			String out_trade_no = shoppingOrder.getOrderCode();
			Double price = shoppingOrder.getTotalFee();
			String url = AliPayService.getInstance().sendWebAliPay(out_trade_no, price);
			return redirect(url);
		}
		 response().setContentType("application/json;charset=utf-8");
		return ok(Json.toJson("此订单不存在"));
	}
	
	/**
	 * 阿里支付的回调(异步) POST
	 * @param id
	 * @return
	 */
	public Result alipayNotify() {
		Map<String,String> params = new HashMap<String,String>();
		Map<String,String[]> all =request().body().asFormUrlEncoded();
		if(all!=null && all.keySet()!=null){
			Iterator<String> keyIt = all.keySet().iterator();
			while(keyIt.hasNext()){
				String key = keyIt.next();
				String[] values = all.get(key);
				String valueStr = "";
				for (int i = 0; i < values.length; i++) {
					valueStr = (i == values.length - 1) ? valueStr + values[i]: valueStr + values[i] + ",";
				}
				//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
				try {
					valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				params.put(key, valueStr);
			}
			String out_trade_no="";//商户订单号
			String trade_no="";//支付宝交易号
			String trade_status="";//交易状态
			try {
				out_trade_no = new String(params.get("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
				trade_no = new String(params.get("trade_no").getBytes("ISO-8859-1"),"UTF-8");
				trade_status = new String(params.get("trade_status").getBytes("ISO-8859-1"),"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			boolean verify_result = AlipayNotify.verifyReturn(params);
			
			if(verify_result){//验证成功
				if(trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")){
					ShoppingOrder shoppingOrder = productService.getShoppingOrderByOrderCode(out_trade_no);
					if(shoppingOrder!=null && shoppingOrder.getStatus()==OrderStatus.NOPAY.getStatus()){
						shoppingOrder.setStatus(OrderStatus.WAITTODELIVER.getStatus());
						shoppingOrder.setPaymethod(PayMethod.ALIWEB.getStatus());
						shoppingOrder.setPaytime(Dates.formatEngLishDateTime(new Date()));
						shoppingOrder.setTradeno(trade_no);
						shoppingOrder.setStage(0);
						productService.saveShoppingOrder(shoppingOrder);
						productService.createParcelByOrder(shoppingOrder.getId());//生成包裹
					}
				}
				return redirect(routes.AliPayAPIController.paySuccess());
			}else{
				return redirect("/pageError");
			}
		}
		return redirect("/pageError");
	}
	/**
	 * 阿里支付的回调(同步) GET
	 * @param id
	 * @return
	 */
	public Result alipayReturn() {
		Map<String,String> params = new HashMap<String,String>();
		Map<String,String[]> allQue = request().queryString();
		if(allQue!=null && allQue.keySet()!=null){
			Iterator<String> keyIt = allQue.keySet().iterator();
			while(keyIt.hasNext()){
				String key = keyIt.next();
				String[] values = allQue.get(key);
				String valueStr = "";
				for (int i = 0; i < values.length; i++) {
					valueStr = (i == values.length - 1) ? valueStr + values[i]: valueStr + values[i] + ",";
				}
				//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
				try {
					valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				params.put(key, valueStr);
			}
			String out_trade_no="";//商户订单号
			String trade_no="";//支付宝交易号
			String trade_status="";//交易状态
			try {
				out_trade_no = new String(params.get("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
				trade_no = new String(params.get("trade_no").getBytes("ISO-8859-1"),"UTF-8");
				trade_status = new String(params.get("trade_status").getBytes("ISO-8859-1"),"UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			boolean verify_result = AlipayNotify.verifyReturn(params);
			
			if(verify_result){//验证成功
				if(trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")){
					ShoppingOrder shoppingOrder = productService.getShoppingOrderByOrderCode(out_trade_no);
					if(shoppingOrder!=null ){
						int a = OrderStatus.WAITTODELIVER.getStatus();
						logger.info(a+"-----");
						shoppingOrder.setStatus(OrderStatus.WAITTODELIVER.getStatus());
						shoppingOrder.setPaymethod(PayMethod.ALIWEB.getStatus());
						shoppingOrder.setPaytime(Dates.formatEngLishDateTime(new Date()));
						shoppingOrder.setTradeno(trade_no);
						shoppingOrder.setStage(0);
						productService.saveShoppingOrder(shoppingOrder);
						productService.createParcelByOrder(shoppingOrder.getId());//生成包裹
					}
				}
				return redirect(routes.AliPayAPIController.paySuccess());
			}else{
				return redirect("/pageError");
			}
		}
		
		return redirect("/pageError");
	}
	
	/**
	 * 付款成功跳转页面
	 * @return
	 */
	public static Result paySuccess(){
		return ok(views.html.pagePaySeccess.render());
	}
	/**
	 * 
	 * 阿里操作中断的URL
	 * @return
	 */
	public Result merchant_url() {
		Map<String,String[]> all =request().body().asFormUrlEncoded();
		if(all!=null && all.keySet()!=null){
			Iterator<String> keyIt = all.keySet().iterator();
			while(keyIt.hasNext()){
				String key = keyIt.next();
				String[] values = all.get(key);
				logger.info("中断merchant_url===========asFormUrlEncoded key is "+key+" == and value is "+values);
			}
		}
		
		Map<String,String[]> allQue = request().queryString();
		if(allQue!=null && allQue.keySet()!=null){
			Iterator<String> keyIt = allQue.keySet().iterator();
			while(keyIt.hasNext()){
				String key = keyIt.next();
				String[] values = allQue.get(key);
				logger.info("中断merchant_url===========queryString key is "+key+" == and value is "+values);
			}
		}
		
		response().setContentType("application/json;charset=utf-8");
		return ok(Json.toJson("Fail"));
	}

}
