/**
 * Created by thangle on 8/13/17.
 */
(function() {
  'use strict';

  angular
    .module('core')
    .factory('friendFactory', friendFactory);

  friendFactory.$inject = ['$resource', 'webSocketFactory', 'STATUS_CODES'];

  function friendFactory($resource, webSocketFactory, STATUS_CODES) {
    var Friend = $resource('/api/friend/:action');
    var Notification = webSocketFactory.Notification;

    function sendRequest(action, username, otherUser, csrfToken) {
      var actionToStatus = {
        'add': STATUS_CODES.PENDING,
        'accept': STATUS_CODES.ACCEPTED
      };
      Friend.save(
        { action: action, csrfToken: csrfToken },
        { sender: username, receiver: otherUser },
        function () {
          if (action !== 'remove') {
            var json = { sender: username, receiver: otherUser , status: actionToStatus[action] };
            Notification.send(json);
          }
        },
        function (error) {
          console.log(error);
        }
      )
    }

    function addFriend(username, otherUser, csrfToken) {
      sendRequest('add', username, otherUser, csrfToken);
    }

    function acceptFriend(username, otherUser, csrfToken) {
      sendRequest('accept', username, otherUser, csrfToken);
    }

    function removeFriend(username, otherUser, csrfToken) {
      sendRequest('remove', username, otherUser, csrfToken);
    }

    function checkStatus (username, otherUser) {
      return Friend.get({action: 'check', first: username, second: otherUser});
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