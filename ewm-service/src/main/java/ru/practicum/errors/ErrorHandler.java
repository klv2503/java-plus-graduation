package ru.practicum.errors;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handlerConstraintViolationException(final ConstraintViolationException e) {
        return buildErrorResponse(e, HttpStatus.BAD_REQUEST, "Got incorrect pathVariable");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(final MethodArgumentNotValidException e) {
        return buildErrorResponse(e, HttpStatus.BAD_REQUEST, "Got incorrect pathVariable");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError>
    handleMissingServletRequestParameterException(final MissingServletRequestParameterException e) {
        return buildErrorResponse(e, HttpStatus.BAD_REQUEST, "Missing required parameter");
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ApiError> handleMissingPathVariableException(final MissingPathVariableException e) {
        String message;
        HttpStatus status;
        if (e.getVariableName().equals("userId")) {
            message = "Authorisation is required.";
            status = HttpStatus.UNAUTHORIZED;
        } else {
            message = e.getVariableName() + "was missed.";
            status = HttpStatus.BAD_REQUEST;
        }
        return buildErrorResponse(e, status, message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(final AccessDeniedException e) {
        return buildErrorResponse(e, HttpStatus.FORBIDDEN, "Access denied");
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handlerEntityNotFoundException(final EntityNotFoundException e) {
        return buildErrorResponse(e, HttpStatus.NOT_FOUND, "The required object was not found.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handlerDataIntegrityViolationException(final DataIntegrityViolationException e) {
        return buildErrorResponse(e, HttpStatus.CONFLICT, "Integrity constraint has been violated.");
    }

    @ExceptionHandler(ForbiddenActionException.class)
    public ResponseEntity<ApiError> handlerForbiddenActionException(final ForbiddenActionException e) {
        return buildErrorResponse(e, HttpStatus.CONFLICT, "Action not allowed.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException e) {
        return buildErrorResponse(e, HttpStatus.BAD_REQUEST, "Argument type mismatch");
    }

    @ExceptionHandler(EventNotPublishedException.class)
    public ResponseEntity<ApiError> handlerForbiddenActionException(final EventNotPublishedException e) {
        return buildErrorResponse(e, HttpStatus.NOT_FOUND, "Event is not available.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "Incorrect arguments.");
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handlerOtherException(final Exception e) {
        return buildErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR, "Got 500 status Internal server error");
    }

    private ResponseEntity<ApiError> buildErrorResponse(Exception e, HttpStatus status, String message) {
        StackTraceElement sElem = e.getStackTrace()[0];
        String className = sElem.getClassName();
        String str = className.contains(".") ? className.substring(className.lastIndexOf(".") + 1) : className;
        log.error("\n{} error - Class: {}; Method: {}; Line: {}; \nMessage: {}",
                status, str, sElem.getMethodName(), sElem.getLineNumber(), e.getMessage());

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String statusStr = status.value() + " " + status.getReasonPhrase().replace(" ", "_");

        return ResponseEntity.status(status)
                .body(new ApiError(statusStr, message, e));
    }
}
