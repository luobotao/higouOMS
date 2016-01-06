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
/*
 * 订单扩展属性表
 */
@Entity
@Table(name = "shopping_Order")
public class ShoppingOrderPro  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1080738944289731407L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add; //代言申请时间
	@Column
	private Long orderId;
	@Column
	private Long pid;
	@Column
	private Double price;
	@Column
	private Integer counts;
	
	@Column
	private Double totalFee;
	@Column(columnDefinition = " int(10) unsigned DEFAULT '0' ")
	private Integer flg;
	
	@Column(columnDefinition = " int(10) unsigned DEFAULT '0' ")
	private Integer cid;

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

	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Integer getCounts() {
		return counts;
	}

	public void setCounts(Integer counts) {
		this.counts = counts;
	}

	public Double getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(Double totalFee) {
		this.totalFee = totalFee;
	}

	public Integer getFlg() {
		return flg;
	}

	public void setFlg(Integer flg) {
		this.flg = flg;
	}

	public Integer getCid() {
		return cid;
	}

	public void setCid(Integer cid) {
		this.cid = cid;
	}
	
	
}
