package io.openindoormap.service;

import java.util.List;

import io.openindoormap.domain.issue.Issue;

/**
 * issue
 * @author jeongdae
 *
 */
public interface IssueService {

	/**
	 * 최근 이슈 목록
	 * @return
	 */
	List<Issue> getListRecentIssue();
}
