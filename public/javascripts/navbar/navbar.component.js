/**
 * Created by thang on 7/12/17.
 */
angular
  .module('navBar')
  .component('navBar', {
    templateUrl: '/assets/javascripts/navbar/navbar.template.html',
    controller: ['$scope', 'Users', 'Friendship', function($scope, Users, Friendship) {
      $scope.userList = Users.query();
      $scope.username = $('#connected-user').text();
      $scope.pendingRequests = Friendship.listPending({username : $scope.username});

      var csrfToken = $('#csrf-token').text();

      // comparator function that checks whether the actual string starts with the
      // expected expression
      $scope.startsWith = function (actual, expected) {
        var lowercaseExpected = expected.toLowerCase();
        return (actual.indexOf(lowercaseExpected) === 0 && actual !== $scope.username);
      };

      $scope.addFriend = function (username) {
        Friendship.save({action: 'add', csrfToken: csrfToken}, {'sender': $scope.username, 'receiver': username})
      };

      $scope.isFriendWith = function (username) {

      };
    }]
  });