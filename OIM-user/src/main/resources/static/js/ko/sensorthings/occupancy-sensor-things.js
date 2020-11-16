const OccupancySensorThings = function (magoInstance) {
    SensorThings.call(this, magoInstance);

    this.magoInstance = magoInstance;
    this.type = 'iot_occupancy';
    this.observedProperty = 'occupancy';
    this.observedPropertyColor = {
        'occupancy': '#E91E63',
        'occupancyBuild': '#FF9800',
        'occupancyFloor': '#2196F3'
    };
    this.occupancyGradeMin = 0;
    this.occupancyGradeMax = 10;

    this.currentTime = "2020-11-11T04:00:00.000Z";
    //this.currentTime = moment.utc().format();
    this.callInterval = 10;         // 10s
    this.filterInterval = 60;     // 60s

    this.gaugeChartNeedle = {};
    this.hourlyAirQualityChart = {};
    this.chartTitle = '재실자(Occupancy)';
    this.chartXAxesTitle = '시간(분)';
    this.chartYAxesTitle = '재실자(명)';

    this.cellSpaceList = [];
    this.selectedDataKey;

};
OccupancySensorThings.prototype = Object.create(SensorThings.prototype);
OccupancySensorThings.prototype.constructor = OccupancySensorThings;

OccupancySensorThings.prototype.getGrade = function (value) {
    const max = 5, min = 0;
    return Math.floor(Math.random() * (max - min)) + min;
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

OccupancySensorThings.prototype.getDataInfo = function(dataId) {
    return $.ajax({
        url: '/datas/' + dataId,
        type: "GET",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        dataType: "json"
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
            //for (const thing of things) {
            for (let i = 0; i < 2; i++) {

                //const thingId = thing['@iot.id'];
                const thing = things[0];
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
                let dataId = 200003 - i;
                data.promises.push(_this.getDataInfo(dataId));
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

OccupancySensorThings.prototype.addOverlay = function (name, floor) {

    const _this = this;

    // 모든 건물
    let observedProperty = 'occupancyBuild';
    let filter = 'Datastreams/ObservedProperty/name eq \'' + observedProperty + '\'';

    if (name && floor) {
        // 건물 + 층
        observedProperty = 'occupancy';
        filter = 'startswith(name, \'' + name + '\') and properties/floor eq ' + floor + ' and Datastreams/ObservedProperty/name eq \'' + observedProperty + '\'';
    }

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
            if (observedProperty === 'occupancyBuild') {
                _this.setCellSpaceList();
            }
            _this.redrawOverlay();
        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });
};

OccupancySensorThings.prototype.setCellSpaceList = function() {

    const _this = this;
    for (const thing of _this.things) {

        const thingId = parseInt(thing['@iot.id']);

        // TODO thingId와 dataGroupId, dataKey 맵핑테이블을 통한 데이터 조회
        const dataGroupId = '10000';
        const dataKey = 'Alphadom_IndoorGML_data';
        const node = _this.magoInstance.getMagoManager().hierarchyManager.getNodeByDataKey(dataGroupId, dataKey);
        const projectFolderName = node.data.projectFolderName;
        const cellSpaceListFileName = dataKey + '_cellspacelist.json';

        Promise.resolve($.getJSON('/f4d/' + projectFolderName + '/' + cellSpaceListFileName))
            .then(function(result){
                _this.cellSpaceList = result;
            });
    }

};

OccupancySensorThings.prototype.redrawOverlay = function () {

    const contents = {
        things: []
    };

    for (const thing of this.things) {

        const thingId = parseInt(thing['@iot.id']);

        // TODO thingId와 dataGroupId, dataKey 맵핑테이블을 통한 데이터 조회
        const dataGroupId = '10000';
        const dataKey = 'Alphadom_IndoorGML_data';
        const nodeData = this.magoInstance.getMagoManager().hierarchyManager.getNodeByDataKey(dataGroupId, dataKey).data;
        const dataName = nodeData.data_name;
        const longitude = nodeData.geographicCoord.longitude;
        const latitude = nodeData.geographicCoord.latitude;
        const coordinates = [longitude, latitude];
        const resultScreenCoord = this.geographicCoordToScreenCoord(coordinates);

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

        // Datastreams
        const dataStreams = thing['Datastreams'];
        if (!dataStreams || dataStreams.length <= 0) continue;
        const dataStream = dataStreams[0];

        // Observations
        const observations = dataStream['Observations'];
        let value = '-', grade = 0, selected = '';
        if (observations && observations.length > 0) {
            const observationTop = observations[0];
            value = this.formatValueByDigits(observationTop.result.value, 3);
            grade = observationTop.result.grade;
        }
        const gradeText = this.getGradeMessage(grade);
        if (this.selectedThingId == thingId) {
            selected = 'on';
        }

        contents.things.push({
            id: thing['@iot.id'],
            value: this.numberWithCommas(value),
            unit: this.getUnit(dataStream),
            stationName: dataName,
            //addr: addr,
            grade: grade,
            gradeText: gradeText,
            top: resultScreenCoord.y,
            left: resultScreenCoord.x,
            selected: selected,
            subTitle: JS_MESSAGE["iot.occupancy"]
        });

    }   // end for

    /*
    if (contents.things.length > 30) {
        alert('검색되는 센서가 너무 많습니다. 지도를 확대 하세요.');
        return;
    }
     */

    const template = Handlebars.compile($("#overlaySource").html());
    $('#overlayDHTML').html("").append(template(contents));

};

OccupancySensorThings.prototype.getBuildingInformation = function(id, name) {

    const _this = this;
    const observedProperty = 'occupancyBuild';
    const queryString = 'Things(' + id + ')?$select=@iot.id,name,description' +
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

    // TODO thingId와 dataGroupId, dataKey 맵핑테이블을 통한 데이터 조회
    const dataGroupId = '10000';
    const dataKey = 'Alphadom_IndoorGML_data';
    const nodeData = _this.magoInstance.getMagoManager().hierarchyManager.getNodeByDataKey(dataGroupId, dataKey).data;
    const dataName = nodeData.data_name;
    const baseFloor = 7;

    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {

            // Thing
            const thing = msg;

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

            const buildingInfo = {
                id: id,
                name: thing.name,
                value: value,
                valueWithCommas: _this.numberWithCommas(value),
                unit: _this.getUnit(dataStream),
                dataName: dataName,
                grade: grade,
                gradeText: _this.getGradeMessage(grade),
                dataGroupId: dataGroupId,
                dataKey: dataKey,
                baseFloor: baseFloor,
                listOfFloorOccupancy: []
            };

            _this.getFloorInformation(buildingInfo, name);

        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });

}

OccupancySensorThings.prototype.getFloorInformation = function (buildingInfo, name) {

    const _this = this;
    const observedProperty = 'occupancyFloor';
    const queryString = 'Datastreams?$select=id,name,unitOfMeasurement&' +
        '$filter=Thing/name eq \'' + name + '\' and ObservedProperty/name eq \'' + observedProperty + '\'&' +
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

            // 재실자 층 선택
            $("#buildingInfoWrap table tr").click(function() {
                $(this).siblings().removeClass('on');
                $(this).toggleClass('on');
                const floor = $(this).data('floor');
                _this.displaySelectedFloor(floor, buildingInfo.dataGroupId);
                searchDataAPI(_this.magoInstance, buildingInfo.dataGroupId, _this.selectedDataKey);
                _this.clearOverlay();
                _this.addOverlay(name, floor);
            });

        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });

};

OccupancySensorThings.prototype.displaySelectedFloor = function(floor, dataGroupId) {
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

OccupancySensorThings.prototype.displaySelectedFloorMaker = function(floor) {

}

OccupancySensorThings.prototype.closeBuildingInformation = function () {
    $('#buildingInfoWrap').hide();
    this.selectedThingId = 0;
    this.selectedDataStreams = [];
};

OccupancySensorThings.prototype.getInformation = function (thingId) {

};

OccupancySensorThings.prototype.closeInformation = function () {

};

OccupancySensorThings.prototype.update = function () {

};