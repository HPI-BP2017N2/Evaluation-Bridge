package de.hpi.evaluationbridge.api;

import de.hpi.evaluationbridge.dto.ErrorResponse;
import de.hpi.evaluationbridge.exception.FetchProcessAlreadyRunningException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Component
@Slf4j
public class ExceptionHandlerController extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {FetchProcessAlreadyRunningException.class})
    protected ResponseEntity<Object> handleFetchProcessAlreadyRunningException(Exception e, WebRequest request) {
        log.info(e.getMessage());
        return new ErrorResponse().withError(e).send(HttpStatus.BAD_REQUEST);
    }
}
