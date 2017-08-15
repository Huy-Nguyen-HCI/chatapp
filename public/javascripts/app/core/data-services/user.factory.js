/**
 * Created by thangle on 8/12/17.
 */
(function() {
  'use strict';

  angular
    .module('core')
    .factory('userFactory', userFactory);


  function userFactory($resource) {
    var User = $resource('/api/users/:name', null, {
      query: {
        method: 'GET',
        isArray: false,
        transformResponse: function (data) {
          var dataArray = JSON.parse(data);
          return new HashSet(dataArray);
        }
      }
    });

    var users = User.query();

    return {
      users: users
    }
  }
})();
