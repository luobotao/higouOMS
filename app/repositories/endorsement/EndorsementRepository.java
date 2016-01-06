package repositories.endorsement;

import javax.inject.Named;
import javax.inject.Singleton;

import models.endorsement.Endorsement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Named
@Singleton
public interface EndorsementRepository extends JpaRepository<Endorsement, Long>,JpaSpecificationExecutor<Endorsement> {

	
	
}