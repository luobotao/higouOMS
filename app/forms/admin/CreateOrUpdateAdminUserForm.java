package forms.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apache.commons.codec.digest.DigestUtils;

import play.data.validation.ValidationError;

public class CreateOrUpdateAdminUserForm {
	public Long id =-1L;

	@Size(min = 2, max = 20, message = "用户名长度必须在2至20之间")
	public String username;

	@Size(min = 2, max = 11, message = "姓名长度必须在2至20之间")
	public String realname;

	@Size(min = 6, max = 20, message = "密码长度必须在6至20之间")
	public String passwd;

	@Size(min = 6, max = 20, message = "确认密码长度必须在6至20之间")
	public String confirmedPassword;
	
	public String adminType;

	public List<ValidationError> validate() {
		List<ValidationError> errors = new ArrayList<ValidationError>();
		if (passwd == null || !passwd.equals(confirmedPassword)) {
			errors.add(new ValidationError("password", "两次密码必须一致"));
		}
		passwd = DigestUtils.md5Hex(passwd);
		return errors.isEmpty() ? null : errors;
	}

}
