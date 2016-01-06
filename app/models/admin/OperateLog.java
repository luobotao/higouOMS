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
 * 日志表
 * @author luobotao
 * @Date 2015年9月11日
 */
@Entity
@Table(name="operate_log")
public class OperateLog implements Serializable{

	private static final long serialVersionUID = 8062516274733431730L;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;
    
    @NotNull
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateAdd;
	
    @NotNull
    @Column(name="adminid")
    public Long adminid;

    @Column(name="adminname",columnDefinition="varchar(16) DEFAULT '' ")
    public String adminname;
    @Column(name="typ",columnDefinition="varchar(32) DEFAULT '' ")
    public String typ;
    @Column(name="remark",columnDefinition="varchar(512) DEFAULT '' ")
    public String remark;

    @NotNull
    public LogType opType;

    @NotNull
    @Column(name="ip")
    public String ip;

    public enum LogType {
        // 注意添加新的类型时候一定不能改变现有的顺序
        // 只能添加在最后面！！！！！！
        LOGIN("登录"), LOGOUT("安全退出"), PRODUCT("商品"), PRODUCTGROUP("组合商品"), CHANNELMOULD("频道卡片"),
        APPVERSION("AppVersion"), ORDER("订单"), COMMENT("评论"),
        MOULED("卡片"), CHANNEL("频道"), SUBJECT("专题"),
        PUSH("推送"), COUPON("优惠券"), CREDITCARD("信用卡"),
        CATEGORISE("分类"),NSTOCK("库存管理"),PRODUCTSTATUS("商品状态"),
        CHANNELMOULDPRO("频道卡片内容"),SUBJECTMOULD("专题卡片"),PARCELS("包裹"),WAYBILL("物流"),HIGOUPRICE("嗨购售价"),ADMINUSER("管理员");

        private String message;

        private LogType(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDateAdd() {
		return dateAdd;
	}

	public void setDateAdd(Date dateAdd) {
		this.dateAdd = dateAdd;
	}

	public Long getAdminid() {
		return adminid;
	}

	public void setAdminid(Long adminid) {
		this.adminid = adminid;
	}

	public String getAdminname() {
		return adminname;
	}

	public void setAdminname(String adminname) {
		this.adminname = adminname;
	}

	public String getTyp() {
		return typ;
	}

	public void setTyp(String typ) {
		this.typ = typ;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public LogType getOpType() {
		return opType;
	}

	public void setOpType(LogType opType) {
		this.opType = opType;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

    
}
