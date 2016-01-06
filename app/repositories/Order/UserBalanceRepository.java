package repositories.Order;

import javax.inject.Named;
import javax.inject.Singleton;

import models.user.UserBalance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * Provides CRUD functionality for accessing people. Spring Data auto-magically
 * takes care of many standard operations here.
 */
@Named
@Singleton
public interface UserBalanceRepository extends JpaRepository<UserBalance, Long>,
		JpaSpecificationExecutor<UserBalance> {

	@Query(value="select * from user_balance where userId=?1",nativeQuery=true)
	UserBalance getbalance(Long uid);
	

	
	
}