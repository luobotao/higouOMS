package repositories.parcels;


import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.ParcelsWaybillInfo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * 
 * <p>Title: ParcelsWaybillInfoRepository.java</p> 
 * <p>Description: 包裹物流详细信息</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年7月31日  下午6:43:06
 * @version
 */
@Named
@Singleton
public interface ParcelsWaybillInfoRepository extends JpaRepository<ParcelsWaybillInfo, Long>,JpaSpecificationExecutor<ParcelsWaybillInfo> {

	@Query(value="SELECT * FROM pardels_Waybill_info WHERE wayBillId=(?1) order by nsort asc, date_txt asc",nativeQuery=true)
	List<ParcelsWaybillInfo> getAllByWayBillId(long wayBillId);


}