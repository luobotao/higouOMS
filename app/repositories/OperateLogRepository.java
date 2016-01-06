package repositories;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import models.admin.OperateLog;

/**
 * 日志操作
 * @author luobotao
 * @Date 2015年9月11日
 */
@Named
@Singleton
public interface OperateLogRepository extends JpaRepository<OperateLog, Long>,
		JpaSpecificationExecutor<OperateLog> {
	

	
	
}