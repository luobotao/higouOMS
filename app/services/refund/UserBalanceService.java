package services.refund;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
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
import models.Parcels;
import models.RefundInfo;
import models.ShoppingOrder;
import models.admin.AdminUser;
import models.product.Fromsite;
import models.user.UserBalance;
import models.user.UserBalanceLog;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import play.Logger;
import repositories.OrderProductRepository;
import repositories.Order.OrderRepository;
import repositories.Order.RefundInfoRepository;
import repositories.Order.UserBalanceLogRepository;
import repositories.Order.UserBalanceRepository;
import repositories.parcels.ParcelsRepository;
import services.ServiceFactory;
import services.parcels.ParcelsService;
import services.product.ProductService;
import utils.Constants;
import utils.Dates;
import utils.JdbcOper;
import utils.StringUtil;
import vo.OrderVO;

import com.google.common.base.Strings;

import forms.order.OrderForm;
import forms.order.RefundForm;

/**
 * 
 * <p>Title: UserBalanceService.java</p> 
 * <p>Description: 用户钱包</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年11月9日  下午12:20:02
 * @version
 */
@Named
@Singleton
public class UserBalanceService {

    private static final Logger.ALogger logger = Logger.of(UserBalanceService.class);
    public static final Integer DEFAULT_VAL = -1;
    @Inject
    private UserBalanceRepository userBalanceRepository;
    @Inject
    private UserBalanceLogRepository userBalanceLogRepository;
    
	public UserBalance getUserBalance(Long uid) {
		return userBalanceRepository.getbalance(uid);
	}

	public UserBalance saveUserBalance(UserBalance userb) {
		return userBalanceRepository.save(userb);
	}

	/**
	 * 
	 * <p>Title: addbalanceLog</p> 
	 * <p>Description: 保存用户钱包日志</p> 
	 * @param ubg
	 * @return
	 */
	public UserBalanceLog addbalanceLog(UserBalanceLog ubg) {
		return userBalanceLogRepository.save(ubg);
	}
    


	
}
