/**
 * Created by thang on 7/9/17.
 */
angular
  .module('chat')
  .component('chatWindow', {
    templateUrl: '/assets/javascripts/chat/chat.template.html',
    controller: ['$scope', '$http', 'chatModel', 'Users',
      function ($scope, $http, chatModel, Users) {

        $scope.rooms = chatModel.getRooms();
        $scope.currentRoom = $scope.rooms[0];
        $scope.msgs = [];
        $scope.inputText = "";
        $scope.user = "Jane Doe #" + Math.floor((Math.random() * 100) + 1);
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
          $scope.currentRoom = room;
          $scope.chatFeed.close();
          $scope.msgs = [];
          $scope.listen();
        };

        /** posting chat text */
        $scope.submitMsg = function () {
          var req = {
            method: 'POST',
            url: '/chat',
            headers: {
              'Csrf-Token': $scope.csrfToken
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