package io.openindoormap.controller.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.domain.FileType;
import io.openindoormap.domain.Key;
import io.openindoormap.domain.UploadDataType;
import io.openindoormap.domain.UploadDirectoryType;
import io.openindoormap.domain.extrusionmodel.DataLibraryUpload;
import io.openindoormap.domain.extrusionmodel.DataLibraryUploadFile;
import io.openindoormap.domain.policy.Policy;
import io.openindoormap.domain.user.UserSession;
import io.openindoormap.service.DataLibraryService;
import io.openindoormap.service.PolicyService;
import io.openindoormap.support.LogMessageSupport;
import io.openindoormap.utils.DateUtils;
import io.openindoormap.utils.FileUtils;
import io.openindoormap.utils.FormatUtils;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 데이터 라이브러리 파일 업로더
 * TODO 설계 파일 안의 texture 의 경우 설계 파일에서 참조하는 경우가 있으므로 이름 변경 불가.
 * @author jeongdae
 *
 */
@Slf4j
@RestController
@RequestMapping("/data-librarys")
public class DataLibraryRestController {
	
	// 파일 copy 시 버퍼 사이즈
	public static final int BUFFER_SIZE = 8192;
	
	@Autowired
	private PolicyService policyService;
	
	@Autowired
	private PropertiesConfig propertiesConfig;
	
	@Autowired
	private DataLibraryService dataLibraryService;


}
