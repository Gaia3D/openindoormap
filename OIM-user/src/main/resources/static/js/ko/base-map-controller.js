
/**
 * @param {Cesium.Viewer} viewer
 */
var BaseMapController = function(viewer) {
	
	this.ID_PREFIX = 'baseMapType-';
	var NAVER_CREDIT_STRING = '<a href="https://www.naver.com/" target="_blank" style="text-decoration: none !important;">© <span style="display: inline; font-family: Tahoma,sans-serif !important; font-size: 9px !important; font-weight: bold !important; font-style: normal !important; color: #009BC8 !important; text-decoration: none !important;">NHN Corp.</span></a><img class="nmap_logo_map" src="http://static.naver.net/maps2/logo_naver_s.png" width="43" height="9" alt="NAVER">';
	var VENDER_TILES = {
		normal : {
			url : 'https://map.pstatic.net/nrb/styles/basic/1597915238/{z}/{x}/{y}@2x.png?mt=bg.ol.sw',
			credit : NAVER_CREDIT_STRING
		},
		cadstral : {
			url : 'https://map.pstatic.net/nrb/styles/basic/1599725229/{z}/{x}/{y}.png?mt=bg.ol.ts.lp',
			credit : NAVER_CREDIT_STRING
		}
	}
	this.baseMapElementContainer = document.getElementById('baseMapToggle');
	this.toggleClassName = 'on';
	this.viewer = viewer;
	
	var imageryLayers = this.viewer.imageryLayers;
	
	var defaultBaseLayer = imageryLayers._layers.filter(function(l) {
		return l.isBaseLayer();
	})[0];
	defaultBaseLayer.baseMapName = 'base';
	
	
	this.layers = [defaultBaseLayer].concat(venderImageryLayers(this.viewer, VENDER_TILES));
	this.toggle(defaultBaseLayer, 0 ,true);
	
	this.setEventListener(); 
	
	function venderImageryLayers(_viewer, vTiles) {
		var layers = [];
		for(var tileName in vTiles) {
			var vTile = vTiles[tileName];
			var vLayer = new Cesium.ImageryLayer(new Cesium.UrlTemplateImageryProvider({
				url : vTile.url,
				enablePickFeatures : false,
				credit : new Cesium.Credit(vTile.credit, true)
			 }), {
				show : false
			});
			vLayer.baseMapName = tileName;
			layers.push(vLayer);
			_viewer.imageryLayers.add(vLayer);
		}
		return layers;
	}
}

BaseMapController.prototype.toggle = function(layer,index,show) {
	var onName = layer.baseMapName;
	var mapElement = this.baseMapElementContainer.children.namedItem(`${this.ID_PREFIX}${onName}`);
	
	show ? this.on(mapElement) : this.off(mapElement);
}
BaseMapController.prototype.off = function(elem) {
	var check = new RegExp(`(\\s|^)${this.toggleClassName}(\\s|$)`);
	elem.className = elem.className.replace(check, " ").trim();
}
BaseMapController.prototype.on = function(elem) {
	var addtext = this.toggleClassName;
	if(elem.className.length > 0 ) {
		addtext = ' ' + addtext;
	}
	elem.className += addtext;
}

BaseMapController.prototype.setEventListener = function() {
	var viewer = this.viewer; 
	viewer.imageryLayers.layerShownOrHidden.addEventListener(this.toggle.bind(this));
	var mapElementList = this.baseMapElementContainer.children;
	for(var i=0,len=mapElementList.length;i<len;i++) {
		var mapElement = mapElementList.item(i);
		var type = mapElement.dataset.type;
		mapElement.addEventListener('click', function() {
			var thisType = this.dataset.type;
			viewer.imageryLayers._layers.forEach(function(ly){
				if(ly.hasOwnProperty('baseMapName')) {
					ly.show = (ly.baseMapName === thisType) ? true : false;  
				}
			});
		}, false);
	}
}
