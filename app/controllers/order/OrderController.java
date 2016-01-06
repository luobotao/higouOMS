package controllers.order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.Account;
import models.Creditcard;
import models.OrderProduct;
import models.Parcels;
import models.ParcelsWaybill;
import models.ShoppingOrder;
import models.admin.AdminUser;
import models.product.Currency;
import models.product.Fromsite;
import models.product.Product;
import models.product.ProductGroup;
import play.Logger;
import play.data.Form;
import play.mvc.Result;
import play.twirl.api.Html;
import services.AccountService;
import services.CreditcardService;
import services.OperateLogService;
import services.ServiceFactory;
import services.admin.AdminUserService;
import services.admin.SmsService;
import services.order.OrderProductService;
import services.order.OrderService;
import services.parcels.ParcelsProService;
import services.parcels.ParcelsService;
import services.parcels.ParcelsWaybillService;
import services.product.CurrencyService;
import services.product.ProductGroupService;
import services.product.ProductService;
import utils.AjaxHelper;
import utils.Constants;
import utils.Constants.LoveLyStatus;
import utils.Constants.PayMethod;
import utils.Htmls;
import utils.Numbers;
import utils.StringUtil;
import vo.OrderCompanyPageVO;
import vo.OrderVO;

import com.google.common.base.Strings;

import controllers.admin.AdminAuthenticated;
import controllers.admin.BaseAdminController;
import forms.order.OrderForm;

/**
 * 
 * <p>Title: ProductController.java</p> 
 * <p>Description: 订单信息管理</p> 
 * <p>Company: higegou</p> 
 * @author  
 * date  2015年7月28日  下午2:12:23
 * @version
 */
@Named
@Singleton
public class OrderController extends BaseAdminController {
	private static final Logger.ALogger logger = Logger.of(OrderController.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final ProductService productService;
    private final OrderService orderService;
    private final OrderProductService orderProductService;
    private final CurrencyService currencyService;
    private final CreditcardService creditcardService;
    private final AccountService accountService;
    private final ParcelsService parcelsService;
    private final ParcelsProService parcelsProService;
    private final ParcelsWaybillService parcelsWaybillService;
    private final SmsService smsService;
    private final ProductGroupService productGroupService;
    private final OperateLogService operateLogService;
    private final AdminUserService adminUserService;
    
    private final Form<OrderForm> orderForm = Form.form(OrderForm.class);
    @Inject
    public OrderController(final ProductService productService,final OrderService orderService,
    		final OrderProductService orderProductService,final CurrencyService currencyService,final CreditcardService creditcardService,AccountService accountService,
    		final ParcelsService parcelsService,final ParcelsProService parcelsProService,final ParcelsWaybillService parcelsWaybillService,
    		final SmsService smsService,final ProductGroupService productGroupService,final OperateLogService operateLogService,final AdminUserService adminUserService) {
        this.productService = productService;
        this.orderService = orderService;
        this.orderProductService=orderProductService;
        this.creditcardService = creditcardService;
        this.currencyService = currencyService;
        this.accountService = accountService;
        this.parcelsService = parcelsService;
        this.parcelsProService = parcelsProService;
        this.parcelsWaybillService = parcelsWaybillService;
        this.smsService = smsService;
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
    @AdminAuthenticated
	public Result orderManage(){
    	AdminUser adminUser =getCurrentAdminUser();
    	ServiceFactory.getCacheService().set(Constants.queryOrderRecordWithUser_KEY+adminUser.getId(), request().uri());
		Form<OrderForm> form = orderForm.bindFromRequest();
		OrderForm formPage = new OrderForm();
        if (!form.hasErrors()) {
        	formPage = form.get();
        }
        
        if(!Strings.isNullOrEmpty(formPage.keyword)){
        	formPage = dealFormWithKeyword(formPage);
        }
        if(!Strings.isNullOrEmpty(formPage.orderCode)){
        	formPage = dealFormWithOrderCode(formPage);
        }
        if("3".equals(adminUser.getAdminType())){//管理员类型 1普通用户 2外部用户 3商铺管理员
        	formPage.fromUid = adminUser.getUid();
		}
        //根据现有条件获取订单总量
        Long totals = orderProductService.getTotalsWithForm(formPage);
        //根据现有条件获取订单集合,分页结果
        List<OrderVO> orderVoList = orderProductService.queryOrderProIdList(formPage);
     	List<Fromsite> fromsiteList  = productService.queryAllFromsite();
     	List<AdminUser> adminUserList  = adminUserService.queryAllLyAdminUser();		//查找出所有的联营商户
     	int totalPage = (int) (totals%formPage.size==0?totals/formPage.size:totals/formPage.size+1);
		return ok(views.html.order.orderManage.render(adminUser,orderVoList,totals,formPage.page,totalPage,
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
     * <p>Title: orderManage</p> 
     * <p>Description: 获取订单信息</p> 
     * @return
     */
    @AdminAuthenticated
    public Result orderManageCompany(){
    	AdminUser adminUser =getCurrentAdminUser();
    	ServiceFactory.getCacheService().set(Constants.queryOrderRecordWithUser_KEY+adminUser.getId(), request().uri());
    	Form<OrderForm> form = orderForm.bindFromRequest();
    	OrderForm formPage = new OrderForm();
    	if (!form.hasErrors()) {
    		formPage = form.get();
    	}
    	
    	if(!Strings.isNullOrEmpty(formPage.keyword)){
    		formPage = dealFormWithKeyword(formPage);
    	}
    	if(!Strings.isNullOrEmpty(formPage.orderCode)){
    		formPage = dealFormWithOrderCode(formPage);
    	}
    	if("3".equals(adminUser.getAdminType())){//管理员类型 1普通用户 2外部用户 3商铺管理员
    		formPage.fromUid = adminUser.getUid();
    	}
    	
    	//根据现有条件获取订单集合,分页结果
    	OrderCompanyPageVO orderCompanyPageVO = orderProductService.queryOrderProIdCompanyList(formPage);
    	//根据现有条件获取订单总量
    	Long totals = orderCompanyPageVO.totals;//orderProductService.getTotalsWithForm(formPage);
    	List<OrderVO> orderVoList = orderCompanyPageVO.orderVOList;
    	List<Fromsite> fromsiteList  = productService.queryAllFromsite();
    	int totalPage = (int) (totals%formPage.size==0?totals/formPage.size:totals/formPage.size+1);
    	return ok(views.html.order.orderManageCompany.render(adminUser,orderVoList,totals,formPage.page,totalPage,
    			Html.apply(Constants.Typs.typs2HTML(Integer.parseInt(formPage.typ))),
    			Html.apply(PayMethod.status2HTML(formPage.payMethod)),
    			Html.apply(Constants.OrderProFlg.orderProFlg2HTML(formPage.orderProFlg)),
    			Html.apply(LoveLyStatus.status2HTML(formPage.ordertype)),
    			Html.apply(ProductService.fromsiteList2Html(fromsiteList,Integer.parseInt(formPage.fromsite)))));
    }

	/**
	 * 
	 * <p>Title: dealFormWithOrderCode</p> 
	 * <p>Description: 对查询条件orderCode进行处理</p> 
	 * @param formPage
	 * @return
	 */
    @AdminAuthenticated()
	private OrderForm dealFormWithOrderCode(OrderForm form) {
		//处理订单号和顺丰单号
		 if(!Strings.isNullOrEmpty(form.orderCode)){
			 String typ = form.typ;
			 if("90".equals(typ)||"0".equals(typ)){
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
	private OrderForm dealFormWithKeyword(OrderForm form) {
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
	 * <p>Title: dealOrder</p> 
	 * <p>Description: 处理相应的代下单订单</p> 
	 * @return
	 */
    @AdminAuthenticated()
	public Result dealOrder(){
		String soId = AjaxHelper.getHttpParam(request(), "soId");
		String sopIds = AjaxHelper.getHttpParam(request(), "sopIds");
		String accounts = dealCommonWithAccount2Html("");
		String creditcards = dealCommonWithCreditcard2Html("");
		String currencys = dealCommonWithCurrency2Html("");
		ShoppingOrder  shoppingOrder  = new ShoppingOrder ();
		List<OrderProduct> orderProductList = new ArrayList<OrderProduct>();
		if(!Strings.isNullOrEmpty(soId)){
			shoppingOrder = orderService.queryShoppingOrderById(Long.parseLong(soId));
			if(!Strings.isNullOrEmpty(sopIds)){
				String[] sopIdsTemp = sopIds.split(",");
				for (String sopId : sopIdsTemp) {
					OrderProduct orderProduct = new OrderProduct();
					orderProduct = orderProductService.queryOrderProductById(Long.parseLong(sopId));
					Product product = productService.findProductById(orderProduct.getpId());
					product.setFromsiteName(productService.queryFromsiteById(product.getFromsite()).getName());
					orderProduct.setProduct(product);
					orderProductList.add(orderProduct);
				}
			}
		}
		return ok(views.html.order.dealOrder.render(shoppingOrder,orderProductList,soId,sopIds,Html.apply(accounts),Html.apply(creditcards),Html.apply(currencys)));
	}
	
	/**
	 * 
	 * <p>Title: dealCommonWithCurrency2Html</p> 
	 * <p>Description: 用于处理汇率信息，用于前段显示金额  格式：货币符合 ： 汇率 相对于一元人民币</p> 
	 * @param currency
	 * @return
	 */
    @AdminAuthenticated()
	private String dealCommonWithCurrency2Html(String value) {
		List<Currency> currencys = currencyService.findAll();
		StringBuilder sb = new StringBuilder();
        for (Currency m : currencys) {
            if (value!=null&&value.equals(m.getId()+"")) {
                sb.append(Htmls.generateSelectedOption(m.getId(),
                        m.getName()+":"+Numbers.formatDouble(m.getRate()/100.0,"#0.0000")));
            } else {
                sb.append(Htmls.generateOption(m.getId(), m.getName()+":"+Numbers.formatDouble(m.getRate()/100.0,"#0.0000")));
            }
        }
        return sb.toString();
	}
	
	/**
	 * 
	 * <p>Title: dealCommonWithCreditcard2Html</p> 
	 * <p>Description: 处理信用卡账号信息，用于前端选择显示，格式：id : 卡号</p> 
	 * @param creditcard
	 * @return
	 */
    @AdminAuthenticated()
	private String dealCommonWithCreditcard2Html(String value) {
		List<Creditcard> creditcards = creditcardService.findAll();
		StringBuilder sb = new StringBuilder();
        for (Creditcard m : creditcards) {
            if (value!=null&&value.equals(m.getId()+"")) {
                sb.append(Htmls.generateSelectedOption(m.getId(),
                        m.getCreditcardID()));
            } else {
                sb.append(Htmls.generateOption(m.getId(), m.getCreditcardID()));
            }
        }
        return sb.toString();
	}
	
	/**
	 * 
	 * <p>Title: dealCommonWithAccount2Html</p> 
	 * <p>Description: 处理下单账号信息，用于前段选择，格式为id : name</p> 
	 * @param value
	 * @return
	 */
    @AdminAuthenticated()
	public String dealCommonWithAccount2Html(String value) {
		List<Account> accounts = accountService.findAll();
		StringBuilder sb = new StringBuilder();
        for (Account m : accounts) {
            if (value!=null&&value.equals(m.getId()+"")) {
                sb.append(Htmls.generateSelectedOption(m.getId(),
                        m.getAccountNo()));
            } else {
                sb.append(Htmls.generateOption(m.getId(), m.getAccountNo()));
            }
        }
        return sb.toString();
	}
	
	/**
	 * 
	 * <p>Title: saveOrder</p> 
	 * <p>Description: 保存订单相关信息，并生成相应的包裹</p> 
	 * @return
	 */
	@AdminAuthenticated
	public Result saveOrder(){
		AdminUser adminUser = getCurrentAdminUser();
		Form<OrderForm> form = orderForm.bindFromRequest();
		OrderForm formPage = new OrderForm();
        if (!form.hasErrors()) {
        	formPage = form.get();
        }
        //获取相应的商品订单内容
        ShoppingOrder shoppingOrder = orderService.queryShoppingOrderById(formPage.soId);
        List<OrderProduct> orderProducts = orderProductService.getOrderProductListByIds(formPage.sopIds);
        //生成包裹
        Parcels parcels = parcelsService.saveParcelsWithForm(shoppingOrder , orderProducts,formPage);
        if(!shoppingOrder.getName().equals(formPage.name)||!shoppingOrder.getPhone().equals(formPage.phone)||!shoppingOrder.getAddress().equals(formPage.address)){
			operateLogService.saveParcelsLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "更新包裹收货人信息,原始信息（收货人-电话-地址）："+shoppingOrder.getName()+"-"+shoppingOrder.getPhone()+"-"+shoppingOrder.getAddress()+",更新后收货人信息为（收货人-电话-地址）:"+parcels.getName()+"-"+parcels.getPhone()+"-"+parcels.getAddress());
		}
        operateLogService.saveParcelsLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "处理订单生成包裹,保存后的包裹ID为:"+parcels.getParcelCode());
        logger.info("生产包裹信息完成，包裹号："+parcels.getParcelCode());
        //添加包裹商品
        parcelsProService.saveParcelsProWithForm(parcels,orderProducts,formPage);
        logger.info("生产包裹商品信息完成，对应包裹号："+parcels.getParcelCode());
        //添加物流信息，pardels_Waybill
        ParcelsWaybill pwb = parcelsWaybillService.save(parcels,"商品已经在官网下单",0);
        operateLogService.saveWaybillLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "代下单包裹物流,包裹号为:"+parcels.getParcelCode()+",物流信息ID:"+pwb.getId());
        //更新订单状态 
        orderProductService.updateOrderProductFlg(orderProducts,"1");
        //发送短信提醒,模板ID:776397
        if("1".equals(formPage.traffic)){
        	String fromsite = "";
        	fromsite = orderService.getFromsietWithOrderCode(parcels.getOrderId());
        	smsService.saveSmsInfo("#name#="+fromsite+"&#code#="+shoppingOrder.getOrderCode(), shoppingOrder.getPhone(), "776393", "1");
        }else if("2".equals(formPage.traffic)){
        	smsService.saveSmsInfo("#code#="+shoppingOrder.getOrderCode(), shoppingOrder.getPhone(), "776699", "1");
        }
    	String requestUri = ServiceFactory.getCacheService().get(Constants.queryOrderRecordWithUser_KEY+adminUser.getId());
		if(!Strings.isNullOrEmpty(requestUri)){
			return redirect(requestUri);
		}
		return redirect("/order/orderManage");
	}
	
	/**
	 * 
	 * <p>Title: viewOrder</p> 
	 * <p>Description: 查看订单详情</p> 
	 * @return
	 */
	@AdminAuthenticated()
	public Result viewOrder(){
		String soId = AjaxHelper.getHttpParam(request(), "soId");
		ShoppingOrder  shoppingOrder  = new ShoppingOrder ();
		List<OrderProduct> orderProducts = new ArrayList<OrderProduct>();
		List<ProductGroup> productGroups = new ArrayList<ProductGroup>();
		if(!Strings.isNullOrEmpty(soId)){
			shoppingOrder = orderService.queryShoppingOrderById(Long.parseLong(soId));
			orderProducts = orderProductService.getAllByShoppingOrderId(shoppingOrder.getId());
			if(orderProducts!=null&&orderProducts.size()>0){
				for (OrderProduct orderProduct : orderProducts) {
					Product product = productService.findProductById(orderProduct.getpId());
					product.setFromsiteName(productService.queryFromsiteById(product.getFromsite()).getName());
					orderProduct.setProduct(product);
					if(product.getIshot()==1){
						List<ProductGroup> productGroup = productGroupService.findProductGroupListByPgId(product.getPid());
						for (ProductGroup productGroup2 : productGroup) {
							productGroup2.setProduct(productService.findProductById(productGroup2.getPid()));
							productGroup2.setBuyNum(orderProduct.getCounts()*productGroup2.getNum());
							productGroups.add(productGroup2);
						}
					}
				}
			}
		}
		return ok(views.html.order.viewOrder.render(shoppingOrder,orderProducts,productGroups,soId));
	}
	/**
	 * 
	 * <p>Title: viewOrder</p> 
	 * <p>Description: 修改订单收货人信息</p> 
	 * @return
	 */
	@AdminAuthenticated()
	public Result updateOrder(){
		Form<OrderForm> form = orderForm.bindFromRequest();
		OrderForm formPage = new OrderForm();
        if (!form.hasErrors()) {
        	formPage = form.get();
        }
        //更新相应的商品订单内容，主要为收货人信息
        ShoppingOrder shoppingOrder = orderService.queryShoppingOrderById(formPage.soId);
        if(shoppingOrder!=null){
        	//判断订单下的商品是否已处理，如果存在已处理，则不允许修改
        	List<OrderProduct> orderProducts = orderProductService.getAllByShoppingOrderId(shoppingOrder.getId());
        	boolean flag = true;
        	for (OrderProduct orderProduct : orderProducts) {
				if(!"0".equals(orderProduct.getFlg())){
					flag = false;//证明存在订单已处理或申请退款
					break;
				}
			}
        	if(flag){
        		if(!shoppingOrder.getName().equals(formPage.name)||!shoppingOrder.getPhone().equals(formPage.phone)||!shoppingOrder.getAddress().equals(formPage.address)){
        			AdminUser adminUser = getCurrentAdminUser();
        			operateLogService.saveOrderLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "更新订单收货人信息,原始信息（收货人-电话-地址）："+shoppingOrder.getName()+"-"+shoppingOrder.getPhone()+"-"+shoppingOrder.getAddress()+",更新后收货人信息为（收货人-电话-地址）:"+formPage.name+"-"+formPage.phone+"-"+formPage.address);
        		}
        		ShoppingOrder tempShoppingOrder = orderService.updateOrderWithForm(shoppingOrder, formPage);
        		logger.info("更新订单收货人信息完成，订单号："+shoppingOrder.getOrderCode());
        	}else{
        		logger.info("更新订单收货人信息失败，订单号："+shoppingOrder.getOrderCode()+"，原因：此订单下存在商品已处理或已申请退款");
        	}
        }
		return redirect("/order/viewOrder?soId="+formPage.soId);
	}
	/**
	 * 
	 * <p>Title: viewOrder</p> 
	 * <p>Description: 申请退款</p> 
	 * @return
	 */
	@AdminAuthenticated()
	public Result refundOrder(){
		String soId = AjaxHelper.getHttpParam(request(), "soId");
		String sopIds = AjaxHelper.getHttpParam(request(), "sopIds");
		if(!Strings.isNullOrEmpty(soId)&&!Strings.isNullOrEmpty(sopIds)){
			orderProductService.refundOrder(soId, sopIds);
			AdminUser adminUser = getCurrentAdminUser();
			operateLogService.saveOrderLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "申请退款，订单ID:"+soId+",订单商品ID:"+sopIds);
		}
		return redirect("/order/orderManage");
	}
	/**
	 * 
	 * <p>Title: updateOrderWithPaystat</p> 
	 * <p>Description:修改商品支付状态</p> 
	 * @return
	 */
	@AdminAuthenticated()
	public Result updateOrderWithPaystat(){
		String soId = AjaxHelper.getHttpParam(request(), "soId");
		String sopIds = AjaxHelper.getHttpParam(request(), "sopIds");
		String paystat = AjaxHelper.getHttpParam(request(), "paystat");
		if(!Strings.isNullOrEmpty(soId)&&!Strings.isNullOrEmpty(sopIds)){
			orderProductService.updateOrderWithPaystat(soId, sopIds, paystat);
			AdminUser adminUser = getCurrentAdminUser();
			operateLogService.saveOrderLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "更新支付状态，订单ID:"+soId+",订单商品ID:"+sopIds+",支付状态："+paystat);
		}
		AdminUser adminUser =getCurrentAdminUser();
		String requestUri = ServiceFactory.getCacheService().get(Constants.queryOrderRecordWithUser_KEY+adminUser.getId());
		if(!Strings.isNullOrEmpty(requestUri)){
			return redirect(requestUri);
		}
		return redirect("/order/orderManage");
	}
	
}
