/**
 * 데이터 라이브러리(모델러)
 * 반드시, pp,ppui,ppmap 로드 후 호출되어야 함
 * @author gravity
 * @since 20200907 init
 */
let ModelerObj = function(){
	//데이터 라이브러리 그룹 목록
	this.dataLibraryGroups = [];
	//데이터 라이브러리 목록
	this.dataLibraries = [];
	//
	this.selectedData = null;
	//
	this.tool = ModelerObj.Tool.NONE;
	//
	this.handler = null;

	//회전 instance
	this.rotate = null;
	
	this.selectByPolygon = null;
	
}

/**
 * 도구
 */
ModelerObj.Tool = {
		'NONE':0,
		'SELECT':1,
		'POINT':2,
		'LINE':3,
		'DELETE':4,
		'MOVE':5,
		'ROTATE':6,
		'SELECTBYPOLYGON':7,
};


/**
 * 
 */
ModelerObj.TargetType = {
	F4D: {'value':0, 'text':'f4d'},
};

/**
 * 초기
 */
ModelerObj.prototype.init = function(){
	
	//
	this.setEventHandler();
	
	//그룹 목록
	this.getGroups();
	this.renderGroups();

	//데이터 목록
	this.getDataLibraries();
	this.renderDatas(1);

	//회전 instance
	this.rotate = new Mago3D.RotateInteraction();
	Ppmap.getManager().interactionCollection.add(this.rotate);
	this.rotate.setTargetType(ModelerObj.TargetType.F4D['text']);

	//영역으로 선택 instance
	this.selectByPolygon = new PolygonDrawer(Ppmap.getViewer(),  this.selectByPolygonCallback);

	//
	this.setTool(ModelerObj.Tool.NONE);
	
	//
	console.log('ModelerObj', '<<.init');
	
};



/**
 * 이벤트 핸들러
 */
ModelerObj.prototype.setEventHandler = function(){
	let _this = this;
	
	//그룹 선택박스 change
	Ppui.change('#data-library-group', function(){
		_this.renderDatas();
	});


	/**
	 * 토글 버튼 클릭
	 */ 
	$('button[class*=ds-tool]').click(function(){
		_this.setTool(ModelerObj.Tool.NONE);

		//
		let b = $(this).hasClass('active')

		//모든 active off
		$('button[class*=ds-tool]').removeClass('active');

		if(b){
			return;
		}

		$(this).addClass('active');
		let tool = _this.getToolByEl(this);

		//
		_this.setTool(tool);
	});

};


/**
 * tool로 해당 button's 엘리먼트 구하기
 * @param {ModelerObj.Tool} tool
 */
ModelerObj.prototype.getElByTool = function (tool) {
	let toolName = '';
	let keys = Object.keys(ModelerObj.Tool);

	for (let i = 0; i < keys.length; i++) {
		let k = keys[i];

		//
		if (tool == ModelerObj.Tool[k]) {
			toolName = k.toLowerCase();
        }
	}


	let coll = Ppui.find('button[class*=ds-tool]');
	for (let i = 0; i < coll.length; i++) {
		let el = coll.item(i);

		for (let j = 0; j < el.classList.length; j++) {
			let className = el.classList[j].replace(/ds-tool-/gi, '');

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
 */
ModelerObj.prototype.getToolByEl = function (el) {
	let keys = Object.keys(ModelerObj.Tool);

	//
	for (let i = 0; i < keys.length; i++) {
		let k = keys[i];

		for (let j = 0; j < el.classList.length; j++) {
			let className = el.classList[j].replace(/ds-tool-/gi, '');

			//
			//console.log(k, className);
			if (className === k.toLowerCase()) {
				return ModelerObj.Tool[k];
            }
        }
    }
	
	//
	return ModelerObj.Tool.NONE;
};



/**
 * setter tool
 * @param {ModelerObj.Tool}
 */
ModelerObj.prototype.setTool = function(tool){
	let beforeTool = this.tool;
	//
	this.tool = tool;

	//
	this.toolChanged(beforeTool, this.tool);
	
};

/**
 * getter tool
 * @returns {ModelerObj.Tool}
 */
 ModelerObj.prototype.getTool = function(){
	return this.tool;
};


ModelerObj.prototype.toolIs = function(tool){
    return this.getTool() === tool;
}



/**
 * dataLibraryId로 데이터 조회
 * @param {string|number} dataLibraryId
 * @returns {object|null}
 *
 */
ModelerObj.prototype.getDataById =function(dataLibraryId){
	
	//
	for(let i=0; i<this.dataLibraries.length; i++){
		let d = this.dataLibraries[i];
		
		//
		if(dataLibraryId == d.dataLibraryId){
			return d;
		}
	}
	
	//
	return null;
};



/**
 * lonLat위치에 데이터 라이브러리 추가(표시)하기
 * @param {LonLat} lonLat
 */
ModelerObj.prototype.showDataLibraryAtMap = function(lonLat){
	 if(Pp.isNull(this.selectedData)){
		 console.log('<<.showDataLibraryAtMap - emtpy data', data);
		 return;
	 }
	 //console.log(lonLat);
	 
	 
	 //
	 if(!Ppmap.getManager().isExistStaticModel(this.selectedData.dataLibraryId)) {
		let model = {};
		model.projectId = this.selectedData.dataLibraryId;
		model.projectFolderName = this.selectedData.dataLibraryPath;
		
		
		//to fix
		model.projectFolderName = model.projectFolderName.split(this.selectedData.dataLibraryKey)[0];
		model.projectFolderName = model.projectFolderName.replace(/\/+$/, '');
		model.buildingFolderName = 'F4D_'+this.selectedData.dataLibraryKey;

		//
		Ppmap.getManager().addStaticModel(model);
	}
	
	//uid는 pk값은 역할을 함. 이 값이 중복되면 해당 데이터는 추가되지 않음
	let uid = Pp.createUid('' + parseInt(Math.random() * 1000));
	//
	Ppmap.getManager().instantiateStaticModel({
		projectId : this.selectedData.dataLibraryId,
		instanceId : uid,
		longitude : lonLat.lon,
		latitude : lonLat.lat,
		height : 0
	});
};


/**
 * 마고 클릭 이벤트 콜백
 * @param {any} e
 */
ModelerObj.prototype.selectedf4dCallback = function (e) {
	//삭제이면
	if (Ppui.hasClass('button.ds-tool-delete', 'active')) {
		var selectionManager = Ppmap.getManager().selectionManager;
		var selected = Ppmap.getManager().selectionManager.getSelectedF4dNode();
		if (Pp.isEmpty(selected)) {
			return;
		}

		//
		MAGO3D_INSTANCE.getF4dController().deleteF4dMember(selected.data.projectId, selected.data.nodeId);
		Ppmap.getManager().defaultSelectInteraction.clear();
		selectionManager.clearCurrents();
		return;
	}

	//회전이면

	//
};


/**
 * 영역으로 선택 후 호출되는 콜백함수
 * @param {Array<Node>} cartesians 
 */
ModelerObj.prototype.selectByPolygonCallback = function(cartesians) {
		
	/**
	 * Cesium Cartesian3의 array를 이욯하여 Mago3D.Polygon2D 객체 생성
	 */
	var polygon2D = Mago3D.Polygon2D.makePolygonByCartesian3Array(cartesians);
	
	var selectionManager = Ppmap.getManager().selectionManager;
	
	/**
	 * selectionManager는 mago3d에서 선택된 데이터들을 관리하는 객체.
	 * selectionByPolygon2D 메소드를 이용하여 영역에 포함된 데이터를 찾을 수 있음.
	 */
	var selected = selectionManager.selectionByPolygon2D(polygon2D, ModelerObj.TargetType.F4D['text']);
	console.log(selected);



	//TODO refactoring. 함수로 생성
	//TODO 선택된 인스턴스의 정보 화면에 표시
	//TODO handlebar 이용
	let s = '';
	for(let i=0; i<selected.length; i++){
		let d = selected[i];
		let data = mobj.getDataById(d.data.projectId);
		s += '<div class="ds-selected-item" data-project-id="'+d.data.projectId+'" data-node-id="'+d.data.nodeId+'">';
		s += '	<span class="mr-10">'+(i+1)+'</span>';
		s += '	<span class="mr-10">'+data.dataLibraryName+'</span>';
		s += '	<a href="javascript:;" class="mr-10 ds-selected-delete">삭제</a>';
		s += '	<a href="javascript:;" class="mr-10 ds-selected-deselect">선택해제</a>';
		s += '</div>';
	}
	$('div.ds-selected-list').html(s);
	//이벤트 등록 - 삭제
	$('.ds-selected-delete').click(function(){
		if(!confirm('삭제하시겠습니까?')){
			return;
		}


		let projectId = $(this).closest('div').data('project-id');
		let nodeId = $(this).closest('div').data('node-id');


		//TODO 삭제
	});

	//이벤트 등록 - 선택해제
	$('.ds-selected-deselect').click(function(){
		let projectId = $(this).closest('div').data('project-id');
		let nodeId = $(this).closest('div').data('node-id');


		//TODO 선택해제
	});


};

/**
 * tool이 변경되면 호출됨
 * @param {ModelerObj.Tool}
 * @returns {void}
 */
ModelerObj.prototype.toolChanged = function (beforeTool, afterTool) {
	console.log(ModelerObj.getToolName(beforeTool), ' => ', ModelerObj.getToolName(afterTool));
    
    //
    if(this.toolIs(ModelerObj.Tool.NONE)){
        //
		Ppmap.getManager().defaultSelectInteraction.setActive(false);
		Ppmap.getManager().defaultTranslateInteraction.setActive(false);
		Ppmap.getManager().off(Mago3D.MagoManager.EVENT_TYPE.SELECTEDF4D, this.selectedf4dCallback);

		if(Pp.isNotEmpty(Ppmap.getManager().selectionManager)){
			Ppmap.getManager().selectionManager.clearCurrents();
		}
		
		//
		if (Pp.isNotNull(this.handler) && !this.handler.isDestroyed()) {
			this.handler.destroy();
		}
		
		this.rotate.setActive(false);
		this.selectByPolygon.setActive(false);
	}

	//
	// Ppui.removeClass(this.getElByTool(beforeTool), 'active');
	// Ppui.addClass(this.getElByTool(afterTool), 'active');

	//
	if (this.toolIs(ModelerObj.Tool.DELETE)) {
		this.processToolDelete();
    }

	//
	if (this.toolIs(ModelerObj.Tool.LINE)) {

		//
		if (0 === Ppui.find('table.ds-data-library-list tr.on').length) {
			toastr.warning('데이터 라이브러리를 선택하시기 바랍니다.');
			this.setTool(ModelerObj.Tool.NONE);
			return;
		}

		//
		this.processToolLine();
	}	

	//
	if (this.toolIs(ModelerObj.Tool.POINT)) {

		//
		if (0 === Ppui.find('table.ds-data-library-list tr.on').length) {
			toastr.warning('데이터 라이브러리를 선택하시기 바랍니다.');
			this.setTool(ModelerObj.Tool.NONE);
			return;
		}
		//
		this.processToolPoint();
	}

	//
	if (this.toolIs(ModelerObj.Tool.SELECT)) {
		this.processToolSelect();
	}

	//
	if (this.toolIs(ModelerObj.Tool.MOVE)) {
		this.processToolMove();
	}

	//
	if(this.toolIs(ModelerObj.Tool.ROTATE)){
		this.processToolRotate();
	}

	//
	if(this.toolIs(ModelerObj.Tool.SELECTBYPOLYGON)){
		this.processToolSelectByPolygon();
	}
};


/**
 * 맵에서 노드 선택이 변경되면 호출됨
 * @param {Node} nodes 선택된 노드들
 */
ModelerObj.prototype.nodeSelected = function(nodes){
    if(Pp.isEmpty(nodes)){
        //
        Ppui.find('.ds-selected-data-library').value = '';
        //
        return;
    }

    //
    for(let i=0; i<nodes.length; i++){
        let d = nodes[i];

        //
        let dataLibraryId = d.data.projectId;
        let dataLibraryKey = d.data.buildingSeed.buildingId.replace(/F4D_/gi, '');

        //
        let data = this.getDataById(dataLibraryId);
        //console.log(data);

        //
        //Ppui.find('.ds-selected-data-library').value = data.dataLibraryName;
    }
};

/**
 *	
 * @param {any} beforeData 변경전 데이터
 * @param {any} afterData 변경후 데이터
 */
ModelerObj.prototype.dataChanged = function(beforeData, afterData){
	//TODO
}


/**
 * tool 이름 리턴
 * @param {ModelerObj.Tool}
 * @returns {string}
 */
ModelerObj.getToolName = function(tool){
	let keys = Object.keys(ModelerObj.Tool);

	//
	for(let i=0; i<keys.length; i++){
		let k = keys[i];
		//
		if(tool === ModelerObj.Tool[k]){
			return k;
		}
	}

	//
	return '';
};




/**
 * 도구 - 이동
 */
ModelerObj.prototype.processToolMove = function(){
	if(!this.toolIs(ModelerObj.Tool.MOVE)){
		return;
	}

	//
	let _this = this;

	//
	Ppmap.getManager().defaultTranslateInteraction.setTargetType(ModelerObj.TargetType.F4D['text']);
	Ppmap.getManager().defaultTranslateInteraction.setActive(true);
	
	//
	Ppmap.getManager().defaultSelectInteraction.setTargetType(ModelerObj.TargetType.F4D['text']);
	Ppmap.getManager().defaultSelectInteraction.setActive(true);
	Ppmap.getManager().isCameraMoved = true;
	

	//
	_this.handler = new Cesium.ScreenSpaceEventHandler(Ppmap.getViewer().scene.canvas);	

	//오른쪽 클릭
	_this.handler.setInputAction(function(){
		if(_this.toolIs(ModelerObj.Tool.MOVE)){
			//
			_this.setTool(ModelerObj.Tool.NONE);
		}
		//console.log('move right clicked');
	}, Cesium.ScreenSpaceEventType.RIGHT_CLICK);
};

/**
 * 도구 - 선택
 */ 
ModelerObj.prototype.processToolSelect = function(){
	if(!this.toolIs(ModelerObj.Tool.SELECT)){
		return;
	}
	//
	let _this = this;

	
	Ppmap.getManager().defaultSelectInteraction.setTargetType(ModelerObj.TargetType.F4D['text']);
	Ppmap.getManager().defaultSelectInteraction.setActive(true);
	Ppmap.getManager().isCameraMoved = true;
	Ppmap.getManager().on(Mago3D.MagoManager.EVENT_TYPE.SELECTEDF4D, _this.selectedf4dCallback);
	

	//
	_this.handler = new Cesium.ScreenSpaceEventHandler(Ppmap.getViewer().scene.canvas);	
	
	////오른쪽 클릭
	_this.handler.setInputAction(function(){
		if (_this.toolIs(ModelerObj.Tool.SELECT)) {
			//
			$('button[class*=ds-tool-select]').trigger('click');
		}
		//console.log('select right clicked');
	}, Cesium.ScreenSpaceEventType.RIGHT_CLICK);
};


/**
 * 도구 - 점
 */
ModelerObj.prototype.processToolPoint = function(){
	if(!this.toolIs(ModelerObj.Tool.POINT)){
		return;
	}


	//
	let _this = this;
	
	//
	_this.handler = new Cesium.ScreenSpaceEventHandler(Ppmap.getViewer().scene.canvas);
	//클릭
	_this.handler.setInputAction(function(event){
		if(_this.toolIs(ModelerObj.Tool.POINT)){
			//
			let lonLat = Ppmap.Convert.ctsn2ToLonLat(event.position);
			
			//데이터 라이브러리 표시
			_this.showDataLibraryAtMap(lonLat);
		}
	}, Cesium.ScreenSpaceEventType.LEFT_CLICK);
	
	//오른쪽 클릭
	_this.handler.setInputAction(function(event){
		if (_this.toolIs(ModelerObj.Tool.POINT)) {
			//
			$('button[class*=ds-tool-point]').trigger('click');
		}
		//console.log('point right clicked');
	}, Cesium.ScreenSpaceEventType.RIGHT_CLICK);
		
};


/**
 * 도구 - 선
 */ 
ModelerObj.prototype.processToolLine = function(){
    //LonLat을 지도에 표시하기 위한 좌표형태로 변환
	let _toDataPositions = function(lonLats){
		let arr=[];

		//
		for(let i=0; i<lonLats.length-1; i++){
			//
			let p1 = {
				'longitude': lonLats[i].lon,
				'latitude': lonLats[i].lat,
				'altitude': 0,
			}
			//
			let p2 = {
				'longitude': lonLats[i+1].lon,
				'latitude': lonLats[i+1].lat,
				'altitude': 0,
			}

            //10m 간격
			let dataPositions = Mago3D.GeographicCoordSegment.getArcInterpolatedGeoCoords(p1, p2, 10);
			//
			arr = arr.concat(dataPositions);			
		}

		//
		return arr;
	};


	if(!this.toolIs(ModelerObj.Tool.LINE)){
		return;
	}
	//
	let _this = this;
	
	//
	let points = [];

	//
	_this.handler = new Cesium.ScreenSpaceEventHandler(Ppmap.getViewer().scene.canvas);

	//클릭
	_this.handler.setInputAction(function(event){
		if(_this.toolIs(ModelerObj.Tool.LINE)){
			//
			let lonLat = Ppmap.Convert.ctsn2ToLonLat(event.position);
			points.push(lonLat);
			
			//
			Ppmap.createPoint('data-library-tool-line-point', lonLat.lon, lonLat.lat);
		}
		
		
	}, Cesium.ScreenSpaceEventType.LEFT_CLICK);
	
	//오른쪽 클릭
	_this.handler.setInputAction(function(event){
			if(_this.toolIs(ModelerObj.Tool.LINE)){
				// 점, 선 삭제
				Ppmap.removeAll();		
				
				//
				let dataPositions = _toDataPositions(points);
				//
				for(let i=0; i<dataPositions.length; i++){
					let d = dataPositions[i];
					// 데이터 라이브러리 표시
					_this.showDataLibraryAtMap(Ppmap.Convert.toLonLat(d.longitude, d.latitude));
				}
		
				//
				$('button[class*=ds-tool-line]').trigger('click');
			}
		//console.log('line right clicked');
	}, Cesium.ScreenSpaceEventType.RIGHT_CLICK);

	//이동
	_this.handler.setInputAction(function(event){
		// 선 그리기
		if(0 == points.length){
			return;
		}


		//
		Ppmap.removeEntity(window['line'+points.length]);

		//
		let lonLat = Ppmap.Convert.ctsn2ToLonLat(event.endPosition);
		//
		if(Pp.isEmpty(lonLat.lon) || Pp.isEmpty(lonLat.lat)){
			return;
		}
		
		let arr=[];
		arr.push(points[points.length-1].lon);
		arr.push(points[points.length-1].lat);
		arr.push(lonLat.lon);
		arr.push(lonLat.lat);
		//console.log(arr);
		
		//
		let entity = MAGO3D_INSTANCE.getViewer().entities.add({
			polyline: {
				// This callback updates positions each frame.
                positions: new Cesium.CallbackProperty(function() {
					return Cesium.Cartesian3.fromDegreesArray(arr);                    
                }, false),
                width: 10,
                clampToGround: true,
                material: new Cesium.PolylineOutlineMaterialProperty({
					color: Cesium.Color.YELLOW,
                })
            },
		});

		//
		Ppmap.removeEntity(window['line'+points.length]);
		window['line'+points.length] = entity;
		
	}, Cesium.ScreenSpaceEventType.MOUSE_MOVE);
};


/**
 * 도구 - 삭제
 */
ModelerObj.prototype.processToolDelete = function(){
	if(!this.toolIs(ModelerObj.Tool.DELETE)){
		return;
	}

	//
	let _this = this;
		
	//
	Ppmap.getManager().defaultSelectInteraction.setTargetType(ModelerObj.TargetType.F4D['text']);
	Ppmap.getManager().defaultSelectInteraction.setActive(true);
	Ppmap.getManager().isCameraMoved = true;
	Ppmap.getManager().on(Mago3D.MagoManager.EVENT_TYPE.SELECTEDF4D, _this.selectedf4dCallback);

	//
	_this.handler = new Cesium.ScreenSpaceEventHandler(Ppmap.getViewer().scene.canvas);
		
	
	//오른쪽 클릭
	_this.handler.setInputAction(function(){
		if (_this.toolIs(ModelerObj.Tool.DELETE)) {
			//
			_this.setTool(ModelerObj.Tool.NONE);
		}
		//console.log('delete right clicked');
	}, Cesium.ScreenSpaceEventType.RIGHT_CLICK);
};



/**
 * 회전
 * @since 20200922 init
 */
ModelerObj.prototype.processToolRotate = function(){
	let _this = this;


	if(!this.toolIs(ModelerObj.Tool.ROTATE)){
		return;
	}

	/**
	 * 선택된 객체를 마우스로 회전시키는 기능
	 */
	this.rotate.setActive(true);
	

	Ppmap.getManager().defaultSelectInteraction.setTargetType(ModelerObj.TargetType.F4D['text']);
	Ppmap.getManager().defaultSelectInteraction.setActive(true);
	Ppmap.getManager().isCameraMoved = true;
	Ppmap.getManager().on(Mago3D.MagoManager.EVENT_TYPE.SELECTEDF4D, _this.selectedf4dCallback);
	


	//
	_this.handler = new Cesium.ScreenSpaceEventHandler(Ppmap.getViewer().scene.canvas);
	//오른쪽 클릭
	_this.handler.setInputAction(function(){
		if (_this.toolIs(ModelerObj.Tool.ROTATE)) {
			//
			$('button[class*=ds-tool-rotate]').trigger('click');
		}
		//console.log('delete right clicked');
	}, Cesium.ScreenSpaceEventType.RIGHT_CLICK);
	
};


/**
 * 영역으로 선택
 */
ModelerObj.prototype.processToolSelectByPolygon = function(){
	if(!this.toolIs(ModelerObj.Tool.SELECTBYPOLYGON)){
		return;
	}

	this.selectByPolygon.setActive(true);

	let _this = this;
	//
	_this.handler = new Cesium.ScreenSpaceEventHandler(Ppmap.getViewer().scene.canvas);
	//오른쪽 클릭
	_this.handler.setInputAction(function(){
		if (_this.toolIs(ModelerObj.Tool.SELECTBYPOLYGON)) {
			//
			$('button[class*=ds-tool-selectbypolygon]').trigger('click');
		}
		//console.log('delete right clicked');
	}, Cesium.ScreenSpaceEventType.RIGHT_CLICK);
};



/**
 * 데이터 라이브러리 > 그룹 목록 로드. 동기호출
 */
ModelerObj.prototype.getGroups = function(){
	let _this = this;
	
	//	
	_this.dataLibraryGroups = [];
	Pp.get('../api/data-library-groups', [], function(res){
		if(Pp.isNotEmpty(res._embedded) && Pp.isNotEmpty(res._embedded.dataLibraryGroups)){
			_this.dataLibraryGroups = res._embedded.dataLibraryGroups;
		}
		//
		return _this.dataLibraryGroups;
		
	}, {'async':false});
};



/**
 * 그룹 선택박스 데이터 바인드
 */
ModelerObj.prototype.renderGroups = function(){
	let option = {
		'tkey': 'dataLibraryGroupName',	
		'vkey': 'dataLibraryGroupId',	
		'headerText': '전체',	
		'headerValue': '',	
		'append': false,	
	};
	
	//
	let datas = [];
	for(let i=0; i<this.dataLibraryGroups.length; i++){
		let d = this.dataLibraryGroups[i];
		
		//depth1만 처리
		if(1 === d.depth){
			datas.push(d);
		}
	}
	
	//
	Ppui.bindDatas('#data-library-group', datas, option);
};


/**
 * 데이터 라이브러리 > 목록 검색
 * @param {number} pageNo
 */
ModelerObj.prototype.renderDatas = function(pageNo){
	let _this = this;
	
	//하위 데이터 그룹 목록 조회
	let _children = function(dataLibraryGroupId, depth){
		//
		if(Pp.isEmpty(dataLibraryGroupId)){
			return _this.dataLibraryGroups;
		}
		
		//
		let arr=[];
		
		//
		for(let i=0; i<_this.dataLibraryGroups.length; i++){
			let d = _this.dataLibraryGroups[i];
			
			if(d.depth <= depth){
				continue;
			}
			
			//
			if(dataLibraryGroupId == d.parent){
				arr.push(d);
				arr = arr.concat(_children(d.dataLibraryGroupId, depth+1));
			}
		}
		
		
		//
		return arr;
	};
	
	//데이터 그룹으로 필터링된 데이터 목록 조회
	let _getDatasByGroup = function(dataLibraryGroupId){
		
		//
		let arr=[];
		
		//
		for(let i=0; i<_this.dataLibraries.length; i++){
			let d = _this.dataLibraries[i];
			
			//
			if(dataLibraryGroupId === d.dataLibraryGroupId){
				arr.push(d);
			}
		}
		
		//
		return arr;
	};
	
	//데이터 그룹 목록으로 필터링된 데이터 목록 조회
	let _getDatasByGroups = function(groups){
		let arr = [];
		
		//
		for(let i=0; i<groups.length; i++){
			let d = groups[i];
			if(Pp.isNull(d)){
				continue;
			}
			
			//
			arr = arr.concat(_getDatasByGroup(d.dataLibraryGroupId));
		}
		
		//
		return arr;
		
	};
	
	//데이터 페이징
	let _paging = function(datas, pageJson){
		let arr=[];
		
		//
		for(let i=pageJson.startIndex; i<=pageJson.endIndex; i++){
			arr.push(datas[i]);
		}
		
		//
		return arr;
	};
	
	//데이터 표시
	let _renderDatas = function(datas){
		//데이터 라이브러리 목록 화면에 표시
		let source = $('#data-library-template').html();
		let template = Handlebars.compile(source);
		
		//
		Handlebars.registerHelper('showGroupName', function(dataLibraryGroupId){
			for(let i=0; i<_this.dataLibraryGroups.length; i++){
				let d = _this.dataLibraryGroups[i];
				//
				if(dataLibraryGroupId == d.dataLibraryGroupId){
					return d.dataLibraryGroupName;
				}
			}
		});
		
		let html = template({'datas': pagedDatas});
		Ppui.find('.ds-modeler-list').innerHTML = html;

		//
		_this.selectedData = null;
	};
	
	//선택된 그룹 하위 모든 그룹 목록 조회
	let dataLibraryGroupId = Ppui.find('#data-library-group').value; 
	let groups = [this.getGroup(dataLibraryGroupId)]; 
	groups = groups.concat(_children(dataLibraryGroupId , 1));
	//그룹에 속하는 모든 데이터 조회
	let datas = _getDatasByGroups(groups);
	//페이징
	let pageJson = Pp.paginate(datas.length, (pageNo?pageNo:1), 10, 5);
	//데이터 페이징
	let pagedDatas = _paging(datas, pageJson);
	//데이터 표시
	_renderDatas(pagedDatas);
	//tr 클릭 이벤트 등록
	Ppui.click('table.ds-data-library-list > tbody > tr', function(){
		let el = Ppui.child(this, '[name=dataLibraryId]');
		el.checked = true;
		//
		_this.selectedData = _this.getDataById(el.value);

		//
		Ppui.removeClass('table.ds-data-library-list > tbody > tr', 'on');
		Ppui.addClass(this, 'on');
	});
	
	
	//페이징 html 표시
	DS.pagination(datas.length, (pageNo?pageNo:1), $('div.ds-modeler .pagination'), function(_pageNo){
		//console.log(_pageNo);
		_this.renderDatas(_pageNo);
	});
		
};


/**
 * 데이터 라이브러리 그룹 조회
 * @param {string|number} dataLibraryGroupId
 * @returns {object}
 */
ModelerObj.prototype.getGroup = function(dataLibraryGroupId){
	for(let i=0; i<this.dataLibraryGroups.length; i++){
		let d = this.dataLibraryGroups[i];
		
		//
		if(dataLibraryGroupId == d.dataLibraryGroupId){
			return d;
		}
	} 
	
	//
	return null;
};


/**
 * 데이터 라이브러리 목록
 */
ModelerObj.prototype.getDataLibraries = function(){
	let _this = this;
	
	//
	_this.dataLibraries = [];
	Pp.get('../api/data-libraries', [], function(res){
		if(Pp.isNotEmpty(res._embedded) && Pp.isNotEmpty(res._embedded.dataLibraries)){
			_this.dataLibraries = res._embedded.dataLibraries;			
		}
		//
		return _this.dataLibraries;
	
	}, {'async':false});
};


//
let mobj = new ModelerObj();

//TODO 모델러(데이터 라이브러리) 메뉴 선택시에 호출되도록 수정해야 함
window.addEventListener('load', function(){
    let intvl = setInterval(function(){
        if(Pp.isNotNull(MAGO3D_INSTANCE)){
            clearInterval(intvl);
            //
            mobj.init();	    
        }
    }, 500);
});
