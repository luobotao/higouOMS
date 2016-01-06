package forms.admin;


public class ChannelQueryForm {
	public Integer page = 0;
	public Integer size = 20;
	
	public Long mouldId = -1L;
	public String manType="-1";
	public String flag="-1";
	public Long userid=-1L;//商户ID
	public Long gid;
	public Long channelId = -1L;//padchannelId
	public String typFlag = "-1";//0商品 1 Banner 2 频道  3 4图
	public Long pid = -1L;//商品ID
	public String productName="";
	
	public String pidOrNewSku;//商品ID或者newSku
	
	//用户筛选条件
	public String cid;
	public String startNsort ;
	public String endNsort ;
}
