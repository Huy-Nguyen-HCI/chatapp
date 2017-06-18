'use strict';

/** Controllers */
angular.module('sseChat.controllers', ['sseChat.services', 'ngCookies']).
    controller('ChatCtrl', function ($scope, $http, $cookies, chatModel) {
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

        /** handle incoming messages: add to messages array */
        $scope.addMsg = function (msg) {
            $scope.$apply(function () { $scope.msgs.push(JSON.parse(msg.data)); });
        };

        /** start listening on messages from selected room */
        $scope.listen = function () {
            $scope.chatFeed = new EventSource("/chatFeed/" + $scope.currentRoom.value);
            $scope.chatFeed.addEventListener("message", $scope.addMsg, false);
        };

        $scope.listen();
    });
