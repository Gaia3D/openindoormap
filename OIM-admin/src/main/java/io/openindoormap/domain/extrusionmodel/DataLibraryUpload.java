package io.openindoormap.domain.extrusionmodel;

import io.openindoormap.domain.UploadDataType;
import io.openindoormap.domain.common.Search;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 데이터 라이브러리 업로드 정보
 * @author Cheon JeongDae
 *
 */
@ToString(callSuper = true)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataLibraryUpload extends Search implements Serializable {

	private static final long serialVersionUID = -5009944708903651572L;
	
	public static final String ZIP_EXTENSION = "zip";

	// converter 대상 파일 유무. true : 대상, false : 대상아님(기본값)
	private Boolean converterTarget;
	
	/****** validator ********/
	private String methodMode;
	
	// 고유번호
	private Long DataLibraryUploadId;
	// 데이터 라이브러리 그룹 고유키
	private Integer dataLibraryGroupId;
	// 데이트 라이브러리 그룹명
	private String dataLibraryGroupName;
	// common : 공통, public : 공개, private : 개인, group : 그룹
	private String sharing;
	// 데이트 라이브러리 타입. 3ds,obj, dae, collada, ifc, las, citygml, indoorgml
	private String dataType;
	// 데이트 라이브러리 타입
	private UploadDataType uploadDataType;
	// 데이트 라이브러리 개수
	private Long dataLibraryCount;
	// 데이트 라이브러리명
	private String dataLibraryName;
	// 사용자 아이디
	private String userId;
	// 사용자명
	private String userName;
	// 기본값 origin : latitude, longitude, height를 origin에 맞춤. boundingboxcenter : latitude, longitude, height를 boundingboxcenter 맞춤
	private String mappingType;

	// 상태. upload : 업로딩 완료, converter : 변환
	private String status;
	// 파일 개수
	private Integer fileCount;
	// converter 변환 대상 파일 수
	private Integer converterTargetCount;
	// converter 횟수
	private Integer converterCount;

	// 가로 기본값
	private Integer basicWidth;
	// 세로 기본값
	private Integer basicDepth;
	// 높이 기본값
	private Integer basicHeight;
	
	// 년도
	private String year;
	// 월
	private String month;
	// 일
	private String day;
	// 일년중 몇주
	private String year_week;
	// 이번달 몇주
	private String week;
	// 시간
	private String hour;
	// 분
	private String minute;

	// 설명
	private String description;
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private String updateDate;
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime insertDate;
}
