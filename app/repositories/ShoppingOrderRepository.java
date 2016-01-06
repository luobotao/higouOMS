package repositories;

import javax.inject.Named;
import javax.inject.Singleton;

import models.ShoppingOrder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Named
@Singleton
public interface ShoppingOrderRepository extends JpaRepository<ShoppingOrder, Long>,
		JpaSpecificationExecutor<ShoppingOrder> {

	ShoppingOrder findByOrderCode(String orderCode);
	
}