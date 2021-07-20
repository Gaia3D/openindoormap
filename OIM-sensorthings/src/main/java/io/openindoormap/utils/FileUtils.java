package io.openindoormap.utils;

import io.openindoormap.domain.UploadDirectoryType;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * TODO N중화 처리를 위해 FTP 로 다른 PM 으로 전송해 줘야 하는데....
 * 
 * 파일 처리 관련 Util
 * @author jeongdae
 *
 */
@Slf4j
public class FileUtils {

	public static boolean makeDirectory(String targetDirectory) {
		File directory = new File(targetDirectory);
		if(directory.exists()) {
			return true;
		} else {
			return directory.mkdir();
		}
	}
	
	/**
	 * 경로를 기준으로 디렉토리를 생성. window, linux 에서 File.separator 가 문제를 일으킴
	 * @param servicePath
	 * @param dataGroupPath
	 * @return
	 */
	public static boolean makeDirectoryByPath(String servicePath, String dataGroupPath) {
		String[] directors = dataGroupPath.split("/");
		String fullName = servicePath;
		
		boolean result = true;
		for(String directoryName : directors) {
			fullName = fullName + directoryName + File.separator;
			File directory = new File(fullName);
			if(directory.exists()) {
				result = true;
			} else {
				result = directory.mkdir();
				if(!result) return result;
			}
		}
		return result;
	}
	
	public static String makeDirectory(String userId, UploadDirectoryType uploadDirectoryType, String targetDirectory) {
		String today = DateUtils.getToday(FormatUtils.YEAR_MONTH_DAY_TIME14);
		String year = today.substring(0,4);
		String month = today.substring(4,6);
		String day = today.substring(6,8);
		String sourceDirectory = targetDirectory;
		
		File rootDirectory = new File(sourceDirectory);
		if(!rootDirectory.exists()) {
			rootDirectory.mkdir();
		}
		
		// 사용자 디렉토리
		if(UploadDirectoryType.USERID_YEAR == uploadDirectoryType 
				|| UploadDirectoryType.USERID_YEAR_MONTH == uploadDirectoryType
				|| UploadDirectoryType.USERID_YEAR_MONTH_DAY == uploadDirectoryType) {
			sourceDirectory = sourceDirectory + userId + File.separator;
			File userDirectory = new File(sourceDirectory);
			if(!userDirectory.exists()) {
				userDirectory.mkdir();
			}
		}
		
		// 년
		if(UploadDirectoryType.USERID_YEAR == uploadDirectoryType 
				|| UploadDirectoryType.USERID_YEAR_MONTH == uploadDirectoryType
				|| UploadDirectoryType.USERID_YEAR_MONTH_DAY == uploadDirectoryType
				|| UploadDirectoryType.YEAR  == uploadDirectoryType
				|| UploadDirectoryType.YEAR_MONTH == uploadDirectoryType
				|| UploadDirectoryType.YEAR_MONTH_DAY == uploadDirectoryType
				|| UploadDirectoryType.YEAR_USERID == uploadDirectoryType
				|| UploadDirectoryType.YEAR_MONTH_USERID == uploadDirectoryType
				|| UploadDirectoryType.YEAR_MONTH_DAY_USERID == uploadDirectoryType) {
			sourceDirectory = sourceDirectory + year + File.separator;
			File yearDirectory = new File(sourceDirectory);
			if(!yearDirectory.exists()) {
				yearDirectory.mkdir();
			}
		}
		
		// 월
		if(UploadDirectoryType.USERID_YEAR_MONTH == uploadDirectoryType
				|| UploadDirectoryType.USERID_YEAR_MONTH_DAY == uploadDirectoryType
				|| UploadDirectoryType.YEAR_MONTH == uploadDirectoryType
				|| UploadDirectoryType.YEAR_MONTH_DAY == uploadDirectoryType
				|| UploadDirectoryType.YEAR_MONTH_USERID == uploadDirectoryType
				|| UploadDirectoryType.YEAR_MONTH_DAY_USERID == uploadDirectoryType) {
			sourceDirectory = sourceDirectory + month + File.separator;
			File monthDirectory = new File(sourceDirectory);
			if(!monthDirectory.exists()) {
				monthDirectory.mkdir();
			}
		}
		
		// 일
		if(UploadDirectoryType.USERID_YEAR_MONTH_DAY == uploadDirectoryType
				|| UploadDirectoryType.YEAR_MONTH_DAY == uploadDirectoryType
				|| UploadDirectoryType.YEAR_MONTH_DAY_USERID == uploadDirectoryType) {
			sourceDirectory = sourceDirectory + day + File.separator;
			File dayDirectory = new File(sourceDirectory);
			if(!dayDirectory.exists()) {
				dayDirectory.mkdir();
			}
		}
		
		// 사용자 디렉토리
		if(UploadDirectoryType.YEAR_USERID == uploadDirectoryType
				|| UploadDirectoryType.YEAR_MONTH_USERID == uploadDirectoryType 
				|| UploadDirectoryType.YEAR_MONTH_DAY_USERID == uploadDirectoryType) {
			sourceDirectory = sourceDirectory + userId + File.separator;
			File userDirectory = new File(sourceDirectory);
			if(!userDirectory.exists()) {
				userDirectory.mkdir();
			}
		}
		
		return sourceDirectory;
	}
	
	public static String getFilePath(String dataGroupPath) {
		String[] names = dataGroupPath.split("/");

		// TODO SpringBuilder
		String filePath = "";
		for(String name : names) {
			filePath = filePath + name + File.separator;
		}
		return filePath;
	}
	
	public static void deleteFileReculsive(String path) {
		File folder = new File(path);
		try {
		    while(folder.exists()) {
		    if(!folder.isDirectory()) {
		    	folder.delete();
		    	break;
		    }
			File[] folder_list = folder.listFiles(); //파일리스트 얻어오기
			for (int j = 0; j < folder_list.length; j++) {
				folder_list[j].delete(); //파일 삭제 
			}
					
			if(folder_list.length == 0 && folder.isDirectory()){ 
				folder.delete(); //대상폴더 삭제
			}
	            }
		 } catch (Exception e) {
			e.getStackTrace();
		}
	}
}