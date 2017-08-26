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

        vm.selectedUser = ""; // selected user in the search box
        vm.angular = angular;
        vm.Notification = webSocketFactory.Notification;
        vm.statusCodes = STATUS_CODES;


        vm.getUserList = getUserList;


        function getUserList() {
          return userFactory.list().then(function(res) {
            var lowercaseExpected = vm.selectedUser;
            return res.data.filter(function(name) {
              return name.indexOf(lowercaseExpected) === 0 && name !== vm.username;
            });
          });
        }
      }],
    controllerAs: 'navBarCtrl'
  });