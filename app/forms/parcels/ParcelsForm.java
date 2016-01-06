package forms.parcels;


import java.util.Date;
import java.util.List;

import forms.DateBetween;

public class ParcelsForm {
	
	public Integer page = 0;
	public Integer size = 20;

	public String status = "-99";			//物流状态
	public Integer checkStatus = -1;			//结算状态
	public String src= "-1";				//包裹类型
	public DateBetween between;			//包裹生成时间
	public String parcelsCode= "";		//包裹号
	public String orderCode= "";		//订单号
	public String keyword="";			//关键字	商品ID/商品名称/收货人/电话
	
	public String pId;					//商品ID
	public String newSku;				//newSku
	public String title;				//商品名称
	public String name;					//订单收货人
	public String phone;				//收货人电话
	public String sfcode;				//黑客号
	
	public String waybillCode;			//运单号
	public String kdCompany;			//货运公司
	public String remark;				//物流内容
	public Integer nsort;				//排序
	public Date addDate;				//商品添加内容时间
	
	public List<Long> parcelIds;		//包裹Id集合
	public Long parId;					//包裹Id
	public Long adminid=0L;				//管理员ID，此处是为联营商品录入的外部商户管理员ID，获取包裹时只能获取该管理员录入的商品生成的包裹
	
	public int adminId = -1;			//联营商品，用户id
}
