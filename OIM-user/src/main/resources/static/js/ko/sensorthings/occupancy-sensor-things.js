const OccupancySensorThings = function (magoInstance) {
    SensorThings.call(this, magoInstance);
    this.magoInstance = magoInstance;
    this.queryString = 'Locations?$select=@iot.id,location,name&$expand=' +
        'Things($select=@iot.id,name,description),' +
        'Things/Datastreams($select=@iot.id,description,unitOfMeasurement;$filter=ObservedProperty/name eq \'pm10Value\'),' +
        'Things/Datastreams/Observations($select=result,phenomenonTime;$orderby=phenomenonTime asc;$top=1)';
    this.locations = [];
};
OccupancySensorThings.prototype = Object.create(SensorThings.prototype);
OccupancySensorThings.prototype.constructor = OccupancySensorThings;

OccupancySensorThings.prototype.addOverlay = function () {
    const _this = this;
    $.ajax({
        url: _this.FROST_SERVER_URL + _this.queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {
            _this.locations = msg.value;
            _this.updateContentPosition();
        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });
};

OccupancySensorThings.prototype.getList = function (pageNo, params) {
    const _this = this;
    $.ajax({
        url: _this.FROST_SERVER_URL +
            'Locations?$select=@iot.id,location,name&$top=5&$count=true&$orderby=name asc&$expand=' +
            'Things($select=@iot.id,name,description),' +
            'Things/Datastreams($select=@iot.id,description,unitOfMeasurement;$filter=ObservedProperty/name eq \'pm10Value\'),' +
            'Things/Datastreams/Observations($select=result,phenomenonTime,resultTime;$orderby=resultTime desc;$top=1)',
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {
            console.info(msg);

            const pagination = new Pagination(pageNo, msg['@iot.count'], 5, msg['@iot.nextLink']);
            msg.pagination = pagination;

            const templateLegend = Handlebars.compile($("#iotLegendSource").html());
            $("#iotLegendDHTML").html("").append(templateLegend(params));

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

OccupancySensorThings.prototype.getComprehensiveAirQualityIndex = function (value) {
    let cai = 0;
    if (value >= 0 && value < 31) {
        cai = 1;
    } else if (value >= 31 && value < 81) {
        cai = 2;
    } else if (value >= 81 && value < 151) {
        cai = 3;
    } else if (value >= 151 && value < 601) {
        cai = 4;
    }
    return cai;
};
OccupancySensorThings.prototype.getComprehensiveAirQualityIndexMessage = function (cai) {
    let message;
    switch (cai) {
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
OccupancySensorThings.prototype.updateContentPosition = function() {

    for (const location of this.locations) {

        const locationId = location['@iot.id'];
        const thing = location.Things[0];
        const dataStream = thing.Datastreams[0];
        const unit = dataStream.unitOfMeasurement.symbol;
        const observation = dataStream.Observations[0].result;
        const addr = location.name;
        const cai = this.getComprehensiveAirQualityIndex(observation);
        const caiText = this.getComprehensiveAirQualityIndexMessage(cai);

        const coordinates = location.location.coordinates;
        const resultWorldPoint = Mago3D.ManagerUtils.geographicCoordToWorldPoint(coordinates[0], coordinates[1], 0);
        const magoManager = this.magoInstance.getMagoManager();
        const resultScreenCoord = Mago3D.ManagerUtils.calculateWorldPositionToScreenCoord(magoManager.getGl(), resultWorldPoint.x, resultWorldPoint.y, resultWorldPoint.z, undefined, magoManager);

        const $containerSelector = $('#magoContainer');
        const $overlaySelector = $('#overlay_' + locationId);
        const top = 0, left = 0;
        let bottom = top + $containerSelector.outerHeight() - 76, right = left + $containerSelector.outerWidth() - 60;
        // bottom -= $('#overlay_' + locationId).outerHeight(); right -= $('#overlay_' + locationId).outerWidth();

        if (resultScreenCoord.x >= left && resultScreenCoord.x <= right &&
            resultScreenCoord.y >= top && resultScreenCoord.y <= bottom) {

            if ($overlaySelector.length === 0) {
                location.contents = {
                    id : locationId,
                    value: observation,
                    unit: unit,
                    stationName: thing.name,
                    addr: addr,
                    cai: cai,
                    caiText: caiText,
                    top: resultScreenCoord.y,
                    left: resultScreenCoord.x
                };
                const template = Handlebars.compile($("#overlaySource").html());
                const html = template(location.contents);
                $containerSelector.prepend(html);
            } else {
                $overlaySelector.children().css({
                    position: 'absolute',
                    top: resultScreenCoord.y,
                    left: resultScreenCoord.x,
                    backgroundColor: 'rgb(0, 255, 255)',
                    zIndex: 1000
                });
            }
            $overlaySelector.show();

        }
    }

}
OccupancySensorThings.prototype.updateContentValue = function() {
    const _this = this;
    // http://localhost:8888/FROST-Server/v1.0/Locations(1)?$select=location&$expand=Things/Datastreams($select=@iot.id,description;$filter=ObservedProperty/name eq '미세먼지(PM10) Particulates'),Things/Datastreams/Observations($select=result,phenomenonTime;$orderby=phenomenonTime desc;$top=1)

    for (const location of this.locations) {
        const locationId = location['@iot.id'];
        const queryString = 'Locations('+locationId+')?$select=location&$expand=Things/Datastreams($select=@iot.id,description;$filter=ObservedProperty/name eq \'미세먼지(PM10) Particulates\'),Things/Datastreams/Observations($select=result,phenomenonTime;$orderby=phenomenonTime desc;$top=1)';
        $.ajax({
            url: _this.FROST_SERVER_URL + queryString,
            type: "GET",
            dataType: "json",
            headers: {"X-Requested-With": "XMLHttpRequest"},
            success: function (msg) {
                location.contents.value = msg.Things[0].Datastreams[0].Observations[0].result;
                const $overlaySelector = $('#overlay_' + locationId);
                $overlaySelector.find('.overlay-value').text(location.contents.value);
                $overlaySelector.show();
            },
            error: function (request, status, error) {
                alert(JS_MESSAGE["ajax.error.message"]);
            }
        });
    }
};
