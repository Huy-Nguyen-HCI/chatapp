/**
 * Created by thangle on 7/20/17.
 */
angular
  .module('navBar')
  .service('FriendSocket', ['$websocket', function ($websocket) {

    var loc = window.location;
    var new_uri;
    if (loc.protocol === "https:") {
      new_uri = "wss:";
    } else {
      new_uri = "ws:";
    }
    new_uri += "//" + loc.host;
    new_uri += loc.pathname + "/notifications";

    var dataStream = $websocket(new_uri);

    var requests = {};

    dataStream.onMessage(function(message) {
      var jsonObj = JSON.parse(message.data);
      var sender = jsonObj.sender;
      var receiver = jsonObj.receiver;

      if (!requests[receiver]) {
        requests[receiver] = new Set();
      }
      requests[receiver].add(sender);
    });

    return {
      requests: requests,
      getRequest: function(sender, receiver) {
        dataStream.send(JSON.stringify({sender: sender, receiver: receiver}))
      }
    };
  }]);