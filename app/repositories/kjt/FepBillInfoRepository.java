package repositories.kjt;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import models.kjt.FepBillInfo;

/**
 * 
 * <p>Title: FepBillInfoRepository.java</p> 
 * <p>Description: 待购汇账单</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年10月13日  下午3:10:55
 * @version
 */
@Named
@Singleton
public interface FepBillInfoRepository extends JpaRepository<FepBillInfo, Long>,
		JpaSpecificationExecutor<FepBillInfo> {
	

	
	
}