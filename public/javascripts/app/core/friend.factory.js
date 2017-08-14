/**
 * Created by thangle on 8/13/17.
 */
(function() {
  'use strict';

  angular
    .module('app.core')
    .factory('friendFactory', friendFactory);

  friendFactory.$inject = ['$resource', 'webSocketFactory', 'STATUS_CODES', 'CSRF_TOKEN', 'USERNAME'];

  function friendFactory($resource, webSocketFactory, STATUS_CODES, CSRF_TOKEN, USERNAME) {
    var Friend = $resource('/api/friend/:action');
    var Notification = webSocketFactory.Notification;

    function sendRequest(action, otherUser) {
      var actionToStatus = {
        'add': STATUS_CODES.PENDING,
        'accept': STATUS_CODES.ACCEPTED
      };
      Friend.save(
        { action: action, csrfToken: CSRF_TOKEN },
        { sender: USERNAME, receiver: otherUser },
        function () {
          if (action !== 'remove') {
            var json = { sender: USERNAME, receiver: otherUser , status: actionToStatus[action] };
            Notification.send(json);
          }
        },
        function (error) {
          console.log(error);
        }
      )
    }

    function addFriend(otherUser) {
      sendRequest('add', otherUser);
    }

    function acceptFriend(otherUser) {
      sendRequest('accept', otherUser);
    }

    function removeFriend(otherUser) {
      sendRequest('remove', otherUser);
    }

    function checkStatus (otherUser) {
      return Friend.get({action: 'check', first: USERNAME, second: otherUser});
    }

    return {
      notifications: Notification.notifications,
      addFriend: addFriend,
      acceptFriend: acceptFriend,
      removeFriend: removeFriend,
      checkStatus: checkStatus
    };
  }
})();