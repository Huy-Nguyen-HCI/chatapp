/**
 * Created by thang on 7/12/17.
 */
angular
  .module('navBar')
  .component('navBar', {
    templateUrl: '/assets/javascripts/app/navbar/navbar.template.html',
    controller: ['userFactory', 'friendFactory', 'USERNAME' , 'STATUS_CODES',
      function (userFactory, friendFactory, USERNAME, STATUS_CODES) {
        var vm = this;

        // constants and services
        vm.STATUS_CODES = STATUS_CODES;
        vm.Friend = friendFactory;

        vm.angular = angular;

        vm.userList = userFactory.users;
        vm.username = USERNAME;

        vm.startsWith = function (actual, expected) {
          var lowercaseExpected = expected.toLowerCase();
          return (actual.indexOf(lowercaseExpected) === 0 && actual !== USERNAME);
        };
      }],
    controllerAs: 'navBarCtrl'
  });