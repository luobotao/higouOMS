package repositories.product;

import javax.inject.Named;
import javax.inject.Singleton;

import models.product.Fromsite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Named
@Singleton
public interface FromsiteRepository extends JpaRepository<Fromsite, Integer>,JpaSpecificationExecutor<Fromsite> {
	
	
}