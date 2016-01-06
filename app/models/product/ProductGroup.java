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
import javax.persistence.Transient;

/**
 * 组合商品表
 * @author luobotao
 * @Date 2015年10月9日
 */
@Entity
@Table(name = "product_group")
public class ProductGroup implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2913375510624628500L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Column
	private Long pgid;	//商品编号
	@Column
	private Long pid;	//商品编号
	private int num;
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;	//添加日期
	@Column(name="`date_upd`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_upd;	//更新日期
	
	@Transient
	private Product product;
	@Transient
	private int buyNum;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getPgid() {
		return pgid;
	}
	public void setPgid(Long pgid) {
		this.pgid = pgid;
	}
	public Long getPid() {
		return pid;
	}
	public void setPid(Long pid) {
		this.pid = pid;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public Date getDate_add() {
		return date_add;
	}
	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}
	public Date getDate_upd() {
		return date_upd;
	}
	public void setDate_upd(Date date_upd) {
		this.date_upd = date_upd;
	}
	public Product getProduct() {
		return product;
	}
	public void setProduct(Product product) {
		this.product = product;
	}
	public int getBuyNum() {
		return buyNum;
	}
	public void setBuyNum(int buyNum) {
		this.buyNum = buyNum;
	}
	
}
