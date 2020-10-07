var LowInfoAnals = function(magoInstance) {
    var magoManager = magoInstance.getMagoManager();

    magoManager.on(Mago3D.MagoManager.EVENT_TYPE.SMARTTILELOADEND, smartTileLoaEndCallbak);
    magoManager.on(Mago3D.MagoManager.EVENT_TYPE.F4DLOADEND, echoLoadEndCallback);

    // var observer;
    //
    // var datepicker = new tui.DatePicker('#solayDatePicker', {
    //     date: new Date(),
    //     input: {
    //         element: '#datepicker-input',
    //         format: 'yyyy-MM-dd'
    //     }
    // });
    //
    // // 일조분석 리포트 다이얼 로그
    // var simulSolarDialog = $( "#simulSolarDialog" ).dialog({
    //     autoOpen: false,
    //     width: 500,
    //     height: 240,
    //     modal: true,
    //     overflow : "auto",
    //     resizable: false
    // });
    //
    // var solarDefaultTime = [12,15,12,9];
    //
    // var timeSlider;
    // var solarMode = false;
    //
    // $('#solarAnalysis .execute').click(function() {
    //     if(!timeSlider) {
    //         timeSlider = new KotSlider('timeInput');
    //         timeSlider.setMin(1);
    //         timeSlider.setMax(24);
    //         timeSlider.setDuration(200);
    //         var html = '';
    //
    //         for(var i=1;i<25;i++){
    //             if(i === 1 || 1 === 10) {
    //                 html += '<span style="margin-left:22px;">' + i + '</span>';
    //             } else if(i < 10) {
    //                 html += '<span style="margin-left:27px;">' + i + '</span>';
    //             } else {
    //                 html += '<span style="margin-left:19px;">' + i + '</span>';
    //             }
    //         }
    //
    //         $('#saRange .rangeWrapChild.legend').html(html);
    //         $('#saRange .rangeWrapChild.legend').on('click','span',function(){
    //             timeSlider.setValue(parseInt($(this).index())+1);
    //         });
    //
    //         var currentHour = new Date().getHours();
    //         currentHour  = currentHour === 0 ? 24 : currentHour;
    //         timeSlider.setValue(currentHour);
    //
    //         //레인지 보이기
    //         $('#saRange').show();
    //         $('#csRange').hide();
    //
    //         magoInstance.getViewer().scene.globe.enableLighting = true;
    //         magoManager.sceneState.setApplySunShadows(true);
    //
    //         $('#shadowDisplayY').prop('checked',true);
    //         $reportBtn.show();
    //         solarMode = true;
    //
    //         changeDateTime();
    //     }
    // });
    //
    // $('#solarAnalysis .drawObserverPoint').click(function() {
    //     var $this = $(this);
    //     if(!solarMode) {
    //         alert(JS_MESSAGE["simulation.analysis.start"]);
    //         return;
    //     }
    //     magoManager.once(Mago3D.Mago3D.MagoManager.EVENT_TYPE.CLICK, function(e) {
    //         deleteSolarMark();
    //     });
    // });
    //
    // var delteSolarMark = function() {
    //     var filtered = magoManager.objMarkerManager.objectMarkerArray.filter(function(om){
    //         return !om.solarAnalysis;
    //     });
    //     magoManager.objMarkerManager.objectMarkerArray = filtered;
    // }
};