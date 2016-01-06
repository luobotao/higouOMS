package services.parcels;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;

import models.Account;
import models.Creditcard;
import models.OrderProduct;
import models.Parcels;
import models.ShoppingOrder;
import play.Logger;
import repositories.AccountRepository;
import repositories.CreditcardRepository;
import repositories.parcels.ParcelsRepository;
import services.ServiceFactory;
import utils.Constants;
import utils.Dates;
import utils.JdbcOper;
import forms.order.OrderForm;
import forms.parcels.ParcelsForm;

/**
 * 包裹相关Service
 * @author luobotao
 * Date: 2015年4月17日 下午2:26:14
 */
@Named
@Singleton
public class ParcelsService {

    private static final Logger.ALogger logger = Logger.of(ParcelsService.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Inject
    private ParcelsRepository parcelsRepository;
    @Inject
    private CreditcardRepository creditcardRepository;
    @Inject
    private AccountRepository accountRepository;
    
    public Parcels queryParcelsById(Long id) {
    	Parcels parcels = (Parcels) ServiceFactory.getCacheService().getObject(Constants.parcels_KEY+id );//从缓存读入
    	if(parcels==null){
    		parcels =parcelsRepository.findOne(id);
    		ServiceFactory.getCacheService().setObject(Constants.parcels_KEY+id, parcels,0 );//写入缓存
    	}
        return parcels;
    } 
    
    @Transactional
	public Parcels saveParcels(Parcels parcels) {
		parcels = parcelsRepository.save(parcels);
		ServiceFactory.getCacheService().setObject(Constants.parcels_KEY+parcels.getId(), parcels, 0);
		return parcels;
	}

	/**
	 * 获取到当前时间段内所有的包裹的商品newsku
	 * <p>Title: findNewSkusWithDateAdd</p> 
	 * <p>Description: </p> 
	 * @param lasttime
	 * @return
	 */
	public List<String> findNewSkusWithDateAdd(Date lasttime) {
		String sql="SELECT DISTINCT pd.newSku as newSku FROM pardels p,pardels_Pro pp,product pd WHERE p.id = pp.pardelsId AND pp.pid = pd.pid AND pd.typ=2 and pd.newSku<>'' and p.date_add >= '"+lasttime+"'";
		logger.info(sql);
		List<String> newSkus = new ArrayList<String>();
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				newSkus.add(rs.getString("newSku"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return newSkus;
	}


	/**
	 * 
	 * <p>Title: saveParcelsWithForm</p> 
	 * <p>Description: 根据订单内容生成相应的包裹</p> 
	 * @param formPage
	 * @return
	 */
	@Transactional
	public Parcels saveParcelsWithForm(ShoppingOrder shoppingOrder, List<OrderProduct> orderProducts,
			OrderForm formPage) {
		Parcels parcels = new Parcels();
		parcels.setDate_add(new Date());
		parcels.setAdminId(new Long(shoppingOrder.getcId()));
		parcels.setParcelCode(reduceParcelCode());
		parcels.setSrc("1");
		parcels.setTotalFee(Double.parseDouble(formPage.totalFee));
		parcels.setGoodsFee(dealGoodsFee(orderProducts));
		parcels.setFreight(0.00);
		parcels.setName(formPage.name);
		parcels.setPhone(formPage.phone);
		parcels.setProvince(formPage.province);
		parcels.setAddress(formPage.address);
		parcels.setStatus(1);
		parcels.setOrderCode(shoppingOrder.getOrderCode());
		parcels.setOrderId(shoppingOrder.getId());
		parcels.setCardId(shoppingOrder.getCardId());
		parcels.setCreditcard(dealCreditCard(formPage.creditcard));
		parcels.setForeignorder(formPage.foreignOrder);
		parcels.setCurrency(formPage.currency);
		parcels.setAccount(dealAccount(formPage.account));
		parcels.setTraffic(formPage.traffic);
		parcels.setTraffic_mark(formPage.traffic_mark);
		parcels.setBbtid("");
		parcels.setMailnum("");
		parcels.setOrder_print((long) 0);
		parcels.setBbt_print((long) 0);
		parcels.setRemark("");
		parcels = parcelsRepository.save(parcels);
		ServiceFactory.getCacheService().setObject(Constants.parcels_KEY+parcels.getId(), parcels, 0);
		return parcels;
	}

	/**
	 * 
	 * <p>Title: dealAccount</p> 
	 * <p>Description: 获取下单账号信息</p> 
	 * @param account
	 * @return
	 */
	private String dealAccount(String account) {
		Account ac=  accountRepository.findOne(Integer.parseInt(account));
		if(ac!=null){
			return ac.getAccountNo();
		}
		return "";
	}


	/**
	 * 获取信用卡号信息
	 * <p>Title: dealCreditCard</p> 
	 * <p>Description: </p> 
	 * @param creditcard
	 * @return
	 */
	private String dealCreditCard(String creditcard) {
		Creditcard cc=  creditcardRepository.findOne(Integer.parseInt(creditcard));
		if(cc!=null){
			return cc.getCreditcardID();
		}
		return "";
	}


	/**
	 * 
	 * <p>Title: reduceParcelCode</p> 
	 * <p>Description: 生成包裹号</p> 
	 * @return
	 */
	private String reduceParcelCode() {
		//String sql = "SELECT CONCAT(DATE_FORMAT(NOW(),'%Y%m%d'),RIGHT(CONCAT('00000',MAX(RIGHT(pardelcode,5))+1),5)) FROM pardels";
		String sql = "SELECT CONCAT(DATE_FORMAT(NOW(),'%Y%m%d'),RIGHT(CONCAT('000000',MAX(RIGHT(pardelcode,6))+1),6)) FROM pardels WHERE DATE_FORMAT(DATE_ADD,'%Y%m%d')=DATE_FORMAT(NOW(),'%Y%m%d')";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		String pardelCode = "";
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				pardelCode = (String) rs.getString(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return pardelCode;
	}


	/**
	 * 
	 * <p>Title: dealGoodsFee</p> 
	 * <p>Description: 获取包裹下所有商品的价格总和</p> 
	 * @param orderProducts
	 * @return
	 */
	private Double dealGoodsFee(List<OrderProduct> orderProducts) {
		double bb = 0.00;
		for (OrderProduct orderProduct : orderProducts) {
			bb += orderProduct.getTotalFee();
		}
		return bb;
	}


	public void del(Parcels parcels) {
		parcelsRepository.delete(parcels);
	}

	/**
	 * 
	 * <p>Title: queryParcelsByOrderId</p> 
	 * <p>Description: 根据订单ID和商品类型，获得包裹信息</p> 
	 * @param parseLong
	 * @param typ
	 * @return
	 */
	public List<Parcels> queryParcelsByOrderId(long soId, String typ) {
		if("0".equals(typ)){
			return parcelsRepository.queryParcelsByOrderId(soId);
		}
		return parcelsRepository.queryParcelsByOrderId(soId, typ);
	}

	/**
	 * 
	 * <p>Title: findParcelsWithDateAdd</p> 
	 * <p>Description: 查询相应时间段后生成的包裹信息</p> 
	 * @param lasttime
	 * @return
	 */
	public List<Parcels> findParcelsWithDateAdd(Date lasttime) {
		return parcelsRepository.findParcelsWithDateAdd(lasttime);
	}

	/**
	 * 
	 * <p>Title: findOrderIdsWithChangedDate</p> 
	 * <p>Description: 获得指定时间范围内的跨境通订单集合</p> 
	 * @param changedDate
	 * @return
	 */
	public List<Parcels> findOrderIdsWithChangedDate(Date startDate) {
		return parcelsRepository.findOrderIdsWithChangedDate(startDate);
	}

	/**
	 * 
	 * <p>Title: queryParcelsByBbtId</p> 
	 * <p>Description: </p> 
	 * @param soId
	 */
	public Parcels queryParcelsByBbtId(long bbtId) {
		return parcelsRepository.queryParcelsByBbtId(bbtId);
	}

	/**
	 * 
	 * <p>Title: queryParcelsByParcelCode</p> 
	 * <p>Description: 根据包裹号查出相关的包裹信息</p> 
	 * @param merchantOrderID
	 * @return
	 */
	public Parcels queryParcelsByParcelCode(String parcelCode) {
		return parcelsRepository.queryParcelsByParcelCode(parcelCode);
	}

	/**
	 * 
	 * <p>Title: findOrderIdsWithDfh</p> 
	 * <p>Description: 获取到待发货的跨境通订单集合</p> 
	 * @return
	 */
	public List<Parcels> findOrderIdsWithDfh(Long adminId) {
		return parcelsRepository.findOrderIdsWithDfh(adminId);
	}

	public List<Parcels> findParcelsWithAdminAndDateAdd(Long adminId, Date lasttime) {
		return parcelsRepository.findParcelsWithAdminAndDateAdd(adminId,lasttime);
	}

}
