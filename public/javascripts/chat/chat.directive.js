/**
 * Created by thang on 7/9/17.
 */
angular
  .module('chat')
  .directive('setupChat', ['$timeout', function($timeout) {
    return {
      restrict: 'E',
      link: function() {
        $timeout(function() {
          // Initialize the UI
          init();
        });
      }
    }
  }]);

