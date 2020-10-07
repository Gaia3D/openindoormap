var AnalsLandScapeDirection = function(viewer, magoInstance) {
    const magoManager = magoInstance.getMagoManager();
    this._viewer = viewer;
    this._scene = viewer.scene;
    let workingMode;
    let realtimeMousePos;
    let pointEntitiy = [];
    let polylineEntitiy;
    let polyLinelastPos;
    let polygonEntitiy;

    const landScapPos = {
        startLocalPosition: undefined,
        endLocalPosition: undefined,
        externalLocalPosition: undefined,
        startWorldPosition: undefined,
        endWorldPosition: undefined
    };
    const LandsacpeDirectEnum = {
        WAIT: 0,
        RUN: 1,
        FINISH: 2,
        POLYGON: 3,
    }
    const MouseEvtEnum = {
        NONE: 0,
        UP: 1,
        DOWN: 2,
        MOVE: 3
    };

    let mouseType = MouseEvtEnum.NONE;
    let landScapeDirectType = LandsacpeDirectEnum.WAIT;
    $('#landscapeDirectToggle').click(function() {
        const statusChecked = $("#landscapeDirectToggle").is(":checked");
        if(statusChecked) {
            workingMode = 'landscapedirect';
            viewer.camera.lookAt(viewer.scene.camera.position,
                new Cesium.HeadingPitchRange(0, Cesium.Math.toRadians(-90.0),
                    viewer.scene.camera.positionCartographic.height));
            viewer.camera.lookAtTransform(Cesium.Matrix4.IDENTITY);
        } else {
            workingMode = undefined;
        }
    });

    landscapeDirectionEvt();

    function landscapeDirectionEvt() {
        leftMouseDoubleClick();
        mouseMove()
    }

    function clearPointEntitiy() {
        pointEntitiy.forEach( p => viewer.entities.remove(p))
    }

    function getPositionByCesiumEvt(e) {
        const cartesian = viewer.scene.pickPosition(e);
        if (cartesian) {
            const cartographic = Cesium.Cartographic.fromCartesian(cartesian);
            const longitudeString = Cesium.Math.toDegrees(cartographic.longitude);
            const latitudeString = Cesium.Math.toDegrees(cartographic.latitude);
            const height = cartographic.height;
            return  {
                long: longitudeString,
                lat: latitudeString,
                alt: height
            };
        }
    }

    function procLandScapeDirection(pos) {
        const startPos = landScapPos.startLocalPosition;
        const endPos = landScapPos.endLocalPosition;
        const externalPos = landScapPos.externalLocalPosition;
        const startWorldPos = landScapPos.startWorldPosition;
        const endWorldPos = landScapPos.endWorldPosition;
        const pXY = getExternalPoint(startPos, endPos, externalPos);
        const externalWorldPos = getPositionByCesiumEvt(pXY);
        const resultX = endWorldPos.long + (endWorldPos.long - externalWorldPos.long);
        const resultY = endWorldPos.lat + (endWorldPos.lat - externalWorldPos.lat);
        const polygonResult = [
            startWorldPos.long, startWorldPos.lat, startWorldPos.alt,
            resultX, resultY, endWorldPos.alt,
            externalWorldPos.long, externalWorldPos.lat, endWorldPos.alt,
        ];
        const pointResultLeft = {
            alt: endWorldPos.alt,
            long: externalWorldPos.long,
            lat: externalWorldPos.lat
        }
        const pointResultRight = {
            alt: endWorldPos.alt,
            long: resultX,
            lat: resultY
        }
        pointEntitiy.push(drawLandScapePoint(pointResultLeft));
        pointEntitiy.push(drawLandScapePoint(pointResultRight));
        drawLandScapePolygon(polygonResult);
        if(polylineEntitiy !== undefined){
            viewer.entities.remove(polylineEntitiy);
        }
    }

    function getExternalPoint(startPos, endPos, externalPos) {
        const angle = calcTheta(startPos, endPos, externalPos);
        startPos.y *= -1;
        endPos.y *= -1;
        const startPosCart = new Cesium.Cartesian2(startPos.x, startPos.y);
        const endPosCart = new Cesium.Cartesian2(endPos.x, endPos.y);
        const angle2Degree = Cesium.Math.toDegrees(angle);
        const rotationMat = [[Math.cos(angle), -Math.sin(angle)], [Math.sin(angle), Math.cos(angle)]];
        let a = rotationMat[0][0];
        let b = rotationMat[0][1];
        let c = rotationMat[1][0];
        let d = rotationMat[1][1];
        const divided = 1 / ((a * d) - (b * c));
        a *= divided;
        b = (b * -1) * divided;
        c = (c * -1) * divided;
        d *= divided;
        const invRotMat = [[a, b], [c, d]];
        const moveStartPos = new Cesium.Cartesian2(0, 0);
        const moveEndPos = new Cesium.Cartesian2(0, 0);
        Cesium.Cartesian2.subtract(startPosCart, startPosCart, moveStartPos);
        Cesium.Cartesian2.subtract(endPosCart, startPosCart, moveEndPos);
        const abLen = Cesium.Cartesian2.distance(moveStartPos, moveEndPos);
        const acLen = abLen / Math.cos(angle);

        const x = invRotMat[0][0] * moveEndPos.x + invRotMat[0][1] * moveEndPos.y;
        const y = invRotMat[1][0] * moveEndPos.x + invRotMat[1][1] * moveEndPos.y;
        const dotB = [x, y];
        const p0 = [dotB[0] / abLen, dotB[1] / abLen];
        const p1 = [p0[0] * acLen, p0[1] * acLen];
        const p2 = [p1[0] + startPosCart.x, p1[1] + startPosCart.y];
        p2[1] *= -1;

        const pXY = {x: p2[0].toFixed(0), y: p2[1].toFixed(0)};
        return pXY;
    }

    function calcTheta(c, v1, v2) {
        const theta = Math.atan2(v2.y - c.y, v2.x - c.x) - Math.atan2(v1.y - c.y, v1.x - c.x);
        return theta;
    }


    function drawLandScapePoint(pos) {
        return viewer.entities.add({
            position: new Cesium.Cartesian3.fromDegrees(pos.long, pos.lat, pos.alt),
            ellipsoid: {
                radii: new Cesium.Cartesian3(10, 10, 10),
                color : new Cesium.Color(255/255, 145/255, 143/255, 0.7),
            }
        });
    }

    function drawLandScapePolygon(result) {
        const polyPosi = new Cesium.PolygonHierarchy(Cesium.Cartesian3.fromDegreesArrayHeights(result));
        polygonEntitiy = viewer.entities.add({
            polygon : {
                hierarchy : polyPosi,
                material : new Cesium.Color(179/255, 216/255, 254/255, 0.7),
                perPositionHeight: true,
                outline : true,
                outlineColor : new Cesium.Color(179/255, 216/255, 254/255, 1),
            }
        });
    }

    function drawLandScapePolyLine(pos) {
        polylineEntitiy = viewer.entities.add({
            polyline: {
                // This callback updates positions each frame.
                positions: new Cesium.CallbackProperty(() => {
                    if(workingMode !== 'landscapedirect')
                        return;
                    let p;
                    if (landScapeDirectType === LandsacpeDirectEnum.FINISH) {
                        p = Cesium.Cartesian3.fromDegreesArrayHeights(polyLinelastPos);
                        return p;
                    }
                    polyLinelastPos = [pos.long, pos.lat, pos.alt, realtimeMousePos.long,
                        realtimeMousePos.lat, realtimeMousePos.alt];
                    p = Cesium.Cartesian3.fromDegreesArrayHeights(
                        polyLinelastPos
                    );
                    return p;
                }, false),
                width: 10,
                // clampToGround: true,
                material: new Cesium.PolylineOutlineMaterialProperty({
                    color: new Cesium.Color(179/255, 216/255, 254/255, 0.7)
                })
            },
        });
    }

    function leftMouseDoubleClick() {
        const handler = new Cesium.ScreenSpaceEventHandler(viewer.canvas);
        handler.setInputAction( (click) => {
                if(workingMode === 'landscapedirect') {
                    const pos = getPositionByCesiumEvt(click.position);
                    switch (landScapeDirectType) {
                        case LandsacpeDirectEnum.WAIT:
                            clearPointEntitiy();
                            initDrawInfo();
                            pointEntitiy.push(drawLandScapePoint(pos));
                            drawLandScapePolyLine(pos);
                            landScapPos.startLocalPosition = {...click.position};
                            landScapPos.startWorldPosition = pos;
                            landScapeDirectType = LandsacpeDirectEnum.RUN;
                            break;
                        case LandsacpeDirectEnum.RUN:
                            landScapeDirectType = LandsacpeDirectEnum.FINISH;
                            pointEntitiy.push(drawLandScapePoint(pos));
                            landScapPos.endLocalPosition = {...click.position};
                            landScapPos.endWorldPosition = pos;
                            break;
                        case LandsacpeDirectEnum.FINISH: // polygon draw
                            landScapPos.externalLocalPosition = {...click.position};
                            procLandScapeDirection(pos);
                            landScapeDirectType = LandsacpeDirectEnum.WAIT;
                            break;
                    }
                }
            },
            Cesium.ScreenSpaceEventType.LEFT_DOUBLE_CLICK
        );
    }

    function initDrawInfo() {
        if(polylineEntitiy !== undefined){
            viewer.entities.remove(polylineEntitiy);
        }
        if(polygonEntitiy !== undefined) {
            viewer.entities.remove(polygonEntitiy);
        }
        polyLinelastPos = undefined;
        polygonEntitiy = undefined;
    }

    function mouseMove() {
        const handler = new Cesium.ScreenSpaceEventHandler(viewer.canvas);
        handler.setInputAction( (move) => {
                if(workingMode === 'landscapedirect') {
                    mouseEndMoveWorldPos(move.endPosition);
                }
            },
            Cesium.ScreenSpaceEventType.MOUSE_MOVE
        );
    }

    function getPositionByCesiumEvt(e) {
        const cartesian = viewer.scene.pickPosition(e);
        if (cartesian) {
            const cartographic = Cesium.Cartographic.fromCartesian(cartesian);
            const longitudeString = Cesium.Math.toDegrees(cartographic.longitude);
            const latitudeString = Cesium.Math.toDegrees(cartographic.latitude);
            const height = cartographic.height;
            return  {
                long: longitudeString,
                lat: latitudeString,
                alt: height
            };
        }
    }

    function mouseEndMoveWorldPos(pos) {
        realtimeMousePos = getPositionByCesiumEvt(pos);
    }
}

