package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import play.filters.csrf._

import models._

/**
  * Handles logging in and creating accounts
  */
class LoginController extends Controller {

  val userForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "email" -> nonEmptyText
    )(UserData.apply)(UserData.unapply)
  )

  def index = Action {
    Ok(views.html.login())
  }

  def login = Action { implicit request =>
    val userLogin : UserData = userForm.bindFromRequest.get
    val username : String = userLogin.getUsername()
    val password : String = userLogin.getPassword()
    val email : String = userLogin.getEmail()
    println("username: " + username)
    println("password: " + password)
    println("emaiL: " + email)
    Ok("hello")
  }
}
