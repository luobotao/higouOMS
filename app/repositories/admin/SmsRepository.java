package repositories.admin;

import javax.inject.Named;
import javax.inject.Singleton;

import models.SmsInfo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Named
@Singleton
public interface SmsRepository extends JpaRepository<SmsInfo, Long>,
		JpaSpecificationExecutor<SmsInfo> {

	
}