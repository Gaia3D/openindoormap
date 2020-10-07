-- FK, Index 는 별도 파일로 분리. 맨 마지막에 작업 예정
drop table if exists urban_group cascade;
drop table if exists urban cascade;

-- 도시 그룹
create table urban_group (
	urban_group_id				    integer,
	urban_group_key				    varchar(60)							not null ,
	urban_group_name				varchar(100)						not null,
	user_id						    varchar(32),
	ancestor					    integer								default 0,
	parent						    integer								default 1,
	depth						    integer								default 1,
	view_order					    integer								default 1,
	children					    integer								default 0,
	basic					    	boolean								default false,
	available					    boolean								default true,

    start_date                      timestamp with time zone,
    end_date                        timestamp with time zone,
    location		 			    GEOMETRY(POINT, 4326),
    altitude					    numeric(13,7),
	duration					    integer,

    area                            bigint                              default 0,
    receiving_population            int                                 default 0,
    receiving_household             int                                 default 0,
    project_operator                varchar(30),
    transfer_local_government       varchar(30),

    description					    varchar(256),
    update_date             	    timestamp with time zone,
	insert_date					    timestamp with time zone			default now(),
	constraint urban_group_pk 	    primary key (urban_group_id)
);

comment on table urban_group is '도시 그룹';
comment on column urban_group.urban_group_id is '고유번호';
comment on column urban_group.urban_group_key is '링크 활용 등을 위한 확장 컬럼';
comment on column urban_group.urban_group_name is '그룹명';
comment on column urban_group.user_id is '사용자 아이디';
comment on column urban_group.ancestor is '조상 고유번호';
comment on column urban_group.parent is '부모 고유번호';
comment on column urban_group.depth is '깊이';
comment on column urban_group.view_order is '나열 순서';
comment on column urban_group.children is '자식 존재 개수';
comment on column urban_group.basic is 'true : 기본(초기 등록), false : 선택';
comment on column urban_group.available is '사용유무, true : 사용, false : 사용안함';
comment on column urban_group.duration is 'Map 이동시간';

comment on column urban_group.start_date is '시작일';
comment on column urban_group.end_date is '종료일';
comment on column urban_group.location is 'POINT(위도, 경도). 공간 검색 속도 때문에 altitude는 분리';
comment on column urban_group.altitude is '높이';
comment on column urban_group.area is '면적';
comment on column urban_group.receiving_population is '수용 인구';
comment on column urban_group.receiving_household is '수용 세대';
comment on column urban_group.project_operator is '사업 시행자';
comment on column urban_group.transfer_local_government is '지자체로 양도 시기';
comment on column urban_group.available is '사용유무, true : 사용, false : 사용안함';

comment on column urban_group.description is '설명';
comment on column urban_group.insert_date is '등록일';

-- new town 정보

/*create table urban (
	urban_id						integer,
	urban_group_id				integer								not null,
	urban_name					varchar(64)							not null,
	user_id						    varchar(32),

    business_period                 varchar(30),
    development_area                int,
    population                      int,
    self_sufficient_area            int,
    house_number                    int,
    job_creation                    int,

	insert_date					timestamp with time zone			default now(),
	constraint urban_pk primary key(urban_id)
);

comment on table urban is 'new town 정보';
comment on column urban.urban_id is '고유번호';
comment on column urban.urban_group_id is 'new town 그룹 고유번호';
comment on column urban.urban_name is 'new town명';
comment on column urban.user_id is '사용자 아이디';

comment on column urban.business_period is '사업 기간';
comment on column urban.development_area is '개발 면적. 단위 m*m';
comment on column urban.population is '인구수. 단위 명';
comment on column urban.self_sufficient_area is '자족 용지. 단위 m*m';
comment on column urban.house_number is '주택수. 단위 호';
comment on column urban.job_creation is '고용 창출. 단위 명';

comment on column urban.insert_date is '등록일';*/







