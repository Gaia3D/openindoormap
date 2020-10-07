/**
 * @since
 *	20200902 remove viewer. add MAGO3D_INSTANCE
 */
const Ppmap = function () {
};



Ppmap.PointType = {
    CARTESIAN2:0,
    CARTESIAN3:1,
    CARTESIAN4:2,
    LONLAT:3,
}

/**
 */
Ppmap.init = function () {
	//
	console.log(new Date(), '<<Ppmap.init()');
};

/**
 * 
 */
Ppmap.getViewer = function () {
    if(Pp.isNull(MAGO3D_INSTANCE)){
        throw new Error('NULL MAGO3D_INSTANCE');
    }
    //
    return MAGO3D_INSTANCE.getViewer();
}

/**
 * @deprecated 20200902
 */
Ppmap.setViewer = function (viewer) {
    this._viewer = viewer;
}


/**
 * 넓이 계산
 * @param {string} theGeom MULTIPOLYGON 문자열
 */
Ppmap.calcAreaByTheGeom = function(theGeom){
    let landLonLats = Ppmap.Convert.multiPolygonToLonLats(theGeom);

    //
    return Ppmap.calcArea(landLonLats, Ppmap.PointType.LONLAT);
};

/**
 * 넓이 계산
 * @param {Array<LonLat|Cartesian3>} arr 
 * @param {Ppmap.PointType} pointType 
 * @returns {Number} 넓이
 */
Ppmap.calcArea = function(arr, pointType){
    let ctsn3s = [];

    //
    if(Ppmap.PointType.LONLAT === pointType){
        for(let i=0; i<arr.length; i++){
            let lonLat = arr[i];

            let ctsn3 = Ppmap.Convert.lonLatToCtsn3(Pp.nvl(lonLat['lon'], lonLat['longitude']), Pp.nvl(lonLat['lat'], lonLat['latitude']));
            ctsn3s.push(ctsn3);
        }
    }

    //
    if(Ppmap.PointType.CARTESIAN3 === pointType){
        ctsn3s = arr;
    }

    //
    areaInMeters = 0;
    if (ctsn3s.length >= 3)
    {
        var points = [];
        for(var i = 0, len = ctsn3s.length; i < len; i++)
        {
            var cartographic = Cesium.Cartographic.fromCartesian(ctsn3s[i]);
            points.push(new Cesium.Cartesian2(cartographic.longitude, cartographic.latitude));
        }
        if(Cesium.PolygonPipeline.computeWindingOrder2D(points) === Cesium.WindingOrder.CLOCKWISE)
        {
            points.reverse();
        }

        var triangles = Cesium.PolygonPipeline.triangulate(points);

        for(var i = 0, len = triangles.length; i < len; i+=3)
        {
            areaInMeters += Ppmap.calTriangleArea(points[triangles[i]], points[triangles[i + 1]], points[triangles[i + 2]]);
        }
    }
    return areaInMeters;
};



/**
 * 삼각형의 넓이 계산
 * @param {*} t1 
 * @param {*} t2 
 * @param {*} t3 
 * @param {*} i 
 * @returns {Long}
 */
Ppmap.calTriangleArea = function(t1, t2, t3, i) {
    var r = Math.abs(t1.x * (t2.y - t3.y) + t2.x * (t3.y - t1.y) + t3.x * (t1.y - t2.y)) / 2;
    var cartographic = new Cesium.Cartographic((t1.x + t2.x + t3.x) / 3, (t1.y + t2.y + t3.y) / 3);
    var cartesian = _viewer.scene.globe.ellipsoid.cartographicToCartesian(cartographic);
    var magnitude = Cesium.Cartesian3.magnitude(cartesian);

    //
    return r * magnitude * magnitude * Math.cos(cartographic.latitude)
}

/**
 * 커서 백업
 */
Ppmap.backupCursor = function(){
	window['cursor'] = MAGO3D_INSTANCE.getViewer()._container.style.cursor;
};

/**
 * 커서 복원
 */
Ppmap.restoreCursor = function(){
	MAGO3D_INSTANCE.getViewer()._container.style.cursor = window['cursor'];
};

/**
 * 커서 복원
 */
Ppmap.setCursor = function(cursor){
    //
    Ppmap.backupCursor();
    //
	MAGO3D_INSTANCE.getViewer()._container.style.cursor = cursor;
};



/**
 */
Ppmap.getManager = function(){
    if(Pp.isNull(MAGO3D_INSTANCE)){
        throw new Error('NULL MAGO3D_INSTANCE');
    }
    //
    return MAGO3D_INSTANCE.getMagoManager();
};


/**
 * @param {Cartesian3|LonLatAlt} ctsnOrXyz
 * @param {Cartesian3} direction
 */
Ppmap.addDirectionRay = function (ctsnOrXyz, direction) {
    const origin = Ppmap.toCartesian3(ctsnOrXyz);

    const directionRay = Cesium.Cartesian3.multiplyByScalar(direction, 100000, new Cesium.Cartesian3());
    Cesium.Cartesian3.add(origin, directionRay, directionRay);

    MAGO3D_INSTANCE.getViewer().entities.add({
        polyline: {
            positions: [origin, directionRay],
            width: 5,
            material: Cesium.Color.WHITE
        }
    });
}


/**
 * 모든 entity 삭제
 */
Ppmap.removeAll = function(){
	//
	MAGO3D_INSTANCE.getViewer().entities.removeAll();
}


/**
 * entity 삭제
 * @param {Entity} entity 엔티티 인스턴스
 */
Ppmap.removeEntity = function(entity){
	if(Pp.isNull(entity)){
		return;
	}
	
	//
	MAGO3D_INSTANCE.getViewer().entities.remove(entity);
}

/**
 * PointAndLabel 생성
 * @param entityName
 * @param lon
 * @param lat
 * @param option
 * @returns {*}
 */
Ppmap.createPointAndLabel = function(entityName, text, lon, lat, option) {
    let worldPosition = Cesium.Cartesian3.fromDegrees(lon, lat);

    //
    let opt = Pp.extend({}, option);

    var entity = MAGO3D_INSTANCE.getViewer().entities.add({
        name: entityName,
        position: worldPosition,
        label: {
            text: text,
            font: "24px Helvetica",
            fillColor: Cesium.Color.SKYBLUE,
            outlineColor: Cesium.Color.BLACK,
            outlineWidth: 2,
            style: Cesium.LabelStyle.FILL_AND_OUTLINE,
            pixelOffset:  new Cesium.Cartesian2(0, -30),
            scale: 0.6,
            showBackground: true,
            horizontalOrigin: Cesium.HorizontalOrigin.CENTER,
            translucencyByDistance: new Cesium.NearFarScalar(
                1.5e1,
                1.0,
                1.5e4,
                0
            ),
            heightReference: Cesium.HeightReference.CLAMP_TO_GROUND
        },
        point: {
            color: (opt.color ? opt.color : Cesium.Color.RED),
            pixelSize: 10,
            outlineColor: Cesium.Color.YELLOW,
            outlineWidth: 2,
            disableDepthTestDistance: Number.POSITIVE_INFINITY,
            heightReference: Cesium.HeightReference.CLAMP_TO_GROUND
        }
    });
    return entity;
}


/**
 * polyline entity 생성
 * @param {string} entityName
 * @param {array} arr [LonLat,LonLat,...]
 * @param {object} option TODO
 * @returns {Entity}
 */
Ppmap.createPolylineAndLabel = function(entityName,text, lonLats, option) {
    //
    let arr=[];
    //
    for(let i=0; i<lonLats.length; i++){
        let d = lonLats[i];
        //
        arr.push(d.lon);
        arr.push(d.lat);
    }
    var pp = Cesium.Cartesian3.fromDegreesArray(arr);
    var result = new Cesium.Cartesian3();
    Cesium.Cartesian3.midpoint(pp[0], pp[1], result);
    var entity = MAGO3D_INSTANCE.getViewer().entities.add({
        name: entityName,
        position: result,
        label: {
            text: text,
            font: "24px Helvetica",
            fillColor: Cesium.Color.SKYBLUE,
            outlineColor: Cesium.Color.BLACK,
            outlineWidth: 2,
            style: Cesium.LabelStyle.FILL_AND_OUTLINE,
            pixelOffset:  new Cesium.Cartesian2(0, -30),
            scale: 0.6,
            showBackground: true,
            horizontalOrigin: Cesium.HorizontalOrigin.CENTER,
            translucencyByDistance: new Cesium.NearFarScalar(
                1.5e1,
                1.0,
                1.5e4,
                0
            ),
            heightReference: Cesium.HeightReference.CLAMP_TO_GROUND
        },
        polyline: {
            // This callback updates positions each frame.
            positions: new Cesium.CallbackProperty(function() {
                return Cesium.Cartesian3.fromDegreesArray(arr);
            }, false),
            width: 10,
            clampToGround: true,
            material: new Cesium.PolylineOutlineMaterialProperty({
                color: Cesium.Color.YELLOW,
            })
        },
    });

    //
    return entity;
}

/**
 * point entity 생성
 * @param {string} entityName
 * @param {number} lon
 * @param {number} lat
 * @param {object} option TODO
 * @returns {Entity}
 */
Ppmap.createPoint = function(entityName, lon, lat, option) {
    let worldPosition = Cesium.Cartesian3.fromDegrees(lon, lat);

    //
    let opt = Pp.extend({}, option);

    var entity = MAGO3D_INSTANCE.getViewer().entities.add({
        name: entityName,
        position: worldPosition,
        point: {
            color: (opt.color ? opt.color : Cesium.Color.RED),
            pixelSize: 10,
            outlineColor: Cesium.Color.YELLOW,
            outlineWidth: 2,
            disableDepthTestDistance: Number.POSITIVE_INFINITY,
            heightReference: Cesium.HeightReference.CLAMP_TO_GROUND
        }
    });
    return entity;
}

/**
 * point entity 생성
 * @param {string} entityName
 * @param {number} lon
 * @param {number} lat
 * @param {object} option TODO
 * @returns {Entity}
 */
Ppmap.createPointAndAlt = function(entityName, lon, lat, alt, option) {
    let worldPosition = Cesium.Cartesian3.fromDegrees(lon, lat, alt);

    //
    let opt = Pp.extend({}, option);

    var entity = MAGO3D_INSTANCE.getViewer().entities.add({
        name: entityName,
        position: worldPosition,
        point: {
            color: (opt.color ? opt.color : Cesium.Color.RED),
            pixelSize: 10,
            outlineColor: Cesium.Color.YELLOW,
            outlineWidth: 2,
            disableDepthTestDistance: Number.POSITIVE_INFINITY,
            heightReference: Cesium.HeightReference.CLAMP_TO_GROUND
        }
    });
    return entity;
}


/**
 * polyline entity 생성
 * @param {string} entityName
 * @param {array} arr [LonLat,LonLat,...]
 * @param {object} option TODO
 * @returns {Entity}
 */
Ppmap.createPolyline = function(entityName, lonLats, option) {
	//
	let arr=[];
	//
	for(let i=0; i<lonLats.length; i++){
		let d = lonLats[i];
		//
		arr.push(d.lon);
		arr.push(d.lat);
	}
	
	//	
    var entity = MAGO3D_INSTANCE.getViewer().entities.add({
		name: entityName,
		 polyline: {
            // This callback updates positions each frame.
            positions: new Cesium.CallbackProperty(function() {
				return Cesium.Cartesian3.fromDegreesArray(arr);                    
            }, false),
            width: 10,
            clampToGround: true,
            material: new Cesium.PolylineOutlineMaterialProperty({
                color: Cesium.Color.YELLOW,
            })
        },
	});

	//
    return entity;
}


/**
 * cartesian2를 LonLat을 변환
 */
Ppmap.cartesian2ToLonLat = function(ctsn2){
	const ctsn3 = MAGO3D_INSTANCE.getViewer().scene.pickPosition(ctsn2);
	const cartographic = Cesium.Cartographic.fromCartesian(ctsn3);
	
	//
	return Ppmap.cartoToLonLat(cartographic);
};



/**
 * cartesian2를 LonLat을 변환
 */
Ppmap.cartesian2ToLonLatAlt = function(ctsn2){
	const ctsn3 = MAGO3D_INSTANCE.getViewer().scene.pickPosition(ctsn2);
	const cartographic = Cesium.Cartographic.fromCartesian(ctsn3);

	//
	return Ppmap.cartoToLonLatAlt(cartographic);
};


/**
 cartographic을 {'lon', 'lat'}으로 변환
 * @param {Cartographic} cartographic
 * @returns {LonLat}
 */
Ppmap.cartoToLonLat = function(cartographic){
    if(Pp.isEmpty(cartographic)){
        return {
            'lon': NaN,
            'lat': NaN,
        }
    }

    //
    return {
        'lon':Cesium.Math.toDegrees(cartographic.longitude),
        'lat': Cesium.Math.toDegrees(cartographic.latitude)
    };
}



/**
 cartographic을 {'lon', 'lat', 'alt'}으로 변환
 * @param {Cartographic} cartographic
 * @returns {LonLatAlt}
 */
Ppmap.cartoToLonLatAlt = function(cartographic){
    if(Pp.isEmpty(cartographic)){
        return {
            'lon': NaN,
            'lat': NaN,
            'alt': NaN
        }
    }

    //
    return {
        'lon':Cesium.Math.toDegrees(cartographic.longitude),
        'lat': Cesium.Math.toDegrees(cartographic.latitude),
        'alt': cartographic.height
    };
}


/**
 * 현재 카메라 상태 추출
 * @returns {object} {'position', 'direction', 'up', 'right', 'transform', 'frustum'}
 * @since 20200904 init
 */
Ppmap.getCameraStatus = function() {
    const camera = MAGO3D_INSTANCE.getViewer().camera;
    const json = {
        position: camera.position.clone(),
        direction: camera.direction.clone(),
        up: camera.up.clone(),
        right: camera.right.clone(),
        transform: camera.transform.clone(),
        frustum: camera.frustum.clone()
    }
    return json;
}


/**
 * 카메라 상태값에 의한 flyTo
 * @param {object} status 카메라 상태. Ppmap.getCameraStatus()의 리턴값 참조
 * @since 20200904 init
 */
Ppmap.flyToByCameraStatus = function(status) {
    MAGO3D_INSTANCE.getViewer().camera.flyTo({
        destination : status.position,
        orientation : {
            direction : status.direction,
            up : status.up,
            right : status.right,
        }
    });
}



/**
 * @param {Cartesian3|LonLatAlt} ctsn1
 * @param {Cartesian3|LonLatAlt} ctsn2
 * @returns {Cartesian3} 
 */
Ppmap.getDirection = function (ctsn1, ctsn2) {
    //
    const origin = Ppmap.toCartesian3(ctsn1);
    const target = Ppmap.toCartesian3(ctsn2);

    //
    const direction = Cesium.Cartesian3.subtract(target, origin, new Cesium.Cartesian3());
    //
    Cesium.Cartesian3.normalize(direction, direction);

    //
    return direction;
}




/**
* header값 계산 & 리턴
* @see https://stackoverflow.com/questions/58323971/cesium-calculate-heading-and-pitch-from-2-cartesian3-points
* @param {Cartesian3|LonLatAlt} pointA  
* @param {Cartesian3|LonLatAlt} pointB  
* @return {Number}
*/
Ppmap.getHeading = function (pointA, pointB) {
    //
    const ctsnA = Ppmap.toCartesian3(pointA);
    const ctsnB = Ppmap.toCartesian3(pointB);

    //		
    const transform = Cesium.Transforms.eastNorthUpToFixedFrame(ctsnA);
    const positionvector = Cesium.Cartesian3.subtract(ctsnB, ctsnA, new Cesium.Cartesian3());
    const vector = Cesium.Matrix4.multiplyByPointAsVector(Cesium.Matrix4.inverse(transform, new Cesium.Matrix4()), positionvector, new Cesium.Cartesian3());
    const direction = Cesium.Cartesian3.normalize(vector, new Cesium.Cartesian3());
    //heading
    const heading = Math.atan2(direction.y, direction.x) - Cesium.Math.PI_OVER_TWO;
    //pitch
    const pitch = Cesium.Math.PI_OVER_TWO - Cesium.Math.acosClamped(direction.z);

    //
    return Cesium.Math.toDegrees(Cesium.Math.TWO_PI - Cesium.Math.zeroToTwoPi(heading));
}



/**
 * 해당 위치로 이동
 * @param {Cartesian3|lonLatAlt} ctsnOrXyz
 * @param {HeadingPitchRoll} hpr
 * @param {object} option {'duration':number}
 * @param {Function} callbackFn flyTo완료 후 호출할 콜백함수. 옵션
 */
Ppmap.flyTo = function (ctsnOrXyz, hpr, option, callbackFn) {
    const ctsn = Ppmap.toCartesian3(ctsnOrXyz);

    //
    let opt = {
        destination: ctsn,
        orientation: {
            heading: hpr.heading,
            pitch: hpr.pitch,
            roll: hpr.roll
        }
    };
    //
    if (null != option && undefined != option && Pp.isNotEmpty(option.duration)) {
        opt.duration = option.duration;
    }
    //
    if (null != callbackFn && undefined != callbackFn) {
        opt.complete = callbackFn;
    }

    //
    MAGO3D_INSTANCE.getViewer().scene.camera.flyTo(opt);
}




/**
 * degree object인지 여부
 * obj는 json형식 && lon라는 키 존재하면 degree라고 판단
 * @param {any} obj
 * @returns {boolean}
 */
Ppmap.isDegree = function (obj) {
    if ('object' !== typeof (obj)) {
        return false;
    }

    //
    return (undefined !== obj['lon'] ? true : false);
}


/**
 * 지도 화면 캡처
 * @param callbackFn 스크린샷 후 호출할 콜백함수
 */
Ppmap.captureMap = function (callbackFn) {
    
    var targetResolutionScale = 1.0;
    var timeout = 500; // in ms

    // define callback functions
    var prepareScreenshot = function () {
        MAGO3D_INSTANCE.getViewer().resolutionScale = targetResolutionScale;
        MAGO3D_INSTANCE.getViewer().scene.preRender.removeEventListener(prepareScreenshot);
        // take snapshot after defined timeout to allow scene update (ie. loading data)
        //startLoading();
        setTimeout(function () {
            MAGO3D_INSTANCE.getViewer().scene.postRender.addEventListener(takeScreenshot);
        }, timeout);
    }

    //
    var takeScreenshot = function () {
        MAGO3D_INSTANCE.getViewer().scene.postRender.removeEventListener(takeScreenshot);
        var canvas = MAGO3D_INSTANCE.getViewer().scene.canvas;
        canvas.toBlob(function (blob) {
            MAGO3D_INSTANCE.getViewer().resolutionScale = 1.0;

            console.log(blob);
            callbackFn(blob);
        });
    }

    //
    MAGO3D_INSTANCE.getViewer().scene.preRender.addEventListener(prepareScreenshot);
}


/**
 * 파라미터를 Cartesian3로 변환
 * @param {any} 가변적 Cartesian3 or LonLatAlt or lon,lat,alt
 * @returns {Cartesian3}
 */
Ppmap.toCartesian3 = function () {
    const args = arguments;


    if (0 === args.length) {
        throw Error('');
    }

    //
    if (1 === args.length && args[0] instanceof (Cesium.Cartesian3)) {
        return args[0];
    }

    //
    if (1 === args.length && Ppmap.isDegree(args[0])) {
        return Cesium.Cartesian3.fromDegrees(args[0].lon, args[0].lat, args[0].alt || 0.0);
    }

    //
    const lon = args[0];
    const lat = args[1];
    const alt = args[2] || 0.0;

    //
    return Cesium.Cartesian3.fromDegrees(lon, lat, alt);
}


/**
* 지도 방향? 초기화
*/
Ppmap.resetRotate = function (callbackFn) {
    let json={};
	json.destination = MAGO3D_INSTANCE.getViewer().scene.camera.positionWC;
	json.duration = 1;
	if(Pp.isNotEmpty(callbackFn)){
		json.complete = callbackFn;
	}
	
    MAGO3D_INSTANCE.getViewer().scene.camera.flyTo(json);
}


/**
 * 이벤트 삭제
 * @param {array|string} arr 이벤트타입의 배열 또는 'all' 문자열
 * @since 20200909 init
 */
Ppmap.removeInputAction = function(arrOrString){
    if(Pp.isEmpty(arrOrString)){
        return;
    }

    //
    let handler = new Cesium.ScreenSpaceEventHandler(MAGO3D_INSTANCE.getViewer().scene.canvas);

    //
    if('string' === arrOrString && 'ALL' === arrOrString.toUpperCase()){
        let arr = [];
        arr.push(Cesium.ScreenSpaceEventType.LEFT_DOWN);
        arr.push(Cesium.ScreenSpaceEventType.LEFT_UP);
        arr.push(Cesium.ScreenSpaceEventType.LEFT_CLICK);
        arr.push(Cesium.ScreenSpaceEventType.LEFT_DOUBLE_CLICK);
        arr.push(Cesium.ScreenSpaceEventType.MOUSE_MOVE);
        arr.push(Cesium.ScreenSpaceEventType.RIGHT_CDOWN);
        arr.push(Cesium.ScreenSpaceEventType.RIGHT_UP);
        arr.push(Cesium.ScreenSpaceEventType.RIGHT_CLICK);

        //
        Ppmap.removeInputAction(arr);
        return;
    }

    //
    for(let i=0; i<arrOrString.length; i++){
        let eventType = arrOrString[i];

        //
        if('number' === eventType){
            handler.removeInputAction(eventType);
            return;
        }        
    }
};


/**
 * zoomTo with headingPitchRoll
 */
Ppmap.zoomTo = function(entity){
	//
	let heading = 0.0;
	let pitch = Cesium.Math.toRadians(-90);
	let roll = 0.0;
	
	//	
	MAGO3D_INSTANCE.getViewer().zoomTo(entity, new Cesium.HeadingPitchRoll(heading, pitch, roll) );
}


/**
 * lonLats의 중심 좌표 구하기
 * @param {Array<LonLat>} lonLats lonlat 목록
 * @returns {LonLat}
 */
Ppmap.getCenterLonLatByLonLats = function(lonLats){
    //
    let minLon = 999.0, maxLon = -999.0;
    let minLat = 999.0, maxLat = -999.0;
    //
    for(let i=0; i<lonLats.length; i++){
        let lonLat = lonLats[i];

        //
        if(minLon > lonLat.longitude){
            minLon = lonLat.longitude;
        }
        if(maxLon < lonLat.longitude){
            maxLon = lonLat.longitude;
        }
        //
        if(minLat > lonLat.latitude){
            minLat = lonLat.latitude;
        }
        if(maxLat < lonLat.latitude){
            maxLat = lonLat.latitude;
        }
    }

    //
    // console.log(minLon, maxLon, minLat, maxLat);
    let lonLat = {
        'longitude': ((maxLon-minLon)/2) + minLon,
        'latitude': ((maxLat-minLat)/2) + minLat,
    }
    lonLat.lon = lonLat.longitude;
    lonLat.lat = lonLat.latitude;

    return lonLat;
};


/**
 * 변환 전문
 */
Ppmap.Convert = {
    /**
     * cartesian2 => cartesian3
     * @param {Cartesian2} ctsn2 카티시안2
     */
    ctsn2ToCtsn3:function(ctsn2){
        return MAGO3D_INSTANCE.getViewer().scene.pickPosition(ctsn2);
    },

    
    /**
     * cartesian2 => cartographic
     * @param {Cartesian2} ctsn2 
     */
    ctsn2ToCartographic: function(ctsn2){
        return Cesium.Cartographic.fromCartesian(this.ctsn2ToCtsn3(ctsn2));
    },


    /**
     * cartesian2 => LonLat
     * @param {Cartesian2} ctsn2 
     */
    ctsn2ToLonLat: function(ctsn2){
        //
        let ctsn3 = this.ctsn2ToCtsn3(ctsn2);
        //
        let cartographic = this.ctsn3ToCartographic(ctsn3);
        //
        return this.cartographicToLonLat(cartographic);
    },


    /**
     * cartesian3 => cartographic
     * @param {Cartesian3} ctsn3 
     */
    ctsn3ToCartographic: function(ctsn3){
        return Cesium.Cartographic.fromCartesian(ctsn3);
    },

    /**
     * cartesian3 => LonLat
     * @param {Cartesian3} ctsn3 
     */
    ctsn3ToLonLat: function(ctsn3){
        //
        let cartographic = this.ctsn3ToCartographic(ctsn3);
        //
        return this.cartographicToLonLat(cartographic);
    },
    
    /**
     * cartographic => LonLat
     * @param {Cartographic} cartographic 
     */
    cartographicToLonLat: function(cartographic){
        //
        return {
            'lon':Cesium.Math.toDegrees(cartographic.longitude),
            'lat': Cesium.Math.toDegrees(cartographic.latitude),
            'longitude':Cesium.Math.toDegrees(cartographic.longitude),
            'latitude': Cesium.Math.toDegrees(cartographic.latitude),
        };
    },

    /**
     * LonLat을 Cartesian3로 변환
     * @param {number} lon 
     * @param {number} lat 
     * @param {number|null} alt 
     * @returns {Cartesian3}
     */
    lonLatToCtsn3: function(lon, lat, alt){
        return Cesium.Cartesian3.fromDegrees(lon, lat);
    },

	toLonLat: function(lon, lat, alt)   {
		return {
			'lon': lon,
			'lat': lat,
			'alt': (alt?alt:0),
		}
    },
    

    /**
     * theGeom(MultiPolygon)을 Array<LonLat>로 변환
     * @param {String} theGeom 
     * @returns {Array<LonLat>}
     */
    multiPolygonToLonLats: function(theGeom){
        let multiPolygon = Terraformer.WKT.parse(theGeom);
        let arr = multiPolygon.coordinates[0][0];

        //
        let lonLats=[];
        for(let i=0; i<arr.length; i++){
            let d = arr[i];
            //
            lonLats.push({
                'lon': d[0],
                'longitude': d[0],
                'lat': d[1],
                'latitude': d[1],
            })
        }

        //
        return lonLats;
    },

    /**
     * @see multiPolygonToLonLats
     * @param {string} theGeom 
     */
    theGeomStringToLonLats : function(theGeom){
        return this.multiPolygonToLonLats(theGeom);
    }

};