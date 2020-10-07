var viewer = new Cesium.Viewer('cesiumContainer');

var origin = new Cesium.Cartesian3.fromDegrees(16, 46, 3000);
var target = new Cesium.Cartesian3.fromDegrees(16.8, 46.2, 6000);

// Origin point
viewer.entities.add({
    position: origin,
    point : {
        color: Cesium.Color.LIGHTBLUE,
        pixelSize: 20,
        outlineColor: Cesium.Color.WHITE,
        outlineWidth: 2
    },
    label: {
        text: 'Origin',
        pixelOffset: { x: 0, y: 20 },
        verticalOrigin: Cesium.VerticalOrigin.TOP
    }
});

// Target point
viewer.entities.add({
    position: target,
    point : {
        color: Cesium.Color.GREENYELLOW,
        pixelSize: 20,
        outlineColor: Cesium.Color.WHITE,
        outlineWidth: 2
    },
    label: {
        text: 'Target',
        pixelOffset: { x: 0, y: 20 },
        verticalOrigin: Cesium.VerticalOrigin.TOP
    }
});

// Cone should point to 'target' from 'origin'
var direction = Cesium.Cartesian3.subtract(target, origin, new Cesium.Cartesian3());
Cesium.Cartesian3.normalize(direction, direction);
console.log(direction.x)

var rotationMatrix = Cesium.Transforms.rotationMatrixFromPositionVelocity(origin, direction);
var rot90 = Cesium.Matrix3.fromRotationY(Cesium.Math.toRadians(90));
Cesium.Matrix3.multiply(rotationMatrix, rot90, rotationMatrix);

viewer.entities.add({
    position: origin,
    orientation: Cesium.Quaternion.fromRotationMatrix(
        rotationMatrix
    ),
    cylinder: {
        length: 10000,
        topRadius: 0,
        bottomRadius: 3000,
        material: Cesium.Color.LIGHTBLUE.withAlpha(0.8)
    }
});

// Visualize direction
var directionRay = Cesium.Cartesian3.multiplyByScalar(direction, 100000, new Cesium.Cartesian3());
Cesium.Cartesian3.add(origin, directionRay, directionRay);

viewer.entities.add({
    polyline: {
        positions: [origin, directionRay],
        width: 1,
        material: Cesium.Color.WHITE
    }
});

console.log()
viewer.camera.setView({
    destination  : origin,
    orientation : {
        direction : new Cesium.Cartesian3(direction.x, direction.y, direction.z),
        up : new Cesium.Cartesian3()
    }
});
