package dao

import javax.inject.{Inject, Singleton}

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import models.Friendship

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class FriendshipDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
                        (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  private val Friendships = TableQuery[FriendshipTable]

  // def insert(user: User): Future[Unit] = db.run(Users += user).map(_ => ())
  def insertFriendRequest(friendship: Friendship) : Future[Unit] = {
    // INSERT INTO `friendship` (`username1, `username2`, `status`, `action_id`) VALUES (1, 2, 0, 1)
    db.run(Friendships += friendship).map(_ => ())
  }

  def acceptFriendRequest(username1: String, username2: String) : Future[Unit] = {
    // UPDATE `friendship` SET `status` = 1, `action_id` = `2` WHERE `username1` = username1 AND `username2` = username2

  }

  def checkFriendship(username1: String, username2: String) : Future[Boolean] = {
    // SELECT * FROM `friendship` WHERE `user_one_id` = 1 AND `user_two_id` = 2 AND `status` = 1
  }

  def getFriends(username: String) = {
    // SELECT * FROM `friendship` WHERE (`username1` = username OR `username2` = username) AND `status` = 1
  }

  /*
   * User table
   */
  private class FriendshipTable(tag: Tag) extends Table[Friendship](tag, "friendship") {

    def username1 = column[String]("USERNAME1")
    def username2 = column[String]("USERNAME2")
    def status = column[Int]("STATUS")
    def actionId = column[Int]("ACTION_ID")

    override def * = (username1, username2, status, actionId) <> (Friendship.tupled, Friendship.unapply)
  }
}
