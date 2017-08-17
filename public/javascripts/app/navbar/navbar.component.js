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
    controller: ['userFactory', 'webSocketFactory', 'STATUS_CODES',
      function (userFactory, webSocketFactory, STATUS_CODES) {
        var vm = this;

        vm.angular = angular;
        vm.Notification = webSocketFactory.Notification;
        vm.users = userFactory.users;
        vm.statusCodes = STATUS_CODES;
      }],
    controllerAs: 'navBarCtrl'
  });