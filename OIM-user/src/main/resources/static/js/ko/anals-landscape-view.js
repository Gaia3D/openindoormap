function AnalsLandScapeView(viewer, magoInstance) {
    const LSViewAnalsWidget = function() {
        this._ele = '#lsViewAnalsWidget'
    }

    LSViewAnalsWidget.prototype.genHTML = function() {
        return $(this._ele).html();
    }

    LSViewAnalsWidget.prototype.defaultRender = function () {
        const templateHtml = Handlebars.compile(this.genHTML());
        $('#lsViewContent').append(templateHtml());
    }

    const LSViewAnals = function() {
        this._xyzList = [{}];
    }

    /**
     * 초기
     */
    LSViewAnals.prototype.init = function(){
        this.setEventHandler();

        //
        console.log('LsAnalsAutoObj', '<<.init');
    };

    /**
     * 이벤트 등록
     */
    LSViewAnals.prototype.setEventHandler = function(){
        let _this = this;
        console.log(Ppui.find('#landscapeViewAnalsBtn'));

        //분석 버튼 클릭
        // Ppui.click('#landscapeViewAnalsBtn', function(){
        //     _this.doAnals();
        // });

        //여러점 선택 버튼 클릭
        Ppui.click('.ds-create-many-points', function(){
            let el = this;

            //
            Ppmap.resetRotate(function(){
                //
                toastr.info('지도상에서 여러 경관점을 클릭하시기 바랍니다.');
                //
                el.disabled = true;

                //
                _this.createTwoPoints();
            });
        });
    };
    /**
     * 지도위에 2점 생성
     * 사용자가 2점 선택하도록 함
     * 2점 모두 마우스 왼쪽 버튼 1클릭으로 생성
     */
    LSViewAnals.prototype.createTwoPoints = function(){
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
                    _this._xyz1.alt = _this._xyz1.alt + new lsAnalsMoveInputBox().getHeight();
                    //
                    Ppmap.createPointAndAlt('ls-anals-auto-xyz1', _this._xyz1.lon, _this._xyz1.lat, _this._xyz1.alt);
                    //
                    return;
                }

                //점2 세팅
                if(Pp.isEmpty(_this._xyz2.lon)){
                    _this._xyz2 = Ppmap.cartesian2ToLonLatAlt(click.position);
                    _this._xyz2.alt = _this._xyz2.alt + new lsAnalsMoveInputBox().getHeight();
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

                    //
                    // Ppui.find('.ds-create-two-points').disabled = false;

                    //분석. 0.5초 지연
                    setTimeout(function(){
                        _this.doAnals();
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


    };




    const lsViewAnalsWidget = new LSViewAnalsWidget();
    lsViewAnalsWidget.defaultRender();
}
