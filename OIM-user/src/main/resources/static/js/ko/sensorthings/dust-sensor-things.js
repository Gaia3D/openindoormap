const DustSensorThings = function (magoInstance) {
    SensorThings.call(this, magoInstance);
    this.magoInstance = magoInstance;
    this.queryString = 'Locations?$select=@iot.id,location,name&$expand=' +
        'Things($select=@iot.id,name,description),' +
        'Things/Datastreams($select=@iot.id,description,unitOfMeasurement;$filter=ObservedProperty/name eq \'미세먼지(PM10) Particulates\'),' +
        'Things/Datastreams/Observations($select=result,phenomenonTime;$orderby=phenomenonTime asc;$top=1)';
    this.locations = [];
};
DustSensorThings.prototype = Object.create(SensorThings.prototype);
DustSensorThings.prototype.constructor = DustSensorThings;

DustSensorThings.prototype.init = function () {
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

DustSensorThings.prototype.getComprehensiveAirQualityIndex = function (value) {
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
DustSensorThings.prototype.getComprehensiveAirQualityIndexMessage = function (cai) {
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
DustSensorThings.prototype.updateContentPosition = function() {

    for (const location of this.locations) {

        const locationId = location['@iot.id'];
        const thing = location.Things[0];
        const dataStream = thing.Datastreams[0];
        const unit = dataStream.unitOfMeasurement.symbol;
        const observation = dataStream.Observations[0].result;
        const addr = location.name;
        const cai = this.getComprehensiveAirQualityIndex(observation);
        const caiText = this.getComprehensiveAirQualityIndexMessage(cai);

        location.contents = {
            id : locationId,
            value: observation,
            unit: unit,
            stationName: thing.name,
            addr: addr,
            cai: cai,
            caiText: caiText
        };

        const coordinates = location.location.geometry.coordinates;
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
                const template = Handlebars.compile($("#overlaySource").html());
                const html = template(location.contents);
                $containerSelector.prepend(html);
            }

            $overlaySelector.children().css({
                position: 'absolute',
                top: resultScreenCoord.y,
                left: resultScreenCoord.x,
                backgroundColor: 'rgb(0, 255, 255)',
                zIndex: 1000
            });
            $overlaySelector.css('visibility', 'visible');

        }
    }

}
DustSensorThings.prototype.updateContentValue = function() {
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
                $overlaySelector.css('visibility', 'visible');
            },
            error: function (request, status, error) {
                alert(JS_MESSAGE["ajax.error.message"]);
            }
        });
    }
};
