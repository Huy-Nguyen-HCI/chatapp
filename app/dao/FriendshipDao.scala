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

  // get the current friendship between two users with id1 and id2
  def getFriendship(id1: Long, id2: Long): Future[Option[(Int, Long)]] = {
    // SELECT * FROM `friendship` WHERE `user_one_id` = 1 AND `user_two_id` = 2 AND `status` = 1
    val (first, second) = sortIds(id1, id2)
    val q = friendships.filter(f => f.id1 === first && f.id2 === second)
                       .map(res => (res.status, res.actionId))
    db.run(q.result.headOption)
  }

  // get the ids of all users who is making an action with the specified status to this user id
  def listUsersMakingStatus(id: Long, status: Int): Future[Seq[Long]] = {
    val q = friendships.filter(f =>
      (f.id1 === id || f.id2 === id) && f.actionId =!= id && f.status === status
    ).map(f => Case If f.id1 =!= id Then f.id1 Else f.id2)
    db.run(q.result)
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
