const SensorThings = function (magoInstance) {
    this.magoInstance = magoInstance;
    this.FROST_SERVER_URL = 'http://localhost:8888/FROST-Server/v1.0/';
    this.queryString = '';
    this.type = 'iot_occupancy'; // iot_occupancy, iot_dust

    this.currentPageNo = 0;
    this.currentTime = "2020-10-23T04:59:40.000Z";
    //this.currentTime = moment.utc().format();
    this.callInterval = 10;         // 10s
    this.filterInterval = 3600;     // 1hour

    this.things = [];
    this.selectedThingId = 0;
    this.selectedDataStreams = [];
};

SensorThings.prototype.createSensorThings = function () {
    const $form = $("#searchIotForm");
    const params = getFormData($form);
    if (!params.searchWord) {
        params.searchWord = this.type;
    }
    if (params.searchWord === 'iot_occupancy') {
        return new OccupancySensorThings(this.magoInstance);
    } else if (params.searchWord === 'iot_dust') {
        return new DustSensorThings(this.magoInstance);
    }
};

SensorThings.prototype.clearOverlay = function () {
    if ($('.overlayWrap').length >= 0) {
        $('#overlayDHTML').html("");
    }
    /*
    if ($('#dustInfoDHTML').is(':visible')) {
        $('#dustInfoDHTML').hide();
    }
     */
    //this.selectedThingId = 0;
    //this.selectedDataStreams = [];
};

SensorThings.prototype.setCurrentTime = function (currentTime) {
    this.currentTime = currentTime;
};

SensorThings.prototype.getCurrentTime = function () {
    return this.currentTime;
};

SensorThings.prototype.getFilterStartTime = function () {
    return moment(this.currentTime).utc().subtract(this.filterInterval, 's').format();
};

SensorThings.prototype.getFilterDayStartTime = function () {
    return moment(this.currentTime).utc().subtract(this.filterInterval * 24, 's').format();
};

SensorThings.prototype.observationTimeToLocalTime = function (observationTime) {
    return moment.parseZone(observationTime).local().format();
};

SensorThings.prototype.formatValueByDigits = function (value, digits) {
    return parseFloat(parseFloat(value).toFixed(digits));
};

SensorThings.prototype.geographicCoordToScreenCoord = function (coordinates) {
    const resultWorldPoint = Mago3D.ManagerUtils.geographicCoordToWorldPoint(coordinates[0], coordinates[1], coordinates[2]);
    const magoManager = this.magoInstance.getMagoManager();
    const resultScreenCoord = Mago3D.ManagerUtils.calculateWorldPositionToScreenCoord(magoManager.getGl(), resultWorldPoint.x, resultWorldPoint.y, resultWorldPoint.z, undefined, magoManager);
    return resultScreenCoord;
};

/**
 * 등급별 상태메세지 가져오기
 * @param grade
 * @returns {*}
 */
SensorThings.prototype.getGradeMessage = function (grade) {
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

SensorThings.prototype.dataSearch = function (pageNo) {
    $('#iotInfoContent div').hide();
    const $form = $("#searchIotForm");
    const params = getFormData($form);

    if (!params.searchWord) {
        params.searchWord = this.type;
    }
    sensorThings.getList(pageNo, params);
};

SensorThings.prototype.getUnit = function(dataStream) {
    return dataStream['unitOfMeasurement']['symbol'];
}

SensorThings.prototype.getObservedPropertyName = function(dataStream) {
    return dataStream['ObservedProperty']['name'];
}

SensorThings.prototype.numberWithCommas = function (x) {
    return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

SensorThings.prototype.closeDetail = function (obj) {
    const $iotDustMoreDHTML = $(obj).parents(".iotDustMoreDHTML");
    $iotDustMoreDHTML.hide();
    $(".show-more").show();
}
