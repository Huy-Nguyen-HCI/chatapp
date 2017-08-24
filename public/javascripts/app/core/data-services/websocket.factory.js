/**
 * Created by thangle on 8/13/17.
 */
(function() {
  'use strict';

  angular
    .module('core')
    .factory('webSocketFactory', webSocketFactory);

  webSocketFactory.$inject = ['$websocket'];

  function webSocketFactory ($websocket) {
    var chatMessages = {};
    var notifications = [];

    var CHAT_MSG = 'chat-message';
    var FRIEND_MSG = 'friend-request';

    var ws = $websocket(getWebSocketUri("/socket"));

    ws.onMessage(function (msg) {
      console.log(msg);
      var data = JSON.parse(msg.data);
      if (data.type === CHAT_MSG) {
        addChatMessage(data.content, data.roomId);
      } else if (data.type === FRIEND_MSG) {
        notifications.unshift(data.content);
      }
    });

    function addChatMessage (msg, roomId) {
      var copyMsg = $.extend({}, msg);

      if (!chatMessages.hasOwnProperty(roomId)) {
        chatMessages[roomId] = [];
      }
      chatMessages[roomId].push(copyMsg);
    }

    function sendChatMessage (msg, roomId, receivers) {
      var copyMsg = $.extend({}, msg);
      var sendData = { type: CHAT_MSG, content: copyMsg, roomId: roomId, receivers: receivers };
      ws.send(JSON.stringify(sendData));
    }

    return {
      Chat: {
        chatMessages: chatMessages,
        addMessage: addChatMessage,
        send: sendChatMessage
      },

      Notification: {
        notifications: notifications,
        send: function (msg, receivers) {
          var copyMsg = $.extend({}, msg);
          var sendData = { type: FRIEND_MSG, content: copyMsg , receivers: receivers };
          ws.send(JSON.stringify(sendData));
        }
      }
    };
  }
})();