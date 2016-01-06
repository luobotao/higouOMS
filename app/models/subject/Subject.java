package models.subject;

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
 * <p>Title: Subject.java</p> 
 * <p>Description: 专题实体类</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年8月9日  下午5:05:04
 * @version
 */
@Entity
@Table(name = "subject")
public class Subject implements Serializable {

	private static final long serialVersionUID = 5876492532367553920L;

	@Id
	@GeneratedValue
	private Long id;

	private String sname;

	private int nsort;
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSname() {
		return sname;
	}

	public void setSname(String sname) {
		this.sname = sname;
	}

	public int getNsort() {
		return nsort;
	}

	public void setNsort(int nsort) {
		this.nsort = nsort;
	}

	public Date getDate_add() {
		return date_add;
	}

	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}

	
}
