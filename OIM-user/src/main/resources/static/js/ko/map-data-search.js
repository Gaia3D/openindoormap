var pageNo = 1;

$(document).ready(function() {

   // 검색 버튼 클릭
   $("#mapDataSearch").click(function() {
      initSearchForm();
      mapDataSearch(1);
      //mapDataInfoList(1, $("#searchDataName").val(), $("#searchDataGroup").val(), $("#searchDataType").val());
   });

   // 검색 엔터키
   $("#mapDataSearch").keyup(function(e) {
      if (e.keyCode == 13) {
         initSearchForm();
         mapDataSearch(1);
      }
      //if(e.keyCode == 13) mapDataInfoList(1, $("#searchDataName").val(), $("#searchDataGroup").val(), $("#searchDataType").val());
   });

   // 필터 더보기 클릭
   $('#searchFilterMore').click(function() {
      $('#searchFilterContent').toggle();
   });
   $('#searchFilterMore').focusout(function(event) {
      if ($(event.relatedTarget).parents('#searchDataFilterForm').length < 1) {
         $('#searchFilterContent').hide();
      }
   });
   $('#searchDataFilterForm input').click(function(e) {
      e.stopPropagation();
   });


   // 필터 적용
   $('#filterApplyButton').click(function() {
      mapDataSearchFilter(1);
      $('#searchFilterContent').hide();
   });

   // 필터 초기화
   $('#filterInitButton').click(function() {
      $("#searchDataFilterForm input:checkbox").prop('checked', false);
      $("#searchDataFilterForm input:radio").prop('checked', false);
   });

   // 필터 닫기
   $('#filterCloseButton').click(function() {
      $('#searchFilterContent').hide();
   });

   // 검색종류 변경
   $('input[name="searchWord"]').change(function() {
      initSearchForm();
      var searchDataType = $(this).val();
      if (searchDataType === 'data_group_name') {
         $('#searchDataName').attr( 'placeholder', JS_MESSAGE['search.input.data.group.name'] );

         $('#searchDataType').hide();
         $('#searchFilterMore').hide();
         $('#searchDataSharing').show();
      } else if (searchDataType === 'data_address_name') {
         $('#searchDataName').attr( 'placeholder', JS_MESSAGE['search.input.data.address'] );

         $('#searchDataSharing').hide();
         $('#searchDataType').show();
         $('#searchFilterMore').show();
      } else {
         $('#searchDataName').attr( 'placeholder', JS_MESSAGE['search.input.data.name'] );

         // data_name
         $('#searchDataSharing').hide();
         $('#searchDataType').show();
         $('#searchFilterMore').show();
      }
   });

   // 데이터 종류 변경
   $('#searchDataType').change(function() {
      mapDataSearch(1);
   });

   // 공유타입 변경
   $('#searchDataSharing').change(function() {
      mapDataSearch(1);
   });
   
   //상단 라디오 클릭 시 해당 종류로 먼저 검색
   $('#searchDataForm div.searchDataType input[type="radio"]').click(function() {
	   tempMapDataSearch(1, $(this).val());
   })
});

function initSearchForm() {
   $('#filterInitButton').click();
   $('#searchDataType').val("");
   $('#searchDataSharing').val("");
   $('#searchFilterContent').hide();
}

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

function tempMapDataSearch(pageNo, type) {

	   $('#dataInfoContent div').hide();

	   var $form = $("#searchDataForm");
	   var params = getFormData($form);
	   if (type === 'data_group_name') {
	      mapDataGroupList(pageNo, params);
	   } else if (type === 'data_name') {
	      mapDataInfoList(pageNo, params);
	   } else if (params.searchWord === 'data_address_name') {
	      alert('Not implemented yet!');
	   }
	}

function mapDataSearch(pageNo) {

   $('#dataInfoContent div').hide();

   var $form = $("#searchDataForm");
   var params = getFormData($form);
   //var params = $('#searchDataForm').serialize();
   if (params.searchWord === 'data_group_name') {
      mapDataGroupList(pageNo, params);
   } else if (params.searchWord === 'data_name') {
      mapDataInfoList(pageNo, params);
   } else if (params.searchWord === 'data_address_name') {
      alert('Not implemented yet!');
   }
}

function mapDataSearchFilter(pageNo) {

   $('#dataInfoContent div').hide();

   var $form = $("#searchDataForm");
   var searchParams = getFormData($form);

   var $formFilter = $("#searchDataFilterForm");
   var filterParams = getFormData($formFilter);

   var params = Object.assign({}, searchParams, filterParams);

   //var params = $('#searchDataForm').serialize();
   if (params.searchWord === 'data_group_name') {
      mapDataGroupList(pageNo, params);
   } else if (params.searchWord === 'data_name') {
      mapDataInfoList(pageNo, params);
   } else if (params.searchWord === 'data_address_name') {
      alert('Not implemented yet!');
   }
}

function pagingDataInfoList(pageNo, searchParameters) {
   mapDataSearch(pageNo);
}

//데이터 검색
var dataSearchFlag = true;
function mapDataInfoList(pageNo, params) {
   // searchOption : 1 like

   params.pageNo = pageNo;
   params.searchOption = "1";
   var recursiveEncoded = $.param(params);

   //searchDataName
   if(dataSearchFlag) {
      dataSearchFlag = false;
      //var formData =$("#searchDataForm").serialize();

      $.ajax({
         url: "/datas",
         type: "GET",
         data: recursiveEncoded, //{ pageNo : pageNo, searchWord : "data_name", searchValue : searchDataName, searchOption : "1", dataGroupId : searchDataGroupId, dataType : searchDataType},
         dataType: "json",
         headers: {"X-Requested-With": "XMLHttpRequest"},
         success: function(msg){

            if(msg.statusCode <= 200) {

               var dataList = msg.dataList;
               var projectsMap = MAGO3D_INSTANCE.getMagoManager().hierarchyManager.projectsMap;

               if (dataList.length > 0) {
                  for (i in dataList) {
                     var data = dataList[i];
                     var dataId = parseInt(data.dataGroupId);
                     var isVisible = true;
                     if (!$.isEmptyObject(projectsMap)) {
                        var projects = projectsMap[dataId];
                        if ($.isEmptyObject(projects)) {
                           data.groupVisible = isVisible;
                           continue;
                        }
                        if(!projects.attributes) {
                           projects.attributes = {};
                           projects.attributes.objectType = "basicF4d";
                           if(data.tiling) {
                              projects.attributes.fromSmartTile = true;
                           }
                        }

                        var groupVisible = projects.attributes.isVisible;
                        if (groupVisible !== undefined) {
                           isVisible = groupVisible;
                        }
                     }
                     data.groupVisible = isVisible;
                  }
               }
               pageNo = msg.pagination.pageNo;

               //핸들바 템플릿 컴파일
               var template = Handlebars.compile($("#dataListSource").html());
               //핸들바 템플릿에 데이터를 바인딩해서 HTML 생성
               $("#dataListDHTML").html("").append(template(msg));

               //핸들바 템플릿 컴파일
               var templateSearchSummary = Handlebars.compile($("#searchSummarySource").html());
               //핸들바 템플릿에 데이터를 바인딩해서 HTML 생성
               $("#dataSearchSummaryDHTML").html("").append(templateSearchSummary(msg));

               //핸들바 템플릿 컴파일
               var templatePagination = Handlebars.compile($("#paginationSource").html());
               //핸들바 템플릿에 데이터를 바인딩해서 HTML 생성
               $("#dataPaginationDHTML").html("").append(templatePagination(msg));

               $('#dataListDHTML').show();

            } else {
               alert(JS_MESSAGE[msg.errorCode]);
            }
            dataSearchFlag = true;

         },
         error:function(request,status,error){
            alert(JS_MESSAGE["ajax.error.message"]);
            dataSearchFlag = true;
         }
      });
   } else {
      alert(JS_MESSAGE["button.dobule.click"]);
   }
}

//데이터 그룹 검색
var dataGroupSearchFlag = true;
function mapDataGroupList(pageNo, params) {
   // searchOption : 1 like

   params.pageNo = pageNo;
   params.searchOption = "1";
   var recursiveEncoded = $.param(params);

   //searchDataName
   if(dataGroupSearchFlag) {
      dataGroupSearchFlag = false;
      //var formData =$("#searchDataForm").serialize();

      $.ajax({
         url: "/data-groups",
         type: "GET",
         data: recursiveEncoded, //{ pageNo : pageNo, searchWord : "data_group_name", searchValue : searchDataGroupName, searchOption : "1"},
         dataType: "json",
         headers: {"X-Requested-With": "XMLHttpRequest"},
         success: function(msg){

            if(msg.statusCode <= 200) {

               var dataGroupList = msg.dataGroupList;
               var projectsMap = MAGO3D_INSTANCE.getMagoManager().hierarchyManager.projectsMap;

               if (dataGroupList.length > 0) {
                  for (i in dataGroupList) {
                     var dataGroup = dataGroupList[i];
                     var dataGroupId = parseInt(dataGroup.dataGroupId);
                     var isVisible = true;
                     if (!$.isEmptyObject(projectsMap)) {
                        var projects = projectsMap[dataGroupId];
                        if ($.isEmptyObject(projects)) {
                           dataGroup.groupVisible = isVisible;
                           continue;
                        }
                        if (projects.attributes.isVisible) {
                           isVisible = projects.attributes.isVisible;
                        }
                     }
                     dataGroup.groupVisible = isVisible;
                  }
               }

               //핸들바 템플릿 컴파일
               var template = Handlebars.compile($("#dataGroupListSource").html());
               //핸들바 템플릿에 데이터를 바인딩해서 HTML 생성
               $("#dataGroupListDHTML").html("").append(template(msg));

               //핸들바 템플릿 컴파일
               var templateSearchSummary = Handlebars.compile($("#searchSummarySource").html());
               //핸들바 템플릿에 데이터를 바인딩해서 HTML 생성
               $("#dataSearchSummaryDHTML").html("").append(templateSearchSummary(msg));

               //핸들바 템플릿 컴파일
               var templatePagination = Handlebars.compile($("#paginationSource").html());
               //핸들바 템플릿에 데이터를 바인딩해서 HTML 생성
               $("#dataPaginationDHTML").html("").append(templatePagination(msg));

               $('#dataGroupListDHTML').show();

            } else {
               alert(JS_MESSAGE[msg.errorCode]);
            }
            dataGroupSearchFlag = true;

         },
         error:function(request,status,error){
            alert(JS_MESSAGE["ajax.error.message"]);
            dataGroupSearchFlag = true;
         }
      });
   } else {
      alert(JS_MESSAGE["button.dobule.click"]);
   }
}


//데이터 그룹 목록
function dataGroupList() {
   let dataGroupMap = new Map();
   $.ajax({
      url: "/data-groups/all",
      type: "GET",
      headers: {"X-Requested-With": "XMLHttpRequest"},
      dataType: "json",
      success: function(msg){
         if(msg.statusCode <= 200) {
            var dataGroupList = msg.dataGroupList;
            if(dataGroupList !== null && dataGroupList !== undefined) {
               var noneTilingDataGroupList = dataGroupList.filter(function(dataGroup){
                  dataGroupMap.set(dataGroup.dataGroupId, dataGroup.dataGroupName);
                  return !dataGroup.tiling;
               });

               OIM.dataGroup = dataGroupMap;

               dataList(noneTilingDataGroupList);

               var tilingDataGroupList = dataGroupList.filter(function(dataGroup){
                  return dataGroup.tiling;
               });

               var f4dController = MAGO3D_INSTANCE.getF4dController();
               for(var i in tilingDataGroupList)
               {
                  var tilingDataGroup = tilingDataGroupList[i];
                  if(i == tilingDataGroupList.length-1) {
                     tilingDataGroup.smartTileIndexPath = 'infra/_TILE';
                  }
                  f4dController.addSmartTileGroup(tilingDataGroup);
               }
            }
         } else {
            alert(JS_MESSAGE[msg.errorCode]);
         }
      },
      error:function(request,status,error){
         alert(JS_MESSAGE["ajax.error.message"]);
      }
   });
}

// 데이터 정보 목록
function dataList(dataGroupArray) {
   var dataArray = new Array();
   var dataGroupArrayLength = dataGroupArray.length;
   for(var i=0; i<dataGroupArrayLength; i++) {
      var dataGroup = dataGroupArray[i];
      if(!dataGroup.tiling) {
         var f4dController = MAGO3D_INSTANCE.getF4dController();
         $.ajax({
            url: "/datas/" + dataGroup.dataGroupId + "/list",
            type: "GET",
            headers: {"X-Requested-With": "XMLHttpRequest"},
            dataType: "json",
            success: function(msg){
               if(msg.statusCode <= 200) {
                  var dataInfoList = msg.dataInfoList;
                  if(dataInfoList != null && dataInfoList.length > 0) {
                     var dataInfoFirst = dataInfoList[0];
                     var dataInfoGroupId = dataInfoFirst.dataGroupId;
                     var group;
                     for(var j in dataGroupArray) {
                        if(dataGroupArray[j].dataGroupId === dataInfoGroupId) {
                           group = dataGroupArray[j];
                           break;
                        }
                     }

                     group.datas = dataInfoList;
                     f4dController.addF4dGroup(group);
                  }
               } else {
                  alert(JS_MESSAGE[msg.errorCode]);
               }
            },
            error:function(request,status,error){
               alert(JS_MESSAGE["ajax.error.message"]);
            }
         });
      }
   }
}