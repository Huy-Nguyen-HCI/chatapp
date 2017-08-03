package models

import java.io.FileInputStream
import javax.inject.{Inject, Singleton}

import play.api.Environment
import play.api.libs.json.Json

/**
  * Created by thangle on 8/1/17.
  */
@Singleton
class FriendshipStatusCode @Inject()(env: Environment) {
  private val stream = new FileInputStream(env.getFile("/public/resources/friendship-status.json"))
  private val json = try { Json.parse(stream) } finally { stream.close() }

  val PENDING: Int = (json \ "PENDING").as[Int]
  val ACCEPTED: Int = (json \ "ACCEPTED").as[Int]
  val DECLINED: Int = (json \ "DECLINED").as[Int]
  val BLOCKED: Int = (json \ "BLOCKED").as[Int]
}

