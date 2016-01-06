package services.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import forms.admin.AdminLoginForm;
import forms.admin.AdminUserQueryForm;
import forms.admin.CreateOrUpdateAdminUserForm;
import models.admin.AdminUser;
import models.admin.OperateLog;
import models.admin.SysUserRole;
import models.product.Fromsite;
import play.Logger;
import play.cache.Cache;
import repositories.OperateLogRepository;
import repositories.admin.AdminUserRepository;
import repositories.product.FromsiteRepository;
import utils.BeanUtils;
import utils.Constants;
import utils.Htmls;

/**
 * @author luobotao
 * @Date 2015年10月19日
 */
@Named
@Singleton
public class AdminUserService {

    private static final Logger.ALogger LOGGER = Logger.of(AdminUserService.class);

    @Inject
    private AdminUserRepository adminUserRepository;
    
	@Inject
	private OperateLogRepository operateLogRepository;

    @Inject
    private FromsiteRepository fromsiteRepository;

    public AdminUser getAdminUser(Long id) {
        return adminUserRepository.findOne(id);
    }

    public AdminUser login(AdminLoginForm form) {
    	String username = form.username;
    	String pass = DigestUtils.md5Hex(form.password);
    	Logger.info(username);
    	Logger.info(pass);
        List<AdminUser> adminUsers = adminUserRepository
                .findByUsernameAndPasswd(form.username,  DigestUtils.md5Hex(form.password));
        if(adminUsers.size()<1){
        	return null;
        }
        return adminUsers.get(0);
    }
    
    @Transactional
    public AdminUser updateAdminUser(AdminUser adminUser) {
        return adminUserRepository.save(adminUser);
    }

    @Transactional
    public AdminUser createAdminUser(OperateLog operateLog,CreateOrUpdateAdminUserForm form) {
        AdminUser adminUser = new AdminUser();
        Fromsite fromsite = null;
        if(form.id!=null && form.id.longValue()==-1){
        	form.id = null;
        }
        BeanUtils.copyProperties(form, adminUser);
        if(adminUser.getId()==null || (adminUser.getId()!=null && adminUser.getId().longValue()==-1)){
        	adminUser.setDate_add(new Date());
        	if(Constants.AdminType.UNION.getType().equals(adminUser.getAdminType())){//联营，需要初始化fromsite表里的数据
        		fromsite = new Fromsite();
        		fromsite.setName(adminUser.getRealname());
        		fromsite.setFee(10);
        		fromsite.setAddfee(5);
        	}
        }
        adminUser.setDate_login(new Date());
        adminUser = adminUserRepository.save(adminUser);
        if(fromsite!=null){
        	fromsite.setId(adminUser.getId().intValue());
        	fromsiteRepository.save(fromsite);
        	operateLog.setRemark("创建管理员，创建后的管理员用户名为:"+adminUser.getUsername()+"，ID为:"+adminUser.getId()); 
        }else{
        	operateLog.setRemark("编辑管理员，编辑管理员的用户名为:"+adminUser.getUsername()+"，ID为:"+adminUser.getId()); 
        }
        operateLogRepository.save(operateLog);
        return adminUser;
    }

    public List<AdminUser> listAdminUsers(final AdminUserQueryForm form) {
        return adminUserRepository.findAll(
        		new Specification<AdminUser>() {

					@Override
					public Predicate toPredicate(Root<AdminUser> adminUser,
							CriteriaQuery<?> query, CriteriaBuilder builder) {
						Path<Long> id = adminUser.get("id");
		
						List<Predicate> predicates = new ArrayList<>();
						if(form.roleid!=null && form.roleid.intValue()!=0){
							Subquery<Long> sq = query.subquery(Long.class);
							Root<SysUserRole>  subroot=sq.from(SysUserRole.class);
							Predicate pidPredicate = builder.equal(subroot.get("roleid").as(Integer.class), form.roleid);
							sq.where(pidPredicate);
							sq.select(subroot.get("userid").as(Long.class));
							Predicate in= builder.in(id).value(sq).not();
							predicates.add(in);
						}
						
						Predicate[] param = new Predicate[predicates.size()];

						predicates.toArray(param);
						return query.where(param).getRestriction();
					}
				}
        		);
    }
    
    public void delete(Long id) {
        adminUserRepository.delete(id);
    }

    public String allAdminUser2HTML(Long id) {
        StringBuilder sb = new StringBuilder();
        List<AdminUser> adminUsers = null;
        try {
            adminUsers = Cache.getOrElse(Constants.USER, new Callable<List<AdminUser>>() {

                @Override
                public List<AdminUser> call() throws Exception {
                    List<AdminUser> adminUsers = new ArrayList<AdminUser>();
                    Iterator<AdminUser> adminUserIt = adminUserRepository.findAll().iterator();
                    while (adminUserIt.hasNext()) {
                        adminUsers.add(adminUserIt.next());
                    }
                    Cache.set(Constants.USER, adminUsers);
                    return adminUsers;
                }
            }, 1);
        } catch (Exception e) {
            LOGGER.error("allAdminUser2HTML error.", e);
        }
        sb.append(Htmls.generateOption(-1, "默认全部"));

        if (CollectionUtils.isNotEmpty(adminUsers)) {
            for (AdminUser adminUser : adminUsers) {
            }
        }

        return sb.toString();
    }

	/**
	 * 根据用户名查找此管理员
	 * @param phone
	 * @return
	 */
	public AdminUser findByUsername(String phone) {
		List<AdminUser> adminUsers = adminUserRepository.findByUsername(phone);
		if(adminUsers.isEmpty()){
			return new AdminUser();
		}else{
			return adminUsers.get(0);
		}
	}

	/**
	 * 
	 * <p>Title: queryAllLyAdminUser</p> 
	 * <p>Description: 查找出所有的联营商户</p> 
	 * @return
	 */
	public List<AdminUser> queryAllLyAdminUser() {
		return adminUserRepository.queryAllLyAdminUser();
	}

	/**
	 * 
	 * <p>Title: adminUserList2Html</p> 
	 * <p>Description: 生成联营商户的select</p> 
	 * @param adminUserList
	 * @param adminid
	 * @return
	 */
	public static String adminUserList2Html(List<AdminUser> adminUserList,
			long adminid) {
		StringBuilder sb = new StringBuilder();
		sb.append(Htmls.generateOption(-1, "全部"));
		for (AdminUser au : adminUserList) {
			if (adminid==au.getId()) {
				sb.append(Htmls.generateSelectedOptionName(au.getId(), au.getId(), au.getRealname()));
			} else {
				sb.append(Htmls.generateOptionName(au.getId(), au.getId(), au.getRealname()));
			}
		}
		return sb.toString();
	}
	/**
     * 生成html中需要的来源select
     * @param fromsiteList
     * @param id
     * @return
     */
    public static String fromsiteList2Html(List<Fromsite> fromsiteList,Integer id){
		StringBuilder sb = new StringBuilder();
		sb.append(Htmls.generateOption(-1, "请选择"));
		for (Fromsite c : fromsiteList) {
			if (id != null && id.equals(c.getId())) {
				sb.append(Htmls.generateSelectedOption(c.getId(), c.getName()));
			} else {
				sb.append(Htmls.generateOption(c.getId(), c.getName()));
			}
		}
		return sb.toString();
    }

    /**
     * 
     * <p>Title: queryAdminUserByPid</p> 
     * <p>Description: 查询商品所在的联营商户</p> 
     * @return
     */
	public List<AdminUser> queryAdminUserByPid(Long pid, int adminid) {
		if(adminid==-1){
			return adminUserRepository.queryAdminUserByPid(pid);
		}else{
			return adminUserRepository.queryAdminUserByPidAndAdminId(pid,adminid);
		}
	}


	/**
	 * 根据管理员id修改管理员状态
	 * @param adminid
	 * @param activate
	 * @return
	 */
	@Transactional
	public int updateAdminUserStatus(Long adminid, Integer activate) {
		return adminUserRepository.updateAdminUserStatus(adminid,activate);
	}
	
	/**
	 * 修改管理员角色
	 * @param adminid
	 * @param role
	 * @return
	 */
	@Transactional
	public int updateAdminRole(Long adminid, Integer role) {
		return adminUserRepository.updateAdminRole(adminid,role);
	}

	
    

}
