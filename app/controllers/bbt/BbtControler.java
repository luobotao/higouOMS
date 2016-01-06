package controllers.bbt;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import controllers.admin.BaseAdminController;
import models.admin.AdminUser;
import models.endorsement.Endorsement;
import models.product.PadChannel;
import models.product.PadChannelPro;
import models.user.User;
import play.Configuration;
import play.Logger;
import play.Play;
import play.libs.Akka;
import play.libs.Json;
import play.mvc.Result;
import scala.concurrent.duration.Duration;
import services.admin.AdminUserService;
import services.endorsement.EndorsementService;
import services.product.ChannelService;
import services.user.UserService;
import services.weixin.WeixinService;
import utils.AjaxHelper;
import utils.BeanUtils;
import utils.MatrixToImageWriter;
import utils.Numbers;
import utils.OSSUtils;
import utils.StringUtil;
import utils.WSUtils;
/*
 * 棒棒糖同步接口使用
 */
@Named
@Singleton
public class BbtControler extends BaseAdminController {
	private static final Logger.ALogger logger = Logger.of(BbtControler.class);
	SimpleDateFormat CHINESE_DATE_TIME_FORMAT_NORMAL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final WeixinService weixinService;
	private final UserService userService;
	private final AdminUserService adminUserService;	
	private final EndorsementService endorsementService;
    private final ChannelService channelService;
	@Inject
	public BbtControler(final WeixinService weixinService,final UserService userService,final AdminUserService adminUserService,final EndorsementService endorsementService,final ChannelService channelService){
		this.weixinService = weixinService;
		this.userService = userService;
		this.adminUserService = adminUserService;
		this.endorsementService = endorsementService;
		this.channelService = channelService;
	}
	/*
	 * 快递员返利接口
	 */
	public Result syncBbtMoney(){
		ObjectNode result=Json.newObject();
		result.put("status", "0");
		result.put("msg", "同步失败");
		Long postman_id=Numbers.parseLong(AjaxHelper.getHttpParam(request(), "postman_id"),0L);
		Integer amount=Numbers.parseInt(AjaxHelper.getHttpParam(request(), "amount"),0);
		String desc=AjaxHelper.getHttpParam(request(), "desc");
		String out_trade_no=AjaxHelper.getHttpParam(request(), "out_trade_no");
		String sub_catalog=AjaxHelper.getHttpParam(request(), "sub_catalog");//收支的描述，比如配送费用，客户表扬，违规扣款
		int catalog=Numbers.parseInt(AjaxHelper.getHttpParam(request(), "catalog"), 2);//科目（1-提现，2-奖励，3-惩罚，4-收入）
		if(postman_id.longValue()==0 || amount.intValue()==0 || StringUtils.isBlank(out_trade_no))
			result.put("msg","参数错误");
		desc=StringUtils.isBlank(desc)?"":desc;
		Map<String,String> getmap=new HashMap<String,String>();
		getmap.put("out_trade_no",out_trade_no);
		String timstr= CHINESE_DATE_TIME_FORMAT_NORMAL.format(new Date());
		getmap.put("time_stamp", timstr);
		String signstr=StringUtil.makeSig(getmap);
		
		boolean IsProduct = Play.application().configuration().getBoolean("production", false);
		
		ObjectNode re=Json.newObject();
		re.put("sign", signstr);
		re.put("time_stamp", timstr);
		re.put("postman_id",postman_id.longValue()+"");
		re.put("amount",amount.intValue()+"");
		re.put("desc", desc);
		re.put("out_trade_no",out_trade_no);
		re.put("catalog",catalog);
		re.put("sub_catalog",sub_catalog);
		String gettokenurl="http://182.92.227.140:9105/open/income";
		if(IsProduct)
			gettokenurl="http://bbtapi.ibbt.com/open/income";
		logger.info(Json.stringify(re));
		JsonNode jsonnode = WSUtils.postByJSON(gettokenurl, re);
		if(jsonnode!=null){
			String err=jsonnode.get("result")==null?"":jsonnode.get("result").textValue();
			if(err.equals("ok")){
				result.put("status", "1");
				result.put("msg", "");
			}else{
				result.put("msg", (jsonnode.get("code")==null?0:jsonnode.get("code").asInt())+(jsonnode.get("message")==null?"":jsonnode.get("message").textValue()));
			}
		}
		return ok(Json.toJson(result));
	}
	
	
	/**
	 * 棒棒糖申请开通商城
	 * @return
	 */
	public Result expressMall(){
		response().setContentType("application/json;charset=utf-8");
		ObjectNode result = Json.newObject();
		JsonNode requestJson = request().body().asJson();
		String postman_id ="";//快递员ID
		String phone = "";//手机
    	String nickname = "";//昵称
    	String timestamp = "";//时间戳
    	String sign = "";//加密
		if(requestJson!=null){
			postman_id = requestJson.get("postman_id")==null?"":requestJson.get("postman_id").asText();
			phone = requestJson.get("phone")==null?"":requestJson.get("phone").asText();
			nickname = requestJson.get("nickname")==null?"":requestJson.get("nickname").asText();
			timestamp = requestJson.get("timestamp")==null?"":requestJson.get("timestamp").asText();
			sign = requestJson.get("sign")==null?"":requestJson.get("sign").asText();
		}
		if(StringUtils.isBlank(postman_id)||StringUtils.isBlank(phone)||StringUtils.isBlank(nickname)||StringUtils.isBlank(timestamp)||StringUtils.isBlank(sign)){
			result.put("result", "error");
			result.put("errorCode", "51000");
			result.put("message", "参数格式错误");
			return ok(Json.toJson(result));
		}else{
			// 设置签名参数
			SortedMap<String, String> signParams = new TreeMap<String, String>();
			signParams.put("phone", phone);
			signParams.put("postman_id", postman_id);
			signParams.put("timestamp", timestamp);
			String signMine=StringUtil.makeSig(signParams);
			logger.info(signMine+"根据棒棒糖传入的参数项目自己生成的sign");
			if(!sign.equals(signMine)){
				result.put("result", "error");
				result.put("errorCode", "51002");
				result.put("message", "签名错误");
				return ok(Json.toJson(result));
			}
	    	List<User> userList = userService.findByPostmanid(postman_id);
	    	User user = new User();
	    	if(!userList.isEmpty()){
	    		user = userList.get(0);
	    		String ticket =user.getWxticket();
	    		if(StringUtils.isBlank(ticket)){
	    			ticket = weixinService.getQrCodeTicket(String.valueOf(user.getUid()));
	    			user.setWxticket(ticket);
	    			user.setNickname(nickname);
	    	    	user = userService.saveUser(user);
	    		}
				result.put("result", "ok");
				result.put("qrcode_ticket", ticket);
				result.put("mall_url", StringUtil.getH5Domain()+"/mall/mallProducts/"+user.getUid());
				return ok(Json.toJson(result));
	    	}
	    	userList = userService.findByPhone(phone);
	    	
	    	if(!userList.isEmpty()){
	    		user = userList.get(0);
	    		user.setGid(6L);//1普通用户组2新用户组3旧用户组4商户组 
	    		user.setActive(1);
	    		user.setState(1);//启用
	    		user.setNickname(nickname);
	    		user.setDateUpd(new Date());
	        	user.setIsEndorsement(1);//可代言
	    	}else{
	    		user.setGid(6L);//1普通用户组2新用户组3旧用户组4商户组
	    		user.setPasswords(StringUtil.getMD5("123456"));
	    		user.setActive(1);
	        	user.setIsEndorsement(1);//可代言
	        	user.setDate_add(new Date());
	        	user.setPhone(phone);
		    	user.setNickname(nickname);
		    	user.setDateUpd(new Date());
		    	user.setState(1);//启用
	    	}
	    	user.setPostmanid(postman_id);
	    	user = userService.saveUser(user);
	    	user.setWxticket(weixinService.getQrCodeTicket(String.valueOf(user.getUid())));
	    	user = userService.saveUser(user);
			result.put("result", "ok");
			result.put("qrcode_ticket", user.getWxticket());
			result.put("mall_url", StringUtil.getH5Domain()+"/mall/mallProducts/"+user.getUid());
        	
        	final User userFinal = user; 
        	Akka.system().scheduler().scheduleOnce(
        	        Duration.create(10, TimeUnit.MILLISECONDS),
        	        new Runnable() {
        	            public void run() {
        	            	AdminUser adminUser = adminUserService.findByUsername(userFinal.getPhone());
        	            	adminUser.setUsername(userFinal.getPhone());
        	            	adminUser.setPasswd(StringUtil.getMD5("123456"));
        	            	adminUser.setRealname(userFinal.getNickname());
        	            	adminUser.setActive(1);
        	            	adminUser.setDate_add(new Date());
        	            	adminUser.setLastip("");
        	            	adminUser.setDate_login(new Date());
        	            	adminUser.setLogin_count(0);
        	            	adminUser.setHeadIcon("/pimgs/comment/tximage/headIcon.png");
        	            	adminUser.setAdminType("3");
        	            	adminUser.setUid(userFinal.getUid());
        	            	adminUser.setRoleId(0);
        	            	adminUserService.updateAdminUser(adminUser);
        	            	endorsementService.delteEndorsementInfoByUserId(userFinal.getUid());
        	            	String uidDefault = StringUtil.getConfigUid();
        	            	logger.info(uidDefault);
        	            	//获取默认商城的频道列表
        	            	List<PadChannel> padChannelList = channelService.findPadchannelByUserid(Numbers.parseLong(uidDefault, 0L));
        	            	for(PadChannel padChannelTemp:padChannelList){
        	            		PadChannel padChannel = new PadChannel();
        						padChannel.setSta(padChannelTemp.getSta());// 默认显示
        						padChannel.setUserid(userFinal.getUid());
        						padChannel.setDate_add(new Date());
        						padChannel.setCname(padChannelTemp.getCname());
        						padChannel.setNsort(padChannelTemp.getNsort());
        						padChannel = channelService.savePadChannel(padChannel);
        						//根据频道ID获取默认的此频道下对应的商品（目的是获取PID）
        						List<PadChannelPro> padChannelProList = channelService.findPadChannelProListByCid(padChannelTemp.getId());
        						for(PadChannelPro padChannelPro:padChannelProList){
        							logger.info(padChannelPro.getPid()+"========默认商城的PAD=========PID=");
        							Endorsement endorsementTemp = endorsementService.findEndorsementById(padChannelPro.getEid());//默认商城的代言
        							Long eid=0L;
        							if(endorsementTemp!=null){
        								Endorsement endorsementNew = new Endorsement();//即将生成的代言
            							String ignorePro[] = {"eid","createTime","userId","dimensionalimg"};//不需要copy的属性
            							BeanUtils.copyProperties(endorsementTemp, endorsementNew,ignorePro );
            							endorsementNew.setCreateTime(new Date());
            							endorsementNew.setUserId(userFinal.getUid());
            							endorsementNew = endorsementService.saveOrUpdateEnorsement(endorsementNew);
            							//接下来处理生成的图片二维码
            							try {
            								String BUCKET_NAME = StringUtil.getBUCKET_NAME();
            								String exportPath = Configuration.root().getString("oss.upload.dimensional.image", "/");// 上传路径
            								String url = StringUtil.getAPIDomain() + "/sheSaid/show?uid=" + userFinal.getUid() + "&pid=" + padChannelPro.getPid()
            										+ "&daiyanid=" + endorsementNew.getEid() + "&wx=0";

            								MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            								Map hints = new HashMap();
            								hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            								BitMatrix bitMatrix = multiFormatWriter.encode(url, BarcodeFormat.QR_CODE, 400, 400, hints);
            								String fileNameLast = UUID.randomUUID().toString().replaceAll("-", "") + ".jpg";// 最终的文件名称
            								String tempPath = Configuration.root().getString("temp.path", "/");// 本地上传临时路径
            								File file1 = new File(tempPath, fileNameLast);
            								file1 = MatrixToImageWriter.writeToFile(bitMatrix, "jpg", file1);
            								String endfilestr = OSSUtils.uploadFile(file1, exportPath, fileNameLast, ".jpg",
            										BUCKET_NAME);
            								endorsementNew.setDimensionalimg(endfilestr);// 二维码路径
            								file1.delete();
            							} catch (Exception e) {
            								e.printStackTrace();
            							}
            							endorsementNew = endorsementService.saveOrUpdateEnorsement(endorsementNew);
            							eid = endorsementNew.getEid();
        							}
        							
        							//处理频道商品（频道商品里有EID所以才有上一步的获取代言）
        							// 根据eid与cid去padChannelPro表里检查是否已存在此商品
        							Long count = channelService.countPadChannelByEidAndCid(eid, padChannel.getId());
        							if (count == null || count.longValue() == 0) {
        								PadChannelPro padChannelProNew = new PadChannelPro();
        								padChannelProNew.setCid(padChannel.getId());
        								padChannelProNew.setEid(eid);
        								padChannelProNew.setDate_add(new Date());
        								padChannelProNew.setPid(padChannelPro.getPid());
        								padChannelProNew.setTyp(padChannelPro.getTyp());// 显示与否
        								padChannelProNew.setTypFlag(padChannelPro.getTypFlag());//0是商品1是banner
        								padChannelProNew.setNsort(padChannelPro.getNsort());
        								padChannelProNew.setImgurl(padChannelPro.getImgurl().replace(StringUtil.getPICDomain(), ""));
        								if("1".equals(padChannelProNew.getTypFlag())){
        									padChannelProNew.setLinkurl(StringUtil.getAPIDomain()+"/sheSaid/show?uid="+userFinal.getUid()+"&pid="+padChannelPro.getPid()+"&daiyanid="+eid+"&wx=0");
        								}
        								
        								padChannelProNew = channelService.savePadChannelPro(padChannelProNew);
        							}
        						}
        	            	}
//        	            	File file = new File("/data/defaultBbtPro.xls");
//        	    			endorsementService.importFile(file,userFinal.getUid(),userFinal.getGid());//新建商户时默认生成频道与频道商品
//        	    			File fileBanner = new File("/data/defaultBbtBanner.xls");
//        	    			endorsementService.importBannerFile(fileBanner,userFinal.getUid(),userFinal.getGid());//新建商户时默认生成频道与频道banner
        	            }
        	        },
        	        Akka.system().dispatcher()
        	);
		}
		return ok(Json.toJson(result));
		
	}
}
