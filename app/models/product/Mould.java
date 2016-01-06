package models.product;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * 卡片实体
 * 
 */
@Entity
@Table(name = "mould")
public class Mould implements Serializable{


	private static final long serialVersionUID = 2907216989850719550L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Column(name="`date_add`",nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;

	@Column( columnDefinition = "varchar(256) " ,name="mname")
	private String mname;
	@Column( columnDefinition = "varchar(2) " ,name="typ")
	private String typ;

	@Column( columnDefinition = "varchar(32) " ,name="structure")
	private String structure;
	@Lob
	private String stream;
	@Column( columnDefinition = "int(11) default 0" ,name="cnt")
	private Integer cnt;
	@Column( columnDefinition = "varchar(256) " ,name="img")
	private String img;
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
	public String getMname() {
		return mname;
	}
	public void setMname(String mname) {
		this.mname = mname;
	}
	public String getTyp() {
		return typ;
	}
	public void setTyp(String typ) {
		this.typ = typ;
	}
	public String getStructure() {
		return structure;
	}
	public void setStructure(String structure) {
		this.structure = structure;
	}
	public String getStream() {
		return stream;
	}
	public void setStream(String stream) {
		this.stream = stream;
	}
	public Integer getCnt() {
		return cnt;
	}
	public void setCnt(Integer cnt) {
		this.cnt = cnt;
	}
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}

}
