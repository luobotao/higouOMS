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
 * 商品品类实体
 * 
 * @author luobotao Date: 2015年4月16日 上午9:59:24
 */
@Entity
@Table(name = "category_new")
public class Category  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2430635196525818802L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;
	@Column(columnDefinition = "varchar(32) ")
	private String name;//品类名称
	@Column(columnDefinition = "int(11) DEFAULT 0 ")
	private int parentid;
	@Column(columnDefinition = "varchar(16) ")
	private String typecode;
	@Column(columnDefinition = "int(11) DEFAULT 1 ")
	private int sort;// 1正常 2置顶
	@Column(columnDefinition = "int(11) DEFAULT 1 ")
	private int leval;
	@Column(columnDefinition = "varchar(10) ")
	private String typecodenew;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getParentid() {
		return parentid;
	}
	public void setParentid(int parentid) {
		this.parentid = parentid;
	}
	public String getTypecode() {
		return typecode;
	}
	public void setTypecode(String typecode) {
		this.typecode = typecode;
	}
	public int getSort() {
		return sort;
	}
	public void setSort(int sort) {
		this.sort = sort;
	}
	public int getLeval() {
		return leval;
	}
	public void setLeval(int leval) {
		this.leval = leval;
	}
	public String getTypecodenew() {
		return typecodenew;
	}
	public void setTypecodenew(String typecodenew) {
		this.typecodenew = typecodenew;
	}

}
