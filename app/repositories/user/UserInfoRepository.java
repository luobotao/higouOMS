package repositories.user;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.user.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

@Named
@Singleton
public interface UserInfoRepository extends JpaRepository<User, Long>,JpaSpecificationExecutor<User> {

	@Query(value="SELECT * FROM `user` WHERE phone=?1",nativeQuery=true)
	List<User> findByPhone(String phone);

	@Query(value="SELECT * FROM `user` WHERE gid=?1",nativeQuery=true)
	List<User> findByGid(int gid);

	List<User> findByPostmanid(String postmanid);
	
}