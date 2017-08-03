/**
 * Created by thang on 7/4/17.
 */
var userListModule = angular.module('userList', ['ngResource']);


userListModule.service('Users', ['$resource', function($resource) {
  return $resource('/api/users/:name', {name: '@username'});
}]);

userListModule.service('Friendship', ['$resource', function($resource) {
  return $resource('/api/friend/:action');
}]);

userListModule.service('statusCodes', ['$http', function ($http) {
  return $http.get('/assets/resources/friendship-status.json');
}]);


