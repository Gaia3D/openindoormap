package io.openindoormap.controller.view;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.openindoormap.domain.extrusionmodel.DesignLayerGroup;
import io.openindoormap.domain.policy.Policy;
import io.openindoormap.service.DesignLayerGroupService;
import io.openindoormap.service.PolicyService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Design Layer 그룹 관리
 */
@Slf4j
@Controller
@RequestMapping("/design-layer-group")
public class DesignLayerGroupController {

	@Autowired
	private DesignLayerGroupService designLayerGroupService;

	@Autowired
	private PolicyService policyService;

	/**
	 * Design Layer 그룹 목록
	 * @param request
	 * @param designLayerGroup
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/list")
	public String list(HttpServletRequest request, @ModelAttribute DesignLayerGroup designLayerGroup, Model model) {
		List<DesignLayerGroup> designLayerGroupList = designLayerGroupService.getListDesignLayerGroup();

		model.addAttribute("designLayerGroupList", designLayerGroupList);

		return "/design-layer-group/list";
	}

	/**
	 * Design Layer 그룹 등록 페이지 이동
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/input")
	public String input(Model model) {
		Policy policy = policyService.getPolicy();

		List<DesignLayerGroup> designLayerGroupList = designLayerGroupService.getListDesignLayerGroup();

        DesignLayerGroup designLayerGroup = new DesignLayerGroup();
		designLayerGroup.setParentName(policy.getContentDesignLayerGroupRoot());
		designLayerGroup.setParent(0);

		model.addAttribute("policy", policy);
		model.addAttribute("designLayerGroup", designLayerGroup);
		model.addAttribute("designLayerGroupList", designLayerGroupList);

		return "/design-layer-group/input";
	}

	/**
	 * Design Layer 그룹 수정 페이지 이동
	 * @param request
	 * @param designLayerGroupId
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/modify")
	public String modify(HttpServletRequest request, @RequestParam Integer designLayerGroupId, Model model) {
		DesignLayerGroup designLayerGroup = new DesignLayerGroup();
		designLayerGroup.setDesignLayerGroupId(designLayerGroupId);
		designLayerGroup = designLayerGroupService.getDesignLayerGroup(designLayerGroup);
		Policy policy = policyService.getPolicy();
		List<DesignLayerGroup> designLayerGroupList = designLayerGroupService.getListDesignLayerGroup();

		if(designLayerGroup.getParent() == 0) {
			designLayerGroup.setParentName(policy.getContentDesignLayerGroupRoot());
		}

		model.addAttribute("policy", policy);
		model.addAttribute("designLayerGroup", designLayerGroup);
		model.addAttribute("designLayerGroupList", designLayerGroupList);

		return "/design-layer-group/modify";
	}

	/**
	 * Design Layer 그룹 삭제
	 * @param designLayerGroupId
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/delete")
	public String delete(@RequestParam("designLayerGroupId") Integer designLayerGroupId, Model model) {
		// TODO validation 체크 해야 함
		DesignLayerGroup designLayerGroup = new DesignLayerGroup();
		designLayerGroup.setDesignLayerGroupId(designLayerGroupId);

		designLayerGroupService.deleteDesignLayerGroup(designLayerGroup);

		return "redirect:/design-layer-group/list";
	}
}
