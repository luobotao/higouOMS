package vo;



/**
 * 订单数据VO
 * @author luobotao
 * @Date 2015年4月30日
 */
public class OrderStaticsVO {

	public String dateTxt;		//日期
	public Integer uAdd = 0;		//新增用户数
	public Integer uAct= 0;		//活跃用户数
	public Integer urLogin= 0;		//登录用户数
	
	public Integer urAdd= 0;		//新增注册用户数
	public Integer uoNewadd= 0;	//新增下单用户数
	public Integer ufoNewadd= 0;	//新增支付用户数
	public String newPayRate;	//新增支付转化率（新增支付用户数/新增注册用户数）
	public Integer actDevice= 0;	//活跃设备数---------------
	
	public Integer uoAdd= 0;		//下单用户数
	public Integer ufoAdd= 0;		//支付用户数
	public String payRate;		//支付转化率（支付用户数/活跃用户数）
	
	public Integer oAdd= 0;		//订单数
	public Integer ofAdd= 0;		//支付订单数
	public String orderPrice;	//订单金额（当日总订单金额-当日嘿客订单金额）
	public Double priceAdd= 0.0;		//支付金额-----------------
	
	public Integer buyProNum= 0;	//下单商品数----------------
	public Integer payProNum= 0;	//支付商品数----------------
	public String perPrice;		//客单价（订单金额/支付用户数）
    
    
}
