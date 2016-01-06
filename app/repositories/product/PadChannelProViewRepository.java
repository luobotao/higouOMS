package repositories.product;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import models.product.PadChannelPro;
import models.product.PadChannelProView;

/**
 * 
 * <p>Title: PadChannelProViewRepository.java</p> 
 * <p>Description: </p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年12月3日  下午8:44:44
 * @version
 */
@Named
@Singleton
public interface PadChannelProViewRepository extends JpaRepository<PadChannelProView, Long>,JpaSpecificationExecutor<PadChannelProView> {

	@Query(value="SELECT * FROM padchannelproview WHERE cpid =?1 order by nsort desc",nativeQuery=true)
	List<PadChannelProView> findPadChannelProViewListByCpId(Long cpid);

	@Query(value="SELECT * FROM padchannelproview WHERE id =?1",nativeQuery=true)
	PadChannelProView findPadChannelProViewById(Long id);
	@Modifying
	@Query(value="UPDATE PadChannelProView SET typ = ?2 WHERE id =?1")
	void updateChannelProViewSta(Long id, String sta);

	@Query(value="SELECT count(id) FROM padchannelproview WHERE cpid =?1 and typ=1",nativeQuery=true)
	int findPadChannelProViewByCpid(Long cpid);


}