package repositories.admin;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.admin.AdminFreshTask;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

@Named
@Singleton
public interface AdminFreshRepository extends JpaRepository<AdminFreshTask, Long>,
		JpaSpecificationExecutor<AdminFreshTask> {

	@Query(value="select * from freshTask t where status=1 and id in ?1", nativeQuery = true)
	List<AdminFreshTask> queryDoingTasksByIds(List<Long> ids);
}