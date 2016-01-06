package repositories.product;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.product.Mould;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * 卡片相关
 * @author luobotao
 * Date: 2015年4月17日 下午2:21:17
 */
@Named
@Singleton
public interface MouldRepository extends JpaRepository<Mould, Long>,JpaSpecificationExecutor<Mould> {
	
	@Query(value="SELECT mname FROM Mould WHERE id = ?1 ")
	String findmnameById(Long mouldId);
	
	@Query(value="SELECT id FROM Mould ")
	List<Long> findIds();




}