package repositories.product;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.product.ProductDetail;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Named
@Singleton
public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long>,JpaSpecificationExecutor<ProductDetail> {

	List<ProductDetail> findByPid(Long pid, Sort sort);
	
	
}