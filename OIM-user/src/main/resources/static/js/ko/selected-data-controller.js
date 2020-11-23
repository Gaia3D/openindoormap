var SelectedDataController = function(magoInstance) {
	this.magoInstance = magoInstance;
	this.selected;
	
	this.setEventHandler();
}

SelectedDataController.prototype.isActive = function() {
	return this.selected ? true : false;
}

SelectedDataController.prototype.setEventHandler = function() {
	var magoManager = this.magoInstance.getMagoManager();
	var translateInteraction = magoManager.defaultTranslateInteraction;
	var that = this;
	
	magoManager.on(Mago3D.MagoManager.EVENT_TYPE.SELECTEDF4D, function(result) {
		var f4d = result.selected;
		if(f4d && f4d instanceof Mago3D.Node) {
			that.selectData(f4d);
		}
	});
	
	magoManager.on(Mago3D.MagoManager.EVENT_TYPE.SELECTEDGENERALOBJECT, function(result) {
		var native = result.selected;
		if(native && native instanceof Mago3D.MagoRenderable) {
			that.selectData(native);
		}
	});
	
	magoManager.on(Mago3D.MagoManager.EVENT_TYPE.DESELECTEDF4D, function(result) {
		var f4d = result.deselected
		if(f4d && f4d instanceof Mago3D.Node) {
			that.deselectData();
		}
	});
	
	magoManager.on(Mago3D.MagoManager.EVENT_TYPE.DESELECTEDGENERALOBJECT, function(result) {
		var native = result.deselected
		if(native && native instanceof Mago3D.MagoRenderable) {
			that.deselectData();
		}
	});
	
	//높이 보정결과 UI 적용 
	magoManager.on(Mago3D.MagoManager.EVENT_TYPE.VALIDHEIGHTEND, function(result) {
		var arr = result.validDataArray;
		var len = arr.length;
		for(var i=0;i<len;i++) {
			if(arr[i] === that.selected) {
    			var heightReference = that.getHeightReferenceFromData();
    			that.changeHeightInfo(heightReference);
				break;
			}
		}
	});
	
	//선택된 데이터 이동 시 결과 리턴
	translateInteraction.on(Mago3D.TranslateInteraction.EVENT_TYPE.MOVING_F4D, function(moved) {
		moving(moved.result);
	});
	translateInteraction.on(Mago3D.TranslateInteraction.EVENT_TYPE.MOVING_NATIVE, function(moved) {
		moving(moved.result);
	});
	
	function moving(target) {
    	var movedGeolocationData = target.getCurrentGeoLocationData();
    	var positionInfo = that.getPositionInfoFromGeolocationData(movedGeolocationData);
    	var heightReference = that.getHeightReferenceFromData();
		that.changePositionInfo(positionInfo);
		that.changeHeightInfo(heightReference);
	}
	
	//높이 참조가 '기준없음(Mago3D.HeightReference.NONE)' 이 아닐 경우 높이 보정 
	translateInteraction.on(Mago3D.TranslateInteraction.EVENT_TYPE.MOVE_END_F4D, function(moved) {
		moveEnd();
	});
	translateInteraction.on(Mago3D.TranslateInteraction.EVENT_TYPE.MOVE_END_NATIVE, function(moved) {
		moveEnd();
	});
	
	function moveEnd() {
    	if($('#dcHeightReference').val() !== Mago3D.HeightReference.NONE) {
    		if(that.selected instanceof Mago3D.MagoRenderable) {
    			console.info(that.selected.smartTileOwner.X + ' : ' + that.selected.smartTileOwner.Y)
    		}
    		that.addNeedValidHeightData();
    	}
	}
	
	//색상변경 적용
	$('#dcColorApply').click(function() {
		that.changeColor();
	});
	//색상변경 취소
	$('#dcColorCancle').click(function() {
		that.restoreColor();
	});
	
	//칼라피커 색상 인풋에 저용
	$('#dcColorPicker').change(function(){
		var color = $('#dcColorPicker').val();
		$('#dcColorInput').val(color).css('color',color);
	});
	
	//회전 변경 range 조절
	$('#dcPitchRange,#dcHeadingRange,#dcRollRange').on('input change',function(){
		var val = $(this).val();
		var type = $(this).data('type');
		$('#dc' + type).val(val);

		that.changePosition();
	});

	//회전 변경 버튼 조절
	var rotBtnHoldInterval;
	$('.dcRangeBtn').on('click', function(e) {
		if (rotBtnHoldInterval) clearInterval(rotBtnHoldInterval);
		changeRotation($(this));
	});
	$('.dcRangeBtn').on('mousedown', function(e) {
		if (rotBtnHoldInterval) clearInterval(rotBtnHoldInterval);
		var $this = $(this);
		rotBtnHoldInterval = setInterval(function(){
			changeRotation($this);
		}, 150);
	});
	$('.dcRangeBtn').on('mouseup mouseleave',function() {
		clearInterval(rotBtnHoldInterval);
	});

	function changeRotation($btn) {
		var type = $btn.data('type');
		var range = $btn.siblings('input[type="range"]');
		var offset = (type ==='prev') ? -1 : 1;
		var curVal = parseFloat(range.val());
		range.val(curVal + offset).change();
	}
	
	//데이터 높이 이벤트
	var locAltholdInterval;
	$('#dcAltUp,#dcAltDown').on('click', function(e) {
		if (locAltholdInterval) clearInterval(locAltholdInterval);
		changeAltitude($(this));
	});
	$('#dcAltUp,#dcAltDown').on('mousedown', function(e) {
		if (locAltholdInterval) clearInterval(locAltholdInterval);
		var $this = $(this);
		locAltholdInterval = setInterval(function(){
			changeAltitude($this);
		}, 150);
	});
	
	function changeAltitude($btn) {
		if($('#dcHeightReference').val() === Mago3D.HeightReference.CLAMP_TO_GROUND) {
			if(confirm('현재 높이 참조가 지표면에 데이터가 위치하도록 되어 있습니다. 높이 참조를 이용하지 않겠습니까?')) {
				$('#dcHeightReference').val(Mago3D.HeightReference.NONE);
				that.changeHeightReference();
			} else {
				return;
			} 
		}
		
		var type = $btn.data('type');
		var offset = parseFloat($('#dcAltitudeOffset').val());
		offset = (type==='up') ? offset : -offset;

		var alt = parseFloat($('#dcAltitude').val());
		$('#dcAltitude').val(alt + offset);

		that.changePosition();
	}
	
	$('#dcAltUp,#dcAltDown').on('mouseup mouseleave',function() {
		clearInterval(locAltholdInterval);
	});
	
	$('#dcHeightReference').on('change', function() {
		that.changeHeightReference();
	});
	
	//속성조회
	$('#dcShowAttr').click(function(){
		detailDataInfo("/datas/" + that.selected.data.dataId);
	});
	
	//위치회전정보 저장
	$('#dcSavePosRot').click(function() {
		if(confirm(JS_MESSAGE["data.update.check"])) {
			if(!that.selected) {
				alert(JS_MESSAGE["data.not.select"]);
				return false;
			}
			startLoading();
			var formData = $('#dcRotLocForm').serialize();
			var dataId = that.selected.data.dataId;
			$.ajax({
				url: "/datas/" + dataId,
				type: "POST",
				headers: {"X-Requested-With": "XMLHttpRequest"},
				data: formData,
				success: function(msg){
					if(msg.statusCode <= 200) {
						alert(JS_MESSAGE["update"]);
					} else if(msg.statusCode === 403) {
						//data.smart.tiling
						alert(JS_MESSAGE["data.smart.tiling.grant.required"]);
					} else if (msg.statusCode === 428) {
						if(confirm(JS_MESSAGE[msg.errorCode])) {
							$('input[name="dataId"]').val(dataId);
							var formData = $('#dcRotLocForm').serialize();
							$.ajax({
								url: "/data-adjust-logs",
								type: "POST",
								headers: {"X-Requested-With": "XMLHttpRequest"},
								data: formData,
								success: function(msg){
									if(msg.statusCode <= 200) {
										alert(JS_MESSAGE["requested"]);
									} else {
										alert(JS_MESSAGE[msg.errorCode]);
										console.log("---- " + msg.message);
									}
									insertDataAdjustLogFlag = true;
								},
								error: function(request, status, error){
							        alert(JS_MESSAGE["ajax.error.message"]);
							        insertDataAdjustLogFlag = true;
								},
								always: function(msg) {
									$('input[name="dataId"]').val("");
								}
							});
						}
					} else {
						alert(JS_MESSAGE[msg.errorCode]);
						console.log("---- " + msg.message);
					}
					updateDataInfoFlag = true;
				},
				error:function(request, status, error){
			        alert(JS_MESSAGE["ajax.error.message"]);
			        updateDataInfoFlag = true;
				}
			}).always(stopLoading);
		} else {
			//alert('no');
		}
	});
}

SelectedDataController.prototype.selectData = function(selected) {
	var that = this;
	that.toggleWrap(true);
	that.selected = selected;
	
	init(selected);
	
	function init(selectedData) {
		setTitle(selectedData);
		setHex(selectedData);
		setPositionInfo(selectedData);
		setHeightInfo(selectedData);
		setBtn(selectedData);
	}
	
	function setTitle(selectData) {
		if(selectData instanceof Mago3D.Node) {
			var data = selectData.data;
			var projectId = data.projectId;

			var dataGroupName = OIM.dataGroup.get(projectId);

			var tempDataName = data.data_name || data.nodeId;
			if(tempDataName.indexOf("F4D_") >= 0) {
				tempDataName = tempDataName.replace("F4D_", "");
			}
			$('#dataControlWrap .layerDivTit').text(dataGroupName + ' / ' + tempDataName);
		} else if(selectData instanceof Mago3D.MagoRenderable) {
			$('#dataControlWrap .layerDivTit').text(selectData._guid);
		}
	}
	
	function setHex(selectData) {
		var hex = '#000000';
		if(selectData instanceof Mago3D.Node) {
			var data = selectData.data; 
			if(data.aditionalColor && data.isColorChanged) {
				hex = data.aditionalColor.getHexCode();
			}
		} else if(selectData instanceof Mago3D.MagoRenderable) {
			if(selectData.color4) {
				hex = selectData.color4.getHexCode();
			}
		}
		$('#dcColorPicker').val(hex).change();
	}
	
	function setPositionInfo(selectData) {
		var currentGeoLocData = selectData.getCurrentGeoLocationData();
		
		var positionInfo = that.getPositionInfoFromGeolocationData(currentGeoLocData);
		that.changePositionInfo(positionInfo);
	}
	
	function setHeightInfo(selectData) {
		var heightReference = that.getHeightReferenceFromData();
		that.changeHeightInfo(heightReference);
		
		//높이 참조 셀렉트
		$('#dcHeightReference').val(heightReference);
	}
	
	function setBtn(selectData) {
		$('#dcSavePosRot').show();
		$('#dcShowAttr').show();
		if(selectData instanceof Mago3D.Node) {
			if(selectData.data.attributes.fromSmartTile || selectData.data.attributes.isReference || !OIM.dataGroup.get(selectData.data.projectId)) {
				$('#dcSavePosRot').hide();
				$('#dcShowAttr').hide();
			}
		} else if(selectData instanceof Mago3D.MagoRenderable) {
			$('#dcSavePosRot').hide();
			$('#dcShowAttr').hide();
		}
	}
}
SelectedDataController.prototype.deselectData = function() {
	this.toggleWrap(false);
	this.selected = undefined;
}
SelectedDataController.prototype.changePositionInfo = function(obj) {
	$('#dcLongitude').val(obj.longitude);
	$('#dcLatitude').val(obj.latitude);
	$('#dcAltitude').val(obj.altitude);

	$('#dcPitch,#dcPitchRange').val(obj.pitch);
	$('#dcHeading,#dcHeadingRange').val(obj.heading);
	$('#dcRoll,#dcRollRange').val(obj.roll);
}
SelectedDataController.prototype.changeHeightInfo = function(heightReference) {
	switch(heightReference) {
		case Mago3D.HeightReference.CLAMP_TO_GROUND : {
			$('#dcAltitude').val(0);
			break;
		}
		case Mago3D.HeightReference.RELATIVE_TO_GROUND :
		case Mago3D.HeightReference.NONE : {
			var currentGeoLocData = this.selected.getCurrentGeoLocationData();
			var positionInfo = this.getPositionInfoFromGeolocationData(currentGeoLocData);
			
			var surfaceHeight = (this.selected instanceof Mago3D.Node) ? this.selected.data.surfaceHeight : this.selected.terrainHeight;
			var height = (heightReference === Mago3D.HeightReference.NONE) ? positionInfo.altitude : positionInfo.altitude - surfaceHeight; 
			$('#dcAltitude').val(height);
			break;
		}
	}
}
SelectedDataController.prototype.changeHeightReference = function() {
	var heightReference = $('#dcHeightReference').val();
	
	this.changeHeightInfo(heightReference);
	
	var oldHeightReference = this.getHeightReferenceFromData();
	if(oldHeightReference !== heightReference) {
		this.setHeightReferenceToData(heightReference);
		if(heightReference !== Mago3D.HeightReference.NONE) {
			if(heightReference === Mago3D.HeightReference.RELATIVE_TO_GROUND) {
				if(this.selected instanceof Mago3D.Node) {
					if(this.selected.data.relativeHeight === undefined || this.selected.data.relativeHeight === null) this.selected.data.relativeHeight = 0;
				} else {
					if(this.selected.relativeHeight === undefined || this.selected.relativeHeight === null) this.selected.relativeHeight = 0;
				}
			}
			
			this.addNeedValidHeightData();
		}
	}
}

SelectedDataController.prototype.getHeightReferenceFromData = function() {
	var store = (this.selected instanceof Mago3D.Node) ? this.selected.data.attributes : this.selected.options;
	return store.heightReference || Mago3D.HeightReference.NONE;
}
SelectedDataController.prototype.setHeightReferenceToData = function(heightReference) {
	var store = (this.selected instanceof Mago3D.Node) ? this.selected.data.attributes : this.selected.options;
	store.heightReference = heightReference;
}

SelectedDataController.prototype.addNeedValidHeightData = function() {
	var targetArrayName = (this.selected instanceof Mago3D.Node) ? '_needValidHeightNodeArray' : '_needValidHeightNativeArray';
	
	var targetArray = this.magoInstance.getMagoManager()[targetArrayName];
	if(targetArray.indexOf(this.selected) < 0) {
		targetArray.push(this.selected);
	}
}

SelectedDataController.prototype.changeColor = function() {
	if(this.selected instanceof Mago3D.Node) {
		this.changeF4dColor();
	} else if (this.selected instanceof Mago3D.MagoRenderable) {
		this.changeNativeColor();
	}
}
SelectedDataController.prototype.restoreColor = function() {
	if(this.selected instanceof Mago3D.Node) {
		this.restoreF4dColor();
	} else if (this.selected instanceof Mago3D.MagoRenderable) {
		this.restoreNativeColor();
	}
}

SelectedDataController.prototype.changeF4dColor = function() {
	if(!this.selected || !(this.selected instanceof Mago3D.Node)) {
		alert(JS_MESSAGE["data.select"]);
		return;
	}
	var data = this.selected.data;

	var rgbArray = hex2rgbArray($('#dcColorInput').val());
	changeColorAPI(this.magoInstance, data.projectId, data.nodeId, null, 'isPhysical=true', rgbArray.join(','));
}

SelectedDataController.prototype.changeNativeColor = function() {
	if(!this.selected || !(this.selected instanceof Mago3D.MagoRenderable)) {
		alert(JS_MESSAGE["data.select"]);
		return;
	}
	var rgbArray = hex2rgbArray($('#dcColorInput').val());
	this.selected.setOneColor(rgbArray[0]/255,rgbArray[1]/255,rgbArray[2]/255,1);
}

SelectedDataController.prototype.restoreF4dColor = function() {
	this.selected.deleteChangeColor(this.magoInstance.getMagoManager());
}

SelectedDataController.prototype.restoreNativeColor = function() {
	this.selected.restoreColor();
}

SelectedDataController.prototype.changePosition = function() {
	if(this.selected instanceof Mago3D.Node) {
		this.changeF4dPosition();
	} else if(this.selected instanceof Mago3D.MagoRenderable) {
		this.changeNativePosition();
	}
}

SelectedDataController.prototype.changeF4dPosition = function() {
	var positionInfo = this.getPositionInfoFromElem();

	var height = 0; 
	if($('#dcHeightReference').val() !== Mago3D.HeightReference.RELATIVE_TO_GROUND) {
		height = positionInfo.altitude
	} else {
		this.selected.data.relativeHeight = positionInfo.altitude;
		height = this.selected.data.relativeHeight + this.selected.data.surfaceHeight;
	}
	
	this.selected.changeLocationAndRotation(positionInfo.latitude, positionInfo.longitude, height, positionInfo.heading, positionInfo.pitch, positionInfo.roll, this.magoInstance.getMagoManager());
}

SelectedDataController.prototype.changeNativePosition = function() {
	var positionInfo = this.getPositionInfoFromElem();

	var height = 0; 
	if($('#dcHeightReference').val() === Mago3D.HeightReference.RELATIVE_TO_GROUND) {
		this.selected.relativeHeight = positionInfo.altitude;
		height = this.selected.relativeHeight + this.selected.terrainHeight;
	} else if($('#dcHeightReference').val() === Mago3D.HeightReference.CLAMP_TO_GROUND){
		height = this.selected.getCurrentGeoLocationData().geographicCoord.altitude;
	} else {
		height = positionInfo.altitude
	}
	
	this.selected.changeLocationAndRotation(positionInfo.latitude, positionInfo.longitude, height, positionInfo.heading, positionInfo.pitch, positionInfo.roll);
}

SelectedDataController.prototype.getPositionInfoFromElem = function() {
	return {
		longitude : parseFloat($('#dcLongitude').val()),
		latitude : parseFloat($('#dcLatitude').val()),
		altitude : parseFloat($('#dcAltitude').val()),
		heading : parseInt($('#dcHeading').val()),
		pitch : parseInt($('#dcPitch').val()),
		roll : parseInt($('#dcRoll').val())
	}
}

SelectedDataController.prototype.getPositionInfoFromGeolocationData = function(geoLocData) {
	var geoCoord = geoLocData.geographicCoord;
	
	return {
		longitude : geoCoord.longitude,
		latitude : geoCoord.latitude,
		altitude : geoCoord.altitude,
		heading : geoLocData.heading,
		pitch : geoLocData.pitch,
		roll : geoLocData.roll,
	}
}

SelectedDataController.prototype.toggleWrap = function(show) {
	var isRightMenuButton = false;
	if(show) {
		$('#dataControlWrap').css({
			width : '340px'
		});
		$('#mapSettingWrap').css({
			width :'0px'
		});
		isRightMenuButton = true;
	} else {
		$('#dataControlWrap').css({
			width : '0px'
		});
		if($('#mapCtrlSetting').hasClass('on')) {
			$('#mapSettingWrap').css({
				width : '340px'
			});
			isRightMenuButton = true;
		}
	}
	if(isRightMenuButton) {
		$('#mapCtrlWrap').css({right:'340px'});
		$('#baseMapToggle').css({right:'392px'});
		$('#terrainToggle').css({right:'612px'});
	} else {
		$('#mapCtrlWrap').css({right:'0px'});
		$('#baseMapToggle').css({right:'50px'});
		$('#terrainToggle').css({right:'270px'});
	}
}