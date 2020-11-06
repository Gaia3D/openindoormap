const OccupancySensorThings = function (magoInstance) {
    SensorThings.call(this, magoInstance);

    this.magoInstance = magoInstance;
    this.type = 'iot_occupancy';
    this.observedProperty = 'occupancy';
    this.observedPropertyColor = {
        'occupancy': '#E91E63',
        'occupancyBuilding': '#FF9800',
        'occupancyFloor': '#2196F3'
    };
    this.occupancyGradeMin = 0;
    this.occupancyGradeMax = 10;

    this.currentTime = "2020-11-06T11:30:59.999Z";
    //this.currentTime = moment.utc().format();
    this.callInterval = 10;         // 10s
    this.filterInterval = 60;     // 60s

    this.gaugeChartNeedle = {};
    this.hourlyAirQualityChart = {};
    this.chartTitle = '재실자(Occupancy)';
    this.chartXAxesTitle = '시간(분)';
    this.chartYAxesTitle = '재실자(명)';

};
OccupancySensorThings.prototype = Object.create(SensorThings.prototype);
OccupancySensorThings.prototype.constructor = OccupancySensorThings;

OccupancySensorThings.prototype.getGrade = function (value) {
    let grade = 0;
    if (value < 3) {
        grade = 1;
    } else if (value >= 3 && value < 6) {
        grade = 2;
    } else if (value >= 6 && value < 11) {
        grade = 3;
    } else if (value >= 11){
        grade = 4;
    }
    return grade;
}
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

            msg.contents = [];
            const things = msg.value;
            for (const thing of things) {

                const thingId = thing['@iot.id'];

                // TODO thingId와 dataGroupId, dataKey 맵핑테이블을 통한 데이터 조회
                const dataGroupId = 'result';
                const dataKey = 'Alphadom_IndoorGML_data';
                const dataName = '알파돔';

                // Datastreams
                const dataStreams = thing['Datastreams'];
                if (!dataStreams || dataStreams.length <= 0) continue;
                const dataStream = dataStreams[0];

                // Observations
                const observations = dataStream['Observations'];
                let value = '-', grade = 0;
                if (observations && observations.length > 0) {
                    const observationTop = observations[0];
                    //value = observationTop.result.value;
                    //grade = observationTop.result.grade;
                    value = _this.numberWithCommas(observationTop.result);
                    grade = _this.getGrade(observationTop.result);
                }

                msg.contents.push({
                    id: thingId,
                    value: value,
                    unit: _this.getUnit(dataStream),
                    dataName: dataName,
                    grade: grade,
                    gradeText: _this.getGradeMessage(grade),
                    dataGroupId: dataGroupId,
                    dataKey: dataKey,
                    moreTitle: '#{common.more}'
                });

            }

            const templateSearchSummary = Handlebars.compile($("#searchSummarySource").html());
            $("#iotSearchSummaryDHTML").html("").append(templateSearchSummary(msg));

            const template = Handlebars.compile($("#occupancyListSource").html());
            $("#iotOccupancyListDHTML").html("").append(template(msg));

            const templatePagination = Handlebars.compile($("#paginationSource").html());
            $("#iotPaginationDHTML").html("").append(templatePagination(msg));

            $('#iotOccupancyListDHTML').show();
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
                '$filter=resultTime lt ' + _this.getCurrentTime() + ' and resultTime ge ' + _this.getFilterDayStartTime() +
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
                    //value = observationTop.result.value;
                    value = _this.numberWithCommas(observationTop.result);
                }
                contents.dataStreams.push({
                    name: dataStream.name,
                    value: value,
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

/**
 * 재실자 건물 더보기 닫기
 * @param obj
 */
DustSensorThings.prototype.closeDetail = function (obj) {
    const $iotDustMoreDHTML = $(obj).parents(".iotDustMoreDHTML");
    $iotDustMoreDHTML.hide();
    $(".show-more").show();
}

/**
 * 지도 Overlay 생성
 */
OccupancySensorThings.prototype.addOverlay = function () {

};

OccupancySensorThings.prototype.redrawOverlay = function () {

};

OccupancySensorThings.prototype.getInformation = function (thingId) {

};

OccupancySensorThings.prototype.closeInformation = function () {

};

OccupancySensorThings.prototype.update = function () {

};