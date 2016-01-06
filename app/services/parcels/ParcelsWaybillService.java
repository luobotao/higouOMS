package services.parcels;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;

import forms.parcels.ParcelsForm;
import models.KdCompany;
import models.Parcels;
import models.ParcelsWaybill;
import play.Logger;
import repositories.KdCompanyRepository;
import repositories.parcels.ParcelsWaybillRepository;
import utils.kuaidi100.Kuaidi100;
/**
 * 
 * <p>Title: ParcelsWaybillService.java</p> 
 * <p>Description: 包裹物流信息</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年7月31日  下午6:41:44
 * @version
 */
@Named
@Singleton
public class ParcelsWaybillService {

    private static final Logger.ALogger logger = Logger.of(ParcelsWaybillService.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Inject
    private ParcelsWaybillRepository parcelsWaybillRepository;
    @Inject
    private KdCompanyRepository kdCompanyRepository;
    
    
	public ParcelsWaybill save(Parcels parcels, String remark, int nsort) {
		ParcelsWaybill pwb = new ParcelsWaybill();
		pwb.setDate_add(new Date());
		pwb.setPardelsId(parcels.getId());
		pwb.setDate_upd(CHINESE_DATE_TIME_FORMAT.format(new Date()));
		pwb.setRemark(remark);
		pwb.setDate_txt(CHINESE_DATE_TIME_FORMAT.format(new Date()));
		pwb.setNsort(nsort);
		pwb = parcelsWaybillRepository.save(pwb);
		return pwb;
		//ServiceFactory.getCacheService().setObject(Constants.parcelsWaybill_KEY+pwb.getId(), pwb, 0);//将包裹物流写入cache
	}

	/**
	 * 
	 * <p>Title: getAllWaybillInfoWithParcelsId</p> 
	 * <p>Description: 根据包裹Id查找出相应的物流信息集合</p> 
	 * @param id
	 * @return
	 */
	public List<ParcelsWaybill> getAllWaybillInfoWithParcelsId(Long id) {
		return parcelsWaybillRepository.getAllWaybillInfoWithParcelsId(id);
	}

	/**
	 * 
	 * <p>Title: saveWithWaybillForm</p> 
	 * <p>Description: 根据表单内容保存物流信息</p> 
	 * @param formPage
	 */
	@Transactional
	public ParcelsWaybill saveWithWaybillForm(ParcelsForm form) {
		ParcelsWaybill pwb = new ParcelsWaybill();
		KdCompany kdCompany = null;
		if(form.between!=null){
			pwb.setDate_add(form.addDate);
			pwb.setDate_upd(CHINESE_DATE_TIME_FORMAT.format(form.addDate));
			pwb.setDate_txt(CHINESE_DATE_TIME_FORMAT.format(form.addDate));
		}else{
			pwb.setDate_add(new Date());
			pwb.setDate_upd(CHINESE_DATE_TIME_FORMAT.format(new Date()));
			pwb.setDate_txt(CHINESE_DATE_TIME_FORMAT.format(new Date()));
		}
		if(form.kdCompany!=null&&!"".equals(form.kdCompany)){
			kdCompany = kdCompanyRepository.getOne(Integer.parseInt(form.kdCompany));
			pwb.setTransport(kdCompany.getCnName());
			pwb.setTrancode(kdCompany.getCode());
		}
		pwb.setPardelsId(form.parId);
		pwb.setWaybillCode(form.waybillCode);
		if(!Strings.isNullOrEmpty(form.remark)){
			pwb.setRemark(form.remark.trim());
		}else if(kdCompany!=null){
			pwb.setRemark("由"+kdCompany.getCnName()+"发货，快递单号："+form.waybillCode);
		}
		pwb.setNsort(form.nsort);
		pwb = parcelsWaybillRepository.save(pwb);
		
		if(kdCompany!=null){
			boolean flag = Kuaidi100.subscribe(kdCompany.getCode(), form.waybillCode,"","");
	  		if(!flag){
	  			logger.info("快递100订阅失败，包裹号："+form.waybillCode+" 物流单号："+form.waybillCode);
	  		}else{
	  			logger.info("快递100订阅成功，包裹号："+form.waybillCode+" 物流单号："+form.waybillCode);
	  		}
		}
		
		return pwb;
	}

	public void delWayBill(Long wbId) {
		parcelsWaybillRepository.delete(wbId);
	}
	
	/**
	 * 
	 * <p>Title: updateWayBill</p> 
	 * <p>Description: 更新物流信息的排序</p> 
	 * @param parseLong
	 * @param parseLong2
	 */
	@Transactional
	public void updateWayBill(long wbId, int nsort) {
		ParcelsWaybill pwb = parcelsWaybillRepository.getOne(wbId);
		pwb.setNsort(nsort);
		parcelsWaybillRepository.save(pwb);
	}

	/**
	 * 
	 * <p>Title: getWaybillInfoWithParcelsIdAndRemark</p> 
	 * <p>Description: 查找指定的物流信息是否已入库</p> 
	 * @param id
	 * @param remark
	 * @return
	 */
	public List<ParcelsWaybill> getWaybillInfoWithParcelsIdAndRemark(Long parcelsId,
			String remark) {
		return parcelsWaybillRepository.getWaybillInfoWithParcelsIdAndRemark(parcelsId,remark);
	}
    
   
    
   
}
