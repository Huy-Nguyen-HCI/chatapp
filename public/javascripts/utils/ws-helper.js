/**
 * Created by thangle on 8/8/17.
 */
/**
 * Returns the URI of the WebSocket.
 * @param s path to the WebSocket
 * @returns {string}
 */
function getWebSocketUri (s) {
  var loc = window.location;
  var wsUri;

  if (loc.protocol === "https:") {
    wsUri = "wss:";
  } else {
    wsUri = "ws:";
  }

  wsUri += "//" + loc.host;
  wsUri += s;

  return wsUri;
}