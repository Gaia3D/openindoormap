Mago3D.ManagerUtils.geographicToWkt = function(geographic, type) {
	var wkt = '';
	
	switch(type) {
		case 'POINT' : {
			wkt = 'POINT (';
			wkt += geographic.longitude;
			wkt += ' ';
			wkt += geographic.latitude;
			wkt += ')';
			break;
		}
		case 'LINE' : {
			wkt = 'LINESTRING (';
			for(var i=0,len=geographic.length;i<len;i++) {
				if(i>0) {
					wkt += ',';
				}
				wkt += geographic[i].longitude;
				wkt += ' ';
				wkt += geographic[i].latitude;
			}
			wkt += ')';
			break;
		}
		case 'POLYGON' : {
			wkt = 'POLYGON ((';
			for(var i=0,len=geographic.length;i<len;i++) {
				if(i>0) {
					wkt += ',';
				}
				wkt += geographic[i].longitude;
				wkt += ' ';
				wkt += geographic[i].latitude;
			}
			wkt += ',';
			wkt += geographic[0].longitude;
			wkt += ' ';
			wkt += geographic[0].latitude;
			wkt += '))';
			break;
		}
	}
	
	function coordToString(coord,str) {
		var text = str ? str : '';
		if(Array.isArray(coord)) {
			for(var i=0,len=coord.length;i<len;i++) {
				coordToString(coord[i],text);
			}
		} else {
			if(text) {
				text += ',';
			}
			text += coord.longitude;
			text += ' ';
			text += coord.latitude;
		}
		
		return text;
	}
	
	return wkt;
}

Mago3D.ManagerUtils.getCoordinateFromWKT = function(wkt, type) {
	switch(type) {
		case 'POINT' : {
			var removePrefix = wkt.replace(/\bpoint\b\s*\(/i, "");
			var removeSuffix = removePrefix.replace(/\s*\)\s*$/, "");
			var coordinates = removeSuffix.match(/[+-]?\d*(\.?\d+)/g);
			return coordinates;
		}
	}
}

Mago3D.MagoManager.prototype.validTerrainHeight = function() {
	var allVisible = this.frustumVolumeControl.getAllVisiblesObject();
	var nodeMap = allVisible.nodeMap;
	var nativeMap = allVisible.nativeMap;
	var that = this;
	
	var nodeArray = [];
	for(var k in nodeMap) {
		if(nodeMap.hasOwnProperty(k)) {
			var node = nodeMap[k];
			var data = node.data;
			if(!node.bboxAbsoluteCenterPos || data.valid) continue;
			nodeArray.push(node);
			node.data.valid = true;
		}
	}
	
	if(nodeArray.length > 0) {
		new Mago3D.Promise(function(resolve) {
			resolve({mm:that,nArray:nodeArray});
		}).then(function(obj){
			var cartographics = [];
			var nArray = obj.nArray;
			for(var i=0,len=nArray.length;i<len;i++ )
			{
				var node = nArray[i];
				var bbox = node.getBBox();
				var gg = Mago3D.ManagerUtils.pointToGeographicCoord(node.bboxAbsoluteCenterPos);
				cartographics.push(Cesium.Cartographic.fromDegrees(gg.longitude, gg.latitude));
			}
			var viewer = MAGO3D_INSTANCE.getViewer();
			var promise = Cesium.sampleTerrain(viewer.terrainProvider, 17, cartographics);
			promise.then(function(samplePositions){
				if(samplePositions.length === nArray.length) {
					for(var j=0,len=samplePositions.length;j<len;j++) {
						var n = nArray[j];
						var cp = n.getCurrentGeoLocationData().geographicCoord;
						var bx = n.getBBox();
						n.changeLocationAndRotation(cp.latitude, cp.longitude, samplePositions[j].height - bx.minZ, 0,0,0, obj.mm);
					}
				}
			});
		});
	}
	
	var modelArray = [];
	for(var k in nativeMap) {
		if(nativeMap.hasOwnProperty(k)) {
			var model = nativeMap[k];
			if(model.valid) continue;
			
			model.valid = true;
			modelArray.push(model);
		}
	}
	if(modelArray.length > 0) {
		new Mago3D.Promise(function(resolve) {
			resolve(modelArray);
		}).then(function(mArray){
			var cartographics = [];
			var mArrayLength = mArray.length;
			for(var j=0;j<mArrayLength;j++) {
				var m = mArray[j];
				var geoLocData = m.getCurrentGeoLocationData();
				var geoCoord = geoLocData.geographicCoord;
				
				cartographics.push(Cesium.Cartographic.fromDegrees(geoCoord.longitude, geoCoord.latitude));
			}
			var viewer = MAGO3D_INSTANCE.getViewer();
			var promise = Cesium.sampleTerrain(viewer.terrainProvider, 17, cartographics);
			promise.then(function(samplePositions){
				if(samplePositions.length === mArray.length) {
					for(var k=0,len=samplePositions.length;k<len;k++) {
						mArray[k].setTerrainHeight(samplePositions[k].height);
					}
				}
			});
		});	
	}
}