//package io.openindoormap.domain.urban;
//
//import io.openindoormap.domain.common.Search;
//import lombok.*;
//import org.springframework.format.annotation.DateTimeFormat;
//
//import javax.validation.constraints.NotBlank;
//import javax.validation.constraints.Size;
//import java.io.Serializable;
//import java.time.LocalDateTime;
//
///**
// * 뉴타운
// */
//@ToString(callSuper = true)
//@Builder
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//public class Urban extends Search implements Serializable {
//
//	private static final long serialVersionUID = -7047149260746719788L;
//
//	/******** 화면 오류 표시용 ********/
//	private String message;
//	private String errorCode;
//
//	/****** validator ********/
//	private String methodMode;
//
//	/********** DB 사용 *************/
//	// 고유번호
//	@NotBlank
//	private Integer newTownId;
//	// 뉴타운 그룹 고유번호
//	private Integer newTownGroupId;
//	// 뉴타운 그룹명(화면용)
//	private String newTownGroupName;
//	// new town 명
//	@Size(max = 64)
//	private String newTownName;
//	// 사용자 아이디
//	private String userId;
//
//	// 사업 기간
//	private String business_period;
//	// 개발 면적. 단위 m*m
//	private Integer development_area;
//	// 인구수. 단위 명
//	private Integer population;
//	// 자족 용지. 단위 m*m
//	private Integer self_sufficient_area;
//	// 주택수. 단위 호
//	private Integer house_number;
//	// 고용 창출. 단위 명
//	private Integer job_creation;
//
//	// 등록일
//	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
//	private LocalDateTime insertDate;
//}
