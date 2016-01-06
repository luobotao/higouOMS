package repositories.Order;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.ShoppingOrder;
import models.product.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * 
 * <p>Title: OrderRepository.java</p> 
 * <p>Description: 订单相关</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年7月28日  下午6:18:24
 * @version
 */
@Named
@Singleton
public interface OrderRepository extends JpaRepository<ShoppingOrder, Long>,JpaSpecificationExecutor<ShoppingOrder> {
	@Query(value="SELECT f.name FROM fromsite f,product p,shopping_Order_Pro sop WHERE f.`id`=p.`fromsite` AND p.`pid`=sop.`pId` AND sop.`orderId`=?1 limit 1",nativeQuery=true)
	String getFromsietWithOrderCode(Long orderId);

	@Query(value="SELECT * FROM shopping_Order WHERE `orderCode`=?1 limit 1",nativeQuery=true)
	ShoppingOrder queryShoppingOrderByOrderCode(String orderCode);


}