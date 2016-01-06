package models.endorsement;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
/*
 * 代言表
 */
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import models.product.Product;
import models.user.User;
import utils.StringUtil;
@Entity
@Table(name = "endorsementduct")
public class Endorsement implements Serializable{
	private static final long serialVersionUID = 2348362607663662138L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long eid;	//代言编号
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime; //代言申请时间
	
	@Column(name="`userId`")
	private Long userId;	//代言人编号
	
	@Column(name="`gid`",columnDefinition="int(11) DEFAULT '1'")
	private Long gid;	//用户组ID
	
	@Column(name="`productId`")
	private Long productId;	//代言商品编号
	
	@Column(name="`remark`")
	private String remark;	//代言描述
	
	@Column(name="`preImgPath`")
	private String preImgPath;	//代言推荐（首）图
	
	@Column(columnDefinition = " int(10) unsigned DEFAULT '0' ")
	private Integer count;	//点赞数量

	@Column(columnDefinition = " int(10) unsigned DEFAULT '1' ")
	private Integer status=0;	//0用户上传未审核，1正常（审核通过），2后台审核未通过,3删除
	
	@Column
	private Integer picnums;	//图片数量
	
	@Column
	private String bannerimg;//图章
	@Column(name="endorsementPrice",columnDefinition = " decimal(9,2) DEFAULT '0.00'")
	private Double endorsementPrice;//好友价（代言价）
	@Column(columnDefinition = " decimal(9,2) DEFAULT '0.00'")
	private Double commision;//佣金（返现）
	@Column(name="commisionTyp",columnDefinition = " int(5) DEFAULT '1'")
	private Integer commisionTyp;//佣金类型 1：金额  2：百分比
	@Column(columnDefinition = " varchar(256) DEFAULT '' ")
	private String dimensionalimg;//二维码路径
	
	@Transient
	private List<EndorsementImg> endorsImgList;
	
	@Transient
	private Product producinfo;
	@Transient
	private User user;
	@Transient
	private List<EndorsementPraise> endorsPraiseList;
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getBannerimg() {
		return bannerimg;
	}

	public void setBannerimg(String bannerimg) {
		this.bannerimg = bannerimg;
	}

	public Integer getPicnums() {
		return picnums;
	}

	public void setPicnums(Integer picnums) {
		this.picnums = picnums;
	}

	public Integer getStatus() {
		if(status==null)
			status=0;
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Product getProducinfo() {
		return producinfo;
	}

	public void setProducinfo(Product producinfo) {
		this.producinfo = producinfo;
	}

	public List<EndorsementImg> getEndorsImgList() {
		return endorsImgList;
	}

	public void setEndorsImgList(List<EndorsementImg> endorsImgList) {
		this.endorsImgList = endorsImgList;
	}

	public List<EndorsementPraise> getEndorsPraiseList() {
		return endorsPraiseList;
	}

	public void setEndorsPraiseList(List<EndorsementPraise> endorsPraiseList) {
		this.endorsPraiseList = endorsPraiseList;
	}
	public Long getEid() {
		return eid;
	}

	public void setEid(Long eid) {
		this.eid = eid;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getGid() {
		return gid;
	}

	public void setGid(Long gid) {
		this.gid = gid;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getPreImgPath() {
		if(StringUtils.isBlank(preImgPath)){
			return "";
		}else{
			return StringUtil.getPICDomain()+preImgPath;
		}
	}

	public void setPreImgPath(String preImgPath) {
		this.preImgPath = preImgPath;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Double getEndorsementPrice() {
		return endorsementPrice;
	}

	public void setEndorsementPrice(Double endorsementPrice) {
		this.endorsementPrice = endorsementPrice;
	}

	public Double getCommision() {
		return commision;
	}

	public void setCommision(Double commision) {
		this.commision = commision;
	}

	public Integer getCommisionTyp() {
		return commisionTyp;
	}

	public void setCommisionTyp(Integer commisionTyp) {
		this.commisionTyp = commisionTyp;
	}

	public String getDimensionalimg() {
		return StringUtil.getPICDomain()+dimensionalimg;
	}

	public void setDimensionalimg(String dimensionalimg) {
		this.dimensionalimg = dimensionalimg;
	}
	
}
