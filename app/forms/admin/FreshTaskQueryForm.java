package forms.admin;

import java.util.List;

import forms.DateBetween;
import forms.DateSimpleBetween;

public class FreshTaskQueryForm {
	public Integer page = 0;
	public Integer size = 20;

	public String taskId="-1";//任务ID 当为-1时查找所有订单 -2时查找由任务创建的订单 大于0时查找相应任务ID的订单
	public DateBetween between;//时间间隔 下单时间
	public DateSimpleBetween simpleBetween;//时间间隔 用于统计的时间
	public String payMethod= "-1";//支付方式
	public String ordertype = "-1";//订单是否是撒娇支付  // 1代购 2撒娇
	
	public Integer device= -1;//访问平台
	
	public Integer categoryId= -1;//品类ID
	public Integer greater= 0;//销量大于等于
	public List<Long> ids;
	public List<Long> orderIds;
	
	
	public String parcelStatus= "-1";//包裹状态
	public String parcelCode= "";//包裹号或者订单号
	
	public Integer typ=0;			//类型	0、默认	1、代下单  2、自营 
	public Integer fromsite=-1;		//来源网站  -1、全部 0、嗨个购  1 美国亚马逊 2、韩国  3、日本乐天  4、日本亚马逊 5莎莎  6、iherb  7、韩国乐天
	public String proInfo="";			//商品ID/名称 （用于模糊查询）
	public Integer countTyp=3;		//销量类别  1、自营 2、嘿客店  3、总体
	
	public long uid = 0L;				//商户id ,主要用于`user` 表里 gid=4
	public long adminid = -1;	//用于判断是否是联营商户
}
