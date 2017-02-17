var exec = require('cordova/exec');
var myFun = function(){};
exports.coolMethod = function(arg0, success, error) {
    exec(success, error, "NativeGeolocation", "coolMethod", [arg0]);
};
