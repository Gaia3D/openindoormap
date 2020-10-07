package io.openindoormap.controller.view;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.openindoormap.domain.layer.LayerGroup;
import io.openindoormap.domain.policy.Policy;
import io.openindoormap.domain.urban.UrbanGroup;
import io.openindoormap.service.PolicyService;
import io.openindoormap.service.UrbanGroupService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/urban-group")
public class UrbanGroupController {

	@Autowired
	private UrbanGroupService urbanGroupService;

	@Autowired
	private PolicyService policyService;

	/**
	 * 도시 그룹 목록
	 * @param request
	 * @param urbanGroup
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/list")
	public String list(HttpServletRequest request, @ModelAttribute UrbanGroup urbanGroup, Model model) {
		List<UrbanGroup> urbanGroupList = urbanGroupService.getListUrbanGroup();

		model.addAttribute("urbanGroupList", urbanGroupList);

		return "/urban-group/list";
	}

	/**
	 * 도시 그룹 등록 페이지 이동
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/input")
	public String input(Model model) {
		Policy policy = policyService.getPolicy();

		List<UrbanGroup> urbanGroupList = urbanGroupService.getListUrbanGroup();

		UrbanGroup urbanGroup = new UrbanGroup();
		urbanGroup.setParentName(policy.getContentUrbanGroupRoot());
		urbanGroup.setParent(0);

		model.addAttribute("policy", policy);
		model.addAttribute("urbanGroup", urbanGroup);
		model.addAttribute("urbanGroupList", urbanGroupList);

		return "/urban-group/input";
	}

	/**
	 * 도시 그룹 수정 페이지 이동
	 * @param request
	 * @param urbanGroupId
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/modify")
	public String modify(HttpServletRequest request, @RequestParam Integer urbanGroupId, Model model) {
		UrbanGroup urbanGroup = new UrbanGroup();
		urbanGroup.setUrbanGroupId(urbanGroupId);
		urbanGroup = urbanGroupService.getUrbanGroup(urbanGroup);
		Policy policy = policyService.getPolicy();
		List<UrbanGroup> urbanGroupList = urbanGroupService.getListUrbanGroup();

		if(urbanGroup.getParent() == 0) {
			urbanGroup.setParentName(policy.getContentUrbanGroupRoot());
		}

		model.addAttribute("policy", policy);
		model.addAttribute("urbanGroup", urbanGroup);
		model.addAttribute("urbanGroupList", urbanGroupList);

		return "/urban-group/modify";
	}

	/**
	 * 도시 그룹 삭제
	 * @param urbanGroupId
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/delete")
	public String delete(@RequestParam("urbanGroupId") Integer urbanGroupId, Model model) {
		// TODO validation 체크 해야 함
		UrbanGroup urbanGroup = new UrbanGroup();
		urbanGroup.setUrbanGroupId(urbanGroupId);

		urbanGroupService.deleteUrbanGroup(urbanGroup);

		return "redirect:/urban-group/list";
	}
}
