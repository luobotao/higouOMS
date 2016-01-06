package vo;



/**
 * 商品销量排行VO
 * @author luobotao
 * @Date 2015年4月30日
 */
public class ProductSellTopVO {

    public Integer pid;			//商品id
    public String categoryName;	//分类
    public String productName;	//商品名称
    public String typ;			//类型
    public String fromsite;	//来源网站
    public Integer counts;		//总销量
    public long nstock;		//可售库存
    
    public Integer orderUserCnt;	//下单用户数
    public Integer orderUserPayCnt;//支付用户数
    public String orderUserRate;	//支付转化率
    public Integer orderCnt;	//订单总数
    public Integer orderPayCnt;	//支付总数
    
    
	

    
}
