var exec = require('cordova/exec');
var myFun = function(){};
myFun.getCurrentPosition = function(arg0, successCallback, errorCallback) {
    exec(successCallback, errorCallback, "NativeGeolocation", "getCurrentPosition", [arg0]);
    // 成功返回 错误返回 插件类名 插件类execute的action名 传参
};
myFun.stop = function (arg0, successCallback, errorCallback) {
  exec(successCallback, errorCallback, "NativeGeolocation", "stop", [arg0]);
}
module.exports = myFun;
