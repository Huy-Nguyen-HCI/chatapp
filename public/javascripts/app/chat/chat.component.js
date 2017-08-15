(function() {
  'use strict';

  angular
    .module('chat')
    .component('chatWindow', {
      templateUrl: '/assets/javascripts/app/chat/chat.template.html',
      bindings: {
        username: '@'
      },
      controller: ChatWindowController,
      controllerAs: 'chatCtrl'
    });

  ChatWindowController.$inject = ['$scope', 'webSocketFactory', 'ChatModel'];

  function ChatWindowController($scope, webSocketFactory, ChatModel) {
    var vm = this;

    // list of chat rooms
    vm.rooms = ChatModel.getRooms();
    vm.currentRoom = vm.rooms[0];

    // chat message data communicated between the users
    vm.MessageData = webSocketFactory.Chat;
    vm.inputText = "";

    vm.filePickerClient = filestack.init('AqRfNWvWJTgcoBKncr9gCz');

    // scope methods
    vm.setCurrentRoom = setCurrentRoom;
    vm.submitMsg = submitMsg;

    // initialize MathJax
    angular.element(document).ready(function () {
      // Save the math input box to scope variable
      var MQElement = document.getElementById("mathquill");
      var MQ = MathQuill.getInterface(2);
      vm.mathField = MQ.MathField(MQElement, {});
      setupMathInput(vm.mathField);
    });

    /** re-render the page whenever any model is updated **/
    $scope.$watch(function(){
      MathJax.Hub.Queue(["Typeset",MathJax.Hub]);
      return true;
    });

    /** change current room, restart EventSource connection */
    function setCurrentRoom (room) {
      vm.currentRoom = room;
      vm.MessageData.chatMessages = [];
    }

    /** posting chat text */
    function submitMsg () {
      var inputText = vm.inputText;
      if (!isInputValid(inputText)) return;

      var sendData = {sender: vm.username, text: inputText};
      vm.MessageData.addMessage(sendData);
      vm.MessageData.send(sendData);

      vm.inputText = "";
    }

    // ws.onMessage = function (msg) {
    //   vm.chatMessages.push(JSON.parse(msg.data));
    //   MathJax.Hub.Queue(["Typeset", MathJax.Hub]);
    // };

    // /** posting math formula */
    // vm.submitMath = function() {
    //   var mathInput = vm.mathField.latex();
    //
    //   // if input is empty or only contains space, ignore
    //   if (!isInputValid(mathInput)) return;
    //
    //   mathInput = "$$" + mathInput + "$$";
    //   vm.chatMessages.push(mathInput);
    //   vm.mathField.latex("");
    // };
    //
    // /** handle file upload */
    // vm.showPicker = function() {
    //   vm.filePickerClient.pick({}).then(function (result) {
    //     var files = result.filesUploaded;
    //     for (var i = 0 ; i < files.length ; i++) {
    //       vm.displayFile(files[i].url, files[i].filename);
    //     }
    //     vm.$digest();
    //   });
    // };
    //
    // vm.displayFile = function(link, fileName) {
    //   console.log("hello");
    //   var sendData = {fileName: fileName, link: link};
    //   vm.chatMessages.push(sendData);
    // };

    /** check if a message is nonempty and does not contain only space **/
    function isInputValid(message) {
      return message && message.replace(/\s/g, '').length > 0;
    }
  }
})();