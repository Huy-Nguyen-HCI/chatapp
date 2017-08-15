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


  FriendButtonController.$inject = ['friendFactory', 'STATUS_CODES'];

  function FriendButtonController (friendFactory, STATUS_CODES) {
    var vm = this;

    vm.STATUS_CODES = STATUS_CODES;
    vm.angular = angular;

    vm.addFriend = addFriend;
    vm.acceptFriend = acceptFriend;
    vm.removeFriend = removeFriend;

    vm.$onInit = function() {
      vm.relationship = friendFactory.checkStatus(vm.username, vm.otherUser);
    };

    function addFriend () {
      friendFactory.addFriend(vm.username, vm.otherUser, vm.csrfToken);
    }

    function acceptFriend () {
      friendFactory.acceptFriend(vm.username, vm.otherUser, vm.csrfToken);
    }

    function removeFriend () {
      friendFactory.removeFriend(vm.username, vm.otherUser, vm.csrfToken);
    }
  }
})();
