package controllers;

import models.admin.AdminUser;
import play.mvc.Controller;
import utils.Constants;

public class BaseController extends Controller{
	public AdminUser getCurrentAdminUser() {
		return (AdminUser) ctx().args.get(Constants.USER);
	}

}
