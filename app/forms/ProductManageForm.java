package forms;


public class ProductManageForm {
	public Integer page = 0;
	public Integer size = 20;
	public Long pid;//商品ID
	public String productName;//商品名称
	public Long userId;//商户ID
	public String userName;//商户名称
	public Long gid;//组ID 标识是商户 1普通用户2新用户3旧用户4商户  
	public String keyWords;//关键字
	public int category=-1;//品类
	public int type=-1;//类型
	public int fromsite = -1;//来源网站
	public Double priceSmall;//嗨购售价最小
	public Double priceBig;//嗨购售价最大
	public int status=-1;//状态
	public int isEndorsement=-1;//是否是代言 -1所有 0 非 1是 
	
	public int ishot=0;//是否时组合商品
	public Long adminUserId=-1L;//管理员ID
	
	public String pidOrNewSku;	//用于查询，pid支持多个按逗号隔开查询
	public String newSkus;	//用于查询newSku支持多个按逗号隔开查询
	public String pids;	//用于查询PID支持多个按逗号隔开查询
	
	public int adminid = -1;//用于查询指定联营联营商户的商品
	public boolean containsUnion = true;//用于查询指定联营联营商户的商品
	
	public boolean isfromshop=false;//是否来自商城
}
