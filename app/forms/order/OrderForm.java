package forms.order;


import java.util.List;

import forms.DateBetween;

public class OrderForm {
	
	public Integer page = 0;
	public Integer size = 20;

	public Integer allocatStatus = -1;	//分配状态
	public String orderProFlg = "-1";	//处理状态 -1全部  0 未处理 1 已处理 2 申请退款 3退款完成
	public String payMethod= "-1";		//支付方式
	public String paystat = "20";		//支付状态
	public String typ= "0";			//商品类型 1、代下单  2、自营  90、嘿客店 0 全部 3联营
	public String preSell= "0";			//预售商品订单阶段 1、定金已支付  2、尾款待支付  3、支付完成 0 全部
	public String ordertype = "-1";		//订单是否是撒娇支付  // 1代购 2撒娇
	public String fromsite="-1";		//来源网站  -1、全部 0、嗨个购  1 美国亚马逊 2、韩国  3、日本乐天  4、日本亚马逊 5莎莎  6、iherb  7、韩国乐天
	public DateBetween between;			//时间间隔 下单时间
	public String orderCode= "";		//订单号
	public String keyword="";			//关键字
	
	public List<Long> pIds;				//满足条件的商品ID集合
	public List<Long> orderIds;			//订单id集合
	
	public String title;				//商品名称
	public String name;					//订单收货人
	public String phone;				//收货人电话
	public String pId;					//商品ID
	public String newSku;				//newSku	
	public String sfcode;				//嘿客店单号
	
	//save order
	public Long soId;						//订单Id  shopping_Order
	public String sopIds;						//订单商品Ids  shopping_Order_pro
	public String province;				//省份
	public String address;				//地址
	public String foreignOrder;			//海外运单号
	public String account;				//下单账号
	public String creditcard;			//信用卡号
	public String currency;				//费率
	public String totalFee;				//支付金额
	public String traffic;				//运送方式
	public String traffic_mark;				//运送公司
	public Long fromUid=0L;//fromUid 会员来源于后台管理员的uid，主要是针对商户
	public int adminid = -1;	//用于判断是否是联营商户
	
	public String opertype;				//操作类型
}
