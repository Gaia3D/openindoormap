package io.openindoormap.utils;

import java.io.File;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.openindoormap.OIMAdminApplication;
import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.domain.UploadDirectoryType;
import io.openindoormap.utils.DateUtils;
import io.openindoormap.utils.FileUtils;
import io.openindoormap.utils.FormatUtils;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = OIMAdminApplication.class)
@SpringBootTest
@Slf4j
class FilePathTest {

	@Autowired
	private PropertiesConfig propertiesConfig;

	@Test
	@Disabled
	void test() {
//		File file = Paths.get("/f4d/test", "aa").toFile();
//		System.out.println(file.getPath());
		
		String dataGroupPath = "basic/";
		
		String[] directors = dataGroupPath.split("/");
		String fullName = "C:\\data\\mago3d\\f4d\\";
		
		boolean result = true;
		for(String directoryName : directors) {
			fullName = fullName + directoryName + File.separator;
			File directory = new File(fullName);
			log.info("----------- fullName = {}", fullName);
		}
	}

	@Test
	void mkdirDatePatternTest() {
		String today = DateUtils.getToday(FormatUtils.YEAR_MONTH_DAY_TIME14);
		String makedDirectory = FileUtils.makeDirectory("admin", UploadDirectoryType.YEAR_MONTH,
				propertiesConfig.getDataConverterLogDir());
		log.info(">>>>>>>>>> today = {}", today);
		log.info(">>>>>>>>>> makedDirectory = {}", makedDirectory);
	}

}