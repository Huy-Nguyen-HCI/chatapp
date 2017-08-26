/**
 * Created by thangle on 8/12/17.
 */
(function() {
  'use strict';

  angular
    .module('core')
    .factory('userFactory', userFactory);

  userFactory.$inject = ['$http'];

  function userFactory($http) {

    /** List all users. */
    function list() {
      return $http.get('/api/users');
    }

    return {
      list: list
    }
  }
})();
