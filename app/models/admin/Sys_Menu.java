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
 * 系统菜单表
 * @author luobotao
 * @Date 2015年9月11日
 */
@Entity
@Table(name="sysMenu")
public class Sys_Menu implements Serializable{

	private static final long serialVersionUID = 2675871632493721950L;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
    
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateAdd;
	
    @Column(name="parentMenuId",columnDefinition=" int(11)  DEFAULT 0 ")
    private Integer parentMenuId;//父菜单ID
    
    @Column(name="menuOrder",columnDefinition=" int(5)  DEFAULT 0 ")
    private Integer menuOrder;//菜单顺序
    @NotNull
    @Column(name="menuName",columnDefinition="varchar(256) DEFAULT '' ")
    private String menuName;//菜单名称
    
    @Column(name="menuUrl",columnDefinition="varchar(512) DEFAULT '' ")
    private String menuUrl;//菜单对应的链接
    
    @Column(name="isLeaf",nullable=false,columnDefinition="tinyint(1) DEFAULT 1 ")
    private int isLeaf;//是否子节点,若为子节点，则parentMenuId需有值 1是0否
    @Column(name="isVisible",nullable=false,columnDefinition="tinyint(1) DEFAULT 1 ")
    private int isVisible;//是否显示 1是0否 (暂时未用)

    @Column(name="isButton",nullable=false,columnDefinition="tinyint(1) DEFAULT 0 ")
    private int isButton;//是否按钮 1是0否
    

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getParentMenuId() {
		return parentMenuId;
	}

	public void setParentMenuId(Integer parentMenuId) {
		this.parentMenuId = parentMenuId;
	}

	public Date getDateAdd() {
		return dateAdd;
	}

	public void setDateAdd(Date dateAdd) {
		this.dateAdd = dateAdd;
	}

	public Integer getMenuOrder() {
		return menuOrder;
	}

	public void setMenuOrder(Integer menuOrder) {
		this.menuOrder = menuOrder;
	}

	public String getMenuName() {
		return menuName;
	}

	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}

	public String getMenuUrl() {
		return menuUrl;
	}

	public void setMenuUrl(String menuUrl) {
		this.menuUrl = menuUrl;
	}

	public int getIsLeaf() {
		return isLeaf;
	}

	public void setIsLeaf(int isLeaf) {
		this.isLeaf = isLeaf;
	}

	public int getIsVisible() {
		return isVisible;
	}

	public void setIsVisible(int isVisible) {
		this.isVisible = isVisible;
	}

	public int getIsButton() {
		return isButton;
	}

	public void setIsButton(int isButton) {
		this.isButton = isButton;
	}

}
