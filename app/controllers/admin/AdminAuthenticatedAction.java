package controllers.admin;

import javax.inject.Inject;
import javax.inject.Named;

import models.admin.AdminUser;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;

import play.Logger;
import play.libs.F;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;
import services.admin.AdminUserService;
import utils.Constants;

/**
 * @author luobotao
 * Date: 2015年4月15日 上午10:49:39
 */
@Named
@Scope("prototype")
public class AdminAuthenticatedAction extends Action<AdminAuthenticated> {
	private static final Logger.ALogger LOGGER = Logger.of(AdminAuthenticatedAction.class);

	private final AdminUserService adminUserService;

	@Inject
	public AdminAuthenticatedAction(final AdminUserService adminUserService) {
		this.adminUserService = adminUserService;
	}

	@Override
	public Promise<Result> call(final Context ctx) throws Throwable {
		F.Promise<Result> result;
		if(StringUtils.isBlank(AdminSession.get())){
			AdminSession.remove();
			result = Promise.promise(new Function0<Result>() {
				@Override
				public Result apply() throws Throwable {
					return unauthorized(views.html.unauthorized.render());
				}
			});
		}else{
			Long id = Long.valueOf(AdminSession.get());
			AdminUser adminUser;
			if (id != null && (adminUser = adminUserService.getAdminUser(id)) != null) {
				AdminSession.put(String.valueOf(adminUser.getId()));
				ctx.args.put(Constants.USER, adminUser);
				result = delegate.call(ctx);
			} else {
				LOGGER.debug("{} is invalid for admin.", ctx.request().uri());
				AdminSession.remove();
				result = Promise.promise(new Function0<Result>() {
					@Override
					public Result apply() throws Throwable {
						return unauthorized(views.html.unauthorized.render());
					}
				});
			}
			
		}
		return result;
	}

}
