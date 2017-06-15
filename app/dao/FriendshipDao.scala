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

  private val friendships = TableQuery[FriendshipTable]

  // def insert(user: User): Future[Unit] = db.run(Users += user).map(_ => ())
  def insertFriendRequest(friendship: Friendship) : Future[Unit] = {
    // INSERT INTO `friendship` (`username1, `username2`, `status`, `action_id`) VALUES (1, 2, 0, 1)
    db.run(friendships += friendship).map(_ => ())
  }

  def acceptFriendRequest(id1: Long, id2: Long) : Future[Unit] = {
    // UPDATE `friendship` SET `status` = 1, `action_id` = `2` WHERE `username1` = username1 AND `username2` = username2
    val q = for { f <- friendships if f.id1 === id1 && f.id2 === id2 } yield (f.status, f.actionId)
    db.run(q.update(1, 2)).map(_ => ())
  }

  def checkFriendship(id1: Long, id2: Long) : Future[Boolean] = {
    // SELECT * FROM `friendship` WHERE `user_one_id` = 1 AND `user_two_id` = 2 AND `status` = 1
    val q = friendships.filter(f => f.id1 === id1 && f.id2 === id2 && f.status === 1).exists.result
    db.run(q)
  }

  def getFriendships(id: Long): Future[Seq[Friendship]] = {
    // SELECT * FROM `friendship` WHERE (`username1` = username OR `username2` = username) AND `status` = 1
    db.run(friendships.filter(f => (f.id1 === id || f.id2 === id) && f.status === 1).result)
  }

  /*
   * User table
   */
  private class FriendshipTable(tag: Tag) extends Table[Friendship](tag, "friendship") {

    def id1 = column[Long]("id1")
    def id2 = column[Long]("id2")
    def status = column[Int]("status")
    def actionId = column[Int]("action_id")

    override def * = (id1, id2, status, actionId) <> (Friendship.tupled, Friendship.unapply)
    def idx = index("friendship_index", (id1, id2), unique = true)
  }
}
