package repositories.product;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.product.ProductGroup;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

@Named
@Singleton
public interface ProductGroupRepository extends JpaRepository<ProductGroup, Long>,JpaSpecificationExecutor<ProductGroup> {

	@Query(value="SELECT * FROM product_group WHERE pgid = ?1 AND pid =?2",nativeQuery=true)
	ProductGroup queryProductGroup(Long pgid, Long pid);

	@Query(value="SELECT * FROM product_group WHERE pgid = ?1 ",nativeQuery=true)
	List<ProductGroup> findProductGroupListByPgId(Long pgid);
	
	
	void deleteByPgid(Long pgid);

	@Query(value="SELECT * FROM product_group WHERE pid = ?1 ",nativeQuery=true)
	List<ProductGroup> getProductGroupByPid(Long pid);

	
}