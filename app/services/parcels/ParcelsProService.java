package services.parcels;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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

import com.google.common.base.Strings;
import com.ibm.icu.math.BigDecimal;

import forms.order.OrderForm;
import forms.parcels.ParcelsForm;
import models.KdCompany;
import models.OrderProduct;
import models.Parcels;
import models.ParcelsPro;
import models.ParcelsWaybill;
import models.admin.AdminUser;
import play.Configuration;
import play.Logger;
import repositories.parcels.ParcelsProRepository;
import repositories.parcels.ParcelsRepository;
import repositories.parcels.ParcelsWaybillRepository;
import services.ServiceFactory;
import services.admin.AdminUserService;
import utils.Constants;
import utils.Dates;
import utils.ExcelGenerateHelper;
import utils.JdbcOper;
import utils.StringUtil;
import utils.kuaidi100.Kuaidi100;
import vo.ParcelsVO;

/**
 * 包裹商品相关Service
 * @author luobotao
 * Date: 2015年4月17日 下午2:26:14
 */
@Named
@Singleton
public class ParcelsProService {

    private static final Logger.ALogger logger = Logger.of(ParcelsProService.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	@Inject
    private ParcelsProRepository parcelsProRepository;
	@Inject
    private ParcelsRepository parcelsRepository;
	@Inject
	private ParcelsWaybillRepository parcelsWaybillRepository;
	@Inject
	private AdminUserService adminUserService;
    @Transactional(readOnly = true)
	public List<ParcelsPro> queryParcelsProListByParcelsId(Long parcelsId) {
		List<ParcelsPro> parcelsProList = this.parcelsProRepository.queryParcelsProListByParcelsId(parcelsId);
        return parcelsProList;
    }

    /**
     * 
     * <p>Title: saveParcelsProWithForm</p> 
     * <p>Description: 保存包裹商品信息</p> 
     * @param parcels
     * @param orderProducts
     * @param formPage
     */
    @Transactional
	public void saveParcelsProWithForm(Parcels parcels, List<OrderProduct> orderProducts, OrderForm formPage) {
		for (OrderProduct orderProduct : orderProducts) {
			ParcelsPro pp = new ParcelsPro();
			pp.setDate_add(new Date());
			pp.setParcelsId(parcels.getId());
			pp.setShopProId(orderProduct.getId());
			pp.setpId(orderProduct.getpId());
			pp.setPrice(orderProduct.getPrice());
			pp.setCounts(orderProduct.getCounts());
			pp.setTotalFee(orderProduct.getTotalFee());
			pp = parcelsProRepository.save(pp);
			ServiceFactory.getCacheService().setObject(Constants.orderProduct_KEY+pp.getId(), pp, 0);
			logger.info("produce pardels_Pro with <"+orderProduct.getId()+"> success");
		}
	}

    /**
     * 
     * <p>Title: queryParcelsProWithPage</p> 
     * <p>Description: 根据条件查询出满足指定条件的包裹集合</p> 
     * @param formPage
     * @return
     */
	public Page<ParcelsPro> queryParcelsProWithPage(ParcelsForm form) {
		return this.parcelsProRepository.findAll(new ParcelsProQuery(form),new PageRequest(form.page, form.size,new Sort(Direction.DESC, "id")));
	}
    
	/**
	 * 
	 * <p>Title: ParcelsProService.java</p> 
	 * <p>Description: 分页查询出指定的包裹商品信息</p> 
	 * <p>Company: higegou</p> 
	 * @author  ctt
	 * date  2015年8月1日  上午10:31:12
	 * @version
	 */
    private static class ParcelsProQuery implements Specification<ParcelsPro> {

        private final ParcelsForm form;

        public ParcelsProQuery(final ParcelsForm form) {
            this.form = form;
        }

        @Override
        public Predicate toPredicate(Root<ParcelsPro> parcelsPro, CriteriaQuery<?> query,
                                     CriteriaBuilder builder) {
            List<Predicate> predicates = new ArrayList<>();
            Path<Long> parcelsId = parcelsPro.get("parcelsId");
            if(form.parcelIds!=null ){
            	if(form.parcelIds.size()>0){
            		predicates.add(parcelsId.in(form.parcelIds));
            	}else{
            		predicates.add(parcelsId.in(0));
            	}
            }
            
            Predicate[] param = new Predicate[predicates.size()];
            
            predicates.toArray(param);
            return query.where(param).getRestriction();
        }
    }

    /**
     * 
     * <p>Title: getAllByParcelsId</p> 
     * <p>Description: 获取包裹下的商品信息</p> 
     * @param id
     * @return
     */
	public List<ParcelsPro> getAllByParcelsId(Long parId) {
		return parcelsProRepository.getAllByParcelsId(parId);
	}

	public ParcelsPro get(long ppId) {
		return parcelsProRepository.getOne(ppId);
	}

	public void del(ParcelsPro pp) {
		parcelsProRepository.delete(pp);
	}

	/**
	 * 
	 * <p>Title: queryParcelsIdList</p> 
	 * <p>Description: 根据条件获取到 包裹ID信息集合</p> 
	 * @param formPage
	 * @return
	 */
	public List<Long> queryParcelsIdList(ParcelsForm form) {
		List<Long> result = new ArrayList<Long>();
		String sql = "select pp.pardelsId from pardels par, pardels_Pro pp,product pro, shopping_Order so where pp.pardelsId=par.id and pp.pId=pro.pId and par.orderId=so.id and pro.pid!=1880 ";
		StringBuffer sb = new StringBuffer();
		sb.append(sql);
		//物流状态
		if (!Strings.isNullOrEmpty(form.status) && !"-99".equals(form.status)) {
			sb.append("and ").append("par.status=").append(form.status).append(" ");
        }
		//包裹类型
		if (!Strings.isNullOrEmpty(form.src)&& !"-1".equals(form.src)) {
			sb.append("and ").append("par.src=").append(form.src).append(" ");
		}
		//生成时间
		if (form.between != null) {
			sb.append("and ").append("par.date_add between '").append(Dates.formatEngLishDateTime(form.between.start)).append("' and '").append(Dates.formatEngLishDateTime(form.between.end)).append("' ");
		}
		//包裹号
        if (!Strings.isNullOrEmpty(form.parcelsCode)) {
        	sb.append("and ").append("par.pardelCode='").append(form.parcelsCode).append("' ");
        }
        //订单号
        if (!Strings.isNullOrEmpty(form.orderCode)) {
        	//嘿客号
        	if (!Strings.isNullOrEmpty(form.sfcode)) {
        		sb.append("and (").append("par.OrderCode='").append(form.orderCode.trim()).append("' or ").append("so.sfcode='").append(form.sfcode.trim()).append("') ");
        	}else{
        		sb.append("and ").append("par.OrderCode='").append(form.orderCode.trim()).append("' ");
        	}
        }
        //商品ID或newsku
        if (!Strings.isNullOrEmpty(form.pId)&&!Strings.isNullOrEmpty(form.newSku)) {
        	sb.append("and ").append("(pro.pId = ").append(form.pId).append(" or pro.newSku='").append(form.newSku).append("') ");
        }
        //收货人用户名和商品名称
        if (!Strings.isNullOrEmpty(form.name)&&!Strings.isNullOrEmpty(form.title)) {
        	sb.append("and ").append("(par.name like '%").append(form.name).append("%' or pro.title like '%").append(form.title).append("%') ");
        }
        //电话
        if (!Strings.isNullOrEmpty(form.phone)) {
        	sb.append("and ").append("par.phone = '").append(form.phone).append("' ");
        }
        sql = sb.toString();
        JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				Long id= rs.getLong(1);
				result.add(id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	/**
	 * 
	 * <p>Title: getTotalsWithForm</p> 
	 * <p>Description: 获取满足条件的包裹总量</p> 
	 * @param formPage
	 * @return
	 */
	public Long getTotalsWithForm(ParcelsForm form) {
		StringBuffer sb = new StringBuffer("select count(pp.id) from pardels par, pardels_Pro pp,product pro, shopping_Order so");
		//联营管理员
		if (form.adminid!=null && form.adminid.longValue()!=0) {
			sb.append(",adminproduct ap ");
        }else if("3".equals(form.src)){
			sb.append(",`adminproduct` ap,`admin` a ");
		}
		sb.append(" where pp.pardelsId=par.id and pp.pId=pro.pId and par.orderId=so.id and pro.pid!=1880 ");
		//物流状态
		if (!Strings.isNullOrEmpty(form.status) && !"-99".equals(form.status)) {
			sb.append("and ").append("par.status=").append(form.status).append(" ");
		}
		//包裹类型
		if (!Strings.isNullOrEmpty(form.src)&& !"-1".equals(form.src)&& !"3".equals(form.src)) {
			sb.append("and ").append("par.src=").append(form.src).append(" ");
		}
		//生成时间
		if (form.between != null) {
			sb.append("and ").append("par.date_add between '").append(Dates.formatEngLishDateTime(form.between.start)).append("' and '").append(Dates.formatEngLishDateTime(form.between.end)).append("' ");
		}
		//包裹号
        if (!Strings.isNullOrEmpty(form.parcelsCode)) {
        	sb.append("and ").append("par.pardelCode='").append(form.parcelsCode).append("' ");
        }
        //订单号
        if (!Strings.isNullOrEmpty(form.orderCode)) {
        	//嘿客号
        	if (!Strings.isNullOrEmpty(form.sfcode)) {
        		sb.append("and (").append("par.OrderCode='").append(form.orderCode.trim()).append("' or ").append("so.sfcode='").append(form.sfcode.trim()).append("') ");
        	}else{
        		sb.append("and ").append("par.OrderCode='").append(form.orderCode.trim()).append("' ");
        	}
        }
        //商品ID或newsku
        if (!Strings.isNullOrEmpty(form.pId)&&!Strings.isNullOrEmpty(form.newSku)) {
        	sb.append("and ").append("(pro.pId = ").append(form.pId).append(" or pro.newSku='").append(form.newSku).append("') ");
        }
        //收货人用户名和商品名称
        if (!Strings.isNullOrEmpty(form.name)&&!Strings.isNullOrEmpty(form.title)) {
        	sb.append("and ").append("(par.name like '%").append(form.name).append("%' or pro.title like '%").append(form.title).append("%' ").append(" or pro.newSku='").append(form.newSku).append("') ");
        }
        //电话
        if (!Strings.isNullOrEmpty(form.phone)) {
        	sb.append("and ").append("par.phone = '").append(form.phone).append("' ");
        }
        //结算状态
        if (form.checkStatus!=-1) {
        	sb.append("and ").append("par.checkStatus = '").append(form.checkStatus).append("' ");
        }
        //联营管理员
		if (form.adminid != null && form.adminid.longValue() != 0) {
			sb.append(" AND ap.adminid=").append(form.adminid).append(" AND pro.pid = ap.pid ");
		}else if("3".equals(form.src)){
	        if(form.adminId!=-1 ){
	        	sb.append(" and a.id=").append(form.adminId).append(" and a.adminType=2 and ap.pid=pro.pid and ap.adminid = a.id ");
	        }else{
	        	sb.append(" and a.adminType=2 and ap.pid=pro.pid and ap.adminid = a.id ");
	        }
        }
        String sql = sb.toString();
        JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
        Long totals = (long) 0;
		try {
			db.getPrepareStateDao(sql);
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

	public List<ParcelsVO> queryParcelsProIdList(ParcelsForm form) {
		StringBuffer sb = new StringBuffer("select pp.id,par.pardelCode,so.sfcode,so.orderCode,pro.pid,pro.newSku,pro.title,pro.listpic,par.name,par.phone,par.date_add,par.src,par.status,par.id,pro.typ,par.`province`,par.address,pp.`counts` AS cnt,par.checkStatus AS checkStatus,pp.price AS price,so.cardId as cardId from pardels par, pardels_Pro pp,product pro, shopping_Order so");
		//联营管理员
		if (form.adminid!=null && form.adminid.longValue()!=0) {
			sb.append(",adminproduct ap ");
        }else if("3".equals(form.src)){
			sb.append(",`adminproduct` ap,`admin` a ");
		}
		sb.append(" where pp.pardelsId=par.id and pp.pId=pro.pId and par.orderId=so.id and pro.pid!=1880 ");
		//物流状态
		if (!Strings.isNullOrEmpty(form.status) && !"-99".equals(form.status)) {
			sb.append("and ").append("par.status=").append(form.status).append(" ");
        }
		//包裹类型
		if (!Strings.isNullOrEmpty(form.src)&& !"-1".equals(form.src)&& !"3".equals(form.src)) {
			sb.append("and ").append("par.src=").append(form.src).append(" ");
		}
		//生成时间
		if (form.between != null) {
			sb.append("and ").append("par.date_add between '").append(Dates.formatEngLishDateTime(form.between.start)).append("' and '").append(Dates.formatEngLishDateTime(form.between.end)).append("' ");
		}
		//包裹号
        if (!Strings.isNullOrEmpty(form.parcelsCode)) {
        	sb.append("and ").append("par.pardelCode='").append(form.parcelsCode).append("' ");
        }
        //订单号
        if (!Strings.isNullOrEmpty(form.orderCode)) {
        	//嘿客号
        	if (!Strings.isNullOrEmpty(form.sfcode)) {
        		sb.append("and (").append("par.OrderCode='").append(form.orderCode.trim()).append("' or ").append("so.sfcode='").append(form.sfcode.trim()).append("') ");
        	}else{
        		sb.append("and ").append("par.OrderCode='").append(form.orderCode.trim()).append("' ");
        	}
        }
        //商品ID或newsku
        if (!Strings.isNullOrEmpty(form.pId)&&!Strings.isNullOrEmpty(form.newSku)) {
        	sb.append("and ").append("(pro.pId = ").append(form.pId).append(" or pro.newSku='").append(form.newSku).append("') ");
        }
        //收货人用户名和商品名称
        if (!Strings.isNullOrEmpty(form.name)&&!Strings.isNullOrEmpty(form.title)) {
        	sb.append("and ").append("(par.name like '%").append(form.name).append("%' or pro.title like '%").append(form.title).append("%' ").append(" or pro.newSku='").append(form.newSku).append("') ");
        }
        //电话
        if (!Strings.isNullOrEmpty(form.phone)) {
        	sb.append("and ").append("par.phone = '").append(form.phone).append("' ");
        }
        //结算状态
        if (form.checkStatus!=-1) {
        	sb.append("and ").append("par.checkStatus = '").append(form.checkStatus).append("' ");
        }
		// 联营管理员
		if (form.adminid != null && form.adminid.longValue() != 0) {
			sb.append(" AND ap.adminid=").append(form.adminid).append(" AND pro.pid = ap.pid ");
			sb.append(" AND par.adminid=").append(form.adminid);
		}else if("3".equals(form.src)){
	        if(form.adminId!=-1 ){
	        	sb.append(" and a.id=").append(form.adminId).append(" and a.adminType=2 and ap.pid=pro.pid and ap.adminid = a.id ");
	        }else{
	        	sb.append(" and a.adminType=2 and ap.pid=pro.pid and ap.adminid = a.id ");
	        }
        }
        sb.append(" order by pp.id desc ");
        sb.append(" limit ").append(form.page*form.size).append(",").append(form.size).append(" ");
        String sql = sb.toString();
        logger.info(sql);
        JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
        List<ParcelsVO> result = new ArrayList<ParcelsVO>();
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				ParcelsVO parcelsVo = new ParcelsVO();
				parcelsVo.parcelProId=rs.getLong(1);
				parcelsVo.parcelCode=rs.getString(2);
				parcelsVo.sfcode=rs.getString(3);
				parcelsVo.orderCode=rs.getString(4);
				parcelsVo.pid=rs.getLong(5);
				parcelsVo.newSku=rs.getString(6);
				parcelsVo.title=rs.getString(7);
				parcelsVo.listpic=StringUtil.getListpic(rs.getString(8));
				parcelsVo.name=rs.getString(9);
				parcelsVo.phone=rs.getString(10);
				parcelsVo.dateAdd=rs.getString(11);
				parcelsVo.status=Constants.WaybillStatus.waybill2Message(rs.getInt(13));

				parcelsVo.parcelId=rs.getLong(14);
				if("2".equals(rs.getString(15))){
					List<AdminUser> lyAdminUsers = adminUserService.queryAdminUserByPid(rs.getLong(5),form.adminId);
					if(lyAdminUsers!=null&&lyAdminUsers.size()>0){
						if(lyAdminUsers.size()>1){
							parcelsVo.src="多家联营";
						}else{
							parcelsVo.src="联营-"+lyAdminUsers.get(0).getRealname();
						}
					}else{
						parcelsVo.src=Constants.TypsProduct.typs2Message(Integer.parseInt(rs.getString(15)));
					}
				}else{
					parcelsVo.src=Constants.TypsProduct.typs2Message(Integer.parseInt(rs.getString(15)));
				}
				parcelsVo.province=rs.getString("province");
				parcelsVo.address=rs.getString("address");
				parcelsVo.cnt=rs.getInt("cnt");
				parcelsVo.checkStatus=rs.getInt("checkStatus");
				parcelsVo.price=rs.getDouble("price");
				parcelsVo.cardId=rs.getString("cardId");
				result.add(parcelsVo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	public File exportFile(List<ParcelsVO> parcelsVoList) {
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("sheet1");
		sheet.setColumnWidth(0, 5000);
		sheet.setColumnWidth(1, 5000);
		sheet.setColumnWidth(2, 5000);
		sheet.setColumnWidth(3, 5000);
		sheet.setColumnWidth(4, 5000);
		sheet.setColumnWidth(5, 7000);
		sheet.setColumnWidth(6, 3000);
		sheet.setColumnWidth(7, 3000);
		sheet.setColumnWidth(8, 5000);
		sheet.setColumnWidth(9, 8000);
		sheet.setColumnWidth(10, 8000);
		sheet.setColumnWidth(11, 8000);
		sheet.setColumnWidth(12, 8000);
		sheet.setColumnWidth(13, 9000);
		ExcelGenerateHelper helper = ExcelGenerateHelper.getInstance(wb);

		// 获取行索引
		int rowIndex = 0;
		int colIndex = 0;

		// 表头
		String[] titles = { "包裹号", "嘿客号","生成时间", "PID", "newSku", "商品名称", "购买数量", "价格","商品总价", "收货人", "收货人电话","身份证号", "省市区", "详细地址" };

		// 生成表头
		helper.setRowIndex(rowIndex++);
		helper.generateHeader(sheet, titles, 0, StringUtils.EMPTY,StringUtils.EMPTY);
		for(ParcelsVO parcelsVo:parcelsVoList){
			colIndex = 0;
			Row row = sheet.createRow(rowIndex++);
			helper.createStringCell(row, colIndex++, parcelsVo.parcelCode);//包裹号
			helper.createStringCell(row, colIndex++, parcelsVo.sfcode);//嘿客号
			helper.createStringCell(row, colIndex++, parcelsVo.dateAdd);//生成时间
			helper.createStringCell(row, colIndex++, parcelsVo.pid.toString());//PID
			helper.createStringCell(row, colIndex++, parcelsVo.newSku);//newSku
			helper.createStringCell(row, colIndex++, parcelsVo.title);//title
			helper.createStringCell(row, colIndex++, parcelsVo.cnt.toString());//购买数量
			helper.createStringCell(row, colIndex++, parcelsVo.price.toString());//购买单价
			helper.createStringCell(row, colIndex++, new BigDecimal(parcelsVo.price).multiply(new BigDecimal(parcelsVo.cnt)).setScale(1,BigDecimal.ROUND_HALF_DOWN).toString());//购买总价
			helper.createStringCell(row, colIndex++, parcelsVo.name);//name
			helper.createStringCell(row, colIndex++, parcelsVo.phone);//phone
			helper.createStringCell(row, colIndex++, parcelsVo.cardId);//cardId
			helper.createStringCell(row, colIndex++, parcelsVo.province);//省市区
			helper.createStringCell(row, colIndex++, parcelsVo.address);//详细地址
		}
		//在application.conf中配置的路径
		String path = Configuration.root().getString("export.path");
		File file = new File(path);
		file.mkdir();//判断文件夹是否存在，不存在就创建
		
		FileOutputStream out = null;
		String fileName = path + "parcelsManage" + System.currentTimeMillis() + ".xls";
		try {
			out = new FileOutputStream(fileName);
			wb.write(out);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new File(fileName);
	}

public boolean extractMailInfo(File file,KdCompany kdCompany) {
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
				String parcelsCode = row.getCell(0, Row.RETURN_NULL_AND_BLANK)==null?"":row.getCell(0, Row.RETURN_NULL_AND_BLANK).toString();//包裹号
				String waybillCode = row.getCell(1, Row.RETURN_NULL_AND_BLANK)==null?"":row.getCell(1, Row.RETURN_NULL_AND_BLANK).toString();//运单号
				Parcels parcels = parcelsRepository.findByParcelCode(parcelsCode);
				if(parcels==null){
					continue;
				}else{
					if(Constants.WaybillStatus.DFH.getStatus()!=parcels.getStatus()){
						continue;
					}
				}
				ParcelsWaybill pwb = new ParcelsWaybill();
				pwb.setDate_add(new Date());
				pwb.setDate_upd(CHINESE_DATE_TIME_FORMAT.format(new Date()));
				pwb.setDate_txt(CHINESE_DATE_TIME_FORMAT.format(new Date()));
				pwb.setTransport(kdCompany.getCnName());
				pwb.setTrancode(kdCompany.getCode());
				pwb.setPardelsId(parcels.getId());
				pwb.setWaybillCode(waybillCode);;
				pwb.setRemark("订单已发货，快递单号："+waybillCode);
				pwb.setNsort(0);
				pwb = parcelsWaybillRepository.save(pwb);
				parcels.setStatus(Constants.WaybillStatus.YFH.getStatus());
				parcels.setMailnum(waybillCode);//将包裹里的运单号写入
				parcelsRepository.save(parcels);//修改包裹状态
				boolean flag = Kuaidi100.subscribe(kdCompany.getCode(), waybillCode,"","");
	      		if(!flag){
	      			logger.info("快递100订阅失败，包裹号："+parcelsCode+" 物流单号："+waybillCode);
	      		}else{
	      			logger.info("快递100订阅成功，包裹号："+parcelsCode+" 物流单号："+waybillCode);
	      		}
			}
		} catch (Exception e) {
			logger.info(e.toString());
			return false;
		}
        return true;
    }
   
}
