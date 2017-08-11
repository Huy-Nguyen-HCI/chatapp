/**
 * Created by thang on 7/4/17.
 */
var userListModule = angular.module('userList', ['ngResource', 'ngWebSocket']);


userListModule.factory('Users', ['$resource', function($resource) {
  return $resource('/api/users/:name', {name: '@username'});
}]);


userListModule.factory('Friendship', ['$resource', function($resource) {
  return $resource('/api/friend/:action');
}]);


userListModule.factory('friendRequestHandler', friendRequestHandlerFactory);

friendRequestHandlerFactory.$inject = ['$websocket', 'Friendship', 'CSRF_TOKEN', 'USERNAME', 'STATUS_CODES'];

function friendRequestHandlerFactory ($websocket, Friendship, CSRF_TOKEN, USERNAME, STATUS_CODES) {
  console.log("here");
  var notifications = [];

  var ws = $websocket(getWebSocketUri("/noti/socket"));

  ws.onMessage(function (msg) {
    console.log(msg);
    var data = JSON.parse(msg.data);
    notifications.unshift({sender: data.sender, status: data.status});
  });

  ws.onClose(function (msg) {
    console.log("noti websocket is close");
  });

  // functions for handling friend requests
  function sendRequest(action, otherUser) {
    var actionToStatus = {
      'add': STATUS_CODES.PENDING,
      'accept': STATUS_CODES.ACCEPTED
    };
    Friendship.save(
      {action: action, csrfToken: CSRF_TOKEN},
      {sender: USERNAME, receiver: otherUser},
      function () {
        if (action !== 'remove') {
          ws.send(JSON.stringify({sender: USERNAME, receiver: otherUser, status: actionToStatus[action]}));
        }
      },
      function (error) {
        console.log(error);
      }
    )
  }

  return {
    notifications: notifications,

    addFriend: function (otherUser) {
      sendRequest('add', otherUser);
    }
    ,
    acceptFriend: function (otherUser) {
      sendRequest('accept', otherUser);
    },

    removeFriend: function (otherUser) {
      sendRequest('remove', otherUser);
    },

    checkStatus: function (otherUser) {
      return Friendship.get({action: 'check', first: USERNAME, second: otherUser});
    }
  };
}
