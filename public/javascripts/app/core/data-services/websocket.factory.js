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
    var CHAT_MSG = 'chat-message';
    var FRIEND_MSG = 'friend-request';

    // open web socket connection.
    var ws = $websocket(getWebSocketUri("/socket"));

    /** Service for sending chat messages. */
    var Chat = {
      chatMessages: {},

      addMessage: function (msg, roomId) {
        var copyMsg = $.extend({}, msg);

        if (!this.chatMessages.hasOwnProperty(roomId)) {
          this.chatMessages[roomId] = [];
        }
        this.chatMessages[roomId].push(copyMsg);
      },

      send: function (msg, roomId, receivers) {
        var copyMsg = $.extend({}, msg);
        var sendData = { type: CHAT_MSG, content: copyMsg, roomId: roomId, receivers: receivers };
        ws.send(JSON.stringify(sendData));
      }
    };

    /** Service for sending friend request notifications. */
    var Notification = {
      notifications: [],

      send: function (msg, receivers) {
        var copyMsg = $.extend({}, msg);
        var sendData = { type: FRIEND_MSG, content: copyMsg , receivers: receivers };
        ws.send(JSON.stringify(sendData));
      },

      addNotification: function (request) {
        this.notifications.unshift(request);
      }
    };

    // callback when receive messages from the socket.
    ws.onMessage(function (msg) {
      var data = JSON.parse(msg.data);
      if (data.type === CHAT_MSG) {
        Chat.addMessage(data.content, data.roomId);
      } else if (data.type === FRIEND_MSG) {
        Notification.addNotification(data.content);
      }
    });

    return {
      Chat: Chat,
      Notification: Notification
    };
  }
})();