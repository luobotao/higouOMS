package services.admin;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import forms.admin.SysConfigQueryForm;
import models.admin.Role;
import models.admin.SysRoleMenu;
import models.admin.SysUserRole;
import models.admin.Sys_Menu;
import play.Logger;
import repositories.admin.SysMenuRepository;
import repositories.admin.SysRoleMenuRepository;
import repositories.admin.SysRoleRepository;
import repositories.admin.SysUserRoleRepository;

/**
 * 系统管理
 * @author luobotao
 * @Date 2015年9月14日
 */
@Named
@Singleton
public class SystemService {


    @Inject
    private SysMenuRepository sysMenuRepository;
    @Inject
    private SysRoleRepository sysRoleRepository;
    @Inject
    private SysRoleMenuRepository sysRoleMenuRepository;
    @Inject
    private SysUserRoleRepository sysUserRoleRepository;

	public List<Sys_Menu> findAllMenu(final SysConfigQueryForm form) {
		return sysMenuRepository.findAll(
				new Specification<Sys_Menu>() {
					@Override
					public Predicate toPredicate(Root<Sys_Menu> sys_Menu, CriteriaQuery<?> query,
							CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<>();
						Path<Integer> isLeaf = sys_Menu.get("isLeaf");
						Path<Long> parentMenuId = sys_Menu.get("parentMenuId");
						if (form.isLeaf!=-1) {
							predicates.add(builder.equal(isLeaf, form.isLeaf));
						}
						if (form.parentMenuId!=-1) {
							predicates.add(builder.equal(parentMenuId, form.parentMenuId));
						}
						Predicate[] param = new Predicate[predicates.size()];
						predicates.toArray(param);
						return query.where(param).getRestriction();
					}
				},
				new Sort(Sort.Direction.DESC,"parentMenuId","menuOrder"));
	}

	/**
	 * 创建菜单
	 * @param sys_Menu
	 * @return
	 */
	public Sys_Menu saveSysMenu(Sys_Menu sys_Menu) {
		return sysMenuRepository.save(sys_Menu);
	}

	/**
	 * 获取所有角色
	 * @param form
	 * @return
	 */
	public List<Role> findAllRole() {
		return sysRoleRepository.findAll(new Sort(Sort.Direction.ASC, "id"));
	}

	/**
	 * 根据id获取role
	 * @param roleId
	 * @return
	 */
	public Role findRoleByRoleId(int roleId) {
		return sysRoleRepository.findOne(roleId);
	}

	/**
	 * 保存角色
	 * @param role
	 * @return
	 */
	public Role saveRole(Role role) {
		return sysRoleRepository.save(role);
	}

	/**
	 * 根据角色ID将该角色下的所有权限全部删除
	 * @param roleid
	 */
	@Transactional
	public void deleteByRoleId(Integer roleid) {
		sysRoleMenuRepository.deleteByRoleid(roleid);
	}
	/**
	 * 保存角色与权限中间表
	 * @param sysRoleMenu
	 * @return
	 */
	public SysRoleMenu saveSysRoleMenu(SysRoleMenu sysRoleMenu) {
		return sysRoleMenuRepository.save(sysRoleMenu);
	}

	/**
	 * 根据roleid获取该角色具有的权限
	 * @param roleid
	 * @return
	 */
	public List<SysRoleMenu> findRoleMenuByRoleId(Integer roleid) {
		return sysRoleMenuRepository.findByRoleid(roleid);
	}
	/**
	 * 根据roleid获取该角色具有的权限菜单ID
	 * @param roleid
	 * @return
	 */
	public List<Integer> findMenuidByRoleId(Integer roleid) {
		return sysRoleMenuRepository.findMenuidByRoleId(roleid);
	}

	/**
	 * 保存用户与角色关联表
	 * @param sysUserRole
	 * @return
	 */
	public SysUserRole saveSysUserRole(SysUserRole sysUserRole) {
		return sysUserRoleRepository.save(sysUserRole);
	}

	/**
	 * 根据管理员ID获取该管理员具有的角色
	 * @param adminId
	 * @return
	 */
	public List<SysUserRole> findRolesByAdminId(Long adminId) {
		return sysUserRoleRepository.findByUserid(adminId);
	}

	/**
	 * 根据menuId获取该menu
	 * @param menuid
	 * @return
	 */
	public Sys_Menu findSysMenuById(Integer menuid) {
		return sysMenuRepository.findOne(menuid);
	}  
	
	 //获得子节点  
    public static List<Sys_Menu> getChild(Sys_Menu rootNode,List<Sys_Menu> treeList){  
        List<Sys_Menu> childList = new ArrayList<Sys_Menu>();  
        for(Sys_Menu sys_Menu:treeList){ 
            if(sys_Menu.getParentMenuId()!=null && sys_Menu.getParentMenuId().longValue()==rootNode.getId().longValue()){//父节点ID等于根节点ID
            	childList.add(sys_Menu);  
            }  
        }  
        return childList;  
    }  
  
    //获得根节点  
    public static List<Sys_Menu> getRootNode(List<Sys_Menu> treeList){  
        List<Sys_Menu> rootList = new ArrayList<Sys_Menu>();  
        for(Sys_Menu sys_Menu:treeList){  
            if(sys_Menu.getIsLeaf()==0){//根节点
                rootList.add(sys_Menu);  
            }  
        }  
        return rootList;  
    }  
  
    //向新list中装入根节点并递归子节点  
    public static List<Sys_Menu> setRootNode(List<Sys_Menu> newList,List<Sys_Menu> treeList){  
        List<Sys_Menu> rootNode = getRootNode(treeList);  
        for(Sys_Menu sys_Menu:rootNode){  
            newList.add(sys_Menu);  
            sortNode(newList,sys_Menu,treeList);  
        }  
          
        return newList;  
    }  
  
  
    //递归子节点  
    public static List<Sys_Menu> sortNode(List<Sys_Menu> newList,Sys_Menu rootNode,List<Sys_Menu> treeList){  
        List<Sys_Menu> childList = getChild(rootNode,treeList); 
        for(Sys_Menu sys_Menu:childList){  
            newList.add(sys_Menu);  
            sortNode(newList,sys_Menu,treeList);  
        }  
        return childList;  
    }

	
}
