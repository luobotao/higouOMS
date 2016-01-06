package controllers;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.codec.binary.Base64;
import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.node.ObjectNode;

import models.admin.AdminUser;
import models.admin.OperateLog;
import models.product.Category;
import models.product.Currency;
import models.product.Fromsite;
import models.product.Product;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.twirl.api.Html;
import play.mvc.Result;
import services.ICacheService;
import services.NeolixService;
import services.OperateLogService;
import services.ServiceFactory;
import services.SfExpressService;
import services.product.CategoryService;
import services.product.ProductService;
import utils.Constants;
import utils.Dates;
import utils.Errors;
import utils.FileUtils;
import utils.OSSUtils;
import utils.StringUtil;
import utils.Constants.ProductStatus;
import utils.Constants.TypsProduct;
import vo.ImportNeolixVO;
import vo.ImportSfExpressVO;
import controllers.admin.AdminAuthenticated;
import forms.CreateOrUpdateUserForm;
import forms.DateBetween;
import forms.OperateLogQueryForm;
import forms.ProductManageForm;
import forms.admin.FreshTaskQueryForm;

@Named
@Singleton
public class Application extends BaseController {
	private static final Logger.ALogger logger = Logger.of(Application.class);
	
	private final String error = "error";
	private final Form<CreateOrUpdateUserForm> createOrUpdateUserForm = Form.form(CreateOrUpdateUserForm.class);
	private final Form<OperateLogQueryForm> operateLogQueryForm = Form.form(OperateLogQueryForm.class);
	
	private final SfExpressService sfExpressService;
	private final NeolixService neolixService;
	private final OperateLogService operateLogService;
	
	@Inject
	public Application(final SfExpressService sfExpressService, final NeolixService neolixService,final OperateLogService operateLogService){
		this.sfExpressService = sfExpressService;
		this.neolixService = neolixService; 
		this.operateLogService = operateLogService; 
	}

	public static Result index() {
		return redirect("/admin");
	}
	
	@AdminAuthenticated()
	public Result welcome() {
		return ok(views.html.welcome.render(getCurrentAdminUser()));
	}
	
    public Result createUser() {
        Form<CreateOrUpdateUserForm> form = createOrUpdateUserForm
                .bindFromRequest(request());
        if (form.hasErrors()) {
        	logger.info(Errors.wrapedWithDiv(form.errors()));
            flash(error, Errors.wrapedWithDiv(form.errors()));
            return redirect(controllers.routes.Application.index());
        } else {
            AdminUser user = null;
            String reason = null;
            return ok("");
        }
    }

	/**
	 * 页面失败跳转页面
	 * @return
	 */
	public static Result pageError(){
		return ok(views.html.pageError.render());
	}
	/**
	 * 日志列表跳转页面
	 * @return
	 */
	@AdminAuthenticated()
	public Result loglist(){
		Form<OperateLogQueryForm> form = operateLogQueryForm.bindFromRequest();
		OperateLogQueryForm formPage = new OperateLogQueryForm();
		if (!form.hasErrors()) {
        	formPage = form.get();
        }
		Page<OperateLog> operateLogPage = operateLogService.findOperateLogPage(formPage);
		return ok(views.html.operateLogManage.render(operateLogPage));
	}
	/**
	 * 顺丰嘿客店导入管理
	 * @return
	 */
	@AdminAuthenticated()
	public Result sfImportManage(){
		return ok(views.html.sfImportManage.render(flash(error)));
	}
	/**
	 * 顺丰嘿客店导出管理
	 * @return
	 */
	@AdminAuthenticated()
	public Result sfExportManage(){
		return ok(views.html.sfExportManage.render(flash(error)));
	}
	
	/**
	 * 顺丰嘿客店文件上传
	 * @return
	 */
	@AdminAuthenticated()
	public Result sfUpload(){
		
		MultipartFormData body = request().body().asMultipartFormData();
        FilePart sfFile = body.getFile("sfFile");
        if (sfFile != null) {
            String fileName = sfFile.getFilename();
            if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                File sfExcel = sfFile.getFile();
                List<ImportSfExpressVO> invalidSfExpress = sfExpressService.extractSfInfo(sfExcel);
                if(invalidSfExpress==null){
                	 flash(error, "文件内容非法");
                     return redirect(routes.Application.sfImportManage());
                }
                for(ImportSfExpressVO importSfExpressVO:invalidSfExpress){
                	sfExpressService.run_SfOrder(importSfExpressVO);
                }
                sfExpressService.run_SFOrder_act();
                flash(error, "导入成功");
                return redirect(routes.Application.sfImportManage());
            } else {
                flash(error, "文件格式错误");
                return redirect(routes.Application.sfImportManage());
            }
        } else {
            flash(error, "上传的excel文件不存在");
            return redirect(routes.Application.sfImportManage());
        }
	}
	
	/**
	 * 嘿客店导出文件
	 * @return
	 */
	@AdminAuthenticated()
	public Result sfExportFile(){
		FreshTaskQueryForm form =  Form.form(FreshTaskQueryForm.class).bindFromRequest(request()).get();
		SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd");
		String start = formate.format(form.between.start);
		String end = formate.format(form.between.end);
		File downFile = sfExpressService.exportSfFile(start,end);
		String fileNameChine = "嘿客店运单.xls";
		try {
			if (request().getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
				fileNameChine = "=?UTF-8?B?" + (new String(Base64.encodeBase64(fileNameChine.getBytes("UTF-8")))) + "?=";    
	        } else{
	        	fileNameChine = java.net.URLEncoder.encode(fileNameChine, "UTF-8");
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		response().setHeader("Content-disposition","attachment;filename=" + fileNameChine);
		response().setContentType("application/msexcel");
		return ok(downFile);
	}
	
	/**
	 * 新石器导入管理
	 * @return
	 */
	@AdminAuthenticated()
	public Result neolixImportManage(){
		return ok(views.html.neolixImportManage.render(flash(error)));
	}
	/**
	 * 新石器导出管理
	 * @return
	 */
	@AdminAuthenticated()
	public Result neolixExportManage(){
		return ok(views.html.neolixExportManage.render(flash(error)));
	}
	
	/**
	 * 新石器文件上传
	 * @return
	 */
	@AdminAuthenticated()
	public Result neolixUpload(){
		
		MultipartFormData body = request().body().asMultipartFormData();
        FilePart neolixFile = body.getFile("neolixFile");
        if (neolixFile != null) {
            String fileName = neolixFile.getFilename();
            if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                File neolixExcel = neolixFile.getFile();
                List<ImportNeolixVO> invalidNeolixExpress = neolixService.extractNeolixInfo(neolixExcel);
                if(invalidNeolixExpress==null){
                	 flash(error, "文件内容非法");
                	 return redirect(routes.Application.neolixImportManage());
                }
                for(ImportNeolixVO importNeolixExpressVO:invalidNeolixExpress){
                	neolixService.run_NeolixOrder(importNeolixExpressVO);
                }
                flash(error, "导入成功");
                return redirect(routes.Application.neolixImportManage());
            } else {
                flash(error, "文件格式错误");
                return redirect(routes.Application.neolixImportManage());
            }
        } else {
            flash(error, "上传的excel文件不存在");
            return redirect(routes.Application.neolixImportManage());
        }
	}
	
	/**
	 * 新石器导出文件
	 * @return
	 */
	@AdminAuthenticated()
	public Result neolixExportFile(){
		FreshTaskQueryForm form =  Form.form(FreshTaskQueryForm.class).bindFromRequest(request()).get();
		SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd");
		String start = formate.format(form.between.start);
		String end = formate.format(form.between.end);
		File downFile = neolixService.exportNeolixFile(start,end);
		String fileNameChine = "新石器运单.xls";
		try {
			if (request().getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
				fileNameChine = "=?UTF-8?B?" + (new String(Base64.encodeBase64(fileNameChine.getBytes("UTF-8")))) + "?=";    
	        } else{
	        	fileNameChine = java.net.URLEncoder.encode(fileNameChine, "UTF-8");
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		response().setHeader("Content-disposition","attachment;filename=" + fileNameChine);
		response().setContentType("application/msexcel");
		return ok(downFile);
		
	}
	/**
	 * editor上传文件
	 */
	public static Result imgUpload(){
		MultipartFormData imageBody = request().body().asMultipartFormData();
		FilePart contentFile = imageBody.getFile("upfile");
		String path=Configuration.root().getString("oss.upload.editor.image", "upload/editor/images/");//上传路径
		String BUCKET_NAME=Configuration.root().getString("oss.bucket.name.higouAPIDev", "higou-api");
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		if(IsProduct){
			BUCKET_NAME=Configuration.root().getString("oss.bucket.name.higouAPIProduct", "higou-api");
		}
		if (contentFile != null && contentFile.getFile() != null) {
			String fileName = contentFile.getFilename();
			File file = contentFile.getFile();//获取到该文件
			int p = fileName.lastIndexOf('.');
			String type = fileName.substring(p, fileName.length()).toLowerCase();
			// 检查文件后缀格式
			String fileNameLast = UUID.randomUUID().toString().replaceAll("-", "")+type;//最终的文件名称
			String endfilestr = OSSUtils.uploadFile(file,path,fileNameLast, type,BUCKET_NAME);		
			String domains=StringUtil.getPICDomain();
			ObjectNode result = Json.newObject();
			result.put("name", fileNameLast);
			result.put("originalName", fileName);
			result.put("size", ""+FileUtils.getSize(file));
			result.put("state", "SUCCESS");
			result.put("type", FileUtils.getStrFileExt(fileName));
			result.put("url", domains+endfilestr);
			response().setContentType("text/html;charset=utf-8");
			return ok(Json.toJson(result).toString());
		}else{
			return ok();
		}
	}
}
