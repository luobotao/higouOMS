package models.product;

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
@Entity
@Table(name = "channel")
public class Channel  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1917956642328391096L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="`date_add`")
	private Date date_add;
	@Column(columnDefinition = "varchar(256) ")
	private String cname;//频道名称
	@Column(columnDefinition = "int(11) DEFAULT 0 ")
	private int nsort;// 排序
	@Column(columnDefinition = "varchar(2) ")
	private String typ;
	@Column(columnDefinition = "varchar(2) ")
	private String sta;
	@Column(columnDefinition = "varchar(256) ")
	private String category;
	@Column(columnDefinition = "varchar(32) DEFAULT '2014-01-01 00:00:00' ")
	private String tag;//
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
	public String getCname() {
		return cname;
	}
	public void setCname(String cname) {
		this.cname = cname;
	}
	public int getNsort() {
		return nsort;
	}
	public void setNsort(int nsort) {
		this.nsort = nsort;
	}
	public String getTyp() {
		return typ;
	}
	public void setTyp(String typ) {
		this.typ = typ;
	}
	public String getSta() {
		return sta;
	}
	public void setSta(String sta) {
		this.sta = sta;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	
}
