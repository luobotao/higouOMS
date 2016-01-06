package repositories.Order;

import javax.inject.Named;
import javax.inject.Singleton;

import models.RefundInfo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * Provides CRUD functionality for accessing people. Spring Data auto-magically
 * takes care of many standard operations here.
 */
@Named
@Singleton
public interface RefundInfoRepository extends JpaRepository<RefundInfo, Long>,
		JpaSpecificationExecutor<RefundInfo> {

	@Query(value="SELECT * FROM refund WHERE `orderCode`=?1 and `status`=?2 limit 1",nativeQuery=true)
	RefundInfo queryRefundInfoWithOrdercode(String ordercode, int status);
	

	
	
}