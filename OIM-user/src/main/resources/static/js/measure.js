"use strict";

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); return Constructor; }

var _Shape = /*#__PURE__*/function(magoInstance) {
	function Shape(pointsCount, condition) {
		_classCallCheck(this, Shape);

		this.pointsCount = pointsCount;
		this.condition = condition;
		this.handler = new Cesium.ScreenSpaceEventHandler(magoInstance.getViewer().scene.canvas);
		this.positions = [];
		this.entityId = [];
		this.end = false;
		//this._changedCamera = this.changedCamera.bind(this);
	}

	_createClass(Shape, [{
		key: "setGeodesic",
		value: function setGeodesic(start, end) {
			var geodesic = new Cesium.EllipsoidGeodesic();
			var startCartographic = Cesium.Cartographic.fromCartesian(start);
			var endCartographic = Cesium.Cartographic.fromCartesian(end);
			geodesic.setEndPoints(startCartographic, endCartographic);
			return geodesic;
		}
	}, {
		key: "getMidpoint",
		value: function getMidpoint() {
			var scratch = new Cesium.Cartographic();
			var len = this.positions.length;
			var geodesic = this.setGeodesic(this.positions[0], this.positions[len - 1]);
			var midpointCartographic = geodesic.interpolateUsingFraction(0.5, scratch);
			return Cesium.Cartesian3.fromRadians(midpointCartographic.longitude, midpointCartographic.latitude);
		}
	}, {
		key: "getLength",
		value: function getLength() {
			var len = this.positions.length;
			var total = 0;
			for (var i = len - 1; i >= 1; i--) {
				var geodesic = this.setGeodesic(this.positions[i - 1], this.positions[i]);
				var lengthInMeters = Math.round(geodesic.surfaceDistance);
				total += lengthInMeters;
			}
			/*var total = 0;
			for(var i = len - 1; i >= 1; i--){
			  var distance = Cesium.Cartesian3.distance(this.positions[i - 1], this.positions[i]);
			  total += distance;
			}*/
			return (total / 1000).toFixed(1) + " km";
		}
	}, {
		key: "calArea",
		value: function calArea(t1, t2, t3, i) {
			var r = Math.abs(t1.x * (t2.y - t3.y) + t2.x * (t3.y - t1.y) + t3.x * (t1.y - t2.y)) / 2;
			var cartographic = new Cesium.Cartographic((t1.x + t2.x + t3.x) / 3, (t1.y + t2.y + t3.y) / 3);
			var cartesian = magoInstance.getViewer().scene.globe.ellipsoid.cartographicToCartesian(cartographic);
			var magnitude = Cesium.Cartesian3.magnitude(cartesian);
			return r * magnitude * magnitude * Math.cos(cartographic.latitude);
		}
	}, {
		key: "getArea",
		value: function getArea() {
			var positionsList = this.positions;
			var areaInMeters = 0;

			if (positionsList.length >= 3) {
				var points = [];

				for (var i = 0, len = positionsList.length; i < len; i++) {
					// points.push(Cesium.Cartesian2.fromCartesian3(positions[i]));
					var cartographic = Cesium.Cartographic.fromCartesian(positionsList[i]);
					points.push(new Cesium.Cartesian2(cartographic.longitude, cartographic.latitude));
				}

				if (Cesium.PolygonPipeline.computeWindingOrder2D(points) === Cesium.WindingOrder.CLOCKWISE) {
					points.reverse();
				}

				var triangles = Cesium.PolygonPipeline.triangulate(points);

				for (var i = 0, len = triangles.length; i < len; i += 3) {
					/* areaInMeters +=
					Cesium.PolygonPipeline.computeArea2D([points[triangles[i]],
					points[triangles[i + 1]], points[triangles[i + 2]]]);*/
					areaInMeters += this.calArea(points[triangles[i]], points[triangles[i + 1]], points[triangles[i + 2]]);
				}
			}

			return areaInMeters.toFixed(3) + " m2";
		}
	}, {
		key: "drawShape",
		value: function drawShape() {
			if (this.pointsCount == 1) {

				var len = this.positions.length;
				var point = magoInstance.getViewer().entities.add({
					position: this.positions[len - 1],
					point: {
						pixelSize: 10,
						color: Cesium.Color.WHITE,
						heightReference: Cesium.HeightReference.CLAMP_TO_GROUND
					}
				});
				this.entityId.push(point.id);
				return point;

			} else if (this.pointsCount == 2) {

				var _that = this;

				var getMidpointCall = function getMidpointCall() {
					return _that.getMidpoint();
				};

				var getLengthCall = function getLengthCall() {
					return _that.getLength();
				};

				var len = this.positions.length;

				var point = magoInstance.getViewer().entities.add({
					position: this.positions[len - 1],
					point: {
						pixelSize: 10,
						color: Cesium.Color.WHITE,
						heightReference: Cesium.HeightReference.CLAMP_TO_GROUND
					}
				});

				var position1 = this.positions[0];
				var position2 = this.positions[len - 1];
				var line = magoInstance.getViewer().entities.add({
					polyline: {
						positions: new Cesium.CallbackProperty(function() {
							return [position1, position2];
						}, false),
						width: 10.0,
						material: new Cesium.PolylineGlowMaterialProperty({
							color: Cesium.Color.DEEPSKYBLUE,
							glowPower: 0.25
						}),
						clampToGround: true,
						//granularity : Cesium.Math.toRadians(0.1)
					}
				});
				position1 = position2;

				this.handler.setInputAction(function(movement) {
					//var movingPosition = magoInstance.getViewer().camera.pickEllipsoid(movement.endPosition, magoInstance.getViewer().scene.globe.ellipsoid);
					var movingPosition = _that.transfromCoord(movement.endPosition);
					if (movingPosition) {
						position2 = movingPosition;
					}

				}, Cesium.ScreenSpaceEventType.MOUSE_MOVE);

				var label = magoInstance.getViewer().entities.add({
					position: new Cesium.CallbackProperty(getMidpointCall, false),
					label: {
						// This callback updates the length to print each frame.
						text: new Cesium.CallbackProperty(getLengthCall, false),
						font: "20px sans-serif",
						fillColor: Cesium.Color.DEEPSKYBLUE,
						pixelOffset: new Cesium.Cartesian2(0.0, 20)
					}
				});

				this.entityId.push(point.id, line.id, label.id);
				this.stopDrawing();
				return line;

			} else if (this.pointsCount > 2) {

				var that = this;

				var _getMidpointCall = function _getMidpointCall() {
					return that.getMidpoint();
				};

				var getAreaCall = function getAreaCall() {
					return that.getArea();
				};

				var line = magoInstance.getViewer().entities.add({
					polyline: {
						positions: new Cesium.CallbackProperty(function() {
							return that.positions;
						}, false),
						width: 4.0,
						material: new Cesium.PolylineGlowMaterialProperty({
							color: Cesium.Color.DEEPSKYBLUE,
							glowPower: 0.25
						}),
						clampToGround: true,
					}
				});

				var len = that.positions.length;
				that.positions.push(that.positions[len - 1]);

				var polygon = magoInstance.getViewer().entities.add({
					polygon: {
						hierarchy: new Cesium.CallbackProperty(function() {
							return new Cesium.PolygonHierarchy(that.positions);
						}, false),
						material: new Cesium.ColorMaterialProperty(Cesium.Color.WHITE.withAlpha(0.05))
					}
				});

				that.handler.setInputAction(function(movement) {
					//var dynamicPos = magoInstance.getViewer().camera.pickEllipsoid(movement.endPosition, magoInstance.getViewer().scene.globe.ellipsoid);
					var dynamicPos = that.transfromCoord(movement.endPosition);
					that.positions[that.positions.length - 1] = dynamicPos; //change last position to moving position
					var shapeInfo = document.getElementById("measureInfo");
					var exitbtn = document.getElementById("exit");
					shapeInfo.textContent = "area : " + that.getArea();
					shapeInfo.appendChild(exitbtn);

				}, Cesium.ScreenSpaceEventType.MOUSE_MOVE);

				that.entityId.push(line.id, polygon.id);
				that.stopDrawing();
				return polygon;
			}
		}
	}, {
		key: "setEventHandler",
		value: function setEventHandler() {
			var that = this;
			var shapeInfo = document.getElementById("measureInfo");
			shapeInfo.style.display = "block";
			shapeInfo.style.background = 'white';
			shapeInfo.style.border = '1px solid #888';
			shapeInfo.style.paddingLeft = '5px';
			shapeInfo.style.position = 'absolute';
			shapeInfo.style.zIndex = '100';
			shapeInfo.style.color = "#00BFFF";
			var exitbtn = document.createElement("button");
			exitbtn.id = "exit";
			exitbtn.innerText = "x";
			exitbtn.style.border = "0px";
			exitbtn.style.padding = "0 5px";
			exitbtn.style.backgroundColor = "transparent";
			exitbtn.addEventListener("click", function() {
				shapeInfo.innerHTML = "";
				that.deleteEntities();
			});
			this.handler.setInputAction(function(event) {
				if (that.condition()) {
					if (that.end) {
						that.init();
					}
					//var position = magoInstance.getViewer().camera.pickEllipsoid(event.position, magoInstance.getViewer().scene.globe.ellipsoid);
					var position = that.transfromCoord(event.position);
					that.positions.push(position);

					that.drawShape();
					if (that.pointsCount == 2) {
						shapeInfo.textContent = "distance : " + that.getLength();
					}

					if (that.pointsCount > 2) {
						shapeInfo.textContent = "area : " + that.getArea();
					}
					var left = event.position.x;
					if($('#navWrap').is(':visible')) {
						left += $('#navWrap').width();
					}
					if($('#contentsWrap').is(':visible')) {
						left += $('#contentsWrap').width();
					}
					var top = event.position.y;
					shapeInfo.style.left = left + 'px';
					shapeInfo.style.top = top + 'px';
					shapeInfo.appendChild(exitbtn);
				}
			}, Cesium.ScreenSpaceEventType.LEFT_CLICK);
		}
	}, {
		key: "stopDrawing",
		value: function stopDrawing() {
			var that = this;

			that.handler.setInputAction(function() {
				//magoInstance.getViewer().scene.camera.changed.removeEventListener(that._changedCamera);
				//magoInstance.getViewer().scene.camera.changed.addEventListener(that._changedCamera);
				// Pre-allocate memory once.  Don't re-allocate for each animation frame.
				var scratch3dPosition = new Cesium.Cartesian3();
				var scratch2dPosition = new Cesium.Cartesian2();
				var isEntityVisible = true;
				var shapeInfo = document.getElementById("measureInfo");

				magoInstance.getViewer().clock.onTick.addEventListener(function(clock) {
					if(!that.end) {
						return;
					}
					var position3d;
					var position2d;

					if (that.positions[that.positions.length - 1] && that.positions.length > 2) {
						position3d = that.positions[that.positions.length - 1];
					}

					if (position3d) {
						position2d = Mago3D.ManagerUtils.calculateWorldPositionToScreenCoord(undefined, position3d.x, position3d.y, position3d.z, position2d, magoInstance.getMagoManager())
						//position2d = Cesium.SceneTransforms.wgs84ToWindowCoordinates(
						//	magoInstance.getViewer().scene, position3d, scratch2dPosition);
					}

					if (position2d) {

						var left = position2d.x;
						if($('#navWrap').is(':visible')) {
							left += $('#navWrap').width();
						}
						if($('#contentsWrap').is(':visible')) {
							left += $('#contentsWrap').width();
						}
						var top = position2d.y-50;
						shapeInfo.style.left = left + 'px';
						shapeInfo.style.top = top + 'px';

					} 
				});
				that.handler.removeInputAction(Cesium.ScreenSpaceEventType.MOUSE_MOVE);
				document.getElementById("mapCtrlMeasureDistance").classList.remove("on");
				document.getElementById("mapCtrlMeasureArea").classList.remove("on");
				if (that.pointsCount == 2) {
					magoInstance.getViewer().entities.removeById(that.entityId[that.entityId.length - 2]); //close polyline on right click
				}
				else if (that.pointsCount > 2) {
					that.positions[that.positions.length - 1] = that.positions[0]; //close polygon
				}
				that.end = true;

			}, Cesium.ScreenSpaceEventType.RIGHT_CLICK);
		}
	}, {
		key: "deleteEntities",
		value: function deleteEntities() {
			for (var index = 0; index < this.entityId.length; index++) {
				magoInstance.getViewer().entities.removeById(this.entityId[index]);
			}
		}
	}, {
		key: "transfromCoord",
		value: function transfromCoord(screenPosition) {
			var cesiumScene = magoInstance.getViewer().scene;
			var cesiumGlobe = cesiumScene.globe;
			var cesiumCamera = cesiumScene.camera;
			var windowCoordinates = new Cesium.Cartesian2(screenPosition.x, screenPosition.y);
			var ray = cesiumCamera.getPickRay(windowCoordinates);
			var intersection = cesiumGlobe.pick(ray, cesiumScene);

			return intersection;
		}
	}, {
		key : "init",
		value : function init() {
			this.deleteEntities();
			this.end = false;
			this.positions = []; //get the last element
			this.entityId.length = 0;
			this.handler.removeInputAction(Cesium.ScreenSpaceEventType.MOUSE_MOVE);
		}
	}
	]);

	return Shape;
};