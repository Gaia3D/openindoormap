package io.openindoormap.domain.extrusionmodel;

import io.openindoormap.domain.common.Search;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * design layer 이력
 * @author jeongdae
 *
 */
@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DesignLayerLog extends Search {
	
	// 총건수
	private Long totalCount;
	
	// 고유번호
	private Long designLayerLogId;
	// design layer 고유번호
	private Long designLayerId;
	// 사용자 아이디
	private String userId;
	// 사용자 이름
	private String userName;
	// 상태. ready : 시뮬레이션전, use : 사용중, temp : 임시저장, complete : 완료
	private String status;
	// 년도
	private String year;
	// 월
	private String month;
	// 일
	private String day;
	// 일년중 몇주
	private String yearWeek;
	// 이번달 몇주
	private String week;
	// 시간
	private String hour;
	// 분
	private String minute;
	
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime insertDate;

}
