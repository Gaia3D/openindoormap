$(document).ready(function() {

    iotDataSearch(1);

    // 검색 버튼 클릭
    $('#iotSearch').click(function() {
        iotDataSearch(1);
    });

    // 검색 엔터키
    $('#iotSearch').keyup(function(e) {
        if (e.keyCode == 13) {
            iotDataSearch(1);
        }
    });

});

function getFormData($form) {
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
}

function occupancyList(pageNo, params) {

    var msg = {
        "owner": "",
        "pagination": {
            "totalCount": 1,
            "rowNumber": 1,
            "pageNo": 1,
            "firstPage": 1,
            "lastPage": 1,
            "startPage": 1,
            "endPage": 1,
            "prePageNo": 0,
            "nextPageNo": 0,
            "existPrePage": false,
            "existNextPage": false,
            "pageRows": 5,
            "pageListCount": 5,
            "offset": 0,
            "uri": "/datas",
            "searchParameters": "&searchWord=data_name&searchOption=1&searchValue=&startDate=&endDate=&orderWord=&orderValue=&listCounter=10&dataType=",
            "pageIndex": 0
        },
        "dataList": [{
            "totalCount": null,
            "offset": null,
            "limit": null,
            "searchWord": null,
            "searchOption": null,
            "searchValue": null,
            "startDate": null,
            "endDate": null,
            "orderWord": null,
            "orderValue": null,
            "listCounter": 10,
            "messageCode": null,
            "errorCode": null,
            "duplicationValue": null,
            "dataCount": null,
            "referrer": null,
            "userGroupId": null,
            "latitude": 37.5134120429424,
            "longitude": 127.10374173176852,
            "userId": "admin",
            "updateUserId": null,
            "userName": null,
            "methodType": null,
            "dataId": 200000,
            "dataGroupId": 10001,
            "converterJobId": 4,
            "dataGroupName": "기본",
            "dataGroupTarget": "user",
            "dataGroupKey": "basic",
            "tiling": false,
            "dataKey": "admin_20201012230116_27204829199200",
            "oldDataKey": null,
            "dataName": "Lotte0603",
            "dataType": "citygml",
            "dataTypes": null,
            "sharing": "public",
            "parent": null,
            "parentName": null,
            "mappingType": "origin",
            "location": "0101000020E6100000474B5CB4A3C65F40B3E75E7CB7C14240",
            "altitude": 0E-7,
            "heading": 22.00000,
            "pitch": 0.00000,
            "roll": 0.00000,
            "childrenAncestor": null,
            "childrenParent": null,
            "childrenDepth": null,
            "childrenViewOrder": null,
            "metainfo": "{\"isPhysical\": true, \"heightReference\": \"relativeToGround\"}",
            "status": "use",
            "attributeExist": false,
            "objectAttributeExist": false,
            "description": null,
            "viewUpdateDate": "2020-10-12 23:12:06",
            "viewInsertDate": "2020-10-12 23:07:42",
            "updateDate": [2020, 10, 12, 23, 12, 6, 962551000],
            "insertDate": [2020, 10, 12, 23, 7, 42, 260364000],
            "viewMetainfo": "{\"isPhysical\": true,...",
            "parameters": "&searchWord=&searchOption=&searchValue=&startDate=&endDate=&orderWord=&orderValue=&listCounter=10"
        }],
        "errorCode": null,
        "message": null,
        "statusCode": 200
    };

    var templateLegend = Handlebars.compile($("#iotLegendSource").html());
    $("#iotLegendDHTML").html("").append(templateLegend(params));

    var templateSearchSummary = Handlebars.compile($("#searchSummarySource").html());
    $("#iotSearchSummaryDHTML").html("").append(templateSearchSummary(msg));

    var template = Handlebars.compile($("#occupancyListSource").html());
    $("#iotOccupancyListDHTML").html("").append(template(msg));

    var templatePagination = Handlebars.compile($("#paginationSource").html());
    $("#iotPaginationDHTML").html("").append(templatePagination(msg));

    $('#iotOccupancyListDHTML').show();
}

function dustList(pageNo, params) {

    var msg = {
        "owner": "",
        "pagination": {
            "totalCount": 1,
            "rowNumber": 1,
            "pageNo": 1,
            "firstPage": 1,
            "lastPage": 1,
            "startPage": 1,
            "endPage": 1,
            "prePageNo": 0,
            "nextPageNo": 0,
            "existPrePage": false,
            "existNextPage": false,
            "pageRows": 5,
            "pageListCount": 5,
            "offset": 0,
            "uri": "/datas",
            "searchParameters": "&searchWord=data_name&searchOption=1&searchValue=&startDate=&endDate=&orderWord=&orderValue=&listCounter=10&dataType=",
            "pageIndex": 0
        },
        "dataList": [{
            "totalCount": null,
            "offset": null,
            "limit": null,
            "searchWord": null,
            "searchOption": null,
            "searchValue": null,
            "startDate": null,
            "endDate": null,
            "orderWord": null,
            "orderValue": null,
            "listCounter": 10,
            "messageCode": null,
            "errorCode": null,
            "duplicationValue": null,
            "dataCount": null,
            "referrer": null,
            "userGroupId": null,
            "latitude": 37.5134120429424,
            "longitude": 127.10374173176852,
            "userId": "admin",
            "updateUserId": null,
            "userName": null,
            "methodType": null,
            "dataId": 200000,
            "dataGroupId": 10001,
            "converterJobId": 4,
            "dataGroupName": "기본",
            "dataGroupTarget": "user",
            "dataGroupKey": "basic",
            "tiling": false,
            "dataKey": "admin_20201012230116_27204829199200",
            "oldDataKey": null,
            "dataName": "Lotte0603",
            "dataType": "citygml",
            "dataTypes": null,
            "sharing": "public",
            "parent": null,
            "parentName": null,
            "mappingType": "origin",
            "location": "0101000020E6100000474B5CB4A3C65F40B3E75E7CB7C14240",
            "altitude": 0E-7,
            "heading": 22.00000,
            "pitch": 0.00000,
            "roll": 0.00000,
            "childrenAncestor": null,
            "childrenParent": null,
            "childrenDepth": null,
            "childrenViewOrder": null,
            "metainfo": "{\"isPhysical\": true, \"heightReference\": \"relativeToGround\"}",
            "status": "use",
            "attributeExist": false,
            "objectAttributeExist": false,
            "description": null,
            "viewUpdateDate": "2020-10-12 23:12:06",
            "viewInsertDate": "2020-10-12 23:07:42",
            "updateDate": [2020, 10, 12, 23, 12, 6, 962551000],
            "insertDate": [2020, 10, 12, 23, 7, 42, 260364000],
            "viewMetainfo": "{\"isPhysical\": true,...",
            "parameters": "&searchWord=&searchOption=&searchValue=&startDate=&endDate=&orderWord=&orderValue=&listCounter=10"
        }],
        "errorCode": null,
        "message": null,
        "statusCode": 200
    };


    var templateLegend = Handlebars.compile($("#iotLegendSource").html());
    $("#iotLegendDHTML").html("").append(templateLegend(params));

    var templateSearchSummary = Handlebars.compile($("#searchSummarySource").html());
    $("#iotSearchSummaryDHTML").html("").append(templateSearchSummary(msg));

    var template = Handlebars.compile($("#dustListSource").html());
    $("#iotDustListDHTML").html("").append(template(msg));

    var templatePagination = Handlebars.compile($("#paginationSource").html());
    $("#iotPaginationDHTML").html("").append(templatePagination(msg));

    $('#iotDustListDHTML').show();
}

function iotDataSearch(pageNo) {

    $('#iotInfoContent div').hide();

    var $form = $("#searchIotForm");
    var params = getFormData($form);

    if (params.searchWord === 'iot_occupancy') {
        occupancyList(pageNo, params);
    } else if (params.searchWord === 'iot_dust') {
        dustList(pageNo, params);
    }

}