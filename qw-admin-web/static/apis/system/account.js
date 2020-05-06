/**
 * 账户管理相关 API
 */
define([
    'request',
    'axios'
], function(Request, axios) {
    var $http = Request.$http;
    return {
        /**
         * 修改用户信息
         * @param {*} form.username
         * @param {*} form.nickname
         * @param {*} form.gender
         * @param {*} form.phone
         * @param {*} form.email
         */
        modifyBaseInfo: function(form) {
            return $http.post('/my/personal/info/rest/modify', form);
        },
        /**
         * 根据 userId 查询用户角色信息
         * @param {*} form.srcPassword
         * @param {*} form.newPassword
         */
        modifyPassword: function(form) {
            return $http.post('/my/personal/info/rest/modify/password', form);
        },
        /**
         * 上传图片
         * @param { Blob } form.file
         */
        uploadPicture: function(form, onUploadProgressCallback) {
            var rquestConfig = {
                //添加请求头 
                headers: {
                    'Content-Type': 'multipart/form-data',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                //添加上传进度监听事件 
                onUploadProgress: function(e) {
                  onUploadProgressCallback && onUploadProgressCallback((e.loaded / e.total * 100) || 0);
                }
            };
            return axios.post('/imgs/rest/upload', form, rquestConfig)
                .then(Request.serverResponseDataHandler);
        },
        /**
         * 修改用户头像
         * @param { String } headerName 
         */
        modifyHeader: function(headerName) {
            return $http.post('/my/personal/info/rest/modify/header', { header: headerName });
        }
    }
});