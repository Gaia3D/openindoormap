var AnalsCityPlanning = function(viewer, magoInstance) {
    var magoManager = magoInstance.getMagoManager();
    this._viewer = viewer;
    this._scene = viewer.scene;
    startDrawPolyLine();

    var viewModel = {
        standardFloorCount: 0,
        buildingHeight: 20,
        buildingAdjust: 0,
    };
    var allObject = {};
    $('#cityPlanAreaTestPoint').click(function(e) {
    });

    $("#inputBuildingHeight").change(()=> {
        floorAreaRatioCalc();
    });
    $("#inputCustomizing").change(() => {
        floorAreaRatioCalc();
    });

    $('#cityPlanAreaTestBtn').click(function(e) {
        const url = "http://'+IP+':8090/anals/cityplanning/area";
        const obj = {
            width : 5,
            leadTime : 0,
            trailTime : 100,
            resolution : 5,
            strokeWidth: 0,
            stroke: Cesium.Color.AQUA.withAlpha(0.0),
            fill: Cesium.Color.AQUA.withAlpha(0.8),
            clampToGround: true
        };

        $.ajax({
            url: url,
            method: 'GET',
            dataType: 'json'
        }).done(function(datas) {
            for ( var obj of datas ) {
                Cesium.GeoJsonDataSource.load(JSON.parse(obj.st_asgeojson))
                    .then(dataSource => {
                        let entitis = dataSource.entities._entities._array;
                        for(let index in entitis) {
                            let entitiyObj = entitis[index];
                            let registeredEntity = _viewer.entities.add(entitiyObj);
                            registeredEntity.name = "sejong_church1";

                            registeredEntity.polygon.extrudedHeightReference = 1;
                            registeredEntity.polygon.heightReference = 2;

                            // Cesium.knockout.getObservable(viewModel, 'standardFloorCount').subscribe(
                            //     function(newValue) {
                            //         registeredEntity.polygon.extrudedHeight = newValue * 4;
                            //     }
                            // );
                            allObject[val].terrain = registeredEntity;
                        }
                        // settingDistrictDisplay();
                        // settingBuildingShadow();
                        _viewer.selectedEntity = allObject[pickedName].terrain;
                    });
            }
        }).fail(function(xhr, status, errorThrown) {
            console.log(status);
        })
    });
    function settingDistrictDisplay() {
        if (allObject[pickedName].terrain.show) {
            $("#districtDisplay").val("enable");
        } else {
            $("#districtDisplay").val("disable");
        }
    }
    function settingBuildingShadow() {
        if (allObject[pickedName].shadowView) {
            $("#buildingShadow").val("enable");
        } else {
            $("#buildingShadow").val("disable");
        }
    }
    // 모든 빌딩들의 연면적 합
    function totalAreaCalc(entityArray) {
        let sum = 0;
        entityArray.forEach(entity => {
            sum += entity.totalBuildingFloorArea;
        });
        return sum;
    }

    // 건폐율 계산 및 view (건축면적 / 대지면적)
    function buildingToLandRatioCalc() {
        if (pickedName === "") {
            alert("오브젝트를 먼저 선택해 주시기 바랍니다.");
            return;
        }
        let plottage = parseFloat(allObject[pickedName].plottage); // 대지면적
        let totalFloorArea = parseFloat(allObject[pickedName].totalFloorArea); // 총 건축면적

        if (plottage === 0.0) {
            return;
        }
        let result = (totalFloorArea / plottage) * 100.0;
        if ($("#selectDistrict").val() === "ecodelta_district") {
            result /= 4;
        }
        $("#curBuildingToLandRatio").val(result.toFixed(2));
    }
    // 용적율 계산 및 view (연면적 / 대지면적)
    function floorAreaRatioCalc() {
        if (pickedName === "") {
            alert("오브젝트를 먼저 선택해 주시기 바랍니다.");
            return;
        }
        let plottage = parseFloat(allObject[pickedName].plottage); // 대지면적
        let totalArea = totalAreaCalc(allObject[pickedName].buildings); // 총 연면적

        if (plottage === 0.0) {
            return;
        }
        let result = (totalArea / plottage) * 100.0;
        if ($("#selectDistrict").val() === "ecodelta_district") {
            result /= 10;
        }
        $("#curFloorAreaRatio").val(result.toFixed(2));
    }
    function startDrawPolyLine() {
        handler = new Cesium.ScreenSpaceEventHandler(viewer.canvas);

        handler.setInputAction(function (event) {
            var earthPosition = viewer.scene.pickPosition(event.position);
            if (Cesium.defined(earthPosition)) {
                var cartographic = Cesium.Cartographic.fromCartesian(earthPosition);
                var tempPosition = Cesium.Cartesian3.fromDegrees(Cesium.Math.toDegrees(cartographic.longitude), Cesium.Math.toDegrees(cartographic.latitude));
                console.log(Cesium.Math.toDegrees(cartographic.longitude), Cesium.Math.toDegrees(cartographic.latitude))

            }
        }, Cesium.ScreenSpaceEventType.LEFT_CLICK);

        handler.setInputAction(function (event) {
            console.log('RIGHT CLICK');
        }, Cesium.ScreenSpaceEventType.RIGHT_CLICK);
    }

    //input cartesian position
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

};