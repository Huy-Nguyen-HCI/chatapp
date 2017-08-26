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

  ChatWindowController.$inject = ['$scope', 'webSocketFactory', 'chatRoomFactory', 'userFactory'];

  function ChatWindowController($scope, webSocketFactory, chatRoomFactory, userFactory) {
    var vm = this;

    vm.addParticipantBtnClicked = false;
    vm.selectedParticipant = "";

    // chat message data communicated between the users
    vm.inputText = "";
    vm.Chat = webSocketFactory.Chat;

    // list of chat rooms
    vm.rooms = chatRoomFactory.listAccessibleRooms(function() {
      vm.currentRoom = vm.rooms[0];
    });

    vm.filePickerClient = filestack.init('AqRfNWvWJTgcoBKncr9gCz');

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
      chatRoomFactory.save(
        { action: 'add', csrfToken: vm.csrfToken, room: vm.currentRoom, username: participantName}, {},
        function(data) {

        }
      );
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

    /** posting chat text */
    function submitMsg () {
      var inputText = vm.inputText;
      if (!isInputValid(inputText)) return;

      chatRoomFactory.listParticipants({ room: vm.currentRoom }, function(participants) {
        // do not send the message back to the sender
        var receivers = participants.filter(function(user) { return user !== vm.username });
        var content = { sender: vm.username, text: inputText };

        // add the message to the room's message list, then send it to all the other participants
        vm.Chat.addMessage(content, vm.currentRoom);
        vm.Chat.send(content, vm.currentRoom, receivers);

        vm.inputText = "";
      });
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


    /** handle file upload */
    function showPicker () {
      vm.filePickerClient.pick({}).then(function (result) {
        var files = result.filesUploaded;
        for (var i = 0 ; i < files.length ; i++) {
          vm.displayFile(files[i].url, files[i].filename);
        }
      });
    }

    function displayFile (link, fileName) {
      var sendData = {fileName: fileName, link: link};
      vm.Chat.addMessage(sendData, vm.currentRoom);
      vm.Chat.send(sendData, vm.currentRoom);
      $scope.$digest();
    }

    /** check if a message is nonempty and does not contain only space **/
    function isInputValid(message) {
      return message && message.replace(/\s/g, '').length > 0;
    }
  }
})();