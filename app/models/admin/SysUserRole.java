package models.admin;

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
import javax.validation.constraints.NotNull;

/**
 * 角色与管理员关联表
 * @author luobotao
 * @Date 2015年9月11日
 */
@Entity
@Table(name="sys_user_role")
public class SysUserRole implements Serializable{
	
	private static final long serialVersionUID = 6495222698899022704L;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
    
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateAdd;
	
    @NotNull
    @Column(name="roleid",columnDefinition=" int(11)  DEFAULT 0 ")
    private Integer roleid;//角色ID
    
    @Column(name="userid",columnDefinition=" int(11)  DEFAULT 0 ")
    private Long userid;//管理员ID

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getDateAdd() {
		return dateAdd;
	}

	public void setDateAdd(Date dateAdd) {
		this.dateAdd = dateAdd;
	}

	public Integer getRoleid() {
		return roleid;
	}

	public void setRoleid(Integer roleid) {
		this.roleid = roleid;
	}

	public Long getUserid() {
		return userid;
	}

	public void setUserid(Long userid) {
		this.userid = userid;
	}

}
