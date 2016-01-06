package repositories;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import models.Bbtaddress;

/**
 * 地址
 * @author luobotao
 * @Date 2015年9月7日
 */
@Named
@Singleton
public interface BbtaddressRepository extends JpaRepository<Bbtaddress, Integer>,JpaSpecificationExecutor<Bbtaddress> {

	List<Bbtaddress> findByParentid(Integer parentid);
	
	
}