package services.product;


import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;

import models.product.ProductGroup;
import play.Logger;
import repositories.product.ProductGroupRepository;

/**
 * 
 * <p>Title: ProductGroupService.java</p> 
 * <p>Description: 组合商品相关Service</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年9月8日  下午5:51:12
 * @version
 */
@Named
@Singleton
public class ProductGroupService {

    private static final Logger.ALogger logger = Logger.of(ProductGroupService.class);

    @Inject
    private ProductGroupRepository productGroupRepository;

    @Transactional
	public ProductGroup save(ProductGroup productGroup) {
		return productGroupRepository.save(productGroup);
	}

	public ProductGroup queryProductGroup(Long pgid, Long pid) {
		return productGroupRepository.queryProductGroup(pgid,pid);
	}

	public List<ProductGroup> findProductGroupListByPgId(Long pgid) {
		 return productGroupRepository.findProductGroupListByPgId(pgid);
	}
	@Transactional
	public void deleteAllWithPgid(Long pgid) {
		productGroupRepository.deleteByPgid(pgid);
	}

	public List<ProductGroup> getProductGroupByPid(Long pid) {
		return productGroupRepository.getProductGroupByPid(pid);
	}

    
}
