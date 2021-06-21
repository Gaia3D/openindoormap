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
        'Alphadom_IndoorGML' : {
            dataId : 5000000,
            baseFloor: 7,
            maxCapacityBuilding : 3000,
            maxCapacityFloor : 200,
            maxCapacity : 10
        },
        'UOS21C_IndoorGML' : {
            dataId : 5000001,
            baseFloor: 0,
            maxCapacityBuilding : 400,
            maxCapacityFloor : 400,
            maxCapacity : 10
        }
    };
    this.occupancyGradeMin = 0;
    this.occupancyGradeMax = 10;
    this.maxCapacity = 10;

    //this.currentTime = "2020-11-23T12:15:00.000Z";
    this.currentTime = moment.utc().format();
    this.processingTime = 60;       // 30s
    this.callInterval = 10;         // 10s
    this.filterInterval = 600;       // 60s

    this.gaugeChartNeedle = {};
    this.occupancyChart = {};
    this.chartTitle = '재실자(Occupancy)';
    this.chartXAxesTitle = '시간(분)';
    this.chartYAxesTitle = '재실자(명)';

    this.selectedBuildingId = 0;
    this.selectedBuildingName = '';
    this.selectedDataGroupId = '';
    this.selectedDataKey = '';
    this.cellSpaceList = {};
    this.selectedFloorSensorList = [];

    this.chartOptions = {
        animation: false,
        responsive: true,
        maintainAspectRatio: false,
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
        plugins: {
            zoom: {
                pan: {
                    enabled: true,
                    mode: 'x',
                    rangeMin: {
                        x: null,
                        y: null
                    },
                    rangeMax: {
                        x: null,
                        y: null
                    }
                },
                zoom: {
                    enabled: true,
                    drag: false,
                    mode: 'x',
                    rangeMin: {
                        x: null,
                        y: null
                    },
                    rangeMax: {
                        x: null,
                        y: null
                    },
                    speed: 0.03
                }
            }
        },
        scales: {
            xAxes: [{
                type: 'time',
                autoSkip: false,
                time: {
                    parser: "YYYY-MM-DD HH:mm:ss",
                    second: 'mm:ss',
                    minute: 'HH:mm',
                    hour: 'HH:mm',
                    day: 'MMM DD',
                    month: 'YYYY MMM',
                    tooltipFormat: 'YYYY-MM-DD HH:mm:ss',
                    displayFormats: {
                        second: 'HH:mm:ss a'
                    },
                    unit: 'second',
                    unitStepSize: 10
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
                },
                ticks: {
                    min: 0,
                    max: 10,
                    stepSize: 2
                }
            }]
        }
    };

};
OccupancySensorThings.prototype = Object.create(SensorThings.prototype);
OccupancySensorThings.prototype.constructor = OccupancySensorThings;

OccupancySensorThings.prototype.getFilterHourlyStartTime = function () {
    let filteredTime = moment(this.currentTime).utc().subtract(this.processingTime, 's');
    let correctTime = this.getCorrectTime(filteredTime, this.filterInterval);
    return moment(correctTime).utc().subtract(3600 * 2, 's').format();
    //return this.getCorrectTime(filteredTime, this.filterInterval);
    //return moment(this.currentTime).utc().subtract(this.filterInterval, 's').format();
};

OccupancySensorThings.prototype.init = function () {
    if ($('#buildingInfoWrap').is(':visible')) {
        $('#buildingInfoWrap').hide();
    }
    if ($('#dustInfoDHTML').is(':visible')) {
        $('#dustInfoDHTML').hide();
    }
    this.things = [];
    this.selectedThingId = 0;
    this.selectedDataStreams = [];
    this.selectedBuildingId = 0;
    this.selectedBuildingName = '';
    this.selectedDataGroupId = '';
    this.selectedDataKey = '';
    this.cellSpaceList = {};
    this.selectedFloorSensorList = [];
};

OccupancySensorThings.prototype.getGradeByPercent = function (percent) {
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

OccupancySensorThings.prototype.getGrade = function(value, min, max) {
    const percent = (value - min) / (max - min) * 100;
    return this.getGradeByPercent(percent);
};

OccupancySensorThings.prototype.getOccupancyColor = function (value) {
    const grade = this.getGrade(value, 0, this.maxCapacity);
    switch (grade) {
        case 1:
            return "50,161,255,200";
        case 2:
            return "0,199,60,200";
        case 3:
            return "255,215,0,200";
        case 4:
            return "255,89,89,200";
        default:
            return "255,255,255,255";
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
            '$filter=resultTime lt ' + _this.getFilterEndTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
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
                '$filter=resultTime lt ' + _this.getFilterEndTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
            ')';

    // TODO DataInfo 조회 결과를 통해 Things을 조회하도록 수정 필요
    $.ajax({
        // http://localhost:8888/FROST-Server/v1.0/Things?$select=@iot.id,name,description&$top=5&$count=true&$orderby=name asc&$filter=Datastreams/ObservedProperty/name eq 'occupancy' and (startswith(name, '1') or endswith(name, '1'))&$expand=Locations($select=@iot.id,location,name),Datastreams($select=@iot.id,description,unitOfMeasurement;$filter=ObservedProperty/name eq 'occupancy'),Datastreams/Observations($select=result,phenomenonTime,resultTime;$orderby=resultTime desc;$filter=resultTime lt 2020-11-03T05:00:00.000Z and resultTime ge 2020-11-03T04:00:00.000Z)
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {

            const pagination = new Pagination(pageNo, msg['@iot.count'], 5, msg['@iot.nextLink']);
            msg.pagination = pagination;

            const templateLegend = Handlebars.compile($("#iotOccupancyLegendSource").html());
            $("#iotLegendDHTML").html("").append(templateLegend(_this));

            const data = {
                promises: [],
                thingsContent: {}
            };

            const things = msg.value;
            for (const thing of things) {

                const thingId = thing['@iot.id'];
                const thingName = thing['name'];

                // Datastreams
                const dataStreams = thing['Datastreams'];
                if (!dataStreams || dataStreams.length <= 0) continue;
                const dataStream = dataStreams[0];

                // MappingInformation
                const mappingInfo = _this.mappingTable[thingName];
                const dataId = mappingInfo['dataId'];
                const maxCapacityBuilding = mappingInfo['maxCapacityBuilding'];
                const maxCapacityFloor = mappingInfo['maxCapacityFloor'];
                const maxCapacity = mappingInfo['maxCapacity'];

                // Observations
                const observations = dataStream['Observations'];
                let value = '-', grade = 0;
                if (observations && observations.length > 0) {
                    const observationTop = observations[0];
                    value = observationTop.result;
                    //grade = observationTop.result.grade;
                    grade = _this.getGrade(value, 0, maxCapacityBuilding);
                }

                // TODO thingId와 dataId 맵핑테이블을 통한 데이터 조회
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

                if (data.promises.length === 1) {
                    const dataInfo = arguments[0]['dataInfo'];
                    const dataId = dataInfo.dataId;
                    const thingsContent = data.thingsContent[dataId];
                    msg.contents.push(Object.assign(thingsContent, dataInfo));
                } else {
                    for (const argument of arguments) {
                        const dataInfo = argument[0]['dataInfo'];
                        const dataId = dataInfo.dataId;
                        const thingsContent = data.thingsContent[dataId];
                        msg.contents.push(Object.assign(thingsContent, dataInfo));
                    }
                }

                const templateSearchSummary = Handlebars.compile($("#searchSummarySource").html());
                $("#iotSearchSummaryDHTML").html("").append(templateSearchSummary(msg));

                const template = Handlebars.compile($("#occupancyListSource").html());
                $("#iotOccupancyListDHTML").html("").append(template(msg));

                const templatePagination = Handlebars.compile($("#iotPaginationSource").html());
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
                '$filter=resultTime lt ' + _this.getFilterEndTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
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
                    value = observationTop.result;
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
                '$filter=resultTime lt ' + _this.getFilterEndTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
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
        const thingName = thing['name'];

        // TODO thingId와 dataId 맵핑테이블을 통한 데이터 조회
        const dataId = _this.mappingTable[thingName]['dataId'];
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
        const thingName = thing['name'];

        // Datastreams
        const dataStreams = thing['Datastreams'];
        if (!dataStreams || dataStreams.length <= 0) continue;
        const dataStream = dataStreams[0];

        // MappingInformation
        const mappingInfo = _this.mappingTable[thingName];
        if (!mappingInfo) return;
        const dataId = mappingInfo['dataId'];
        const maxCapacityBuilding = mappingInfo['maxCapacityBuilding'];
        const maxCapacityFloor = mappingInfo['maxCapacityFloor'];
        const maxCapacity = mappingInfo['maxCapacity'];

        // Observations
        const observations = dataStream['Observations'];
        let value = '-', grade = 0, selected = '';
        if (observations && observations.length > 0) {
            const observationTop = observations[0];
            value = observationTop.result;
            //grade = observationTop.result.grade;
            grade = _this.getGrade(value, 0, maxCapacityBuilding);
        }
        const gradeText = _this.getGradeMessage(grade);
        if (_this.selectedThingId == thingId) {
            selected = 'on';
        }

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

    // MappingInformation
    const mappingInfo = _this.mappingTable[_this.selectedBuildingName];
    if (!mappingInfo) return;
    const dataId = mappingInfo['dataId'];
    const maxCapacityBuilding = mappingInfo['maxCapacityBuilding'];
    const maxCapacityFloor = mappingInfo['maxCapacityFloor'];
    const maxCapacity = mappingInfo['maxCapacity'];

    const contents = {
        things : []
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
            value = observationTop.result;
            //grade = observationTop.result.grade;
            grade = _this.getGrade(value, 0 ,maxCapacity);
        }
        const gradeText = _this.getGradeMessage(grade);
        if (_this.selectedThingId == thingId) {
            selected = 'on';
        }

        const cellId = thing['properties']['cell'];
        const cellSpace = _this.cellSpaceList[dataId][cellId];
        if (!cellSpace) continue;
        const localCoordinate = {x: cellSpace.x, y: cellSpace.y, z: cellSpace.z};

        const magoManager = _this.magoInstance.getMagoManager();
        const targetNode = magoManager.hierarchyManager.getNodeByDataKey(_this.selectedDataGroupId, _this.selectedDataKey);
        const targetNodeGeoLocDataManager = targetNode.getNodeGeoLocDataManager();
        const targetNodeGeoLocData = targetNodeGeoLocDataManager.getCurrentGeoLocationData();
        const tempGlobalCoordinateObject = targetNodeGeoLocData.localCoordToWorldCoord(localCoordinate);
        const wgs84CoordinateObject = Mago3D.Globe.CartesianToGeographicWgs84(tempGlobalCoordinateObject.x, tempGlobalCoordinateObject.y, tempGlobalCoordinateObject.z);

        const longitude = Number(wgs84CoordinateObject.longitude);
        const latitude = Number(wgs84CoordinateObject.latitude);
        const altitude = Number(wgs84CoordinateObject.altitude + 5.0);

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

        contents.things.push({
            id: thingId,
            value: value,
            valueWithCommas: _this.numberWithCommas(value),
            unit: _this.getUnit(dataStream),
            //addr: addr,
            grade: grade,
            gradeText: gradeText,
            selected: selected,
            subTitle: JS_MESSAGE["iot.occupancy"],
            stationName: cellId,
            top: resultScreenCoord.y,
            left: resultScreenCoord.x
        });

        const template = Handlebars.compile($("#overlaySource").html());
        $('#overlayDHTML').html("").append(template(contents));

    }

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
    let mappingInfo;
    if (_this.observedProperty === 'occupancyBuild') {
        mappingInfo = _this.mappingTable[thingId];
    } else if (_this.observedProperty === 'occupancyFloor') {
        mappingInfo = _this.mappingTable[_this.selectedBuildingName];
    }

    const dataId = mappingInfo['dataId'];
    const baseFloor = mappingInfo['baseFloor'];
    const maxCapacityBuilding = mappingInfo['maxCapacityBuilding'];
    const maxCapacityFloor = mappingInfo['maxCapacityFloor'];
    const maxCapacity = mappingInfo['maxCapacity'];

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
            value = observationTop.result;
            //grade = observationTop.result.grade;
            //grade = _this.getGrade(value);
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
        _this.selectedDataGroupId = dataInfo.dataGroupId;
        if (_this.observedProperty === 'occupancyBuild') {
            // 층별 정보 조회
            content.grade = _this.getGrade(value, 0, maxCapacityBuilding);
            _this.getFloorInformation(content);
        } else if (_this.observedProperty === 'occupancyFloor') {
            // 센서 정보 조회
            content.grade = _this.getGrade(value, 0, maxCapacityFloor);
            _this.getSensorInformation(content);
        }

    });

};

OccupancySensorThings.prototype.getFloorInformation = function (buildingInfo) {

    const _this = this;
    _this.selectedBuildingId = buildingInfo.id;
    _this.selectedBuildingName = buildingInfo.name;

    const observedProperty = 'occupancyFloor';
    const queryString = 'Datastreams?$select=id,name,unitOfMeasurement&' +
        '$filter=Thing/name eq \'' + buildingInfo.name + '\' and ObservedProperty/name eq \'' + observedProperty + '\'&' +
        '$expand=Thing,Observations(' +
            '$select=result,resultTime;' +
            '$orderby=resultTime desc;' +
            '$filter=resultTime lt ' + _this.getFilterEndTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
        ')&' +
        '$orderby=id desc';

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

                // MappingInformation
                const mappingInfo = _this.mappingTable[_this.selectedBuildingName];
                const dataId = mappingInfo['dataId'];
                const maxCapacityBuilding = mappingInfo['maxCapacityBuilding'];
                const maxCapacityFloor = mappingInfo['maxCapacityFloor'];
                const maxCapacity = mappingInfo['maxCapacity'];

                // Observations
                const observations = dataStream['Observations'];
                let value = '-', grade = 0;
                if (observations && observations.length > 0) {
                    const observationTop = observations[0];
                    value = observationTop.result;
                    //grade = observationTop.result.grade;
                    grade = _this.getGrade(value, 0, maxCapacityFloor);
                }

                buildingInfo.listOfFloorOccupancy.push({
                    floor: thing.properties.floor,
                    floorText: _this.getFloorText(thing.properties.floor, baseFloor),
                    value: value,
                    valueWithCommas: _this.numberWithCommas(value),
                    grade: grade,
                    gradeText: _this.getGradeMessage(grade),
                    unit: _this.getUnit(dataStream)
                });

            }

            const template = Handlebars.compile($("#buildingInfoSource").html());
            $("#buildingInfoDHTML").html("").append(template(buildingInfo)).data('buildingInfo', buildingInfo);
            $('#buildingInfoWrap').show();
            if ($('#mapSettingWrap').css('width') !== '0px') {
                $('#buildingInfoWrap').css('right', '400px');
            } else {
                $('#buildingInfoWrap').css('right', '60px');
            }

        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });

};

OccupancySensorThings.prototype.displaySelectedFloor = function(floor) {

    // TODO 시립대 데이터인 경우 하드코딩 삭제
    if (this.selectedDataKey === 'admin_20201013064147_346094873669678') {
        searchDataAPI(this.magoInstance, this.selectedDataGroupId, this.selectedDataKey);
        return;
    }

    const nodes = this.magoInstance.getMagoManager().hierarchyManager.getNodesMap(this.selectedDataGroupId, null);
    for (const i in nodes) {
        const node = nodes[i];
        const nodeData = node.data;
        if (!nodeData) continue;
        const nodeId = nodeData.nodeId;
        const nodeAttribute = nodeData.attributes;
        if (!nodeId || !nodeAttribute) continue;
        if (nodeId === this.selectedDataGroupId) {
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
    searchDataAPI(this.magoInstance, this.selectedDataGroupId, this.selectedDataKey);
};

OccupancySensorThings.prototype.addSelectedFloorOverlay = function(floor, name) {

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
                '$filter=resultTime lt ' + _this.getFilterEndTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
            ')';


    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {
            _this.things = msg.value;
            _this.redrawOverlayFloor();
            _this.changeRoomColor();
        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });

};

OccupancySensorThings.prototype.changeBuildingMode = function (dataGroupId) {

    // 색상 변경
    const magoManager = this.magoInstance.getMagoManager();
    if (this.selectedFloorSensorList.length > 0) {
        const rgbColorCode = "255,255,255,255";
        for (const cellId of this.selectedFloorSensorList) {
            changeColorAPI(magoManager, this.selectedDataGroupId, this.selectedDataKey, [cellId], "isPhysical=true", rgbColorCode);
        }
    }

    // Visibility 변경
    const nodes = magoManager.hierarchyManager.getNodesMap(dataGroupId, null);
    for (const i in nodes) {
        const node = nodes[i];
        const nodeData = node.data;
        if (!nodeData) continue;
        const nodeId = nodeData.nodeId;
        const nodeAttribute = nodeData.attributes;
        if (!nodeId || !nodeAttribute) continue;
        nodeAttribute.isVisible = true;
    }

    // 멤버 초기화
    this.selectedThingId = 0;
    this.selectedBuildingId = 0;
    this.selectedDataGroupId = '';
    this.selectedDataKey = '';

    // 모드 변경
    this.observedProperty = 'occupancyBuild';
    this.addOverlay();

}

OccupancySensorThings.prototype.closeBuildingInformation = function (dataGroupId, dataKey) {
    $('#buildingInfoWrap').hide();
    this.changeBuildingMode(dataGroupId);
    searchDataAPI(this.magoInstance, dataGroupId, dataKey);
};

OccupancySensorThings.prototype.flyTo = function (dataGroupId, dataKey) {
    if (this.selectedDataGroupId) {
        $('#buildingInfoWrap').hide();
        this.changeBuildingMode(this.selectedDataGroupId);
    }
    searchDataAPI(this.magoInstance, dataGroupId, dataKey);
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
                '$filter=resultTime lt ' + _this.getFilterEndTime() + ' and resultTime ge ' + _this.getFilterHourlyStartTime() +
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
                    value = observationTop.result;
                    //grade = observationTop.result.grade;
                    grade = _this.getGrade(value, 0, _this.maxCapacity);
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
    $('#dustInfoDHTML').hide();
    this.selectedThingId = 0;
    this.selectedDataStreams = [];
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
                y: observation.result
            });
        }
        const observedPropertyName = dataStream.observedPropertyName;
        const propertyColor = this.observedPropertyColor[observedPropertyName];
        datasets.push({
            label: dataStream.name,
            data: points,
            borderColor: propertyColor,
            backgroundColor: new Color(propertyColor).alpha(0.2).rgbString(),
            observedPropertyName: observedPropertyName,
            steppedLine: 'middle'
        });
    }

    this.occupancyChart = new Chart(document.getElementById("hourlyAirQualityChart"), {
        type: 'line',
        data: {datasets: datasets},
        options: this.chartOptions
    });

};

OccupancySensorThings.prototype.update = function () {

    // TODO 램덤 값 삭제
    const randomValue = Math.floor(Math.random() * (this.occupancyGradeMax - this.occupancyGradeMin)) + this.occupancyGradeMin;
    //const randomValue = 0;

    this.updateOverlay(randomValue);

    if (this.selectedBuildingId !== 0) {
        this.updateFloorInformation(randomValue);
    }

    if (this.selectedDataStreams.length > 0) {
        this.updateSensorInformation(randomValue);
    }

};

OccupancySensorThings.prototype.updateOverlay = function (randomValue) {

    const _this = this;
    const overlayIds = _this.getOverlay();
    const length = overlayIds.length;
    if (!overlayIds || length <= 0) {
        return;
    }

    //let filter = 'ObservedProperty/name eq \'' + _this.observedProperty + '\'';
    //filter += 'and (';
    for (let i = 0; i < length; i += 5) {
        let filter = '';
        for (let j = i; j < i + 5; j++) {
            const thingId = overlayIds[j];
            if (!thingId) return;
            if (j === i) {
                filter += 'Things/id eq ' + thingId;
            } else {
                filter += ' or Things/id eq ' + thingId;
            }
        }
        //console.log("filter = " + filter);
        _this.callDatastreamsByThingsId(filter, randomValue);
    }

    /*
    let filter = '';
    for (const i in overlayIds) {
        const thingId = overlayIds[i];
        if (i == 0) {
            filter += 'Things/id eq ' + thingId;
        } else {
            filter += ' or Things/id eq ' + thingId;
        }
    }
    */
    //filter += ')';

};

OccupancySensorThings.prototype.callDatastreamsByThingsId = function (filter, randomValue) {
    const _this = this;
    const queryString = 'Datastreams?$select=@iot.id,description,name,unitOfMeasurement' +
        '&$filter=' + filter +
        '&$orderby=ObservedProperty/@iot.id asc' +
        '&$expand=Thing($select=@iot.id,name),' +
            'ObservedProperty($select=name),' +
            'Observations(' +
                '$select=result,resultTime;' +
                '$orderby=resultTime desc;' +
                '$filter=resultTime lt ' + _this.getFilterEndTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
            ')';

    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {

            // Datastreams
            for (const dataStream of msg.value) {
                // Thing
                const thing = dataStream['Thing'];
                const thingId = thing['@iot.id'];
                // Observations
                let value = "-", grade = 0, selected = '';

                if (!dataStream['Observations'] || dataStream['Observations'].length <= 0) {
                    continue;
                }

                // ObservationTop
                const observationTop = dataStream['Observations'][0];
                //let value = parseFloat(observation.result.value);
                value = parseInt(observationTop.result);
                value += randomValue;
                value = _this.formatValueByDigits(value, 3);
                //grade = observationTop.result.grade;

                const ObservedPropertyName = _this.getObservedPropertyName(dataStream);

                // MappingInformation
                let mappingInfo;
                if (ObservedPropertyName === 'occupancyBuild') {
                    mappingInfo = _this.mappingTable[thingId];
                    const maxCapacityBuilding = mappingInfo['maxCapacityBuilding'];
                    grade = _this.getGrade(value, 0, maxCapacityBuilding);
                } else if (ObservedPropertyName === 'occupancyFloor') {
                    mappingInfo = _this.mappingTable[_this.selectedBuildingName];
                    const maxCapacityFloor = mappingInfo['maxCapacityFloor'];
                    grade = _this.getGrade(value, 0, maxCapacityFloor);
                } else if (ObservedPropertyName === 'occupancy') {
                    grade = _this.getGrade(value, 0, _this.maxCapacity);
                }

                for (const thing of _this.things) {
                    const originalId = thing['@iot.id'];
                    if (originalId == thingId) {
                        const observationTopOld = thing['Datastreams'][0]['Observations'];
                        if (observationTopOld.length === 0) {
                            observationTopOld.push({'result': value});
                        } else {
                            observationTopOld[0]['result'] = value;
                            observationTopOld[0]['resultTime'] = observationTop['resultTime'];
                        }
                    }
                }

                const contents = {
                    things: [{
                        id: thingId,
                        value: value,
                        valueWithCommas: _this.numberWithCommas(value),
                        unit: _this.getUnit(dataStream),
                        stationName: $('#overlay_' + thingId + ' .stationName').text(),
                        grade: grade,
                        gradeText: _this.getGradeMessage(grade),
                        selected: selected,
                        subTitle: JS_MESSAGE["iot.occupancy"]
                    }]
                };

                // 지도 측정소 정보 업데이트
                const template = Handlebars.compile($("#overlaySource").html());
                const innerHtml = $(template(contents)).find('ul').html();
                $('#overlay_' + thingId + '> ul').html(innerHtml);

                //console.debug("updated thingId: " + thingId);

            }

            if (_this.observedProperty === 'occupancyFloor') {
                _this.changeRoomColor();
            }

        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });

};


OccupancySensorThings.prototype.updateFloorInformation = function (randomValue) {

    const _this = this;

    // TODO thingId와 dataId 맵핑테이블을 통한 데이터 조회
    // MappingInformation
    let dataId = '', baseFloor = 0, maxCapacityBuilding = 0, maxCapacityFloor = 0, maxCapacity = 0;
    const mappingInfo = _this.mappingTable[_this.selectedBuildingName];
    dataId = mappingInfo['dataId'];
    baseFloor = mappingInfo['baseFloor'];
    maxCapacityBuilding = mappingInfo['maxCapacityBuilding'];
    maxCapacityFloor = mappingInfo['maxCapacityFloor'];
    maxCapacity = mappingInfo['maxCapacity'];

    const promises = [];
    promises.push(_this.ajaxDataInfo(dataId));
    promises.push(_this.ajaxThingInfo(_this.selectedBuildingId));

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
            value = parseInt(observationTop.result);
            if (randomValue) {
                value += randomValue;
            }
            //grade = observationTop.result.grade;
            grade = _this.getGrade(value, 0, maxCapacityBuilding);
        }

        const buildingInfo = {
            id: thing['@iot.id'],
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

        const observedProperty = 'occupancyFloor';
        const queryString = 'Datastreams?$select=id,name,unitOfMeasurement&' +
            '$filter=Thing/name eq \'' + buildingInfo.name + '\' and ObservedProperty/name eq \'' + observedProperty + '\'&' +
            '$expand=Thing,Observations(' +
                '$select=result,resultTime;' +
                '$orderby=resultTime desc;' +
                '$filter=resultTime lt ' + _this.getFilterEndTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
            ')&' +
            '$orderby=id desc';

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
                        value = parseInt(observationTop.result);
                        if (randomValue) {
                            value += randomValue;
                        }
                        //grade = observationTop.result.grade;
                        grade = _this.getGrade(value, 0, maxCapacityFloor);
                    }

                    let selectedFloorOn = '';
                    if (_this.selectedDataKey !== '') {
                        // TODO 시립대 데이터인 경우 하드코딩 삭제
                        if (_this.selectedDataKey === 'admin_20201013064147_346094873669678') {
                            selectedFloorOn = 'on';
                        }
                        const dataKeys = _this.selectedDataKey.split('_');
                        const selectedFloor = parseInt(dataKeys[dataKeys.length - 1]);
                        if (thing.properties.floor === selectedFloor) {
                            selectedFloorOn = 'on';
                        }
                    }

                    buildingInfo.listOfFloorOccupancy.push({
                        floor: thing.properties.floor,
                        floorText: _this.getFloorText(thing.properties.floor, baseFloor),
                        selectedFloor: selectedFloorOn,
                        value: value,
                        valueWithCommas: _this.numberWithCommas(value),
                        grade: grade,
                        gradeText: _this.getGradeMessage(grade),
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

            },
            error: function (request, status, error) {
                alert(JS_MESSAGE["ajax.error.message"]);
            }
        });

    });

};

OccupancySensorThings.prototype.updateSensorInformation = function (randomValue) {

    const _this = this;

    //let filter = 'ObservedProperty/name eq \'' + _this.observedProperty + '\'';

    const dataStreamIds = _this.selectedDataStreams;
    const length = dataStreamIds.length;
    if (!dataStreamIds || length <= 0 || _this.selectedThingId == 0) return;
    //filter += 'and (';

    for (let i = 0; i < length; i += 5) {
        let filter = '';
        for (let j = i; j < i + 5; j++) {
            const dataStreamId = dataStreamIds[j];
            if (!dataStreamId) break;
            if (j === i) {
                filter += 'id eq ' + dataStreamId;
            } else {
                filter += ' or id eq ' + dataStreamId;
            }
        }
        //console.log("filter = " + filter);
        _this.callDatastreamsById(filter, randomValue);
    }

    //filter += ')';
    /*
    for (const i in dataStreamIds) {
        const dataStreamId = dataStreamIds[i];
        if (i == 0) {
            filter += 'id eq ' + dataStreamId;
        } else {
            filter += ' or id eq ' + dataStreamId;
        }
    }
    */

};

OccupancySensorThings.prototype.callDatastreamsById = function(filter, randomValue) {
    const _this = this;
    const queryString = 'Datastreams?$select=@iot.id,description,name,unitOfMeasurement' +
        '&$filter=' + filter +
        '&$orderby=ObservedProperty/@iot.id asc' +
        '&$expand=Thing($select=name),' +
            'ObservedProperty($select=name),' +
            'Observations(' +
                '$select=result,resultTime;' +
                '$orderby=resultTime desc;' +
                '$top=1;' +
                '$filter=resultTime lt ' + _this.getFilterEndTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
            ')';
    //console.debug("from: " + _this.observationTimeToLocalTime(_this.getFilterStartTime()) + ", to: " + _this.observationTimeToLocalTime(_this.getFilterEndTime()));

    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {
            console.info(msg);

            const dataStreamContents = {
                dataStreams: []
            };

            for (const dataStream of msg.value) {

                // Datastreams
                const observedPropertyName = _this.getObservedPropertyName(dataStream);

                // Observations
                let value = "-", grade = 0;
                if (dataStream['Observations'] && dataStream['Observations'].length > 0) {

                    // ObservationTop
                    const observationTop = dataStream['Observations'][0];
                    //value = parseFloat(observation.result.value);
                    value = parseInt(observationTop.result);
                    value += randomValue;
                    value = _this.formatValueByDigits(value, 3);
                    //grade = observationTop.result.grade;
                    grade = _this.getGrade(value, 0, _this.maxCapacity);

                    if (_this.gaugeChartNeedle.data) {
                        // 게이지 차트 업데이트
                        _this.updateGaugeChart(_this.occupancyGradeMin, _this.occupancyGradeMax, value, grade);
                    }

                    // 라인 차트 업데이트
                    _this.updateOccupancyChart(dataStream, randomValue);

                }

                dataStreamContents.dataStreams.push({
                    name: dataStream.name,
                    unit: _this.getUnit(dataStream),
                    value: value,
                    grade: grade,
                    gradeText: _this.getGradeMessage(grade),
                    observedPropertyName: observedPropertyName
                });

            }

            // 테이블 업데이트
            _this.updateInformationTable(dataStreamContents);

        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });
};

OccupancySensorThings.prototype.updateOccupancyChart = function (dataStream, randomValue) {
    const _this = this;
    let value = undefined;
    const time = _this.observationTimeToLocalTime(_this.getCurrentTime());

    const occupancyChartData = _this.occupancyChart.data;
    if (!occupancyChartData) return;

    if (dataStream['Observations'].length > 0) {
        for (const observation of dataStream['Observations']) {
            const observedPropertyName = _this.getObservedPropertyName(dataStream);
            let value = parseInt(observation.result);
            value += randomValue;
            occupancyChartData.datasets.forEach(function (dataset) {
                if (dataset.observedPropertyName === observedPropertyName) {
                    //console.debug("observedPropertyName: " + observedPropertyName + "value: " + value + ", time: " + time);
                    if (occupancyChartData.datasets.length > 100) {
                        dataset.data.pop();
                    }
                    dataset.data.unshift({x: time, y: value});
                }
            });
        }
    } else {
        occupancyChartData.datasets.forEach(function (dataset) {
            if (occupancyChartData.datasets.length > 100) {
                dataset.data.pop();
            }
            dataset.data.unshift({x: time, y: value});
        });
    }
    _this.occupancyChart.update();
};

OccupancySensorThings.prototype.changeRoomColor = function() {

    this.selectedFloorSensorList = [];
    const magoManager = this.magoInstance.getMagoManager();

    for (const thing of this.things) {

        // Datastreams
        const dataStreams = thing['Datastreams'];
        if (!dataStreams || dataStreams.length <= 0) continue;
        const dataStream = dataStreams[0];

        // Observations
        const observations = dataStream['Observations'];
        let value = '-';
        if (observations && observations.length > 0) {
            const observationTop = observations[0];
            value = observationTop.result;
        }

        const cellId = thing['properties']['cell'];
        this.selectedFloorSensorList.push(cellId);

        const rgbColorCode = this.getOccupancyColor(value);
        changeColorAPI(magoManager, this.selectedDataGroupId, this.selectedDataKey, [cellId], "isPhysical=true", rgbColorCode);

    }

};

/**
 * 등급별 상태메세지 가져오기
 * @param grade
 * @returns {*}
 */
OccupancySensorThings.prototype.getGradeMessage = function (grade) {
    let message;
    const num = parseInt(grade);
    switch (num) {
        case 1:
            message = JS_MESSAGE["iot.occupancy.legend.good"];
            break;
        case 2:
            message = JS_MESSAGE["iot.occupancy.legend.normal"];
            break;
        case 3:
            message = JS_MESSAGE["iot.occupancy.legend.bad"];
            break;
        case 4:
            message = JS_MESSAGE["iot.occupancy.legend.very-bad"];
            break;
        default:
            message = JS_MESSAGE["iot.occupancy.legend.nodata"];
            break;
    }
    return message;
};