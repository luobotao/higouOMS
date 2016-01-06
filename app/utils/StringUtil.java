package utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

import org.apache.commons.codec.binary.Base64;

import play.Configuration;
import play.Logger;
import play.Play;

/**
 * String加密与解密
 * 
 * @author luobotao Date: 2015年4月14日 下午5:44:40
 */
public class StringUtil {
	private static final Logger.ALogger LOGGER = Logger.of(StringUtil.class);
	private static final byte[] KEY = "a9b8c7d6".getBytes();
	private static final DESKeySpec DKS;
	private static final SecretKeyFactory KEY_FACTORY;
	private static final Key SECRET_KEY;
	private static final IvParameterSpec IV = new IvParameterSpec(KEY);
	private static final String CIPHER_INSTANCE_NAME = "DES/CBC/PKCS5Padding";
	private static final Charset UTF_8;
	private final static String[] hexDigits = {
			"0", "1", "2", "3", "4", "5", "6", "7",
			"8", "9", "a", "b", "c", "d", "e", "f"};

	static {
		try {
			UTF_8 = Charset.forName("UTF-8");
			DKS = new DESKeySpec(KEY);
			KEY_FACTORY = SecretKeyFactory.getInstance("DES");
			SECRET_KEY = KEY_FACTORY.generateSecret(DKS);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String encode(String s) {
		try {
			Cipher cipherEncode;
			cipherEncode = Cipher.getInstance(CIPHER_INSTANCE_NAME);
			cipherEncode.init(Cipher.ENCRYPT_MODE, SECRET_KEY, IV);
			return Base64.encodeBase64String(cipherEncode.doFinal(s
					.getBytes(UTF_8)));
		} catch (IllegalBlockSizeException | BadPaddingException
				| InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | InvalidAlgorithmParameterException e) {
			LOGGER.error("salary codec encode " + s + " error.", e);
			return null;
		}
	}

	public static String decode(String str) {
		try {
			Cipher cipherDecode;
			cipherDecode = Cipher.getInstance(CIPHER_INSTANCE_NAME);
			cipherDecode.init(Cipher.DECRYPT_MODE, SECRET_KEY, IV);
			return new String(cipherDecode.doFinal(Base64.decodeBase64(str)),
					UTF_8);
		} catch (IllegalBlockSizeException | BadPaddingException
				| InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | InvalidAlgorithmParameterException e) {
			LOGGER.error("salary codec decode " + str + " error.", e);
			return null;
		}
	}

	public static String salt(String str) {
		byte[] payload = str.getBytes();
		byte f = (byte) (sum(payload, 0, payload.length / 2) % 26 + 97);
		byte l = (byte) (sum(payload, payload.length / 2, payload.length) % 26 + 97);
		byte[] bytes = new byte[payload.length + 2];
		bytes[0] = f;
		System.arraycopy(payload, 0, bytes, 1, payload.length);
		bytes[bytes.length - 1] = l;
		return new String(bytes);
	}

	private static int sum(byte[] payload, int start, int end) {
		int sum = 0;
		for (; start < end; start++) {
			sum += payload[start];
		}
		return sum;
	}

	/**
	 * 
	 * @param text
	 *            目标字符串
	 * @param length
	 *            截取长度
	 * @param encode
	 *            采用的编码方式
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String substring(String text, int length, String encode) {
		if (text == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		int currentLength = 0;
		for (char c : text.toCharArray()) {
			try {
				currentLength += String.valueOf(c).getBytes(encode).length;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if (currentLength <= length) {
				sb.append(c);
			} else {
				break;
			}
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		String text = "我ABC汉DEF";
		int length1 = 3;
		int length2 = 6;
		String[] encodes = new String[] { "GB2312", "GBK", "GB18030", "CP936",
				"CNS11643" };
		for (String encode : encodes) {
			System.out.println(new StringBuilder().append("用").append(encode)
					.append("编码截取字符串 -- 【").append(text).append("】")
					.append(length1).append("个字节的结果是【")
					.append(substring(text, length1, encode)).append("】")
					.toString());
			System.out.println(new StringBuilder().append("用").append(encode)
					.append("编码截取字符串 -- 【").append(text).append("】")
					.append(length2).append("个字节的结果是【")
					.append(substring(text, length2, encode)).append("】")
					.toString());
		}
		
		
		String test = "北京市房山区良乡拱辰街道拱辰南大街44号院1号楼北京荣鹏花园店";
		int cityNum = test.indexOf("市");
		int provinceNum = test.indexOf("省");
		System.out.println(cityNum+"cityNum"+provinceNum+"provinceNum");
	}

	public static String filterString(String str){
    	String regEx="[`~!@#$%^&*()+=|{}':;',.<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";  
    	Pattern p = Pattern.compile(regEx);
    	Matcher m = p.matcher(str);
    	return m.replaceAll("").trim();
    }

	
	/**
	 * 
	 * <p>Title: isMobileNO</p> 
	 * <p>Description: 判断是否传入手机号</p> 
	 * @param mobiles
	 * @return
	 */
	public static boolean isMobileNO(String mobiles) {
		//Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
		Pattern p = Pattern.compile("1\\d{10}");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}
	
	/**
	 * 
	 * <p>Title: isNumeric</p> 
	 * <p>Description: 判断字符串是否为数字</p> 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str){
		for (int i = str.length();--i>=0;){  
		    if (!Character.isDigit(str.charAt(i))){
		    	return false;
		    }
		}
		return true;
		/*Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");    
		return pattern.matcher(str).matches();*/    
	}


	/**
	 * 获取API域名
	 * @return
	 */
	public static String getAPIDomain(){
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domains = Configuration.root().getString("domain_API_dev","http://182.92.227.140:9004");
		if(IsProduct){
			domains = Configuration.root().getString("domain_API_product","http://api.higegou.com");
		}
		return domains;
	}
	/**
	 * 获取H5域名
	 * @return
	 */
	public static String getH5Domain(){
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domains = Configuration.root().getString("h5_dev","http://h5dev.higegou.com");
		if(IsProduct){
			domains = Configuration.root().getString("h5_product","http://h5.higegou.com");
		}
		return domains;
	}
	/**
	 * 获取PIC域名
	 * @return
	 */
	public static String getPICDomain(){
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domains = Configuration.root().getString("domain_dev","http://apitestpic.higegou.com");
		if(IsProduct){
			domains = Configuration.root().getString("domain_product","http://apipic.higegou.com");
		}
		return domains;
	}
	/**
	 * 获取BUCKET_NAME
	 * @return
	 */
	public static String getBUCKET_NAME(){
		String BUCKET_NAME=Configuration.root().getString("oss.bucket.name.higouAPIDev", "higou-api");
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		if(IsProduct){
			BUCKET_NAME=Configuration.root().getString("oss.bucket.name.higouAPIProduct", "higou-api");
		}
		return BUCKET_NAME;
	}
	/**
	 * 获取配置文件中的uid
	 * @return
	 */
	public static String getConfigUid() {
		String uid = "";
    	boolean IsProduct = Play.application().configuration().getBoolean("production", false);
		if(IsProduct){
			uid = Play.application().configuration().getString("wx.default.uid.product");
		}else{
			uid = Play.application().configuration().getString("wx.default.uid.dev");
		}
		return uid;
	}
	/**
	 * 获取列表图或者规格图
	 * @param pic
	 * @return
	 */
	public static String getListpic(String pic) {
		if(StringUtils.isBlank(pic)){
			return "";
		}
		String kjdImagePre = Configuration.root().getString("kjd.ImagePre","http://image.kjt.com.pre");
		if(pic.startsWith(kjdImagePre)){
			return pic;
		}
		String domain =getPICDomain();
		pic=pic.replaceAll(domain, "");
		if(pic.indexOf("/pimgs/adload/")>=0 || pic.indexOf("/pimgs")>=0 ||pic.indexOf("/upload")>=0){
			return StringUtil.getPICDomain()+pic;
		}
		if(pic.indexOf("-")>0){
			String listPicArray[] = pic.split("-");
			if (listPicArray.length > 2) {
				String pathTemp = listPicArray[1];
				pic = domain + "/pimgs/p1/" + pathTemp.substring(0, 2) + "/"
						+ pathTemp.substring(2, 4) + "/" + pathTemp.substring(4, 6)
						+ "/" + pathTemp.substring(6, 8) + "/" + pic;
			}
		}else{
			String listPicArray[] = pic.split("_");
			if (listPicArray.length > 1) {
				String pathTemp = listPicArray[0];
				if(pathTemp.length()>=8){
					pic = domain + "/pimgs/p1/" + pathTemp.substring(0, 2) + "/"
						+ pathTemp.substring(2, 4) + "/" + pathTemp.substring(4, 6)
						+ "/" + pathTemp.substring(6, 8) + "/" + pic;
				}
			}
		}
		if(pic.indexOf(domain)<0){
			pic = domain+pic;
		}
		return pic;
	}
	/**
	 * 获取列表图或者规格图在数据库里存在的值
	 * @param pic
	 * @return
	 */
	public static String getListpicRecover(String pic) {
		if(StringUtils.isBlank(pic)){
			return "";
		}
		return pic.replace(getPICDomain(), "");
	}
	 //取随机代言APP使用图章
    public static String getSheSaidIcon(){
		ArrayList<String> strtemp= new ArrayList<String>();
		String domainimg="";//CdnAssets.CDN_API_PUBLIC_URL;
		strtemp.add(domainimg+"images/appIcon/1.png");
		strtemp.add(domainimg+"images/appIcon/2.png");
		strtemp.add(domainimg+"images/appIcon/3.png");
		strtemp.add(domainimg+"images/appIcon/4.png");
		strtemp.add(domainimg+"images/appIcon/5.png");
		strtemp.add(domainimg+"images/appIcon/6.png");
		strtemp.add(domainimg+"images/appIcon/7.png");
		strtemp.add(domainimg+"images/appIcon/8.png");
		strtemp.add(domainimg+"images/appIcon/9.png");
		strtemp.add(domainimg+"images/appIcon/10.png");
		strtemp.add(domainimg+"images/appIcon/11.png");
		
		Integer num = (int) Math.ceil(Math.random()*11);
		return strtemp.get(num-1);
    }
    
	public static String getMD5(String str) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			String result = "";
			try {
				result = MD5(str, md);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

	public static String MD5(String strSrc, MessageDigest md) throws UnsupportedEncodingException {
		byte[] bt = strSrc.getBytes("utf-8");
		md.update(bt);
		String strDes = bytes2Hex(md.digest()); // to HexString
		return strDes;
	}

	private static String bytes2Hex(byte[] bts) {
		StringBuffer des = new StringBuffer();
		String tmp = null;
		for (int i = 0; i < bts.length; i++) {
			tmp = (Integer.toHexString(bts[i] & 0xFF));
			if (tmp.length() == 1) {
				des.append("0");
			}
			des.append(tmp);
		}
		return des.toString();
	}

	public static String MD5Encode(String origin) {
		String resultString = null;

		try {
			resultString = new String(origin);
			MessageDigest md = MessageDigest.getInstance("MD5");
			resultString = byteArrayToString(md.digest(resultString.getBytes()));
		} catch (Exception ex) {

		}
		return resultString;
	}

	/**
	 * 转换字节数组为16进制字串
	 * 
	 * @param b
	 *            字节数组
	 * @return 16进制字串
	 */
	public static String byteArrayToString(byte[] b) {
		StringBuffer resultSb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			// resultSb.append(byteToHexString(b[i]));//若使用本函数转换则可得到加密结果的16进制表示，即数字字母混合的形式
			resultSb.append(byteToNumString(b[i]));// 使用本函数则返回加密结果的10进制数字字串，即全数字形式
		}
		return resultSb.toString();
	}

	private static String byteToNumString(byte b) {

		int _b = b;
		if (_b < 0) {
			_b = 256 + _b;
		}

		return String.valueOf(_b);
	}

	private static String byteToHexString(byte b) {
		int n = b;
		if (n < 0) {
			n = 256 + n;
		}
		int d1 = n / 16;
		int d2 = n % 16;
		return hexDigits[d1] + hexDigits[d2];
	}

	/**
	 * 
	 * <p>Title: getStringFromList</p> 
	 * <p>Description: 将list集合转出string对象</p> 
	 * @param 
	 * @return
	 */
	public static String getStringFromList(List<String> productIds) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < productIds.size(); i++ ){
			sb.append(productIds.get(i));
			if(i<productIds.size()-1){
				sb.append(",");
			}
		}
		return sb.toString();
	}
     
	// 组装签名字符串,棒棒糖使用
	public static String makeSig(Map<String, String> sortMap) {
		sortMap.put("token", "aeb13b6a5cb82d43ce66b7f34f44c175");
		StringBuilder sb = new StringBuilder();
		Object[] keys = sortMap.keySet().toArray();
		Arrays.sort(keys);
		for (int i = 0; i < keys.length; i++) {
			String mapkey = (String) keys[i];
			if (i == keys.length - 1) {// 拼接时，不包括最后一个&字符
				sb.append(mapkey).append("=").append(sortMap.get(mapkey));// QSTRING_EQUAL为=,QSTRING_SPLIT为&
			} else {
				sb.append(mapkey).append("=").append(sortMap.get(mapkey))
						.append("&");
			}
		}
		String data = sb.toString();// 参数拼好的字符串			
		
		LOGGER.info("加密参数为：" + data);
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(data.getBytes("utf-8"));
			byte[] digest = messageDigest.digest();
			data = Base64.encodeBase64String(digest);
		} catch (Exception e) {
		}
		return data;
	}
	
	// 组装签名字符串
	public static String makeWxSig(Map<String, String> sortMap) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		Object[] keys = sortMap.keySet().toArray();
		Arrays.sort(keys);
		for (int i = 0; i < keys.length; i++) {
			String mapkey = (String) keys[i];
			if (i == keys.length - 1) {// 拼接时，不包括最后一个&字符
				sb.append(mapkey).append("=").append(sortMap.get(mapkey));// QSTRING_EQUAL为=,QSTRING_SPLIT为&
			} else {
				sb.append(mapkey).append("=").append(sortMap.get(mapkey))
						.append("&");
			}
		}
		String data = sb.toString();// 参数拼好的字符串			
		
		System.out.println("加密参数为：" + data);
		return data;
	}
	
	// 组装微信支付签名
	public static String getWxSign(Map<String, String> map) {

		String str = makeSig(map);
		str = str + "&key=" + Constants.WXappsecretpay;
		try{
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		str=MD5Util.MD5Encode(str, "UTF-8");
		}
		catch(Exception e){
			str="";
		}
		return str.toUpperCase();
	}
	
}
