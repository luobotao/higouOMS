package models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * 
 * <p>Title: RefundInfo.java</p> 
 * <p>Description: 订单退款记录</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年11月9日  上午11:20:29
 * @version
 */
@Entity
@Table(name = "refund")
public class RefundInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6881239945444620545L;

	@Id
	@GeneratedValue
	private Long id;
	//支付宝退款信息
	@Column(name="`refund_success_fee`")
	private String refundSuccessFee;			//退款金额
	@Column(name="`batch_no`")
	private String batchNo;						//退款批次号
	@Column(name="`batch_num`")
	private String batchNum;					//退款笔数
	@Column(name="`detail_data`")
	private String detailData;					//退款明细 交易流水^退款金额^备注
	@Column(name="`success_data`")
	private String successNum;					//退款成功笔数
	@Column(name="`result_details`")
	private String resultDetails;				//退款结果明细
	@Column(name="`date_succ`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateSucc;						//更新时间
	//订单业务信息
	private String paymethod;					//支付方式
	private String orderCode;					//订单号
	@Column(name="`refund_fee`")
	private String refundFee;					//退款金额
	@Column(name="`trade_no`")					//退款流水
	private String tradeNo;
	private String status;						//0 新建 1 成功
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateAdd;						//更新时间				

	private String operator;					//操作人
	private String reason;						//退款原因
	private String memo;						//备注
	private String spid;						//订单商品id
	private String nums;						//数量
	@Column(name="`cal_refund_fee`")
	private String calRefundFee;				//计算的退款金额
	@Column(name="`product_price`")
	private String productPrice;				//商品价格，计算出的
	@Column(name="`coupon_price`")
	private String couponPrice;					//优惠券价值
	@Column(name="`wallet_refund_fee`")
	private String walletRefundFee;				//钱包退款金额

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getPaymethod() {
		return paymethod;
	}
	public void setPaymethod(String paymethod) {
		this.paymethod = paymethod;
	}
	public String getOrderCode() {
		return orderCode;
	}
	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}
	public String getRefundFee() {
		return refundFee;
	}
	public void setRefundFee(String refundFee) {
		this.refundFee = refundFee;
	}
	public String getTradeNo() {
		return tradeNo;
	}
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Date getDateAdd() {
		return dateAdd;
	}
	public void setDateAdd(Date dateAdd) {
		this.dateAdd = dateAdd;
	}
	public Date getDateSucc() {
		return dateSucc;
	}
	public void setDateSucc(Date dateSucc) {
		this.dateSucc = dateSucc;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public String getSpid() {
		return spid;
	}
	public void setSpid(String spid) {
		this.spid = spid;
	}
	public String getNums() {
		return nums;
	}
	public void setNums(String nums) {
		this.nums = nums;
	}
	public String getCalRefundFee() {
		return calRefundFee;
	}
	public void setCalRefundFee(String calRefundFee) {
		this.calRefundFee = calRefundFee;
	}
	public String getProductPrice() {
		return productPrice;
	}
	public void setProductPrice(String productPrice) {
		this.productPrice = productPrice;
	}
	public String getCouponPrice() {
		return couponPrice;
	}
	public void setCouponPrice(String couponPrice) {
		this.couponPrice = couponPrice;
	}
	public String getWalletRefundFee() {
		return walletRefundFee;
	}
	public void setWalletRefundFee(String walletRefundFee) {
		this.walletRefundFee = walletRefundFee;
	}
	public String getRefundSuccessFee() {
		return refundSuccessFee;
	}
	public void setRefundSuccessFee(String refundSuccessFee) {
		this.refundSuccessFee = refundSuccessFee;
	}
	public String getBatchNo() {
		return batchNo;
	}
	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}
	public String getBatchNum() {
		return batchNum;
	}
	public void setBatchNum(String batchNum) {
		this.batchNum = batchNum;
	}
	public String getDetailData() {
		return detailData;
	}
	public void setDetailData(String detailData) {
		this.detailData = detailData;
	}
	public String getSuccessNum() {
		return successNum;
	}
	public void setSuccessNum(String successNum) {
		this.successNum = successNum;
	}
	public String getResultDetails() {
		return resultDetails;
	}
	public void setResultDetails(String resultDetails) {
		this.resultDetails = resultDetails;
	}
	
}
