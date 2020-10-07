const analsSavedEntitiy = {
    point: [],
    line: [],
    removeThis: function() {
        this.point.forEach(p => MAGO3D_INSTANCE.getViewer().entities.remove(p));
        this.line.forEach(p => MAGO3D_INSTANCE.getViewer().entities.remove(p));
    }
}


/**
 * 경관분석 - 분석
 * @param {string} id 경관 아이디
 */
function showData(id) {
	
	//데이터 조회
    $.get(LS_POINT_REST_URL + '/'+id).done(function(diffObj) {
		lsAnalsSavedObj.doAnals(diffObj);
    });
}



/**
 * 경관분석 - 저장된 데이터 기반 분석
 * @author gravity
 * @since 20200908 init
 */
const LsAnalsSavedObj = function(){
	this._xyz1 = {};
	this._xyz2 = {};
	//
	this._cursor = 'default';
};

LsAnalsSavedObj.prototype.init = function(){
	this.setEventHandler();
};

LsAnalsSavedObj.prototype.setEventHandler = function(){
	
};


/**
 * 분석
 */
LsAnalsSavedObj.prototype.doAnals = function(data){
	if(Pp.isEmpty(data)){
		console.log(data);
		alert('관련 정보가 존재하지 않습니다.');
		return;
	}
	
	
	//
	let _this = this;
	
	//heading,pitch,roll이 0이어야 함
	Ppmap.removeAll();		
	//
	if('점' === data.landScapePointType){
		_this._point(data);
	}else{
		_this._line(data);
	}		
	
};


/**
 * 분석
 */
LsAnalsSavedObj.prototype._doAnals = function(){
	let _this = this;
	//0.5초 지연
	setTimeout(function(){
		//
		new SkylineObj().init().process(_this._xyz1, _this._xyz2);		
	}, 500);
};


/**
 * 데이터가 선일 때 처리
 */
LsAnalsSavedObj.prototype._line = function(data){
	//
	this._xyz1 = {
		'lon': data.startLandScapePos.x, 
		'lat': data.startLandScapePos.y
	};
	//
	this._xyz2 = {
		'lon': data.endLandScapePos.x, 
		'lat': data.endLandScapePos.y
	};
	
	//선 생성
	let entity = Ppmap.createPolyline('ls-anals-saved-line', [this._xyz1, this._xyz2]);
	
	//이동
	Ppmap.zoomTo(entity);
	
	//
	this._doAnals();
	
};




/**
 * 데이터가 점일 때 처리
 */
LsAnalsSavedObj.prototype._point = function(data){
	let _this = this;
	
	
	//
	_this._xyz1 = {
		'lon': data.startLandScapePos.x,	
		'lat': data.startLandScapePos.y,	
	};
	//
	_this._xyz2 = {};
	
	
	//점 생성
	let entity = Ppmap.createPoint('ls-anals-saved', this._xyz1.lon, this._xyz1.lat);
	
	//이동
	Ppmap.zoomTo(entity);
	
	//
	alert('경관분석을 위한 위치를 지도에서 클릭하시기 바랍니다.');
	
	//커서 변경
	Ppmap.setCursor('pointer');
	//
	
	//클릭 이벤트 등록
	const handler = new Cesium.ScreenSpaceEventHandler(MAGO3D_INSTANCE.getViewer().scene.canvas);
	handler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);
	handler.removeInputAction(Cesium.ScreenSpaceEventType.MOUSE_MOVE);
	
	//
    handler.setInputAction( (click) => {

			//이벤트 삭제
			handler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);
			
			//restore 커서
			Ppmap.restoreCursor();
			
			
			//
			//const cartesian = MAGO3D_INSTANCE.getViewer().scene.pickPosition(click.position);
			//const cartographic = Cesium.Cartographic.fromCartesian(cartesian);
            //console.log('click', click, 'cartesian', cartesian, 'cartographic', cartographic);
			//console.log(Cesium.Math.toDegrees(cartographic.longitude),Cesium.Math.toDegrees(cartographic.latitude));
			
			//
			_this._xyz2 = Ppmap.cartesian2ToLonLat(click.position);
			
			//
			Ppmap.removeAll();
			//2점 표시
			Ppmap.createPoint('ls-anals-saved-xyz1', _this._xyz1.lon, _this._xyz1.lat);
			Ppmap.createPoint('ls-anals-saved-xyz2', _this._xyz2.lon, _this._xyz2.lat);
			//2점간 선 표시
			Ppmap.createPolyline('ls-anals-saved-line', [_this._xyz1, _this._xyz2]);

			//분석
			_this._doAnals();
        },
        Cesium.ScreenSpaceEventType.LEFT_CLICK
    );
};

//
let lsAnalsSavedObj = new LsAnalsSavedObj();