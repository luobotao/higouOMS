package forms;


/**
 * 查询用户form
 * @author luobotao
 * @Date 2015年8月31日
 */
public class UserQueryForm {
	public Integer page = 0;
	public Integer size = 20;
	public Long userId;//用户ID
	public Integer gid=-1;//组ID
	public String userName;//用户名称
	public String postman;//是否是快递商户
	public String phone;//用户手机
	public String danyanFlag;
	public Integer province=-1;//省ID
	public Integer city=-1;//城市ID
	public String keywords;//关键字
	public Long fromUid=-1L;//fromUid 会员来源于后台管理员的uid，主要是针对商户
}
