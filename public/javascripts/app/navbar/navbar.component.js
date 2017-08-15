/**
 * Created by thang on 7/12/17.
 */
angular
  .module('navBar')
  .component('navBar', {
    templateUrl: '/assets/javascripts/app/navbar/navbar.template.html',
    bindings: {
      username: '@',
      csrfToken: '@'
    },
    controller: ['userFactory', 'friendFactory', 'STATUS_CODES',
      function (userFactory, friendFactory, STATUS_CODES) {
        var vm = this;

        vm.angular = angular;
        vm.Friend = friendFactory;
        vm.users = userFactory.users;
        vm.statusCodes = STATUS_CODES;
      }],
    controllerAs: 'navBarCtrl'
  });