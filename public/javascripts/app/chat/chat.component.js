(function() {
  'use strict';

  angular
    .module('chat')
    .component('chatWindow', {
      templateUrl: '/assets/javascripts/app/chat/chat.template.html',
      bindings: {
        username: '@',
        csrfToken: '@'
      },
      controller: ChatWindowController,
      controllerAs: 'chatCtrl'
    });

  ChatWindowController.$inject = ['webSocketFactory', 'chatRoomFactory', 'userFactory'];

  function ChatWindowController(webSocketFactory, chatRoomFactory, userFactory) {
    var vm = this;
    vm.angular = angular;
    vm.filePickerClient = filestack.init('AqRfNWvWJTgcoBKncr9gCz');

    vm.selectedParticipant = "";

    // chat message data communicated between the users
    vm.inputText = "";
    vm.Chat = webSocketFactory.Chat;

    // list of chat rooms
    vm.rooms = chatRoomFactory.listAccessibleRooms(function() {
      vm.currentRoom = vm.rooms[0];
    });

    // scope methods
    vm.setCurrentRoom = setCurrentRoom;
    vm.createRoom = createRoom;
    vm.addParticipant = addParticipant;
    vm.getUserList = getUserList;

    vm.submitMsg = submitMsg;
    vm.submitMath = submitMath;

    vm.showPicker = showPicker;
    vm.displayFile = displayFile;

    // initialize MathJax
    angular.element(document).ready(function () {
      // Save the math input box to scope variable
      var MQElement = document.getElementById("mathquill");
      var MQ = MathQuill.getInterface(2);
      vm.mathField = MQ.MathField(MQElement, {});
      setupMathInput(vm.mathField);
    });


    // /** re-render the page whenever any model is updated **/
    // $scope.$watch(function(){
    //   MathJax.Hub.Queue(["Typeset",MathJax.Hub]);
    //   return true;
    // });

    /** change current room. */
    function setCurrentRoom (room) {
      vm.currentRoom = room;
    }

    /** create a new room. */
    function createRoom() {
      chatRoomFactory.save({ action: 'create', csrfToken: vm.csrfToken }, {}, function(data) {
        vm.rooms.push(data.roomId);
      });
    }

    function addParticipant (participantName) {
      chatRoomFactory.save({ action: 'add', room: vm.currentRoom, username: participantName,
          csrfToken: vm.csrfToken }, {});
    }

    function getUserList () {
      return userFactory.list().then(function(res) {
        var users = [];
        var lowercaseExpected = vm.selectedParticipant.toLowerCase();
        angular.forEach(res.data, function(name) {
            if (name.indexOf(lowercaseExpected) === 0 && name !== vm.username)
              users.push(name);
        });
        return users;
      });
    }


    /** Send the message to all participants in the current room. */
    function sendMsgToAllParticipants (msg) {
      chatRoomFactory.listParticipants({ room: vm.currentRoom }, function(participants) {
        // do not send the message back to the sender
        var receivers = participants.filter(function(user) { return user !== vm.username });

        // add the message to the room's message list, then send it to all the other participants
        vm.Chat.addMessage(msg, vm.currentRoom);
        vm.Chat.send(msg, vm.currentRoom, receivers);
      });
    }


    /** posting chat text */
    function submitMsg () {
      if (isInputValid(vm.inputText)) {
        var msg = { sender: vm.username, text: vm.inputText };
        sendMsgToAllParticipants(msg);
      }
      vm.inputText = "";
    }


    /** handle file upload */
    function showPicker () {
      vm.filePickerClient.pick({}).then(function (result) {
        var files = result.filesUploaded;
        for (var i = 0 ; i < files.length ; i++) {
          vm.displayFile(files[i].url, files[i].filename);
        }
      });
    }

    /** Display file in chat message. */
    function displayFile (link, fileName) {
      var msg = { sender: vm.username, fileName: fileName, link: link };
      sendMsgToAllParticipants(msg);
    }


    /** posting math formula */
    function submitMath() {
      var mathInput = vm.mathField.latex();

      // if input is empty or only contains space, ignore
      if (!isInputValid(mathInput)) return;

      mathInput = "$$" + mathInput + "$$";
      var sendData = {sender: vm.username, text: mathInput};
      vm.Chat.addMessage(sendData, vm.currentRoom);
      vm.Chat.send(sendData, vm.currentRoom);
      vm.mathField.latex("");
    }


    /** check if a message is nonempty and does not contain only space **/
    function isInputValid(message) {
      return message && message.replace(/\s/g, '').length > 0;
    }
  }
})();