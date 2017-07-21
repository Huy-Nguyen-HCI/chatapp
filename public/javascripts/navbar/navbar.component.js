/**
 * Created by thang on 7/12/17.
 */
angular
  .module('navBar')
  .component('navBar', {
    templateUrl: '/assets/javascripts/navbar/navbar.template.html',
    controller: ['$scope', 'Users', 'Friendship', 'STATUSES', 'FriendSocket',
        function($scope, Users, Friendship, STATUSES, FriendSocket) {
      $scope.userList = Users.query();
      $scope.username = $('#connected-user').text();
      Friendship.listPendingRequests({username : $scope.username}).forEach(function(e) {
        FriendSocket.getRequest($scope.username, e);
      });

      var csrfToken = $('#csrf-token').text();

      // comparator function that checks whether the actual string starts with the
      // expected expression
      $scope.startsWith = function (actual, expected) {
        var lowercaseExpected = expected.toLowerCase();
        return (actual.indexOf(lowercaseExpected) === 0 && actual !== $scope.username);
      };

      $scope.addFriend = function (otherUser) {
        Friendship.save({action: 'add', csrfToken: csrfToken}, {'sender': $scope.username, 'receiver': otherUser});
        FriendSocket.getRequest($scope.username, otherUser);
        console.log(FriendSocket.requests);
      };
    }]
  });