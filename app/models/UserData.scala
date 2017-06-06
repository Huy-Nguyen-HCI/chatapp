package models

import play.api.libs.json.Json

/**
  * Created by HuyNguyen on 6/4/17.
  */
case class UserData(username: String, password: String){
  def getUsername(): String = username

  def getPassword(): String = password

}

object UserData{
  implicit val userFormat= Json.format[UserData]
}

