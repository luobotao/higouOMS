package controllers.redis;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.product.Category;
import models.product.Channel;
import models.product.ChannelMould;
import models.product.ChannelMouldPro;
import models.product.Currency;
import models.product.Fromsite;
import models.product.Mould;
import models.product.Product;
import models.product.ProductDetail;
import models.product.ProductDetailPram;
import models.product.ProductImages;
import models.subject.SubjectMouldPro;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;

import play.Configuration;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.twirl.api.Html;
import services.ICacheService;
import services.ServiceFactory;
import services.product.CategoryService;
import services.product.ChannelService;
import services.product.ProductService;
import services.subject.SubjectService;
import utils.AjaxHelper;
import utils.BeanUtils;
import utils.Constants;
import utils.Constants.DepositType;
import utils.Constants.Lovelydistinct;
import utils.Constants.NationalFlag;
import utils.Constants.ProductStatus;
import utils.Constants.TypsProduct;
import utils.Constants.Wayremark;
import utils.JedisHelper;
import utils.Numbers;
import utils.OSSUtils;
import utils.StringUtil;

import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.admin.AdminAuthenticated;
import controllers.admin.BaseAdminController;
import forms.ProductManageForm;

@Named
@Singleton
public class ProductRedisController extends BaseAdminController {
	private static final Logger.ALogger logger = Logger.of(ProductRedisController.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
	private final ProductService productService;
	private final ChannelService channelService;
	private final SubjectService subjectService;
	private final CategoryService categoryService;
	
    @Inject
    public ProductRedisController(final ProductService productService,final CategoryService categoryService,final ChannelService channelService,final SubjectService subjectService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.subjectService = subjectService;
        this.channelService = channelService;
    }
    private static ICacheService cache = ServiceFactory.getCacheService();
	/**
	 * 跳转至redis管理商品的页面
	 * @return
	 */
    @AdminAuthenticated()
	public Result productsManageRedis(){
		return ok(views.html.redis.productsManageRedis.render());
	}
	
	/**
	 * 商品在redis中存在的列表
	 * @return
	 */
    @AdminAuthenticated()
	public Result productsRedisList(){
		List<Product> productList = new ArrayList<Product>();
		
		if(cache instanceof JedisHelper){
			JedisHelper redis =(JedisHelper) cache;
			Set<String> productkeys = redis.getKeys(Constants.product_KEY);
			for(String key:productkeys){
				Product product = (Product) ServiceFactory.getCacheService().getObject(key );//从缓存读入
				if(product!=null)
					productList.add(product);
			}
		}
		Map<String,List<Product>> result = new HashMap<String, List<Product>>();
		result.put("data", productList);
		return ok(Json.toJson(result));
	}
	
	/**
	 * 删除redis中的商品
	 * @param pid
	 * @return
	 */
    @AdminAuthenticated()
	public Result deleteRedisProductByPid(Long pid){
		cache.clear(Constants.product_KEY+pid);
		ObjectNode result=Json.newObject();
		result.put("status", 1);
		return ok(Json.toJson(result));
	}
	
	/**
	 * 跳转至redis管理频道的页面
	 * @return
	 */
    @AdminAuthenticated()
	public Result channelContentManageRedis(){
		List<Channel> channelList = channelService.findAll();
		String selector="";
		if(channelList!=null && channelList.size()>0){
			selector = ChannelService.channelList2Html(channelList,channelList.get(0).getId());
		}
		return ok(views.html.redis.channelContentManageRedis.render(Html.apply(selector),channelList));
	}
	
	/**
	 * 根据频道ID获取下面的频道卡片（reids操作）
	 * @return
	 */
    @AdminAuthenticated()
	public Result getChannelMouldByCidRedis(Long channelId){
		List<ChannelMould> channelMouldList = channelService.findChannelMouldListByCid(channelId);
		for(ChannelMould channelMould : channelMouldList){
			Mould mould = channelService.findMouldByMouldId(channelMould.getMouldId());
			if(mould!=null)
				channelMould.setMouldName(mould.getMname());
		}
		Map<String,List<ChannelMould>> result = new HashMap<String, List<ChannelMould>>();
		result.put("data", channelMouldList);
		return ok(Json.toJson(result));
	}
	
	/**
	 * 根据频道卡片ID重置频道卡片
	 * @return
	 */
    @AdminAuthenticated()
	public Result updateChmould(Long cmid){
		cache.clear(Constants.channel_mould_KEY+cmid);//将此mouldId重置
		cache.clear(Constants.channelMouldProIds_KEY+cmid);//将此mouldId下的pro ids重置
		ObjectNode result=Json.newObject();
		result.put("status", 1);
		return ok(Json.toJson(result));
	}
	/**
	 * 根据频道ID重置频道
	 * @return
	 */
    @AdminAuthenticated()
	public Result updateChannel(Long cid){
		cache.clear(Constants.channelMouldIds_KEY+cid);//将此Channel下的mould ids重置
		ObjectNode result=Json.newObject();
		result.put("status", 1);
		return ok(Json.toJson(result));
	}
	
	/**
	 * 跳转至redis管理频道内容的页面
	 * @return
	 */
    @AdminAuthenticated()
	public Result channelMouldProManageRedis(Long cid,Long cmid){
		List<ChannelMouldPro> chmoProList = channelService.findChMoProListByCmId(cmid,cid);
		return ok(views.html.redis.channelMouldProManageRedis.render(chmoProList));
	}
	
	/**
	 * 根据频道内容ID重置频道内容
	 * @return
	 */
    @AdminAuthenticated()
	public Result deleteChmouldPro(Long cmpid){
		cache.clear(Constants.channel_mould_pro_KEY+cmpid);
		ObjectNode result=Json.newObject();
		result.put("status", 1);
		return ok(Json.toJson(result));
	}
}
