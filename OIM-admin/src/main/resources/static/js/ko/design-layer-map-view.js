/**
 * 지도 객체 생성
 */
var mapViewer = function(policy, layer, versionId, extent) {

    if (!(this instanceof mapViewer)) {
        throw new Error("New 를 통해 생성 하십시오.");
    }
    
    // geoPolicy 정보
    var geoserverDataUrl = policy.geoserverDataUrl;
	var geoserverDataWorkspace = policy.geoserverDataWorkspace;
	var geoserverDataStore = policy.geoserverDataStore;
	var coordinate = policy.layerTargetCoordinate;
	
	// cql_flter값 : versionId가 있을 경우 해당 버전 보여주고 없을경우 현재 활성화된 레이어 보여줌 
	var layerKey = layer.designLayerKey;
	var designlayerId = layer.designLayerId;
	var queryString = (versionId > 0) ? "version_id=" + versionId  :"enable_yn='Y'";
	queryString = "design_layer_id=" + designlayerId + " and " + queryString;
	var layerName = geoserverDataWorkspace + ":" + layerKey;
	var layerParam = {
			'VERSION' : '1.1.1',
			tiled: true,
            srs: coordinate,
            layers: [layerName],
			cql_filter : queryString
	};

	extent = extent.split(",").map(function(a){return Number(a);})
	// 배경 지도는 임시로 OSM으로.
	var layers = [
		/*
		new ol.layer.Tile({
			source: new ol.source.OSM()
		}),
		 */

		new ol.layer.Image({
			id: layerKey,
			visible: true,
			source: new ol.source.ImageWMS({
				url: geoserverDataUrl + '/' + geoserverDataWorkspace + '/wms',
				params: layerParam
			})
		})
	];

	var proj = new ol.proj.Projection({
		code: coordinate,
		units: 'm',
		global: false,
		extent: extent
	});

	var view = new ol.View({
		zoom: 1,
		maxZoom: 20,
		center : ol.extent.getCenter(extent),
		projection : proj
	});

	var map = new ol.Map({
		layers: layers,
		target: 'map',
		view : view
	});
};
