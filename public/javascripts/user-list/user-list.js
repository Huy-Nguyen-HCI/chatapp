/**
 * Created by thang on 7/4/17.
 */
var userListModule = angular.module('userList', ['ngResource']);


userListModule.service('Users', ['$resource', function($resource) {
  return $resource('/api/users/:name', {name: '@username'})
}]);