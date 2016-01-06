package services.weixin;

import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.Logger;
import play.Play;
import play.libs.Json;
import services.ServiceFactory;
import utils.StringUtil;
import utils.WSUtils;

@Named
@Singleton
public class WeixinService extends Thread {
	private static final Logger.ALogger logger = Logger.of(WeixinService.class);
	// appid
	private static final String app_id = "wx99199cff15133f37";
	private static final String app_secret = "a017774f117bf0100a2f7939ef56c89a";
//	private static final String app_id = "wx0cef7e835f598e36";//测试
//	private static final String app_secret = "416704762a6b40c20025ea169bb00e61";//测试
	
	private static final String secretKeyWithBbt = "416704762a6b40c20025ea169bb00e61";//测试
	
	
	/**
	 * 获取TOKEN，一天最多获取200次，需要所有用户共享值
	 */
	public String getAccessToken() {
		String wxpay_access_token = ServiceFactory.getCacheService().get("wxpay_access_token");
		if(StringUtils.isBlank(wxpay_access_token)){
			wxpay_access_token = getTokenReal();
		}
		return wxpay_access_token;

	}
	
	public String getTokenReal(){
		String token = "";
		String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+app_id+"&secret="+app_secret;
		JsonNode result = WSUtils.getResponseAsJson(url);
		if(result!=null){
			token = result.get("access_token")==null?"":result.get("access_token").asText();
			ServiceFactory.getCacheService().set("wxpay_access_token",token);
		}
		return token;
	}

	/**
	 * 获取二维码ticket
	 * @param scene_id
	 * @return
	 */
	public String getQrCodeTicket(String scene_str){
		String ticket="";
		String url = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token="+getTokenReal();
		ObjectNode json = Json.newObject();
		json.put("action_name", "QR_LIMIT_STR_SCENE");//二维码类型，QR_SCENE为临时,QR_LIMIT_SCENE为永久,QR_LIMIT_STR_SCENE为永久的字符串参数值
		ObjectNode action_info = Json.newObject();//二维码详细信息
		ObjectNode scene = Json.newObject();//二维码详细信息
//		action_info.put("scene_id", scene_id);//场景值ID，临时二维码时为32位非0整型，永久二维码时最大值为100000（目前参数只支持1--100000）
		scene.put("scene_str", scene_str);//场景值ID（字符串形式的ID），字符串类型，长度限制为1到64，仅永久二维码支持此字段
		action_info.set("scene", scene);
		json.set("action_info", action_info);
		JsonNode result = WSUtils.postByJSON(url, json);
		if(result!=null){
			ticket = result.get("ticket")==null?"":result.get("ticket").asText();
			String urlResult = result.get("url")==null?"":result.get("url").asText();
		}
		return ticket;
	}

}
