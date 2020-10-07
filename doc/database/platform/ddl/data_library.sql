drop table if exists data_library_converter_job cascade;
drop table if exists data_library_converter_job_file cascade;
drop table if exists data_library_group cascade;
drop table if exists data_library cascade;
drop table if exists data_library_upload cascade;
drop table if exists data_library_upload_file cascade;


-- 데이터 라이브러리 파일 변환 job
create table data_library_converter_job (
	data_library_converter_job_id				bigint,
	data_library_upload_id					    bigint,
	data_library_group_target				    varchar(5)							default 'user',
	user_id							            varchar(32),
	title							            varchar(256)						not null,
	converter_template				            varchar(30)							default 'basic',
	usf								            numeric(13,5),
	y_axis_up						            char(1)								default 'N',
	file_count						            integer								default 0,
	status							            varchar(20)							default 'ready',
	error_code						            varchar(4000),
	year							            char(4)								default to_char(now(), 'yyyy'),
	month							            varchar(2)							default to_char(now(), 'MM'),
	day								            varchar(2)							default to_char(now(), 'DD'),
	year_week						            varchar(2)							default to_char(now(), 'WW'),
	week							            varchar(2)							default to_char(now(), 'W'),
	hour							            varchar(2)							default to_char(now(), 'HH24'),
	minute							            varchar(2)							default to_char(now(), 'MI'),
	update_date						            timestamp with time zone,
	insert_date						            timestamp with time zone			default now(),
	constraint data_library_converter_job_pk 	primary key (data_library_converter_job_id)
);

comment on table data_library_converter_job is '데이터 라이브러리 파일 변환 job';
comment on column data_library_converter_job.data_library_converter_job_id is '고유번호';
comment on column data_library_converter_job.data_library_upload_id is '데이터 라이브러리 업로더 고유번호';
comment on column data_library_converter_job.data_library_group_target is '[중복] admin : 관리자용 데이터 라이브러리 그룹, user : 일반 사용자용 데이터 라이브러리 그룹';
comment on column data_library_converter_job.title is '제목';
comment on column data_library_converter_job.user_id is '사용자 아이디';
comment on column data_library_converter_job.converter_template is '변환 유형. basic : 기본, building : 빌딩, extra-big-building : 초대형 빌딩, point-cloud : point cloud 데이터';
comment on column data_library_converter_job.usf is 'unit scale factor. 설계 파일의 1이 의미하는 단위. 기본 1 = 0.01m';
comment on column data_library_converter_job.y_axis_up is '높이방향. y축이 건물의 천장을 향하는 경우 Y. default = N';
comment on column data_library_converter_job.file_count is '대상 file 개수';
comment on column data_library_converter_job.status is '상태. ready : 준비, success : 성공, waiting : 승인대기, fail : 실패';
comment on column data_library_converter_job.error_code is '에러 코드';
comment on column data_library_converter_job.year is '년';
comment on column data_library_converter_job.month is '월';
comment on column data_library_converter_job.day is '일';
comment on column data_library_converter_job.year_week is '일년중 몇주';
comment on column data_library_converter_job.week is '이번달 몇주';
comment on column data_library_converter_job.hour is '시간';
comment on column data_library_converter_job.minute is '분';
comment on column data_library_converter_job.update_date is '수정일';
comment on column data_library_converter_job.insert_date is '등록일';


-- 데이터 라이브러리 변환 이력
create table data_library_converter_job_file(
	data_library_converter_job_file_id				bigint,
	data_library_converter_job_id					bigint,
	data_library_upload_id						    bigint,
	data_library_upload_file_id					    bigint,
	data_library_group_id						    int,
	user_id								            varchar(32),
	status								            varchar(20)							default 'ready',
	error_code							            varchar(4000),
	year								            char(4)								default to_char(now(), 'yyyy'),
	month								            varchar(2)							default to_char(now(), 'MM'),
	day									            varchar(2)							default to_char(now(), 'DD'),
	year_week							            varchar(2)							default to_char(now(), 'WW'),
	week								            varchar(2)							default to_char(now(), 'W'),
	hour								            varchar(2)							default to_char(now(), 'HH24'),
	minute								            varchar(2)							default to_char(now(), 'MI'),
	insert_date							            timestamp with time zone			default now(),
	constraint data_library_converter_job_file_pk 	primary key (data_library_converter_job_file_id)
);

comment on table data_library_converter_job_file is '데이터 라이브러리 변환 이력';
comment on column data_library_converter_job_file.data_library_converter_job_file_id is '고유번호';
comment on column data_library_converter_job_file.data_library_converter_job_id is '데이터 라이브러리 변환 job 고유번호';
comment on column data_library_converter_job_file.data_library_upload_id is '데이터 라이브러리 업로드 고유번호';
comment on column data_library_converter_job_file.data_library_upload_file_id is '데이터 라이브러리 업로드 파일 고유번호';
comment on column data_library_converter_job_file.data_library_group_id is '데이터 라이브러리 그룹 고유번호(중복)';
comment on column data_library_converter_job_file.user_id is '사용자 아이디';
comment on column data_library_converter_job_file.status is '상태. ready : 준비, success : 성공, fail : 실패';
comment on column data_library_converter_job_file.error_code is '에러 코드';
comment on column data_library_converter_job_file.year is '년';
comment on column data_library_converter_job_file.month is '월';
comment on column data_library_converter_job_file.day is '일';
comment on column data_library_converter_job_file.year_week is '일년중 몇주';
comment on column data_library_converter_job_file.week is '이번달 몇주';
comment on column data_library_converter_job_file.hour is '시간';
comment on column data_library_converter_job_file.minute is '분';
comment on column data_library_converter_job_file.insert_date is '등록일';


-- data library 그룹
create table data_library_group (
	data_library_group_id		            integer,
	data_library_group_key				    varchar(60)				        		not null,
	data_library_group_name      		    varchar(256)			        		not null,
	data_library_group_path				    varchar(256),
	data_library_group_target			    varchar(5)		        				default 'user',
	sharing						            varchar(30)					        	default 'public',
	user_id						            varchar(32),
	ancestor					            integer					        		default 0,
	parent                		            integer					        		default 0,
	depth                	  	            integer					        		default 1,
	view_order					            integer					        		default 1,
	children					            integer						        	default 0,
	basic						            boolean							        default false,
	available					            boolean							        default true,
	data_library_count					    integer							        default 0,
	description					            varchar(256),
	update_date             	            timestamp with time zone,
	insert_date					            timestamp with time zone		        default now(),
	constraint data_library_group_pk        primary key (data_library_group_id)
);

comment on table data_library_group is 'data library 그룹';
comment on column data_library_group.data_library_group_id is 'data library 그룹 고유번호';
comment on column data_library_group.data_library_group_key is '링크 활용 등을 위한 확장 컬럼';
comment on column data_library_group.data_library_group_name is 'data library 그룹 그룹명';
comment on column data_library_group.data_library_group_path is '서비스 경로';
comment on column data_library_group.data_library_group_target is 'admin : 관리자용 data library 그룹, user : 일반 사용자용 data library 그룹';
comment on column data_library_group.sharing is 'common : 공통, public : 공개, private : 비공개, group : 그룹';
comment on column data_library_group.user_id is '사용자 아이디';
comment on column data_library_group.data_library_count is '데이터 라이브러리 총 건수';
comment on column data_library_group.ancestor is '조상';
comment on column data_library_group.parent is '부모';
comment on column data_library_group.depth is '깊이';
comment on column data_library_group.view_order is '나열 순서';
comment on column data_library_group.children is '자식 존재 개수';
comment on column data_library_group.basic is 'true : 기본, false : 선택';
comment on column data_library_group.available is 'true : 사용, false : 사용안함';
comment on column data_library_group.description is '설명';
comment on column data_library_group.update_date is '수정일';
comment on column data_library_group.insert_date is '등록일';

-- data library
create table data_library (
	data_library_id					    bigint,
	data_library_group_id			    integer,
	data_library_converter_job_id		bigint,
	data_library_key					varchar(100)					not null,
	data_library_name				    varchar(256)					not null,
	data_library_path				    varchar(256),
	data_library_thumbnail              varchar(256),
	data_type					        varchar(30),
	user_id						        varchar(32),
	service_type				        varchar(30),
    view_order					        integer							default 1,
	available					        boolean							default true,
    status						        varchar(20)						default 'use',
	description					        varchar(256),
	update_date					        timestamp with time zone,
	insert_date					        timestamp with time zone 		default now(),
	constraint data_library_pk 		    primary key (data_library_id)
);


comment on table data_library is 'data library';
comment on column data_library.data_library_id is 'data library 고유번호';
comment on column data_library.data_library_group_id is 'data library 그룹 고유번호';
comment on column data_library.data_library_converter_job_id is 'data library converter job 고유번호';
comment on column data_library.data_library_key is 'data library 고유키(API용)';
comment on column data_library.data_library_name is 'data library명';
comment on column data_library.data_library_path is 'data library 경로';
comment on column data_library.data_library_thumbnail is 'data library 썸네일';
comment on column data_library.data_type is '데이터 타입(중복). 3ds,obj,dae,collada,ifc,las,citygml,indoorgml,etc';
comment on column data_library.user_id is '사용자명';
comment on column data_library.service_type is '서비스 타입 (정적, 동적)';
comment on column data_library.view_order is '나열 순서';
comment on column data_library.available is '사용유무.';
comment on column data_library.status is '상태. processing : 변환중, use : 사용중, unused : 사용중지(관리자), delete : 삭제(비표시)';
comment on column data_library.description is '설명';
comment on column data_library.update_date is '수정일';
comment on column data_library.insert_date is '등록일';



-- 데이터 라이브러리 업로드 정보
create table data_library_upload (
	data_library_upload_id			bigint,
	data_library_group_id			int,
	sharing							varchar(30)							default 'public',
	data_type						varchar(30),
	data_library_name				varchar(256),
	user_id							varchar(32),
	mapping_type					varchar(30)							default 'origin',
	file_count						int									default 0,
	converter_target_count			int									default 0,
	converter_count					int 								default 0,
	status							varchar(20)							default 'upload',
	basic_width                     int                                 default 0,
	basic_depth                     int                                 default 0,
	basic_height                    int                                 default 0,
	year							char(4)								default to_char(now(), 'yyyy'),
	month							varchar(2)							default to_char(now(), 'MM'),
	day								varchar(2)							default to_char(now(), 'DD'),
	year_week						varchar(2)							default to_char(now(), 'WW'),
	week							varchar(2)							default to_char(now(), 'W'),
	hour							varchar(2)							default to_char(now(), 'HH24'),
	minute							varchar(2)							default to_char(now(), 'MI'),
	description						varchar(256),
	update_date						timestamp with time zone,
	insert_date						timestamp with time zone			default now(),
	constraint data_library_upload_pk		primary key (data_library_upload_id)
);

comment on table data_library_upload is '데이터 라이브러리 업로드 정보';
comment on column data_library_upload.data_library_upload_id is '고유번호';
comment on column data_library_upload.data_library_group_id is '데이터 라이브러리 그룹 고유번호';
comment on column data_library_upload.sharing is '공유 유형. 0 : common, 1: public, 2 : private, 3 : sharing';
comment on column data_library_upload.data_type is '데이터 타입. 3ds,obj,dae,collada,ifc,las,citygml,indoorgml,gml,jpg,jpeg,gif,png,bmp,zip,mtl';
comment on column data_library_upload.data_library_name is '데이터 라이브러리명';
comment on column data_library_upload.user_id is '사용자 아이디';
comment on column data_library_upload.mapping_type is '기본값 origin : latitude, longitude, height를 origin에 맞춤. boundingboxcenter : latitude, longitude, height를 boundingboxcenter 맞춤';
comment on column data_library_upload.file_count is '파일 개수';
comment on column data_library_upload.converter_target_count is 'converter 변환 대상 파일 수';
comment on column data_library_upload.converter_count is 'converter 횟수';
comment on column data_library_upload.status is '상태. upload : 업로딩 완료, converter : 변환';
comment on column data_library_upload.basic_width is '가로 기본값';
comment on column data_library_upload.basic_depth is '세로 기본값';
comment on column data_library_upload.basic_height is '높이 기본값';
comment on column data_library_upload.year is '년';
comment on column data_library_upload.month is '월';
comment on column data_library_upload.day is '일';
comment on column data_library_upload.year_week is '일년중 몇주';
comment on column data_library_upload.week is '이번달 몇주';
comment on column data_library_upload.hour is '시간';
comment on column data_library_upload.minute is '분';
comment on column data_library_upload.description is '설명';
comment on column data_library_upload.update_date is '수정일';
comment on column data_library_upload.insert_date is '등록일';


-- 데이터 라이브러리 업로드 파일 정보
create table data_library_upload_file(
	data_library_upload_file_id				bigint,
	data_library_upload_id					bigint,
	converter_target				        boolean								default false,
	user_id							        varchar(32),
	file_type						        varchar(9)							default 'file',
	file_name						        varchar(100)						not null,
	file_real_name					        varchar(100)						not null,
	file_path						        varchar(256)						not null,
	file_sub_path					        varchar(256),
	file_size						        varchar(12)							not null,
	file_ext						        varchar(20),
	depth							        int									default 1,
	error_message					        varchar(256),
	insert_date						        timestamp with time zone			default now(),
	constraint data_library_upload_file_pk	primary key (data_library_upload_file_id)
);

comment on table data_library_upload_file is '데이터 라이브러리 업로드 파일 정보';
comment on column data_library_upload_file.data_library_upload_file_id is '고유번호';
comment on column data_library_upload_file.data_library_upload_id is '데이터 라이브러리 업로드 고유번호';
comment on column data_library_upload_file.converter_target is 'converter 대상 파일 유무. Y : 대상, N : 대상아님(기본값)';
comment on column data_library_upload_file.user_id is '사용자 아이디';
comment on column data_library_upload_file.file_type is '디렉토리/파일 구분. D : 디렉토리, F : 파일';
comment on column data_library_upload_file.file_name is '파일 이름';
comment on column data_library_upload_file.file_real_name is '파일 실제 이름';
comment on column data_library_upload_file.file_path is '파일 경로';
comment on column data_library_upload_file.file_sub_path is '프로젝트 경로 또는 공통 디렉토리 이하 부터의 파일 경로';
comment on column data_library_upload_file.file_size is '파일 사이즈';
comment on column data_library_upload_file.file_ext is '파일 확장자';
comment on column data_library_upload_file.depth is '계층구조 깊이. 1부터 시작';
comment on column data_library_upload_file.error_message is '오류 메시지';
comment on column data_library_upload_file.insert_date is '등록일';

