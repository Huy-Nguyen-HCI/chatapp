'use strict';

/** chatModel service, provides chat rooms (could as well be loaded from server) */
var chatModule = angular.module('chat');


chatModule.service('ChatModel', function () {
  var getRooms = function () {
    return [ {name: 'Room 1', value: 'room1'}, {name: 'Room 2', value: 'room2'},
      {name: 'Room 3', value: 'room3'}, {name: 'Room 4', value: 'room4'},
      {name: 'Room 5', value: 'room5'} ];
  };
  return { getRooms: getRooms };
});


chatModule.service('MessageData', ['$websocket', function($websocket) {
  var ws = $websocket(getWebSocketUri("/chat/socket"));

  var msgs = [];

  ws.onMessage(function(message) {
    msgs.push(JSON.parse(message.data));
  });

  return  {
    msgs: msgs,
    send: function(json) {
      ws.send(JSON.stringify(json));
    }
  };
}]);