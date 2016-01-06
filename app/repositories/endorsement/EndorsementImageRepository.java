package repositories.endorsement;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.endorsement.EndorsementImg;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 代言商品图片
 * @author luobotao
 * @Date 2015年8月31日
 */
@Named
@Singleton
public interface EndorsementImageRepository extends JpaRepository<EndorsementImg, Long>,JpaSpecificationExecutor<EndorsementImg> {

	List<EndorsementImg> findByEid(Long eid);
	
	
}