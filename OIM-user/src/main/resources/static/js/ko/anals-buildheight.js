var AnalsBuildHeight = function(viewer, magoInstance) {
    var magoManager = magoInstance.getMagoManager();
    this._viewer = viewer;
    this._scene = viewer.scene;

    this._polylines = [];
    this._labels = [];
    this._polyPoint = [];

    var handler = null;

    var drawingMode = undefined;
    var activeShapePoints = [];
    var activeShape;
    var activeLabel;


    $('#heightAvgToggle').change(function() {
        clearObj();
        clearMagoInit();
        if($('#heightAvgToggle').is(':checked')) {
            changeLightingAPI(MAGO3D_INSTANCE, 0.3);
            toastr.info('지도상에서 여러 점을 클릭하시기 바랍니다.');
            drawingMode = 'heightAvgAnals';
            startDrawPolyLine();
        } else {
            drawingMode = "";
        }
    });

    function clearObj() {
        _polylines.forEach(obj => viewer.entities.remove(obj));
        _labels.forEach(obj => viewer.entities.remove(obj));
        _polyPoint.forEach(obj => viewer.entities.remove(obj));
        _polylines = [];
        _labels = [];
        _polyPoint = [];
    }

    function clearMagoInit() {
        deleteAllChangeColorAPI(magoInstance);
        changeLightingAPI(MAGO3D_INSTANCE, 0.7);
    }

    function getColor(v, min, max) {
        function getC(f, l, r) {
            return {
                r: Math.floor((1 - f) * l.r + f * r.r),
                g: Math.floor((1 - f) * l.g + f * r.g),
                b: Math.floor((1 - f) * l.b + f * r.b),
            };
        }

        var left = { r: 127, g: 191, b: 253 },
            middle = { r: 246, g: 128, b: 251 },
            right = { r: 250, g: 134, b: 127 },
            mid = (max - min) / 2;

        return v < min + mid ?
            getC((v - min) / mid, left, middle) :
            getC((v - min - mid) / mid, middle, right);
    }
    $('#heightAvgBtn').click(function() {
        clearObj();
        clearMagoInit()
    });

    function analsHeight() {
        const geometryInfo = [];
        for(let i=0; i<_polyPoint.length; i++){
            let d = _polyPoint[i];
            geometryInfo.push({'longitude': d.lon, 'latitude': d.lat});
        }
        let d = _polyPoint[0];
        geometryInfo.push({'longitude': d.lon, 'latitude': d.lat});

        const param = {
            geometryInfo: geometryInfo
        };

        //
        $.ajax({
            url: "/api/geometry/intersection/datas",
            type: "POST",
            data: JSON.stringify(param),
            dataType: 'json',
            contentType: 'application/json;charset=utf-8'
        }).done(function(data) {
            if(Pp.isEmpty(data) || Pp.isEmpty(data._embedded)){
                console.log('empty data', data);
                return;
            }

            const dataInfos = data._embedded.dataInfos;
            min = 0;
            max = dataInfos.length
            for(let p in dataInfos) {
                const obj = dataInfos[p];
                const color = getColor(p, min, max);
                console.log(color);
                // data_group_id = 2, master datakey = MasterPlan
                // changeColorAPI(magoInstance, obj.dataGroupId,  'F4D_'+ obj.dataKey, null,
                //     'isPhysical=true', color.r + ',' + color.g + ',' + color.b)
                changeColorAPI(magoInstance, obj.dataGroupId,  obj.dataKey, null,
                    'isPhysical=true', color.r + ',' + color.g + ',' + color.b)
            }
        });
    }

    function startDrawPolyLine() {
        handler = new Cesium.ScreenSpaceEventHandler(viewer.canvas);
        var dynamicPositions = new Cesium.CallbackProperty(function () {
            if(drawingMode === 'heightAvgAnals') {
                return new Cesium.PolygonHierarchy(activeShapePoints);
            } else {
                return activeShapePoints;
            }
        }, false);

        handler.setInputAction(function(event) {
            if(drawingMode === 'heightAvgAnals') {
                var earthPosition = viewer.scene.pickPosition(event.position);
                if (Cesium.defined(earthPosition)) {
                    var cartographic = Cesium.Cartographic.fromCartesian(earthPosition);
                    var tempPosition = Cesium.Cartesian3.fromDegrees(Cesium.Math.toDegrees(cartographic.longitude), Cesium.Math.toDegrees(cartographic.latitude));
                    activeShapePoints.push(tempPosition);

                    if (activeShapePoints.length === 1) {
                        activeShape = drawShape(dynamicPositions);
                        if (drawingMode === 'heightAvgAnals') {
                            activeLabel = viewer.entities.add({
                                name     : "TempLabel for area measurement",
                                position: dynamicCenter,
                                label: {
                                    text: dynamicLabel,
                                    font: 'bold 20px sans-serif',
                                    fillColor: Cesium.Color.BLUE,
                                    style: Cesium.LabelStyle.FILL,
                                    verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
                                    disableDepthTestDistance: Number.POSITIVE_INFINITY,
                                    heightReference: Cesium.HeightReference.CLAMP_TO_GROUND
                                }
                            });
                        }
                    }
                    else {
                        this._labels.push(drawLabel(tempPosition));
                    }
                    this._polyPoint.push({
                        lon: Cesium.Math.toDegrees(cartographic.longitude),
                        lat: Cesium.Math.toDegrees(cartographic.latitude)
                    });
                    this._polylines.push(createPoint(tempPosition));
                }
            }
        }, Cesium.ScreenSpaceEventType.LEFT_CLICK);

        handler.setInputAction(function (event) {
            if(drawingMode === 'heightAvgAnals') {
                terminateShape();
                analsHeight();
                $('#heightAvgToggle').click();
                toastr.info('평균 높이 분석을 시작합니다');
            }
        }, Cesium.ScreenSpaceEventType.RIGHT_CLICK);
    }

    // Redraw the shape so it's not dynamic and remove the dynamic shape.
    function terminateShape() {
        // activeShapePoints.pop();
        lengthInMeters = 0;
        areaInMeters = 0
        this._polylines.push(drawShape(activeShapePoints));
        if (drawingMode === 'heightAvgAnals')  this._labels.push(drawAreaLabel());

        viewer.entities.remove(activeShape);
        viewer.entities.remove(activeLabel);

        activeShape = undefined;
        activeLabel = undefined;
        activeShapePoints = [];
    }

    function drawAreaLabel() {
        var label;
        var bs = Cesium.BoundingSphere.fromPoints(activeShapePoints);
        var position = Cesium.Ellipsoid.WGS84.scaleToGeodeticSurface(bs.center);
        var text = getArea(activeShapePoints);

        label = viewer.entities.add({
            name     : "Label for area measurement",
            position: position,
            label: {
                text: text,
                font: 'bold 20px sans-serif',
                fillColor: Cesium.Color.BLUE,
                style: Cesium.LabelStyle.FILL,
                verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
                disableDepthTestDistance: Number.POSITIVE_INFINITY,
                heightReference: Cesium.HeightReference.CLAMP_TO_GROUND
            }
        });

        return label;
    }

    function drawShape(positionData) {
        var shape;
        if (drawingMode === 'heightAvgAnals') {
            shape = viewer.entities.add({
                name     : "Polygon for area measurement",
                polygon: {
                    hierarchy: positionData,
                    material: new Cesium.ColorMaterialProperty(Cesium.Color.YELLOW.withAlpha(0.3)),
                    /* height: 0.1, */
                    //heightReference: Cesium.HeightReference.CLAMP_TO_GROUND
                }
            });
        }
        return shape;
    }

    function drawLabel(positionData) {
        var label;
        // if (drawingMode === 'line') {
        label = viewer.entities.add({
            position: positionData,
            label: {
                text: getLineLength(activeShapePoints),
                font: 'bold 20px sans-serif',
                fillColor: Cesium.Color.YELLOW,
                style: Cesium.LabelStyle.FILL,
                verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
                disableDepthTestDistance: Number.POSITIVE_INFINITY,
                heightReference: Cesium.HeightReference.CLAMP_TO_GROUND/*
				 * ,
				 * pixelOffset :
				 * new
				 * Cesium.Cartesian2(5,
				 * 20)
				 */
            }
        });
        // }
        return label;
    }

    function createPoint(worldPosition) {
        var entity = viewer.entities.add({
            position: worldPosition,
            point: {
                color: Cesium.Color.YELLOW,
                pixelSize: 5,
                outlineColor: Cesium.Color.BLACK,
                outlineWidth: 2,
                disableDepthTestDistance: Number.POSITIVE_INFINITY,
                heightReference: Cesium.HeightReference.CLAMP_TO_GROUND
            }
        });
        return entity;
    }

    var dynamicCenter = new Cesium.CallbackProperty(function () {
        var bs = Cesium.BoundingSphere.fromPoints(activeShapePoints);
        return Cesium.Ellipsoid.WGS84.scaleToGeodeticSurface(bs.center);
    }, false);

    var dynamicLabel = new Cesium.CallbackProperty(function () {
        return getArea(activeShapePoints);
    }, false);

    function getArea(positions) {
        areaInMeters = 0;
        if (positions.length >= 3)
        {
            var points = [];
            for(var i = 0, len = positions.length; i < len; i++)
            {
                // points.push(Cesium.Cartesian2.fromCartesian3(positions[i]));
                var cartographic = Cesium.Cartographic.fromCartesian(positions[i]);
                points.push(new Cesium.Cartesian2(cartographic.longitude, cartographic.latitude));
            }
            if(Cesium.PolygonPipeline.computeWindingOrder2D(points) === Cesium.WindingOrder.CLOCKWISE)
            {
                points.reverse();
            }

            var triangles = Cesium.PolygonPipeline.triangulate(points);

            for(var i = 0, len = triangles.length; i < len; i+=3)
            {
                // areaInMeters +=
                // Cesium.PolygonPipeline.computeArea2D([points[triangles[i]],
                // points[triangles[i + 1]], points[triangles[i + 2]]]);
                areaInMeters += calArea(points[triangles[i]], points[triangles[i + 1]], points[triangles[i + 2]]);
            }
        }
        return formatArea(areaInMeters);
    }
    function calArea(t1, t2, t3, i) {
        var r = Math.abs(t1.x * (t2.y - t3.y) + t2.x * (t3.y - t1.y) + t3.x * (t1.y - t2.y)) / 2;
        var cartographic = new Cesium.Cartographic((t1.x + t2.x + t3.x) / 3, (t1.y + t2.y + t3.y) / 3);
        var cartesian = viewer.scene.globe.ellipsoid.cartographicToCartesian(cartographic);
        var magnitude = Cesium.Cartesian3.magnitude(cartesian);
        return r * magnitude * magnitude * Math.cos(cartographic.latitude)
    }

    function getLineLength(positions) {
        lengthInMeters = 0;
        for (var i = 1, len = positions.length; i < len; i++) {
            var startPoint = positions[i - 1];
            var endPoint = positions[i];

            lengthInMeters += Cesium.Cartesian3.distance(startPoint, endPoint);
        }
        return formatDistance(lengthInMeters);
    }

    const wktManager = {
        geoByPoint: function(geographic) {
            let wkt = 'POINT (';
            wkt += geographic.lon;
            wkt += ' ';
            wkt += geographic.lat;
            wkt += ')';
            return wkt;
        },
        geoByLINE: function(geographicList) {
            let wkt = 'LINESTRING (';
            for(let i=0,len=geographicList.length;i<len;i++) {
                if(i>0) {
                    wkt += ',';
                }
                wkt += geographic[i].lon;
                wkt += ' ';
                wkt += geographic[i].lat;
            }
            wkt += ')';
            return wkt;
        },
        geoByPOLYGON: function(geographicList) {
            let wkt = 'POLYGON ((';
            for(var i=0,len=geographicList.length;i<len;i++) {
                if(i>0) {
                    wkt += ',';
                }
                wkt += geographicList[i].lon;
                wkt += ' ';
                wkt += geographicList[i].lat;
            }
            wkt += ',';
            wkt += geographicList[0].lon;
            wkt += ' ';
            wkt += geographicList[0].lat;
            wkt += '))';
            return wkt;
        }
    }
};