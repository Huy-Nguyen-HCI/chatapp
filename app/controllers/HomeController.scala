package controllers

import play.api.mvc._

/**
  * Created by thangle on 8/10/17.
  */
class HomeController extends Controller {
  def index = Action { implicit request =>
    Redirect(routes.LoginController.index())
  }
}
