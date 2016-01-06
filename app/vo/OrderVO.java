package vo;

/**
 * 
 * <p>Title: OrderVO.java</p> 
 * <p>Description: 订单Vo</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年8月29日  上午10:05:43
 * @version
 */
public class OrderVO {

	public Long orderProId;		//订单商品ID
	public String sfcode;			//嘿客号
	public String orderCode;		//订单号
	public Long pid;				//pid
	public String newSku;			//商品newSku
	public String title;			//商品名称
	public String typeName;			//商品类型
	public String listpic;			//商品图片
	public String exturl;			//商品外链
	public String fromsite;			//来源网站
	public String price;			//订单商品价格
	public String counts;			//订单商品数量 
	public String name;				//收货人
	public String phone;			//电话
	public String ordertype;		//撒娇订单
	public String paymethod;		//支付方式
	public String paystat;			//支付状态
	public String dateAdd;			//订单生成时间
	public String flg;				//处理状态
	public Long orderId;			//订单ID
	public int typ;					//商品类型
	public boolean existsParcels;	//是否存在包裹
	public String status;			//订单状态
}
