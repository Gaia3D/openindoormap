package io.openindoormap.controller.view;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.openindoormap.domain.statistics.StatisticsForYear;
import io.openindoormap.service.StatisticsService;

@RequestMapping("/statistics")
@Controller
public class StatisticsController {

    @Autowired
    StatisticsService service;

    @GetMapping(value = "/threedimension")
    public String threedimension(HttpServletRequest request, Integer from, Integer to, Model model) {
        List<StatisticsForYear> l;
        if (from != null && to != null && from <= to) {
            int count = (to - from + 1);
            StatisticsForYear year = StatisticsForYear.builder().year(String.valueOf(from)).count(count * 12).build();

            l = service.getStatisticsDataInfo(year);
        } else {
            l = new ArrayList<>();
        }
        model.addAttribute("list", l);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "/statistics/threedimension";
    }

    @GetMapping(value = "/userdata")
    public String userdata(HttpServletRequest request, Integer from, Integer to, Model model) {
        List<StatisticsForYear> l;
        if (from != null && to != null && from <= to) {
            int count = (to - from + 1);
            StatisticsForYear year = StatisticsForYear.builder().year(String.valueOf(from)).count(count * 12).build();
            l = service.getStatisticsUploadData(year);
        } else {
            l = new ArrayList<>();
        }
        model.addAttribute("list", l);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "/statistics/threedimension";
    }

    @GetMapping(value = "/transformation")
    public String transformation(HttpServletRequest request, Integer from, Integer to, Model model) {
        List<StatisticsForYear> l;
        if (from != null && to != null && from <= to) {
            int count = (to - from + 1);
            StatisticsForYear year = StatisticsForYear.builder().year(String.valueOf(from)).count(count * 12).build();
            l = service.getStatisticsConverter(year);
        } else {
            l = new ArrayList<>();
        }
        model.addAttribute("list", l);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "/statistics/threedimension";
    }

    @GetMapping(value = "/access")
    public String accessLogs(HttpServletRequest request, Integer from, Integer to, Model model) {
        List<StatisticsForYear> l;
        if (from != null && to != null && from <= to) {
            int count = (to - from + 1);
            StatisticsForYear year = StatisticsForYear.builder().year(String.valueOf(from)).count(count * 12).build();

            l = service.getStatisticsAccess(year);
        } else {
            l = new ArrayList<>();
        }
        model.addAttribute("list", l);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "/statistics/threedimension";
    }

}
