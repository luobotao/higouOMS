package controllers.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

import forms.admin.SysConfigQueryForm;
import models.admin.Role;
import models.admin.SysRoleMenu;
import models.admin.SysUserRole;
import models.admin.Sys_Menu;
import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import services.admin.SystemService;
import utils.AjaxHelper;
import utils.Numbers;
import vo.TreeVO;


/**
 * 系统管理
 * @author luobotao
 * @Date 2015年9月14日
 */
@Named
@Singleton
public class SystemController extends BaseAdminController {

    private final ALogger logger = Logger.of(SystemController.class);

    private final SystemService systemService;

    @Inject
    public SystemController(final SystemService systemService) {
        this.systemService = systemService;
    }

    /**
     * 菜单列表
     */
    @AdminAuthenticated
    public Result sysMenuList() {
    	SysConfigQueryForm form = new SysConfigQueryForm();
    	List<Sys_Menu> sys_MenuList = systemService.findAllMenu(form);
    	List<Sys_Menu> newList=new ArrayList<Sys_Menu>();//用来临时存储排序后的菜单列表  
    	List<Sys_Menu> result=SystemService.setRootNode(newList,sys_MenuList);  
        return ok(views.html.admin.systemManage.render(result));
    }
    
    /**
     * 保存菜单/按钮
     */
    @AdminAuthenticated
    public Result saveSysMenu() {
    	SysConfigQueryForm form = Form.form(SysConfigQueryForm.class).bindFromRequest().get();
    	Sys_Menu sys_Menu = new Sys_Menu();
    	sys_Menu.setIsLeaf(form.isLeaf);
    	sys_Menu.setMenuName(form.menuName);
    	sys_Menu.setMenuOrder(form.menuOrder);
    	if(form.isLeaf==1){//子菜单
    		sys_Menu.setParentMenuId(form.parentMenuId);
    		sys_Menu.setMenuUrl(form.menuUrl);
    		sys_Menu.setIsButton(form.isButton);//是否是按钮
    	}
    	sys_Menu.setDateAdd(new Date());
    	sys_Menu = systemService.saveSysMenu(sys_Menu);
        return redirect("/admin/sysMenuList");
    }
    
    
    
    /**
     * 角色列表
     */
    @AdminAuthenticated
    public Result sysRoleList() {
    	List<Role> sysRoleList = systemService.findAllRole();
    	return ok(views.html.admin.roleManage.render(sysRoleList));
    }
    
    /**
     * 跳转至新建或编辑角色页面
     */
    public Result newOrUpdateRole(){
    	int roleId = Numbers.parseInt(AjaxHelper.getHttpParam(request(), "id"), 0);
    	Role role = systemService.findRoleByRoleId(roleId);
    	if(role==null){
    		role = new Role();
    	}
    	return ok(views.html.admin.newOrUpdateRole.render(role));
    }
    
    /**
     * 保存菜单/按钮
     */
    @AdminAuthenticated
    public Result saveRole() {
    	String roleid = Form.form().bindFromRequest().get("roleid");//
    	Role role = systemService.findRoleByRoleId(Numbers.parseInt(roleid, 0));
    	if(role==null){
    		role = new Role();
    		role.setDateAdd(new Date());
    	}
    	String roleName = Form.form().bindFromRequest().get("roleName");//
    	String remark = Form.form().bindFromRequest().get("remark");//
    	role.setRemark(remark);
    	role.setRole(roleName);
    	role = systemService.saveRole(role);
    	systemService.deleteByRoleId(role.getId());
    	String checkedIds = Form.form().bindFromRequest().get("checkedIds");//
    	String menuIdArray[] = checkedIds.split(",");
		for(String menuId : menuIdArray){
			if(StringUtils.isBlank(menuId)){
				continue;
			}else{
				SysRoleMenu sysRoleMenu = new SysRoleMenu();
				sysRoleMenu.setDateAdd(new Date());
				sysRoleMenu.setRoleid(role.getId());
				sysRoleMenu.setMenuid(Numbers.parseInt(menuId, 0));
				systemService.saveSysRoleMenu(sysRoleMenu);
			}
		}
    	
    	return ok(Json.toJson("1"));
    }
    
    /**
     * 根据角色ID获取该角色具有的权限
     */
    @AdminAuthenticated
    public Result getRole(){
    	String roleid = AjaxHelper.getHttpParam(request(), "roleid");
    	List<Integer> sysRoleMenuidList = systemService.findMenuidByRoleId(Numbers.parseInt(roleid, 0));
    	List<Sys_Menu> sys_MenuList = systemService.findAllMenu(new SysConfigQueryForm());
    	List<TreeVO> treeVOList = Lists.newArrayList();
    	for(Sys_Menu sys_Menu : sys_MenuList){
    		TreeVO treeVO = new TreeVO();
    		treeVO.id = sys_Menu.getId()==null?0:sys_Menu.getId().intValue();
    		treeVO.pId = sys_Menu.getParentMenuId()==null?0:sys_Menu.getParentMenuId().intValue();
    		treeVO.name = sys_Menu.getMenuName();
    		if(sysRoleMenuidList.contains(sys_Menu.getId())){
    			treeVO.checked = true;
    		}else{
    			treeVO.checked = false;
    		}
    		treeVOList.add(treeVO);
    	}
    	return ok(Json.toJson(treeVOList));
    }
    
    /**
     * 给选中的用户分配角色
     * @return
     */
    @AdminAuthenticated()
    public Result assignRoleToAdminUsers() {
    	String adminUserIds = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "adminUsers");
    	String role = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "role");
    	JsonNode json = Json.parse(adminUserIds);
    	for(JsonNode adminidTemp : json){
    		Long adminid = adminidTemp.asLong();
    		SysUserRole sysUserRole = new SysUserRole();
    		sysUserRole.setRoleid(Numbers.parseInt(role, 0));
    		sysUserRole.setUserid(adminid);
    		sysUserRole.setDateAdd(new Date());
    		sysUserRole = systemService.saveSysUserRole(sysUserRole);
    	}
    	return ok(Json.toJson("1"));
    }
    
    
    /**
     * 菜单列表
     */
    @AdminAuthenticated
    public Result sysMenuList2() {
    	SysConfigQueryForm form = new SysConfigQueryForm();
    	List<Sys_Menu> sys_MenuList = systemService.findAllMenu(form);
    	List<Sys_Menu> newList=new ArrayList<Sys_Menu>();//用来临时存储排序后的菜单列表  
    	List<Sys_Menu> result=SystemService.setRootNode(newList,sys_MenuList);  
    	return ok(views.html.admin.systemManage2.render(result));
    }
    
   
       

}
