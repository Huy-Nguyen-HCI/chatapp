/**
 * Created by thang on 7/12/17.
 */
angular
  .module('navBar')
  .component('navBar', {
    templateUrl: '/assets/javascripts/navbar/navbar.template.html',
    controller: ['$scope', 'Users', 'Friendship','STATUSES', function($scope, Users, Friendship, STATUSES) {

      var csrfToken = $('#csrf-token').text();

      $scope.userList = Users.query();
      $scope.username = $('#connected-user').text();

      $scope.notifications = [];
      $scope.STATUSES = STATUSES;
      var pendingRequests = Friendship.listPending({ username : $scope.username }, function() {
        pendingRequests.forEach(function(user) {
          $scope.notifications.push({sender: user, status: STATUSES.PENDING});
        });
      });

      // websocket for real-time notifications
      var ws = new WebSocket("ws://localhost:9000/noti/socket");
      ws.onmessage = function(msg) {
        console.log(msg);
        var data = JSON.parse(msg.data);
        $scope.notifications.unshift({ sender: data.sender, status: data.status });
        $scope.$digest();
      };

      // functions for handling friend requests
      function sendRequest (action, otherUser) {
        var actionToStatus = {
          'add': STATUSES.PENDING,
          'accept': STATUSES.ACCEPTED
        };
        Friendship.save(
          { action: action, csrfToken: csrfToken },
          { sender: $scope.username, receiver: otherUser },
          function() {
            ws.send(JSON.stringify({ sender: $scope.username, receiver: otherUser, status: actionToStatus[action] }));
          },
          function(error) {
            console.log(error);
          }
        )
      }

      $scope.addFriend = function (otherUser) {
        sendRequest('add', otherUser);
      };

      $scope.acceptFriend = function (otherUser) {
        sendRequest('accept', otherUser);
      };

      // comparator function that checks whether the actual string starts with the
      // expected expression
      $scope.startsWith = function (actual, expected) {
        var lowercaseExpected = expected.toLowerCase();
        return (actual.indexOf(lowercaseExpected) === 0 && actual !== $scope.username);
      };
    }]
  });