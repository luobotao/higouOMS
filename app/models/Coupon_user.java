package models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * 优惠券实体
 * 
 * @author luobotao
 * @Date 2015年5月5日
 */
@Entity
@Table(name = "coupon_user")
public class Coupon_user implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5461842524592997438L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;

	@Column(columnDefinition = "int(11) DEFAULT 1 ")
	private Long couponId;//
	@Column(columnDefinition = "varchar(32) DEFAULT ''")
	private String coupon_code;//
	@Column(columnDefinition = "int(11) DEFAULT 0")
	private Long uid;//
	@Lob
	private String usetim;//
	@Column(columnDefinition = "varchar(2) DEFAULT '0'")
	private String states;//
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
	public Long getCouponId() {
		return couponId;
	}
	public void setCouponId(Long couponId) {
		this.couponId = couponId;
	}
	public String getCoupon_code() {
		return coupon_code;
	}
	public void setCoupon_code(String coupon_code) {
		this.coupon_code = coupon_code;
	}
	public Long getUid() {
		return uid;
	}
	public void setUid(Long uid) {
		this.uid = uid;
	}
	public String getUsetim() {
		return usetim;
	}
	public void setUsetim(String usetim) {
		this.usetim = usetim;
	}
	public String getStates() {
		return states;
	}
	public void setStates(String states) {
		this.states = states;
	}
	

}
