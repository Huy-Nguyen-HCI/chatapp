(function() {
  'use strict';

  var userListModule = angular.module('userList', ['ngResource', 'ngWebSocket']);


  userListModule.factory('Users', ['$resource', function($resource) {
    return $resource('/api/users/:name', {name: '@username'});
  }]);


  userListModule.factory('Friendship', ['$resource', function($resource) {
    return $resource('/api/friend/:action');
  }]);


  userListModule.factory('WebSocketData', WebSocketData);

  WebSocketData.$inject = ['$websocket', 'Friendship', 'CSRF_TOKEN', 'USERNAME', 'STATUS_CODES'];

  function WebSocketData ($websocket, Friendship, CSRF_TOKEN, USERNAME, STATUS_CODES) {
    var chatMessages = [];
    var notifications = [];

    var ws = $websocket(getWebSocketUri("/socket"));

    ws.onMessage(function (msg) {
      var data = JSON.parse(msg.data);
      if (data.type === 'chat-message') {
        chatMessages.push(data);
      } else {
        notifications.unshift(data);
      }
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
            ws.send(JSON.stringify({type: 'friend-request',
              sender: USERNAME, receiver: otherUser, status: actionToStatus[action]}));
          }
        },
        function (error) {
          console.log(error);
        }
      )
    }

    return {
      Chat: {
        chatMessages: chatMessages,
        send: function (msg) {
          ws.send(JSON.stringify(msg));
        }
      },

      Notification: {
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
      }
    };
  }
})();

