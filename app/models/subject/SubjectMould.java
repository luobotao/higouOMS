package models.subject;

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

import com.fasterxml.jackson.databind.node.ObjectNode;
@Entity
@Table(name = "subject_mould")
public class SubjectMould  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2568387943875286928L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Column(name="`date_add`",nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;
	@Column(nullable=false,name="mouldId",columnDefinition = "int(11) default 0")
	private Long mouldId;
	@Column(nullable=false,columnDefinition = "int(11) ")
	private Long sid;
	@Column(columnDefinition = "int(11) DEFAULT 0 ")
	private int nsort;// 排序
	@Column(columnDefinition = "varchar(256) ")
	private String title;//
	@Column(columnDefinition = "varchar(256) ")
	private String datetxt;//
	@Column(columnDefinition = "varchar(2) DEFAULT '0' ")
	private String flag;//1展示 0不展示
	@Column(columnDefinition = "varchar(32) ",name="sectionPic")
	private String sectionPic;
	@Column(columnDefinition = "varchar(2) DEFAULT '0' ",name="manType")
	private String manType;//
	private Date beginTime;//
	
	@Transient
	private List<ObjectNode> subjectMouldContent;//卡片内容（商品的一些信息等）
	@Transient
	private String mouldName;
	
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
	public Long getMouldId() {
		return mouldId;
	}
	public void setMouldId(Long mouldId) {
		this.mouldId = mouldId;
	}
	public int getNsort() {
		return nsort;
	}
	public void setNsort(int nsort) {
		this.nsort = nsort;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDatetxt() {
		return datetxt;
	}
	public void setDatetxt(String datetxt) {
		this.datetxt = datetxt;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public String getSectionPic() {
		return sectionPic;
	}
	public void setSectionPic(String sectionPic) {
		this.sectionPic = sectionPic;
	}
	public String getManType() {
		return manType;
	}
	public void setManType(String manType) {
		this.manType = manType;
	}
	public String getMouldName() {
		return mouldName;
	}
	public void setMouldName(String mouldName) {
		this.mouldName = mouldName;
	}
	public List<ObjectNode> getSubjectMouldContent() {
		return subjectMouldContent;
	}
	public void setSubjectMouldContent(List<ObjectNode> subjectMouldContent) {
		this.subjectMouldContent = subjectMouldContent;
	}
	public Long getSid() {
		return sid;
	}
	public void setSid(Long sid) {
		this.sid = sid;
	}
	public Date getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}
	
}
