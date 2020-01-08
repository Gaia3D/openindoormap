package io.openindoormap.service.impl;

import java.io.File;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import io.openindoormap.config.AMQPConfig;
import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.domain.ConverterJob;
import io.openindoormap.domain.ConverterJobFile;
import io.openindoormap.domain.ConverterTarget;
import io.openindoormap.domain.DataInfo;
import io.openindoormap.domain.F4D;
import io.openindoormap.domain.Project;
import io.openindoormap.domain.UploadData;
import io.openindoormap.domain.UploadDataFile;
import io.openindoormap.persistence.ConverterMapper;
import io.openindoormap.service.ConverterService;
import io.openindoormap.service.DataService;
import io.openindoormap.service.ProjectService;
import io.openindoormap.service.UploadDataService;
import io.openindoormap.util.FileUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConverterServiceImpl implements ConverterService {

	// @Autowired
	// private AMQPPublishService aMQPPublishService;
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@Autowired
	private PropertiesConfig propertiesConfig;

	@Autowired
	private DataService dataService;
	
	@Autowired
	private ProjectService projectService;

	@Autowired
	private UploadDataService uploadDataService;
	
	@Autowired
	private ConverterMapper converterMapper;
	
	/**
	 * converter job 총 건수
	 * @param converterJob
	 * @return
	 */
	@Transactional(readOnly=true)
	public Long getListConverterJobTotalCount(ConverterJob converterJob) {
		return converterMapper.getListConverterJobTotalCount(converterJob);
	}
	
	/**
	 * converter job file 총 건수
	 * @param converterJobFile
	 * @return
	 */
	@Transactional(readOnly=true)
	public Long getListConverterJobFileTotalCount(ConverterJobFile converterJobFile) {
		return converterMapper.getListConverterJobFileTotalCount(converterJobFile);
	}
	
	/**
	 * converter job 목록
	 * @param converterLog
	 * @return
	 */
	@Transactional(readOnly=true)
	public List<ConverterJob> getListConverterJob(ConverterJob converterJob) {
		return converterMapper.getListConverterJob(converterJob);
	}
	
	/**
	 * converter job file 목록
	 * @param converterJobFile
	 * @return
	 */
	@Transactional(readOnly=true)
	public List<ConverterJobFile> getListConverterJobFile(ConverterJobFile converterJobFile) {
		return converterMapper.getListConverterJobFile(converterJobFile);
	}
	
	/**
	 * converter 변환
	 * @param converterJob
	 * @return
	 */
	@Transactional
	public void insertConverterJob(ConverterJob converterJob) {
		
		// String projectRootPath = CacheManager.getPolicy().getGeo_data_path() + File.separator;
		// String title = converterJob.getTitle();

		String projectRootPath = propertiesConfig.getUserConverterDir() + File.separator;
		Long upload_data_id = converterJob.getUpload_data_id();
		String userId = converterJob.getUser_id();


		// 1. job을 하나씩 등록
		// ConverterJob inConverterJob = new ConverterJob();
		// inConverterJob.setUpload_data_id(Long.valueOf(upload_data_id));
		// inConverterJob.setUser_id(userId);
		// inConverterJob.setTitle(title);
		// inConverterJob.setConverter_type(converterType);
		
		UploadData uploadData = new UploadData();
		uploadData.setUser_id(userId);
		uploadData.setUpload_data_id(upload_data_id);
		uploadData.setConverter_target_yn(ConverterTarget.Y.name());
		List<UploadDataFile> uploadDataFileList = uploadDataService.getListUploadDataFile(uploadData);
		
		converterJob.setFile_count(uploadDataFileList.size());
		converterMapper.insertConverterJob(converterJob);
		
		// job 고유번호
		Long converter_job_id = converterJob.getConverter_job_id();
		for(UploadDataFile uploadDataFile : uploadDataFileList) {
			ConverterJobFile converterJobFile = new ConverterJobFile();
			converterJobFile.setConverter_job_id(converter_job_id);
			converterJobFile.setUpload_data_id(upload_data_id);
			converterJobFile.setUpload_data_file_id(uploadDataFile.getUpload_data_file_id());
			converterJobFile.setProject_id(uploadDataFile.getProject_id());
			converterJobFile.setUser_id(userId);
			
			// 2. job file을 하나씩 등록
			converterMapper.insertConverterJobFile(converterJobFile);
			
			// 3. 데이터를 등록
			insertData(userId, uploadDataFile);
			
			// queue 를 실행
			executeConverter(projectRootPath, converterJobFile, uploadDataFile);
		}
		
		Project project = new Project();
		projectService.updateProject(project);
	}
	
	private void executeConverter(String projectRootPath, ConverterJobFile converterJobFile, UploadDataFile uploadDataFile) {
		Project project = new Project();
		project.setProject_id(uploadDataFile.getProject_id());
		project = projectService.getProject(project);
		
		F4D f4d = new F4D();
		f4d.setConverter_job_id(converterJobFile.getConverter_job_id());
		f4d.setInputPath(uploadDataFile.getFile_path());
		f4d.setOutputPath(projectRootPath + project.getProject_path());
		f4d.setLogFile(projectRootPath + project.getProject_path() + "logTest.txt");
		//f4d.setUnitScaleFactor(0.1);
		f4d.setCreateIndex(true);
		f4d.setMeshType(0);
		
		// TODO
		// 조금 미묘하다. transaction 처리를 할지, 관리자 UI 재 실행을 위해서는 여기가 맞는거 같기도 하고....
		// 별도 기능으로 분리해야 하나?
		try {
			// aMQPPublishService.send(queueMessage);
			System.out.println("============== Converting... ==============");
			FileUtil.makeDirectory(f4d.getOutputPath());
			rabbitTemplate.convertAndSend(AMQPConfig.INCOMING_QUEUE_NAME, f4d);
			System.out.println("Send message = " + f4d);
		} catch(Exception ex) {
			ConverterJob converterJob = new ConverterJob();
			converterJob.setConverter_job_id(converterJobFile.getConverter_job_id());
			converterJob.setStatus(ConverterJob.JOB_CONFIRM);
			converterJob.setError_code(ex.getMessage());
			converterMapper.updateConverterJob(converterJob);
			
			ex.printStackTrace();
		}
	}
	
	private void insertData(String userId, UploadDataFile uploadDataFile) {
		DataInfo rootDataInfo = dataService.getRootDataByProjectId(uploadDataFile.getProject_id());
		int order = 1;
		// TODO nodeType 도 입력해야 함
		String attributes = "{\"isPhysical\": true}";
		
		DataInfo dataInfo = new DataInfo();
		dataInfo.setProject_id(uploadDataFile.getProject_id());
		dataInfo.setSharing_type(uploadDataFile.getSharing_type());
		dataInfo.setData_key(uploadDataFile.getFile_real_name().substring(0, uploadDataFile.getFile_real_name().lastIndexOf(".")));
		dataInfo.setData_name(uploadDataFile.getFile_name().substring(0, uploadDataFile.getFile_name().lastIndexOf(".")));
		dataInfo.setUser_id(userId);
		dataInfo.setParent(rootDataInfo.getData_id());
		dataInfo.setDepth(rootDataInfo.getDepth() + 1);
		dataInfo.setLatitude(uploadDataFile.getLatitude());
		dataInfo.setLongitude(uploadDataFile.getLongitude());
		dataInfo.setHeight(uploadDataFile.getHeight());
		dataInfo.setLocation("POINT(" + dataInfo.getLongitude() + " " + dataInfo.getLatitude() + ")");
		dataInfo.setView_order(order);
		dataInfo.setAttributes(attributes);
		dataService.insertData(dataInfo);
	}
	
	/**
	 * update
	 * @param converterJob
	 */
	@Transactional
	public void updateConverterJob(ConverterJob converterJob) {
		converterMapper.updateConverterJob(converterJob);
	}

	/**
	 * converter 변환
	 * @param userId
	 * @param checkIds
	 * @param converterJob
	 * @return
	 */
	@Transactional
	public int insertConverter(String userId, String checkIds, ConverterJob converterJob) {
		// String projectRootPath = CacheManager.getPolicy().getGeo_data_path() + File.separator;
		String projectRootPath = propertiesConfig.getUserConverterDir() + File.separator;
		String title = converterJob.getTitle();
		String converterType = converterJob.getConverter_type();
		
		String[] uploadDataIds = checkIds.split(",");
		for(String upload_data_id : uploadDataIds) {
			// 1. job을 하나씩 등록
			ConverterJob inConverterJob = new ConverterJob();
			inConverterJob.setUpload_data_id(Long.valueOf(upload_data_id));
			inConverterJob.setUser_id(userId);
			inConverterJob.setTitle(title);
			inConverterJob.setConverter_type(converterType);
			
			UploadData uploadData = new UploadData();
			uploadData.setUser_id(userId);
			uploadData.setUpload_data_id(Long.valueOf(upload_data_id));

			uploadData = uploadDataService.getUploadData(uploadData);
			uploadData.setConverter_count(uploadData.getConverter_count()+1);
			uploadData.setConverter_target_yn(ConverterTarget.Y.name());
			uploadDataService.updateUploadData(uploadData);
			List<UploadDataFile> uploadDataFileList = uploadDataService.getListUploadDataFile(uploadData);
			
			inConverterJob.setFile_count(uploadDataFileList.size());
			converterMapper.insertConverterJob(inConverterJob);
			
			Long converter_job_id = inConverterJob.getConverter_job_id();
			for(UploadDataFile uploadDataFile : uploadDataFileList) {
				ConverterJobFile converterJobFile = new ConverterJobFile();
				converterJobFile.setConverter_job_id(converter_job_id);
				converterJobFile.setUpload_data_id(Long.valueOf(upload_data_id));
				converterJobFile.setUpload_data_file_id(uploadDataFile.getUpload_data_file_id());
				converterJobFile.setProject_id(uploadDataFile.getProject_id());
				converterJobFile.setUser_id(userId);
				
				// 2. job file을 하나씩 등록
				converterMapper.insertConverterJobFile(converterJobFile);
				
				// 3. 데이터를 등록
				insertData(userId, uploadDataFile);
				
				// queue 를 실행
				executeConverter(projectRootPath, converterJobFile, uploadDataFile);
			}
			
			Project project = new Project();
			//project.setProject_id(project_id);
			projectService.updateProject(project);
		}
		
		return uploadDataIds.length;
	}

	@Override
	public void insertConverter(ConverterJob converterJob) {

	}
}
