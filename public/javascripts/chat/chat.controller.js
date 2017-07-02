'use strict';

/** Controllers */

angular
  .module('chat')
  .controller('ChatCtrl', ['$scope', '$http', 'chatModel', 'Upload', '$timeout',
    function ($scope, $http, chatModel, Upload, $timeout) {

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
        var csrfToken = $("#csrfToken").attr("value");
        var req = {
          method: 'POST',
          url: '/chat',
          headers: {
            'Csrf-Token': csrfToken
          },
          data: {
            text: $scope.inputText,
            user: $scope.user,
            time: (new Date()).toUTCString(),
            room: $scope.currentRoom.value,
          }
        };
        $http(req);
        $scope.inputText = "";
      };

      $scope.displayFile = function(link, fileName) {
        var csrfToken = $("#csrfToken").attr("value");
        var req = {
          method: 'POST',
          url: '/chat',
          headers: {
            'Csrf-Token': csrfToken
          },
          data: {
            link: link,
            fileName: fileName,
            user: $scope.user,
            time: (new Date()).toUTCString(),
            room: $scope.currentRoom.value,
          }
        };
        $http(req);
        $scope.inputText = "";
      };

      /* upload file in chat */
      $scope.uploadFiles = function(file, errFiles) {
        var csrfToken = $("#csrfToken").attr("value");
        $scope.f = file;
        $scope.errFile = errFiles && errFiles[0];
        if (file) {
          file.upload = Upload.upload({
            url: '/upload',
            headers: {
              'Csrf-Token': csrfToken
            },
            data: {file: file}
          });

          file.upload.then(function (response) {
            $timeout(function () {
              file.result = response.data;
              $scope.displayFile("/file-upload/" + file.name, file.name)
            });
          }, function (response) {
            if (response.status > 0) {
              console.log("error");
              $scope.errorMsg = response.status + ': ' + response.data;
            }
          });
        }
      };

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
