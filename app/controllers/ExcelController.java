/*package controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.codec.binary.Base64;
import play.Logger;
import play.mvc.Result;
import utils.AjaxHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

*//**
 * 
 * <p>Title: ExcelTemplate.java</p> 
 * <p>Description: datatables 导出excel工具类</p> 
 * <p>Company: </p> 
 * @author  
 * date  2015年7月17日  下午2:50:44
 * @version
 *//*
@Named
@Singleton
public class ExcelController extends BaseController{
    private static final long serialVersionUID = 643369157164454172L;
    private static final Logger.ALogger log = Logger.of(ExcelController.class);

    *//**
     * 
     * <p>Title: exportExcel</p>
     * <p>Description: exportExcel using input data </p>
     * 
     * @return
     *//*
    public Result exportExcel() {
    	response().setContentType("application/json;charset=utf-8");
		 //获取商品
        String data = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "data");
        String filename = AjaxHelper.getHttpParamOfFormUrlEncoded(request(), "filename");
        if (data == null) {
            return ok("参数错误！");
        }
        
        // default filename 
        if (StringUtils.isBlank(filename)) {
            filename = "export.xls";
        }
        List<List<String>> list = new Gson().fromJson(data, new TypeToken<List<List<String>>>() {}.getType()); 
        if (list != null) {
            log.debug("export data converted list size is {}", list.size());
        }
        File downFile = ComponentExcel.toExcel(list); 
        String fileNameChine = filename;
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
    
    
}
*/