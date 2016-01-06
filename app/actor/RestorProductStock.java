package actor;


import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import play.Logger;
import play.Logger.ALogger;
import play.libs.Akka;
import scala.concurrent.duration.Duration;
import services.ServiceFactory;
import utils.Constants;
import utils.JdbcOper;


/**
 * 释放未支付订单的库存
 * @author luobotao
 * @Date 2015年9月16日
 */
public class RestorProductStock extends UntypedActor{
	private final ALogger logger = Logger.of(RestorProductStock.class);
	
	public static ActorRef myActor = Akka.system().actorOf(Props.create(RestorProductStock.class));
	@Override
	public void onReceive(Object message) throws Exception {
		if("ACT".equals(message)){
			restoreProductStock();
		}
	}

	/**
	 * 查找订单列表中未付款的订单，将该订单置成取消，并将库存进行恢复
	 */
	private void restoreProductStock() {
		//将这些商品的库存进行恢复
		String sql = "{call run_auto_ntock ()}";// SQL语句// //调用存储过程
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				Long pid = rs.getLong("pId");
				logger.info(pid+"======update nstock======");
				ServiceFactory.getCacheService().clear(Constants.product_KEY + pid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
	}
}
