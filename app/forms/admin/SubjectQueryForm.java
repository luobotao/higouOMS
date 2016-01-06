package forms.admin;

/**
 * 
 * <p>Title: SubjectQueryForm.java</p> 
 * <p>Description: 用于专题表单提交</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年8月9日  下午5:00:31
 * @version
 */
public class SubjectQueryForm {
	public Integer page = 0;
	public Integer size = 20;
	
	public String sname;
	public Integer nsort;
	public Long id;
	//用户筛选条件
	public String sid;
	public String pidOrNewSku;
	public String startNsort ;
	public String endNsort ;
}
