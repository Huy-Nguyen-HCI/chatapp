/**
 * Created by thangle on 8/13/17.
 */
(function() {
  'use strict';

  angular
    .module('core')
    .factory('friendFactory', friendFactory);

  friendFactory.$inject = ['$resource'];

  function friendFactory($resource) {
    var Friend = $resource('/api/friend/:action');

    function sendRequest(action, username, otherUser, csrfToken) {
      return Friend.save(
        { action: action, csrfToken: csrfToken },
        { sender: username, receiver: otherUser }
      ).$promise;
    }

    function checkStatus (username, otherUser) {
      return Friend.get({action: 'check', first: username, second: otherUser});
    }

    return {
      sendRequest: sendRequest,
      checkStatus: checkStatus
    };
  }
})();