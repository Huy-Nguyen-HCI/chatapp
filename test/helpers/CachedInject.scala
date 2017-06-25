package helpers

import play.api.Application

import scala.reflect.ClassTag

/**
  * Created by thang on 6/22/17.
  */
trait CachedInject {
  def getInstance[T: ClassTag](implicit app: Application): T = {
    val app2Instance = Application.instanceCache[T]
    app2Instance(app)
  }
}