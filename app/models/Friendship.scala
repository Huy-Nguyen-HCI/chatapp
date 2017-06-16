package models

case class Friendship(id1: Long, id2: Long, status: Int, actionId: Int)

object Friendship {
  val STATUS_PENDING = 0
  val STATUS_ACCEPTED = 1
  val STATUS_DECLINED = 2
  val STATUS_BLOCKED = 3
}