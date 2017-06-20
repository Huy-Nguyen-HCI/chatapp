'use strict';

/** Controllers */

angular
  .module('sseChat.controllers', ['sseChat.services'])
  .controller('ChatCtrl', function ($scope, $http, chatModel) {

  angular.element(document).ready(function () {
    $('.chat[data-chat=room1]').addClass('active-chat');
    $('.person[data-chat=room1]').addClass('active');
    $('.left .person').mousedown(function(){
      if ($(this).hasClass('.active')) {
        return false;
      } else {
        var findChat = $(this).attr('data-chat');
        var personName = $(this).find('.name').text();
        $('.right .top .name').html(personName);
        $('.chat').removeClass('active-chat');
        $('.left .person').removeClass('active');
        $(this).addClass('active');
        $('.chat[data-chat = '+findChat+']').addClass('active-chat');
      }
    });
  });


  $scope.rooms = chatModel.getRooms();
  $scope.msgs = [];
  $scope.inputText = "";
  $scope.user = "Jane Doe #" + Math.floor((Math.random() * 100) + 1);
  $scope.currentRoom = $scope.rooms[0];

  /** change current room, restart EventSource connection */
  $scope.setCurrentRoom = function (room) {
    $scope.currentRoom = room;
    $scope.chatFeed.close();
    $scope.msgs = [];
    $scope.listen();
  };

  /** posting chat text to server */
  $scope.submitMsg = function () {
    var csrfValue = $("#csrfToken").attr("value");

    var req = {
      method: 'POST',
      url: '/chat',
      headers: {
        'Csrf-Token': csrfValue
      },
      data: {
        text: $scope.inputText,
        user: $scope.user,
        time: (new Date()).toUTCString(),
        room: $scope.currentRoom.value,
      }
    }
    $http(req);
    $scope.inputText = "";
  };

  /** handle incoming messages: add to messages array */
  $scope.addMsg = function (msg) {
    $scope.$apply(function () { $scope.msgs.push(JSON.parse(msg.data)); });
  };

  /** start listening on messages from selected room */
  $scope.listen = function () {
    $scope.chatFeed = new EventSource("/chatFeed/" + $scope.currentRoom.value);
    $scope.chatFeed.addEventListener("message", $scope.addMsg, false);
  };

  $scope.listen();
});
