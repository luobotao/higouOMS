package services.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.OrderProduct;
import models.admin.AdminFreshTask;
import models.admin.AdminFreshTask.TaskStatus;
import models.admin.AdminUser;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import play.Logger;
import repositories.OrderProductRepository;
import repositories.ShoppingOrderRepository;
import repositories.admin.AdminFreshRepository;
import utils.Dates;
import vo.CloseTaskVO;
import forms.admin.FreshTaskQueryForm;

@Named
@Singleton
public class AdminFreshService {

    private static final Logger.ALogger logger = Logger.of(AdminFreshService.class);

    @Inject
    private AdminFreshRepository adminFreshRepository;
    @Inject
    private ShoppingOrderRepository shoppingOrderRepository;


    @Transactional
    public AdminFreshTask saveAdminFreshTask(AdminFreshTask adminFreshTask) {
        return adminFreshRepository.save(adminFreshTask);
    }

    public Iterator<AdminFreshTask> listFreshTask() {
        return adminFreshRepository.findAll().iterator();
    }
    
    @Transactional(readOnly = true)
    public Page<AdminFreshTask> query(FreshTaskQueryForm form) {
        return this.adminFreshRepository.findAll(new FreshTaskQuery(form),
                new PageRequest(form.page, form.size));
    }
    
    @Transactional
    public void delete(Long id) {
    	adminFreshRepository.delete(id);
    }
    

    /**
     * 根据任务IDs结束任务
     * @param currentAdminUser
     * @param ids
     * @return
     */
    @Transactional
	public CloseTaskVO closeFreshTaskByIds(AdminUser currentAdminUser,
			List<Long> ids) {
		List<AdminFreshTask> tasks = adminFreshRepository.queryDoingTasksByIds(ids);
        return closeFreshTasks(currentAdminUser, tasks);
	}

	private CloseTaskVO closeFreshTasks(AdminUser currentAdminUser,
			List<AdminFreshTask> tasks) {
		CloseTaskVO vo = new CloseTaskVO();
		for (AdminFreshTask task : tasks) {
			task.setAdminUser(currentAdminUser);
			task.setStatus(TaskStatus.END);
		}
		adminFreshRepository.save(tasks);
		for (AdminFreshTask task : tasks) {
			vo.addItem(task.getId(), task.getStatus().ordinal());
		}
		return vo;
	}
	
	
	
	
	/**
	 * 任务查询内部类
	 * @author luobotao
	 * Date: 2015年4月17日 上午10:04:07
	 */
	private static class FreshTaskQuery implements Specification<AdminFreshTask> {

        private final FreshTaskQueryForm form;

        public FreshTaskQuery(final FreshTaskQueryForm form) {
            this.form = form;
        }

        @Override
        public Predicate toPredicate(Root<AdminFreshTask> freshTask, CriteriaQuery<?> query,
                                     CriteriaBuilder builder) {
            Path<String> unit = freshTask.get("unit");
            List<Predicate> predicates = new ArrayList<>();
            Predicate[] param = new Predicate[predicates.size()];
            predicates.toArray(param);
            return query.where(param).getRestriction();
        }
    }
    

}
