package actor;


import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import play.Logger;
import play.Play;
import play.Logger.ALogger;
import play.libs.Akka;
import play.libs.Json;
import scala.concurrent.duration.Duration;
import services.ServiceFactory;
import utils.AjaxHelper;
import utils.Constants;
import utils.Dates;
import utils.JdbcOper;
import utils.Numbers;
import utils.StringUtil;
import utils.WSUtils;


/**
 * 向棒棒糖快递员返现
 * @author luobotao
 * @Date 2015年9月16日
 */
public class RefundToBBT extends UntypedActor{
	private final ALogger logger = Logger.of(RefundToBBT.class);
	
	public static ActorRef myActor = Akka.system().actorOf(Props.create(RefundToBBT.class));
	
	@Override
	public void onReceive(Object message) throws Exception {
		if("ACT".equals(message)){
			refundToBBT();
		}
	}

	/**
	 * 查找棒棒糖返现表，进行返现操作
	 */
	private void refundToBBT() {
		//将这些商品的库存进行恢复
		String sql = "select * from wx_user_PriceBack where sta=0";// SQL语句查找未进行返现的数据集
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				Long postman_id=rs.getLong("postmanid");
				int amount=rs.getInt("balance");
				int id=rs.getInt("id");
				int typ=rs.getInt("typ");
				String desc="";
				String sub_catalog="";//收支的描述，比如配送费用，客户表扬，违规扣款
				if(typ==1){//首购
					desc="首购返现";
					sub_catalog="首购返现";
				}else if(typ==2){
					desc="购买收益";
					sub_catalog="购买收益";
				}else if(typ==3){
					desc="站长分成";
					sub_catalog="站长分成";
				}else if(typ==4){
					desc="关注返现";
					sub_catalog="关注返现";
				}else{
					desc="其他";
					sub_catalog="其他";
				}
				String out_trade_no=rs.getString("orderCode");
				int catalog=4;//科目（1-提现，2-奖励，3-惩罚，4-收入）
				
				Map<String,String> getmap=new HashMap<String,String>();
				getmap.put("out_trade_no",out_trade_no);
				String timstr= Dates.formatEngLishDateTime(new Date());
				getmap.put("time_stamp", timstr);
				String signstr=StringUtil.makeSig(getmap);
				
				boolean IsProduct = Play.application().configuration().getBoolean("production", false);
				
				ObjectNode re=Json.newObject();
				re.put("sign", signstr);
				re.put("time_stamp", timstr);
				re.put("postman_id",postman_id.longValue()+"");
				re.put("amount",amount);
				re.put("desc", desc);
				re.put("type", typ);
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
					if(err.equals("ok")){//返现成功
						String sql2="UPDATE wx_user_PriceBack SET sta=1 WHERE id="+id;
						JdbcOper db2 = JdbcOper.getInstance();// 创建DBHelper对象
						try {
							db2.getPrepareStateDao(sql2);
							db2.pst.execute();
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							db2.close();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
	}
}
