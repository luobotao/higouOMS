package repositories.product;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.product.ProductDetailPram;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Named
@Singleton
public interface ProductDetailParamRepository extends JpaRepository<ProductDetailPram, Long>,JpaSpecificationExecutor<ProductDetailPram> {

	List<ProductDetailPram> findByPdid(Long pdid, Sort sort);

	void deleteByPdid(Long detailId);
	
	
}