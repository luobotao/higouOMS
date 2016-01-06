package repositories.product;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.product.ProductImages;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Named
@Singleton
public interface ProductImagesRepository extends JpaRepository<ProductImages, Long>,JpaSpecificationExecutor<ProductImages> {

	List<ProductImages> findByPid(Long pid);

	
}