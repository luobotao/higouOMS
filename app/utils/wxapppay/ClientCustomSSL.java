
package utils.wxapppay;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import play.Configuration;
import utils.Constants;
import utils.wxwebpay.XMLUtil;


/**
 * 
 * <p>Title: ClientCustomSSL.java</p> 
 * <p>Description: </p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年11月13日  上午11:05:46
 * @version
 */
public class ClientCustomSSL {


   
    public static Map call(String url, String arrayToXml) throws Exception{
    	String certPath = Configuration.root().getString("wxapppay.cert.path", "D:\\workspace\\cert\\1234290501_20151113172131.pfx");
    	Map doXMLtoMap=new HashMap();
        KeyStore keyStore  = KeyStore.getInstance("PKCS12");
        FileInputStream instream = new FileInputStream(new File(certPath));
        try {
            keyStore.load(instream, Constants.wxapp_partner.toCharArray());
        } finally {
            instream.close();
        }
        SSLContext sslcontext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, Constants.wxapp_partner.toCharArray())
                .build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[] { "TLSv1" },
                null,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();
        try {
           // HttpGet httpget = new HttpGet("https://api.mch.weixin.qq.com/secapi/pay/refund");
        	HttpPost httpPost = new HttpPost(url);
        	httpPost.setEntity(new StringEntity(arrayToXml, "UTF-8"));
        	
            System.out.println(httpPost.getURI());
            
            System.out.println("executing request" + httpPost.getRequestLine());

            CloseableHttpResponse response = httpclient.execute(httpPost);
            
            String jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");
            doXMLtoMap = XMLUtil.doXMLParse(jsonStr);
			System.out.println(jsonStr);
            
			response.close();
     
        } finally {
        
            httpclient.close();
        }
		return doXMLtoMap;
    
    }
}
