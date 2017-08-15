/**
 * Created by thangle on 8/13/17.
 */
function HashSet(array) {
  // check parameters
  if (array === undefined)
    array = [];

  // initialize class variables
  var self = this;
  self.mySet = {};

  array.forEach(function(e) {
    self.mySet[e] = true;
  });

  // class methods
  self.values = function() {
    return Object.keys(self.mySet);
  };

  self.add = function(value) {
    self.mySet[value] = true;
  };

  self.remove = function(value) {
    if (self.contains(value)) {
      delete self.mySet[value];
    }
  };

  self.contains = function(value) {
    return self.mySet.hasOwnProperty(value);
  };
}