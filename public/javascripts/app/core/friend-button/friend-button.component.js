/**
 * Created by thangle on 8/14/17.
 */
(function() {
  'use strict';

  angular
    .module('core')
    .component('friendButton', {
      templateUrl: '/assets/javascripts/app/core/friend-button/friend-button.template.html',
      bindings: {
        username: '<',
        otherUser: '<',
        csrfToken: '@'
      },
      controller: FriendButtonController
    });


  FriendButtonController.$inject = ['friendFactory', 'webSocketFactory', 'STATUS_CODES'];

  function FriendButtonController (friendFactory, webSocketFactory, STATUS_CODES) {
    var vm = this;

    vm.STATUS_CODES = STATUS_CODES;
    vm.angular = angular;

    vm.addFriend = addFriend;
    vm.acceptFriend = acceptFriend;
    vm.removeFriend = removeFriend;

    vm.$onInit = function() {
      vm.relationship = friendFactory.checkStatus(vm.username, vm.otherUser);
    };

    function sendRequest (action) {
      var actionToStatus = {
        'add': STATUS_CODES.PENDING,
        'accept': STATUS_CODES.ACCEPTED
      };
      var promise = friendFactory.sendRequest(action, vm.username, vm.otherUser, vm.csrfToken);
      promise.then(function() {
        if (action !== 'remove') {
          var json = { sender: vm.username, receiver: vm.otherUser , status:  actionToStatus[action] };
          webSocketFactory.Notification.send(json);
        }
        vm.relationship = friendFactory.checkStatus(vm.username, vm.otherUser);
      }, function(error) {
        // recheck the status
        vm.relationship = friendFactory.checkStatus(vm.username, vm.otherUser);
        console.log(error);
      });
    }

    function addFriend () {
      sendRequest('add');
    }

    function acceptFriend () {
      sendRequest('accept');
    }

    function removeFriend () {
      sendRequest('remove');
    }
  }
})();
