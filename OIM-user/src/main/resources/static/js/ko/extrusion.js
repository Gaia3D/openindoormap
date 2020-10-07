var extrusionTools = function (magoInstance){
	var magoManager = magoInstance.getMagoManager();
	
	/**
	 * 선택된 객체를 마우스로 회전시키는 기능
	 */
	var rotate = new Mago3D.RotateInteraction();
	magoManager.interactionCollection.add(rotate);
	
	/**
	 * 선택된 객체(디자인 레이어)를 마우스로 높낮이 조절하는 기능
	 */
	var upanddown = new Mago3D.NativeUpDownInteraction();
	magoManager.interactionCollection.add(upanddown);
	
	upanddown.on(Mago3D.NativeUpDownInteraction.EVENT_TYPE.CHANGEHEIGHT, function(e){
		console.info(e);
	});
	
	/**
	 * 클릭을 통한 데이터 라이브러리를 지도상에 표출
	 * Mago3D.ClickInteraction 인스턴스 생성 시 handleUpEvent를 등록하면 다른 기능들도 활용 가능.
	 */
	var addStaticModelByPointInteraction = new Mago3D.ClickInteraction({
		handleUpEvent : function(e) {
			var selectedElem = $('#dataLibraryDHTML li.listElement div.data-library.on');
			var dataLibrary = selectedElem.data();
			var cartographic = e.point.geographicCoordinate;
			
			var model = {};
			model.projectId = dataLibrary.id;
			model.projectFolderName = dataLibrary.path;
			
			//to fix
			model.projectFolderName = model.projectFolderName.split(dataLibrary.key)[0];
			model.projectFolderName = model.projectFolderName.replace(/\/+$/, '');
			model.buildingFolderName = 'F4D_'+dataLibrary.key;
			
			if(!this.manager.isExistStaticModel(model.projectId)) this.manager.addStaticModel(model);
			
			this.manager.instantiateStaticModel({
				projectId : model.projectId,
				instanceId : parseInt(Math.random() * 1000),
				longitude : cartographic.longitude,
				latitude : cartographic.latitude,
				height : 40
			});
		}
	}); 
	magoManager.interactionCollection.add(addStaticModelByPointInteraction);
	

	var extrudeLandByClickInteraction = new Mago3D.ClickInteraction({
		handleUpEvent : function(e) {
			
			var viewer = MAGO3D_INSTANCE.getViewer(); 
			var scene = viewer.scene;
	        var pickRay = viewer.camera.getPickRay(e.point.screenCoordinate);

	        //선택된 imageryLayer들이 배열로 넘어옴. 그중에서 선택해서 쓰세요. 이거는 예제라 하나라고 가정하고 진행할게요.
	        var selectedImageryLayers = viewer.imageryLayers.pickImageryLayerInRay(pickRay, scene, function (layer) {
	        	return !layer.isBaseLayer();
	        });
	        
	        if(selectedImageryLayers) {
	  			var selectedImageryLayer = selectedImageryLayers[0];
	  			var imagerProvider = selectedImageryLayer.imageryProvider;
	  			var layerName = imagerProvider.layers;
	  			var currentCqlFilter = imagerProvider._resource.queryParameters.cql_filter;
	  			var geoCoord = e.point.geographicCoordinate;
	  			
	  			var req = new Cesium.Resource({
	  				url : 'http://localhost:18080/geoserver/lhdt/wfs',
	  				queryParameters : {
	  					service : 'wfs',
	  					version : '1.0.0',
	  					request : 'GetFeature',
	  					typeNames : layerName,
	  					srsName : 'EPSG:3857',
	  					outputFormat : 'application/json',
	  					cql_filter : currentCqlFilter + ' AND ' + 'CONTAINS(the_geom, POINT(' + geoCoord.longitude + ' ' + geoCoord.latitude + '))'
	  				}
	  			});
	  			
	  			new Cesium.GeoJsonDataSource().load(req).then(function(e) {
	  				var entities = e.entities.values;
	       	 		
	       	 		for(var i in entities) {
	       	 			var entity = entities[i];
	       	 			var polygonHierarchy  = entity.polygon.hierarchy.getValue().positions;
	       	 			polygonHierarchy.pop();
	       	 			
	       	 			var abc = Mago3D.GeographicCoordsList.fromCartesians(polygonHierarchy.reverse());
	       	 			var objectsArray = MAGO3D_INSTANCE.getMagoManager().modeler.objectsArray;
	       	 			for(var i in objectsArray) {
	       	 				var ob = objectsArray[i];
	       	 				//ob.setLimitationGeographicCoords(abc.geographicCoordsArray);
	       	 				ob.setLimitationHeight(60);
	       	 			}
	       	 			return;
	       	 			/**
			   	 		 * @class Mago3D.ExtrusionBuilding
			   	 		 * Polygon geometry과 높이를 이용하여 건물을 생성
			   	 		 * 
			   	 		 * Mago3D.ExtrusionBuilding의 static method인 makeExtrusionBuildingByCartesian3Array 함수를 통해 빌딩을 생성,
			   	 		 * Cesium의 Cartesian3 배열과 높이, 스타일관련 옵션으로 건물 객체 반환
			   	 		 */
			   	 		var building = Mago3D.ExtrusionBuilding.makeExtrusionBuildingByCartesian3Array(polygonHierarchy.reverse(), 20, {
			   	 			color : new Mago3D.Color(0.2, 0.7, 0.8, 0.4)
			   	 		});
			   	 		
			   	 		building.type = 'land';
			   	 		building.layerId = entity.id; 
			   	 		/**
			   	 		 * magoManager에 속한 modeler 인스턴스의 addObject 메소드를 통해 모델 등록, 뒤의 숫자는 데이터가 표출되는 최소 레벨을 의미. 숫자가 낮을수록 멀리서 보임
			   	 		 */
			   	 		magoManager.modeler.addObject(building, 12);
	       	 			
	       	 		}
	  			});
	        }
		}
	}); 
	magoManager.interactionCollection.add(extrudeLandByClickInteraction);
	var viewer = magoInstance.getViewer();
	
	/**
	 * @class LineDrawer
	 * 선그리기를 통해 비즈니스 로직을 구현할때 사용, 현재 샘플은 선그리기를 통해 데이터 라이브러리들을 표출 시 사용한다.
	 * @param {Cesium.Viewer} viewer
	 * @param {function} drawEndHandler, 사용자가 그린 선들의 위치정보를 결과물로 받음
	 * @param {object} Cesium PolylineGraphics 스타일 옵션
	 */
	var drawDataLibararyByLine = new LineDrawer(viewer, function(linePosition) {
		var selectedElem = $('#dataLibraryDHTML li.listElement div.data-library.on');
		var dataLibrary = selectedElem.data();
		
		var model = {};
		model.projectId = dataLibrary.id;
		model.projectFolderName = dataLibrary.path;
		
		//to fix
		model.projectFolderName = model.projectFolderName.split(dataLibrary.key)[0];
		model.projectFolderName = model.projectFolderName.replace(/\/+$/, '');
		model.buildingFolderName = 'F4D_'+dataLibrary.key;
		
		if(!magoManager.isExistStaticModel(model.projectId)) magoManager.addStaticModel(model);
		
		for(var i=0,len=linePosition.length-1;i<len;i++) {
			var line1 = worldCoordToGeographic(linePosition[i]); 
			var line2 = worldCoordToGeographic(linePosition[i+1]);
			
			var dataPositions = Mago3D.GeographicCoordSegment.getArcInterpolatedGeoCoords(line1, line2, 10);
			var crt3s = dataPositions.map(function(e) {
			    return Cesium.Cartographic.fromDegrees(e.longitude, e.latitude)
			})
			
			var promise = Cesium.sampleTerrain(viewer.terrainProvider, 17, crt3s);
        	promise.then(function(t){
        		for(var j in t) {
        			var bb = t[j];
        			magoManager.instantiateStaticModel({
    					projectId : model.projectId,
    					instanceId : parseInt(Math.random() * 1000),
    					longitude : Cesium.Math.toDegrees(bb.longitude),
    					latitude : Cesium.Math.toDegrees(bb.latitude),
    					height : bb.height
    				});
        		}
        	});
		}
	}, {
		// https://cesium.com/docs/cesiumjs-ref-doc/PolylineGraphics.html?classFilter=LINEGRA
		// positions는 제외.
		lineStyle : {
			width : 3,
			clampToGround : true,
			material :  Cesium.Color.YELLOW
		}
	});
	
	/**
	 * @class PolygonDrawer
	 * 도형 그리기를 통해 비즈니스 로직을 구현할때 사용, 현재 샘플은 도형그리기를 통해 데이터들을 선택할 때 사용
	 * @param {Cesium.Viewer} viewer
	 * @param {function} drawEndHandler, 사용자가 그린 폴리곤의 버텍스 정보를 결과물로 받음
	 * @param {object} Cesium.PolylineGraphics,Cesium.PolygonGraphics  스타일 옵션
	 */
	var selectDataLibararyInArea = new PolygonDrawer(viewer,  function(cartesians) {
		
		/**
		 * Cesium Cartesian3의 array를 이욯하여 Mago3D.Polygon2D 객체 생성
		 */
		var polygon2D = Mago3D.Polygon2D.makePolygonByCartesian3Array(cartesians);
		
		var selectionManager = magoManager.selectionManager;
		
		/**
		 * selectionManager는 mago3d에서 선택된 데이터들을 관리하는 객체.
		 * selectionByPolygon2D 메소드를 이용하여 영역에 포함된 데이터를 찾을 수 있음.
		 */
		var selected = selectionManager.selectionByPolygon2D(polygon2D, 'f4d');
	}/*
	,{
		polygonStyle : {},
		lineStyle : {}
	}
	*/);
	
	var selectExtrusionInArea = new PolygonDrawer(viewer,  function(cartesians) {
		var polygon2D = Mago3D.Polygon2D.makePolygonByCartesian3Array(cartesians);
		
		var selectionManager = magoManager.selectionManager;
		var selected = selectionManager.selectionByPolygon2D(polygon2D, 'native');
	}/*
	,{
		polygonStyle : {},
		lineStyle : {}
	}
	*/); 
		
	init();
	
	function init()
	{
		setEventHandler();
	}

	function extrusionModelToggle(model, on) {
		var requestType = model.ogctype;
		var toggleFunc;
		switch(requestType) {
			case 'wms' : toggleFunc = extrusionModelWMSToggle;break;
			//case 'wfs' : toggleFunc = (model.division === 'building') ? extrusionModelBuildingToggle : extrusionModelEntityToggle;break;
			case 'wfs' : toggleFunc = extrusionModelBuildingToggle;break;
		}
		
		toggleFunc.call(this, model, on);
	}
	
	/**
	 * design_layer의 wms 데이터들을 on/off
	 */
	function extrusionModelWMSToggle(model, on) {
		var imageryLayers = magoInstance.getViewer().imageryLayers;
		if(on) {
			var currentCqlFilter = `design_layer_id=${model.id} AND enable_yn='Y'`;
			var prov = new Cesium.WebMapServiceImageryProvider({
			    url : 'http://localhost:18080/geoserver/lhdt/wms',
			    parameters : {
			    	transparent : true,
			    	srs:'EPSG:4326',
			    	format: "image/png",
			    	cql_filter : currentCqlFilter
			    },
			    layers : model.layername
			});

			var imageryLayer = new Cesium.ImageryLayer(prov/*, {alpha : 0.7}*/);
			imageryLayer.layerId = model.id;
			imageryLayers.add(imageryLayer);
			
			$.ajax({
				url : `/api/design-layers/${model.id}`,
				type: "GET",
	            headers: {"X-Requested-With": "XMLHttpRequest"},
	            dataType: "json",
	            success: function(json){
	            	var req = new Cesium.Resource({
	      				url : 'http://localhost:18080/geoserver/lhdt/wfs',
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
	            	var wfsReq = new Cesium.GeoJsonDataSource().load(req);
	            	
	            	var designLayerGroupType = json.designLayerGroupType;
	            	var viewer = magoInstance.getViewer();
	            	if(designLayerGroupType === 'land') {
	            		wfsReq.then(function(e) {
	          				var entities = e.entities.values;
	          				var ds = new Cesium.CustomDataSource();
	          				ds.labelLayerId = model.id;
	          				
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
	           	 			
	               	 			var positions = entity.polygon.hierarchy.getValue().positions;
	               	 			
	               	 			var center = Cesium.BoundingSphere.fromPoints(positions).center;
	               	 			Cesium.Ellipsoid.WGS84.scaleToGeodeticSurface(center, center);
	               	 		
	               	 			ds.entities.add({
		               	 			position : center,
		               	 			label : {
			               	 			text: labelText,
		           	 					scale :0.5,
		           	 					font: "normal normal bolder 22px Helvetica",
		           	 					fillColor: Cesium.Color.BLACK,
		           	 					outlineColor: Cesium.Color.WHITE,
		           	 					outlineWidth: 1,
		           	 					//scaleByDistance : new Cesium.NearFarScalar(500, 1.2, 1200, 0.0),
		           	 					heightReference : Cesium.HeightReference.CLAMP_TO_GROUND,
		           	 					style: Cesium.LabelStyle.FILL_AND_OUTLINE,
		           	 					//translucencyByDistance : new Cesium.NearFarScalar(1200, 1.0, 2000, 0.0),
		           	 					distanceDisplayCondition : new Cesium.DistanceDisplayCondition(0.0, 2000)
		               	 			}
		          				});
	               	 		}
	               	 		viewer.dataSources.add(ds);
	               	 		ds.clustering.enabled = true;
	               	 		
	               	 		ds.clustering.pixelRange = 2;
	               	 		ds.clustering.minimumClusterSize = 3;
	               	 		ds.clustering.clusterPoints = false;
	               	 		ds.clustering.clusterBillboards = false;
	          			});
	            	} else if (designLayerGroupType === 'building_height') {
	            		var designLayerName = json.designLayerName;
	            		wfsReq.then(function(e) {
	          				var entities = e.entities.values;
	          				var ds = new Cesium.CustomDataSource();
	          				ds.labelLayerId = model.id;
	               	 		for(var i in entities) {
	               	 			var entity = entities[i];
	               	 			var properties = entity.properties;
	               	 			
	               	 			var labelText = designLayerName;
	               	 			var maxFloor = properties.build_maximum_floors.getValue();
	               	 			if(maxFloor) labelText += `\n층수 제한 : ${maxFloor}`;  
	               	 			
	               	 			var positions = entity.polygon.hierarchy.getValue().positions;
	               	 			var center = Cesium.BoundingSphere.fromPoints(positions).center;
	               	 			Cesium.Ellipsoid.WGS84.scaleToGeodeticSurface(center, center);
	               	 			
		               	 		ds.entities.add({
		               	 			position : center,
		               	 			label : {
			               	 			text: labelText,
		           	 					scale :0.5,
		           	 					font: "normal normal bolder 22px Helvetica",
		           	 					fillColor: Cesium.Color.BLACK,
		           	 					outlineColor: Cesium.Color.WHITE,
		           	 					outlineWidth: 1,
		           	 					//scaleByDistance : new Cesium.NearFarScalar(500, 1.2, 1200, 0.0),
		           	 					heightReference : Cesium.HeightReference.CLAMP_TO_GROUND,
		           	 					style: Cesium.LabelStyle.FILL_AND_OUTLINE,
		           	 					//translucencyByDistance : new Cesium.NearFarScalar(1200, 1.0, 2000, 0.0),
		           	 					distanceDisplayCondition : new Cesium.DistanceDisplayCondition(0.0, 2000)
		               	 			}
		          				});
	               	 		}
	               	 		viewer.dataSources.add(ds);
	          			});
	            	}
	            }
			});
		} else {
			var target = imageryLayers._layers.filter(function(layer){return layer.layerId === model.id});
			if(target.length === 1)
			{
				imageryLayers.remove(target[0]);
			}
			var viewer = magoInstance.getViewer();
			
			var dataSources = viewer.dataSources;
			var filter = dataSources._dataSources.filter(function(ds) {
				return ds.labelLayerId  === model.id; 
			})[0];
			
			dataSources.remove(filter, true);
		}
	}
	
	/**
	 * wfs 요청(건물 등의 데이터를 요청할 때 사용) 기본 객체
	 */
	var wfsResource = new Cesium.Resource({
		url : 'http://localhost:18080/geoserver/lhdt/wfs',
		queryParameters : {
			service : 'wfs',
			version : '1.0.0',
			request : 'GetFeature',
			srsName : 'EPSG:3857',
			outputFormat : 'application/json'
		}
	});
	var FLOOR_HEIGHT = 3.3;
	
	function extrusionModelBuildingToggle(model, on) {
		if(on) {
			/**
			 * 위에서 생성한 기본 wfs 요청 객체를 복사,
			 */
			var res = wfsResource.clone();
			
			/**
			 * 대상 레이어를 typeNames에 선언
			 */
			res.queryParameters.typeNames = model.layername;
			/**
			 * 대상 레이어의 id를 찾는 쿼리를  cql_filter에 선언
			 */
			res.queryParameters.cql_filter = `design_layer_id=${model.id} AND enable_yn='Y'`;
			
			/**
			 * Cesium GeoJsonDataSource 클래스를 이용하여 wfs요청. 자동으로 geojson을 파싱하여 객체화 해주어 비즈니스로직만 구현하면 됨.
			 */
			var loader = new Cesium.GeoJsonDataSource().load(res).then(function(e){
				var entities = e.entities.values;
				
	   	 		for(var i in entities) {
	   	 			var entity = entities[i];
	   	 			var properties = entity.properties;
	   	 			
	   	 			var building = entityToMagoExtrusionBuilding(entity, model.layername);
		   	 		building.layerId = model.id;
		   	 		building.floorHeight = FLOOR_HEIGHT;
		   	 		/**
		   	 		 * magoManager에 속한 modeler 인스턴스의 addObject 메소드를 통해 모델 등록, 뒤의 숫자는 데이터가 표출되는 최소 레벨을 의미. 숫자가 낮을수록 멀리서 보임
		   	 		 */
		   	 		magoManager.modeler.addObject(building, 12);
	   	 		}
			});
		} else {
			var modeler = magoManager.modeler;
			
			var modelList = modeler.objectsArray;
			modelList.forEach(function(building){
				if(building.layerId === model.id) {
					/**
		   	 		 * modeler 인스턴스의 removeObject 메소드를 통해 모델 삭제
		   	 		 */
					modeler.removeObject(building);
				}
			});
		}
	}
	
	var DataLibraryController = function(){
		this.drawDataLibararyByPoint = function(on){
			if(on) {
				addStaticModelByPointInteraction.setActive(true);
			} else {
				addStaticModelByPointInteraction.setActive(false);
			}
		}
		this.drawDataLibararyByLine = function(on){
			if(on) {
				drawDataLibararyByLine.setActive(true);
			} else {
				drawDataLibararyByLine.setActive(false);
			}
		}
		this.selectDataLibarary = function(on){
			if(on) {
				/**
				 * magoManager에 defaultSelectInteraction (mago3d 기본 데이터 선택 기능)를 이용하여 데이터를 선택
				 * data library 데이터들은 f4d형태이므로 타겟타입을 f4d로 선언
				 * 
				 * setActive 메소드를 이용하여 기능 활성화/비활성화
				 */
				magoManager.defaultSelectInteraction.setTargetType('f4d');
				magoManager.defaultSelectInteraction.setActive(true);
				magoManager.isCameraMoved = true;
			} else {
				magoManager.defaultSelectInteraction.setActive(false);
			}
		}
		this.selectDataLibararyInArea = function(on){
			if(on) {
				selectDataLibararyInArea.setActive(true);
			} else {
				selectDataLibararyInArea.setActive(false);
			}
		}
		this.translateDataLibarary = function(on){
			if(on) {
				/**
				 * magoManager에 defaultTranslateInteraction (mago3d 기본 데이터 이동 기능)를 이용하여 데이터 이동 기능 사용
				 * 선택된 데이터들을 대상으로 기능이 작동하므로 아래에 selectDataLibarary함수(defaultSelectInteraction 토글) 를 같이 이용
				 * data library 데이터들은 f4d형태이므로 타겟타입을 f4d로 선언
				 * 
				 * setActive 메소드를 이용하여 기능 활성화/비활성화
				 */
				magoManager.defaultTranslateInteraction.setTargetType('f4d');
				magoManager.defaultTranslateInteraction.setActive(true);
			} else {
				magoManager.defaultTranslateInteraction.setActive(false);
			}
			this.selectDataLibarary(on);
		}
		
		this.rotateDataLibarary = function(on){
			if(on) {
				/**
				 * 위에서 선언한 데이터 회전 기능
				 * 선택된 데이터들을 대상으로 기능이 작동하므로 아래에 selectDataLibarary함수(defaultSelectInteraction 토글) 를 같이 이용
				 * data library 데이터들은 f4d형태이므로 타겟타입을 f4d로 선언
				 * 
				 * setActive 메소드를 이용하여 기능 활성화/비활성화
				 */
				rotate.setTargetType('f4d');
				rotate.setActive(true);
			} else {
				rotate.setActive(false);
			}
			this.selectDataLibarary(on);
		}
		
		this.deleteDataLibarary = function(on){
			/**
			 * 선택된 데이터를 삭제
			 * magoManager의 selectionManager 인스턴스가 mago3djs에서 데이터 선택을 관리하고 있음. 
			 * 
			 * f4dController의 deleteF4dMember 메소드를 통해 각 객체를 삭제
			 */
			
			var selectionManager = magoManager.selectionManager;
			var selectedDatas = magoManager.selectionManager.getSelectedF4dNodeArray();
			if(selectedDatas.length === 0) {
				alert('선택된 데이터가 없습니다');
				return;
			}
			for(var i in selectedDatas) {
				var selectedData = selectedDatas[i];
				magoInstance.getF4dController().deleteF4dMember(selectedData.data.projectId, selectedData.data.nodeId);
			}
			magoManager.defaultSelectInteraction.clear();
			selectionManager.clearCurrents();
		}
	};
	
	var ExtrusionModelController = function() {
		this.selectExtrusion = function(on) {
			if(on) {
				magoManager.defaultSelectInteraction.setTargetType('native');
				magoManager.defaultSelectInteraction.setActive(true);
				magoManager.isCameraMoved = true;
			} else {
				magoManager.defaultSelectInteraction.setActive(false);
			}
		}
		this.selectExtrusionInArea = function(on){
			if(on) {
				selectExtrusionInArea.setActive(true);
			} else {
				selectExtrusionInArea.setActive(false);
			}
		}
		/**
		 * 선택된 익스트루전 모델들의 이격거리를 체크 후 가까운 면을 하이라이팅하는 기능. 보완중에 있음. 
		 */
		this.checkDistanceExtrusion = function() {
			var selectionManager = magoManager.selectionManager;
			var selectedDatas = selectionManager.getSelectedGeneralArray();

			var checkCache = {};
			var faceMap = {};

			var loopCnt = selectedDatas.length;
			for(var i=0; i<loopCnt;i++) {
				var m1 = selectedDatas[i];
				var guid1 = m1._guid;
				if(!checkCache[guid1]) checkCache[guid1] = {};
				
				var m1GeoLocationData = m1.getCurrentGeoLocationData();
				var tmat1 = m1GeoLocationData.tMatrix;
				var mesh1 = m1.objectsArray[0];
				var bs1 = mesh1.getBoundingSphere();
				var bs1CenterWC = tmat1.transformPoint3D(bs1.centerPoint);
				var sArray1 = mesh1.surfacesArray;
				var sArray1Length = sArray1.length;
				
				for(var j=0; j<loopCnt; j++) {
					var m2 = selectedDatas[j];
					var guid2 = m2._guid;
					if(!checkCache[guid2]) checkCache[guid2] = {};
					
					if(guid1 === guid2 || checkCache[guid1][guid2] || checkCache[guid2][guid1]) continue;
					checkCache[guid1][guid2] = true;
					checkCache[guid2][guid1] = true;
					
					var m2GeoLocationData = m2.getCurrentGeoLocationData();
					var tmat2 = m2GeoLocationData.tMatrix;
					var mesh2 = m2.objectsArray[0];
					var bs2 = mesh2.getBoundingSphere();
					var bs2CenterWC = tmat2.transformPoint3D(bs2.centerPoint);
					
					var sphereDistance = bs1CenterWC.distToPoint(bs2CenterWC) - bs1.r - bs2.r;
					if(sphereDistance > 0) continue;
					
					var minSurface1;
					var minSurface2;
					var minSurfaceDistance = Infinity;
					
					var sArray2 = mesh2.surfacesArray;
					var sArray2Length = sArray2.length;
					
					for(var k=0;k<sArray1Length;k++) {
						var s1 = sArray1[k];
						var sbs1 = s1.getBoundingSphere();
						var sbs1CenterWC = tmat1.transformPoint3D(sbs1.centerPoint);
						for(var x=0;x<sArray2Length;x++) {
							s2 = sArray2[x];
							var sbs2 = s2.getBoundingSphere();
							var sbs2CenterWC = tmat2.transformPoint3D(sbs2.centerPoint);
							
							var surfaceDistance = sbs1CenterWC.distToPoint(sbs2CenterWC) - sbs1.r - sbs2.r;
							if(minSurfaceDistance > surfaceDistance) {
								minSurface1 = s1;
								minSurface2 = s2;
								minSurfaceDistance = surfaceDistance;
							}
						}
					}
					
					var face1;
					var face2;
					var fArray1 = minSurface1.facesArray;
					var fArray1Length = fArray1.length;
					var fArray2 = minSurface2.facesArray;
					var fArray2Length = fArray2.length;
					var minFaceDistance = Infinity;
					for(var k=0;k<fArray1Length;k++) {
						var f1 = fArray1[k];
						var fbs1 = f1.getBoundingSphere();
						var fbs1CenterWC = tmat1.transformPoint3D(fbs1.centerPoint);
						for(var x=0;x<fArray2Length;x++) {
							f2 = fArray2[x];
							var fbs2 = f2.getBoundingSphere();
							var fbs2CenterWC = tmat2.transformPoint3D(fbs2.centerPoint);
							
							var faceDistance = fbs1CenterWC.distToPoint(fbs2CenterWC) - fbs1.r - fbs2.r;
							if(minFaceDistance > faceDistance) {
								face1 = f1;
								face2 = f2;
								minFaceDistance = faceDistance;
							}
						}
					}
					if(!faceMap[face1._guid]) faceMap[face1._guid] = {face : face1, tMat : tmat1};
					if(!faceMap[face2._guid]) faceMap[face2._guid] = {face : face2, tMat : tmat2};
				}
			}
			var faceCnt = Object.keys(faceMap).length;
			
			if(faceCnt === 0) {
				alert('가까운거 없읍니다..');
				return false;
			}
			
			var r= 0, g=0, b=0;
			//New color every time it's called
			var fadeColor = new Cesium.CallbackProperty(function(time, result){
				    b += 4;
				    if(b > 255) b = 0;
			        return Cesium.Color.fromBytes(0, 0, b, 255,result);
			}, false);
			
			for(var y in faceMap) {
				var face = faceMap[y].face;
				var tmat = faceMap[y].tMat;
				var vertexArray = face.vertexArray;
				var positions = [];
				for(var v in vertexArray) {
					var vertex = vertexArray[v];
					var vP3d = tmat.transformPoint3D(vertex.point3d);
					positions.push(new Cesium.Cartesian3(vP3d.x, vP3d.y, vP3d.z));
				}
				
				magoInstance.getViewer().entities.add(new Cesium.Entity({
					wall : new Cesium.WallGraphics({
						positions : positions,
						material : new Cesium.ColorMaterialProperty(fadeColor)
					})
				}));
			}
			
			selectionManager.clearCurrents();
		}
		this.translateExtrusion = function(on){
			if(on) {
				magoManager.defaultTranslateInteraction.setTargetType('native');
				magoManager.defaultTranslateInteraction.setActive(true);
			} else {
				magoManager.defaultTranslateInteraction.setActive(false);
			}
			this.selectExtrusion(on);
		}
		
		this.rotateExtrusion = function(on){
			if(on) {
				rotate.setTargetType('native');
				rotate.setActive(true);
			} else {
				rotate.setActive(false);
			}
			this.selectExtrusion(on);
		}
		
		this.updownExtrusion = function(on) {
			if(on) {
				upanddown.setActive(true);
			} else {
				upanddown.setActive(false);
			}
			this.selectExtrusion(on);
		}
		
		this.deleteExtrusion = function(){
			/**
			 * 선택된 데이터를 삭제
			 * magoManager의 selectionManager 인스턴스가 mago3djs에서 데이터 선택을 관리하고 있음. 
			 * 
			 * modeler의 removeObject 메소드를 통해 각 객체를 삭제
			 */
			var selectionManager = magoManager.selectionManager;
			var selectedDatas = selectionManager.getSelectedGeneralArray();
			if(selectedDatas.length === 0) {
				alert('선택된 데이터가 없습니다');
				return;
			}
			for(var i in selectedDatas) {
				var selectedData = selectedDatas[i];
				magoManager.modeler.removeObject(selectedData);
			}
			selectionManager.clearCurrents();
			magoManager.defaultSelectInteraction.clear();
		}
		
		this.extrudeLandByClickInteraction = function(on) {
			if(on) {
				extrudeLandByClickInteraction.setActive(true);
			} else {
				extrudeLandByClickInteraction.setActive(false);
			}
		}
		
		this.deleteExtrudeLand = function() {
			
			var selectionManager = magoManager.selectionManager;
			var modeler = magoManager.modeler;
			var lands = modeler.getObjectByKV('type','land');
			
			for(var i in lands) {
				var land = lands[i];
				modeler.removeObject(land);
			}
			selectionManager.clearCurrents();
			magoManager.defaultSelectInteraction.clear();
			magoManager.defaultSelectInteraction.clear();
		}
	}
	
	
	var dataLibraryController = new DataLibraryController();
	var extrusionModelController = new ExtrusionModelController();
	
	function setEventHandler() {
		$('#extrusionModel').on('click','#designLayerDHTML li.listElement div', function() {
			if($(this).hasClass('on')) {
				$(this).removeClass('on').css({
					backgroundColor: '#ffffff'
				});
				extrusionModelToggle($(this).data(), false);
			} else {
				$(this).addClass('on').css({
					backgroundColor: '#ff4422'
				});
			    
				extrusionModelToggle($(this).data(), true);
			}
		});
		
		$('#dataLibrary').on('click','#dataLibraryDHTML li.listElement div.data-library', function() {
			if($(this).hasClass('on')) {
				$(this).removeClass('on').css({
					backgroundColor: '#ffffff'
				});
			} else {
				$('#dataLibraryDHTML li.listElement div.data-library').each(function(){
					$(this).removeClass('on').css({
						backgroundColor: '#ffffff'
					});
				});
				$(this).addClass('on').css({
					backgroundColor: '#ff4422'
				});
			}
		});

		$( "#designLayerDialog" ).on( "dialogclose", function( event, ui ) {
			$('#designLayerDialog .btnGroup button[data-runtype="toggle"]').each(function(){
				if($(this).hasClass('on')) {
					offBtn($(this), extrusionModelController);
				}
			});
		});
		$( "#dataLibraryDialog" ).on( "dialogclose", function( event, ui ) {
			$('#dataLibraryDialog .btnGroup button[data-runtype="toggle"]').each(function(){
				if($(this).hasClass('on')) {
					offBtn($(this), dataLibraryController);
				}
			});
		});
		
		$('#dataLibraryDialog .btnGroup button[data-runtype="toggle"]').click(function(){
			var selected = $('#dataLibraryDHTML li.listElement div.data-library.on');
			if(selected.length === 0) {
				alert('선택된 라이브러리가 없습니다');
				return;
			}
			
			$(this).siblings().each(function(){
				if($(this).hasClass('on')) {
					offBtn($(this), dataLibraryController);
				}
			});
			
			if($(this).hasClass('on')) {
				offBtn($(this), dataLibraryController);
			} else {
				$(this).addClass('on').css({
					color: '#fff',
					border: '1px solid #555',
					backgroundColor: '#555'
				});
				dataLibraryController[$(this).attr('id')](true);
			}
		});
		
		$('#dataLibraryDialog .btnGroup button[data-runtype="run"]').click(function(){
			dataLibraryController[$(this).attr('id')]();
		});
		
		$('#designLayerDialog .btnGroup button[data-runtype="toggle"]').click(function(){
			$(this).siblings().each(function(){
				if($(this).hasClass('on')) {
					offBtn($(this), extrusionModelController);
				}
			});
			
			if($(this).hasClass('on')) {
				offBtn($(this), extrusionModelController);
			} else {
				$(this).addClass('on').css({
					color: '#fff',
					border: '1px solid #555',
					backgroundColor: '#555'
				});
				extrusionModelController[$(this).attr('id')](true);
			}
		});
		
		$('#designLayerDialog .btnGroup button[data-runtype="run"]').click(function(){
			extrusionModelController[$(this).attr('id')]();
		});
		
		function offBtn($obj, instance) {
			$obj.removeClass('on').css({
				color: '#333',
				border: '1px solid #555',
				backgroundColor: '#fff'
			});
			instance[$obj.attr('id')](false);
		}
	}
	
	/**
	 * 스크린픽셀을 월드좌표로 변경
	 * @param screen
	 * @returns {Mago3D.Point3D}
	 */
	function screenToWorldCoord(screen){
		return Mago3D.ManagerUtils.screenCoordToWorldCoord(undefined, screen.x, screen.y, undefined, undefined, undefined, undefined, magoManager);
	}

	/**
	 * 월드좌표를 경위도좌표로 변경
	 * @param wc
	 * @returns {Mago3D.GeographicCoord}
	 */
	function worldCoordToGeographic(wc){
		return Mago3D.ManagerUtils.pointToGeographicCoord(wc, undefined, magoManager);
	}

	/**
	 * 스크린픽셀을 경위도좌표로 변경
	 * @param screen
	 * @returns {Mago3D.Point3D}
	 */
	function screenToGeographicCoord(screen){
		return worldCoordToGeographic(screenToWorldCoord(screen));
	}
	
	function lonlatArrayToPolygon2d(lonlatArray) {
		var geographicCoordsList = new Mago3D.GeographicCoordsList();
		var array = [];
		for(var i in lonlatArray) {
			var lonlat = lonlatArray[i];
			array.push(new Mago3D.GeographicCoord(lonlat.longitude, lonlat.latitude, 0));
		}
		
		return Mago3D.Polygon2D.makePolygonByGeographicCoordArray(array);
	}
};