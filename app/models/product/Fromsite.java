package models.product;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * fromsite
 * 
 * @author luobotao
 *
 */
@Entity
@Table(name = "fromsite")
public class Fromsite implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -8274273798904696768L;

	@Id
	private Integer id;
	private String name;
	private String url;
	private String img;
	private Integer fee;
	private Integer addfee;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public Integer getFee() {
		return fee;
	}
	public void setFee(Integer fee) {
		this.fee = fee;
	}
	public Integer getAddfee() {
		return addfee;
	}
	public void setAddfee(Integer addfee) {
		this.addfee = addfee;
	}
	
}
