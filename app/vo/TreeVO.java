package vo;

/**
 * 构造简单树VO
 * 
 * @author luobotao
 * @Date 2015年9月21日
 */
public class TreeVO {
	public Integer id; // ID
	public Integer pId; // tree的上级ID
	public String name; // 树名称
	public boolean checked; // 是否选中
	public boolean open = true; // 是否打开
}
