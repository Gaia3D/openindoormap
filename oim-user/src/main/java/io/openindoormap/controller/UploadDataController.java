package io.openindoormap.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.domain.CacheManager;
import io.openindoormap.domain.ConverterTarget;
import io.openindoormap.domain.FileType;
import io.openindoormap.domain.PageType;
import io.openindoormap.domain.Pagination;
import io.openindoormap.domain.Policy;
import io.openindoormap.domain.UploadData;
import io.openindoormap.domain.UploadDataFile;
import io.openindoormap.domain.UploadDirectoryType;
import io.openindoormap.domain.UserSession;
import io.openindoormap.service.UploadDataService;
import io.openindoormap.util.DateUtil;
import io.openindoormap.util.FileUtil;
import io.openindoormap.util.FormatUtil;
import io.openindoormap.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * UploadData
 *
 */
@Slf4j
@Controller
@RequestMapping("/upload-data")
public class UploadDataController {
	
	// 설계 파일 안의 texture 의 경우 설계 파일에서 참조하는 경우가 있으므로 이름 변경 불가.
	private final String[] RENAME_FILE_TYPE = {"3ds", "obj", "dae", "ifc", "citygml", "indoorgml"};
	
	// 파일 copy 시 버퍼 사이즈
	public static final int BUFFER_SIZE = 8192;
	
	@Autowired
	private PropertiesConfig propertiesConfig;
	
	@Autowired
	private UploadDataService uploadDataService;
	
	/**
	 * 파일 등록 화면
	 * @param model
	 * @return
	 */
	@GetMapping("/input-upload-data")
	public String inputUploadData(HttpServletRequest request, Model model) {
		model.addAttribute("uploadData", new UploadData());
		return "upload-data/input-upload-data";
	}
	
	/**
	 * TODO 비동기로 처리해야 할듯
	 * data upload 처리
	 * @param model
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@PostMapping("/insert-upload-data")
	@ResponseBody
	public Map<String, Object> insertUploadData(MultipartHttpServletRequest request) {
		// return () -> {
			// TODO 파일 목록에 zip 파일이 포함되어 있는 경우 어떻게 가져갈지
			
			Map<String, Object> map = new HashMap<>();
			String result = "success";
			
			List<String> renamePossibleFileList = Arrays.asList(RENAME_FILE_TYPE);
			try {
				String errorCode = dataValidate(request);
				if(!StringUtils.isEmpty(errorCode)) {
					log.info("@@@@@@@@@@@@ errorCode = {}", errorCode);
					map.put("result", errorCode);
					return map;
				}
				
				// UserSession userSession = (UserSession)request.getSession().getAttribute(UserSession.KEY);
				// String userId = userSession.getUser_id();
				String userId = "guest";
				List<UploadDataFile> uploadDataFileList = new ArrayList<>();
				Map<String, MultipartFile> fileMap = request.getFileMap();
				Policy policy = CacheManager.getPolicy();
				
				Map<String, Object> uploadMap = null;
				String today = DateUtil.getToday(FormatUtil.YEAR_MONTH_DAY_TIME14);
				
				// 1 directory 생성
				String makedDirectory = FileUtil.makeDirectory(userId, UploadDirectoryType.YEAR_MONTH, propertiesConfig.getUploadData());
				log.info("@@@@@@@ = {}", makedDirectory);
				
				// 2 한건이면서 zip 의 경우
				boolean isZipFile = false;
				int fileCount = fileMap.values().size();
				if(fileCount == 1) {
					//processAsync(policy, userId, fileMap, makedDirectory);
					for (MultipartFile multipartFile : fileMap.values()) {
						String[] divideNames = multipartFile.getOriginalFilename().split("\\.");
						String fileExtension = divideNames[divideNames.length - 1];
						if(UploadData.ZIP_EXTENSION.equals(fileExtension.toLowerCase())) {
							isZipFile = true;
							// zip 파일
							uploadMap = unzip(policy, today, userId, multipartFile, makedDirectory);
							uploadDataFileList = (List<UploadDataFile>)uploadMap.get("uploadDataFileList");
						}
					}
				}
				
				if(!isZipFile) {
					// 3 그 외의 경우는 재귀적으로 파일 복사
					for (MultipartFile multipartFile : fileMap.values()) {
						log.info("@@@@@@@@@@@@@@@ name = {}, original_name = {}", multipartFile.getName(), multipartFile.getOriginalFilename());
						
						UploadDataFile uploadDataFile = new UploadDataFile();
						String converterTargetYn = ConverterTarget.N.name();
						
						// 파일 기본 validation 체크
						errorCode = fileValidate(policy, multipartFile);
						if(!StringUtils.isEmpty(errorCode)) {
							log.info("@@@@@@@@@@@@ errorCode = {}", errorCode);
							map.put("result", errorCode);
							return map;
						}
						
						String originalName = multipartFile.getOriginalFilename();
						String[] divideFileName = originalName.split("\\.");
	        			String saveFileName = userId + "_" + today + "_" + System.nanoTime();
	        			String tempDirectory = userId + "_" + System.nanoTime();
	        			String extension = null;
	        			if(divideFileName != null && divideFileName.length != 0) {
	        				extension = divideFileName[divideFileName.length - 1];
	        				if(UploadData.ZIP_EXTENSION.equals(extension.toLowerCase())) {
	        					log.info("@@@@@@@@@@@@ upload.file.type.invalid");
	    						map.put("result", "upload.file.type.invalid");
	    						return map;
	        				}
	        				
	        				if(renamePossibleFileList.contains(extension)) {
	        					saveFileName = saveFileName + "." + extension;
	        					converterTargetYn = ConverterTarget.Y.name();
	        				} else {
	        					saveFileName = originalName;
	        				}
	        			}
	        			
						// 파일을 upload 디렉토리로 복사
						FileUtil.makeDirectory(makedDirectory + tempDirectory);
	        			long size = 0L;
						try (	InputStream inputStream = multipartFile.getInputStream();
								OutputStream outputStream = new FileOutputStream(makedDirectory + tempDirectory + File.separator + saveFileName)) {
						
							int bytesRead = 0;
							byte[] buffer = new byte[BUFFER_SIZE];
							while ((bytesRead = inputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
								size += bytesRead;
								outputStream.write(buffer, 0, bytesRead);
							}
						
							uploadDataFile.setFile_type(FileType.FILE.getValue());
							uploadDataFile.setFile_ext(extension);
	            			uploadDataFile.setFile_name(multipartFile.getOriginalFilename());
	            			uploadDataFile.setFile_real_name(saveFileName);
	            			uploadDataFile.setFile_path(makedDirectory + tempDirectory + File.separator);
	            			uploadDataFile.setFile_sub_path(tempDirectory);
	            			uploadDataFile.setFile_size(String.valueOf(size));
	            			uploadDataFile.setConverter_target_yn(converterTargetYn);
	            			uploadDataFile.setDepth(1);
						} catch(Exception e) {
							e.printStackTrace();
							uploadDataFile.setError_message(e.getMessage());
						}
	
						uploadDataFileList.add(uploadDataFile);
			        }
				}
	
				UploadData uploadData = new UploadData();
				uploadData.setProject_id(Integer.valueOf(request.getParameter("project_id")));
				uploadData.setSharing_type(request.getParameter("sharing_type"));
				uploadData.setData_type(request.getParameter("data_type"));
				uploadData.setData_name(request.getParameter("data_name"));
				uploadData.setUser_id(userId);
				uploadData.setLatitude(new BigDecimal(request.getParameter("latitude")));
				uploadData.setLongitude(new BigDecimal(request.getParameter("longitude")));
				uploadData.setHeight(new BigDecimal(request.getParameter("height")));
				uploadData.setDescription(request.getParameter("description"));
				if(isZipFile) {
					uploadData.setCompress_yn("Y");
				} else {
					uploadData.setCompress_yn("N");
				}
				uploadData.setFile_count(uploadDataFileList.size());
				
				uploadDataService.insertUploadData(uploadData, uploadDataFileList);       
			} catch(Exception e) {
				e.printStackTrace();
				result = "db.exception";
			}
			
			map.put("result", result);
			return map;
		// };
	}
	
	/**
	 * 업로딩 파일을 압축 해제
	 * @param policy
	 * @param today
	 * @param userId
	 * @param multipartFile
	 * @param targetDirectory
	 * @return
	 * @throws Exception
	 */
	private Map<String, Object> unzip(Policy policy, String today, String userId, MultipartFile multipartFile, String targetDirectory) throws Exception {
		Map<String, Object> result = new HashMap<>();
		String errorCode = fileValidate(policy, multipartFile);
		if(!StringUtils.isEmpty(errorCode)) {
			result.put("errorCode", errorCode);
			return result;
		}
		
		List<String> renamePossibleFileList = Arrays.asList(RENAME_FILE_TYPE);
		
		// input directory 생성
		targetDirectory = targetDirectory + userId + "_" + System.nanoTime() + File.separator;
		FileUtil.makeDirectory(targetDirectory);

		List<UploadDataFile> uploadDataFileList = new ArrayList<>();
		
		log.debug("Create Directory : " + targetDirectory);
		File uploadedFile = new File(targetDirectory + multipartFile.getOriginalFilename());
		multipartFile.transferTo(uploadedFile);
		log.debug("Copy file : " + uploadedFile.getName());
		
		try ( ZipFile zipFile = new ZipFile(uploadedFile);) {
			String directoryPath = targetDirectory;
			String subDirectoryPath = "";
			String directoryName = null;
			int depth = 1;
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while( entries.hasMoreElements() ) {
            	UploadDataFile uploadDataFile = new UploadDataFile();
            	
            	ZipEntry entry = entries.nextElement();
            	String unzipfileName = Paths.get(targetDirectory + entry.getName()).normalize().toString();
            	String converterTargetYn = ConverterTarget.N.name();
            	if( entry.isDirectory() ) {
            		uploadDataFile.setFile_type(FileType.DIRECTORY.getValue());
            		if(directoryName == null) {
            			uploadDataFile.setFile_name(entry.getName());
            			uploadDataFile.setFile_real_name(entry.getName());
            			directoryName = entry.getName();
            			directoryPath = directoryPath + File.separator + directoryName;
            		} else {
            			String fileName = entry.getName().substring(entry.getName().indexOf(directoryName) + directoryName.length());  
            			uploadDataFile.setFile_name(fileName);
            			uploadDataFile.setFile_real_name(fileName);
            			directoryName = fileName;
            			directoryPath = directoryPath + File.separator + fileName;
            			subDirectoryPath = fileName;
            		}
					
					directoryPath = Paths.get(directoryPath).normalize().toString();
					subDirectoryPath = Paths.get(subDirectoryPath).normalize().toString();

                	File file = new File(unzipfileName);
                    file.mkdirs();
                    uploadDataFile.setFile_path(directoryPath);
                    uploadDataFile.setFile_sub_path(subDirectoryPath);
                    uploadDataFile.setDepth(depth);
                    depth++;
            	} else {
            		String fileName = null;
            		String extension = null;
            		String[] divideFileName = null;
            		String saveFileName = null;
            		if(directoryName == null) {
            			fileName = entry.getName();
            			divideFileName = fileName.split("\\.");
            			saveFileName = fileName;
            			if(divideFileName != null && divideFileName.length != 0) {
            				extension = divideFileName[divideFileName.length - 1];
            				if(renamePossibleFileList.contains(extension)) {
            					saveFileName = userId + "_" + today + "_" + System.nanoTime() + "." + extension;
            					converterTargetYn = ConverterTarget.Y.name();
	        				}
            			}
            		} else {
            			fileName = entry.getName().substring(entry.getName().indexOf(directoryName) + directoryName.length());  
            			divideFileName = fileName.split("\\.");
            			saveFileName = fileName;
            			if(divideFileName != null && divideFileName.length != 0) {
            				extension = divideFileName[divideFileName.length - 1];
            				if(renamePossibleFileList.contains(extension)) {
            					saveFileName = userId + "_" + today + "_" + System.nanoTime() + "." + extension;
            					converterTargetYn = ConverterTarget.Y.name();
	        				}
            			}
            		}
            		
                	try ( 	InputStream inputStream = zipFile.getInputStream(entry);
                			FileOutputStream outputStream = new FileOutputStream(directoryPath + File.separator + saveFileName); ) {
                		int data = inputStream.read();
                		while(data != -1){
                			outputStream.write(data);
                            data = inputStream.read();
                        }
                		
                		uploadDataFile.setFile_type(FileType.FILE.getValue());
                		uploadDataFile.setFile_ext(extension);
                		uploadDataFile.setFile_name(fileName);
                		uploadDataFile.setFile_real_name(saveFileName);
                		uploadDataFile.setFile_path(directoryPath);
                		uploadDataFile.setFile_sub_path(subDirectoryPath);
                		uploadDataFile.setDepth(depth);
                    } catch(Exception e) {
                    	e.printStackTrace();
                    	uploadDataFile.setError_message(e.getMessage());
                    }
                }
            	uploadDataFile.setConverter_target_yn(converterTargetYn);
            	uploadDataFile.setFile_size(String.valueOf(entry.getSize()));
            	uploadDataFileList.add(uploadDataFile);
            }
		} catch(IOException ex) {
			ex.printStackTrace(); 
		}
		
		result.put("uploadDataFileList", uploadDataFileList);
		return result;
	}
	
	/**
	 * 업로딩 파일 목록
	 * @param request
	 * @param uploadData
	 * @param pageNo
	 * @param model
	 * @return
	 */
	@RequestMapping("/list-upload-data")
	public String listUploadData(HttpServletRequest request, UploadData uploadData, @RequestParam(defaultValue="1") String pageNo, Model model) {
		log.info("@@ uploadData = {}", uploadData);
		
		// UserSession userSession = (UserSession)request.getSession().getAttribute(UserSession.KEY);
		// String userId = userSession.getUser_id();
		String userId = "guest";
		uploadData.setUser_id(userId);
		
		if(StringUtil.isNotEmpty(uploadData.getStart_date())) {
			uploadData.setStart_date(uploadData.getStart_date().substring(0, 8) + DateUtil.START_TIME);
		}
		if(StringUtil.isNotEmpty(uploadData.getEnd_date())) {
			uploadData.setEnd_date(uploadData.getEnd_date().substring(0, 8) + DateUtil.END_TIME);
		}
		long totalCount = uploadDataService.getUploadDataTotalCount(uploadData);
		
		Pagination pagination = new Pagination(	request.getRequestURI(), 
				getSearchParameters(PageType.LIST, request, uploadData), 
				totalCount, 
				Long.valueOf(pageNo).longValue(), 
				uploadData.getList_counter());
		log.info("@@ pagination = {}", pagination);
		
		uploadData.setOffset(pagination.getOffset());
		uploadData.setLimit(pagination.getPageRows());
		List<UploadData> uploadDataList = new ArrayList<>();
		if(totalCount > 0l) {
			uploadDataList = uploadDataService.getListUploadData(uploadData);
		}
		
		model.addAttribute(pagination);
		model.addAttribute("uploadDataList", uploadDataList);
		
		return "upload-data/list-upload-data";
	}
	
	/**
	 * UploadData 정보
	 * @param data_id
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "detail-upload-data")
	public String detailData(@RequestParam("upload_data_id") String upload_data_id, HttpServletRequest request, Model model) {
		
		// String listParameters = getSearchParameters(PageType.DETAIL, request, null);
		UploadData uploadData = new UploadData();
		uploadData.setUser_id("guest");
		uploadData.setUpload_data_id(Long.valueOf(upload_data_id));
		uploadData = uploadDataService.getUploadData(uploadData);
		List<UploadDataFile> uploadDataFileList = uploadDataService.getListUploadDataFile(uploadData);

		model.addAttribute("uploadData", uploadData);
		model.addAttribute("uploadDataFileList", uploadDataFileList);

		return "upload-data/detail-upload-data";
	}

	/**
	 * @param policy
	 * @param multipartFile
	 * @return
	 */
	private static String fileValidate(Policy policy, MultipartFile multipartFile) {
		
		// 2 파일 이름
		String fileName = multipartFile.getOriginalFilename();
//		if(fileName == null) {
//			log.info("@@ fileName is null");
//			uploadLog.setError_code("fileinfo.name.invalid");
//			return uploadLog;
//		} else if(fileName.indexOf("..") >= 0 || fileName.indexOf("/") >= 0) {
//			// TODO File.seperator 정규 표현식이 안 먹혀서 이렇게 처리함
//			log.info("@@ fileName = {}", fileName);
//			uploadLog.setError_code("fileinfo.name.invalid");
//			return uploadLog;
//		}
		
		// 3 파일 확장자
		String[] fileNameValues = fileName.split("\\.");
//		if(fileNameValues.length != 2) {
//			log.info("@@ fileNameValues.length = {}, fileName = {}", fileNameValues.length, fileName);
//			uploadLog.setError_code("fileinfo.name.invalid");
//			return uploadLog;
//		}
//		if(fileNameValues[0].indexOf(".") >= 0 || fileNameValues[0].indexOf("..") >= 0) {
//			log.info("@@ fileNameValues[0] = {}", fileNameValues[0]);
//			uploadLog.setError_code("fileinfo.name.invalid");
//			return uploadLog;
//		}
		// LowerCase로 비교
		String extension = fileNameValues[fileNameValues.length - 1];
//		List<String> extList = new ArrayList<String>();
//		if(policy.getUser_upload_type() != null && !"".equals(policy.getUser_upload_type())) {
//			String[] uploadTypes = policy.getUser_upload_type().toLowerCase().split(",");
//			extList = Arrays.asList(uploadTypes);
//		}
//		if(!extList.contains(extension.toLowerCase())) {
//			log.info("@@ extList = {}, extension = {}", extList, extension);
//			uploadLog.setError_code("fileinfo.ext.invalid");
//			return uploadLog;
//		}
		
		// 4 파일 사이즈
		// TODO data object attribute 파일은 사이즈가 커서 제한을 하지 않음
		long fileSize = multipartFile.getSize() / 1024;	// KByte
		long checkFileSize = 500 * 1024 ;				// Kbyte(default : 10Mb)
		if(policy != null)
		{
			checkFileSize = policy.getUser_upload_max_filesize() * 1024;	// Kbyte
		}
		log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@ user upload file size = {} KB", checkFileSize);
		if( fileSize > checkFileSize) {
			log.info("@@ fileSize = {} MB, user upload max filesize = {} MB", (fileSize / 1024), (checkFileSize / 1024));
			return "fileinfo.size.invalid";
		}
		
		return null;
	}
	
	/**
	 * data upload 수정
	 * @param model
	 * @return
	 */
	@GetMapping("/modify-upload-data")
	public String modifyUploadData(HttpServletRequest request, UploadData uploadData, Model model) {
		
		// UserSession userSession = (UserSession)request.getSession().getAttribute(UserSession.KEY);
		// uploadData.setUser_id(userSession.getUser_id());
		uploadData.setUser_id("guest");
		uploadData = uploadDataService.getUploadData(uploadData);
		List<UploadDataFile> uploadDataFileList = uploadDataService.getListUploadDataFile(uploadData);
		
		model.addAttribute("uploadData", uploadData);
		model.addAttribute("uploadDataFileList", uploadDataFileList);
		return "upload-data/modify-upload-data";
	}
	
	/**
	 * 등록 자료 삭제
	 * @param request
	 * @param check_ids
	 * @param model
	 * @return
	 */
	@PostMapping("/ajax-delete-upload-data")
	@ResponseBody
	public Map<String, Object> ajaxDeleteDatas(HttpServletRequest request, UploadData uploadData) {

		log.info("@@@@@@@ uploadData = {}", uploadData);
		Map<String, Object> map = new HashMap<>();
		String result = "success";
		try {
			if(uploadData.getUpload_data_id() == null || uploadData.getUpload_data_id().intValue() <=0) {
				map.put("result", "uploadData.upload_data_id.empty");
				return map;
			}

			// UserSession userSession = (UserSession)request.getSession().getAttribute(UserSession.KEY);
			// String userId = userSession.getUser_id();
			String userId = "guest";
			uploadData.setUser_id(userId);
			
			//uploadDataService.deleteUploadDatas(userSession.getUser_id(), check_ids);

			uploadDataService.deleteUploadData(uploadData);
		} catch(Exception e) {
			e.printStackTrace();
			map.put("result", "db.exception");
		}
		
		map.put("result", result	);
		return map;
	}
	
	/**
	 * validation 체크
	 * @param request
	 * @return
	 */
	private String dataValidate(MultipartHttpServletRequest request) {
		
		if(StringUtils.isEmpty(request.getParameter("sharing_type"))) {
			return "sharing.type.empty";
		}
		if(StringUtils.isEmpty(request.getParameter("data_type"))) {
			return "data.type.empty";
		}
		if(StringUtils.isEmpty(request.getParameter("project_id"))) {
			return "project.id.empty";
		}
		if(StringUtils.isEmpty(request.getParameter("latitude"))) {
			return "latitude.empty";
		}
		if(StringUtils.isEmpty(request.getParameter("longitude"))) {
			return "longitude.empty";
		}
		if(StringUtils.isEmpty(request.getParameter("height"))) {
			return "height.empty";
		}
		
		Map<String, MultipartFile> fileMap = request.getFileMap();
		if(fileMap.isEmpty()) {
			return "file.empty";
		}
		
		return null;
	}
	
	/**
	 * 검색 조건
	 * @param tokenLog
	 * @return
	 */
	private String getSearchParameters(PageType pageType, HttpServletRequest request, UploadData uploadData) {
		StringBuffer buffer = new StringBuffer();
		boolean isListPage = true;
		if(pageType.equals(PageType.MODIFY) || pageType.equals(PageType.DETAIL)) {
			isListPage = false;
		}
		
		if(!isListPage) {
			buffer.append("pageNo=" + request.getParameter("pageNo"));
		}
		buffer.append("&");
		buffer.append("search_option=" + StringUtil.getDefaultValue(isListPage ? uploadData.getSearch_option() : request.getParameter("search_option")));
		buffer.append("&");
		try {
			buffer.append("search_value=" + URLEncoder.encode(StringUtil.getDefaultValue(
					isListPage ? uploadData.getSearch_value() : request.getParameter("search_value")), "UTF-8"));
		} catch(Exception e) {
			e.printStackTrace();
			buffer.append("search_value=");
		}
		buffer.append("&");
		buffer.append("search_word=" + StringUtil.getDefaultValue(isListPage ? uploadData.getSearch_word() : request.getParameter("search_type")));
		buffer.append("&");
		buffer.append("start_date=" + StringUtil.getDefaultValue(isListPage ? uploadData.getStart_date() : request.getParameter("start_date")));
		buffer.append("&");
		buffer.append("end_date=" + StringUtil.getDefaultValue(isListPage ? uploadData.getEnd_date() : request.getParameter("end_date")));
		buffer.append("&");
		buffer.append("order_word=" + StringUtil.getDefaultValue(isListPage ? uploadData.getOrder_word() : request.getParameter("order_word")));
		buffer.append("&");
		buffer.append("order_value=" + StringUtil.getDefaultValue(isListPage ? uploadData.getOrder_value() : request.getParameter("order_value")));
		if(!isListPage) {
			buffer.append("&");
			buffer.append("list_count=" + uploadData.getList_counter());
		}
		return buffer.toString();
	}
}