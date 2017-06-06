package controllers

import javax.inject.Inject

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.db.Database
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import models._

/**
  * Handles logging in and creating accounts
  */
class LoginController @Inject()(db: Database) extends Controller {

  val userForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "email" -> nonEmptyText
    )(UserData.apply)(UserData.unapply)
  )

  def index = Action { implicit request =>
    Ok(views.html.login(userForm))
  }

  def login = Action { implicit request =>
    val userLogin : UserData = userForm.bindFromRequest.get
    val username : String = userLogin.getUsername()
    val password : String = userLogin.getPassword()
    val email : String = userLogin.getEmail()
    val savedPassword: String = DBUtility.getPasswordFromUsername(db, username)
    if (savedPassword == null || savedPassword != password) {
      BadRequest("incorrect username or password")
    }
    Ok("welcome " + username)
  }
}
