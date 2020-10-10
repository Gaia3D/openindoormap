package io.openindoormap.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.controller.AuthorizationController;
import io.openindoormap.domain.Key;
import io.openindoormap.domain.ShapeFileExt;
import io.openindoormap.domain.extrusionmodel.DesignLayer;
import io.openindoormap.domain.extrusionmodel.DesignLayerFileInfo;
import io.openindoormap.domain.policy.GeoPolicy;
import io.openindoormap.domain.policy.Policy;
import io.openindoormap.domain.user.UserSession;
import io.openindoormap.geospatial.ShapeFileParser;
import io.openindoormap.service.DesignLayerFileInfoService;
import io.openindoormap.service.DesignLayerService;
import io.openindoormap.service.GeoPolicyService;
import io.openindoormap.service.PolicyService;
import io.openindoormap.support.LogMessageSupport;
import io.openindoormap.support.ZipSupport;
import io.openindoormap.utils.FileUtils;
import io.openindoormap.utils.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
@RestController
@RequestMapping("/design-layers")
public class DesignLayerRestController implements AuthorizationController {

	@Autowired
	private GeoPolicyService geoPolicyService;
	@Autowired
    private DesignLayerService designLayerService;
    @Autowired
    private DesignLayerFileInfoService designLayerFileInfoService;
    @Autowired
    private PolicyService policyService;
    @Autowired
    private PropertiesConfig propertiesConfig;
    @Autowired
	private ObjectMapper objectMapper;

    // 파일 copy 시 버퍼 사이즈
    public static final int BUFFER_SIZE = 8192;

	/**
	 * 디자인 레이어 Key 중복 체크
	 * @param request
	 * @param designLayer
	 * @return
	 */
	@GetMapping(value = "/duplication")
	public Map<String, Object> designLayerKeyDuplicationCheck(HttpServletRequest request, DesignLayer designLayer) {
		Map<String, Object> result = new HashMap<>();
		String errorCode = null;
		String message = null;

		// TODO @Valid 로 구현해야 함
		if(StringUtils.isEmpty(designLayer.getDesignLayerKey())) {
			result.put("statusCode", HttpStatus.BAD_REQUEST.value());
			result.put("errorCode", "data.group.key.empty");
			result.put("message", message);
			return result;
		}

		Boolean duplication = designLayerService.isDesignLayerKeyDuplication(designLayer.getDesignLayerKey());
		log.info("@@ duplication = {}", duplication);
		int statusCode = HttpStatus.OK.value();

		result.put("duplication", duplication);
		result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		return result;
	}

	/**
	 * shape 파일 변환 TODO dropzone 이 파일 갯수만큼 form data를 전송해 버려서 command 패턴을(Layer
	 * layer) 사용할 수 없음 dropzone 이 예외 처리가 이상해서 BAD_REQUEST 를 던지지 않고 OK 를 넣짐
	 *
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@PostMapping(value = "/insert")
	public Map<String, Object> insert(MultipartHttpServletRequest request) {
		Map<String, Object> result = new HashMap<>();
		int statusCode = 0;
		String errorCode = null;
		String message = null;
		Map<String, Object> updateDesignLayerMap = new HashMap<>();
		try {
			errorCode = validate(request);
			if (!StringUtils.isEmpty(errorCode)) {
				result.put("statusCode", HttpStatus.BAD_REQUEST.value());
				result.put("errorCode", errorCode);
				return result;
			}
			Boolean layerKeyDuplication = designLayerService.isDesignLayerKeyDuplication(request.getParameter("designLayerKey"));
			if(layerKeyDuplication) {
				result.put("statusCode", HttpStatus.BAD_REQUEST.value());
				result.put("errorCode", "design.layer.key.duplication");
				return result;
			}

			UserSession userSession = (UserSession) request.getSession().getAttribute(Key.USER_SESSION.name());
			String userId = userSession.getUserId();

			List<DesignLayerFileInfo> designLayerFileInfoList = new ArrayList<>();
			Map<String, MultipartFile> fileMap = request.getFileMap();

			Policy policy = policyService.getPolicy();
			GeoPolicy geoPolicy = geoPolicyService.getGeoPolicy();
			String shapeEncoding = replaceInvalidValue(request.getParameter("shapeEncoding"));
			String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
			// 디자인 레이어 객체 생성
			DesignLayer designLayer = getDesignLayerFromRequest(request, userId);
			log.info("@@ designLayer = {}", designLayer);

			// 디자인 레이어 변경 횟수가 많지 않아서 년 단위로 관리할 예정
			// TODO 월 단위로 관리해야 할지도
			String makedDirectory = propertiesConfig.getDesignLayerUploadDir();
			FileUtils.makeDirectory(makedDirectory);
			makedDirectory = makedDirectory + today.substring(0, 4) + File.separator;
			FileUtils.makeDirectory(makedDirectory);
			log.info("@@@@@@@ = {}", makedDirectory);

			String groupFileName = request.getParameter("designLayerKey") + "_" + today;

			// 한건이면서 zip 의 경우
			boolean isZipFile = false;
			int fileCount = fileMap.values().size();
			log.info("********************************************* fileCount = {}", fileCount);
			if (fileCount == 1) {
				for (MultipartFile multipartFile : fileMap.values()) {
					String[] divideNames = multipartFile.getOriginalFilename().split("\\.");
					String fileExtension = divideNames[divideNames.length - 1];
					if (DesignLayerFileInfo.ZIP_EXTENSION.equalsIgnoreCase(fileExtension)) {
						isZipFile = true;
						// zip 파일
						Map<String, Object> uploadMap = unzip(policy, groupFileName, multipartFile, shapeEncoding, makedDirectory);
						designLayerFileInfoList = (List<DesignLayerFileInfo>) uploadMap.get("designLayerFileInfoList");
					}
				}
			}

			if (!isZipFile) {
				for (MultipartFile multipartFile : fileMap.values()) {
					log.info("@@@@@@@@@@@@@@@ name = {}, original_name = {}", multipartFile.getName(), multipartFile.getOriginalFilename());

					String saveFileName = groupFileName;
					DesignLayerFileInfo designLayerFileInfo = new DesignLayerFileInfo();

					// 파일 기본 validation 체크
					errorCode = fileValidate(policy, multipartFile);
					if (!StringUtils.isEmpty(errorCode)) {
						result.put("statusCode", HttpStatus.BAD_REQUEST.value());
						result.put("errorCode", errorCode);
						return result;
					}

					String originalName = multipartFile.getOriginalFilename();
					String[] divideFileName = originalName.split("\\.");
					String extension = null;
					if (divideFileName.length != 0) {
						extension = divideFileName[divideFileName.length - 1];
						if (DesignLayerFileInfo.ZIP_EXTENSION.equalsIgnoreCase(extension)) {
							log.info("@@@@@@@@@@@@ upload.file.type.invalid");
							result.put("statusCode", HttpStatus.BAD_REQUEST.value());
							result.put("errorCode", "upload.file.type.invalid");
							return result;
						}
						saveFileName = saveFileName + "." + extension;
					}

					designLayerFileInfoList.add(getCopyDesignLayerFileInfo(multipartFile, makedDirectory, saveFileName, extension, shapeEncoding, designLayerFileInfo));
				}
			}

			// shape 필수 파일 확인
			errorCode = shapeFileValidate(designLayerFileInfoList);
			if(!StringUtils.isEmpty(errorCode)) {
				log.info("@@@@@@@@@@@@ errorCode = {}", errorCode);
				result.put("statusCode", HttpStatus.BAD_REQUEST.value());
				result.put("errorCode", errorCode);
	            return result;
			}

			// shp 파일 필수 필드 확인
			ShapeFileParser shapeFileParser = new ShapeFileParser(makedDirectory + groupFileName + "." + ShapeFileExt.SHP.getValue());
//			if(!shapeFileParser.fieldValidate()) {
//				result.put("statusCode", HttpStatus.BAD_REQUEST.value());
//				result.put("errorCode", "upload.shpfile.requried");
//				return result;
//			}

			// 1. extrusion model 시뮬레이션에서 사용할 디자인 레이어 정보 parsing
			String extrusionColumns = null;
			if(DesignLayer.DesignLayerType.LAND == DesignLayer.DesignLayerType.valueOf(designLayer.getDesignLayerGroupType().toUpperCase())) {
				extrusionColumns = geoPolicy.getShapeLandRequiredColumns();
			} else if(DesignLayer.DesignLayerType.BUILDING == DesignLayer.DesignLayerType.valueOf(designLayer.getDesignLayerGroupType().toUpperCase())) {
				extrusionColumns = geoPolicy.getShapeBuildingRequiredColumns();
			} else if(DesignLayer.DesignLayerType.BUILDING_HEIGHT == DesignLayer.DesignLayerType.valueOf(designLayer.getDesignLayerGroupType().toUpperCase())) {
				extrusionColumns = geoPolicy.getShapeBuildingHeightRequiredColumns();
			}

			if(StringUtils.isEmpty(extrusionColumns)) {
				result.put("statusCode", HttpStatus.BAD_REQUEST.value());
				result.put("errorCode", "upload.designlayer.policy");
				return result;
			}

			// 2. 레이어 기본 정보 및 레이어 이력 정보 등록
			updateDesignLayerMap = designLayerService.insertDesignLayer(designLayer, designLayerFileInfoList);
			log.info("++++++++++++++++++++++++++++++++-------------------------- designLayerFileInfoList = {}", designLayerFileInfoList);
			if (!designLayerFileInfoList.isEmpty()) {
				// 3. geometry 정보 insert
				List<DesignLayer> shapePropertiesList = shapeFileParser.getExtrusionModelList(objectMapper, extrusionColumns);
				log.info("++++++++++++++++++++++++++++++++-------------------------- shapePropertiesList = {}", shapePropertiesList);
				designLayerService.insertShapeInfo(designLayer, shapePropertiesList);
				// 4. 속성 파일이 있다면 업데이트(geometry insert 후에 versionId 가 없는 것들 중에서 업데이트)
				String attributeFileName = getAttributeFile(designLayerFileInfoList);
				if(attributeFileName != null) {
					designLayerService.updateDesignLayerAttributes(attributeFileName, designLayer.getDesignLayerGroupType());
				}
				// org2ogr 로 등록한 데이터의 version을 갱신
				Map<String, String> orgMap = new HashMap<>();
				orgMap.put("fileVersion", ((Integer)updateDesignLayerMap.get("fileVersion")).toString());
				orgMap.put("designLayerGroupType", designLayer.getDesignLayerGroupType());
				// 5. shape 파일 테이블의 현재 데이터를 활성화
				designLayerFileInfoService.updateDataFileVersion(orgMap);
				// 6. geoserver에 신규 등록일 경우 등록, 아닐경우 통과
				designLayerService.registerDesignLayer(geoPolicy, designLayer);
				designLayerService.updateDesignLayerStyle(designLayer);
			}

			statusCode = HttpStatus.OK.value();
		} catch(DataAccessException e) {
			// ogr2ogr2 실행하다가 에러날경우 이미 들어간 레이어, 레이러 파일정보 삭제
			Long designLayerId = (Long) updateDesignLayerMap.get("designLayerId");
			Long designLayerFileInfoTeamId = (Long) updateDesignLayerMap.get("designLayerFileInfoTeamId");
			designLayerService.deleteDesignLayer(designLayerId);
			designLayerFileInfoService.deleteDesignLayerFileInfoByTeamId(designLayerFileInfoTeamId);
			
			statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
			errorCode = "db.exception";
			message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
			LogMessageSupport.printMessage(e, "@@ db.exception. message = {}", message);
		} catch(RuntimeException e) {
			// ogr2ogr2 실행하다가 에러날경우 이미 들어간 레이어, 레이러 파일정보 삭제 
			Long designLayerId = (Long) updateDesignLayerMap.get("designLayerId");
			Long designLayerFileInfoTeamId = (Long) updateDesignLayerMap.get("designLayerFileInfoTeamId");
			designLayerService.deleteDesignLayer(designLayerId);
			designLayerFileInfoService.deleteDesignLayerFileInfoByTeamId(designLayerFileInfoTeamId);
			
			statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
			errorCode = "runtime.exception";
			message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
			LogMessageSupport.printMessage(e, "@@ runtime.exception. message = {}", message);
		} catch(Exception e) {
			// ogr2ogr2 실행하다가 에러날경우 이미 들어간 레이어, 레이러 파일정보 삭제 
			Long designLayerId = (Long) updateDesignLayerMap.get("designLayerId");
			Long designLayerFileInfoTeamId = (Long) updateDesignLayerMap.get("designLayerFileInfoTeamId");
			designLayerService.deleteDesignLayer(designLayerId);
			designLayerFileInfoService.deleteDesignLayerFileInfoByTeamId(designLayerFileInfoTeamId);
			
			statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
			errorCode = "unknown.exception";
			message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
			LogMessageSupport.printMessage(e, "@@ exception. message = {}", message);
		}

		result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		return result;
	}

	/**
	 * shape 파일 변환
	 * TODO dropzone 이 파일 갯수만큼 form data를 전송해 버려서 command 패턴을(Layer layer) 사용할 수 없음
	 * dropzone 이 예외 처리가 이상해서 BAD_REQUEST 를 던지지 않고 OK 를 넣짐
	 * @param request
	 * @param designLayerId
	 * @return
	 */
    @SuppressWarnings("unchecked")
	@PostMapping(value = "/update/{designLayerId:[0-9]+}")
    public Map<String, Object> update(MultipartHttpServletRequest request, @PathVariable("designLayerId") Long designLayerId) {

    	Map<String, Object> result = new HashMap<>();
		int statusCode = 0;
		String errorCode = null;
		String message = null;

        boolean isRollback = false;
        DesignLayer rollbackLayer = new DesignLayer();
        boolean isDesignDesignLayerFileInfoExist = false;
        DesignLayerFileInfo rollbackDesignLayerFileInfo = null;
        Long deleteDesignLayerFileInfoTeamId = null;

        try {
            errorCode = validate(request);
            if(!StringUtils.isEmpty(errorCode)) {
            	result.put("statusCode", HttpStatus.BAD_REQUEST.value());
                result.put("errorCode", errorCode);
                return result;
            }

            UserSession userSession = (UserSession)request.getSession().getAttribute(Key.USER_SESSION.name());
            String userId = userSession.getUserId();

            List<DesignLayerFileInfo> designLayerFileInfoList = new ArrayList<>();
            Map<String, MultipartFile> fileMap = request.getFileMap();

            Policy policy = policyService.getPolicy();
			GeoPolicy geoPolicy = geoPolicyService.getGeoPolicy();
			String shapeEncoding = replaceInvalidValue(request.getParameter("shapeEncoding"));
			String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            // layer 변경 횟수가 많지 않아서 년 단위로 관리할 예정
            String makedDirectory = propertiesConfig.getLayerUploadDir();
            FileUtils.makeDirectory(makedDirectory);
            makedDirectory = makedDirectory + today.substring(0,4) + File.separator;
			FileUtils.makeDirectory(makedDirectory);
            log.info("@@@@@@@ = {}", makedDirectory);

            // 1. 롤백 처리를 위해 기존 레이어 정보 복사
            DesignLayer designLayer = designLayerService.getDesignLayer(designLayerId);
            BeanUtils.copyProperties(designLayer, rollbackLayer);
            // layer 파일의 경우 한 세트가 같은 이름에 확장자만 달라야 한다.
            String groupFileName = designLayer.getDesignLayerKey() + "_" + today;

            // 한건이면서 zip 의 경우
            boolean isZipFile = false;
            int fileCount = fileMap.values().size();
            log.info("********************************************* fileCount = {}", fileCount);
            if(fileCount == 1) {
                for (MultipartFile multipartFile : fileMap.values()) {
                    String[] divideNames = multipartFile.getOriginalFilename().split("\\.");
                    String fileExtension = divideNames[divideNames.length - 1];
                    if(DesignLayerFileInfo.ZIP_EXTENSION.equalsIgnoreCase(fileExtension)) {
                        isZipFile = true;
                        // zip 파일
                        Map<String, Object> uploadMap = unzip(policy, groupFileName, multipartFile, shapeEncoding, makedDirectory);
                        designLayerFileInfoList = (List<DesignLayerFileInfo>)uploadMap.get("designLayerFileInfoList");
                    }
                }
            }

            if(!isZipFile) {
                for (MultipartFile multipartFile : fileMap.values()) {
                    log.info("@@@@@@@@@@@@@@@ name = {}, original_name = {}", multipartFile.getName(), multipartFile.getOriginalFilename());

                    String saveFileName = groupFileName;
                    DesignLayerFileInfo designLayerFileInfo = new DesignLayerFileInfo();

                    // 파일 기본 validation 체크
                    errorCode = fileValidate(policy, multipartFile);
                    if(!StringUtils.isEmpty(errorCode)) {
                    	result.put("statusCode", HttpStatus.BAD_REQUEST.value());
                        result.put("errorCode", errorCode);
                        return result;
                    }

					String originalName = multipartFile.getOriginalFilename();
					String[] divideFileName = originalName.split("\\.");
					String extension = null;
					if (divideFileName.length != 0) {
						extension = divideFileName[divideFileName.length - 1];
						if (DesignLayerFileInfo.ZIP_EXTENSION.equalsIgnoreCase(extension)) {
							log.info("@@@@@@@@@@@@ upload.file.type.invalid");
							result.put("statusCode", HttpStatus.BAD_REQUEST.value());
							result.put("errorCode", "upload.file.type.invalid");
							return result;
						}
						saveFileName = saveFileName + "." + extension;
					}

					designLayerFileInfoList.add(getCopyDesignLayerFileInfo(multipartFile, makedDirectory, saveFileName, extension, shapeEncoding, designLayerFileInfo));
                }
            }

            // shape 필수 파일 확인
 			errorCode = shapeFileValidate(designLayerFileInfoList);
 			if(!StringUtils.isEmpty(errorCode)) {
 				log.info("@@@@@@@@@@@@ errorCode = {}", errorCode);
 				result.put("statusCode", HttpStatus.BAD_REQUEST.value());
 				result.put("errorCode", errorCode);
 	            return result;
 			}

            // shp 파일 필수 필드 확인
 			ShapeFileParser shapeFileParser = new ShapeFileParser(makedDirectory + groupFileName + "." + ShapeFileExt.SHP.getValue());
 			/*if(!shapeFileParser.fieldValidate()) {
 				result.put("statusCode", HttpStatus.BAD_REQUEST.value());
 				result.put("errorCode", "upload.shpfile.requried");
 				return result;
 			}*/

			// 디자인 레이어 객체 생성
			designLayer = getDesignLayerFromRequest(request, userId);
			designLayer.setDesignLayerId(designLayerId);

            // 여기서 부터 문제가 생기는 것은 rollback 처리를 합니다.
            // 2. 레이어 이력 파일이 존재하는 검사
            isDesignDesignLayerFileInfoExist = designLayerFileInfoService.isDesignLayerFileInfoExist(designLayerId);
            if(isDesignDesignLayerFileInfoExist) {
                // 3. 존재하면 백업을 위해 조회
                rollbackDesignLayerFileInfo = designLayerFileInfoService.getEnableDesignLayerFileInfo(designLayerId);
            }
            log.info("----- isDesignDesignLayerFileInfoExist = {}", isDesignDesignLayerFileInfoExist);

			String extrusionColumns = null;
			if(DesignLayer.DesignLayerType.LAND == DesignLayer.DesignLayerType.valueOf(designLayer.getDesignLayerGroupType().toUpperCase())) {
				extrusionColumns = geoPolicy.getShapeLandRequiredColumns();
			} else if(DesignLayer.DesignLayerType.BUILDING == DesignLayer.DesignLayerType.valueOf(designLayer.getDesignLayerGroupType().toUpperCase())) {
				extrusionColumns = geoPolicy.getShapeBuildingRequiredColumns();
			} else if(DesignLayer.DesignLayerType.BUILDING_HEIGHT == DesignLayer.DesignLayerType.valueOf(designLayer.getDesignLayerGroupType().toUpperCase())) {
				extrusionColumns = geoPolicy.getShapeBuildingHeightRequiredColumns();
			}

			if(StringUtils.isEmpty(extrusionColumns)) {
				result.put("statusCode", HttpStatus.BAD_REQUEST.value());
				result.put("errorCode", "upload.designlayer.policy");
				return result;
			}

            // 4. 레이어 기본 정보 및 레이어 이력 정보 등록
            Map<String, Object> updateDesignLayerMap = designLayerService.updateDesignLayer(designLayer, isDesignDesignLayerFileInfoExist, designLayerFileInfoList);
            if(!designLayerFileInfoList.isEmpty()) {
                isRollback = true;

                deleteDesignLayerFileInfoTeamId = (Long)updateDesignLayerMap.get("designLayerFileInfoTeamId");
                // 4. org2ogr 실행
				List<DesignLayer> shapePropertiesList = shapeFileParser.getExtrusionModelList(objectMapper, extrusionColumns);
				designLayerService.insertShapeInfo(designLayer, shapePropertiesList);
				// 5. 속성 파일이 있다면 업데이트(geometry insert 후에 versionId 가 없는 것들 중에서 업데이트)
				String attributeFileName = getAttributeFile(designLayerFileInfoList);
				if(attributeFileName != null) {
					designLayerService.updateDesignLayerAttributes(attributeFileName, designLayer.getDesignLayerGroupType());
				}
                // org2ogr 로 등록한 데이터의 version을 갱신
                Map<String, String> orgMap = new HashMap<>();
				orgMap.put("fileVersion", ((Integer)updateDesignLayerMap.get("fileVersion")).toString());
				orgMap.put("designLayerGroupType", designLayer.getDesignLayerGroupType());
                // 6. shape 파일 테이블의 현재 데이터의 활성화 하고 날짜를 업데이트
                designLayerFileInfoService.updateDataFileVersion(orgMap);
                // 7. geoserver에 신규 등록일 경우 등록, 아닐경우 통과
                designLayerService.registerDesignLayer(geoPolicy, designLayer);
            }
			// 스타일 업데이트
			designLayerService.updateDesignLayerStyle(designLayer);

            statusCode = HttpStatus.OK.value();
        } catch(DataAccessException e) {
        	if(isRollback) {
                // rollback 처리
                designLayerService.rollbackDesignLayer(rollbackLayer, isDesignDesignLayerFileInfoExist, rollbackDesignLayerFileInfo, deleteDesignLayerFileInfoTeamId);
            }

			statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
			errorCode = "db.exception";
			message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
			LogMessageSupport.printMessage(e, "@@ db.exception. message = {}", message);
		} catch(RuntimeException e) {
			if(isRollback) {
                // rollback 처리
                designLayerService.rollbackDesignLayer(rollbackLayer, isDesignDesignLayerFileInfoExist, rollbackDesignLayerFileInfo, deleteDesignLayerFileInfoTeamId);
            }

			statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
			errorCode = "runtime.exception";
			message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
			LogMessageSupport.printMessage(e, "@@ runtime.exception. message = {}", message);
		} catch(Exception e) {
			if(isRollback) {
                // rollback 처리
                designLayerService.rollbackDesignLayer(rollbackLayer, isDesignDesignLayerFileInfoExist, rollbackDesignLayerFileInfo, deleteDesignLayerFileInfoTeamId);
            }

			statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
			errorCode = "unknown.exception";
			message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
			LogMessageSupport.printMessage(e, "@@ exception. message = {}", message);
        }

        result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		return result;
    }

	private DesignLayer getDesignLayerFromRequest(MultipartHttpServletRequest request, String userId) {
		return DesignLayer.builder()
				.urbanGroupId(Integer.valueOf(request.getParameter("urbanGroupId")))
				.designLayerGroupId(Integer.valueOf(request.getParameter("designLayerGroupId")))
				.designLayerName(request.getParameter("designLayerName"))
				.designLayerKey(request.getParameter("designLayerKey"))
				.designLayerGroupType(request.getParameter("designLayerGroupType"))
				.sharing(request.getParameter("sharing"))
				.ogcWebServices(request.getParameter("ogcWebServices"))
				.geometryType(request.getParameter("geometryType"))
				.layerLineColor(request.getParameter("layerLineColor"))
				.layerLineStyle(Float.valueOf(request.getParameter("layerLineStyle")))
				.layerFillColor(request.getParameter("layerFillColor"))
				.layerAlphaStyle(Float.valueOf(request.getParameter("layerAlphaStyle")))
				.available(Boolean.valueOf(request.getParameter("available")))
				.coordinate(request.getParameter("coordinate"))
				.description(request.getParameter("description"))
				.zIndex(Integer.valueOf(request.getParameter("zIndex")))
				.cacheAvailable(Boolean.valueOf(request.getParameter("cacheAvailable")))
				.viewOrder(Integer.valueOf(request.getParameter("viewOrder")))
				.userId(userId)
				.build();
	}

	private DesignLayerFileInfo getCopyDesignLayerFileInfo(
			MultipartFile multipartFile, String makedDirectory, String saveFileName, String extension, String shapeEncoding, DesignLayerFileInfo designLayerFileInfo) {
		long size = 0L;
		try (	InputStream inputStream = multipartFile.getInputStream();
				 OutputStream outputStream = new FileOutputStream(makedDirectory + saveFileName)) {

			int bytesRead;
			byte[] buffer = new byte[BUFFER_SIZE];
			while ((bytesRead = inputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
				size += bytesRead;
				outputStream.write(buffer, 0, bytesRead);
			}
			designLayerFileInfo.setFileExt(extension);
			designLayerFileInfo.setFileName(multipartFile.getOriginalFilename());
			designLayerFileInfo.setFileRealName(saveFileName);
			designLayerFileInfo.setFilePath(makedDirectory);
			designLayerFileInfo.setFileSize(String.valueOf(size));
			designLayerFileInfo.setShapeEncoding(shapeEncoding);
		} catch(IOException e) {
			LogMessageSupport.printMessage(e, "@@@@@@@@@@@@ IOException. message = {}", e.getMessage());
			designLayerFileInfo.setErrorMessage(e.getMessage());
		} catch(Exception e) {
			LogMessageSupport.printMessage(e, "@@@@@@@@@@@@ Exception. message = {}", e.getMessage());
			designLayerFileInfo.setErrorMessage(e.getMessage());
		}

		return designLayerFileInfo;
	}

	/**
	 * 레이어 삭제 삭제
	 * @param designLayerId
	 * @return
	 */
	@DeleteMapping(value = "/delete/{designLayerId:[0-9]+}")
	public Map<String, Object> delete(@PathVariable Long designLayerId) {
		Map<String, Object> result = new HashMap<>();
		String errorCode = null;
		String message = null;

		designLayerService.deleteDesignLayer(designLayerId);
		int statusCode = HttpStatus.OK.value();

		result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		return result;
	}
	/**
	 * shape 파일 목록
	 * @param request
	 * @param designLayerId
	 * @return
	 */
    @GetMapping(value = "/{designLayerId:[0-9]+}/layer-fileinfos")
    public Map<String, Object> listDesignLayerFileInfo(HttpServletRequest request, @PathVariable Long designLayerId) {

    	Map<String, Object> result = new HashMap<>();
		String errorCode = null;
		String message = null;

		List<DesignLayerFileInfo> designLayerFileInfoList = designLayerFileInfoService.getListDesignLayerFileInfo(designLayerId);
        int statusCode = HttpStatus.OK.value();

        result.put("designLayerFileInfoList", designLayerFileInfoList);
        result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		return result;
    }

	/**
	 * shape 파일 다운 로드
	 * @param request
	 * @param response
	 * @param designLayerId
	 * @param designLayerFileInfoTeamId
	 */
    @GetMapping(value = "/{designLayerId:[0-9]+}/layer-file-info/{designLayerFileInfoTeamId:[0-9]+}/download")
    public void download(HttpServletRequest request, HttpServletResponse response, @PathVariable Long designLayerId, @PathVariable Long designLayerFileInfoTeamId) {
        log.info("@@@@@@@@@@@@ designLayerId = {}, designLayerFileInfoTeamId = {}", designLayerId, designLayerFileInfoTeamId);
        try {

			DesignLayer layer = designLayerService.getDesignLayer(designLayerId);
            String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String filePath = propertiesConfig.getLayerExportDir() + today.substring(0, 6) + File.separator;
            String fileRealName = layer.getDesignLayerKey() + "_" + today + "_" + System.nanoTime();
            fileRealName = fileRealName.replaceAll("&", "");

			FileUtils.makeDirectory(filePath);
            log.info("@@@@@@@ zip directory = {}", filePath);

            List<DesignLayerFileInfo> designLayerFileInfoList = designLayerFileInfoService.getDesignLayerFileInfoTeam(designLayerFileInfoTeamId);
            DesignLayerFileInfo designLayerFileInfo = designLayerFileInfoList.get(0);
            designLayerFileInfo.setFilePath(filePath);
            designLayerFileInfo.setFileRealName(fileRealName);
            // db에 해당 versionId의 데이터를 shape으로 export
            designLayerService.exportOgr2Ogr(designLayerFileInfo, layer);

            String zipFileName = filePath + fileRealName + ".zip";
            List<DesignLayerFileInfo> makeFileList = new ArrayList<>();
            for(ShapeFileExt shapeFileExt : ShapeFileExt.values()) {
				DesignLayerFileInfo fileInfo = new DesignLayerFileInfo();
				fileInfo.setFilePath(filePath);
				fileInfo.setFileRealName(fileRealName + "." + shapeFileExt.getValue());
				makeFileList.add(fileInfo);
			}

            ZipSupport.makeZip(zipFileName, makeFileList);

            response.setContentType("application/force-download");
            response.setHeader("Content-Transfer-Encoding", "binary");
            setDisposition(layer.getDesignLayerName() + ".zip", request, response);

            File zipFile = new File(zipFileName);
            try(	BufferedInputStream in = new BufferedInputStream(new FileInputStream(zipFile));
                    BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream())) {

                FileCopyUtils.copy(in, out);
                out.flush();
            } catch(IOException e) {
            	LogMessageSupport.printMessage(e, "@@ IOException. message = {}", e.getMessage());
            	throw new RuntimeException(e.getMessage());
            }
        } catch(DataAccessException e) {
        	LogMessageSupport.printMessage(e, "@@ DataAccessException. message = {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        } catch(RuntimeException e) {
			LogMessageSupport.printMessage(e, "@@ RuntimeException. message = {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        } catch(IOException e) {
			LogMessageSupport.printMessage(e, "@@ FileNotFoundException. message = {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        } catch(Exception e) {
			LogMessageSupport.printMessage(e, "@@ Exception. message = {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
		}
    }

	/**
	 * 비활성화 상태의 layer를 활성화
	 * @param request
	 * @param designLayerId
	 * @param designLayerFileInfoId
	 * @param designLayerFileInfoTeamId
	 * @return
	 */
    @PostMapping(value = "/{designLayerId:[0-9]+}/layer-file-infos/{designLayerFileInfoId:[0-9]+}")
    public Map<String, Object> updateByDesignLayerFileInfoId(HttpServletRequest request,
                                                            @PathVariable Long designLayerId,
                                                            @PathVariable Long designLayerFileInfoId,
                                                            Long designLayerFileInfoTeamId) {
        log.info("@@@@@@@@@@@@ designLayerId = {}, designLayerFileInfoId = {}, designLayerFileInfoTeamId = {} ", designLayerId, designLayerFileInfoId, designLayerFileInfoTeamId);

        Map<String, Object> result = new HashMap<>();
		String errorCode = null;
		String message = null;
		designLayerService.updateDesignLayerByDesignLayerFileInfoId(designLayerId, designLayerFileInfoTeamId, designLayerFileInfoId);

        int statusCode = HttpStatus.OK.value();

        result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		return result;
    }

	/**
	 * 파일 상세보기
	 * @param designLayerFileInfoId
	 * @return
	 */
    @GetMapping(value = "/file-info/{designLayerFileInfoId:[0-9]+}")
    public Map<String, Object> viewFileDetail(@PathVariable Long designLayerFileInfoId) {
    	Map<String, Object> result = new HashMap<>();
		String errorCode = null;
		String message = null;

		DesignLayerFileInfo designLayerFileInfo = designLayerFileInfoService.getDesignLayerFileInfo(designLayerFileInfoId);
        log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@ designLayerFileInfo = {}", designLayerFileInfo);
        int statusCode = HttpStatus.OK.value();

    	result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		result.put("designLayerFileInfo", designLayerFileInfo);
		return result;
    }

   private String replaceInvalidValue(String value) {
        if("null".equals(value)) value = null;
        return value;
    }

	/**
	 * 업로딩 파일을 압축 해제
	 * 	 * TODO 여기 Exception을 던지면 안될거 같음. 수정 필요
	 * @param policy
	 * @param groupFileName groupFileName 디자인 레이어의 경우 한 세트가 같은 이름에 확장자만 달라야 함
	 * @param multipartFile
	 * @param shapeEncoding
	 * @param targetDirectory
	 * @return
	 * @throws Exception
	 */
    private Map<String, Object> unzip(Policy policy, String groupFileName, MultipartFile multipartFile, String shapeEncoding, String targetDirectory) throws Exception {
        Map<String, Object> result = new HashMap<>();
        String errorCode = fileValidate(policy, multipartFile);
        if(!StringUtils.isEmpty(errorCode)) {
            result.put("errorCode", errorCode);
            return result;
        }

        // temp 디렉토리에 압축을 해제 함
        String tempDirectory = targetDirectory + "temp" + File.separator;
		FileUtils.makeDirectory(tempDirectory);

        List<DesignLayerFileInfo> designLayerFileInfoList = new ArrayList<>();

        File uploadedFile = new File(tempDirectory + multipartFile.getOriginalFilename());
        multipartFile.transferTo(uploadedFile);

        try ( ZipFile zipFile = new ZipFile(uploadedFile)) {
            String directoryPath = targetDirectory;
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while( entries.hasMoreElements() ) {
                DesignLayerFileInfo designLayerFileInfo = new DesignLayerFileInfo();
                ZipEntry entry = entries.nextElement();
                if( entry.isDirectory() ) {
                    String unzipfileName = directoryPath + entry.getName();
                    log.info("--------- unzip directory = {}", unzipfileName);
                    File file = new File(unzipfileName);
                    file.mkdirs();
                } else {
                    String extension = null;
                    String saveFileName = null;

					String fileName = entry.getName();
					String[] divideFileName = fileName.split("\\.");
                    if(divideFileName.length != 0) {
                        extension = divideFileName[divideFileName.length - 1];
                        saveFileName = groupFileName + "." + extension;
                        log.info("--------- unzip saveFileName = {}", saveFileName);
                    }

                    long size = 0L;
                    try ( 	InputStream inputStream = zipFile.getInputStream(entry);
                            FileOutputStream outputStream = new FileOutputStream(targetDirectory + saveFileName) ) {
                    	int bytesRead;
                        byte[] buffer = new byte[BUFFER_SIZE];
                        while ((bytesRead = inputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
                            size += bytesRead;
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        designLayerFileInfo.setFileExt(extension);
                        designLayerFileInfo.setFileName(fileName);
                        designLayerFileInfo.setFileRealName(saveFileName);
                        designLayerFileInfo.setFilePath(directoryPath);
                        designLayerFileInfo.setFileSize(String.valueOf(size));
                        designLayerFileInfo.setShapeEncoding(shapeEncoding);

                    } catch(IOException e) {
                    	LogMessageSupport.printMessage(e, "@@ IOException. message = {}", e.getMessage());
                    	designLayerFileInfo.setErrorMessage(e.getMessage());
                    	throw new RuntimeException(e.getMessage());
                    }
                }
                designLayerFileInfo.setFileSize(String.valueOf(entry.getSize()));
                if( !entry.isDirectory() ) {
                    designLayerFileInfoList.add(designLayerFileInfo);
                }
            }
        } catch(RuntimeException ex) {
        	LogMessageSupport.printMessage(ex, "@@ RuntimeException. message = {}", ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
        } catch(Exception ex) {
        	LogMessageSupport.printMessage(ex, "@@ RuntimeException. message = {}", ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
        }

        log.info("##################### unzip designLayerFileInfoList = {}", designLayerFileInfoList.size());
        result.put("designLayerFileInfoList", designLayerFileInfoList);
        return result;
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
        long fileSize = multipartFile.getSize();
        log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@ upload file size = {} KB", (fileSize / 1000));
        if( fileSize > (policy.getUserUploadMaxFilesize() * 1000000L)) {
            log.info("@@ fileSize = {}, user upload max filesize = {} M", (fileSize / 1000), policy.getUserUploadMaxFilesize());
            return "fileinfo.size.invalid";
        }

        return null;
    }

    // shape 필수 파일 있는지 확인
    private String shapeFileValidate(List<DesignLayerFileInfo> designLayerFileInfoList) {
    	if(!designLayerFileInfoList.isEmpty()) {
    		long validCount = designLayerFileInfoList.stream()
    				.filter(designLayerFileInfo -> {
    					log.info("==== designLayerFileInfo = {}", designLayerFileInfo);
    					String fileExt = designLayerFileInfo.getFileExt().toLowerCase().trim();
    					return fileExt.equals(ShapeFileExt.SHP.getValue()) || fileExt.equals(ShapeFileExt.DBF.getValue()) || fileExt.equals(ShapeFileExt.SHX.getValue());
    				})
    				.count();
    		if(ShapeFileExt.values().length-1 != (int)validCount) {
    			return "upload.shpfile.invalid";
    		}
    	}
    	
    	return null;
    }

    /**
    * validation 체크
    * @param request
    * @return
    */
    private String validate(MultipartHttpServletRequest request) {
        if(StringUtils.isEmpty(request.getParameter("designLayerName"))) {
            return "design.layer.name.empty";
        }
        
        return null;
    }

    private String getAttributeFile(List<DesignLayerFileInfo> designLayerFileInfoList) {
		return designLayerFileInfoList.stream()
				.map(f-> {
					if(DesignLayer.AttributeType.findBy(f.getFileExt()) != null) {
						return f.getFilePath() + f.getFileRealName();
					} else {
						return "";
					}
				})
				.collect(Collectors.joining());
	}

    /**
    * 다운로드시 한글 깨짐 방지 처리
    */
    private void setDisposition(String filename, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String browser = WebUtils.getBrowser(request);
        String dispositionPrefix = "attachment; filename=";
        String encodedFilename = null;

        log.info("================================= browser = {}", browser);
        if (browser.equals("MSIE")) {
            encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        } else if (browser.equals("Trident")) {       // IE11 문자열 깨짐 방지
            encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        } else if (browser.equals("Firefox")) {
            encodedFilename = "\"" + new String(filename.getBytes(StandardCharsets.UTF_8), "8859_1") + "\"";
            encodedFilename = URLDecoder.decode(encodedFilename, StandardCharsets.UTF_8);
        } else if (browser.equals("Opera")) {
            encodedFilename = "\"" + new String(filename.getBytes(StandardCharsets.UTF_8), "8859_1") + "\"";
        } else if (browser.equals("Chrome")) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < filename.length(); i++) {
                char c = filename.charAt(i);
                if (c > '~') {
                    sb.append(URLEncoder.encode("" + c, StandardCharsets.UTF_8));
                } else {
                    sb.append(c);
                }
            }
            encodedFilename = sb.toString();
        } else if (browser.equals("Safari")){
            encodedFilename = "\"" + new String(filename.getBytes(StandardCharsets.UTF_8), "8859_1")+ "\"";
            encodedFilename = URLDecoder.decode(encodedFilename, StandardCharsets.UTF_8);
        }
        else {
            encodedFilename = "\"" + new String(filename.getBytes(StandardCharsets.UTF_8), "8859_1")+ "\"";
        }

        response.setHeader("Content-Disposition", dispositionPrefix + encodedFilename);
        if ("Opera".equals(browser)){
            response.setContentType("application/octet-stream;charset=UTF-8");
        }
    }
}
