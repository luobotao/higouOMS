package services;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.KdCompany;
import play.Logger;
import repositories.KdCompanyRepository;
import utils.Constants;

/**
 * 
 * <p>Title: KdCompanyService.java</p> 
 * <p>Description: 货运公司管理</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年8月3日  下午1:50:10
 * @version
 */
@Named
@Singleton
public class KdCompanyService {

    private static final Logger.ALogger logger = Logger.of(KdCompanyService.class);

    @Inject
    private KdCompanyRepository kdCompanyRepository;

	public List<KdCompany> findAll() {
		return kdCompanyRepository.findAll();
	}

	 /**
     * @param id
     * @return
     */
    public KdCompany getkdCompanyById(Integer id){
    	KdCompany kcdCompany = (KdCompany) ServiceFactory.getCacheService().getObject(Constants.kd_company_Key+id );//从缓存读入
    	if(kcdCompany==null){
    		kcdCompany = kdCompanyRepository.findOne(id);
    		ServiceFactory.getCacheService().setObject(Constants.kd_company_Key+id,kcdCompany,0 );//
    	}
    	return kcdCompany;
    }

}
