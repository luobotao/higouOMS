package repositories.subject;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.subject.SubjectMould;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * 
 * <p>Title: SubjectMouldRepository.java</p> 
 * <p>Description: 专题内容相关</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年8月9日  下午4:59:14
 * @version
 */
@Named
@Singleton
public interface SubjectMouldRepository extends JpaRepository<SubjectMould, Long>,JpaSpecificationExecutor<SubjectMould> {

	@Query(value="SELECT id FROM SubjectMould WHERE sid = ?1 ORDER BY nsort DESC ,id DESC")
	List<Long> findSubjectMouldIdsBySid(Long subjectId);




}