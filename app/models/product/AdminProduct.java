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

/**
 * 管理员与商品的中间表
 * 
 * @author luobotao Date: 2015年4月16日 上午9:59:24
 */
@Entity
@Table(name = "adminproduct")
public class AdminProduct  implements Serializable{

	private static final long serialVersionUID = 3726824079178442492L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;
	
	@Column(columnDefinition = "int(11) DEFAULT 0 ")
	private Long adminid;//管理员ID
	
	@Column(columnDefinition = "int(11) DEFAULT 1 ")
	private Long pid;// 该管理员创建的商品ID

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getDate_add() {
		return date_add;
	}

	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}

	public Long getAdminid() {
		return adminid;
	}

	public void setAdminid(Long adminid) {
		this.adminid = adminid;
	}

	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}
	
	
}
