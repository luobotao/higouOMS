package repositories.admin;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import models.admin.SysUserRole;

/**
 * 用户角色关联表的Repository
 * @author luobotao
 * @Date 2015年9月14日
 */
@Named
@Singleton
public interface SysUserRoleRepository extends JpaRepository<SysUserRole, Integer>,JpaSpecificationExecutor<SysUserRole> {

	List<SysUserRole> findByUserid(Long adminId);

}