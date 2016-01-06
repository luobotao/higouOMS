package models.admin;

import java.awt.Menu;
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
import javax.validation.constraints.NotNull;

import com.google.common.collect.Lists;

/**
 * 角色表
 * @author luobotao
 * @Date 2015年9月11日
 */
@Entity
@Table(name="role")
public class Role implements Serializable{

	private static final long serialVersionUID = -6574291131752415829L;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
    
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateAdd;
	
    @NotNull
    @Column(name="role",columnDefinition="varchar(256) DEFAULT '' ")
    private String role;//菜单名称
    
    @Column(name="remark",columnDefinition="varchar(512) DEFAULT '' ")
    private String remark;//菜单对应的链接

    @Transient
    private List<Sys_Menu> menuList = Lists.newArrayList(); // 拥有菜单列表
    
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

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public List<Sys_Menu> getMenuList() {
		return menuList;
	}

	public void setMenuList(List<Sys_Menu> menuList) {
		this.menuList = menuList;
	}
    
   

}
