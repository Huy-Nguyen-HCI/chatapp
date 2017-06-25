package controllers

import java.io.File
import java.nio.file.attribute.PosixFilePermission._
import java.nio.file.attribute.PosixFilePermissions
import java.nio.file.{Files, Path, Paths}
import java.util
import javax.inject._

import akka.stream.IOResult
import akka.stream.scaladsl._
import akka.util.ByteString
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi
import play.api.libs.streams._
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.core.parsers.Multipart.FileInfo

import scala.concurrent.Future

case class FileFormData(name: String)

/**
  * This controller handles a file upload.
  */
@Singleton
class FileController @Inject() (implicit val messagesApi: MessagesApi) extends Controller with i18n.I18nSupport {

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  val form = Form(
    mapping(
      "name" -> text
    )(FileFormData.apply)(FileFormData.unapply)
  )

  type FilePartHandler[A] = FileInfo => Accumulator[ByteString, FilePart[A]]


  /**
    * Uses a custom FilePartHandler to return a type of "File" rather than
    * using Play's TemporaryFile class.  Deletion must happen explicitly on
    * completion, rather than TemporaryFile (which uses finalization to
    * delete temporary files).
    *
    * @return
    */
  private def handleFilePartAsFile: FilePartHandler[File] = {
    case FileInfo(partName, filename, contentType) =>
      val attr = PosixFilePermissions.asFileAttribute(util.EnumSet.of(OWNER_READ, OWNER_WRITE))
      val path: Path = Files.createTempFile("multipartBody", "tempFile", attr)
      val file = path.toFile
      val fileSink: Sink[ByteString, Future[IOResult]] = FileIO.toFile(file)
      val accumulator: Accumulator[ByteString, IOResult] = Accumulator(fileSink)
      accumulator.map {
        case IOResult(count, status) =>
          logger.info(s"count = $count, status = $status")
          FilePart(partName, filename, contentType, file)
      }(play.api.libs.concurrent.Execution.defaultContext)
  }

  /**
    * A generic operation on the temporary file that deletes the temp file after completion.
    */
  private def operateOnTempFile(file: File) = {
//    val size = Files.size(file.toPath)
//    logger.info(s"size = ${size}")
//    Files.deleteIfExists(file.toPath)
//    size
    Files.move(file.toPath, Paths.get("file-upload/" + file.getName))
  }

  /**
    * Uploads a multipart file as a POST request.
    *
    * @return an OK response containing information about the file size
    */
  def upload = Action(parse.multipartFormData(handleFilePartAsFile)) { implicit request =>
    val fileOption = request.body.file("file").map {
      case FilePart(key, filename, contentType, file) =>
        logger.info(s"key = ${key}, filename = ${filename}, contentType = ${contentType}, file = $file")
        val data = operateOnTempFile(file)
        data
    }

    Ok(s"file size = ${fileOption.getOrElse("no file")}")
  }

}
