/**
 * Created by thang on 7/9/17.
 */
angular
  .module('chat')
  .component('chatWindow', {
    templateUrl: '/assets/javascripts/chat/chat.template.html',
    controller: ['$scope', '$http', 'chatModel', 'Users',
      function ($scope, $http, chatModel, Users) {
        var ws = new WebSocket("ws://localhost:9000/chat/socket");

        $scope.rooms = chatModel.getRooms();
        $scope.currentRoom = $scope.rooms[0];
        $scope.msgs = [];
        $scope.inputText = "";
        $scope.user = $('#connected-user').text();
        $scope.csrfToken = $("#csrf-token").text();
        $scope.filePickerClient = filestack.init('AqRfNWvWJTgcoBKncr9gCz');

        angular.element(document).ready(function () {
          // Save the math input box to scope variable
          var MQElement = document.getElementById("mathquill");
          var MQ = MathQuill.getInterface(2);
          $scope.mathField = MQ.MathField(MQElement, {});
          setupMathInput($scope.mathField);
        });

        /** change current room, restart EventSource connection */
        $scope.setCurrentRoom = function (room) {
          console.log("here");
          $scope.currentRoom = room;
          $scope.msgs = [];
        };

        /** posting chat text */
        $scope.submitMsg = function () {
          $scope.msgs.push($scope.inputText);
          ws.send($scope.inputText);
          $scope.inputText = "";
        };

        ws.onmessage = function (msg) {
          $scope.msgs.push(msg.data);
          $scope.$digest();
        };

        /** posting math formula */
        $scope.submitMath = function() {
          var mathInput = $scope.mathField.latex();
          if (!mathInput || mathInput === '') {
            return;
          }
          var req = {
            method: 'POST',
            url: '/chat',
            headers: {
              'Csrf-Token': $scope.csrfToken
            },
            data: {
              text: "$$" + mathInput + "$$",
              user: $scope.user,
              time: (new Date()).toUTCString(),
              room: $scope.currentRoom.value
            }
          };
          $http(req);
          $scope.mathField.latex("");
        };

        /** handle file upload */
        $scope.showPicker = function() {
          $scope.filePickerClient.pick({}).then(function (result) {
            var files = result.filesUploaded;
            for (var i = 0 ; i < files.length ; i++) {
              $scope.displayFile(files[i].url, files[i].filename);
            }
          });
        };

        $scope.displayFile = function(link, fileName) {
          var req = {
            method: 'POST',
            url: '/chat',
            headers: {
              'Csrf-Token': $scope.csrfToken
            },
            data: {
              link: link,
              fileName: fileName,
              user: $scope.user,
              time: (new Date()).toUTCString(),
              room: $scope.currentRoom.value
            }
          };
          $http(req);
          $scope.inputText = "";
        };
      }]
  });
