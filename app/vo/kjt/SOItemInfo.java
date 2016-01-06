package vo.kjt;


/**
 * 
 * <p>Title: SOItemInfo.java</p> 
 * <p>Description: 订单中购买商品列表</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年9月28日  下午3:45:56
 * @version
 */
public class SOItemInfo {
	
	public String productId;		//商品ID
	public int quantity;			//购买数量
	public double salePrice;		//商品在分销渠道下单时的单品金额，保留2位小数，需要满足Sum(SalePrice*Quantity)=PayInfo.ProductAmount
	public double taxPrice;			//商品在分销渠道下单时的单品税费，保留2位小数，需要满足Sum(taxPrice*Quantity)=PayInfo.TaxAmount
}
