package services.product;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import forms.ProductManageForm;
import forms.admin.FreshTaskQueryForm;
import forms.order.OrderForm;
import models.OrderProduct;
import models.Parcels;
import models.ShoppingOrder;
import models.admin.AdminUser;
import models.product.AdminProduct;
import models.product.Category;
import models.product.Currency;
import models.product.Fromsite;
import models.product.Product;
import models.product.ProductDetail;
import models.product.ProductDetailPram;
import models.product.ProductGroup;
import models.product.ProductImages;
import models.product.ProductUnion;
import play.Logger;
import repositories.OrderProductRepository;
import repositories.ShoppingOrderRepository;
import repositories.parcels.ParcelsRepository;
import repositories.product.AdminProductRepository;
import repositories.product.CategoryRepository;
import repositories.product.CurrencyRepository;
import repositories.product.FromsiteRepository;
import repositories.product.ProductDetailParamRepository;
import repositories.product.ProductDetailRepository;
import repositories.product.ProductImagesRepository;
import repositories.product.ProductRepository;
import repositories.product.ProductUnionRepository;
import services.ServiceFactory;
import services.admin.AdminUserService;
import utils.Constants;
import utils.Dates;
import utils.Htmls;
import utils.JdbcOper;
import utils.Numbers;
import utils.StringUtil;
import vo.ImportAddStockVO;
import vo.OrderStaticsVO;
import vo.ProductSellTopVO;

/**
 * 商品相关Service
 * @author luobotao
 * Date: 2015年4月17日 下午2:26:14
 */
@Named
@Singleton
public class ProductService {

    private static final Logger.ALogger logger = Logger.of(ProductService.class);

    @Inject
    private ProductRepository productRepository;
    @Inject
    private ProductUnionRepository productUnionRepository;
    @Inject
    private ProductDetailRepository productDetailRepository;
    @Inject
    private ProductImagesRepository productImagesRepository;
    @Inject
    private ProductDetailParamRepository productDetailParamRepository;
    @Inject
    private ShoppingOrderRepository shoppingOrderRepository;
    @Inject
    private ParcelsRepository pardelsRepository;
    @Inject
    private OrderProductRepository orderProductRepository;
    @Inject
    private CategoryRepository categoryRepository;
    @Inject
    private CurrencyRepository currencyRepository;
    @Inject
    private FromsiteRepository fromsiteRepository;
    @Inject
    private AdminProductRepository adminProductRepository;
    @Inject
    private ProductGroupService productGroupService;
    @Inject
    private AdminUserService adminUserService;
    
    public Product findProductById(Long id) {
    	Product product = (Product) ServiceFactory.getCacheService().getObject(Constants.product_KEY+id );//从缓存读入
    	if(product==null){
    		product = productRepository.findOne(id);
    		if(product == null){
    			return null;
    		}else{
    			if(product.getIshot()!=1 &&("1".equals(product.getPtyp())||"2".equals(product.getPtyp()))){//只写入普通和撒娇商品  组合、预售和定时开抢将不写redis
    	    		ServiceFactory.getCacheService().setObject(Constants.product_KEY+product.getPid(), product, 0);//将商品写入cache
    	    	}
    		}
    	}
        return product;
    }
    
    /**
     * 分页查询所有商品
     * @param form
     * @return
     */
    @Transactional(readOnly = true)
    public Page<Product> queryProductPage(ProductManageForm form) {
        return this.productRepository.findAll(new ProductQuery(form),
                new PageRequest(form.page, form.size,new Sort(Direction.DESC, "pid")));
    }
    
	/**
	 * 汇率符号
	 * @param id
	 * @return
	 */
	public Currency queryCurrencyById(int id) {
		Currency currency = (Currency) ServiceFactory.getCacheService().getObject(Constants.currency_KEY+id );//从缓存读入
    	if(currency==null){
    		currency =currencyRepository.findOne(id);
    		ServiceFactory.getCacheService().setObject(Constants.currency_KEY+id, currency,0 );//写入缓存
    	}
		return currency;
	}
	/**
	 * 汇率符号
	 * @param id
	 * @return
	 */
	public List<Currency> currencyList() {
		List<Currency> currency = currencyRepository.findAll();
		return currency;
	}
	/**
	 * 来源
	 * @param id
	 * @return
	 */
	public Fromsite queryFromsiteById(int id) {
		Fromsite fromsite = (Fromsite) ServiceFactory.getCacheService().getObject(Constants.fromsite_KEY+id );//从缓存读入
    	if(fromsite==null){
    		fromsite =fromsiteRepository.findOne(id);
    		ServiceFactory.getCacheService().setObject(Constants.fromsite_KEY+id, fromsite,0 );//写入缓存
    	}
		return fromsite;
	}
	/**
	 * 获取所有来源
	 * @return
	 */
	public List<Fromsite> queryAllFromsite() {
		List<Fromsite> fromsiteList = fromsiteRepository.findAll();
		return fromsiteList;
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
    
    public ShoppingOrder getShoppingOrderById(Long id) {
    	return shoppingOrderRepository.findOne(id);
    }
    

    /**
     * 查询商品订单
     * @param form
     * @return
     */
    @Transactional(readOnly = true)
    public Page<OrderProduct> queryorderProductPage(FreshTaskQueryForm form) {
        return this.orderProductRepository.findAll(new OrderProductQuery(form),
                new PageRequest(form.page, form.size,new Sort(Direction.DESC, "orderId")));
    }
    
    /**
     * 保存商品
     * @param pro
     * @return
     */
    public Product saveProduct(Product pro){
    	if(!StringUtils.isBlank(pro.getListpic())){
    		pro.setListpic(pro.getListpic().replaceAll(StringUtil.getPICDomain(), ""));
    	}
    	Product product= productRepository.save(pro);
    	if(product.getIshot()!=1 && ("1".equals(product.getPtyp())||"2".equals(product.getPtyp()))){//只写入普通和撒娇商品
    		ServiceFactory.getCacheService().setObject(Constants.product_KEY+product.getPid(), product, 0);//将商品写入cache
    	}else{
    		ServiceFactory.getCacheService().clear(Constants.product_KEY+product.getPid());
    	}
    	return product;
    }
    /**
     * 保存商品
     * @param pro
     * @return
     */
    @Transactional
    public int updateProductStock(int stock,Long pid){
    	int result = productRepository.updateProductStock(stock,pid);
    	Product product = productRepository.findOne(pid);
		ServiceFactory.getCacheService().setObject(Constants.product_KEY+pid, product,0 );//写入缓存
		return result;
    }
    /**
     * 根据订单号去获取此订单
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    public ShoppingOrder getShoppingOrderByOrderCode(String orderCode) {
    	return shoppingOrderRepository.findByOrderCode(orderCode);
    }
    
    /**
     * 保存订单
     * @param shoppingOrder
     * @return
     */
    @Transactional
    public ShoppingOrder saveShoppingOrder(ShoppingOrder shoppingOrder) {
    	return shoppingOrderRepository.save(shoppingOrder);
    }
    

	/**
	 * 查询商品订单
	 * @param formPage
	 * @return
	 */
    @Transactional(readOnly = true)
	public List<ShoppingOrder> queryShoppingOrderList(FreshTaskQueryForm formPage) {
		return this.shoppingOrderRepository.findAll(new ShoppingOrderQuery(formPage));
	}
	
	/**
	 * 查询包裹
	 * @param formPage
	 * @return
	 */
	@Transactional(readOnly = true)
	public Page<Parcels> queryPardelsPage(FreshTaskQueryForm form) {
        return this.pardelsRepository.findAll(new ParcelsQuery(form),
                new PageRequest(form.page, form.size));
    }
	
	@Transactional(readOnly = true)
	public List<Product> queryProductListByParcelsId(Long parcelsId) {
		List<Product> productList = this.productRepository.queryProductListByParcelsId(parcelsId);
		for(Product product:productList){
			product.setCounts(this.productRepository.queryProductCounts(parcelsId,product.getPid()));
		}
        return productList;
    }
	
	/**
	 * 阿里支付生成包裹
	 * @param orderId
	 */
	public void createParcelByOrder(Long orderId) {
		String sql = "{call run_orderPardel (" + orderId + ")}";// SQL语句// //调用存储过程
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			db.pst.executeQuery();// 执行语句，得到结果集
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
	}
	
	/**
	 * 获取所有品类
	 * @return
	 */
	public List<Category> getCategoryList() {
		return categoryRepository.findAll();
	}
	
	/**
	 * 订单数据统计
	 * @param start
	 * @param end
	 * @return
	 */
	public List<OrderStaticsVO> getOrderStatics(String start,String end) {
		List<OrderStaticsVO> result = new ArrayList<OrderStaticsVO>();
		String sql = "select date_txt,u_add,u_act,ur_login,ur_add,uo_newadd,ufo_newadd,uo_add,ufo_add,o_add,of_add,all_add_total,sfo_sum from data_statistics where date_txt between '"+start+"' and '"+end+"'";// SQL语句 
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			OrderStaticsVO orderStaticsVOSum = new OrderStaticsVO();
			OrderStaticsVO orderStaticsVOAvg = new OrderStaticsVO();
			orderStaticsVOSum.dateTxt="合计";
			orderStaticsVOAvg.dateTxt="均值";
			Integer count = 0;
			Double newPayRate =0.0;
			Double payRate =0.0;
			Double orderPrice =0.0;
			Double perPrice =0.0;
			while(rs.next()){
				OrderStaticsVO orderStaticsVO = new OrderStaticsVO();
				orderStaticsVO.dateTxt=rs.getString(1);
				orderStaticsVO.uAdd=rs.getInt(2);
				orderStaticsVO.uAct=rs.getInt(3);
				orderStaticsVO.urLogin=rs.getInt(4);
				orderStaticsVO.urAdd=rs.getInt(5);
				orderStaticsVO.uoNewadd=rs.getInt(6);
				orderStaticsVO.ufoNewadd=rs.getInt(7);
				orderStaticsVO.uoAdd=rs.getInt(8);
				orderStaticsVO.ufoAdd=rs.getInt(9);
				orderStaticsVO.oAdd=rs.getInt(10);
				orderStaticsVO.ofAdd=rs.getInt(11);
				orderStaticsVO.newPayRate=Numbers.formatDouble(100.0*rs.getInt(7)/rs.getInt(5), "#0.00");
				orderStaticsVO.payRate=Numbers.formatDouble(100.0*rs.getInt(9)/rs.getInt(3), "#0.00");
				orderStaticsVO.orderPrice=Numbers.formatDouble(rs.getDouble(12)-rs.getDouble(13), "#0.00");
				orderStaticsVO.perPrice=Numbers.formatDouble((rs.getDouble(12)-rs.getDouble(13))/rs.getInt(9), "#0.00");
				orderStaticsVOSum.uAdd += rs.getInt(2);
				orderStaticsVOSum.uAct += rs.getInt(3);
				orderStaticsVOSum.urLogin += rs.getInt(4);
				orderStaticsVOSum.urAdd += rs.getInt(5);
				orderStaticsVOSum.uoNewadd += rs.getInt(6);
				orderStaticsVOSum.ufoNewadd += rs.getInt(7);
				orderStaticsVOSum.uoAdd += rs.getInt(8);
				orderStaticsVOSum.ufoAdd += rs.getInt(9);
				orderStaticsVOSum.oAdd += rs.getInt(10);
				orderStaticsVOSum.ofAdd += rs.getInt(11);
				newPayRate += 100.0*rs.getInt(7)/rs.getInt(5);
				payRate += 100.0*rs.getInt(9)/rs.getInt(3);
				orderPrice += rs.getDouble(12)-rs.getDouble(13);
				perPrice += (rs.getDouble(12)-rs.getDouble(13))/rs.getInt(9);
				result.add(orderStaticsVO);
				count++;
			}
			if(count>0){
				orderStaticsVOSum.newPayRate=Numbers.formatDouble(newPayRate, "#0.00");
				orderStaticsVOSum.payRate=Numbers.formatDouble(payRate, "#0.00");
				orderStaticsVOSum.orderPrice=Numbers.formatDouble(orderPrice, "#0.00");
				orderStaticsVOSum.perPrice=Numbers.formatDouble(perPrice, "#0.00");
				result.add(orderStaticsVOSum);
				orderStaticsVOAvg.uAdd = orderStaticsVOSum.uAdd/count;
				orderStaticsVOAvg.uAct = orderStaticsVOSum.uAct/count;
				orderStaticsVOAvg.urLogin = orderStaticsVOSum.urLogin/count;
				orderStaticsVOAvg.urAdd = orderStaticsVOSum.urAdd/count;
				orderStaticsVOAvg.uoNewadd = orderStaticsVOSum.uoNewadd/count;
				orderStaticsVOAvg.ufoNewadd = orderStaticsVOSum.ufoNewadd/count;
				orderStaticsVOAvg.uoAdd = orderStaticsVOSum.uoAdd/count;
				orderStaticsVOAvg.ufoAdd = orderStaticsVOSum.ufoAdd/count;
				orderStaticsVOAvg.oAdd = orderStaticsVOSum.oAdd/count;
				orderStaticsVOAvg.ofAdd = orderStaticsVOSum.ofAdd/count;
				orderStaticsVOAvg.newPayRate = Numbers.formatDouble(newPayRate/count, "#0.00");
				orderStaticsVOAvg.payRate = Numbers.formatDouble(payRate/count, "#0.00");
				orderStaticsVOAvg.orderPrice = Numbers.formatDouble(orderPrice/count, "#0.00");
				orderStaticsVOAvg.perPrice = Numbers.formatDouble(perPrice/count, "#0.00");
				result.add(orderStaticsVOAvg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}
	
	/**
     * 订单查询内部类
     * @author luobotao
     * Date: 2015年4月17日 上午10:04:26
     */
    private static class ShoppingOrderQuery implements Specification<ShoppingOrder> {

        private final FreshTaskQueryForm form;

        public ShoppingOrderQuery(final FreshTaskQueryForm form) {
            this.form = form;
        }

        @Override
        public Predicate toPredicate(Root<ShoppingOrder> shoppingOrder, CriteriaQuery<?> query,
                                     CriteriaBuilder builder) {
            Path<String> taskId = shoppingOrder.get("taskId");
            Path<String> ordertype = shoppingOrder.get("ordertype");
            Path<String> payMethod = shoppingOrder.get("paymethod");
            Path<Date> date_add = shoppingOrder.get("date_add");
            
            List<Predicate> predicates = new ArrayList<>();
            if (!Strings.isNullOrEmpty(form.taskId) && !"-1".equals(form.taskId)) {
            	if(Numbers.parseInt(form.taskId, -1)==-2){
            		predicates.add(builder.notEqual(taskId, 0));
            	}else{
            		predicates.add(builder.equal(taskId, form.taskId));
            	}
            }
            if (!Strings.isNullOrEmpty(form.payMethod) && !"-1".equals(form.payMethod)) {
            	predicates.add(builder.equal(payMethod, form.payMethod));
            }
            if (!Strings.isNullOrEmpty(form.ordertype) && !"-1".equals(form.ordertype) ) {
            	predicates.add(builder.equal(ordertype, form.ordertype));
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
     * 商品查询内部类
     * @author luobotao
     * Date: 2015年4月17日 上午10:04:26
     */
    private static class ProductQuery implements Specification<Product> {

        private final ProductManageForm form;

        public ProductQuery(final ProductManageForm form) {
            this.form = form;
        }

        @Override
        public Predicate toPredicate(Root<Product> product, CriteriaQuery<?> query,
                                     CriteriaBuilder builder) {
            List<Predicate> predicates = new ArrayList<>();
            Path<Long> pid = product.get("pid");
            Path<Long> ppid = product.get("ppid");
            Path<String> newSku = product.get("newSku");
            Path<String> extcode = product.get("extcode");
            Path<String> type = product.get("typ");
            Path<Integer> category = product.get("category");
            Path<String> title = product.get("title");
            Path<Integer> fromsite = product.get("fromsite");
            Path<Double> rmbprice = product.get("rmbprice");
            Path<Double> status = product.get("status");
            Path<Integer> ishot = product.get("ishot");
            Path<Integer> isEndorsement = product.get("isEndorsement");//是否是代言商品列表
            if(!Strings.isNullOrEmpty(form.pidOrNewSku)){
            	Long tempPid = Numbers.parseLong(form.pidOrNewSku, 0L);
            	if(tempPid!=0L){
	            	Predicate p1 = builder.equal(pid, form.pidOrNewSku);
	            	Predicate p2 = builder.equal(newSku, form.pidOrNewSku);
	            	Predicate p3 = builder.equal(extcode, form.pidOrNewSku);
	            	predicates.add(builder.or(p1,p2,p3));
            	}else{
            		Predicate p1 = builder.equal(newSku, form.pidOrNewSku);
            		Predicate p2 = builder.equal(extcode, form.pidOrNewSku);
            		predicates.add(builder.or(p1,p2));
            	}
            }
            if(form.adminUserId!=null && form.adminUserId.longValue()!=-1){
            	Subquery<Long> sq = query.subquery(Long.class);
				Root<AdminProduct>  subroot=sq.from(AdminProduct.class);
				Predicate pidPredicate = builder.equal(subroot.get("adminid").as(Long.class), form.adminUserId);
				sq.where(pidPredicate);
				sq.select(subroot.get("pid").as(Long.class));
				Predicate in= builder.in(pid).value(sq);
				predicates.add(in);
            }
            if(!StringUtils.isBlank(form.keyWords)){
            	 predicates.add(builder.like(title, "%"+form.keyWords + "%"));
            }
            if(form.fromsite!=-1 ){
            	predicates.add(builder.equal(fromsite, form.fromsite));
            }
            if(form.priceSmall!=null){
            	predicates.add(builder.greaterThanOrEqualTo(rmbprice, form.priceSmall));
            }
            if(form.priceBig!=null){
            	predicates.add(builder.lessThanOrEqualTo(rmbprice, form.priceBig));
            }
            if(form.status!=0 && form.status!=-1){
            	predicates.add(builder.equal(status, form.status));
            }
            if(form.type!=-1&&form.type!=3){	
            	predicates.add(builder.equal(type, form.type));
            }
		    //联营商品查询
            if(form.type==3){
            	 if(form.adminid==-1){
                 	//全部联营商户
            		 Subquery<Long> sq = query.subquery(Long.class);
     				 Root<AdminUser>  subroot=sq.from(AdminUser.class);
     				 Predicate apidPredicate = builder.equal(subroot.get("adminType").as(String.class), 2);
     				 sq.where(apidPredicate);
     				 sq.select(subroot.get("id").as(Long.class));
     				
     				 Subquery<Long> sqap = query.subquery(Long.class);
     				 Root<AdminProduct>  subap=sqap.from(AdminProduct.class);
     				 Predicate pidPredicate = builder.in(subap.get("adminid").as(Long.class)).value(sq);
     				 sqap.where(pidPredicate);
     				 sqap.select(subap.get("pid").as(Long.class));
     				 Predicate insp= builder.in(pid).value(sqap);
     				 predicates.add(insp);
                 }else{
                	 //指定的联营商户
                	 Subquery<Long> sq = query.subquery(Long.class);
                	 Root<AdminProduct>  subroot=sq.from(AdminProduct.class);
      				 Predicate pidPredicate = builder.equal(subroot.get("adminid").as(Long.class), form.adminid);
      				 sq.where(pidPredicate);
      				 sq.select(subroot.get("pid").as(Long.class));
      				 Predicate in= builder.in(pid).value(sq);
      				 predicates.add(in);
                 }
            }
            if(form.category!=-1){
            	predicates.add(builder.equal(category, form.category));
            }
            if(form.isEndorsement!=-1 ){
            	predicates.add(builder.equal(isEndorsement, form.isEndorsement));
            	if(form.isEndorsement==1){//是代言商品，只搜大哥
            		predicates.add(builder.equal(pid,ppid));
            	}
            }
            predicates.add(builder.equal(ishot, form.ishot));
            predicates.add(builder.notEqual(status, "40"));//等候彻底删除的不再展示
            
            Predicate[] param = new Predicate[predicates.size()];
            
            predicates.toArray(param);
            return query.where(param).getRestriction();
        }
    }
    /**
     * 订单商品查询内部类
     * @author luobotao
     * Date: 2015年4月17日 上午10:04:26
     */
    private static class OrderProductQuery implements Specification<OrderProduct> {
    	
    	private final FreshTaskQueryForm form;
    	
    	public OrderProductQuery(final FreshTaskQueryForm form) {
    		this.form = form;
    	}
    	
    	@Override
    	public Predicate toPredicate(Root<OrderProduct> orderProduct, CriteriaQuery<?> query,
    			CriteriaBuilder builder) {
    		List<Predicate> predicates = new ArrayList<>();
    		Path<Long> orderId = orderProduct.get("orderId");
    		if(form.orderIds!=null ){
    			if(form.orderIds.size()>0){
    				predicates.add(orderId.in(form.orderIds));
    			}else{
    				predicates.add(orderId.in(0));
    			}
    		}
    		
    		Predicate[] param = new Predicate[predicates.size()];
    		
    		
    		predicates.toArray(param);
    		return query.where(param).getRestriction();
    	}
    }
    
    /**
     * 包裹查询内部类
     * @author luobotao
     * Date: 2015年4月17日 上午10:04:26
     */
    private static class ParcelsQuery implements Specification<Parcels> {

        private final FreshTaskQueryForm form;

        public ParcelsQuery(final FreshTaskQueryForm form) {
            this.form = form;
        }

        @Override
        public Predicate toPredicate(Root<Parcels> parcels, CriteriaQuery<?> query,
                                     CriteriaBuilder builder) {
            Path<String> pardelStatus = parcels.get("status");
            Path<String> parcelCode = parcels.get("parcelCode");
            Path<String> OrderCode = parcels.get("OrderCode");
            Path<Date> date_add = parcels.get("date_add");
            Path<Long> orderId = parcels.get("orderId");
            
            
            
            List<Predicate> predicates = new ArrayList<>();
            if (!Strings.isNullOrEmpty(form.parcelStatus) && !"-1".equals(form.parcelStatus)) {
                predicates.add(builder.equal(pardelStatus, form.parcelStatus));
            }
            if (!Strings.isNullOrEmpty(form.parcelCode) ) {
            	Predicate parcelCodePredicate = builder.equal(parcelCode, form.parcelCode);
            	Predicate OrderCodePredicate = builder.equal(OrderCode, form.parcelCode);
            	predicates.add(builder.or(parcelCodePredicate,OrderCodePredicate));
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
            if(form.orderIds!=null ){
            	if(form.orderIds.size()>0){
            		predicates.add(orderId.in(form.orderIds));
            	}else{
            		predicates.add(orderId.in(0));
            	}
            }
            
            Predicate[] param = new Predicate[predicates.size()];
            predicates.toArray(param);
            
            return query.where(param).getRestriction();
        }
    }

	/**
	 * 解析批量增加库存模版文件
	 * @param stockExcel
	 * @return
	 */
	public List<ImportAddStockVO> extractAddStockInfo(File stockExcel) {
		List<ImportAddStockVO> result = new LinkedList<>();
		// 解析文件
		Workbook workBook = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(stockExcel);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("没有找到对应的文件", e);
		}
		try {
			workBook = WorkbookFactory.create(fis);
			Sheet sheet = workBook.getSheetAt(0);
			int lastRowNumber = sheet.getLastRowNum();
			Row rowTitle = sheet.getRow(0);
			if (rowTitle == null) {
				result = null;
				return result;
			}
			// 从第1行开始（不算标题）
			for (int i = 1; i <= lastRowNumber; i++) {
				Row row = sheet.getRow(i);
				if (row == null) {
	                continue;
	            }
				ImportAddStockVO vo = convert(row);
				if(vo==null){
					return result;
				}
				result.add(vo);
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			result = null;
		}
        return result;
	}
	
	private ImportAddStockVO convert(Row row) {
		ImportAddStockVO vo = new ImportAddStockVO();
        String pid = row.getCell(0, Row.RETURN_NULL_AND_BLANK)==null?"":row.getCell(0, Row.RETURN_NULL_AND_BLANK).toString();//pid
        if(StringUtils.isBlank(pid)){
        	return null;
        }
        String stock = row.getCell(1, Row.RETURN_NULL_AND_BLANK)==null?"":row.getCell(1, Row.RETURN_NULL_AND_BLANK).toString();//实际入库数量
        vo.pid = Numbers.parseLong(pid, 0L);//pid
        vo.stock = Numbers.parseInt(stock, 0);//实际入库数量
        return vo;
    }

	/**
	 * 
	 * <p>Title: getProductSellTopInfo</p> 
	 * <p>Description: 根据条件获取到商品销量排行</p> 
	 * @param start
	 * @param end
	 * @param greater
	 * @param proInfo
	 * @param typ
	 * @param countTyp
	 * @return
	 */
	public List<ProductSellTopVO> getProductSellTopInfo(String start,
			String end, Integer count, String proInfo, Integer typ,
			Integer countTyp,long adminId) {
		if(count==null){
			count=0;
		}
		//针对联营商户，将商品类型改为2（自营）
		String pretitle = "";
		if(typ==3){
			typ = 2;
			pretitle = "联营";
		}
		List<ProductSellTopVO> result = new ArrayList<ProductSellTopVO>();
		String sql = "{call get_prdsales_stat ('"+start+"','"+end+"','"+proInfo+"',"+count+","+typ+","+countTyp+","+adminId+")}";// SQL语句// //调用存储过程
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		AdminUser adminUser = adminUserService.getAdminUser(adminId);
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				ProductSellTopVO productSellTopVO = new ProductSellTopVO();
				productSellTopVO.pid=rs.getInt(1);
				productSellTopVO.counts=rs.getInt(2);
				productSellTopVO.productName=rs.getString(4);
				Fromsite fromsiteV = queryFromsiteById(rs.getInt(5));
				productSellTopVO.fromsite=fromsiteV==null?"":fromsiteV.getName();
				if(!Strings.isNullOrEmpty(pretitle)){
					if(adminUser!=null){
						productSellTopVO.typ=pretitle+"-"+adminUser.getRealname();
					}else{
						productSellTopVO.typ=pretitle+"-全部";
					}
				}else{
					productSellTopVO.typ=Constants.Typs.typs2Message(rs.getInt(6));
				}
				productSellTopVO.nstock=rs.getInt(7);
				if(rs.getInt(7)<=-99){
					long nstocks = dealNstockWithProduct(rs.getLong(1));
					productSellTopVO.nstock=nstocks;
				}
				result.add(productSellTopVO);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	/**
	 * 
	 * <p>Title: dealNstockWithProduct</p> 
	 * <p>Description: 获取到指定组合商品的商品库存</p> 
	 * @param product
	 * @return
	 */
	public long dealNstockWithProduct(Long pgid) {
		List<ProductGroup> productGroups = productGroupService.findProductGroupListByPgId(pgid);
		long nstock = 9999L;
		for (ProductGroup productGroup : productGroups) {
			Product product = findProductById(productGroup.getPid());
			if(product.getNstock()==0L||product.getNstock()<productGroup.getNum()){
				nstock = 0L;
				break;
			}
			long tempNstock = product.getNstock() / productGroup.getNum();
			if(tempNstock < nstock&&tempNstock>0){
				nstock = tempNstock;
			}
		}
		return nstock==9999L?0:nstock;
	}
	
	/**
	 * 
	 * <p>Title: getProductVistTopInfo</p> 
	 * <p>Description: 获取商品的访购信息</p> 
	 * @param start
	 * @param end
	 * @param typ
	 * @param fromsite
	 * @param proInfo
	 * @return
	 */
	public List<ProductSellTopVO> getProductVistTopInfo(String start, String end,
			Integer typ, Integer fromsite, String proInfo) {
		List<ProductSellTopVO> result = new ArrayList<ProductSellTopVO>();
		String sql = "{call get_prdsales_stat2 ('"+start+"','"+end+"','"+proInfo+"',"+typ+","+fromsite+")}";// SQL语句// //调用存储过程
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				ProductSellTopVO productSellTopVO = new ProductSellTopVO();
				productSellTopVO.pid=rs.getInt(1);
				productSellTopVO.productName=rs.getString(2);
				productSellTopVO.typ=Constants.Typs.typs2Message(rs.getInt(3));
				Fromsite fromsiteV = queryFromsiteById(rs.getInt(4));
				productSellTopVO.fromsite=fromsiteV==null?"":fromsiteV.getName();
				productSellTopVO.orderUserCnt=rs.getInt(5);
				productSellTopVO.orderUserPayCnt=rs.getInt(6);
				productSellTopVO.orderUserRate=Numbers.formatDouble(rs.getInt(6)*100.0/rs.getInt(5),"#0.00");
				productSellTopVO.orderCnt=rs.getInt(7);
				productSellTopVO.orderPayCnt=rs.getInt(8);
				result.add(productSellTopVO);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}


	/**
	 * 
	 * <p>Title: queryProductList</p> 
	 * <p>Description: 根据指定条件查询符合条件的商品
	 * 		用于订单管理，通过来源网站和关键字查询商品
	 * </p> 
	 * @param formPage
	 * @return
	 */
	public List<Product> queryProductList(OrderForm formPage) {
		return this.productRepository.findAll(new ProductQueryWithForm(formPage));
	}

	/**
     * 订单查询内部类
     * @author luobotao
     * Date: 2015年4月17日 上午10:04:26
     */
    private static class ProductQueryWithForm implements Specification<Product> {

        private final OrderForm form;

        public ProductQueryWithForm(final OrderForm form) {
            this.form = form;
        }

        @Override
        public Predicate toPredicate(Root<Product> product, CriteriaQuery<?> query,
                                     CriteriaBuilder builder) {
            Path<String> fromsite = product.get("fromsite");			//来源网站
            Path<String> title = product.get("title");	//商品名称
            
            List<Predicate> predicates = new ArrayList<>();
            if (!Strings.isNullOrEmpty(form.fromsite) && !"-1".equals(form.fromsite)) {
            	predicates.add(builder.equal(fromsite, form.fromsite));
            }
            /*if (!Strings.isNullOrEmpty(form.title)) {
            	predicates.add(builder.like(title, form.title));
            }*/
            
            Predicate[] param = new Predicate[predicates.size()];
            predicates.toArray(param);
            
            return query.where(param).getRestriction();
        }
    }

	/**
	 * 获取该商品下的商品详情
	 * @param pid
	 * @return
	 */
	public List<ProductDetail> findDetailByPid(Long pid) {
		return productDetailRepository.findByPid(pid,new Sort(Direction.DESC, "nsort"));
	}
	/**
	 * 获取该商品详情下的参数列表
	 * @param pid
	 * @return
	 */
	public List<ProductDetailPram> findDetailParamsByPdid(Long pdid) {
		return productDetailParamRepository.findByPdid(pdid,new Sort(Direction.DESC, "nsort"));
	}
	


	
	/**
	 * 根据商品ID获取商品图片
	 * @param pid
	 * @return
	 */
	public List<ProductImages> findProductImagesByPid(Long pid) {
		return productImagesRepository.findByPid(pid);
	}
	/**
	 * 保存商品图片
	 * @param productDetail
	 * @return
	 */
	@Transactional
	public ProductImages saveProductImages(ProductImages productImages) {
		return productImagesRepository.save(productImages);
	}
	/**
	 * 删除商品图片
	 * @param imageid
	 */
	public void deleteProductImageById(Long imageid) {
		productImagesRepository.delete(imageid);
		
	}
	/**
	 * 根据商品详情ID获取该详情
	 * @param detailId
	 * @return
	 */
	@Transactional
	public ProductDetail findProductDetailById(Long detailId) {
		return productDetailRepository.findOne(detailId);
	}
	/**
	 * 保存商品详情
	 * @param productDetail
	 * @return
	 */
	@Transactional
	public ProductDetail saveDetail(ProductDetail productDetail) {
		return productDetailRepository.save(productDetail);
	}

	/**
	 * 根据ppid获取同规格下的所有商品
	 * @param ppid
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<Product> findProductListByPpId(Long ppid) {
		return this.productRepository.findByPpid(ppid);
	}
	/**
	 * 删除商品详情同时还需要将此详情ID对应的详情参数进行删除
	 * @param detailId
	 */
	@Transactional
	public void deleteProductDetail(Long detailId) {
		productDetailRepository.delete(detailId);
		productDetailParamRepository.deleteByPdid(detailId);
	}
	/**
	 * 将某一商品从该规格中删除
	 * @param pid
	 * @return
	 */
	@Transactional
	public int deleteSpec(Long pid) {
		int result = productRepository.deleteSpec(pid);
		Product product = productRepository.findOne(pid);
		ServiceFactory.getCacheService().setObject(Constants.product_KEY+pid, product,0 );//写入缓存
		return result;
	}

	/**
	 * 保存商品详情参数
	 * @param productDetailPram
	 * @return
	 */
	public ProductDetailPram saveProductDetailPram(
			ProductDetailPram productDetailPram) {
		return productDetailParamRepository.save(productDetailPram);
	}
	@Transactional
	public void deleteDetailParamById(Long paramId) {
		productDetailParamRepository.delete(paramId);
	}

	/**
	 * 将pid为@pid的商品的ppid设置成@ppid
	 * @param pid
	 * @param ppid
	 * @return
	 */
	@Transactional
	public int updateProductPpid(Long pid, Long ppid) {
		int result = productRepository.updateProductPpid(pid,ppid);
		Product product = productRepository.findOne(pid);
		ServiceFactory.getCacheService().setObject(Constants.product_KEY+pid, product,0 );//写入缓存
		return result;
	}

	/**
	 * 根据pid与title查询product
	 * @param pidForSearch
	 * @param pNameForSearch
	 * @return
	 */
	public List<Product> queryProduct(final ProductManageForm productManageForm) {
		return productRepository.findAll(new Specification<Product>() {

			@Override
			public Predicate toPredicate(Root<Product> product,
					CriteriaQuery<?> query, CriteriaBuilder builder) {
				Path<Long> pid = product.get("pid");
				Path<String> newSku = product.get("newSku");
				Path<Integer> isEndorsement = product.get("isEndorsement");
				Path<String> typ = product.get("typ");
				Path<String> title = product.get("title");
				Path<String> status = product.get("status");
				List<Predicate> predicates = new ArrayList<>();
				if (!Strings.isNullOrEmpty(productManageForm.pidOrNewSku)) {
					Object[] pidOrNewSkus = productManageForm.pidOrNewSku.split(",");
					boolean flag = true;
					for (Object pidOrNewsku : pidOrNewSkus) {
						Long tempPid = Numbers.parseLong(String.valueOf(pidOrNewsku), 0L);
						if(tempPid==0L){
							flag = false;
							break;
						}
					}
	            	if(flag){
		            	Predicate p1 = pid.in(pidOrNewSkus);
		            	Predicate p2 = newSku.in(pidOrNewSkus);
		            	predicates.add(builder.or(p1,p2));
	            	}else{
	            		Predicate p = newSku.in(pidOrNewSkus);
	            		predicates.add(p);
	            	}
				}
				if (!Strings.isNullOrEmpty(productManageForm.pids)) {
					String pidsArray[] = productManageForm.pids.split(",");
					List<Long> pidList = Lists.newArrayList();
					for(String pidTemp : pidsArray){
						if(Numbers.parseLong(pidTemp, 0L)>0){
							pidList.add(Numbers.parseLong(pidTemp, 0L));
						}
					}
					predicates.add(pid.in(pidList));
				}
				if (!Strings.isNullOrEmpty(productManageForm.newSkus)) {
					String newSkuArray[] = productManageForm.newSkus.split(",");
					List<String> newSkuList = Lists.newArrayList();
					for(String newSkuTemp : newSkuArray){
						if(!StringUtils.isBlank(newSkuTemp)){
							newSkuList.add(newSkuTemp);
						}
					}
					predicates.add(newSku.in(newSkuList));
				}
				if (!StringUtils.isBlank(productManageForm.keyWords)) {
					predicates.add(builder.like(title, "%" + productManageForm.keyWords + "%"));
				}
				if (productManageForm.isEndorsement!=-1) {
					predicates.add(builder.equal(isEndorsement, productManageForm.isEndorsement));
				}
				if (productManageForm.type!=-1&&productManageForm.type!=3) {
					predicates.add(builder.equal(typ, productManageForm.type));
				}
				if (productManageForm.ishot==1){		//组合商品增加只能对线上商品的查找
					//predicates.add(builder.equal(status, "10"));
				}
				//是否来自商城的查询，如果是则默认要求商品在线
				if (productManageForm.isfromshop){
					predicates.add(builder.equal(status, "10"));
				}
				if(productManageForm.adminUserId!=null && productManageForm.adminUserId.longValue()!=-1){//根据联营管理员的ID创建该管理员创建的商品
	            	Subquery<Long> sq = query.subquery(Long.class);
					Root<AdminProduct>  subroot=sq.from(AdminProduct.class);
					Predicate pidPredicate = builder.equal(subroot.get("adminid").as(Long.class), productManageForm.adminUserId);
					sq.where(pidPredicate);
					sq.select(subroot.get("pid").as(Long.class));
					Predicate in= builder.in(pid).value(sq);
					predicates.add(in);
	            }
				if(productManageForm.containsUnion==false){//不展示联营商品
					Subquery<Long> sq = query.subquery(Long.class);
					Root<AdminProduct>  subroot=sq.from(AdminProduct.class);
					sq.select(subroot.get("pid").as(Long.class));
					Predicate notin= builder.in(pid).value(sq).not();
					predicates.add(notin);
				}
				Predicate[] param = new Predicate[predicates.size()];

				predicates.toArray(param);
				return query.where(param).getRestriction();
			}

		});
	}

	/**
	 * 根据pid删除商品
	 * @param pid
	 */
	@Transactional
	public void deleteProductById(Long pid) {
		productRepository.delete(pid);
	}

	/**
	 * 根据pid获取这个商品在数据库中存在的listPic
	 * @param asLong
	 * @return
	 */
	public String getDBListPic(long pid) {
		return productRepository.findListPicByPid(pid);
	}

	/**
	 * 修改商品状态
	 * @param pid
	 * @param status
	 * @return
	 */
	@Transactional
	public int updateProductState(Long pid, int status) {
		return productRepository.updateProductState(pid,status);
	}
	/**
	 * 修改商品代言状态
	 * @param pid
	 * @param status
	 * @return
	 */
	@Transactional
	public int updateProductEndorsement(Long pid, int status) {
		return productRepository.updateProductEndorsement(pid,status);
	}
	/**
     * 生成html中需要的币种select
     * @param categoryList
     * @param id
     * @return
     */
    public static String currencyList2Html(List<Currency> currencyList,Integer id){
		StringBuilder sb = new StringBuilder();
		for (Currency c : currencyList) {
			BigDecimal b1 = new BigDecimal(Double.toString(c.getRate()));
			BigDecimal b2 = new BigDecimal(Double.toString(0.01));
			Double rate = b1.multiply(b2).doubleValue();
			if (id != null && id.equals(c.getId())) {
				sb.append(Htmls.generateSelectedOptionName(c.getId(), c.getRate(), c.getName()+ rate));
			} else {
				sb.append(Htmls.generateOptionName(c.getId(),c.getRate(), c.getName() + rate));
			}
		}
		return sb.toString();
    }

	/**
	 * 获取newSku
	 * @param category
	 * @return
	 */
	public String getNewSkuByCategory(int category) {
		String result="";
		String sql = "SELECT LEFT(CONCAT(c.typecode,'000000'),6), CONCAT(LEFT(p.newSku,6),RIGHT(CONCAT('0000',RIGHT(MAX(p.newSku),4)+1),4)) FROM product p,(SELECT typecode FROM category_new WHERE id = "+category+") c WHERE p.newSku LIKE CONCAT(LEFT(CONCAT(c.typecode,'000000'),6),'%')";// SQL语句
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				String resultTemp = rs.getString(1);
				result = rs.getString(2);
				if(StringUtils.isBlank(result)){
					result = resultTemp+"0001";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	/**
	 * 保存管理员与商品的中间表
	 * @param adminProduct
	 * @return
	 */
	public AdminProduct saveAdminProduct(AdminProduct adminProduct) {
		return adminProductRepository.save(adminProduct);
	}
	/**
	 * 保存管理员与商品的中间表
	 * @param adminProduct
	 * @return
	 */
	public List<AdminProduct> findAdminProductByPidAndAdminid(Long pid,Long adminid) {
		return adminProductRepository.findByPidAndAdminid(pid,adminid);
	}

	/**
	 * 根据newSku查询商品
	 * @param newSku
	 * @return
	 */
	public Product findproductByNewSku(String newSku) {
		return productRepository.findByNewSku(newSku);
	}

	/**
	 * 
	 * <p>Title: findProductByExtcode</p> 
	 * <p>Description: 根据extcode获取懂啊商品信息</p> 
	 * @param extcode
	 * @return
	 */
	public Product findProductByExtcode(String extcode) {
		return  productRepository.findProductByExtcode(extcode);
	}

	/**
	 * 根据商品ID获取该商品在分表中的信息
	 * @param pid
	 * @return
	 */
	public ProductUnion findProductUnionByPid(Long pid) {
		ProductUnion result = (ProductUnion) ServiceFactory.getCacheService().getObject(Constants.productUnion_Pid_KEY+pid );//从缓存读入
    	if(result==null){
    		result = productUnionRepository.findByPid(pid);
    		if(result == null){
    			return null;
    		}else{
    	    	ServiceFactory.getCacheService().setObject(Constants.productUnion_Pid_KEY+pid, result, 0);//将商品分表写入cache
    		}
    	}
		return result;
	}

	/**
	 * 保存商品分表
	 * @param productUnion
	 * @return
	 */
	@Transactional
	public ProductUnion saveProductUnion(ProductUnion productUnion) {
		productUnion = productUnionRepository.save(productUnion);
		ServiceFactory.getCacheService().setObject(Constants.productUnion_Pid_KEY+productUnion.getPid(), productUnion, 0);//将商品分表写入cache
		return productUnion;
	}

	/**
	 * 批量上架商品
	 * @param ids
	 * @param status
	 */
	@Transactional
	public void updateProductStateBatch(List<Long> ids, int status) {
		productRepository.updateProductStateBatch(ids,status,new Date());
		
	}

	/**
	 * 
	 * <p>Title: doJobWithStage</p> 
	 * <p>Description: 根据预售商品不同的状态执行相应的调整</p> 
	 * @param parseInt
	 */
	public void doJobWithStage(int stage,long pid) {
		String sql = "";
		if(2 == stage){
			sql = "UPDATE shopping_Order s INNER JOIN shopping_Order_Pro sp ON s.id = sp.orderId SET s.stage='2' WHERE s.status='21' AND sp.pid="+pid;
		}else if(3 == stage){
			sql = "UPDATE shopping_Order s INNER JOIN shopping_Order_Pro sp ON s.id = sp.orderId SET s.status='22',s.stage='3' WHERE s.status='21' AND sp.pid="+pid;
		}else if(4 == stage){
			sql = "UPDATE shopping_Order s INNER JOIN shopping_Order_Pro sp ON s.id = sp.orderId SET s.status='5' WHERE s.status='0' AND sp.pid="+pid;
		}
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			db.pst.executeUpdate();// 执行语句，得到结果集
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		if(1!=stage){
			sql = "UPDATE shopping_Order s INNER JOIN shopping_Order_Pro sp ON s.id = sp.orderId SET s.status='5' WHERE s.ordertype='3' and s.status='0' AND sp.pid="+pid;
		}
		JdbcOper db1 = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db1.getPrepareStateDao(sql);
			db1.pst.executeUpdate();// 执行语句，得到结果集
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db1.close();
		}
	}
	
	/**
	 * 
	 * <p>Title: getfreight</p> 
	 * <p>Description: 获取指定来源的运费，根据来源和重量计算</p> 
	 * @param fromsiteid
	 * @param weight
	 * @return
	 */
	public static Double getfreight(int fromsiteid, String weight) {
		Double freight = 0D;
		String sql = "select fee,addfee from fromsite where id=" + fromsiteid;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		db.getPrepareStateDao(sql);// 执行语句，得到结果集
		ResultSet rs;
		try {
			rs = db.pst.executeQuery();
			while (rs.next()) {
				int fee = rs.getInt("fee");
				int addfee = rs.getInt("addfee");
				Double weightTemp = 0.0;
				if (Numbers.parseDouble(weight, 0.0) > 0.5) {
					weightTemp = Numbers.parseDouble(weight, 0.0) - 0.5;
				}
				freight = fee + addfee * weightTemp / 0.1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return freight;
	}
}
