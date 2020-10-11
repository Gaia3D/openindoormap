var SensorThingsController = function(dataGroupId, dataKey) {
    this.dataGroupId = dataGroupId;
    this.dataKey = dataKey;
    this.cellSpaceList = [];
    this.dataSource = [];
    this.occupancyOfBuilding = 0;
    this.listOfFloorOccupancy = {};
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
    const listOfSum = {};
    const nodes = MAGO3D_INSTANCE.getMagoManager().hierarchyManager.getNodesMap(dataGroupId, null);
    const targetNode = nodes[dataKey];
    const floors = targetNode["data"]["attributes"]["floors"];
    if (floors == undefined || floors == null) return;
    for (const i of floors) {
        if (!(i in listOfSum)) {
            listOfSum[i] = 0;
        }
    }
    for (const sensor of this.dataSource) {
        const cellspaceId = sensor.name.split(":")[1];
        const floorInfo = this.cellSpaceList[cellspaceId];
        if (floorInfo !== undefined) {
            const floorNum = floorInfo.floor;
            if (floorNum in listOfSum) {
                listOfSum[floorNum] += sensor.Datastreams.Observations[0].result;
            }
        }
    }
    this.listOfFloorOccupancy = listOfSum;
}