package repositories.product;

import java.util.Date;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import models.product.Product;

/**
 * 商品相关
 * @author luobotao
 * Date: 2015年4月17日 下午2:21:17
 */
@Named
@Singleton
public interface ProductRepository extends JpaRepository<Product, Long>,JpaSpecificationExecutor<Product> {

	@Query(value="SELECT * FROM product WHERE pid IN(SELECT pid FROM pardels_Pro WHERE pardelsId=?1)",nativeQuery=true)
	List<Product> queryProductListByParcelsId(Long parcelsId);
	
	@Query(value="SELECT counts FROM pardels_Pro WHERE pardelsId = ?1 AND pid =?2",nativeQuery=true)
	public int queryProductCounts(Long parcelsId, Long pid);
	
	@Modifying
	@Query(value="UPDATE product SET stock = ?1 WHERE pId =?2",nativeQuery=true)
	public int updateProductStock(int stock, Long pid);

	List<Product> findByPpid(Long ppid);

	@Modifying
	@Query(value="UPDATE Product SET ppid = ?1 WHERE pId =?1")
	public int deleteSpec(Long pid);
	@Modifying
	@Query(value="UPDATE Product SET ppid = ?2 WHERE pId =?1")
	public int updateProductPpid(Long pid,Long ppid);

	@Query(value="SELECT listpic FROM product WHERE pid=?1",nativeQuery=true)
	String findListPicByPid(long pid);
	
	@Modifying
	@Query(value="UPDATE Product SET status = ?2 WHERE pId =?1")
	int updateProductState(Long pid, int status);
	
	@Modifying
	@Query(value="UPDATE Product SET isEndorsement = ?2 WHERE pId =?1")
	int updateProductEndorsement(Long pid, int status);

	Product findByNewSku(String newSku);
	
	@Query(value="SELECT * FROM product WHERE extcode=?1",nativeQuery=true)
	Product findProductByExtcode(String extcode);

	@Modifying
	@Query(value="UPDATE Product SET status = ?2 , date_upd =?3  WHERE pId in ?1")
	void updateProductStateBatch(List<Long> ids, int status,Date date);

}