package repositories.subject;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.subject.SubjectMouldPro;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * 
 * <p>Title: SubjectMouldProRepository.java</p> 
 * <p>Description: 专题内容管理</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年8月9日  下午4:58:55
 * @version
 */
@Named
@Singleton
public interface SubjectMouldProRepository extends JpaRepository<SubjectMouldPro, Long>,JpaSpecificationExecutor<SubjectMouldPro> {

	@Query(value="SELECT id FROM subject_mould_pro WHERE smid=?1 ORDER BY nsort DESC",nativeQuery=true)
	List<Integer> findIdsBySmid(Long smid);
	/**
	 * 根据商品ID获取所有的pro列表
	 * @param pid
	 * @return
	 */
	@Query(value="SELECT * FROM subject_mould_pro where pid=?1",nativeQuery=true)
	List<SubjectMouldPro> findByPid(Long pid);



}