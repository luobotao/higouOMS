package services.product;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.product.Currency;
import play.Logger;
import repositories.product.CurrencyRepository;
import services.ServiceFactory;
import utils.Constants;
import utils.Numbers;

/**
 * 
 * <p>Title: CurrencyService.java</p> 
 * <p>Description: 货比汇率管理</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年7月30日  上午11:35:36
 * @version
 */
@Named
@Singleton
public class CurrencyService {

    private static final Logger.ALogger logger = Logger.of(CurrencyService.class);

    @Inject
    private CurrencyRepository currencyRepository;

	public List<Currency> findAll() {
		return currencyRepository.findAll();
	}

	public Currency queryCurrencyById(String idStr) {
		int id = Numbers.parseInt(idStr, 0);
		Currency currency = (Currency) ServiceFactory.getCacheService().getObject(Constants.currency_KEY+id );//从缓存读入
    	if(currency==null){
    		currency = currencyRepository.findOne(id);
    		if(currency == null){
    			return null;
    		}
    		ServiceFactory.getCacheService().setObject(Constants.currency_KEY+id, currency,0 );//写入缓存
    	}
        return currency;
	}

   

}
