package repositories.admin;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import models.admin.SysRoleMenu;

/**
 * 角色与菜单中间表Repository
 * @author luobotao
 * @Date 2015年9月14日
 */
@Named
@Singleton
public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu, Integer>,JpaSpecificationExecutor<SysRoleMenu> {

	void deleteByRoleid(Integer roleid);

	List<SysRoleMenu> findByRoleid(Integer roleid);

	@Query(value="select rm.menuid from SysRoleMenu rm where rm.roleid=?1")
	List<Integer> findMenuidByRoleId(Integer roleid);

}