package io.openindoormap.controller.view;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;
import io.openindoormap.domain.policy.Policy;
import io.openindoormap.domain.widget.Widget;
import io.openindoormap.service.PolicyService;
import io.openindoormap.service.WidgetService;
import io.openindoormap.utils.DateUtils;
import io.openindoormap.utils.FormatUtils;


/**
 * 메인
 * @author jeongdae
 *
 */
@Slf4j
@Controller
@RequestMapping("/main")
public class MainController {

    @Autowired
    private PolicyService policyService;
    @Autowired
    private WidgetService widgetService;

    /**
     * 메인 페이지
     * @param model
     * @return
     */
    @GetMapping(value = "/index")
    public String index(HttpServletRequest request, Model model) {
        Policy policy = policyService.getPolicy();
        boolean autoRefresh = true;

        Widget widget = new Widget();
        widget.setLimit(policy.getContentMainWidgetCount());
        List<Widget> widgetList = widgetService.getListWidget(widget);

        String today = DateUtils.getToday(FormatUtils.VIEW_YEAR_MONTH_DAY_TIME);
        String yearMonthDay = today.substring(0, 4) + today.substring(5,7) + today.substring(8,10);

        // 메인 페이지 자동 갱신 여부
        model.addAttribute("autoRefresh", autoRefresh);
        // 메인 페이지 갱신 속도
        model.addAttribute("widgetInterval", policy.getContentMainWidgetInterval());

        model.addAttribute("today", today);
        model.addAttribute("yearMonthDay", today.subSequence(0, 10));
        model.addAttribute("thisYear", yearMonthDay.subSequence(0, 4));

        model.addAttribute(widget);
        model.addAttribute("widgetList", widgetList);

        model.addAttribute("converterTotalCount", 0l);
        model.addAttribute("converterSuccessCount", 0l);
        model.addAttribute("converterFailCount", 0l);

        return "/main/index";
    }
}
