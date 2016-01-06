package repositories.parcels;

import java.util.Date;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import models.Parcels;

/**
 * 包裹相关
 * @author luobotao
 * Date: 2015年4月20日 下午2:40:53
 */
@Named
@Singleton
public interface ParcelsRepository extends JpaRepository<Parcels, Long>,JpaSpecificationExecutor<Parcels> {

	List<Parcels> findByOrderId(Long orderId);

	Parcels findByParcelCode(String pardelCode);

	@Query(value="select p.* from pardels p, pardels_Pro pp,`adminproduct` ap,`admin` a where p.id=pp.pardelsId and ap.pid=pp.pId and ap.adminid = a.id and p.date_add >= ?1 and a.adminType=2",nativeQuery=true)
	List<Parcels> findParcelsWithDateAdd(Date lasttime);

	@Query(value="select * from pardels where orderId = ?1 and src= ?2",nativeQuery=true)
	List<Parcels> queryParcelsByOrderId(long soId, String typ);

	@Query(value="select * from pardels where orderId = ?1",nativeQuery=true)
	List<Parcels> queryParcelsByOrderId(long soId);

	@Query(value="select * from pardels where bbtId = ?1",nativeQuery=true)
	Parcels queryParcelsByBbtId(long bbtId);

	@Query(value="select * from pardels where pardelCode = ?1",nativeQuery=true)
	Parcels queryParcelsByParcelCode(String parcelCode);

	@Query(value="SELECT * FROM pardels WHERE adminId=?1 and status=0 AND bbtid!=''",nativeQuery=true)
	List<Parcels> findOrderIdsWithDfh(Long adminId);

	@Query(value="SELECT * FROM pardels WHERE adminId=?1 and checkStatus=0 AND bbtid='' AND `date_add` >= ?2 ",nativeQuery=true)
	List<Parcels> findParcelsWithAdminAndDateAdd(Long adminId, Date lasttime);

	@Query(value="SELECT * FROM pardels WHERE adminId=?1 and checkStatus=0 AND bbtid!='' AND `date_add` >= ?2 ",nativeQuery=true)
	List<Parcels> findOrderIdsWithChangedDate(Date startDate);

}