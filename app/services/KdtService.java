package services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.http.HttpResponse;





import com.fasterxml.jackson.databind.JsonNode;

import play.Logger;
import play.libs.Json;
import utils.kdt.KdtApiClient;


@Named
@Singleton
public class KdtService extends Thread{
    private static final Logger.ALogger LOGGER = Logger.of(KdtService.class);
    private static final String APP_ID = "c47e4a0d8a3bcb58f7"; //这里换成你的app_id
	private static final String APP_SECRET = "2343559a21ae971997f1103abce4e459"; //这里换成你的app_secret
    private static KdtService instance = new KdtService();
    private Executor executor = Executors.newSingleThreadExecutor();
    private LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
	 /* 私有构造方法，防止被实例化 */
	private KdtService(){
		this.start();
	}
	public void run(){
//		LOGGER.info("start KdtService service ");
		System.out.println("start KdtService service ");
		Runnable r;
		try {
			while((r = tasks.take()) != null){
				executor.execute(r);
			}
		} catch (InterruptedException e) {
			LOGGER.error("InterruptedException in KdtService service",e);
		}
	}
	public static KdtService getInstance(){
		return instance;
	}

	/**
	 * 获取单个商品信息
	 * @param num_iid
	 */
	private JsonNode getProduct(long num_iid){
		JsonNode resultJson = Json.newObject();
		String method = "kdt.item.get";
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("num_iid", String.valueOf(num_iid));
		
		KdtApiClient kdtApiClient;
		HttpResponse response;
		
		try {
			kdtApiClient = new KdtApiClient(APP_ID, APP_SECRET);
			response = kdtApiClient.get(method, params);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			resultJson = Json.parse(result.toString());
			System.out.println(resultJson);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultJson;
	}
	/**
	 * 删除一个商品
	 * @param num_iid
	 * @return
	 */
	private JsonNode deleteProduct(long num_iid){
		JsonNode resultJson = Json.newObject();
		String method = "kdt.item.delete";
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("num_iid", String.valueOf(num_iid));
		
		KdtApiClient kdtApiClient;
		HttpResponse response;
		
		try {
			kdtApiClient = new KdtApiClient(APP_ID, APP_SECRET);
			response = kdtApiClient.get(method, params);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			resultJson = Json.parse(result.toString());
			System.out.println(resultJson);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultJson;
	}
	/**
	 * 更新SKU信息
	 * @param num_iid 必须
	 * @param sku_id 必须
	 * @param quantity非必须 
	 * @param price非必须
	 * @param outer_id非必须
	 * @return
	 */
	private JsonNode updateSKU(long num_iid,long sku_id,long quantity,String price,String outer_id){
		JsonNode resultJson = Json.newObject();
		String method = "kdt.item.sku.update";
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("num_iid", String.valueOf(num_iid));
		params.put("sku_id", String.valueOf(sku_id));
		params.put("quantity", String.valueOf(quantity));
		params.put("price", price);
		params.put("outer_id", outer_id);
		
		KdtApiClient kdtApiClient;
		HttpResponse response;
		
		try {
			kdtApiClient = new KdtApiClient(APP_ID, APP_SECRET);
			response = kdtApiClient.get(method, params);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			resultJson = Json.parse(result.toString());
			System.out.println(resultJson);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultJson;
	}
	
	/**
	 * 新增一个商品
	 * @param cid 商品分类的叶子类目id
	 * @param promotion_cid商品推广栏目id
	 * @param tag_ids商品标签id串，结构如：1234,1342,...
	 * @param price商品价格。取值范围：0.01-100000000；精确到2位小数；单位：元。需要在Sku价格所决定的的区间内
	 * @param title商品标题。不能超过100字，受违禁词控制
	 * @param desc商品描述。字数要大于5个字符，小于25000个字符 ，受违禁词控制
	 * @param is_virtual是否是虚拟商品。0为否，1为是。目前不支持虚拟商品
	 * @param post_fee运费。取值范围：0.00-999.00；精确到2位小数；单位：元
	 * @param sku_properties Sku的属性串。如：颜色:黄色;尺寸:M;重量:1KG,颜色:黄色;尺寸:S;重量:1KG 格式：pText:vText;pText:vText，多个sku之间用逗号分隔，如：颜色:黄色;尺寸:M,颜色:黄色;尺寸:S。pText和vText文本中不可以存在冒号和分号以及逗号  为了兼顾移动端商品界面展示的美观，目前有赞仅支持Sku的属性个数小于等于三个（比如：颜色、尺寸、重量 这三个属性）。无Sku则为空
	 * @param sku_quantities Sku的数量串。结构如：num1,num2,num3 如：2,3。无Sku则为空
	 * @param sku_prices Sku的价格串。结构如：10.00,5.00,... 精确到2位小数。单位:元。无Sku则为空
	 * @param sku_outer_ids Sku的商家编码（商家为Sku设置的外部编号）串。结构如：1234,1342,... 。sku_properties, sku_quantities, sku_prices, sku_outer_ids在输入数据时要一一对应，即使商家编码为空，也要用逗号相连。无Sku则为空
	 * @param skus_with_json商品Sku信息的Json字符串，[{"sku_price":"10.00","sku_property":{"颜色":"黄色","尺寸":"M","重量":"1KG"},"sku_quantity":"2","sku_outer_id":"1234"},{"sku_price":"5.00","sku_property":{"颜色":"黄色","尺寸":"S","重量":"1KG"},"sku_quantity":"3","sku_outer_id":"1242"}]  调用时，参数 sku_properties、sku_quantities、sku_prices、sku_outer_ids四个字段组合方式 和 skus_with_json 单字段输入方式 选其一个方式即可，无Sku则为空。 具体参见kdt.item.update文档描述。
	 * @param origin_price 显示在“原价”一栏中的信息
	 * @param buy_url 该商品的外部购买地址。当用户购买环境不支持微信或微博支付时会跳转到此地址
	 * @param outer_id 商品货号（商家为商品设置的外部编号）
	 * @param buy_quota每人限购多少件。0代表无限购，默认为0
	 * @param quantity商品总库存。当商品没有Sku的时候有效，商品有Sku时，总库存会自动按所有Sku库存之和计算
	 * @param hide_quantity是否隐藏商品库存。在商品展示时不显示商品的库存，默认0：显示库存，设置为1：不显示库存
	 * @param fields需要返回的商品对象字段，如title,price,desc等。可选值：Item商品结构体中所有字段均可返回；多个字段用“,”分隔。如果为空则返回所有
	 * @param fileKey images[] 商品图片文件列表，可一次上传多张。最大支持 1M，支持的文件类型：gif,jpg,jpeg,png注：图片参数不参与通讯协议签名，参数名中的中括号"[]"必须有，否则不能正常工作
	 * @param filePaths
	 * @return
	 */
	private JsonNode productAdd(String cid, String promotion_cid,
			String tag_ids, String price, String title, String desc,
			String is_virtual, String post_fee, String sku_properties,
			String sku_quantities, String sku_prices, String sku_outer_ids,
			String skus_with_json, String origin_price, String buy_url,
			String outer_id, String buy_quota, String quantity,
			String hide_quantity, String fields, String fileKey,
			List<String> filePaths) {
		JsonNode resultJson = Json.newObject();
		String method = "kdt.item.add";
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("cid", cid);
		params.put("promotion_cid", promotion_cid);
		params.put("tag_ids", tag_ids);
		params.put("price", price);
		params.put("title", title);
		params.put("desc", desc);
		params.put("is_virtual", is_virtual);
		params.put("post_fee", post_fee);
		params.put("sku_properties", sku_properties);
		params.put("sku_quantities", sku_quantities);
		params.put("sku_prices", sku_prices);
		params.put("sku_outer_ids",sku_outer_ids);
		params.put("skus_with_json", skus_with_json);
		params.put("origin_price", origin_price);
		params.put("buy_url", buy_url);
		params.put("outer_id", outer_id);
		params.put("buy_quota", buy_quota);
		params.put("quantity", quantity);
		params.put("hide_quantity", hide_quantity);
		params.put("fields", fields);
		
		KdtApiClient kdtApiClient;
		HttpResponse response;
		
		try {
			kdtApiClient = new KdtApiClient(APP_ID, APP_SECRET);
			response = kdtApiClient.post(method, params, filePaths, fileKey);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}

			resultJson = Json.parse(result.toString());
			System.out.println(resultJson);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultJson;
	}
	/**
	 *  商品下架
	 * @param num_iid
	 * @param fields需要返回的商品对象字段，如title,price,desc等。可选值：Item商品结构体中所有字段均可返回；多个字段用“,”分隔。如果为空则返回所有
	 * @return
	 */
	private JsonNode delistingProduct(long num_iid,String fields){
		JsonNode resultJson = Json.newObject();
		String method = "kdt.item.update.delisting";
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("num_iid", String.valueOf(num_iid));
		params.put("fields", fields);
		
		KdtApiClient kdtApiClient;
		HttpResponse response;
		
		try {
			kdtApiClient = new KdtApiClient(APP_ID, APP_SECRET);
			response = kdtApiClient.get(method, params);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			resultJson = Json.parse(result.toString());
			System.out.println(resultJson);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultJson;
	}
	/**
	 * 商品上架
	 * @param num_iid
	 * @param fields需要返回的商品对象字段，如title,price,desc等。可选值：Item商品结构体中所有字段均可返回；多个字段用“,”分隔。如果为空则返回所有
	 * @return
	 */
	private JsonNode listingProduct(long num_iid,String fields){
		JsonNode resultJson = Json.newObject();
		String method = "kdt.item.update.listing";
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("num_iid", String.valueOf(num_iid));
		params.put("fields", fields);
		
		KdtApiClient kdtApiClient;
		HttpResponse response;
		
		try {
			kdtApiClient = new KdtApiClient(APP_ID, APP_SECRET);
			response = kdtApiClient.get(method, params);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			resultJson = Json.parse(result.toString());
			System.out.println(resultJson);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultJson;
	}
	/**
	 * 根据商品货号获取商品
	 * @param fields
	 * @param outer_id
	 * @return
	 */
	private JsonNode getProductByOut(String fields,String outer_id){
		JsonNode resultJson = Json.newObject();
		String method = "kdt.items.custom.get";
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("fields", fields);
		params.put("outer_id", outer_id);
		
		KdtApiClient kdtApiClient;
		HttpResponse response;
		
		try {
			kdtApiClient = new KdtApiClient(APP_ID, APP_SECRET);
			response = kdtApiClient.get(method, params);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			resultJson = Json.parse(result.toString());
			System.out.println(resultJson);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultJson;
	}
	/**
	 * 获取商品分类二维列表
	 */
	private JsonNode getCategories(){
		JsonNode resultJson = Json.newObject();
		String method = "kdt.itemcategories.get";
		HashMap<String, String> params = new HashMap<String, String>();
		
		KdtApiClient kdtApiClient;
		HttpResponse response;
		
		try {
			kdtApiClient = new KdtApiClient(APP_ID, APP_SECRET);
			response = kdtApiClient.get(method, params);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			resultJson = Json.parse(result.toString());
			System.out.println(resultJson);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultJson;
	}
	/**
	 * 获取商品推广栏目列表
	 */
	private JsonNode getCategoriesPromotions(){
		JsonNode resultJson = Json.newObject();
		String method = "kdt.itemcategories.promotions.get";
		HashMap<String, String> params = new HashMap<String, String>();
		
		KdtApiClient kdtApiClient;
		HttpResponse response;
		
		try {
			kdtApiClient = new KdtApiClient(APP_ID, APP_SECRET);
			response = kdtApiClient.get(method, params);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			resultJson = Json.parse(result.toString());
			System.out.println(resultJson);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultJson;
	}
	/**
	 * 获取商品自定义标签列表
	 */
	private JsonNode getCategoriesTags(){
		JsonNode resultJson = Json.newObject();
		String method = "kdt.itemcategories.tags.get";
		HashMap<String, String> params = new HashMap<String, String>();
		
		KdtApiClient kdtApiClient;
		HttpResponse response;
		
		try {
			kdtApiClient = new KdtApiClient(APP_ID, APP_SECRET);
			response = kdtApiClient.get(method, params);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			resultJson = Json.parse(result.toString());
			System.out.println(resultJson);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultJson;
	}
	
	/**
	 * 分页获取商品自定义标签列表
	 * @param page_no 页码
	 * @param page_size 每页条数
	 * @param order_by 排序方式。格式为column:asc/desc，column可选值：created 创建时间 / modified 修改时间
	 */
	private JsonNode getCategoriesTagsByPage(int page_no,int page_size,String order_by){
		JsonNode resultJson = Json.newObject();
		String method = "kdt.itemcategories.tags.getpage";
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("page_no", String.valueOf(page_no));
		params.put("page_size", String.valueOf(page_size));
		params.put("order_by", order_by);
		KdtApiClient kdtApiClient;
		HttpResponse response;
		
		try {
			kdtApiClient = new KdtApiClient(APP_ID, APP_SECRET);
			response = kdtApiClient.get(method, params);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			resultJson = Json.parse(result.toString());
			System.out.println(resultJson);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultJson;
	}
	
	/**
	 * 获取仓库中的商品列表
	 * @param fields 需要返回的商品对象字段，如title,price,desc等。可选值：Item商品结构体中所有字段均可返回；多个字段用“,”分隔。如果为空则返回所有
	 * @param q 搜索字段。搜索商品的title
	 * @param tag_id 商品标签的ID 为0则表示此项为空
	 * @param page_no 页码
	 * @param page_size 每页条数
	 * @param order_by
	 * @return 排序方式。格式为column:asc/desc，column可选值：created 创建时间 / modified 修改时间
	 */
	private JsonNode getProductsInventory(String fields,String q,String banner,long tag_id,int page_no,int page_size,String order_by){
		JsonNode resultJson = Json.newObject();
		String method = "kdt.items.inventory.get";
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("fields", fields);
		params.put("q", q);
		params.put("banner", banner);
		params.put("tag_id", String.valueOf(tag_id));
		params.put("page_no", String.valueOf(page_no));
		params.put("page_size", String.valueOf(page_size));
		params.put("order_by", order_by);
		KdtApiClient kdtApiClient;
		HttpResponse response;
		
		try {
			kdtApiClient = new KdtApiClient(APP_ID, APP_SECRET);
			response = kdtApiClient.get(method, params);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			resultJson = Json.parse(result.toString());
			System.out.println(resultJson);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultJson;
	}
	/**
	 * 获取出售中的商品列表
	 * @param fields 需要返回的商品对象字段，如title,price,desc等。可选值：Item商品结构体中所有字段均可返回；多个字段用“,”分隔。如果为空则返回所有
	 * @param q 搜索字段。搜索商品的title
	 * @param tag_id 商品标签的ID 为0则表示此项为空
	 * @param page_no 页码
	 * @param page_size 每页条数
	 * @param order_by
	 * @return 排序方式。格式为column:asc/desc，column可选值：created 创建时间 / modified 修改时间
	 */
	private JsonNode getProductsOnsale(String fields,String q,long tag_id,int page_no,int page_size,String order_by){
		JsonNode resultJson = Json.newObject();
		String method = "kdt.items.onsale.get";
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("fields", fields);
		params.put("q", q);
		params.put("tag_id", String.valueOf(tag_id));
		params.put("page_no", String.valueOf(page_no));
		params.put("page_size", String.valueOf(page_size));
		params.put("order_by", order_by);
		KdtApiClient kdtApiClient;
		HttpResponse response;
		
		try {
			kdtApiClient = new KdtApiClient(APP_ID, APP_SECRET);
			response = kdtApiClient.get(method, params);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			resultJson = Json.parse(result.toString());
			System.out.println(resultJson);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultJson;
	}
	/**
	 * 根据外部编号取商品Sku
	 * 跟据商家编码（商家为Sku设置的外部编号）获取商品Sku，如果一个outer_id对应多个Sku会返回所有符合条件的Sku
	 * @param fields
	 * @param outer_id
	 * @param num_iid
	 * @return
	 */
	private JsonNode getSkusCustom(String fields,String outer_id,long num_iid){
		JsonNode resultJson = Json.newObject();
		String method = "kdt.skus.custom.get";
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("fields", fields);
		params.put("outer_id", outer_id);
		params.put("num_iid", String.valueOf(num_iid));
		KdtApiClient kdtApiClient;
		HttpResponse response;
		
		try {
			kdtApiClient = new KdtApiClient(APP_ID, APP_SECRET);
			response = kdtApiClient.get(method, params);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			resultJson = Json.parse(result.toString());
			System.out.println(resultJson);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultJson;
	}
	
	public static void main(String args[]){
		KdtService.getInstance().getProduct(6807080);
//		KdtService.getInstance().delistingProduct(6807080,"");
//		KdtService.getInstance().listingProduct(6807080,"");
//		KdtService.getInstance().updateSKU(6807080,6807080,3,"","");
		KdtService.getInstance().getProductByOut("num_iid,title,price,desc,skus,item_imgs","G1230233");
		KdtService.getInstance().getCategories();
		KdtService.getInstance().getCategoriesPromotions();
		KdtService.getInstance().getCategoriesTags();
		KdtService.getInstance().getProductsOnsale("num_iid,title,price,desc,skus,item_imgs", "", 0, 1, 20, "created:asc");
		KdtService.getInstance().getCategoriesTagsByPage(2,2,"created:asc");
		KdtService.getInstance().getProductsInventory("num_iid,title,price,desc,skus,item_imgs", "", "",0, 1, 20, "created:asc");
		KdtService.getInstance().getSkusCustom("num_iid,title,item_imgs","",6807080);
		String cid="";
		String promotion_cid="";
		String tag_ids="";
		String price="999.01";
		String title="测试商品";
		String desc ="";
		String is_virtual ="";
		String post_fee ="";
		String sku_properties ="";
		String sku_quantities ="";
		String sku_prices ="";
		String sku_outer_ids ="";
		String skus_with_json ="";
		String origin_price ="";
		String buy_url ="";
		String outer_id ="";
		String buy_quota ="";
		String quantity ="";
		String hide_quantity ="";
		String fields ="";
		String fileKey = "images[]";
		List<String> filePaths=new ArrayList<String>();
		filePaths.add("/Users/xuexiaozhe/Desktop/1.png");
		filePaths.add("/Users/xuexiaozhe/Desktop/2.png");
//		KdtService.getInstance().productAdd(cid, promotion_cid, tag_ids, price, title, desc, is_virtual, post_fee, sku_properties, sku_quantities, sku_prices, sku_outer_ids, skus_with_json, origin_price, buy_url, outer_id, buy_quota, quantity, hide_quantity, fields, fileKey, filePaths);
	}
	

}
