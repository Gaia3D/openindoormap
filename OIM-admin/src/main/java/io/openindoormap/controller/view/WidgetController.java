package io.openindoormap.controller.view;

import javax.servlet.http.HttpServletRequest;

import io.openindoormap.domain.widget.Widget;
import io.openindoormap.service.WidgetService;
import io.openindoormap.utils.DateUtils;
import io.openindoormap.utils.FormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/widget")
public class WidgetController {

    @Autowired
    private WidgetService widgetService;

    @GetMapping(value = "/modify")
    public String modify(HttpServletRequest reuqet, Model model) {

        Widget widget = new Widget();
        List<Widget> widgetList = widgetService.getListWidget(widget);

        String today = DateUtils.getToday(FormatUtils.VIEW_YEAR_MONTH_DAY_TIME);
        String yearMonthDay = today.substring(0, 4) + today.substring(5,7) + today.substring(8,10);

        model.addAttribute("today", today);
        model.addAttribute("yearMonthDay", today.subSequence(0, 10));
        model.addAttribute("thisYear", yearMonthDay.subSequence(0, 4));

        model.addAttribute(widget);
        model.addAttribute(widgetList);
        StringBuffer sb = new StringBuffer();
        for (Widget widget2 : widgetList) {
            sb.append("<div th:replace='~{")
               .append(widget2.getWidgetKey())
               .append(":: #")
               .append(widget2.getWidgetKey())
               .append(" '></div> \n");
        }
        model.addAttribute("widgets", sb.toString());

        // | <div th:replace='~{__${dbWidget.widgetKey}__ :: #__${dbWidget.widgetKey}__'></div>|

        return "/widget/modify";
    }
}
