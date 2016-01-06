package services.subject;

import java.sql.ResultSet;
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

import models.Parcels;
import models.admin.AdminUser;
import models.product.Channel;
import models.product.Fromsite;
import models.product.Mould;
import models.subject.Subject;
import models.subject.SubjectMould;
import models.subject.SubjectMouldPro;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;

import play.Configuration;
import play.Logger;
import repositories.product.MouldRepository;
import repositories.subject.SubjectMouldProRepository;
import repositories.subject.SubjectMouldRepository;
import repositories.subject.SubjectRepository;
import services.ServiceFactory;
import utils.Constants;
import utils.Dates;
import utils.Htmls;
import utils.JdbcOper;
import utils.Numbers;
import utils.StringUtil;
import vo.OrderVO;
import vo.SubjectMouldProVO;
import vo.SubjectMouldVO;
import forms.admin.SubjectQueryForm;
import forms.order.OrderForm;

/**
 * 
 * <p>Title: SubjectService.java</p> 
 * <p>Description: 专题相关service</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年8月9日  下午5:04:04
 * @version
 */
@Named
@Singleton
public class SubjectService {

    private static final Logger.ALogger logger = Logger.of(SubjectService.class);

    @Inject
    private SubjectRepository subjectRepository;
    @Inject
    private SubjectMouldRepository subjectMouldRepository;
    @Inject
    private MouldRepository mouldRepository;
    @Inject
    private SubjectMouldProRepository subjectMouldProRepository;
    
	/**
	 * 分页查询专题
	 * @param subjectQueryForm
	 * @return
	 */
	public Page<Subject> query(SubjectQueryForm subjectQueryForm) {
		return subjectRepository.findAll(new SubjectQuery(subjectQueryForm),new PageRequest(subjectQueryForm.page, subjectQueryForm.size,new Sort(Direction.DESC, "nsort")));
	}

	/**
	 * 
	 * <p>Title: SubjectService.java</p> 
	 * <p>Description: 专题查询内部类</p> 
	 * <p>Company: higegou</p> 
	 * @author  ctt
	 * date  2015年8月10日  上午9:49:33
	 * @version
	 */
    private static class SubjectQuery implements Specification<Subject> {

        private final SubjectQueryForm form;

        public SubjectQuery(final SubjectQueryForm form) {
            this.form = form;
        }

        @Override
        public Predicate toPredicate(Root<Subject> subject, CriteriaQuery<?> query,
                                     CriteriaBuilder builder) {
            List<Predicate> predicates = new ArrayList<>();
            Path<String> sname = subject.get("sname");
            if(form.sname!=null&&!"".equals(form.sname) ){
            	predicates.add(builder.like(sname, "%"+form.sname + "%"));
            }
            Predicate[] param = new Predicate[predicates.size()];
            predicates.toArray(param);
            return query.where(param).getRestriction();
        }
    }
    
    /**
	 * 删除专题
	 * @param subjectId
	 */
	@Transactional
	public void deleteSubjectById(Long subjectId) {
		subjectRepository.delete(subjectId);
	}
	
	/**
	 * 根据ID获取专题
	 * @param subjectId
	 * @return
	 */
	public Subject getSubjectById(Long subjectId) {
		return subjectRepository.findOne(subjectId);
	}
	
	/**
	 * 
	 * <p>Title: saveSubject</p> 
	 * <p>Description: 保存专题</p> 
	 * @param formPage
	 */
	public Subject saveSubject(Subject subject,SubjectQueryForm formPage) {
		if(subject==null){
			subject = new Subject();
		}
		subject.setSname(formPage.sname);
		subject.setNsort(formPage.nsort);
		subject.setDate_add(new Date());
		subject = save(subject);
		return subject;
	}
	/**
	 * 保存专题
	 * @param subject
	 * @return
	 */
	@Transactional
	public Subject save(Subject subject) {
		return subjectRepository.save(subject);
	}

	/**
	 * 获取所有专题
	 * @return
	 */
	public List<Subject> findAll() {
		return subjectRepository.findAll();
	}
	/**
	 * 获取所有显示的专题
	 * @return
	 */
	public List<Subject> findAllShow() {
		return subjectRepository.findAll(new Specification<Subject>() {
			@Override
			public Predicate toPredicate(Root<Subject> subject, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				Predicate[] param = new Predicate[predicates.size()];
				predicates.toArray(param);
				return query.where(param).getRestriction();
			}
		}, new Sort(Sort.Direction.DESC,"nsort"));
	}

	public Subject getSubjectBySname(String sname) {
		return subjectRepository.getSubjectBySname(sname);
	}

	/**
	 * 
	 * <p>Title: subjectList2Html</p> 
	 * <p>Description: 生成Html中需要的select</p> 
	 * @param subjectList
	 * @param id
	 * @return
	 */
	public static String subjectList2Html(List<Subject> subjectList, Long id) {
		StringBuilder sb = new StringBuilder();
		for (Subject c : subjectList) {
			if (id!=null && id.equals(c.getId().longValue())) {
				sb.append(Htmls.generateSelectedOption(c.getId().longValue(), c.getSname()));
			} else {
				sb.append(Htmls.generateOption(c.getId().longValue(), c.getSname()));
			}
		}
		return sb.toString();
	}
	
	/**
	 * 
	 * <p>Title: findSubjectMouldListBySid</p> 
	 * <p>Description: 根据专题ID获得专题内容</p> 
	 * @param subjectId
	 * @return
	 */
	public List<SubjectMould> findSubjectMouldListBySid(Long subjectId) {
		List<Long> ids = (List<Long>) ServiceFactory.getCacheService().getObject(Constants.subjectMouldIds_KEY+subjectId );
		if(ids==null || ids.size()==0){
			ids = subjectMouldRepository.findSubjectMouldIdsBySid(subjectId);
			ServiceFactory.getCacheService().setObject(Constants.subjectMouldIds_KEY+subjectId,ids,0 );
		}
		List<SubjectMould> subjectMouldList = new ArrayList<SubjectMould>();
		for(Long id:ids){
			SubjectMould subjectMould = getSubjectMouldById(id);
			if(subjectMould!=null){
				subjectMouldList.add(subjectMould);
			}
		}
    	
		return subjectMouldList;
	}

	/**
	 * 根据专题卡片ID获取该专题卡片
	 * @param subjectMouldId
	 */
	public SubjectMould getSubjectMouldById(Long subjectMouldId) {
		SubjectMould subjectMould = (SubjectMould) ServiceFactory.getCacheService().getObject(Constants.subject_mould_KEY+subjectMouldId );//从缓存读入
		if(subjectMould==null){
			subjectMould = subjectMouldRepository.findOne(subjectMouldId);
			ServiceFactory.getCacheService().setObject(Constants.subject_mould_KEY+subjectMouldId, subjectMould,0 );//写入缓存
		}
		return subjectMould;
	}

	/**
	 * 根据卡片ID获取该卡片
	 * @param mouldId
	 * @return
	 */
	public Mould findMouldByMouldId(Long mouldId) {
		Mould mould = (Mould) ServiceFactory.getCacheService().getObject(Constants.mould_KEY+mouldId );//从缓存读入
    	if(mould==null){
    		mould = mouldRepository.findOne(mouldId);
    		ServiceFactory.getCacheService().setObject(Constants.mould_KEY+mouldId, mould,0 );//写入缓存
    	}
		return mould;
	}

	/**
	 * 根据专题卡片ID获取该专题卡片商品列表
	 * @param mouldId
	 * @return
	 */
	public List<SubjectMouldPro> findSuMoProListBySmId(Long smId,Long subjectId) {
		List<SubjectMouldPro> result = new ArrayList<SubjectMouldPro>();
		List<Long> ids = (List<Long>) ServiceFactory.getCacheService().getObject(Constants.subjectMouldProIds_KEY+smId );
		if(ids==null || ids.size()==0){
			List<Integer> tids = subjectMouldProRepository.findIdsBySmid(smId);
			if(tids!=null){
				ids=new ArrayList<Long>();
				for(Integer tid:tids){
					if(tid!=null){
						ids.add(Numbers.parseLong(tid.toString().trim(), 0L));
					}
				}
			}
			ServiceFactory.getCacheService().setObject(Constants.subjectMouldProIds_KEY+smId,ids,0 );
		}
		for(Long id:ids){
			SubjectMouldPro subjectMouldPro = findSuMoPrById(id,subjectId);
			if(subjectMouldPro!=null){
				result.add(subjectMouldPro);
			}
		}
		return result;
	}

	/**
	 * 根据专题卡片商品ID获取该专题卡片商品
	 * @param mouldId
	 * @return
	 */
	public SubjectMouldPro findSuMoPrById(Long id,Long subjectId) {
		SubjectMouldPro subjectMouldPro = (SubjectMouldPro) ServiceFactory.getCacheService().getObject(Constants.subject_mould_pro_KEY+id );//从缓存读入
    	if(subjectMouldPro==null){
    		subjectMouldPro = subjectMouldProRepository.findOne(id);
    		if(subjectMouldPro!=null){
    			String imag=subjectMouldPro.getImgurl()==null?"":subjectMouldPro.getImgurl().replace(StringUtil.getPICDomain(), "");
    			if(imag.indexOf("/pimgs/adload/")>=0 || imag.indexOf("/pimgs")>=0 ||imag.indexOf("/upload")>=0){
    				imag = StringUtil.getPICDomain()+imag;
    			}else{
    				Long pid=subjectMouldPro.getPid();
    				if(pid==null || pid.longValue()==0){
    					imag = StringUtil.getPICDomain()+"/pimgs/adload/"+subjectId+"/"+imag;
    				}else{
    					imag=StringUtil.getListpic(imag);
    				}
    			}
    			subjectMouldPro.setImgurl(imag);
    		}
    		ServiceFactory.getCacheService().setObject(Constants.subject_mould_pro_KEY+id, subjectMouldPro,0 );//写入缓存
    	}
		return subjectMouldPro;
	}

	/**
	 * 获取所有的卡片模版（去除大图和双图）
	 * @param mouldId
	 * @return
	 */
	public List<Mould> findMouldList() {
		List<Long> ids = mouldRepository.findIds();
		List<Mould> result = new ArrayList<Mould>();
		for(Long id:ids){
			Mould mould = findMouldByMouldId(id);
			if(mould!=null){
				result.add(mould);
			}
		}
		return result;
	}

	/**
     * 生成卡片html中需要的select mould select
     * @param mouldList
     * @param id
     * @return
     */
    public static String mouldList2Html(List<Mould> mouldList,Long id){
    	StringBuilder sb = new StringBuilder();
		sb.append(Htmls.generateOptionName(-1,"", "请选择"));
		for (Mould c : mouldList) {
			if (id!=null && id.equals(c.getId().longValue())) {
				sb.append(Htmls.generateSelectedOptionName(c.getId().longValue(), c.getTyp(), c.getMname()));
			} else {
				sb.append(Htmls.generateOptionName(c.getId().longValue(),c.getTyp(), c.getMname()));
			}
		}
		return sb.toString();
    }

    /**
	 * 保存专题卡片内容
	 * @param subjectMouldPro
	 * @param subjectId 
	 * @param smid 
	 */
    @Transactional
	public SubjectMouldPro saveSubjectMouldPro(SubjectMouldPro subjectMouldPro,String subjectId, Long smid) {
		subjectMouldPro = subjectMouldProRepository.save(subjectMouldPro);
		if(subjectMouldPro!=null){
			String imag=subjectMouldPro.getImgurl()==null?"":subjectMouldPro.getImgurl().replace(StringUtil.getPICDomain(), "");
			if(imag.indexOf("/pimgs/adload/")>=0 || imag.indexOf("/pimgs")>=0 ||imag.indexOf("/upload")>=0){
				imag = StringUtil.getPICDomain()+imag;
			}else{
				String kjdImagePre = Configuration.root().getString("kjd.ImagePre","http://image.kjt.com.pre");
				if(!imag.startsWith(kjdImagePre)){
					imag = StringUtil.getPICDomain()+"/pimgs/adload/"+subjectId+"/"+imag;
				}
			}
			subjectMouldPro.setImgurl(imag);
		}
		ServiceFactory.getCacheService().setObject(Constants.subject_mould_pro_KEY+subjectMouldPro.getId(), subjectMouldPro,0 );//写入缓存
		List<Long> ids = new ArrayList<Long>();
		List<Integer> tids =subjectMouldProRepository.findIdsBySmid(smid);
		if(tids!=null){
			ids=new ArrayList<Long>();
			for(Integer tid:tids){
				if(tid!=null){
					ids.add(Numbers.parseLong(tid.toString().trim(), 0L));
				}
			}
		}
		ServiceFactory.getCacheService().setObject(Constants.subjectMouldProIds_KEY+smid,ids,0 );
		SubjectMould subjectMould = getSubjectMouldById(smid);
		Mould mould = null;
		if(subjectMould!=null){
			mould = findMouldByMouldId(subjectMould.getMouldId());
		}
		if(ids!=null && mould!=null && subjectMould!=null && "1".equals(subjectMould.getFlag()) && !"4".equals(mould.getTyp())){
			if(mould.getCnt()!=ids.size())
				subjectMould.setFlag("0");//不展示
			else
				subjectMould.setFlag("1");//展示
			saveSubjectMould(subjectMould, smid);
		}
		return subjectMouldPro;
	}

	/**
	 * 保存专题卡片
	 * @param subjectMould
	 * @return
	 */
	public SubjectMould saveSubjectMould(SubjectMould subjectMould,Long subjectId) {
		subjectMould = subjectMouldRepository.save(subjectMould);
		ServiceFactory.getCacheService().setObject(Constants.subject_mould_KEY+subjectMould.getId(), subjectMould,0 );//写入缓存
		List<Long> ids = (List<Long>) ServiceFactory.getCacheService().getObject(Constants.subjectMouldIds_KEY+subjectId );
		if(ids!=null && !ids.contains(subjectMould.getId())){
			ids.add(subjectMould.getId());
			ServiceFactory.getCacheService().setObject(Constants.subjectMouldIds_KEY+subjectId,ids,0 );
		}
		return subjectMould;
	}

	/**
	 * 根据专题内容ID删除该专题卡片(subjectMould)
	 * @param subjectMouldId
	 */
	public void deletesubjectContentById(Long subjectMouldId,Long subjectId) {
		subjectMouldRepository.delete(subjectMouldId);
		ServiceFactory.getCacheService().setObject(Constants.subject_mould_KEY+subjectMouldId, null,0 );//写入缓存
		//级联删除专题内容商品信息
		List<SubjectMouldPro> subjectMouldPros = findSuMoProListBySmId(subjectMouldId, subjectId);
		subjectMouldProRepository.delete(subjectMouldPros);
		ServiceFactory.getCacheService().setObject(Constants.subjectMouldProIds_KEY+subjectMouldId, null,0 );//写入缓存
		List<Long> ids = subjectMouldRepository.findSubjectMouldIdsBySid(subjectId);
		ServiceFactory.getCacheService().setObject(Constants.subjectMouldIds_KEY+subjectId,ids,0 );
	}

	/**
	 * 删除专题卡片内容商品
	 * @param smpid 商品ID
	 * @param smid 专题卡片ID
	 */
	public void deletesubjectMouldProById(Long smpid,Long smid) {
		subjectMouldProRepository.delete(smpid);
		ServiceFactory.getCacheService().setObject(Constants.subject_mould_pro_KEY+smpid, null,0 );//写入缓存
		List<Long> ids = new ArrayList<Long>();
		List<Integer> tids = subjectMouldProRepository.findIdsBySmid(smid);
		if(tids!=null){
			ids=new ArrayList<Long>();
			for(Integer tid:tids){
				if(tid!=null){
					ids.add(Numbers.parseLong(tid.toString().trim(), 0L));
				}
			}
		}
		ServiceFactory.getCacheService().setObject(Constants.subjectMouldProIds_KEY+smid,ids,0 );
		SubjectMould subjectMould = getSubjectMouldById(smid);
		Mould mould = findMouldByMouldId(subjectMould.getMouldId());
		if(ids!=null && mould!=null ){
			if(mould.getCnt()!=ids.size())
				subjectMould.setFlag("0");//不展示
			else
				subjectMould.setFlag("1");//展示
			saveSubjectMould(subjectMould, smid);
		}
	}

	public List<SubjectMouldPro> findSuMoPrByPId(Long pid) {
		return subjectMouldProRepository.findByPid(pid);
	}

	public Long getTotalsWithForm(SubjectQueryForm form) {
		StringBuffer sb = new StringBuffer("SELECT count(sm.`id`) ")
		.append(" FROM `subject` s , `subject_mould` sm, `mould` m ");
		if(!Strings.isNullOrEmpty(form.pidOrNewSku)){
			sb.append(",`product` p, `subject_mould_pro` smp ") ;
		}
		sb.append(" WHERE s.`id`= sm.`sid` AND sm.`mouldId`=m.`id` ");
		//专题id
		if(!Strings.isNullOrEmpty(form.sid)&&!"-1".equals(form.sid)){
			sb.append(" AND s.`id`= ").append(form.sid);
		}
		//商品ID或newsku
		if (!Strings.isNullOrEmpty(form.pidOrNewSku)) {
			sb.append(" and sm.`id`=smp.`smid` AND smp.`pid`=p.`pid` and ").append("(p.`pid` = ").append(form.pidOrNewSku).append(" or p.newSku='").append(form.pidOrNewSku).append("') ");
		}
		if (!Strings.isNullOrEmpty(form.startNsort)) {
			sb.append(" and ").append("sm.`nsort` >= ").append(form.startNsort);
		}
		if (!Strings.isNullOrEmpty(form.endNsort)) {
			sb.append(" and ").append("sm.`nsort` >= ").append(form.endNsort);
		}
		String sql = sb.toString();
        logger.info(sql+"===============");
        JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
        Long totals = (long) 0;
        db.getPrepareStateDao(sql);
		try {
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				totals = rs.getLong(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return totals;
	}
	/**
	 * 
	 * <p>Title: findSubjectMouldListByForm</p> 
	 * <p>Description: 根据条件查询出指定专题卡片集合</p> 
	 * @param formPage
	 * @return
	 */
	public List<SubjectMouldVO> querySubjectMouldListByForm(
			SubjectQueryForm form) {
		StringBuffer sb = new StringBuffer("SELECT s.`id`,sm.`id`,m.`id`,m.`mname`,sm.`nsort`,sm.`flag`,sm.`manType` ")
				.append(" FROM `subject` s , `subject_mould` sm, `mould` m ");
		if(!Strings.isNullOrEmpty(form.pidOrNewSku)){
			sb.append(",`product` p, `subject_mould_pro` smp ") ;
        }
		sb.append(" WHERE s.`id`= sm.`sid` AND sm.`mouldId`=m.`id` ");
		//专题id
		if(!Strings.isNullOrEmpty(form.sid)&&!"-1".equals(form.sid)){
			sb.append(" AND s.`id`= ").append(form.sid);
		}
		//商品ID或newsku
        if (!Strings.isNullOrEmpty(form.pidOrNewSku)) {
        	sb.append(" and sm.`id`=smp.`smid` AND smp.`pid`=p.`pid` and ").append("(p.`pid` = ").append(form.pidOrNewSku).append(" or p.newSku='").append(form.pidOrNewSku).append("') ");
        }
        if (!Strings.isNullOrEmpty(form.startNsort)) {
        	sb.append(" and ").append("sm.`nsort` >= ").append(form.startNsort);
        }
        if (!Strings.isNullOrEmpty(form.endNsort)) {
        	sb.append(" and ").append("sm.`nsort` <= ").append(form.endNsort);
        }
        sb.append(" order by sm.`nsort` desc, sm.`id` desc ");
        sb.append("limit ").append(form.page*form.size).append(",").append(form.size).append(" ");
        String sql = sb.toString();
        logger.info(sql); 
        JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
        List<SubjectMouldVO> result = new ArrayList<SubjectMouldVO>();
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				long sid = rs.getLong(1);
				long smid = rs.getLong(2);
				long mid = rs.getLong(3);
				String mname = rs.getString(4);
				int nsort = rs.getInt(5);
				String flag = rs.getString(6);
				String manType = rs.getString(7);
				//添加专题信息
				SubjectMouldVO smvo = new SubjectMouldVO();
				smvo.flag=flag;
				smvo.manType=manType;
				smvo.mname=mname;
				smvo.mouldId=mid;
				smvo.nsort=nsort;
				smvo.sid=sid;
				smvo.smid=smid;
				//将subjectmould加入到result中
				result.add(smvo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

}
