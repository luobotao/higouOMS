package models;

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

import models.admin.AdminUser;
import models.product.Product;

/**
 * 包裹与商品关联实体
 * 
 * @author luobotao Date: 2015年4月20日 下午2:13:08
 */
@Entity
@Table(name = "pardels_Pro")
public class ParcelsPro implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5570699483908169832L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;

	@Column(nullable = false, name = "pardelsId")
	private Long parcelsId;
	@Column(nullable = false)
	private Long shopProId;
	@Column(nullable = false)
	private Long pId;

	@Column(nullable = true, columnDefinition = "decimal(9,2) DEFAULT NULL ")
	private Double price;

	private int counts;

	@Column(nullable = true, columnDefinition = "decimal(9,2) DEFAULT NULL ")
	private Double totalFee;

	@Transient
	private Parcels parcels;
	
	@Transient
	private Product product;
	
	@Transient
	private ShoppingOrder shoppingOrder;
	
	@Transient
	private AdminUser adminUser;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDate_add() {
		return date_add;
	}

	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}

	public Long getParcelsId() {
		return parcelsId;
	}

	public void setParcelsId(Long parcelsId) {
		this.parcelsId = parcelsId;
	}

	public Long getShopProId() {
		return shopProId;
	}

	public void setShopProId(Long shopProId) {
		this.shopProId = shopProId;
	}

	public Long getpId() {
		return pId;
	}

	public void setpId(Long pId) {
		this.pId = pId;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}


	public int getCounts() {
		return counts;
	}

	public void setCounts(int counts) {
		this.counts = counts;
	}

	public Double getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(Double totalFee) {
		this.totalFee = totalFee;
	}

	public Parcels getParcels() {
		return parcels;
	}

	public void setParcels(Parcels parcels) {
		this.parcels = parcels;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public AdminUser getAdminUser() {
		return adminUser;
	}

	public void setAdminUser(AdminUser adminUser) {
		this.adminUser = adminUser;
	}

	public ShoppingOrder getShoppingOrder() {
		return shoppingOrder;
	}

	public void setShoppingOrder(ShoppingOrder shoppingOrder) {
		this.shoppingOrder = shoppingOrder;
	}

	
}
