package services.kjt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;

import forms.parcels.ParcelsForm;
import models.AutoSyncTaskLog;
import models.Parcels;
import models.ParcelsPro;
import models.ParcelsWaybill;
import models.ShoppingOrder;
import models.ShoppingOrderPay;
import models.admin.AdminUser;
import models.kjt.ChinaAddress;
import models.kjt.FepBillInfo;
import models.product.AdminProduct;
import models.product.ChannelMouldPro;
import models.product.Product;
import models.product.ProductDetail;
import models.product.ProductImages;
import models.product.ProductUnion;
import models.subject.SubjectMouldPro;
import play.Logger;
import play.Play;
import play.libs.Json;
import repositories.Order.ChinaAddressRepository;
import repositories.kjt.FepBillInfoRepository;
import services.AutoSyncTaskLogService;
import services.OperateLogService;
import services.admin.AdminUserService;
import services.order.OrderPayService;
import services.order.OrderService;
import services.parcels.ParcelsProService;
import services.parcels.ParcelsService;
import services.parcels.ParcelsWaybillService;
import services.product.ChannelService;
import services.product.ProductService;
import services.subject.SubjectService;
import utils.Constants;
import utils.Dates;
import utils.EmailService;
import utils.Numbers;
import utils.StringUtil;
import utils.kjt.KjtApiClient;
import vo.kjt.SOAuthenticationInfo;
import vo.kjt.SOItemInfo;
import vo.kjt.SOPayInfo;
import vo.kjt.SOShippingInfo;


@Named
@Singleton
public class KjtService extends Thread{
    private static final Logger.ALogger logger = Logger.of(KjtService.class);
    private static KjtService instance = new KjtService();
    
    @Inject
    private AdminUserService adminUserService;
    @Inject
    private ProductService productService;
    @Inject
    private ParcelsProService parcelsProService;
    @Inject
    private ParcelsService parcelsService;
    @Inject
    private SubjectService subjectService;
    @Inject
    private ChannelService channelService;
    @Inject
    private OperateLogService operateLogService;
    @Inject
    private ChinaAddressRepository chinaAddressRepository;
    @Inject
    private FepBillInfoRepository fepBillInfoRepository;
    @Inject
    private OrderPayService orderPayService;
    @Inject
    private OrderService orderService;
    @Inject
    private ParcelsWaybillService parcelsWaybillService;
    @Inject
    private AutoSyncTaskLogService autoSyncTaskLogService;
    
    
    public double salePriceTotal = 0.0;
	public double taxPriceTotal = 0.0;
    private static final String saleChannelSysNo = "1097";
    private Executor executor = Executors.newSingleThreadExecutor();
    private LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
	 /* 私有构造方法，防止被实例化 */
	private KjtService(){
		this.start();
	}
	public void run(){
//		LOGGER.info("start KjtService service ");
		System.out.println("start KjtService service ");
		Runnable r;
		try {
			while((r = tasks.take()) != null){
				executor.execute(r);
			}
		} catch (InterruptedException e) {
			logger.error("InterruptedException in KjtService service",e);
		}
	}
	public static KjtService getInstance(){ 
		return instance;
	}

	/**
	 * 时间段内信息变化的商品ID列表获取
	 * 1.4.2
	 * @param num_iid
	 */
	public List<String> productIdGetQuery(String changedDateBegin,String changedDateEnd,int limitRows, int startRow){
		JsonNode resultJson = Json.newObject();
		String method = "product.productidgetquery";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("SaleChannelSysNo", saleChannelSysNo);
		params.put("ChangedDateBegin", changedDateBegin);
		params.put("ChangedDateEnd", changedDateEnd);
		params.put("StartRow", startRow);
		List<String> productIds = new ArrayList<String>();
		try {
			KjtApiClient kjtApiClient = new KjtApiClient();
			HttpResponse response = kjtApiClient.get(method, params);
			//logger.info("[kjt]productIdGetQuery : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			logger.info("[kjt]productIdGetQuery :"+result.toString());
			resultJson = Json.parse(result.toString());
			JsonNode jsonData = resultJson.get("Data");
			if(jsonData!=null){
				int total = jsonData.get("Total").asInt();
				JsonNode productList = jsonData.withArray("ProductList");
				AdminUser adminUser = adminUserService.findByUsername("kjt");
				for(int i = 0; i < productList.size(); i++){
					int status = productList.get(i).get("Status").asInt();//商品状态
					String productId = productList.get(i).get("ProductID").asText();
					Product product = productService.findProductByExtcode(productId);
					productIds.add(productId);//将信息变动的商品加入集合中
					if(product!=null&&status!=1){//商品在跨境通非上架状态
						operateLogService.saveProductStateLog(adminUser.getId(), adminUser.getUsername(), "127.0.0.1", product.getPid(), product.getStatus(), 20);
						product.setStatus(20);//将此商品下架
						productService.saveProduct(product);
					}
					logger.info("获取时间段内信息变化的跨境通商品总数:"+total+"，商品ID："+productList.get(i).get("ProductID").asText()+"加入完成，商品信息变更时间："+productList.get(i).get("ChangeDate").asText());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return productIds;
	}
	/**
	 * 根据商品ID批量获取商品详细信息
	 * 1.4.3
	 * @param objects
	 */
	public boolean productInfoBatchGet(Object[] objects){
		JsonNode resultJson = Json.newObject();
		String method = "product.proudctinfobatchget";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("ProductIDs", objects);
		params.put("SaleChannelSysNo", saleChannelSysNo);
		boolean flag = true;
		try {
			KjtApiClient kjtApiClient = new KjtApiClient();
			HttpResponse response = kjtApiClient.get(method, params);
			//logger.info("[kjt]productInfoBatchGet : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"utf-8"));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			logger.info("[kjt]productInfoBatchGet : "+result.toString());
			resultJson = Json.parse(result.toString());
			JsonNode jsonData = resultJson.get("Data");
			if(jsonData!=null){
				JsonNode productList = jsonData.withArray("ProductList");
				for(int i = 0; i < productList.size(); i++){
					flag = addOrUpdateProductWithKjt(productList.get(i));
					logger.info("添加或更新跨境通商品"+productList.get(i).get("ProductID").asText()+"入库完成，状态："+flag);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());
			return false;
		}
		return flag;
	}
	
	/**
	 *  
	 * <p>Title: addOrUpdateProductWithKjt</p> 
	 * <p>Description: 保存或更新kjt过来的商品信息</p> 
	 * @param jsonNode
	 * @return
	 */
	private boolean addOrUpdateProductWithKjt(JsonNode jsonNode) {
		JsonNode productEntryInfo = jsonNode.get("ProductEntryInfo");
		logger.info("[kjt]获取跨境通商品详细信息："+productEntryInfo.asText());
		Long pid = 0L;
		//获取商品ID
		String extcode = jsonNode.get("ProductID").asText();
		//判断商品ID是否已入库，入库则进行更新操作，未入库则进行添加操作
		Product product = productService.findProductByExtcode(extcode);
		AdminUser adminUser = adminUserService.findByUsername("kjt");
		if(product == null ){
			product = new Product();
			product.setTyp("2");			//自营
			product.setStatus(1);//新品待审
			product.setEndorsementPrice(0.0);
			product.setDate_add(new Date());
			product.setIsSyncErp(9);//是否同步ERP,默认不同步
			//商品价格 Price
			product.setChinaprice(0.0);
			product.setRmbprice(jsonNode.get("Price").asDouble());
			product.setEndorsementPrice(jsonNode.get("Price").asDouble());
			product.setPrice(0.0);
			product.setCurrency(1);
			product.setIslockprice(1);//锁定价格
			product.setFreight((double) 0);
			product.setMancnt(0);//映射的是int 默认值暂时不起作用
			product.setDeposit(0.0);
			product.setRtitle("");
			product.setPreselltoast("");
			product.setPtyp("1");
			product.setLovelydistinct(10.0);
			product.setIslovely("0");//普通商品
			product.setBtim("");
			product.setEtim("");
			product.setNewMantype("0");//1首购商品 2仅一次商品 0普通（不做处理）3、0元商品
			//product.setCategory(1);
			product.setAdstr1("");//推荐理由
			product.setLimitcount(2);								//限购数量
			product.setWayremark(Constants.Wayremark.wayRemark2Message(6));//发货地
			product.setNlikes(0);//喜欢数
			product.setIsopenid(1);//是否需要身份证信息
			product.setIsopenidimg(0);//是否需要身份证图片信息
			product.setNationalFlag("");//是否展示国旗
			product.setExtcode(jsonNode.get("ProductID").asText());
			product.setNstock(jsonNode.get("OnlineQty").asLong());//库存
			product.setTitle(jsonNode.get("ProductName").asText());//标题
			product.setSubtitle(jsonNode.get("ProductDesc").asText());//子标题
		}else{
			pid = product.getPid();
		}
		product.setWeight(Numbers.parseDouble(Numbers.formatDouble(jsonNode.get("Weight").asDouble()/1000.0, "0.0000"), 0.00));	//重量 g 转  kg
		product.setDate_upd(new Date());
		product = productService.saveProduct(product);
		ProductUnion productUnion = productService.findProductUnionByPid(product.getPid());
		if(productUnion == null){
			productUnion = new ProductUnion();
			productUnion.setDateAdd(new Date());
			productUnion.setBuyNowFlag("1");
			productUnion.setPid(product.getPid());
			productUnion.setTaxPrice(Numbers.parseDouble(Numbers.formatDouble(jsonNode.get("Price").asDouble(), "0.00"), 0.00));
		}
		//更新商品税率信息
		productUnion.setDateUpd(new Date());
		productUnion.setTaxRate(jsonNode.get("ProductEntryInfo").get("TariffRate").asDouble()*100);
		productService.saveProductUnion(productUnion);
		product.setSkucode("1000"+product.getPid());//设置Skucode
		if(StringUtils.isBlank(product.getNewSku())){
			String newSku = productService.getNewSkuByCategory(product.getCategory());
			//product.setNewSku(newSku);
		}
		if(pid.longValue()==0||product.getPpid()==null){
			product.setPpid(product.getPid());
		}
		product = productService.saveProduct(product);
		if(pid.longValue()==0){//如果是新增，需要初始化两个详情
			operateLogService.saveProductLog(adminUser.getId(), adminUser.getUsername(), "127.0.0.1", "新建跨境通商品,创建后的商品ID为:"+product.getPid());
			ProductDetail productDetail=new ProductDetail();
			productDetail.setChname("商品详情");
			productDetail.setEnname("Details");
			productDetail.setDetail("");
			productDetail.setDate_add(new Date());
			productDetail.setPid(product.getPid());
			productDetail.setNsort(0);
			productDetail = productService.saveDetail(productDetail);
			ProductDetail productDetail2=new ProductDetail();
			productDetail2.setChname("商品参数");
			productDetail2.setEnname("Parameters");
			productDetail2.setDetail("");
			productDetail2.setDate_add(new Date());
			productDetail2.setPid(product.getPid());
			productDetail2.setNsort(0);
			productDetail2 = productService.saveDetail(productDetail2);
		}
		//初始化插入列表图片和商品图片和规格图
		String listPic = jsonNode.get("DefaultImage").asText();
		//初始化插入列表图片和商品图片和规格图
		if (product!=null && listPic != null&&!"null".endsWith(listPic)) {
			int p = listPic.lastIndexOf('.');
			String type = listPic.substring(p, listPic.length()).toLowerCase();
			
			// 检查文件后缀格式
			if (".jpg".equals(type)||".gif".equals(type)||".png".equals(type)||".jpeg".equals(type)||".bmp".equals(type)) {
				//上次列表图片
				product.setListpic(listPic);
				product.setSpecpic(listPic);
				product.setSpecifications("");
				productService.saveProduct(product);
				//上次商品图片
				ProductImages productImage = new ProductImages();
				List<ProductImages> productImages= productService.findProductImagesByPid(pid);
				if(productImages!=null&&productImages.size()>0){
					productImage = productImages.get(0);
				}
				productImage.setDate_add(new Date());
				productImage.setFilename(listPic);
				productImage.setPid(pid);
				productImage.setPcode(product.getSkucode());
				productService.saveProductImages(productImage);
				// 上次规格图
				List<ChannelMouldPro> chmouldProList = channelService.findChMoPrByPId(product.getPid());
				for(ChannelMouldPro channelMouldPro:chmouldProList){
					channelMouldPro.setImgurl(listPic);
					channelService.saveChannelMouldPro(channelMouldPro, "", channelMouldPro.getCmid());//此处channelId传入的是空，是因为肯定是新图片，不需要再传入channelid了
				}
				List<SubjectMouldPro> subjectMouldProList = subjectService.findSuMoPrByPId(product.getPid());
				for(SubjectMouldPro subjectMouldPro:subjectMouldProList){
					subjectMouldPro.setImgurl(listPic);
					subjectService.saveSubjectMouldPro(subjectMouldPro,"",subjectMouldPro.getId());
				}
			}
		}
		//增加店铺和商品绑定关系，跨境通商品信息
		List<AdminProduct> adminProductList = productService.findAdminProductByPidAndAdminid(product.getPid(), adminUser.getId());
		if(adminProductList==null || adminProductList.isEmpty()){
			AdminProduct adminProduct = new AdminProduct();
			adminProduct.setAdminid(adminUser.getId());
			adminProduct.setDate_add(new Date());
			adminProduct.setPid(product.getPid());
			productService.saveAdminProduct(adminProduct); 
		}
		return true;
	}
	/**
	 * 时间段内价格变化的商品ID列表获取
	 * 1.4.4
	 * @param num_iid
	 */
	public boolean productPriceChangeIDQuery(String priceChangedDateBegin, String priceChangedDateEnd, int limitRows, int startRow){
		JsonNode resultJson = Json.newObject();
		boolean flag = true;
		String method = "product.productpricechangeidquery";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("SaleChannelSysNo", saleChannelSysNo);
		params.put("PriceChangedDateBegin", priceChangedDateBegin);
		params.put("PriceChangedDateEnd", priceChangedDateEnd);
		params.put("StartRow", startRow);
		try {
			KjtApiClient kjtApiClient = new KjtApiClient();
			HttpResponse response = kjtApiClient.get(method, params);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			resultJson = Json.parse(result.toString());
			System.out.println(resultJson);
			JsonNode jsonData = resultJson.get("Data");
			if(jsonData!=null){
				int total = jsonData.get("Total").asInt();
				JsonNode productPriceList = jsonData.withArray("ProductPriceList");
				StringBuffer sb = new StringBuffer();
				AdminUser adminUser = adminUserService.findByUsername("kjt");
				for(int i = 0; i < productPriceList.size(); i++){
					String productId = productPriceList.get(i).get("ProductID").asText();//将价格变动的商品更新入库中
					Product product = productService.findProductByExtcode(productId);
					if(product!=null){
						//更新商品状态和销售价格
						int status = productPriceList.get(i).get("Status").asInt();//商品状态
						if(status!=1){//商品在跨境通非上架状态
							operateLogService.saveProductStateLog(adminUser.getId(), adminUser.getUsername(), "127.0.0.1", product.getPid(), product.getStatus(), 20);
							product.setStatus(20);//将此商品下架
						}
						double productPrice = productPriceList.get(i).get("ProductPrice").asDouble();//渠道销售价格
						String priceChangedDate = productPriceList.get(i).get("PriceChangedDate").asText();//发生商品创建、修改或订阅的最后时间
						//product.setRmbprice(productPrice);
						//productService.saveProduct(product);
						//跨境通商品价格变化，记录，发送邮件到王双师
						sb.append("跨境通商品ID:"+productId+"对应NewSku为："+product.getNewSku()+",价格发生了变化，由原先的："+product.getPrice()+"变更为："+productPrice+",请核实！");
						//更新商品价格
						logger.info("获取时间段内价格变化的跨境通商品总数:"+total+"，商品ID："+productPriceList.get(i).get("ProductID").asText()+"加入完成，商品价格变更时间："+productPriceList.get(i).get("PriceChangeDate").asText());
					}
				}
				if(!"".equals(sb.toString())){
					monitorProductPrice(sb.toString());	
				}
			}else{
				flag = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}
	
	/**
	 * 
	 * <p>Title: monitorProductPrice</p> 
	 * <p>Description: 邮件发送跨境通商品价格变化的信息</p> 
	 * @param string
	 */
	public boolean monitorProductPrice(String content) {
		String sendTo = Play.application().configuration().getString("kjt.product.pricechange.sendto");
		EmailService.getInstance().sendEmailWithKjtProductPrice(sendTo,content);
        logger.info("邮件发送跨境通商品价格变化的信息完成");
		return false;
	}
	/**
	 * 根据商品ID批量获取商品价格信息
	 * 1.4.5
	 * @param num_iid
	 */
	public boolean productPriceBatchGet(String[] productIds){
		JsonNode resultJson = Json.newObject();
		boolean flag = true;
		String method = "product.productpricebatchget";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("ProductIDs", productIds);
		params.put("SaleChannelSysNo", saleChannelSysNo);
		try {
			KjtApiClient kjtApiClient = new KjtApiClient();
			HttpResponse response = kjtApiClient.get(method, params);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			resultJson = Json.parse(result.toString());
			System.out.println(resultJson);
			JsonNode jsonData = resultJson.get("Data");
			if(jsonData!=null){
				JsonNode productPriceList = jsonData.withArray("ProductPriceList");
				AdminUser adminUser = adminUserService.findByUsername("kjt");
				for(int i = 0; i < productPriceList.size(); i++){
					String productId = productPriceList.get(i).get("ProductID").asText();//将价格变动的商品更新入库中
					Product product = productService.findProductByExtcode(productId);
					if(product!=null){
						//更新商品状态和销售价格
						int status = productPriceList.get(i).get("Status").asInt();//商品状态
						if(status!=1){//商品在跨境通非上架状态
							operateLogService.saveProductStateLog(adminUser.getId(), adminUser.getUsername(), "127.0.0.1", product.getPid(), product.getStatus(), 20);
							product.setStatus(20);//将此商品下架
						}
						double productPrice = productPriceList.get(i).get("ProductPrice").asDouble();//渠道销售价格
						String priceChangedDate = productPriceList.get(i).get("PriceChangedDate").asText();//发生商品创建、修改或订阅的最后时间
						//记录，出发邮件到王双师
						//product.setRmbprice(productPrice);
						//productService.saveProduct(product);
						//更新商品价格
						logger.info("获取时间段内价格变化的跨境通商品,商品ID："+productPriceList.get(i).get("ProductID").asText()+"加入完成，商品价格变更时间："+productPriceList.get(i).get("PriceChangeDate").asText());
					}
				}
			}else{
				flag = false;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}
	
	/**
	 * 商品分销渠道库存批量获取
	 * 1.5.3
	 * @param productIds
	 */
	public boolean channelQ4SBatchGet(String productIds){
		JsonNode resultJson = Json.newObject();
		boolean flag = true;
		String method = "inventory.channelq4sbatchget";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("ProductIDs", productIds);	//多个ID,用英文，分隔,最多20个商品ID
		params.put("SaleChannelSysNo", saleChannelSysNo);
		try {
			KjtApiClient kjtApiClient = new KjtApiClient();
			HttpResponse response = kjtApiClient.get(method, params);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			resultJson = Json.parse(result.toString());
			System.out.println(resultJson);
			JsonNode productPriceList = resultJson.withArray("Data");
			if(productPriceList!=null&&productPriceList.size()>0){
				AdminUser adminUser = adminUserService.findByUsername("kjt");
				for(int i = 0; i < productPriceList.size(); i++){
					String productId = productPriceList.get(i).get("ProductID").asText();//将库存变动的商品更新入库中
					Product product = productService.findProductByExtcode(productId);
					if(product!=null){
						//更新商品状态和销售价格
						long onlineQty = productPriceList.get(i).get("OnlineQty").asInt();//可销售库存
						int wareHouseId = productPriceList.get(i).get("WareHouseID").asInt();//出库仓库
						operateLogService.saveParcelsLog(adminUser.getId(), adminUser.getUsername(), "127.0.0.1", "跨境通商品ID:"+productId+"库存调整，由原先的："+product.getNstock()+"变更为："+onlineQty);
						product.setNstock(onlineQty);
						productService.saveProduct(product);
						//更新商品价格
						logger.info("跨境通商品库存,商品ID："+productPriceList.get(i).get("ProductID").asText()+"库存："+productPriceList.get(i).get("OnlineQty").asInt());
					}
				}
			}else{
				flag = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}
	
	//###################订单服务###########################
    /**
    * 分销渠道订单创建接收接口
    * 1.6.1          
    * @param parcel
    */
    public JsonNode soCreate(Parcels parcel){
         JsonNode resultJson = Json.newObject();
         String method = "order.socreate";
         HashMap<String, Object> params = new HashMap<String, Object>();
         params.put("SaleChannelSysNo", saleChannelSysNo);//渠道编号
         params.put("MerchantOrderID", parcel.getParcelCode());//订单在上架销售平台上的唯一编号   订单号
         params.put("ServerType", "S02");//S01:一般进口  S02:保税区进口  为空默认S02
         params.put("WarehouseID", "52");//订单出库仓库在Kjt平台的编号
         ShoppingOrderPay sopay = orderPayService.findByOrderId(parcel.getOrderId());
         ShoppingOrder so = orderService.queryShoppingOrderById(parcel.getOrderId());
		 //订单中购买商品列表
		 List<SOItemInfo> soItemInfos = new ArrayList<SOItemInfo>(); 
         soItemInfos = dealSOItemInfo(parcel, soItemInfos);
         params.put("ItemList", Json.toJson(soItemInfos));
         //订单支付信息
         SOPayInfo soPayInfo = dealSoPayInfo(parcel,sopay,so);
         params.put("PayInfo", Json.toJson(soPayInfo));
         //订单配送信息
         SOShippingInfo soShippingInfo = dealSoShippingInfo(parcel);
         params.put("ShippingInfo", Json.toJson(soShippingInfo));
         //下单用户实名认证信息
         SOAuthenticationInfo soAuthenticationInfo = dealSOAuthenticationInfo(parcel);
         params.put("AuthenticationInfo", Json.toJson(soAuthenticationInfo));
        
         try {
              KjtApiClient kjtApiClient = new KjtApiClient();
              HttpResponse response = kjtApiClient.get(method, params);
              System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
              BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
              StringBuffer result = new StringBuffer();
              String line = "";
              while ((line = bufferedReader.readLine()) != null) {
                   result.append(line);
              }
              resultJson = Json.parse(URLDecoder.decode(result.toString(),"utf-8"));
              logger.info("跨境通订单同步返回日志："+resultJson);
              String code = resultJson.get("Code").asText();
              long pardelId = parcel.getId();
              if("0".equals(code)){
            	  	//订单发送失败，记录
            	  	AutoSyncTaskLog autoSyncTaskLog = autoSyncTaskLogService.queryWithRecord(String.valueOf(pardelId),"SoCreateErr","kjterr");
					if(autoSyncTaskLog==null){
						autoSyncTaskLog = new AutoSyncTaskLog();
						autoSyncTaskLog.setRecord(String.valueOf(pardelId));
						autoSyncTaskLog.setDateAdd(new Date());
						autoSyncTaskLog.setMemo("包裹同步至KJT失败");
						autoSyncTaskLog.setOperType("SoCreateErr");
						autoSyncTaskLog.setTarget("kjterr");
						autoSyncTaskLog.setDateUpd(new Date());
						autoSyncTaskLogService.save(autoSyncTaskLog);
						logger.info("添加包裹同步至KJT失败日志,包裹ID为："+autoSyncTaskLog.getRecord()+"，当前时间为："+Dates.formatDateTime(new Date()));
					}else{
						logger.info("添加包裹同步至KJT失败,日志已记录，将会在凌晨2点进行再次同步，包裹ID为："+autoSyncTaskLog.getRecord()+"，当前时间为："+Dates.formatDateTime(new Date()));
					}
              }else{
	  			  JsonNode jsonData = resultJson.get("Data");
	  			  if(jsonData!=null){
	  				  String merchantOrderID = jsonData.get("MerchantOrderID").asText();
	  				  long soSysNo = jsonData.get("SOSysNo").asLong();
	  				  double productAmount = jsonData.get("ProductAmount").asDouble();
	  				  double taxAmount = jsonData.get("TaxAmount").asDouble();
	  				  double shippingAmount = jsonData.get("ShippingAmount").asDouble();
	  				  if(merchantOrderID.equals(parcel.getParcelCode())){
	  					  parcel.setBbtid(String.valueOf(soSysNo));
	  					  parcel.setBbt_print(shippingAmount);
	  					  AdminUser adminUser = adminUserService.findByUsername("kjt");
	  					  operateLogService.saveParcelsLog(adminUser.getId(), adminUser.getUsername(), "127.0.0.1", "kjt跨境通包裹号："+parcel.getParcelCode()+"同步至跨境通成功，返回跨境通订单号:"+soSysNo+",运费："+shippingAmount);
	  					  parcelsService.saveParcels(parcel);
	  				  }
	  				  
	  				  System.out.println(resultJson);
	  				  Logger.info("订单返回信息："+merchantOrderID+":"+soSysNo+":"+productAmount+":"+taxAmount+":"+shippingAmount);
	  			  }
              }
         } catch (Exception e) {
              e.printStackTrace();
         }
         return resultJson;
    }
   
    /**
     * 
     * <p>Title: soOutputCustoms</p> 
     * <p>Description: 跨境通通知分销渠道订单已出罐区，商户提供API</p>
     * 1.6.2 
     * @param merchantOrderID
     * @param status
     * @param shipTypeID
     * @param trackingNumber
     * @param commitTime
     * @param message
     * @return
     */
    public boolean soOutputCustoms(String merchantOrderID, String status,
			String shipTypeID, String trackingNumber, String commitTime,
			String message) {
    	boolean flag = true;
    	Parcels parcels = parcelsService.queryParcelsByParcelCode(merchantOrderID);
    	if(parcels!=null){
	    	AdminUser adminUser = adminUserService.findByUsername("kjt");
	    	//出关 1 成功 	-1  失败
	    	if("1".equals(status)){
	    		ParcelsForm form = new ParcelsForm();
	    		if("1".equals(shipTypeID)){
	    		//确认快递公司，1顺丰 2 圆通 93 申通快递(27 测试用) 84 如风达(25测试用)
	    			form.kdCompany="23";
	    		}else if("2".equals(shipTypeID)){
	    			form.kdCompany="24";
	    		}else if("93".equals(shipTypeID)){
	    			form.kdCompany="78";
	    		}else if("84".equals(shipTypeID)){
	    			form.kdCompany="79";
	    		}
				form.nsort=0;
				form.parId = parcels.getId();
				form.waybillCode=trackingNumber;
				ParcelsWaybill pwb = parcelsWaybillService.saveWithWaybillForm(form);
				parcels.setMailnum(trackingNumber);
				parcels.setStatus(11);
				parcelsService.saveParcels(parcels);
				operateLogService.saveWaybillLog(adminUser.getId(), adminUser.getUsername(), "127.0.0.1", "kjt通知分销渠道订单出区成功，物流运单ID:"+pwb.getId()+",物流单号："+trackingNumber);
	    	}else if("-1".equals(status)){
	    		//出区失败，记录
	    		logger.info("[kjt]订单出区失败，包裹号:"+merchantOrderID+",失败原因："+message);
	    		operateLogService.saveWaybillLog(adminUser.getId(), adminUser.getUsername(), "127.0.0.1", "kjt通知分销渠道订单出区失败，包裹号:"+merchantOrderID+",失败原因："+message);
	    	}
    	}else{
    		flag = false;
    	}
		return flag;
	}
    
    /**
	 * 
	 * <p>Title: soTrace</p> 
	 * <p>Description: 订单状态追踪</p> 
	 * 1.6.3
	 * @param split
	 * @return
	 */
	public boolean soTrace(List<Parcels> parcelses) {
		boolean flag = true;
		List<String> strs = new ArrayList<String>();
		//20个为一组查询
		for(int i = 0; i < parcelses.size(); i++){
			strs.add(parcelses.get(i).getBbtid());
			if(strs.size()%20==0||((i==parcelses.size()-1)&&strs.size()%20!=0)){
				flag = doSoTrace(strs);
				strs = new ArrayList<String>();
			}
		}
		return flag;
	}
	
	/**
	 * 
	 * <p>Title: doSoTrace</p> 
	 * <p>Description: 订单状态追踪</p>
	 * 1.6.3 
	 * @param strs
	 * @return
	 */
	private boolean doSoTrace(List<String> strs) {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("SaleChannelSysNo", saleChannelSysNo);
		params.put("OrderIds", strs.toArray());
		String method = "order.sotrace";
		boolean flag = true;
		JsonNode resultJson = Json.newObject();
		try {
			KjtApiClient kjtApiClient = new KjtApiClient();
			HttpResponse response = kjtApiClient.get(method, params);
			//System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"utf-8"));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			logger.info("[kjt] doSoTrace:"+result.toString());
			resultJson = Json.parse(result.toString());
			JsonNode jsonData = resultJson.get("Data");
			if(jsonData!=null){
				JsonNode orderList = jsonData.withArray("TraceOrderList");
				for(int i = 0; i < orderList.size(); i++){
					logger.info("跨境通订单状态"+orderList.get(i).get("SOID").asText()+"入库完成，状态："+orderList.get(i).get("SOStatus").asText());
					long soId = orderList.get(i).get("SOID").asLong();
					int soStatus = orderList.get(i).get("SOStatus").asInt();
					dealPardelsWithSoStatus(soId,soStatus);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());
			flag = false;
		}
		return flag;
	}
	/**
	 * 
	 * <p>Title: dealPardelsWithSoStatus</p> 
	 * <p>Description: 处理包裹状态，并生成相应的waybill信息</p> 
	 * @param soId
	 * @param soStatus
	 */
	private void dealPardelsWithSoStatus(long soId, int soStatus) {
		ParcelsForm form = new ParcelsForm();
		Parcels parcels = parcelsService.queryParcelsByBbtId(soId);
		soStatus = 4;
		if(soStatus==4){
			//已出库待申报
			form.remark = "您的订单已出库，待申报海关~ ";
		}else if(soStatus==41){
			//包裹申报待通关
			form.remark = "您的订单已向海关申报完毕，即将完成通关，请耐心等待哦~";
		}
		//判断该包裹的物流信息是否已包含，若无，则可添加
		if(!Strings.isNullOrEmpty(form.remark)){
			List<ParcelsWaybill> waybills = parcelsWaybillService.getWaybillInfoWithParcelsIdAndRemark(parcels.getId(),form.remark);
			if(waybills==null||(waybills!=null&&waybills.size()==0)){
				form.nsort=0;
				form.parId = parcels.getId();
				AdminUser adminUser = adminUserService.findByUsername("kjt");
				ParcelsWaybill pwb = parcelsWaybillService.saveWithWaybillForm(form);
				operateLogService.saveWaybillLog(adminUser.getId(), adminUser.getUsername(), "127.0.0.1", "系统自动新增跨境通物流运单，物流运单ID:"+pwb.getId());
			}
		}
	}
	/**
	 * 
	 * <p>Title: soVoid</p> 
	 * <p>Description: 分销渠道订单作废</p> 
	 * 1.6.4
	 * @param objects
	 * @return
	 */
	public boolean soVoid(String[] objects) {
		JsonNode resultJson = Json.newObject();
		String method = "order.sovoid";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("OrderIds", objects);
		params.put("SaleChannelSysNo", saleChannelSysNo);
		boolean flag = true;
		try {
			KjtApiClient kjtApiClient = new KjtApiClient();
			HttpResponse response = kjtApiClient.get(method, params);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"utf-8"));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			System.out.println(result.toString());
			resultJson = Json.parse(result.toString());
			JsonNode jsonData = resultJson.get("Data");
			if(jsonData!=null){
				JsonNode itemList = jsonData.withArray("ItemList");
				System.out.println(resultJson);
				for(int i = 0; i < itemList.size(); i++){
					logger.info("跨境通商品："+itemList.get(i).get("ProductID").asText()+"，可销售库存："+itemList.get(i).get("OnlineQty").asInt()+",出库仓库编号："+itemList.get(i).get("WareHouseID").asInt());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());
			return false;
		}
		return flag;
	}
	
	/**
	 * 
	 * <p>Title: fepBillPost</p> 
	 * <p>Description: 待购汇账单接收接口</p> 
	 * 1.7.1
	 * @param objects
	 * @return
	 */
	public boolean fepBillPost(List<Parcels> parcelses) {
		JsonNode resultJson = Json.newObject();
		String method = "invoice.fepbillpost";
		List bbtIds = new ArrayList();
		for (Parcels parcels : parcelses) {
			bbtIds.add(parcels.getBbtid());
		}
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("OrderIds", bbtIds.toArray());
		params.put("SalesChannelCode", saleChannelSysNo);
		boolean flag = true;
		try {
			KjtApiClient kjtApiClient = new KjtApiClient();
			HttpResponse response = kjtApiClient.get(method, params);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"utf-8"));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			System.out.println(result.toString());
			resultJson = Json.parse(result.toString());
			JsonNode jsonData = resultJson.get("Data");
			if(jsonData!=null){
				logger.info("跨境通待购汇订单编号："+jsonData.get("FEPBillId").asText()+"，渠道结算金额："+jsonData.get("PurchasingTotalAmount").asDouble());
				//记录本次的待购汇订单号和结算金额
				int fepBillId = jsonData.get("FEPBillId").asInt();
				double purchasingTotalAmount = jsonData.get("PurchasingTotalAmount").asDouble();
				FepBillInfo fbi = new FepBillInfo();
				fbi.setDate_add(new Date());
				fbi.setFepBillId(fepBillId);
				fbi.setOrderIds(StringUtil.getStringFromList(bbtIds));
				fbi.setPurchasingTotalAmount(purchasingTotalAmount);
				fepBillInfoRepository.save(fbi);
				//更新记录本次的订单编号集合
				AdminUser adminUser = adminUserService.findByUsername("kjt");
				for(Parcels parcels : parcelses){
					parcels.setCheckStatus(1);		//将店铺结算状态从未计算->申请结算
					parcelsService.saveParcels(parcels);
					operateLogService.saveParcelsLog(adminUser.getId(), adminUser.getUsername(), "127.0.0.1", "跨境通包裹id:"+parcels.getId()+",包裹号："+parcels.getParcelCode()+",更改结算状态待申请 为 已申请待结算！");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());
			return false;
		}
		return flag;
	}
	
    
    /**
     * 
     * <p>Title: dealSOItemInfo</p> 
     * <p>Description: 订单中购买商品列表</p> 
     * @param parcel
     * @return
     */
    private List<SOItemInfo> dealSOItemInfo(Parcels parcel, List<SOItemInfo> soItemInfos) {
    	List<ParcelsPro> parcelsPros = parcelsProService.getAllByParcelsId(parcel.getId());		//商品信息列表
    	salePriceTotal = 0.0;
    	taxPriceTotal = 0.0;
    	for (ParcelsPro parcelsPro : parcelsPros) {
			SOItemInfo soItemInfo = new SOItemInfo();
			Product product = productService.findProductById(parcelsPro.getpId());
			ProductUnion pu = productService.findProductUnionByPid(parcelsPro.getpId());
			soItemInfo.productId = product.getExtcode();		//商品ID
			soItemInfo.quantity = parcelsPro.getCounts();										//购买数量
			soItemInfo.salePrice = pu.getTaxPrice();											//商品在分销渠道下单时的商品金额，需满足Sum(SalePrice*Quantity)=PayInfo.ProductAmount
			soItemInfo.taxPrice = pu.getTaxRate();												//商品在分销渠道下单时的商品税费，保留2位小数，需要满足Sum(TaxPrice * Quantity) = PayInfo.TaxAmount
			salePriceTotal += pu.getTaxPrice()*parcelsPro.getCounts();
			taxPriceTotal += pu.getTaxRate()*parcelsPro.getCounts();
			soItemInfos.add(soItemInfo);
    	}
		return soItemInfos;
	}
	/**
     * 
     * <p>Title: dealSOAuthenticationInfo</p> 
     * <p>Description: 下单用户实名认证信息, 可选</p> 
     * @param parcel
     * @return
     */
    private SOAuthenticationInfo dealSOAuthenticationInfo(Parcels parcel) {
    	ShoppingOrder shoppingOrder = orderService.queryShoppingOrderById(parcel.getOrderId());
    	SOAuthenticationInfo soAuthenticationInfo = new SOAuthenticationInfo();
    	soAuthenticationInfo.name=shoppingOrder.getName();							//下单用户真实姓名
    	soAuthenticationInfo.idCardType=0;							//下单用户证件类型
    	soAuthenticationInfo.idCardNumber=shoppingOrder.getCardId();		//下单用户证件编号
    	soAuthenticationInfo.phoneNumber = shoppingOrder.getPhone();			//下单用户联系电话
    	soAuthenticationInfo.email = "lvcheng@neolix.com";				//下单用户电子邮箱
    	//soAuthenticationInfo.address =shoppingOrder.getProvince()+shoppingOrder.getAddress();						//下单用户联系地址
		return soAuthenticationInfo;
	}
	/**
    *
    * <p>Title: dealSoShippingInfo</p>
    * <p>Description: 订单配送信息</p>
    * @param parcel
    * @return
    */
    private SOShippingInfo dealSoShippingInfo(Parcels parcel) {
         SOShippingInfo soShippingInfo = new SOShippingInfo();				
         soShippingInfo.receiveName = parcel.getName();							//收件人姓名
         soShippingInfo.receivePhone = parcel.getPhone();						//收件人电话
         soShippingInfo.receiveAddress = parcel.getAddress();					//收件人收货地址
         soShippingInfo.receiveAreaCode = getAreaCode(parcel.getProvince());	//收件人地区编号
         soShippingInfo.receiveAreaName = getAreaName(parcel.getProvince());//parcel.getProvince();//需要进一步处理	收件省市区名称，分隔，如上海，上海市，静安区
         soShippingInfo.shipTypeID = "2";//需要进一步处理	订单物流运输公司编号 ,默认为圆通
         return soShippingInfo;
    }
    /**
    *
    * <p>Title: dealSoPayInfo</p>
    * <p>Description: 封装订单支付信息</p>
    * @param parcel
    * @return
    */
    private SOPayInfo dealSoPayInfo(Parcels parcel,ShoppingOrderPay sopay,ShoppingOrder so) {
    	SOPayInfo soPayInfo = new SOPayInfo();
    	soPayInfo.productAmount = salePriceTotal;	//商品总金额
    	soPayInfo.shippingAmount = so.getForeignfee();				//运费总金额
    	soPayInfo.taxAmount = taxPriceTotal;		//商品行邮税总金额
    	soPayInfo.commissionAmount = 0.0;			//下单支付产生的手续费
    	if(sopay!=null){
    		String paymethod = sopay.getPaymethod();
    		if("10".equals(paymethod)||"11".equals(paymethod)){
    			soPayInfo.payTypeSysNo = 118;				//支付方式处理	111：东方支付  112：支付宝  114：财付通  117：银联支付   118：微信支付
    		}else if("20".equals(paymethod)||"21".equals(paymethod)||"22".equals(paymethod)){
    			soPayInfo.payTypeSysNo = 112;
    		}
    		soPayInfo.paySerialNumber = sopay.getTradeno();	//支付流水号
    	}
        return soPayInfo;
    }
	
	/**
	 * 传入包裹里的省市区获取对应的地区编码（与ERP地址类似）
	 * @param province
	 * @return
	 */
	public String getAreaCode(String province) {
		String result = "";
		try {
			String[] str = dealParcelsWithProvince(province);
			result = str[str.length-1].split("-")[2];
		} catch (Exception e) {
			logger.info(e.toString()+province);
		}
		return result;
	}
	
	/**
	 * 获取上收货省市区
	 * <p>Title: getAreaName</p> 
	 * <p>Description: </p> 
	 * @param province
	 * @return
	 */
	public String getAreaName(String province) {
		String result = "";
		try {
			String[] str = dealParcelsWithProvince(province);
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < str.length; i++) {
				sb.append(str[i].split("-")[1]);
				if(i!=str.length-1){
					sb.append(",");
				}
			}
			result = sb.toString();
		} catch (Exception e) {
			logger.info(e.toString()+province);
		}
		return result;
	}
	
	/**
	 * 将包裹里的省市区转成数组
	 * @param province
	 * @return
	 */
	public String[]  dealParcelsWithProvince(String province) {

		String[] str = new String[3];
		int tempNum = 0; 
		while(true){
			str = dealParcelsWithProvinceNext(str, province,tempNum);
			if(str.length>tempNum && str[tempNum]!=null && str[tempNum].split("-").length>1){
				province = province.replace(str[tempNum].split("-")[1], "");
			}
			tempNum++;
			if(tempNum>=3){
				break;
			}
		}
		//针对异常数据进行置空处理
		List<String> resultList = new ArrayList<>();
		for(String strTemp : str){
			if(!StringUtils.isBlank(strTemp)){
				resultList.add(strTemp);
			}
		}
		String[] param = new String[resultList.size()];
		return resultList.toArray(param);
	
	}

	private String[] dealParcelsWithProvinceNext(String[] strs, String province,int num){
		
		if(StringUtils.isBlank(province)){
			List<String> resultList = new ArrayList<>();
			for(String str:strs){
				if(!StringUtils.isBlank(str)){
					resultList.add(str);
				}
			}
			String[] param = new String[resultList.size()];
			return resultList.toArray(param);
		}
		List<ChinaAddress> addresses = getChinaAddressWithProvince(province);
		if(addresses!=null&&addresses.size()>0){
			for (ChinaAddress chinaAddress : addresses) {
				if(num==0){
					strs[num]=chinaAddress.getId()+"-"+chinaAddress.getName()+"-"+chinaAddress.getCode();
				}
				if(num==1){
					int tier = chinaAddress.getTier();
					if(tier==3){
						ChinaAddress chinaAddressCity = new ChinaAddress();
						ChinaAddress chinaAddressTemp = getChinaAddressById(chinaAddress.getParentid().toString());
						if("市辖区".equals(chinaAddressTemp.getName())||"县".equals(chinaAddressTemp.getName())){//此处与ERP处理的逻辑有不同，存在市辖区的概念，像直辖市他们的市辖区是区的parent
							chinaAddressCity = getChinaAddressById(chinaAddressTemp.getParentid().toString());
						}
						String[] tempStr = strs[num-1].split("-");
						if(tempStr[0].equals(String.valueOf(chinaAddressCity.getId()))){
							strs[num]=chinaAddressTemp.getId()+"-"+chinaAddressTemp.getName()+"-"+chinaAddressTemp.getCode();
							strs[num+1]=chinaAddress.getId()+"-"+chinaAddress.getName()+"-"+chinaAddress.getCode();
						}
					}else{
						String[] tempStr = strs[num-1].split("-");
						if(tempStr[0].equals(String.valueOf(chinaAddress.getParentid()))){
							strs[num]=chinaAddress.getId()+"-"+chinaAddress.getName()+"-"+chinaAddress.getCode();
						}
					}
				}
				if(num==2){
					String[] tempStr = strs[num-1].split("-");
					if("市辖区".equals(tempStr[1])){//此处与ERP处理的逻辑有不同，存在市辖区的概念，像直辖市他们的市辖区是区的parent
						chinaAddress = getChinaAddressById(tempStr[0]);
					}
					if(tempStr[0].equals(String.valueOf(chinaAddress.getParentid()))){
						strs[num]=chinaAddress.getId()+"-"+chinaAddress.getName()+"-"+chinaAddress.getCode();
					}
				}
			}
		}
		return strs;
	}

	/**
	 * 根据城市名获取所有包含类似的邮编地址的集合
	 * @param province
	 * @return
	 */
	public List<ChinaAddress> getChinaAddressWithProvince(String province) {
		return chinaAddressRepository.getErpAddressWithProvince(province);
	}
	/**
	 * 根据ID获取带邮编的地址
	 * @param id
	 * @return
	 */
	public ChinaAddress getChinaAddressById(String id) {
		return chinaAddressRepository.findOne(Numbers.parseInt(id, 0));
	}
	
	/**
	 * 
	 * <p>Title: doJobWithSoCreateErr</p> 
	 * <p>Description: 每天凌晨两点将之前同步至跨境通失败的包裹重新发送，直到成功</p>
	 */
	public void doJobWithSoCreateErr() {
		List<AutoSyncTaskLog> autoSyncTaskLogs = autoSyncTaskLogService.getAllWithOperType("SoCreateErr","kjterr");
		if(autoSyncTaskLogs!=null&&autoSyncTaskLogs.size()>0){
			for (AutoSyncTaskLog autoSyncTaskLog : autoSyncTaskLogs) {
				String id = autoSyncTaskLog.getRecord();
				Parcels parcel = parcelsService.queryParcelsById(Numbers.parseLong(id, 0L));
				JsonNode result = soCreate(parcel);
				logger.info("凌晨2点，再次同步包裹至KJT完成日志："+autoSyncTaskLog.getRecord()+"，当前时间为："+Dates.formatDateTime(new Date()));
			}
		}
	}
	
	public static void main(String args[]) throws ParseException{
		String code = KjtService.getInstance().getAreaCode("北京市朝阳区");
		System.out.println(code);
		
		String d1 = "2015-09-16 11:10:03";
		String d2 = "2015-09-20 11:10:03";
		//KjtService.getInstance().productIdGetQuery(d1,d2,0,1);
		String [] str = new String[4];
		/*str[0]="291BRE338550008";
		str[1]="295JPB014560001";
		str[2]="295JPB014560002";
		str[3]="305AUA036000003";*/
		str[3]="305BEA036000004";
		str[0]="314ARD024560001";
		str[1]="314ARD024560002";
		str[2]="314ARD024560003";
		//KjtService.getInstance().productInfoBatchGet(str);
		//KjtService.getInstance().productPriceChangeIdQuery(new Date(),new Date(),1,1);
		//KjtService.getInstance().productPriceBatchGet(str);
//		KjtService.getInstance().delistingProduct(6807080,"");
//		KjtService.getInstance().listingProduct(6807080,"");
//		KjtService.getInstance().updateSKU(6807080,6807080,3,"","");
		/*KjtService.getInstance().getProductByOut("num_iid,title,price,desc,skus,item_imgs","G1230233");
		KjtService.getInstance().getCategories();
		KjtService.getInstance().getCategoriesPromotions();
		KjtService.getInstance().getCategoriesTags();
		KjtService.getInstance().getProductsOnsale("num_iid,title,price,desc,skus,item_imgs", "", 0, 1, 20, "created:asc");
		KjtService.getInstance().getCategoriesTagsByPage(2,2,"created:asc");
		KjtService.getInstance().getProductsInventory("num_iid,title,price,desc,skus,item_imgs", "", "",0, 1, 20, "created:asc");
		KjtService.getInstance().getSkusCustom("num_iid,title,item_imgs","",6807080);*/
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
//		KjtService.getInstance().productAdd(cid, promotion_cid, tag_ids, price, title, desc, is_virtual, post_fee, sku_properties, sku_quantities, sku_prices, sku_outer_ids, skus_with_json, origin_price, buy_url, outer_id, buy_quota, quantity, hide_quantity, fields, fileKey, filePaths);
	}
	
	
}
