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
 * 角色与菜单关联表
 * @author luobotao
 * @Date 2015年9月11日
 */
@Entity
@Table(name="sys_role_menu")
public class SysRoleMenu implements Serializable{


	private static final long serialVersionUID = -1051894087801622571L;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
    
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateAdd;
	
    @NotNull
    @Column(name="roleid",columnDefinition=" int(11)  DEFAULT 0 ")
    private Integer roleid;//角色ID
    
    @Column(name="menuid",columnDefinition=" int(11)  DEFAULT 0 ")
    private Integer menuid;//菜单ID

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

	public Integer getMenuid() {
		return menuid;
	}

	public void setMenuid(Integer menuid) {
		this.menuid = menuid;
	}



}
