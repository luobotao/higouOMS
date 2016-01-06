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

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import utils.StringUtil;

/**
 * 商品实体
 * 
 * @author luobotao Date: 2015年4月16日 上午9:59:24
 */
@Entity
@Table(name = "product")
public class Product implements Serializable{

	private static final long serialVersionUID = 2605259483934326809L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long pid;

	private String skucode;
	private String title;//商品名称
	private String subtitle;//商品描述

	@Column(columnDefinition = "int(10) DEFAULT 0 ")
	private int category;
	@Column(nullable = false, columnDefinition = "decimal(12,4) NOT NULL DEFAULT '999999.9900'")
	private Double price=999999.9900;//嗨购售价
	@Column(nullable = false, columnDefinition = "decimal(12,4) NOT NULL DEFAULT '999999.9900'")
	private Double list_price=999999.9900;//原价
	@Column(columnDefinition = " int(10) unsigned DEFAULT '0' ")
	private int discount;
	@Column(columnDefinition = " tinyint(4) unsigned DEFAULT '0' ")
	private int imgnums;//是否是联营商品 0否 1是

	private String exturl;
	@Column(columnDefinition = " int(10) unsigned DEFAULT '0' ")
	private int salesrank;
	@Column(name="`status`",columnDefinition = " tinyint(4) DEFAULT '0' ")
	private int status;//NEW(1, "新品待审"),ONSALE(10, "在线"), OFFSALE(20, "下架"), OUTOFSALE(21, "缺货自动下架"), 	RECYCLE(30, "回收站"),WAITDELETE(40, "等候彻底删除"),RETENTION(50, "条目暂时保留"),AMAZON(102, "早期amazon商品");
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_upd;
	
	@Column(columnDefinition = " int(10) DEFAULT '0' ")
	private int nlikes;
	@Column(columnDefinition = " tinyint(4) DEFAULT '0' ")
	private int ishot;		//用于标识商品是否是组合商品
	@Column(columnDefinition = " int(10) DEFAULT '0' ")
	private int version;
	private String extcode;//原站sku号
	@Column(columnDefinition = " int(10) DEFAULT '0' ")
	private int fromsite;//来源
	@Column(columnDefinition = " int(10) DEFAULT '0' ")
	private int currency=1;//货币
	private String imgstr;
	private String listpic;//列表图
	private String adstr1;//推荐理由
	private String adstr3;
	private String detail;
	@Column(name="`sort`")
	private int sort;
	@Column(columnDefinition = " decimal(12,4) ")
	private Double chinaprice;//国内售价
	private Long nstock;//库存
	@Column(columnDefinition = " tinyint(4) DEFAULT '0' COMMENT '1自动更新; 0不更新' ")
	private int nstock_autoupd;
	@Column(columnDefinition = " varchar(2) DEFAULT '0' ")
	private String islovely;//是否支持撒娇
	@Column(columnDefinition = " varchar(2) DEFAULT '1' ")
	private String typ;//(1, "代下单"), AUTOTROPHY(2, "自营")
	@Column(columnDefinition = " decimal(9,2) DEFAULT '0.00' ")
	private Double weight;//重量
	@Column(columnDefinition = " decimal(9,2) DEFAULT '0.00' ")
	private Double freight;
	@Column(columnDefinition = " varchar(32) DEFAULT '' ")
	private String wayremark;
	@Column(columnDefinition = " int(11) DEFAULT '0' ")
	private int wishcount;
	@Column(columnDefinition = " varchar(16) ")
	private String activityname;
	@Column(columnDefinition = " varchar(64) ")
	private String activityimage;
	@Column(columnDefinition = " varchar(256) DEFAULT '' ")
	private String PromiseURL;
	@Column(columnDefinition = " int(11) DEFAULT '-1' ")
	private int limitcount;//限购数量
	@Column(columnDefinition = " decimal(3,1) DEFAULT '10.0' ")
	private Double lovelydistinct;//撒娇折扣
	@Column(columnDefinition = " decimal(6,2) DEFAULT NULL ")
	private Double rmbprice;
	@Column(columnDefinition = " int(11) DEFAULT '0' ")
	private int islockprice;
	@Column(columnDefinition = " varchar(64) DEFAULT '' ")
	private String distinctimg;
	@Column(columnDefinition = " varchar(2) DEFAULT '0' ")
	private String sendmailflg;
	@Column(columnDefinition = " int(11) DEFAULT '0' ")
	private int backnstock;
	@Column(columnDefinition = " varchar(128) DEFAULT '' ")
	private String specifications;
	private Long ppid;//根据此标识商品的规格 相当是大哥 不相等是小弟 没有则为空
	private String stitle;
	@Column(columnDefinition = " int(11) DEFAULT '0' ")
	private int isopenid;//是否需要身份证信息 0不需要 1需要
	@Column(columnDefinition = " int(11) DEFAULT '0' ")
	private int isopenidimg;//是否需要身份证图片 0不需要 1需要
	@Column(columnDefinition = " varchar(256) DEFAULT '' ")
	private String specpic;
	private String btim;
	private String etim;
	@Column(columnDefinition = " varchar(2) DEFAULT '1' ")
	private String ptyp;//1普通 2撒娇 3预售 4定时开抢 
	
	@Column(columnDefinition = " varchar(2) DEFAULT '0' ")
	private String newMantype;//1首购商品 2仅一次商品 0普通（不做处理）3、0元商品
	
	@Column(columnDefinition = " decimal(9,2) DEFAULT '0.00' ")
	private Double deposit;//预售订金
	@Column(columnDefinition = " int(11) DEFAULT '0' ")
	private int mancnt;//预售参与人数
	@Column(columnDefinition = " int(11) DEFAULT '0' ")
	private int stage;//预售当前阶段（1订金待支付 2采中集购中3尾款待支付4结束）
	@Column(columnDefinition = " varchar(256) DEFAULT '' ")
	private String preselltoast;
	@Column(columnDefinition = " varchar(32) DEFAULT '' ")
	private String rtitle;
	@Column(columnDefinition = " varchar(32) DEFAULT '' ")
	private String num_iid;
	@Column(columnDefinition = " varchar(128) DEFAULT '' ")
	private String paytim;
	
	@Column
	private String commision_average;	//人均赚取佣金值
	
	@Column(columnDefinition = " int(10) unsigned DEFAULT '0' ")
	private Integer forbiddenCnt;	//限制购买数量
	@Column(columnDefinition = " varchar(32) DEFAULT '' ")
	
	private String jpntitle;
	private String jpncode;
	private Date wx_upd;
	@Column(columnDefinition = "  varchar(1) DEFAULT '0' ")
	private String wx_flg;
	@Column(columnDefinition = "  int(11) DEFAULT '0' ")
	private int stock;
	@Column(columnDefinition = " varchar(32) DEFAULT '' ")
	private String newSku;
	@Column(name="is_sync_erp",columnDefinition = " int(4) DEFAULT '2' COMMENT '更新ERP库存：1、<=5置0; 2、<=0置0; 9、不更新'")
	private int isSyncErp;//9不同步 2正常的同步
	@Column(nullable = false, columnDefinition = "decimal(9,2) NOT NULL DEFAULT '0.00'")
	private Double costPrice=0.00;
	@Column(columnDefinition = " int(2) DEFAULT '0' ")
	private Integer isFull;//代言是否已满 0未满，１已满
	private String nationalFlag;
	/*
	 * 是否可代言
	 */
	@Column(columnDefinition = "  int(11) DEFAULT '0' ")
	private int isEndorsement;
	
	/*
	 * 代言次数
	 */
	@Column(columnDefinition = "  int(11) DEFAULT '0' ")
	private int endorsementCount;
	/*
	 * 代言上限数量
	 */
	@Column(columnDefinition = "  int(11) DEFAULT '0' ")
	private int maxEndorsementCount;
	/*
	 * 代言价格（好友价格）
	 */
	@Column(columnDefinition = " decimal(9,2) DEFAULT '0.00'")
	private Double	endorsementPrice;
	/*
	 * 佣金
	 */
	@Column(columnDefinition = " decimal(9,2) DEFAULT '0.00'")
	private Double	commision;
	/*
	 * 佣金类型 1：金额  2：百分比
	 */
	@Column(columnDefinition = " int(5) DEFAULT '1'")
	private Integer commisionTyp; 
	
	/*
	 * 邮费首重500克,每100g续重
	 */
	@Transient
	private Integer postfee;
	
	@Transient
	private String symbol;//货币符号
	
	@Transient
	private String fromsiteName;//来源名称
	@Transient
	private int counts;
	@Transient
	private String categoryName;
	/*
	 * 物流对象
	 */
	@Transient
	private Fromsite fromobj;
	@Transient
	private String date_txt;

	@Transient
	private String hiPrice;//嗨购售价 展示到前端的实际嗨购售价
	@Transient
	private String ChaListPrice;//市场价 展示到前端的实际原价（由于代下单的商品，两个价格会因是否锁定价格而取不同字段的值，故加入这两个transient字段）
	@Transient
	private String typeName;
	
	public String getHiPrice() {
		return hiPrice;
	}
	public void setHiPrice(String hiPrice) {
		this.hiPrice = hiPrice;
	}
	public String getChaListPrice() {
		return ChaListPrice;
	}
	public void setChaListPrice(String chaListPrice) {
		ChaListPrice = chaListPrice;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public int getIsopenidimg() {
		return isopenidimg;
	}
	public void setIsopenidimg(int isopenidimg) {
		this.isopenidimg = isopenidimg;
	}
	public String getFromsiteName() {
		return fromsiteName;
	}
	public void setFromsiteName(String fromsiteName) {
		this.fromsiteName = fromsiteName;
	}
	public Integer getPostfee() {
		return postfee;
	}
	public void setPostfee(Integer postfee) {
		this.postfee = postfee;
	}
	public int getMaxEndorsementCount() {
		return maxEndorsementCount;
	}
	public void setMaxEndorsementCount(int maxEndorsementCount) {
		this.maxEndorsementCount = maxEndorsementCount;
	}
	public int getIsEndorsement() {
		return isEndorsement;
	}
	public void setIsEndorsement(int isEndorsement) {
		this.isEndorsement = isEndorsement;
	}
	public int getEndorsementCount() {
		return endorsementCount;
	}
	public void setEndorsementCount(int endorsementCount) {
		this.endorsementCount = endorsementCount;
	}
	
	public Fromsite getFromobj() {
		return fromobj;
	}
	public void setFromobj(Fromsite fromobj) {
		this.fromobj = fromobj;
	}
	
	
	public String getNationalFlag() {
		return nationalFlag;
	}
	public void setNationalFlag(String nationalFlag) {
		this.nationalFlag = nationalFlag;
	}

	
	public String getDate_txt() {
		return date_txt;
	}
	public void setDate_txt(String date_txt) {
		this.date_txt = date_txt;
	}
	public int getCounts() {
		return counts;
	}
	public void setCounts(int counts) {
		this.counts = counts;
	}
	public Long getPid() {
		return pid;
	}
	public void setPid(Long pid) {
		this.pid = pid;
	}
	public String getSkucode() {
		return skucode;
	}
	public void setSkucode(String skucode) {
		this.skucode = skucode;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSubtitle() {
		return subtitle;
	}
	public String getPaytim() {
		return paytim;
	}
	public void setPaytim(String paytim) {
		this.paytim = paytim;
	}
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
	public int getCategory() {
		return category;
	}
	public void setCategory(int category) {
		this.category = category;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public Double getList_price() {
		return list_price;
	}
	public void setList_price(Double list_price) {
		this.list_price = list_price;
	}
	public int getDiscount() {
		return discount;
	}
	public void setDiscount(int discount) {
		this.discount = discount;
	}
	public int getImgnums() {
		return imgnums;
	}
	public void setImgnums(int imgnums) {
		this.imgnums = imgnums;
	}
	public String getExturl() {
		return exturl;
	}
	public void setExturl(String exturl) {
		this.exturl = exturl;
	}
	public int getSalesrank() {
		return salesrank;
	}
	public void setSalesrank(int salesrank) {
		this.salesrank = salesrank;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
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
	public int getNlikes() {
		return nlikes;
	}
	public void setNlikes(int nlikes) {
		this.nlikes = nlikes;
	}
	public int getIshot() {
		return ishot;
	}
	public void setIshot(int ishot) {
		this.ishot = ishot;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public String getExtcode() {
		return extcode;
	}
	public void setExtcode(String extcode) {
		this.extcode = extcode;
	}
	
	public String getJpntitle() {
		return jpntitle;
	}
	public void setJpntitle(String jpntitle) {
		this.jpntitle = jpntitle;
	}
	public String getJpncode() {
		return jpncode;
	}
	public void setJpncode(String jpncode) {
		this.jpncode = jpncode;
	}
	public int getFromsite() {
		return fromsite;
	}
	public void setFromsite(int fromsite) {
		this.fromsite = fromsite;
	}
	public int getCurrency() {
		return currency;
	}
	public void setCurrency(int currency) {
		this.currency = currency;
	}
	public String getImgstr() {
		return imgstr;
	}
	public void setImgstr(String imgstr) {
		this.imgstr = imgstr;
	}

	public String getListpic() {
		return StringUtil.getListpic(listpic);
	}
	public void setListpic(String listpic) {
		this.listpic = listpic;
	}
	public String getAdstr1() {
		return adstr1;
	}
	public void setAdstr1(String adstr1) {
		this.adstr1 = adstr1;
	}
	public String getAdstr3() {
		return adstr3;
	}
	public void setAdstr3(String adstr3) {
		this.adstr3 = adstr3;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public int getSort() {
		return sort;
	}
	public void setSort(int sort) {
		this.sort = sort;
	}
	public Double getChinaprice() {
		return chinaprice;
	}
	public void setChinaprice(Double chinaprice) {
		this.chinaprice = chinaprice;
	}
	
	public Long getNstock() {
		return nstock;
	}
	public void setNstock(Long nstock) {
		this.nstock = nstock;
	}
	public int getNstock_autoupd() {
		return nstock_autoupd;
	}
	public void setNstock_autoupd(int nstock_autoupd) {
		this.nstock_autoupd = nstock_autoupd;
	}
	public String getIslovely() {
		return islovely;
	}
	public void setIslovely(String islovely) {
		this.islovely = islovely;
	}
	public String getTyp() {
		return typ;
	}
	public void setTyp(String typ) {
		this.typ = typ;
	}
	public Double getWeight() {
		return weight;
	}
	public void setWeight(Double weight) {
		this.weight = weight;
	}
	public Double getFreight() {
		return freight;
	}
	public void setFreight(Double freight) {
		this.freight = freight;
	}
	public String getWayremark() {
		return wayremark;
	}
	public void setWayremark(String wayremark) {
		this.wayremark = wayremark;
	}
	public int getWishcount() {
		return wishcount;
	}
	public void setWishcount(int wishcount) {
		this.wishcount = wishcount;
	}
	public String getActivityname() {
		return activityname;
	}
	public void setActivityname(String activityname) {
		this.activityname = activityname;
	}
	public String getActivityimage() {
		return activityimage;
	}
	public void setActivityimage(String activityimage) {
		this.activityimage = activityimage;
	}
	public String getPromiseURL() {
		return PromiseURL;
	}
	public void setPromiseURL(String promiseURL) {
		PromiseURL = promiseURL;
	}
	public int getLimitcount() {
		return limitcount;
	}
	public void setLimitcount(int limitcount) {
		this.limitcount = limitcount;
	}
	public Double getLovelydistinct() {
		return lovelydistinct;
	}
	public void setLovelydistinct(Double lovelydistinct) {
		this.lovelydistinct = lovelydistinct;
	}
	public Double getRmbprice() {
		return rmbprice;
	}
	public void setRmbprice(Double rmbprice) {
		this.rmbprice = rmbprice;
	}
	public int getIslockprice() {
		return islockprice;
	}
	public void setIslockprice(int islockprice) {
		this.islockprice = islockprice;
	}
	public String getDistinctimg() {
		return distinctimg;
	}
	public void setDistinctimg(String distinctimg) {
		this.distinctimg = distinctimg;
	}
	public String getSendmailflg() {
		return sendmailflg;
	}
	public void setSendmailflg(String sendmailflg) {
		this.sendmailflg = sendmailflg;
	}
	public int getBacknstock() {
		return backnstock;
	}
	public void setBacknstock(int backnstock) {
		this.backnstock = backnstock;
	}
	public String getSpecifications() {
		return specifications;
	}
	public void setSpecifications(String specifications) {
		this.specifications = specifications;
	}
	
	public Long getPpid() {
		return ppid;
	}
	public void setPpid(Long ppid) {
		this.ppid = ppid;
	}
	public String getStitle() {
		return stitle;
	}
	public void setStitle(String stitle) {
		this.stitle = stitle;
	}
	public int getIsopenid() {
		return isopenid;
	}
	public void setIsopenid(int isopenid) {
		this.isopenid = isopenid;
	}
	public String getSpecpic() {
		return StringUtils.isBlank(specpic)?"":StringUtil.getListpic(specpic);
	}
	public void setSpecpic(String specpic) {
		this.specpic = specpic;
	}
	public String getBtim() {
		return btim;
	}
	public void setBtim(String btim) {
		this.btim = btim;
	}
	public String getEtim() {
		return etim;
	}
	public void setEtim(String etim) {
		this.etim = etim;
	}
	public String getPtyp() {
		return ptyp;
	}
	public void setPtyp(String ptyp) {
		this.ptyp = ptyp;
	}
	public Double getDeposit() {
		return deposit;
	}
	public void setDeposit(Double deposit) {
		this.deposit = deposit;
	}
	public int getMancnt() {
		return mancnt;
	}
	public void setMancnt(int mancnt) {
		this.mancnt = mancnt;
	}
	public int getStage() {
		return stage;
	}
	public void setStage(int stage) {
		this.stage = stage;
	}
	public String getPreselltoast() {
		return preselltoast;
	}
	public void setPreselltoast(String preselltoast) {
		this.preselltoast = preselltoast;
	}
	public String getRtitle() {
		return rtitle;
	}
	public void setRtitle(String rtitle) {
		this.rtitle = rtitle;
	}
	public String getNum_iid() {
		return num_iid;
	}
	public void setNum_iid(String num_iid) {
		this.num_iid = num_iid;
	}
	public Date getWx_upd() {
		return wx_upd;
	}
	public void setWx_upd(Date wx_upd) {
		this.wx_upd = wx_upd;
	}
	public String getWx_flg() {
		return wx_flg;
	}
	public void setWx_flg(String wx_flg) {
		this.wx_flg = wx_flg;
	}
	public int getStock() {
		return stock;
	}
	public void setStock(int stock) {
		this.stock = stock;
	}
	public String getNewSku() {
		return newSku;
	}
	public void setNewSku(String newSku) {
		this.newSku = newSku;
	}
	public String getNewMantype() {
		return newMantype;
	}
	public void setNewMantype(String newMantype) {
		this.newMantype = newMantype;
	}
	public String getCommision_average() {
		return commision_average;
	}
	public void setCommision_average(String commision_average) {
		this.commision_average = commision_average;
	}
	public Integer getForbiddenCnt() {
		return forbiddenCnt;
	}
	public void setForbiddenCnt(Integer forbiddenCnt) {
		this.forbiddenCnt = forbiddenCnt;
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
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public int getIsSyncErp() {
		return isSyncErp;
	}
	public void setIsSyncErp(int isSyncErp) {
		this.isSyncErp = isSyncErp;
	}
	public Double getCostPrice() {
		return costPrice;
	}
	public void setCostPrice(Double costPrice) {
		this.costPrice = costPrice;
	}
	public Integer getIsFull() {
		return isFull;
	}
	public void setIsFull(Integer isFull) {
		this.isFull = isFull;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	
}
