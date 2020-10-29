const SensorThings = function (magoInstance) {
    this.magoInstance = magoInstance;
    this.FROST_SERVER_URL = 'http://localhost:8888/FROST-Server/v1.0/';
    this.queryString = '';
    this.type = 'iot_occupancy'; // iot_occupancy, iot_dust
    this.currentTime = "2020-10-23T04:59:40.000Z";
    //this.currentTime = moment.utc().format();
    this.callInterval = 3600;         // 10s
    this.filterInterval = 3600;     // 1hour
};

SensorThings.prototype.init = function () {

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
    const resultWorldPoint = Mago3D.ManagerUtils.geographicCoordToWorldPoint(coordinates[0], coordinates[1], 0);
    const magoManager = this.magoInstance.getMagoManager();
    const resultScreenCoord = Mago3D.ManagerUtils.calculateWorldPositionToScreenCoord(magoManager.getGl(), resultWorldPoint.x, resultWorldPoint.y, resultWorldPoint.z, undefined, magoManager);
    return resultScreenCoord;
};

SensorThings.prototype.dataSearch = function (pageNo) {
    $('#iotInfoContent div').hide();
    const $form = $("#searchIotForm");
    const params = getFormData($form);

    if (this.type === 'iot_occupancy') {
        occupancySensorThings.getList(pageNo, params);
    } else if (this.type === 'iot_dust') {
        dustSensorThings.getList(pageNo, params);
    }
};
