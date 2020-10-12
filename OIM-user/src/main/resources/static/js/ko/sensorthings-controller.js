var SensorThingsController = function(dataGroupId, dataKey) {
    this.dataGroupId = dataGroupId;
    this.dataKey = dataKey;
    this.selectedDataKey = dataKey;
    this.cellSpaceList = [];
    this.dataSource = [];
    this.occupancyOfBuilding = 0;
    this.listOfFloorOccupancy = [];
    this.selectedFloorSensorList = [];
};

SensorThingsController.prototype.setCellSpaceList = function(cellSpaceList) {
    this.cellSpaceList = cellSpaceList;
};

SensorThingsController.prototype.arrangeDataFromSensorThingsAPIData = function(response) {
    const resultArray = response.value;
    for (const result of resultArray) {
        const arrangedData = {};
        arrangedData["location"] = result.location;
        arrangedData["name"] = result.name;
        arrangedData["Datastreams"] = result.Things[0].Datastreams[0];
        arrangedData["gmlID"] = result.Things[0].properties.gmlID;
        const cellSpaceId = result.name.split(":")[1];
        const floorInfo = this.cellSpaceList[cellSpaceId];
        if (floorInfo !== undefined) {
            arrangedData["floor"] = floorInfo.floor;
            arrangedData["cellSpaceId"] = cellSpaceId;
        }
        this.dataSource.push(arrangedData);
    }
};
SensorThingsController.prototype.calculateSumOfOccupancyOfBuilding = function() {
    let sum = 0;
    for (const sensor of this.dataSource) {
        sum += sensor.Datastreams.Observations[0].result;
    }
    this.occupancyOfBuilding = sum;
};
SensorThingsController.prototype.calculateSumOfOccupancyOfFloor = function(dataGroupId, dataKey) {
    const listOfSum = [];
    const nodes = MAGO3D_INSTANCE.getMagoManager().hierarchyManager.getNodesMap(dataGroupId, null);
    const targetNode = nodes[dataKey];
    const floors = targetNode["data"]["attributes"]["floors"];
    if (floors == undefined || floors == null) return;
    for (let i = 21; i >= 7; i--) {
        const floorObj = {
            floor: i,
            sum: 0
        };
        listOfSum.push(floorObj);
    }
    for (const sensor of this.dataSource) {
        const cellSpaceId = sensor.name.split(":")[1];
        const floorInfo = this.cellSpaceList[cellSpaceId];
        if (floorInfo !== undefined) {
            const floorNum = floorInfo.floor;
            for (const i in listOfSum) {
                if (listOfSum[i].floor == floorNum) {
                    listOfSum[i].sum += sensor.Datastreams.Observations[0].result;
                }
            }
        }
    }
    this.listOfFloorOccupancy = listOfSum;
};
SensorThingsController.prototype.openFloorInformation = function(dataGroupId, dataKey) {
    var _this = this;
    var template = Handlebars.compile($("#buildingInfoSource").html());
    $("#buildingInfoDHTML").html("").append(template(_this))
    $('#buildingInfoWrap').show();
    if ($('#mapSettingWrap').css('width') !== '0px') {
        $('#buildingInfoWrap').css('right', '400px');
    } else {
        $('#buildingInfoWrap').css('right', '60px');
    }
    // 재실자 층 선택
    $("#buildingInfoWrap table tr").click(function() {
        $(this).siblings().removeClass('selected');
        $(this).toggleClass('selected');
        const floor = $(this).data('floor');
        if (_this.selectedFloorSensorList.length > 0) {
            const rgbColorCode = "255,255,255,255";
            for (const sensor of _this.selectedFloorSensorList) {
                changeColorAPI(MAGO3D_INSTANCE.getMagoManager(), dataGroupId, _this.selectedDataKey, [sensor.cellSpaceId], "isPhysical=true", rgbColorCode);
            }
        }
        _this.displaySelectedFloor(floor, dataGroupId);
        _this.displaySelectedFloorMaker(floor, dataGroupId, _this.selectedDataKey);
        searchDataAPI(MAGO3D_INSTANCE, dataGroupId, _this.selectedDataKey);
    });
};
SensorThingsController.prototype.closeFloorInformation = function(dataGroupId, dataKey) {
    $('#buildingInfoWrap').hide();
};
SensorThingsController.prototype.displaySelectedFloor = function(floor, dataGroupId) {
    const nodes = MAGO3D_INSTANCE.getMagoManager().hierarchyManager.getNodesMap(dataGroupId, null);
    for (const i in nodes) {
        const node = nodes[i];
        const nodeData = node.data;
        if (!nodeData) continue;
        const nodeId = nodeData.nodeId;
        const nodeAttribute = nodeData.attributes;
        if (!nodeId || !nodeAttribute) continue;
        if (nodeId === dataGroupId) {
            nodeAttribute.isVisible = false;
            continue;
        }
        if (nodeAttribute.floors && nodeAttribute.floors.length > 0) {
            nodeAttribute.isVisible = false;
            continue;
        }
        const nodeIds = nodeId.split('_');
        const nodeFloor = parseInt(nodeIds[nodeIds.length - 1]);
        if (nodeFloor > floor) {
            nodeAttribute.isVisible = false;
        } else if (nodeFloor === floor) {
            nodeAttribute.isVisible = true;
            this.selectedDataKey = nodeId;
        } else {
            nodeAttribute.isVisible = true;
        }
    }
};
SensorThingsController.prototype.displaySelectedFloorMaker = function(floor, dataGroupId, dataKey) {
    const magoManager = MAGO3D_INSTANCE.getMagoManager();
    const objMarkerManager = magoManager.objMarkerManager;
    objMarkerManager.deleteObjects();
    this.selectedFloorSensorList = [];
    for (const data of this.dataSource) {
        if (data.floor === floor) {
            this.selectedFloorSensorList.push(data);
        }
    }

    const bubbleWidth = 40;
    const bubbleHeight = 40;
    const textSize = 16;
    const commentTextOption = {
        pixel: textSize,
        color: 'black',
        borderColor: 'white',
        text: ''
    };
    let rgbColorCode;
    for (const sensor of this.selectedFloorSensorList) {
        const sensorValue = sensor.Datastreams.Observations[0].result;
        commentTextOption.text = sensorValue.toString();

        rgbColorCode = this.getOccupancyColor(sensorValue);
        changeColorAPI(magoManager, dataGroupId, dataKey, [sensor.cellSpaceId], "isPhysical=true", rgbColorCode);

        rgbColorCode = rgbColorCode.split(",");
        rgbColorCode[0] = parseInt(rgbColorCode[0]) / 255;
        rgbColorCode[1] = parseInt(rgbColorCode[1]) / 255;
        rgbColorCode[2] = parseInt(rgbColorCode[2]) / 255;

        const speechBubbleOptions = {
            width: bubbleWidth,
            height: bubbleHeight,
            commentTextOption: commentTextOption,
            bubbleColor: {r: rgbColorCode[0], g: rgbColorCode[1], b: rgbColorCode[2]}
        };
        const target = {
            projectId: dataGroupId,
            buildingId: dataKey,
            objectId: sensor.cellSpaceId
        };
        const options = {
            speechBubbleOptions: speechBubbleOptions,
            target: target,
            id: sensor.cellSpaceId
        };
        objMarkerManager.newObjectMarkerSpeechBubble(options, magoManager);
    }
}
SensorThingsController.prototype.getOccupancyColor = function (value) {
    if (value === 0) {
        return "255,255,255,200";
    } else if (value < 3 && value >= 1) {
        return "0,153,255,200";
    } else if (value >= 3 && value < 6) {
        return "51,204,0,200";
    } else if (value >= 6) {
        return "255,255,0,200";
    }
};