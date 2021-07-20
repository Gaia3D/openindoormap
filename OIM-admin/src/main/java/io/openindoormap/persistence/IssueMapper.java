package io.openindoormap.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import io.openindoormap.domain.issue.Issue;

/**
 * 이슈
 * @author jeongdae
 *
 */
@Repository
public interface IssueMapper {
	
	/**
	 * 최근 이슈 목록
	 * @return
	 */
	List<Issue> getListRecentIssue();
}
