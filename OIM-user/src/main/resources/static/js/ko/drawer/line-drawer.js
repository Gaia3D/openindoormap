/**
 * @param {Cesium.Viewer} viewer 생성한 세슘 Viewer 객체
 * @param {function} handleEventEnd 라인그리기 동작을 완료한 후 처리할 기능
 * @param {object} options 라인 스타일 옵션, 'positions'항목은 무시됨.
 */
var LineDrawer = function(viewer, handleEventEnd ,options) {
	options = options ? options : {};
	this.viewer = viewer;
	this.handler = new Cesium.ScreenSpaceEventHandler(this.viewer.canvas);
	this.active = false;
	
	this.changeWc;
	this.startPointEntity;
	this.startClickTime;
	this.startPosition;
	this.lineEntity;
	this.points = [];
	
	var that = this;
	this.positionCallback = new Cesium.CallbackProperty(function(){
		if(that.changeWc) {
			var pointsCopy = [...that.points];
			pointsCopy.push(that.changeWc);
			return pointsCopy;
		}
	}, false);
	
	this.handleEventEnd = handleEventEnd;
	
	
	//ASSGIN으로 변경하는게...
	//this.pointStyle = options.pointStyle ? options.pointStyle : {heightReference : Cesium.HeightReference.CLAMP_TO_GROUND};
	this.lineStyle = options.lineStyle ? options.lineStyle : {width : 3, clampToGround : true};
	this.lineStyle.positions = this.positionCallback;
}

LineDrawer.prototype.setActive = function(active) {
	this.active = active;
	if(!active) {
		this.clear();
	} else {
		this.handler.setInputAction(this.click.bind(this), Cesium.ScreenSpaceEventType.LEFT_CLICK);
		this.handler.setInputAction(this.move.bind(this), Cesium.ScreenSpaceEventType.MOUSE_MOVE);
	}
}

LineDrawer.prototype.click = function(e){
	if(!this.startPointEntity)
	{
		this.startPosition = e.position.clone();
		this.startClickTime = new Date().getTime();
		var wc = this.screenToWorldCoord(e.position);
		this.startPointEntity = this.viewer.entities.add(new Cesium.Entity({
			point : new Cesium.PointGraphics(),
			position : wc
		}));
		
		this.points.push(wc);
	} else {
		
		var curTime = new Date().getTime();
		var dbclick = false;
		if((curTime - this.startClickTime < 600) && Math.abs(this.startPosition.x - e.position.x) < 1 &&  Math.abs(this.startPosition.y - e.position.y) < 1) {
			dbclick = true;
		}
		
		if(dbclick) {
			var linePosition = this.lineEntity.polyline.positions.getValue();
			linePosition.pop();
			
			if(this.handleEventEnd && typeof this.handleEventEnd === 'function') this.handleEventEnd.call(this, linePosition);
			
			this.initVariable();
		} else {
			this.points.push(this.screenToWorldCoord(e.position));
			this.startClickTime = curTime;
			this.startPosition = e.position.clone();
		}
	}
}
LineDrawer.prototype.move = function(e) {
	if(this.startPointEntity) {
		this.changeWc = this.screenToWorldCoord(e.endPosition);
		if(!this.lineEntity)
		{
			var style = this.lineStyle
			this.lineEntity = this.viewer.entities.add(new Cesium.Entity({
				polyline : new Cesium.PolylineGraphics(this.lineStyle)
			}));
		}
	}
}

LineDrawer.prototype.clear = function (){
	this.handler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);
	this.handler.removeInputAction(Cesium.ScreenSpaceEventType.MOUSE_MOVE);
	this.initVariable();
}
LineDrawer.prototype.initVariable = function (){
	if(this.lineEntity) this.viewer.entities.removeById(this.lineEntity.id);
	if(this.startPointEntity) this.viewer.entities.removeById(this.startPointEntity.id);
	this.lineEntity = undefined 
	this.startPointEntity = undefined;
	this.changeWc = undefined;
	this.startClickTime = undefined;
	this.startPosition = undefined;
	this.points = [];
}

LineDrawer.prototype.screenToWorldCoord = function(pixel) {
	var cesiumScene = this.viewer.scene; 
	var cesiumGlobe = cesiumScene.globe;
	var cesiumCamera = cesiumScene.camera;
	var windowCoordinates = new Cesium.Cartesian2(pixel.x, pixel.y);
	var ray = cesiumCamera.getPickRay(windowCoordinates);
	var intersection = cesiumGlobe.pick(ray, cesiumScene);
	
	return intersection;
}