const OccupancySensorThings = function (magoInstance) {
    SensorThings.call(this, magoInstance);

    this.magoInstance = magoInstance;
    this.type = 'iot_occupancy';
    this.observedProperty = 'occupancyBuild';
    this.observedPropertyColor = {
        'occupancy': '#E91E63',
        'occupancyBuild': '#FF9800',
        'occupancyFloor': '#2196F3'
    };
    this.mappingTable = {
        2683 : {
            dataId : 200003,
            baseFloor: 7
        },
        2834 : {
            dataId : 200002,
            baseFloor: 0
        }
    };
    this.occupancyGradeMin = 0;
    this.occupancyGradeMax = 10;

    this.currentTime = "2020-11-17T12:15:00.000Z";
    //this.currentTime = moment.utc().format();
    this.callInterval = 10;         // 10s
    this.filterInterval = 120;      // 120s

    this.gaugeChartNeedle = {};
    this.occupancyChart = {};
    this.chartTitle = '재실자(Occupancy)';
    this.chartXAxesTitle = '시간(분)';
    this.chartYAxesTitle = '재실자(명)';

    this.selectedBuildingId = 0;
    this.selectedDataKey = '';
    this.cellSpaceList = {};
    this.selectedFloorSensorList = [];

    this.chartOptions = {
        responsive: true,
        legend: {
            position: 'bottom',
            labels: {
                fontSize: 10,
                usePointStyle: true
            }
        },
        title: {
            display: true,
            text: this.chartTitle
        },
        tooltips: {
            mode: 'index',
            intersect: false,
        },
        hover: {
            mode: 'nearest',
            intersect: true
        },
        scales: {
            xAxes: [{
                type: 'time',
                time: {
                    parser: "YYYY-MM-DD HH:mm:ss",
                    second: 'mm:ss',
                    minute: 'HH:mm',
                    hour: 'HH:mm',
                    day: 'MMM DD',
                    month: 'YYYY MMM',
                    tooltipFormat: 'YYYY-MM-DD HH:mm',
                    displayFormats: {
                        second: 'HH:mm:ss a'
                    }
                },
                display: true,
                scaleLabel: {
                    display: true,
                    labelString: this.chartXAxesTitle
                }
            }],
            yAxes: [{
                display: true,
                scaleLabel: {
                    display: true,
                    labelString: this.chartYAxesTitle
                }
            }]
        }
    };
};
OccupancySensorThings.prototype = Object.create(SensorThings.prototype);
OccupancySensorThings.prototype.constructor = OccupancySensorThings;

OccupancySensorThings.prototype.getGrade = function (value) {
    // Math.floor(Math.random() * (max - min)) + min
    const max = 519, min = 250;
    const percent = (value - min) / (max - min) * 100;
    let grade = 0;
    if (percent >= 0 && percent <= 20) {
        grade = 1;
    } else if (percent > 20 && percent <= 50) {
        grade = 2;
    } else if (percent > 50 && percent <= 80) {
        grade = 3;
    } else if (percent > 80) {
        grade = 4;
    }
    return grade;
};

OccupancySensorThings.prototype.getOccupancyColor = function (value) {
    if (value === 0) {
        return "30,144,255,200";
    } else if (value < 3 && value >= 1) {
        return "0,199,60,200";
    } else if (value >= 3 && value < 6) {
        return "255,215,0,200";
    } else if (value >= 6) {
        return "255,89,89,200";
    }
};

OccupancySensorThings.prototype.getFloorText = function(floor, baseFloor) {
    let floorText = 0;
    floor = floor - baseFloor;
    if (floor >= 0) {
        floorText = floor + 1;
    } else {
        floorText = 'B' + Math.abs(floor);
    }
    return floorText;
}

OccupancySensorThings.prototype.getDataInfoResultProcess = function (promises, callback) {
    $.when.apply($, promises)
        .done(callback)
        .fail(function (e) {
            alert(JS_MESSAGE["ajax.error.message"]);
        })
        .always(function (e) {
        });
}

OccupancySensorThings.prototype.ajaxDataInfo = function(dataId) {
    return $.ajax({
        url: '/datas/' + dataId,
        type: "GET",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        dataType: "json"
    });
};

OccupancySensorThings.prototype.ajaxThingInfo = function(thingId) {
    const _this = this;
    //const observedProperty = 'occupancyBuild';
    const queryString = 'Things(' + thingId + ')?$select=@iot.id,name,description' +
        '&$expand=Locations($select=@iot.id,location,name),' +
        'Datastreams(' +
            '$select=@iot.id,description,unitOfMeasurement' +
            //'$filter=ObservedProperty/name eq \'' + observedProperty + '\'' +
        '),' +
        'Datastreams/Observations(' +
            '$select=result,phenomenonTime,resultTime;' +
            '$orderby=resultTime desc;' +
            '$filter=resultTime lt ' + _this.getCurrentTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
        ')';

    return $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"}
    });
};

OccupancySensorThings.prototype.getList = function (pageNo, params) {

    const _this = this;
    pageNo = parseInt(pageNo);
    _this.currentPageNo = pageNo;
    const skip = (pageNo - 1) * 5;
    const observedProperty = 'occupancyBuild';

    let filter = 'Datastreams/ObservedProperty/name eq \'' + observedProperty + '\'';
    if (params.searchValue) {
        filter += 'and (startswith(name, \'' + params.searchValue + '\') or endswith(name, \'' + params.searchValue + '\'))';
    }

    const queryString = 'Things?$select=@iot.id,name,description' +
        '&$top=5&$skip=' + skip + '&$count=true&$orderby=name asc&$filter=' + filter +
        '&$expand=Locations($select=@iot.id,location,name),' +
            'Datastreams(' +
                '$select=@iot.id,description,unitOfMeasurement;' +
                '$filter=ObservedProperty/name eq \'' + observedProperty + '\'' +
            '),' +
            'Datastreams/Observations(' +
                '$select=result,phenomenonTime,resultTime;' +
                '$orderby=resultTime desc;' +
                '$filter=resultTime lt ' + _this.getCurrentTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
            ')';

    $.ajax({
        // http://localhost:8888/FROST-Server/v1.0/Things?$select=@iot.id,name,description&$top=5&$count=true&$orderby=name asc&$filter=Datastreams/ObservedProperty/name eq 'occupancy' and (startswith(name, '1') or endswith(name, '1'))&$expand=Locations($select=@iot.id,location,name),Datastreams($select=@iot.id,description,unitOfMeasurement;$filter=ObservedProperty/name eq 'occupancy'),Datastreams/Observations($select=result,phenomenonTime,resultTime;$orderby=resultTime desc;$filter=resultTime lt 2020-11-03T05:00:00.000Z and resultTime ge 2020-11-03T04:00:00.000Z)
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {

            const pagination = new Pagination(pageNo, msg['@iot.count'], 5, msg['@iot.nextLink']);
            msg.pagination = pagination;

            const templateLegend = Handlebars.compile($("#iotLegendSource").html());
            $("#iotLegendDHTML").html("").append(templateLegend(_this));

            const data = {
                promises: [],
                thingsContent: {}
            };

            const things = msg.value;
            for (const thing of things) {

                const thingId = thing['@iot.id'];

                // Datastreams
                const dataStreams = thing['Datastreams'];
                if (!dataStreams || dataStreams.length <= 0) continue;
                const dataStream = dataStreams[0];

                // Observations
                const observations = dataStream['Observations'];
                let value = '-', grade = 0;
                if (observations && observations.length > 0) {
                    const observationTop = observations[0];
                    value = observationTop.result.value;
                    grade = observationTop.result.grade;
                }

                // TODO thingId와 dataId 맵핑테이블을 통한 데이터 조회
                const dataId = _this.mappingTable[thingId]['dataId'];
                data.promises.push(_this.ajaxDataInfo(dataId));
                data.thingsContent[dataId] = {
                    id: thingId,
                    name: thing.name,
                    value: value,
                    valueWithCommas: _this.numberWithCommas(value),
                    unit: _this.getUnit(dataStream),
                    grade: grade,
                    gradeText: _this.getGradeMessage(grade)
                };

            }

            _this.getDataInfoResultProcess(data.promises, function() {

                msg.contents = [];

                for (const argument of arguments) {
                    const dataInfo = argument[0]['dataInfo'];
                    const dataId = dataInfo.dataId;
                    const thingsContent = data.thingsContent[dataId];
                    msg.contents.push(Object.assign(thingsContent, dataInfo));
                }

                const templateSearchSummary = Handlebars.compile($("#searchSummarySource").html());
                $("#iotSearchSummaryDHTML").html("").append(templateSearchSummary(msg));

                const template = Handlebars.compile($("#occupancyListSource").html());
                $("#iotOccupancyListDHTML").html("").append(template(msg));

                const templatePagination = Handlebars.compile($("#paginationSource").html());
                $("#iotPaginationDHTML").html("").append(templatePagination(msg));

                $('#iotOccupancyListDHTML').show();

            });

        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });

};

/**
 * 재실자 건물 더보기 조회
 * @param obj
 * @param thingId
 */
OccupancySensorThings.prototype.getDetail = function(obj, thingId) {

    const _this = this;
    const queryString = 'Datastreams?$select=@iot.id,description,name,unitOfMeasurement' +
        '&$filter=Things/@iot.id eq ' + thingId +
        '&$orderby=ObservedProperty/@iot.id asc' +
        '&$expand=ObservedProperty($select=name),' +
            'Observations(' +
                '$select=result,resultTime;' +
                '$orderby=resultTime desc;' +
                '$filter=resultTime lt ' + _this.getCurrentTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
            ')';

    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {

            const contents = {dataStreams: []};

            // Datastreams
            const dataStreams = msg.value;
            if (!dataStreams || dataStreams.length <= 0) return;
            for (const dataStream of dataStreams) {

                // Observations
                const observations = dataStream['Observations'];
                let value = '-';
                if (observations && observations.length > 0) {
                    const observationTop = observations[0];
                    value = observationTop.result.value;
                }
                contents.dataStreams.push({
                    name: dataStream.name,
                    value: _this.numberWithCommas(value),
                    unit: _this.getUnit(dataStream)
                });

            }

            // 더보기 템플릿 생성
            const $iotDustMoreDHTML = $(obj).parent().siblings(".iotDustMoreDHTML");
            const template = Handlebars.compile($("#dustMoreSource").html());
            $iotDustMoreDHTML.html("").append(template(contents));
            $iotDustMoreDHTML.show();

            // 더보기 보이기/숨기기 (순서를 바꾸지 마세요!)
            $(".show-more").not($(obj)).show();
            $(obj).hide();
            $('.iotDustMoreDHTML').not($iotDustMoreDHTML).hide();

        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });
};

OccupancySensorThings.prototype.addOverlay = function () {

    const _this = this;

    // 모든 건물
    let observedProperty = 'occupancyBuild';
    let filter = 'Datastreams/ObservedProperty/name eq \'' + observedProperty + '\'';

    // TODO 화면 영역에 해당하는 Location을 필터링하여 호출하도록 수정
    const queryString = 'Things?$select=@iot.id,name,description&$top=1000' +
        '&$filter=' + filter +
        '&$expand=Locations($select=@iot.id,location,name),' +
            'Datastreams(' +
                '$select=@iot.id,description,unitOfMeasurement;' +
                '$filter=ObservedProperty/name eq \'' + observedProperty + '\'' +
            '),' +
            'Datastreams/Observations(' +
                '$select=result,phenomenonTime,resultTime;' +
                '$orderby=resultTime desc;' +
                '$filter=resultTime lt ' + _this.getCurrentTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
            ')';

    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {
            _this.things = msg.value;
            _this.setCellSpaceList();
            _this.redrawOverlay();
        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });
};

OccupancySensorThings.prototype.setCellSpaceList = function() {

    const _this = this;
    const data = {
        promises: []
    };

    for (const thing of _this.things) {

        const thingId = parseInt(thing['@iot.id']);

        // TODO thingId와 dataId 맵핑테이블을 통한 데이터 조회
        const dataId = _this.mappingTable[thingId]['dataId'];
        data.promises.push(_this.ajaxDataInfo(dataId));

    }

    _this.getDataInfoResultProcess(data.promises, function() {

        for (const argument of arguments) {
            const dataInfo = argument[0]['dataInfo'];

            const dataId = dataInfo.dataId;
            const dataGroupId = dataInfo.dataGroupId;
            const dataKey = dataInfo.dataKey;

            const magoManager = _this.magoInstance.getMagoManager();
            const nodeData = magoManager.hierarchyManager.getNodeByDataKey(dataGroupId, dataKey).data;
            const projectFolderName = nodeData.projectFolderName;
            const cellSpaceListFileName = dataKey + '_cellspacelist.json';

            $.ajax({
                url: '/f4d/' + projectFolderName + '/' + cellSpaceListFileName,
                type: "GET",
                dataType: "json",
                headers: {"X-Requested-With": "XMLHttpRequest"},
                success: function (msg) {
                    _this.cellSpaceList[dataId] = msg;
                },
                error: function (request, status, error) {
                    alert(JS_MESSAGE["ajax.error.message"]);
                }
            });
        }

    });

};

OccupancySensorThings.prototype.redrawOverlayBuilding = function() {
    const _this = this;
    const data = {
        promises: [],
        thingsContent: {}
    };
    for (const thing of _this.things) {
        const thingId = parseInt(thing['@iot.id']);

        // Datastreams
        const dataStreams = thing['Datastreams'];
        if (!dataStreams || dataStreams.length <= 0) continue;
        const dataStream = dataStreams[0];

        // Observations
        const observations = dataStream['Observations'];
        let value = '-', grade = 0, selected = '';
        if (observations && observations.length > 0) {
            const observationTop = observations[0];
            value = _this.formatValueByDigits(observationTop.result.value, 3);
            grade = observationTop.result.grade;
        }
        const gradeText = _this.getGradeMessage(grade);
        if (_this.selectedThingId == thingId) {
            selected = 'on';
        }

        const dataId = _this.mappingTable[thingId]['dataId'];
        data.promises.push(_this.ajaxDataInfo(dataId));
        data.thingsContent[dataId] = {
            id: thingId,
            value: value,
            valueWithCommas: _this.numberWithCommas(value),
            unit: _this.getUnit(dataStream),
            //addr: addr,
            grade: grade,
            gradeText: gradeText,
            selected: selected,
            subTitle: JS_MESSAGE["iot.occupancy"]
        };
    }

    _this.getDataInfoResultProcess(data.promises, function() {
        const contents = {
            things : []
        };
        for (const argument of arguments) {
            const dataInfo = argument[0]['dataInfo'];
            const dataId = dataInfo.dataId;
            const dataName = dataInfo.dataName;
            const longitude = dataInfo.longitude;
            const latitude = dataInfo.latitude;
            const altitude = 0;

            const coordinates = [longitude, latitude, altitude];
            const resultScreenCoord = _this.geographicCoordToScreenCoord(coordinates);

            // 지도화면 픽셀정보 구하기
            const $containerSelector = $('#magoContainer');
            //const $overlaySelector = $('.overlayDHTML').find('#overlay_' + locationId);
            const top = 0, left = 0;
            let bottom = top + $containerSelector.outerHeight() - 55;
            let right = left + $containerSelector.outerWidth() - 160;

            // 화면 밖에 있는 관측소는 스킵
            if ((resultScreenCoord.x < left || resultScreenCoord.x > right) ||
                (resultScreenCoord.y < top || resultScreenCoord.y > bottom)) {
                continue;
            }

            const positionInfo = {
                stationName: dataName,
                top: resultScreenCoord.y,
                left: resultScreenCoord.x
            };

            const thingsContent = data.thingsContent[dataId];
            contents.things.push(Object.assign(thingsContent, positionInfo));
        }
        const template = Handlebars.compile($("#overlaySource").html());
        $('#overlayDHTML').html("").append(template(contents));
    });

};

OccupancySensorThings.prototype.redrawOverlayFloor = function() {

    const _this = this;
    _this.selectedFloorSensorList = [];
    const dataId = _this.mappingTable[_this.selectedBuildingId]['dataId'];
    const data = {
        promises: [ _this.ajaxDataInfo(dataId) ],
        thingsContents: []
    };

    for (const thing of _this.things) {
        const thingId = parseInt(thing['@iot.id']);

        // Datastreams
        const dataStreams = thing['Datastreams'];
        if (!dataStreams || dataStreams.length <= 0) continue;
        const dataStream = dataStreams[0];

        // Observations
        const observations = dataStream['Observations'];
        let value = '-', grade = 0, selected = '';
        if (observations && observations.length > 0) {
            const observationTop = observations[0];
            value = _this.formatValueByDigits(observationTop.result.value, 3);
            grade = observationTop.result.grade;
        }
        const gradeText = _this.getGradeMessage(grade);
        if (_this.selectedThingId == thingId) {
            selected = 'on';
        }

        const cellId = thing['properties']['cell'];
        _this.selectedFloorSensorList.push(cellId);
        const cellSpace = _this.cellSpaceList[dataId][cellId];
        const localCoordinate = {x: cellSpace.x, y: cellSpace.y, z: cellSpace.z};

        data.thingsContents.push({
            id: thingId,
            value: value,
            valueWithCommas: _this.numberWithCommas(value),
            unit: _this.getUnit(dataStream),
            //addr: addr,
            grade: grade,
            gradeText: gradeText,
            selected: selected,
            subTitle: JS_MESSAGE["iot.occupancy"],
            localCoordinate: localCoordinate,
            stationName: cellId
        });

    }

    _this.getDataInfoResultProcess(data.promises, function(msg) {
        const contents = {
            things : []
        };

        const dataInfo = msg['dataInfo'];
        const dataGroupId = dataInfo.dataGroupId;

        for (const thingsContent of data.thingsContents) {

            const localCoordinate = thingsContent['localCoordinate'];
            const magoManager = _this.magoInstance.getMagoManager();
            const targetNode = magoManager.hierarchyManager.getNodeByDataKey(dataGroupId, _this.selectedDataKey);
            const targetNodeGeoLocDataManager = targetNode.getNodeGeoLocDataManager();
            const targetNodeGeoLocData = targetNodeGeoLocDataManager.getCurrentGeoLocationData();
            const tempGlobalCoordinateObject = targetNodeGeoLocData.localCoordToWorldCoord(localCoordinate);
            const wgs84CoordinateObject = Mago3D.Globe.CartesianToGeographicWgs84(tempGlobalCoordinateObject.x, tempGlobalCoordinateObject.y, tempGlobalCoordinateObject.z);

            const longitude = Number(wgs84CoordinateObject.longitude);
            const latitude = Number(wgs84CoordinateObject.latitude);
            const altitude = Number(wgs84CoordinateObject.altitude);

            const coordinates = [longitude, latitude, altitude];
            const resultScreenCoord = _this.geographicCoordToScreenCoord(coordinates);

            // 지도화면 픽셀정보 구하기
            const $containerSelector = $('#magoContainer');
            //const $overlaySelector = $('.overlayDHTML').find('#overlay_' + locationId);
            const top = 0, left = 0;
            let bottom = top + $containerSelector.outerHeight() - 55;
            let right = left + $containerSelector.outerWidth() - 160;

            // 화면 밖에 있는 관측소는 스킵
            if ((resultScreenCoord.x < left || resultScreenCoord.x > right) ||
                (resultScreenCoord.y < top || resultScreenCoord.y > bottom)) {
                continue;
            }

            const positionInfo = {
                top: resultScreenCoord.y,
                left: resultScreenCoord.x
            };

            contents.things.push(Object.assign(thingsContent, positionInfo));

            const rgbColorCode = _this.getOccupancyColor(thingsContent.value);
            changeColorAPI(_this.magoInstance.getMagoManager(), dataGroupId, _this.selectedDataKey, [thingsContent.stationName], "isPhysical=true", rgbColorCode);

        }
        const template = Handlebars.compile($("#overlaySource").html());
        $('#overlayDHTML').html("").append(template(contents));
    });
};

OccupancySensorThings.prototype.redrawOverlay = function () {
    const _this = this;
    if (_this.observedProperty === 'occupancyBuild') {
        // 건물별 오버레이
        _this.redrawOverlayBuilding();
    } else if (_this.observedProperty === 'occupancyFloor'){
        // 층별 오버레이
        _this.redrawOverlayFloor();
    }
};



OccupancySensorThings.prototype.getInformation = function(thingId) {

    const _this = this;

    // TODO thingId와 dataId 맵핑테이블을 통한 데이터 조회
    let dataId = '', baseFloor = 0;
    if (_this.observedProperty === 'occupancyBuild') {
        dataId = _this.mappingTable[thingId]['dataId'];
        baseFloor = _this.mappingTable[thingId]['baseFloor'];
    } else if (_this.observedProperty === 'occupancyFloor') {
        dataId = _this.mappingTable[_this.selectedBuildingId]['dataId'];
        baseFloor = _this.mappingTable[_this.selectedBuildingId]['baseFloor'];
    }

    const promises = [];
    promises.push(_this.ajaxDataInfo(dataId));
    promises.push(_this.ajaxThingInfo(thingId));

    _this.getDataInfoResultProcess(promises, function(msg1, msg2) {

        const dataInfo = msg1[0]['dataInfo'];   // DataInfo
        const thing = msg2[0];                  // Thing

        // Datastreams
        const dataStreams = thing['Datastreams'];
        const dataStream = dataStreams[0];

        // Observations
        const observations = dataStream['Observations'];
        let value = '-', grade = 0;
        if (observations && observations.length > 0) {
            const observationTop = observations[0];
            value = observationTop.result.value;
            grade = observationTop.result.grade;
        }

        const content = {
            id: thingId,
            name: thing.name,
            value: value,
            valueWithCommas: _this.numberWithCommas(value),
            unit: _this.getUnit(dataStream),
            grade: grade,
            gradeText: _this.getGradeMessage(grade),
            dataName: dataInfo.dataName,
            dataGroupId: dataInfo.dataGroupId,
            dataKey: dataInfo.dataKey,
            baseFloor: baseFloor,
            listOfFloorOccupancy: []
        };

        _this.selectedThingId = thingId;
        if (_this.observedProperty === 'occupancyBuild') {
            // 층별 정보 조회
            _this.getFloorInformation(content);
        } else if (_this.observedProperty === 'occupancyFloor') {
            // 센서 정보 조회
            _this.getSensorInformation(content);
        }

    });

};

OccupancySensorThings.prototype.getFloorInformation = function (buildingInfo) {

    const _this = this;
    _this.selectedBuildingId = buildingInfo.id;

    const observedProperty = 'occupancyFloor';
    const queryString = 'Datastreams?$select=id,name,unitOfMeasurement&' +
        '$filter=Thing/name eq \'' + buildingInfo.name + '\' and ObservedProperty/name eq \'' + observedProperty + '\'&' +
        '$expand=Thing,Observations(' +
            '$select=result,resultTime;' +
            '$filter=resultTime lt ' + _this.getCurrentTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
        ')';

    const baseFloor = buildingInfo.baseFloor;
    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {

            for (const dataStream of msg.value) {

                // Thing
                const thing = dataStream['Thing'];

                // Observations
                const observations = dataStream['Observations'];
                let value = '-', grade = 0;
                if (observations && observations.length > 0) {
                    const observationTop = observations[0];
                    value = observationTop.result.value;
                    grade = observationTop.result.grade;
                }

                buildingInfo.listOfFloorOccupancy.push({
                    floor: thing.properties.floor,
                    floorText: _this.getFloorText(thing.properties.floor, baseFloor),
                    value: value,
                    valueWithCommas: _this.numberWithCommas(value),
                    //grade: grade,
                    grade : _this.getGrade(value),
                    gradeText: _this.getGradeMessage(_this.getGrade(value)),
                    unit: _this.getUnit(dataStream)
                });

            }

            const template = Handlebars.compile($("#buildingInfoSource").html());
            $("#buildingInfoDHTML").html("").append(template(buildingInfo));
            $('#buildingInfoWrap').show();
            if ($('#mapSettingWrap').css('width') !== '0px') {
                $('#buildingInfoWrap').css('right', '400px');
            } else {
                $('#buildingInfoWrap').css('right', '60px');
            }

            // 재실자 층 선택
            $("#buildingInfoWrap table tr").click(function() {
                _this.observedProperty = 'occupancyFloor';
                $(this).siblings().removeClass('on');
                $(this).toggleClass('on');
                const floor = $(this).data('floor');

                if (_this.selectedFloorSensorList.length > 0) {
                    const rgbColorCode = "255,255,255,255";
                    for (const cellId of _this.selectedFloorSensorList) {
                        changeColorAPI(_this.magoInstance.getMagoManager(), buildingInfo.dataGroupId, _this.selectedDataKey, [cellId], "isPhysical=true", rgbColorCode);
                    }
                }

                _this.displaySelectedFloor(floor, buildingInfo.dataGroupId, buildingInfo.dataKey);
                searchDataAPI(_this.magoInstance, buildingInfo.dataGroupId, _this.selectedDataKey);

                _this.clearOverlay();
                _this.addSelectedFloorOverlay(buildingInfo.name, floor);
            });

        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });

};

OccupancySensorThings.prototype.displaySelectedFloor = function(floor, dataGroupId, dataKey) {
    if (dataKey === 'admin_20201013064147_346094873669678') {
        this.selectedDataKey = dataKey;
        return;
    }
    const nodes = this.magoInstance.getMagoManager().hierarchyManager.getNodesMap(dataGroupId, null);
    for (const i in nodes) {
        const node = nodes[i];
        const nodeData = node.data;
        if (!nodeData) continue;
        const nodeId = nodeData.nodeId;
        const nodeAttribute = nodeData.attributes;
        if (!nodeId || !nodeAttribute) continue;
        if (nodeId === dataGroupId) {
            nodeAttribute.isVisible = false;
            continue;
        }
        if (nodeAttribute.floors && nodeAttribute.floors.length > 0) {
            nodeAttribute.isVisible = false;
            continue;
        }
        const nodeIds = nodeId.split('_');
        const nodeFloor = parseInt(nodeIds[nodeIds.length - 1]);
        if (nodeFloor > floor) {
            nodeAttribute.isVisible = false;
        } else if (nodeFloor === floor) {
            nodeAttribute.isVisible = true;
            this.selectedDataKey = nodeId;
        } else {
            nodeAttribute.isVisible = true;
        }
    }
};

OccupancySensorThings.prototype.addSelectedFloorOverlay = function(name, floor) {

    const _this = this;

    const observedProperty = 'occupancy';
    const filter = 'startswith(name, \'' + name + '\') and properties/floor eq ' + floor + ' and Datastreams/ObservedProperty/name eq \'' + observedProperty + '\'';

    // TODO 화면 영역에 해당하는 Location을 필터링하여 호출하도록 수정
    const queryString = 'Things?$select=@iot.id,name,description,properties&$top=1000' +
        '&$filter=' + filter +
        '&$expand=Locations($select=@iot.id,location,name),' +
            'Datastreams(' +
                '$select=@iot.id,description,unitOfMeasurement;' +
                '$filter=ObservedProperty/name eq \'' + observedProperty + '\'' +
            '),' +
            'Datastreams/Observations(' +
                '$select=result,phenomenonTime,resultTime;' +
                '$orderby=resultTime desc;' +
                '$filter=resultTime lt ' + _this.getCurrentTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
            ')';


    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {
            _this.things = msg.value;
            _this.redrawOverlay();
        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });

    /*
    const observedProperty = 'occupancy';
    const filter = 'startswith(Thing/name, \'' + name + '\') and Thing/properties/floor eq ' + floor + ' and ObservedProperty/name eq \'' + observedProperty + '\'';
    const queryString = 'Datastreams?$select=id,name,unitOfMeasurement&' +
        '$filter=' + filter + '&' +
        '$expand=Thing,Observations(' +
            '$select=result,resultTime;' +
            '$filter=resultTime lt ' + _this.getCurrentTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
        ')';

    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {
            console.info(msg);
            //_this.selectedDataStreams = msg.value;
        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });
    */
};

OccupancySensorThings.prototype.closeBuildingInformation = function () {
    $('#buildingInfoWrap').hide();
    this.selectedThingId = 0;
    this.selectedDataStreams = [];
    this.observedProperty = 'occupancyBuild';
    this.addOverlay();
};

OccupancySensorThings.prototype.getSensorInformation = function(content) {

    const _this = this;
    _this.selectedDataStreams = [];
    const queryString = 'Datastreams?$select=@iot.id,description,name,unitOfMeasurement' +
        '&$filter=Things/@iot.id eq ' + content.id +
        '&$orderby=ObservedProperty/@iot.id asc' +
        '&$expand=Thing($select=name,properties),' +
            'ObservedProperty($select=name),' +
            'Observations(' +
                '$select=result,resultTime;' +
                '$orderby=resultTime desc;' +
                '$filter=resultTime lt ' + _this.getCurrentTime() + ' and resultTime ge ' + _this.getFilterDayStartTime() +
            ')';

    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {

            // Datastreams
            const dataStreams = msg.value;
            const properties = dataStreams[0]['Thing']['properties'];

            const cell = properties.cell;
            const floor = _this.getFloorText(properties.floor, content.baseFloor);

            const contents = {
                observedProperty: 'occupancy',
                min: _this.occupancyGradeMin,
                max: _this.occupancyGradeMax,
                stationName: content.dataName + ', ' + floor + '층: ' + cell,
                dataStreams: []
            };

            for (const dataStream of dataStreams) {

                const observedPropertyName = _this.getObservedPropertyName(dataStream);
                const observations = dataStream['Observations'];
                let value = '-', grade = 0;
                if (observations && observations.length > 0) {
                    const observationTop = observations[0];
                    value = _this.formatValueByDigits(observationTop.result.value, 3);
                    grade = observationTop.result.grade;
                }

                const data = {
                    id: dataStream['@iot.id'],
                    name: content.dataName + ', ' + floor + '층: ' + cell,
                    unit: _this.getUnit(dataStream),
                    value: _this.formatValueByDigits(value, 3),
                    grade: grade,
                    gradeText: _this.getGradeMessage(grade),
                    observations: observations,
                    observedPropertyName: observedPropertyName
                };
                _this.selectedDataStreams.push(data.id);
                contents.dataStreams.push(data);
                contents.grade = grade;
                contents.pm10 = data.value;
                contents.occupancyPercent = Math.max(Math.min(data.value, _this.occupancyGradeMax), _this.occupancyGradeMin) / (_this.occupancyGradeMax - _this.occupancyGradeMin) * 100;
                contents.chartTitle = JS_MESSAGE["iot.occupancy"];
            }

            const $dustInfoDHTML = $('#dustInfoDHTML');
            const template = Handlebars.compile($("#dustInfoSource").html());
            const html = template(contents);
            if ($dustInfoDHTML.length === 0) {
                const wrapper = '<div id="dustInfoDHTML" class="sensor-detail-wrap">' + html + '</div>';
                $('.cesium-viewer').append(wrapper);
            }

            $dustInfoDHTML.html(html);
            $dustInfoDHTML.show();

            const total = _this.occupancyGradeMax - _this.occupancyGradeMin;
            const range = [0, 2, 5, 8, 10];
            _this.drawGaugeChart(range, total, contents.occupancyPercent);
            _this.drawOccupancyChart(contents.dataStreams);

        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });

};

OccupancySensorThings.prototype.closeInformation = function () {

};

/**
 * 재실자 차트 그리기
 * @param dataStreams
 */
OccupancySensorThings.prototype.drawOccupancyChart = function (dataStreams) {

    const datasets = [];
    for (const dataStream of dataStreams) {
        const points = [];
        for (const observation of dataStream.observations) {
            points.push({
                x: this.observationTimeToLocalTime(observation.resultTime),
                y: this.formatValueByDigits(observation.result.value, 3)
            });
        }
        const observedPropertyName = dataStream.observedPropertyName;
        const propertyColor = this.observedPropertyColor[observedPropertyName];
        datasets.push({
            label: dataStream.name,
            data: points,
            borderColor: propertyColor,
            backgroundColor: new Color(propertyColor).alpha(0.2).rgbString(),
            observedPropertyName: observedPropertyName
        });
    }

    this.occupancyChart = new Chart(document.getElementById("hourlyAirQualityChart"), {
        type: 'line',
        data: {datasets: datasets},
        options: this.chartOptions
    });

};

OccupancySensorThings.prototype.update = function () {

};