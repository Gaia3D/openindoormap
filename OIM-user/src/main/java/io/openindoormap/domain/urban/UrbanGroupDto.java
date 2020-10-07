package io.openindoormap.domain.urban;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.server.core.Relation;

import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 도시 그룹
 *
 * @author jeongdae
 */
@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Relation(collectionRelation = "urbanGroups")
public class UrbanGroupDto {

    /****** 화면 표시용 *******/
    // 위도
    private BigDecimal latitude;
    // 경도
    private BigDecimal longitude;

    // 고유번호
    private Integer urbanGroupId;
    // 링크 활용 등을 위한 확장 컬럼
    private String urbanGroupKey;
    // 도시 그룹명
    @Size(max = 100)
    private String urbanGroupName;
    // 사용자 아이디
    private String userId;
    // 시작일
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;
    // 종료일
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;
    // POINT(위도, 경도)
    private String location;
    // 높이
    private BigDecimal altitude;
    // Map 이동시간
    private Integer duration;
    // 면적
    private Integer area;
    // 수용 인구
    private Integer receivingPopulation;
    // 수용 세대
    private Integer receivingHousehold;
    // 사업 시행자
    private String projectOperator;
    // 지자체로 양도 시간
    private String transferLocalGovernment;

	// 조상 고유번호
	private Integer ancestor;
	// 부모 고유번호
	private Integer parent;
	// 깊이
	private Integer depth;
	// 나열 순서
	private Integer viewOrder;
	// 자식 존재 유무
	private Integer children;

	// true : 기본(초기 등록), false : 선택
	private Boolean basic;
	// true : 사용, false : 사용안함
	private Boolean available;

    // 설명
    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime insertDate;
}
