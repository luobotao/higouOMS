package services.product;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;

import forms.admin.ChannelQueryForm;
import models.product.Channel;
import models.product.ChannelMould;
import models.product.ChannelMouldPro;
import models.product.Mould;
import models.product.PadChannel;
import models.product.PadChannelPro;
import models.product.PadChannelProView;
import models.product.Product;
import play.Configuration;
import play.Logger;
import play.Play;
import repositories.product.ChannelMouldProRepository;
import repositories.product.ChannelMouldRepository;
import repositories.product.ChannelRepository;
import repositories.product.MouldRepository;
import repositories.product.PadChannelProRepository;
import repositories.product.PadChannelProViewRepository;
import repositories.product.PadChannelRepository;
import services.ServiceFactory;
import utils.Constants;
import utils.Htmls;
import utils.JdbcOper;
import utils.Numbers;
import utils.StringUtil;
import vo.ChannelMouldVO;

/**
 * 频道相关Service
 * @author luobotao
 * Date: 2015年4月17日 下午2:26:14
 */
@Named
@Singleton
public class ChannelService {

    private static final Logger.ALogger logger = Logger.of(ChannelService.class);

    @Inject
    private ChannelRepository channelRepository;
    @Inject
    private PadChannelRepository padChannelRepository;
    @Inject
    private PadChannelProRepository padChannelProRepository;
    @Inject
    private PadChannelProViewRepository padChannelProViewRepository;
    @Inject
    private ChannelMouldRepository channelMouldRepository;
    @Inject
    private ChannelMouldProRepository channelMouldProRepository;
    @Inject
    private MouldRepository mouldRepository;

	/**
	 * 分页查询频道
	 * @param channelQueryForm
	 * @return
	 */
	public Page<Channel> query(ChannelQueryForm channelQueryForm) {
		return channelRepository.findAll(new PageRequest(channelQueryForm.page, channelQueryForm.size,new Sort(Direction.DESC, "nsort")));
	}

	/**
	 * 保存频道
	 * @param channel
	 * @return
	 */
	public Channel saveChannel(Channel channel) {
		return channelRepository.save(channel);
	}

	/**
	 * 根据ID获取频道
	 * @param channelId
	 * @return
	 */
	public Channel findById(Long channelId) {
		return channelRepository.findById(channelId);
	}

	/**
	 * 删除频道
	 * @param channelId
	 */
	@Transactional
	public void deletechannelById(Long channelId) {
		channelRepository.deleteById(channelId);
	}

	/**
	 * 修改频道显示状态
	 * @param channelId
	 * @param channelsta
	 */
	@Transactional
	public void updateChannelSta(Long channelId, String channelsta) {
		channelRepository.updateChannelSta(channelId,channelsta);
	}

	/**
	 * 获取所有频道
	 * @return
	 */
	public List<Channel> findAll() {
		return channelRepository.findAll(new Sort(Sort.Direction.DESC,"nsort"));
	}
	/**
	 * 获取所有显示的频道
	 * @return
	 */
	public List<Channel> findAllShow() {
		return channelRepository.findAll(new Specification<Channel>() {
			@Override
			public Predicate toPredicate(Root<Channel> channel, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				Path<String> sta = channel.get("sta");
				predicates.add(builder.equal(sta, "1"));
				Predicate[] param = new Predicate[predicates.size()];
				predicates.toArray(param);
				return query.where(param).getRestriction();
			}
		}, new Sort(Sort.Direction.DESC,"nsort"));
	}
	
	/**
     * 生成html中需要的select
     * @param categoryList
     * @param id
     * @return
     */
    public static String channelList2Html(List<Channel> channelList,Long id){
    	StringBuilder sb = new StringBuilder();
		for (Channel c : channelList) {
			if (id!=null && id.equals(c.getId().longValue())) {
				sb.append(Htmls.generateSelectedOption(c.getId().longValue(), c.getCname()));
			} else {
				sb.append(Htmls.generateOption(c.getId().longValue(), c.getCname()));
			}
		}
		return sb.toString();
    }

	/**
	 * 根据频道ID获取下面的频道内容
	 * @param channelId
	 * @return
	 */
	public List<ChannelMould> findChannelMouldListByCid(Long channelId) {
		List<Long> ids =null;
		try{
			ids=(List<Long>) ServiceFactory.getCacheService().getObject(Constants.channelMouldIds_KEY+channelId );
		}
		catch(Exception e){
			logger.info("error cache key:"+Constants.channelMouldIds_KEY+channelId);
		}
		if(ids==null || ids.size()==0){
			ids = channelMouldRepository.findChannelMouldIdsByCid(channelId);
			ServiceFactory.getCacheService().setObject(Constants.channelMouldIds_KEY+channelId,ids,0 );
		}
		List<ChannelMould> channelMouldList = new ArrayList<ChannelMould>();
		for(Long id:ids){
			ChannelMould channelMould = getChannelMouldById(id);
			if(channelMould!=null){
				channelMouldList.add(channelMould);
			}
		}
    	
		return channelMouldList;
	}

	/**
	 * 根据频道卡片ID获取该频道卡片
	 * @param channelMouldId
	 */
	public ChannelMould getChannelMouldById(Long channelMouldId) {
		ChannelMould channelMould = (ChannelMould) ServiceFactory.getCacheService().getObject(Constants.channel_mould_KEY+channelMouldId );//从缓存读入
		if(channelMould==null){
			channelMould = channelMouldRepository.findOne(channelMouldId);
			ServiceFactory.getCacheService().setObject(Constants.channel_mould_KEY+channelMouldId, channelMould,0 );//写入缓存
		}
		return channelMould;
	}
	
	/**
	 * 根据频道内容ID删除该频道卡片(channelMould)
	 * @param channelMouldId
	 */
	public void deletechannelMouldById(Long channelMouldId,Long channelId) {
		channelMouldRepository.delete(channelMouldId);
		ServiceFactory.getCacheService().setObject(Constants.channel_mould_KEY+channelMouldId, null,0 );//写入缓存
		//级联删除频道内容商品信息
		List<ChannelMouldPro> channelMouldPros = findChMoProListByCmId(channelMouldId, channelId);
		channelMouldProRepository.delete(channelMouldPros);
		ServiceFactory.getCacheService().setObject(Constants.channelMouldProIds_KEY+channelMouldId, null,0 );//写入缓存
		List<Long> ids = channelMouldRepository.findChannelMouldIdsByCid(channelId);
		ServiceFactory.getCacheService().setObject(Constants.channelMouldIds_KEY+channelId,ids,0 );
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
	 * 根据频道卡片ID获取该频道卡片商品列表
	 * @param mouldId
	 * @return
	 */
	public List<ChannelMouldPro> findChMoProListByCmId(Long cmId,Long channelId) {
		List<ChannelMouldPro> result = new ArrayList<ChannelMouldPro>();
		List<Long> ids = null;
		try{
			ids =(List<Long>) ServiceFactory.getCacheService().getObject(Constants.channelMouldProIds_KEY+cmId );
		}catch(Exception e){
			logger.info("findChMoProListByCmId err cache key:"+Constants.channelMouldProIds_KEY+cmId);
		}
		if(ids==null || ids.size()==0){
			ids=new ArrayList<Long>();
			List<Integer> tmpids = channelMouldProRepository.findIdsByCmid(cmId);
			if(tmpids!=null)
			{
				for(Integer tid:tmpids){
					if(tid!=null){
						ids.add(Numbers.parseLong(tid.toString().trim(), 0L));
					}
				}
				ServiceFactory.getCacheService().setObject(Constants.channelMouldProIds_KEY+cmId,ids,0 );
			}
		}
		if(ids!=null && ids.size()>0){
			for(Long id:ids){
				ChannelMouldPro ChannelMouldPro = findChMoPrById(id,channelId);
				if(ChannelMouldPro !=null){
					result.add(ChannelMouldPro);
				}
			}
		}
		return result;
	}
	/**
	 * 根据频道卡片商品ID获取该频道卡片商品
	 * @param mouldId
	 * @return
	 */
	public ChannelMouldPro findChMoPrById(Long id,Long channelId) {
		ChannelMouldPro channelMouldPro = (ChannelMouldPro) ServiceFactory.getCacheService().getObject(Constants.channel_mould_pro_KEY+id);//从缓存读入
    	if(channelMouldPro==null){
    		channelMouldPro = channelMouldProRepository.findOne(id);
    		if(channelMouldPro!=null){
    			String imag=channelMouldPro.getImgurl()==null?"":channelMouldPro.getImgurl().replace(StringUtil.getPICDomain(), "");
    			if(!imag.startsWith(Configuration.root().getString("kjd.ImagePre","http://image.kjt.com.pre"))){//非跨境通图片
    				if(imag.indexOf("/pimgs/adload/")>=0 || imag.indexOf("/pimgs")>=0 ||imag.indexOf("/upload")>=0){
        				imag = StringUtil.getPICDomain()+imag;
        			}else{
        				Long pid=channelMouldPro.getPid();
        				if(pid==null || pid.longValue()==0){
        					imag = StringUtil.getPICDomain()+"/pimgs/adload/"+channelId+"/"+imag;
        				}else{
        					imag=StringUtil.getListpic(imag);
        				}
        			}
    			}
    			channelMouldPro.setImgurl(imag);
    			ServiceFactory.getCacheService().setObject(Constants.channel_mould_pro_KEY+id, channelMouldPro,0 );//写入缓存
    		}
    	}
		return channelMouldPro;
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
				sb.append(Htmls.generateOptionName(c.getId().longValue(), c.getTyp(), c.getMname()));
			}
		}
		return sb.toString();
    }

	/**
	 * 保存频道卡片
	 * @param channelMould
	 * @return
	 */
	public ChannelMould saveChannelMould(ChannelMould channelMould,Long channelId) {
		channelMould = channelMouldRepository.save(channelMould);
		ServiceFactory.getCacheService().setObject(Constants.channel_mould_KEY+channelMould.getId(), channelMould,0 );//写入缓存
		List<Long> ids = (List<Long>) ServiceFactory.getCacheService().getObject(Constants.channelMouldIds_KEY+channelId );
		if(ids!=null && !ids.contains(channelMould.getId())){
			ids.add(channelMould.getId());
			ServiceFactory.getCacheService().setObject(Constants.channelMouldIds_KEY+channelId,ids,0 );
		}
		return channelMould;
	}

	/**
	 * 删除频道卡片内容商品
	 * @param cmpid 商品ID
	 * @param cmid 频道卡片ID
	 */
	public void deletechannelMouldProById(Long cmpid,Long cmid) {
		channelMouldProRepository.delete(cmpid);
		ServiceFactory.getCacheService().setObject(Constants.channel_mould_pro_KEY+cmpid, null,0 );//写入缓存
		List<Long> ids =new ArrayList<Long>();
		List<Integer> tmpids = channelMouldProRepository.findIdsByCmid(cmpid);
		if(tmpids!=null)
		{
			for(Integer tid:tmpids)
			{
				if(ids !=null){
					ids.add(Numbers.parseLong(tid.toString().trim(), 0L));
				}
			}
		}
		
		 //List<Long> ids =channelMouldProRepository.findIdsByCmid(cmid,"nsort");
		ServiceFactory.getCacheService().setObject(Constants.channelMouldProIds_KEY+cmid,ids,0 );
		ChannelMould channelMould = getChannelMouldById(cmid);
		Mould mould = findMouldByMouldId(channelMould.getMouldId());
		if(ids!=null && mould!=null  && "1".equals(channelMould.getFlag())  && !"4".equals(mould.getTyp())){
			if(mould.getCnt()!=ids.size())
				channelMould.setFlag("0");//不展示
			else
				channelMould.setFlag("1");//展示
			saveChannelMould(channelMould, cmid);
		}
	}

	/**
	 * 保存频道卡片内容
	 * @param channelMouldPro
	 * @param channelId 
	 * @param cmid 
	 */
	public ChannelMouldPro saveChannelMouldPro(ChannelMouldPro channelMouldPro,String channelId, Long cmid) {
		channelMouldPro = channelMouldProRepository.save(channelMouldPro);
		if(channelMouldPro!=null){
			String imag=channelMouldPro.getImgurl()==null?"":channelMouldPro.getImgurl().replace(StringUtil.getPICDomain(), "");
			if(imag.indexOf("/pimgs/adload/")>=0 || imag.indexOf("/pimgs")>=0 ||imag.indexOf("/upload")>=0){
				imag = StringUtil.getPICDomain()+imag;
			}else{
				String kjdImagePre = Configuration.root().getString("kjd.ImagePre","http://image.kjt.com.pre");
				if(!imag.startsWith(kjdImagePre)){
					imag = StringUtil.getPICDomain()+"/pimgs/adload/"+channelId+"/"+imag;
				}
			}
			channelMouldPro.setImgurl(imag);
		}
		ServiceFactory.getCacheService().setObject(Constants.channel_mould_pro_KEY+channelMouldPro.getId(), channelMouldPro,0 );//写入缓存
		//List<Long> ids = channelMouldProRepository.findIdsByCmid(cmid,"nsort");
		List<Long> ids=new ArrayList<Long>();
		List<Integer> tmpids = channelMouldProRepository.findIdsByCmid(cmid);
		if(tmpids!=null)
		{
			for(Integer tid:tmpids){
				if(tid!=null){
					ids.add(Numbers.parseLong(tid.toString().trim(), 0L));
				}
			}
		}
		ServiceFactory.getCacheService().setObject(Constants.channelMouldProIds_KEY+cmid,ids,0 );
		ChannelMould channelMould = getChannelMouldById(cmid);
		Mould mould = null;
		if(channelMould!=null){
			mould = findMouldByMouldId(channelMould.getMouldId());
		}
		if(ids!=null && mould!=null && "1".equals(channelMould.getFlag())  && !"4".equals(mould.getTyp())){
			
			if(mould.getCnt()!=ids.size())
				channelMould.setFlag("0");//不展示
			else
				channelMould.setFlag("1");//展示
			saveChannelMould(channelMould, cmid);
		}
		return channelMouldPro;
	}

	/**
	 * 根据商品ID去获取频道商品为这个pid的列表
	 * @param pid
	 * @return
	 */
	public List<ChannelMouldPro> findChMoPrByPId(Long pid) {
		return channelMouldProRepository.findByPid(pid);
	}

	/**
	 * 查询此频道下有多少mould
	 * @param channelId
	 * @return
	 */
	public Long findChannelMouldTotal(final Long channelId) {
		return channelMouldRepository.count(new Specification<ChannelMould>() {
			@Override
			public Predicate toPredicate(Root<ChannelMould> channelMould, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				Path<Long> cid = channelMould.get("cid");
				predicates.add(builder.equal(cid, channelId));
				Predicate[] param = new Predicate[predicates.size()];
				predicates.toArray(param);
				return query.where(param).getRestriction();
			}
		});
	}

	public Page<ChannelMould> queryChannelMouldPage(final Long channelId,final String search,Integer start,Integer limit) {
		return channelMouldRepository.findAll(
				new Specification<ChannelMould>() {
					@Override
					public Predicate toPredicate(Root<ChannelMould> channelMould, CriteriaQuery<?> query,
							CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<>();
						Path<Long> id = channelMould.get("id");
						Path<Long> cid = channelMould.get("cid");
						predicates.add(builder.equal(cid, channelId));
						Long idKey=Numbers.parseLong(search, 0L);
						if(idKey.longValue()>0){
							predicates.add(builder.equal(id, Numbers.parseLong(search, 0L)));
						}
						Predicate[] param = new Predicate[predicates.size()];
						predicates.toArray(param);
						return query.where(param).getRestriction();
					}
				},
				new PageRequest(start, limit, new Sort(Sort.Direction.DESC,"nsort","id")));
	}

	
	/**
	 * 分页查询Pad频道
	 * @param channelQueryForm
	 * @return
	 */
	public Page<PadChannel> queryPadChannel(final ChannelQueryForm channelQueryForm) {
		return padChannelRepository.findAll(
				new Specification<PadChannel>() {
					@Override
					public Predicate toPredicate(Root<PadChannel> channel, CriteriaQuery<?> query,
							CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<>();
						Path<Long> userid = channel.get("userid");
						if(channelQueryForm.userid!=null && channelQueryForm.userid.longValue()>=0){
							predicates.add(builder.equal(userid, channelQueryForm.userid));
						}
						Predicate[] param = new Predicate[predicates.size()];
						predicates.toArray(param);
						return query.where(param).getRestriction();
					}
				},
				new PageRequest(channelQueryForm.page, channelQueryForm.size,new Sort(Direction.DESC, "userid","nsort")));
	}
	
	/**
	 * 根据ID获取Pad频道
	 * @param channelId
	 * @return
	 */
	public PadChannel findPadChannelById(Long channelId) {
		return padChannelRepository.findById(channelId);
	}
	/**
	 * 修改Pad频道显示状态
	 * @param channelId
	 * @param channelsta
	 */
	@Transactional
	public void updatePadChannelSta(Long channelId, String channelsta) {
		padChannelRepository.updateChannelSta(channelId,channelsta);
	}
	/**
	 * 根据频道ID获取此频道下的商品列表
	 * @param cid
	 * @return
	 */
	public List<PadChannelPro> findPadChannelProListByCid(final Long channelId){
		return padChannelProRepository.findAll(new Specification<PadChannelPro>() {
			@Override
			public Predicate toPredicate(Root<PadChannelPro> padChannelPro, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				Path<Long> cid = padChannelPro.get("cid");
				predicates.add(builder.equal(cid, channelId));
				Predicate[] param = new Predicate[predicates.size()];
				predicates.toArray(param);
				return query.where(param).getRestriction();
			}
		},new Sort(Direction.DESC, "nsort"));
	}
	/**
	 * 保存pad频道
	 * @param padChannel
	 * @return
	 */
	public PadChannel savePadChannel(PadChannel padChannel) {
		return padChannelRepository.save(padChannel);
	}
	/**
	 * 保存pad频道商品
	 * @param padChannelPro
	 * @return
	 */
	@Transactional
	public PadChannelPro savePadChannelPro(PadChannelPro padChannelPro) {
		return padChannelProRepository.save(padChannelPro);
	}
	

	/**
	 * 删除pad频道
	 * @param channelId
	 */
	@Transactional
	public void deletePadchannelById(Long channelId) {
		padChannelRepository.delete(channelId);
	}

	/**
	 * 分页显示pad商户频道商品
	 * @param channelQueryForm
	 * @return
	 */
	public Page<PadChannelPro> queryPadChannelProPage(final ChannelQueryForm channelQueryForm) {
		return padChannelProRepository.findAll(
				new Specification<PadChannelPro>() {
					@Override
					public Predicate toPredicate(Root<PadChannelPro> padChannelPro, CriteriaQuery<?> query,
							CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<>();
						Path<Long> cid = padChannelPro.get("cid");
						Path<Long> pid = padChannelPro.get("pid");
						//Path<String> typFlag = padChannelPro.get("typFlag");//0商品 1Banner2频道3 4图
						predicates.add(builder.equal(cid, channelQueryForm.channelId));
						//predicates.add(builder.equal(typFlag, channelQueryForm.typFlag));
						if(!Strings.isNullOrEmpty(channelQueryForm.pidOrNewSku)){
							Long tempPid = Numbers.parseLong(channelQueryForm.pidOrNewSku, 0L);
			            	if(tempPid!=0L){
				            	Predicate p1 = builder.equal(pid, channelQueryForm.pidOrNewSku);
				            	Subquery<Long> sq = query.subquery(Long.class);
								Root<Product>  subroot=sq.from(Product.class);
								Predicate pidPredicate = builder.equal(subroot.get("newSku").as(String.class), channelQueryForm.pidOrNewSku);
								sq.where(pidPredicate);
								sq.select(subroot.get("pid").as(Long.class));
								Predicate in= builder.in(pid).value(sq);
				            	predicates.add(builder.or(p1,in));
			            	}else{
			            		Subquery<Long> sq = query.subquery(Long.class);
								Root<Product>  subroot=sq.from(Product.class);
								Predicate pidPredicate = builder.equal(subroot.get("newSku").as(String.class), channelQueryForm.pidOrNewSku);
								sq.where(pidPredicate);
								sq.select(subroot.get("pid").as(Long.class));
								Predicate in= builder.in(pid).value(sq);
			            		predicates.add(in);
			            	}
						}
						if(!StringUtils.isBlank(channelQueryForm.productName)){
							Subquery<Long> sq = query.subquery(Long.class);
							Root<Product>  subroot=sq.from(Product.class);
							Predicate pidPredicate = builder.like(subroot.get("title").as(String.class), "%"+channelQueryForm.productName+"%");
							sq.where(pidPredicate);
							sq.select(subroot.get("pid").as(Long.class));
							Predicate in= builder.in(pid).value(sq);
							predicates.add(in);
						}
						Predicate[] param = new Predicate[predicates.size()];
						predicates.toArray(param);
						return query.where(param).getRestriction();
					}
				},
				new PageRequest(channelQueryForm.page, channelQueryForm.size,new Sort(Direction.DESC, "nsort")));
	}

	/**
	 * 根据endorsementId与channelId判断此pad频道商品是否已存在
	 * @param endorsementId
	 * @param channelId
	 * @return
	 */
	public Long countPadChannelByEidAndCid(final Long endorsementId,final Long channelId) {
		return padChannelProRepository.count(new Specification<PadChannelPro>() {
					@Override
					public Predicate toPredicate(Root<PadChannelPro> padChannelPro, CriteriaQuery<?> query,
							CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<>();
						Path<Long> eid = padChannelPro.get("eid");
						Path<Long> cid = padChannelPro.get("cid");
						predicates.add(builder.equal(eid, endorsementId));
						predicates.add(builder.equal(cid, channelId));
						Predicate[] param = new Predicate[predicates.size()];
						predicates.toArray(param);
						return query.where(param).getRestriction();
					}
				});
	}
	/**
	 * 根据endorsementId与channelId判断此pad频道商品是否已存在
	 * @param endorsementId
	 * @param channelId
	 * @return
	 */
	public PadChannelPro getPadChannelByEidAndCid(final Long endorsementId,final Long channelId) {
		List<PadChannelPro> padChannelProList = padChannelProRepository.findAll(new Specification<PadChannelPro>() {
			@Override
			public Predicate toPredicate(Root<PadChannelPro> padChannelPro, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				Path<Long> eid = padChannelPro.get("eid");
				Path<Long> cid = padChannelPro.get("cid");
				predicates.add(builder.equal(eid, endorsementId));
				predicates.add(builder.equal(cid, channelId));
				Predicate[] param = new Predicate[predicates.size()];
				predicates.toArray(param);
				return query.where(param).getRestriction();
			}
		});
		if(padChannelProList!=null && padChannelProList.size()>0){
			return padChannelProList.get(0);
		}else{
			return null;
		}
	}
	
	/**
	 * 修改Pad频道显示状态
	 * @param channelId
	 * @param channelsta
	 */
	@Transactional
	public void updatePadChannelProSta(Long id, String sta) {
		padChannelProRepository.updateChannelProSta(id,sta);
	}

	/**
	 * 根据ID删除商户Pad频道商品
	 * @param id
	 */
	@Transactional
	public void deletePadChannelProById(Long id) {
		padChannelProRepository.delete(id);
		
	}

	/**
	 * 根据ID获取padChannlePro
	 * @param id
	 * @return
	 */
	public PadChannelPro findPadChannelProById(Long id) {
		return padChannelProRepository.findOne(id);
	}

	/**
	 * 修改padChannlePro的排序值
	 * @param channelProIdForUpdate
	 * @param nsortForUpdate
	 */
	@Transactional
	public void updatePadChannelProNsort(Long channelProIdForUpdate, int nsortForUpdate) {
		padChannelProRepository.updateChannelProSta(channelProIdForUpdate,nsortForUpdate);
	}

	/**
	 * 根据频道名称与用户ID查询是商户频道
	 * @param channelName
	 * @param userId
	 * @return
	 */
	public PadChannel findPadChannelByCnameAndUserid(String channelName, Long userId) {
		return padChannelRepository.findByCnameAndUserid(channelName,userId);
	}

	/**
	 * 根据用户ID获取该用户下的所有pad频道
	 * @param uid
	 * @return
	 */
	public List<PadChannel> findPadchannelByUserid(Long uid) {
		return padChannelRepository.findPadchannelByUserid(uid);
	}

	/**
	 * 根据Pad频道ID删除此pad频道下的所有商品
	 * @param id
	 */
	@Transactional
	public void deletePadChannelProByCid(Long cid) {
		padChannelProRepository.deleteByCid(cid);
	}

	/**
	 * 根据用户ID删除该用户下的所有Pad频道
	 * @param uid
	 */
	@Transactional
	public void deletePadChannelByUserid(Long uid) {
		padChannelRepository.deleteByUserid(uid);
		
	}

	public Long getTotalsWithForm(ChannelQueryForm form) {
		StringBuffer sb = new StringBuffer("SELECT count(cm.`id`) ")
		.append(" FROM `channel` c , `channel_mould` cm, `mould` m ");
		if(!Strings.isNullOrEmpty(form.pidOrNewSku)){
			sb.append(",`product` p, `channel_mould_pro` cmp ") ;
		}
		sb.append(" WHERE c.`id`= cm.`cid` AND cm.`mouldId`=m.`id` ");
		//专题id
		if(!Strings.isNullOrEmpty(form.cid)&&!"-1".equals(form.cid)){
			sb.append(" AND c.`id`= ").append(form.cid);
		}
		//商品ID或newsku
		if (!Strings.isNullOrEmpty(form.pidOrNewSku)) {
			sb.append(" and cm.`id`=cmp.`cmid` AND cmp.`pid`=p.`pid` and ").append("(p.`pid` = ").append(form.pidOrNewSku).append(" or p.newSku='").append(form.pidOrNewSku).append("') ");
		}
		if (!Strings.isNullOrEmpty(form.startNsort)) {
			sb.append(" and ").append("cm.`nsort` >= ").append(form.startNsort);
		}
		if (!Strings.isNullOrEmpty(form.endNsort)) {
			sb.append(" and ").append("cm.`nsort` <= ").append(form.endNsort);
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

	public List<ChannelMouldVO> queryChannelMouldListByForm(
			ChannelQueryForm form) {
		StringBuffer sb = new StringBuffer("SELECT c.`id`,cm.`id`,m.`id`,m.`mname`,cm.`nsort`,cm.`flag`,cm.`manType` ")
		.append(" FROM `channel` c , `channel_mould` cm, `mould` m ");
		if(!Strings.isNullOrEmpty(form.pidOrNewSku)){
			sb.append(",`product` p, `channel_mould_pro` cmp ") ;
		}
		sb.append(" WHERE c.`id`= cm.`cid` AND cm.`mouldId`=m.`id` ");
		//专题id
		if(!Strings.isNullOrEmpty(form.cid)&&!"-1".equals(form.cid)){
			sb.append(" AND c.`id`= ").append(form.cid);
		}
		//商品ID或newsku
		if (!Strings.isNullOrEmpty(form.pidOrNewSku)) {
			sb.append(" and cm.`id`=cmp.`cmid` AND cmp.`pid`=p.`pid` and ").append("(p.`pid` = ").append(form.pidOrNewSku).append(" or p.newSku='").append(form.pidOrNewSku).append("') ");
		}
		if (!Strings.isNullOrEmpty(form.startNsort)) {
			sb.append(" and ").append("cm.`nsort` >= ").append(form.startNsort);
		}
		if (!Strings.isNullOrEmpty(form.endNsort)) {
			sb.append(" and ").append("cm.`nsort` <= ").append(form.endNsort);
		}
		sb.append(" order by cm.`nsort` desc,cm.`id` desc ");
		sb.append("limit ").append(form.page*form.size).append(",").append(form.size).append(" ");
		String sql = sb.toString();
		logger.info(sql); 
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		List<ChannelMouldVO> result = new ArrayList<ChannelMouldVO>();
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				long cid = rs.getLong(1);
				long cmid = rs.getLong(2);
				long mid = rs.getLong(3);
				String mname = rs.getString(4);
				int nsort = rs.getInt(5);
				String flag = rs.getString(6);
				String manType = rs.getString(7);
				//添加专题信息
				ChannelMouldVO cmvo = new ChannelMouldVO();
				cmvo.flag=flag;
				cmvo.manType=manType;
				cmvo.mname=mname;
				cmvo.mouldId=mid;
				cmvo.nsort=nsort;
				cmvo.cid=cid;
				cmvo.cmid=cmid;
				//将channelmould加入到result中
				result.add(cmvo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	public PadChannelPro getPadChannelProById(Long id) {
		return padChannelProRepository.findOne(id);
	}

	public PadChannelProView savePadChannelProView(
			PadChannelProView padChannelProView) {
		return padChannelProViewRepository.save(padChannelProView);
	}

	public List<PadChannelProView> findPadChannelProViewListByCpId(Long cpid) {
		return padChannelProViewRepository.findPadChannelProViewListByCpId(cpid);
	}

	public PadChannelProView findPadChannelProViewById(Long id) {
		return padChannelProViewRepository.findPadChannelProViewById(id);
	}

	@Transactional
	public void updatePadChannelProViewSta(Long id, String sta) {
		padChannelProViewRepository.updateChannelProViewSta(id,sta);
	}

	@Transactional
	public void deletePadChannelProViewById(Long id) {
		padChannelProViewRepository.delete(id);
		
	}

	public int findPadChannelProViewByCpid(Long cpid) {
		return padChannelProViewRepository.findPadChannelProViewByCpid(cpid);
	}

	/**
	 * 
	 * <p>Title: syncTemplate</p> 
	 * <p>Description: 同步嗨个购商城模板到快递员</p>
	 */
	public boolean syncTemplate() {
		//单独启动线程进行同步操作
	    /*Runnable runnable = new Runnable() {
	    public void run() {
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();*/
		String uderidstr = "";
		if(Play.application().configuration().getBoolean("production", false)){
			uderidstr= Configuration.root().getString("wx.default.uid.product", "430003");
        }else{
        	uderidstr= Configuration.root().getString("wx.default.uid.dev", "11413");
        }
		//同步的存储过程
    	String sql = "SELECT userid FROM endorsementduct WHERE gid=6 AND userid<>"+uderidstr+" GROUP BY userid";// SQL语句// 
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		String refreshsyncinfo = "";
		try {
			db.getPrepareStateDao(sql);
			ResultSet rst = db.pst.executeQuery();// 执行语句，得到结果集
			while(rst.next()){
				long userid = rst.getLong(1);//获取userid列表
				refreshsyncinfo = "[快递员用户id:"+userid+"同步开始<-";
				syncHgTemplate(userid);
				refreshsyncinfo += "->快递员用户id:"+userid+"同步完成]";
				ServiceFactory.getCacheService().set("refreshsyncinfo", refreshsyncinfo);
			}
			refreshsyncinfo = " ok 同步完成！   ";
			ServiceFactory.getCacheService().set("refreshsyncinfo", refreshsyncinfo);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return true;
	 }
	
	/**
	 * 
	 * <p>Title: syncHgTemplate</p> 
	 * <p>Description: 同步快递员商城为嗨购商城操作</p> 
	 * @param userid
	 */
	public void syncHgTemplate(Long userid) {
		//同步的存储过程
    	String sql = "call `sp_endorsement_AutoShop`("+userid+")";// SQL语句// //调用存储过程
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			db.pst.executeQuery();// 执行语句，得到结果集
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
	}

}
