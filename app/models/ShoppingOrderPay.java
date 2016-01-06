package models;

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
 * <p>Title: ShoppingOrderPay.java</p> 
 * <p>Description: 订单支付属性</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年10月9日  上午10:02:55
 * @version
 */
@Entity
@Table(name = "shopping_OrderPay")
public class ShoppingOrderPay  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1080738944289731407L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add; //订单支付创建时间
	@Column
	private Long orderId;
	@Column
	private String orderCode;
	@Column
	private Double fee;
	@Column
	private String paymethod;
	
	@Column
	private Integer paystat;
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date paytime; //订单支付时间
	
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getDate_add() {
		return date_add;
	}
	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public String getOrderCode() {
		return orderCode;
	}
	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}
	public Double getFee() {
		return fee;
	}
	public void setFee(Double fee) {
		this.fee = fee;
	}
	public String getPaymethod() {
		return paymethod;
	}
	public void setPaymethod(String paymethod) {
		this.paymethod = paymethod;
	}
	public Integer getPaystat() {
		return paystat;
	}
	public void setPaystat(Integer paystat) {
		this.paystat = paystat;
	}
	public Date getPaytime() {
		return paytime;
	}
	public void setPaytime(Date paytime) {
		this.paytime = paytime;
	}
	public String getTradeno() {
		return tradeno;
	}
	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
	}
	@Column
	private String tradeno;
	
		
}
