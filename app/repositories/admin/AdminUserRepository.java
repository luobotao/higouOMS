package repositories.admin;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.admin.AdminUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
/**
 * Provides CRUD functionality for accessing people. Spring Data auto-magically takes care of many standard
 * operations here.
 */
@Named
@Singleton
public interface AdminUserRepository extends JpaRepository<AdminUser, Long>,JpaSpecificationExecutor<AdminUser> {
	List<AdminUser> findByUsernameAndPasswd(String username, String passwd);

	List<AdminUser> findByUsername(String username);

	@Query(value="SELECT * FROM admin WHERE adminType = 2",nativeQuery=true)
	List<AdminUser> queryAllLyAdminUser();

	@Query(value="SELECT a.* FROM admin a, adminproduct ap WHERE a.id=ap.adminid and a.adminType = 2 AND ap.pid=?1 ",nativeQuery=true)
	List<AdminUser> queryAdminUserByPid(Long pid);

	@Query(value="SELECT a.* FROM admin a, adminproduct ap WHERE a.id=ap.adminid and a.adminType = 2 AND ap.pid=?1 and a.id=?2",nativeQuery=true)
	List<AdminUser> queryAdminUserByPidAndAdminId(Long pid, int adminid);

	@Modifying
	@Query(value="UPDATE AdminUser SET active = ?2 WHERE id =?1")
	int updateAdminUserStatus(Long adminid, Integer activate);

	@Modifying
	@Query(value="UPDATE AdminUser SET roleId = ?2 WHERE id =?1")
	int updateAdminRole(Long adminid, Integer role);
}