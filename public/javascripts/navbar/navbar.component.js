/**
 * Created by thang on 7/12/17.
 */
angular
  .module('navBar')
  .component('navBar', {
    templateUrl: '/assets/javascripts/navbar/navbar.template.html',
    controller: ['Users', 'friendRequestHandler', 'USERNAME' , 'STATUS_CODES',
      function (Users, friendRequestHandler, USERNAME, STATUS_CODES) {
        var vm = this;

        // constants and services
        vm.STATUS_CODES = STATUS_CODES;
        vm.friendRequestHandler = friendRequestHandler;
        vm.angular = angular;

        vm.userList = Users.query();
        vm.username = USERNAME;

        vm.startsWith = function (actual, expected) {
          var lowercaseExpected = expected.toLowerCase();
          return (actual.indexOf(lowercaseExpected) === 0 && actual !== USERNAME);
        };
      }],
    controllerAs: 'friendCtrl'
  });