/**
 * Created by thangle on 8/12/17.
 */
angular.module('core', ['ngWebSocket', 'ngResource']);

angular
  .module('core')
  .constant('STATUS_CODES', JSON.parse($('#friendship-status-codes').text()));