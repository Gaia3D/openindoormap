'use strict';

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

/**
 * ui와 관련된 것들
 * es6 버전
 * jquery사용하지 않음
 * 원본 파일 : ppui-version-es6.js
 * 변환 파일 : ppui-version-legacy.js
 * 변환툴 : babel
 * es5로 컴파일하는 방법 : @see https://stackoverflow.com/questions/34747693/how-do-i-get-babel-6-to-compile-to-es5-javascript
 * 주의! 직접 ppui-version-lagacy.js파일 변경 불허
 * @since   
 *  2020-07-16  init
 * @author gravity
 */
var Ppui = function () {
    function Ppui() {
        _classCallCheck(this, Ppui);
    }

    _createClass(Ppui, null, [{
        key: 'bindDatas',


        /**
         * 엘리먼트에 값 바인드
         * @param {string|object} selectorOrEl 엘리먼트 선택자 또는 엘리먼트
         * @param {array} datas 데이터 목록
         * @param {object} option {initValue:string|null, tkey:string, vkey:string, headerText:string|null, headerValue:string|null, append:boolean}
         *                  initValue : 초기값. 선택
         *                  tkey : 텍스트 키. datas에서 text로 사용될 키의 정보. 필수
         *                  vkey : 값 키. datas에서 value으로 사용될 키의 정보. 필수
         *                  headerText : 헤더 텍스트. 헤더 텍스트 존재시 headerValue도 반드시 존재해야 함. 선택
         *                  headerValue : 헤더 값. 헤더 값 존재시 headerText도 반드시 존재해야 함. 선택
         *                  append : 추가 여부. false이면 기존 option 모두 삭제 후 데이터 바인드. 선택. 초기값:true
         * @since 20200827 init
         */
        value: function bindDatas(selectorOrEl, datas, option) {
            var arr = Ppui.flat(selectorOrEl);
            //
            if (Pp.isNull(arr)) {
                return;
            }

            //
            var opt = Pp.extend({ 'initValue': null, 'append': true, 'headerText': null, 'headerValue': null }, option);

            //
            var _select = function _select(el, datas, opt) {
                //추가가 아니면
                if (!opt.append) {
                    //기존 옵션 모두 삭제
                    el.options.length = 0;
                }

                //
                if (Pp.isNull(datas)) {
                    datas = [];
                }

                //헤더 텍스트 존재하면
                if (Pp.isNotEmpty(opt.headerText)) {
                    var _option = document.createElement('option');
                    el.appendChild(_option);
                    //
                    _option.text = opt.headerText;
                    _option.value = opt.headerValue;
                }

                //
                for (var i = 0; i < datas.length; i++) {
                    var d = datas[i];

                    //
                    var _option2 = document.createElement('option');
                    el.appendChild(_option2);
                    //
                    _option2.value = d[opt.vkey];
                    _option2.text = d[opt.tkey];
                }

                //초기값 존재하면
                if (Pp.isNotEmpty(opt.initValue)) {
                    for (var _i = 0; _i < el.options.length; _i++) {
                        if (opt.initValue == el.options[_i].value) {
                            el.selectedIndex = _i;
                            break;
                        }
                    }
                }

                //
                return el;
            };

            //
            for (var i = 0; i < arr.length; i++) {
                var el = arr[i];

                //
                if ('SELECT' === el.tagName) {
                    _select(el, datas, opt);
                }
            }

            //
            return arr;
        }
    }, {
        key: 'bindEnterKey',


        /**
         * 엔터키 처리
         * @param {HTMLElement|HTMLCollection|NodeListOf<Element>|string} elOrSelector getElement or getElements or querySelectorAll()
         * @param {Function} callback 
         * @returns {void}
         */
        value: function bindEnterKey(elOrSelector, callback) {
            var arr = Ppui.flat(elOrSelector);
            //
            arr.forEach(function (el) {
                //
                el.removeEventListener('keypress', function () {
                    //nothing
                });

                //
                el.addEventListener('keypress', function (ev) {
                    if (13 === ev.keyCode) {
                        callback(ev);
                    }
                });
            });
        }

        /**
         * el이 엘리먼트인지 여부
         * @param {HTMLElement|any} el 엘리먼트
         * @returns {boolean} 엘리먼트이면 true
         */

    }, {
        key: '_isElement',
        value: function _isElement(el) {
            return el instanceof HTMLElement;
        }

        /**
         * 콜렉션인지 여부
         * @param {HTMLCollection|any} coll  콜렉션
         * @returns {boolean} 콜렉션이면 true
         */

    }, {
        key: '_isCollection',
        value: function _isCollection(coll) {
            return coll instanceof HTMLCollection;
        }

        /**
         * 노드리스트인지 여부
         * @param {NodeList|any} nl  노드리스트
         * @returns {boolean} 노드리스트이면 true
         */

    }, {
        key: '_isNodeList',
        value: function _isNodeList(nl) {
            return nl instanceof NodeList;
        }

        /**
         * TODO
         * @param {string} selector 셀렉터
         */

    }, {
        key: 'data',
        value: function data(selector) {}

        /**
         * 배열, 콜렉션, 노드목록을 1차원 배열로 변환
         * @param {Array|any} arrOrAny 
         * @returns {Array} 배열
         */

    }, {
        key: 'flat',
        value: function flat(arrOrAny) {
            if (!Array.isArray(arrOrAny)) {
                return Ppui.flat([arrOrAny]);
            }

            //
            var arr = [];

            arrOrAny.forEach(function (x) {
                if ('string' === typeof x) {
                    arr = arr.concat(Array.from(document.querySelectorAll(x)));
                    return;
                }

                if (x instanceof NodeList) {
                    arr = arr.concat(Array.from(x));
                    return;
                }

                if (x instanceof HTMLCollection) {
                    arr = arr.concat(Array.from(x));
                    return;
                }

                if (x instanceof Element) {
                    arr = arr.concat(Array.from([x]));
                    return;
                }
            });

            //
            return arr.filter(function (x) {
                return Pp.isNotNull(x);
            });
        }
    }, {
        key: 'child',
        value: function child(parentEl, selector) {
            return parentEl.querySelector(selector);
        }

        /**
         * 엘리먼트 인스턴스 리턴
         * @param {string} selector 셀렉터
         */

    }, {
        key: 'find',
        value: function find(selector) {
            var coll = document.querySelectorAll(selector);
            if (Pp.isNull(coll)) {
                return null;
            }

            //
            if (1 == coll.length) {
                return coll.item(0);
            } else {
                return coll;
            }
        }

        /**
         * el에 클래스 추가. like jq's addClass
         * @param {HTMLElement|HTMLCollection|NodeListOf<Element>|Array|string} el getElement or getElements or querySelectorAll()
         * @param {string} className 클래스명
         * @returns {object} Ppui
         * @since
         *  20200831    el에 string 추가
         */

    }, {
        key: 'addClass',
        value: function addClass(obj, className) {
            var arr = Ppui.flat(obj);

            //
            arr.forEach(function (el) {
                //
                if (Ppui.hasClass(el, className)) {
                    return;
                }

                //
                el.classList.add(className);
            });
        }

        /**
         * el에 클래스가 존재하는지 여부. like jq's hasClass
         * @param {Element|HTMLCollection|NodeListOf<Element>|Array|string} elOrSelector getElement or getElements or querySelectorAll()
         * @param {string} className 클래스명
         * @returns {boolean|null}
         */

    }, {
        key: 'hasClass',
        value: function hasClass(elOrSelector, className) {
            var arr = Ppui.flat(elOrSelector);

            //
            var b = false;
            arr.forEach(function (el) {
                b = b || Ppui._hasClassAtElement(el, className);
            });

            //
            return b;
        }

        /**
         * like jquery's toggleClass
         * @param {Element|HTMLCollection|NodeList|string} elOrSelector 
         * @param {string} className 
         * @returns {object} Ppui
         */

    }, {
        key: 'toggleClass',
        value: function toggleClass(elOrSelector, className) {
            Ppui.hasClass(elOrSelector, className) ? Ppui.removeClass(elOrSelector, className) : Ppui.addClass(elOrSelector, className);

            return Ppui;
        }

        /**
         * 
         * @param {Element|null} el 
         * @param {string} className 
         * @returns {boolean}
         */

    }, {
        key: '_hasClassAtElement',
        value: function _hasClassAtElement(el, className) {
            if (Pp.isNull(el)) {
                return false;
            }

            //
            var b = false;
            //
            el.classList.forEach(function (x) {
                if (x === className) {
                    b = true;
                }
            });

            //
            return b;
        }

        /**
         * el에서 클래스 삭제. like jq's removeClass
         * @param {HTMLElement|HTMLCollection|NodeListOf<Element>|string} el getElement or getElements or querySelectorAll()
         * @param {string} className 클래스명
         * @returns {object} Ppui
         * @since
         *  20200831    el에 string추가
         */

    }, {
        key: 'removeClass',
        value: function removeClass(elOrSelector, className) {
            var arr = Ppui.flat(elOrSelector);

            //
            arr.forEach(function (el) {
                el.classList.remove(className);
            });
        }

        /**
         * 
         * @param {Element|Node|NodeList|string} elOrSelector 
         * @param {string} beforeClassName 변경전 classname
         * @param {string} afterClassName 변경후 classname
         */

    }, {
        key: 'replaceClass',
        value: function replaceClass(elOrSelector, beforeClassName, afterClassName) {
            //
            Ppui.removeClass(elOrSelector, beforeClassName).addClass(elOrSelector, afterClassName);
        }

        /**
         * 추가하기
         * @param {Element|Collection|NodeList|Array|string} elOrSelector 엘리먼트
         * @param {string} htmlString 문자열
         */

    }, {
        key: 'append',
        value: function append(elOrSelector, htmlString) {
            var arr = Ppui.flat(elOrSelector);

            var div = document.createElement('div');
            div.innerHTML = htmlString;

            //
            arr.forEach(function (el) {
                el.appendChild(div.firstChild.cloneNode(true));
            });
        }

        /**
         * 엘리먼트 삭제
         * @param {Element|Collection|NodeList|string} elOrSelector 엘리먼트|콜렉션|노드목록|셀렉터
         * @since 20200903 init
         */

    }, {
        key: 'remove',
        value: function remove(elOrSelector) {
            var arr = Ppui.flat(elOrSelector);
            //
            arr.forEach(function (el) {
                el.remove();
            });
        }

        /**
         * 화면에서 숨기기
         * @param {Element|Collection|NodeList|Array|string} elOrSelector 엘리먼트|콜렉션|노드목록|셀렉터
         */

    }, {
        key: 'hide',
        value: function hide(elOrSelector) {
            Ppui._showHide(elOrSelector, false);
        }

        /**
         * 화면에 표시하기
         * @param {Element|Collection|NodeList|Array|string} elOrSelector 엘리먼트|콜렉션|노드목록|셀렉터
         */

    }, {
        key: 'show',
        value: function show(elOrSelector) {
            Ppui._showHide(elOrSelector, true);
        }

        /**
         * 화면에서 숨기기/표시하기
         * @param {Element|Collection|NodeList|Array|string} elOrSelector 엘리먼트|콜렉션|노드목록|배열|셀렉터
         * @param {boolean} isShow 표시여부
         */

    }, {
        key: '_showHide',
        value: function _showHide(elOrSelector, isShow) {
            var arr = Ppui.flat(elOrSelector);
            //
            arr.forEach(function (el) {
                el.style.display = isShow ? 'block' : 'none';
            });
        }

        /**
         * blob로 <img> 생성
         * @param {Object} blob 이미지 blob
         * @param {object} option 속성값 {'width':number, 'height':number, 'id':string, 'name':string}
         * @param {function} callbackFn 이미지 생성 완료 후 호출할 콜백함수. FileReader가 비동기적으로 처리되기 때문에 콜백사용
         */

    }, {
        key: 'createImgByBlob',
        value: function createImgByBlob(blob, option, callbackFn) {
            var img = document.createElement('img');

            //
            var opt = Pp.extend({}, option);

            //
            if (Pp.isNotEmpty(opt.id)) {
                img.id = opt.id;
            }
            //
            if (Pp.isNotEmpty(opt.name)) {
                img.name = opt.name;
            }
            //
            if (Pp.isNotEmpty(opt.width)) {
                img.width = opt.width;
            }
            //
            if (Pp.isNotEmpty(opt.height)) {
                img.height = opt.height;
            }

            //@see http://www.jongminjang.com/html5/file/2018/12/27/blob-as-img-src.html
            var reader = new FileReader();
            reader.onload = function (e) {
                img.src = reader.result;

                //
                callbackFn(img);
            };
            //
            reader.readAsDataURL(blob);
        }

        /**
         * element 생성
         * @param {string} tagName 
         * @param {string} name 
         * @param {any} value 
         * @param {any|undefined} opt case3
         * @returns {HTMLElement}
         */

    }, {
        key: 'createElement',
        value: function createElement(tagName, name, value, opt) {
            var htmlElement = document.createElement(tagName);

            //
            if ('INPUT' === tagName.toUpperCase()) {
                htmlElement.setAttribute('type', 'hidden');
                htmlElement.setAttribute('value', value);
            }
            //
            htmlElement.setAttribute('id', name);
            htmlElement.setAttribute('name', name);

            //
            if (Pp.isNull(opt)) {
                return htmlElement;
            }

            //
            var keys = Object.keys(opt);
            //
            keys.forEach(function (k) {
                var key = k;
                var value = opt[k];
                //
                htmlElement.setAttribute(key, value);
            });

            //
            return htmlElement;
        }

        /**
         * 폼 생성
         * @param {Array|any|undefined} param case1,case2,case3,case4
         * @returns {HTMLFormElement}
         */

    }, {
        key: 'createForm',
        value: function createForm(param) {
            var htmlFormElement = document.createElement('form');
            //
            htmlFormElement.setAttribute('id', Pp.createUid());

            //
            if (Pp.isNull(param)) {
                return htmlFormElement;
            }

            //
            var map = Pp.toMap(param);
            //
            map.forEach(function (value, key) {
                //
                var el = Ppui.createElement('INPUT', key, value);
                //
                htmlFormElement.appendChild(el);
            });

            //
            return htmlFormElement;
        }

        /**
         * 폼 생성 & 제출
         * @param {string} url 
         * @param {Array|any|undefined} param 
         * @returns {void}
         */

    }, {
        key: 'createFormAndSubmit',
        value: function createFormAndSubmit(url, param) {
            var htmlFormElement = Ppui.createForm(param);
            //
            if (Pp.isNull(htmlFormElement)) {
                return;
            }

            //
            var el = document.querySelector('body:last-child');
            //
            if (Pp.isNull(el)) {
                return;
            }

            //
            el.appendChild(htmlFormElement);

            //
            htmlFormElement.setAttribute('method', 'post');
            htmlFormElement.setAttribute('action', url);
            //
            htmlFormElement.submit();
        }

        /**
         * 폼 전송
         * @param {string} url 
         * @param {htmlFormElement} htmlFormElement  form element
         * @returns {void}
         */

    }, {
        key: 'submitForm',
        value: function submitForm(url, htmlFormElement) {
            //
            htmlFormElement.setAttribute('method', 'post');
            htmlFormElement.setAttribute('action', url);
            //
            htmlFormElement.submit();
        }

        /**
         *
         * @param {string} url
         * @param {any} param case1~4
         * @returns {void}
         */

    }, {
        key: 'submitGet',
        value: function submitGet(url, param) {
            var htmlFormElement = Ppui.createForm(param);

            //
            htmlFormElement.setAttribute("method", "get");
            //
            htmlFormElement.submit();
        }

        /**
         *
         * @param {string} url
         * @param {any} param case1~4
         * @returns {void}
         */

    }, {
        key: 'submitPost',
        value: function submitPost(url, param) {
            var htmlFormElement = Ppui.createForm(param);

            //
            htmlFormElement.setAttribute("method", "post");
            //
            htmlFormElement.submit();
        }

        /**
         * like jquery's serializeArray
         * @param {HTMLCollection} htmlCollection document.getElementsByTagName('body') 
         * @returns {Array}
         */

    }, {
        key: 'serializeArray',
        value: function serializeArray(htmlCollection) {
            /**
             * children쪽 처리
             * @param {htmlCollection} htmlCollection body하위의 children
             * @return {Array} 
             */
            function _children(htmlCollection) {
                var arr = [];
                //
                if (Pp.isNull(htmlCollection)) {
                    return arr;
                }

                //
                for (var i = 0; i < htmlCollection.length; i++) {
                    var el = htmlCollection.item(i);
                    if (Pp.isNull(el)) {
                        continue;
                    }

                    //
                    if ('INPUT' === el.tagName) {
                        arr.push({
                            'name': el.name,
                            'value': el.value
                        });
                    }
                    //
                    if ('TEXTAREA' === el.tagName) {
                        arr.push({
                            'name': el.name,
                            'value': el.value
                        });
                    }
                    //TODO others
                }

                //
                return arr;
            } //


            //결과
            var arr = [];

            //
            if (Pp.isNull(htmlCollection) || 0 == htmlCollection.length) {
                return [];
            }

            //body 갯수만큼 루프
            for (var i = 0; i < htmlCollection.length; i++) {
                var el = htmlCollection.item(i);
                if (Pp.isNull(el)) {
                    continue;
                }

                //body내의 children (input, textarea, ...)
                var children = el.children;
                //children에서 값을 추출해 arr에 추가
                arr = arr.concat(_children(children));
            }

            //
            return arr;
        }

        /**
         * 필수입력 항목 검사
         * @param {NodeList} nodeList document.querySelectorAll('[required]')
         * @param {Map|undefined} option {showMessage:boolean}
         * @returns {Map} {b:boolean, title:string, node:Node}
         */

    }, {
        key: 'checkReq',
        value: function checkReq(nodeList, option) {
            var map = new Map();
            map.set('b', true);

            //
            var opt = Pp.extend(new Map().set('showMessage', true), option);

            //
            if (null == nodeList || 0 == nodeList.length) {
                return map;
            }

            //
            for (var i = 0; i < nodeList.length; i++) {
                var _node = nodeList.item(i);

                //
                if (Pp.isEmpty(_node.value)) {
                    map.set('b', false);
                    map.set('node', _node);

                    break;
                }
            }

            //
            if (Pp.isNotEmpty(map.get('node').title)) {
                map.set('title', map.get('node').title);
            } else {
                var id = node.id;
                var htmlElement = document.querySelector('label[for="' + id + '"]');
                if (Pp.isNotNull(htmlElement)) {
                    map.set('title', htmlElement.innerHTML);
                }
            }

            //메시지 표시이면
            if (opt.get('showMessage')) {
                map.get('node').focus();
                //
                if (Pp.isNotEmpty(map.get('title'))) {
                    alert(map.get('title') + '\uC740(\uB294) \uD544\uC218\uD56D\uBAA9\uC785\uB2C8\uB2E4.');
                } else {
                    alert('필수항목입니다.');
                }
            }

            //
            return map;
        }

        /**
         * el에 이벤트 등록
         * @param {HtmlElement|HTMLCollection|NodeList|string} elOrSelector
         * @param {string} eventName
         * @param {Function} callbackFn
         * @since	20200818	init
         */

    }, {
        key: 'on',
        value: function on(elOrSelector, eventName, callbackFn) {
            var arr = Ppui.flat(elOrSelector);
            //
            arr.forEach(function (el) {
                //
                Ppui.unbind(el, eventName, callbackFn);
                //
                el.addEventListener(eventName, callbackFn);
            });
        }

        /**
         * 이벤트 핸들러 삭제
         * @param {Element|Collection|NodeList|string} elOrSelector 엘리먼트
         * @param {string} eventName 이벤트명
         * @param {function} callbackFn 콜백함수
         */

    }, {
        key: 'unbind',
        value: function unbind(elOrSelector, eventName, callbackFn) {
            var arr = Ppui.flat(elOrSelector);
            //
            arr.forEach(function (el) {
                el.removeEventListener(eventName, callbackFn, false);
            });
        }

        /**
         * 이벤트 강제로 실행시키기
         * @param {HTMLElement|HTMLCollection|NodeList|string} elOrSelector 
         * @param {string} eventName 이벤트명
         */

    }, {
        key: 'trigger',
        value: function trigger(elOrSelector, eventName) {
            var arr = Ppui.flat(elOrSelector);
            //
            arr.forEach(function (el) {
                //
                el.dispatchEvent(new Event(eventName));
            });
        }

        /**
         * click 이벤트 등록
         * @param {HTMLElement|HTMLCollection|NodeList|string} elOrSelector 
         * @param {function} callbackFn 
         */

    }, {
        key: 'click',
        value: function click(elOrSelector, callbackFn) {
            Ppui.on(elOrSelector, 'click', callbackFn);
        }

        /**
         * change 이벤트 등록
         * @param {HTMLElement|HTMLCollection|NodeList|string} elOrSelector 
         * @param {function} callbackFn 
         */

    }, {
        key: 'change',
        value: function change(elOrSelector, callbackFn) {
            Ppui.on(elOrSelector, 'change', callbackFn);
        }

        /**
         * 파일 업로드
         * @param {string} url
         * @param {File} file 
         * @param {Function} callbackSuccess
         * @param {any|undefined} option
         */

    }, {
        key: 'uploadFile',
        value: function uploadFile(url, file, callbackSuccess, option) {
            if (Pp.isNull(file)) {
                callbackSuccess({ errorCode: 'E_NULL' });
                return;
            }

            //파일 크기 검사
            if (!Pp.checkFileSize(file, 123456)) {
                callbackSuccess({ errorCode: 'E_FILE_SIZE' });
                return;
            }

            //파일 확장자 검사
            if (!Pp.checkFileExt(file)) {
                callbackSuccess({ errorCode: 'E_EXT' });
                return;
            }

            //
            var xhr = new XMLHttpRequest();
            xhr.open('post', url, true);
            xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');

            //
            xhr.onreadystatechange = function () {
                if (XMLHttpRequest.DONE === xhr.readyState) {
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
                        callbackSuccess({ errorCode: 'E_500' });
                        return;
                    }
                }
            };

            //
            var fd = new FormData();
            fd.append('file', file);

            //
            xhr.send(fd);
        }
    }]);

    return Ppui;
}();

/**
 * jQuery같은거 흉내내기
 * @param {*} args 
 * @since 20200904 init
 */


var globalFunction = function globalFunction(args) {
    return new X_(args);
};

//
window['x'] = globalFunction;

var X_ = function () {
    function X_(args) {
        _classCallCheck(this, X_);

        this.items = Ppui.flat(args);
    }

    _createClass(X_, [{
        key: 'addClass',
        value: function addClass(className) {
            Ppui.addClass(this.items, className);
            //
            return this;
        }
    }, {
        key: 'removeClass',
        value: function removeClass(className) {
            Ppui.removeClass(this.items, className);
            //
            return this;
        }
    }, {
        key: 'toggleClass',
        value: function toggleClass(className) {
            Ppui.toggleClass(this.items, className);
            //
            return this;
        }
    }, {
        key: 'hasClass',
        value: function hasClass(className) {
            return Ppui.hasClass(this.items, className);
        }
    }, {
        key: 'hide',
        value: function hide() {
            Ppui.hide(this.items);
            //
            return this;
        }
    }, {
        key: 'show',
        value: function show() {
            Ppui.show(this.items);
            //
            return this;
        }
    }, {
        key: 'click',
        value: function click(callbackFn) {
            Ppui.click(this.items, callbackFn);
            //
            return this;
        }
    }, {
        key: 'change',
        value: function change(callbackFn) {
            Ppui.change(this.items, callbackFn);
            //
            return this;
        }
    }, {
        key: 'unbind',
        value: function unbind(eventName, callbackFn) {
            Ppui.unbind(this.items, eventName, callbackFn);
            //
            return this;
        }
    }, {
        key: 'on',
        value: function on(eventName, callbackFn) {
            Ppui.on(this.items, eventName, callbackFn);
            //
            return this;
        }
    }, {
        key: 'length',
        value: function length() {
            return this.items.length;
        }
    }, {
        key: 'size',
        value: function size() {
            return this.length();
        }
    }, {
        key: 'each',
        value: function each(callbackFn) {
            var i = 0;
            this.items.forEach(function (el) {
                callbackFn(i++, el);
            });

            //
            return this;
        }
    }, {
        key: 'find',
        value: function find(selector) {
            var arr = [];

            this.items.forEach(function (el) {
                arr = arr.concat(Array.from(el.querySelectorAll(selector)));
            });

            //
            this.items = arr;
            //
            return this;
        }
    }, {
        key: 'append',
        value: function append(htmlString) {
            Ppui.append(this.items, htmlString);

            //
            return this;
        }
    }, {
        key: 'remove',
        value: function remove() {
            Ppui.remove(this.items);
            //
            this.items = [];
            //
            return this;
        }
    }, {
        key: 'each',
        value: function each(arr, callback) {
            if (Pp.isEmpty(arr)) {
                return;
            }

            //
            var i = 0;
            arr.forEach(function (x) {
                callback(i++, x);
            });
        }
    }]);

    return X_;
}();
