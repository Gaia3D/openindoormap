/**
 * 디자인 레이어
 * @author gravity
 * @since 20200910 init
 */
const DesignLayerObj = function(){
    //도시 그룹 목록
    this.urbanGroups = [];
    //디자인 레이어 그룹 목록
    this.groups = [];
    //디자인 레이어 목록
    this.datas = [];
    //선택된 도구
    this.tool = DesignLayerObj.Tool.NONE;
    //intersection한 필지 정보 목록
    this.lands=[];
    //체크(show)된 필지 레이어 정보
    this.landLayer = {};
    //선택된 extrusion 건물
    this.selectedExtrusionBuilding={};

    //선택된 필지
    this.selectedLand = {};
    //선택된 건물
    this.selectedBuilding = {};

    //회전
    this.rotate = null;
    //높이변경
    this.upanddown = null;
    //
    this.handler = null;

    //단지가시화 모달리스
    this.$dialog = null;

    //레이어 on/off 이력
    this.toggleLayerHists = [];
};

/**
 * 도구 종류
 */
DesignLayerObj.Tool = {
    NONE: 0,
    SELECT: 1,
    POINT: 2,
    LINE: 3,
    DELETE: 4,
    MOVE: 5,
    ROTATE: 6,
    UPDOWN: 7,
    INTERSECTION : 8,
    LANDUPDOWN : 9,
    CHKDISTANCE : 10,
};

/**
 * 
 */
DesignLayerObj.GeometryType = {
    LAND: {value:0, text:'land'},
    BUILDING: {value:1, text:'building'},
}


/**
 * 디자인 레이어 그룹 타입
 */
DesignLayerObj.GroupType = {
	NONE: {text:'none', value:-1},
	LAND: {text:'land', value:0},
	BUILDING: {text:'building', value:1},
	BUILDING_HEIGHT: {text:'building_height', value:2},
};


/**
 * 
 */
DesignLayerObj.OgcType = {
    WMS: {value:0, text:'wms'},
    WFS: {value:1, text:'wfs'},
}

/**
 * 초기
 */
DesignLayerObj.prototype.init = function(){
    this.setEventHandler();

    //
    this.getUrbanGroups();
    this.getDesignLayerGroups();
    this.getDesignLayers();

    //
    let depth1Datas = this.getUrbanGroupsByParent(0);
    this.renderUrbanGroup('#urban-group1', depth1Datas);

    //
    console.log('DesignLayerObj', '<<.init');
};


/**
 * 이벤트 등록
 */
DesignLayerObj.prototype.setEventHandler = function(){
    let _this = this;


    /**
     * 전체 초기화 클릭
     */
    $('button.reset-xxx').click(function(){

        //on만 filter
        let _getOnLayers = function(){
            let arr=[];
            for(let i=0; i<_this.toggleLayerHists.length; i++){
                let d = _this.toggleLayerHists[i];
                // console.log(d);
                if(d.isShow){
                    arr.push(d);
                }
            }
    
            //
            // console.log(arr);
            return arr;
        };


        /**
         * last -> first 중복제거
         * @param {Array<Object>}
         * @returns {Array<Object>}
         */ 
        let _deDupl = function(arr){
            for(let i=arr.length-1; i>=1; i--){
                let d1 = arr[i];
    
                for(let j=i-1; j>=0; j--){
                    let d2 = arr[j];
    
                    //
                    if(null === d2.designLayerId){
                        continue;
                    }
    
                    //
                    if(d1.designLayerId == d2.designLayerId){
                        d2.designLayerId = null;
                    }
                }
            }
            // console.log(arr);
            //
            return arr;
        };

        /**
         * 현재 체크된 레이어 목록 조회    
         * @returns {Array<String>} 현재 on중인 레이어 아이디 목록
         */ 
        let _getCheckedLayers = function(){
            let designLayerIds = [];
            $('input[name=design-layer-id]:checked').each(function(i,item){
                designLayerIds.push(item.value);
            });
            // console.log('designLayerIds', designLayerIds);
            //
            return designLayerIds;
        }; 


        /**
         * 모든 레이어 off
         * @param {Array<String>} designLayerIds 현재 on중인 레이어 아이디 목록
         * @param {void}
         */ 
        let _offLayers = function(designLayerIds){
            for(let i=0; i<designLayerIds.length; i++){
                let designLayerId = designLayerIds[i];
    
                
                $('input[name=design-layer-id][value='+designLayerId+']').closest('td')
                    .trigger('click');
            }
        };

        /**
         * 순서대로 레이어 on
         * @param {Array<Object>} arr 이력 목록
         * @param {Array<String>} designLayerIds 현재 on중인 레이어 아이디 목록
         * @param {void}
         */
        let _onLayers = function(arr, designLayerIds){
            for(let i=0; i<arr.length; i++){
                let d = arr[i];
    
                if(null == d.designLayerId){
                    continue;
                }
    
                let b = false;
                for(let j=0; j<designLayerIds.length; j++){
                    let designLayerId = designLayerIds[j];
    
                    //
                    if(d.designLayerId == designLayerId){
                        b=true;
                        break;
                    }
                }
    
                //
                if(b){
                    //해당 레이어 체크박스 클릭 이벤트 트리거 실행
                    $('input[name=design-layer-id][value='+d.designLayerId+']').closest('td')
                        .trigger('click');
                }    
            }
        };

        //
        let arr = _getOnLayers();
        //
        arr = _deDupl(arr);
        //
        let designLayerIds = _getCheckedLayers();
        //
        _offLayers(designLayerIds);
        //
        _onLayers(arr, designLayerIds);

        _this.setTool(DesignLayerObj.Tool.NONE);
    });



    /**
     * 건물 높이 change
     */
    Ppui.change('input.ds-design-layer-building-floor-co', function(){
        //console.log(this.value);

        if(Pp.isEmpty(_this.selectedExtrusionBuilding)){
            return;
        }

        //건물 높이 수정
        let h = _this.toHeight(parseInt(this.value));
        _this.selectedExtrusionBuilding.setHeight(h);

        //
        _this.xxx();
    });


    /**
     * 도시 그룹1 change
     */
    Ppui.change('#urban-group1', function(){
        //
        let urbanGroupId = this.value;
        if(Pp.isEmpty(urbanGroupId)){
            _this.renderUrbanGroup('#urban-group2', []);
            return;
        }

        //
        let datas = _this.getUrbanGroupsByParent(urbanGroupId);
        //도시 그룹2 렌더링
        _this.renderUrbanGroup('#urban-group2', datas);
    });

    /**
     * 도시 그룹2 change
     */
    Ppui.change('#urban-group2', function(){
        //해당 지역으로 flyto
        let _flyTo = function(urbanGroupId){
            if(Pp.isEmpty(urbanGroupId)){
                $('#mapCtrlHome').trigger('click');
                return;
            }

            //
            let urbanGroup = _this.getUrbanGroup(urbanGroupId);
            Ppmap.getManager().flyTo(urbanGroup.longitude, urbanGroup.latitude, urbanGroup.altitude, 1);
        };

        //관련 레이어 모두 off
        let _offLayer = function(urbanGroupId){
            $('table.design-layers input[name=design-layer-id]:checked').each(function(i,item){
                //
                _this.showDesignLayer($(item).val(), false);
            });
        };

        //
        let urbanGroupId = this.value;

        // 관련 레이어 모두 off
        _offLayer(urbanGroupId);

        // 툴 변경
        _this.setTool(DesignLayerObj.Tool.NONE);
        
        //
        _this.showUrbanInfo(urbanGroupId);
        
        //지도 이동
        _flyTo(urbanGroupId);
    
        //디자인 레이어 목록 렌더링
        _this.renderDesignLayersByUrbanGroupId(urbanGroupId);
    });


    /**
     * 도구 - 선택
     */
    Ppui.click('[class*=design-layer-tool]', function () {
        //
        let afterTool = Ppui.hasClass(this, 'active') ? ModelerObj.Tool.NONE : _this.getToolByEl(this);
        //
        _this.setTool(DesignLayerObj.Tool.NONE);
        _this.setTool(afterTool);
    });


    /**
     * 전체 필지 높이 up/down
     */
    Ppui.click('.ds-toggle-land-updown', function(){

        /**
         * on된 디자인 레이어와 관련된 정보(디자인 레이어 목록, imageryLayer 목록)
         */
        let _getDatas = function(){
            let coll = Ppui.find('[name=design-layer-id]:checked');
            if(Pp.isEmpty(coll)){
                return [];
            }

            //
            if(coll instanceof Element){
                coll = [coll];
            }

            let json = {};
            for(let i=0; i<coll.length; i++){
                let el = coll[i];
                let designLayerId = el.value;
                let data = _this.getDataById(designLayerId)

                //
                if(null == data){
                    continue;
                }

                //
                if(DesignLayerObj.GroupType.LAND['text']  !== data.designLayerGroupType){
                    //continue;
                }

                if(Pp.isNull(_this.landLayer[designLayerId])){
                    continue;
                }

                //
                json[designLayerId] = {
                    'data': data,
                    'layer': _this.landLayer[designLayerId] /*imageryLayer */,
                }
            }

            //
            return json;
        };


        /**
         * 필지 높이 up/down
         * @param {Object} d {'data':Object, 'layer':ImageryLayer}
         * @param {boolean} isShow
         */
        let _updownFeatures = function (d, isShow) {

            //
            if(!isShow){
                _this.offExtrusionModel(d.data.designLayerId);
               
                //
                return;
            }

            //
            let imageryProvider = d.layer.imageryProvider;
            let layerName = imageryProvider.layers;
            let currentCqlFilter = imageryProvider._resource.queryParameters.cql_filter;

            //feature 정보 요청
            _this.getFeatures({ 'typeNames': layerName, 'cql_filter': currentCqlFilter }, function (e) {
                let entities = e.entities.values;

                //
                for (let i = 0; i < entities.length; i++) {
                    let entity = entities[i];
                    var polygonHierarchy = entity.polygon.hierarchy.getValue().positions;

                    //
                    let h = Pp.nvl(entity.properties._maximum_building_floors._value, null);
                    if (Pp.isEmpty(h)) {
                        //높이값 없음
                        //console.log('empty height', entity);
                        continue;
                    }

                    //필지 height 변경
					let color = new Mago3D.Color.fromHexCode(d.data.layerFillColor);
					color.a = 0.5;
                    var building = Mago3D.ExtrusionBuilding.makeExtrusionBuildingByCartesian3Array(polygonHierarchy.reverse(), h * 3.3, {
                        color: color /*new Mago3D.Color(color.r, color.b, color.b, 0.4)*/
                    });

                    building.type = 'land';
                    building.layerId = entity.id;
                    building.designLayerId = d.data.designLayerId;
                    /**
                     * magoManager에 속한 modeler 인스턴스의 addObject 메소드를 통해 모델 등록, 뒤의 숫자는 데이터가 표출되는 최소 레벨을 의미. 숫자가 낮을수록 멀리서 보임
                     */
                    Ppmap.getManager().modeler.addObject(building, 12);
                }
            });
        }


        //on/off 판단
        let b = this.checked;

        //get on된 디자인 레이어(필지) 목록
        let json = _getDatas();

        //get 각 디자인 레이별 height
        let designLayerIds = Object.keys(json);
		for(let i=0; i<designLayerIds.length; i++){
            let designLayerId = designLayerIds[i];
			let d = json[designLayerId];
             
            //
            _updownFeatures(d, b);
		}


    });
};


/**
 * 도구 설정
 * @param {number} tool 도구
 */
DesignLayerObj.prototype.setTool = function(tool){
    let beforeTool = this.tool;
    //
    this.tool = tool;

    //
    this.toolChanged(beforeTool, this.tool);

};


/**
 * 설정된 도구 조회
 * @returns {DesignLayerObj.Tool}
 */
DesignLayerObj.prototype.getTool = function(){
    return this.tool;
};


/**
 * 현재 설정된 툴이 tool과 동일한지 여부
 * @param {DesignLayerObj.TOOL} tool 
 * @returns {Boolean}
 */
DesignLayerObj.prototype.currentToolIs = function(tool){
    return this.tool === tool;
}


/**
 * tool로 해당 button's 엘리먼트 구하기
 * @param {DesignLayerObj.Tool} tool
 */
DesignLayerObj.prototype.getElByTool = function (tool) {
    let toolName = '';
    let keys = Object.keys(DesignLayerObj.Tool);

    for (let i = 0; i < keys.length; i++) {
        let k = keys[i];

        //
        if (tool == DesignLayerObj.Tool[k]) {
            toolName = k.toLowerCase();
        }
    }


    let coll = Ppui.find('button[class*=design-layer-tool]');
    for (let i = 0; i < coll.length; i++) {
        let el = coll.item(i);

        for (let j = 0; j < el.classList.length; j++) {
            let className = el.classList[j].replace(/design-layer-tool-/gi, '');

            //
            if (toolName === className) {
                return el;
            }
        }
    }

    //
    return;
};


/**
 * el로 tool 구하기
 * @param {Element} el
 * @returns {DesignLayerObj.Tool}
 */
DesignLayerObj.prototype.getToolByEl = function (el) {
    let keys = Object.keys(DesignLayerObj.Tool);

    //
    for (let i = 0; i < keys.length; i++) {
        let k = keys[i];

        for (let j = 0; j < el.classList.length; j++) {
            let className = el.classList[j].replace(/design-layer-tool-/gi, '');

            //
            //console.log(k, className);
            if (className === k.toLowerCase()) {
                return DesignLayerObj.Tool[k];
            }
        }
    }

    //
    return DesignLayerObj.Tool.NONE;
};



/**
 * f4d 선택시 호출되는 콜백함수
 * @param {BrowserEvent} e
 */
DesignLayerObj.prototype.selectedf4dCallback = function (e) {
    console.log('selectedf4d', e);

   

    //
    if (Ppui.hasClass('button.design-layer-tool-delete', 'active')) {
        //TODO 삭제
        return;
    }
};


/**
 * 건물 높이 변경되면 호출되는 콜백함수
 * @param {BrowserEvent} e 
 */
DesignLayerObj.prototype.changeHeightCallback = function(e){
    console.log(e);
};


/**
 * ExtrusionBuilding이 선택 해제되면 호출되는 콜백함수
 * @param {*} json
 */
DesignLayerObj.prototype.deselectedGeneralObjectCallback = function(json){
    //console.log('deselectedGeneralObjectCallback', json);

    //
    let _this = designLayerObj;

    //
    if(_this.currentToolIs(DesignLayerObj.Tool.SELECT)){
        if(Pp.isNotEmpty(_this.selectedBuilding) && json.deselected._guid == _this.selectedBuilding._guid){
            _this.selectedBuilding = null;
        }
    
        //
        _this.showBuildingInfo(_this.selectedBuilding);
    }
}


/**
 * ExtrusionBuilding 선택되면 호출되는 콜백함수
 * @param {*} json
 */
DesignLayerObj.prototype.selectedGeneralObjectCallback = function(json){
    //console.log('selectedGeneralObjectCallback', json);

    //
    let _this = designLayerObj;


    //
    if(_this.currentToolIs(DesignLayerObj.Tool.SELECT)){   
        //
        _this.selectedBuilding = null;
        //
        if(Pp.isNotNull(json.selected) && json.selected instanceof Mago3D.ExtrusionBuilding){
            _this.selectedBuilding = json.selected;
        }

        //
        _this.showBuildingInfo(_this.selectedBuilding);







        // let nodes = Ppmap.getManager().selectionManager.getSelectedGeneralArray();
        // console.log(nodes);
        // //
        // if(Pp.isEmpty(nodes)){
        //     //
        //     designLayerObj.selectedExtrusionBuilding = null;
        //     Ppui.hide('div.ds-design-layer-building-updown');

        //     //건물/필지 정보 모달리스 창 삭제
        //     $('div.design-layer-land-modal').each(function(i,item){
        //         $(item).remove();
        //     });
            

        //     //
        //     return;
        // }

        // //
        // for(let i=0; i<nodes.length; i++){
        //     let d = nodes[i];
            
            
        //     //
        //     if(Pp.isNotEmpty(d['getHeight'])){
        //         designLayerObj.selectedExtrusionBuilding = d;
        //         console.log('selectedExtrusionBuilding', d);

        //         let h = d.getHeight();
        //         // //층수
        //         let floorCo = parseInt(h / HEIGHT_PER_FLOOR);
        //         // console.log(h, floorCo);
        //         // // d.setHeight(100);

        //         //화면에 표시
        //         Ppui.show('div.ds-design-layer-building-updown');
        //         Ppui.find('input.ds-design-layer-building-floor-co').value = floorCo;

        //         //
        //         designLayerObj.xxx();
        //     }
        // }
        
    }
};


/**
 * left up시 호출되는 콜백함수
 * @param {BrowserEvent} e 
 */
DesignLayerObj.prototype.leftupCallback = function(e){
    console.log('leftup', e);

    //
    if(designLayerObj.currentToolIs(DesignLayerObj.Tool.SELECT)){
        //필지정보표시
        designLayerObj.showLandInfo(e);
    }
};


/**
 * 도구 변경시 호출됨   
 * @param {number} beforeTool 변경 전 도구
 * @param {number} afterTool 변경 후 도구
 */
DesignLayerObj.prototype.toolChanged = function (beforeTool, afterTool) {
    console.log(this.getToolName(beforeTool), '=>', this.getToolName(afterTool));

    //
    Ppui.hide('div.design-layer-land-wrapper');

    //모달리스 창 삭제
    $('div.design-layer-land-modal').each(function(i,item){
        $(item).remove();
    });

    //
    Ppui.removeClass(this.getElByTool(beforeTool), 'active');
    Ppui.addClass(this.getElByTool(afterTool), 'active');


    //건물 높이 extrusion 삭제
    let _clearExtrusionLands = function(){
        let selectionManager = Ppmap.getManager().selectionManager;
        let modeler = Ppmap.getManager().modeler;
        let lands = modeler.getObjectByKV('type', 'land');

        if(Pp.isEmpty(lands)){
            return;
        }

        for(let i=0; i<lands.length; i++){
            modeler.removeObject(lands[i]);
        }

        //
        selectionManager.clearCurrents();
        Ppmap.getManager().defaultSelectInteraction.clear();
    };

    //모든 이벤트/정보 클리어
    if(this.currentToolIs(DesignLayerObj.Tool.NONE)){
        //
        this.setSelectionInteraction(false);
        //
        Ppmap.getManager().defaultTranslateInteraction.setActive(false);
        //
        if(Pp.isNotNull(this.upanddown)){
            this.upanddown.setActive(false);
        }
        //
        if(Pp.isNotNull(this.rotate)){
            this.rotate.setActive(false);
        }

        //
        if (Pp.isNotNull(this.handler) && !this.handler.isDestroyed()) {
            this.handler.destroy();
        }

        //
        Ppui.hide('div.ds-design-layer-building-updown');

        //close 모달리스 창 
        $('div.design-layer-modeless-wrapper').dialog('close');


        //
        //_clearExtrusionLands();
    }

    //선택
    if(this.currentToolIs(DesignLayerObj.Tool.SELECT)){
        this.processToolSelect();
    }

    //삭제
    if(this.currentToolIs(DesignLayerObj.Tool.DELETE)){
        this.processToolDelete();
    }
    
    //이동
    if(this.currentToolIs(DesignLayerObj.Tool.MOVE)){
        this.processToolMove();
    }
    
    //회전
    if(this.currentToolIs(DesignLayerObj.Tool.ROTATE)){
        this.processToolRotate();
    }
    
    //높이조절
    if(this.currentToolIs(DesignLayerObj.Tool.UPDOWN)){
        this.processToolUpdown();
    }
    
    //필지정보조회
    if(this.currentToolIs(DesignLayerObj.Tool.INTERSECTION)){
        this.processToolIntersection();
    }

    //필지높이조절
    if(this.currentToolIs(DesignLayerObj.Tool.LANDUPDOWN)){
        this.processToolLandUpdown();
    }

    //이격거리체크
    if(this.currentToolIs(DesignLayerObj.Tool.CHKDISTANCE)){
        this.processChkDistance();
    }

};


/**
 * 지도에서 객체 선택 상태로 할지 말지 
 * @param {boolean} onOff 
 */
DesignLayerObj.prototype.setSelectionInteraction = function(onOff){
    if(onOff){
        //
        Ppmap.getManager().defaultSelectInteraction.setTargetType('native');
        Ppmap.getManager().defaultSelectInteraction.setActive(onOff);
        Ppmap.getManager().defaultSelectInteraction.setFilter(function(m) {
        	return !m.hasOwnProperty('designLayerId');
        });
        Ppmap.getManager().isCameraMoved = true;
    }else{
        Ppmap.getManager().defaultSelectInteraction.setActive(onOff);
        Ppmap.getManager().defaultSelectInteraction.setFilter(undefined);
        Ppmap.getManager().off(Mago3D.MagoManager.EVENT_TYPE.SELECTEDF4D, this.selectedf4dCallback);
        Ppmap.getManager().off(Mago3D.MagoManager.EVENT_TYPE.LEFTUP, this.leftupCallback);
        Ppmap.getManager().off(Mago3D.MagoManager.EVENT_TYPE.CHANGEHEIGHT, this.changeHeightCallback);
        Ppmap.getManager().off(Mago3D.MagoManager.EVENT_TYPE.SELECTEDGENERALOBJECT, this.selectedGeneralObjectCallback);
        Ppmap.getManager().off(Mago3D.MagoManager.EVENT_TYPE.DESELECTEDGENERALOBJECT, this.deselectedGeneralObjectCallback);
    }
};


/**
 * toggle extrusion model(building)
 * @param {Object} d {'data':object, 'layer':ImageryLayer}
 * @param {Boolean} isShow
 */
DesignLayerObj.prototype.toggleExtrusionBuilding = function(d, isShow){
    
    //높이 값 구하기
    //designLayerGroupType에 따라 property 속성이 다름
    let _height = function(data, entity){
        let h = null;

        //필지
        if(DesignLayerObj.GroupType.LAND['text'] == data.designLayerGroupType){
            h = Pp.nvl(entity.properties._maximum_building_floors.getValue(), null);
        }
        //건축물 높이
        if(DesignLayerObj.GroupType.BUILDING_HEIGHT['text'] == data.designLayerGroupType){
            h = Pp.nvl(entity.properties.build_maximum_floors.getValue(), null);
        }

        //
        return Pp.isEmpty(h) ? null : (h * HEIGHT_PER_FLOOR);
    };

    
    /**
     * get 색
     * 용도지역에 따른 색 리턴
     * @param {Object} data
     * @param {Entity} entity
     */ 
    let _color = function(data, entity){

        if(DesignLayerObj.GroupType.BUILDING_HEIGHT['text'] == data.designLayerGroupType){
            return data.layerFillColor;
        }
        
        let color={};
        color['가스공급설비'] = '#df6fc3';
        color['경관녹지'] = '#81fe02';
        color['공공녹지'] = '#a5dd00';
        color['공공청사'] = '#0080ff';
        color['공동주택(아파트)'] = '#febd00';
        color['공동주택(연립)'] = '#fee07e';
        color['공영차고지'] = '#e3e3e3';
        color['공장용지'] = '#de7fff';
        color['광장'] = '#dec171';
        color['근린공원'] = '#00de01';
        color['근린생활시설용지'] = '#fefa03';
        color['농업관련용지'] = '#bdfe7c';
        color['단독주택'] = '#ffff81';
        color['도로'] = '#ffffff';
        color['도서관'] = '#7e9fff';
        color['도시지원용지'] = '#00a5db';
        color['문화시설'] = '#7e9fff';
        color['보행자전용도로'] = '#d4a617';
        color['복합용지'] = '#ff809e';
        color['사회복지시설'] = '#7e9fff';
        color['상업용지'] = '#fd0002';
        color['수도용지'] = '#01dddd';
        color['시장'] = '#fe0002';
        color['어린이공원'] = '#00de01';
        color['업무시설'] = '#0080ff';
        color['연결녹지'] = '#81fe02';
        color['열공급설비'] = '#df6fc3';
        color['완충녹지'] = '#81fe02';
        color['운동장'] = '#00ba89';
        color['유원지'] = '#baff10';
        color['유통업무설비'] = '#ff00be';
        color['자동차정류장'] = '#de6d89';
        color['재활용회수시설'] = '#c1dd6f';
        color['저류지'] = '#7fe0ff';
        color['전기공급설비'] = '#df6fc3';
        color['종교용지'] = '#ff80ff';
        color['종합의료시설'] = '#7fbffd';
        color['주유소'] = '#dda46f';
        color['주제공원'] = '#00de01';
        color['주차장'] = '#c8c8c8';
        color['준주거용지'] = '#fefa03';
        color['체육공원'] = '#00de01';
        color['체육시설용지'] = '#6fdca3';
        color['폐기물처리시설'] = '#df6fc3';
        color['하수도시설'] = '#01dddd';
        color['하천'] = '#00c0fe';
        color['학교'] = '#01fffd';

        let landuseZoning = entity._properties._landuse_zoning._value;

        if(Pp.isNotEmpty(landuseZoning)){
            let c = color[landuseZoning];
            if(Pp.isEmpty(c)){
                console.log(landuseZoning);
                return '#efefef';
            }else{
                return c;
            }
        }

        //
        return '#efefef';
    };


    //
    if(!isShow){
        this.offExtrusionModel(d.data.designLayerId);
        this.offLimitInfo();
        //
        return;
    }

	//
    let imageryProvider = d.layer.imageryProvider;
    let layerName = imageryProvider.layers;
    let currentCqlFilter = imageryProvider._resource.queryParameters.cql_filter;

    let _this = this;
    startLoading();
    //feature 정보 요청
    this.getFeatures({ 'typeNames': layerName, 'cql_filter': currentCqlFilter }, function (e) {
        let entities = e.entities.values;
        
        //
        for (let i = 0; i < entities.length; i++) {
            let entity = entities[i];
            var polygonHierarchy = entity.polygon.hierarchy.getValue().positions;

            let h = _height(d.data, entity);
            if(null == h){
                continue;
            }
            
            // let color = new Mago3D.Color.fromHexCode(d.data.layerFillColor);
            let color = new Mago3D.Color.fromHexCode(_color(d.data, entity));
            color.a = 0.5;
            var building = Mago3D.ExtrusionBuilding.makeExtrusionBuildingByCartesian3Array(polygonHierarchy.reverse(), h, {	
                color: color, /*new Mago3D.Color(color.r, color.b, color.b, 0.4)*/
                wireframeColor4 : color
            });

            building.type = d.data.designLayerGroupType;
            building.layerId = entity.id;
            building.designLayerId = d.data.designLayerId;
            
            /**
             * magoManager에 속한 modeler 인스턴스의 addObject 메소드를 통해 모델 등록, 뒤의 숫자는 데이터가 표출되는 최소 레벨을 의미. 숫자가 낮을수록 멀리서 보임
             */
            Ppmap.getManager().modeler.addObject(building, 12);
            
            /**
             * 필지 폴리곤 정보로 건물에 제한 정보 설정
             */ 
            if(entity.properties.landuse_zoning && entity.properties.landuse_zoning.getValue() === '공동주택(아파트)') {
            	polygonHierarchy.pop();
                _this.setLimitInfoByPolygon(polygonHierarchy, h);
            }
        }
        stopLoading();
    });
};
/**
 * 필지 폴리곤 정보로 건물에 제한 정보 설정
 * @param {Array<Cesium.Cartesin3>} polygonHierarchy 영역제한 정보 및 건물 찾기 용도
 * @param {number} h 높이 제한 값
 */
DesignLayerObj.prototype.setLimitInfoByPolygon = function(polygonHierarchy, h) {
	
	let geographicCoordsList = Mago3D.GeographicCoordsList.fromCartesians(polygonHierarchy); 
    let polygon2ds = Mago3D.Polygon2D.makePolygonByGeographicCoordArray(geographicCoordsList.geographicCoordsArray);
    
    let buildings = this.getBuildingsByPolygon2D(polygon2ds);
    
    if(buildings.length === 0) return;
    
    for(let i in buildings) {
    	let building = buildings[i];
    	building.setLimitationGeographicCoords(geographicCoordsList.geographicCoordsArray);
    	building.setLimitationHeight(h+building.terrainHeight);
    }
}

/**
 * 건물 제한 정보 해제, 임시로 건물 전체 해제
 */
DesignLayerObj.prototype.offLimitInfo = function() {
	Ppmap.getManager().modeler.objectsArray.forEach(function(building) {
		building.setLimitationGeographicCoords(undefined);
		building.setLimitationHeight(0);
	});
}

/**
 * selecteImageryLayer로 필지 높이조절하기
 * @param {ImageryLayer} selectedImageryLayer 
 * @param {LonLat} geoCoord 
 */
DesignLayerObj.prototype.extrudeLandByImageryLayer = function(selectedImageryLayer, geoCoord){
    //
    var imagerProvider = selectedImageryLayer.imageryProvider;
    var layerName = imagerProvider.layers;
    var currentCqlFilter = imagerProvider._resource.queryParameters.cql_filter;    

    //
    let opt = {
        'typeNames' : layerName,
        'cql_filter': currentCqlFilter + ' AND ' + 'CONTAINS(the_geom, POINT(' + geoCoord.longitude + ' ' + geoCoord.latitude + '))',
    };

    //
    this.getFeatures(opt, function(e){
        var entities = e.entities.values;
        //
        if(Pp.isEmpty(entities)){
            console.log('empty entities');
            return;
        }

        //
        for(let i=0; i<entities.length; i++) {
            var entity = entities[i];

            //
            let h = Pp.nvl(entity.properties._maximum_building_floors._value, null);
            
            if(Pp.isEmpty(h)){
                //높이값 없음
                //console.log('empty height', entity);
                continue;
            }

            var polygonHierarchy  = entity.polygon.hierarchy.getValue().positions;
            
            /**
             * @class Mago3D.ExtrusionBuilding
             * Polygon geometry과 높이를 이용하여 건물을 생성
             * 
             * Mago3D.ExtrusionBuilding의 static method인 makeExtrusionBuildingByCartesian3Array 함수를 통해 빌딩을 생성,
             * Cesium의 Cartesian3 배열과 높이, 스타일관련 옵션으로 건물 객체 반환
             */
            var building = Mago3D.ExtrusionBuilding.makeExtrusionBuildingByCartesian3Array(polygonHierarchy.reverse(), h*3.3, {
                color : new Mago3D.Color(0.2, 0.7, 0.8, 0.4)
            });
            building.type = 'land';
            building.designLayerId = selectedImageryLayer.layerId;
            /**
             * magoManager에 속한 modeler 인스턴스의 addObject 메소드를 통해 모델 등록, 뒤의 숫자는 데이터가 표출되는 최소 레벨을 의미. 숫자가 낮을수록 멀리서 보임
             */
            Ppmap.getManager().modeler.addObject(building, 12);            
        }
    });
};

DesignLayerObj.prototype.checkDistance = function(ctsn2){
    let viewer = MAGO3D_INSTANCE.getViewer();
    let scene = viewer.scene;
    let pickRay = viewer.camera.getPickRay(ctsn2.position);

}

/**
 * 특정 위치(지도에서 마우스 클릭)의 필지 높이조절
 * @param {Cartesian2} ctsn2 
 */
DesignLayerObj.prototype.extrudeLandByCtsn2 = function(ctsn2){
    let viewer = MAGO3D_INSTANCE.getViewer();
    let scene = viewer.scene;
    let pickRay = viewer.camera.getPickRay(ctsn2.position);

    //
    let selectedImageryLayers = viewer.imageryLayers.pickImageryLayerInRay(pickRay, scene, function (layer) {
        return !layer.isBaseLayer();
    });
    

    //
    if(Pp.isEmpty(selectedImageryLayers)){
        toastr.warning('선택된 필지정보가 없습니다.');
        return;
    }

    //
    var geoCoord = Ppmap.Convert.ctsn2ToLonLat(ctsn2.position);

    //
    for(let i=0; i<selectedImageryLayers.length; i++){
        let d = selectedImageryLayers[i];

        //
        this.extrudeLandByImageryLayer(d, geoCoord);
    }
};

DesignLayerObj.prototype.processChkDistance = function(){
    //
    if(!this.currentToolIs(DesignLayerObj.Tool.CHKDISTANCE)){
        return;
    }

    //
    let _this = this;


    //
    _this.handler = new Cesium.ScreenSpaceEventHandler(Ppmap.getViewer().scene.canvas);

    //왼쪽 클릭
    _this.handler.setInputAction(function(event){
        //
        _this.checkDistance(event);

    }, Cesium.ScreenSpaceEventType.LEFT_CLICK);

    //오른쪽 클릭
    _this.handler.setInputAction(function(event){
        //
        _this.setTool(DesignLayerObj.Tool.NONE);
    }, Cesium.ScreenSpaceEventType.RIGHT_CLICK);

}

/**
 * 도구 - 필지 높이조절
 */
DesignLayerObj.prototype.processToolLandUpdown = function(){
    //
    if(!this.currentToolIs(DesignLayerObj.Tool.LANDUPDOWN)){
        return;
    }

    //
    let _this = this;

    
    //
    _this.handler = new Cesium.ScreenSpaceEventHandler(Ppmap.getViewer().scene.canvas);

    //왼쪽 클릭
    _this.handler.setInputAction(function(event){
        //
        _this.extrudeLandByCtsn2(event);

    }, Cesium.ScreenSpaceEventType.LEFT_CLICK);

    //오른쪽 클릭
    _this.handler.setInputAction(function(event){        
        //
        _this.setTool(DesignLayerObj.Tool.NONE);
    }, Cesium.ScreenSpaceEventType.RIGHT_CLICK);
};

/**
 * 도구 - 필지정보조회 처리
 */
DesignLayerObj.prototype.processToolIntersection = function() {
    let _getData = function(datas){
        if(Pp.isEmpty(datas)){
            return null;
        }

        //강제로 1st정보만 get
        let designLayerId = datas[0].designLayerId;

        return _this.getDataById(designLayerId);
    };


    //데이터 화면에 표시
    let _showData = function(data){
        Ppui.remove('table.design-layer-land');

        //handlerbars
        let source = $('#design-layer-land-template').html();
        let template = Handlebars.compile(source);

        //
        let html = template({'data': data});
        $('div.design-layer-land').append(html);

    };

        
    /**
     * get land's polygon
     * @param {*} theGeom land의 polygon정보
     * @returns {Array<LonLat>}
     */
    let _getLandPolygon = function(theGeom){
        // const theGeom = res._embedded.designLayerLands[0].theGeom;
        //
        const parseArr = Terraformer.WKT.parse(theGeom);
        const geometryInfo = [];
        const oneLot = parseArr.coordinates[0][0];
        for(let obj of oneLot) {
            geometryInfo.push({
                'longitude': obj[0],
                'latitude': obj[1],
                'altitude': null
            });
        }

        //
        return geometryInfo;
    };


    //
    let _getLandGeometryCallback = function(res){

    };

    //
    let _getBuildingGeometryCallback = function(res){

    };



    //
    if(!this.currentToolIs(DesignLayerObj.Tool.INTERSECTION)){
        return;
    }
    
    //
    let _this = this;
   
    //
    _this.setSelectionInteraction(true);

    Ppmap.getManager().on(Mago3D.MagoManager.EVENT_TYPE.LEFTUP, _this.leftupCallback);
   
    //
    _this.handler = new Cesium.ScreenSpaceEventHandler(Ppmap.getViewer().scene.canvas);

     //왼쪽 클릭
    // _this.handler.setInputAction(function(event){
    //     let lonLat = Ppmap.Convert.ctsn2ToLonLat(event.position);

    //     //
    //     if(Pp.isEmpty(lonLat) || Pp.isEmpty(lonLat.lon)){
    //         console.log('empty lonLat');
    //         return;
    //     }

    //     //request 특정 위치의 필지정보
    //     _this.getGeometryByIntersection(DesignLayerObj.GeometryType.LAND, [lonLat], function(res){
    //         _this.lands=[];
    //         //
    //         if(Pp.isNotEmpty(res._embedded) && Pp.isNotEmpty(res._embedded.designLayerLands)){
    //             _this.lands = res._embedded.designLayerLands;
    //         }

    //         //값 표시
    //         _this.showLandData(1);

    //         //
    //         if(0 == _this.lands.length){
    //             return;
    //         }


        
    //         //get land's polygon 
    //         let geometryInfo = _getLandPolygon(_this.lands[0].theGeom);

    //         //request geometry by intersection of building
    //         _this.getGeometryByIntersection(DesignLayerObj.GeometryType.BUILDING, geometryInfo, function(res){
    //             if(Pp.isEmpty(res._embedded) || Pp.isEmpty(res._embedded.designLayerBuildings)){
    //                 console.log('empty designLayerBuildings');
    //                 return;
    //             }

    //             //
    //             const lotDegreesArray = [];
    //             for(let i=0; i<geometryInfo.length; i++) {
    //                 lotDegreesArray.push(geometryInfo[i].longitude);
    //                 lotDegreesArray.push(geometryInfo[i].latitude);
    //             }
    //             //console.log(lotArr)
    //             const ctsn3sOfLot = Cesium.Cartesian3.fromDegreesArray(lotDegreesArray);

    //             const designLayerBuildings = res._embedded.designLayerBuildings;
    //             const buildsMap = {};
    //             //sum(전체 건물면적)
    //             let sumArea = 0;

    //             //(필지에 속한)빌딩 갯수만큼 루프
    //             for(let building of designLayerBuildings) {
    //                 const buildingCoords = [];
    //                 let multiPolygon = Terraformer.WKT.parse(building.theGeom);
    //                 //좌표 갯수만큼 루프
    //                 for(let obj2 of multiPolygon.coordinates[0][0]) {
    //                     buildingCoords.push(obj2[0]);
    //                     buildingCoords.push(obj2[1]);
    //                 }

    //                 //
    //                 const ctsn3sOfBuilding = Cesium.Cartesian3.fromDegreesArray(buildingCoords);
    //                 buildsMap[building.designLayerBuildingId] = {
    //                     position : ctsn3sOfBuilding,
    //                     area: getArea(ctsn3sOfBuilding),
    //                     height: building.buildFloor
    //                 };
    //                 sumArea += buildsMap[building.designLayerBuildingId].area;
    //                 //console.log(buildsMap)
    //             }

    //             //용적율
    //             const buildFloorAreaParam = [];
    //             for(const p in buildsMap){
    //                 buildFloorAreaParam.push(buildsMap[p].area)
    //             }

    //             //건폐율
    //             const buildConvAreaParam = [];
    //             for(const p in buildsMap){
    //                 buildConvAreaParam.push([buildsMap[p].area, buildsMap[p].height])
    //             }

    //             //
    //             const lotArea = getArea(ctsn3sOfLot);
    //             console.log(lotArea, ctsn3sOfLot);
    //             //결과 화면 표시
    //             $('#nowFloorCov').text(calcFloorCoverage(buildFloorAreaParam, lotArea));//건폐율
    //             $('#nowBuildCov').text(calcBuildCoverage(buildConvAreaParam, lotArea));//용적율

    //             // 필지에 대한 면적을 알고있음..
    //             // 필지에 대한 면적을 구한다
    //             // 빌딩들에 대한 면적을 알고있음..
    //             // 빌딩들에 대한 면적을 구한다.
    //         });

    //     });


    //     //
    //     // let param = {
    //     //     'wkt': null,
    //     //     'type': 'land',
    //     //     'buffer': 0.0001,
    //     //     'maxFeatures': 10,
    //     //     'geometryInfo': [lonLat]
    //     // };

    //     //
    // //    $.ajax({
    // //         url: "/api/geometry/intersection/design-layers",
    // //         type: "POST",
    // //         data: JSON.stringify(param),
	// // 		dataType: 'json',
	// // 		contentType: 'application/json;charset=utf-8'
    // //     }).done(function(res) {
    // //         debugger;
    // //         _this.lands=[];
    // //         //
    // //         if(Pp.isNotEmpty(res._embedded) && Pp.isNotEmpty(res._embedded.designLayerLands)){
    // //             _this.lands = res._embedded.designLayerLands;
    // //         }

    // //         //값 표시
    // //         _this.showLandData(1);
            


    // //         //////////////갓도//////////////////////
    // //        const geometry = res._embedded.designLayerLands[0].theGeom;
    // //        //
    // //        const parseArr = Terraformer.WKT.parse(geometry);
    // //        const geometryInfo = [];
    // //        const oneLot = parseArr.coordinates[0][0];
    // //        for(let obj of oneLot) {
    // //            geometryInfo.push({
    // //                'longitude': obj[0],
    // //                'latitude': obj[1],
    // //                'altitude': null
    // //            });
    // //        }
    // //        let param = {
    // //            'wkt': null,
    // //            'type': 'building',
    // //            'buffer': 0.0001,
    // //            'maxFeatures': 10,
    // //            'geometryInfo': geometryInfo
    // //        };
    // //        $.ajax({
    // //            url: "/api/geometry/intersection/design-layers",
    // //            type: "POST",
    // //            data: JSON.stringify(param),
    // //            dataType: 'json',
    // //            contentType: 'application/json;charset=utf-8'
    // //        }).done(function(res) {
    // //            const lotArr = [];
    // //            for(let obj of oneLot) {
    // //                lotArr.push(obj[0])
    // //                lotArr.push(obj[1])
    // //            }
    // //            //console.log(lotArr)
    // //            const lotCart = Cesium.Cartesian3.fromDegreesArray(lotArr)

    // //            const builds = res._embedded.designLayerBuildings;
    // //            const buildsMap = {};
    // //            let sumArea = 0;
    // //            for(let obj of builds) {
    // //                const buildArr = []
    // //                for(let obj2 of Terraformer.WKT.parse(obj.theGeom).coordinates[0][0]) {
    // //                    buildArr.push(obj2[0])
    // //                    buildArr.push(obj2[1])
    // //                }
    // //                const cart = Cesium.Cartesian3.fromDegreesArray(buildArr);
    // //                buildsMap[obj.designLayerBuildingId] = {
    // //                    position : cart,
    // //                    area: getArea(cart),
    // //                    height: obj.buildFloor
    // //                };
    // //                sumArea += buildsMap[obj.designLayerBuildingId].area;
    // //                //console.log(buildsMap)
    // //            }

    // //            const buildFloorAreaParam = [];
    // //            for(const p in buildsMap){
    // //                buildFloorAreaParam.push(buildsMap[p].area)
    // //            }

    // //            const buildConvAreaParam = [];
    // //            for(const p in buildsMap){
    // //                buildConvAreaParam.push([buildsMap[p].area, buildsMap[p].height])
    // //            }
    // //            const lotArea = getArea(lotCart);
    // //             $('#nowFloorCov').text(calcFloorCoverage(buildFloorAreaParam, lotArea));
    // //             $('#nowBuildCov').text(calcBuildCoverage(buildConvAreaParam, lotArea));

    // //            // 필지에 대한 면적을 알고있음..
    // //            // 필지에 대한 면적을 구한다
    // //            // 빌딩들에 대한 면적을 알고있음..
    // //            // 빌딩들에 대한 면적을 구한다.
    // //        });

	// 	// });
			
    // }, Cesium.ScreenSpaceEventType.LEFT_CLICK);

    //오른쪽 클릭
    _this.handler.setInputAction(function(event){
        //       
        _this.setTool(DesignLayerObj.Tool.NONE);

   }, Cesium.ScreenSpaceEventType.RIGHT_CLICK);
};


/**
 * 필지 정보 표시
 * @param {number} pageNo 페이지 번호 1부터시작
 */
DesignLayerObj.prototype.showLandData = function(pageNo){
    let _this = this;
    //
    $('div.design-layer-land-modal').each(function(i,item){
        $(item).remove();
    });

    //
    if(0 === this.lands.length){
        toastr.info('필지정보가 없습니다.');
        return;
    }

    //handlerbars
    let source = $('#design-layer-land-template').html();
    let template = Handlebars.compile(source);

    //
    Handlebars.registerHelper('getDataName', function(designLayerId){
        if(Pp.isEmpty(designLayerId)){
            return '';
        }
        //
        return _this.getDataById(designLayerId).designLayerName;        
    });

    //
    let html = template({'data': this.lands[pageNo-1]});
    $('body').append(html);
    
    //
    $('div.design-layer-land-modal').dialog({
        autoOpen: false,
		width: 600,
		height: 400,
		modal: true,
		resizable: false,
		title : '필지정보',
		show:{
			'effect':'fade',
			'duration':500
		},
		buttons:{
			'닫기':function(){
				$(this).dialog('close')
			}
		}
    }).dialog('open');
};

/**
 * 도구 - 높이조절 처리
 */
DesignLayerObj.prototype.processToolUpdown = function(){    
    //
    if(!this.currentToolIs(DesignLayerObj.Tool.UPDOWN)){
        return;
    }
    
    //
    let _this = this;
    
    //
    _this.setSelectionInteraction(true);
    /**
	 * 선택된 객체(디자인 레이어)를 마우스로 높낮이 조절하는 기능
	 */
    if(Pp.isNull(_this.upanddown)){
        _this.upanddown = new Mago3D.NativeUpDownInteraction();
        _this.upanddown.on(Mago3D.NativeUpDownInteraction.EVENT_TYPE.CHANGEHEIGHT, _this.changeHeightCallback);
    
        Ppmap.getManager().interactionCollection.add(_this.upanddown);
    }

    _this.upanddown.setActive(true);

    //
    _this.handler = new Cesium.ScreenSpaceEventHandler(Ppmap.getViewer().scene.canvas);

    //left up click event
    Ppmap.getManager().on(Mago3D.MagoManager.EVENT_TYPE.LEFTUP, _this.leftupCallback);
    
    //오른쪽 클릭
    _this.handler.setInputAction(function (event) {
        _this.setTool(DesignLayerObj.Tool.NONE);
   }, Cesium.ScreenSpaceEventType.RIGHT_CLICK);
};

/**
 * 도구 - 회전 처리
 */
DesignLayerObj.prototype.processToolRotate = function(){    
    //
    if(!this.currentToolIs(DesignLayerObj.Tool.ROTATE)){
        return;
    }
    
    //
    let _this = this;
    /**
	 * 선택된 객체를 마우스로 회전시키는 기능
	 */
    if(Pp.isNull(_this.rotate)){
        _this.rotate = new Mago3D.RotateInteraction();
        _this.rotate.setTargetType('native');
        Ppmap.getManager().interactionCollection.add(_this.rotate);
    }

    //
	_this.setSelectionInteraction(true);
	_this.rotate.setActive(true);

    //
    _this.handler = new Cesium.ScreenSpaceEventHandler(Ppmap.getViewer().scene.canvas);


    //오른쪽 클릭
    _this.handler.setInputAction(function (event) {
        _this.setTool(DesignLayerObj.Tool.NONE);
   }, Cesium.ScreenSpaceEventType.RIGHT_CLICK);
};

/**
 * 도구 - 이동 처리
 */
DesignLayerObj.prototype.processToolMove = function(){    
    //
    if(!this.currentToolIs(DesignLayerObj.Tool.MOVE)){
        return;
    }
    
    //
    let _this = this;

    //
	_this.setSelectionInteraction(true);

    Ppmap.getManager().defaultTranslateInteraction.setTargetType('native');
    Ppmap.getManager().defaultTranslateInteraction.setActive(true);

    //
    _this.handler = new Cesium.ScreenSpaceEventHandler(Ppmap.getViewer().scene.canvas);


    //오른쪽 클릭
    _this.handler.setInputAction(function(event){
        _this.setTool(DesignLayerObj.Tool.NONE);
   }, Cesium.ScreenSpaceEventType.RIGHT_CLICK);
};



/**
 * 도구 - 삭제 처리
 */
DesignLayerObj.prototype.processToolDelete = function(){
    //삭제
    //@param {Array<ExtrusionBuilding>}
    let _delete = function(datas){
        if(Pp.isEmpty(datas)){
            return;
        }

        //
        for(let i=0; i<datas.length; i++){
            let d = datas[i];

            //
            Ppmap.getManager().modeler.removeObject(d);
        }

        //
        Ppmap.getManager().selectionManager.clearCurrents();
        Ppmap.getManager().defaultSelectInteraction.clear();
    };



    //
    if(!this.currentToolIs(DesignLayerObj.Tool.DELETE)){
        return;
    }
    
    //
    let _this = this;

    //
	_this.setSelectionInteraction(true);

    //
    _this.handler = new Cesium.ScreenSpaceEventHandler(Ppmap.getViewer().scene.canvas);

     //왼쪽 클릭
    _this.handler.setInputAction(function(event){
        // 선택된 데이터 라이브러리 정보 추출
        let extrusionBuildings = Ppmap.getManager().selectionManager.getSelectedGeneralArray();
        //
        _delete(extrusionBuildings);

   }, Cesium.ScreenSpaceEventType.LEFT_CLICK);

   //오른쪽 클릭
    _this.handler.setInputAction(function(event){
       _this.setTool(DesignLayerObj.Tool.NONE);
   }, Cesium.ScreenSpaceEventType.RIGHT_CLICK);

};


/**
 * 도구 - 선택 처리
 */
DesignLayerObj.prototype.processToolSelect = function(){
    //
    if(!this.currentToolIs(DesignLayerObj.Tool.SELECT)){
        return;
    }

    //
    let _this = this;

    //
    _this.setSelectionInteraction(true);
    //
    Ppmap.getManager().on(Mago3D.MagoManager.EVENT_TYPE.LEFTUP, _this.leftupCallback);
    Ppmap.getManager().on(Mago3D.MagoManager.EVENT_TYPE.SELECTEDGENERALOBJECT, _this.selectedGeneralObjectCallback);
    Ppmap.getManager().on(Mago3D.MagoManager.EVENT_TYPE.DESELECTEDGENERALOBJECT, _this.deselectedGeneralObjectCallback);

    //
    _this.handler = new Cesium.ScreenSpaceEventHandler(Ppmap.getViewer().scene.canvas);

    //왼쪽 클릭
    _this.handler.setInputAction(function(event){
         // 선택된 데이터 라이브러리 정보 추출
         //let nodes = Ppmap.getManager().selectionManager.getSelectedGeneralArray();
         //console.log(nodes);

    }, Cesium.ScreenSpaceEventType.LEFT_CLICK);

    //오른쪽 클릭
    _this.handler.setInputAction(function(event){
        _this.setTool(DesignLayerObj.Tool.NONE);
    }, Cesium.ScreenSpaceEventType.RIGHT_CLICK);
};



DesignLayerObj.prototype.getToolName = function(tool){
    let keys = Object.keys(DesignLayerObj.Tool);

    //
    for(let i=0; i<keys.length; i++){
        let k = keys[i];

        //
        if(DesignLayerObj.Tool[k] === tool){
            return k;
        }
    }

    //
    return '';
};


/**
 * 전체 디자인 레이어 그룹 목록. 동기 호출
 * @returns {Array}
 */
DesignLayerObj.prototype.getDesignLayerGroups = function(){
    let _this = this;

    //
    _this.groups = [];
    Pp.get('../api/design-layer-groups', [], function(res){
        if(Pp.isNotEmpty(res._embedded) && Pp.isNotEmpty(res._embedded.designLayerGroups)){
            _this.groups = res._embedded.designLayerGroups;
        }
        return _this.groups;
    }, {'async':false});
};


/**
 * 전체 디자인 레이어 목록 조회. 동기 호출
 * @returns {Array}
 */
DesignLayerObj.prototype.getDesignLayers = function(){
    let _this = this;

    //
    _this.datas = [];
    Pp.get('../api/design-layers', [], function(res){
        if(Pp.isNotEmpty(res._embedded) && Pp.isNotEmpty(res._embedded.designLayers)){
            _this.datas = res._embedded.designLayers;
        }
        
        var gara = {
    		available: true,
    		cacheAvailable: false,
    		coordinate: "EPSG:4326",
    		description: "",
    		designLayerGroupId: 4,
    		designLayerGroupName: "과천 도시계획 제한",
    		designLayerGroupType: "building_limit_line",
    		designLayerId: 63,
    		designLayerKey: "limit_line",
    		designLayerName: "벽면한계선",
    		designLayerType: null,
    		geometryType: "Polygon",
    		insertDate: "2020-09-17T21:42:25.969804",
    		labelDisplay: null,
    		layerAlphaStyle: 1,
    		layerFillColor: "#000000",
    		layerLineColor: "#000000",
    		layerLineStyle: 1,
    		ogcWebServices: "wfs",
    		sharing: null,
    		styleFileContent: null,
    		updateDate: "2020-09-17T21:45:33.604132",
    		urbanGroupId: 7,
    		userId: "admin",
    		viewOrder: 1,
    		viewZIndex: 0,
    		zindex: 0
        }
        _this.datas.push(gara);
        return _this.datas;
    }, {'async':false});
};


/**
 * 도시 그룹 전체 목록 조회. 동기 호출
 * @returns {Array}
 */
DesignLayerObj.prototype.getUrbanGroups = function(){
    let _this = this;

    _this.urbanGroups = [];
    //
    Pp.get('../api/urban-groups', [], function(res){
        if(Pp.isNotEmpty(res._embedded) && Pp.isNotEmpty(res._embedded.urbanGroups)){
            _this.urbanGroups = res._embedded.urbanGroups;
        }
        return _this.urbanGroups;
    }, {'async':false});
};



/**
 * parent로 도시 그룹 목록 조회
 * @param {string|number} parent 부모
 * @returns {Array}
 */
DesignLayerObj.prototype.getUrbanGroupsByParent = function(parent){
    let arr=[];
    
    //
    for(let i=0; i<this.urbanGroups.length; i++){
        let d = this.urbanGroups[i];

        //
        if(parent == d.parent){
            arr.push(d);
        }
    }

    //
    return arr;
}


/**
 * 도시 그룹 조회
 * @param {string|number} urbanGroupId 도시 그룹 아이디
 */
DesignLayerObj.prototype.getUrbanGroup = function(urbanGroupId){
    for(let i=0; i<this.urbanGroups.length; i++){
        let d = this.urbanGroups[i];

        if(urbanGroupId == d.urbanGroupId){
            return d;
        }
    }

    //
    return null;
}


/**
 * datas를 selector에 표시하기
 * @param {string} selector 셀렉터
 * @param {Array} datas 도시 그룹 데이터 목록
 */
DesignLayerObj.prototype.renderUrbanGroup = function(selector, datas){
	let option = {
		'tkey': 'urbanGroupName',	
		'vkey': 'urbanGroupId',	
        'append': false,	
        'headerText': '선택하세요',
        'headerValue': ''
    };
    
    //
    Ppui.bindDatas(selector, datas, option);
    

    //
    Ppui.trigger(selector, 'change');
};


/**
 * urbanGroupId로 디자인 레이어 목록 조회
 * @param {string|number} urbanGroupId 도시 그룹 아이디
 * @returns {Array}
 */
DesignLayerObj.prototype.getDatasByUrbanGroupId = function(urbanGroupId){
    let arr=[];

    //
    for(let i=0; i<this.datas.length; i++){
        let d = this.datas[i];

        //
        if(urbanGroupId == d.urbanGroupId){
            arr.push(d);
        }
    }

    //
    return arr;
};


/**
 * designLayerId로 데이터 조회
 * @param {string|number} designLayerId 디자인 레이어 아이디
 */
DesignLayerObj.prototype.getDataById = function(designLayerId){
    for(let i=0; i<this.datas.length; i++){
        let d = this.datas[i];

        //
        if(designLayerId == d.designLayerId){
            return d;
        }
    }

    //
    return null;
};


/**
 * urbanGroupId에 해당하는 디자인 레이어만 화면에 표시
 * @param {string|number} urbanGroupId 도시 그룹 아이디
 */
DesignLayerObj.prototype.renderDesignLayersByUrbanGroupId = function(urbanGroupId){
    let _this = this;

    //
    let datas = this.getDatasByUrbanGroupId(urbanGroupId);
    //handlerbars
    let source = $('#design-layer-template').html();
    let template = Handlebars.compile(source);
    
    //
    let html = template({'datas': datas});
    Ppui.find('div.design-layers').innerHTML = html;


	//레이어 td 클릭 이벤트
	$('td.toggle-design-layer').unbind('click')
		.click(function(){
			let $tr = $(this).parent();
			$tr.toggleClass('on');
			let b = $tr.hasClass('on');
			$tr.find('[name=design-layer-id]').prop('checked', b);
			let designLayerId = $tr.find('[name=design-layer-id]').val();
			//
        	_this.showDesignLayer(designLayerId, b);

			//높이 checkbox 처리
			if(b){
                $tr.find('input.toggle-extrusion-model-height')
                    .prop('disabled', false);
				
			}else{
                $tr.find('input.toggle-extrusion-model-height')
                    .prop('disabled', true)
					.prop('checked', false);				
            }
            
            //
            _this.toggleLayerHists.push({'designLayerId': designLayerId, 
                                        'designLayerGroupType': $tr.data('design-layer-group-type'), 
                                        'imageryLayer': _this.landLayer[designLayerId],
                                        'isShow': b});
            //building만 이력에 저장
            // if(DesignLayerObj.GroupType.BUILDING['text'] == $tr.data('design-layer-group-type')){
            //     _this.toggleLayerHists.push({'designLayerId':designLayerId, 'isShow':b});
            // }
        });
        
		
	//높이 checkbox 클릭 이벤트
	$('input.toggle-extrusion-model-height').unbind('click')
		.click(function(){
            let designLayerId = $(this).val();
            let imageryLayer = _this.getImageryLayer(designLayerId);
            let data = _this.getDataById(designLayerId);

			let b = $(this).prop('checked');
            _this.toggleExtrusionBuilding({'data':data, 'layer':imageryLayer}, b);			
		});

};


/**
 * 화면 표시 레이어의 imageryLayer값 구하기
 * @param {number|string} designLayerId
 * @returns {ImageryLayer}
 */
DesignLayerObj.prototype.getImageryLayer = function(designLayerId){
	return this.landLayer[designLayerId];
};

/**
 * 해당 디자인 레이어 on/off
 * @param {string|number} designLayerId 디자인 레이어 아이디
 * @param {boolean} isShow 표시 여부
 */
DesignLayerObj.prototype.showDesignLayer = function(designLayerId, isShow){
    let data = this.getDataById(designLayerId);

    let model = {
        'id': designLayerId,
        'layername': data.designLayerKey,
        'ogctype': data.ogcWebServices,
    };

    //
    if(DesignLayerObj.OgcType.WMS['text'] === model.ogctype){
        this.extrusionModelWMSToggle(model, isShow);
    }

    if(DesignLayerObj.OgcType.WFS['text'] === model.ogctype){
    	if(model.layername === 'limit_line') {
    		this.extrusionGaraLine(model, isShow);
    	} else {
    		this.extrusionModelBuildingToggle(model, isShow);
    	}
    }
};


/**
 * wms요청
 * @see extrusion.js > extrusionModelWMSToggle()
 * @param {object} model 
 * @param {boolean} isShow 화면 표시 여부
 */
DesignLayerObj.prototype.extrusionModelWMSToggle = function(model, isShow){
    let url = [OIM.policy.geoserverDataUrl, OIM.policy.geoserverDataStore, model.ogctype].join('/');
    //
    var imageryLayers = Ppmap.getViewer().imageryLayers;
    if(isShow) {
    	var currentCqlFilter = `design_layer_id=${model.id} AND enable_yn='Y'`;
        var prov = new Cesium.WebMapServiceImageryProvider({
            url : url,
            parameters : {
                transparent : true,
                srs:'EPSG:4326',
                format: "image/png",
                cql_filter : currentCqlFilter,
            },
            layers : model.layername
        });

        var imageryLayer = new Cesium.ImageryLayer(prov/*, {alpha : 0.7}*/);
        imageryLayer.layerId = model.id;
        imageryLayers.add(imageryLayer);

        //show된 레이어 목록
        this.landLayer[model.id] = imageryLayer;
        
        /**
         * wms labeling 
         */
        $.ajax({
			url : `/api/design-layers/${model.id}`,
			type: "GET",
            headers: {"X-Requested-With": "XMLHttpRequest"},
            dataType: "json",
            success: function(json){
            	var req = new Cesium.Resource({
      				url : [OIM.policy.geoserverDataUrl, OIM.policy.geoserverDataStore, 'wfs'].join('/'),
      				queryParameters : {
      					service : 'wfs',
      					version : '1.0.0',
      					request : 'GetFeature',
      					typeNames : model.layername,
      					srsName : 'EPSG:3857',
      					outputFormat : 'application/json',
      					cql_filter : currentCqlFilter
      				}
      			});
            	
            	var viewer = Ppmap.getViewer();
            	var designLayerGroupType = json.designLayerGroupType;
            	startLoading();
            	new Cesium.GeoJsonDataSource().load(req).then(function(e) {
      				var entities = e.entities.values;
      				var ds = new Cesium.CustomDataSource();
      				ds.labelLayerId = model.id;
      				
      				if(designLayerGroupType === 'land') {
      					for(var i in entities) {
               	 			var entity = entities[i];
               	 			var properties = entity.properties;
               	 			
               	 			var cRatio = properties.building_coverage_ratio.getValue();
               	 			var fRatio = properties.floor_area_ratio.getValue();
               	 			var labelText;
               	 			if(!fRatio || !cRatio) {
               	 				labelText = `${properties.landuse_zoning.getValue()}`;
               	 			} else {
               	 				labelText = `${properties.lot_code.getValue()}\n건폐율 : ${cRatio}\n용적률 : ${fRatio}`;
               	 			}
           	 			
               	 			ds.entities.add({
	               	 			position :  _getPolygonEntityBoundingSphereCenter(entity),
	               	 			label :  _defaultLabelOption(labelText)
	          				});
               	 		}
               	 		ds.clustering.enabled = true;
               	 		ds.clustering.pixelRange = 40;
               	 		ds.clustering.minimumClusterSize = 5;
               	 		ds.clustering.clusterPoints = false;
               	 		ds.clustering.clusterBillboards = false; 
               	 		
               	 		ds.clustering.clusterEvent.addEventListener(function (clusteredEntities, cluster, e) {
               	 			if(cluster.label.id.length > 5) {
               	 				cluster.label.show = false;
               	 			} else {
               	 				cluster.label.show = true;
               	 			}
               	        });
      				} else if(designLayerGroupType === 'building_height') {
      					var designLayerName = json.designLayerName;
               	 		for(var i in entities) {
               	 			var entity = entities[i];
               	 			var properties = entity.properties;
               	 			
               	 			var labelText = designLayerName;
               	 			var maxFloor = properties.build_maximum_floors.getValue();
               	 			if(maxFloor) labelText += `\n층수 제한 : ${maxFloor}`;  
               	 			
	               	 		ds.entities.add({
	               	 			position : _getPolygonEntityBoundingSphereCenter(entity),
	               	 			label : _defaultLabelOption(labelText)
	          				});
               	 		}
      				}
      				
      				viewer.dataSources.add(ds);
      			});
            }
		});
        
        function _getPolygonEntityBoundingSphereCenter(cEntity) {
        	if(!cEntity || !(cEntity instanceof Cesium.Entity)) {
        		return;
        	}
        	
        	var positions = cEntity.polygon.hierarchy.getValue().positions;
 			var center = Cesium.BoundingSphere.fromPoints(positions).center;
 			Cesium.Ellipsoid.WGS84.scaleToGeodeticSurface(center, center);
 			
 			return center
        }
        
        function _defaultLabelOption(lText) {
        	return {
   	 			text: lText,
				scale :0.5,
				font: "normal normal bolder 22px Helvetica",
				fillColor: Cesium.Color.BLACK,
				outlineColor: Cesium.Color.WHITE,
				outlineWidth: 1,
				//scaleByDistance : new Cesium.NearFarScalar(500, 1.2, 1200, 0.0),
				heightReference : Cesium.HeightReference.CLAMP_TO_GROUND,
				style: Cesium.LabelStyle.FILL_AND_OUTLINE,
				//translucencyByDistance : new Cesium.NearFarScalar(1200, 1.0, 2000, 0.0),
				distanceDisplayCondition : new Cesium.DistanceDisplayCondition(0.0, 800)
 			}
        }
    } else {
        var target = imageryLayers._layers.filter(function(layer){return layer.layerId === model.id});
        if(target.length === 1)
        {
            imageryLayers.remove(target[0]);
        }

        //
        this.landLayer[model.id] = null;

        //
        this.offExtrusionModel(model.id);
        
        //라벨 제거
        var dataSources = Ppmap.getViewer().dataSources;
		var filter = dataSources._dataSources.filter(function(ds) {
			return ds.labelLayerId  === model.id; 
		})[0];
		
		dataSources.remove(filter, true);
    }
};



/**
 * extrusion model(필지 바닥정보로 높이를 올린 디자인 레이어,빌딩) 지도에서 삭제하기
 * @param {*} designLayerId 
 */
DesignLayerObj.prototype.offExtrusionModel = function(designLayerId){
    var modeler = Ppmap.getManager().modeler;
        
    var models = modeler.objectsArray;
    if(Pp.isEmpty(models)){
        return;
    }
    //
    for(let i=0; i<models.length; i++){
        let building = models[i];
        if(Pp.isNull(building)){
            continue;
        }

        //console.log(building, designLayerId);
        if(building.designLayerId == designLayerId || building.layerId == designLayerId) {
            modeler.removeObject(building);
        }
    }
};

/**
 * 
 * @param {object} model 
 * @param {bool} isShow
 */
DesignLayerObj.prototype.extrusionModelBuildingToggle = function(model, isShow) {
    let _this = this;


    if(isShow) {
      
        let opt = {
            'typeNames': model.layername,
            'cql_filter': 'design_layer_id=' + model.id
        };
        this.getFeatures(opt, function(e){
        // var loader = new Cesium.GeoJsonDataSource().load(res).then(function(e){
            var entities = e.entities.values;
            var FLOOR_HEIGHT = 3.3;
            
                for(var i in entities) {
                    var entity = entities[i];                    
                    var polygonHierarchy  = entity.polygon.hierarchy.getValue().positions;
                    
                    /**
                     * @class Mago3D.ExtrusionBuilding
                     * Polygon geometry과 높이를 이용하여 건물을 생성
                     * 
                     * Mago3D.ExtrusionBuilding의 static method인 makeExtrusionBuildingByCartesian3Array 함수를 통해 빌딩을 생성,
                     * Cesium의 Cartesian3 배열과 높이, 스타일관련 옵션으로 건물 객체 반환
                     */
                    
                    
                    // let h = parseFloat(entity.properties.build_height._value);
                    // var building = Mago3D.ExtrusionBuilding.makeExtrusionBuildingByCartesian3Array(polygonHierarchy.reverse(), _this.toFloorCo(h))
                    let building = entityToMagoExtrusionBuilding(entity, model.layername);
                    
                    building.layerId = model.id; 
                    building['__originHeight'] = Pp.nvl(building.getHeight(), 0.0);
                    //면적
                    if(Pp.isNotEmpty(entity.properties['build_area'])){
                        building.area = parseFloat(entity.properties['build_area'].getValue());
                    }else{
                        building.area = 0.0;
                    }   
                    //unit type
                    if(Pp.isNotEmpty(entity.properties['build_unit_type'])){
                        building.unitType = entity.properties['build_unit_type'].getValue();
                        //console.log('unittype', building);
                    }else{
                        building.unitType = '0';
                    }
                    //unit count
                    if(Pp.isNotEmpty(entity.properties['build_unit_count'])){
                        building.unitCount = parseInt(entity.properties['build_unit_count'].getValue());
                    }else{
                        building.unitCount = 0;
                    }
                    /**
                     * magoManager에 속한 modeler 인스턴스의 addObject 메소드를 통해 모델 등록, 뒤의 숫자는 데이터가 표출되는 최소 레벨을 의미. 숫자가 낮을수록 멀리서 보임
                     */
                    Ppmap.getManager().modeler.addObject(building, 12);
                }
        });
    } else {
        this.offExtrusionModel(model.id);
        // var modeler = Ppmap.getManager().modeler;
        
        // var models = modeler.objectsArray;
        // if(Pp.isEmpty(models)){
        //     return;
        // }
        // //
        // for(let i=0; i<models.length; i++){
        //     let building = models[i];

        //     if(building.layerId == model.id) {
        //         /**
        //              * modeler 인스턴스의 removeObject 메소드를 통해 모델 삭제
        //              */
        //         modeler.removeObject(building);
        //     }
        // }
    }
}

/**
 * 가라 선 올리기
 * @param {object} model 
 * @param {bool} isShow
 */
DesignLayerObj.prototype.extrusionGaraLine = function(model, isShow) {
    let _this = this;


    if(isShow) {
      
        let opt = {
            'typeNames': model.layername,
            'cql_filter': 'design_layer_id=' + model.id
        };
        //this.getFeatures(opt, function(e){
        var loader = new Cesium.GeoJsonDataSource().load('http://localhost/sample/json/limit_line.geojson').then(function(e){
            var entities = e.entities.values;
            for(var i in entities) {
                var entity = entities[i];       
                
                var polyline = entity.polyline.positions.getValue();
                var properties = entity.properties;
                var maxHeight = properties.max_height.getValue();
                var gcl = Mago3D.GeographicCoordsList.fromCartesians(polyline);
                
                var manager = Ppmap.getManager();
                var options= {};
                options.doubleFace = true;
                var resultRenderableObject = gcl.getExtrudedWallRenderableObject(parseFloat(maxHeight) * 3.3 , undefined, manager, undefined, options, undefined);
                resultRenderableObject.layerId = model.id;
                resultRenderableObject.type = 'land';
                resultRenderableObject.setDirty(false);
                resultRenderableObject.color4 = new Mago3D.Color(0, 170/ 255, 224 / 255, 0.8);
                resultRenderableObject.options = {};
                resultRenderableObject.options.renderWireframe = true;
                
                resultRenderableObject.makeMesh = function(){
                	this.setDirty(false);
                	this.validTerrainHeight();
                	return true;
                }
                
                manager.modeler.addObject(resultRenderableObject, 10);
            }
        });
    } else {
        this.offExtrusionModel(model.id);
        // var modeler = Ppmap.getManager().modeler;
        
        // var models = modeler.objectsArray;
        // if(Pp.isEmpty(models)){
        //     return;
        // }
        // //
        // for(let i=0; i<models.length; i++){
        //     let building = models[i];

        //     if(building.layerId == model.id) {
        //         /**
        //              * modeler 인스턴스의 removeObject 메소드를 통해 모델 삭제
        //              */
        //         modeler.removeObject(building);
        //     }
        // }
    }
}


/**
 * geoserver에 요청
 * @param {Object} option {'url':string, 'typeNames':string, 'cql_filter':string}
 * @param {Function} callbackFn 콜백함수
 */
DesignLayerObj.prototype.getFeatures = function(option, callbackFn){
    let opt = Pp.extend({'url':[OIM.policy.geoserverDataUrl, OIM.policy.geoserverDataStore, 'wfs'].join('/')}, option);

    //
    let queryParameters = {
        serivice: 'wfs',
        version: '1.0.0',
        request: 'GetFeature',
        srsName: 'EPSG:3857',
        outputFormat: 'application/json',
    };
    if(Pp.isNotEmpty(opt.typeNames)){
        queryParameters.typeNames = opt.typeNames;
    }
    if(Pp.isNotEmpty(opt.cql_filter)){
        queryParameters.cql_filter = opt.cql_filter;
    }

    //
    let req = new Cesium.Resource({
        url : opt.url,
        queryParameters : queryParameters
    });

    //
    new Cesium.GeoJsonDataSource.load(req).then(function(e){
        callbackFn(e);
    });

};


/**
 * 인터섹션으로 geometry 구하기
 * @param {GeometryType} geometryType
 * @param {Array} geometryInfo
 * @param {Function} callbackFn
 * @param {Object} option {'async':Boolean}
 */
DesignLayerObj.prototype.getGeometryByIntersection = function(geometryType, geometryInfo, callbackFn, option) {
      //
      let data = {
        'wkt': null,
        'type': geometryType.text,
        'buffer': 0.0001,
        'maxFeatures': 10,
        'geometryInfo': geometryInfo
    };

    //
    let opt = Pp.extend({'async':true}, option);

    //
    let result = '';

    //
   $.ajax({
        url: "/api/geometry/intersection/design-layers",
        type: "POST",
        data: JSON.stringify(data),
        dataType: 'json',
        contentType: 'application/json;charset=utf-8',
        async: opt.async,
        success : function(res){
            //비동기 호출이면
            if(!opt.async){
                result = res;
            }else{
                //
                callbackFn(res);        
            }
    
        }
    });

    //
    return result;
};


/**
 * 필지에 속한 건물 목록 조회
 * @param {Array<LonLat>} lonLats 필지의 LonLat 목록
 */
DesignLayerObj.prototype.getBuildingsAtLand = function(lonLats){
    let res = this.getGeometryByIntersection(DesignLayerObj.GeometryType.BUILDING, lonLats, null, {'async':false});
    // console.log(res);

    //
    if(Pp.isAnyEmpty([res, res._embedded, res._embedded.designLayerBuildings])){
        return [];
    }

    //    
    return res._embedded.designLayerBuildings;
};


/**
 * 선택된 건물로부터...
 *  1. get 건물 polygon
 *  2. request 필지 feature by 1
 *  3. 필지 넓이 계산
 *  4. request 건물 feature 목록 by 2
 *  5. 건물 바닥 넓이 계산
 *  6. get 4와 매핑되는 지도위 extrusionBuilding
 *  7. 건물 넓이(높이/3.3 * 바닥넓이) 계산
 *  8. 건폐율, 용적율 계산
 * TODO 성능향상은 나중에
 */
DesignLayerObj.prototype.xxx = function(){
    let _this = this;

    /**
     * 선택된 건물의 중심 좌표
     * @returns {LonLat}
     */
    let _getCenterLonLat = function(){
        let lonLats = _this.selectedExtrusionBuilding.geographicCoordList.geographicCoordsArray;

        return Ppmap.getCenterLonLatByLonLats(lonLats);
    }



    /**
     * 전체 건물 바닥 면적 목록
     * @param {Array<any>} designLayerBuildings 
     * @requires {Array<number>}
     */
    let _getFloorAreas = function(designLayerBuildings){
        let arr=[];
        //
        for(let i=0; i<designLayerBuildings.length; i++){
            arr.push( designLayerBuildings[i].area);
        }

        //
        return arr;
    }

    /**
     * 전체 건물 용적 목록
     * @param {Array<any>} designLayerBuildings 
     * @requires {Array<number>}
     */
    let _getTotFloorAreas = function(designLayerBuildings){
        let arr=[];
        //
        for(let i=0; i<designLayerBuildings.length; i++){
            arr.push( designLayerBuildings[i].floorCo * designLayerBuildings[i].area);
        }

        //
        return arr;
    };


    /**
     * designLayerLand 구하기. lotArea에 값이 존재하는 1st 데이터 리턴
     * @param {Object} res api호출해서 받은 데이터
     */
    let _getDesignLayerLand = function(res){
        for(let i=0; i<res._embedded.designLayerLands.length; i++){
            let d = res._embedded.designLayerLands[i];
            //
            if(Pp.isNotEmpty(d.lotArea)){
                return d;
            }
        }

        //default로 0번째 데이터 리턴
        return res._embedded.designLayerLands[0];
    }


    /**
     * 모달리스 실행
     * @param {Object} designLayerLand 필지 정보
     * @param {ExtrusionBuilding} selectedExtrusionBuilding 선택된 건물
     * @param {Number} buildingCoverageRatio 건폐율
     * @param {Number} floorAreaRatio 용적률
     */
    let _showModeless = function(designLayerLand, selectedExtrusionBuilding, buildingCoverageRatio, floorAreaRatio){
        let $div = $('div.design-layer-land-modal');

        //
        let left=null, top=null;
        if(0 < $div.closest('div.ui-dialog').length){
            left = $div.closest('div.ui-dialog').css('left').replace(/px/gi, '');
            top = $div.closest('div.ui-dialog').css('top').replace(/px/gi, '');
        }
        $div.each(function(i,item){
            $(item).remove();
        });

        //
        let source = $('#design-layer-land-template').html();
        let template = Handlebars.compile(source);
        let data = designLayerLand;
        data.nowFloorCov = Number.parseFloat(floorAreaRatio).toFixed(2);
        data.nowBuildCov = Number.parseFloat(buildingCoverageRatio).toFixed(2); 
        console.log(data);
        let html = template({'data':data});
        $('body').append(html);

        //
        let option = {
            autoOpen: false,
            width: 600,
            height: 400,
            modal: false,
            resizable: false,
            title : '정보',
            buttons:{
                '닫기':function(){
                    $(this).dialog('close')
                }
            }
        };
        if(Pp.isNotNull(left)){
            option.position = {'my':'left top',
                'at': 'left+' + parseFloat(left) + ' top+' + parseFloat(top),
                'of': window
            };
        }
        //
        $('div.design-layer-land-modal').dialog(option).dialog('open');
    };




    //get 선택된 건물이 속한 필지 정보
    let res = this.getGeometryByIntersection(DesignLayerObj.GeometryType.LAND, [_getCenterLonLat()], null, {'async':false});

    //
    if(Pp.isAnyEmpty([res, res._embedded, res._embedded.designLayerLands])){
        //TODO 데이터 없음
        return;
    }

    //필지. 
    let designLayerLand = _getDesignLayerLand(res);

    //multiPolygon문자열을 LonLat 목록으로 변환
    let landLonLats = Ppmap.Convert.multiPolygonToLonLats(designLayerLand.theGeom);
    // console.log(lonLats);

    //LonLat목록으로 필지 넓이 계산
    let landArea = Ppmap.calcArea(landLonLats, Ppmap.PointType.LONLAT);
    // console.log(landArea);

    //필지에 속한 전체 건물 목록 조회
    let designLayerBuildings = this.getBuildingsAtLand(landLonLats);
    
    //건물별 LonLat목록 저장
    for(let i=0; i<designLayerBuildings.length; i++){
        let lonLats = Ppmap.Convert.multiPolygonToLonLats(designLayerBuildings[i].theGeom);
        designLayerBuildings[i].lonLats = lonLats;
    }
    
    //건물별 바닥면적 저장
    for(let i=0; i<designLayerBuildings.length; i++){
        let area = Ppmap.calcArea(designLayerBuildings[i].lonLats, Ppmap.PointType.LONLAT);
        designLayerBuildings[i].area = area;
    }

    //전체 건물 바닥면적 목록
    let floorAreas = _getFloorAreas(designLayerBuildings);
    //건폐율
    let buildingCoverageRatio = this.calcBuildingCoverageRatio(landArea, floorAreas);
    // console.log('buildingCoverageRatio', buildingCoverageRatio);
    
    //TODO GET 이 건물목록과 매핑되는 지도위 extrusionBuilding 목록. 건물 높이값 구하기 위해
    
    //TODO 건물별 층수 저장. 층수=높이/3.3
    for(let i=0; i<designLayerBuildings.length; i++){
        // let area = Ppmap.calcArea(designLayerBuildings[i].lonLats, Ppmap.PointType.LONLAT);
        designLayerBuildings[i].floorCo = 10;//FIXME
    }
   
    //전체 건물 용적 목록
    let totFloorAreas = _getTotFloorAreas(designLayerBuildings);
    //용적률
    let floorAreaRatio = this.calcFloorAreaRatio(landArea, totFloorAreas);
    // console.log('floorAreaRatio', floorAreaRatio);

    //모달리스 실행
    _showModeless(designLayerLand, this.selectedExtrusionBuilding, buildingCoverageRatio, floorAreaRatio);
};


/**
 * 건폐율 계산
 * @param {Number} landArea 바닥 면적
 * @param {Array<Number>} floorAreas 건물 바닥 면적 목록
 * @returns {Number} 건폐율
 */
DesignLayerObj.prototype.calcBuildingCoverageRatio = function(landArea, floorAreas){
    let tot=0.0;
    //
    for(let i=0; i<floorAreas.length; i++){
        tot += floorAreas[i];
    }
    //
    return tot / landArea * 100;
}


/**
 * 용적률 계산
 * @param {Number} landArea 대지면적
 * @param {Array<Number>} totFloorAreas 건물 용적(바닥면적*층수) 목록
 * @requires {Number} 용적률
 */
DesignLayerObj.prototype.calcFloorAreaRatio = function(landArea, totFloorAreas){
    let tot=0.0;

    //
    if(Pp.isEmpty(totFloorAreas)){
        return tot;
    }


    //
    for(let i=0; i<totFloorAreas.length; i++){
        tot += totFloorAreas[i];
    }

    //
    return (tot / landArea * 100).toFixed(2);
}



/**
 * 지역 정보 모달 표시
 * @param {string|number} urbanGroupId 도시 그룹 아이디
 */
DesignLayerObj.prototype.showUrbanInfo = function(urbanGroupId){
  
    //
    $('div.design-layer-urban-wrapper').hide();
    $('div.design-layer-land-wrapper').hide();
    $('div.design-layer-building-wrapper').hide();


    //
    if(Pp.isEmpty(urbanGroupId)){
        if(Pp.isNotNull(this.$dialog)){
            this.$dialog.dialog('close');
        }
        //
        return;
    }

    //
    if(null == this.$dialog){
        let source = $('#design-layer-modeless-template').html();
        let template = Handlebars.compile(source);
        let html = template({'data':{}});
    
        //
        $('body').append(html);
        
        //
        let left = parseInt($('#mapCtrlWrap').css('left').replace(/px/gi, '')) - 500;
        let top = $('#baseMapToggle').height() + 10;
        
        this.$dialog = $('div.design-layer-modeless-wrapper').dialog({
            autoOpen: false,
            width: 500,
            height: 250,
            position: {my:'center', at:'right top+' + top, of:'.cesium-viewer'},
            resizable: false,
            title : '정보',
            buttons:{
                '닫기':function(){
                    $(this).dialog('close')
                }
            }
        });
    }

    //
      //get 지역 정보
    //calc 전체 세대수
    //calc 전체 평균 평형
    let urbanGroup = this.getUrbanGroup(urbanGroupId);

    let $wrapper = $('div.design-layer-urban-wrapper');
    $wrapper.find('td.urban-group-name').text(urbanGroup.urbanGroupName);

    //
    $wrapper.show();
    this.resizeModelessHeight();
};




/**
 * 필지 정보 표시
 * @param {BrowserEvent}
 */
DesignLayerObj.prototype.showLandInfo = function(browserEvent){
    let _this = this;



    //
    let _getDesignLayerLand = function(res){
        for(let i=0; i<res._embedded.designLayerLands.length; i++){
            let d = res._embedded.designLayerLands[i];
            //
            if(Pp.isNotEmpty(d.lotCode)){
                return d;
            }
        }

        //default로 0번째 데이터 리턴
        return res._embedded.designLayerLands[0];
    }

    //층수 select에 값 바인드, 이벤트 설정
    let _renderFloorCo = function(){
        let $floorCo = $wrapper.find('select.floor-co');
        if(0 === $floorCo.find('option').length){
            let options = '';
            for(let i=0; i<100; i++){
                options += '<option value="'+i+'">'+i+'</option>';
            }
            //select 이벤트 등록
            $floorCo.html(options)
                .unbind('change')
                .change(function(){
                    // 필지내 전체 건물 층수 변경
                    _this.setBuldingHeightByTheGeom(_this.selectedLand.theGeom, $floorCo.val());

                    //필지 정보 
                    _this.renderLandInfo(_this.selectedLand);

                    _this.resizeModelessHeight();
                });
            
            //up 이벤트 등록
            $wrapper.find('a.up-floor-co').unbind('click')
                .click(function(){
                    // select value증가
                    let v = parseInt($wrapper.find('select.floor-co').val()) + 1;
                    $wrapper.find('select.floor-co').val((100<v ? 100 : v));
                    // select change 이벤트 호출
                    $wrapper.find('select.floor-co').trigger('change');
                });
    
            //down 이벤트 등록
            $wrapper.find('a.down-floor-co').unbind('click')
                .click(function(){
                    // select value 감소
                    let v = parseInt($wrapper.find('select.floor-co').val()) - 1;
                    $wrapper.find('select.floor-co').val((0>v ? 0 : v));
                    // select change 이벤트 호출
                    $wrapper.find('select.floor-co').trigger('change');
                });
        }
    }




    //get 필지 정보 by 좌표    
    let geographicCoord = browserEvent.point.geographicCoordinate;
    
    let res = this.getGeometryByIntersection(DesignLayerObj.GeometryType.LAND, [geographicCoord], null, {'async':false});
    
    //
    let isEmpty = Pp.isEmpty(res._embedded) || Pp.isEmpty(res._embedded.designLayerLands);
	this.selectedLand = isEmpty ? null : _getDesignLayerLand(res);
	/*
    //필지 정보 없으면 모달창>필지 hide
    if(isEmpty){
        //
        this.selectedLand = null;
        //
        $('div.design-layer-building-wrapper').hide();
    }else{
        //
        this.selectedLand = _getDesignLayerLand(res);
    }
	*/

	//
	let landVo = this.getLandVoByLand(this.selectedLand);
	this.showLandVo(landVo);
        
    this.resizeModelessHeight();
    
    //
    if(isEmpty){
        return;
    }
    
    //
    let $wrapper = $('div.design-layer-land-wrapper');
        
    //층수 select값 바인드
    _renderFloorCo();
    
    // 값 바인드
    $wrapper.find('td.plan-building-coverage-ratio').text(this.selectedLand.buildingCoverageRatio);
    $wrapper.find('td.plan-floor-area-ratio').text(this.selectedLand.floorAreaRatio);
    $wrapper.find('td.plan-maximum-building-floors').text(this.selectedLand.maximumBuildingFloors);

    this.buildingHeightChanged(this.selectedLand, null);
};



/**

 */
DesignLayerObj.prototype.getLandDataByGeographicCoord = function(geographicCoord){
	

    //
    let _getDesignLayerLand = function(res){
        for(let i=0; i<res._embedded.designLayerLands.length; i++){
            let d = res._embedded.designLayerLands[i];
            //
            if(Pp.isNotEmpty(d.lotCode)){
                return d;
            }
        }

        //default로 0번째 데이터 리턴
        return res._embedded.designLayerLands[0];
    }


	let res = this.getGeometryByIntersection(DesignLayerObj.GeometryType.LAND, [geographicCoord], null, {'async':false});
    
    //
    let isEmpty = Pp.isEmpty(res._embedded) || Pp.isEmpty(res._embedded.designLayerLands);
    //필지 정보 없으면 모달창>필지 hide
    if(isEmpty){
        return {};
    }

	
    //
    let land  = _getDesignLayerLand(res);
    
};



/**
 * 건물 vo 조회
 * @param {ExtrusionBuilding} building
 */
DesignLayerObj.prototype.getBuildingVo = function(building){
	let data={
		'building': building,
		'floorCo0': 0,
		'floorCo1': 0,
		'householdCo0': 0,
		'householdCo1': 0,
		'totalFloorArea0': 0,
		'totalFloorArea1': 0,
		'unitType': '',
		'unionType': '',
	};	
	
	if(!building){
		return data;
	}
	
	
    //층수
    data.floorCo0 = this.toFloorCo(building['__originHeight']);
    data.floorCo1 = this.toFloorCo(building.getHeight());
    //세대수
    data.householdCo0 = building.unitCount * data.floorCo0;
    data.householdCo1 = building.unitCount * data.floorCo1;
    //연면적
    data.totalFloorArea0 = parseInt(building.unitType) * data.householdCo0;
    data.totalFloorArea1 = parseInt(building.unitType) * data.householdCo1;
    //평형
    data.unitType = building.unitType;
    //주동조합
    data.unionType = '2호';

	//
	return data;
};


/**
 * 건물 vo 화면에 표시
 * @param {BuildingVO} vo
 */
DesignLayerObj.prototype.showBuildingVo = function(vo){
	 //
    let $wrapper = $('div.design-layer-building-wrapper');
	$wrapper.show();
	
	if(!vo || !vo.building){
		$wrapper.hide();
		return;
	}

    //
    $wrapper.find('td.floor-co:first').text(vo.floorCo0);
    $wrapper.find('select.floor-co').val(vo.floorCo1);
    //세대수
    $wrapper.find('td.household-co:first').text(Pp.addComma(vo.householdCo0));
    $wrapper.find('td.household-co:last').text(Pp.addComma(vo.householdCo1));
    $wrapper.find('td.household-co:last').removeClass('color-exceed');
	if(vo.householdCo1 > vo.householdCo0){
	    $wrapper.find('td.household-co:last').addClass('color-exceed');
	}
    //연면적
    $wrapper.find('td.total-floor-area:first').text(Pp.addComma(vo.totalFloorArea0));
    $wrapper.find('td.total-floor-area:last').text(Pp.addComma(vo.totalFloorArea1));
    $wrapper.find('td.total-floor-area:last').removeClass('color-exceed');
	if(vo.totalFloorArea1 > vo.totalFloorArea0){
	    $wrapper.find('td.total-floor-area:last').addClass('color-exceed');		
	}
    //
    $wrapper.find('td.unit-type:first').text(vo.unitType);
    //
    $wrapper.find('td.union-type:first').text(vo.unionType);
};


/**
 * 건물정보 표시
 * @param {Mago3D.ExtrusionBuilding} extrusionBuilding 
 */
DesignLayerObj.prototype.showBuildingInfo = function(extrusionBuilding){
    let _this = this;

    //값 표시
    let _render = function(data){
        //층수-기준
        $wrapper.find('td.floor-co').text(data.floorCo0);
        //층수-계획
        $wrapper.find('select.floor-co').val(data.floorCo1);
        
        //세대수-기준
        $wrapper.find('td.household-co:first').text(data.householdCo0);
        //세대수-계획
        $wrapper.find('td.household-co:last').text(data.householdCo1);

        //연면적-기준
        $wrapper.find('td.total-floor-area:first').text(data.totalFloorArea0);
        //연면적-계획
        $wrapper.find('td.total-floor-area:last').text(data.totalFloorArea1);
    };

    //
    let _renderFloorCo = function($wrapper){
        if(0 === $wrapper.find('select.floor-co > option').length){
            let s = '';
            for(let i=0; i<100; i++){
                s += '<option value="'+i+'">'+i+'</option>';
            }
            //
            $wrapper.find('select.floor-co').html(s)
                .unbind('change')
                .change(function(){
                    // 건물 높이 변경
                    let h =  _this.toHeight(parseInt($wrapper.find('select.floor-co').val()));
                    _this.selectedBuilding.setHeight(h);

                    _this.renderBuildingInfo(_this.selectedBuilding);
                });
    
            //UP 이벤트 등록
            $wrapper.find('a.up-floor-co')
                .unbind('click')
                .click(function(){
                    // 층수 증가
                    let v = parseInt($wrapper.find('select.floor-co').val()) + 1;
                    v = (100 < v ? 100 : v);
                    $wrapper.find('select.floor-co').val(v);
                    //
                    $wrapper.find('select.floor-co').trigger('change');
                });
                
            //down 이벤트 등록
            $wrapper.find('a.down-floor-co')
                .unbind('click')
                .click(function(){
                    // 층수 감소
                    let v = parseInt($wrapper.find('select.floor-co').val()) - 1;
                    v = (0 > v ? 0 : v);
                    $wrapper.find('select.floor-co').val(v);
                    //
                    $wrapper.find('select.floor-co').trigger('change');
                });
        }
    };


    let $wrapper = $('div.design-layer-building-wrapper');

    //
    _renderFloorCo($wrapper);
    

    //
    if(Pp.isNull(extrusionBuilding)){
        this.selectedBuilding = null;
        $wrapper.hide();
    }else{
        $wrapper.show();
    }
    this.resizeModelessHeight();
    

    //
    this.renderBuildingInfo(extrusionBuilding);
};




/**
 * 
 * @param {*} landData 
 */
DesignLayerObj.prototype.renderUrbanInfo = function(landData){
    let data={};
    //세대수
	data.householdCo0 = 7162;
    data.householdCo = landData.householdCo + 6800;
    //인구수
	data.populationCo0 = 15232;
    data.populationCo = Math.round(data.householdCo * 2.3);

    //
    let $wrapper = $('div.design-layer-urban-wrapper');
    //
    $wrapper.find('td.household-co:first').text(Pp.addComma(data.householdCo));
    $wrapper.find('td.household-co:first').removeClass('color-exceed');
	if(data.householdCo > data.householdCo0){
	    $wrapper.find('td.household-co:first').addClass('color-exceed');		
	}
    $wrapper.find('td.population-co:first').text(Pp.addComma(data.populationCo));
    $wrapper.find('td.population-co:first').removeClass('color-exceed');
	if(data.populationCo > data.populationCo0){
	    $wrapper.find('td.population-co:first').addClass('color-exceed');
	}
};




/**
 * land object를 화면 표시용 json으로 생성
 * @param {object} land
 */
DesignLayerObj.prototype.getLandVoByLand = function(land){
	let _this = this;

    //
    let _landArea = function(land){
        //multiPolygon문자열을 LonLat 목록으로 변환
        let landLonLats = Ppmap.Convert.theGeomStringToLonLats(land.theGeom);

        //LonLat목록으로 필지 넓이 계산
        return Ppmap.calcArea(landLonLats, Ppmap.PointType.LONLAT);
    };


    //건폐율 = sum(건물바닥넓이) / 필지바닥넓이 * 100
    let _buildingCoverageRatio = function(land){
        //multiPolygon문자열을 LonLat 목록으로 변환
        let lonLats = Ppmap.Convert.theGeomStringToLonLats(land.theGeom);

        //필지내 건물 목록
        let buildings = _this.getBuildingsByLonLats(lonLats);

        //
        let totBuildingArea=0;
        for(let i=0; i<buildings.length; i++){
            let d = buildings[i];

            if(Pp.isEmpty(d.area)){
                continue;
            }

            totBuildingArea += d.area;
        }

        //
        return totBuildingArea / _landArea(land) * 100;
    };

    //세대수 = unit_count * 층수
    let _householdCo = function(land){
        //multiPolygon문자열을 LonLat 목록으로 변환
        let lonLats = Ppmap.Convert.theGeomStringToLonLats(land.theGeom);

        //필지내 건물 목록
        let buildings = _this.getBuildingsByLonLats(lonLats);

        //
        let co=0;
        for(let i=0; i<buildings.length; i++){
            let d = buildings[i];

            //
            co += (d.unitCount * _this.toFloorCo(d.getHeight()));
        }

        //
        return co;
    };

    //용적률 = sum(건물바닥넓이*층수) / 필지넓이 * 100
    let _floorAreaRatio = function(land){
        //multiPolygon문자열을 LonLat 목록으로 변환
        let lonLats = Ppmap.Convert.theGeomStringToLonLats(land.theGeom);

        //필지내 건물 목록
        let buildings = _this.getBuildingsByLonLats(lonLats);

         //
         let tot=0;
         for(let i=0; i<buildings.length; i++){
             let d = buildings[i];

             if(Pp.isEmpty(d.area)){
                 continue;
             }
 
             //
             tot += (d.area * _this.toFloorCo(d.getHeight()));
         }

         //
         return tot / _landArea(land) * 100;
    };



	//
	let data={'land': land,
		'landArea':0,
		'lotCode': '',
		'landuseZoning': '',
		'buildingCoverageRatioStandard': 0,
		'buildingCoverageRatioAllowed': 0,
		'buildingCoverageRatio': 0,
		'floorAreaRatioStandard': 0,
		'floorAreaRatioAllowed': 0,
		'floorAreaRatio': 0,
		'householdCo': 0
		};


	if(!land){
		return data;
	}
	
	
    //필지 면적
    data.landArea = _landArea(land);
    //단지명
    data.lotCode = land.lotCode;
    //용도지역지구
    data.landuseZoning = land.landuseZoning;
    //건폐율-기준,허용,변경
    data.buildingCoverageRatioStandard = land.buildingCoverageRatioStandard;
    data.buildingCoverageRatioAllowed = land.buildingCoverageRatio;
    data.buildingCoverageRatio = _buildingCoverageRatio(land);
    //용적률-기준,허용,변경
    data.floorAreaRatioStandard = land.floorAreaRatioMaximum;
    data.floorAreaRatioAllowed = land.floorAreaRatio;
    data.floorAreaRatio = _floorAreaRatio(land);
    //세대수
    data.householdCo = _householdCo(land);

	return data;
}


/**
 * 필지관련 건폐,용적,.... 화면에 표시
 * @param {object} land 필지
 */
DesignLayerObj.prototype.renderLandInfo = function(land){

    let vo= this.getLandVoByLand(land);

    this.showLandVo(vo);

    //
    this.renderUrbanInfo(vo);
};



/**
 * 필지 vo 화면에 표시
 * @param {LandVO} vo
 */
DesignLayerObj.prototype.showLandVo = function(vo){
	
	 //
    let $wrapper = $('div.design-layer-land-wrapper');
	$wrapper.show();


	if(!vo || !vo.land){
		$wrapper.hide();
		return;
	}
	
	
    //
    $wrapper.find('td.building-coverage-ratio:first').text(vo.buildingCoverageRatioStandard);
    $wrapper.find('td.building-coverage-ratio:eq(1)').text(vo.buildingCoverageRatioAllowed);
    $wrapper.find('td.building-coverage-ratio:last').text(vo.buildingCoverageRatio.toFixed(2));
	$wrapper.find('td.building-coverage-ratio:last').removeClass('color-exceed');
	if(vo.buildingCoverageRatio > vo.buildingCoverageRatioAllowed){
		$wrapper.find('td.building-coverage-ratio:last').addClass('color-exceed');		
	}
    //
    $wrapper.find('td.floor-area-ratio:first').text(vo.floorAreaRatioStandard);
    $wrapper.find('td.floor-area-ratio:eq(1)').text(vo.floorAreaRatioAllowed);
    $wrapper.find('td.floor-area-ratio:last').text(vo.floorAreaRatio.toFixed(2));
    $wrapper.find('td.floor-area-ratio:last').removeClass('color-exceed');
	if(vo.floorAreaRatio > vo.floorAreaRatioAllowed){
	    $wrapper.find('td.floor-area-ratio:last').addClass('color-exceed');
	}
    //
    $wrapper.find('td.lot-code').text(vo.lotCode);
    //
    $wrapper.find('td.landuse-zoning').text(vo.landuseZoning);
    //
    $wrapper.find('td.household-co').text(Pp.addComma(vo.householdCo));

};

/**
 * 건물 정보 화면에 표시
 * @param {ExtrusionBuilding} building 건물
 */
DesignLayerObj.prototype.renderBuildingInfo = function(building){
    if(Pp.isEmpty(building)){
        return;
    }

    
    let vo= this.getBuildingVo(building);

    //
    this.showBuildingVo(vo);

    //get 선택된 건물이 속한 필지 정보
    let centerLonLat = building.getCenter();
    let res = this.getGeometryByIntersection(DesignLayerObj.GeometryType.LAND, [centerLonLat], null, {'async':false});


    this.selectedLand = this.getLandObjectFromApiResponse(res);


    this.renderLandInfo(this.selectedLand);
};


/**
 * api 호출 결과에서 land정보 리턴
 * @param {Object} res 
 * @returns {Object}
 */
DesignLayerObj.prototype.getLandObjectFromApiResponse = function(res){
    if(Pp.isEmpty(res)){
        return null;
    }

    if(Pp.isEmpty(res._embedded)){
        return null;
    }

    if(Pp.isEmpty(res._embedded.designLayerLands)){
        return null;
    }

    for(let i=0; i<res._embedded.designLayerLands.length; i++){
        let d = res._embedded.designLayerLands[i];
        if(Pp.isNotEmpty(d.lotCode)){
            return d;
        }
    }

    //
    return res._embedded.designLayerLands[0];
};


/**
 * 특정영역(필지)내의 모든 건물 층수 설정
 * @param {String} theGeom (필지의)multipolygon 문자열
 * @param {number} floorCo 층수
 */
DesignLayerObj.prototype.setBuldingHeightByTheGeom = function(theGeom, floorCo){

    //
    let lonLats = Ppmap.Convert.multiPolygonToLonLats(theGeom);

    //건물들 높이 변경
    this.setBuldingHeightByLonLats(lonLats, floorCo);

    //
    this.buildingHeightChanged(this.selectedLand, null);
};


/**
 * 빌딩 높이 변경 후 호출됨
 * @param {object} selectedLand 필지
 * @param {Mago3D.ExtrusionBuilding|null} selectedBuilding 건물
 */
DesignLayerObj.prototype.buildingHeightChanged = function(selectedLand, selectedBuilding){
    let $wrapper = $('div.design-layer-land-wrapper');

    //필지 > 현재 건폐율 계산&표시
    let buildingCoverageRatio = this.calcBuildingCoverageRatioByLand(selectedLand);
    $wrapper.find('td.now-building-coverage-ratio').text(buildingCoverageRatio);
    
    //필지 > 현재 용적률 계산&표시
    let floorAreaRatio = this.calcFloorAreaRatioByLand(selectedLand);
    $wrapper.find('td.now-floor-area-ratio').text(floorAreaRatio);
    
    //필지 > 현재 최고층수 계산&표시
    let maxh = this.getMaximumFloorCoByLand(selectedLand);
    $wrapper.find('td.now-maximum-building-floors').text(maxh);
    
    //
    this.renderBuildingInfo(selectedBuilding);
    
};



/**
 * 필지의 건폐율 계산
 * @param {object} designLayerLand 필지
 */
DesignLayerObj.prototype.calcBuildingCoverageRatioByLand = function(designLayerLand){
    //필지의 영역
    let lonLats = Ppmap.Convert.theGeomStringToLonLats(designLayerLand.theGeom);
    
    //
    return this.calcBuildingCoverageRatioByLonLats(lonLats);
};


/**
 * 필지의 건폐율 계산
 * @param {Array<LonLat>} lonLats 필지의 영역
 */
DesignLayerObj.prototype.calcBuildingCoverageRatioByLonLats = function(lonLats){
    //필지의 면적
    let landArea = Ppmap.calcArea(lonLats, Ppmap.PointType.LONLAT);
    //필지내 건물 목록
    let buildings = this.getBuildingsByLonLats(lonLats);

    //
    return this.calcBuildingCoverageRatioByBuildings(landArea, buildings);
};


/**
 * 필지의 건폐율 계산. 견폐율=sum(건물바닥면적) / 필지면적 * 100
 * @param {Number} 필지 면적
 * @param {Array<Mago3D.ExtrusionBuilding>} buildings 필지내 건물 목록
 */
DesignLayerObj.prototype.calcBuildingCoverageRatioByBuildings = function(landArea, buildings){
    
    //
    let tot=0.0;

    for(let i=0; i<buildings.length; i++){
        let d = buildings[i];

        //
        tot += d.area;
    }

    //
    return (tot / landArea * 100).toFixed(2);
};

/**
 * 필지의 용적률 계산
 * @param {object} designLayerLand 필지
 */
DesignLayerObj.prototype.calcFloorAreaRatioByLand = function(designLayerLand){
    //필지의 영역
    let lonLats = Ppmap.Convert.theGeomStringToLonLats(designLayerLand.theGeom);
    //필지의 면적
    let landArea = Ppmap.calcArea(lonLats, Ppmap.PointType.LONLAT);
    //필지내 건물 목록
    let buildings = this.getBuildingsByLonLats(lonLats);
    
    //필지내 건물들의 용적률
    return this.calcFloorAreaRatioByBuildings(landArea, buildings);
};



/**
 * 필지내 건물 목록에서 최고 층수 추출
 * @param {object} desingLayerLand (선택된) 필지
 * @requires {Number}
 */
DesignLayerObj.prototype.getMaximumFloorCoByLand = function(designLayerLand){
    //필지의 영역
    let lonLats = Ppmap.Convert.theGeomStringToLonLats(designLayerLand.theGeom);
    //필지내 건물 목록
    let buildings = this.getBuildingsByLonLats(lonLats);

    //
    return this.getMaximumFloorCo(buildings);
};

/**
 * 건물 목록에서 최고 층수 추출
 * @param {Array<Mago3D.ExtrusionBuilding>} buildings 건물 목록
 * @requires {Number}
 */
DesignLayerObj.prototype.getMaximumFloorCo = function(buildings){
    if(Pp.isEmpty(buildings)){
        return 0;
    }

    //
    let max = -999;
    for(let i=0; i<buildings.length; i++){
        let d = buildings[i];
        let h = parseInt(d.getHeight() / HEIGHT_PER_FLOOR);
        if(max < h){
            max = h;
        }
    }

    //
    return max;
};


/**
 * 좌표 목록으로 ExtrusionBuilding 목록 구하기
 * @param {Array<LonLat>} lonLats 좌표 목록
 * @returns {Array<ExtrusionBuilding>}
 */
DesignLayerObj.prototype.getBuildingsByLonLats = function(lonLats){
    //
    let arr = [];
    for(let i=0; i<lonLats.length; i++){
        let d = lonLats[i];

        arr.push(new Mago3D.GeographicCoord(d.lon, d.lat, 0));
    }

    //
    let polygon2ds = Mago3D.Polygon2D.makePolygonByGeographicCoordArray(arr);
    
    //
    return this.getBuildingsByPolygon2D(polygon2ds);
}

/**
 * Mago3D Polygon2D으로 ExtrusionBuilding 목록 구하기
 * @param {Mago3D.Polygon2D} polygon2d 좌표 목록
 * @returns {Array<ExtrusionBuilding>}
 */
DesignLayerObj.prototype.getBuildingsByPolygon2D = function(polygon2d){
    //
    return Ppmap.getManager().frustumVolumeControl.selectionByPolygon2D(polygon2d, 'native', function(model) {
    	return model.hasOwnProperty('type') || model.type === 'land'
    });
}


/**
 * 건물 층수 설정
 * @param {Array<LonLat>} lonLats lonlat 목록
 * @param {number} floorCo 층수
 */
DesignLayerObj.prototype.setBuldingHeightByLonLats = function(lonLats, floorCo){
    
    //
	let buildings = this.getBuildingsByLonLats(lonLats);

    //
    this.setBuldingHeightByBuildings(buildings, floorCo);
};


/**
 * 건물 층수 설정
 * @param {Mago3D.ExtrusionBuilding} buildings 건물 목록
 * @param {number} floorCo 층수
 */
DesignLayerObj.prototype.setBuldingHeightByBuildings = function(buildings, floorCo){
    for(let i=0; i<buildings.length; i++){
        this.setBuldingHeightByBuilding(buildings[i], floorCo);
    }
};


/**
 * 건물 층수 설정
 * @param {Mago3D.ExtrusionBuilding} building 건물
 * @param {number} floorCo 층수
 */
DesignLayerObj.prototype.setBuldingHeightByBuilding = function(building, floorCo){
    if(Pp.isEmpty(building)){
        return;
    }

    //
    building.setHeight(this.toHeight(floorCo));
};


/**
 * 필지의 용적률 계산
 * @param {number} landArea 필지 면적
 * @param {Array<Mago3D.ExtrusionBuilding>} buildings 필지내 건물 목록
 * @returns {Number} 용적률
 */
DesignLayerObj.prototype.calcFloorAreaRatioByBuildings = function(landArea, buildings){

    //
    if(Pp.isEmpty(buildings)){
        return 0.0;
    }

    //
    let totFloorAreas=[];
    for(let i=0; i<buildings.length; i++){
        let d = buildings[i];
        //console.log('DesignLayerObj', 'calcFloorAreaRatioByBuildings', 'TODO 빌딩의 바닥면적 필요(가이아가 제공해주는 값)');
        totFloorAreas.push(this.toFloorCo(d.getHeight()) * d.area);
        //console.log(this.toFloorCo(d.getHeight()), d.area, d.getHeight());
    }

    //
    return this.calcFloorAreaRatio(landArea, totFloorAreas);
}


/**
 * modeless창 height 자동 변경
 */
DesignLayerObj.prototype.resizeModelessHeight = function(){
	let self = this;
	
	setTimeout(function(){
	    let h = 330;
	   
        //필지
        if($('div.design-layer-land-wrapper').is(':visible')){
            h += 300;
        }
        //건물
        if($('div.design-layer-building-wrapper').is(':visible')){
	        h += 250;
	    }
	
	    //
	    if(null == self.$dialog){
	        return;
	    }
	
	    //
	    self.$dialog
	        .dialog('option', {'height':h})
	        .dialog((0==h?'close':'open'));
		
	}, 200);
    
    // console.log(h, Pp.isNotEmpty(this.selectedLand), Pp.isNotEmpty(this.selectedBuilding));
};


/**
 * 건물 높이를 층수로 변환
 * @param {*} buildingHeight 건물 높이(m)
 */
DesignLayerObj.prototype.toFloorCo = function(buildingHeight){
    return Math.round(buildingHeight / HEIGHT_PER_FLOOR);
}


/**
 * 건물 층수를 높이로 변환
 * @param {*} buildingFloorCo 건물 층수
 */
DesignLayerObj.prototype.toHeight = function(buildingFloorCo){
    return buildingFloorCo * HEIGHT_PER_FLOOR;
}



//
let designLayerObj = new DesignLayerObj();
//
window.addEventListener('load', function(){
    let intvl = setInterval(function(){
        if(Pp.isNotNull(MAGO3D_INSTANCE)){
            clearInterval(intvl);
            //
            designLayerObj.init();
        }
    }, 500);
});



const ratioStructure = {

}


// 건폐율 계산 및 view (건축면적 / 대지면적)
function buildingToLandRatioCalc() {
    if (pickedName === "") {
        alert("오브젝트를 먼저 선택해 주시기 바랍니다.");
        return;
    }
    let plottage = parseFloat(allObject[pickedName].plottage); // 대지면적
    let totalFloorArea = parseFloat(allObject[pickedName].totalFloorArea); // 총 건축면적

    if (plottage === 0.0) {
        return;
    }
    let result = (totalFloorArea / plottage) * 100.0;
    // $("#curBuildingToLandRatio").val(result.toFixed(2));
}

// 건폐율 계산
// 건물바닥면적 / 대지면적 * 100
/**
 *
 * @param floorCoverList build floor Area Calc Val List sample => [50, 20, 30, 10]
 * @param lotTargetArea lot floor Area Calc Val List  sample => 50
 * @returns {number}
 */
function calcFloorCoverage(floorCoverList, lotTargetArea) {
    // 각층 바닥 면접의 합
    // 각층 * 바닥 면접
    let sumFllor = 0;
    for(const obj of floorCoverList) {
        sumFllor += obj;
    }
    return parseInt(sumFllor / lotTargetArea * 100);
}

/**
 *
 * @param floorCoverList build floor Area with height Calc Val List sample => [[50, 10] [20, 5], [30, 5], [10, 5]]
 * @param lotTargetArea lot floor Area Calc Val List  sample => 50
 * @returns {number}
 */
function calcBuildCoverage(floorCoverList, lotTargetArea) {
    // 각층 바닥 면접의 합
    // 각층 * 바닥 면접
    let sumFloor = 0;
    for(const obj of floorCoverList) {
        sumFloor += obj[0] * obj[1];
    }
    return parseInt(sumFloor / lotTargetArea * 100);
}

// 모든 빌딩들의 연면적 합
function totalAreaCalc(entityArray) {
    let sum = 0;
    entityArray.forEach(entity => {
        sum += entity.totalBuildingFloorArea;
    });
    return sum;
}


/**
 * 넓이 계산
 * @param {Array<Cartesian3>} positions 
 * @returns {Long}
 */
function getArea(positions) {
    areaInMeters = 0;
    if (positions.length >= 3)
    {
        var points = [];
        for(var i = 0, len = positions.length; i < len; i++)
        {
            var cartographic = Cesium.Cartographic.fromCartesian(positions[i]);
            points.push(new Cesium.Cartesian2(cartographic.longitude, cartographic.latitude));
        }
        if(Cesium.PolygonPipeline.computeWindingOrder2D(points) === Cesium.WindingOrder.CLOCKWISE)
        {
            points.reverse();
        }

        var triangles = Cesium.PolygonPipeline.triangulate(points);

        for(var i = 0, len = triangles.length; i < len; i+=3)
        {
            areaInMeters += calArea(points[triangles[i]], points[triangles[i + 1]], points[triangles[i + 2]]);
        }
    }
    return areaInMeters;
}

/**
 * 삼각형의 넓이 계산
 * @param {*} t1 
 * @param {*} t2 
 * @param {*} t3 
 * @param {*} i 
 * @returns {Long}
 */
function calArea(t1, t2, t3, i) {
    var r = Math.abs(t1.x * (t2.y - t3.y) + t2.x * (t3.y - t1.y) + t3.x * (t1.y - t2.y)) / 2;
    var cartographic = new Cesium.Cartographic((t1.x + t2.x + t3.x) / 3, (t1.y + t2.y + t3.y) / 3);
    var cartesian = _viewer.scene.globe.ellipsoid.cartographicToCartesian(cartographic);
    var magnitude = Cesium.Cartesian3.magnitude(cartesian);
    return r * magnitude * magnitude * Math.cos(cartographic.latitude)
}




const LandObj = function(){
	this.landArea = 0;
	this.lotCode = 0;
	this.landuseZoning = '';
	this.buildingCoverageRatioStandard = 0;
	this.buildingCoverageRatioAllowed = 0;
	this.buildingCoverageRatio = 0;
	this.floorAreaRatioStandard = 0;
	this.floorAreaRatioAllowed = 0;
	this.floorAreaRatio = 0;
	this.householdCo = 0;
	
	this.land = null;	
};


const DesignLayerVo = function(){
	this.data = null;
	this.designLayerId = '';
	this.designLayerName = '';
	this.designLayerGroupType = DesignLayerObj.GroupType.NONE;
	this.imageryLayer = null;
};

/*
LandObj.prototype.setLand = function(land){
	this.land = land;
	
	//
	this.landArea = this.calcLandArea(land);
	this.buildingCoverageRatio = this.calcBuildingCoverageRatio(land);
	

    //건폐율 = sum(건물바닥넓이) / 필지바닥넓이 * 100
    let _buildingCoverageRatio = function(land){
        //multiPolygon문자열을 LonLat 목록으로 변환
        let lonLats = Ppmap.Convert.theGeomStringToLonLats(land.theGeom);

        //필지내 건물 목록
        let buildings = _this.getBuildingsByLonLats(lonLats);

        //
        let totBuildingArea=0;
        for(let i=0; i<buildings.length; i++){
            let d = buildings[i];

            totBuildingArea += d.area;
        }

        //
        return totBuildingArea / _landArea(land) * 100;
    };

    //세대수 = unit_count * 층수
    let _householdCo = function(land){
        //multiPolygon문자열을 LonLat 목록으로 변환
        let lonLats = Ppmap.Convert.theGeomStringToLonLats(land.theGeom);

        //필지내 건물 목록
        let buildings = _this.getBuildingsByLonLats(lonLats);

        //
        let co=0;
        for(let i=0; i<buildings.length; i++){
            let d = buildings[i];

            //
            co += (d.unitCount * _this.toFloorCo(d.getHeight()));
        }

        //
        return co;
    };

    //용적률 = sum(건물바닥넓이*층수) / 필지넓이 * 100
    let _floorAreaRatio = function(land){
        //multiPolygon문자열을 LonLat 목록으로 변환
        let lonLats = Ppmap.Convert.theGeomStringToLonLats(land.theGeom);

        //필지내 건물 목록
        let buildings = _this.getBuildingsByLonLats(lonLats);

         //
         let tot=0;
         for(let i=0; i<buildings.length; i++){
             let d = buildings[i];
 
             //
             tot += (d.area * _this.toFloorCo(d.getHeight()));
         }

         //
         return tot / _landArea(land) * 100;
    };
};

LandObj.prototype.calcLandArea = function(land){
 	//multiPolygon문자열을 LonLat 목록으로 변환
    let landLonLats = Ppmap.Convert.theGeomStringToLonLats(land.theGeom);

    //LonLat목록으로 필지 넓이 계산
    return Ppmap.calcArea(landLonLats, Ppmap.PointType.LONLAT);
};


*/