package vo;

/**
 * 
 * <p>Title: ScanCodeVO.java</p> 
 * <p>Description: 扫码统计</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年9月26日  下午2:06:26
 * @version
 */
public class ScanCodeVO {

	public String dateTxt;		//日期
	public String uid;			//店铺名
	public int saomaPv;		//扫码pv
	public int saomaUv;		//扫码uv
	public int productDetailPv;		//商品详情pv
	public int productDetailUv;		//商品详情Uv
	public int clickBuyPv;			//点击立即购买pv
	public int clickBuyUv;			//点击立即购买uv
	public int jiesuanPagePv;	//结算页面访问pv
	public int jiesuanPageUv;	//结算页面访问uv
	public int zhifuBtnPv;		//点击支付按钮pv
	public int zhifuBtnUv;		//点击支付按钮Uv
	public int orderSuccessNums;	//成功订单数
	public double orderSuccessTotalFee;	//成功订单金额
	public int orderSuccessUsers;//成功下单用户数
	public int wxJianquanUser;		//微信鉴权用户
	public int mobileLoginPv;		//手机登录pv
	public int mobileLoginUv;		//手机登录uv
	public int mobileLoginSuccessUsers;//手机登录成功用户数
	public int orderFinishClickDownPv;	//订单完成点击下载pv
	public int orderFinishClickDownUv;	//订单完成点击下载uv
	public int changeAppUsers;			//转化app用户数
	
	public int newAddUsers;				//新增注册用户数
	public int newBuyUsers;				//新增购买用户数
	public int bugUsers;				//购买用户数
	public int reBuy30DayUser;			//30天内复购人数
	public double reBuy30DayStat;		//30天内复购率
}
