'use strict';

/** Controllers */

angular
  .module('sseChat.controllers', ['sseChat.services'])
  .controller('ChatCtrl', ['$scope', '$http', 'chatModel', function ($scope, $http, chatModel) {

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

    // tell MathJax to recognize inline math by $ $
    MathJax.Hub.Config({
        tex2jax: {inlineMath: [["$","$"]]}
    });

    $('#fileInput').onchange = function() {
        console.log("changed");
        $(this).closest('form').submit();
    }
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

//  /** upload file to server **/
//
//  $scope.fileChanged = function(ele) {
//    var files = ele.files;
//    var csrfValue = $("#csrfToken").attr("value");
//    var file = $scope.fileInput.get(0).files[0];
//    var formData = new FormData();
//    formData.append('file', file);
//
//    var req = {
//      method: 'POST',
//      url: '/upload',
//      headers: {
//        'Csrf-Token' : csrfValue
//      },
//      data: formData
//    }
//
//    $http(req).then(
//      function successCallback(response) {
//        // this callback will be called asynchronously
//        // when the response is available
//        console.log("success", response);
//      },
//      function errorCallback(response) {
//        // called asynchronously if an error occurs
//        // or server returns response with an error status.
//        console.log("error", response);
//      }
//    );

  /** handle incoming messages: add to messages array */
  $scope.addMsg = function (msg) {
    $scope.$apply(function () { $scope.msgs.push(JSON.parse(msg.data)); });
    MathJax.Hub.Queue(["Typeset", MathJax.Hub]);
  };

  /** start listening on messages from selected room */
  $scope.listen = function () {
    $scope.chatFeed = new EventSource("/chatFeed/" + $scope.currentRoom.value);
    $scope.chatFeed.addEventListener("message", $scope.addMsg, false);
  };

  $scope.listen();
}]);
