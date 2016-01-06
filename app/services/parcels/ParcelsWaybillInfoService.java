package services.parcels;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.ParcelsWaybillInfo;
import play.Logger;
import repositories.KdCompanyRepository;
import repositories.parcels.ParcelsWaybillInfoRepository;
/**
 * 
 * <p>Title: ParcelsWaybillInfoService.java</p> 
 * <p>Description: 包裹物流详细信息</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年7月31日  下午6:41:44
 * @version
 */
@Named
@Singleton
public class ParcelsWaybillInfoService {

    private static final Logger.ALogger logger = Logger.of(ParcelsWaybillInfoService.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Inject
    private ParcelsWaybillInfoRepository parcelsWaybillInfoRepository;
    
    /**
     * 
     * <p>Title: getAllByWayBillId</p> 
     * <p>Description: 根据waybillID获取到waybillInfo的详细物流信息</p> 
     * @param wayBillId
     * @return
     */
	public List<ParcelsWaybillInfo> getAllByWayBillId(long wayBillId) {
		return parcelsWaybillInfoRepository.getAllByWayBillId(wayBillId);
	}
    
    
    
    
   
}
