package repositories.parcels;


import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.ParcelsWaybill;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * 
 * <p>Title: ParcelsWaybillRepository.java</p> 
 * <p>Description: 包裹物流信息</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年7月31日  下午6:43:06
 * @version
 */
@Named
@Singleton
public interface ParcelsWaybillRepository extends JpaRepository<ParcelsWaybill, Long>,JpaSpecificationExecutor<ParcelsWaybill> {

	@Query(value="SELECT * FROM pardels_Waybill WHERE pardelsId=(?1) order by nsort desc, date_txt desc",nativeQuery=true)
	List<ParcelsWaybill> getAllWaybillInfoWithParcelsId(Long id);

	@Query(value="SELECT * FROM pardels_Waybill WHERE pardelsId=(?1) and remark =?2 order by nsort desc, date_txt desc",nativeQuery=true)
	List<ParcelsWaybill> getWaybillInfoWithParcelsIdAndRemark(Long parcelsId,
			String remark);


}