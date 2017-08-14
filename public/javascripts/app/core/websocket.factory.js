/**
 * Created by thangle on 8/13/17.
 */
(function() {
  'use strict';

  angular
    .module('app.core')
    .factory('webSocketFactory', webSocketFactory);

  webSocketFactory.$inject = ['$websocket'];

  function webSocketFactory ($websocket) {

    var chatMessages = [];
    var notifications = [];

    var CHAT_MSG = 'chat-message';
    var FRIEND_MSG = 'friend-request';

    var ws = $websocket(getWebSocketUri("/socket"));

    ws.onMessage(function (msg) {
      var data = JSON.parse(msg.data);
      if (data.type === CHAT_MSG) {
        chatMessages.push(data);
      } else if (data.type === FRIEND_MSG) {
        notifications.unshift(data);
      }
    });

    return {
      Chat: {
        chatMessages: chatMessages,
        addMessage: function (msg) {
          var newMsg = $.extend({}, msg);
          newMsg.type = CHAT_MSG;
          chatMessages.push(newMsg);
        },
        send: function (msg) {
          msg.type = CHAT_MSG;
          ws.send(JSON.stringify(msg));
        }
      },

      Notification: {
        notifications: notifications,
        send: function (msg) {
          msg.type = FRIEND_MSG;
          ws.send(JSON.stringify(msg));
        }
      }
    };
  }
})();