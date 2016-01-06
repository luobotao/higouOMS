package services;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.Account;
import play.Logger;
import repositories.AccountRepository;

/**
 * 
 * <p>Title: AccountService.java</p> 
 * <p>Description: 账号管理</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年7月28日  下午6:33:44
 * @version
 */
@Named
@Singleton
public class AccountService {

    private static final Logger.ALogger logger = Logger.of(AccountService.class);

    @Inject
    private AccountRepository accountRepository;

	public List<Account> findAll() {
		return accountRepository.findAll();
	}

   

}
