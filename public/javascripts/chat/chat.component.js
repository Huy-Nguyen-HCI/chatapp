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
          $scope.currentRoom = room;
          $scope.msgs = [];
        };

        /** re-render the page whenever any model is updated **/
        $scope.$watch(function(){
          MathJax.Hub.Queue(["Typeset",MathJax.Hub]);
          return true;
        });

        /** check if a message is nonempty and does not contain only space **/
        function isInputValid(message) {
          return message && message.replace(/\s/g, '').length > 0;
        }

        /** posting chat text */
        $scope.submitMsg = function () {
          var inputText = $scope.inputText;
          if (!isInputValid(inputText)) return;

          var sendData = {text: inputText}
          $scope.msgs.push(inputText);
          ws.send(JSON.stringify(sendData));

          $scope.inputText = "";
        };

        ws.onmessage = function (msg) {
          console.log(JSON.stringify(msg.data));
          $scope.msgs.push(JSON.parse(msg.data).text);
          MathJax.Hub.Queue(["Typeset", MathJax.Hub]);
          $scope.$digest();
        };

        /** posting math formula */
        $scope.submitMath = function() {
          var mathInput = $scope.mathField.latex();

          // if input is empty or only contains space, ignore
          if (!isInputValid(mathInput)) return;

          mathInput = "$$" + mathInput + "$$";
          $scope.msgs.push(mathInput);
          ws.send(mathInput);
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
          var sendData = {text: fileName, link: link};
          $scope.msgs.push(sendData);
          ws.send(sendData);
        };
      }]
  });
