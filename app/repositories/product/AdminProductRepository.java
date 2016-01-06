package repositories.product;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import models.product.AdminProduct;

/**
 * 商品与外部管理员关联
 * @author luobotao
 * Date: 2015年4月17日 下午2:21:17
 */
@Named
@Singleton
public interface AdminProductRepository extends JpaRepository<AdminProduct, Long>,JpaSpecificationExecutor<AdminProduct> {

	List<AdminProduct> findByPidAndAdminid(Long pid, Long adminid);



}