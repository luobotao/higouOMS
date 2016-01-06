package repositories.admin;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import models.admin.Sys_Menu;

/**
 * 系统菜单Repository
 * @author luobotao
 * @Date 2015年9月14日
 */
@Named
@Singleton
public interface SysMenuRepository extends JpaRepository<Sys_Menu, Integer>,JpaSpecificationExecutor<Sys_Menu> {

}