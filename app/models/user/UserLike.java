package models.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * 用户喜欢实体
 * 
 * @author luobotao
 *
 */
@Entity
@Table(name = "userlike")
public class UserLike implements Serializable {

	private static final long serialVersionUID = -5547283417399494237L;

	@Id
	@GeneratedValue
	private Long id;

	private Long uid;

	private Long pid;

	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;

	public Date getDate_add() {
		return date_add;
	}

	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUid() {
		return uid;
	}

	public void setUid(Long uid) {
		this.uid = uid;
	}

	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

}
