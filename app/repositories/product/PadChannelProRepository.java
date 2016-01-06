package repositories.product;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import models.product.PadChannelPro;

/**
 * pad频道pro相关
 * @author luobotao
 * Date: 2015年4月17日 下午2:21:17
 */
@Named
@Singleton
public interface PadChannelProRepository extends JpaRepository<PadChannelPro, Long>,JpaSpecificationExecutor<PadChannelPro> {

	@Modifying
	@Query(value="UPDATE PadChannelPro SET typ = ?2 WHERE id =?1")
	void updateChannelProSta(Long id, String sta);
	@Modifying
	@Query(value="UPDATE PadChannelPro SET nsort = ?2 WHERE id =?1")
	void updateChannelProSta(Long channelProIdForUpdate, int nsortForUpdate);
	
	void deleteByCid(Long cid);

	


}