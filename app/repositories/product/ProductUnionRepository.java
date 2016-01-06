package repositories.product;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import models.product.ProductUnion;

@Named
@Singleton
public interface ProductUnionRepository extends JpaRepository<ProductUnion, Long>,JpaSpecificationExecutor<ProductUnion> {

	ProductUnion findByPid(Long pid);


	
}