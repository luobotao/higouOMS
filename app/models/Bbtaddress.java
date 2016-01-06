package models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 地址字典表
 * @author luobotao
 * @Date 2015年9月7日
 */
@Entity
@Table(name = "bbtaddress")
public class Bbtaddress  implements Serializable{

	private static final long serialVersionUID = 1728047600072559064L;
	@Id
	@GeneratedValue
	@Column(name="id")
	private Integer id;
	@Column(name="`parentid`")
	private Integer parentid;
	@Column(name="`code`")
	private String code;
	@Column(name="`name`")
	private String name;
	@Column(name="`tier`")
	private String tier;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getParentid() {
		return parentid;
	}
	public void setParentid(Integer parentid) {
		this.parentid = parentid;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTier() {
		return tier;
	}
	public void setTier(String tier) {
		this.tier = tier;
	}

}
