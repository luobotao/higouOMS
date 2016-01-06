package forms.admin;

import java.util.ArrayList;
import java.util.List;

import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;
import play.libs.Json;
import utils.Constants;

public class SmsTemplateForm {
	
	@Required(message = "模版ID不能为空")
	public String smsTemplateId;
	
	@Required(message = "短信类型不能为空")
	public Integer type=2;
	@Required(message = "短信变量不能为空")
	public String templateVariable;
	@Required(message = "手机号码不能为空")
	public String phones;
	public String flag;
	public List<ValidationError> validate() {
		List<ValidationError> errors = new ArrayList<ValidationError>();
		String phoneArray[]=phones.split(",");
		for(String phone :phoneArray){
			if(!Constants.PHONE_PATTERN.matcher(phone).matches()){
				errors.add(new ValidationError("phones", "发送失败，手机号中有错误的手机号，请重新输入！"));
			}
		}
		return errors.isEmpty() ? null : errors;
	}
}
