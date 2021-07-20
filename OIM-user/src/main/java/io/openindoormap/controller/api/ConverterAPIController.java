package io.openindoormap.controller.api;

import io.openindoormap.domain.agent.ConverterResultLog;
import io.openindoormap.domain.converter.ConverterJob;
import io.openindoormap.service.ConverterService;
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

@Slf4j
@RestController
@RequestMapping(value = "/api/converters")
// @RequestMapping(value = "/api/design-layers", produces = MediaTypes.HAL_JSON_VALUE)
// RestControllerExceptionHandler
public class ConverterAPIController {

    @Autowired
    private ConverterService converterService;

    @Autowired
    private MessageSource messageSource;

    @PostMapping(value = "{converterJobId}/status", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ConverterJob> status(@RequestBody ConverterJob converterJob,
                                               @PathVariable("converterJobId") Long converterJobId,
                                               HttpServletRequest request, HttpServletResponse response) {

        HttpStatus statusCode = HttpStatus.OK;
        String errorCode = null;
        String message = null;
        Locale locale = LocaleUtils.getUserLocale(request);

        try {
            log.info(" >>>>>> converterJobId = {}", converterJobId);
            converterService.updateConverterJob(converterJob);
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
        converterJob.setErrorCode(errorCode);

        return new ResponseEntity<>(converterJob, statusCode);
    }

    @PostMapping(value = "{converterJobId}/logs", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ConverterResultLog> logs(@RequestBody ConverterResultLog converterResultLog,
                                                   @PathVariable("converterJobId") Long converterJobId,
                                                   HttpServletRequest request, HttpServletResponse response) {

        HttpStatus statusCode = HttpStatus.OK;
        String errorCode = null;
        String message = null;
        Locale locale = LocaleUtils.getUserLocale(request);

        try {
            log.info(" >>>>>> converterJobId = {}", converterJobId);
            converterService.updateConverterJobStatus(converterResultLog);
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
        converterResultLog.setFailureLog(errorCode);

        return new ResponseEntity<>(converterResultLog, statusCode);
    }

}