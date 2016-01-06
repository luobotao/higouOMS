package utils.kjt;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import play.Logger;
import play.libs.Json;

public class KjtApiClient {
	private static final String Version = "2.0";		//接口版本

    private static final String apiEntry = "http://preapi.kjt.com/open.api?";	//kjt 程序接口

    private static final String format = "json";		//接口返回结果类型
    public final static String APPID = "seller325";		//api身份标识符
	public final static String APPSECRET = "kjt@325";	//api验签密钥

    /**
     * 
     * <p>Title: get</p> 
     * <p>Description: get方式请求api</p> 
     * @param method	由接口提供方指定的接口标识符
     * @param parames	data参数（需转成json string)
     * @return
     * @throws Exception
     */
    public HttpResponse get(String method, HashMap<String,Object> parames) throws Exception{
        //拼接指定url
    	String url = apiEntry + getParamStr(method, parames);
        System.out.println("===>>method:"+method+"url:"+url);
        Logger.info("url=============>>"+url);
        HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
 
		HttpResponse response = client.execute(request);
		 
		return response;
    }
    /**
     * 
     * <p>Title: post</p> 
     * <p>Description: post方式请求api</p> 
     * @param method	由接口提供方指定的接口标识符
     * @param parames	data参数（需转成json string)
     * @return
     * @throws Exception
     */
    public HttpResponse post(String method, HashMap<String, Object> parames) throws Exception{
    	String url = apiEntry + getParamStr(method, parames);
    	
    	HttpClient client = new DefaultHttpClient();
    	HttpPost httppost = new HttpPost(url);
    	
        System.out.println("executing request " + httppost.getRequestLine());
        HttpResponse response = client.execute(httppost);
        
        return response; 
    }
    
    /**
     * 
     * <p>Title: getParamStr</p> 
     * <p>Description: 处理参数，替换特殊字符，并进行utf-8转码</p> 
     * @param method
     * @param parames
     * @return
     */
    public String getParamStr(String method, HashMap<String, Object> parames){
        String str = "";
        try {
            str = URLEncoder.encode(buildParamStr(buildCompleteParams(method, parames)), "UTF-8")
                    .replace("%3A", ":")
                    .replace("%2F", "/")
                    .replace("%26", "&")
                    .replace("%3D", "=")
                    .replace("%3F", "?");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }
    
    /**
     * 
     * <p>Title: buildParamStr</p> 
     * <p>Description: 组合参数，添加密钥等信息</p> 
     * @param param
     * @return
     */
    private String buildParamStr(HashMap<String, String> param){
        String paramStr = "";
        Object[] keyArray = param.keySet().toArray();
        for(int i = 0; i < keyArray.length; i++){
            String key = (String)keyArray[i];

            if(0 == i){
                paramStr += (key + "=" + param.get(key));
            }
            else{
                paramStr += ("&" + key + "=" + param.get(key));
            }
        }
        Logger.info("post info:"+paramStr);
        return paramStr;
    }

    /**
     * 
     * <p>Title: buildCompleteParams</p> 
     * <p>Description: </p> 
     * @param method	
     * @param parames	记录data传递的内容，转成json
     * @return
     * @throws Exception
     */
    private HashMap<String, String> buildCompleteParams(String method, HashMap<String, Object> parames) throws Exception{
        //获取指定的common 参数集合
    	HashMap<String, String> commonParams = getCommonParams();
        System.out.println(Json.toJson(parames).toString());
        //接口标识符
        commonParams.put(KjtApiProtocol.METHOD_KEY, method);
        //参数实体信息，json string
        commonParams.put("data", Json.toJson(parames).toString());
        //MD5生成密钥
        commonParams.put(KjtApiProtocol.SIGN_KEY, KjtApiProtocol.sign(APPSECRET, commonParams));
        return commonParams;
    }

    /**
     * 
     * <p>Title: getCommonParams</p> 
     * <p>Description: 获取指定的common集合</p> 
     * @param method
     * @return
     */
    private HashMap<String, String> getCommonParams(){
	    HashMap<String, String> parames = new HashMap<String, String>();
	    parames.put(KjtApiProtocol.VERSION_KEY, Version);		//接口版本
	    parames.put(KjtApiProtocol.APP_ID_KEY, APPID);			//身份标识符
	    parames.put(KjtApiProtocol.FORMAT_KEY, format);			//返回结果类型
	    parames.put(KjtApiProtocol.TIMESTAMP_KEY, new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));	//时间戳，格式yyyyMMddHHmmss
	    Random random = new Random();
		int x = random.nextInt(899999);
		x = x+100000;
	    parames.put(KjtApiProtocol.NONCE_KEY, x+"");			//随机数，在约定的调用时差范围内不可重复
	    return parames;
    }
}
