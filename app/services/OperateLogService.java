package services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import forms.OperateLogQueryForm;
import models.admin.OperateLog;
import models.admin.OperateLog.LogType;
import repositories.OperateLogRepository;
import utils.Constants.ProductStatus;
import utils.Constants.ShowFlag;
import utils.Dates;

/**
 * 日志操作
 * 
 * @author luobotao
 * @Date 2015年9月11日
 */
@Named
@Singleton
public class OperateLogService {


	@Inject
	private OperateLogRepository operateLogRepository;

	public Page<OperateLog> findOperateLogPage(final OperateLogQueryForm form) {
		return operateLogRepository.findAll(new Specification<OperateLog>() {

			@Override
			public Predicate toPredicate(Root<OperateLog> operateLog, CriteriaQuery<?> query, CriteriaBuilder builder) {
				Path<Date> date_add = operateLog.get("dateAdd"); // 日志产生时间
				List<Predicate> predicates = new ArrayList<>();
				if (form.between != null) {
					if (form.between.start.equals(form.between.end)) {
						predicates.add(builder.greaterThanOrEqualTo(date_add, form.between.start));
						predicates.add(builder.lessThanOrEqualTo(date_add, Dates.getEndOfDay(form.between.start)));
					} else {
						predicates.add(builder.greaterThanOrEqualTo(date_add, form.between.start));
						predicates.add(builder.lessThanOrEqualTo(date_add, form.between.end));
					}
				}
				Predicate[] param = new Predicate[predicates.size()];

				predicates.toArray(param);
				return query.where(param).getRestriction();
			}

		}, new PageRequest(form.page, form.size, new Sort(Direction.DESC, "id")));
	}

	public OperateLog create(Long adminid, String adminname, String typ, String remark, String ip, LogType opType) {
		OperateLog operateLog = new OperateLog();
		operateLog.setAdminid(adminid);
		operateLog.setAdminname(adminname);
		operateLog.setDateAdd(new Date());
		operateLog.setIp(ip);
		operateLog.setTyp(typ);
		operateLog.setRemark(remark);
		operateLog.setOpType(opType);
		return operateLog;
	}

	/**
	 * 登录日志
	 * 
	 * @param id
	 * @param name
	 * @param ip
	 */
	public void saveLoginLog(Long id, String name, String ip) {
		operateLogRepository.save(createLoginLog(id, name, ip));
	}

	private OperateLog createLoginLog(Long adminUserId, String adminUserName, String ip) {
		return create(adminUserId, adminUserName, LogType.LOGIN.getMessage(), adminUserName + "登录", ip, LogType.LOGIN);
	}

	/**
	 * 登出日志
	 * 
	 * @param id
	 * @param name
	 * @param ip
	 */
	public void saveLogoutLog(Long id, String name, String ip) {
		operateLogRepository.save(createLogoutLog(id, name, ip));
	}

	private OperateLog createLogoutLog(Long adminUserId, String adminUserName, String ip) {
		return create(adminUserId, adminUserName, LogType.LOGOUT.getMessage(), adminUserName + "登出", ip,LogType.LOGOUT);
	}
	
	/**
	 * 商品日志
	 * @param id
	 * @param name
	 * @param ip
	 * @param remark
	 */
	public void saveProductLog(Long id, String name, String ip,String remark) {
		operateLogRepository.save(createProductLog(id, name, ip,remark));
	}

	private OperateLog createProductLog(Long adminUserId, String adminUserName, String ip,String remark) {
		return create(adminUserId, adminUserName, LogType.PRODUCT.getMessage(), adminUserName+remark, ip,LogType.PRODUCT);
	}
	/**
	 * 商品库存日志
	 * @param adminUserId
	 * @param adminUserName
	 * @param ip
	 * @param pid
	 * @param nstockOld
	 * @param nstockNow
	 */
	public void saveProductNstockLog(Long adminUserId, String adminUserName, String ip,Long pid,Long nstockOld,Long nstockNow ) {
		operateLogRepository.save(createProductNstockLog(adminUserId, adminUserName, ip,pid,nstockOld,nstockNow));
	}

	private OperateLog createProductNstockLog(Long adminUserId, String adminUserName, String ip,Long pid,Long nstockOld,Long nstockNow ) {
		return create(adminUserId, adminUserName, LogType.NSTOCK.getMessage(), adminUserName+"将商品PID为"+pid+"的库存由"+nstockOld+"修改为"+nstockNow, ip,LogType.NSTOCK);
	}
	/**
	 * 商品嗨购售价日志
	 * @param adminUserId
	 * @param adminUserName
	 * @param ip
	 * @param pid
	 * @param nstockOld
	 * @param nstockNow
	 */
	public void saveProductHigouPriceLog(Long adminUserId, String adminUserName, String ip,Long pid,double priceOld,double priceNow ) {
		operateLogRepository.save(createProductHigouPriceLog(adminUserId, adminUserName, ip,pid,priceOld,priceNow));
	}

	private OperateLog createProductHigouPriceLog(Long adminUserId, String adminUserName, String ip,Long pid,double priceOld,double priceNow ) {
		return create(adminUserId, adminUserName, LogType.HIGOUPRICE.getMessage(), adminUserName+"将商品PID为"+pid+"的嗨购售价由"+priceOld+"修改为"+priceNow, ip,LogType.HIGOUPRICE);
	}
	/**
	 * 商品状态日志(上架下架)
	 * @param adminUserId
	 * @param adminUserName
	 * @param ip
	 * @param pid
	 * @param statusOld
	 * @param statusNow
	 */
	public void saveProductStateLog(Long adminUserId, String adminUserName, String ip,Long pid,int statusOld,int statusNow ) {
		operateLogRepository.save(createProductStateLog(adminUserId, adminUserName, ip,pid,statusOld,statusNow));
	}

	private OperateLog createProductStateLog(Long adminUserId, String adminUserName, String ip,Long pid,int statusOld,int statusNow ) {
		return create(adminUserId, adminUserName, LogType.PRODUCTSTATUS.getMessage(), adminUserName+"将商品PID为"+pid+"的商品状态由"+ProductStatus.status2Message(statusOld)+"修改为"+ProductStatus.status2Message(statusNow), ip,LogType.PRODUCTSTATUS);
	}
	
	
	/**
	 * 频道日志
	 * @param adminUserId
	 * @param adminUserName
	 * @param ip
	 * @param remark
	 */
	public void saveChannelLog(Long adminUserId, String adminUserName, String ip,String remark) {
		operateLogRepository.save(createChannelLog(adminUserId, adminUserName, ip,remark));
	}

	private OperateLog createChannelLog(Long adminUserId, String adminUserName, String ip,String remark ) {
		return create(adminUserId, adminUserName, LogType.CHANNEL.getMessage(), adminUserName+remark, ip,LogType.CHANNEL);
	}
	
	/**
	 * 频道日志(显示隐藏)
	 * @param adminUserId
	 * @param adminUserName
	 * @param ip
	 * @param cid
	 * @param statusOld
	 * @param statusNow
	 */
	public void saveChannelStateLog(Long adminUserId, String adminUserName, String ip,Long cid,String statusNow ) {
		operateLogRepository.save(createChannelLog(adminUserId, adminUserName, ip,cid,statusNow));
	}

	private OperateLog createChannelLog(Long adminUserId, String adminUserName, String ip,Long cid,String statusNow ) {
		return create(adminUserId, adminUserName, LogType.CHANNEL.getMessage(), adminUserName+"将频道ID为"+cid+"的频道状态修改为"+ShowFlag.typ2Message(statusNow), ip,LogType.CHANNEL);
	}
	
	/**
	 * 频道卡片日志
	 * @param adminUserId
	 * @param adminUserName
	 * @param ip
	 * @param remark
	 */
	public void saveChannelMouldLog(Long adminUserId, String adminUserName, String ip,String remark) {
		operateLogRepository.save(createChannelMouldLog(adminUserId, adminUserName, ip,remark));
	}

	private OperateLog createChannelMouldLog(Long adminUserId, String adminUserName, String ip,String remark ) {
		return create(adminUserId, adminUserName, LogType.CHANNELMOULD.getMessage(), adminUserName+remark, ip,LogType.CHANNELMOULD);
	}
	
	/**
	 * 频道卡片状态日志(显示隐藏)
	 * @param adminUserId
	 * @param adminUserName
	 * @param ip
	 * @param cid
	 * @param statusOld
	 * @param statusNow
	 */
	public void saveChannelMouldStateLog(Long adminUserId, String adminUserName, String ip,Long cid,String statusNow ) {
		operateLogRepository.save(createChannelMouldLog(adminUserId, adminUserName, ip,cid,statusNow));
	}

	private OperateLog createChannelMouldLog(Long adminUserId, String adminUserName, String ip,Long cid,String statusNow ) {
		String statusMessage="隐藏";
		if("1".equals(statusNow)){
			statusMessage="显示";
		}
		return create(adminUserId, adminUserName, LogType.CHANNELMOULD.getMessage(), adminUserName+"将频道卡片ID为"+cid+"的频道卡片状态修改为"+statusMessage, ip,LogType.CHANNELMOULD);
	}

	/**
	 * 频道卡片内容日志
	 * @param adminUserId
	 * @param adminUserName
	 * @param ip
	 * @param remark
	 */
	public void saveChannelMouldProLog(Long adminUserId, String adminUserName, String ip,String remark) {
		operateLogRepository.save(createChannelMouldProLog(adminUserId, adminUserName, ip,remark));
		
	}
	private OperateLog createChannelMouldProLog(Long adminUserId, String adminUserName, String ip,String remark ) {
		return create(adminUserId, adminUserName, LogType.CHANNELMOULDPRO.getMessage(), adminUserName+remark, ip,LogType.CHANNELMOULDPRO);
	}
	
	/**
	 * 专题日志
	 * @param adminUserId
	 * @param adminUserName
	 * @param ip
	 * @param remark
	 */
	public void saveSubjectLog(Long adminUserId, String adminUserName, String ip,String remark) {
		operateLogRepository.save(createSubjectLog(adminUserId, adminUserName, ip,remark));
	}

	private OperateLog createSubjectLog(Long adminUserId, String adminUserName, String ip,String remark ) {
		return create(adminUserId, adminUserName, LogType.SUBJECT.getMessage(), adminUserName+remark, ip,LogType.SUBJECT);
	}
	
	
	/**
	 * 专题卡片日志
	 * @param adminUserId
	 * @param adminUserName
	 * @param ip
	 * @param remark
	 */
	public void saveSubjectMouldLog(Long adminUserId, String adminUserName, String ip,String remark) {
		operateLogRepository.save(createSubjectMouldLog(adminUserId, adminUserName, ip,remark));
	}

	private OperateLog createSubjectMouldLog(Long adminUserId, String adminUserName, String ip,String remark ) {
		return create(adminUserId, adminUserName, LogType.SUBJECTMOULD.getMessage(), adminUserName+remark, ip,LogType.SUBJECTMOULD);
	}
	
	/**
	 * 频道卡片状态日志(显示隐藏)
	 * @param adminUserId
	 * @param adminUserName
	 * @param ip
	 * @param cid
	 * @param statusOld
	 * @param statusNow
	 */
	public void saveSubjectMouldStateLog(Long adminUserId, String adminUserName, String ip,Long cid,String statusNow ) {
		operateLogRepository.save(createSubjectMouldLog(adminUserId, adminUserName, ip,cid,statusNow));
	}

	private OperateLog createSubjectMouldLog(Long adminUserId, String adminUserName, String ip,Long cid,String statusNow ) {
		String statusMessage="隐藏";
		if("1".equals(statusNow)){
			statusMessage="显示";
		}
		return create(adminUserId, adminUserName, LogType.SUBJECTMOULD.getMessage(), adminUserName+"将专题卡片ID为"+cid+"的专题卡片状态修改为"+statusMessage, ip,LogType.SUBJECTMOULD);
	}

	/**
	 * 专题卡片内容日志
	 * @param adminUserId
	 * @param adminUserName
	 * @param ip
	 * @param remark
	 */
	public void saveSubjectMouldProLog(Long adminUserId, String adminUserName, String ip,String remark) {
		operateLogRepository.save(createSubjectMouldProLog(adminUserId, adminUserName, ip,remark));
		
	}
	private OperateLog createSubjectMouldProLog(Long adminUserId, String adminUserName, String ip,String remark ) {
		return create(adminUserId, adminUserName, LogType.CHANNELMOULDPRO.getMessage(), adminUserName+remark, ip,LogType.CHANNELMOULDPRO);
	}
	
	/**
	 * 订单日志
	 * @param adminUserId
	 * @param adminUserName
	 * @param ip
	 * @param remark
	 */
	public void saveOrderLog(Long adminUserId, String adminUserName, String ip,String remark) {
		operateLogRepository.save(createOrderLog(adminUserId, adminUserName, ip,remark));
	}

	private OperateLog createOrderLog(Long adminUserId, String adminUserName, String ip,String remark ) {
		return create(adminUserId, adminUserName, LogType.ORDER.getMessage(), adminUserName+remark, ip,LogType.ORDER);
	}
	
	/**
	 * 包裹日志
	 * @param adminUserId
	 * @param adminUserName
	 * @param ip
	 * @param remark
	 */
	public void saveParcelsLog(Long adminUserId, String adminUserName, String ip,String remark) {
		operateLogRepository.save(createParcelsLog(adminUserId, adminUserName, ip,remark));
	}

	private OperateLog createParcelsLog(Long adminUserId, String adminUserName, String ip,String remark ) {
		return create(adminUserId, adminUserName, LogType.PARCELS.getMessage(), adminUserName+remark, ip,LogType.PARCELS);
	}
	/**
	 * 物流日志
	 * @param adminUserId
	 * @param adminUserName
	 * @param ip
	 * @param remark
	 */
	public void saveWaybillLog(Long adminUserId, String adminUserName, String ip,String remark) {
		operateLogRepository.save(createWaybillLog(adminUserId, adminUserName, ip,remark));
	}
	
	private OperateLog createWaybillLog(Long adminUserId, String adminUserName, String ip,String remark ) {
		return create(adminUserId, adminUserName, LogType.WAYBILL.getMessage(), adminUserName+remark, ip,LogType.WAYBILL);
	}
	/**
	 * 组合商品
	 * @param adminUserId
	 * @param adminUserName
	 * @param ip
	 * @param remark
	 */
	@Transactional
	public void saveProductGroupLog(Long adminUserId, String adminUserName, String ip,String remark) {
		operateLogRepository.save(createProductGroupLog(adminUserId, adminUserName, ip,remark));
	}
	
	private OperateLog createProductGroupLog(Long adminUserId, String adminUserName, String ip,String remark ) {
		return create(adminUserId, adminUserName, LogType.PRODUCTGROUP.getMessage(), adminUserName+remark, ip,LogType.PRODUCTGROUP);
	}
	
	@Transactional
	public void saveAdminUserLog(Long adminUserId, String adminUserName, String ip,String remark) {
		operateLogRepository.save(createAdminUserLog(adminUserId, adminUserName, ip,remark));
	}
	
	public OperateLog createAdminUserLog(Long adminUserId, String adminUserName, String ip,String remark ) {
		return create(adminUserId, adminUserName, LogType.ADMINUSER.getMessage(), adminUserName+remark, ip,LogType.ADMINUSER);
	}
}
