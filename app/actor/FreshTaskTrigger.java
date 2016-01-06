package actor;



import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import play.Logger;
import utils.JdbcOper;

public class FreshTaskTrigger extends Thread{
	
	
	public void run() {
		while(true){
			try {
				TimeUnit.SECONDS.sleep(600);
				startFreshTask();
			} catch (InterruptedException e) {
				e.printStackTrace();
				
			}
		}
	}
	
	
	/**
	 * 查找是否存在可以执行的任务单
	 */
	private void startFreshTask(){
		 String sql = "{call run_freshTask()}";//SQL语句  //调用存储过程
    	 JdbcOper  db = JdbcOper.getInstance();//创建DBHelper对象  
         try {  
        	 db.getPrepareStateDao(sql);
        	 db.pst.execute();//执行语句
         } catch (SQLException e) {  
             e.printStackTrace();  
         }finally{
        	 db.close();
         }  
	}
}
