package forms;

import play.data.validation.Constraints.Required;

public class CreateOrUpdateUserForm {

	
	@Required(message = "用户名不能为空")
	public String username;
	
	public String realname;

	@Required(message = "密码不能为空")
	public String passwd;

	public String headIcon;



}
