/**
 * Created by thang on 7/4/17.
 */
var userListModule = angular.module('userList', ['ngResource']);


userListModule.service('Users', ['$resource', function($resource) {
  return $resource('/api/users/:name', {name: '@username'});
}]);


userListModule.constant('STATUSES', {PENDING: 0, ACCEPTED: 1, DECLINED: 2, BLOCKED: 3});


userListModule.service('Friendship', ['$resource', 'STATUSES', function($resource, STATUSES) {
  return $resource('/api/friend/:action', {}, {
    listPending: {method: 'GET', params: {action: 'search', status: STATUSES.PENDING}, isArray: true}
  });
}]);