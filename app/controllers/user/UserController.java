package controllers.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.JsonNode;

import models.admin.AdminUser;
import models.product.Product;
import models.user.GroupInfo;
import models.user.User;
import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.twirl.api.Html;
import services.admin.AdminFreshService;
import services.admin.AdminUserService;
import services.product.ProductService;
import services.user.UserService;
import utils.AjaxHelper;
import utils.Numbers;
import controllers.admin.AdminAuthenticated;
import controllers.admin.BaseAdminController;
import forms.ProductManageForm;
import forms.UserQueryForm;
import forms.admin.FreshTaskForm;
import forms.admin.FreshTaskQueryForm;

/**
 * @author luobotao
 *
 */
@Named
@Singleton
public class UserController extends BaseAdminController {

    private final ALogger logger = Logger.of(UserController.class);

    private final AdminUserService adminUserService;
    private final AdminFreshService adminFreshService;
    private final ProductService productService;
    private final UserService userService;

    private final Form<UserQueryForm> userQueryForm = Form.form(UserQueryForm.class);


    @Inject
    public UserController(final UserService userService,final AdminUserService adminUserService,final AdminFreshService adminFreshService,final ProductService productService) {
        this.userService = userService;
        this.adminUserService = adminUserService;
        this.adminFreshService = adminFreshService;
        this.productService = productService;
    }


	
    
    /**
     * 用户管理
     * @return
     */
    @AdminAuthenticated()
    public Result usersManage() {
    	AdminUser adminUser = getCurrentAdminUser();
    	Form<UserQueryForm> form = userQueryForm.bindFromRequest();
    	UserQueryForm userForm = new UserQueryForm();
		if (!form.hasErrors()) {
			userForm = form.get();
        }
		if("3".equals(adminUser.getAdminType())){//管理员类型 1普通用户 2外部用户 3商铺管理员
			userForm.fromUid = adminUser.getUid();
		}
		List<GroupInfo> groupList = userService.findAllGroup();
    	Page<User> userList = userService.findUserPage(userForm);
    	return ok(views.html.user.usersManage.render(adminUser,Html.apply(UserService.groupInfoList2Html(groupList, userForm.gid)),userList));
    }
    /**
     * 新建或编辑用户
     * @return
     */
    @AdminAuthenticated()
    public Result newOrUpdateUser() {
    	return ok(views.html.user.newOrUpdateUser.render(Html.apply(UserService.groupInfoList2Html(userService.findAllGroup(), 1))));
    }
    /**
     * 保存用户
     * @return
     */
    @AdminAuthenticated()
    public Result saveUser() {
    	User user = new User();
    	String gid = Form.form().bindFromRequest().get("gid");//组ID
    	String phone = Form.form().bindFromRequest().get("phone");//手机
    	String nickname = Form.form().bindFromRequest().get("nickname");//昵称
    	user.setGid(Numbers.parseLong(gid, 0L));
    	user.setPhone(phone);
    	user.setNickname(nickname);
    	user.setPasswords("123456");
    	user.setActive(1);
    	user.setDate_add(new Date());
    	user.setDateUpd(new Date());
    	user = userService.saveUser(user);
    	logger.info(gid);
    	logger.info(phone);
    	logger.info(nickname);
    	return ok(Json.toJson("1"));
    }
    /**
     * 用户列表(弹窗)
     * @return
     */
    @AdminAuthenticated()
    public Result userListForJson() {
    	Form<UserQueryForm> form = userQueryForm.bindFromRequest();
    	UserQueryForm userForm = new UserQueryForm();
    	if (!form.hasErrors()) {
    		userForm = form.get();
    	}
    	if (userForm.userId==null && StringUtils.isBlank(userForm.phone)&& StringUtils.isBlank(userForm.userName)) {
    		return ok(views.html.user.userList.render(new ArrayList<User>()));
    	}
    	List<User> userList = userService.findUserList(userForm);
    	return ok(views.html.user.userList.render(userList));
    }
    /**
     * 用户列表(弹窗)批量
     * @return
     */
    @AdminAuthenticated()
    public Result userListBatchForJson() {
    	Form<UserQueryForm> form = userQueryForm.bindFromRequest();
    	UserQueryForm userForm = new UserQueryForm();
    	if (!form.hasErrors()) {
    		userForm = form.get();
    	}
    	if (userForm.gid==null && userForm.userId==null && StringUtils.isBlank(userForm.phone)&& StringUtils.isBlank(userForm.userName)&& StringUtils.isBlank(userForm.danyanFlag)) {
    		return ok(views.html.user.userList.render(new ArrayList<User>()));
    	}
    	List<User> userList = userService.findUserList(userForm);
    	return ok(views.html.user.userListBatch.render(userList));
    }
    /**
     * 用户(JSON)
     * @return
     */
    @AdminAuthenticated()
    public Result findUserByIdJson(Long id) {
    	User user = userService.findUserById(id);
    	return ok(Json.toJson(user));
    }
    
    /**
     * 用户列表(JSON)
     * @return
     */
    @AdminAuthenticated()
    public Result userListWithUidsForJson() {
    	String users = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "users");
		List<User> userList = new ArrayList<User>();
		JsonNode json = Json.parse(users);
		for(JsonNode temp : json){
			User User = userService.findUserById(temp.asLong());
			if(User!=null){
				userList.add(User);
			}
		}
		return ok(Json.toJson(userList));
    }
    
   

}
