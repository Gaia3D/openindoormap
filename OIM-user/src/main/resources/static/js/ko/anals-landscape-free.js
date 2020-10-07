//흠...전역번수는 싫은데....
let skylineObj = null;

const lsFreeAnalsWidget = function() {
    this._ele = '#lsFreeAnalsWidget'
}

lsFreeAnalsWidget.prototype.genHTML = function() {
    return $(this._ele).html();
}

lsFreeAnalsWidget.prototype.defaultRender = function () {
    $('#lsAnalsContent').empty()
    const templateHtml = Handlebars.compile(this.genHTML());
    $('#lsAnalsContent').append(templateHtml());
}


const lsSavedAnalsWidget = function() {
    this._ele = '#lsSavedAnalsWidget'
}

lsSavedAnalsWidget.prototype.genHTML = function() {
    return $(this._ele).html();
}

lsSavedAnalsWidget.prototype.defaultRenderByData = function (data) {
    $('#lsAnalsContent').empty()
    const templateHtml = Handlebars.compile(this.genHTML());
    $('#lsAnalsContent').append(templateHtml(data));
}

lsSavedAnalsWidget.prototype.reqeustDataBylsAnalsPg = function(lsAnalsPg) {
    const that = this;
    let param = '';
    if(lsAnalsPg !== undefined) {
        param += '?lsDiffPage='+lsAnalsPg;
    }
    $.ajax({
		url: 'http://118.42.112.206:5891/adminsvc/ls-point-rest' + param,
        method: 'GET'
    }).done(function(data) {
        that.defaultRenderByData(data);
    })
}

function paginSavedAnalsList(paginNum) {
    const p = new lsSavedAnalsWidget();
    p.reqeustDataBylsAnalsPg(paginNum)
}

const lsDrawLingComponent = function() {
    this._ele = '#lsDrawLineChk';
}

lsDrawLingComponent.prototype.evenctInit = function() {

}

lsDrawLingComponent.prototype.isChecked = function() {
    return $(this._ele).is(":checked");
}

lsDrawLingComponent.prototype.checked = function() {
    $(this._ele).prop('checked', true);
}

lsDrawLingComponent.prototype.unChecked = function() {
    $(this._ele).prop('checked', false);
}

lsDrawLingComponent.prototype.drawLine = function() {

}

const cesiumMouseEvt = {
    viewer: undefined,
    scene: undefined,
    canvas: undefined,
    pos: {
        start: undefined,
        end: undefined,
        move: undefined
    },
    entity: {
        start: undefined,
        end: undefined,
        line: undefined
    },
    workType: undefined,
    init: function(cesiumObj) {
        cesiumMouseEvt.viewer = cesiumObj.viewer;
        cesiumMouseEvt.scene = cesiumObj.scene;
        cesiumMouseEvt.canvas = cesiumObj.canvas;
        cesiumMouseEvt.workType = LandsDirecWorkType.WAIT;
        cesiumMouseEvt.leftMouseDBClick();
        cesiumMouseEvt.mouseMove();
    },
    clearEntity: function() {
        cesiumMouseEvt.pos.start = undefined;
        cesiumMouseEvt.pos.end = undefined;
        const entityLine = cesiumMouseEvt.entity.line;
        const entityStartDot = cesiumMouseEvt.entity.start;
        const entityEndDot = cesiumMouseEvt.entity.end;

        if (entityLine !== undefined) {
            cesiumMouseEvt.viewer.entities.remove(entityLine);
            cesiumMouseEvt.entity.line = undefined;
        }
        if (entityStartDot !== undefined) {
            cesiumMouseEvt.viewer.entities.remove(entityStartDot);
            cesiumMouseEvt.entity.start = undefined;
        }
        if (entityEndDot !== undefined) {
            cesiumMouseEvt.viewer.entities.remove(entityEndDot);
            cesiumMouseEvt.entity.end = undefined;
        }
    },
    leftMouseDBClick: function() {
        const handler = new Cesium.ScreenSpaceEventHandler(cesiumMouseEvt.canvas);
        handler.setInputAction( (click) => {
                if ( render.renderType === RenderType.DOT ) {
                    cesiumMouseEvt.action.dot(click);
                } else {
                    cesiumMouseEvt.action.line(click);
                }
            },
            Cesium.ScreenSpaceEventType.LEFT_DOUBLE_CLICK
        );
    },
    mouseMove: function() {
        const handler = new Cesium.ScreenSpaceEventHandler(cesiumMouseEvt.canvas);
        handler.setInputAction( (move) => {
                cesiumMouseEvt.pos.move = cesiumMouseEvt.posByEvt(move.endPosition);
            },
            Cesium.ScreenSpaceEventType.MOUSE_MOVE
        );
    },
    action: {
        dot: function(click) {
            cesiumMouseEvt.clearEntity();
            const pos = cesiumMouseEvt.posByEvt(click.position);
            cesiumMouseEvt.pos.start = pos;
            cesiumMouseEvt.entity.start = render.dot(cesiumMouseEvt.viewer, pos);
        },
        line: function(click) {
            const pos = cesiumMouseEvt.posByEvt(click.position);
            const workType = cesiumMouseEvt.workType;
            if( workType === LandsDirecWorkType.WAIT) {
                cesiumMouseEvt.clearEntity();
                console.log(`wait`); // line drawing
                cesiumMouseEvt.pos.start = pos;
                cesiumMouseEvt.entity.start = render.dot(cesiumMouseEvt.viewer, pos);
                cesiumMouseEvt.entity.line = render.line(cesiumMouseEvt.viewer, pos);
                cesiumMouseEvt.workType = LandsDirecWorkType.RUN;
            } else if(workType === LandsDirecWorkType.RUN) {
                console.log(`run`);
                cesiumMouseEvt.workType = LandsDirecWorkType.WAIT;
                cesiumMouseEvt.entity.end = render.dot(cesiumMouseEvt.viewer, pos);
                cesiumMouseEvt.pos.end = pos;
            }
        }
    },
    posByEvt: function(e) {
        const cartesian = cesiumMouseEvt.scene.pickPosition(e);
        if (cartesian) {
            const cartographic = Cesium.Cartographic.fromCartesian(cartesian);
            const longitudeString = Cesium.Math.toDegrees(
                cartographic.longitude
            );
            const latitudeString = Cesium.Math.toDegrees(
                cartographic.latitude
            );
            const height = cartographic.height;
            return  {
                long: longitudeString,
                lat: latitudeString,
                alt: height
            };
        }
    }
};

const render = {
    renderType: undefined,
    init: function() {
        this.renderType = RenderType.LINE
    },
    dot: function(viewer, pos) {
        const result = viewer.entities.add({
            position: new Cesium.Cartesian3.fromDegrees(pos.long, pos.lat, pos.alt),
            ellipsoid: {
                radii: new Cesium.Cartesian3(10, 10, 10),
                material: Cesium.Color.RED,
            },
        });
        return result;
    },
    line: (viewer, pos) => {
        const result = viewer.entities.add({
            polyline: {
                // This callback updates positions each frame.
                positions: new Cesium.CallbackProperty(() => {
                    const resTimeMousePos = cesiumMouseEvt.pos.move;
                    let p;
                    if (cesiumMouseEvt.workType === LandsDirecWorkType.WAIT) {
                        p = Cesium.Cartesian3.fromDegreesArrayHeights(this.polyLinelastPos);
                        return p;
                    }
                    this.polyLinelastPos = [pos.long, pos.lat, pos.alt, resTimeMousePos.long,
                        resTimeMousePos.lat, resTimeMousePos.alt];
                    p = Cesium.Cartesian3.fromDegreesArrayHeights(
                        this.polyLinelastPos
                    );
                    return p;
                }, false),
                width: 10,
                clampToGround: true,
                material: new Cesium.PolylineOutlineMaterialProperty({
                    color: Cesium.Color.YELLOW,
                })
            },
        });
        return result;
    }
};


const LandsDirecWorkType = {
    WAIT : 0,
    RUN : 1,
    FINISH : 2,
    POLY : 3
};
const MouseEvtType = {
    UP : 0,
    DOWN : 1,
    MOVE : 2
};
const RenderType = {
    DOT : 0,
    LINE : 1
};


// $(document).ready(function(){
// 	let _init = function(){
// 	    const viewer = MAGO3D_INSTANCE.getViewer();
// 	    const scene = MAGO3D_INSTANCE.getViewer().scene;
// 	    const canvas = MAGO3D_INSTANCE.getViewer().scene.canvas;
// 	    cesiumMouseEvt.init({
// 	        viewer: viewer,
// 	        scene: scene,
// 	        canvas: canvas,
// 	    });
// 	    render.init();
//
// 	    const p = new lsAnalsBtn();
//         p.init();
//
//         console.log(new Date(), 'anals-landscape-free', '<<._init()');
// 	};
//
// 	//
// 	//가끔 MAGO3D_INSTANCE생성되기전에 아래 로직 호출되는 경우 존재. 해서, 인터벌 사용
// 	let interval = setInterval(function(){
// 		if(Pp.isNotNull(MAGO3D_INSTANCE)){
// 			clearInterval(interval);
// 			_init();
// 		}
//
// 	}, 500);
//
// });









/**
 * 경관분석 - 자율분석
 * @author gravity
 * @since 20200908 init
 */
const LsAnalsAutoObj = function(){
	this._xyz1 = {};
	this._xyz2 = {};
};

/**
 * 초기
 */
LsAnalsAutoObj.prototype.init = function(){
	this.setEventHandler();
	
	//
	console.log('LsAnalsAutoObj', '<<.init');
};

/**
 * 이벤트 등록
 */
LsAnalsAutoObj.prototype.setEventHandler = function(){
	let _this = this;
	console.log(Ppui.find('#landscapeAnalsBtn'));
	
	//분석 버튼 클릭
	Ppui.click('#landscapeAnalsBtn', function(){
		_this.doAnals();
	});
	
	//두점선택 버튼 클릭
	Ppui.click('.ds-create-two-points', function(){
		let el = this;
		
		//
		Ppmap.resetRotate(function(){
			//
			toastr.info('지도상에서 두점을 클릭하시기 바랍니다.');
			//
			// el.disabled = true;
			
			//
			_this.createTwoPoints();
		});
	});
};


/**
 * 지도위에 2점 생성
 * 사용자가 2점 선택하도록 함
 * 2점 모두 마우스 왼쪽 버튼 1클릭으로 생성
 */
LsAnalsAutoObj.prototype.createTwoPoints = function(){
	let _this = this;
	
	//
	_this._xyz1 = {};
	_this._xyz2 = {};
	
	//
	Ppmap.removeAll();

	
	//
	const handler = new Cesium.ScreenSpaceEventHandler(MAGO3D_INSTANCE.getViewer().scene.canvas);
	//	
	handler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);
    handler.removeInputAction(Cesium.ScreenSpaceEventType.MOUSE_MOVE);
    
    //
    Ppmap.setCursor('pointer');
	
	//마우스 왼쪽 클릭 이벤트 등록
    handler.setInputAction( function(click) {

		//점1 세팅
		if(Pp.isEmpty(_this._xyz1.lon)){
			_this._xyz1 = Ppmap.cartesian2ToLonLatAlt(click.position);
            _this._xyz1.alt = _this._xyz1.alt + new lsAnalsMoveInputBox().getHeight();
			//		
			Ppmap.createPointAndAlt('ls-anals-auto-xyz1', _this._xyz1.lon, _this._xyz1.lat, _this._xyz1.alt);
			//
			return;
		}

		//점2 세팅
		if(Pp.isEmpty(_this._xyz2.lon)){
			_this._xyz2 = Ppmap.cartesian2ToLonLatAlt(click.position);
            _this._xyz2.alt = _this._xyz2.alt + new lsAnalsMoveInputBox().getHeight();
			//		
			Ppmap.createPointAndAlt('ls-anals-auto-xyz2', _this._xyz2.lon, _this._xyz2.lat, _this._xyz2.alt);
		}
		
		//
		if(Pp.isNotEmpty(_this._xyz1.lon) && Pp.isNotEmpty(_this._xyz2.lon)){
			//이벤트 삭제
			handler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);
			handler.removeInputAction(Cesium.ScreenSpaceEventType.MOUSE_MOVE);
            
            //
            Ppmap.restoreCursor();		

			//
			// Ppui.find('.ds-create-two-points').disabled = false;

			//분석. 0.5초 지연
			setTimeout(function(){
				_this.doAnals();
                Ppmap.removeAll();
			}, 500);
		}
			
        },
        Cesium.ScreenSpaceEventType.LEFT_CLICK
    );


	//마우스 이동
	handler.setInputAction( function(e) {
		//
		let xyz = Ppmap.cartesian2ToLonLat(e.endPosition);
		//console.log(e.endPosition, xyz);
		
		//
		if(Pp.isEmpty(_this._xyz1.lon) || Pp.isEmpty(xyz.lon)){
			return;
		}
		
		//
		Ppmap.removeEntity(window['entity']);
		
		//
		let entity = MAGO3D_INSTANCE.getViewer().entities.add({
			 polyline: {
                // This callback updates positions each frame.
                positions: new Cesium.CallbackProperty(function() {
					return Cesium.Cartesian3.fromDegreesArray([_this._xyz1.lon, _this._xyz1.lat, xyz.lon, xyz.lat]);                    
                }, false),
                width: 10,
                clampToGround: true,
                material: new Cesium.PolylineOutlineMaterialProperty({
                    color: Cesium.Color.YELLOW,
                })
            },
		});
		
		//
		window['entity'] = entity;		
		},
		Cesium.ScreenSpaceEventType.MOUSE_MOVE
	);
	
	
};

/**
 * 분석
 */
LsAnalsAutoObj.prototype.doAnals = function(){
	if(Pp.isEmpty(this._xyz1.lon) || Pp.isEmpty(this._xyz2.lon)){
		toastr.warning('경관점이 선택되지 않았습니다. <br>분석을 취소합니다.');
		return;
	}
	
	//
	new SkylineObj().init().process(this._xyz1, this._xyz2);		
};

//
let lsAnalsAutoObj = new LsAnalsAutoObj();
$(function() {
    let interval = setInterval(function(){
        if(0 != Ppui.find('.ds-create-two-points').length){
            //
            clearInterval(interval);
            lsAnalsAutoObj.init();
        }

    }, 500);
})

// $(function() {
//     leftMouseDoubleClick();
//     function leftMouseDoubleClick() {
//         const handler = new Cesium.ScreenSpaceEventHandler(Ppmap.getViewer().canvas);
//         handler.setInputAction( (click) => {
//                 console.log(click);
//                 debugger;
//                 let xyz = Ppmap.cartesian2ToLonLat(click.position);
//                 console.log(xyz);
//             },
//             Cesium.ScreenSpaceEventType.LEFT_CLICK
//         );
//     }
// })