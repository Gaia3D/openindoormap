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
            floorName: (i - 6) + " 층",
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
    $("#buildingInfoDHTML").html("").append(template(_this));
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
        const magoManager = MAGO3D_INSTANCE.getMagoManager();
        const objMarkerManager = magoManager.objMarkerManager;
        objMarkerManager.deleteObjects();
        _this.displaySelectedFloor(floor, dataGroupId);
        _this.displaySelectedFloorMaker(floor, dataGroupId, _this.selectedDataKey);
        /*
        window.setInterval(function(){
            _this.displaySelectedFloorMaker(floor, dataGroupId, _this.selectedDataKey);
        }, 2000);
         */
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
    //objMarkerManager.deleteObjects();
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
        const sensorValue = sensor.Datastreams.Observations[0].result /*+ parseInt(Math.random() * 10)*/;
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

        const marker = objMarkerManager.getObjectMarkerById(sensor.cellSpaceId);
        if (marker) {
            marker.setImageFilePath(Mago3D.SpeechBubble.getImage(speechBubbleOptions, magoManager));
        } else {
            objMarkerManager.newObjectMarkerSpeechBubble(options, magoManager);
        }

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
SensorThingsController.prototype.getSensorInformation = function(marker) {
    const _this = this;
    const sensorId = marker.id;
    const selectedSensorArray = _this.dataSource.filter(sensor => sensor.cellSpaceId === sensorId);

    const sensorData = {};
    if (selectedSensorArray === undefined || selectedSensorArray.length === 0) {
        return;
    }

    const selectedSensor = selectedSensorArray[0];
    sensorData.sensorId = sensorId;
    sensorData.sensorDatastreams = selectedSensor.Datastreams;
    sensorData.sensorName = selectedSensor.name;
    sensorData.sensorGMLId = selectedSensor.gmlID;
    sensorData.lastValue = sensorData.sensorDatastreams.Observations[0].result;
    sensorData.lastTime = sensorData.sensorDatastreams.Observations[0].phenomenonTime;

    var template = Handlebars.compile($("#sensorInfoSource").html());
    $("#sensorInfoDHTML").html("").append(template(sensorData));

    const thingsResultList = [];
    const thingsTimeList = [];
    for (let i = 0; i < sensorData.sensorDatastreams.Observations.length; i++) {
        const observation = sensorData.sensorDatastreams.Observations[i];
        thingsResultList.push(observation.result);
        thingsTimeList.push(observation.phenomenonTime);
    }
    this.drawSensorChart(thingsTimeList, thingsResultList, "sensorChart");
    $('#sensorInfoWrap').show();
};
SensorThingsController.prototype.gotoSensor = function(sensorId) {
    const cellSpace = this.cellSpaceList[sensorId];
    const localCoordinate = {x: cellSpace.x, y: cellSpace.y, z: cellSpace.z};
    const magoManager = MAGO3D_INSTANCE.getMagoManager();
    const targetNode = magoManager.hierarchyManager.getNodeByDataKey(this.dataGroupId, this.selectedDataKey);
    const targetNodeGeoLocDataManager = targetNode.getNodeGeoLocDataManager();
    const targetNodeGeoLocData = targetNodeGeoLocDataManager.getCurrentGeoLocationData();
    const tempGlobalCoordinateObject = targetNodeGeoLocData.localCoordToWorldCoord(localCoordinate);
    const wgs84CoordinateObject = Mago3D.Globe.CartesianToGeographicWgs84(tempGlobalCoordinateObject.x, tempGlobalCoordinateObject.y, tempGlobalCoordinateObject.z);

    const longitude = Number(wgs84CoordinateObject.longitude);
    const latitude = Number(wgs84CoordinateObject.latitude);
    const altitude = Number(wgs84CoordinateObject.altitude);

    if (isNaN(longitude) || isNaN(latitude) || isNaN(altitude))
        return;

    magoManager.flyTo(longitude, latitude, altitude + 10.0, 3);
};
SensorThingsController.prototype.drawSensorChart = function(xData, yData, canvasName) {
    sensorChart = new Chart(document.getElementById(canvasName), {
            type: 'line',
            data: {
                labels: xData,
                datasets: [{
                    label: "people",
                    data: yData,
                    fill: false,
                    borderColor: "rgba(255, 201, 14, 1)"
                }]
            },
            options: {
                animation: false,
                responsive: true,
                maintainAspectRatio: false,
                hover: {
                    mode: 'index',
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
                            tooltipFormat: 'YYYY-MM-DD HH:mm',
                            displayFormats: {
                                second: 'HH:mm:ss a'
                            }
                        },
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: 'date'
                        }
                    }],
                    yAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: 'number'
                        },
                        ticks: {
                            autoSkip: true,
                            minRotation: 0,
                            min: 0,
                            max: 15
                        }
                    }]
                }
            }
        }
    );
};
SensorThingsController.prototype.closeSensorInformation = function() {
    $('#sensorInfoWrap').hide();
};