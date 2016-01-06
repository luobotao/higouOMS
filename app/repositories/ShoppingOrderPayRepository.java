package repositories;

import javax.inject.Named;
import javax.inject.Singleton;

import models.ShoppingOrderPay;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

@Named
@Singleton
public interface ShoppingOrderPayRepository extends JpaRepository<ShoppingOrderPay, Long>,
		JpaSpecificationExecutor<ShoppingOrderPay> {

	@Query(value="SELECT * FROM shopping_OrderPay WHERE orderId =?1",nativeQuery=true)
	ShoppingOrderPay findByOrderId(long orderId);
	
}