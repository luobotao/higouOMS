package repositories.product;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import models.product.PadChannel;

/**
 * pad频道相关
 * @author luobotao
 * Date: 2015年4月17日 下午2:21:17
 */
@Named
@Singleton
public interface PadChannelRepository extends JpaRepository<PadChannel, Long>,JpaSpecificationExecutor<PadChannel> {

	PadChannel findById(Long channelId);

	void deleteById(Long channelId);

	@Modifying
	@Query(value="UPDATE PadChannel SET sta = ?2 WHERE id =?1")
	void updateChannelSta(Long channelId, String channelsta);

	PadChannel findByCnameAndUserid(String channelName, Long userId);

	List<PadChannel> findPadchannelByUserid(Long uid);

	void deleteByUserid(Long uid);


}