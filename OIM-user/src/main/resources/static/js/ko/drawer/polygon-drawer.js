/**
 * @param {Cesium.Viewer} viewer 생성한 세슘 Viewer 객체
 * @param {function} handleEventEnd 라인그리기 동작을 완료한 후 처리할 기능
 * @param {object} options 라인 스타일 옵션, 'positions'항목은 무시됨.
 */
var PolygonDrawer = function(viewer, handleEventEnd ,options) {
	options = options ? options : {};
	this.viewer = viewer;
	this.handler = new Cesium.ScreenSpaceEventHandler(this.viewer.canvas);
	this.active = false;
	
	this.changeWc;
	this.startPointEntity;
	this.startClickTime;
	this.startPosition;
	this.lineEntity;
	this.polygonEntity;
	this.points = [];
	
	var that = this;
	this.positionCallback = new Cesium.CallbackProperty(function(){
		if(that.changeWc) {
			var pointsCopy = [...that.points];
			pointsCopy.push(that.changeWc);
			
			if(pointsCopy.length > 2) {
				return new Cesium.PolygonHierarchy(pointsCopy);
			} else {
				return pointsCopy;
			}
		}
	}, false);
	
	this.handleEventEnd = handleEventEnd;
	
	//ASSGIN으로 변경하는게...
	//this.pointStyle = options.pointStyle ? options.pointStyle : {heightReference : Cesium.HeightReference.CLAMP_TO_GROUND};
	this.lineStyle = options.lineStyle ? options.lineStyle : {width : 3, clampToGround : true};
	this.lineStyle.positions = this.positionCallback;
	
	this.polygonStyle = options.polygonStyle ? options.polygonStyle : {material : Cesium.Color.WHITE.withAlpha(0.7), heightReference : Cesium.HeightReference.CLAMP_TO_GROUND};
	this.polygonStyle.hierarchy = this.positionCallback;
}

PolygonDrawer.prototype.setActive = function(active) {
	this.active = active;
	if(!active) {
		this.clear();
	} else {
		this.handler.setInputAction(this.click.bind(this), Cesium.ScreenSpaceEventType.LEFT_CLICK);
		this.handler.setInputAction(this.move.bind(this), Cesium.ScreenSpaceEventType.MOUSE_MOVE);
	}
}

PolygonDrawer.prototype.click = function(e){
	if(this.points.length === 0) {
		this.startPosition = e.position.clone();
		this.startClickTime = new Date().getTime();
		var wc = this.screenToWorldCoord(e.position);
		this.startPointEntity = this.viewer.entities.add(new Cesium.Entity({
			point : new Cesium.PointGraphics(),
			position : wc
		}));
		
		this.points.push(wc);
	} else if (this.points.length === 1) {
		this.points.push(this.screenToWorldCoord(e.position));
		
		this.startClickTime = new Date().getTime();
		this.startPosition = e.position.clone();
		
		if(this.lineEntity) this.viewer.entities.removeById(this.lineEntity.id);
	} else {
		var curTime = new Date().getTime();
		var dbclick = false;
		if((curTime - this.startClickTime < 600) && Math.abs(this.startPosition.x - e.position.x) < 1 &&  Math.abs(this.startPosition.y - e.position.y) < 1) {
			dbclick = true;
		}
		
		if(dbclick) {
			var cartesians = this.polygonEntity.polygon.hierarchy.getValue(new Date()).positions;
			cartesians.pop();
			
			if(this.handleEventEnd && typeof this.handleEventEnd === 'function') this.handleEventEnd.call(this, cartesians);

			this.initVariable();
		} else {
			this.points.push(this.screenToWorldCoord(e.position));
			this.startClickTime = curTime;
			this.startPosition = e.position.clone();
		}
	}
}
PolygonDrawer.prototype.move = function(e) {
	if(this.points.length === 1) {
		this.changeWc = this.screenToWorldCoord(e.endPosition);
		if(!this.lineEntity)
		{
			this.lineEntity = this.viewer.entities.add(new Cesium.Entity({
				polyline : new Cesium.PolylineGraphics({
					positions : this.positionCallback,
					width : 3,
					clampToGround : true
				})
			}));
			if(this.startPointEntity) this.viewer.entities.removeById(this.startPointEntity.id);
		}
	} else if(this.points.length > 1) {
		this.changeWc = this.screenToWorldCoord(e.endPosition);
		if(!this.polygonEntity)
		{
			this.polygonEntity = this.viewer.entities.add(new Cesium.Entity({
				polygon : new Cesium.PolygonGraphics({
					hierarchy : this.positionCallback,
					heightReference : Cesium.HeightReference.CLAMP_TO_GROUND,
					material : Cesium.Color.WHITE.withAlpha(0.7)
				})
			}));
		}
	}
}

PolygonDrawer.prototype.clear = function (){
	this.handler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);
	this.handler.removeInputAction(Cesium.ScreenSpaceEventType.MOUSE_MOVE);
	this.initVariable();
}
PolygonDrawer.prototype.initVariable = function (){
	if(this.lineEntity) this.viewer.entities.removeById(this.lineEntity.id);
	if(this.startPointEntity) this.viewer.entities.removeById(this.startPointEntity.id);
	if(this.polygonEntity) this.viewer.entities.removeById(this.polygonEntity.id);
	
	this.lineEntity = undefined 
	this.startPointEntity = undefined;
	this.polygonEntity = undefined; 
	this.changeWc = undefined;
	this.startClickTime = undefined;
	this.startPosition = undefined;
	this.points = [];
}

PolygonDrawer.prototype.screenToWorldCoord = function(pixel) {
	var cesiumScene = this.viewer.scene; 
	var cesiumGlobe = cesiumScene.globe;
	var cesiumCamera = cesiumScene.camera;
	var windowCoordinates = new Cesium.Cartesian2(pixel.x, pixel.y);
	var ray = cesiumCamera.getPickRay(windowCoordinates);
	var intersection = cesiumGlobe.pick(ray, cesiumScene);
	
	return intersection;
}