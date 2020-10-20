const SensorThings = function (magoInstance) {

    this.magoInstance = magoInstance;
    this.FROST_SERVER_URL = 'http://localhost:8888/FROST-Server/v1.0/';
    this.queryString = '';
    this.type = 'iot_occupancy'; // iot_occupancy, iot_dust

    // http://localhost:8888/FROST-Server/v1.0/ObservedProperties(1)/Datastreams?$select=@iot.id,description,unitOfMeasurement&$expand=Observations($select=result,phenomenonTime;$orderby=phenomenonTime%20desc;$top=1;$count=true),Thing($select=@iot.id,name,description;$expand=Locations($select=location,name))
    // http://localhost:8888/FROST-Server/v1.0/Locations?$select=@iot.id,location,name&$top=5&$count=true&$expand=Things($select=@iot.id,name,description),Things/Datastreams($select=@iot.id,description;$filter=ObservedProperty/name eq '미세먼지(PM10) Particulates'),Things/Datastreams/Observations($select=result,phenomenonTime;$orderby=phenomenonTime desc;$top=1)

};


SensorThings.prototype.init = function () {
};

SensorThings.prototype.dataSearch = function (pageNo) {
    $('#iotInfoContent div').hide();
    const $form = $("#searchIotForm");
    const params = getFormData($form);

    if (this.type === 'iot_occupancy') {
        this.occupancyList(pageNo, params);
    } else if (this.type === 'iot_dust') {
        this.dustList(pageNo, params);
    }

};

SensorThings.prototype.occupancyList = function (pageNo, params) {
    const _this = this;
    $.ajax({
        url: _this.FROST_SERVER_URL +
            'Locations?$select=@iot.id,location,name&$top=1&$count=true&$expand=' +
            'Things($select=@iot.id,name,description),' +
            'Things/Datastreams($select=@iot.id,description,unitOfMeasurement;$filter=ObservedProperty/name eq \'미세먼지(PM10) Particulates\'),' +
            'Things/Datastreams/Observations($select=result,phenomenonTime;$orderby=phenomenonTime desc;$top=1)',
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {
            console.info(msg);

            const pagination = new Pagination(pageNo, msg['@iot.count'], 1, msg['@iot.nextLink']);
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

SensorThings.prototype.dustList = function (pageNo, params) {
    const _this = this;
    $.ajax({
        url: _this.FROST_SERVER_URL +
            'Locations?$select=@iot.id,location,name&$top=1&$count=true&$expand=' +
            'Things($select=@iot.id,name,description),' +
            'Things/Datastreams($select=@iot.id,description,unitOfMeasurement;$filter=ObservedProperty/name eq \'미세먼지(PM10) Particulates\'),' +
            'Things/Datastreams/Observations($select=result,phenomenonTime;$orderby=phenomenonTime desc;$top=1)',
        type: "GET",
        dataType: "json",
        headers: {"X-Requested-With": "XMLHttpRequest"},
        success: function (msg) {
            console.info(msg);

            const pagination = new Pagination(pageNo, msg['@iot.count'], 1, msg['@iot.nextLink']);
            msg.pagination = pagination;

            const templateLegend = Handlebars.compile($("#iotLegendSource").html());
            $("#iotLegendDHTML").html("").append(templateLegend(params));

            const templateSearchSummary = Handlebars.compile($("#searchSummarySource").html());
            $("#iotSearchSummaryDHTML").html("").append(templateSearchSummary(msg));

            const template = Handlebars.compile($("#dustListSource").html());
            $("#iotDustListDHTML").html("").append(template(msg));

            const templatePagination = Handlebars.compile($("#paginationSource").html());
            $("#iotPaginationDHTML").html("").append(templatePagination(msg));

            $('#iotDustListDHTML').show();
        },
        error: function (request, status, error) {
            alert(JS_MESSAGE["ajax.error.message"]);
        }
    });
};
