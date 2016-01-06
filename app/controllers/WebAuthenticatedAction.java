package controllers;

import javax.inject.Named;

import models.admin.AdminUser;

import org.springframework.context.annotation.Scope;

import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;
import utils.Constants;
import utils.UserLoginCache;

@Named
@Scope("prototype")
public class WebAuthenticatedAction extends Action<WebAuthenticated> {

	@Override
	public Promise<Result> call(final Context ctx) throws Throwable {
        String token = ctx.session().get(Constants.SESSION_TOKEN);
        AdminUser user = UserLoginCache.getByTokenAndRefresh(token);

        if (user == null && configuration.process()) {
            return Promise.promise(new Function0<Result>() {
                @Override
                public Result apply() throws Throwable {
                    ctx.session().clear();
                    return redirect("/");
                }
            });
        }

        ctx.args.put(Constants.USER, user);

        return delegate.call(ctx);
	}

}

