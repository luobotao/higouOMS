package services.product;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.product.Category;
import play.Logger;
import repositories.product.CategoryRepository;
import services.ServiceFactory;
import utils.Constants;
import utils.Htmls;

/**
 * 频道相关Service
 * @author luobotao
 * Date: 2015年4月17日 下午2:26:14
 */
@Named
@Singleton
public class CategoryService {

    private static final Logger.ALogger logger = Logger.of(CategoryService.class);

    @Inject
    private CategoryRepository categoryRepository;

    /**
     * 根据parentid与leval获取该leval下的分类
     * @param leval
     * @return
     */
    public List<Category> categoryList(int parentId,int leval){
    	return categoryRepository.findByParentidAndLeval(parentId,leval);
    }

    public Category findCategoryById(Integer id){
    	Category category = (Category) ServiceFactory.getCacheService().getObject(Constants.category_KEY+id );//从缓存读入
    	if(category==null){
    		category = categoryRepository.findOne(id);
    		ServiceFactory.getCacheService().setObject(Constants.category_KEY+id,category,0 );//从缓存读入
    	}
    	return category;
    }
    /**
     * 生成html中需要的select
     * @param categoryList
     * @param id
     * @return
     */
    public static String categoryList2Html(List<Category> categoryList,Integer id){
    	StringBuilder sb = new StringBuilder();
		sb.append(Htmls.generateOption(-1, "请选择"));
		for (Category c : categoryList) {
			if (id!=null && id.equals(c.getId())) {
				sb.append(Htmls.generateSelectedOption(c.getId(), c.getName()));
			} else {
				sb.append(Htmls.generateOption(c.getId(), c.getName()));
			}
		}
		return sb.toString();
    }
    /**
     * 获取品类名称
     * @param categoryList
     * @param id
     * @return
     */
    public String category2Categoryname(Integer id,String result){
		Category category = findCategoryById(id);
		if(category!=null){
			result = category.getName()+"-"+result;
			if(category!=null && category.getLeval()>=1){
				result = category2Categoryname(category.getParentid(),result);
			}
		}
    	return result;
    }
}
