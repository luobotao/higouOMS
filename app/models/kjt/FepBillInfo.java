package models.kjt;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * 
 * <p>Title: FepBillInfo.java</p> 
 * <p>Description: 待购汇订单信息记录</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年10月13日  下午3:03:22
 * @version
 */
@Entity
@Table(name = "fep_bill_info")
public class FepBillInfo implements Serializable {

	private static final long serialVersionUID = 5067254946860451894L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;
	
	@Column(name="`fep_bill_id`")
	private int fepBillId;						//待购汇账单编号
	
	@Column(name="`purchasing_total_amount`")
	private double purchasingTotalAmount;		//渠道结算金额
	
	@Column(name="`order_ids`")
	private String orderIds;					//本次用于购汇的订单集合
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Date getDate_add() {
		return date_add;
	}
	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}
	public int getFepBillId() {
		return fepBillId;
	}
	public void setFepBillId(int fepBillId) {
		this.fepBillId = fepBillId;
	}
	public double getPurchasingTotalAmount() {
		return purchasingTotalAmount;
	}
	public void setPurchasingTotalAmount(double purchasingTotalAmount) {
		this.purchasingTotalAmount = purchasingTotalAmount;
	}
	public String getOrderIds() {
		return orderIds;
	}
	public void setOrderIds(String orderIds) {
		this.orderIds = orderIds;
	}
	
}
