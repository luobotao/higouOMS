package forms.admin;

import java.util.ArrayList;
import java.util.List;

import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;

public class FreshTaskForm {
	
	@Required(message = "开始时间不能为空")
	public String date_start;
	
	@Required(message = "时间间隔不能为空")
	public String timeBetween;
	
	@Required(message = "时间间隔单位不能为空")
	public String unit;
	
	@Required(message = "单量不能为空")
	public String orderAmount;

	public List<ValidationError> validate() {
		List<ValidationError> errors = new ArrayList<ValidationError>();
		if ("-1".equals(unit)) {
			errors.add(new ValidationError("unit", "请选择时间间隔单位"));
		}
		return errors.isEmpty() ? null : errors;
	}
}
