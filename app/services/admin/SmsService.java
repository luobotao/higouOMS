package services.admin;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.SmsInfo;
import play.Logger;
import repositories.admin.SmsRepository;


/**
 * 对短信表操作service
 * @author luobotao
 * @Date 2015年7月27日
 */
@Named
@Singleton
public class SmsService extends Thread{
	private static final Logger.ALogger LOGGER = Logger.of(SmsService.class);
    @Inject
    private SmsRepository smsRepository;
    

	public  List<SmsInfo> saveSmsList(List<SmsInfo> smsInfoList) {
		return smsRepository.save(smsInfoList);
		
	}
	
	public SmsInfo saveSmsInfo(String args,String phone,String tpl_id,String type){
		LOGGER.info("args is "+args+ "; Phone is "+phone+";tpl_id is "+tpl_id);
		SmsInfo smsInfo = new SmsInfo();
		smsInfo.setFlag("0");//未发送
		smsInfo.setTpl_id(tpl_id);
		smsInfo.setPhone(phone);
		smsInfo.setArgs(args);
		smsInfo.setType(type);//1普通短信 2营销短信 3语音短信
		smsInfo.setInsertTime(new Date());
		smsInfo.setUpdateTime(new Date());
		return smsRepository.save(smsInfo);
		
	}
}