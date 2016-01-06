package forms.order;


import java.util.Date;

public class RefundForm {
	
	public Integer page = 0;
	public Integer size = 20;
	
	public String type;				//微信 wxapp webweb 支付宝 alipay
	public String refundSuccessFee;
	public String batchNo;
	public String batchNum;
	public String detailData;
	public String successNum;
	public String resultDetails;

	public String paymethod;					//支付方式
	public String orderCode;					//订单号
	public String refundFee;					//退款金额
	public String tradeNo;
	public String status="0";						//0 新建 1 成功
	public Date dateAdd;						//更新时间				
	public Date dateSucc;						//更新时间
	public String operator;					//操作人
	public String refundReason;						//退款原因
	public String memo;						//备注
	public String spid;						//订单商品id
	public String nums;						//数量
	public String calRefundFee;				//计算的退款金额
	public String productPrice;				//商品价格，计算出的
	public String couponPrice;					//优惠券价值
	public String walletRefundFee;				//钱包退款金额
	
	public String sopIds;					//退款商品列表
	public String soId;						//订单id
	public String url;						
}
