package services.user;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;

import forms.UserQueryForm;
import models.product.Fromsite;
import models.user.GroupInfo;
import models.user.User;
import play.Logger;
import repositories.user.GroupInfoRepository;
import repositories.user.UserInfoRepository;
import services.ServiceFactory;
import utils.Constants;
import utils.Htmls;
import utils.Numbers;

/**
 * 用户相关Service
 * @author luobotao
 * @Date 2015年8月29日
 */
@Named
@Singleton
public class UserService {

    private static final Logger.ALogger logger = Logger.of(UserService.class);

    @Inject
    private UserInfoRepository userRepository;
    @Inject
    private GroupInfoRepository groupInfoRepository;

	/**
	 * 根据用户ID获取此用户
	 * @param userId
	 * @return
	 */
	public User findUserById(Long userId) {
		User user = (User) ServiceFactory.getCacheService().getObject(Constants.user_KEY+userId );//从缓存读入
		if(user==null){
			user = userRepository.findOne(userId);
			ServiceFactory.getCacheService().setObject(Constants.user_KEY+userId, user,0 );//写入缓存
		}
		return user;
	}

	public List<User> findUserList(final UserQueryForm userQueryForm) {
		List<User> result = new ArrayList<User>();
		if(userQueryForm.userId!=null && userQueryForm.userId.intValue()!=0){
			User user = (User) ServiceFactory.getCacheService().getObject(Constants.user_KEY+userQueryForm.userId );//从缓存读入
			if(user==null){
				user = userRepository.findOne(userQueryForm.userId);
				ServiceFactory.getCacheService().setObject(Constants.user_KEY+userQueryForm.userId, user,0 );//写入缓存
			}
			if(user!=null){
				result.add(user);
			}
		}else{
			result = userRepository.findAll(new Specification<User>() {

				@Override
				public Predicate toPredicate(Root<User> user,
						CriteriaQuery<?> query, CriteriaBuilder builder) {
					Path<String> nickname = user.get("nickname");
					Path<String> phone = user.get("phone");
					Path<Integer> isEndorsement = user.get("isEndorsement");//代言状态
					Path<Long> gid = user.get("gid");//组ID
					List<Predicate> predicates = new ArrayList<>();
					if (!StringUtils.isBlank(userQueryForm.userName)) {
						predicates.add(builder.like(nickname, "%" + userQueryForm.userName + "%"));
					}
					if (!StringUtils.isBlank(userQueryForm.phone)) {
						predicates.add(builder.like(phone, "%" + userQueryForm.phone + "%"));
					}
					if (!StringUtils.isBlank(userQueryForm.danyanFlag) && Numbers.parseInt(userQueryForm.danyanFlag, -1).intValue()!=-1) {
						predicates.add(builder.equal(isEndorsement, Numbers.parseInt(userQueryForm.danyanFlag, -1)));
					}
					if (userQueryForm.gid!=null ) {
						predicates.add(builder.equal(gid, userQueryForm.gid));
					}
					Predicate[] param = new Predicate[predicates.size()];
					predicates.toArray(param);
					return query.where(param).getRestriction();
				}
			});
		}
		return result;
	}

	/** 
	 * 分页获取用户列表
	 * @param userForm
	 * @return
	 */
	public Page<User> findUserPage(final UserQueryForm userQueryForm) {
		return this.userRepository.findAll(new Specification<User>() {

			@Override
			public Predicate toPredicate(Root<User> user,
					CriteriaQuery<?> query, CriteriaBuilder builder) {
				Path<Long> uid = user.get("uid");
				Path<Long> fromUid = user.get("fromUid");//用户来源于哪个admin
				Path<String> nickname = user.get("nickname");
				Path<String> phone = user.get("phone");
				Path<Integer> isEndorsement = user.get("isEndorsement");//代言状态
				Path<Long> gid = user.get("gid");//组ID
				Path<Integer> province = user.get("province");//省份
				Path<Integer> city = user.get("city");//城市
				Path<String> address = user.get("address");//地址
				Path<String> contactPerson = user.get("contactPerson");//联系人
				Path<String> contactPhone = user.get("contactPhone");//联系方式
				Path<String> postmanid = user.get("postmanid");//快递员ID
				List<Predicate> predicates = new ArrayList<>();
				if (userQueryForm.userId!=null && userQueryForm.userId!=-1) {
					predicates.add(builder.equal(uid, userQueryForm.userId));
				}
				if (userQueryForm.province!=null && userQueryForm.province!=-1) {
					predicates.add(builder.equal(province, userQueryForm.province));
				}
				if (userQueryForm.city!=null && userQueryForm.city!=-1) {
					predicates.add(builder.equal(city, userQueryForm.city));
				}
				if (!StringUtils.isBlank(userQueryForm.userName)) {
					predicates.add(builder.like(nickname, "%" + userQueryForm.userName + "%"));
				}
				if (!StringUtils.isBlank(userQueryForm.keywords)) {
					Predicate p1 = builder.like(contactPhone, "%" + userQueryForm.keywords + "%");
					Predicate p2 = builder.like(contactPerson, "%" + userQueryForm.keywords + "%");
					Predicate p3 = builder.like(address, "%" + userQueryForm.keywords + "%");
	            	predicates.add(builder.or(p1,p2,p3));
				}
				if (!StringUtils.isBlank(userQueryForm.phone)) {
					predicates.add(builder.like(phone, "%" + userQueryForm.phone + "%"));
				}
				if (!StringUtils.isBlank(userQueryForm.danyanFlag) && Numbers.parseInt(userQueryForm.danyanFlag, -1).intValue()!=-1) {
					predicates.add(builder.equal(isEndorsement, Numbers.parseInt(userQueryForm.danyanFlag, -1)));
				}
				if (userQueryForm.gid!=null && userQueryForm.gid.longValue()!=-1) {
					predicates.add(builder.equal(gid, userQueryForm.gid));
				}
				if (userQueryForm.fromUid!=null && userQueryForm.fromUid.longValue()!=-1) {
					predicates.add(builder.equal(fromUid, userQueryForm.fromUid));
				}
				/*if ("1".equals(userQueryForm.postman)) {//快递商户
					predicates.add(builder.isNotNull(postmanid));
					predicates.add(builder.notEqual(postmanid, ""));
				}else{
					Predicate p1 = builder.isNull(postmanid);
					Predicate p2 = builder.equal(postmanid, "");
	            	predicates.add(builder.or(p1,p2));
				}*/
				Predicate[] param = new Predicate[predicates.size()];
				predicates.toArray(param);
				return query.where(param).getRestriction();
			}
		},
                new PageRequest(userQueryForm.page, userQueryForm.size,new Sort(Direction.DESC, "uid")));
	}

	/**
	 * 获取所有用户组
	 * @return
	 */
	public List<GroupInfo> findAllGroup() {
		return groupInfoRepository.findAll();
	}

	
	/**
     * 生成html中需要的来源select
     * @param groupInfoList
     * @param id
     * @return
     */
    public static String groupInfoList2Html(List<GroupInfo> groupInfoList,Integer id){
		StringBuilder sb = new StringBuilder();
		if(id.intValue()==-1){
			sb.append(Htmls.generateOption(-1, "全部"));
		}
		for (GroupInfo c : groupInfoList) {
			if (id != null && id.equals(c.getId())) {
				sb.append(Htmls.generateSelectedOption(c.getId(), c.getGname()));
			} else {
				sb.append(Htmls.generateOption(c.getId(), c.getGname()));
			}
		}
		return sb.toString();
    }

	/**
	 * 保存用户
	 * @param user
	 * @return
	 */
	public User saveUser(User user) {
		user = userRepository.save(user);
		ServiceFactory.getCacheService().setObject(Constants.user_KEY+user.getUid(), user,0 );//写入缓存
		return user;
	}

	/**
	 * 根据手机查询用户列表
	 * @param phone
	 * @return
	 */
	public List<User> findByPhone(String phone) {
		return userRepository.findByPhone(phone);
	}

	/**
	 * 根据棒棒糖传来的快递员ID查询用户列表
	 * @param phone
	 * @return
	 */
	public List<User> findByPostmanid(String postmanid) {
		return userRepository.findByPostmanid(postmanid);
	}
	
	/**
	 * 
	 * <p>Title: findByGid</p> 
	 * <p>Description: 根据gid查询用户列表</p> 
	 * @param gid
	 * @return
	 */
	public List<User> findByGid(int gid) {
		return userRepository.findByGid(gid);
	}

	/**
	 * 
	 * <p>Title: userList2Html</p> 
	 * <p>Description: 生成html需要的用户select</p> 
	 * @param userList
	 * @param uid
	 * @return
	 */
	public static String userList2Html(List<User> userList, long uid) {
		StringBuilder sb = new StringBuilder();
		sb.append(Htmls.generateOption(0, "请选择"));
		for (User u : userList) {
			if (uid==u.getUid()) {
				sb.append(Htmls.generateSelectedOption(u.getUid(), u.getNickname()));
			} else {
				sb.append(Htmls.generateOption(u.getUid(), u.getNickname()));
			}
		}
		return sb.toString();
	}

}
