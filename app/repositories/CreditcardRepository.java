package repositories;

import javax.inject.Named;
import javax.inject.Singleton;

import models.Creditcard;
import models.product.Currency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Provides CRUD functionality for accessing people. Spring Data auto-magically
 * takes care of many standard operations here.
 */
@Named
@Singleton
public interface CreditcardRepository extends JpaRepository<Creditcard, Integer>,JpaSpecificationExecutor<Creditcard> {
	
	
}