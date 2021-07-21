//펼치기
function openAll() {
    $(".rowDepth-5").show();
    $(".rowDepth-4").show();
    $(".rowDepth-3").show();
    $(".rowDepth-2").show();

    // fa-caret-right
    // fa-caret-down
    $(".depthArrow-1").removeClass("fa-caret-right");
    $(".depthArrow-1").addClass("fa-caret-down");
    $(".depthArrow-2").removeClass("fa-caret-right");
    $(".depthArrow-2").addClass("fa-caret-down");
    $(".depthArrow-3").removeClass("fa-caret-right");
    $(".depthArrow-3").addClass("fa-caret-down");
    $(".depthArrow-4").removeClass("fa-caret-right");
    $(".depthArrow-4").addClass("fa-caret-down");
    $(".depthArrow-5").removeClass("fa-caret-right");
    $(".depthArrow-5").addClass("fa-caret-down");

    $(".depthFolder-1").removeClass("fa-folder");
    $(".depthFolder-1").addClass("fa-folder-open");
    $(".depthFolder-2").removeClass("fa-folder");
    $(".depthFolder-2").addClass("fa-folder-open");
    $(".depthFolder-3").removeClass("fa-folder");
    $(".depthFolder-3").addClass("fa-folder-open");
    $(".depthFolder-4").removeClass("fa-folder");
    $(".depthFolder-4").addClass("fa-folder-open");
    $(".depthFolder-5").removeClass("fa-folder");
    $(".depthFolder-5").addClass("fa-folder-open");
}

// 접기
function closeAll() {
    $(".rowDepth-5").hide();
    $(".rowDepth-4").hide();
    $(".rowDepth-3").hide();
    $(".rowDepth-2").hide();

    $(".depthArrow-1").removeClass("fa-caret-down");
    $(".depthArrow-1").addClass("fa-caret-right");
    $(".depthArrow-2").removeClass("fa-caret-down");
    $(".depthArrow-2").addClass("fa-caret-right");
    $(".depthArrow-3").removeClass("fa-caret-down");
    $(".depthArrow-3").addClass("fa-caret-right");
    $(".depthArrow-4").removeClass("fa-caret-down");
    $(".depthArrow-4").addClass("fa-caret-right");
    $(".depthArrow-5").removeClass("fa-caret-down");
    $(".depthArrow-5").addClass("fa-caret-right");

    $(".depthFolder-1").removeClass("fa-folder-open");
    $(".depthFolder-1").addClass("fa-folder");
    $(".depthFolder-2").removeClass("fa-folder-open");
    $(".depthFolder-2").addClass("fa-folder");
    $(".depthFolder-3").removeClass("fa-folder-open");
    $(".depthFolder-3").addClass("fa-folder");
    $(".depthFolder-4").removeClass("fa-folder-open");
    $(".depthFolder-4").addClass("fa-folder");
    $(".depthFolder-5").removeClass("fa-folder-open");
    $(".depthFolder-5").addClass("fa-folder");
}

// 화살표 클릭시
function childrenDisplayToggle(id, depth, status, originDepth) {
    if(originDepth === undefined) {
        originDepth = depth;
    } else {
        if(depth > originDepth ) {
            status = "close";
        }
    }

    var clickClass = $("#myArrow-" + id).attr("class");
    if(status === "open") {
        // 펼침
        $("#myArrow-" + id).removeClass("fa-caret-right");
        $("#myArrow-" + id).addClass("fa-caret-down");
        $("#myFolder-" + id).removeClass("fa-folder");
        $("#myFolder-" + id).addClass("fa-folder-open");
    } else if(status === "close") {
        // 닫힘
        $("#myArrow-" + id).removeClass("fa-caret-down");
        $("#myArrow-" + id).addClass("fa-caret-right");
        $("#myFolder-" + id).removeClass("fa-folder-open");
        $("#myFolder-" + id).addClass("fa-folder");
    } else {
        if (clickClass.indexOf("right") >= 0) {
            status = "open";
            // 닫힘 상태라 펼침
            $("#myArrow-" + id).removeClass("fa-caret-right");
            $("#myArrow-" + id).addClass("fa-caret-down");
            $("#myFolder-" + id).removeClass("fa-folder");
            $("#myFolder-" + id).addClass("fa-folder-open");
        } else {
            status = "close";
            // 펼침 상태라 닫힘
            $("#myArrow-" + id).removeClass("fa-caret-down");
            $("#myArrow-" + id).addClass("fa-caret-right");
            $("#myFolder-" + id).removeClass("fa-folder-open");
            $("#myFolder-" + id).addClass("fa-folder");
        }
    }

    if(depth === 5) return;

    $(".rowParent-" + id).each(function() {
        var tempClass = $(this).attr("class");
        console.log("----------- tempClass=" + tempClass);
        var nextClassIndex = tempClass.indexOf("rowDepth");
        var idLength = nextClassIndex - 6 - 1;

        var childId = tempClass.substr(6, idLength);
        if(status === "open") {
            // 자식을 표시
            $(".rowId-" + childId).show();
        } else {
            // 자식을 오픈한다.
            $(".rowId-" + childId).hide();
        }
        childrenDisplayToggle(childId, depth + 1, status, originDepth);
    });
}