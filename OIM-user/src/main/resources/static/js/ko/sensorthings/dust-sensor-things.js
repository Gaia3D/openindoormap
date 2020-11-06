const DustSensorThings = function (magoInstance) {
    SensorThings.call(this, magoInstance);

    this.magoInstance = magoInstance;
    this.observedProperty = 'pm10Value';
    this.observedPropertyColor = {
        'pm10Value': '#E91E63',
        'pm25Value': '#9C27B0',
        'so2Value': '#FF9800',
        'coValue': '#2196F3',
        'o3Value': '#607d8b',
        'no2Value': '#00BCD4'
    };

    this.currentTime = "2020-10-26T04:50:00.000Z";
    //this.currentTime = moment.utc().format();
    this.callInterval = 10;         // 10s
    this.filterInterval = 3600;     // 1hour

    this.gaugeChartNeedle = {};
    this.hourlyAirQualityChart = {};
    this.chartTitle = '1시간 공기질(Hourly Air Quality)';
    this.chartXAxesTitle = '시간(Hour)';
    this.chartYAxesTitle = '공기질(Value)';
    this.chartOptions = {
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
            text: this.chartTitle
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
                    labelString: this.chartXAxesTitle
                }
            }],
            yAxes: [{
                display: true,
                scaleLabel: {
                    display: true,
                    labelString: this.chartYAxesTitle
                }
            }]
        }
    };

};
DustSensorThings.prototype = Object.create(SensorThings.prototype);
DustSensorThings.prototype.constructor = DustSensorThings;

/**
 * 관측소 목록 조회
 * @param pageNo
 * @param params
 */
DustSensorThings.prototype.getList = function (pageNo, params) {

    const _this = this;
    pageNo = parseInt(pageNo);
    this.currentPageNo = pageNo;
    const skip = (pageNo - 1) * 5;

    let filter = 'Datastreams/ObservedProperty/name eq \'' + _this.observedProperty + '\'';
    if (params.searchValue) {
        filter += 'and (startswith(name, \'' + params.searchValue + '\') or endswith(name, \'' + params.searchValue + '\'))';
    }

    const queryString = 'Things?$select=@iot.id,name,description' +
        '&$top=5&$skip=' + skip + '&$count=true&$orderby=name asc&$filter=' + filter +
        '&$expand=Locations($select=@iot.id,location,name),' +
            'Datastreams(' +
                '$select=@iot.id,description,unitOfMeasurement;' +
                '$filter=ObservedProperty/name eq \'' + _this.observedProperty + '\'' +
            '),' +
            'Datastreams/Observations(' +
                '$select=result,phenomenonTime,resultTime;' +
                '$orderby=resultTime desc;' +
                '$filter=resultTime lt ' + _this.getCurrentTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
            ')';

    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {

            console.info(msg);
            const pagination = new Pagination(pageNo, msg['@iot.count'], 5, msg['@iot.nextLink']);
            msg.pagination = pagination;

            const templateLegend = Handlebars.compile($("#iotLegendSource").html());
            $("#iotLegendDHTML").html("").append(templateLegend(_this));

            msg.contents = [];
            const things = msg.value;
            for (const thing of things) {

                // Locations
                const location = thing.Locations[0];
                const addr = location.name;
                const coordinates = location.location.coordinates;

                // Datastreams
                const dataStream = thing.Datastreams[0];
                const unit = dataStream.unitOfMeasurement.symbol;

                // Observations
                if (!dataStream['Observations'] || dataStream['Observations'].length <= 0) continue;
                const observation = dataStream.Observations[0];
                const value = observation.result.value;
                const grade = observation.result.grade;
                const gradeText = _this.getGradeMessage(grade);

                msg.contents.push({
                    id: thing['@iot.id'],
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
 * @param thingId
 */
DustSensorThings.prototype.getDetail = function(obj, thingId) {

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
            console.info(msg);
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
 * 관측소 더보기 닫기
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
DustSensorThings.prototype.addOverlay = function () {

    const _this = this;
    const filter = 'Datastreams/ObservedProperty/name eq \'' + _this.observedProperty + '\'';

    // TODO 화면 영역에 해당하는 Location을 필터링하여 호출하도록 수정
    const queryString = 'Things?$select=@iot.id,name,description&$top=1000' +
            '&$filter=' + filter +
            '&$expand=Locations($select=@iot.id,location,name),' +
                'Datastreams(' +
                    '$select=@iot.id,description,unitOfMeasurement;' +
                    '$filter=ObservedProperty/name eq \'' + _this.observedProperty + '\'' +
                '),' +
                'Datastreams/Observations(' +
                    '$select=result,phenomenonTime,resultTime;' +
                    '$orderby=resultTime desc;' +
                    '$filter=resultTime lt ' + _this.getCurrentTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
                ')';

    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {
            console.info(msg);
            _this.things = msg.value;
            _this.redrawOverlay();
        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });

};

/**
 * 지도 오버레이 다시 그리기
 */
DustSensorThings.prototype.redrawOverlay = function () {

    const contents = {
        things: []
    };

    for (const thing of this.things) {

        const thingId = thing['@iot.id'];

        // Locations
        if (!thing['Locations'] || thing['Locations'].length <= 0) continue;
        const location = thing['Locations'][0];
        //const addr = location.name;
        const coordinates = location.location.coordinates;
        const resultScreenCoord = this.geographicCoordToScreenCoord(coordinates);

        // 지도화면 픽셀정보 구하기
        const $containerSelector = $('#magoContainer');
        //const $overlaySelector = $('.overlayDHTML').find('#overlay_' + locationId);
        const top = 0, left = 0;
        let bottom = top + $containerSelector.outerHeight() - 55;
        let right = left + $containerSelector.outerWidth() - 160;

        // 화면 밖에 있는 관측소는 스킵
        if ((resultScreenCoord.x < left || resultScreenCoord.x > right) ||
            (resultScreenCoord.y < top || resultScreenCoord.y > bottom)) {
            continue;
        }

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
        let selected = '';
        if (this.selectedThingId == thingId) {
            selected = 'on';
        }

        contents.things.push({
            id: thing['@iot.id'],
            value: value,
            unit: unit,
            stationName: thing.name,
            //addr: addr,
            grade: grade,
            gradeText: gradeText,
            top: resultScreenCoord.y,
            left: resultScreenCoord.x,
            selected : selected
        });

    }   // end for

    if (contents.things.length > 30) {
        alert('검색되는 센서가 너무 많습니다. 지도를 확대하하세요.');
        return;
    }

    const template = Handlebars.compile($("#overlaySource").html());
    $('#overlayDHTML').html("").append(template(contents));

};

/**
 * 화면에 보이는 지도 오버레이 thingId 가져오기
 */
DustSensorThings.prototype.getOverlay = function() {
    const result = [];
    for (const thing of this.things) {
        const thingId = thing['@iot.id'];
        if ($('#overlay_' + thingId).length > 0) {
            result.push(thingId);
        }
    }
    return result;
}


/**
 * 관측소 상세정보 조회
 * @param thingId
 */
DustSensorThings.prototype.getInformation = function (thingId) {

    const _this = this;
    _this.selectedThingId = thingId;
    _this.selectedDataStreams = [];
    const queryString = 'Datastreams?$select=@iot.id,description,name,unitOfMeasurement' +
        '&$filter=Things/@iot.id eq ' + thingId +
        '&$orderby=ObservedProperty/@iot.id asc' +
        '&$expand=Thing($select=name),' +
            'ObservedProperty($select=name),' +
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
            console.info(msg);
            const dataStreams = msg.value;

            // Thing
            const thing = dataStreams[0]['Thing'];
            const contents = {
                min: 0,
                max: 600,
                stationName: thing.name,
                dataStreams: []
            };

            // Datastream
            for (const dataStream of dataStreams) {

                if (!dataStream['Observations'] || dataStream['Observations'].length <= 0) continue;
                const observation = dataStream['Observations'][0];
                const value = observation.result.value;
                const grade = observation.result.grade;
                const observedPropertyName = dataStream['ObservedProperty']['name'];

                const data = {
                    id: dataStream['@iot.id'],
                    name: dataStream.name,
                    unit: dataStream['unitOfMeasurement']['symbol'],
                    value: _this.formatValueByDigits(value, 3),
                    grade: grade,
                    gradeText: _this.getGradeMessage(grade),
                    observations: dataStream['Observations'],
                    observedPropertyName: observedPropertyName
                };
                _this.selectedDataStreams.push(data.id);
                contents.dataStreams.push(data);

                if (observedPropertyName === _this.observedProperty) {
                    contents.grade = grade;
                    contents.pm10 = data.value;
                    contents.pm10Percent = data.value / 600 * 100;
                }

            }

            const $dustInfoDHTML = $('#dustInfoDHTML');
            const template = Handlebars.compile($("#dustInfoSource").html());
            const html = template(contents);
            if ($dustInfoDHTML.length === 0) {
                const wrapper ='<div id="dustInfoDHTML" class="sensor-detail-wrap">' + html + '</div>';
                $('.cesium-viewer').append(wrapper);
            }

            $dustInfoDHTML.html(html);
            $dustInfoDHTML.show();

            _this.drawGaugeChart(contents.pm10Percent);
            _this.drawHourlyAirQualityChart(contents.dataStreams);

        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });
};

/**
 * 관측소 상세정보 닫기
 */
DustSensorThings.prototype.closeInformation = function () {
    $('#dustInfoDHTML').hide();
    this.selectedThingId = 0;
    this.selectedDataStreams = [];
};

/**
 * 게이지 차트 그리기
 * @param pm10Percent
 */
DustSensorThings.prototype.drawGaugeChart = function (pm10Percent) {

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
            labels: ["좋음", "보통", "나쁨", "아주나쁨"],
            datasets: [
                {
                    label: '통합대기환경지수(CAI)',
                    data: [30 / 6, 80 / 6, 150 / 6, 100 - (30 / 6 + 80 / 6 + 150 / 6)],
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
        options: gaugeChartOptions
    });

}

/**
 * 시간별 공기질 차트 그리기
 * @param dataStreams
 */
DustSensorThings.prototype.drawHourlyAirQualityChart = function (dataStreams) {

    const datasets = [];
    for (const dataStream of dataStreams) {
        const points = [];
        for (const observation of dataStream.observations) {
            points.push({
                x: this.observationTimeToLocalTime(observation.resultTime),
                y: this.formatValueByDigits(observation.result.value, 3)
            });
        }
        const observedPropertyName = dataStream.observedPropertyName;
        const propertyColor = this.observedPropertyColor[observedPropertyName];
        datasets.push({
            label: dataStream.name,
            data: points,
            borderColor: propertyColor,
            backgroundColor: new Color(propertyColor).alpha(0.2).rgbString(),
            observedPropertyName: observedPropertyName
        });
    }

    this.hourlyAirQualityChart = new Chart(document.getElementById("hourlyAirQualityChart"), {
        type: 'line',
        data: {datasets: datasets},
        options: this.chartOptions
    });

};

/**
 * 센서 값 갱신
 */
DustSensorThings.prototype.update = function () {

    // TODO 램덤 값 삭제
    const randomValue = Math.floor(Math.random() * 100);

    this.updateOverlay(randomValue);

    const _this = this;
    //let filter = 'ObservedProperty/name eq \'' + _this.observedProperty + '\'';
    let filter = '';
    const dataStreamIds = _this.selectedDataStreams;
    const length = dataStreamIds.length;
    if (!dataStreamIds || length <= 0 || _this.selectedThingId == 0) return;

    if (length > 0) {
        //filter += 'and (';
        for (const i in dataStreamIds) {
            const dataStreamId = dataStreamIds[i];
            if (i == 0) {
                filter += 'id eq ' + dataStreamId;
            } else {
                filter += ' or id eq ' + dataStreamId;
            }
        }
        //filter += ')';
    }

    const queryString = 'Datastreams?$select=@iot.id,description,name,unitOfMeasurement' +
        '&$filter=' + filter +
        '&$orderby=ObservedProperty/@iot.id asc' +
        '&$expand=Thing($select=name),' +
            'ObservedProperty($select=name),' +
            'Observations(' +
                '$select=result,resultTime;' +
                '$orderby=resultTime desc;' +
                '$filter=resultTime lt ' + _this.getCurrentTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
            ')';

    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {
            const dataStreamContents = {
                dataStreams: []
            };
            for (const dataStream of msg.value) {
                // Datastreams
                const observedPropertyName = dataStream['ObservedProperty']['name'];
                const unit = dataStream['unitOfMeasurement']['symbol'];
                // Observations
                let value = 0, grade = 0;

                if (dataStream['Observations'] && dataStream['Observations'].length > 0) {
                    if (observedPropertyName === _this.observedProperty && _this.gaugeChartNeedle.data) {
                        // 게이지 차트 업데이트
                        _this.updateGaugeChart(dataStream, randomValue);
                    }
                    // 라인 차트 업데이트
                    _this.updateHourlyAirQualityChart(dataStream, randomValue);

                    const observationTop = dataStream['Observations'][0];
                    //let value = parseFloat(observation.result.value);
                    value = _this.formatValueByDigits(observationTop.result.value, 3);
                    value += randomValue;
                    value = _this.formatValueByDigits(value, 3);
                    grade = observationTop.result.grade;

                }

                const gradeText = _this.getGradeMessage(grade);
                dataStreamContents.dataStreams.push({
                    name: dataStream.name,
                    unit: unit,
                    value: value,
                    grade: grade,
                    gradeText: gradeText,
                    observedPropertyName: observedPropertyName
                });

            }
            // 테이블 업데이트
            _this.updateInformationTable(dataStreamContents);
        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });

};

DustSensorThings.prototype.updateOverlay = function (randomValue) {

    const _this = this;

    const overlayIds = _this.getOverlay();
    const length = overlayIds.length;
    if (!overlayIds || length <= 0) return;

    let filter = 'ObservedProperty/name eq \'' + _this.observedProperty + '\'';
    filter += 'and (';
    for (const i in overlayIds) {
        const thingId = overlayIds[i];
        if (i == 0) {
            filter += 'Things/id eq ' + thingId;
        } else {
            filter += ' or Things/id eq ' + thingId;
        }
    }
    filter += ')';

    const queryString = 'Datastreams?$select=@iot.id,description,name,unitOfMeasurement' +
        '&$filter=' + filter +
        '&$orderby=ObservedProperty/@iot.id asc' +
        '&$expand=Thing($select=@iot.id,name),' +
            'ObservedProperty($select=name),' +
            'Observations(' +
                '$select=result,resultTime;' +
                '$orderby=resultTime desc;' +
                '$filter=resultTime lt ' + _this.getCurrentTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
            ')';

    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {

            for (const dataStream of msg.value) {

                // Datastreams
                //const observedPropertyName = dataStream['ObservedProperty']['name'];
                const unit = dataStream['unitOfMeasurement']['symbol'];

                // Thing
                const thing = dataStream['Thing'];
                const thingId = thing['@iot.id'];

                // Observations
                let value = 0, grade = 0;
                if (dataStream['Observations'] && dataStream['Observations'].length > 0) {
                    // ObservationTop
                    const observationTop = dataStream['Observations'][0];
                    for (const thing of _this.things) {
                        const originalId = thing['@iot.id'];
                        if (originalId == thingId) {
                            thing['Datastreams'][0]['Observations'][0] = observationTop;
                        }
                    }

                    //let value = parseFloat(observation.result.value);
                    value = _this.formatValueByDigits(observationTop.result.value, 3);
                    value += randomValue;
                    value = _this.formatValueByDigits(value, 3);
                    grade = observationTop.result.grade;
                }
                const gradeText = _this.getGradeMessage(grade);

                // 지도 측정소 정보 업데이트
                let selected = '';
                if (_this.selectedThingId == thingId) {
                    selected = 'on';
                }
                const contents = {
                    things: [{
                        id: thingId,
                        value: value,
                        unit: unit,
                        stationName: thing.name,
                        grade: grade,
                        gradeText: gradeText,
                        selected: selected
                    }]
                };

                const template = Handlebars.compile($("#overlaySource").html());
                const innerHtml = $(template(contents)).find('ul').html();
                $('#overlay_' + thingId + '> ul').html(innerHtml);

                console.debug("updated thingId: " + thingId);
            }
        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });

};

DustSensorThings.prototype.updateGaugeChart = function (dataStream, randomValue) {

    const _this = this;
    const observationTop = dataStream['Observations'][0];
    //let value = parseFloat(observation.result.value);
    let value = _this.formatValueByDigits(observationTop.result.value, 3);
    value += randomValue;
    value = _this.formatValueByDigits(value, 3);
    const grade = observationTop.result.grade;

    const pm10Percent = value / 600 * 100;
    _this.gaugeChartNeedle.data.datasets[0].data = [pm10Percent - 0.5, 1, 100 - (pm10Percent + 0.5)];
    _this.gaugeChartNeedle.update();

    console.debug("value: " + value + ", pm10Percent: " + pm10Percent);

    // 게이지 차트 영역 값, 등급 업데이트
    $('#dustInfoValue').text(value);
    $('#dustInfoGrade').removeClass();
    $('#dustInfoGrade').addClass('dust lv' + grade);

};

DustSensorThings.prototype.updateHourlyAirQualityChart = function (dataStream, randomValue) {
    const _this = this;
    for (const observation of dataStream['Observations']) {
        const observedPropertyName = dataStream['ObservedProperty']['name'];
        let value = _this.formatValueByDigits(observation.result.value, 3);
        value += randomValue;
        value = _this.formatValueByDigits(value, 3);
        const hourlyAirQualityChartData = _this.hourlyAirQualityChart.data;
        if (!hourlyAirQualityChartData) continue;
        hourlyAirQualityChartData.datasets.forEach(function (dataset) {
            if (dataset.observedPropertyName === observedPropertyName) {
                console.debug("observedPropertyName: " + observedPropertyName);
                console.debug("value: " + value + ", currentTime: " + _this.getCurrentTime());
                dataset.data.pop();
                dataset.data.unshift({
                    x: _this.observationTimeToLocalTime(_this.getCurrentTime()),
                    y: value
                });
            }
        });
        _this.hourlyAirQualityChart.update();
    }
};

DustSensorThings.prototype.updateInformationTable = function (dataStreamContents) {
    const $dustInfoTableWrap = $('#dustInfoTableSource');
    const dustInfoTemplate = Handlebars.compile($("#dustInfoSource").html());
    const innerHtml = $(dustInfoTemplate(dataStreamContents)).find("#dustInfoTableSource").html();
    $dustInfoTableWrap.html(innerHtml);
};