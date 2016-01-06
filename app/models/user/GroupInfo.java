package models.user;

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
 * 用户组表
 * @author luobotao
 * @Date 2015年9月2日
 */
@Entity
@Table(name = "groupInfo")
public class GroupInfo implements Serializable {

	private static final long serialVersionUID = 8813192890182846028L;

	@Id
	@GeneratedValue
	private Integer id;

	@Column(name="`gname`",columnDefinition = " varchar(32) DEFAULT '' ")
	private String gname;

	@Column(name="`flg`",columnDefinition = " char(1) DEFAULT '1' ")
	private String flg;

	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getGname() {
		return gname;
	}

	public void setGname(String gname) {
		this.gname = gname;
	}

	public String getFlg() {
		return flg;
	}

	public void setFlg(String flg) {
		this.flg = flg;
	}

	public Date getDate_add() {
		return date_add;
	}

	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}

	
}
