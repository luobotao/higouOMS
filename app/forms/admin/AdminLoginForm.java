package forms.admin;

import javax.validation.constraints.Size;

public class AdminLoginForm {
	@Size(min = 2, max = 20, message = "用户名长度必须在2到20之间")
	public String username;

	@Size(min = 2, max = 20, message = "密码长度必须在6到20之间")
	public String password;

}
