function AnalsHeightLandScapeView(viewer, magoInstance) {
    const LSHeightViewAnalsWidget = function() {
        this._ele = '#lsHeightViewAnalsWidget'
    }

    LSHeightViewAnalsWidget.prototype.genHTML = function() {
        return $(this._ele).html();
    }

    LSHeightViewAnalsWidget.prototype.defaultRender = function () {
        const templateHtml = Handlebars.compile(this.genHTML());
        $('#lsHeightViewContent').append(templateHtml());
    }

    const LSHeightDropDownList = function() {
        this._ele = '.lsHeightAnalsList';
        this.items = [];
    }

    LSHeightDropDownList.prototype.init = function() {
        this.evt();
    }

    LSHeightDropDownList.prototype.evt = function() {
        $(this._ele).change(function(value) {
            lsHeightAnals.analsHeightView($(this).val());
        });
    }

    LSHeightDropDownList.prototype.render = function(maxHeight) {
        $(this._ele).empty();
        const domItems = this.genItem(maxHeight);
        $(this._ele).append(domItems);
    }
    LSHeightDropDownList.prototype.genItem = function(maxHeight) {
        let dom = "<option value='0'>"+"비교조망점"+"</option>";
        for(let index = 1; index < maxHeight+1; index++) {
            dom += "<option value='"+index+"'>" + index + "층"+ "</option>";
        }
        return dom;
    }

    const LSHeightViewAnals = function() {
        this._xyz1 = {};
        this._xyz2 = {};
    }

    /**
     * 초기
     */
    LSHeightViewAnals.prototype.init = function(){
        this.setEventHandler();

        //
        console.log('LsAnalsAutoObj', '<<.init');
    };

    /**
     * 이벤트 등록
     */
    LSHeightViewAnals.prototype.setEventHandler = function(){
        let that = this;
        console.log(Ppui.find('#landscapeViewAnalsBtn'));

        //분석 버튼 클릭
        // Ppui.click('#landscapeViewAnalsBtn', function(){
        //     _this.doAnals();
        // });

        //여러점 선택 버튼 클릭
        Ppui.click('#lsHeightBtn', function(){
            let el = this;
            toastr.info('지도상에서 경관점을 클릭하시기 바랍니다.');
            that.createTwoPoint();
        });
    };

    LSHeightViewAnals.prototype.createTwoPoint = function() {
        let _this = this;

        //
        _this._xyz1 = {};
        _this._xyz2 = {};

        //
        Ppmap.removeAll();


        //
        const handler = new Cesium.ScreenSpaceEventHandler(MAGO3D_INSTANCE.getViewer().scene.canvas);
        //
        handler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);
        handler.removeInputAction(Cesium.ScreenSpaceEventType.MOUSE_MOVE);

        //
        Ppmap.setCursor('pointer');

        //마우스 왼쪽 클릭 이벤트 등록
        handler.setInputAction( function(click) {

                //점1 세팅
                if(Pp.isEmpty(_this._xyz1.lon)){
                    _this._xyz1 = Ppmap.cartesian2ToLonLatAlt(click.position);
                    //
                    Ppmap.createPointAndAlt('ls-anals-auto-xyz1', _this._xyz1.lon, _this._xyz1.lat, _this._xyz1.alt);
                    //
                    toastr.info('지도상에서 경관방향을 지정해주시길 바랍니다.');
                    return;
                }

                //점2 세팅
                if(Pp.isEmpty(_this._xyz2.lon)){
                    _this._xyz2 = Ppmap.cartesian2ToLonLatAlt(click.position);
                    //
                    Ppmap.createPointAndAlt('ls-anals-auto-xyz2', _this._xyz2.lon, _this._xyz2.lat, _this._xyz2.alt);
                }

                //
                if(Pp.isNotEmpty(_this._xyz1.lon) && Pp.isNotEmpty(_this._xyz2.lon)){
                    //이벤트 삭제
                    handler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);
                    handler.removeInputAction(Cesium.ScreenSpaceEventType.MOUSE_MOVE);

                    //
                    Ppmap.restoreCursor();

                    //분석. 0.5초 지연
                    setTimeout(function(){
                        _this.doAnals();
                        Ppmap.removeAll();
                        toastr.info('높이 데이터를 생성완료 했습니다');
                    }, 500);
                }

            },
            Cesium.ScreenSpaceEventType.LEFT_CLICK
        );


        //마우스 이동
        handler.setInputAction( function(e) {
                //
                let xyz = Ppmap.cartesian2ToLonLat(e.endPosition);
                //console.log(e.endPosition, xyz);

                //
                if(Pp.isEmpty(_this._xyz1.lon) || Pp.isEmpty(xyz.lon)){
                    return;
                }

                //
                Ppmap.removeEntity(window['entity']);

                //
                let entity = MAGO3D_INSTANCE.getViewer().entities.add({
                    polyline: {
                        // This callback updates positions each frame.
                        positions: new Cesium.CallbackProperty(function() {
                            return Cesium.Cartesian3.fromDegreesArray([_this._xyz1.lon, _this._xyz1.lat, xyz.lon, xyz.lat]);
                        }, false),
                        width: 10,
                        clampToGround: true,
                        material: new Cesium.PolylineOutlineMaterialProperty({
                            color: Cesium.Color.YELLOW,
                        })
                    },
                });

                //
                window['entity'] = entity;
            },
            Cesium.ScreenSpaceEventType.MOUSE_MOVE
        );

    }

    /**
     * 분석
     */
    LSHeightViewAnals.prototype.doAnals = function() {
        if(Pp.isEmpty(this._xyz1.lon) || Pp.isEmpty(this._xyz2.lon)){
            toastr.warning('경관점이 선택되지 않았습니다. <br>분석을 취소합니다.');
            return;
        }
 // lon lat to cartesian
        lsHeightAnals.init(this._xyz1, this._xyz2);
        lsHeightAnals.genHeightView(parseInt($('#lsHeightAnalsInput').val()));
    };


    const LSHeightAnals = function() {
        //시작 위치
        this._xyz1 = null;
        //종료 위치
        this._xyz2 = null;
        this.cameraHeight = [];
    }
    LSHeightAnals.prototype.init = function(xyz1, xyz2) {
        //시작 위치
        this._xyz1 = xyz1;
        //종료 위치
        this._xyz2 = xyz2;
    }
    LSHeightAnals.prototype.getCameraHeight = function() {
        return this.cameraHeight;
    }
    LSHeightAnals.prototype.analsHeightView = function(idx) {
        this.cameraHeight[idx-1]();
    }
    /**
     * 높이 층수에 따른 높이 정보를 가진 카메라 뷰를 생성합니다
     * @param maxHeight
     */
    LSHeightAnals.prototype.genHeightView = function(maxHeight) {
        const direction = this.getDirectionByTwoPoint();
        this.cameraHeight = [];
        for(let i = 0; i < maxHeight; i++) {
            this.cameraHeight.push(this.genView(i+1, direction));
        }
        lsHeightDropDown.render(maxHeight);
    }
    LSHeightAnals.prototype.getDirectionByTwoPoint = function() {
        const origin = new Cesium.Cartesian3.fromDegrees(this._xyz1.lon, this._xyz1.lat, this._xyz1.alt);
        const target = new Cesium.Cartesian3.fromDegrees(this._xyz2.lon, this._xyz2.lat, this._xyz2.alt);
        const direction = Cesium.Cartesian3.subtract(target, origin, new Cesium.Cartesian3());
        Cesium.Cartesian3.normalize(direction, direction);
        return direction;
    }
    /**
     *
     * @param height 층수
     * @returns {function(): void}
     */
    LSHeightAnals.prototype.genView = function(height, direction) {
        const long = this._xyz1.lon;
        const lat = this._xyz1.lat;
        const alt = this._xyz1.alt;
        const pos = new Cesium.Cartesian3.fromDegrees(long, lat, alt + (height * 3.5));
        const dirCart3 = new Cesium.Cartesian3(direction.x, direction.y, direction.z);
        return function wrap() {
            viewer.camera.setView({
                destination  : pos,
                orientation : {
                    direction : dirCart3,
                    up : new Cesium.Cartesian3()
                }
            });
        }
    }
    const lsHeightDropDown = new LSHeightDropDownList();
    const lsHeightAnals = new LSHeightAnals();
    const lsViewAnalsWidget = new LSHeightViewAnalsWidget();
    const lsHeightViewAnals =  new LSHeightViewAnals();
    lsViewAnalsWidget.defaultRender();
    lsHeightViewAnals.init();
    $(document).ready(function() {
        let interval = setInterval(function(){
            if(null != Ppui.find(lsHeightDropDown._ele)){
                //
                clearInterval(interval);
                lsHeightDropDown.init();
            }

        }, 500);
    });
}
