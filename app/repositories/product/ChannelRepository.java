package repositories.product;

import javax.inject.Named;
import javax.inject.Singleton;

import models.product.Channel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * 频道相关
 * @author luobotao
 * Date: 2015年4月17日 下午2:21:17
 */
@Named
@Singleton
public interface ChannelRepository extends JpaRepository<Channel, Long>,JpaSpecificationExecutor<Channel> {

	Channel findById(Long channelId);

	void deleteById(Long channelId);

	@Modifying
	@Query(value="UPDATE Channel SET sta = ?2 WHERE id =?1")
	void updateChannelSta(Long channelId, String channelsta);


}