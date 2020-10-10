package io.openindoormap.controller.rest;

import io.openindoormap.domain.PageType;
import io.openindoormap.domain.data.DataInfoLog;
import io.openindoormap.domain.extrusionmodel.DataLibrary;
import io.openindoormap.domain.extrusionmodel.DataLibraryGroup;
import io.openindoormap.domain.extrusionmodel.DesignLayer;
import io.openindoormap.domain.layer.LayerGroup;
import io.openindoormap.domain.urban.UrbanGroup;
import io.openindoormap.service.DataLibraryGroupService;
import io.openindoormap.service.DataLibraryService;
import io.openindoormap.service.DesignLayerService;
import io.openindoormap.service.UrbanGroupService;
import io.openindoormap.support.LayerDisplaySupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * extrusion model 예제
 */
@Slf4j
@RestController
@RequestMapping("/extrusions")
public class ExtrusionRestController {

    @Autowired
    private DataLibraryService dataLibraryService;
    @Autowired
    private DataLibraryGroupService dataLibraryGroupService;
    @Autowired
    private DesignLayerService designLayerService;
    @Autowired
    private UrbanGroupService urbanGroupService;

    /**
     * 도시 그룹
     * @param request
     * @param model
     * @return
     */
    @GetMapping
    public Map<String, Object> list(HttpServletRequest request, Model model) {

        Map<String, Object> result = new HashMap<>();
        String errorCode = null;
        String message = null;

        List<UrbanGroup> oneDepthUrbanGroupList = urbanGroupService.getListUrbanGroupByDepth(1);
        List<UrbanGroup> twoDepthUrbanGroupList = new ArrayList<>();
        if(!oneDepthUrbanGroupList.isEmpty()) twoDepthUrbanGroupList = urbanGroupService.getListUrbanGroupByParent(oneDepthUrbanGroupList.get(0).getUrbanGroupId());

        int statusCode = HttpStatus.OK.value();

        result.put("oneDepthUrbanGroupList", oneDepthUrbanGroupList);
        result.put("twoDepthUrbanGroupList", twoDepthUrbanGroupList);
        result.put("statusCode", statusCode);
        result.put("errorCode", errorCode);
        result.put("message", message);

        return result;
    }

    /**
     * 도시 그룹에 등록된 디자인 레이어 목록
     * @param request
     * @param model
     * @return
     */
    @GetMapping("/{urbanGroupId}/design-layers")
    public Map<String, Object> designLayerList(HttpServletRequest request, @PathVariable Integer urbanGroupId, Model model) {

        Map<String, Object> result = new HashMap<>();
        String errorCode = null;
        String message = null;

        DesignLayer designLayer = DesignLayer.builder().urbanGroupId(urbanGroupId).build();
        List<DesignLayer> designLayerList = designLayerService.getListDesignLayer(designLayer);

        int statusCode = HttpStatus.OK.value();

        result.put("designLayerList", designLayerList);
        result.put("statusCode", statusCode);
        result.put("errorCode", errorCode);
        result.put("message", message);

        return result;
    }

//    /**
//     * 모든 데이터 라이브러리
//     * @param request
//     * @param model
//     * @return
//     */
//    @GetMapping("/data-library-groups")
//    public Map<String, Object> dataLibraryGroup(HttpServletRequest request, Model model) {
//
//        Map<String, Object> result = new HashMap<>();
//        String errorCode = null;
//        String message = null;
//
//        List<DataLibrary> dataLibraryList = dataLibraryService.getListDataLibrary(DataLibrary.builder().build());
//
//        int statusCode = HttpStatus.OK.value();
//
//        result.put("dataLibraryList", dataLibraryList);
//        result.put("statusCode", statusCode);
//        result.put("errorCode", errorCode);
//        result.put("message", message);
//
//        return result;
//    }

    /**
     * 모든 데이터 라이브러리 그룹별 데이터 라이브 러리
     * @param request
     * @param model
     * @return
     */
    @GetMapping("/data-library-groups")
    public Map<String, Object> dataLibraryGroup(HttpServletRequest request, Model model) {

        Map<String, Object> result = new HashMap<>();
        String errorCode = null;
        String message = null;

        List<DataLibraryGroup> dataLibraryGroupList = dataLibraryGroupService.getListDataLibraryGroupAndDataLibrary();

        int statusCode = HttpStatus.OK.value();

        result.put("dataLibraryGroupList", dataLibraryGroupList);
        result.put("statusCode", statusCode);
        result.put("errorCode", errorCode);
        result.put("message", message);

        return result;
    }
}