package controllers.refund;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import models.Account;
import models.Creditcard;
import models.OrderProduct;
import models.Parcels;
import models.ParcelsWaybill;
import models.RefundInfo;
import models.ShoppingOrder;
import models.ShoppingOrderPay;
import models.admin.AdminUser;
import models.product.Currency;
import models.product.Fromsite;
import models.product.Product;
import models.product.ProductGroup;
import models.user.User;
import models.user.UserBalance;
import models.user.UserBalanceLog;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import play.twirl.api.Html;
import services.AccountService;
import services.AliPayService;
import services.CreditcardService;
import services.OperateLogService;
import services.ServiceFactory;
import services.WxappPayService;
import services.WxwebPayService;
import services.admin.AdminUserService;
import services.admin.SmsService;
import services.order.OrderPayService;
import services.order.OrderProductService;
import services.order.OrderService;
import services.parcels.ParcelsProService;
import services.parcels.ParcelsService;
import services.parcels.ParcelsWaybillService;
import services.product.CurrencyService;
import services.product.ProductGroupService;
import services.product.ProductService;
import services.refund.RefundInfoService;
import services.refund.UserBalanceService;
import services.user.UserService;
import utils.AjaxHelper;
import utils.Constants;
import utils.Constants.LoveLyStatus;
import utils.Constants.PayMethod;
import utils.Numbers;
import vo.OrderVO;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;

import controllers.admin.AdminAuthenticated;
import controllers.admin.BaseAdminController;
import forms.order.OrderForm;
import forms.order.RefundForm;

/**
 * 
 * <p>Title: RefundController.java</p> 
 * <p>Description: 申请退款</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年11月9日  下午5:20:12
 * @version
 */
@Named
@Singleton
public class RefundController extends BaseAdminController {
	private static final Logger.ALogger logger = Logger.of(RefundController.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final ProductService productService;
    private final OrderService orderService;
    private final OrderProductService orderProductService;
    private final OrderPayService orderPayService;
    private final AdminUserService adminUserService;
    private final AliPayService alipayService;
    private final WxwebPayService wxwebpayService;
    private final WxappPayService wxapppayService;
    private final OperateLogService operateLogService;
    private final RefundInfoService refundInfoService;
    private final UserBalanceService userBalanceService;
    private final UserService userService;
    
    private final Form<OrderForm> orderForm = Form.form(OrderForm.class);
    private final Form<RefundForm> refundForm = Form.form(RefundForm.class);
    @Inject
    public RefundController(final ProductService productService,final OrderService orderService,
    		final OrderProductService orderProductService,final OperateLogService operateLogService,final AdminUserService adminUserService,
    		final RefundInfoService refundInfoService, final UserBalanceService userBalanceService,final UserService userService,final AliPayService alipayService,
    		final OrderPayService orderPayService,final WxwebPayService wxwebpayService,final WxappPayService wxapppayService) {
        this.productService = productService;
        this.orderService = orderService;
        this.orderProductService=orderProductService;
        this.operateLogService = operateLogService;
        this.adminUserService = adminUserService;
        this.refundInfoService = refundInfoService;
        this.userBalanceService = userBalanceService;
        this.userService = userService;
        this.alipayService = alipayService;
        this.orderPayService = orderPayService;
        this.wxwebpayService = wxwebpayService;
        this.wxapppayService = wxapppayService;
    }
	
	  /**
     * 
     * <p>Title: refundManage</p> 
     * <p>Description: 退款管理</p> 
     * @return
     */
    @AdminAuthenticated
	public Result refundManage(){
    	AdminUser adminUser =getCurrentAdminUser();
    	ServiceFactory.getCacheService().set(Constants.queryOrderRecordWithUser_KEY+adminUser.getId(), request().uri());
		Form<OrderForm> form = orderForm.bindFromRequest();
		OrderForm formPage = new OrderForm();
        if (!form.hasErrors()) {
        	formPage = form.get();
        }
        //标记操作类型为退款
        formPage.opertype = "refund";
        //根据现有条件获取订单总量
        Long totals = orderProductService.getTotalsWithForm(formPage);
        //根据现有条件获取订单集合,分页结果
        List<OrderVO> orderVoList = orderProductService.queryOrderProIdList(formPage);
     	List<Fromsite> fromsiteList  = productService.queryAllFromsite();
     	List<AdminUser> adminUserList  = adminUserService.queryAllLyAdminUser();		//查找出所有的联营商户
     	int totalPage = (int) (totals%formPage.size==0?totals/formPage.size:totals/formPage.size+1);
		return ok(views.html.refund.refundManage.render(adminUser,orderVoList,totals,formPage.page,totalPage,
				Html.apply(Constants.Typs.typs2HTML(Integer.parseInt(formPage.typ))),
				Html.apply(PayMethod.status2HTML(formPage.payMethod)),
				Html.apply(Constants.OrderProFlg.orderProFlg2HTML(formPage.orderProFlg)),
				Html.apply(LoveLyStatus.status2HTML(formPage.ordertype)),
				Html.apply(ProductService.fromsiteList2Html(fromsiteList,Integer.parseInt(formPage.fromsite))),
				Html.apply(AdminUserService.adminUserList2Html(adminUserList,formPage.adminid)),
				Html.apply(Constants.Paystat.status2HTML(formPage.paystat)),
				Html.apply(Constants.PreSells.typs2HTML(Integer.parseInt(formPage.preSell)))));
	}
    
    /**
     * 
     * <p>Title: toRefund</p> 
     * <p>Description: 跳转到退款页面</p> 
     * @return
     */
    @AdminAuthenticated()
	public Result dealRefund(){
		String soId = AjaxHelper.getHttpParam(request(), "soId");
		String sopIds = AjaxHelper.getHttpParam(request(), "sopIds");
		String refundReason = AjaxHelper.getHttpParam(request(), "refundReason");
		String memo = AjaxHelper.getHttpParam(request(), "memo");
		ShoppingOrder  shoppingOrder  = new ShoppingOrder ();
		List<OrderProduct> orderProductList = new ArrayList<OrderProduct>();
		double calRefundFee = 0.0;
		double useBalance = 0.0;
		//默认原因为嗨购原因
		if(Strings.isNullOrEmpty(refundReason)){
			refundReason = "1";
		}
		//查看是否使用钱包支付，记录钱包金额
		if(!Strings.isNullOrEmpty(soId)){
			shoppingOrder = orderService.queryShoppingOrderById(Long.parseLong(soId));
			if("1".equals(shoppingOrder.getUsewallet())){
				useBalance = Numbers.parseDouble(shoppingOrder.getUsebalance(), 0.0);
			}
			//优惠券价值
			double couponPrice = shoppingOrder.getCoupon_price();
			//计算订单优惠券使用前总价		= totalFee（订单总金额）+couponPrice（优惠券）   ||   goods_fee(商品总金额) + foreignfee(运费)
			double preTotalFee = shoppingOrder.getTotalFee()+couponPrice;
			//获得该订单下的所有商品订单shopping_order_pro
			orderProductList = orderProductService.getAllByShoppingOrderId(Long.parseLong(soId));
			if(orderProductList!=null&&orderProductList.size()>0){
				for (OrderProduct orderProduct : orderProductList) {
					Product product = productService.findProductById(orderProduct.getpId());
					product.setFromsiteName(productService.queryFromsiteById(product.getFromsite()).getName());
					orderProduct.setProduct(product);
				}
				//获取计算得到的应退款
				calRefundFee = dealRefundFeeWithSopIds(shoppingOrder,orderProductList,sopIds,couponPrice,preTotalFee,refundReason,memo);
			}
		}
		if(calRefundFee<=useBalance){
			useBalance = calRefundFee;
			calRefundFee = 0.0;
		}else{
			calRefundFee = calRefundFee-useBalance;
		}
		calRefundFee = Numbers.parseDouble(Numbers.formatDouble(calRefundFee, "0.00"), 0.00);
		return ok(views.html.refund.dealRefund.render(shoppingOrder,orderProductList,soId,sopIds,calRefundFee,useBalance));
	}

    /**
     * 
     * <p>Title: calRefundFee</p> 
     * <p>Description: 重新计算退款金额，包括应退金额和钱包应退金额</p> 
     * @return
     */
    @AdminAuthenticated()
   	public Result calRefundFee(){
   		String soId = AjaxHelper.getHttpParam(request(), "soId");
   		String sopIds = AjaxHelper.getHttpParam(request(), "sopIds");
   		String refundReason = AjaxHelper.getHttpParam(request(), "refundReason");
   		String memo = AjaxHelper.getHttpParam(request(), "memo");
   		logger.info("soId:"+soId);
   		logger.info("sopIds:"+sopIds);
   		logger.info("refundReason:"+refundReason);
   		ShoppingOrder  shoppingOrder  = new ShoppingOrder ();
   		List<OrderProduct> orderProductList = new ArrayList<OrderProduct>();
   		double calRefundFee = 0.0;
   		double useBalance = 0.0;
   		//默认原因为嗨购原因
   		if(Strings.isNullOrEmpty(refundReason)){
   			refundReason = "1";
   		}
   		//查看是否使用钱包支付，记录钱包金额
   		if(!Strings.isNullOrEmpty(soId)){
   			shoppingOrder = orderService.queryShoppingOrderById(Long.parseLong(soId));
   			if("1".equals(shoppingOrder.getUsewallet())){
   				useBalance = Numbers.parseDouble(shoppingOrder.getUsebalance(), 0.0);
   			}
   			//优惠券价值
   			double couponPrice = shoppingOrder.getCoupon_price();
   			//计算订单优惠券使用前总价		= totalFee（订单总金额）+couponPrice（优惠券）   ||   goods_fee(商品总金额) + foreignfee(运费)
   			double preTotalFee = shoppingOrder.getTotalFee()+couponPrice;
   			//获得该订单下的所有商品订单shopping_order_pro
   			orderProductList = orderProductService.getAllByShoppingOrderId(Long.parseLong(soId));
   			if(orderProductList!=null&&orderProductList.size()>0){
   				for (OrderProduct orderProduct : orderProductList) {
   					Product product = productService.findProductById(orderProduct.getpId());
   					product.setFromsiteName(productService.queryFromsiteById(product.getFromsite()).getName());
   					orderProduct.setProduct(product);
   				}
   				//获取计算得到的应退款
   				calRefundFee = dealRefundFeeWithSopIds(shoppingOrder,orderProductList,sopIds,couponPrice,preTotalFee,refundReason,memo);
   				logger.info("calRefundFee pre:"+calRefundFee);
   			}
   		}
   		logger.info("useBalance:"+useBalance);
   		if(calRefundFee<=useBalance){
   			useBalance = calRefundFee;
   			calRefundFee = 0.0;
   		}else{
   			calRefundFee = calRefundFee-useBalance;
   		}
   		logger.info("calRefundFee result:"+calRefundFee);
   		calRefundFee = Numbers.parseDouble(Numbers.formatDouble(calRefundFee, "0.00"), 0.00);
   		ObjectNode result=Json.newObject();
   		result.put("calRefundFee", calRefundFee);
   		result.put("useBalance", useBalance);
   		return ok(Json.toJson(result));
   	}
    /**
     * 
     * <p>Title: dealRefundFeeWithSopIds</p> 
     * <p>Description: 计算应退金额</p> 
     * @param sopIds
     * @param memo 
     * @return
     */
	private double dealRefundFeeWithSopIds(ShoppingOrder shoppingOrder,List<OrderProduct> orderProductList, String sopIds,double couponPrice,double preTotalFee,String refundReason, String memo) {
		String[] sopIdsTemp = sopIds.split(",");	//	用户选择的该订单下的应退商品
		double refundFee = 0.0;	//退款金额
		double totalPrice = 0.0;
		double totalForeighFee = 0.0;
		//根据fromsite来分类订单商品，从而用于计算应退金额
		Map<Integer, List<OrderProduct>> opMap = new HashMap<Integer, List<OrderProduct>>();
		opMap = getOpMapWith(orderProductList,sopIdsTemp);
		//我方原因，（运费超额退款)
		//计算商品价格和运费
		for(Integer key : opMap.keySet()){
			List<OrderProduct> ops = opMap.get(key);
			////根据不同的原因计算商品的价格和运费 商品来源获取运费 ，默认运费计算规则根据退款原因为我方原因		1、我方原因： 按照商品重量退器运费	2、用户原因： 只退续重
			for (OrderProduct orderProduct : ops) {
				Product product = orderProduct.getProduct();
				if("1".equals(refundReason)){//操作，直接计算退款商品的运费总额
					totalPrice += orderProduct.getTotalFee();
					totalForeighFee += productService.getfreight(product.getFromsite(), String.valueOf(product.getWeight()*orderProduct.getCounts()));
				}else if("2".equals(refundReason)){//属于用户原因
					//totalPrice += orderProduct.getTotalFee();
					//totalForeighFee += productService.getfreight(product.getFromsite(), String.valueOf(product.getWeight()*orderProduct.getCounts()));
				}
			}
			//公式：（商品价格+运费）- [（商品价格+运费）/订单优惠券使用前总价]*优惠券价值
			double proFee = totalPrice + totalForeighFee;
			if(proFee<=0){
				proFee = 0.0;
			}
			logger.info("proFee:"+proFee);
			logger.info("preTotalFee:"+preTotalFee);
			logger.info("couponPrice:"+couponPrice);
			refundFee += proFee - (proFee/preTotalFee)*couponPrice;
			logger.info("refundFee key:"+key+" , refundFee val:"+refundFee);
		}
		//针对多个来源的订单商品，则相加
		return refundFee;
	}

	/**
	 * 
	 * <p>Title: getOpMapWith</p> 
	 * <p>Description: 对商品根据来源进行分类，存入map</p> 
	 * @param orderProductList
	 * @param sopIdsTemp
	 * @return
	 */
	private Map<Integer, List<OrderProduct>> getOpMapWith(
			List<OrderProduct> orderProductList, String[] sopIdsTemp) {
		Map<Integer, List<OrderProduct>> opMap = new HashMap<Integer, List<OrderProduct>>();
		List<Integer> keys = new ArrayList<Integer>();
		//如果是单独勾选的商品进行金额计算，则
		if(sopIdsTemp!=null&&sopIdsTemp.length>0&&!StringUtils.isBlank(sopIdsTemp[0])){
			for (String sopId : sopIdsTemp) {
				List<OrderProduct> orderProducts = new ArrayList<OrderProduct>();
				OrderProduct orderProduct = orderProductService.queryOrderProductById(Numbers.parseLong(sopId, 0l));
				if("2".equals(orderProduct.getFlg())){
					Product product = orderProduct.getProduct();
					if(product == null){
						product = productService.findProductById(orderProduct.getpId());
					}
					int fromsite = product.getFromsite();
					if(!keys.contains(fromsite)){
						keys.add(fromsite);
						opMap.put(fromsite, new ArrayList<OrderProduct>());
					}
					orderProduct.setProduct(product);
					orderProducts = opMap.get(fromsite);
					orderProducts.add(orderProduct);
					opMap.put(fromsite, orderProducts);
				}
			}
		}else{
			//针对该订单下所有退款商品，计算运费和价格
			for (OrderProduct orderProduct : orderProductList) {
				if("2".equals(orderProduct.getFlg())){
					Product product = orderProduct.getProduct();
					if(product == null){
						product = productService.findProductById(orderProduct.getpId());
					}
					int fromsite = product.getFromsite();
					if(!keys.contains(fromsite)){
						keys.add(fromsite);
						opMap.put(fromsite, new ArrayList<OrderProduct>());
					}
					List<OrderProduct> orderProducts = opMap.get(fromsite);
					orderProducts.add(orderProduct);
					opMap.put(fromsite, orderProducts);
				}
			}
		}
		return opMap;
	}
	
	/**
	 * 
	 * <p>Title: saveRefund</p> 
	 * <p>Description: 保存退款信息</p> 
	 * @return
	 */
	@AdminAuthenticated
	public Result saveRefund(){
		response().setContentType("application/json;charset=utf-8");
		ObjectNode result = Json.newObject();
		AdminUser adminUser = getCurrentAdminUser();
		Form<RefundForm> form = refundForm.bindFromRequest();
		RefundForm formPage = new RefundForm();
        if (!form.hasErrors()) {
        	formPage = form.get();
        }
        //判断信息的正确性，包扩退款金额和程序计算退款金额，优惠券，钱包退款金额等
        //判断当前退款列表中是否存在该支付流水下的未完成退款记录，如果存在，则不允许给予退款操作， 退款状态，申请退款，退款中，退款完成，退款失败
        //判断退款金额是否>0,钱包退款和应退金额相加
        if(Strings.isNullOrEmpty(formPage.refundFee)||Strings.isNullOrEmpty(formPage.soId)||Numbers.parseDouble(formPage.refundFee, 0.0)+Numbers.parseDouble(formPage.walletRefundFee, 0.0)<=0){
        	result.put("status", "0");
        	result.put("msg", "退款信息错误，请检查！");
        	return ok(Json.toJson(result));
        }
        //根据支付类型选择退款操作
        String paymethod = formPage.paymethod;
        long soId = Numbers.parseLong(formPage.soId,0l);
        ShoppingOrder shoppingOrder = orderService.queryShoppingOrderById(soId);
        ShoppingOrderPay shoppingOrderPay = orderPayService.findByOrderId(soId);
        double refundFee = Numbers.parseDouble(formPage.refundFee, 0.0);
        formPage.tradeNo=shoppingOrderPay.getTradeno();
        //根据支付方式调用不同的退款接口
        if(refundFee>0.0){
	        if("10".equals(paymethod)||"11".equals(paymethod)){//10 微信app 接入财付通退款
	        	//记录本次退款日志
		    	RefundInfo refundInfo = refundInfoService.saveRefundInfoWithForm(formPage,adminUser);
		    	//进微信app退款操作
		    	try {
			    	if("10".equals(paymethod)){
			    		formPage.type="wxapp";
			    		result = wxapppayService.refundAppPay(result,shoppingOrder,refundFee, formPage);
			    	}else{
			    		formPage.type="wxweb";
			    		result = wxwebpayService.refundWebPay(result,shoppingOrder,refundFee,formPage);
			    	}
				} catch (Exception e) {
					e.printStackTrace();
				}
		    	return ok(Json.toJson(result));
	        }else if("20".equals(paymethod)||"21".equals(paymethod)||"22".equals(paymethod)){//20 支付宝快捷 21 支付宝wap 22支付宝国际
	        	formPage.type="alipay";
	        	formPage = alipayService.refundFastPay(shoppingOrder,refundFee, formPage);
	        	String url = formPage.url;
	        	logger.info("--------->>>refund url "+url);
	        	//记录本次退款日志
	        	if(!Strings.isNullOrEmpty(url)){
	        		RefundInfo refundInfo = refundInfoService.saveRefundInfoWithForm(formPage,adminUser);
	        		return redirect(url);
	        	}else{
	        		return redirect("/pageError");
	        	}
	        }else{
	        	//不支持的退款方式
	        	logger.info("不支持的退款方式");
	        	result.put("status", "0");
	        	result.put("msg", "不支持的退款方式！");
	        	return ok(Json.toJson(result));
	        }
        }else{
        	logger.info("本次退款无相应的微信或支付宝退款金额，直接操作钱包");
        	formPage.status="1";
        	formPage.dateSucc = new Date();
        	//变更业务信息
			logger.info("退款申请已提交，退款单号："+formPage.tradeNo+",现金退款金额:"+refundFee+",钱包退款金额:"+formPage.walletRefundFee);
			//更新refundInfo ,退款信息记录
			RefundInfo refundInfo = refundInfoService.saveRefundInfoWithForm(formPage, adminUser);
			//更新本次退款所涉及到的业务信息
			refundInfoService.updateRefundOrderInfo(refundInfo);
			//记录订单操作日志，退款操作
			operateLogService.saveOrderLog(1l, refundInfo.getOperator(), "127.0.0.1", "[钱包退款]订单"+refundInfo.getOrderCode()+"退款商品"+refundInfo.getSpid()+"操作成功");
			return redirect("/refund/refundManage");
        }
	}
	
	
	/**
	 * 
	 * <p>Title: alipayNotifyRefund</p> 
	 * <p>Description: 阿里退款回调接口</p> 
	 * @return
	 */
	public Result alipayNotifyRefund(){
		Map<String,String> params = new HashMap<String,String>();
		logger.info("====================>>> alipay notify refund <<<=================");
		System.out.println(request());
		System.out.println(request().body());
		RequestBody rb = request().body();
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
				logger.info("[alipay] refund notify info, key:"+key+",value:"+valueStr);
			}
			String notify_time="";//通知时间
			String notify_id="";	//通知校验id, 用于校验该消息是否真实来源于支付宝
			String batch_no="";//退款批次号		年月日加本次退款订单商品id（id,id）
			String success_num="";//退款成功的笔数
			//退手续费结果返回格式：交易号^退款金额^处理结果\$退费账号^退费账户ID^退费金额^处理结果；不退手续费结果返回格式：交易号^退款金额^处理结果。若退款申请提交成功，处理结果会返回“SUCCESS”。若提交失败，退款的处理结果中会有报错码，参见即时到账批量退款业务错误码。
			String result_details="";//退款结果明细
			try {
				notify_time = new String(params.get("notify_time").getBytes("ISO-8859-1"),"UTF-8");
				batch_no = new String(params.get("batch_no").getBytes("ISO-8859-1"),"UTF-8");
				success_num = new String(params.get("success_num").getBytes("ISO-8859-1"),"UTF-8");
				notify_id = new String(params.get("notify_id").getBytes("ISO-8859-1"),"UTF-8");
				result_details = new String(params.get("result_details").getBytes("ISO-8859-1"),"UTF-8");
				//String checkResultStr = AlipayNotify.verifyResponse(notify_id);
				String checkResultStr = "true";
				if(checkResultStr!=null&&"true".equals(checkResultStr)){
					logger.info("校验notify_id："+notify_id+"来源于支付宝正确");
					//更新refundInfo ,退款信息记录
					RefundInfo refundInfo = refundInfoService.updateRefundInfoWithAliPayInfo(notify_time, batch_no,success_num,result_details);
					if("1".equals(refundInfo.getStatus())){
						//更新本次退款所涉及到的业务信息
						refundInfoService.updateRefundOrderInfo(refundInfo);
						//记录订单操作日志，退款操作
						operateLogService.saveOrderLog(1l, refundInfo.getOperator(), "127.0.0.1", "订单"+refundInfo.getOrderCode()+"退款商品"+refundInfo.getSpid()+"操作成功，退款成功");
					}else{
						operateLogService.saveOrderLog(1l, refundInfo.getOperator(), "127.0.0.1", "订单"+refundInfo.getOrderCode()+"退款商品"+refundInfo.getSpid()+"操作成功，退款失败");
					}
					
					return ok("success");
				}
				logger.info("校验notify_id："+notify_id+"来源于支付宝错误，请关注此消息");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return redirect("/pageError");
	}
	
}
