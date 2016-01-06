package repositories.subject;

import javax.inject.Named;
import javax.inject.Singleton;

import models.subject.Subject;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 
 * <p>Title: SubjectRepository.java</p> 
 * <p>Description: 专题相关</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年8月9日  下午4:59:36
 * @version
 */
@Named
@Singleton
public interface SubjectRepository extends JpaRepository<Subject, Long>,JpaSpecificationExecutor<Subject> {

	Subject findById(Long subjectId);
	
	Subject getSubjectBySname(String sname);

}