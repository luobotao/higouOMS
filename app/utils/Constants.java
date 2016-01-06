package utils;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import play.Configuration;
import services.ServiceFactory;


/**
 * 常量配置
 * @author luobotao
 * Date: 2015年4月18日 上午11:46:25
 */
public class Constants {
    public static final String DOMAIN = Configuration.root().getString("domain","http://127.0.0.1");

    public static final String NL = "\r\n";

    public static final String COMMA = ",";

    public static final String NAMESPACE_SMS = "sms.";

    public static final String NAMESPACE_SESSION_USER = "session.user.";
    public static final String product_KEY = "product.id.";//商品
    public static final String productUnion_Pid_KEY = "productUnion.pid.";//商品分表
    public static final String user_KEY = "user.id.";//用户
    public static final String category_KEY = "category.id.";//品类
    public static final String currency_KEY = "currency.id.";//符号
    public static final String fromsite_KEY = "fromsite.id.";//来源
    public static final String mould_KEY = "mould.id.";//卡片
    public static final String channel_mould_KEY = "channel_mould.id.";//频道卡片
    public static final String channel_mould_pro_KEY = "channel_mould_pro.id.";//卡片商品
    public static final String channelMouldIds_KEY = "channelMouldIds.cid.";
    public static final String channelMouldProIds_KEY = "channelMouldProIds.cid.";
    public static final String subject_mould_KEY = "subject_mould.id.";//专题卡片
    public static final String subject_mould_pro_KEY = "subject_mould_pro.id.";//专题卡片商品
    public static final String subjectMouldIds_KEY = "subjectMouldIds.cid.";	
    public static final String subjectMouldProIds_KEY = "subjectMouldProIds.cid.";
    
    public static final String shoppingOrder_KEY = "shoppingOrder.id.";
    public static final String orderProduct_KEY = "orderProduct.id.";
    public static final String parcels_KEY = "parcels.id.";
    public static final String parcelsWaybill_KEY = "parcelsWaybill.id.";
    public static final String endorsement_KEY = "endorsement.id.";//代言对象
    public static final String groupName_KEY = "groupName.id.";//组名称
    public static final String address_KEY = "address.id.";//地址名称
    public static final String bbtaddress_parent_KEY = "bbtaddress.parentid.";//代言对象
    
    public static final String queryOrderRecordWithUser_KEY = "queryOrderRecordWithUser.id.";
    public static final String NAMESPACE_SESSION_TOKEN = "session.token.";
    public static final String kd_company_Key = "kd_company.";//快递公司
    public static final String USER = "adminUser";
    
    public static final Integer FIVE_MINUTES = 60 * 5;

    public static final Integer THIRTY_MINUTES = 60 * 30;

    public static final Integer SESSION_TOKEN_EXPIRE_IN = 60 * 20;

    public static final Integer SMS_CODE_CACHE_EXPIRATION = 60 * 30;

    public static final String INIT = "init";

    public static final String CONFIRM = "confirm";

    public static final String MODIFY = "modify";

    public static final List<String> USER_AGENTS = new ArrayList(Arrays.asList("iphone","android","ucbrowser"));

    public static final String DYNAMIC_TOKEN_HEADER = "dToken";

    public static final Integer DYNAMIC_TOKEN_LENGTH = 32;

    public static final String CACHE_NAMESPACE_LOGIN_TOKEN = "loginToken.";

    public static final Integer LOGIN_TOKEN_EXPIRE_IN = 14 * 24 * 60 * 60;

    public static final List<Pattern> NEED_CHECK_REFERER_URL_PATTERN = new ArrayList<>();

    public static final String NAMESPACE_CACHE_INIT = Constants.INIT + ".";

    public static final String NAMESPACE_CACHE_CONFIRM = Constants.CONFIRM + ".";

    public static final String NAMESPACE_CACHE_MODIFY = Constants.MODIFY + ".";

    public static final String SESSION_TOKEN = "token";

    public static final String ADMIN_SESSION_NAME = "_A_S";

    public static final String CACHE_NAMESPACE_ADMINSESSION_USER = "admin.session.user.";

    public static final Integer ADMINSESSION_TOKEN_EXPIRE_IN = 60 * 20;

    public static final String CACHE_NAMESPACE_CONFIG = "config.";

    public static final Integer CONFIG_CACHE_EXPIRE_IN = 60 * 2;
    
    public static final Integer DEFAULT_VAL = -1;

    public static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    public static final boolean IsProduct = Configuration.root().getBoolean("production", false);

    public static final java.util.regex.Pattern PHONE_PATTERN = java.util.regex.Pattern.compile("^((1))\\d{10}$");

    /*
     * 微信AppID
     */
    //public static final String WXappID="wx0cef7e835f598e36";
    public static final String WXappID="wx99199cff15133f37";//wx80a445e58ff37347";
    /*
     * 微信密钥
     */
    //public static final String WXappsecret="416704762a6b40c20025ea169bb00e61";
    public static final String WXappsecret="a017774f117bf0100a2f7939ef56c89a";//"9992b9b4a1cc16c59c6ff49d99378cc5";
    /*
     * 微信支付密钥
     */
    public static final String WXappsecretpay="neolixxinshiqiqinengwanwei123456";//"neolixxinshiqiqinengwanwei123456";
    /*
     * 微信支付商户号
     */
    public static final String WXMCID="1235413502";//"1228736802";

	public static final String CFTPARTNER = "1234290501";//1234290501财付通账号

	public static final String CFTPASSWD = "618050";//财付通密码
	
	//商户号
	public static final String wxapp_partner = "1234290501";
	//appid//密钥
	public static final String partner_key = "97d12b655c585625710e31309d2ee3c4";
	public static final String wxapp_app_id="wx73bdf02facab9ca2";
	public static final String wxapp_app_secret = "f17f13bbac8893946a267566c24628fb";

	//appkey
	public static final String wxapp_app_key="CBTFJjMoeCoIweHgZfmuPg4nCwVrYy5QjqEWGvgghVvj44iDy9eToInNIi4DizrUYvMYSUkQnGvqqck9xKqAd1BrRPEKZeFo6fWDkQbJ93xniBbrGbkH2EC1VpGRVNec";

	public static String waapp_passwd="321312";

    static{
        NEED_CHECK_REFERER_URL_PATTERN.add(Pattern.compile("paymentPassword"));
        NEED_CHECK_REFERER_URL_PATTERN.add(Pattern.compile("/my/"));
        NEED_CHECK_REFERER_URL_PATTERN.add(Pattern.compile("/account/"));
    }
    

    //订单状态
	public static enum OrderStatus {
		DELETE(-99, "已删除"), NOPAY(0, "未支付"), WAITTODELIVER(1, "已支付(待发货)"), DELIVERED(
				2, "已发货"), END(3, "已完成"), CANCEL(5, "已取消"), REMAINPAY(20,
				"尾款支付");

		private int status;
		private String message;

		private OrderStatus(int status, String message) {
			this.message = message;
			this.status = status;
		}

		public int getStatus() {
			return status;
		}

		public String getMessage() {
			return message;
		}
	}
	
	//订单商品状态
	public static enum OrderProFlg {
		WCL(0, "未处理"), YCL(1, "已处理"), SQTK(2, "申请退款"), TKWC(3, "退款完成");

		private int status;
		private String message;

		private OrderProFlg(int status, String message) {
			this.message = message;
			this.status = status;
		}

		public int getStatus() {
			return status;
		}

		public String getMessage() {
			return message;
		}
		public static String orderProFlg2HTML(String value) {
			StringBuilder sb = new StringBuilder();
			OrderProFlg[] orderProFlgs = OrderProFlg.values();
            sb.append(Htmls.generateOption(DEFAULT_VAL, "全部"));
            for (OrderProFlg m : orderProFlgs) {
                if (value.equals(m.status+"")) {
                    sb.append(Htmls.generateSelectedOption(m.status,
                            m.message));
                } else {
                    sb.append(Htmls.generateOption(m.status, m.message));
                }
            }
            return sb.toString();
		}
		
		public static String flgs2Message(Object status) {
			OrderProFlg[] flgs = OrderProFlg.values();
			for (OrderProFlg s : flgs) {
				if (String.valueOf(status).trim().equals(String.valueOf(s.status).trim())) {
					return s.getMessage();
				}
			}
			return "";
		}
	}
	//支付方式
	public static enum PayMethod {
		WXAPPPAY(10, "微信app"), WXWEBPPAY(11, "微信网页"), ALIDIRECT(20, "支付宝快捷"), ALIWAP(
				21, "支付宝wap"), ALIWEB(22, "支付宝国际"), COUPONS(90, "优惠券");

		private int status;
		private String message;

		private PayMethod(int status, String message) {
			this.message = message;
			this.status = status;
		}

		public int getStatus() {
			return status;
		}

		public String getMessage() {
			return message;
		}
		
		public static String status2HTML(String value) {
            StringBuilder sb = new StringBuilder();
            PayMethod[] payMethod = PayMethod.values();
            sb.append(Htmls.generateOption(DEFAULT_VAL, "全部"));
            for (PayMethod m : payMethod) {
                if (value.equals(m.status+"")) {
                    sb.append(Htmls.generateSelectedOption(m.status,
                            m.message));
                } else {
                    sb.append(Htmls.generateOption(m.status, m.message));
                }
            }
            return sb.toString();
        }
		
		public static String status2Message(int status) {
			PayMethod[] statuses = PayMethod.values();
			for (PayMethod s : statuses) {
				if (status==s.status) {
					return s.getMessage();
				}
			}
			return "";
		}
	}
	//支付状态
	public static enum Paystat {
		stat0(0, "未支付"), stat10(10, "支付异常"), stat20(20, "支付成功");
		
		private int status;
		private String message;
		
		private Paystat(int status, String message) {
			this.message = message;
			this.status = status;
		}
		
		public int getStatus() {
			return status;
		}
		
		public String getMessage() {
			return message;
		}
		
		public static String status2HTML(String value) {
			StringBuilder sb = new StringBuilder();
			Paystat[] paystat = Paystat.values();
			for (Paystat m : paystat) {
				if (value.equals(m.status+"")) {
					sb.append(Htmls.generateSelectedOption(m.status,
							m.message));
				} else {
					sb.append(Htmls.generateOption(m.status, m.message));
				}
			}
			return sb.toString();
		}
		
		public static String status2Message(int status) {
			Paystat[] statuses = Paystat.values();
			for (Paystat s : statuses) {
				if (status==s.status) {
					return s.getMessage();
				}
			}
			return "";
		}
	}

	//是否是撒娇支付
	public static enum LoveLyStatus {
		LOVELYNOT(1, "否"), LOVELYYES(2, "是");

		private int status;
		private String message;

		private LoveLyStatus(int status, String message) {
			this.message = message;
			this.status = status;
		}

		public int getStatus() {
			return status;
		}

		public String getMessage() {
			return message;
		}
		
		public static String status2HTML(String value) {
            StringBuilder sb = new StringBuilder();
            LoveLyStatus[] loveLyStatus = LoveLyStatus.values();
            sb.append(Htmls.generateOption(DEFAULT_VAL, "全部"));
            for (LoveLyStatus s : loveLyStatus) {
                if (value.equals(s.status+"")) {
                    sb.append(Htmls.generateSelectedOption(s.status,
                            s.message));
                } else {
                    sb.append(Htmls.generateOption(s.status, s.message));
                }
            }
            return sb.toString();
        }
	}
	
	
	// 包裹状态
	public static enum ParcelStatus {
		WAITINGTODELIVER(1, "待发货"), DELIVERD(2, "已发货"), END(3, "已完成"), WAITINGTOPAY(
				4, "待支付"), CANCELED(5, "已取消"), DELETED(-99, "已删除");

		private int status;
		private String message;

		private ParcelStatus(int status, String message) {
			this.message = message;
			this.status = status;
		}

		public int getStatus() {
			return status;
		}

		public String getMessage() {
			return message;
		}

		public static String status2HTML(String value) {
			StringBuilder sb = new StringBuilder();
			ParcelStatus[] parcelStatus = ParcelStatus.values();
			sb.append(Htmls.generateOption(DEFAULT_VAL, "全部"));
			for (ParcelStatus s : parcelStatus) {
				if (value.equals(s.status + "")) {
					sb.append(Htmls.generateSelectedOption(s.status, s.message));
				} else {
					sb.append(Htmls.generateOption(s.status, s.message));
				}
			}
			return sb.toString();
		}
	}
	
	//访问平台
	public static enum Devices {
		Android("Android"), IOS("IPhone");

		private String message;

		private Devices( String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}
		
		public static String devices2HTML(Integer value) {
            StringBuilder sb = new StringBuilder();
            Devices[] devices = Devices.values();
            sb.append(Htmls.generateOption(DEFAULT_VAL, "全部"));
            for (Devices s : devices) {
                if (value.equals(s.ordinal())) {
                    sb.append(Htmls.generateSelectedOption(s.ordinal(),
                            s.message));
                } else {
                    sb.append(Htmls.generateOption(s.ordinal(), s.message));
                }
            }
            return sb.toString();
        }
	}
	
	//商品类型 下单类型
	public static enum Typs {
		AGENTORDER(1, "代下单"), AUTOTROPHY(2, "自营"),HEYINN(90, "嘿客店"),CONSORTIUM(3, "联营"),PRESELL(4, "预售");
		
		private int status;
		private String message;
		
		private Typs(int status, String message) {
			this.message = message;
			this.status = status;
		}

		public int getStatus() {
			return status;
		}

		public String getMessage() {
			return message;
		}

		public static String typs2HTML(int value) {
			StringBuilder sb = new StringBuilder();
			Typs[] typs = Typs.values();
			sb.append(Htmls.generateOption(0, "全部"));
			for (Typs s : typs) {
				if (value==s.status) {
					sb.append(Htmls.generateSelectedOption(s.status, s.message));
				} else {
					sb.append(Htmls.generateOption(s.status, s.message));
				}
			}
			return sb.toString();
		}
		
		public static String typs2Message(int status) {
			Typs[] typs = Typs.values();
			for (Typs s : typs) {
				if (status==s.status) {
					return s.getMessage();
				}
			}
			return "";
		}
	}
	//预售阶段
	public static enum PreSells {
		DJWZF(1,"订金未支付"),DJYZF(2, "订金已支付"), WKDZF(3, "尾款待支付"),ZFWC(4, "支付完成");
		
		private int status;
		private String message;
		
		private PreSells(int status, String message) {
			this.message = message;
			this.status = status;
		}
		
		public int getStatus() {
			return status;
		}
		
		public String getMessage() {
			return message;
		}
		
		public static String typs2HTML(int value) {
			StringBuilder sb = new StringBuilder();
			PreSells[] typs = PreSells.values();
			sb.append(Htmls.generateOption(0, "全部"));
			for (PreSells s : typs) {
				if (value==s.status) {
					sb.append(Htmls.generateSelectedOption(s.status, s.message));
				} else {
					sb.append(Htmls.generateOption(s.status, s.message));
				}
			}
			return sb.toString();
		}
		
		public static String typs2Message(int status) {
			PreSells[] typs = PreSells.values();
			for (PreSells s : typs) {
				if (status==s.status) {
					return s.getMessage();
				}
			}
			return "";
		}
	}
	//商品类型 下单类型(去除嘿客店)
	public static enum TypsProduct {
		AGENTORDER(1, "代下单"), AUTOTROPHY(2, "自营"), CONSORTIUM(3, "联营");
		
		private int status;
		private String message;
		
		private TypsProduct(int status, String message) {
			this.message = message;
			this.status = status;
		}
		
		public int getStatus() {
			return status;
		}
		
		public String getMessage() {
			return message;
		}
		
		public static String typs2HTML(int value) {
			StringBuilder sb = new StringBuilder();
			TypsProduct[] typs = TypsProduct.values();
			sb.append(Htmls.generateOption(-1, "全部"));
			for (TypsProduct s : typs) {
				if (value==s.status) {
					sb.append(Htmls.generateSelectedOption(s.status, s.message));
				} else {
					sb.append(Htmls.generateOption(s.status, s.message));
				}
			}
			return sb.toString();
		}
		
		public static String typs2Message(int status) {
			TypsProduct[] typs = TypsProduct.values();
			for (TypsProduct s : typs) {
				if (status==s.status) {
					return s.getMessage();
				}
			}
			return "";
		}
	}
	//商品类型
	public static enum PTyps {
		COMMON(1, "普通"), 
		LOVELY(2, "撒娇"), 
		PRESALE(3, "预售 "), 
		REGULAR (4, "定时开抢");
		
		private int status;
		private String message;
		
		private PTyps(int status, String message) {
			this.message = message;
			this.status = status;
		}
		
		public int getStatus() {
			return status;
		}
		
		public String getMessage() {
			return message;
		}
		
		public static String typs2HTML(int value) {
			StringBuilder sb = new StringBuilder();
			PTyps[] typs = PTyps.values();
			sb.append(Htmls.generateOption(0, "全部"));
			for (PTyps s : typs) {
				if (value==s.status) {
					sb.append(Htmls.generateSelectedOption(s.status, s.message));
				} else {
					sb.append(Htmls.generateOption(s.status, s.message));
				}
			}
			return sb.toString();
		}
		
		public static String typs2Message(int status) {
			PTyps[] typs = PTyps.values();
			for (PTyps s : typs) {
				if (status==s.status) {
					return s.getMessage();
				}
			}
			return "";
		}
	}
	
	//销量所属类别
	public static enum CountTyps {
		ZIYING(1, "嗨个购"), HEIKEDIAN(2, "嘿客店");
		
		private int status;
		private String message;
		
		private CountTyps(int status, String message) {
			this.message = message;
			this.status = status;
		}

		public int getStatus() {
			return status;
		}

		public String getMessage() {
			return message;
		}

		public static String countTyps2HTML(int value) {
			StringBuilder sb = new StringBuilder();
			CountTyps[] countTyps = CountTyps.values();
			sb.append(Htmls.generateOption(3, "全部"));
			for (CountTyps s : countTyps) {
				if (value==s.status) {
					sb.append(Htmls.generateSelectedOption(s.status, s.message));
				} else {
					sb.append(Htmls.generateOption(s.status, s.message));
				}
			}
			return sb.toString();
		}
	}
	//商品订单状态
	public static enum ProStatus {
		DFH(0, "待发货"), YXD(1, "已下单"),GUWLYFH(2, "国际物流已发货"),HGQGZ(3, "海关清关中"),YSD(4, "已送达");
		
		private int status;
		private String message;
		
		private ProStatus(int status, String message) {
			this.message = message;
			this.status = status;
		}
		
		public int getStatus() {
			return status;
		}
		
		public String getMessage() {
			return message;
		}
		
		public static String proStatus2HTML(int value) {
			StringBuilder sb = new StringBuilder();
			ProStatus[] proStatus = ProStatus.values();
			sb.append(Htmls.generateOption(DEFAULT_VAL, ""));
			for (ProStatus s : proStatus) {
				if (value==s.status) {
					sb.append(Htmls.generateSelectedOption(s.status, s.message));
				} else {
					sb.append(Htmls.generateOption(s.status, s.message));
				}
			}
			return sb.toString();
		}
		

		public static String proStatus2Message(int status) {
			ProStatus[] proStatus = ProStatus.values();
			for (ProStatus s : proStatus) {
				if (status==s.status) {
					return s.getMessage();
				}
			}
			return "";
		}
	}
	//短信类型
	public static enum SmsStatus {
		PUSHSMS(2, "营销短信"),COMMONSMS(1, "普通短信"),VOICESMS(3, "语音验证码");
		
		private int status;
		private String message;
		
		private SmsStatus(int status, String message) {
			this.message = message;
			this.status = status;
		}
		
		public int getStatus() {
			return status;
		}
		
		public String getMessage() {
			return message;
		}
		
		public static String smsStatus2HTML(int value) {
			StringBuilder sb = new StringBuilder();
			SmsStatus[] smsStatus = SmsStatus.values();
			for (SmsStatus s : smsStatus) {
				if (value==s.status) {
					sb.append(Htmls.generateSelectedOption(s.status, s.message));
				} else {
					sb.append(Htmls.generateOption(s.status, s.message));
				}
			}
			return sb.toString();
		}
	}
	//包裹结算状态（针对联营商品）
	public static enum ParcelCheckStatus {
		WaitIng(0, "待申请"),Submited(1, "已申请待结算"),Checked(2, "已结算");
		
		private int status;
		private String message;
		
		private ParcelCheckStatus(int status, String message) {
			this.message = message;
			this.status = status;
		}
		
		public int getStatus() {
			return status;
		}
		
		public String getMessage() {
			return message;
		}
		
		public static String checkStatus2HTML(int value) {
			StringBuilder sb = new StringBuilder();
			sb.append(Htmls.generateOption(-1, "全部"));
			ParcelCheckStatus[] parcelCheckStatus = ParcelCheckStatus.values();
			for (ParcelCheckStatus s : parcelCheckStatus) {
				if (value==s.status) {
					sb.append(Htmls.generateSelectedOption(s.status, s.message));
				} else {
					sb.append(Htmls.generateOption(s.status, s.message));
				}
			}
			return sb.toString();
		}
		
		public static String checkStatus2Message(int status) {
			ParcelCheckStatus[] parcelCheckStatus = ParcelCheckStatus.values();
			for (ParcelCheckStatus s : parcelCheckStatus) {
				if (status==s.status) {
					return s.getMessage();
				}
			}
			return "";
		}
	}

	
	// 包裹状态
	public static enum WaybillStatus {
		DFH(0, "待发货"), YXD(1, "已下单"), GUWLYFH(2, "国际物流已发货"), HGQGZ(
				3, "海关清关中"), GNWLYFH(4, "国内物流已发货"), DXDYSD(5, "代下单已送达"), YFH(11, "已发货"), YSD(12, "已送达");

		private int status;
		private String message;

		private WaybillStatus(int status, String message) {
			this.message = message;
			this.status = status;
		}

		public int getStatus() {
			return status;
		}

		public String getMessage() {
			return message;
		}

		public static String waybill2HTML(String value) {
			StringBuilder sb = new StringBuilder();
			WaybillStatus[] parcelWaybillStatus = WaybillStatus.values();
			sb.append(Htmls.generateOption(-99, "全部"));
			for (WaybillStatus s : parcelWaybillStatus) {
				if (value.equals(s.status + "")) {
					sb.append(Htmls.generateSelectedOption(s.status, s.message));
				} else {
					sb.append(Htmls.generateOption(s.status, s.message));
				}
			}
			return sb.toString();
		}
		public static String waybill2HTMLDxd(String value) {
			StringBuilder sb = new StringBuilder();
			WaybillStatus[] parcelWaybillStatus = WaybillStatus.values();
			sb.append(Htmls.generateOption(-99, "全部"));
			for (WaybillStatus s : parcelWaybillStatus) {
				if(s.getStatus()==1||s.getStatus()==2||s.getStatus()==3||s.getStatus()==4||s.getStatus()==5){
					if (value.equals(s.status + "")) {
						sb.append(Htmls.generateSelectedOption(s.status, s.message));
					} else {
						sb.append(Htmls.generateOption(s.status, s.message));
					}
				}
			}
			return sb.toString();
		}
		public static String waybill2HTMLZyOrHkd(String value) {
			StringBuilder sb = new StringBuilder();
			WaybillStatus[] parcelWaybillStatus = WaybillStatus.values();
			sb.append(Htmls.generateOption(-99, "全部"));
			for (WaybillStatus s : parcelWaybillStatus) {
				if(s.getStatus()==0||s.getStatus()==11||s.getStatus()==12){
					if (value.equals(s.status + "")) {
						sb.append(Htmls.generateSelectedOption(s.status, s.message));
					} else {
						sb.append(Htmls.generateOption(s.status, s.message));
					}
				}
			}
			return sb.toString();
		}
		public static String waybill2Message(int status) {
			WaybillStatus[] waybills = WaybillStatus.values();
			for (WaybillStatus s : waybills) {
				if (status==s.status) {
					return s.getMessage();
				}
			}
			return "";
		}
	}
	
	//包裹类型
	public static enum Srcs {
		AGENTORDER(1, "代下单"), AUTOTROPHY(2, "自营"),HEYINN(90, "嘿客店"),CONSORTIUM(3, "联营");
		
		private int status;
		private String message;
		
		private Srcs(int status, String message) {
			this.message = message;
			this.status = status;
		}

		public int getStatus() {
			return status;
		}

		public String getMessage() {
			return message;
		}

		public static String srcs2HTML(int value) {
			StringBuilder sb = new StringBuilder();
			Srcs[] srcs = Srcs.values();
			sb.append(Htmls.generateOption(DEFAULT_VAL, "全部"));
			for (Srcs s : srcs) {
				if (value==s.status) {
					sb.append(Htmls.generateSelectedOption(s.status, s.message));
				} else {
					sb.append(Htmls.generateOption(s.status, s.message));
				}
			}
			return sb.toString();
		}
		

		public static String srcs2Message(int status) {
			Srcs[] srcs = Srcs.values();
			for (Srcs s : srcs) {
				if (status==s.status) {
					return s.getMessage();
				}
			}
			return "";
		}
	}

	
	//商品状态
	public static enum ProductStatus {
//		NEW(1, "新品待审"), 
//		ONSALE(10, "在线"), 
//		OFFSALE(20, "下架"), 
//		OUTOFSALE(21, "缺货自动下架"), 
//		RECYCLE(30, "回收站"), 
//		WAITDELETE(40, "等候彻底删除"), 
//		RETENTION(50, "条目暂时保留"), 
//		AMAZON(102, "早期amazon商品");
		NEW(1, "新品待审"), 
		ONSALE(10, "在线"), 
		OFFSALE(20, "下架"), 
		OUTOFSALE(21, "自动下架"), 
		RECYCLE(30, "回收站");
		
		private int status;
		private String message;
		
		private ProductStatus(int status, String message) {
			this.message = message;
			this.status = status;
		}

		public int getStatus() {
			return status;
		}

		public String getMessage() {
			return message;
		}

		public static String status2HTML(int value) {
			StringBuilder sb = new StringBuilder();
			ProductStatus[] status = ProductStatus.values();
			sb.append(Htmls.generateOption(DEFAULT_VAL, "全部"));
			for (ProductStatus s : status) {
				if (value==s.status) {
					sb.append(Htmls.generateSelectedOption(s.status, s.message));
				} else {
					sb.append(Htmls.generateOption(s.status, s.message));
				}
			}
			return sb.toString();
		}
		
		public static String status2Message(int value) {
			ProductStatus[] status = ProductStatus.values();
			for (ProductStatus s : status) {
				if (value==s.status) {
					return s.getMessage();
				}
			}
			return "";
		}
	}
	//发货地
	public static enum Wayremark {
		SELF(1, "嗨个购"), 
		SELFSHBSC(6, "保税仓"), 
		AMERICAN(2, "美国"), 
		HOKONG(3, "香港"), 
		JAPAN(4, "日本"), 
		KOREA(5, "韩国"),
		HWZY(7, "海外");
		
		private int status;
		private String message;
		
		private Wayremark(int status, String message) {
			this.message = message;
			this.status = status;
		}
		
		public int getStatus() {
			return status;
		}
		
		public String getMessage() {
			return message;
		}
		
		public static String wayRemark2HTML(String message) {
			StringBuilder sb = new StringBuilder();
			Wayremark[] status = Wayremark.values();
			sb.append(Htmls.generateOption(DEFAULT_VAL, "请选择"));
			for (Wayremark s : status) {
				if (!StringUtils.isBlank(message) && message.indexOf(s.message)>=0) {
					sb.append(Htmls.generateSelectedOption(s.status, s.message));
				} else {
					sb.append(Htmls.generateOption(s.status, s.message));
				}
			}
			return sb.toString();
		}
		public static String wayRemark2Message(int value) {
			Wayremark[] wayremarks = Wayremark.values();
			for (Wayremark s : wayremarks) {
				if (value==s.status) {
					return s.getMessage()+"_中国";
				}
			}
			return "";
		}
	}
	
	//撒娇折扣
	public static enum Lovelydistinct {
		DISTINCT75(75, "7.5折"), 
		DISTINCT80(80, "8折"), 
		DISTINCT85(85, "8.5折"), 
		DISTINCT88(88, "8.8折"), 
		DISTINCT9(90, "9折");
		
		private int distinct;
		private String message;
		
		private Lovelydistinct(int distinct, String message) {
			this.message = message;
			this.distinct = distinct;
		}
		public int getDistinct() {
			return distinct;
		}

		public String getMessage() {
			return message;
		}



		public static String distinct2HTML(int value) {
			StringBuilder sb = new StringBuilder();
			Lovelydistinct[] distincts = Lovelydistinct.values();
			sb.append(Htmls.generateOption(0, "请选择撒娇折扣"));
			for (Lovelydistinct s : distincts) {
				if (value==s.distinct) {
					sb.append(Htmls.generateSelectedOption(s.distinct, s.message));
				} else {
					sb.append(Htmls.generateOption(s.distinct, s.message));
				}
			}
			return sb.toString();
		}
		
		public static String distinct2Message(int value) {
			Lovelydistinct[] distincts = Lovelydistinct.values();
			for (Lovelydistinct s : distincts) {
				if (value==s.distinct) {
					return s.getMessage();
				}
			}
			return "";
		}
	}
	//国旗
	public static enum NationalFlag {
		USA("usa.png", "美国"), 
		JPN("jpn.png", "日本"), 
		KOR("kor.png", "韩国"), 
		AUS("aus.png", "澳大利亚"),
		GER("ger.png", "德国");
		
		private String pic;
		private String message;
		
		private NationalFlag(String pic, String message) {
			this.message = message;
			this.pic = pic;
		}
		public String getPic() {
			return pic;
		}
		public String getMessage() {
			return message;
		}

		public static String pic2HTML(String value) {
			StringBuilder sb = new StringBuilder();
			NationalFlag[] nationalFlags = NationalFlag.values();
			for (NationalFlag s : nationalFlags) {
				if (s.pic.equals(value)) {
					sb.append(Htmls.generateSelectedOptionString(s.pic, s.message));
				} else {
					sb.append(Htmls.generateOptionString(s.pic, s.message));
				}
			}
			return sb.toString();
		}
		
		public static String distinct2Message(String value) {
			NationalFlag[] nationalFlags = NationalFlag.values();
			for (NationalFlag s : nationalFlags) {
				if (s.pic.equals(value)) {
					return s.getMessage();
				}
			}
			return "";
		}
	}
	//频道类型
	public static enum ChannelTypes {
		Custom("1", "自定义商品"), 
		Auto("2", "自动更新");
		
		private String typ;
		private String message;
		
		private ChannelTypes(String typ, String message) {
			this.message = message;
			this.typ = typ;
		}
		
		
		public String getTyp() {
			return typ;
		}
		public String getMessage() {
			return message;
		}


		public static String typ2HTML(String value) {
			StringBuilder sb = new StringBuilder();
			ChannelTypes[] channelTypes = ChannelTypes.values();
			sb.append(Htmls.generateOption(DEFAULT_VAL, "请选择频道类型"));
			for (ChannelTypes s : channelTypes) {
				if (s.typ.equals(value)) {
					sb.append(Htmls.generateSelectedOptionString(s.typ, s.message));
				} else {
					sb.append(Htmls.generateOptionString(s.typ, s.message));
				}
			}
			return sb.toString();
		}
		
		public static String typ2Message(String value) {
			ChannelTypes[] channelTypes = ChannelTypes.values();
			for (ChannelTypes s : channelTypes) {
				if (s.typ.equals(value)) {
					return s.getMessage();
				}
			}
			return "";
		}
	}
	//频道可见用户组
	public static enum ShowTypes {
		Custom("0", "全部用户"), 
		NewMan("1", "新用户"), 
		OldMan("2", "旧用户");
		
		private String typ;
		private String message;
		
		private ShowTypes(String typ, String message) {
			this.message = message;
			this.typ = typ;
		}
		
		
		public String getTyp() {
			return typ;
		}
		public String getMessage() {
			return message;
		}
		
		
		public static String typ2HTML(String value) {
			StringBuilder sb = new StringBuilder();
			ShowTypes[] showTypes = ShowTypes.values();
			for (ShowTypes s : showTypes) {
				if (s.typ.equals(value)) {
					sb.append(Htmls.generateSelectedOptionString(s.typ, s.message));
				} else {
					sb.append(Htmls.generateOptionString(s.typ, s.message));
				}
			}
			return sb.toString();
		}
		
		public static String typ2Message(String value) {
			ShowTypes[] showTypes = ShowTypes.values();
			for (ShowTypes s : showTypes) {
				if (s.typ.equals(value)) {
					return s.getMessage();
				}
			}
			return "";
		}
	}
	//频道是否显示
	public static enum ShowFlag {
		Hide("0", "隐藏"),
		Show("1", "显示"); 
		
		private String flag;
		private String message;
		
		private ShowFlag(String flag, String message) {
			this.flag = flag;
			this.message = message;
		}
		
		public String getFlag() {
			return flag;
		}
		public String getMessage() {
			return message;
		}


		public static String flag2HTML(String value) {
			StringBuilder sb = new StringBuilder();
			ShowFlag[] showFlag = ShowFlag.values();
			for (ShowFlag s : showFlag) {
				if (s.flag.equals(value)) {
					sb.append(Htmls.generateSelectedOptionString(s.flag, s.message));
				} else {
					sb.append(Htmls.generateOptionString(s.flag, s.message));
				}
			}
			return sb.toString();
		}
		
		public static String typ2Message(String value) {
			ShowFlag[] showFlag = ShowFlag.values();
			for (ShowFlag s : showFlag) {
				if (s.flag.equals(value)) {
					return s.getMessage();
				}
			}
			return "";
		}
	}
	//预售商品阶段类型
	public static enum DepositType {
		Deposit(1, "订金待支付"), 
		Collect(2, "集中采购中"), 
		WaitPayEnd(3, "尾款待支付 "), 
		End (4, "结束");
		
		private int status;
		private String message;
		
		private DepositType(int status, String message) {
			this.message = message;
			this.status = status;
		}
		
		public int getStatus() {
			return status;
		}
		
		public String getMessage() {
			return message;
		}
		
		public static String typs2HTML(int value) {
			StringBuilder sb = new StringBuilder();
			DepositType[] typs = DepositType.values();
			for (DepositType s : typs) {
				if (value==s.status) {
					sb.append(Htmls.generateSelectedOption(s.status, s.message));
				} else {
					sb.append(Htmls.generateOption(s.status, s.message));
				}
			}
			return sb.toString();
		}
		
		public static String typs2Message(int status) {
			DepositType[] typs = DepositType.values();
			for (DepositType s : typs) {
				if (status==s.status) {
					return s.getMessage();
				}
			}
			return "";
		}
	}
	
	//代言列表状态
	public static enum EndorsementStatus {
		Unchecked(0, "未审核"), 
		Common(1, "正常"), 
		CheckFalse(2, "未通过 "), 
		Deleted(3, "删除 ");
		
		private int status;
		private String message;
		
		private EndorsementStatus(int status, String message) {
			this.message = message;
			this.status = status;
		}
		
		public int getStatus() {
			return status;
		}
		
		public String getMessage() {
			return message;
		}
		
		public static String typs2HTML(int value) {
			StringBuilder sb = new StringBuilder();
			EndorsementStatus[] typs = EndorsementStatus.values();
			for (EndorsementStatus s : typs) {
				if (value==s.status) {
					sb.append(Htmls.generateSelectedOption(s.status, s.message));
				} else {
					sb.append(Htmls.generateOption(s.status, s.message));
				}
			}
			return sb.toString();
		}
		
		public static String typs2Message(int status) {
			EndorsementStatus[] typs = EndorsementStatus.values();
			for (EndorsementStatus s : typs) {
				if (status==s.status) {
					return s.getMessage();
				}
			}
			return "";
		}
	}
	
	//管理员状态
	public static enum AdminStatus {
		STOP(0, "停用"),COMMON(1, "正常");
		
		private int status;
		private String message;
		
		private AdminStatus(int status, String message) {
			this.message = message;
			this.status = status;
		}

		public int getStatus() {
			return status;
		}

		public String getMessage() {
			return message;
		}

		public static String status2HTML(int value) {
			StringBuilder sb = new StringBuilder();
			AdminStatus[] adminStatus = AdminStatus.values();
			sb.append(Htmls.generateOption(0, "全部"));
			for (AdminStatus s : adminStatus) {
				if (value==s.status) {
					sb.append(Htmls.generateSelectedOption(s.status, s.message));
				} else {
					sb.append(Htmls.generateOption(s.status, s.message));
				}
			}
			return sb.toString();
		}
		
		public static String status2Message(int status) {
			AdminStatus[] adminStatus = AdminStatus.values();
			for (AdminStatus s : adminStatus) {
				if (status==s.status) {
					return s.getMessage();
				}
			}
			return "";
		}
	}
	//管理员类型
	public static enum AdminType {
		COMMON("1", "正常"),UNION("2","联营"),OUT("3","商户");
		
		private String type;
		private String message;
		
		private AdminType(String type, String message) {
			this.message = message;
			this.type = type;
		}
		
		public String getType() {
			return type;
		}
		
		public String getMessage() {
			return message;
		}
		
		public static String type2HTML(String value) {
			StringBuilder sb = new StringBuilder();
			AdminType[] adminTypes = AdminType.values();
			sb.append(Htmls.generateOption(0, "全部"));
			for (AdminType s : adminTypes) {
				if (value!=null && value.equals(s.getType())) {
					sb.append(Htmls.generateSelectedOptionString(s.type, s.message));
				} else {
					sb.append(Htmls.generateOptionString(s.type, s.message));
				}
			}
			return sb.toString();
		}
		
		public static String type2Message(String value) {
			AdminType[] adminTypes = AdminType.values();
			for (AdminType s : adminTypes) {
				if (value!=null && value.equals(s.getType())) {
					return s.getMessage();
				}
			}
			return "";
		}
	}
	
	
	/**
	 * 根据组ID获取组名称
	 * @param gid
	 * @return
	 */
	public static String getUserTypeStr(Long gid){
		String result=ServiceFactory.getCacheService().get(groupName_KEY+gid);
		if(StringUtils.isBlank(result)){
			String sql="SELECT gname FROM groupInfo WHERE id="+gid;
			JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
	        db.getPrepareStateDao(sql);
			try {
				ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
				while(rs.next()){
					result = rs.getString("gname");
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.close();
			}
			if(!StringUtils.isBlank(result)){
				ServiceFactory.getCacheService().set(groupName_KEY+gid,result);
			}
		}
		
		return result;
	}
	/**
	 * 根据组ID获取组名称
	 * @param gid
	 * @return
	 */
	public static String getAddressName(Integer id){
		String result=ServiceFactory.getCacheService().get(address_KEY+id);
		if(StringUtils.isBlank(result)){
			String sql="SELECT `name` AS addressname FROM bbtaddress WHERE id="+id;
			JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
			db.getPrepareStateDao(sql);
			try {
				ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
				while(rs.next()){
					result = rs.getString("addressname");
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.close();
			}
			if(!StringUtils.isBlank(result)){
				ServiceFactory.getCacheService().set(address_KEY+id,result);
			}
		}
		return result;
	}
	
	//快递商户模板类型
  	public static enum PadChannelProCounts {
  		FOURPIC(4, "1*4图");
  		
  		private int typ;
  		private String message;
  		
  		private PadChannelProCounts(int typ, String message) {
  			this.message = message;
  			this.typ = typ;
  		}
  		
  		public int getTyp() {
  			return typ;
  		}
  		
  		public String getMessage() {
  			return message;
  		}
  		
  		public static String typs2HTML(int value) {
  			StringBuilder sb = new StringBuilder();
  			PadChannelProCounts[] typs = PadChannelProCounts.values();
  			sb.append(Htmls.generateOption(-1, "全部"));
  			for (PadChannelProCounts s : typs) {
  				if (value==s.typ) {
  					sb.append(Htmls.generateSelectedOption(s.typ, s.message));
  				} else {
  					sb.append(Htmls.generateOption(s.typ, s.message));
  				}
  			}
  			return sb.toString();
  		}
  		
  		public static String typs2Message(int typ) {
  			PadChannelProCounts[] typs = PadChannelProCounts.values();
  			for (PadChannelProCounts s : typs) {
  				if (typ==s.typ) {
  					return s.getMessage();
  				}
  			}
  			return "";
  		}
  	}
	
}
