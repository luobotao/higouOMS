package repositories.admin;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import models.admin.Role;

/**
 * 角色Repository
 * @author luobotao
 * @Date 2015年9月14日
 */
@Named
@Singleton
public interface SysRoleRepository extends JpaRepository<Role, Integer>,JpaSpecificationExecutor<Role> {

}