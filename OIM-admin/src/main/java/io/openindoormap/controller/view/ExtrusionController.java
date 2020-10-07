package io.openindoormap.controller.view;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.openindoormap.domain.Key;
import io.openindoormap.domain.PageType;
import io.openindoormap.domain.common.Pagination;
import io.openindoormap.domain.data.DataGroup;
import io.openindoormap.domain.data.DataInfoLog;
import io.openindoormap.domain.urban.UrbanGroup;
import io.openindoormap.domain.user.UserSession;
import io.openindoormap.service.DataGroupService;
import io.openindoormap.service.DataLogService;
import io.openindoormap.service.UrbanGroupService;
import io.openindoormap.support.SQLInjectSupport;
import io.openindoormap.utils.DateUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Controller
@RequestMapping("/extrusion")
public class ExtrusionController {

    @Autowired
    private UrbanGroupService urbanGroupService;

    /**
     * extrusion model example
     * @param request
     * @param model
     * @return
     */
    @GetMapping(value = "/list")
    public String list(HttpServletRequest request, Model model) {

        UrbanGroup urbanGroup = UrbanGroup.builder().depth(1).build();
        List<UrbanGroup> oneDepthUrbanGroupList  = urbanGroupService.getListUrbanGroup();

        model.addAttribute("oneDepthUrbanGroupList", oneDepthUrbanGroupList);
        return "/extrusion/list";
    }

    /**
     * 검색 조건
     * @param pageType
     * @param dataInfoLog
     * @return
     */
    private String getSearchParameters(PageType pageType, DataInfoLog dataInfoLog) {
        return dataInfoLog.getParameters();
    }
}