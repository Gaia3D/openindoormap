package io.openindoormap.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.domain.*;
import io.openindoormap.domain.agent.DataLibraryConversionJobResult;
import io.openindoormap.domain.agent.DataLibraryConverterResultLog;
import io.openindoormap.domain.common.QueueMessage;
import io.openindoormap.domain.extrusionmodel.*;
import io.openindoormap.persistence.DataLibraryConverterMapper;
import io.openindoormap.service.*;
import io.openindoormap.support.LogMessageSupport;
import io.openindoormap.utils.FileUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 데이터 라이브러리 변환
 * @author jeongdae
 *
 */
@Slf4j
@Service
public class DataLibraryConverterServiceImpl implements DataLibraryConverterService {

    @Autowired
    private AMQPPublishService aMQPPublishService;

    @Autowired
    private DataLibraryConverterMapper dataLibraryConverterMapper;

    @Autowired
    private DataLibraryService dataLibraryService;

    @Autowired
    private DataLibraryGroupService dataLibraryGroupService;

    @Autowired
    private PropertiesConfig propertiesConfig;

    @Autowired
    private DataLibraryUploadService dataLibraryUploadService;

    /**
     * 데이터 라이브러리 converter job 총 건수
     * @param dataLibraryConverterJob dataLibraryConverterJob
     * @return converter job 총 건수
     */
    @Transactional(readOnly=true)
    public Long getDataLibraryConverterJobTotalCount(DataLibraryConverterJob dataLibraryConverterJob) {
        return dataLibraryConverterMapper.getDataLibraryConverterJobTotalCount(dataLibraryConverterJob);
    }

    /**
     * 데이터 라이브러리 converter job file 총 건수
     * @param dataLibraryConverterJobFile dataLibraryConverterJobFile
     * @return converter job file 총 건수
     */
    @Transactional(readOnly=true)
    public Long getDataLibraryConverterJobFileTotalCount(DataLibraryConverterJobFile dataLibraryConverterJobFile) {
        return dataLibraryConverterMapper.getDataLibraryConverterJobFileTotalCount(dataLibraryConverterJobFile);
    }

    /**
     * 데이터 라이브러리 f4d converter job 목록
     * @param dataLibraryConverterJob dataLibraryConverterJob
     * @return f4d converter job 목록
     */
    @Transactional(readOnly=true)
    public List<DataLibraryConverterJob> getListDataLibraryConverterJob(DataLibraryConverterJob dataLibraryConverterJob) {
        return dataLibraryConverterMapper.getListDataLibraryConverterJob(dataLibraryConverterJob);
    }

    /**
     * 데이터 라이브러리 f4d converter job file 목록
     * @param dataLibraryConverterJobFile dataLibraryConverterJobFile
     * @return f4d converter job file 목록
     */
    @Transactional(readOnly=true)
    public List<DataLibraryConverterJobFile> getListDataLibraryConverterJobFile(DataLibraryConverterJobFile dataLibraryConverterJobFile) {
        return dataLibraryConverterMapper.getListDataLibraryConverterJobFile(dataLibraryConverterJobFile);
    }

    /**
     * 데이터 라이브러리 converter job 등록
     * @param dataLibraryConverterJob    dataLibraryConverterJob
     */
    @Transactional
    public void insertDataLibraryConverter(DataLibraryConverterJob dataLibraryConverterJob) {

        // 1. 변환해야 할 파일 목록(데이터 라이브러리 목록)을 취득
        // 2. 데이터 라이브러리 converter job 을 등록
        // 3. 데이터 라이브러리 convert job file 하나씩 등록. 변환 상태를 ready(준비)로 등록.
        // 4. queue 를 실행
        // 5. 데이터 라이브러리 업로드 ConverterCount를 1로 갱신

        String dataLibraryGroupRootPath = propertiesConfig.getDataServiceDir();

        String title = dataLibraryConverterJob.getTitle();
        String converterTemplate = dataLibraryConverterJob.getConverterTemplate();
        String userId = dataLibraryConverterJob.getUserId();
        BigDecimal usf = dataLibraryConverterJob.getUsf();

        String[] dataLibraryUploadIds = dataLibraryConverterJob.getConverterCheckIds().split(",");
        for (String dataLibraryUploadId : dataLibraryUploadIds) {

            // 1. 변환해야 할 파일 목록을 취득
            DataLibraryUpload dataLibraryUpload = new DataLibraryUpload();
            // dataLibraryUpload.setUserId(userId);
            dataLibraryUpload.setDataLibraryUploadId(Long.valueOf(dataLibraryUploadId));
            dataLibraryUpload.setConverterTarget(true);
            List<DataLibraryUploadFile> dataLibraryUploadFileList = dataLibraryUploadService.getListDataLibraryUploadFile(dataLibraryUpload);

            // 2. converter job 을 등록
            DataLibraryConverterJob inDataLibraryConverterJob = new DataLibraryConverterJob();
            inDataLibraryConverterJob.setDataLibraryUploadId(Long.valueOf(dataLibraryUploadId));
            inDataLibraryConverterJob.setDataLibraryGroupTarget(ServerTarget.ADMIN.name().toLowerCase());
            inDataLibraryConverterJob.setUserId(userId);
            inDataLibraryConverterJob.setTitle(title);
            inDataLibraryConverterJob.setUsf(usf);
            inDataLibraryConverterJob.setConverterTemplate(converterTemplate);
            inDataLibraryConverterJob.setFileCount(dataLibraryUploadFileList.size());
            inDataLibraryConverterJob.setYAxisUp(dataLibraryConverterJob.getYAxisUp());
            dataLibraryConverterMapper.insertDataLibraryConverterJob(inDataLibraryConverterJob);

            Long dataLibraryConverterJobId = inDataLibraryConverterJob.getDataLibraryConverterJobId();
            int converterTargetCount = dataLibraryUploadFileList.size();
            for (int i = 0; i < converterTargetCount; i++) {

                DataLibraryUploadFile dataLibraryUploadFile = dataLibraryUploadFileList.get(i);

                // 3. convert job file 하나씩 등록. 변환 상태를 ready(준비)로 등록.
                DataLibraryConverterJobFile dataLibraryConverterJobFile = new DataLibraryConverterJobFile();
                dataLibraryConverterJobFile.setUserId(userId);
                dataLibraryConverterJobFile.setDataLibraryConverterJobId(dataLibraryConverterJobId);
                dataLibraryConverterJobFile.setDataLibraryUploadId(Long.valueOf(dataLibraryUploadId));
                dataLibraryConverterJobFile.setDataLibraryUploadFileId(dataLibraryUploadFile.getDataLibraryUploadFileId());
                dataLibraryConverterJobFile.setDataLibraryGroupId(dataLibraryUploadFile.getDataLibraryGroupId());
                dataLibraryConverterJobFile.setUserId(userId);
                dataLibraryConverterJobFile.setUsf(usf);
                dataLibraryConverterJobFile.setStatus(ConverterJobStatus.READY.getValue());
                dataLibraryConverterMapper.insertDataLibraryConverterJobFile(dataLibraryConverterJobFile);

                // 왜 마지막에만 Queue를 실행하나요?
                if (i == converterTargetCount - 1) {
                    // 4. queue 를 실행
                    executeConverter(userId, dataLibraryGroupRootPath, inDataLibraryConverterJob, dataLibraryUploadFile);
                }

            }

            // 7. 업로드 데이터의 ConverterCount를 1로 갱신
            dataLibraryUpload.setConverterCount(1);
            dataLibraryUploadService.updateDataLibraryUpload(dataLibraryUpload);
        }
    }

    /**
     * 데이터 라이브러리 converter job 수정
     * @param dataLibraryConverterJob
     */
    @Transactional
    public void updateDataLibraryConverterJob(DataLibraryConverterJob dataLibraryConverterJob) {
        dataLibraryConverterMapper.updateDataLibraryConverterJob(dataLibraryConverterJob);
    }

    /**
     * 데이터 라이브러리 변환 상태를 갱신
     * @param dataLibraryConverterResultLog
     */
    @Transactional
    public void updateDataLibraryConverterJobStatus(DataLibraryConverterResultLog dataLibraryConverterResultLog) {

        DataLibraryConverterJob dataLibraryConverterJob = dataLibraryConverterResultLog.getDataLibraryConverterJob();

        // 1. 로그파일 정보를 통해 데이터 라이브러리 ConvertJob 갱신
        updateDataLibraryConverterJob(dataLibraryConverterJob, dataLibraryConverterResultLog);
        // 2. 로그파일 정보를 통해 ConvertJobFile 갱신
        List<DataLibraryConverterJobFile> dataLibraryConverterJobFiles = dataLibraryConverterMapper.getListDataLibraryConverterJobFileByParent(dataLibraryConverterJob);

        // List to Map
        List<DataLibraryConversionJobResult> conversionJobResultList = dataLibraryConverterResultLog.getConversionJobResult();
        Map<String, DataLibraryConversionJobResult> dataLibraryConverterJobResultMap = conversionJobResultList
                .stream()
                .collect(Collectors.toMap(DataLibraryConversionJobResult::getFileName, result -> result));

        // TODO 그냥 key 로 update 를 바로 해도 될거 같은데..... 왜 upload 를 가지고 오지?
        String userId = dataLibraryConverterJob.getUserId();
        Long dataLibraryConverterJobId = dataLibraryConverterJob.getDataLibraryConverterJobId();
        int converterTargetCount = dataLibraryConverterJobFiles.size();
        for (DataLibraryConverterJobFile dataLibraryConverterJobFile : dataLibraryConverterJobFiles) {
            DataLibraryUploadFile dataLibraryUploadFile = new DataLibraryUploadFile();
            // dataLibraryUploadFile.setUserId(userId);
            dataLibraryUploadFile.setDataLibraryUploadFileId(dataLibraryConverterJobFile.getDataLibraryUploadFileId());
            dataLibraryUploadFile = dataLibraryUploadService.getDataLibraryUploadFile(dataLibraryUploadFile);

            String key = dataLibraryUploadFile.getFileRealName();
            DataLibraryConversionJobResult conversionJobResult = dataLibraryConverterJobResultMap.get(key);

            // TODO enum binding 이 잘 안되서, 임시로 string으로 함. 고쳐야 함
            log.info("### status = {}, dataLibraryConverterJobFile = {}", conversionJobResult.getResultStatus(), dataLibraryConverterJobFile);
            if (ConverterJobResultStatus.SUCCESS == conversionJobResult.getResultStatus()) {
                // 상태가 성공인 경우
                // 데이터를 등록 혹은 갱신. 상태를 use(사용중)로 등록.
                DataLibrary dataLibrary = upsertDataLibrary(userId, dataLibraryConverterJobId, converterTargetCount, dataLibraryUploadFile);

                // 데이터 라이브러리 그룹 신규 생성의 경우 데이터 건수 update
                updateDataLibraryGroup(userId, dataLibrary, dataLibraryUploadFile);
                dataLibraryConverterJobFile.setStatus(ConverterJobStatus.SUCCESS.getValue());
            } else {
                // 상태가 실패인 경우
                // 1) 데이터 삭제
                // 2) 데이터 그룹 데이터 건수 -1
                // 3) 데이터 그룹 최신 이동 location 은? 이건 그냥 다음에 하는걸로~
                DataLibrary dataLibrary = new DataLibrary();
                // dataLibrary.setUserId(dataLibraryConverterJob.getUserId());
                dataLibrary.setDataLibraryConverterJobId(dataLibraryConverterJobId);
                List<DataLibrary> dataLibraryList = dataLibraryService.getDataLibraryByDataLibraryConverterJob(dataLibrary);
                deleteFailDataLibrary(dataLibraryList);

                dataLibraryConverterJobFile.setStatus(ConverterJobStatus.FAIL.getValue());
                dataLibraryConverterJobFile.setErrorCode(conversionJobResult.getMessage());
            }

            // ConvertJobFile status, errorCode 갱신
            dataLibraryConverterMapper.updateDataLibraryConverterJobFile(dataLibraryConverterJobFile);

        }
    }

    /**
     * QueueMessage 실행
     * @param userId	userId
     * @param dataLibraryGroupRootPath	dataLibraryGroupRootPath
     * @param inDataLibraryConverterJob	inDataLibraryConverterJob
     * @param dataLibraryUploadFile	dataLibraryUploadFile
     */
    private void executeConverter(String userId, String dataLibraryGroupRootPath, DataLibraryConverterJob inDataLibraryConverterJob, DataLibraryUploadFile dataLibraryUploadFile) {

        DataLibraryGroup dataLibraryGroup = new DataLibraryGroup();
        //dataLibraryGroup.setUserId(userId);
        dataLibraryGroup.setDataLibraryGroupId(dataLibraryUploadFile.getDataLibraryGroupId());
        dataLibraryGroup = dataLibraryGroupService.getDataLibraryGroup(dataLibraryGroup);

        Long dataLibraryConverterJobId = inDataLibraryConverterJob.getDataLibraryConverterJobId();

        // path 와 File.seperator 의 차이점 때문에 변환
        String dataLibraryGroupFilePath = FileUtils.getFilePath(dataLibraryGroup.getDataLibraryGroupPath());

        // 로그파일을 DataLibraryConverterJobId로 분리하여 쓰도록 수정
        String makedDirectory = FileUtils.makeDirectory(userId, UploadDirectoryType.YEAR_MONTH, propertiesConfig.getDataLibraryConverterLogDir());
        String logFilePath = makedDirectory + "logTest_" + dataLibraryConverterJobId + ".txt";

        log.info("-------------------------------------------------------");
        log.info("----------- dataLibraryGroupRootPath = {}", dataLibraryGroupRootPath);
        log.info("----------- dataLibraryGroup.getDataLibraryGroupPath() = {}", dataLibraryGroup.getDataLibraryGroupPath());

        log.info("----------- input = {}", dataLibraryUploadFile.getFilePath());
        log.info("----------- output = {}", dataLibraryGroupRootPath + dataLibraryGroupFilePath);
        log.info("----------- log = {}", logFilePath);

        log.info("-------------------------------------------------------");

        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setConverterType(ConverterType.DATA_LIBRARY);
        queueMessage.setServerTarget(ServerTarget.ADMIN);
        queueMessage.setDataLibraryConverterJobId(dataLibraryConverterJobId);
        //queueMessage.setDataLibraryConverterJobFileId(inDataLibraryConverterJob.getDataLibraryConverterJobFileId());
        queueMessage.setInputFolder(dataLibraryUploadFile.getFilePath());
        queueMessage.setOutputFolder(dataLibraryGroupRootPath + dataLibraryGroupFilePath);
        //queueMessage.setMeshType("0");
        queueMessage.setLogPath(logFilePath);
        queueMessage.setIndexing("y");
        queueMessage.setUsf(inDataLibraryConverterJob.getUsf());
        queueMessage.setIsYAxisUp(inDataLibraryConverterJob.getYAxisUp());
        queueMessage.setUserId(userId);
        queueMessage.setUploadDataType(UploadDataType.findBy(dataLibraryUploadFile.getDataType()));

        // 템플릿 별 meshType과 skinLevel 설정
        ConverterTemplate template = ConverterTemplate.findBy(inDataLibraryConverterJob.getConverterTemplate());
        //assert template != null;
        queueMessage.setMeshType(template.getMeshType());
        queueMessage.setSkinLevel(template.getSkinLevel());

        // TODO
        // 조금 미묘하다. transaction 처리를 할지, 관리자 UI 재 실행을 위해서는 여기가 맞는거 같기도 하고....
        // 별도 기능으로 분리해야 하나?
        try {
            aMQPPublishService.send(queueMessage);
        } catch(AmqpException e) {
            DataLibraryConverterJob dataLibraryConverterJob = new DataLibraryConverterJob();
            //dataLibraryConverterJob.setUserId(userId);
            dataLibraryConverterJob.setDataLibraryConverterJobId(dataLibraryConverterJobId);
            dataLibraryConverterJob.setStatus(ConverterJobStatus.WAITING.name());
            dataLibraryConverterJob.setErrorCode(e.getMessage());
            dataLibraryConverterMapper.updateDataLibraryConverterJob(dataLibraryConverterJob);
            LogMessageSupport.printMessage(e, "@@@@@@@@@@@@ AmqpException. message = {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }
    }

    /**
     * 로그파일을 통한 데이터 라이브러리 변환 작업 상태를 갱신
     * @param dataLibraryConverterJob
     * @param dataLibraryConverterResultLog
     */
    private void updateDataLibraryConverterJob(DataLibraryConverterJob dataLibraryConverterJob, DataLibraryConverterResultLog dataLibraryConverterResultLog) {
        if (dataLibraryConverterResultLog.getIsSuccess()) {
            if (dataLibraryConverterResultLog.getNumberOfFilesConverted() != dataLibraryConverterResultLog.getNumberOfFilesToBeConverted()) {
                dataLibraryConverterJob.setStatus(ConverterJobStatus.PARTIAL_SUCCESS.getValue());
            } else {
                dataLibraryConverterJob.setStatus(ConverterJobStatus.SUCCESS.getValue());
            }
        } else {
            dataLibraryConverterJob.setStatus(ConverterJobStatus.FAIL.getValue());
            dataLibraryConverterJob.setErrorCode(dataLibraryConverterResultLog.getFailureLog());
        }
        dataLibraryConverterMapper.updateDataLibraryConverterJob(dataLibraryConverterJob);
    }

    /**
     * TODO 현재는 dataLibraryConverterJob 과 dataLibrary 가 1:1 의 관계여서 dataLibraryConverterJobId를 받지만, 나중에는 dataLibraryConverterJobFileId 를 받아야 함
     * dataLibraryKey가 존재하지 않을 경우 insert, 존재할 경우 update
     * @param userId	userId
     * @param dataLibraryUploadFile	dataLibraryUploadFile
     */
    private DataLibrary upsertDataLibrary(String userId, Long dataLibraryConverterJobId, int converterTargetCount, DataLibraryUploadFile dataLibraryUploadFile) {

        // converterTargetCount = 1 이면 uploading 시 데이터 이름을 넣고, 아닐 경우 dataFile명을 등록
        Integer dataLibraryGroupId = dataLibraryUploadFile.getDataLibraryGroupId();
        String fileRealName = dataLibraryUploadFile.getFileRealName();
        String fileName = dataLibraryUploadFile.getFileName();

        String dataLibraryKey = fileRealName.substring(0, fileRealName.lastIndexOf("."));
        String dataLibraryName;
        if (converterTargetCount == 1) {
            dataLibraryName = dataLibraryUploadFile.getDataLibraryName();
        } else {
            dataLibraryName = fileName.substring(0, fileName.lastIndexOf("."));
        }

        // 데이터 라이브러리 경로
        String adminDataLibraryServicePath = propertiesConfig.getAdminDataLibraryServicePath();
        DataLibraryGroup dataLibraryGroup = dataLibraryGroupService.getDataLibraryGroup(DataLibraryGroup.builder().dataLibraryGroupId(dataLibraryGroupId).build());
        String dataLibraryPath = adminDataLibraryServicePath + dataLibraryGroup.getDataLibraryGroupKey() + "/" + dataLibraryKey;
        String dataLibraryThumbnail = adminDataLibraryServicePath + dataLibraryGroup.getDataLibraryGroupKey() + "/" + DataLibrary.F4D_PREFIX + dataLibraryKey + "/thumbnail.png";

        String dataType = dataLibraryUploadFile.getDataType();
        String sharing = dataLibraryUploadFile.getSharing();
        String mappingType = dataLibraryUploadFile.getMappingType();

        DataLibrary dataLibrary = new DataLibrary();
        dataLibrary.setDataLibraryGroupId(dataLibraryGroupId);
        dataLibrary.setDataLibraryKey(dataLibraryKey);
        dataLibrary = dataLibraryService.getDataLibraryByDataLibraryKey(dataLibrary);

        if (dataLibrary == null) {
            // int order = 1;

            dataLibrary = new DataLibrary();
            dataLibrary.setDataLibraryGroupId(dataLibraryGroupId);
            dataLibrary.setDataLibraryConverterJobId(dataLibraryConverterJobId);
//            dataLibrary.setMappingType(mappingType);
            dataLibrary.setDataType(dataType);
            dataLibrary.setDataLibraryKey(dataLibraryKey);
            dataLibrary.setDataLibraryName(dataLibraryName);
            dataLibrary.setDataLibraryPath(dataLibraryPath);
            dataLibrary.setDataLibraryThumbnail(dataLibraryThumbnail);
            dataLibrary.setUserId(userId);
            //dataLibrary.setStatus(DataStatus.PROCESSING.name().toLowerCase());
            dataLibrary.setStatus(DataLibraryStatus.USE.name().toLowerCase());
            dataLibrary.setMethodType(MethodType.INSERT);
            dataLibraryService.insertDataLibrary(dataLibrary);

        } else {
            dataLibrary.setDataLibraryConverterJobId(dataLibraryConverterJobId);
//            dataLibrary.setMappingType(mappingType);
            dataLibrary.setDataType(dataType);
            dataLibrary.setDataLibraryName(dataLibraryName);
            dataLibrary.setDataLibraryPath(dataLibraryPath);
            dataLibrary.setDataLibraryThumbnail(dataLibraryThumbnail);
            dataLibrary.setUserId(userId);
            //dataLibrary.setStatus(DataStatus.PROCESSING.name().toLowerCase());
            dataLibrary.setStatus(DataLibraryStatus.USE.name().toLowerCase());
            dataLibrary.setMethodType(MethodType.UPDATE);
            dataLibraryService.updateDataLibrary(dataLibrary);
        }

        return dataLibrary;
    }

    /**
     * 데이터 라이브러리 그룹 신규 생성의 경우 데이터 건수 update
     * location_update_type 이 auto 일 경우 dataLibrary 위치 정보로 dataLibraryGroup 위치 정보 수정
     * @param userId userId
     * @param dataLibrary dataLibrary
     * @param dataLibraryUploadFile	dataLibraryUploadFile
     */
    private void updateDataLibraryGroup(String userId, DataLibrary dataLibrary, DataLibraryUploadFile dataLibraryUploadFile) {
        DataLibraryGroup dataLibraryGroup = DataLibraryGroup.builder()
//				.userId(userId)
                .dataLibraryGroupId(dataLibraryUploadFile.getDataLibraryGroupId())
                .build();

        DataLibraryGroup dbDataLibraryGroup = dataLibraryGroupService.getDataLibraryGroup(dataLibraryGroup);
        if (MethodType.INSERT == dataLibrary.getMethodType()) {
            dataLibraryGroup.setDataLibraryCount(dbDataLibraryGroup.getDataLibraryCount() + 1);
        }

        dataLibraryGroupService.updateDataLibraryGroup(dataLibraryGroup);
    }

    /**
     * TODO 경도 -180 ~ 180, 위도 -90 ~ 90 추가
     * 경위도 유효성 검증
     * @param longitude	경도
     * @param latitude	위도
     * @return 유효한 값일 경우 true, 아닐경우 false
     */
    private boolean isNotNull(BigDecimal longitude, BigDecimal latitude) {
        return longitude != null && latitude != null;
    }

    /**
     * 실패한 데이터 라이브러리의 데이터 라이브러리 삭제, 데이터 그룹의 데이터 갯수 -1
     * TODO update 문 하나로 하는 것이 맞을 듯
     * @param dataLibraryList	dataLibraryList
     */
    private void deleteFailDataLibrary(List<DataLibrary> dataLibraryList) {
        for(DataLibrary dataLibrary : dataLibraryList) {
            DataLibraryGroup dataLibraryGroup = new DataLibraryGroup();
            dataLibraryGroup.setDataLibraryGroupId(dataLibraryGroup.getDataLibraryGroupId());
            dataLibraryGroup.setDataLibraryCount(-1);
            dataLibraryGroupService.updateDataLibraryGroupChildren(dataLibraryGroup);
        }
    }
}
