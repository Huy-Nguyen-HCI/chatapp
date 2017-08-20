package dao

import models.User
import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile


private[dao] trait UserComponents { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import driver.api._

  /*
   * User table
   */
  class UsersTable(tag: Tag) extends Table[User](tag, "user") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username")
    def password = column[String]("password")
    def email = column[String]("email")

    override def * = (id.?, username, password, email) <> (User.tupled, User.unapply)

    def usernameIndex = index("username_constraint", username, unique = true)
    def emailIndex = index("email_constraint", email, unique = true)
  }
}

