package models

import play.api.libs.json.Json

/**
  * Created by HuyNguyen on 6/4/17.
  */
case class UserData(username: String, password: String, email: String){
  def getUsername(): String = username

  def getPassword(): String = password

  def getEmail(): String = email

}

object UserData{
  implicit val userFormat= Json.format[UserData]
}

