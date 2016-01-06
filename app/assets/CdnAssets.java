package assets;

import play.Configuration;
import play.Play;
import controllers.AssetsBuilder;


public class CdnAssets extends AssetsBuilder {

    
    private static String getCDN_BASE_URL(){
    	if(Play.application().configuration().getBoolean("production", false)){
        	return Configuration.root().getString("cdn.url.product", "http://192.168.0.109/");
        }else{
        	return Configuration.root().getString("cdn.url.dev", "http://192.168.0.109/");
        }
    }
    

    private static final String CDN_ADMIN_ADMINLTE_URL;
    private static final String CDN_ADMIN_ADMINLTE_PLUGINS_URL;
    private static final String CDN_ADMIN_BOOTSTRAP_URL;
    private static final String CDN_ADMIN_EDITOR_URL;
    public static final String CDN_ADMIN_PUBLIC_URL;
    static {
        CDN_ADMIN_ADMINLTE_URL = getCDN_BASE_URL() + "/adminLTE/";
        CDN_ADMIN_BOOTSTRAP_URL = getCDN_BASE_URL() + "/bootstrap/";
        CDN_ADMIN_ADMINLTE_PLUGINS_URL = CDN_ADMIN_ADMINLTE_URL + "plugins/";
    	CDN_ADMIN_PUBLIC_URL = getCDN_BASE_URL() + "/public/";
    	CDN_ADMIN_EDITOR_URL = getCDN_BASE_URL() + "/umeditor/";
    }

    /*后台模版等文件所在路径*/
    public static String urlForAdminPublic(String file) {
        StringBuilder sb = new StringBuilder(CDN_ADMIN_PUBLIC_URL);
        sb.append(file);
        return sb.toString();
    }
    /*后台系统静态资源AdminLTE根路径*/
    public static String urlForAdminLTE(String file) {
        StringBuilder sb = new StringBuilder(CDN_ADMIN_ADMINLTE_URL);
        sb.append(file);
        return sb.toString();
    }
    /*后台系统静态资源Bootstrap根路径*/
    public static String urlForAdminBootstrap(String file) {
    	StringBuilder sb = new StringBuilder(CDN_ADMIN_BOOTSTRAP_URL);
    	sb.append(file);
    	return sb.toString();
    }
    /*后台系统静态资源Plugins根路径*/
    public static String urlForAdminLTEPlugins(String file) {
    	StringBuilder sb = new StringBuilder(CDN_ADMIN_ADMINLTE_PLUGINS_URL);
    	sb.append(file);
    	return sb.toString();
    }
    /*后台系统静态资源Plugins根路径*/
    public static String urlForEditor(String file) {
    	StringBuilder sb = new StringBuilder(CDN_ADMIN_EDITOR_URL);
    	sb.append(file);
    	return sb.toString();
    }
}
