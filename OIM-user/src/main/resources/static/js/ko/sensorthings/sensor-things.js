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

    this.gaugeChartNeedle = {};
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

/**
 * 게이지 차트 그리기
 * @param percent
 */
SensorThings.prototype.drawGaugeChart = function (range, total, percent) {

    const gaugeChartOptions = {
        rotation: 1 * Math.PI,
        circumference: 1 * Math.PI,
        legend: {
            display: false
        },
        tooltips: {
            enabled: false
        },
        cutoutPercentage: 80
    };

    const gaugeChart = new Chart(document.getElementById("gaugeChart"), {
        type: 'doughnut',
        data: {
            labels: [this.getGradeMessage(1), this.getGradeMessage(2), this.getGradeMessage(3), this.getGradeMessage(4)],
            datasets: [
                {
                    data: [
                        (range[1] - range[0]) / total * 100,
                        (range[2] - range[1]) / total * 100,
                        (range[3] - range[2]) / total * 100,
                        (range[4] - range[3]) / total * 100
                    ],
                    backgroundColor: [
                        'rgba(30, 144, 255, 1)',
                        'rgba(0, 199, 60, 1)',
                        'rgba(255, 215, 0, 1)',
                        'rgba(255, 89, 89, 1)'
                    ],
                    borderColor: [
                        'rgba(255, 255, 255 ,1)',
                        'rgba(255, 255, 255 ,1)',
                        'rgba(255, 255, 255 ,1)'
                    ],
                    borderWidth: 0
                }
            ]
        },
        options: gaugeChartOptions
    });

    this.gaugeChartNeedle = new Chart(document.getElementById("gaugeChartNeedle"), {
        type: 'doughnut',
        data: {
            datasets: [
                {
                    data: [percent - 0.5, 1, 100 - (percent + 0.5)],
                    backgroundColor: [
                        'rgba(0, 0, 0 ,0)',
                        'rgba(255,255,255,1)',
                        'rgba(0, 0, 0 ,0)',
                    ],
                    borderColor: [
                        'rgba(0, 0, 0 ,0)',
                        'rgba(0, 0, 0 ,1)',
                        'rgba(0, 0, 0 ,0)'
                    ],
                    borderWidth: 1
                }
            ]
        },
        options: gaugeChartOptions
    });

};