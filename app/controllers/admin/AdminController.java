package controllers.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;

import com.google.common.collect.Lists;

import forms.admin.AdminLoginForm;
import forms.admin.AdminUserQueryForm;
import forms.admin.CreateOrUpdateAdminUserForm;
import forms.admin.FreshTaskForm;
import forms.admin.FreshTaskQueryForm;
import models.OrderProduct;
import models.Parcels;
import models.ShoppingOrder;
import models.admin.AdminFreshTask;
import models.admin.AdminUser;
import models.admin.OperateLog;
import models.admin.Role;
import models.admin.SysRoleMenu;
import models.admin.SysUserRole;
import models.admin.Sys_Menu;
import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.twirl.api.Html;
import services.OperateLogService;
import services.ServiceFactory;
import services.admin.AdminFreshService;
import services.admin.AdminUserService;
import services.admin.SystemService;
import services.kjt.KjtService;
import services.product.ProductService;
import utils.AjaxHelper;
import utils.Constants;
import utils.Constants.LoveLyStatus;
import utils.Constants.ParcelStatus;
import utils.Constants.PayMethod;
import utils.Errors;
import utils.Numbers;
import vo.CloseTaskVO;

/**
 * @author luobotao
 *
 */
@Named
@Singleton
public class AdminController extends BaseAdminController {

    private final ALogger logger = Logger.of(AdminController.class);

    private final AdminUserService adminUserService;
    private final AdminFreshService adminFreshService;
    private final ProductService productService;
	private final OperateLogService operateLogService;
	private final KjtService kjtService;
	private final SystemService systemService;
    private final Form<FreshTaskForm> freshTaskForm = Form.form(FreshTaskForm.class);
    private final Form<AdminLoginForm> adminLoginForm = Form.form(AdminLoginForm.class);
    private final Form<FreshTaskQueryForm> freshTaskQueryForm = Form.form(FreshTaskQueryForm.class);
    private final Form<CreateOrUpdateAdminUserForm> createOrUpdateAdminUserForm = Form.form(CreateOrUpdateAdminUserForm.class);
    private final String error = "error";
    private final String errorSave = "errorSave";


    @Inject
    public AdminController(final AdminUserService adminUserService,final AdminFreshService adminFreshService,final ProductService productService,final OperateLogService operateLogService,final SystemService systemService,final KjtService kjtService) {
        this.adminUserService = adminUserService;
        this.adminFreshService = adminFreshService;
        this.productService = productService;
        this.operateLogService = operateLogService;
        this.systemService = systemService;
        this.kjtService = kjtService;
    }


	public Result index() {
		
		String currentId = AdminSession.get();
		if(!StringUtils.isBlank(currentId)){
			AdminUser adminUserCurrent = adminUserService.getAdminUser(Long.valueOf(currentId));
			if(adminUserCurrent!=null){
				return redirect(controllers.admin.routes.AdminController.main());
			}
		}
		
		Form<AdminLoginForm> form = adminLoginForm.bindFromRequest(request());
		if (form==null || form.hasErrors() || form.get()==null) {
			flash(error, Errors.wrapedWithDiv(form.errors()));
			return redirect(controllers.admin.routes.AdminController.login());
		} else {
			if(StringUtils.isBlank(form.get().username)||StringUtils.isBlank(form.get().password)){
				flash(error, "请重新登录。");
				return redirect(controllers.admin.routes.AdminController.login());
			}
			AdminUser adminUser = adminUserService.login(form.get());
			if (adminUser == null) {
				flash(error, "用户名,密码不匹配");
				return redirect(controllers.admin.routes.AdminController.login());
			}
			if (adminUser.getActive() == 0) {
				flash(error, "此用户已停用，请联系管理员");
				return redirect(controllers.admin.routes.AdminController.login());
			}
			adminUser.setLastip(request().remoteAddress());
			adminUser.setLogin_count(adminUser.getLogin_count() + 1);
			adminUser.setDate_login(new Date());
			adminUser = adminUserService.updateAdminUser(adminUser);
			AdminSession.put(String.valueOf(adminUser.getId()));
			ServiceFactory.getCacheService().setObject(Constants.USER+adminUser.getId(), adminUser, 0);
			operateLogService.saveLoginLog(adminUser.getId(),adminUser.getUsername(), request().remoteAddress());
			return redirect(controllers.admin.routes.AdminController.main());
		}
	}
	
	@AdminAuthenticated()
	public Result main() {
		AdminUser adminUser =getCurrentAdminUser();
		List<SysUserRole> roleList = systemService.findRolesByAdminId(adminUser.getId());
		List<SysRoleMenu> roleMenuList = Lists.newArrayList();
		for(SysUserRole sysUserRole : roleList){
			List<SysRoleMenu> menuListTemp = systemService.findRoleMenuByRoleId(sysUserRole.getRoleid());
			roleMenuList.removeAll(menuListTemp);
			roleMenuList.addAll(menuListTemp);
		}
		List<Sys_Menu> menuList = Lists.newArrayList();
		for(SysRoleMenu sysRoleMenu : roleMenuList){//循环具有的权限，生成菜单
			Sys_Menu sysMenu = systemService.findSysMenuById(sysRoleMenu.getMenuid());
			menuList.add(sysMenu);
		}
		List<Sys_Menu> rootMenuList = SystemService.getRootNode(menuList);
		StringBuilder menuResult = new StringBuilder();
		for(Sys_Menu sys_Menu : rootMenuList){
          
          StringBuilder menuStr = new StringBuilder("<li class=\"treeview\"><a href=\"#\"><i class=\"fa fa-bar-chart-o fa-fw\"></i> <span>");
          menuStr.append(sys_Menu.getMenuName());
          menuStr.append("</span> <i class=\"fa fa-angle-left pull-right\"></i></a>");
          List<Sys_Menu> nodeMenuList = SystemService.getChild(sys_Menu, menuList);
          if(!nodeMenuList.isEmpty()){
        	  menuStr.append("<ul class=\"treeview-menu\">");
        	  for(Sys_Menu nodeMenu : nodeMenuList){
        		  menuStr.append("<li><a href=\"");
        		  menuStr.append(nodeMenu.getMenuUrl());
        		  menuStr.append("\" target=\"mainFrame\">");
        		  menuStr.append(nodeMenu.getMenuName());
        		  menuStr.append("</a></li>");
        	  }
        	  menuStr.append(" </ul>");
          }
          menuStr.append("</li>");
          menuResult.append(menuStr);
		}
		logger.info(menuResult.toString());
		return ok(views.html.header.render(getCurrentAdminUser(),Html.apply(menuResult.toString())));
	}
	
    public Result login() {
        return ok(views.html.login.render(flash(error)));
    }
    
    /**
     * 管理员管理
     * @return
     */
    @AdminAuthenticated()
    public Result users() {
    	AdminUser adminUser =getCurrentAdminUser();
    	List<AdminUser> adminUsers = adminUserService.listAdminUsers(new AdminUserQueryForm());
        return ok(views.html.admin.users.render(adminUser,adminUsers));
    }
    /**
     * 根据管理员ID获取该管理员
     * @return
     */
    @AdminAuthenticated()
    public Result getAdminUser(long id) {
    	AdminUser current = adminUserService.getAdminUser(id);
    	if(current==null)
    		current = new AdminUser();
    	return ok(views.html.admin.addOrEditAdminUser.render(current,flash(errorSave),Html.apply(Constants.AdminType.type2HTML(current.getAdminType()))));
    }
    
    @AdminAuthenticated()
    public Result saveUser() {
    	Form<CreateOrUpdateAdminUserForm> form = createOrUpdateAdminUserForm
                .bindFromRequest(request());
        if (form.hasErrors()) {
            flash(errorSave, Errors.wrapedWithDiv(form.errors()));
            return redirect(controllers.admin.routes.AdminController.getAdminUser(Numbers.parseLong(form.data().get("id"), -1L)));
        } else {
            AdminUser adminUser = null;
            String reason = null;
            try {
            	OperateLog operateLog =operateLogService.createAdminUserLog(getCurrentAdminUser().getId(),getCurrentAdminUser().getUsername(), request().remoteAddress(), "");
            	adminUser = adminUserService.createAdminUser(operateLog,form.get());
            } catch (Throwable t) {
                reason = Errors.trace(t);
            }
            if (adminUser == null) {
                flash(errorSave, "<span>创建用户失败,原因如下</span><div>" + reason + "</div>");
                return redirect(controllers.admin.routes.AdminController.getAdminUser(form.get().id==null?-1L:form.get().id));
            }
            flash(errorSave, "<span>创建管理员" + adminUser.getUsername() + "成功!</span>");
        }
    	return redirect(routes.AdminController.users());
    }
    
    /**
     * 修改管理员状态
     * @return
     */
    @AdminAuthenticated()
    public Result updateAdminStatus() {
    	String adminIdForStatus = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "adminIdForStatus");
    	String adminStatus = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "adminStatus");
    	logger.info(adminIdForStatus);
    	logger.info(adminStatus);
    	int rows = adminUserService.updateAdminUserStatus(Numbers.parseLong(adminIdForStatus, 0L),Numbers.parseInt(adminStatus, 0));
    	return ok(Json.toJson(rows));
    }
    
    /**
     * 根据管理员ID获取该管理员的角色并进行修改
     * @return
     */
    @AdminAuthenticated()
    public Result usersRole(long id) {
    	AdminUser current = adminUserService.getAdminUser(id);
    	List<Role> roleList = systemService.findAllRole();
    	if(current==null)
    		current = new AdminUser();
    	return ok(views.html.admin.userRole.render(current,flash(errorSave),roleList));
    }
    
    /**
     * 修改管理员角色
     * @return
     */
    @AdminAuthenticated()
    public Result updateAdminRole() {
    	String adminId = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "adminId");
    	String roleValue = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "roleValue");
    	int rows = adminUserService.updateAdminRole(Numbers.parseLong(adminId, 0L),Numbers.parseInt(roleValue, 0));
    	return ok(Json.toJson(rows));
    }
    /**
     * 用户列表(弹窗)批量
     * @return
     */
    @AdminAuthenticated()
    public Result adminUserListBatchForJson() {
    	String roleid = AjaxHelper.getHttpParam(request(), "roleid");
    	AdminUserQueryForm form = new AdminUserQueryForm();
    	if(!StringUtils.isBlank(roleid)){
    		form.roleid = Numbers.parseInt(roleid, 0);
    	}
    	List<AdminUser> adminUsers = adminUserService.listAdminUsers(form);
        return ok(views.html.admin.adminUsersBatch.render(adminUsers));
    }
    
    //任务管理
    @AdminAuthenticated()
    public Result freshTaskManage() {
    	AdminUser adminUser = getCurrentAdminUser();
    	FreshTaskQueryForm formPage = new FreshTaskQueryForm();
    	String page = request().getQueryString("page");
    	if(!StringUtils.isBlank(page)){
    		formPage.page= Integer.valueOf(page);
    	}
    	Page<AdminFreshTask> freshTasks = adminFreshService.query(formPage);
    	return ok(views.html.admin.freshTaskManage.render(adminUser,freshTasks));
    }
    
    //创建任务
    @AdminAuthenticated()
    public Result createFreshTask() {
    	AdminUser adminUser =getCurrentAdminUser();
    	Form<FreshTaskForm> form = freshTaskForm.bindFromRequest();
    	FreshTaskForm freshTaskForm = new FreshTaskForm();
        if (form.hasErrors()) {
        	flash(error, Errors.wrapedWithDiv(form.errors()));
        	return ok(Errors.wrapedWithDiv(form.errors()));
        }else{
        	freshTaskForm = form.get();
        	AdminFreshTask adminFreshTask = AdminFreshTask.convertFromForm(freshTaskForm, adminUser);
        	adminFreshService.saveAdminFreshTask(adminFreshTask);
        }
    	return redirect(routes.AdminController.freshTaskManage());
    }
    //结束任务
    @AdminAuthenticated()
    public Result closeFreshTaskByIds(String idsString) {
    	CloseTaskVO vo = new CloseTaskVO();
        String[] ids = idsString.split(",");
        List<Long> longs = new ArrayList<>(ids.length);
        for (int i = 0, size = ids.length; i < size; i++) {
            longs.add(Numbers.parseLong(ids[i], 0L));
        }
        if (longs.size() > 0) {
            vo = adminFreshService.closeFreshTaskByIds(getCurrentAdminUser(), longs);
        }
        return ok(Json.toJson(vo));
    }
    
    //任务订单列表
    @AdminAuthenticated()
    public Result shopingOrder() {
    	Form<FreshTaskQueryForm> form = freshTaskQueryForm.bindFromRequest();
    	FreshTaskQueryForm formPage = new FreshTaskQueryForm();
        if (!form.hasErrors()) {
        	formPage = form.get();
        }
    	
    	List<ShoppingOrder> shoppingOrderList = productService.queryShoppingOrderList(formPage);
    	List<Long> orderIds = new ArrayList<>();
    	for(ShoppingOrder shoppingOrder:shoppingOrderList){
    		orderIds.add(shoppingOrder.getId());
    	}
    	formPage.orderIds = orderIds;
    	Page<OrderProduct> orderProductPage = productService.queryorderProductPage(formPage);
    	if(orderProductPage!=null && orderProductPage.getContent() != null && orderProductPage.getContent().size() > 0){
    		for(OrderProduct orderProduct : orderProductPage.getContent()){
    			logger.info(orderProduct.getpId()+"==============");
    			orderProduct.setProduct(productService.findProductById(orderProduct.getpId()));
    			orderProduct.setShoppingOrder(productService.getShoppingOrderById(orderProduct.getOrderId()));
    			orderProduct.setAdminUser(adminUserService.getAdminUser(orderProduct.getCid()));
    		}
    		
    	}
		return ok(views.html.admin.shopingOrder.render(orderProductPage,
				Html.apply(PayMethod.status2HTML(formPage.payMethod)),
				Html.apply(LoveLyStatus.status2HTML(formPage.ordertype))));
    }
    
    /**
     * 任务管理模块下的包裹管理
     * @return
     */
    @AdminAuthenticated()
    public Result pardelsManage() {
    	Form<FreshTaskQueryForm> form = freshTaskQueryForm.bindFromRequest();
    	FreshTaskQueryForm formPage = new FreshTaskQueryForm();
        if (!form.hasErrors()) {
        	formPage = form.get();
        }
        if(StringUtils.isBlank(formPage.taskId)){
        	formPage.taskId = "-2";
        }
        List<ShoppingOrder> shoppingOrderList = productService.queryShoppingOrderList(formPage);
        List<Long> orderIds = new ArrayList<>();
    	for(ShoppingOrder shoppingOrder:shoppingOrderList){
    		orderIds.add(shoppingOrder.getId());
    	}
    	formPage.orderIds = orderIds;
    	Page<Parcels> pardelsPage = productService.queryPardelsPage(formPage);
    	if(pardelsPage!=null && pardelsPage.getContent() != null && pardelsPage.getContent().size() > 0){
    		for(Parcels parcels : pardelsPage.getContent()){
    			parcels.setProductList( productService.queryProductListByParcelsId(parcels.getId()));
    		}
    		
    	}
		return ok(views.html.admin.parcelsManage.render(pardelsPage,
				Html.apply(ParcelStatus.status2HTML(formPage.parcelStatus))));
    }
    
    
    
    @AdminAuthenticated
    public Result logout() {
    	operateLogService.saveLogoutLog(getCurrentAdminUser().getId(),getCurrentAdminUser().getUsername(), request().remoteAddress());
        AdminSession.remove();
        return redirect(controllers.admin.routes.AdminController.login());
    }

}
