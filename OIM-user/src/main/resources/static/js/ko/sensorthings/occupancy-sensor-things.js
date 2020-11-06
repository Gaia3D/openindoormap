const OccupancySensorThings = function (magoInstance) {
    SensorThings.call(this, magoInstance);

    this.magoInstance = magoInstance;
    this.observedProperty = 'occupancy';
    this.observedPropertyColor = {
        'occupancy': '#E91E63',
        'occupancyBuilding': '#FF9800',
        'occupancyFloor': '#2196F3'
    };

    this.currentTime = "2020-11-03T04:00:00.000Z";
    //this.currentTime = moment.utc().format();
    this.callInterval = 10;         // 10s
    this.filterInterval = 60;     // 60s

};
OccupancySensorThings.prototype = Object.create(SensorThings.prototype);
OccupancySensorThings.prototype.constructor = OccupancySensorThings;

OccupancySensorThings.prototype.getList = function (pageNo, params) {

    const _this = this;
    pageNo = parseInt(pageNo);
    _this.currentPageNo = pageNo;
    const skip = (pageNo - 1) * 5;

    let filter = 'Datastreams/ObservedProperty/name eq \'' + _this.observedProperty + '\'';
    if (params.searchValue) {
        filter += 'and (startswith(name, \'' + params.searchValue + '\') or endswith(name, \'' + params.searchValue + '\'))';
    }

    const queryString = 'Things?$select=@iot.id,name,description' +
        '&$top=5&$skip=' + skip + '&$count=true&$orderby=name asc&$filter=' + filter +
        '&$expand=Locations($select=@iot.id,location,name),' +
        'Datastreams($select=@iot.id,description,unitOfMeasurement;$filter=ObservedProperty/name eq \'' + _this.observedProperty + '\'),' +
        'Datastreams/Observations($select=result,phenomenonTime,resultTime;$orderby=resultTime desc;$filter=resultTime lt ' + _this.getCurrentTime() + ' and resultTime ge ' + _this.getFilterStartTime() + ')';

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

                // Locations
                //const location = thing.Locations[0];
                //const locationId = location['@iot.id'];
                //const addr = location.name;
                //const coordinates = location.location.coordinates;

                const thingId = thing['@iot.id'];

                // Datastreams
                const dataStream = thing.Datastreams[0];
                const unit = dataStream.unitOfMeasurement.symbol;

                // Observations
                if (!dataStream['Observations'] || dataStream['Observations'].length <= 0) continue;
                const observation = dataStream.Observations[0];
                const value = observation.result.value;
                //const grade = observation.result.grade;
                const grade = Math.floor(Math.random() * 5);
                const gradeText = _this.getGradeMessage(grade);

                msg.contents.push({
                    id: thingId,
                    value: value,
                    unit: unit,
                    stationName: thing.name,
                    //addr: addr,
                    grade: grade,
                    gradeText: gradeText,
                    //longitude: coordinates[0],
                    //latitude: coordinates[1],
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
        '$filter=resultTime le ' + _this.getCurrentTime() + ' and resultTime gt ' + _this.getFilterDayStartTime() +
        ')';

    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {

            // Datastreams
            const dataStreams = msg.value;

            const contents = {
                dataStreams: []
            };

            for (const dataStream of dataStreams) {
                const observation = dataStream['Observations'];
                if (!observation || observation.length <= 0) continue;
                contents.dataStreams.push({
                    name: dataStream.name,
                    value: _this.formatValueByDigits(observation[0].result.value, 3),
                    unit: dataStream['unitOfMeasurement']['symbol']
                });
            }

            const $iotDustMoreDHTML = $(obj).parent().siblings(".iotDustMoreDHTML");
            const template = Handlebars.compile($("#dustMoreSource").html());
            $iotDustMoreDHTML.html("").append(template(contents));
            $iotDustMoreDHTML.show();

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