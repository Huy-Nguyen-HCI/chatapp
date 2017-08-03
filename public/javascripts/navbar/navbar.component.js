/**
 * Created by thang on 7/12/17.
 */
angular
  .module('navBar')
  .component('navBar', {
    templateUrl: '/assets/javascripts/navbar/navbar.template.html',
    controller: ['$scope', 'Users', 'Friendship','statusCodes',
      function($scope, Users, Friendship, statusCodes) {

      var csrfToken = $('#csrf-token').text();

      $scope.userList = Users.query();
      $scope.username = $('#connected-user').text();

      $scope.notifications = [];

      /**
       * Containing the status codes for pending, accepted, declined and blocked.
       * The status codes are to be retrieved from the server.
       *
       * @type {{PENDING: number, BLOCKED: number, DECLINED: number, ACCEPTED: number}}
       */
      $scope.STATUSES = {};

      // retrieve the status codes from the server and initialize notifications
      statusCodes.then(
        function (response) {
          $scope.STATUSES = response.data;

          // after having the status codes, get the list of pending requests
          var pendingRequests = Friendship.query(
            { action: 'search', username: $scope.username, status: $scope.STATUSES.PENDING},
            function() {
              pendingRequests.forEach(function(user) {
                $scope.notifications.push({sender: user, status: $scope.STATUSES.PENDING});
              });
            }
          );
      });

      /**
       * Websocket for friendship request notification.
       *
       * @type {WebSocket}
       */
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
          'add': $scope.STATUSES.PENDING,
          'accept': $scope.STATUSES.ACCEPTED
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