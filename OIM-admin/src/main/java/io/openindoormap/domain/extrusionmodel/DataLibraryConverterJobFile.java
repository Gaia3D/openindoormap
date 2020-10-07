package io.openindoormap.domain.extrusionmodel;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import io.openindoormap.domain.common.Search;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 데이터 라이브러리 변환 job 파일
 * @author Cheon JeongDae
 *
 */
@ToString(callSuper = true)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataLibraryConverterJobFile extends Search {
	
	// 화면 표기용
	private String sharing;
	private String dataType;
	private String fileName;
	private BigDecimal usf;
	// 위젯 표현용
	private Long count;
	
	/****** validator ********/
	private String methodMode;
	
	// 고유번호
	private Long dataLibraryConverterJobFileId;
	// 데이터 라이브러리 변환 job
	private Long dataLibraryConverterJobId;
	// 데이터 라이브러리 업로드 고유번호
	private Long dataLibraryUploadId;
	// 데이터 라이브러리 업로드 파일 고유번호
	private Long dataLibraryUploadFileId;
	// 데이터 라이브러리 그룹 고유번호
	private Integer dataLibraryGroupId;
	// 데이터 라이브러리 그룹명
	private String dataLibraryGroupName;
	// user id
	private String userId;
	// 상태. ready : 준비, success : 성공, fail : 실패
	private String status;
	// 에러 코드
	private String errorCode;
	
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
	
	// 등록일
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime insertDate;
	
	public String validate() {
		// TODO 구현해야 한다.
		return null;
	}
	
}
