package vo;

/**
 * 
 * <p>Title: ParcelsVO.java</p> 
 * <p>Description: 包裹VO</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年8月29日  下午3:31:47
 * @version
 */
public class ParcelsVO {

	public Long parcelProId;		//包裹商品ID
	public String parcelCode;		//包裹号
	public String sfcode;			//嘿客号
	public String orderCode;		//订单号
	public Long pid;				//pid
	public String newSku;			//商品newSku
	public String title;			//商品名称
	public String listpic;			//商品图片
	public String name;				//收货人
	public String phone;			//电话
	public String dateAdd;			//订单生成时间
	public String src;				//包裹类型
	public String status;			//包裹状态
	public Long parcelId;			//包裹ID
	public String province;			//省市区
	public String address;			//详细地址
	public Integer cnt;			//商品购买数量
	public Double price;			//商品购买单价
	public Integer checkStatus;			//结算状态 Constants.ParcelCheckStatus
	public String cardId;//身份证号
}
