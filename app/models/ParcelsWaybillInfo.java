package models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * 包裹运单详细物流实体
 * 快递100返回
 */
@Entity
@Table(name = "pardels_Waybill_info")
public class ParcelsWaybillInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8611234270321297022L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;

	@Column(nullable = false)
	private Long wayBillId;

	private String date_txt;
	private String remark;
	private int nsort;
	private String src;
	
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
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getDate_txt() {
		return date_txt;
	}
	public void setDate_txt(String date_txt) {
		this.date_txt = date_txt;
	}
	public int getNsort() {
		return nsort;
	}
	public void setNsort(int nsort) {
		this.nsort = nsort;
	}
	public Long getWayBillId() {
		return wayBillId;
	}
	public void setWayBillId(Long wayBillId) {
		this.wayBillId = wayBillId;
	}
	public String getSrc() {
		return src;
	}
	public void setSrc(String src) {
		this.src = src;
	}

}
