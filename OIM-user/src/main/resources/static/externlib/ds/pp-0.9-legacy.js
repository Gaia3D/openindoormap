'use strict';

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

/**
 * ui와 관련없는 것들
 * es6 버전
 * jquery사용하지 않음
 * 원본 파일 : pp-version-es6.js
 * 변환 파일 : pp-version-legacy.js
 * 변환툴 : babel
 * es5로 컴파일하는 방법 : @see https://stackoverflow.com/questions/34747693/how-do-i-get-babel-6-to-compile-to-es5-javascript
 * 주의! 직접 ppui-version-lagacy.js파일 변경 불허
 * @since
 *  2020-07-xx 바닐라js
 *  2020-07-16  pp와 ppui 분리
 * @author gravity
 */

/**
  * 파일 확장자 enum
  */
var Exts = Object.freeze({
    TEXT: { ext: '.txt' },
    IMAGE: { ext: '.bmp .gif .png .jpg .jpeg' },
    OFFICE: { ext: '.doc .docx .xls .xlsx .ppt .pptx .hwp .hwpx' },
    ZIP: { ext: '.zip .alz .7z' }
});

/**
 * performance platform util js
 */

var Pp = function () {
    function Pp() {
        _classCallCheck(this, Pp);
    }

    _createClass(Pp, null, [{
        key: 'base64ToBlob',


        /**
         * base64 문자열을 Blob로 변환하기
         * @see https://stackoverflow.com/questions/16245767/creating-a-blob-from-a-base64-string-in-javascript
         * @param {string} base64 
         * @param {string} contentType 
         * @returns {Blob}
         */
        value: function base64ToBlob(base64) {
            var contentType = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 'image/png';

            var byteCharacters = window.atob(base64);
            var byteArrays = [];
            var sliceSize = 512;

            //
            for (var offset = 0; offset < byteCharacters.length; offset += sliceSize) {
                var slice = byteCharacters.slice(offset, offset + sliceSize);

                //
                var byteNumbers = new Array(slice.length);
                for (var i = 0; i < slice.length; i++) {
                    byteNumbers[i] = slice.charCodeAt(i);
                }

                //
                var byteArray = new Uint8Array(byteNumbers);
                byteArrays.push(byteArray);
            }

            //
            var blob = new Blob(byteArrays, { type: contentType });
            return blob;
        }

        /**
         * 천단위 콤마 추가
         * @param {string|number} strOrNum
         * @returns {string}
         */

    }, {
        key: 'addComma',
        value: function addComma(strOrNum) {
            var s = strOrNum;
            if (Pp.isEmpty(strOrNum)) {
                return "";
            }
            //
            if ("number" === typeof s) {
                s = s.toString();
            }
            //
            return s.replace(/(\d)(?=(?:\d{3})+(?!\d))/g, "$1,");
        }

        /**
         * 콤마 제거
         * @param {string} str
         * @returns {string}
         */

    }, {
        key: 'unComma',
        value: function unComma(str) {
            if (Pp.isEmpty(str)) {
                return "";
            }
            //
            return str.replace(/,/gi, "");
        }

        /**
         * 문자열(또는 숫자)에 천단위 콤마 추가
         * @param {string|number} strOrNum 문자열 또는 숫자
         */

    }, {
        key: 'formatNumber',
        value: function formatNumber(strOrNum) {
            return Pp.addComma(strOrNum);
        }

        /**
         * 
         * @param {string|Date} str
         * @param {number} len 4,6,8,10,12,14
         */

    }, {
        key: 'formatDate',
        value: function formatDate(str, len) {
            if ('string' === typeof str) {
                var s = str.replace(/-/gi, '').replace(/ /gi, '').replace(/:/gi, '');

                //
                var result = '';
                if (4 <= len) {
                    //년
                    result = '' + s.substring(0, 4);
                }
                //
                if (6 <= len) {
                    //월
                    result += '-' + s.substring(4, 6);
                }
                //
                if (8 <= len) {
                    //일
                    result += '-' + s.substring(6, 8);
                }
                //
                if (10 <= len) {
                    //시
                    result += ' ' + s.substring(8, 10);
                }
                //
                if (12 <= len) {
                    //분
                    result += ':' + s.substring(10, 12);
                }
                //
                if (14 <= len) {
                    //초
                    result += ':' + s.substring(12, 14);
                }

                //
                return result;
            }

            //TODO
            throw Error('not impl');
        }

        /**
         *
         * @param {string|number} strOrNum
         * @param {number} padLength
         * @param {string} padStr
         * @returns {string}
         */

    }, {
        key: 'lpad',
        value: function lpad(strOrNum, padLength, padStr) {
            var s;
            //
            if ("number" === typeof strOrNum) {
                s = strOrNum.toString();
            } else if ("string" === typeof strOrNum) {
                s = strOrNum;
            } else {
                throw new Error(".lpad - not allowed type");
            }
            //
            if (Pp.isEmpty(s)) {
                return "";
            }
            //
            while (s.length < padLength) {
                s = padStr + s;
            }
            //
            return s;
        }

        /**
         * unique한 문자열 생성
         * @param {string|undefined} pre prefix
         * @returns {string}
         */

    }, {
        key: 'createUid',
        value: function createUid(pre) {
            return (pre ? pre : "UID") + new Date().getTime();
        }

        /**
         * jquery의 $.extend()같은거. source의 key/value를 몽땅 target에 추가
         *          target  source
         * case1    object  object
         * case2    object  Map
         * case3    Map     object
         * case4    Map     Map
         * @param {object|Map} target
         * @param {object|Map} source
         * @returns {object|Map} target의 type에 의해 return type결정됨
         * @history
         *  20200717    Map처리 추가
         */

    }, {
        key: 'extend',
        value: function extend(target, source) {
            /**
             * object + object
             * @param {Object} target 
             * @param {Object} source 
             * @returns {Object}
             */
            var _mergeObjectAndObject = function _mergeObjectAndObject(target, source) {
                Object.keys(source).forEach(function (value, index) {
                    var k = value;
                    //
                    target[k] = source[k];
                });

                //
                return target;
            };

            /**
             * Object + Map
             * @param {Object} target 
             * @param {Map} sourceMap 
             * @returns {Object}
             */
            var _mergeObjectAndMap = function _mergeObjectAndMap(target, sourceMap) {
                sourceMap.forEach(function (value, key) {
                    target[key] = value;
                });

                //
                return target;
            };

            /**
             * Map + Object
             * @param {Map} targetMap 
             * @param {Object} source 
             * @returns {Map}
             */
            var _mergeMapAndObject = function _mergeMapAndObject(targetMap, source) {
                Object.keys(source).forEach(function (value, index) {
                    //
                    var k = value;
                    //
                    targetMap.set(k, source[k]);
                });

                //
                return targetMap;
            };

            /**
             * Map + Map
             * @param {Map} targetMap 
             * @param {Map} sourceMap 
             * @returns {Map}
             */
            var _mergeMapAndMap = function _mergeMapAndMap(targetMap, sourceMap) {
                sourceMap.forEach(function (value, key) {
                    targetMap.set(key, value);
                });

                //
                return targetMap;
            };

            if (Pp.isNull(target) || Pp.isNull(source)) {
                return target;
            }

            //case1
            if (target instanceof Object && source instanceof Object) {
                return _mergeObjectAndObject(target, source);
            }

            //case2
            if (target instanceof Object && source instanceof Map) {
                return _mergeObjectAndMap(target, source);
            }

            //case3
            if (target instanceof Map && source instanceof Object) {
                return _mergeMapAndObject(target, source);
            }

            //case4
            if (target instanceof Map && source instanceof Map) {
                return _mergeMapAndMap(target, source);
            }

            //
            throw Error('');
        }

        /**
         * 널인지 여부
         * @param {any} obj 오브젝트
         * @returns {boolean}
         */

    }, {
        key: 'isNull',
        value: function isNull(obj) {
            if (null === obj) {
                return true;
            }

            //
            if (undefined === obj) {
                return true;
            }

            //
            return false;
        }

        /**
         * not isNull
         * @param {any} obj
         * @returns {boolean}
         */

    }, {
        key: 'isNotNull',
        value: function isNotNull(obj) {
            return !Pp.isNull(obj);
        }

        /**
         * not isEmpty
         * @param {string|number|Array|undefined}strOrArr
         * @returns {boolean}
         */

    }, {
        key: 'isNotEmpty',
        value: function isNotEmpty(strOrArr) {
            return !Pp.isEmpty(strOrArr);
        }

        /**
         * arr요소중 하나라도 empty인지 검사
         * @param {Array<any>} arr 
         * @returns {Boolean} 하나라도 empty이면 true
         * @since 20200915 init
         */

    }, {
        key: 'isAnyEmpty',
        value: function isAnyEmpty(arr) {
            if (Pp.isEmpty(arr)) {
                return false;
            }

            //
            for (var i = 0; i < arr.length; i++) {
                var d = arr[i];

                var b = Pp.isEmpty(d);
                if (b) {
                    return true;
                }
            }

            //
            return false;
        }

        /**
         * arr의 모든 요소가 empty인지 여부
         * @param {Array<any>} arr 
         * @returns {Boolean} 모든 요소가 empty이면 true
         * @since 20200915 init
         */

    }, {
        key: 'isAllEmpty',
        value: function isAllEmpty(arr) {
            if (Pp.isEmpty(arr)) {
                return true;
            }

            //
            for (var i = 0; i < arr.length; i++) {
                var d = arr[i];

                var b = Pp.isEmpty(d);
                if (!b) {
                    return false;
                }
            }

            //
            return true;
        }

        /**
         * obj가 공백인지 여부
         * @param {any} obj 문자열|배열
         * @returns {boolean}
         */

    }, {
        key: 'isEmpty',
        value: function isEmpty(obj) {
            if (Pp.isNull(obj)) {
                return true;
            }

            //숫자형은 항상 false
            if ("number" === typeof obj) {
                return false;
            }

            //
            if (obj instanceof Map) {
                return 0 === obj.size;
            }

            //
            if (Array.isArray(obj)) {
                if (0 === obj.length) {
                    return true;
                }
            }

            //
            if ("string" === typeof obj) {
                if (0 === obj.length) {
                    return true;
                }
            }

            //object이면 키의 갯수로 확인
            if ('object' === (typeof obj === 'undefined' ? 'undefined' : _typeof(obj))) {
                return 0 === Object.keys(obj).length;
            }

            //
            return false;
        }

        /**
         * str이 한글인지 여부
         * @param {string} str 문자열
         * @returns {boolean} true(한글)
         */

    }, {
        key: 'isHangul',
        value: function isHangul(str) {
            var pattern_kor = /[ㄱ-ㅎ|ㅏ-ㅣ|가-힣]/;
            if (pattern_kor.test(str)) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * like oracle's nvl()
         * @param {any} obj
         * @param {any} defaultValue
         * @returns {any}
         */

    }, {
        key: 'nvl',
        value: function nvl(obj, defaultValue) {
            if (Pp.isNotNull(obj)) {
                return obj;
            }
            //
            if (Pp.isNull(defaultValue)) {
                return "";
            } else {
                return defaultValue;
            }
        }

        /**
         * ajax 호출을 Promise 패턴으로 구현
         * @param {string} url 
         * @param {any} param 
         * @param {any|undefined} option {'method':string|undefined, ascyn:boolean|undefined}
         * @returns {string|any} 리턴값
         */

    }, {
        key: 'ajaxPromise',
        value: function ajaxPromise(url, param, option) {
            if (Pp.isEmpty(url) || Pp.isNull(param)) {
                return new Promise(function (resolve, reject) {
                    reject('url or param is empty');
                });
            }

            //
            var defaultSetting = {
                method: "POST",
                async: true
            };

            //
            var opt = Pp.extend(defaultSetting, option);

            //
            var xhr = new XMLHttpRequest();
            //
            xhr.open(opt.method, url, opt.async);
            xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');

            //
            xhr.upload.onprogress = function (e) {
                if (!e.lengthComputable) {
                    return;
                }

                //
                var percentComplete = e.loaded / e.total * 100;
                console.log(percentComplete + '% uploaded');
            };

            //
            xhr.onreadystatechange = function () {
                if (XMLHttpRequest.DONE === xhr.readyState) {
                    var v = xhr.responseText.trim();

                    //
                    return new Promise(function (resolve, reject) {
                        //성공
                        if (200 === xhr.status) {
                            //json 형식 or text 형식
                            v.startsWith('{') ? resolve(JSON.parse(v)) : resolve(v);
                        } else {
                            //실패
                            reject(v);
                        }
                    });
                }
            };

            //
            var fd = new FormData();
            var p = Pp.toKv(param);
            //
            Object.keys(p).forEach(function (k) {
                fd.append(k, p[k]);
            });

            //
            xhr.send(fd);
        }

        /**
         * get 방식으로 요청
         * @param {string} url url
         * @param {object} param case1~4
         * @param {function} callbackFn 콜백함수
         * @param {object} option @see submitAjax's option
         * @since 20200902 init
         */

    }, {
        key: 'get',
        value: function get(url, param, callbackFn, option) {
            var opt = { 'method': 'get' };
            if (!Pp.isNull(option)) {
                opt = Pp.extend(opt, option);
            }

            //
            Pp.submitAjax(url, param, callbackFn, opt);
        }

        /**
         * post 방식으로 요청
         * @param {string} url url
         * @param {object} param case1~4
         * @param {function} callbackFn 콜백함수
         * @param {object} option @see submitAjax's option
         * @since 20200902 init
         */

    }, {
        key: 'post',
        value: function post(url, param, callbackFn, option) {
            var opt = { 'method': 'post' };
            if (!Pp.isNull(option)) {
                opt = Pp.extend(opt, option);
            }

            //
            Pp.submitAjax(url, param, callbackFn, opt);
        }

        /**
         * ajax 요청
         * @param {string} url url
         * @param {any} param case1~4
         * @param {Function} callbackSuccess 콜백함수
         * @param {any|undefined} option {'method':string, 'async':boolean, 'callbackError':function, 'header':{}}
         */

    }, {
        key: 'submitAjax',
        value: function submitAjax(url, param, callbackSuccess, option) {
            if (Pp.isEmpty(url) || Pp.isNull(param)) {
                return;
            }

            //
            var defaultSetting = {
                method: "POST",
                async: true,
                callbackError: null
            };
            //
            var opt = Pp.extend(defaultSetting, option);

            //
            var xhr = new XMLHttpRequest();
            //
            xhr.open(opt.method, url, opt.async);
            xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
            if (Pp.isNotEmpty(opt.header)) {
                var keys = Object.keys(opt.header);
                for (var i = 0; i < keys.length; i++) {
                    var k = keys[i];
                    //
                    xhr.setRequestHeader(k, opt.header[k]);
                }
            }

            //
            xhr.upload.onprogress = function (e) {
                if (!e.lengthComputable) {
                    return;
                }

                //
                var percentComplete = e.loaded / e.total * 100;
                console.log(percentComplete + '% uploaded');
            };

            //
            xhr.onreadystatechange = function () {
                if (XMLHttpRequest.DONE === xhr.readyState) {
                    var v = xhr.responseText.trim();

                    //성공
                    if (200 === xhr.status) {
                        //json 형식
                        if (v.startsWith("{")) {
                            callbackSuccess(JSON.parse(v));
                        } else {
                            //text 형식
                            callbackSuccess(v);
                        }
                    } else {
                        //실패
                        if (Pp.isNotNull(opt.callbackError)) {
                            opt.callbackError(v);
                        } else {
                            alert("오류가 발생했습니다.");
                        }
                    }
                }
            };

            //
            var fd = new FormData();
            var p = Pp.toKv(param);
            //
            Object.keys(p).forEach(function (k) {
                fd.append(k, p[k]);
            });

            //
            xhr.send(fd);
        }

        /**
         * 파일 또는 blob 전송
         * @param {string} url 
         * @param {File|Blob} fileOrBlob 
         * @param {Function} callbackFn 
         * @param {Object} option 
         */

    }, {
        key: 'submitFile',
        value: function submitFile(url, fileOrBlob, callbackFn, option) {

            //
            var fd = new FormData();
            fd.append('file', fileOrBlob); //TODO 파일인 경우 파싱필요?

            //
            this.submitFormData(url, fd, callbackFn, option);
        }

        /**
         *  formdata 전송
         * @since
         *	20200818	init
         */

    }, {
        key: 'submitFormData',
        value: function submitFormData(url, formData, callbackFn, option) {
            var xhr = new XMLHttpRequest();
            xhr.open('POST', url, true);
            xhr.setRequestHeader("Access-Control-Allow-Headers", "*");
            xhr.onload = function (e) {}
            //console.log(e);


            //
            ;xhr.onreadystatechange = function () {
                if (4 === xhr.readyState) {
                    var str = xhr.response;
                    if (Pp.isEmpty(str)) {
                        str = '{}';
                    }

                    //json이면
                    if ('{' === str.trim()[0]) {
                        var json = JSON.parse(str);
                        callbackFn(json);
                    } else {
                        //json 문자열이 아니면
                        callbackFn(str);
                    }
                }
            };

            //
            xhr.send(formData);
        }

        /**
         * 업로드 가능한 확장자인지 검사
         * @param {File} file 
         * @param {Array<Exts>} arrOfExts
         * @returns {boolean}
         */

    }, {
        key: 'checkFileExt',
        value: function checkFileExt(file, arrOfExts) {
            if (Pp.isNull(file)) {
                return false;
            }

            //TODO
            return true;
        }

        /**
         * 파일 크기 검사
         * @param {File} file 
         * @param {number} maxFileSize 
         * @returns {boolean}
         */

    }, {
        key: 'checkFileSize',
        value: function checkFileSize(file, maxFileSize) {
            if (Pp.isNull(file)) {
                return false;
            }

            //
            return file.size < maxFileSize;
        }

        /**
         * 파라미터 형 변환
         * @param {any} param 파라미터
         *  case1 {'name':string, 'value':any}
         *  case2 [case1]
         *  case3 {'key':'value'}
         *  case4 [case3]
         * @returns {any}
         */

    }, {
        key: 'toKv',
        value: function toKv(param) {
            var p = {};
            //case2, case4인 경우
            if (Array.isArray(param)) {
                return Pp.toKvFromArray(param);
            }

            //case1
            if (Pp.isNotEmpty(param.name)) {
                return Pp.toKvFromNameValue(param.name, param.value);
            }

            //case3
            return param;
        }

        /**
         * toKv()와 로직은 동일
         * 다른점. toKv()는 Object 리턴, toMap()은 Map 리턴
         * case1~4의 정보는 @see toKv()참고
         * @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map
         * @param {Map|Array|object} param 
         * @returns {Map}
         */

    }, {
        key: 'toMap',
        value: function toMap(param) {

            /**
             * 
             * @param {Array<Map>|Array<Object>} arr 
             */
            var _toMapFromArray = function _toMapFromArray(arr) {

                //
                if (0 == arr.length) {
                    return new Map();
                }

                //
                if (arr[0] instanceof Map) {
                    return _toMapFromMapArray(arr);
                }

                //
                if (arr[0] instanceof Object) {
                    return _toMapFromObjectArray(arr);
                }
            };

            /**
             * 
             * @param {Array<Object>} arr 
             */
            var _toMapFromObjectArray = function _toMapFromObjectArray(arr) {
                var resultMap = new Map();

                //
                arr.forEach(function (value, index) {
                    var json = value;
                    //
                    if (!json.hasOwnProperty('name')) {
                        return;
                    }

                    //
                    resultMap = Pp.extend(resultMap, json);
                });

                //
                return resultMap;
            };

            /**
             * 
             * @param {Array<Map>} arr 
             * @returns {Map}
             */
            var _toMapFromMapArray = function _toMapFromMapArray(arr) {
                var resultMap = new Map();

                //
                arr.forEach(function (value, index) {
                    var map = value;
                    //
                    if (!map.has('name')) {
                        return;
                    }

                    //
                    resultMap = Pp.extend(resultMap, map);
                });

                //
                return resultMap;
            };

            //
            var map = new Map();

            //case2, case4
            if (Array.isArray(param)) {
                return _toMapFromArray(param);
            }

            //case1
            if (param instanceof Object && param.hasOwnProperty('name')) {
                var resultMap = new Map();
                //
                var k = param.name;
                var v = param.value;
                //
                resultMap.set(k, v);

                //
                return resultMap;
            }

            //
            return param;
        }

        /**
         * 파리미터 형 변환
         * @param {any} arr 파라미터 배열
         * @returns {any}
         */

    }, {
        key: 'toKvFromArray',
        value: function toKvFromArray(arr) {
            if (Pp.isEmpty(arr)) {
                return {};
            }

            //
            var json = arr[0];

            //
            if (Pp.isNotEmpty(json.name)) {
                //case2
                return Pp.toKvFromNameValueArray(arr);
            } else {
                //case4
                var p = {};
                //
                arr.forEach(function (json) {
                    p = Pp.extend(p, json);
                });

                //
                return p;
            }
        }

        /**
         * 파라미터 형변환
         * @param {Array} arr 파라미터 배열. case2
         * @returns {any}
         */

    }, {
        key: 'toKvFromNameValueArray',
        value: function toKvFromNameValueArray(arr) {
            var _this = this;

            var p = {};

            //
            arr.forEach(function (json) {
                p = Pp.extend(p, _this.toKvFromNameValue(json.name, json.value));
            });

            //
            return p;
        }

        /**
         * 파라미터 형 변환
         * @param {string} name
         * @param {string} value
         * @returns {any}
         */

    }, {
        key: 'toKvFromNameValue',
        value: function toKvFromNameValue(name, value) {
            var k = name;
            var v = value;

            //
            var p = {};
            p[k] = v;
            //
            return p;
        }

        /**
         * @see https://jasonwatmore.com/post/2018/08/07/javascript-pure-pagination-logic-in-vanilla-js-typescript
         * @param {number} totalItems 전체 아이템 갯수
         * @param {number} currentPage 현재 페이지 번호. default:1
         * @param {number} pageSize 페이징 크기. default:10
         * @param {number} maxPages 화면에 표시할 페이지 번호 갯수. default:10
         */

    }, {
        key: 'paginate',
        value: function paginate(totalItems) {
            var currentPage = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 1;
            var pageSize = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : 10;
            var maxPages = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : 10;


            // calculate total pages
            var totalPages = Math.ceil(totalItems / pageSize);

            // ensure current page isn't out of range
            if (currentPage < 1) {
                currentPage = 1;
            } else if (currentPage > totalPages) {
                currentPage = totalPages;
            }

            var startPage = void 0,
                endPage = void 0;
            if (totalPages <= maxPages) {
                // total pages less than max so show all pages
                startPage = 1;
                endPage = totalPages;
            } else {
                // total pages more than max so calculate start and end pages
                var maxPagesBeforeCurrentPage = Math.floor(maxPages / 2);
                var maxPagesAfterCurrentPage = Math.ceil(maxPages / 2) - 1;
                if (currentPage <= maxPagesBeforeCurrentPage) {
                    // current page near the start
                    startPage = 1;
                    endPage = maxPages;
                } else if (currentPage + maxPagesAfterCurrentPage >= totalPages) {
                    // current page near the end
                    startPage = totalPages - maxPages + 1;
                    endPage = totalPages;
                } else {
                    // current page somewhere in the middle
                    startPage = currentPage - maxPagesBeforeCurrentPage;
                    endPage = currentPage + maxPagesAfterCurrentPage;
                }
            }

            // calculate start and end item indexes
            var startIndex = (currentPage - 1) * pageSize;
            var endIndex = Math.min(startIndex + pageSize - 1, totalItems - 1);

            // create an array of pages to ng-repeat in the pager control
            var pages = Array.from(Array(endPage + 1 - startPage).keys()).map(function (i) {
                return startPage + i;
            });

            // return object with all pager properties required by the view
            return {
                totalItems: totalItems,
                currentPage: currentPage,
                pageSize: pageSize,
                totalPages: totalPages,
                startPage: startPage,
                endPage: endPage,
                startIndex: startIndex,
                endIndex: endIndex,
                pages: pages
            };
        }
    }]);

    return Pp;
}();
