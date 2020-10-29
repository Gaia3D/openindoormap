const DustSensorThings = function (magoInstance) {
    SensorThings.call(this, magoInstance);
    this.magoInstance = magoInstance;
    this.currentPageNo = 0;
    this.selectedLocationId = 0;
    this.locations = [];
    this.observedProperty = 'pm10Value';
    this.observedPropertyColor = {
        'pm10Value': '#E91E63',
        'pm25Value': '#9C27B0',
        'so2Value': '#FF9800',
        'coValue': '#2196F3',
        'o3Value': '#607d8b',
        'no2Value': '#00BCD4'
    };
    this.gaugeChartNeedle = {};
    this.hourlyAirQualityChart = {};
    this.overlayWrap = $('.overlayWrap');
};
DustSensorThings.prototype = Object.create(SensorThings.prototype);
DustSensorThings.prototype.constructor = DustSensorThings;

/**
 * 지도 Overlay 생성
 */
DustSensorThings.prototype.addOverlay = function () {

    const _this = this;
    const queryString = 'Locations?$select=@iot.id,location,name&$top=1000&$count=true' +
        '&$expand=Things($select=@iot.id,name,description),' +
            'Things/Datastreams($select=@iot.id,description,unitOfMeasurement;$filter=ObservedProperty/name eq \'' + _this.observedProperty + '\'),' +
            'Things/Datastreams/Observations($select=result,phenomenonTime,resultTime;$orderby=resultTime desc;$filter=resultTime le ' + _this.getCurrentTime() + ' and resultTime gt ' + _this.getFilterStartTime() + ')';
            //'Things/Datastreams/Observations($select=result,phenomenonTime,resultTime;$orderby=resultTime desc;$top=1)';

    $.ajax({
        // http://localhost:8888/FROST-Server/v1.0/Locations?$select=@iot.id,location,name&$top=1000&$count=true&$expand=Things($select=@iot.id,name,description),Things/Datastreams($select=@iot.id,description,unitOfMeasurement;$filter=ObservedProperty/name eq 'pm10Value'),Things/Datastreams/Observations($select=result,phenomenonTime,resultTime;$orderby=resultTime desc;$filter=resultTime le 2020-10-23T05:00:00.000Z and resultTime gt 2020-10-23T04:00:00.000Z)
        url: _this.FROST_SERVER_URL + queryString,
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
DustSensorThings.prototype.hideOverlay = function () {
    this.overlayWrap.hide();
};
DustSensorThings.prototype.clearOverlay = function () {
    this.overlayWrap.remove();
};

/**
 * CAI 등급별 상태메세지 가져오기
 * @param grade
 * @returns {*}
 */
DustSensorThings.prototype.getGradeMessage = function (grade) {
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

/**
 * 센서 컨텐츠 위치 갱신
 */
DustSensorThings.prototype.updateContentPosition = function () {

    const contents = {
        locations: []
    };

    for (const location of this.locations) {

        // Locations
        const locationId = location['@iot.id'];
        const addr = location.name;
        const coordinates = location.location.coordinates;
        const resultScreenCoord = this.geographicCoordToScreenCoord(coordinates);

        // 지도화면 픽셀정보 구하기
        const $containerSelector = $('#magoContainer');
        //const $overlaySelector = $('.overlayWrap').find('#overlay_' + locationId);
        const top = 0, left = 0;
        let bottom = top + $containerSelector.outerHeight() - 55;
        let right = left + $containerSelector.outerWidth() - 160;

        // 화면 밖에 있는 관측소는 스킵
        if ((resultScreenCoord.x < left || resultScreenCoord.x > right) ||
            (resultScreenCoord.y < top || resultScreenCoord.y > bottom)) {
            continue;
        }

        // Things
        if (!location['Things'] || location['Things'].length <= 0) continue;
        const thing = location['Things'][0];

        // Datastreams
        if (!thing['Datastreams'] || thing['Datastreams'].length <= 0) continue;
        const dataStream = thing['Datastreams'][0];
        const unit = dataStream['unitOfMeasurement'].symbol;

        // Observations
        if (!dataStream['Observations'] || dataStream['Observations'].length <= 0) continue;
        const observation = dataStream['Observations'][0];
        const value = observation.result.value;
        const grade = observation.result.grade;
        const gradeText = this.getGradeMessage(grade);

        contents.locations.push({
            id: locationId,
            value: value,
            unit: unit,
            stationName: thing.name,
            addr: addr,
            grade: grade,
            gradeText: gradeText,
            top: resultScreenCoord.y,
            left: resultScreenCoord.x
        });

        /*
        if ($overlaySelector.length === 0) {
            // 없으면 만들기
        } else {
            // 있으면 위치만 이동
            $overlaySelector.children().css({
                top: resultScreenCoord.y,
                left: resultScreenCoord.x
            });
        }
        $overlaySelector.show();
         */
    }

    const template = Handlebars.compile($("#overlaySource").html());
    $('#overlayWrap').html("").append(template(contents));

};

/**
 * 센서 컨텐츠 값 갱신
 */
DustSensorThings.prototype.updateContentValue = function () {

    const _this = this;
    for (const location of _this.locations) {

        // Locations
        const locationId = location['@iot.id'];
        const addr = location.name;
        const queryString = 'Locations(' + locationId + ')?' +
            '$select=@iot.id' +
            '&$expand=Things/Datastreams($select=@iot.id,description,name,unitOfMeasurement),' +
                'Things/Datastreams/ObservedProperty($select=name),' +
                'Things/Datastreams/Observations($select=result,resultTime;$orderby=resultTime desc;$filter=resultTime le ' + _this.getCurrentTime() + ' and resultTime gt ' + _this.getFilterStartTime() + ')';
                //'Things/Datastreams/Observations($select=result,resultTime;$orderby=resultTime desc;$top=1)';
                //'Things/Datastreams/Observations($select=result,resultTime;$orderby=resultTime desc)';

        $.ajax({
            // http://localhost:8888/FROST-Server/v1.0/Locations(1)?$select=@iot.id&$expand=Things/Datastreams($select=@iot.id,description,name,unitOfMeasurement),Things/Datastreams/ObservedProperty($select=name),Things/Datastreams/Observations($select=result,phenomenonTime,resultTime;$orderby=resultTime desc;$filter=resultTime le 2020-10-23T05:00:00.000Z and resultTime gt 2020-10-23T04:00:00.000Z)
            url: _this.FROST_SERVER_URL + queryString,
            type: "GET",
            dataType: "json",
            headers: {"X-Requested-With": "XMLHttpRequest"},
            success: function (msg) {

                const contents = {
                    dataStreams: []
                };

                for (const datastream of msg.Things[0].Datastreams) {

                    // Things
                    if (!location['Things'] || location['Things'].length <= 0) continue;
                    const thing = location['Things'][0];

                    // Datastreams
                    const observedPropertyName = datastream.ObservedProperty.name;
                    const unit = datastream.unitOfMeasurement.symbol;

                    for (const observation of datastream.Observations) {

                        //const observation = datastream.Observations[0];
                        //const observation = datastream.Observations[index];

                        let value = observation.result.value;
                        value = parseFloat(parseFloat(value).toFixed(3));
                        const grade = observation.result.grade;
                        const gradeText = _this.getGradeMessage(grade);

                        const content = {
                            id: locationId,
                            value: value,
                            unit: unit,
                            stationName: thing.name,
                            addr: addr,
                            grade: grade,
                            gradeText: gradeText
                        };

                        if (observedPropertyName === _this.observedProperty) {

                            // 지도 측정소 정보 업데이트
                            location.content = content;
                            const template = Handlebars.compile($("#overlaySource").html());
                            const innerHtml = $(template(content)).find('ul').html();
                            $('#overlay_' + locationId + '> ul').html(innerHtml);

                            // 도넛차트 업데이트
                            const doughnutChartData = _this.gaugeChartNeedle.data;
                            if (!doughnutChartData || _this.selectedLocationId == 0 || _this.selectedLocationId != locationId) continue;
                            const pm10Percent = value / 600 * 100;
                            console.info("value: " + value + ", pm10Percent: " + pm10Percent);
                            doughnutChartData.datasets[0].data = [pm10Percent - 0.5, 1, 100 - (pm10Percent + 0.5)];
                            _this.gaugeChartNeedle.update();

                            // 도넛 차트 영역 값, 등급 업데이트
                            $('#dustInfoValue').text(value);
                            $('#dustInfoGrade').removeClass();
                            $('#dustInfoGrade').addClass('dust lv' + grade);
                        }

                        // 라인차트 업데이트
                        const hourlyAirQualityChartData = _this.hourlyAirQualityChart.data;
                        if (!hourlyAirQualityChartData || _this.selectedLocationId == 0 || _this.selectedLocationId != locationId) continue;
                        hourlyAirQualityChartData.datasets.forEach(function(dataset) {
                            if (dataset.observedPropertyName === observedPropertyName) {
                                console.info("observedPropertyName: " + observedPropertyName);
                                console.info("value: " + value + ", currentTime: " + _this.getCurrentTime());
                                dataset.data.pop();
                                dataset.data.unshift({
                                    x: _this.observationTimeToLocalTime(_this.getCurrentTime()),
                                    y: value
                                });
                            }
                        });
                        _this.hourlyAirQualityChart.update();

                        const data = {
                            id: datastream['@iot.id'],
                            name: datastream.name,
                            unit: unit,
                            value: value,
                            grade: grade,
                            gradeText: gradeText,
                            observations: datastream['Observations'],
                            observedPropertyName: observedPropertyName
                        };
                        contents.dataStreams.push(data);

                    }

                }

                // 측정항목 테이블 업데이트
                if (_this.selectedLocationId != 0 && _this.selectedLocationId == locationId) {
                    const $dustInfoTableWrap = $('#dustInfoTableSource');
                    const dustInfoTemplate = Handlebars.compile($("#dustInfoSource").html());
                    const innerHtml = $(dustInfoTemplate(contents)).find("#dustInfoTableSource").html();
                    $dustInfoTableWrap.html(innerHtml);
                }

            },
            error: function (request, status, error) {
                alert(JS_MESSAGE["ajax.error.message"]);
            }
        });
    }

};



/**
 * 관측소 목록 조회
 * @param pageNo
 * @param params
 */
DustSensorThings.prototype.getList = function (pageNo, params) {

    const _this = this;
    pageNo = parseInt(pageNo);
    _this.currentPageNo = pageNo;
    const skip = (pageNo - 1) * 5;

    let filter = '';
    if (params.searchValue) {
        filter = '&$filter=startswith(name, \'' + params.searchValue + '\') or endswith(name, \'' + params.searchValue + '\')';
    }

    const queryString = 'Things?$select=@iot.id,name,description' +
        '&$top=5&$skip=' + skip + '&$count=true&$orderby=name asc' + filter +
        '&$expand=Locations($select=@iot.id,location,name),' +
            'Datastreams($select=@iot.id,description,unitOfMeasurement;$filter=ObservedProperty/name eq \'' + _this.observedProperty + '\'),' +
            'Datastreams/Observations($select=result,phenomenonTime,resultTime;$orderby=resultTime desc;$filter=resultTime le ' + _this.getCurrentTime() + ' and resultTime gt ' + _this.getFilterStartTime() + ')';
            //'Datastreams/Observations($select=result,phenomenonTime,resultTime;$orderby=resultTime desc;$top=1)';

    $.ajax({
        // http://localhost:8888/FROST-Server/v1.0/Things?$select=@iot.id,name,description&$top=5&$count=true&$orderby=name asc&$filter=startswith(name, '이') or endswith(name, '이')&$expand=Locations($select=@iot.id,location,name),Datastreams($select=@iot.id,description,unitOfMeasurement;$filter=ObservedProperty/name eq 'pm10Value'),Datastreams/Observations($select=result,phenomenonTime,resultTime;$orderby=resultTime desc;$top=1)
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
                const grade = observation.result.grade;
                const gradeText = _this.getGradeMessage(grade);

                msg.contents.push({
                    id: locationId,
                    value: value,
                    unit: unit,
                    stationName: thing.name,
                    addr: addr,
                    grade: grade,
                    gradeText: gradeText,
                    longitude: coordinates[0],
                    latitude: coordinates[1],
                    moreTitle: '#{common.more}'
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
 * 관측소 더보기 조회
 * @param obj
 * @param locationId
 */
DustSensorThings.prototype.getSensorMoreInformation = function(obj, locationId) {

    const _this = this;
    let queryString = 'Locations(' + locationId + ')?' +
        '$select=@iot.id&' +
        '$expand=Things/DataStreams($select=@iot.id,description,name,unitOfMeasurement;$orderby=ObservedProperty/@iot.id asc),' +
            'Things/DataStreams/Observations($select=result,resultTime;$orderby=resultTime desc;$filter=resultTime le ' + _this.getCurrentTime() + ' and resultTime gt ' + _this.getFilterStartTime() + ')';
            //'Things/DataStreams/Observations($select=result,resultTime;$orderby=resultTime desc;$top=1)';

    $.ajax({
        // http://localhost:8888/FROST-Server/v1.0/Locations(1)?$select=@iot.id&$expand=Things/DataStreams($select=@iot.id,description,name,unitOfMeasurement;$orderby=ObservedProperty/@iot.id asc),Things/DataStreams/ObservedProperty($select=name),Things/DataStreams/Observations($select=result,resultTime;$orderby=resultTime desc;$filter=resultTime le 2020-10-23T05:00:00.000Z and resultTime gt 2020-10-23T04:00:00.000Z)
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {

            const contents = {
                dataStreams: []
            };

            for (const dataStream of msg.Things[0].Datastreams) {
                contents.dataStreams.push({
                    name: dataStream.name,
                    value: _this.formatValueByDigits(dataStream.Observations[0].result.value, 3),
                    unit: dataStream.unitOfMeasurement.symbol
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

DustSensorThings.prototype.closeSensorMoreInformation = function (obj) {
    const $iotDustMoreDHTML = $(obj).parents(".iotDustMoreDHTML");
    $iotDustMoreDHTML.hide();
    $(".show-more").show();
}

/**
 * 관측소 상세정보 조회
 * @param locationId
 */
DustSensorThings.prototype.getSensorInformation = function (locationId) {

    const _this = this;
    _this.selectedLocationId = locationId;
    let queryString = 'Locations(' + locationId + ')?' +
        '$select=@iot.id&' +
        '$expand=Things/DataStreams($select=@iot.id,description,name,unitOfMeasurement;$orderby=ObservedProperty/@iot.id asc),' +
            'Things/DataStreams/ObservedProperty($select=name),' +
            'Things/DataStreams/Observations($select=result,phenomenonTime,resultTime;$orderby=resultTime desc;$filter=resultTime le ' + _this.getCurrentTime() + ' and resultTime gt ' + _this.getFilterDayStartTime() + ')';
            //'Things/DataStreams/Observations($select=result,phenomenonTime,resultTime;$orderby=resultTime desc;$top=24)';

    $.ajax({
        // http://localhost:8888/FROST-Server/v1.0/Locations(1)?$select=@iot.id&$expand=Things/DataStreams($select=@iot.id,description,name,unitOfMeasurement;$orderby=ObservedProperty/@iot.id asc),Things/DataStreams/ObservedProperty($select=name),Things/DataStreams/Observations($count=true;$select=result,phenomenonTime,resultTime;$orderby=resultTime desc;$filter=resultTime le 2020-10-23T05:00:00.000Z and resultTime gt 2020-10-22T05:00:00.000Z)
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {

            // Things
            const thing = msg['Things'][0];
            const contents = {
                min: 0,
                max: 600,
                stationName: thing.name,
                dataStreams: []
            };

            for (const dataStream of thing.Datastreams) {

                const observation = dataStream['Observations'][0];
                const value = observation.result.value;
                const grade = observation.result.grade;
                const observedPropertyName = dataStream.ObservedProperty.name;

                const data = {
                    id: dataStream['@iot.id'],
                    name: dataStream.name,
                    unit: dataStream.unitOfMeasurement.symbol,
                    value: parseFloat(parseFloat(value).toFixed(3)),
                    grade: grade,
                    gradeText: _this.getGradeMessage(grade),
                    observations: dataStream['Observations'],
                    observedPropertyName: observedPropertyName
                };
                contents.dataStreams.push(data);

                if (observedPropertyName === _this.observedProperty) {
                    contents.grade = grade;
                    contents.pm10 = data.value;
                    contents.pm10Percent = data.value / 600 * 100;
                }

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

    const gaugeChart = new Chart(document.getElementById("gaugeChart"), {
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

    this.gaugeChartNeedle = new Chart(document.getElementById("gaugeChartNeedle"), {
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
                x : this.observationTimeToLocalTime(observation.resultTime),
                y : observation.result.value
            };
            points.push(point);
        }
        const name = dataStream.name;
        const observedPropertyName = dataStream.observedPropertyName;
        const propertyColor = this.observedPropertyColor[observedPropertyName];
        const dataset = {
            label: name,
            data: points,
            borderColor: propertyColor,
            backgroundColor: new Color(propertyColor).alpha(0.2).rgbString(),
            observedPropertyName: observedPropertyName
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

    this.hourlyAirQualityChart = new Chart(document.getElementById("hourlyAirQualityChart"), {
        type: 'line',
        data : {
            datasets: datasets,
        },
        options: options
    });
}

DustSensorThings.prototype.closeSensorInformation = function () {
    $('#dustInfoWrap').hide();
    this.selectedLocationId = 0;
};