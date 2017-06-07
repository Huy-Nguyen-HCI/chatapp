package models

case class User(username: String, password: String, email: String)
case class UserSignupData(username: String, password: String, email: String)
case class UserLoginData(username: String, password: String)
