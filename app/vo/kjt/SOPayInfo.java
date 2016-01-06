package vo.kjt;


/**
 * 
 * <p>Title: PayInfo.java</p> 
 * <p>Description: 订单支付信息</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年9月28日  下午3:32:26
 * @version
 */
public class SOPayInfo {

	public double productAmount;		//商品总金额
	public double shippingAmount;		//运维总金额
	public double taxAmount;			//商品行邮税总金额
	public double commissionAmount;		//下单支付产生的手续费 ，默认0.0
	public int payTypeSysNo;			//支付方式编号  111：东方支付 112：支付宝 114：财付通 117：银联支付 118：微信支付
	public String paySerialNumber;		//支付流水号
    
}
