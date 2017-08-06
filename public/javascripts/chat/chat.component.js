/**
 * Created by thang on 7/9/17.
 */
angular
  .module('chat')
  .component('chatWindow', {
    templateUrl: '/assets/javascripts/chat/chat.template.html',
    controller: ChatWindowController,
    controllerAs: 'chatCtrl'
  });

ChatWindowController.$inject = ['$scope', 'chatModel'];

function ChatWindowController($scope, chatModel) {
  // websocket for sending chat messages
  var vm = this;
  var ws = new WebSocket("ws://localhost:9000/chat/socket");

  vm.rooms = chatModel.getRooms();
  vm.currentRoom = vm.rooms[0];

  vm.msgs = [];
  vm.inputText = "";

  vm.user = $('#connected-user').text();
  vm.csrfToken = $("#csrf-token").text();
  vm.filePickerClient = filestack.init('AqRfNWvWJTgcoBKncr9gCz');

  angular.element(document).ready(function () {
    // Save the math input box to scope variable
    var MQElement = document.getElementById("mathquill");
    var MQ = MathQuill.getInterface(2);
    vm.mathField = MQ.MathField(MQElement, {});
    setupMathInput(vm.mathField);
  });

  /** change current room, restart EventSource connection */
  vm.setCurrentRoom = function (room) {
    vm.currentRoom = room;
    vm.msgs = [];
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
  vm.submitMsg = function () {
    var inputText = vm.inputText;
    if (!isInputValid(inputText)) return;

    var sendData = {text: inputText};
    vm.msgs.push(sendData);
    ws.send(JSON.stringify(sendData));

    vm.inputText = "";
  };

  ws.onmessage = function (msg) {
    vm.msgs.push(JSON.parse(msg.data));
    MathJax.Hub.Queue(["Typeset", MathJax.Hub]);
    $scope.$digest();
  };

  /** posting math formula */
  vm.submitMath = function() {
    var mathInput = vm.mathField.latex();

    // if input is empty or only contains space, ignore
    if (!isInputValid(mathInput)) return;

    mathInput = "$$" + mathInput + "$$";
    vm.msgs.push(mathInput);
    ws.send(mathInput);
    vm.mathField.latex("");
  };

  /** handle file upload */
  vm.showPicker = function() {
    vm.filePickerClient.pick({}).then(function (result) {
      var files = result.filesUploaded;
      for (var i = 0 ; i < files.length ; i++) {
        vm.displayFile(files[i].url, files[i].filename);
      }
      vm.$digest();
    });
  };

  vm.displayFile = function(link, fileName) {
    console.log("hello");
    var sendData = {fileName: fileName, link: link};
    vm.msgs.push(sendData);
    ws.send(JSON.stringify(sendData));
  };
}