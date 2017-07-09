/**
 * Created by thang on 7/9/17.
 */
angular
  .module('chat')
  .component('chatWindow', {
    templateUrl: '/assets/javascripts/chat/chat.template.html',
    controller: ['$scope', '$http', 'chatModel', 'Upload', '$timeout', 'Users',
      function ($scope, $http, chatModel, Upload, $timeout, Users) {

        $scope.rooms = chatModel.getRooms();
        $scope.currentRoom = $scope.rooms[0];
        $scope.msgs = [];
        $scope.inputText = "";
        $scope.user = "Jane Doe #" + Math.floor((Math.random() * 100) + 1);
        $scope.userList = Users.query();

        // comparator function that checks whether the actual string starts with the
        // expected expression
        $scope.startsWith = function (actual, expected) {
          var lowercaseExpected = expected.toLowerCase();
          return (actual.indexOf(lowercaseExpected) === 0);
        };

        /** change current room, restart EventSource connection */
        $scope.setCurrentRoom = function (room) {
          $scope.currentRoom = room;
          $scope.chatFeed.close();
          $scope.msgs = [];
          $scope.listen();
        };

        /** posting chat text to server */
        $scope.submitMsg = function () {
          var csrfToken = $("#csrf-token").text();

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
              room: $scope.currentRoom.value
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
      }]
  });
