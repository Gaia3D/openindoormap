const DustSensorThings = function (magoInstance) {
    SensorThings.call(this, magoInstance);
    this.magoInstance = magoInstance;
    this.queryString = 'Locations?$select=@iot.id,location,name&$top=1000&$count=true&$expand=' +
        'Things($select=@iot.id,name,description),' +
        'Things/Datastreams($select=@iot.id,description,unitOfMeasurement;$filter=ObservedProperty/name eq \'pm10Value\'),' +
        'Things/Datastreams/Observations($select=result,phenomenonTime,resultTime;$orderby=resultTime desc;$top=1)';
    this.locations = [];
    this.observedPropertyColor = {
        '미세먼지(PM10)': '#E91E63',
        '미세먼지(PM2.5)': '#9C27B0',
        '오존 농도': '#FF9800',
        '아황산가스 농도': '#2196F3',
        '일산화탄소 농도': '#607D8B',
        '이산화질소 농도': '#00BCD4'
    }
};
DustSensorThings.prototype = Object.create(SensorThings.prototype);
DustSensorThings.prototype.constructor = DustSensorThings;

DustSensorThings.prototype.dustList = function (pageNo, params) {
    const _this = this;

    // http://localhost:8888/FROST-Server/v1.0/Things?$select=@iot.id,name,description&$top=5&$count=true&$orderby=name asc&$filter=startswith(name, '이') or endswith(name, '이')&$expand=Locations($select=@iot.id,location,name),Datastreams($select=@iot.id,description,unitOfMeasurement;$filter=ObservedProperty/name eq 'pm10Value'),Datastreams/Observations($select=result,phenomenonTime,resultTime;$orderby=resultTime desc;$top=1)

    pageNo = parseInt(pageNo);
    const skip = (pageNo - 1) * 5;

    let filter = '';
    if (params.searchValue) {
        filter = '&$filter=startswith(name, \'' + params.searchValue + '\') or endswith(name, \'' + params.searchValue + '\')';
    }

    const queryString = 'Things?$select=@iot.id,name,description' +
        '&$top=5&$skip=' + skip + '&$count=true&$orderby=name asc' + filter +
        '&$expand=Locations($select=@iot.id,location,name),' +
        'Datastreams($select=@iot.id,description,unitOfMeasurement;$filter=ObservedProperty/name eq \'pm10Value\'),' +
        'Datastreams/Observations($select=result,phenomenonTime,resultTime;$orderby=resultTime desc;$top=1)';

    $.ajax({
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
                const location = thing.Locations[0];
                const locationId = location['@iot.id'];
                const addr = location.name;
                const coordinates = location.location.coordinates;

                // Datastreams
                const dataStream = thing.Datastreams[0];
                const unit = dataStream.unitOfMeasurement.symbol;

                // Observations
                const observation = dataStream.Observations[0];
                const value = observation.result.value;
                const cai = observation.result.grade;
                const caiText = _this.getComprehensiveAirQualityIndexGrade(cai);

                msg.contents.push({
                    id: locationId,
                    value: value,
                    unit: unit,
                    stationName: thing.name,
                    addr: addr,
                    cai: cai,
                    caiText: caiText,
                    longitude: coordinates[0],
                    latitude: coordinates[1]
                });

            }

            const templateSearchSummary = Handlebars.compile($("#searchSummarySource").html());
            $("#iotSearchSummaryDHTML").html("").append(templateSearchSummary(msg));

            const template = Handlebars.compile($("#dustListSource").html());
            $("#iotDustListDHTML").html("").append(template(msg));

            const templatePagination = Handlebars.compile($("#iotPaginationSource").html());
            $("#iotPaginationDHTML").html("").append(templatePagination(msg));

            $('#iotDustListDHTML').show();
        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });
};

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
    const num = parseInt(cai);
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

/**
 * 센서 컨텐츠 위치 갱신
 */
DustSensorThings.prototype.updateContentPosition = function () {

    for (const location of this.locations) {

        const locationId = location['@iot.id'];
        const thing = location.Things[0];
        const dataStream = thing.Datastreams[0];
        const unit = dataStream.unitOfMeasurement.symbol;
        const observation = dataStream.Observations[0].result.value;
        const addr = location.name;
        //const cai = this.getComprehensiveAirQualityIndex(observation);
        const cai = dataStream.Observations[0].result.grade;
        const caiText = this.getComprehensiveAirQualityIndexGrade(cai);

        const coordinates = location.location.coordinates;
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
    // http://localhost:8888/FROST-Server/v1.0/Locations(1)?$select=location&$expand=Things/Datastreams($select=@iot.id,description;$filter=ObservedProperty/name eq 'pm10Value'),Things/Datastreams/Observations($select=result,phenomenonTime,resultTime;$orderby=resultTime desc;$top=1)

    for (const location of this.locations) {
        const locationId = location['@iot.id'];
        const queryString = 'Locations(' + locationId + ')?' +
            '$select=location&$expand=Things/Datastreams($select=@iot.id,description;$filter=ObservedProperty/name eq \'pm10Value\'),' +
            'Things/Datastreams/Observations($select=result,phenomenonTime,resultTime;$orderby=resultTime desc;$top=1)';
        $.ajax({
            url: _this.FROST_SERVER_URL + queryString,
            type: "GET",
            dataType: "json",
            headers: {"X-Requested-With": "XMLHttpRequest"},
            success: function (msg) {
                location.contents.value = msg.Things[0].Datastreams[0].Observations[0].result.value;
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

DustSensorThings.prototype.getSensorMoreInformation = function(obj, locationId) {
    $(".iotDustMoreDHTML").hide();
    if ($(obj).hasClass('on')) {
        $(obj).removeClass('on');
        return;
    }
    $("#iotDustListDHTML .more").not($(obj)).removeClass('on');

    const _this = this;

    // http://localhost:8888/FROST-Server/v1.0/Locations(1)?$select=@iot.id&$expand=Things/DataStreams($select=@iot.id,description,name,unitOfMeasurement;$orderby=ObservedProperty/@iot.id asc),Things/DataStreams/Observations($select=result,resultTime;$orderby=resultTime desc;$top=1)

    let queryString = 'Locations('+locationId+')?' +
        '$select=@iot.id&' +
        '$expand=Things/DataStreams($select=@iot.id,description,name,unitOfMeasurement;$orderby=ObservedProperty/@iot.id asc),' +
        'Things/DataStreams/Observations($select=result,resultTime;$orderby=resultTime desc;$top=1)';

    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {

            const contents = {
                dataStreams: []
            };
            for (const dataStream of msg.Things[0].Datastreams) {
                const value = dataStream.Observations[0].result.value;
                const grade = dataStream.Observations[0].result.grade;
                const data = {
                    id: dataStream['@iot.id'],
                    name: dataStream.name,
                    unit: dataStream.unitOfMeasurement.symbol,
                    value: parseFloat(parseFloat(value).toFixed(3)),
                    cai: grade,
                    caiText: _this.getComprehensiveAirQualityIndexGrade(grade)
                };
                contents.dataStreams.push(data);
            }
            const $iotDustMoreDHTML = $(obj).parent().siblings(".iotDustMoreDHTML");
            const template = Handlebars.compile($("#dustMoreSource").html());
            $iotDustMoreDHTML.html("").append(template(contents));
            $iotDustMoreDHTML.show();
            $(obj).addClass('on');
        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });


}

DustSensorThings.prototype.getSensorInformation = function (locationId) {
    const _this = this;

    // http://localhost:8888/FROST-Server/v1.0/Locations(1)?$select=@iot.id&$expand=Things/DataStreams($select=@iot.id,description,name,unitOfMeasurement;$orderby=ObservedProperty/@iot.id asc),Things/DataStreams/Observations($select=result,phenomenonTime,resultTime;$orderby=resultTime desc;$top=24)

    let queryString = 'Locations('+locationId+')?' +
        '$select=@iot.id&' +
        '$expand=Things/DataStreams($select=@iot.id,description,name,unitOfMeasurement;$orderby=ObservedProperty/@iot.id asc),' +
                'Things/DataStreams/Observations($select=result,phenomenonTime,resultTime;$orderby=resultTime desc;$top=24)';
    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {

            const thing = msg.Things[0];
            const contents = {
                min: 0,
                max: 600,
                stationName: thing.name,
                dataStreams: []
            };

            for (const dataStream of thing.Datastreams) {

                const value = dataStream.Observations[0].result.value;
                const grade = dataStream.Observations[0].result.grade;

                const data = {
                    id: dataStream['@iot.id'],
                    name: dataStream.name,
                    unit: dataStream.unitOfMeasurement.symbol,
                    value: parseFloat(parseFloat(value).toFixed(3)),
                    cai: grade,
                    caiText: _this.getComprehensiveAirQualityIndexGrade(grade),
                    observations: dataStream.Observations
                };
                if (data.name === '미세먼지(PM10)') {
                    //contents.cai = _this.getComprehensiveAirQualityIndex(contents.pm10);
                    contents.cai = grade;
                    contents.pm10 = value;
                    contents.pm10Percent = contents.pm10 / 600 * 100;
                }
                contents.dataStreams.push(data);
            }

            const $dustInfoWrap = $('#dustInfoWrap');
            const template = Handlebars.compile($("#dustInfoSource").html());
            const html = template(contents);
            if ($dustInfoWrap.length === 0) {
                const wrapper ='<div id="dustInfoWrap" class="sensor-detail-wrap">' + html + '</div>';
                $('.cesium-viewer').append(wrapper);
            }
            $dustInfoWrap.html(html);
            $dustInfoWrap.show();
            _this.drawDoughnutChart(contents.pm10Percent);
            _this.drawHourlyAirQualityChart(contents.dataStreams);
        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });
};

DustSensorThings.prototype.drawDoughnutChart = function (pm10Percent) {

    const doughnutChartOptions = {
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

    const doughnutChart = new Chart(document.getElementById("doughnutChart"), {
        type: 'doughnut',
        data: {
            labels: ["좋음", "보통", "나쁨", "아주나쁨"],
            datasets: [
                {
                    label: '통합대기환경지수(CAI)',
                    data: [30/6, 80/6, 150/6, 100 - (30/6 + 80/6 + 150/6)],
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
        options: doughnutChartOptions
    });

    const doughnutChartBar = new Chart(document.getElementById("doughnutChartBar"), {
        type: 'doughnut',
        data: {
            datasets: [
                {
                    data: [pm10Percent - 0.5, 1, 100 - (pm10Percent + 0.5)],
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
        options: doughnutChartOptions
    });

}

DustSensorThings.prototype.drawHourlyAirQualityChart = function (dataStreams) {

    const datasets = [];
    for (const dataStream of dataStreams) {
        const points = [];
        for (const observation of dataStream.observations) {
            const point = {
                x : observation.resultTime,
                y : observation.result.value
            };
            points.push(point);
        }
        const name = dataStream.name;
        const dataset = {
            label: name,
            data: points,
            borderColor: this.observedPropertyColor[name],
            backgroundColor: new Color(this.observedPropertyColor[name]).alpha(0.2).rgbString()
        };
        datasets.push(dataset);
    }

    const options = {
        responsive: true,
        legend: {
            position: 'bottom',
            labels: {
                fontSize: 10,
                usePointStyle: true
            }
        },
        title: {
            display: true,
            text: '1시간 공기질(Hourly Air Quality)'
        },
        tooltips: {
            mode: 'index',
            intersect: false,
        },
        hover: {
            mode: 'nearest',
            intersect: true
        },
        scales: {
            xAxes: [{
                type: 'time',
                time: {
                    parser: "YYYY-MM-DD HH:mm:ss",
                    second: 'mm:ss',
                    minute: 'HH:mm',
                    hour: 'HH:mm',
                    day: 'MMM DD',
                    month: 'YYYY MMM',
                    tooltipFormat: 'YYYY-MM-DD HH:mm',
                    displayFormats: {
                        second: 'HH:mm:ss a'
                    }
                },
                display: true,
                scaleLabel: {
                    display: true,
                    labelString: '시간(Hour)'
                }
            }],
            yAxes: [{
                display: true,
                scaleLabel: {
                    display: true,
                    labelString: '공기질(Value)'
                }
            }]
        }
    };

    const hourlyAirQualityChart = new Chart(document.getElementById("hourlyAirQualityChart"), {
        type: 'line',
        data : {
            datasets: datasets,
        },
        options: options
    });
}

DustSensorThings.prototype.closeSensorInformation = function () {
    $('#dustInfoWrap').hide();
};