package models.admin;

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
 * 管理员实体
 * @author luobotao
 * Date: 2015年4月14日 下午4:37:39
 */
@Entity
@Table(name="admin")
public class AdminUser implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1463875347895448194L;

	@Id
	@GeneratedValue
	private Long id;
	
	@Column(length = 16,nullable=false,unique=true)
	private String username;
	
	@Column(length = 16)
	private String realname;
	
	@Column(length = 32,nullable=false)
	private String passwd;
	
	@Column(nullable=false,columnDefinition="tinyint(1) DEFAULT 1 ")
	private int active;//0停用 1启用
	
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;
	
	@Column(length = 32)
	private String lastip;
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_login;
	
	@Column(columnDefinition="int(10)  DEFAULT 0 ")
	private int login_count;
	
	@Column(length = 256)
	private String headIcon;
	
	@Column(name="adminType",columnDefinition="varchar(10) DEFAULT '1' ")
	private String adminType;//管理员类型 1普通用户 2外部用户(联营) 3商铺管理员
	
	@Column(name="uid",columnDefinition="int(11)  DEFAULT 0 ")
	private Long uid;//当adminType为3（商铺管理员）的时候，需要用用户表`user`加一个映射
	
	@Column(name="roleId",columnDefinition="int(10)  DEFAULT 0 ")
	private int roleId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public int getActive() {
		return active;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public Date getDate_add() {
		return date_add;
	}

	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}

	public String getLastip() {
		return lastip;
	}

	public void setLastip(String lastip) {
		this.lastip = lastip;
	}

	public Date getDate_login() {
		return date_login;
	}

	public void setDate_login(Date date_login) {
		this.date_login = date_login;
	}

	public int getLogin_count() {
		return login_count;
	}

	public void setLogin_count(int login_count) {
		this.login_count = login_count;
	}

	public String getHeadIcon() {
		return headIcon;
	}

	public void setHeadIcon(String headIcon) {
		this.headIcon = headIcon;
	}

	public int getRoleId() {
		return roleId;
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}

	public String getAdminType() {
		return adminType;
	}

	public void setAdminType(String adminType) {
		this.adminType = adminType;
	}

	public Long getUid() {
		return uid;
	}

	public void setUid(Long uid) {
		this.uid = uid;
	}

	
	
}
