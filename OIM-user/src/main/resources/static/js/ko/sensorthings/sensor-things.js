var SensorThings = function(magoInstance) {

    this.magoInstance = magoInstance;
    this.FROST_SERVER_URL = 'http://localhost:8888/FROST-Server/v1.0/';
    this.queryString = '';

    // http://localhost:8888/FROST-Server/v1.0/ObservedProperties(1)/Datastreams?$select=@iot.id,description,unitOfMeasurement&$expand=Observations($select=result,phenomenonTime;$orderby=phenomenonTime%20desc;$top=1;$count=true),Thing($select=@iot.id,name,description;$expand=Locations($select=location,name))
    // http://localhost:8888/FROST-Server/v1.0/Locations?$select=location,name&$expand=Things($select=@iot.id,name,description),Things/Datastreams($select=@iot.id,description;$filter=ObservedProperty/name eq '미세먼지(PM10) Particulates'),Things/Datastreams/Observations($select=result,phenomenonTime;$orderby=phenomenonTime desc;$top=1)

};

SensorThings.prototype.init = function() {

};
SensorThings.prototype.getFormData = function() {
    var unindexed_array = $form.find(':visible').serializeArray();
    var indexed_array = {};
    $.map(unindexed_array, function (n, i) {
        if (indexed_array[n['name']]) {
            indexed_array[n['name']] += ',' + n['value'];
        } else {
            indexed_array[n['name']] = n['value'];
        }
    });
    return indexed_array;
};


function occupancyList(pageNo, params) {
/*
    var templateLegend = Handlebars.compile($("#iotLegendSource").html());
    $("#iotLegendDHTML").html("").append(templateLegend(params));

    var templateSearchSummary = Handlebars.compile($("#searchSummarySource").html());
    $("#iotSearchSummaryDHTML").html("").append(templateSearchSummary(msg));

    var template = Handlebars.compile($("#occupancyListSource").html());
    $("#iotOccupancyListDHTML").html("").append(template(msg));

    var templatePagination = Handlebars.compile($("#paginationSource").html());
    $("#iotPaginationDHTML").html("").append(templatePagination(msg));
*/
    $('#iotOccupancyListDHTML').show();
}

function dustList(pageNo, params) {
/*
    var templateLegend = Handlebars.compile($("#iotLegendSource").html());
    $("#iotLegendDHTML").html("").append(templateLegend(params));

    var templateSearchSummary = Handlebars.compile($("#searchSummarySource").html());
    $("#iotSearchSummaryDHTML").html("").append(templateSearchSummary(msg));

    var template = Handlebars.compile($("#dustListSource").html());
    $("#iotDustListDHTML").html("").append(template(msg));

    var templatePagination = Handlebars.compile($("#paginationSource").html());
    $("#iotPaginationDHTML").html("").append(templatePagination(msg));
*/
    $('#iotDustListDHTML').show();
}

// 검색 버튼 클릭
$('#iotSearch').click(function() {
    iotDataSearch(1);

    dustSensorThings.updateContentValue();
    setInterval(function(){
        dustSensorThings.updateContentValue();
    }, 1000 * 60 * 60);

});

// 검색 엔터키
$('#iotSearch').keyup(function(e) {
    if (e.keyCode == 13) {
        iotDataSearch(1);
    }
});

function iotDataSearch(pageNo) {

    $('#iotInfoContent div').hide();

    var $form = $("#searchIotForm");
    var params = getFormData($form);

    if (params.searchWord === 'iot_occupancy') {
        //occupancyList(pageNo, params);
    } else if (params.searchWord === 'iot_dust') {
        //dustList(pageNo, params);
    }

}