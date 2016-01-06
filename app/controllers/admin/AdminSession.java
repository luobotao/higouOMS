package controllers.admin;

import play.mvc.Http;
import utils.Constants;

/**
 * 后台管理端Session
 * @author luobotao
 * Date: 2015年4月14日 下午6:18:12
 */
public class AdminSession {

    public static void put(String value) {
        Http.Context.current().response().setCookie(Constants.ADMIN_SESSION_NAME, value, 3600, "/", null, false, true);
    }

    public static void remove() {
        Http.Context.current().response().discardCookie(Constants.ADMIN_SESSION_NAME, "/", null, true);
    }

    public static String get() {
        Http.Cookie cookie = Http.Context.current().request().cookie(Constants.ADMIN_SESSION_NAME);
        return null == cookie ? null : cookie.value();
    }

}
