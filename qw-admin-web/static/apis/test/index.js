/**
 * 测试环境相关 API。
 */
define([
    'request',
    'axios'
], function(Request, axios) {
  // var $http = Request.$http;
  return {
    /**
     * 查询城市天气
     * @param 城市名称
     */
    queryWeather: function(cityName) {
      // 实际访问的是 /admin/rest/forget 接口
      // return $http.get('/rest/forget');
      // 这里使用一个查询天气的接口做演示
      return axios.get('http://wthrcdn.etouch.cn/weather_mini?city=' + cityName);
    }
  }
});