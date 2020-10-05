package io.openindoormap.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import io.openindoormap.domain.CacheManager;
import io.openindoormap.domain.DataInfo;
import io.openindoormap.domain.DataSharingType;
import io.openindoormap.domain.Policy;
import io.openindoormap.domain.Project;
import io.openindoormap.domain.ProjectDataJson;
import io.openindoormap.domain.UserPolicy;
import io.openindoormap.service.DataService;
import io.openindoormap.service.ProjectService;
import io.openindoormap.service.UserPolicyService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/map")
public class MapController {

    @Autowired
    private DataService dataService;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private UserPolicyService userPolicyService;
    
    @RequestMapping("/")
    public String mapProject(Model model) throws Exception {
        // Policy policy = CacheManager.getPolicy();
        Policy policy = new Policy();
        policy.setGeo_view_library("cesium");

        // Map<Integer, String> initProjectJsonMap = new HashMap<>();
        
        String userId = "guest";
        UserPolicy userPolicy = userPolicyService.getUserPolicy(userId);
        //
        Map<Integer, List<DataInfo>> projectDataMap = null;
        Map<Integer, String> initProjectJsonMap = null;
        
        // 최초 로딩시
        projectDataMap = new HashMap<>();
        initProjectJsonMap = new HashMap<>();
        
        int initProjectsLength = 0;
        Project commonProject = new Project();
        commonProject.setSharing_type(DataSharingType.PUBLIC.getValue());
        List<Project> projectList = projectService.getListProject(commonProject);
        for(Project project : projectList) {
            DataInfo dataInfo = new DataInfo();
            dataInfo.setProject_id(project.getProject_id());
            List<DataInfo> dataInfoList = dataService.getListDataByProjectId(dataInfo);
            log.info("++++++++++++ Project_id = {},  dataInfoList = {}", project.getProject_id(), dataInfoList);
            projectDataMap.put(project.getProject_id(), dataInfoList);
            log.info("++++++++++++ json = {}", ProjectDataJson.getProjectDataJson(log, project.getProject_id(), dataInfoList));
            initProjectJsonMap.put(project.getProject_id(), ProjectDataJson.getProjectDataJson(log, project.getProject_id(), dataInfoList));
            initProjectsLength++;
        }
            
        // Project project = new Project();
        // project.setUser_id(userId);
        // project = projectService.getProject(project);
        
        // DataInfo dataInfo = new DataInfo();
        // dataInfo.setProject_id(project.getProject_id());
        // List<DataInfo> dataInfoList = dataService.getListDataByProjectId(dataInfo);
        
        // int initProjectsLength = 0;
        // if(!dataInfoList.isEmpty()) {
        //     initProjectJsonMap.put( project.getProject_id(), ProjectDataJson.getProjectDataJson(log, project.getProject_id(), dataInfoList));
        //     initProjectsLength++;
        // }

        ObjectMapper mapper = new ObjectMapper();
        
        model.addAttribute("userPolicy", userPolicy);
		model.addAttribute("geoViewLibrary", policy.getGeo_view_library());
		model.addAttribute("now_latitude", policy.getGeo_init_latitude());
		model.addAttribute("now_longitude", policy.getGeo_init_longitude());
		model.addAttribute("commonProjectList", CacheManager.getProjectList());
		model.addAttribute("initProjectsLength", initProjectsLength);
		model.addAttribute("initProjectJsonMap", mapper.writeValueAsString(initProjectJsonMap));
		model.addAttribute("cache_version", policy.getContent_cache_version());
		model.addAttribute("policyJson", mapper.writeValueAsString(userPolicy));
		
		log.info("@@@@@@ policy = {}", policy);
		log.info("@@@@@@ initProjectsLength = {}", initProjectsLength);
        log.info("@@@@@@ initProjectJsonMap = {}", mapper.writeValueAsString(initProjectJsonMap));
        
        return "map/index";
    }

}