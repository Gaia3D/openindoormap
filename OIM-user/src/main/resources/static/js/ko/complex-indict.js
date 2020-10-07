/**
 * 단지 가시화
 * @since 20200921 init
 */
const ComplexIndictObj = function(){
	//
	this.tool = ComplexIndictObj.ToolType.NONE;
};


ComplexIndictObj.ToolType = {
	NONE: {value:0, name:'none'},
	SELECT: {value:1, name:'select'},
	DELETE: {value:2, name:'delete'},
	MOVE: {value:3, name:'move'},
	RORATE: {value:4, name:'rotate'},
	UPDOWN: {value:5, name:'updown'},
};

ComplexIndictObj.prototype.getTool = function(){
	return this.tool;
};

ComplexIndictObj.prototype.getToolName = function(tool){
	if(tool){
		return tool.name;		
	}else{
		return this.tool.name;
	}
};

ComplexIndictObj.prototype.setTool = function(tool){
	let beforeTool = this.tool;
	
	
	this.tool = tool;
	
	this.toolChanged(beforeTool, tool);
};

ComplexIndictObj.prototype.currentToolIs = function(tool){
	return this.tool == tool;
};


ComplexIndictObj.prototype.toolChanged = function(beforeTool, afterTool){
	
};



ComplexIndictObj.prototype.init = function(){
	//신도시 데이터 로드
	
	
	this.setEventHandler();
};


ComplexIndictObj.prototype.setEventHandler = function(){
	let self = this;
	
	
	/**
	 * 신도시 select change
	 */
	$('select.urban-group1').change(function(){
		//지역 데이터 로드	
	});
	
	
	/**
	 * 지역 select change
	 */
	$('select.urban-group2').change(function(){
		//레이어 목록 로드
	});
	
	
	/**
	 * 레이어 on/off
	 */
	$('tr.layer').click(function(){
		
	});
	
	
	/**
	 * 레이어의 높이 on/off
	 */
	$('tr.layer.updown').click(function(){
		
	});
	
	/**
	 * 도구 - 선택 on/off
	 */
	$('button.tool-select').click(function(){
		//setTool
	});
	
	
	/**
	 * 도구 - 삭제 on/off
	 */
	$('button.tool-delete').click(function(){
		
	});
	
	
	/**
	 * 도구 - 이동 on/off
	 */
	$('button.tool-move').click(function(){
		
	});
	
	
	/**
	 * 도구 - 회전 on/off
	 */
	$('button.tool-rotate').click(function(){
		
	});
	
	
	/**
	 * 도구 - 필지높이 on/off
	 */
	$('button.tool-updown').click(function(){
		
	});


	/**
	 * 건물 높이 변경
	 */
	$('select.building-updown').click(function(){
		
	});
	
	
	/**
	 * 필지내 전체 건물 높이 변경
	 */
	$('select.all-building-updown').click(function(){
		
	});

};



/**
 *
 */
ComplexIndictObj.prototype.processToolNone = function(){
	
};


/**
 *
 */
ComplexIndictObj.prototype.processToolSelect = function(){
	
};


/**
 *
 */
ComplexIndictObj.prototype.processToolDelete = function(){
	
};


/**
 *
 */
ComplexIndictObj.prototype.processToolMove = function(){
	
};


/**
 *
 */
ComplexIndictObj.prototype.processToolRotate = function(){
	
};


/**
 *
 */
ComplexIndictObj.prototype.processToolUpDown = function(){
	
};

/**
 * 레이어 목록 표시
 */



/**
 * 정보 표시 - 지역
 */

/**
 * 정보 표시 - 필지
 */

/**
 * 정보 표시 - 건물
 */


/**
 * extrusion model 선택 callback
 */
ComplexIndictObj.prototype.selectedGeneralObjectCallback = function(e){
	
};


/**
 * extrusion model 선택해제 callback
 */
ComplexIndictObj.prototype.deselectedGeneralObjectCallback = function(e){
	
};


/**
 * leftup callback
 */
ComplexIndictObj.prototype.leftupCallback = function(e){
	
};

