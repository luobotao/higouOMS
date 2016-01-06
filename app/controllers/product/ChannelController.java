package controllers.product;

import java.io.File;
import java.net.URLDecoder;
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

import models.admin.AdminUser;
import models.product.Category;
import models.product.Channel;
import models.product.ChannelMould;
import models.product.ChannelMouldPro;
import models.product.Mould;
import models.product.Product;
import models.subject.Subject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;

import play.Configuration;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.twirl.api.Html;
import services.OperateLogService;
import services.product.CategoryService;
import services.product.ChannelService;
import services.product.ProductService;
import services.subject.SubjectService;
import utils.AjaxHelper;
import utils.Constants.ChannelTypes;
import utils.Constants.ShowFlag;
import utils.Constants.ShowTypes;
import utils.Numbers;
import utils.OSSUtils;
import utils.StringUtil;
import vo.ChannelMouldProVO;
import vo.ChannelMouldVO;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;

import controllers.admin.AdminAuthenticated;
import controllers.admin.BaseAdminController;
import forms.ProductManageForm;
import forms.admin.ChannelQueryForm;


/**
 * @author luobotao
 * @Date 2015年8月9日
 */
@Named
@Singleton
public class ChannelController extends BaseAdminController {
	private static final Logger.ALogger logger = Logger.of(ChannelController.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private final String newOrupdateError = "productNewOrupdateError";
    private final Form<ProductManageForm> productManageForm = Form.form(ProductManageForm.class);
	private final CategoryService categoryService;
	private final ProductService productService;
	private final ChannelService channelService;
	private final SubjectService subjectService;
	private final OperateLogService operateLogService;
	private final Form<ChannelQueryForm> channelQueryForm = Form.form(ChannelQueryForm.class);
	
    @Inject
    public ChannelController(final CategoryService categoryService,final ProductService productService,final ChannelService channelService,final SubjectService subjectService,final OperateLogService operateLogService) {
        this.productService = productService;
        this.channelService = channelService;
        this.categoryService = categoryService;
        this.subjectService = subjectService;
        this.operateLogService = operateLogService;
    }
	/**
	 * 频道管理
	 * @return
	 */
    @AdminAuthenticated()
	public Result channelManage(){
		ChannelQueryForm channelQueryForm = new ChannelQueryForm();
    	String page = request().getQueryString("page");
    	if(!StringUtils.isBlank(page)){
    		channelQueryForm.page= Integer.valueOf(page);
    	}
    	List<Category> categoryList = categoryService.categoryList(0,1);
    	Page<Channel> channelPage = channelService.query(channelQueryForm);
    	return ok(views.html.product.channelManage.render(Html.apply(ChannelTypes.typ2HTML("")),Html.apply(CategoryService.categoryList2Html(categoryList,-1)),channelPage));
	}
	
	/**
	 * 创建新的频道
	 * @return
	 */
    @AdminAuthenticated()
	public Result createchannel(){
    	AdminUser adminUser = getCurrentAdminUser();
    	String remark = "";
		String cid=AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "cid");
		String cname=AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "cname");
		String nsort=AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "nsort");
		String typ=AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "typ");
		String categorys=AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "categorys");
		Channel channel = new Channel();
		if(!StringUtils.isBlank(cid) && Numbers.parseLong(cid, 0L).longValue()!=0){
			channel = channelService.findById(Numbers.parseLong(cid, 0L));
			remark = "编辑频道,编辑后的频道名称为:"+cname+",频道ID为:";
		}else{//新建频道
			channel.setSta("0");//默认不显示
			remark = "新建频道,频道名称为:"+cname+",频道ID为:";
		}
		channel.setCname(cname);
		channel.setNsort(Numbers.parseInt(nsort, 0));
		channel.setDate_add(new Date());
		channel.setTyp(typ);
		if(ChannelTypes.Auto.getTyp().equals(typ)){
			channel.setCategory(categorys);
		}
		channel = channelService.saveChannel(channel);
		operateLogService.saveChannelLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), remark+channel.getId());
		return redirect("/product/channelManage");
	}
	/**
	 * 根据ID获取该频道
	 * @return
	 */
    @AdminAuthenticated()
	public Result getChannelById(Long channelId){
		Channel channel = channelService.findById(channelId);
		return ok(Json.toJson(channel));
	}
	/**
	 * 修改频道显示或隐藏
	 * @return
	 */
    @AdminAuthenticated()
	public Result changeChannelSta(){
    	AdminUser adminUser = getCurrentAdminUser();
		Long channelId = Numbers.parseLong(AjaxHelper.getHttpParam(request(), "channelId"), 0L);
		String channelsta = AjaxHelper.getHttpParam(request(), "channelsta");
		channelService.updateChannelSta(channelId,channelsta);
		operateLogService.saveChannelStateLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), channelId,channelsta);
		return ok(Json.toJson("1"));
	}
	/**
	 * 根据ID删除该频道
	 * @return
	 */
    @AdminAuthenticated()
	public Result deletechannelById(Long channelId){
		channelService.deletechannelById(channelId);
		AdminUser adminUser = getCurrentAdminUser();
		operateLogService.saveChannelLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "删除频道,频道ID为:"+channelId);
		return ok(Json.toJson("1"));
	}
	
	
	/**
	 * 频道内容管理
	 * @return
	 */
    @AdminAuthenticated()
	public Result channelContentManage(){
    	Form<ChannelQueryForm> form = channelQueryForm.bindFromRequest();
		ChannelQueryForm formPage = new ChannelQueryForm();
		if (!form.hasErrors()) {
			formPage = form.get();
		}
		List<Channel> channelList = channelService.findAll();
		if(Strings.isNullOrEmpty(formPage.cid)){
			formPage.cid=String.valueOf(channelList.get(0).getId());
		}
		 //根据现有条件获取订单总量
        Long totals = channelService.getTotalsWithForm(formPage);
		//根据条件查询出指定的专题列表集合
		List<ChannelMouldVO> channelMouldList = channelService.queryChannelMouldListByForm(formPage);
		for (ChannelMouldVO channelMouldVO : channelMouldList) {
			List<ChannelMouldProVO> channelMouldProVOs = new ArrayList<ChannelMouldProVO>();
			List<ChannelMouldPro> channelMouldProList = channelService.findChMoProListByCmId(channelMouldVO.cmid,channelMouldVO.cid);
			for(ChannelMouldPro channelMouldPro:channelMouldProList){
				ChannelMouldProVO smpv = new ChannelMouldProVO();
				String href = "";
				String productTitle = "";
				Long pid=channelMouldPro.getPid();
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
				smpv.img=channelMouldPro.getImgurl();
				smpv.title=productTitle;
				smpv.href=href;
				channelMouldProVOs.add(smpv);
			}
			channelMouldVO.channelMouldProVOs = channelMouldProVOs;
		}
		int totalPage = (int) (totals%formPage.size==0?totals/formPage.size:totals/formPage.size+1);
		return ok(views.html.product.channelContentManage.render(totals,formPage.page,totalPage,Html.apply(ChannelService.channelList2Html(channelList,Numbers.parseLong(formPage.cid, -1L))),channelMouldList,formPage));
	}
	
	/**
	 * 根据频道ID获取下面的频道卡片
	 * @return
	 */
    @AdminAuthenticated()
	public Result getChannelMouldByCid(Long channelId){
		int start = Numbers.parseInt(AjaxHelper.getHttpParam(request(), "start"),0);
		int length = Numbers.parseInt(AjaxHelper.getHttpParam(request(), "length"),0);
		int draw = Numbers.parseInt(AjaxHelper.getHttpParam(request(), "draw"),0);
		String search=AjaxHelper.getHttpParam(request(), "search[value]").trim();
		int page = start/length;
		Page<ChannelMould> productPage = channelService.queryChannelMouldPage(channelId,search,page,length);
		for(ChannelMould channelMould : productPage.getContent()){
			List<ChannelMouldPro> ChannelMouldProList = channelService.findChMoProListByCmId(channelMould.getId(),channelId);
			List<ObjectNode> objectNodeList= new ArrayList<ObjectNode>();
			for(ChannelMouldPro channelMouldPro:ChannelMouldProList){
				ObjectNode objectNode = Json.newObject();
				String href = "";
				String productTitle = "";
				Long pid=channelMouldPro.getPid();
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
				objectNode.put("imag", channelMouldPro.getImgurl());
				objectNode.put("href", href);
				objectNode.put("productTitle", productTitle);
				objectNodeList.add(objectNode);
			}
			channelMould.setChannelMouldContent(objectNodeList);
			Mould mould = channelService.findMouldByMouldId(channelMould.getMouldId());
			if(mould!=null)
				channelMould.setMouldName(mould.getMname());
		}
		Map<String,Object> result = new HashMap<String, Object>();
		result.put("data", productPage.getContent());
		result.put("draw", draw);
		result.put("recordsTotal", productPage.getNumberOfElements());
		result.put("recordsFiltered", productPage.getTotalElements());
		return ok(Json.toJson(result));
	}
	
	/**
	 * 根据channelMould ID删除该频道卡片（channelMould）
	 * @return
	 */
    @AdminAuthenticated()
	public Result deletechannelContentById(Long channelContentId){
		Long channelId = Numbers.parseLong(AjaxHelper.getHttpParam(request(), "channelId"), 0L);
		channelService.deletechannelMouldById(channelContentId,channelId);
		AdminUser adminUser = getCurrentAdminUser();
		operateLogService.saveChannelMouldLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "删除频道卡片,频道卡片ID为:"+channelContentId);
		return ok(Json.toJson("1"));
	}
	
	/**
	 * 跳转至新建或编辑卡片页面
	 * @return
	 */
    @AdminAuthenticated()
	public Result newOrUpdateChannelMould(){
		Long cid = Numbers.parseLong(Form.form().bindFromRequest().get("cid"), 0L);//频道ID
		ChannelMould channelMould=new ChannelMould();
		Long cmid = Numbers.parseLong(AjaxHelper.getHttpParam(request(), "cmid"), 0L);
		if(cmid.longValue()!=0){
			channelMould=channelService.getChannelMouldById(cmid);
		}
		List<Mould> mouldList = channelService.findMouldList();
		Mould mould = null;
		if(channelMould.getMouldId()!=null){
			mould = channelService.findMouldByMouldId(channelMould.getMouldId());
		}
		List<ChannelMouldPro> chmoProList = channelService.findChMoProListByCmId(cmid,cid);
		return ok(views.html.product.newOrUpdateChannelMould.render(cid,cmid,channelMould,mould,chmoProList,
				Html.apply(ChannelService.mouldList2Html(mouldList,channelMould.getMouldId())),
				Html.apply(ShowTypes.typ2HTML(channelMould.getManType())),
				Html.apply(ShowFlag.flag2HTML(channelMould.getFlag()))
				));
	}
	/**
	 * 保存频道卡片
	 * @return
	 */
    @AdminAuthenticated()
	public Result saveChannelMould(){
    	AdminUser adminUser = getCurrentAdminUser();
    	String remark = "";
		ChannelMould channelMould=new ChannelMould();
		Long cmid = Numbers.parseLong(AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "cmid"), 0L);
		if(cmid.longValue()!=0){
			channelMould=channelService.getChannelMouldById(cmid);
			remark = "编辑频道卡片,频道卡片ID为:";
		}else{
			remark = "新建频道卡片,频道卡片ID为:";
		}
		Long cid = Numbers.parseLong(Form.form().bindFromRequest().get("cid"), 0L);//频道ID
		channelMould.setCid(cid);
		Long moulds = Numbers.parseLong(Form.form().bindFromRequest().get("moulds"), 0L);//卡片模版
		channelMould.setMouldId(moulds);
		String titleBig = Form.form().bindFromRequest().get("titleBig");
		channelMould.setTitle(titleBig);
		String titlelittle = Form.form().bindFromRequest().get("titlelittle");
		channelMould.setDatetxt(titlelittle);
		String manType = Form.form().bindFromRequest().get("manType");
		channelMould.setManType(manType);
		Integer nsort =  Numbers.parseInt(Form.form().bindFromRequest().get("nsort"),0);
		channelMould.setNsort(nsort);
		String flag = Form.form().bindFromRequest().get("flag");
		channelMould.setFlag(flag);
		channelMould.setDate_add(new Date());
		channelMould = channelService.saveChannelMould(channelMould,cid);
		List<Mould> mouldList = channelService.findMouldList();
		List<ChannelMouldPro> chmoProList = channelService.findChMoProListByCmId(channelMould.getId(),cid);
		
		operateLogService.saveChannelMouldLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), remark+channelMould.getId());
		Mould mould = channelService.findMouldByMouldId(channelMould.getMouldId());
		return ok(views.html.product.newOrUpdateChannelMould.render(cid,channelMould.getId(),channelMould,mould,chmoProList,
				Html.apply(ChannelService.mouldList2Html(mouldList,moulds)),
				Html.apply(ShowTypes.typ2HTML(manType)),
				Html.apply(ShowFlag.flag2HTML(flag))));
	}
	/**
	 * 修改频道卡片是否显示
	 * @return
	 */
    @AdminAuthenticated()
	public Result updateChmouldFlag(){
    	AdminUser adminUser = getCurrentAdminUser();
		Long channelId = Numbers.parseLong(AjaxHelper.getHttpParam(request(), "channelId"), 0L);
		Long mouldId = Numbers.parseLong(AjaxHelper.getHttpParam(request(), "mouldId"), 0L);
		String flag = AjaxHelper.getHttpParam(request(), "flag");
		ChannelMould channelMould=channelService.getChannelMouldById(mouldId);
		if(mouldId!=null){
			channelMould.setFlag(flag);
			channelMould = channelService.saveChannelMould(channelMould,channelId);
			operateLogService.saveChannelMouldStateLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), channelId,flag);
		}
		return redirect("/product/channelContentManage");
	}

	/**
	 * 跳转至选取频道页面（Iframe）
	 * @return
	 */
    @AdminAuthenticated()
	public Result channels(){
		List<Channel> channelList = channelService.findAllShow();
		return ok(views.html.product.channels.render(channelList));
	}
	/**
	 * 跳转至选取专题页面（Iframe）
	 * @return
	 */
    @AdminAuthenticated()
	public Result subjects(){
		List<Subject> subjectList = subjectService.findAllShow();
		return ok(views.html.product.subjects.render(subjectList));
	}
	/**
	 * 保存频道内容
	 * @return
	 */
    @AdminAuthenticated()
	public Result saveChannelMouldPro(){
		String endfilestr = "";
		String remark = "";
		String chmouldProId = Form.form().bindFromRequest().get("chmouldProId");//频道内容商品ID
		String typesRadios = Form.form().bindFromRequest().get("typesRadios");//类型
		String linkTitle = Form.form().bindFromRequest().get("linkTitle");//链接
		String beginTime = Form.form().bindFromRequest().get("beginTime");//开抢时间
		String nsortForSavePro = Form.form().bindFromRequest().get("nsortForSavePro");//排序
		String channelId = Form.form().bindFromRequest().get("cidForSavePro");//AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "cidForSavePro");
		Long pid =Numbers.parseLong(Form.form().bindFromRequest().get("pidForSavePro"), 0L); 
		Long cmid =Numbers.parseLong(Form.form().bindFromRequest().get("cmidForSavePro"), 0L); 
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart channelPic = body.getFile("channelPic");
        if (channelPic != null) {
        	String path=Configuration.root().getString("oss.upload.adload.image", "pimgs/adload/");//上传路径
        	path = path+channelId+"/";
        	String BUCKET_NAME=StringUtil.getBUCKET_NAME();
    		if (channelPic != null && channelPic.getFile() != null) {
    			String fileName = channelPic.getFilename();
    			File file = channelPic.getFile();//获取到该文件
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
        ChannelMouldPro channelMouldPro=new ChannelMouldPro();
        if(!Strings.isNullOrEmpty(chmouldProId)){
        	channelMouldPro = channelService.findChMoPrById(Long.parseLong(chmouldProId), Long.parseLong(channelId));
        	remark = "编辑频道卡片内容,频道卡片内容ID为:";
        }else{
        	remark = "新建频道卡片内容,频道卡片内容ID为:";
        }
        channelMouldPro.setTyp(typesRadios);
        channelMouldPro.setCmid(cmid);
        channelMouldPro.setDate_add(new Date());
        if(!"".equals(endfilestr)){
        	channelMouldPro.setImgurl(endfilestr);
        }
        channelMouldPro.setLinkurl(linkTitle);
        channelMouldPro.setPid(pid);
        channelMouldPro.setMouldId(0L);
        channelMouldPro.setNsort(Numbers.parseInt(nsortForSavePro, 0));
        ChannelMould chm = channelService.getChannelMouldById(cmid);
        Mould mould = channelService.findMouldByMouldId(chm.getMouldId());
        if(chm!=null && chm.getMouldId()!=null && mould!=null && ("4".equals(mould.getTyp())||"6".equals(mould.getTyp()))){
			try {
				channelMouldPro.setBeginTime(CHINESE_DATE_TIME_FORMAT.parse(beginTime));
			} catch (ParseException e) {
				logger.info(e.toString()+"==========beginTime is "+beginTime);
			}
        }
        
        channelMouldPro = channelService.saveChannelMouldPro(channelMouldPro,channelId,cmid);
        AdminUser adminUser = getCurrentAdminUser();
        operateLogService.saveChannelMouldProLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), remark+channelMouldPro.getId());
		return redirect("newOrUpdateChannelMould?cid="+channelId+"&cmid="+cmid);
	}
	/**
	 * 删除频道内容
	 * @return
	 */
    @AdminAuthenticated()
	public Result deleteChmouldPro(Long cmpid){
		Long cmid = Numbers.parseLong(AjaxHelper.getHttpParam(request(), "cmid"), 0L);
		channelService.deletechannelMouldProById(cmpid,cmid);
		AdminUser adminUser = getCurrentAdminUser();
        operateLogService.saveChannelMouldProLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "删除频道卡片内容,频道卡片内容ID为:"+cmpid);
		return ok(Json.toJson("1"));
	}
	/**
	 * 批量添加大图商品和双图商品
	 * @return
	 */
    @AdminAuthenticated()
	public Result channelMouldBatch(){
		String flag = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "flag");
		String channelId = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "channelId");
		String pIds = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "products");
		int sort = Numbers.parseInt(AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "sort"),0);
		logger.info("=================>>>>>>>>flag:"+flag+",channelId:"+channelId+"pIds:"+pIds+"sort:"+sort);
		JsonNode json = Json.parse(pIds);
		int i=0;
		Long chmId =0L;
		for(JsonNode temp : json){
			Long mould=0L;
			if("2".equals(flag) ){
				mould =2L;
				if(i==0){
					ChannelMould chm = new ChannelMould();
					chm.setCid(Numbers.parseLong(channelId, 0L));
					chm.setDate_add(new Date());
					chm.setFlag("0");//不显示
					chm.setTitle("");
					chm.setManType("0");
					chm.setNsort(sort);
					chm.setDatetxt("");
					chm.setMouldId(mould);
					chm = channelService.saveChannelMould(chm,Numbers.parseLong(channelId, 0L));
					chmId = chm.getId();
					i++;
				}else{
					ChannelMould chm = channelService.getChannelMouldById(chmId);
					chm.setFlag("1");//显示
					chm = channelService.saveChannelMould(chm,Numbers.parseLong(channelId, 0L));
					i=0;
				}
			}else if("1".equals(flag)){
				mould =1L;
				ChannelMould chm = new ChannelMould();
				chm.setCid(Numbers.parseLong(channelId, 0L));
				chm.setDate_add(new Date());
				chm.setFlag("1");
				chm.setTitle("");
				chm.setManType("0");
				chm.setNsort(sort);
				chm.setDatetxt("");
				chm.setMouldId(mould);
				chm = channelService.saveChannelMould(chm,Numbers.parseLong(channelId, 0L));
				chmId = chm.getId();
			}
			ChannelMouldPro chmPro = new ChannelMouldPro();
			chmPro.setPid(temp.asLong());
			chmPro.setCmid(chmId);
			chmPro.setDate_add(new Date());
			chmPro.setLinkurl("pDe://pid="+temp.asLong());
			chmPro.setNsort(0);
			chmPro.setTyp("0");
			chmPro.setMouldId(mould);
			String imag = productService.getDBListPic(temp.asLong());
			chmPro.setImgurl(StringUtil.getListpic(imag));
			chmPro = channelService.saveChannelMouldPro(chmPro, channelId,chmId);
			AdminUser adminUser = getCurrentAdminUser();
	        operateLogService.saveChannelMouldProLog(adminUser.getId(), adminUser.getUsername(), request().remoteAddress(), "新建频道卡片内容,频道卡片内容ID为"+chmPro.getId());
			
		}
		return ok("1");
	}
	
	/**
	 * 根据ID获取该频道内容商品
	 * @return
	 */
    @AdminAuthenticated()
	public Result getChmouldProById(Long chmouldProId, Long channelId){
		ChannelMouldPro chMouldPro = channelService.findChMoPrById(chmouldProId,channelId);
		return ok(Json.toJson(chMouldPro));
	}
    
    
    /**
     * 
     * <p>Title: piliangChangeNsort</p> 
     * <p>Description: 批量修改频道排序值</p> 
     * @return
     */
    @AdminAuthenticated()
    public Result piliangChangeNsort(){
    	String cmids =AjaxHelper.getHttpParam(request(), "cmids");
    	int newnsort =Numbers.parseInt(AjaxHelper.getHttpParam(request(), "newnsort"), 0);
    	if(!Strings.isNullOrEmpty(cmids)&&newnsort!=0){
    		String[] ids = cmids.split(",");
    		for (String str : ids) {
				ChannelMould channelMould = channelService.getChannelMouldById(Numbers.parseLong(str, 0L));
				if(channelMould!=null){
					channelMould.setNsort(newnsort);
					channelService.saveChannelMould(channelMould, channelMould.getId());
				}
			}
    	}
    	return redirect("/product/channelContentManage");
    }
}
