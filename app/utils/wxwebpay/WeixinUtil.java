package utils.wxwebpay;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import utils.StringUtil;



/**
 * 公众平台通用接口工具类
 */
public class WeixinUtil {

	/**
	 * 把HashMap转换成xml
	 * @param arr
	 * @return
	 * #time:下午5:32:36
	 *
	 */
	public static String ArrayToXml(SortedMap<String, String> arr) {
		String xml = "<xml>";
		Iterator<Entry<String, String>> iter = arr.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			String key = entry.getKey();
			String val = entry.getValue();
			if (StringUtil.isNumeric(val)) {
				xml += "<" + key + ">" + val + "</" + key + ">";

			} else
				xml += "<" + key + "><![CDATA[" + val + "]]></" + key + ">";
		}

		xml += "</xml>";
		return xml;
	}
}