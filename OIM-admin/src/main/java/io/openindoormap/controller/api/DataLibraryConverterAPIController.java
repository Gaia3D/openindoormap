package io.openindoormap.controller.api;

import io.openindoormap.domain.agent.DataLibraryConverterResultLog;
import io.openindoormap.domain.extrusionmodel.DataLibraryConverterJob;
import io.openindoormap.service.DataLibraryConverterService;
import io.openindoormap.support.LogMessageSupport;
import io.openindoormap.utils.LocaleUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * 데이터 라이브러리 변환 후처리
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/data-library-converters")
public class DataLibraryConverterAPIController {

    @Autowired
    private DataLibraryConverterService dataLibraryConverterService;

    @Autowired
    private MessageSource messageSource;


    @PostMapping(value = "{dataLibraryConverterJobId}/status", consumes = "application/json", produces = "application/json")
    public ResponseEntity<DataLibraryConverterJob> status(@RequestBody DataLibraryConverterJob dataLibraryConverterJob,
                                                          @PathVariable("dataLibraryConverterJobId") Long dataLibraryConverterJobId,
                                                          HttpServletRequest request, HttpServletResponse response) {

        log.info(" >>>>>> status. dataLibraryConverterJob = {}", dataLibraryConverterJob);

        HttpStatus statusCode = HttpStatus.OK;
        String errorCode = null;
        String message = null;
        Locale locale = LocaleUtils.getUserLocale(request);

        try {
            dataLibraryConverterService.updateDataLibraryConverterJob(dataLibraryConverterJob);
        } catch (DataAccessException e) {
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
            errorCode = messageSource.getMessage("db.exception", null, locale);
            message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            LogMessageSupport.printMessage(e, "@@ db.exception. message = {}", message);
        } catch (RuntimeException e) {
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
            errorCode = messageSource.getMessage("runtime.exception", null, locale);
            message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            LogMessageSupport.printMessage(e, "@@ runtime.exception. message = {}", message);
        } catch (Exception e) {
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
            errorCode = messageSource.getMessage("unknown.exception", null, locale);
            message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            LogMessageSupport.printMessage(e, "@@ exception. message = {}", message);
        }
        dataLibraryConverterJob.setErrorCode(errorCode);

        return new ResponseEntity<>(dataLibraryConverterJob, statusCode);
    }

    @PostMapping(value = "{dataLibraryConverterJobId}/logs", consumes = "application/json", produces = "application/json")
    public ResponseEntity<DataLibraryConverterResultLog> logs(@RequestBody DataLibraryConverterResultLog dataLibraryConverterResultLog,
                                                               @PathVariable("dataLibraryConverterJobId") Long dataLibraryConverterJobId,
                                                               HttpServletRequest request, HttpServletResponse response) {

        log.info(" >>>>>> logs. dataLibraryConverterResultLog = {}", dataLibraryConverterResultLog);

        HttpStatus statusCode = HttpStatus.OK;
        String errorCode = null;
        String message = null;
        Locale locale = LocaleUtils.getUserLocale(request);

        try {
            dataLibraryConverterService.updateDataLibraryConverterJobStatus(dataLibraryConverterResultLog);
        } catch (DataAccessException e) {
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
            errorCode = messageSource.getMessage("db.exception", null, locale);
            message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            LogMessageSupport.printMessage(e, "@@ db.exception. message = {}", message);
        } catch (RuntimeException e) {
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
            errorCode = messageSource.getMessage("runtime.exception", null, locale);
            message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            LogMessageSupport.printMessage(e, "@@ runtime.exception. message = {}", message);
        } catch (Exception e) {
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
            errorCode = messageSource.getMessage("unknown.exception", null, locale);
            message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            LogMessageSupport.printMessage(e, "@@ exception. message = {}", message);
        }
        dataLibraryConverterResultLog.setFailureLog(errorCode);

        return new ResponseEntity<>(dataLibraryConverterResultLog, statusCode);
    }
}