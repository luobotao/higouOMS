package controllers.subject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;

import controllers.admin.AdminAuthenticated;
import controllers.admin.BaseAdminController;
import forms.ProductManageForm;
import forms.admin.SubjectQueryForm;
import models.admin.AdminUser;
import models.product.Channel;
import models.product.Currency;
import models.product.Fromsite;
import models.product.Mould;
import models.product.Product;
import models.subject.Subject;
import models.subject.SubjectMould;
import models.subject.SubjectMouldPro;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.twirl.api.Html;
import services.OperateLogService;
import services.admin.AdminUserService;
import services.product.ChannelService;
import services.product.ProductService;
import services.subject.SubjectService;
import utils.AjaxHelper;
import utils.Constants.ShowFlag;
import utils.Constants.ShowTypes;
import utils.Constants;
import utils.Numbers;
import utils.OSSUtils;
import utils.StringUtil;
import vo.SubjectMouldProVO;
import vo.SubjectMouldVO;


/**
 * 
 * <p>Title: SubjectController.java</p> 
 * <p>Description: 专题管理</p> 
 * <p>Company: higegou</p> 
 * @author  ctt
 * date  2015年8月9日  下午4:43:20
 * @version
 */
@Named
@Singleton
public class SubjectController extends BaseAdminController {
	private static final Logger.ALogger logger = Logger.of(SubjectController.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private final OperateLogService operateLogService;
	private final ProductService productService;
	private final SubjectService subjectService;
	private final ChannelService channelService;
	private final AdminUserService adminUserService;
	
	private final Form<SubjectQueryForm> subjectQueryForm = Form.form(SubjectQueryForm.class);
	
    @Inject
    public SubjectController(final OperateLogService operateLogService,final ProductService productService,final SubjectService subjectService,final ChannelService channelService,final AdminUserService adminUserService) {
        this.productService = productService;
        this.subjectService = subjectService;
        this.operateLogService = operateLogService;
        this.channelService = channelService;
        this.adminUserService = adminUserService;
    }
	/**
	 * 专题管理
	 * @return
	 */
    @AdminAuthenticated()
	public Result subjectManage(){
		Form<SubjectQueryForm> form = subjectQueryForm.bindFromRequest();
		SubjectQueryForm formPage = new SubjectQueryForm();
        if (!form.hasErrors()) {
        	formPage = form.get();
        }
    	String page = request().getQueryString("page");
    	if(!StringUtils.isBlank(page)){
    		formPage.page= Integer.valueOf(page);
    	}
    	Page<Subject> subjectPage = subjectService.query(formPage);
    	return ok(views.html.subject.subjectManage.render(subjectPage));
	}
	
	/**
	 * 根据ID删除该专题
	 * @return
	 */
    @AdminAuthenticated()
	public Result deleteSubjectById(Long subjectId){
		subjectService.deleteSubjectById(subjectId);
		AdminUser adminUser = getCurrentAdminUser();
		operateLogService.saveSubjectLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "删除专题,专题ID为:"+subjectId);
		return ok(Json.toJson("1"));
	}
	
	/**
	 * 
	 * <p>Title: addSubject</p> 
	 * <p>Description: 跳转到添加专题页面</p> 
	 * @param subjectId
	 * @return
	 */
    @AdminAuthenticated()
	public Result addSubject(){
		Form<SubjectQueryForm> form = subjectQueryForm.bindFromRequest();
		SubjectQueryForm formPage = new SubjectQueryForm();
        if (!form.hasErrors()) {
        	formPage = form.get();
        }
        Subject subject = new Subject();
        subject = subjectService.saveSubject(subject,formPage);
        AdminUser adminUser = getCurrentAdminUser();
		operateLogService.saveSubjectLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "新建专题,专题ID为:"+subject.getId());
		return redirect("/subject/subjectManage");
	}
	
	/**
	 * 
	 * <p>Title: updateSubject</p> 
	 * <p>Description: 修改专题</p> 
	 * @return
	 */
    @AdminAuthenticated()
	public Result updateSubject(){
		Form<SubjectQueryForm> form = subjectQueryForm.bindFromRequest();
		SubjectQueryForm formPage = new SubjectQueryForm();
		if (!form.hasErrors()) {
			formPage = form.get();
		}
		Subject subject = subjectService.getSubjectById(formPage.id);
		subjectService.saveSubject(subject,formPage);
		AdminUser adminUser = getCurrentAdminUser();
		operateLogService.saveSubjectLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "编辑专题,专题ID为:"+formPage.id);
			
		return ok(Json.toJson("1"));
	}
	
	/**
	 * 专题内容管理
	 * @return
	 */
    @AdminAuthenticated()
	public Result subjectContentManage(){
    	Form<SubjectQueryForm> form = subjectQueryForm.bindFromRequest();
		//SubjectQueryForm subjectQueryForm = new SubjectQueryForm();
		SubjectQueryForm formPage = new SubjectQueryForm();
		if (!form.hasErrors()) {
			formPage = form.get();
		}
		List<Subject> subjectList = subjectService.findAll();
		if(Strings.isNullOrEmpty(formPage.sid)){
			formPage.sid = String.valueOf(subjectList.get(0).getId());
		}
		 //根据现有条件获取订单总量
        Long totals = subjectService.getTotalsWithForm(formPage);
		//根据条件查询出指定的专题列表集合
		List<SubjectMouldVO> subjectMouldList = subjectService.querySubjectMouldListByForm(formPage);
		for (SubjectMouldVO subjectMouldVO : subjectMouldList) {
			List<SubjectMouldProVO> subjectMouldProVOs = new ArrayList<SubjectMouldProVO>();
			List<SubjectMouldPro> subjectMouldProList = subjectService.findSuMoProListBySmId(subjectMouldVO.smid,subjectMouldVO.sid);
			for(SubjectMouldPro subjectMouldPro:subjectMouldProList){
				SubjectMouldProVO smpv = new SubjectMouldProVO();
				String href = "";
				String productTitle = "";
				Long pid=subjectMouldPro.getPid();
				if(pid==null || pid.longValue()==0){
					href="#";
				}else{
					Product product =productService.findProductById(pid); 
					if(product!=null&&product.getIshot()==1){		//组合商品
						href="/product/newOrUpdateProductGroup?pid="+pid;
					}else{
						href="/product/newOrUpdateProduct?pid="+pid;
					}
					productTitle = product==null?"":product.getTitle();
				}
				smpv.img=subjectMouldPro.getImgurl();
				smpv.title=productTitle;
				smpv.href=href;
				subjectMouldProVOs.add(smpv);
			}
			subjectMouldVO.subjectMouldProVOs = subjectMouldProVOs;
		}
		int totalPage = (int) (totals%formPage.size==0?totals/formPage.size:totals/formPage.size+1);
		return ok(views.html.subject.subjectContentManage.render(totals,formPage.page,totalPage,Html.apply(SubjectService.subjectList2Html(subjectList,Numbers.parseLong(formPage.sid, -1l))),subjectMouldList,formPage));
	}
	
	/**
	 * 根据专题ID获取下面的专题卡片
	 * @return
	 */
    @AdminAuthenticated()
	public Result getSubjectMouldBySid(Long subjectId){
		List<SubjectMould> subjectMouldList = subjectService.findSubjectMouldListBySid(subjectId);
		for(SubjectMould subjectMould : subjectMouldList){
			List<SubjectMouldPro> subjectMouldProList = subjectService.findSuMoProListBySmId(subjectMould.getId(),subjectId);
			List<ObjectNode> objectNodeList= new ArrayList<ObjectNode>();
			for(SubjectMouldPro subjectMouldPro:subjectMouldProList){
				ObjectNode objectNode = Json.newObject();
				String href = "";
				String productTitle = "";
				Long pid=subjectMouldPro.getPid();
				if(pid==null || pid.longValue()==0){
					href="#";
				}else{
					Product product =productService.findProductById(pid); 
					if(product!=null&&product.getIshot()==1){		//组合商品
						href="/product/newOrUpdateProductGroup?pid="+pid;
					}else{
						href="/product/newOrUpdateProduct?pid="+pid;
					}
					productTitle = product==null?"":product.getTitle();
				}
				objectNode.put("imag", subjectMouldPro.getImgurl());
				objectNode.put("href", href);
				objectNode.put("productTitle", productTitle);
				objectNodeList.add(objectNode);
			}
			subjectMould.setSubjectMouldContent(objectNodeList);
			Mould mould = subjectService.findMouldByMouldId(subjectMould.getMouldId());
			if(mould!=null)
				subjectMould.setMouldName(mould.getMname());
		}
		Map<String,List<SubjectMould>> result = new HashMap<String, List<SubjectMould>>();
		result.put("data", subjectMouldList);
		return ok(Json.toJson(result));
	}
	
	/**
	 * 跳转至新建或编辑卡片页面
	 * @return
	 */
    @AdminAuthenticated()
	public Result newOrUpdateSubjectMould(){
		Long sid = Numbers.parseLong(Form.form().bindFromRequest().get("sid"), 0L);//专题ID
		SubjectMould subjectMould=new SubjectMould();
		Long smid = Numbers.parseLong(AjaxHelper.getHttpParam(request(), "smid"), 0L);
		if(smid.longValue()!=0){
			subjectMould=subjectService.getSubjectMouldById(smid);
		}
		List<Mould> mouldList = subjectService.findMouldList();
		List<SubjectMouldPro> sumoProList = subjectService.findSuMoProListBySmId(smid,sid);
		Mould mould = null;
		if(subjectMould.getMouldId()!=null){
			mould = channelService.findMouldByMouldId(subjectMould.getMouldId());
		}
		return ok(views.html.subject.newOrUpdateSubjectMould.render(sid,smid,subjectMould,mould,sumoProList,
				Html.apply(SubjectService.mouldList2Html(mouldList,subjectMould.getMouldId())),
				Html.apply(ShowTypes.typ2HTML(subjectMould.getManType())),
				Html.apply(ShowFlag.flag2HTML(subjectMould.getFlag()))
				));
	}
	
	/**
	 * 修改专题卡片是否显示
	 * @return
	 */
    @AdminAuthenticated()
	public Result updateSumouldFlag(){
		Long subjectId = Numbers.parseLong(AjaxHelper.getHttpParam(request(), "subjectId"), 0L);
		Long mouldId = Numbers.parseLong(AjaxHelper.getHttpParam(request(), "mouldId"), 0L);
		String flag = AjaxHelper.getHttpParam(request(), "flag");
		SubjectMould subjectMould=subjectService.getSubjectMouldById(mouldId);
		if(mouldId!=null){
			subjectMould.setFlag(flag);
			subjectMould = subjectService.saveSubjectMould(subjectMould,subjectId);
			AdminUser adminUser = getCurrentAdminUser();
			operateLogService.saveSubjectMouldStateLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), subjectId,flag);
			
		}
		return redirect("/subject/subjectContentManage");
	}
	
	/**
	 * 根据专题内容ID删除该专题卡片（subjectMould）
	 * @return
	 */
    @AdminAuthenticated()
	public Result deletesubjectContentById(Long subjectContentId){
		Long subjectId = Numbers.parseLong(AjaxHelper.getHttpParam(request(), "subjectId"), 0L);
		subjectService.deletesubjectContentById(subjectContentId,subjectId);
		AdminUser adminUser = getCurrentAdminUser();
		operateLogService.saveSubjectMouldLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "删除专题卡片,专题卡片ID为:"+subjectContentId);
		
		return ok(Json.toJson("1"));
	}
	
	/**
	 * 保存专题卡片
	 * @return
	 */
    @AdminAuthenticated()
	public Result saveSubjectMould(){
    	AdminUser adminUser = getCurrentAdminUser();
    	String remark = "";
		SubjectMould subjectMould=new SubjectMould();
		Long smid = Numbers.parseLong(AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "smid"), 0L);
		if(smid.longValue()!=0){
			subjectMould=subjectService.getSubjectMouldById(smid);
			remark = "编辑专题卡片,专题卡片ID为:";
		}else{
			remark = "新建专题卡片,专题卡片ID为:";
		}
		Long sid = Numbers.parseLong(Form.form().bindFromRequest().get("sid"), 0L);//专题ID
		subjectMould.setSid(sid);
		Long moulds = Numbers.parseLong(Form.form().bindFromRequest().get("moulds"), 0L);//卡片模版
		subjectMould.setMouldId(moulds);
		String titleBig = Form.form().bindFromRequest().get("titleBig");
		subjectMould.setTitle(titleBig);
		String titlelittle = Form.form().bindFromRequest().get("titlelittle");
		subjectMould.setDatetxt(titlelittle);
		String manType = Form.form().bindFromRequest().get("manType");
		subjectMould.setManType(manType);
		Integer nsort =  Numbers.parseInt(Form.form().bindFromRequest().get("nsort"),0);
		subjectMould.setNsort(nsort);
		String flag = Form.form().bindFromRequest().get("flag");
		subjectMould.setFlag(flag);
		subjectMould.setDate_add(new Date());
		subjectMould = subjectService.saveSubjectMould(subjectMould,sid);
		operateLogService.saveChannelMouldLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), remark+subjectMould.getId());
		List<Mould> mouldList = subjectService.findMouldList();
		List<SubjectMouldPro> sumoProList = subjectService.findSuMoProListBySmId(subjectMould.getId(),sid);
		Mould mould = subjectService.findMouldByMouldId(subjectMould.getMouldId());
		return ok(views.html.subject.newOrUpdateSubjectMould.render(sid,subjectMould.getId(),subjectMould,mould,sumoProList,
				Html.apply(SubjectService.mouldList2Html(mouldList,moulds)),
				Html.apply(ShowTypes.typ2HTML(manType)),
				Html.apply(ShowFlag.flag2HTML(flag))));
	}
	
	/**
	 * 保存专题内容
	 * @return
	 */
    @AdminAuthenticated()
	public Result saveSubjectMouldPro(){
		String endfilestr = "";
		String remark = "";
		String sumouldProId = Form.form().bindFromRequest().get("sumouldProId");//专题内容商品ID
		String typesRadios = Form.form().bindFromRequest().get("typesRadios");//类型
		String linkTitle = Form.form().bindFromRequest().get("linkTitle");//链接
		String beginTime = Form.form().bindFromRequest().get("beginTime");//开抢时间
		String nsortForSavePro = Form.form().bindFromRequest().get("nsortForSavePro");//排序
		String subjectId = Form.form().bindFromRequest().get("sidForSavePro");//AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "sidForSavePro");
		Long pid =Numbers.parseLong(Form.form().bindFromRequest().get("pidForSavePro"), 0L); 
		Long smid =Numbers.parseLong(Form.form().bindFromRequest().get("smidForSavePro"), 0L); 
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart subjectPic = body.getFile("subjectPic");
        if (subjectPic != null) {
        	String path=Configuration.root().getString("oss.upload.adload.image", "pimgs/adload/");//上传路径
        	path = path+subjectId+"/";
        	String BUCKET_NAME=StringUtil.getBUCKET_NAME();
    		if (subjectPic != null && subjectPic.getFile() != null) {
    			String fileName = subjectPic.getFilename();
    			File file = subjectPic.getFile();//获取到该文件
    			int p = fileName.lastIndexOf('.');
    			String type = fileName.substring(p, fileName.length()).toLowerCase();
    			
    			if (".jpg".equals(type)||".gif".equals(type)||".png".equals(type)||".jpeg".equals(type)||".bmp".equals(type)) {
    				// 检查文件后缀格式
    				String fileNameLast = UUID.randomUUID().toString().replaceAll("-", "")+type;//最终的文件名称
    				endfilestr = OSSUtils.uploadFile(file,path,fileNameLast, type,BUCKET_NAME);	
    			}
    		}
        }else{
        	Product product =productService.findProductById(pid);
        	if(product!=null){
        		endfilestr = StringUtil.getListpicRecover(product.getListpic());
        	}
        }
        SubjectMouldPro subjectMouldPro=new SubjectMouldPro();
        if(!Strings.isNullOrEmpty(sumouldProId)){
        	subjectMouldPro = subjectService.findSuMoPrById(Long.parseLong(sumouldProId), Long.parseLong(subjectId));
        	remark = "编辑专题卡片内容,专题卡片内容ID为:";
        }else{
        	remark = "新建专题卡片内容,专题卡片内容ID为:";
        }
        subjectMouldPro.setTyp(typesRadios);
        subjectMouldPro.setSmid(smid);
        subjectMouldPro.setDate_add(new Date());
        if(!"".equals(endfilestr)){
        	subjectMouldPro.setImgurl(endfilestr);
        }
        subjectMouldPro.setLinkurl(linkTitle);
        subjectMouldPro.setPid(pid);
        subjectMouldPro.setMouldId(0L);
        subjectMouldPro.setNsort(Numbers.parseInt(nsortForSavePro, 0));
        SubjectMould sum = subjectService.getSubjectMouldById(smid);
        Mould mould = subjectService.findMouldByMouldId(sum.getMouldId());
        if(sum!=null && sum.getMouldId()!=null && mould!=null && ("4".equals(mould.getTyp())||"6".equals(mould.getTyp()))){
        	try {
				subjectMouldPro.setBeginTime(CHINESE_DATE_TIME_FORMAT.parse(beginTime));
			} catch (ParseException e) {
				e.printStackTrace();
			}
        }
        subjectMouldPro = subjectService.saveSubjectMouldPro(subjectMouldPro,subjectId,smid);
        AdminUser adminUser = getCurrentAdminUser();
        operateLogService.saveChannelMouldProLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), remark+subjectMouldPro.getId());
		return redirect("newOrUpdateSubjectMould?sid="+subjectId+"&smid="+smid);
	}
	
	/**
	 * 批量添加大图商品和双图商品
	 * @return
	 */
    @AdminAuthenticated()
	public Result subjectMouldBatch(){
		String flag = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "flag");
		String subjectId = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "subjectId");
		String pIds = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "products");
		JsonNode json = Json.parse(pIds);
		int i=0;
		Long sumId =0L;
		for(JsonNode temp : json){
			Long mould=0L;
			if("2".equals(flag) ){
				mould =2L;
				if(i==0){
					SubjectMould sum = new SubjectMould();
					sum.setSid(Numbers.parseLong(subjectId, 0L));
					sum.setDate_add(new Date());
					sum.setFlag("0");//不显示
					sum.setTitle("");
					sum.setManType("0");
					sum.setDatetxt("");
					sum.setMouldId(mould);
					sum = subjectService.saveSubjectMould(sum,Numbers.parseLong(subjectId, 0L));
					sumId = sum.getId();
					i++;
				}else{
					SubjectMould sum = subjectService.getSubjectMouldById(sumId);
					sum.setFlag("1");//显示
					sum = subjectService.saveSubjectMould(sum,Numbers.parseLong(subjectId, 0L));
					i=0;
				}
			}else if("1".equals(flag)){
				mould =1L;
				SubjectMould sum = new SubjectMould();
				sum.setSid(Numbers.parseLong(subjectId, 0L));
				sum.setDate_add(new Date());
				sum.setFlag("1");
				sum.setTitle("");
				sum.setManType("0");
				sum.setDatetxt("");
				sum.setMouldId(mould);
				sum = subjectService.saveSubjectMould(sum,Numbers.parseLong(subjectId, 0L));
				sumId = sum.getId();
			}
			SubjectMouldPro sumPro = new SubjectMouldPro();
			sumPro.setPid(temp.asLong());
			sumPro.setSmid(sumId);
			sumPro.setDate_add(new Date());
			sumPro.setLinkurl("pDe://pid="+temp.asLong());
			sumPro.setNsort(0);
			sumPro.setTyp("0");
			sumPro.setMouldId(mould);
			String imag = productService.getDBListPic(temp.asLong());
			sumPro.setImgurl(StringUtil.getListpic(imag));
			sumPro = subjectService.saveSubjectMouldPro(sumPro, subjectId,sumId);
			AdminUser adminUser = getCurrentAdminUser();
	        operateLogService.saveChannelMouldProLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "新建专题卡片内容,专题卡片内容ID为"+sumPro.getId());
			
		}
		return ok("1");
	}
	
	/**
	 * 删除专题内容
	 * @return
	 */
    @AdminAuthenticated()
	public Result deleteSumouldPro(Long smpid){
		Long smid = Numbers.parseLong(AjaxHelper.getHttpParam(request(), "smid"), 0L);
		subjectService.deletesubjectMouldProById(smpid,smid);
		AdminUser adminUser = getCurrentAdminUser();
        operateLogService.saveChannelMouldProLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "删除专题卡片内容,专题卡片内容ID为:"+smpid);
		return ok(Json.toJson("1"));
	}
	
	/**
	 * 跳转至选取频道页面（Iframe）
	 * @return
	 */
    @AdminAuthenticated()
	public Result channels(){
		List<Channel> channelList = channelService.findAll();
		return ok(views.html.subject.channels.render(channelList));
	}
	/**
	 * 跳转至选取专题页面（Iframe）
	 * @return
	 */
    @AdminAuthenticated()
	public Result subjects(){
		List<Subject> subjectList = subjectService.findAll();
		return ok(views.html.subject.subjects.render(subjectList));
	}
	
	/**
	 * 商品列表(批量添加)
	 * @return
	 */
    @AdminAuthenticated()
	public Result productsBatch(){
		String isEndorsement = AjaxHelper.getHttpParam(request(), "isEndorsement");
		ProductManageForm productManageForm = new ProductManageForm();
		productManageForm.pidOrNewSku=AjaxHelper.getHttpParam(request(), "pidForSearch");
		productManageForm.keyWords = AjaxHelper.getHttpParam(request(), "pNameForSearch");
		productManageForm.isEndorsement = Numbers.parseInt(isEndorsement, -1);
		if (Strings.isNullOrEmpty(productManageForm.pidOrNewSku) && StringUtils.isBlank(productManageForm.keyWords)) {
			return ok(views.html.product.products.render(new ArrayList<Product>()));
		}
		List<Product> productList = productService.queryProduct(productManageForm);
		for(Product product : productList){
			Fromsite fromsite =productService.queryFromsiteById(product.getFromsite());
			product.setFromsiteName(fromsite.getName());
			Currency currency = productService.queryCurrencyById(product.getCurrency());
			product.setSymbol(currency.getSymbol());
			if(product.getNstock()<=-99){
				long nstock = productService.dealNstockWithProduct(product.getPid());
				product.setNstock(nstock);
			}
			product = dealProductWithTyp(product);
		}
		return ok(views.html.subject.productsBatch.render(productList));
	}
    
    /**
     * 
     * <p>Title: dealProductWithTyp</p> 
     * <p>Description: 处理商品类型展示内容</p> 
     * @param product
     * @return
     */
	private Product dealProductWithTyp(Product product) {
		if(product!=null&&product.getTyp()!=null){
			if("2".equals(product.getTyp())){
				List<AdminUser> lyAdminUsers = adminUserService.queryAdminUserByPid(product.getPid(),-1);
				if(lyAdminUsers!=null&&lyAdminUsers.size()>0){
					if(lyAdminUsers.size()>1){
						product.setTypeName("多家联营");
					}else{
						product.setTypeName("联营-"+lyAdminUsers.get(0).getRealname());
					}
				}else{
					product.setTypeName(Constants.TypsProduct.typs2Message(Integer.parseInt(product.getTyp())));
				}
			}else{
				product.setTypeName(Constants.TypsProduct.typs2Message(Integer.parseInt(product.getTyp())));
			}
		}
		return product;
	}
	
	/**
	 * 根据ID获取该专题内容商品
	 * @return
	 */
    @AdminAuthenticated()
	public Result getSumouldProById(Long sumouldProId, Long subjectId){
		response().setContentType("application/json;charset=utf-8");
		SubjectMouldPro suMouldPro = subjectService.findSuMoPrById(sumouldProId,subjectId);
		return ok(Json.toJson(suMouldPro));
	}
    
    /**
     * 
     * <p>Title: piliangChangeNsort</p> 
     * <p>Description: 批量修改专题排序值</p> 
     * @return
     */
    @AdminAuthenticated()
    public Result piliangChangeNsort(){
    	String smids =AjaxHelper.getHttpParam(request(), "smids");
    	int newnsort =Numbers.parseInt(AjaxHelper.getHttpParam(request(), "newnsort"), 0);
    	if(!Strings.isNullOrEmpty(smids)&&newnsort!=0){
    		String[] ids = smids.split(",");
    		for (String str : ids) {
				SubjectMould subjectMould = subjectService.getSubjectMouldById(Numbers.parseLong(str, 0L));
				if(subjectMould!=null){
					subjectMould.setNsort(newnsort);
					subjectService.saveSubjectMould(subjectMould, subjectMould.getId());
				}
			}
    	}
    	return redirect("/subject/subjectContentManage");
    }
    
    /**
     * 
     * <p>Title: piliangChangeNsort</p> 
     * <p>Description: 获取指定的subjectmoudlepro</p> 
     * @return
     */
    public Result getSubjectMoudleProWithSeid(){
    	long smid = Numbers.parseLong(AjaxHelper.getHttpParam(request(), "tmpsmid"), 0l);
    	long sid = Numbers.parseLong(AjaxHelper.getHttpParam(request(), "tmpsid"), -1l);
    	if(sid==-1l){
			SubjectMould subjectMould = subjectService.getSubjectMouldById(smid);
			if(subjectMould!=null){
				sid = subjectMould.getSid();
			}
		}
    	List<ObjectNode> objectNodeList= new ArrayList<ObjectNode>();
    	if(smid!=0l&&sid!=-1l){
    		List<SubjectMouldPro> subjectMouldProList = subjectService.findSuMoProListBySmId(smid,sid);
			for(SubjectMouldPro subjectMouldPro:subjectMouldProList){
				ObjectNode objectNode = Json.newObject();
				String href = "";
				String productTitle = "";
				Long pid=subjectMouldPro.getPid();
				if(pid==null || pid.longValue()==0){
					href="#";
				}else{
					Product product =productService.findProductById(pid); 
					if(product!=null&&product.getIshot()==1){		//组合商品
						href="/product/newOrUpdateProductGroup?pid="+pid;
					}else{
						href="/product/newOrUpdateProduct?pid="+pid;
					}
					productTitle = product==null?"":product.getTitle();
				}
				objectNode.put("imag", subjectMouldPro.getImgurl());
				objectNode.put("href", href);
				objectNode.put("productTitle", productTitle);
				objectNodeList.add(objectNode);
			}
    	}
    	return ok(Json.toJson(objectNodeList));
    }
}
