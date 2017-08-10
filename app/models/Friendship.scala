package models

case class Friendship(id1: Long, id2: Long, status: Int, actionId: Long)

object Friendship {
  val PENDING: Int = 0
  val ACCEPTED: Int = 1
  val DECLINED: Int = 2
  val BLOCKED: Int = 3

  def getStatusJsonString: String =
    s"""{ "PENDING": $PENDING, "ACCEPTED": $ACCEPTED, "DECLINED": $DECLINED, "BLOCKED": $BLOCKED }"""
}
