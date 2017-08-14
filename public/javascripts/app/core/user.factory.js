/**
 * Created by thangle on 8/12/17.
 */
(function() {
  'use strict';

  angular
    .module('app.core')
    .factory('userFactory', userFactory);


  function userFactory($resource) {
    var User = $resource('/api/users/:name');

    var users = User.query();

    return {
      users: users
    }
  }
})();
