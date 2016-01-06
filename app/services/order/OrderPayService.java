package services.order;

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
import models.ShoppingOrder;
import models.ShoppingOrderPay;
import models.product.Fromsite;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import play.Logger;
import repositories.OrderProductRepository;
import repositories.ShoppingOrderPayRepository;
import repositories.Order.OrderRepository;
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

/**
 * 
 * <p>Title: OrderPayService.java</p> 
 * <p>Description: 订单支付</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年10月9日  上午10:10:43
 * @version
 */
@Named
@Singleton
public class OrderPayService {

    private static final Logger.ALogger logger = Logger.of(OrderPayService.class);
    public static final Integer DEFAULT_VAL = -1;
    @Inject
    private ShoppingOrderPayRepository shoppingOrderPayRepository;

    public ShoppingOrderPay findByOrderId(long orderId){
    	return shoppingOrderPayRepository.findByOrderId(orderId);
    }
}
