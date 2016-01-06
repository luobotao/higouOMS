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
import javax.persistence.Transient;

/**
 * 
 * <p>Title: Account.java</p> 
 * <p>Description: 下单账号实体类</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年7月30日  上午11:21:07
 * @version
 */
@Entity
@Table(name = "account")
public class Account implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name="id")
	private Integer id;
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;
	private String accountNo;
	@Column(columnDefinition = " Int(11) DEFAULT 0 ")
	private Long adminId;
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
	public String getAccountNo() {
		return accountNo;
	}
	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}
	public Long getAdminId() {
		return adminId;
	}
	public void setAdminId(Long adminId) {
		this.adminId = adminId;
	}
	
}