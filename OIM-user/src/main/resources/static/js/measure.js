const Measure = function(magoInstance) {
	this.magoInstance = magoInstance;
	
	this.drawer;
	this._type = Measure.TYPE.NONE;
	
	this._result;
	
	this.setEventHandler();
}

Object.defineProperties(Measure.prototype, {
	type : {
		get : function() {
			return this._type;
		},
		set : function(type) {
			this._type = type;
			this.setDrawer();
		}
	},
	result : {
		get : function() {
			return this._result;
		},
		set : function(result) {
			var old = this._result;
			
			this._result = result;
			
			if(this._result) {
				this.restore();
			}
			
			if(old) {
				this.removeEntity(old);
				old = undefined;
			}
		} 
	}
});

Measure.STATUS = {
	NOTSTART : 'notstart',
	READY : 'ready',
	NEEDSTARTPOINT : 'needstartpoint',
	NEEDLINE : 'needline',
	NEEDLASTPOINT : 'needlastpoint',
	NEEDVERTEXPOINT : 'needvertexpoint',
	NEEDGUIDEPOINT : 'needguidepoint',
	NEEDPOLYGON : 'needpolygon',
	COMPLETE : 'complete'
}

Measure.TYPE = {
	DISTANCE : 'distance',
	AREA : 'area',
	HEIGHT : 'height',
	NONE : 'none'
}

Measure.prototype.setEventHandler = function() {
	var self = this;
	
	var $btns = $('#toolbarWrap div.toolbox-measure button.toolbox-measure-btn');
	$btns.click(function(){
		var target = this;
		$btns.each(function(_, btn) {
			if(target !== btn) $(btn).removeClass('on');
		});
		
		$(target).toggleClass('on');
		self.type = $(target).hasClass('on') ?  $(target).data('type') : Measure.TYPE.NONE;
	});
	
	var popupObserver = new MutationObserver(function(mutations) {
		mutations.forEach(function(mutation) {
			var mutationStyle = window.getComputedStyle(mutation.target);
			if(mutationStyle.display === 'none') {
				self.destroyDrawer();
				self.result = undefined;
			} else {
				$('#toolbarWrap div.detaildata.poplayer').hide().removeClass('on');
			}
			return false;			
		});
	});
	
	popupObserver.observe(document.querySelector('.toolbox-measure'), { attributes: true, attributeFilter:['style'], subtree: false, childList:false, attributeOldValue:true});	
}

Measure.prototype.restore = function() {
	var viewer = this.magoInstance.getViewer();
	//restore
	var _add = function(entt) {
		if(Array.isArray(entt)) {
			for(var i in entt) {
				_add(entt[i]);					
			}
		} else {
			viewer.entities.add(entt);
		}
	}
	for(var i in this.result) {
		var obj = this.result[i];
		_add(obj);
	}
}

Measure.prototype.removeEntity = function(obj) {
	var viewer = this.magoInstance.getViewer();
	var _remove = function(entt) {
		if(Array.isArray(entt)) {
			for(var i in entt) {
				_remove(entt[i]);					
			}
		} else {
			viewer.entities.remove(entt);
		}
	}

	for(var i in obj) {
		_remove(obj[i]);
	}
}


Measure.prototype.setDrawer = function() {
	this.destroyDrawer();
	
	if(!this.type || this.type === Measure.TYPE.NONE) return;
	
	this.result = undefined;
	this.drawer = new Cesium.ScreenSpaceEventHandler(this.magoInstance.getViewer().canvas);
	this.drawer.result = {};
	this.drawer.status = Measure.STATUS.NOTSTART;
	
	switch(this.type) {
		case Measure.TYPE.DISTANCE : {
			this.decorateDistance();
			break;
		}
		case Measure.TYPE.AREA : {
			this.decorateArea();
			break;
		}
		case Measure.TYPE.HEIGHT : {
			this.decorateHeight();
			break;
		}
	}
}

Measure.prototype.destroyDrawer = function() {
	if(!this.drawer) return;
	var viewer = this.magoInstance.getViewer();
	var _destroy = function (any) {
		if(Array.isArray(any)) {
			for(var i in any) {
				_destroy(any[i]);	
			}
		} else {
			if(any instanceof Cesium.Entity) {
				viewer.entities.remove(any);
			}
			any = undefined;
		}
	}
	
	if(this.drawer.result) {
		for(var i in this.drawer.result) {
			_destroy(this.drawer.result[i]);
		}
		
		delete this.drawer.result;
	}
	
	this.drawer = this.drawer.destroy();
}

Measure.prototype.decorateHeight = function() {
	var viewer = this.magoInstance.getViewer();
	var magoManager = this.magoInstance.getMagoManager();
	var depthDetected = false;
	
	var self = this;
	const pointGraphic = {
		color : Cesium.Color.WHITE,
		outlineColor : new Cesium.Color(1, 0.30196, 0.92549, 1),
		outlineWidth : 3,
		pixelSize : 6,
		disableDepthTestDistance: Number.POSITIVE_INFINITY
	}
	let labelOption = {
        scale :0.5,
        font: "normal normal bolder 24px Helvetica",
        fillColor: Cesium.Color.RED,
        outlineColor: Cesium.Color.RED,
        outlineWidth: 1,
		pixelOffset : new Cesium.Cartesian2(-25,-10), 
        style: Cesium.LabelStyle.FILL_AND_OUTLINE,
        distanceDisplayCondition : new Cesium.DistanceDisplayCondition(0.0, 100000),
		backgroundColor : Cesium.Color.WHITE,
		showBackground : true,
		disableDepthTestDistance: Number.POSITIVE_INFINITY
	}
	
	var _lineCoordinate = function() {
		var pointsCoordinates = self.drawer.result.points.map(function(point) {
			return point.position.getValue();
		});
		pointsCoordinates.push(self.drawer.result.guide.position.getValue());
		return pointsCoordinates;
	}
	
	var _calcHeight = function() {
		var crtsArray = _lineCoordinate();
		
		return `${Cesium.Cartesian3.distance(crtsArray[1], crtsArray[0]).toFixed(1)}m`;
	}
	
	var _complete = function(e) {
		var drawer = self.drawer;
		
		if(drawer.status !== Measure.STATUS.NEEDLASTPOINT) return;
		
		drawer.result.guide.label.text = _calcHeight();
		
		//reinitialize
		drawer.result.line.polyline.positions = _lineCoordinate();
		var cloneLine = Cesium.clone(drawer.result.line, false);
		
		drawer.result.points.push(drawer.result.guide);
		var clonePoints = drawer.result.points.map(function(point) {
			var clonePoint = Cesium.clone(point, false);
			return clonePoint;
		});
		
		drawer.status = Measure.STATUS.COMPLETE;
		$('#toolbox-measure-btn-height').trigger('click');
		
		self.result = {
			line : cloneLine, 
			points : clonePoints
		};
	}
	
	var _click = function(e){
		var drawer = self.drawer;
		
		if(drawer.status === Measure.STATUS.NEEDVERTEXPOINT) {
			if(drawer.result.points.length === 0) {
				depthDetected = Mago3D.ManagerUtils.detectedDepth(e.position.x, e.position.y, magoManager);
			}
			
			var point3d = Mago3D.ManagerUtils.screenCoordToWorldCoordUseDepthCheck(e.position.x, e.position.y, magoManager, {highPrecision:true}); 
			var geographic = Mago3D.ManagerUtils.pointToGeographicCoord(point3d);
			var crts3 = Cesium.Cartesian3.fromDegrees(geographic.longitude, geographic.latitude, geographic.altitude);
			
			drawer.result.points.push(viewer.entities.add({
				position : crts3,
				point : pointGraphic
			}));
			
			labelOption.text = new Cesium.CallbackProperty(_calcHeight);
			drawer.result.guide.label = labelOption;
			
			drawer.status = Measure.STATUS.NEEDLINE;
		}
		
		if(drawer.status === Measure.STATUS.NEEDLASTPOINT) {
			drawer.result.points.push(viewer.entities.add({
				position : drawer.result.guide.position.getValue(),
				point : pointGraphic
			}));
			
			_complete(e.position);
		}
	}
	var _move = function(e) {
		var drawer = self.drawer;
		
		if(drawer.status === Measure.STATUS.COMPLETE) return;
		
		if(drawer.status === Measure.STATUS.NEEDLASTPOINT) {
			var startPointCrts = drawer.result.points[0].position.getValue();
			var height;
			
			if(depthDetected) {
				var point3d = Mago3D.ManagerUtils.screenCoordToWorldCoordUseDepthCheck(e.endPosition.x, e.endPosition.y, magoManager, {highPrecision:true});
				var geographic = Mago3D.ManagerUtils.pointToGeographicCoord(point3d);
				
				height = geographic.altitude;
			}
			
			if(!depthDetected || Math.abs(height) < 0.8) {
				var scene = viewer.scene;
				var startPointCrtsClone = new Cesium.Cartesian3(startPointCrts.x, startPointCrts.y, startPointCrts.z);
				
				var surfaceNormal = scene.globe.ellipsoid.geodeticSurfaceNormal(startPointCrtsClone);
                var planeNormal = Cesium.Cartesian3.subtract(scene.camera.position, startPointCrtsClone, new Cesium.Cartesian3());
                planeNormal = Cesium.Cartesian3.normalize(planeNormal, planeNormal);
                var ray =  viewer.scene.camera.getPickRay(e.endPosition);
                var plane = Cesium.Plane.fromPointNormal(startPointCrtsClone, planeNormal);
                var newCartesian =  Cesium.IntersectionTests.rayPlane(ray, plane);
                var newCartographic = scene.globe.ellipsoid.cartesianToCartographic(newCartesian);

                height = newCartographic.height;
                if(height < 0) height *= -1;
			}
			
			var startPointGeographic = Mago3D.ManagerUtils.pointToGeographicCoord(startPointCrts);
			var guideCrts = Cesium.Cartesian3.fromDegrees(startPointGeographic.longitude, startPointGeographic.latitude, height);
			
			drawer.result.guide.position = guideCrts;
		} else {
			var point3d = API.Converter.screenCoordToMagoPoint3D(e.endPosition.x, e.endPosition.y, magoManager);
			var crts3 = API.Converter.magoToCesiumForPoint3D(point3d);
			
			if(drawer.status === Measure.STATUS.NOTSTART) {
				drawer.result.points = [];
				drawer.result.guide = viewer.entities.add({
					point : pointGraphic
				});
				
				drawer.status = Measure.STATUS.NEEDVERTEXPOINT;
			}
			
			if(drawer.status === Measure.STATUS.NEEDLINE) {
				drawer.result.line = viewer.entities.add({
					polyline : {
						positions : new Cesium.CallbackProperty(_lineCoordinate),
						width : 3,
						depthFailMaterial : Cesium.Color.RED,
						material : new Cesium.PolylineGlowMaterialProperty({
							color: new Cesium.Color(0.88627, 0.19216, 0.86667, 1),
							glowPower: 0.25
						})
					}
				});
				drawer.status = Measure.STATUS.NEEDLASTPOINT;
			}
			
			drawer.result.guide.position = crts3;
		}
	}
	
	this.drawer.setInputAction(_click ,Cesium.ScreenSpaceEventType.LEFT_CLICK);
	this.drawer.setInputAction(_move ,Cesium.ScreenSpaceEventType.MOUSE_MOVE);
}


Measure.prototype.decorateArea = function() {
	var viewer = this.magoInstance.getViewer();
	var magoManager = this.magoInstance.getMagoManager();
	
	var self = this;
	const pointGraphic = {
		color : Cesium.Color.WHITE,
		outlineColor : new Cesium.Color(0.094118, 0.2, 0.89804, 1),
		outlineWidth : 3,
		pixelSize : 6,
		heightReference : Cesium.HeightReference.CLAMP_TO_GROUND
	}
	let labelOption = {
        scale :0.5,
        font: "normal normal bolder 24px Helvetica",
        fillColor: Cesium.Color.RED,
        outlineColor: Cesium.Color.RED,
        outlineWidth: 1,
		pixelOffset : new Cesium.Cartesian2(-25,-10), 
        heightReference : Cesium.HeightReference.CLAMP_TO_GROUND,
        style: Cesium.LabelStyle.FILL_AND_OUTLINE,
        distanceDisplayCondition : new Cesium.DistanceDisplayCondition(0.0, 100000),
		backgroundColor : Cesium.Color.WHITE,
		showBackground : true
	}
	
	var _lineCoordinate = function() {
		var pointsCoordinates = self.drawer.result.points.map(function(point) {
			return point.position.getValue();
		});
		pointsCoordinates.push(self.drawer.result.guide.position.getValue());
		return pointsCoordinates;
	}
	
	var _polygonHierarchy = function() {
		return new Cesium.PolygonHierarchy(_lineCoordinate());
	}
	
	var _calcArea = function() {
		var _calTriangleArea = function(p1, p2, p3) {
			var r = Math.abs(p1.x * (p2.y - p3.y) + p2.x * (p3.y - p1.y) + p3.x * (p1.y - p2.y)) / 2;
			var cartographic = new Cesium.Cartographic((p1.x + p2.x + p3.x) / 3, (p1.y + p2.y + p3.y) / 3);
			var cartesian = viewer.scene.globe.ellipsoid.cartographicToCartesian(cartographic);
			var magnitude = Cesium.Cartesian3.magnitude(cartesian);
			return r * magnitude * magnitude * Math.cos(cartographic.latitude);
		}
		var existingPoint = _lineCoordinate().map(function(crts) {
			var cartographic = Cesium.Cartographic.fromCartesian(crts);
			return new Cesium.Cartesian2(cartographic.longitude, cartographic.latitude);
		});
		
		if (Cesium.PolygonPipeline.computeWindingOrder2D(existingPoint) === Cesium.WindingOrder.CLOCKWISE) {
			existingPoint.reverse();
		}
		var triangles = Cesium.PolygonPipeline.triangulate(existingPoint);
		
		var area = 0;
		for(var i=0,len=triangles.length;i<len;i += 3) {
			area += _calTriangleArea(existingPoint[triangles[i]], existingPoint[triangles[i + 1]], existingPoint[triangles[i + 2]]);
		}
		
		return `${area.toFixed(3)}㎡`;
	}
	
	var _complete = function(e) {
		var drawer = self.drawer;
		
		if(drawer.status !== Measure.STATUS.NEEDVERTEXPOINT) return;
		
		var point3d = API.Converter.screenCoordToMagoPoint3D(e.position.x, e.position.y, self.magoInstance.getMagoManager());
		var crts3 = API.Converter.magoToCesiumForPoint3D(point3d);
		
		drawer.result.guide.position = crts3;
		drawer.result.guide.label.text = _calcArea();
		
		//reinitialize
		drawer.result.line.polyline.positions = _lineCoordinate();
		var cloneLine = Cesium.clone(drawer.result.line, false);
		
		drawer.result.points.push(drawer.result.guide);
		var clonePoints = drawer.result.points.map(function(point) {
			var clonePoint = Cesium.clone(point, false);
			return clonePoint;
		});
		
		drawer.result.polygon.polygon.hierarchy = _polygonHierarchy();
		var clonePolygon = Cesium.clone(drawer.result.polygon, false);
		
		drawer.status = Measure.STATUS.COMPLETE;
		$('#toolbox-measure-btn-area').trigger('click');
		
		self.result = {
			line : cloneLine, 
			points : clonePoints,
			polygon : clonePolygon
		};
	}
	
	var _click = function(e){
		var drawer = self.drawer;
		
		var point3d = API.Converter.screenCoordToMagoPoint3D(e.position.x, e.position.y, self.magoInstance.getMagoManager());
		var crts3 = API.Converter.magoToCesiumForPoint3D(point3d);
		
		if(drawer.status === Measure.STATUS.NEEDVERTEXPOINT) {
			drawer.result.points.push(viewer.entities.add({
				position : crts3,
				point : pointGraphic
			}));
			
			if(drawer.result.points.length === 1) {
				drawer.status = Measure.STATUS.NEEDLINE;
			}
			if(drawer.result.points.length === 2) {
				labelOption.text = new Cesium.CallbackProperty(_calcArea);
				drawer.result.guide.label = labelOption;
				drawer.status = Measure.STATUS.NEEDPOLYGON;
			}
		}
	}
	var _move = function(e) {
		var drawer = self.drawer;
		
		if(drawer.status === Measure.STATUS.COMPLETE) return;
		
		var point3d = API.Converter.screenCoordToMagoPoint3D(e.endPosition.x, e.endPosition.y, magoManager);
		var crts3 = API.Converter.magoToCesiumForPoint3D(point3d);
		
		if(drawer.status === Measure.STATUS.NOTSTART) {
			drawer.result.points = [];
			drawer.result.guide = viewer.entities.add({
				point : pointGraphic
			});
			
			drawer.status = Measure.STATUS.NEEDVERTEXPOINT;
		}
		
		if(drawer.status === Measure.STATUS.NEEDLINE) {
			drawer.result.line = viewer.entities.add({
				polyline : {
					positions : new Cesium.CallbackProperty(_lineCoordinate),
					width : 3,
					clampToGround : true,
					material : new Cesium.Color(0.4, 0.47059, 0.92157, 1)
				}
			});
			drawer.status = Measure.STATUS.NEEDVERTEXPOINT;
		}
		
		if(drawer.status === Measure.STATUS.NEEDPOLYGON) {
			/*viewer.entities.remove(drawer.result.line);
			delete drawer.result.line;*/
			
			drawer.result.polygon = viewer.entities.add({
				polygon : {
					hierarchy : new Cesium.CallbackProperty(_polygonHierarchy),
					heightReference : Cesium.HeightReference.CLAMP_TO_GROUND,
					outline : true,
					outlineWidth : 1,
					outlineColor : new Cesium.Color(0.4, 0.47059, 0.92157, 1),
					material :  new Cesium.Color(0.81961, 0.83921, 0.98039, 0.35),
					zIndex : 1
				}
			});
			drawer.status = Measure.STATUS.NEEDVERTEXPOINT;
		}
		
		drawer.result.guide.position = crts3;
	}
	
	this.drawer.setInputAction(_complete ,Cesium.ScreenSpaceEventType.RIGHT_CLICK);
	this.drawer.setInputAction(_click ,Cesium.ScreenSpaceEventType.LEFT_CLICK);
	this.drawer.setInputAction(_move ,Cesium.ScreenSpaceEventType.MOUSE_MOVE);
}

Measure.prototype.decorateDistance = function() {
	var viewer = this.magoInstance.getViewer();
	var magoManager = this.magoInstance.getMagoManager();
	
	var self = this;
	const pointGraphic = {
		color : Cesium.Color.WHITE,
		outlineColor : Cesium.Color.RED,
		outlineWidth : 3,
		pixelSize : 6,
		heightReference : Cesium.HeightReference.CLAMP_TO_GROUND
	}
	
	let labelOption = {
        scale :0.5,
        font: "normal normal bolder 24px Helvetica",
        fillColor: Cesium.Color.RED,
        outlineColor: Cesium.Color.RED,
        outlineWidth: 1,
		pixelOffset : new Cesium.Cartesian2(-25,-10), 
        heightReference : Cesium.HeightReference.CLAMP_TO_GROUND,
        style: Cesium.LabelStyle.FILL_AND_OUTLINE,
        distanceDisplayCondition : new Cesium.DistanceDisplayCondition(0.0, 100000),
		backgroundColor : Cesium.Color.WHITE,
		showBackground : true
	}
	var _lineCoordinate = function() {
		var pointsCoordinates = self.drawer.result.points.map(function(point) {
			return point.position.getValue();
		});
		pointsCoordinates.push(self.drawer.result.guide.position.getValue());
		return pointsCoordinates;
	}
	
	var _accumDistance = function() {
		var existingPoint = _lineCoordinate();
		var accumDistance = existingPoint.reduce(function(acc, crts, idx, array) {
		    if(idx === 0) return acc;
		    var d = Cesium.Cartesian3.distance(crts,array[idx-1]);
		    return acc + d;
		} , 0);
		
		return `${accumDistance.toFixed(0)}m`;
	}
	
	var _complete = function(e) {
		var drawer = self.drawer;
		
		if(drawer.status !== Measure.STATUS.NEEDVERTEXPOINT) return;
		
		var point3d = API.Converter.screenCoordToMagoPoint3D(e.position.x, e.position.y, self.magoInstance.getMagoManager());
		var crts3 = API.Converter.magoToCesiumForPoint3D(point3d);
		
		drawer.result.guide.position = crts3;
		drawer.result.guide.label.text = _accumDistance();
		
		//reinitialize
		drawer.result.line.polyline.positions = _lineCoordinate();
		var cloneLine = Cesium.clone(drawer.result.line, false);
		
		drawer.result.points.push(drawer.result.guide);
		var clonePoints = drawer.result.points.map(function(point) {
			var clonePoint = Cesium.clone(point, false);
			return clonePoint;
		});
		
		drawer.status = Measure.STATUS.COMPLETE;
		$('#toolbox-measure-btn-distance').trigger('click');
		
		self.result = {
			line : cloneLine, 
			points : clonePoints
		};
	}
	
	var _click = function(e){
		var drawer = self.drawer;
		
		var point3d = API.Converter.screenCoordToMagoPoint3D(e.position.x, e.position.y, self.magoInstance.getMagoManager());
		var crts3 = API.Converter.magoToCesiumForPoint3D(point3d);
		
		if(drawer.status === Measure.STATUS.NEEDVERTEXPOINT) {
			labelOption.text = _accumDistance();
			drawer.result.points.push(viewer.entities.add({
				position : crts3,
				point : pointGraphic,
				label : labelOption
			}));
			
			if(drawer.result.points.length === 1) {
				labelOption.text = new Cesium.CallbackProperty(_accumDistance)
				drawer.result.guide.label = labelOption;
				
				drawer.status = Measure.STATUS.NEEDLINE
			}
		}
	}
	var _move = function(e) {
		var drawer = self.drawer;
		
		if(drawer.status === Measure.STATUS.COMPLETE) return;
		
		var point3d = API.Converter.screenCoordToMagoPoint3D(e.endPosition.x, e.endPosition.y, magoManager);
		var crts3 = API.Converter.magoToCesiumForPoint3D(point3d);
		
		if(drawer.status === Measure.STATUS.NOTSTART) {
			drawer.result.points = [];
			drawer.result.guide = viewer.entities.add({
				point : pointGraphic
			});
			
			drawer.status = Measure.STATUS.NEEDVERTEXPOINT;
		}
		
		if(drawer.status === Measure.STATUS.NEEDLINE) {
			drawer.result.line = viewer.entities.add({
				polyline : {
					positions : new Cesium.CallbackProperty(_lineCoordinate),
					width : 3,
					clampToGround : true,
					material : Cesium.Color.RED
				}
			});
			drawer.status = Measure.STATUS.NEEDVERTEXPOINT;
		}
		
		drawer.result.guide.position = crts3;
	}
	
	this.drawer.setInputAction(_complete ,Cesium.ScreenSpaceEventType.RIGHT_CLICK);
	this.drawer.setInputAction(_click ,Cesium.ScreenSpaceEventType.LEFT_CLICK);
	this.drawer.setInputAction(_move ,Cesium.ScreenSpaceEventType.MOUSE_MOVE);
}