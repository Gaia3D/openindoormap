update geopolicy set
	geoserver_data_url = 'http://localhost:18080/geoserver',
	geoserver_data_workspace = 'lhdt',
	geoserver_data_store ='lhdt',
	geoserver_user ='admin',
	geoserver_password = 'geoserver';


update geopolicy
set init_longitude = '127.00598139968887',
	init_latitude = '37.44829387479118';
commit;

-- smart tiling 이후에 해 줘야 할 작업, 벌크 업로드 시 처리됨.
-- 1 데이터 건수 수정	

-- 2 데이터 타입 수정
-- 한줄짜리는 위험

-- 3 그룹 기본 좌표 확인
