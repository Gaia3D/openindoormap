package io.openindoormap.domain;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 사용자 업로드 정보
 * @author Cheon JeongDae
 *
 */
@Getter
@Setter
@ToString
public class UploadData {
	
	public static final String ZIP_EXTENSION = "zip";
	
	// 페이지 처리를 위한 시작
	private Long offset;
	// 페이지별 표시할 건수
	private Long limit;
	
	/********** 검색 조건 ************/
	private String search_word;
	// 검색 옵션. 0 : 일치, 1 : 포함
	private String search_option;
	private String search_value;
	private String start_date;
	private String end_date;
	private String order_word;
	private String order_value;
	private Long list_counter = 10l;
	
	/****** validator ********/
	private String method_mode;
	
	// 고유번호
	private Long upload_data_id;
	// 프로젝트 고유키
	private Integer project_id;
	// 프로젝트명
	private String project_name;
	// 공유 타입. 0 : common, 1: public, 2 : private, 3 : sharing
	private String sharing_type;
	// 데이터 타입. 3ds, .obj, .dae, .ifc, citygml, indoorgml
	private String data_type;
	// 사용자 아이디
	private String user_id;
	// 사용자명
	private String user_name;
	// 데이터명
	private String data_name;
	
	// 위도
	private BigDecimal latitude;
	// 경도
	private BigDecimal longitude;
	// 높이
	private BigDecimal height;
	// 설명
	private String description;
	// 상태. 0 : 업로딩 완료, 1 : 변환
	private String status;
	// 압축유무. N : 압축안함(기본값). Y : 압축
	private String compress_yn;
	// 파일 개수
	private Integer file_count;
	// converter 횟수
	private Integer converter_count;
	private String update_date;
	private String insert_date;
	
	public String validate() {
		// TODO 구현해야 한다.
		return null;
	}
	
	public String getViewInsertDate() {
		if(this.insert_date == null || "".equals( insert_date)) {
			return "";
		}
		return insert_date.substring(0, 19);
	}
}