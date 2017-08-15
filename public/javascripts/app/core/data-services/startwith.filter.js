/**
 * Created by thangle on 8/13/17.
 */
angular
  .module('core')
  .filter('startWith', function() {
    return function (input, expected, exclude) {
      if (exclude === undefined)
        exclude = '';
      var lowercaseExpected = expected.toLowerCase();
      return input.filter(function(str) {
        return str.indexOf(lowercaseExpected) === 0 && str !== exclude;
      });
    };
  });