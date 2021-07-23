package io.openindoormap.controller.view;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.openindoormap.domain.PageType;
import io.openindoormap.domain.accesslog.AccessLog;
import io.openindoormap.domain.common.Pagination;
import io.openindoormap.domain.user.UserInfo;

@RequestMapping("/statistics")
@Controller
public class StatisticsController {


    /**
     *
     * @param request
     * @param pageNo
     * @param accessLog
     * @param model
     * @return
     */
    @GetMapping(value = "/threedimension")
    public String list(HttpServletRequest request, @RequestParam(defaultValue="1") String pageNo, UserInfo userInfo, Model model) {
        List<UserInfo> userList = new ArrayList<>();
        long totalCount = 0;
        Pagination pagination = new Pagination(request.getRequestURI(), getSearchParameters(PageType.LIST, userInfo),
                totalCount, Long.parseLong(pageNo), userInfo.getListCounter());
        userInfo.setOffset(pagination.getOffset());
        userInfo.setLimit(pagination.getPageRows());
        model.addAttribute(pagination);
        model.addAttribute("userList", userList);
        return "/statistics/threedimension";
    }

    /**
     * 검색 조건
     * @param userInfo
     * @return
     */
    private String getSearchParameters(PageType pageType, UserInfo userInfo) {
        StringBuffer buffer = new StringBuffer(userInfo.getParameters());
        boolean isListPage = true;
        if(pageType == PageType.MODIFY || pageType == PageType.DETAIL) {
            isListPage = false;
        }

        return buffer.toString();
    }


}
