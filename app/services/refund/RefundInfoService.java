package services.refund;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.OrderProduct;
import models.Parcels;
import models.RefundInfo;
import models.ShoppingOrder;
import models.admin.AdminUser;
import models.product.Fromsite;
import models.user.UserBalance;
import models.user.UserBalanceLog;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import play.Logger;
import repositories.OrderProductRepository;
import repositories.Order.OrderRepository;
import repositories.Order.RefundInfoRepository;
import repositories.parcels.ParcelsRepository;
import services.OperateLogService;
import services.ServiceFactory;
import services.order.OrderProductService;
import services.order.OrderService;
import services.parcels.ParcelsService;
import services.product.ProductService;
import services.user.UserService;
import utils.Constants;
import utils.Dates;
import utils.JdbcOper;
import utils.Numbers;
import utils.StringUtil;
import vo.OrderVO;

import com.google.common.base.Strings;

import forms.order.OrderForm;
import forms.order.RefundForm;

/**
 * 
 * <p>Title: RefundInfoService.java</p> 
 * <p>Description: 退款</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年11月9日  下午12:20:02
 * @version
 */
@Named
@Singleton
public class RefundInfoService {

    private static final Logger.ALogger logger = Logger.of(RefundInfoService.class);
    public static final Integer DEFAULT_VAL = -1;
    @Inject
    private RefundInfoRepository refundInfoRepository;
    @Inject
    private OperateLogService operateLogService;
    @Inject
    private UserBalanceService userBalanceService;
    @Inject
    private OrderService orderService;
    @Inject
    private OrderProductService orderProductService;
    
    /**
     * 
     * <p>Title: saveRefundInfoWithForm</p> 
     * <p>Description: 保存退款记录</p> 
     * @param formPage
     * @param adminUser 
     * @return
     */
	public RefundInfo saveRefundInfoWithForm(RefundForm formPage, AdminUser adminUser) {
		RefundInfo refundInfo = refundInfoRepository.queryRefundInfoWithOrdercode(formPage.orderCode, 0);
		if(refundInfo==null){
			refundInfo = new RefundInfo();
			refundInfo.setPaymethod(formPage.paymethod);
			refundInfo.setOrderCode(formPage.orderCode);
			refundInfo.setRefundFee(formPage.refundFee);
			refundInfo.setTradeNo(formPage.tradeNo);
			refundInfo.setStatus(formPage.status);
			refundInfo.setDateAdd(new Date());
			refundInfo.setDateAdd(formPage.dateSucc);
			refundInfo.setReason(formPage.refundReason);
			refundInfo.setMemo(formPage.memo);
			refundInfo.setSpid(formPage.sopIds);
			String nums = String.valueOf(formPage.sopIds.split(",").length);
			refundInfo.setNums(nums);
			refundInfo.setCalRefundFee(formPage.calRefundFee);
			refundInfo.setProductPrice(formPage.productPrice);
			refundInfo.setCouponPrice(formPage.couponPrice);
			refundInfo.setWalletRefundFee(formPage.walletRefundFee);
		
			if("alipay".equals(formPage.type)){
				refundInfo.setBatchNo(formPage.batchNo);
				refundInfo.setBatchNum(formPage.batchNum);
				refundInfo.setDetailData(formPage.detailData);
			}else{
				refundInfo.setBatchNo("");
				refundInfo.setBatchNum("");
				refundInfo.setDetailData("");
			}
		}
		refundInfo.setOperator(adminUser.getUsername());
		refundInfo = refundInfoRepository.save(refundInfo);
		return refundInfo;
	}

	/**
	 * 
	 * <p>Title: updateRefundInfoWithAliPayInfo</p> 
	 * <p>Description: 根据alipay返回的退款信息更新入库</p> 
	 * @param notify_time	退款通知发送的时间
	 * @param batch_no		退款批次号 时间+订单号
	 * @param success_num	退款成功笔数
	 * @param result_details	退款结果明细  退手续费结果返回格式：交易号^退款金额^处理结果\$退费账号^退费账户ID^退费金额^处理结果；不退手续费结果返回格式：交易号^退款金额^处理结果。若退款申请提交成功，处理结果会返回“SUCCESS”。若提交失败，退款的处理结果中会有报错码
	 * @return
	 * @throws ParseException 
	 */
	public RefundInfo updateRefundInfoWithAliPayInfo(String notify_time,
			String batch_no, String success_num, String result_details) throws ParseException {
		SimpleDateFormat sdf=  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String[] resultDetailsStr = result_details.split("$");
		String ordercode = batch_no.substring(14);//格式，日期+订单号,获取相应的订单号
		RefundInfo refundInfo = refundInfoRepository.queryRefundInfoWithOrdercode(ordercode,0);
		if(resultDetailsStr!=null&&resultDetailsStr.length>0){
			String[] refundInfoStr = resultDetailsStr[0].split("\\^");//交易号，退款金额，处理结果
			if(refundInfoStr!=null&&refundInfoStr.length==3){
				if(refundInfo!=null){
					refundInfo.setResultDetails(result_details);
					refundInfo.setSuccessNum(success_num);
				}
				if("SUCCESS".equals(refundInfoStr[2])||"success".equals(refundInfoStr[2])){
					//订单退款成功
					String tranno = refundInfoStr[0];	//交易流水
					String refundFee = refundInfoStr[1];//退款金额
					refundInfo.setRefundSuccessFee(refundFee);
					refundInfo.setStatus("1");	//退款成功
				}else{
					logger.info("退款失败："+result_details);
					refundInfo.setStatus("2");//退款失败
				}
				refundInfo.setDateSucc(sdf.parse(notify_time));
				refundInfo = refundInfoRepository.save(refundInfo);
			}
		}
		return refundInfo;
	}

	/**
	 * 
	 * <p>Title: updateRefundOrderInfo</p> 
	 * <p>Description: </p> 
	 * @param refundInfo
	 */
	public void updateRefundOrderInfo(RefundInfo refundInfo){
		//退款成功，更新状态相应的状态
        //修改指定的商品为退款完成状态(table = `shopping_order_pro`)
		if("1".equals(refundInfo.getStatus())){
			logger.info("本次退款shopping_order_pro ids:==========>>>"+refundInfo.getSpid());
	        List<OrderProduct> orderProducts = orderProductService.getOrderProductListByIds(refundInfo.getSpid());
	       	orderProductService.updateOrderProductFlg(orderProducts,"3");
	        //修改订单列表， orderService.update();
	       	ShoppingOrder shoppingOrder = orderService.queryShoppingOrderByOrderCode(refundInfo.getOrderCode());
	       	if("1".equals(shoppingOrder.getUsewallet())){
	       		double usebalance = Numbers.parseDouble(shoppingOrder.getUsebalance(), 0.0);
	       		double refundwalletfee = Numbers.parseDouble(refundInfo.getWalletRefundFee(), 0.0);
	       		if(usebalance>0&&usebalance>=refundwalletfee){
	       			//将部分钱退入钱包，更改钱包余额(table = `shopping_order`)
	       			shoppingOrder.setUsebalance(String.valueOf(usebalance - refundwalletfee));
	       			logger.info("本次退款用户钱包入账："+refundwalletfee+",订单剩余钱包使用余额为："+(usebalance - refundwalletfee));
	       			orderService.save(shoppingOrder);
	       			ServiceFactory.getCacheService().setObject(Constants.shoppingOrder_KEY+shoppingOrder.getId(), shoppingOrder, 0);
	       			//变更钱包金额(table = `user_balance`)
	       		  	UserBalance userb=userBalanceService.getUserBalance(shoppingOrder.getuId());
	       		  	BigDecimal beforBalance = userb.getBalance();
	       	       	userb.setBalance(userb.getBalance().add(new BigDecimal(refundwalletfee)));
	       			userb.setRedFlag("1");
	       			userb = userBalanceService.saveUserBalance(userb);
	       			//记录钱包使用日志(table = `user_balance_log`)
	       			UserBalanceLog ubg = new UserBalanceLog();
	       			BigDecimal balance = new BigDecimal(refundwalletfee);
	       			ubg.setBalance(balance);
	       			ubg.setBeforBalance(beforBalance);
	       			ubg.setCurentBalance(userb.getBalance());
	       			ubg.setCreateTime(new Date());
	       			ubg.setFlg(1);
	       			ubg.setUserId(shoppingOrder.getuId());
	       			ubg.setPaycardno("");
	       			ubg.setUserName("");
	       			ubg.setRemark("钱包退款"
	       					+ balance.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "元");
	       			ubg=userBalanceService.addbalanceLog(ubg);
	       		}
	       	}
		}
	}

	/**
	 * 
	 * <p>Title: updateRefundInfoWithWxPayInfo</p> 
	 * <p>Description: 更新退款信息记录，针对微信退款</p> 
	 * @param refund_id
	 * @param refund_channel
	 * @param cash_refund_fee
	 * @return
	 */
	public RefundInfo updateRefundInfoWithWxPayInfo(RefundForm formPage, String refund_id,
			String refund_channel, String cash_refund_fee, String resultDetails) {
		SimpleDateFormat sdf=  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		RefundInfo refundInfo = refundInfoRepository.queryRefundInfoWithOrdercode(formPage.orderCode,0);
		if(!"".equals(refund_id)&&!"".equals(cash_refund_fee)){
			//订单退款成功
			refundInfo.setRefundSuccessFee(cash_refund_fee);
			refundInfo.setStatus("1");	//退款成功
		}
		refundInfo.setResultDetails(resultDetails);
		refundInfo.setDateSucc(new Date());
		refundInfo = refundInfoRepository.save(refundInfo);
		return refundInfo;
	}
}
