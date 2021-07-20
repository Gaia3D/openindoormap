
/**
 * @param {Cesium.Viewer} viewer
 */
var TerrainController = function(viewer, newUrl) {
	
	this.ID_PREFIX = 'terrainType-';
	var TERRAIN_INFO = {
		new : {
			url : 'http://localhost:8888/tilesets/terrain/'
		}
	}
	this.baseMapElementContainer = document.getElementById('terrainToggle');
	this.toggleClassName = 'on';
	this.viewer = viewer;
	
	var currentTerrainProvider = this.viewer.terrainProvider;
	currentTerrainProvider.terrainName = 'base';
	
	var newTerrain = new Cesium.CesiumTerrainProvider({
		url: newUrl
	});
	newTerrain.terrainName = 'new';
	
	this.terrains = {
		base : currentTerrainProvider,
		new : newTerrain
	}
	this.setEventListener(); 
}

TerrainController.prototype.toggle = function(layer,index,show) {
	var onName = layer.baseMapName;
	var mapElement = this.baseMapElementContainer.children.namedItem(`${this.ID_PREFIX}${onName}`);
	
	show ? this.on(mapElement) : this.off(mapElement);
}
TerrainController.prototype.off = function(elem) {
	var check = new RegExp(`(\\s|^)${this.toggleClassName}(\\s|$)`);
	elem.className = elem.className.replace(check, " ").trim();
}
TerrainController.prototype.on = function(elem) {
	var addtext = this.toggleClassName;
	if(elem.className.length > 0 ) {
		addtext = ' ' + addtext;
	}
	elem.className += addtext;
}

TerrainController.prototype.setEventListener = function() {
	var viewer = this.viewer;
	var terrains = this.terrains;
	var terrainElementList = this.baseMapElementContainer.children;
	var that = this;
	for(var i=0,len=terrainElementList.length;i<len;i++) {
		var terrainElement = terrainElementList.item(i);
		terrainElement.addEventListener('click', function() {
			var thisType = this.dataset.type;
			viewer.terrainProvider = terrains[thisType];
			that.on(this);
			var siblings = this.previousElementSibling || this.nextElementSibling;
			that.off(siblings);
			
			MAGO3D_INSTANCE.getMagoManager().modeler.objectsArray.forEach(function(o) {
				o.valid = false;
			});
			
			var hierarchyManager = MAGO3D_INSTANCE.getMagoManager().hierarchyManager;
			var projects = hierarchyManager.projectsMap;
			for(var j in projects) {
				var project = projects[j];
				for(var k in project) {
					var node = project[k];
					if(node instanceof Mago3D.Node && node.data.attributes.isPhysical === true) {
						node.data.valid = false;
					}
				}
			}
		}, false);
	}
}
