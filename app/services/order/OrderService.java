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
import models.product.Fromsite;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import play.Logger;
import repositories.OrderProductRepository;
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
 * <p>Title: OrderService.java</p> 
 * <p>Description: 订单处理</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年7月28日  下午6:33:44
 * @version
 */
@Named
@Singleton
public class OrderService {

    private static final Logger.ALogger logger = Logger.of(OrderService.class);
    public static final Integer DEFAULT_VAL = -1;
    @Inject
    private OrderRepository shoppingOrderRepository;
    @Inject
    private OrderProductRepository orderProductRepository;
    @Inject
    private ParcelsRepository parcelsRepository;
    @Inject
    private ProductService productService;
    @Inject
    private ParcelsService parcelsService;

    /**
     * 
     * <p>Title: queryOrderList</p> 
     * <p>Description: 查询商品订单</p> 
     * @param formPage
     * @return
     */
	public List<ShoppingOrder> queryOrderList(OrderForm formPage) {
		return this.shoppingOrderRepository.findAll(new ShoppingOrderQuery(formPage));
	}

	/**
     * 订单查询内部类
     * @author luobotao
     * Date: 2015年4月17日 上午10:04:26
     */
    private static class ShoppingOrderQuery implements Specification<ShoppingOrder> {

        private final OrderForm form;

        public ShoppingOrderQuery(final OrderForm form) {
            this.form = form;
        }

        @Override
        public Predicate toPredicate(Root<ShoppingOrder> shoppingOrder, CriteriaQuery<?> query,
                                     CriteriaBuilder builder) {
            Path<String> ordertype = shoppingOrder.get("ordertype");	//订单类型，是否撒娇支付
            Path<String> payMethod = shoppingOrder.get("paymethod");	//支付方式
            Path<Date> date_add = shoppingOrder.get("date_add");		//下单时间
            Path<String> orderCode = shoppingOrder.get("orderCode");	//订单号
            Path<String> name = shoppingOrder.get("name");			//订单收货人
            Path<String> phone = shoppingOrder.get("phone");	//收货人电话
            
            List<Predicate> predicates = new ArrayList<>();
            if (!Strings.isNullOrEmpty(form.payMethod) && !DEFAULT_VAL.equals(form.payMethod)) {
            	predicates.add(builder.equal(payMethod, form.payMethod));
            }
            if (!Strings.isNullOrEmpty(form.ordertype) && !DEFAULT_VAL.equals(form.ordertype) ) {
            	predicates.add(builder.equal(ordertype, form.ordertype));
            }
            if (!Strings.isNullOrEmpty(form.orderCode)) {
            	predicates.add(builder.equal(orderCode, form.orderCode));
            }
            if (!Strings.isNullOrEmpty(form.name)) {
            	predicates.add(builder.like(name, form.name));
            }
            if (!Strings.isNullOrEmpty(form.phone)) {
            	predicates.add(builder.like(phone, form.phone));
            }
            if (form.between != null) {
                if (form.between.start.equals(form.between.end)) {
                    predicates.add(builder.greaterThanOrEqualTo(date_add,
                            form.between.start));
                    predicates.add(builder.lessThanOrEqualTo(date_add,
                            Dates.getEndOfDay(form.between.start)));
                } else {
                    predicates.add(builder.greaterThanOrEqualTo(date_add,
                            form.between.start));
                    predicates.add(builder.lessThanOrEqualTo(date_add,
                            form.between.end));
                }
            }
            
            Predicate[] param = new Predicate[predicates.size()];
            predicates.toArray(param);
            
            return query.where(param).getRestriction();
        }
    }

	/**
	 * 
	 * <p>Title: queryShoppingOrderById</p> 
	 * <p>Description: 查询订单信息</p> 
	 * @param id
	 * @return
	 */
	public ShoppingOrder queryShoppingOrderById(Long id) {
		ShoppingOrder shoppingOrder = (ShoppingOrder) ServiceFactory.getCacheService().getObject(Constants.shoppingOrder_KEY+id);
		if(shoppingOrder==null){
			shoppingOrder = shoppingOrderRepository.findOne(id);
			if(shoppingOrder==null){
				return null;
			}
			ServiceFactory.getCacheService().setObject(Constants.shoppingOrder_KEY+id, shoppingOrder, 0);
		}
		return shoppingOrder;
	}

	/**
	 * 
	 * <p>Title: updateOrderWithForm</p> 
	 * <p>Description: 更新订单的收货人信息</p> 
	 * @param shoppingOrder
	 * @param formPage
	 */
	@Transactional
	public ShoppingOrder updateOrderWithForm(ShoppingOrder shoppingOrder,
			OrderForm formPage) {
		shoppingOrder.setName(formPage.name);
		shoppingOrder.setPhone(formPage.phone);
		shoppingOrder.setProvince(formPage.province);
		shoppingOrder.setAddress(formPage.address);
		shoppingOrder = shoppingOrderRepository.save(shoppingOrder);
		ServiceFactory.getCacheService().setObject(Constants.shoppingOrder_KEY+shoppingOrder.getId(), shoppingOrder, 0);
		return shoppingOrder;
	}

	/**
	 * 
	 * <p>Title: dealOrderWithStatus</p> 
	 * <p>Description:根据包裹送达情况反查订单，看所有商品是否均已送达且不包含未处理，满足条件更改订单的状态为3，已完成 </p>
	 * @param orderId 
	 */
	public void dealOrderWithStatus(Parcels parcels) {
		Long orderId = parcels.getOrderId();
		List<OrderProduct> orderProducts = orderProductRepository.getAllByShoppingOrderId(orderId);
		boolean flag = true;
		//查看订单下的商品是否不包含未处理
		for (OrderProduct orderProduct : orderProducts) {
			if("0".equals(orderProduct.getFlg())){
				flag = false;
				break;
			}
		}
		//查看订单下的包裹，是否均已发送完成
		List<Parcels> parcelses = parcelsRepository.findByOrderId(orderId);
		for (Parcels parcelsTemp : parcelses) {
			if(5==parcelsTemp.getStatus()||12==parcelsTemp.getStatus()){
				continue;
			}else{
				flag = false;
				break;
			}
		}
		if(flag){
			ShoppingOrder shoppingOrder = (ShoppingOrder) ServiceFactory.getCacheService().getObject(Constants.shoppingOrder_KEY+orderId);
			if(shoppingOrder==null){
				shoppingOrder = shoppingOrderRepository.findOne(orderId);
			}
			shoppingOrder.setStatus(3);
			shoppingOrder = shoppingOrderRepository.save(shoppingOrder);
			ServiceFactory.getCacheService().setObject(Constants.shoppingOrder_KEY+shoppingOrder.getId(), shoppingOrder, 0);
			logger.info("订单"+shoppingOrder.getOrderCode()+"已完成");
		}
	}

	/**
	 * 
	 * <p>Title: getFromsietWithOrderCode</p> 
	 * <p>Description: 根据订单号获得商品来源</p> 
	 * @param orderId
	 * @return
	 */
	public String getFromsietWithOrderCode(Long orderId) {
		return shoppingOrderRepository.getFromsietWithOrderCode(orderId);
	}

	public void save(ShoppingOrder shoppingOrder) {
		shoppingOrderRepository.save(shoppingOrder);
	}

	public ShoppingOrder queryShoppingOrderByOrderCode(String orderCode) {
		return shoppingOrderRepository.queryShoppingOrderByOrderCode(orderCode);
	}

	
}
