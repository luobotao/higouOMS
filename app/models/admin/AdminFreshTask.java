package models.admin;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import utils.Dates;
import utils.Numbers;

import com.fasterxml.jackson.annotation.JsonIgnore;

import forms.admin.FreshTaskForm;

/**
 * 换量管理中的任务实体
 * @author luobotao
 * Date: 2015年4月15日 上午11:28:55
 */
@Entity(name="freshTask")
public class AdminFreshTask implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -1251921527010226083L;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;

	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_start;
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_end;
	
    private int orderAmount;//此任务总单量
    
    private int orderEnd;//已完成单量
    
    private int timeBetween;//时间间隔
    private int unit;//0分 1时 2天
    
    private TaskStatus status;//0未开始1进行中2已完成
    
    private AdminUser adminUser;
    
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_next;//下次启动时间
	
    
    public static enum TaskStatus {
        // 注意添加新的类型时候一定不能改变现有的顺序
        // 只能添加在最后面！！！！！！
        WAITING("未开始"), DOING("进行中"), END("已完成");
        private String message;

        private TaskStatus(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public static TaskStatus getTaskStatus(String str) {
            TaskStatus[] TaskStatuss = TaskStatus.values();
            for (TaskStatus o : TaskStatuss) {
                if (o.message.equals(str)
                        || o.ordinal() == Numbers.parseInt(str, -1)) {
                    return o;
                }
            }
            return null;
        }

        public static TaskStatus getTaskStatus(int ordinal) {
            TaskStatus[] TaskStatuss = TaskStatus.values();
            for (TaskStatus o : TaskStatuss) {
                if (o.ordinal() == ordinal) {
                    return o;
                }
            }
            return null;
        }
    }


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public Date getDate_add() {
		return date_add;
	}


	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}


	public Date getDate_start() {
		return date_start;
	}


	public void setDate_start(Date date_start) {
		this.date_start = date_start;
	}


	public Date getDate_end() {
		return date_end;
	}


	public void setDate_end(Date date_end) {
		this.date_end = date_end;
	}


	public int getOrderAmount() {
		return orderAmount;
	}


	public void setOrderAmount(int orderAmount) {
		this.orderAmount = orderAmount;
	}


	public int getOrderEnd() {
		return orderEnd;
	}


	public void setOrderEnd(int orderEnd) {
		this.orderEnd = orderEnd;
	}


	public TaskStatus getStatus() {
		return status;
	}


	public void setStatus(TaskStatus status) {
		this.status = status;
	}


	public Date getDate_next() {
		return date_next;
	}


	public void setDate_next(Date date_next) {
		this.date_next = date_next;
	}



	public int getTimeBetween() {
		return timeBetween;
	}


	public void setTimeBetween(int timeBetween) {
		this.timeBetween = timeBetween;
	}


	public int getUnit() {
		return unit;
	}


	public void setUnit(int unit) {
		this.unit = unit;
	}

	@ManyToOne
    @JoinColumn(name = "adminUser_id")
    @NotFound(action = NotFoundAction.IGNORE)
	@JsonIgnore
    public AdminUser getAdminUser() {
		return adminUser;
	}


	public void setAdminUser(AdminUser adminUser) {
		this.adminUser = adminUser;
	}


	public static AdminFreshTask convertFromForm(FreshTaskForm freshTaskForm,AdminUser adminUser){
    	AdminFreshTask adminFreshTask = new AdminFreshTask();
    	adminFreshTask.setAdminUser(adminUser);
    	adminFreshTask.setDate_add(new Date());
    	adminFreshTask.setStatus(TaskStatus.WAITING);
    	try {
    		adminFreshTask.setOrderAmount(Integer.valueOf(freshTaskForm.orderAmount));
    		adminFreshTask.setOrderEnd(0);
    		adminFreshTask.setTimeBetween(Integer.valueOf(freshTaskForm.timeBetween));
			adminFreshTask.setDate_start(Dates.parseDate(freshTaskForm.date_start,"yyyy-MM-dd HH:mm"));
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return adminFreshTask;
    }
}
