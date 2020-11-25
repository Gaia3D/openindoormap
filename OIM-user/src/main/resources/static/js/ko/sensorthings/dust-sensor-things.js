const DustSensorThings = function (magoInstance) {
    SensorThings.call(this, magoInstance);

    this.magoInstance = magoInstance;
    this.type = 'iot_dust';
    this.observedProperty = 'pm10Value';
    this.observedPropertyColor = {
        'pm10Value': '#E91E63',
        'pm25Value': '#9C27B0',
        'so2Value': '#FF9800',
        'coValue': '#2196F3',
        'o3Value': '#607d8b',
        'no2Value': '#00BCD4'
    };
    this.pm10GradeMin = 0;
    this.pm10GradeMax = 600;

    //this.currentTime = "2020-11-23T12:15:00.000Z";
    this.currentTime = moment.utc().format();
    this.processingTime = 1800;     // 30m
    this.callInterval = 10;         // 10s
    this.filterInterval = 3600;     // 1hour

    this.hourlyAirQualityChart = {};
    this.chartTitle = '1시간 공기질(Hourly Air Quality)';
    this.chartXAxesTitle = '시간(시)';
    this.chartYAxesTitle = '공기질(Value)';

    this.chartOptions = {
        animation: false,
        responsive: true,
        maintainAspectRatio: false,
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
        plugins: {
            zoom: {
                pan: {
                    enabled: true,
                    mode: 'x',
                    rangeMin: {
                        x: null,
                        y: null
                    },
                    rangeMax: {
                        x: null,
                        y: null
                    }
                },
                zoom: {
                    enabled: true,
                    drag: false,
                    mode: 'x',
                    rangeMin: {
                        x: null,
                        y: null
                    },
                    rangeMax: {
                        x: null,
                        y: null
                    },
                    speed: 0.03
                }
            }
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
                    tooltipFormat: 'YYYY-MM-DD HH:mm:ss',
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

DustSensorThings.prototype.init = function () {

    if ($('#dustInfoDHTML').is(':visible')) {
        $('#dustInfoDHTML').hide();
    }
    this.things = [];
    this.selectedThingId = 0;
    this.selectedDataStreams = [];
};

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
                '$filter=resultTime lt ' + _this.getFilterEndTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
            ')';

    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {

            const pagination = new Pagination(pageNo, msg['@iot.count'], 5, msg['@iot.nextLink']);
            msg.pagination = pagination;

            const templateLegend = Handlebars.compile($("#iotDustLegendSource").html());
            $("#iotLegendDHTML").html("").append(templateLegend(_this));

            msg.contents = [];
            const things = msg.value;
            for (const thing of things) {

                // Locations
                const locations = thing['Locations'];
                if (!locations || locations.length <= 0) continue;
                const location = locations[0];
                const addr = location.name;
                const coordinates = location.location.geometry.coordinates;

                // Datastreams
                const dataStreams = thing['Datastreams'];
                if (!dataStreams || dataStreams.length <= 0) continue;
                const dataStream = dataStreams[0];

                // Observations
                const observations = dataStream['Observations'];
                let value = '-', grade = 0;
                if (observations && observations.length > 0) {
                    const observationTop = observations[0];
                    value = _this.formatValueByDigits(observationTop.result.value, 3);
                    grade = observationTop.result.grade;
                }

                msg.contents.push({
                    id: thing['@iot.id'],
                    value: value,
                    unit: _this.getUnit(dataStream),
                    stationName: thing.name,
                    addr: addr,
                    grade: grade,
                    gradeText: _this.getGradeMessage(grade),
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
                '$filter=resultTime lt ' + _this.getFilterEndTime() + ' and resultTime ge ' + _this.getFilterDayStartTime() +
            ')';

    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {

            const contents = {dataStreams: []};

            // Datastreams
            const dataStreams = msg.value;
            if (!dataStreams || dataStreams.length <= 0) return;
            for (const dataStream of dataStreams) {

                // Observations
                const observations = dataStream['Observations'];
                let value = '-';
                if (observations && observations.length > 0) {
                    const observationTop = observations[0];
                    value = _this.formatValueByDigits(observationTop.result.value, 3);
                }
                contents.dataStreams.push({
                    name: dataStream.name,
                    value: value,
                    unit: _this.getUnit(dataStream)
                });

            }

            // 더보기 템플릿 생성
            const $iotDustMoreDHTML = $(obj).parent().siblings(".iotDustMoreDHTML");
            const template = Handlebars.compile($("#dustMoreSource").html());
            $iotDustMoreDHTML.html("").append(template(contents));
            $iotDustMoreDHTML.show();

            // 더보기 보이기/숨기기 (순서를 바꾸지 마세요!)
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
                    '$filter=resultTime lt ' + _this.getFilterEndTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
                ')';

    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {
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

        const thingId = parseInt(thing['@iot.id']);

        // Locations
        const locations = thing['Locations'];
        if (!locations || locations.length <= 0) continue;
        const location = locations[0];
        //const addr = location.name;
        const coordinates = location.location.geometry.coordinates;
        coordinates[2] = 0;
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
        const dataStreams = thing['Datastreams'];
        if (!dataStreams || dataStreams.length <= 0) continue;
        const dataStream = dataStreams[0];

        // Observations
        const observations = dataStream['Observations'];
        let value = '-', grade = 0, selected = '';
        if (observations && observations.length > 0) {
            const observationTop = observations[0];
            value = this.formatValueByDigits(observationTop.result.value, 3);
            grade = observationTop.result.grade;
        }
        const gradeText = this.getGradeMessage(grade);
        if (this.selectedThingId == thingId) {
            selected = 'on';
        }

        contents.things.push({
            id: thing['@iot.id'],
            value: value,
            valueWithCommas: this.numberWithCommas(value),
            unit: this.getUnit(dataStream),
            stationName: thing.name,
            //addr: addr,
            grade: grade,
            gradeText: gradeText,
            top: resultScreenCoord.y,
            left: resultScreenCoord.x,
            selected: selected,
            subTitle: JS_MESSAGE["iot.dust.fine"]
        });

    }   // end for

    if (contents.things.length > 30) {
        alert('검색되는 센서가 너무 많습니다. 지도를 확대 하세요.');
        return;
    }

    const template = Handlebars.compile($("#overlaySource").html());
    $('#overlayDHTML').html("").append(template(contents));

};




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
                '$filter=resultTime lt ' + _this.getFilterEndTime() + ' and resultTime ge ' + _this.getFilterDayStartTime() +
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
                observedProperty: 'dust',
                min: _this.pm10GradeMin,
                max: _this.pm10GradeMax,
                stationName: dataStreams[0]['Thing'].name,
                dataStreams: []
            };

            for (const dataStream of dataStreams) {

                const observedPropertyName = _this.getObservedPropertyName(dataStream);
                const observations = dataStream['Observations'];
                let value = '-', grade = 0;
                if (observations && observations.length > 0) {
                    const observationTop = observations[0];
                    value = _this.formatValueByDigits(observationTop.result.value, 3);
                    grade = observationTop.result.grade;
                }

                const data = {
                    id: dataStream['@iot.id'],
                    name: dataStream.name,
                    unit: _this.getUnit(dataStream),
                    value: _this.formatValueByDigits(value, 3),
                    grade: grade,
                    gradeText: _this.getGradeMessage(grade),
                    observations: observations,
                    observedPropertyName: observedPropertyName
                };
                _this.selectedDataStreams.push(data.id);
                contents.dataStreams.push(data);

                if (observedPropertyName === _this.observedProperty) {
                    contents.grade = grade;
                    contents.pm10 = data.value;
                    contents.pm10Percent = Math.max(Math.min(data.value, _this.pm10GradeMax), _this.pm10GradeMin) / (_this.pm10GradeMax - _this.pm10GradeMin) * 100;
                    contents.chartTitle = JS_MESSAGE["iot.dust"] + 'PM10';
                }

            }

            const $dustInfoDHTML = $('#dustInfoDHTML');
            const template = Handlebars.compile($("#dustInfoSource").html());
            const html = template(contents);
            if ($dustInfoDHTML.length === 0) {
                const wrapper = '<div id="dustInfoDHTML" class="sensor-detail-wrap">' + html + '</div>';
                $('.cesium-viewer').append(wrapper);
            }

            $dustInfoDHTML.html(html);
            $dustInfoDHTML.show();

            const total = _this.pm10GradeMax - _this.pm10GradeMin;
            const range = [0, 30, 80, 150, 600];

            _this.drawGaugeChart(range, total, contents.pm10Percent);
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
    //const randomValue = Math.floor(Math.random() * 100);
    const randomValue = 0;

    this.updateOverlay(randomValue);

    const _this = this;
    //let filter = 'ObservedProperty/name eq \'' + _this.observedProperty + '\'';
    let filter = '';
    const dataStreamIds = _this.selectedDataStreams;
    const length = dataStreamIds.length;
    if (!dataStreamIds || length <= 0 || _this.selectedThingId == 0) return;
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

    const queryString = 'Datastreams?$select=@iot.id,description,name,unitOfMeasurement' +
        '&$filter=' + filter +
        '&$orderby=ObservedProperty/@iot.id asc' +
        '&$expand=Thing($select=name),' +
            'ObservedProperty($select=name),' +
            'Observations(' +
                '$select=result,resultTime;' +
                '$orderby=resultTime desc;' +
                '$filter=resultTime lt ' + _this.getFilterEndTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
            ')';
    console.debug("from: " + _this.observationTimeToLocalTime(_this.getFilterStartTime()) + ", to: " + _this.observationTimeToLocalTime(_this.getFilterEndTime()));

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
                const observedPropertyName = _this.getObservedPropertyName(dataStream);

                // Observations
                let value = "-", grade = 0;
                if (dataStream['Observations'] && dataStream['Observations'].length > 0) {

                    // ObservationTop
                    const observationTop = dataStream['Observations'][0];
                    //value = parseFloat(observation.result.value);
                    value = _this.formatValueByDigits(observationTop.result.value, 3);
                    value += randomValue;
                    value = _this.formatValueByDigits(value, 3);
                    grade = observationTop.result.grade;

                    if (observedPropertyName === _this.observedProperty && _this.gaugeChartNeedle.data) {
                        // 게이지 차트 업데이트
                        _this.updateGaugeChart(_this.pm10GradeMin, _this.pm10GradeMax, value, grade);
                    }

                    // 라인 차트 업데이트
                    _this.updateHourlyAirQualityChart(dataStream, randomValue);

                }
                const gradeText = _this.getGradeMessage(grade);
                dataStreamContents.dataStreams.push({
                    name: dataStream.name,
                    unit: _this.getUnit(dataStream),
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
                '$filter=resultTime lt ' + _this.getFilterEndTime() + ' and resultTime ge ' + _this.getFilterStartTime() +
            ')';

    $.ajax({
        url: _this.FROST_SERVER_URL + queryString,
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {

            // Datastreams
            for (const dataStream of msg.value) {

                // Thing
                const thing = dataStream['Thing'];
                const thingId = thing['@iot.id'];

                // Observations
                let value = "-", grade = 0, selected = '';
                if (dataStream['Observations'] && dataStream['Observations'].length > 0) {
                    // ObservationTop
                    const observationTop = dataStream['Observations'][0];
                    //let value = parseFloat(observation.result.value);
                    value = _this.formatValueByDigits(observationTop.result.value, 3);
                    value += randomValue;
                    value = _this.formatValueByDigits(value, 3);
                    grade = observationTop.result.grade;

                    for (const thing of _this.things) {
                        const originalId = thing['@iot.id'];
                        if (originalId == thingId) {
                            thing['Datastreams'][0]['Observations'][0]['result'].grade = grade;
                            thing['Datastreams'][0]['Observations'][0]['result'].value = value;
                            thing['Datastreams'][0]['Observations'][0]['resultTime'] = observationTop['resultTime'];
                        }
                    }
                }
                if (_this.selectedThingId == thingId) {
                    selected = 'on';
                }
                const contents = {
                    things: [{
                        id: thingId,
                        value: value,
                        valueWithCommas: _this.numberWithCommas(value),
                        unit: _this.getUnit(dataStream),
                        stationName: thing.name,
                        grade: grade,
                        gradeText: _this.getGradeMessage(grade),
                        selected: selected,
                        subTitle: JS_MESSAGE["iot.dust.fine"]
                    }]
                };

                // 지도 측정소 정보 업데이트
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

DustSensorThings.prototype.updateHourlyAirQualityChart = function (dataStream, randomValue) {
    const _this = this;
    let value = undefined;
    const time = _this.observationTimeToLocalTime(_this.getCurrentTime());

    const hourlyAirQualityChartData = _this.hourlyAirQualityChart.data;
    if (!hourlyAirQualityChartData) return;

    if (dataStream['Observations'].length > 0) {
        for (const observation of dataStream['Observations']) {
            const observedPropertyName = _this.getObservedPropertyName(dataStream);
            let value = _this.formatValueByDigits(observation.result.value, 3);
            value += randomValue;
            hourlyAirQualityChartData.datasets.forEach(function (dataset) {
                if (dataset.observedPropertyName === observedPropertyName) {
                    console.debug("observedPropertyName: " + observedPropertyName + "value: " + value + ", time: " + time);
                    if (hourlyAirQualityChartData.datasets.length > 100) {
                        dataset.data.pop();
                    }
                    dataset.data.unshift({x: time, y: value});
                }
            });
        }
    } else {
        hourlyAirQualityChartData.datasets.forEach(function (dataset) {
            if (hourlyAirQualityChartData.datasets.length > 100) {
                dataset.data.pop();
            }
            dataset.data.unshift({x: time, y: value});
        });
    }
    _this.hourlyAirQualityChart.update();

};