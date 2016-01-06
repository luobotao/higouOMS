package forms.admin;

public class SysConfigQueryForm {
	public Integer page = 0;
	public Integer size = 20;

	public Integer parentMenuId = -1;// 父菜单ID
	public Integer isLeaf = -1;// 是否子节点,若为子节点，则parentMenuId需有值 1是0否

	public Integer menuOrder = 0;// 菜单顺序
	public String menuName = "";// 菜单名称
	public String menuUrl = "";// 菜单对应的链接
	public Integer isButton = -1;// 是否按钮 只有子菜单才可能是按钮 1是0否
}
