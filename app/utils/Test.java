package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Test {

	public static void main(String[] args) {
		Test test = new Test();
		String str = "appid=seller325&data={\"ShippingInfo\":{\"receiveName\":\"贾磊\",\"receivePhone\":\"18612833134\",\"receiveAddress\":\"测试\",\"receiveAreaCode\":\"110100\",\"receiveZip\":\"\",\"shipTypeID\":\"1\",\"senderName\":\"\",\"senderTel\":\"\",\"senderCompanyName\":\"\",\"senderAddr\":\"\",\"senderZip\":\"\",\"senderCity\":\"\",\"senderProvince\":\"\",\"senderCountry\":\"\",\"receiveAreaName\":\"北京,北京市,东城区\"},\"ItemList\":[{\"productId\":\"299KRB02ew30001\",\"quantity\":1,\"salePrice\":810.0,\"taxPrice\":0.0}],\"WarehouseID\":\"52\",\"AuthenticationInfo\":{\"name\":\"ctt\",\"idCardType\":0,\"idCardNumber\":\"331082199009301399\",\"phoneNumber\":\"18500329421\",\"email\":\"chentaotao@higegou.com\",\"address\":\"北京朝阳\"},\"PayInfo\":{\"productAmount\":810.0,\"shippingAmount\":0.0,\"taxAmount\":0.0,\"commissionAmount\":0.0,\"payTypeSysNo\":112,\"paySerialNumber\":\"1008080244201504230082923666\"},\"ServerType\":\"S02\",\"SaleChannelSysNo\":\"1097\",\"MerchantOrderID\":\"2015092825276\"}&format=json&method=order.socreate&nonce=584056&timestamp=20150929184905&version=2.0&appsecret=kjt@325";
		System.out.println(test.encryption(str));
	}

	/**
	 * 
	 * @param plainText
	 *            明文
	 * @return 32位密文
	 */
	public String encryption(String plainText) {
		String re_md5 = new String();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte b[] = md.digest();

			int i;

			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}

			re_md5 = buf.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return re_md5;
	}
}