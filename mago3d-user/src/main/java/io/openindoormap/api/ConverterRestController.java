package io.openindoormap.api;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.openindoormap.domain.ConverterJob;
import io.openindoormap.service.ConverterService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
public class ConverterRestController {

    @Autowired
    ConverterService converterService;

	/**
	 * 데이터변환 작업의 상태를 업데이트 한다.
	 *
	 * @param userId
	 * @param productIdList
	 */
	@PostMapping(value="/converter/status", produces = "application/json; charset=UTF-8")
	public void updateDownloadStatus(HttpServletRequest request, @RequestBody ConverterJob converterJob) {
        converterService.updateConverterJob(converterJob);
	}
}