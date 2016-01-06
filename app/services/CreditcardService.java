package services;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.Creditcard;
import play.Logger;
import repositories.CreditcardRepository;

/**
 * 
 * <p>Title: CreditcardService.java</p> 
 * <p>Description: 信用卡账号管理</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年7月30日  上午11:34:42
 * @version
 */
@Named
@Singleton
public class CreditcardService {

    private static final Logger.ALogger logger = Logger.of(CreditcardService.class);

    @Inject
    private CreditcardRepository creditcardRepository;

	public List<Creditcard> findAll() {
		return creditcardRepository.findAll();
	}

   

}
