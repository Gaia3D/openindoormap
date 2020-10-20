const DustSensorThings = function (magoInstance) {
    SensorThings.call(this, magoInstance);
    this.magoInstance = magoInstance;
    this.queryString = 'Locations?$select=@iot.id,location,name&$count=true&$expand=' +
        'Things($select=@iot.id,name,description),' +
        'Things/Datastreams($select=@iot.id,description,unitOfMeasurement;$filter=ObservedProperty/name eq \'미세먼지(PM10) Particulates\'),' +
        'Things/Datastreams/Observations($select=result,phenomenonTime;$orderby=phenomenonTime asc;$top=1)';
    this.locations = [];
};
DustSensorThings.prototype = Object.create(SensorThings.prototype);
DustSensorThings.prototype.constructor = DustSensorThings;

/**
 * 초기 데이터 표출
 */
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

/**
 * CAI(통합대기환경지수) 구하기
 * @param value
 * @returns {number}
 */
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

/**
 * CAI 지수구분 구하기
 * @param cai
 * @returns {*}
 */
DustSensorThings.prototype.getComprehensiveAirQualityIndexGrade = function (cai) {
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

/**
 * 센서 컨텐츠 위치 갱신
 */
DustSensorThings.prototype.updateContentPosition = function () {

    for (const location of this.locations) {

        const locationId = location['@iot.id'];
        const thing = location.Things[0];
        const dataStream = thing.Datastreams[0];
        const unit = dataStream.unitOfMeasurement.symbol;
        const observation = dataStream.Observations[0].result;
        const addr = location.name;
        const cai = this.getComprehensiveAirQualityIndex(observation);
        const caiText = this.getComprehensiveAirQualityIndexGrade(cai);

        const coordinates = location.location.geometry.coordinates;
        const resultWorldPoint = Mago3D.ManagerUtils.geographicCoordToWorldPoint(coordinates[0], coordinates[1], 0);
        const magoManager = this.magoInstance.getMagoManager();
        const resultScreenCoord = Mago3D.ManagerUtils.calculateWorldPositionToScreenCoord(magoManager.getGl(), resultWorldPoint.x, resultWorldPoint.y, resultWorldPoint.z, undefined, magoManager);

        const $containerSelector = $('#magoContainer');
        const $overlaySelector = $('#overlay_' + locationId);
        const top = 0, left = 0;
        let bottom = top + $containerSelector.outerHeight() - 55, right = left + $containerSelector.outerWidth() - 160;
        // bottom -= $('#overlay_' + locationId).outerHeight(); right -= $('#overlay_' + locationId).outerWidth();

        if (resultScreenCoord.x >= left && resultScreenCoord.x <= right &&
            resultScreenCoord.y >= top && resultScreenCoord.y <= bottom) {

            if ($overlaySelector.length === 0) {
                location.contents = {
                    id: locationId,
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
                    zIndex: 1000
                });
            }
            $overlaySelector.show();

        }
    }
};

/**
 * 센서 컨텐츠 값 갱신
 */
DustSensorThings.prototype.updateContentValue = function () {
    const _this = this;
    // http://localhost:8888/FROST-Server/v1.0/Locations(1)?$select=location&$expand=Things/Datastreams($select=@iot.id,description;$filter=ObservedProperty/name eq '미세먼지(PM10) Particulates'),Things/Datastreams/Observations($select=result,phenomenonTime;$orderby=phenomenonTime desc;$top=1)

    for (const location of this.locations) {
        const locationId = location['@iot.id'];
        const queryString = 'Locations(' + locationId + ')?' +
            '$select=location&$expand=Things/Datastreams($select=@iot.id,description;$filter=ObservedProperty/name eq \'미세먼지(PM10) Particulates\'),' +
            'Things/Datastreams/Observations($select=result,phenomenonTime;$orderby=phenomenonTime desc;$top=1)';
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

DustSensorThings.prototype.getSensorInformation = function (locationId) {
    const _this = this;

    // http://localhost:8888/FROST-Server/v1.0/Locations(1)?$select=@iot.id&$expand=Things/DataStreams($select=@iot.id,description,name,unitOfMeasurement),Things/DataStreams/Observations($select=result,phenomenonTime;$orderby=phenomenonTime desc;$top=24)

    let queryString = 'Locations('+locationId+')?' +
        '$select=@iot.id&' +
        '$expand=Things/DataStreams($select=@iot.id,description,name,unitOfMeasurement),' +
                'Things/DataStreams/Observations($select=result,phenomenonTime;$orderby=phenomenonTime desc;$top=24)';
    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {

            const contents = {
                min: 0,
                max: 600,
                dataStreams: []
            };

            for (const dataStream of msg.Things[0].Datastreams) {
                const data = {
                    id: dataStream['@iot.id'],
                    name: dataStream.name,
                    unit: dataStream.unitOfMeasurement.symbol,
                    observations: dataStream.Observations
                };
                if (data.name === '미세먼지(PM10)') {
                    contents.pm10 = data.observations[0].result;
                    contents.pm10Percent = contents.pm10 / 600 * 100;
                    contents.cai = _this.getComprehensiveAirQualityIndex(contents.pm10);
                }
                contents.dataStreams.push(data);
            }

            console.info(contents);

            const $dustInfoWrap = $('#dustInfoWrap');
            const template = Handlebars.compile($("#dustInfoSource").html());
            const html = template(contents);
            if ($dustInfoWrap.length === 0) {
                const wrapper ='<div id="dustInfoWrap" class="sensor-detail-wrap">' + html + '</div>';
                $('.cesium-viewer').append(wrapper);
            }
            $dustInfoWrap.html(html);
            $dustInfoWrap.show();
            _this.drawChart(contents.pm10Percent);

        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });
};

DustSensorThings.prototype.drawChart = function (pm10Percent) {

    const doughnutChart = new Chart(document.getElementById("doughnutChart"), {
        type: 'doughnut',
        data: {
            labels: ["좋음", "보통", "나쁨", "아주나쁨"],
            datasets: [
                {
                    label: '# of Votes',
                    data: [25, 25, 25, 25],
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
        options: {
            rotation: 1 * Math.PI,
            circumference: 1 * Math.PI,
            legend: {
                display: false
            },
            tooltip: {
                enabled: true
            },
            cutoutPercentage: 80
        }
    });

    const doughnutChartBar = new Chart(document.getElementById("doughnutChartBar"), {
        type: 'doughnut',
        data: {
            labels: ["", "", ""],
            datasets: [
                {
                    data: [pm10Percent - 0.5, 1, 100 - (pm10Percent + 0.5)],
                    backgroundColor: [
                        "rgba(0,0,0,0)",
                        "rgba(255,255,255,1)",
                        "rgba(0,0,0,0)",
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
        options: {
            rotation: 1 * Math.PI,
            circumference: 1 * Math.PI,
            legend: {
                display: false
            },
            tooltips: {
                enabled: false
            },
            cutoutPercentage: 80
        }
    });
/*
    const hourlyAirQualityChart = new Chart(document.getElementById("hourlyAirQualityChart"), {
        type: 'doughnut',
        data: {
            labels: ["좋음", "보통", "나쁨", "아주나쁨"],
            datasets: [
                {
                    label: '# of Votes',
                    data: [25, 25, 25, 25],
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
        options: {
            rotation: 1 * Math.PI,
            circumference: 1 * Math.PI,
            legend: {
                display: false
            },
            tooltip: {
                enabled: true
            },
            cutoutPercentage: 80
        }
    });
*/

}

DustSensorThings.prototype.closeSensorInformation = function () {
    $('#dustInfoWrap').hide();
};