(function() {
  'use strict';

  angular
    .module('chat')
    .factory('chatRoomFactory', chatRoomFactory);

  chatRoomFactory.$inject = ['$resource'];

  function chatRoomFactory($resource) {
    return $resource('/api/chatroom/:action', {}, {
        listAccessibleRooms: { method: 'GET', params: { action: 'list' }, isArray: true },
        listParticipants: { method: 'GET', params: { action: 'participants' }, isArray: true }
      }
    );
  }
})();

