package io.openindoormap.service.impl;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.openindoormap.domain.issue.Issue;
import io.openindoormap.persistence.IssueMapper;
import io.openindoormap.service.IssueService;

/**
 * issue
 * @author jeongdae
 *
 */
@RequiredArgsConstructor
@Service
public class IssueServiceImpl implements IssueService {

	private final IssueMapper issueMapper;

	/**
	 * 최근 이슈 목록
	 * @return
	 */

	@Transactional(readOnly=true)
	public List<Issue> getListRecentIssue() {
		return issueMapper.getListRecentIssue();
	}
}
