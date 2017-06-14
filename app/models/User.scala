package models

case class User(id: Option[Long] = None, username: String, password: String, email: String)

