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

  // return the ids in ascending order. This ensures that
  // there is no duplicate pair of ids
  private def sortIds(id1: Long, id2: Long) = {
    val first = math.min(id1, id2)
    val second = math.max(id1, id2)
    (first, second)
  }

  // return an instance of Friendship with sorted ids and friend status
  private def buildFriendship(id1: Long, id2: Long, status: Int) = {
    val actionId = id1
    val (first, second) = sortIds(id1, id2)
    Friendship(first, second, status, actionId)
  }


  def insertOrUpdateFriendship(id1: Long, id2: Long, status: Int): Future[Unit] = {
    val friendship = buildFriendship(id1, id2, status)
    db.run(friendships.insertOrUpdate(friendship)).map(_ => ())
  }


  def checkFriendship(id1: Long, id2: Long): Future[Option[(Int, Long)]] = {
    // SELECT * FROM `friendship` WHERE `user_one_id` = 1 AND `user_two_id` = 2 AND `status` = 1
    val (first, second) = sortIds(id1, id2)
    val q = friendships.filter(f => f.id1 === first && f.id2 === second)
                       .map(res => (res.status, res.actionId))
                       .result.headOption
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
    def actionId = column[Long]("action_id")

    override def * = (id1, id2, status, actionId) <> ((Friendship.apply _).tupled, Friendship.unapply)
    def idx = index("friendship_index", (id1, id2), unique = true)
  }
}
