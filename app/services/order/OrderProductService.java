package services.order;

import java.sql.ResultSet;
import java.sql.Types;
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

import models.OrderProduct;
import models.Parcels;
import models.ParcelsPro;
import models.ShoppingOrder;
import models.admin.AdminUser;
import models.product.Fromsite;
import models.product.Product;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;

import play.Logger;
import repositories.OrderProductRepository;
import repositories.Order.OrderRepository;
import services.ServiceFactory;
import services.admin.AdminUserService;
import services.parcels.ParcelsProService;
import services.parcels.ParcelsService;
import services.product.ProductService;
import utils.Constants;
import utils.Dates;
import utils.JdbcOper;
import utils.Numbers;
import utils.StringUtil;
import vo.OrderCompanyPageVO;
import vo.OrderVO;
import forms.order.OrderForm;

/**
 * 
 * <p>Title: OrderProductService.java</p> 
 * <p>Description: 商品关联订单</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年7月28日  下午6:32:55
 * @version
 */
@Named
@Singleton
public class OrderProductService {

    private static final Logger.ALogger logger = Logger.of(OrderProductService.class);

    @Inject
    private OrderProductRepository orderProductRepository;
    @Inject
    private OrderRepository orderRepository;
    @Inject
    private ParcelsService parcelsService;
    @Inject
    private ParcelsProService parcelsProService;
    @Inject
    private ProductService productService;
    @Inject
    private AdminUserService adminUserService;

	public Page<OrderProduct> queryOrderProductWithPage(OrderForm form) {
		return this.orderProductRepository.findAll(new OrderProductQuery(form),new PageRequest(form.page, form.size,new Sort(Direction.DESC, "orderId")));
	}

	/**
     * 订单商品查询内部类
     * @author luobotao
     * Date: 2015年4月17日 上午10:04:26
     */
    private static class OrderProductQuery implements Specification<OrderProduct> {

        private final OrderForm form;

        public OrderProductQuery(final OrderForm form) {
            this.form = form;
        }

        @Override
        public Predicate toPredicate(Root<OrderProduct> orderProduct, CriteriaQuery<?> query,
                                     CriteriaBuilder builder) {
            List<Predicate> predicates = new ArrayList<>();
            Path<Long> orderProId = orderProduct.get("id");
            if(form.orderIds!=null ){
            	if(form.orderIds.size()>0){
            		predicates.add(orderProId.in(form.orderIds));
            	}else{
            		predicates.add(orderProId.in(0));
            	}
            }
            
            Predicate[] param = new Predicate[predicates.size()];
            
            predicates.toArray(param);
            
            return query.where(param).getRestriction();
        }
    }

    /**
     * 
     * <p>Title: queryOrderProductById</p> 
     * <p>Description: 查询订单商品信息</p> 
     * @param id
     * @return
     */
	public OrderProduct queryOrderProductById(Long id) {
		OrderProduct orderProduct = (OrderProduct) ServiceFactory.getCacheService().getObject(Constants.orderProduct_KEY+id );//从缓存读入
    	if(orderProduct==null){
    		orderProduct = orderProductRepository.findOne(id);
    		if(orderProduct == null){
    			return null;
    		}
    		ServiceFactory.getCacheService().setObject(Constants.orderProduct_KEY+id, orderProduct,0 );//写入缓存
    	}
        return orderProduct;
	}

	/**
	 * 
	 * <p>Title: getOrderProductListByIds</p> 
	 * <p>Description: 根据ID集合获取相应的model</p> 
	 * @param sopIds
	 * @return
	 */
	public List<OrderProduct> getOrderProductListByIds(String sopIds) {
		List<OrderProduct> orderProductList = new ArrayList<OrderProduct>();
		String[] sopIdes = sopIds.split(",");
		for (String str : sopIdes) {
			OrderProduct orderProduct = orderProductRepository.findOne(Numbers.parseLong(str, 0L));
			if(orderProduct!=null){
				orderProductList.add(orderProduct);
			}
		}
		return orderProductList;
	}

	/**
	 * 
	 * <p>Title: getAllByShoppingOrderId</p> 
	 * <p>Description: 根据订单id获取旗下订单商品</p> 
	 * @param id
	 * @return
	 */
	public List<OrderProduct> getAllByShoppingOrderId(Long orderId) {
		return orderProductRepository.getAllByShoppingOrderId(orderId);
	}
	
	/**
	 * 
	 * <p>Title: updateOrderProductFlg</p> 
	 * <p>Description: 更新订单处理状态</p> 
	 * @param orderProducts
	 */
	@Transactional
	public void updateOrderProductFlg(List<OrderProduct> orderProducts,String flg) {
		for (OrderProduct orderProduct : orderProducts) {
			logger.info(orderProduct.getId()+"--------->>>order refund 1<<<<--------"+orderProduct.getFlg());
			orderProduct.setFlg(flg);
			logger.info(orderProduct.getId()+"--------->>>order refund 2<<<<--------"+flg);
			orderProduct = orderProductRepository.save(orderProduct);
			ServiceFactory.getCacheService().setObject(Constants.orderProduct_KEY+orderProduct.getId(), orderProduct, 0);
		}
	}

	/**
	 * 
	 * <p>Title: refundOrder</p> 
	 * <p>Description: 订单提交申请退款操作</p> 
	 * @param sopIds
	 */
	@Transactional
	public void refundOrder(String soId, String sopIds) {
		OrderProduct opTemp = orderProductRepository.getOne(Long.parseLong(sopIds));
		Product product = productService.findProductById(opTemp.getpId());
		//获取该订单下跟商品同一个类型的包裹
		List<Parcels> parcels = parcelsService.queryParcelsByOrderId(Long.parseLong(soId),product.getTyp());
		if(parcels!=null&&parcels.size()>0){
			for (Parcels parcel : parcels) {
				//获取该包裹下的所有商品
				List<ParcelsPro> pps = parcelsProService.getAllByParcelsId(parcel.getId());
				for (ParcelsPro pp : pps) {
					//更改订单状态为申请退款
					updateShoppingOrderProduct(pp.getShopProId());
					//删除相应的包裹商品信息
					parcelsProService.del(pp);
				}
				//删除相应的包裹信息
				parcelsService.del(parcel);
			}
		}else{
			logger.info("未获取到相应的包裹信息，直接进行申请退款操作。查询条件如下！订单ID："+soId+", 商品类型："+product.getTyp());
			updateShoppingOrderProduct(Long.parseLong(sopIds));
		}
	}
	
	/**
	 * 
	 * <p>Title: updateShoppingOrder</p> 
	 * <p>Description: 更改订单状态为申请退款</p> 
	 * @param shopProId
	 */
	@Transactional
	private void updateShoppingOrderProduct(Long shopProId) {
		//更改订单的处理状态为申请退款
		OrderProduct op = orderProductRepository.findOne(shopProId);
		if(op!=null){
			op.setFlg("2");
			op = orderProductRepository.save(op);
			ServiceFactory.getCacheService().setObject(Constants.orderProduct_KEY+op.getId(), op, 0);
			logger.info("订单ID："+op.getOrderId()+"，订单商品ID:"+op.getpId()+"，从包裹退回成订单,并申请退款完成");
		}else{
			logger.info("订单ID："+op.getOrderId()+"，订单商品ID:"+op.getpId()+"，从包裹退回成订单，并申请退款失败");
		}
		
	}

	/**
	 * 
	 * <p>Title: backOrder</p> 
	 * <p>Description: 包裹退回成订单操作</p> 
	 * @param shopProId
	 */
	@Transactional
	public void backOrder(Long shopProId) {
		OrderProduct op = orderProductRepository.getOne(shopProId);
		if(op!=null){
			op.setFlg("0");
			op = orderProductRepository.save(op);
			ServiceFactory.getCacheService().setObject(Constants.orderProduct_KEY+op.getId(), op, 0);
			logger.info("订单ID："+op.getOrderId()+"，订单商品ID:"+op.getpId()+"，从包裹退回成订单完成");
		}else{
			logger.info("订单ID："+op.getOrderId()+"，订单商品ID:"+op.getpId()+"，从包裹退回成订单失败");
		}
	}
    
	/**
	 * 
	 * <p>Title: getTotalsWithForm</p> 
	 * <p>Description: 获取满足天剑的订单总量</p> 
	 * @param form
	 * @return
	 */
	public Long getTotalsWithForm(OrderForm form) {
		StringBuffer sb = new StringBuffer("select count(sop.id) from shopping_Order_Pro sop, shopping_Order so,product p ");
		if(form.fromUid!=null && form.fromUid.longValue()!=0){
			sb.append(",`user` u ");
        }
		if("3".equals(form.typ)){
			sb.append(",`adminproduct` ap,`admin` a ");
		}
		sb.append(" where sop.pId=p.pId and sop.orderId=so.id and p.pid!=1880 ");
		//支付方式
		if (!Strings.isNullOrEmpty(form.payMethod) && !"-1".equals(form.payMethod)) {
			sb.append("and ").append("so.paymethod=").append(form.payMethod).append(" ");
        }
		//商品类型
		if (!Strings.isNullOrEmpty(form.typ)&& !"0".equals(form.typ)&&!"3".equals(form.typ)&&!"4".equals(form.typ)) {
			if("90".equals(form.typ)){
				sb.append("and so.sfcode<>'' ");
			}else{
				sb.append("and ").append("p.typ=").append(form.typ).append(" ");
			}
		}
		//撒娇订单
        if (!Strings.isNullOrEmpty(form.ordertype) && !"-1".equals(form.ordertype) ) {
        	sb.append("and ").append("so.ordertype='").append(form.ordertype).append("' ");
        }
        //订单号
        if (!Strings.isNullOrEmpty(form.orderCode)) {
        	//嘿客号
        	if (!Strings.isNullOrEmpty(form.sfcode)) {
        		sb.append("and (").append("so.orderCode='").append(form.orderCode.trim()).append("' or ").append("so.sfcode='").append(form.sfcode.trim()).append("') ");
        	}else{
        		sb.append("and ").append("so.orderCode='").append(form.orderCode.trim()).append("' ");
        	}
        }
        //商品ID或newsku
        if (!Strings.isNullOrEmpty(form.pId)&&!Strings.isNullOrEmpty(form.newSku)) {
        	sb.append("and ").append("(p.pId = ").append(form.pId).append(" or p.newSku='").append(form.newSku).append("') ");
        }
        //收货人用户名和商品名称
        if (!Strings.isNullOrEmpty(form.name)&&!Strings.isNullOrEmpty(form.title)) {
        	sb.append("and ").append("(so.name like '%").append(form.name.trim()).append("%' or p.title like '%").append(form.title.trim()).append("%' ").append(" or p.newSku='").append(form.newSku).append("') ");
        }
        //电话
        if (!Strings.isNullOrEmpty(form.phone)) {
        	sb.append("and ").append("so.phone like '").append(form.phone).append("' ");
        }
        //下单时间
        if (form.between != null) {
            sb.append("and ").append("so.date_add between '").append(Dates.formatEngLishDateTime(form.between.start)).append("' and '").append(Dates.formatEngLishDateTime(form.between.end)).append("' ");
        }
		//支付状态
		sb.append("and ").append("so.paystat=").append(form.paystat).append(" ");
        //处理状态
        if (!Strings.isNullOrEmpty(form.orderProFlg)&&!"-1".equals(form.orderProFlg)) {
        	sb.append("and ").append("sop.flg='").append(form.orderProFlg).append("' ");
        }else{
        	if("refund".equals(form.opertype)){
        		sb.append("and ").append("sop.flg in('2','3') ");
        	}
        }
        //来源网站
        if (!Strings.isNullOrEmpty(form.fromsite) && !"-1".equals(form.fromsite) ) {
        	sb.append("and ").append("p.fromsite=").append(form.fromsite).append(" ");
        }
        if(form.fromUid!=null && form.fromUid.longValue()!=0){
        	sb.append(" and u.fromUid=").append(form.fromUid).append(" and so.uId=u.uid ");
        }
        if("3".equals(form.typ)){
	        if(form.adminid!=-1 ){
	        	sb.append(" and a.id=").append(form.adminid).append(" and a.adminType=2 and ap.pid=p.pid and ap.adminid = a.id");
	        }else{
	        	sb.append(" and a.adminType=2 and ap.pid=p.pid and ap.adminid = a.id");
	        }
        }else if("4".equals(form.typ)){//增加对预售商品的订单查询条件
			sb.append(" and so.ordertype=3 ");
			if("1".equals(form.preSell)){
				sb.append(" and so.status = 0 ");
			}else if("2".equals(form.preSell)){
				sb.append(" and so.status = 21 ");
			}else if("3".equals(form.preSell)){
				sb.append(" and so.status = 22 ");
			}else if("4".equals(form.preSell)){
				sb.append(" and so.status = 1 ");
			}
		}
        String sql = sb.toString();
        logger.info(sql+"===============");
        JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
        Long totals = (long) 0;
        db.getPrepareStateDao(sql);
		try {
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				totals = rs.getLong(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return totals;
	}
	
	 /**
     * 
     * <p>Title: queryOrderProIdList</p> 
     * <p>Description: 根据条件查询出满足的订单集合</p> 
     * @param formPage
     * @return
     */
	public List<OrderVO> queryOrderProIdList(OrderForm form) {
		StringBuffer sb = new StringBuffer("select sop.id,so.sfcode,so.orderCode,p.pid,p.newSku,p.listpic,p.title,p.exturl,p.fromsite,sop.price,sop.counts,so.name,so.phone,so.ordertype,so.paymethod,so.date_add,sop.flg,so.id,p.typ,so.paystat,so.status "
				+ " from shopping_Order_Pro sop, shopping_Order so,product p");
		if(form.fromUid!=null && form.fromUid.longValue()!=0){
			sb.append(",`user` u ") ;
        }
		if("3".equals(form.typ)){
			sb.append(",`adminproduct` ap,`admin` a ");
		}
		sb.append(" where sop.pId=p.pId and sop.orderId=so.id and p.pid!=1880 ") ;
		//支付方式
		if (!Strings.isNullOrEmpty(form.payMethod) && !"-1".equals(form.payMethod)) {
			sb.append("and ").append("so.paymethod=").append(form.payMethod).append(" ");
        }
		//商品类型
		if (!Strings.isNullOrEmpty(form.typ)&& !"0".equals(form.typ)&& !"3".equals(form.typ)&&!"4".equals(form.typ)) {
			if("90".equals(form.typ)){
				sb.append("and so.sfcode<>'' ");
			}else{
				sb.append("and ").append("p.typ=").append(form.typ).append(" ");
			}
		}
		//撒娇订单
        if (!Strings.isNullOrEmpty(form.ordertype) && !"-1".equals(form.ordertype) ) {
        	sb.append("and ").append("so.ordertype='").append(form.ordertype).append("' ");
        }
        //订单号
        if (!Strings.isNullOrEmpty(form.orderCode)) {
        	//嘿客号
        	if (!Strings.isNullOrEmpty(form.sfcode)) {
        		sb.append("and (").append("so.orderCode='").append(form.orderCode.trim()).append("' or ").append("so.sfcode='").append(form.sfcode.trim()).append("') ");
        	}else{
        		sb.append("and ").append("so.orderCode='").append(form.orderCode.trim()).append("' ");
        	}
        }
        //商品ID或newsku
        if (!Strings.isNullOrEmpty(form.pId)&&!Strings.isNullOrEmpty(form.newSku)) {
        	sb.append("and ").append("(p.pId = ").append(form.pId).append(" or p.newSku='").append(form.newSku).append("') ");
        }
        //收货人用户名和商品名称
        if (!Strings.isNullOrEmpty(form.name)&&!Strings.isNullOrEmpty(form.title)) {
        	sb.append("and ").append("(so.name like '%").append(form.name.trim()).append("%' or p.title like '%").append(form.title.trim()).append("%' ").append(" or p.newSku='").append(form.newSku).append("') ");
        }
        //电话
        if (!Strings.isNullOrEmpty(form.phone)) {
        	sb.append("and ").append("so.phone like '").append(form.phone).append("' ");
        }
        //下单时间
        if (form.between != null) {
            sb.append("and ").append("so.date_add between '").append(Dates.formatEngLishDateTime(form.between.start)).append("' and '").append(Dates.formatEngLishDateTime(form.between.end)).append("' ");
        }
        //支付状态
      	sb.append("and ").append("so.paystat=").append(form.paystat).append(" ");
        //处理状态
        if (!Strings.isNullOrEmpty(form.orderProFlg)&& !"-1".equals(form.orderProFlg)) {
        	sb.append("and ").append("sop.flg='").append(form.orderProFlg).append("' ");
        }else{
        	if("refund".equals(form.opertype)){
        		sb.append("and ").append("sop.flg in('2','3') ");
        	}
        }
        //来源网站
        if (!Strings.isNullOrEmpty(form.fromsite) && !"-1".equals(form.fromsite) ) {
        	sb.append("and ").append("p.fromsite=").append(form.fromsite).append(" ");
        }
        if(form.fromUid!=null && form.fromUid.longValue()!=0){
        	sb.append(" and u.fromUid=").append(form.fromUid).append(" and so.uId=u.uid ");
        }
        if("3".equals(form.typ)){
	        if(form.adminid!=-1 ){
	        	sb.append(" and a.id=").append(form.adminid).append(" and a.adminType=2 and ap.pid=p.pid and ap.adminid = a.id ");
	        }else{
	        	sb.append(" and a.adminType=2 and ap.pid=p.pid and ap.adminid = a.id ");
	        }
        }else if("4".equals(form.typ)){
			sb.append(" and so.ordertype=3 ");
			if("1".equals(form.preSell)){
				sb.append(" and so.status = 0 ");
			}else if("2".equals(form.preSell)){
				sb.append(" and so.status = 21 ");
			}else if("3".equals(form.preSell)){
				sb.append(" and so.status = 22 ");
			}else if("4".equals(form.preSell)){
				sb.append(" and so.status = 1 ");
			}
		}
        sb.append(" order by sop.id desc ");
        sb.append("limit ").append(form.page*form.size).append(",").append(form.size).append(" ");
        String sql = sb.toString();
        logger.info(sql); 
        JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
        List<OrderVO> result = new ArrayList<OrderVO>();
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				OrderVO orderVo = new OrderVO();
				orderVo.orderProId=rs.getLong(1);
				orderVo.sfcode=rs.getString(2);
				orderVo.orderCode=rs.getString(3);
				orderVo.pid=rs.getLong(4);
				orderVo.newSku=rs.getString(5);
				orderVo.listpic=StringUtil.getListpic(rs.getString(6));
				orderVo.title=rs.getString(7);
				
				if(form.fromUid!=null && form.fromUid.longValue()!=0){
					orderVo.exturl=StringUtil.getAPIDomain()+"/H5/product?pid="+rs.getLong(4);
		        }else{
		        	orderVo.exturl=rs.getString(8);
		        }
				Fromsite fromsiteV = productService.queryFromsiteById(rs.getInt(9));
				orderVo.fromsite=fromsiteV==null?"":fromsiteV.getName();
				orderVo.price=rs.getString(10);
				orderVo.counts=rs.getString(11);
				orderVo.name=rs.getString(12);
				orderVo.phone=rs.getString(13);
				if(rs.getInt(14)==1){
					orderVo.ordertype="否";
				}else if(rs.getInt(14)==2){
					orderVo.ordertype="是";
				}
				orderVo.paymethod=Constants.PayMethod.status2Message(rs.getInt(15));
				orderVo.dateAdd=rs.getString(16);
				orderVo.flg=Constants.OrderProFlg.flgs2Message(rs.getInt(17));
				orderVo.orderId=rs.getLong(18);
				if("2".equals(rs.getString(19))){
					List<AdminUser> lyAdminUsers = adminUserService.queryAdminUserByPid(rs.getLong(4),-1);
					if(lyAdminUsers!=null&&lyAdminUsers.size()>0){
						if(lyAdminUsers.size()>1){
							orderVo.typeName="多家联营";
						}else{
							orderVo.typeName="联营-"+lyAdminUsers.get(0).getRealname();
						}
					}else{
						orderVo.typeName=Constants.TypsProduct.typs2Message(Integer.parseInt(rs.getString(19)));
					}
				}else{
					orderVo.typeName=Constants.TypsProduct.typs2Message(Integer.parseInt(rs.getString(19)));
				}
				orderVo.typ=Integer.parseInt(rs.getString(19));
				List<Parcels> parcels = parcelsService.queryParcelsByOrderId(rs.getLong(18), "0");
     			if(parcels!=null&&parcels.size()>0){
     				orderVo.existsParcels=true;
     			}else{
     				orderVo.existsParcels=false;
     			}
     			orderVo.paystat=rs.getString(20);
     			orderVo.status = rs.getString(21);
				result.add(orderVo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}
	
	
	/**
	 * 查询商户的订单列表
	 * @param form
	 * @return
	 */
	public OrderCompanyPageVO queryOrderProIdCompanyList(OrderForm form) {
		String start="";
		String end="";
		//下单时间
        if (form.between != null) {
            start = Dates.formatEngLishDateTime(form.between.start);
            end = Dates.formatEngLishDateTime(form.between.end);
        }
		//将这些商品的库存进行恢复
		String sql = "{call get_endorsement_gid ("+form.fromUid+",'"+start+"','"+end+"','"+form.keyword+"',"+form.page+","+form.size+")}";// SQL语句// //调用存储过程
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		
		db.cst = db.getCalledbleDao("{call get_endorsement_gid(?,?,?,?,?,?,?,?)}");
		
		
		OrderCompanyPageVO result= new OrderCompanyPageVO();
		List<OrderVO> orderVOList = new ArrayList<OrderVO>();
		try {
			db.cst.setInt(1, form.fromUid.intValue());
			db.cst.setString(2, start);
			db.cst.setString(3, end);
			db.cst.setString(4, form.keyword);
			db.cst.setString(5, form.orderCode);
			db.cst.setInt(6, form.page);
			db.cst.setInt(7, form.size);
			db.cst.registerOutParameter(8, Types.INTEGER);
			db.rs = db.cst.executeQuery();
			while(db.rs.next()){
				OrderVO orderVo = new OrderVO();
				orderVo.orderProId=db.rs.getLong("spid");
				orderVo.orderCode=db.rs.getString("orderCode");
				orderVo.pid=db.rs.getLong("pid");
				orderVo.newSku=db.rs.getString("newSku");
				orderVo.listpic=StringUtil.getListpic(db.rs.getString("listpic"));
				orderVo.title=db.rs.getString("title");
				
				if(form.fromUid!=null && form.fromUid.longValue()!=0){
					orderVo.exturl=StringUtil.getAPIDomain()+"/H5/product?pid="+db.rs.getLong("pid");
				}else{
					orderVo.exturl=db.rs.getString("exturl");
				}
				Fromsite fromsiteV = productService.queryFromsiteById(db.rs.getInt("fromsite"));
				orderVo.fromsite=fromsiteV==null?"":fromsiteV.getName();
				orderVo.price=db.rs.getString("price");
				orderVo.counts=db.rs.getString("counts");
				orderVo.name=db.rs.getString("name");
				orderVo.phone=db.rs.getString("phone");
				orderVo.dateAdd=db.rs.getString("date_add");
				orderVo.flg=Constants.OrderProFlg.flgs2Message(db.rs.getInt("flg"));
				orderVo.orderId=db.rs.getLong("orderId");
				orderVOList.add(orderVo);
			}
			result.totals=db.cst.getLong(8);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		result.orderVOList = orderVOList;
		return result;
	}

	@Transactional
	public void updateOrderWithPaystat(String soId, String sopIds,
			String paystat) {
		ShoppingOrder shoppingOrder = orderRepository.getOne(Long.parseLong(soId));
		shoppingOrder.setPaystat(Integer.parseInt(paystat));
		orderRepository.save(shoppingOrder);
	}
	
}
