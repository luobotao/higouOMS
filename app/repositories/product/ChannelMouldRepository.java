package repositories.product;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.product.ChannelMould;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * 频道内容相关
 * @author luobotao
 * Date: 2015年4月17日 下午2:21:17
 */
@Named
@Singleton
public interface ChannelMouldRepository extends JpaRepository<ChannelMould, Long>,JpaSpecificationExecutor<ChannelMould> {

	List<ChannelMould> findByCid(Long channelId, Sort sort);
	
	@Query(value="SELECT id FROM ChannelMould WHERE cid = ?1 ORDER BY nsort DESC ,id DESC")
	List<Long> findChannelMouldIdsByCid(Long channelId);



}