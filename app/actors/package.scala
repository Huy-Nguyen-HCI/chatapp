/**
  * Created by thangle on 7/26/17.
  */
package object actors {
  val WS_SENDER_KEY = "sender"
  val WS_RECEIVER_KEY = "receiver"

  final case class ClientSentMessage(text: String)
}
