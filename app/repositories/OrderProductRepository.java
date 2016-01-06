package repositories;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.OrderProduct;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

@Named
@Singleton
public interface OrderProductRepository extends JpaRepository<OrderProduct, Long>,JpaSpecificationExecutor<OrderProduct> {


	@Query(value="SELECT * FROM shopping_Order_Pro WHERE orderId=(?1)",nativeQuery=true)
	List<OrderProduct> getAllByShoppingOrderId(Long orderId);
	
}