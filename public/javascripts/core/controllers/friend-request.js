/**
 * Created by thangle on 8/5/17.
 */

FriendRequestController.$inject = ['$scope', 'Users', 'Friendship', 'statusCodes'];

/** A common controller for communicating friend requests */
function FriendRequestController ($scope, Users, Friendship, statusCodes) {
  var vm = this;
  var csrfToken = $('#csrf-token').text();

  vm.userList = Users.query();
  vm.username = $('#connected-user').text();

  vm.notifications = [];

  /**
   * Containing the status codes for pending, accepted, declined and blocked.
   * The status codes are to be retrieved from the server.
   *
   * @type {{PENDING: number, BLOCKED: number, DECLINED: number, ACCEPTED: number}}
   */
  vm.STATUSES = {};

  // retrieve the status codes from the server and initialize notifications
  statusCodes.then(
    function (response) {
      vm.STATUSES = response.data;

      // after having the status codes, get the list of pending requests
      var pendingRequests = Friendship.query(
        {action: 'search', username: vm.username, status: vm.STATUSES.PENDING},
        function () {
          pendingRequests.forEach(function (user) {
            vm.notifications.push({sender: user, status: vm.STATUSES.PENDING});
          });
        }
      );
    });

  /**
   * Websocket for friendship request notification.
   *
   * @type {WebSocket}
   */
  var ws = new WebSocket(getWebSocketUri("/noti/socket"));

  ws.onmessage = function (msg) {
    console.log(msg);
    var data = JSON.parse(msg.data);
    vm.notifications.unshift({sender: data.sender, status: data.status});
    $scope.$digest();
  };

  // functions for handling friend requests
  function sendRequest(action, otherUser) {
    var actionToStatus = {
      'add': vm.STATUSES.PENDING,
      'accept': vm.STATUSES.ACCEPTED
    };
    Friendship.save(
      {action: action, csrfToken: csrfToken},
      {sender: vm.username, receiver: otherUser},
      function () {
        if (action !== 'remove') {
          ws.send(JSON.stringify({sender: vm.username, receiver: otherUser, status: actionToStatus[action]}));
        }
      },
      function (error) {
        console.log(error);
      }
    )
  }

  vm.addFriend = function (otherUser) {
    sendRequest('add', otherUser);
  };

  vm.acceptFriend = function (otherUser) {
    sendRequest('accept', otherUser);
  };

  vm.removeFriend = function (otherUser) {
    sendRequest('remove', otherUser);
  };

  vm.checkStatus = function (otherUser) {
    return Friendship.get({action: 'check', first: vm.username, second: otherUser});
  };

  // comparator function that checks whether the actual string starts with the
  // expected expression
  vm.startsWith = function (actual, expected) {
    var lowercaseExpected = expected.toLowerCase();
    return (actual.indexOf(lowercaseExpected) === 0 && actual !== vm.username);
  };
}