package controllers.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.OrderProduct;
import models.Parcels;
import models.ShoppingOrder;
import models.SmsInfo;
import models.admin.AdminFreshTask;
import models.admin.AdminUser;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;

import play.Configuration;
import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.Result;
import play.twirl.api.Html;
import services.ServiceFactory;
import services.admin.AdminFreshService;
import services.admin.AdminUserService;
import services.admin.SmsService;
import services.product.ProductService;
import utils.AjaxHelper;
import utils.Constants;
import utils.Constants.SmsStatus;
import utils.Errors;
import utils.Numbers;
import vo.CloseTaskVO;
import forms.admin.AdminLoginForm;
import forms.admin.FreshTaskForm;
import forms.admin.FreshTaskQueryForm;
import forms.admin.SmsTemplateForm;

/**
 * @author luobotao
 *
 */
@Named
@Singleton
public class SmsController extends BaseAdminController {

    private final ALogger logger = Logger.of(SmsController.class);

    private final SmsService smsService;

    private final Form<SmsTemplateForm> smsTemplateForm = Form.form(SmsTemplateForm.class);

    private final String error = "sms_error";


    @Inject
    public SmsController(final SmsService smsService) {
        this.smsService = smsService;
    }
    @AdminAuthenticated()
    public Result smsManage() {
    	SmsTemplateForm formPage = new SmsTemplateForm();
    	Form<SmsTemplateForm> form = smsTemplateForm.bindFromRequest(request());
    	String flag = Form.form().bindFromRequest().get("flag");
    	String resultMsg = "";
    	if(!StringUtils.isBlank(flag)){
        	if (form==null || form.hasErrors() || form.get()==null) {
        		resultMsg= Errors.wrapedWithDiv(form.errors());
    		}else{
    			formPage = form.get();
    			String type =String.valueOf(formPage.type);
                String smsTemplateId =formPage.smsTemplateId;
                String templateVariable =formPage.templateVariable;
                String phones=formPage.phones;
                String phoneArray[]=phones.split(",");
                List<SmsInfo> smsInfoList = new ArrayList<SmsInfo>();
        		for(String phone :phoneArray){
        			SmsInfo smsInfo = new SmsInfo();
        			smsInfo.setTpl_id(smsTemplateId);
        			smsInfo.setType(type);
        			smsInfo.setFlag("0");//待发送
        			smsInfo.setPhone(phone);
        			smsInfo.setArgs(templateVariable);
        			smsInfo.setInsertTime(new Date());
        			smsInfo.setUpdateTime(new Date());
        			smsInfoList.add(smsInfo);
        		}
        		smsService.saveSmsList(smsInfoList);
        		resultMsg = "发送成功";
    		}
    	}else{
    		resultMsg="";
    	}
    	
        return ok(views.html.admin.smsSend.render(resultMsg,Html.apply(SmsStatus.smsStatus2HTML(formPage.type))));
    }
    
    @AdminAuthenticated()
    public Result createSendSmsTask() {
    	Form<SmsTemplateForm> form = smsTemplateForm.bindFromRequest(request());
    	SmsTemplateForm formPage = new SmsTemplateForm();
        if (!form.hasErrors()) {
        	formPage = form.get();
        }
    	
    	if (form==null || form.hasErrors() || form.get()==null) {
			flash(error, Errors.wrapedWithDiv(form.errors()));
		} 
    	return redirect(controllers.admin.routes.SmsController.smsManage());
    }
    

}
