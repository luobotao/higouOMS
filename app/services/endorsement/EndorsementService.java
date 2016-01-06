package services.endorsement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import forms.ProductManageForm;
import models.Bbtaddress;
import models.endorsement.Endorsement;
import models.endorsement.EndorsementImg;
import models.product.PadChannel;
import models.product.PadChannelPro;
import models.product.Product;
import models.user.User;
import play.Configuration;
import play.Logger;
import repositories.BbtaddressRepository;
import repositories.endorsement.EndorsementImageRepository;
import repositories.endorsement.EndorsementRepository;
import services.ServiceFactory;
import services.product.ChannelService;
import services.product.ProductService;
import utils.Constants;
import utils.Htmls;
import utils.JdbcOper;
import utils.MatrixToImageWriter;
import utils.Numbers;
import utils.OSSUtils;
import utils.StringUtil;

/**
 * 代言相关Service
 * @author luobotao
 * @Date 2015年8月29日
 */
@Named
@Singleton
public class EndorsementService {

    private static final Logger.ALogger logger = Logger.of(EndorsementService.class);

    @Inject
    private EndorsementRepository endorsementRepository;
    @Inject
    private EndorsementImageRepository endorsementImageRepository;
    @Inject
    private BbtaddressRepository bbtaddressRepository;
    @Inject
    private ChannelService channelService;
    @Inject
    private ProductService productService;
    
	public List<Endorsement> findAll() {
		return endorsementRepository.findAll();
	}

	public Page<Endorsement> queryEndorsementPage(final ProductManageForm form) {
		return this.endorsementRepository.findAll(
				new Specification<Endorsement>() {
					@Override
					public Predicate toPredicate(Root<Endorsement> endorsement,
							CriteriaQuery<?> query, CriteriaBuilder builder) {
						Path<Long> userId = endorsement.get("userId");
						Path<Long> productId = endorsement.get("productId");
						Path<Long> gid = endorsement.get("gid");
						Path<Integer> status = endorsement.get("status");
		
						List<Predicate> predicates = new ArrayList<>();
						if(form.pid!=null){
							 predicates.add(builder.equal(productId, form.pid));
						}
						if(!StringUtils.isBlank(form.productName)){//商品名称
							Subquery<Long> sq = query.subquery(Long.class);
							Root<Product>  subroot=sq.from(Product.class);
							Predicate pidPredicate = builder.like(subroot.get("title").as(String.class), "%"+form.productName+"%");
							sq.where(pidPredicate);
							sq.select(subroot.get("pid").as(Long.class));
							Predicate in= builder.in(productId).value(sq);
							predicates.add(in);
						}
						if(form.userId!=null){
							predicates.add(builder.equal(userId, form.userId));
						}
						if(!StringUtils.isBlank(form.userName)){//商户名称
							Subquery<Long> sq = query.subquery(Long.class);
							Root<User>  subroot=sq.from(User.class);
							Predicate uidPredicate = builder.like(subroot.get("nickname").as(String.class), "%"+form.userName+"%");
							sq.where(uidPredicate);
							sq.select(subroot.get("uid").as(Long.class));
							Predicate in= builder.in(userId).value(sq);
							predicates.add(in);
						}
						if(form.gid!=null){
							predicates.add(builder.equal(gid, form.gid));
						}
						predicates.add(builder.notEqual(status, 3));//不展示删除的代言
						Predicate[] param = new Predicate[predicates.size()];

						predicates.toArray(param);
						return query.where(param).getRestriction();
					}

				}, new PageRequest(form.page, form.size, new Sort(Direction.DESC, "eid")));
	}

	
	/**
	 * 保存或更新代言
	 * @param endorsement
	 * @return
	 */
	@Transactional
	public Endorsement saveOrUpdateEnorsement(Endorsement endorsement) {
		return endorsementRepository.save(endorsement);
	}
	/**
	 * 保存或更新代言图片
	 * @param endorsementImg
	 */
	@Transactional
	public EndorsementImg saveOrUpdateEnorsementImage(EndorsementImg endorsementImg) {
		return endorsementImageRepository.save(endorsementImg);
	}

	/**
	 * 根据代言ID获取此代言
	 * @param id
	 * @return
	 */
	public Endorsement findEndorsementById(Long id) {
		return endorsementRepository.findOne(id);
	}

	/**
	 * 根据代言ID获取代言图片
	 * @param eid
	 * @return
	 */
	public List<EndorsementImg> findEndorsementImageByEid(Long eid) {
		return endorsementImageRepository.findByEid(eid);
	}

	/**
	 * 根据代言图片ID删除该图片
	 * @param id
	 */
	public void deleteEndorsementImageById(Long id) {
		endorsementImageRepository.delete(id);
	}


	public List<Endorsement> queryEndorsementList(final ProductManageForm form) {
		return this.endorsementRepository.findAll(
				new Specification<Endorsement>() {

					@Override
					public Predicate toPredicate(Root<Endorsement> endorsement,
							CriteriaQuery<?> query, CriteriaBuilder builder) {
						Path<Long> userId = endorsement.get("userId");
						Path<Long> productId = endorsement.get("productId");
						Path<Integer> status = endorsement.get("status");
		
						List<Predicate> predicates = new ArrayList<>();
						if(form.pid!=null){
							 predicates.add(builder.equal(productId, form.pid));
						}
						if(form.userId!=null){
							predicates.add(builder.equal(userId, form.userId));
						}
						predicates.add(builder.notEqual(status, 3));//不展示删除的代言
						Predicate[] param = new Predicate[predicates.size()];

						predicates.toArray(param);
						return query.where(param).getRestriction();
					}

				});
	}
	
	/**
	 * 根据parentid获取下面的地域信息
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Bbtaddress> findByParentid(Integer parentid) {
		List<Bbtaddress> bbtaddressList = (List<Bbtaddress>) ServiceFactory.getCacheService().getObject(Constants.bbtaddress_parent_KEY+parentid );//从缓存读入
		if(bbtaddressList==null || bbtaddressList.isEmpty()){
			bbtaddressList = bbtaddressRepository.findByParentid(parentid);
			if(bbtaddressList!=null ){
				 ServiceFactory.getCacheService().setObject(Constants.bbtaddress_parent_KEY+parentid, bbtaddressList,0);//写入缓存
			}
		}
		return bbtaddressList;
	}

	/**
	 * 省份select
	 * @param provinceList
	 * @param province
	 * @return
	 */
	public static String provinceList2Html(List<Bbtaddress> provinceList, Integer province) {
		StringBuilder sb = new StringBuilder();
		sb.append(Htmls.generateOption(-1, "省份"));
		for (Bbtaddress c : provinceList) {
			if (province != null && province.equals(c.getId())) {
				sb.append(Htmls.generateSelectedOption(c.getId(), c.getName()));
			} else {
				sb.append(Htmls.generateOption(c.getId(), c.getName()));
			}
		}
		return sb.toString();
	}
	/**
	 * 城市select
	 * @param cityList
	 * @param province
	 * @return
	 */
	public static String cityList2Html(List<Bbtaddress> cityList, Integer cityid) {
		StringBuilder sb = new StringBuilder();
		sb.append(Htmls.generateOption(-1, "城市"));
		for (Bbtaddress c : cityList) {
			if (cityid != null && cityid.equals(c.getId())) {
				sb.append(Htmls.generateSelectedOption(c.getId(), c.getName()));
			} else {
				sb.append(Htmls.generateOption(c.getId(), c.getName()));
			}
		}
		return sb.toString();
	}

	/**
	 * 频道商品导入
	 * @param file
	 * @return
	 */
	public Boolean importFile(File file, Long userId, Long gid) {
		// 解析文件
		Workbook workBook = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("没有找到对应的文件", e);
		}
		try {
			workBook = WorkbookFactory.create(fis);
			Sheet sheet = workBook.getSheetAt(0);
			int lastRowNumber = sheet.getLastRowNum();
			Row rowTitle = sheet.getRow(0);
			if (rowTitle == null) {
				return false;
			}
			// 从第1行开始（不算标题）
			for (int i = 1; i <= lastRowNumber; i++) {
				Row row = sheet.getRow(i);
				if (row == null) {
					continue;
				}
				String channelName = row.getCell(0, Row.RETURN_NULL_AND_BLANK) == null ? ""
						: row.getCell(0, Row.RETURN_NULL_AND_BLANK).toString();// 频道名称
				String pid = row.getCell(1, Row.RETURN_NULL_AND_BLANK) == null ? ""
						: row.getCell(1, Row.RETURN_NULL_AND_BLANK).toString();// pid
				Long pidLong = Numbers.parseLong(pid, 0L);
				String newSku = row.getCell(2, Row.RETURN_NULL_AND_BLANK) == null ? ""
						: row.getCell(2, Row.RETURN_NULL_AND_BLANK).toString();// sku
				String endorsementPrice = row.getCell(4, Row.RETURN_NULL_AND_BLANK) == null ? ""
						: row.getCell(4, Row.RETURN_NULL_AND_BLANK).toString();// 商户售价
				String percentVal = row.getCell(5, Row.RETURN_NULL_AND_BLANK) == null ? ""
						: row.getCell(5, Row.RETURN_NULL_AND_BLANK).toString();// 佣金比例
				String moneyVal = row.getCell(6, Row.RETURN_NULL_AND_BLANK) == null ? ""
						: row.getCell(6, Row.RETURN_NULL_AND_BLANK).toString();// 佣金金额
				logger.info(channelName + "========");
				logger.info(pidLong + "========");
				logger.info(newSku + "========");
				logger.info(endorsementPrice + "========");
				logger.info(percentVal + "========");
				logger.info(moneyVal + "========");

				PadChannel padChannel = channelService.findPadChannelByCnameAndUserid(channelName, userId);
				if (padChannel == null) {
					padChannel = new PadChannel();
					padChannel.setSta("1");// 默认显示
					padChannel.setUserid(userId);
					padChannel.setDate_add(new Date());
					padChannel.setCname(channelName);
					padChannel.setNsort(0);
					padChannel = channelService.savePadChannel(padChannel);
					if(gid.longValue()==6){//快递员商城
						
					}
				}

				String BUCKET_NAME = StringUtil.getBUCKET_NAME();
				String exportPath = Configuration.root().getString("oss.upload.dimensional.image", "/");// 上传路径
				if (pidLong.longValue() == 0) {
					Product product = productService.findproductByNewSku(newSku);
					if (product != null)
						pidLong = product.getPid();
				}
				// 根据pid与用户id去检查此用户是否已代言过此商品,且此代言处于未删除状态
				ProductManageForm form = new ProductManageForm();
				form.pid = pidLong;
				form.userId = userId;
				Endorsement endorsement = null;
				List<Endorsement> endorsementList = queryEndorsementList(form);
				if (endorsementList.isEmpty()) {
					Product product = productService.findProductById(pidLong);
					if (product != null) {
						endorsement = new Endorsement();
						endorsement.setUserId(userId);
						endorsement.setGid(gid);
						endorsement.setProductId(pidLong);
						endorsement.setRemark("");
						endorsement.setEndorsementPrice(Double.valueOf(endorsementPrice));
						if (StringUtils.isBlank(percentVal)) {// 比例为空取金额
							endorsement.setCommisionTyp(1);// 佣金类型 1金额 2百分比
							endorsement.setCommision(Double.valueOf(moneyVal));
						}else{
							endorsement.setCommisionTyp(2);
							endorsement.setCommision(Double.valueOf(percentVal));
						}
						endorsement.setStatus(0);// 0用户上传未审核，1正常（审核通过），2后台审核未通过,3删除
						endorsement.setBannerimg(StringUtil.getSheSaidIcon());
						endorsement.setCreateTime(new Date());
						endorsement.setCount(0);
						endorsement = saveOrUpdateEnorsement(endorsement);
						// 生成二维码
						try {
							String url = StringUtil.getAPIDomain() + "/sheSaid/show?uid=" + userId + "&pid=" + pidLong
									+ "&daiyanid=" + endorsement.getEid() + "&wx=0";

							MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
							Map hints = new HashMap();
							hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
							BitMatrix bitMatrix = multiFormatWriter.encode(url, BarcodeFormat.QR_CODE, 400, 400, hints);
							String fileNameLast = UUID.randomUUID().toString().replaceAll("-", "") + ".jpg";// 最终的文件名称
							String tempPath = Configuration.root().getString("temp.path", "/");// 本地上传临时路径
							File file1 = new File(tempPath, fileNameLast);
							file1 = MatrixToImageWriter.writeToFile(bitMatrix, "jpg", file1);
							String endfilestr = OSSUtils.uploadFile(file1, exportPath, fileNameLast, ".jpg",
									BUCKET_NAME);
							endorsement.setDimensionalimg(endfilestr);// 二维码路径
							file1.delete();
						} catch (Exception e) {
							e.printStackTrace();
						}
						endorsement = saveOrUpdateEnorsement(endorsement);
					}else{
						continue;
					}
				} else {
					endorsement = endorsementList.get(0);
				}
				Long eid = endorsement.getEid();
				// 根据eid与cid去padChannelPro表里检查是否已存在此商品
				Long count = channelService.countPadChannelByEidAndCid(eid, padChannel.getId());
				if (count == null || count.longValue() == 0) {
					PadChannelPro padChannelPro = new PadChannelPro();
					padChannelPro.setCid(padChannel.getId());
					padChannelPro.setEid(eid);
					padChannelPro.setDate_add(new Date());
					padChannelPro.setPid(pidLong);
					padChannelPro.setTyp("1");// 显示与否
					padChannelPro.setTypFlag("0");//0是商品1是banner
					padChannelPro.setNsort(0);
					padChannelPro = channelService.savePadChannelPro(padChannelPro);
				}
			}
		} catch (Exception e) {
			logger.info(e.toString());
			return false;
		}
		return true;
	}
	/**
	 * 频道banner导入
	 * @param file
	 * @return
	 */
	public Boolean importBannerFile(File file, Long userId, Long gid) {
		// 解析文件
		Workbook workBook = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("没有找到对应的文件", e);
		}
		try {
			workBook = WorkbookFactory.create(fis);
			Sheet sheet = workBook.getSheetAt(0);
			int lastRowNumber = sheet.getLastRowNum();
			Row rowTitle = sheet.getRow(0);
			if (rowTitle == null) {
				return false;
			}
			// 从第1行开始（不算标题）
			for (int i = 1; i <= lastRowNumber; i++) {
				Row row = sheet.getRow(i);
				if (row == null) {
					continue;
				}
				String channelName = row.getCell(0, Row.RETURN_NULL_AND_BLANK) == null ? ""
						: row.getCell(0, Row.RETURN_NULL_AND_BLANK).toString();// 频道名称
				String pid = row.getCell(1, Row.RETURN_NULL_AND_BLANK) == null ? ""
						: row.getCell(1, Row.RETURN_NULL_AND_BLANK).toString();// pid
				Long pidLong = Numbers.parseLong(pid, 0L);
				String newSku = row.getCell(2, Row.RETURN_NULL_AND_BLANK) == null ? ""
						: row.getCell(2, Row.RETURN_NULL_AND_BLANK).toString();// sku
				String endorsementPrice = row.getCell(4, Row.RETURN_NULL_AND_BLANK) == null ? ""
						: row.getCell(4, Row.RETURN_NULL_AND_BLANK).toString();// 商户售价
				String percentVal = row.getCell(5, Row.RETURN_NULL_AND_BLANK) == null ? ""
						: row.getCell(5, Row.RETURN_NULL_AND_BLANK).toString();// 佣金比例
				String moneyVal = row.getCell(6, Row.RETURN_NULL_AND_BLANK) == null ? ""
						: row.getCell(6, Row.RETURN_NULL_AND_BLANK).toString();// 佣金金额
				String picUrl = row.getCell(7, Row.RETURN_NULL_AND_BLANK) == null ? ""
						: row.getCell(7, Row.RETURN_NULL_AND_BLANK).toString();// 佣金金额
				logger.info(channelName + "========");
				logger.info(pidLong + "========");
				logger.info(newSku + "========");
				logger.info(endorsementPrice + "========");
				logger.info(percentVal + "========");
				logger.info(moneyVal + "========");
				logger.info(picUrl + "========");
				
				PadChannel padChannel = channelService.findPadChannelByCnameAndUserid(channelName, userId);
				if (padChannel == null) {
					padChannel = new PadChannel();
					padChannel.setSta("1");// 默认显示
					padChannel.setUserid(userId);
					padChannel.setDate_add(new Date());
					padChannel.setCname(channelName);
					padChannel.setNsort(0);
					padChannel = channelService.savePadChannel(padChannel);
					if(gid.longValue()==6){//快递员商城
						
					}
				}
				
				String BUCKET_NAME = StringUtil.getBUCKET_NAME();
				String exportPath = Configuration.root().getString("oss.upload.dimensional.image", "/");// 上传路径
				if (pidLong.longValue() == 0) {
					Product product = productService.findproductByNewSku(newSku);
					if (product != null)
						pidLong = product.getPid();
				}
				// 根据pid与用户id去检查此用户是否已代言过此商品,且此代言处于未删除状态
				ProductManageForm form = new ProductManageForm();
				form.pid = pidLong;
				form.userId = userId;
				Endorsement endorsement = null;
				List<Endorsement> endorsementList = queryEndorsementList(form);
				if (endorsementList.isEmpty()) {
					Product product = productService.findProductById(pidLong);
					if (product != null) {
						endorsement = new Endorsement();
						endorsement.setUserId(userId);
						endorsement.setGid(gid);
						endorsement.setProductId(pidLong);
						endorsement.setRemark("");
						endorsement.setEndorsementPrice(Double.valueOf(endorsementPrice));
						if (StringUtils.isBlank(percentVal)) {// 比例为空取金额
							endorsement.setCommisionTyp(1);// 佣金类型 1金额 2百分比
							endorsement.setCommision(Double.valueOf(moneyVal));
						}else{
							endorsement.setCommisionTyp(2);
							endorsement.setCommision(Double.valueOf(percentVal));
						}
						endorsement.setStatus(0);// 0用户上传未审核，1正常（审核通过），2后台审核未通过,3删除
						endorsement.setBannerimg(StringUtil.getSheSaidIcon());
						endorsement.setCreateTime(new Date());
						endorsement.setCount(0);
						endorsement = saveOrUpdateEnorsement(endorsement);
						// 生成二维码
						try {
							String url = StringUtil.getAPIDomain() + "/sheSaid/show?uid=" + userId + "&pid=" + pidLong
									+ "&daiyanid=" + endorsement.getEid() + "&wx=0";
							
							MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
							Map hints = new HashMap();
							hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
							BitMatrix bitMatrix = multiFormatWriter.encode(url, BarcodeFormat.QR_CODE, 400, 400, hints);
							String fileNameLast = UUID.randomUUID().toString().replaceAll("-", "") + ".jpg";// 最终的文件名称
							String tempPath = Configuration.root().getString("temp.path", "/");// 本地上传临时路径
							File file1 = new File(tempPath, fileNameLast);
							file1 = MatrixToImageWriter.writeToFile(bitMatrix, "jpg", file1);
							String endfilestr = OSSUtils.uploadFile(file1, exportPath, fileNameLast, ".jpg",
									BUCKET_NAME);
							endorsement.setDimensionalimg(endfilestr);// 二维码路径
							file1.delete();
						} catch (Exception e) {
							e.printStackTrace();
						}
						endorsement = saveOrUpdateEnorsement(endorsement);
					}else{
						continue;
					}
				} else {
					endorsement = endorsementList.get(0);
				}
				Long eid = endorsement.getEid();
				// 根据eid与cid去padChannelPro表里检查是否已存在此商品
				PadChannelPro padChannelPro = channelService.getPadChannelByEidAndCid(eid, padChannel.getId());
				if (padChannelPro == null ) {
					padChannelPro = new PadChannelPro();
					padChannelPro.setCid(padChannel.getId());
					padChannelPro.setEid(eid);
					padChannelPro.setDate_add(new Date());
					padChannelPro.setPid(pidLong);
					padChannelPro.setTyp("1");// 显示与否
					padChannelPro.setTypFlag("0");//0是商品1是banner
					padChannelPro.setNsort(0);
					padChannelPro = channelService.savePadChannelPro(padChannelPro);
				}
				PadChannelPro padChannelProBanner = new PadChannelPro();
				padChannelProBanner.setCid(padChannel.getId());
				padChannelProBanner.setEid(eid);
				padChannelProBanner.setDate_add(new Date());
				padChannelProBanner.setPid(pidLong);
				padChannelProBanner.setTyp("1");// 显示与否
				padChannelProBanner.setTypFlag("1");//0是商品1是banner
				padChannelProBanner.setNsort(10);
				padChannelProBanner.setImgurl(picUrl);
				padChannelProBanner.setLinkurl(StringUtil.getAPIDomain()+"/sheSaid/show?uid="+userId+"&pid="+pidLong+"&daiyanid="+eid+"&wx=0");
				padChannelProBanner = channelService.savePadChannelPro(padChannelProBanner);
			}
		} catch (Exception e) {
			logger.info(e.toString());
			return false;
		}
		return true;
	}

	/**
	 * 根据用户ID删除此用户下的所有代言信息
	 * @param uid
	 */
	@Transactional
	public void delteEndorsementInfoByUserId(Long uid) {
		List<PadChannel> padChannelList = channelService.findPadchannelByUserid(uid);
		for(PadChannel padChannel : padChannelList){
			channelService.deletePadChannelProByCid(padChannel.getId());
		}
		channelService.deletePadChannelByUserid(uid);
	}

	public PadChannelPro findPadChannelProById(Long id) {
		PadChannelPro padChannelPro = channelService.getPadChannelProById(id);
		return padChannelPro;
	}

	public PadChannelPro savePadChannelPro(PadChannelPro padChannelPro) {
		return channelService.savePadChannelPro(padChannelPro);
	}
}
