'use strict';

/** app level module which depends on services and controllers */
angular.module('socketChat', ['chat', 'navBar']);

angular
  .module('socketChat')
  .constant('CSRF_TOKEN', $('#csrf-token').text())
  .constant('USERNAME', $('#connected-user').text())
  .constant('STATUS_CODES', JSON.parse($('#friendship-status-codes').text()));