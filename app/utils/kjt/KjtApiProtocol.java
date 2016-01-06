package utils.kjt;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import utils.StringUtil;

public class KjtApiProtocol {
	public static final String METHOD_KEY = "method";
	public static final String VERSION_KEY = "version";
	public static final String APP_ID_KEY = "appid";
	public static final String FORMAT_KEY = "format";
    public static final String TIMESTAMP_KEY = "timestamp";
    public static final String NONCE_KEY = "nonce";
    public static final String SECRET_KEY = "appsecret";
    public static final String DATA_KEY = "data";
    public static final String SIGN_KEY = "sign";

    /**
     * 
     * <p>Title: sign</p> 
     * <p>Description: 拼接生成密钥所需的参数信息</p> 
     * @param appSecret
     * @param parames
     * @return
     */
    public static String sign(String appSecret, HashMap<String,String> parames){
        Object[] keyArray = parames.keySet().toArray();
        List<String> keyList = new ArrayList<String>();
        for(int i = 0; i < keyArray.length; i++){
            keyList.add((String)keyArray[i]);
        }
        Collections.sort(keyList);				//按字典排序
        String signContent = "";
    	for(int i = 0; i < keyList.size(); i++){
             String key = (String)keyList.get(i);

             if(0 == i){
            	 signContent += (key + "=" + parames.get(key));
             }
             else{
            	 signContent += ("&" + key + "=" +parames.get(key));
             }
        }
        signContent = signContent +"&" + KjtApiProtocol.SECRET_KEY+"="+appSecret;		//添加验签密钥
        return StringUtil.getMD5(signContent.toLowerCase());
    }
    
   
}
