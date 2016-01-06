package repositories.user;

import javax.inject.Named;
import javax.inject.Singleton;

import models.user.GroupInfo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 用户组
 * @author luobotao
 * @Date 2015年9月6日
 */
@Named
@Singleton
public interface GroupInfoRepository extends JpaRepository<GroupInfo, Long>,JpaSpecificationExecutor<GroupInfo> {
	
	
}