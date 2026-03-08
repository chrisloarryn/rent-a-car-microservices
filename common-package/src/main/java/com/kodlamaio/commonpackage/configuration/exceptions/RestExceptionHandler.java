package com.kodlamaio.commonpackage.configuration.exceptions;

import com.kodlamaio.commonpackage.utils.constants.ExceptionTypes;
import com.kodlamaio.commonpackage.utils.exceptions.BusinessException;
import com.kodlamaio.commonpackage.utils.results.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler
{
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request)
    {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        for(FieldError fieldError : exception.getBindingResult().getFieldErrors())
        {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ExceptionTypes.Exception.Validation,
                "Request validation failed.",
                validationErrors,
                request.getRequestURI());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(
            ConstraintViolationException exception,
            HttpServletRequest request)
    {
        List<String> validationErrors = exception.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ExceptionTypes.Exception.ConstraintViolation,
                "Request validation failed.",
                validationErrors,
                request.getRequestURI());
    }

    @ExceptionHandler({ValidationException.class, BindException.class})
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            Exception exception,
            HttpServletRequest request)
    {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ExceptionTypes.Exception.Validation,
                "Request validation failed.",
                List.of(exception.getMessage()),
                request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception,
            HttpServletRequest request)
    {
        String message = exception.getMostSpecificCause() != null
                ? exception.getMostSpecificCause().getMessage()
                : exception.getMessage();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ExceptionTypes.Exception.RequestFormat,
                "Request body contains invalid or unsupported values.",
                List.of(message),
                request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request)
    {
        String expectedType = exception.getRequiredType() == null ? "unknown" : exception.getRequiredType().getSimpleName();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ExceptionTypes.Exception.RequestFormat,
                "Request parameter has an invalid format.",
                List.of(exception.getName() + " must be a valid " + expectedType + "."),
                request.getRequestURI());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request)
    {
        return buildResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ExceptionTypes.Exception.Business,
                exception.getMessage(),
                List.of(exception.getMessage()),
                request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException exception,
            HttpServletRequest request)
    {
        String message = exception.getMostSpecificCause() == null
                ? exception.getMessage()
                : exception.getMostSpecificCause().getMessage();

        return buildResponse(
                HttpStatus.CONFLICT,
                ExceptionTypes.Exception.DataIntegrityViolation,
                "The requested operation violates data integrity constraints.",
                List.of(message),
                request.getRequestURI());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(
            RuntimeException exception,
            HttpServletRequest request)
    {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ExceptionTypes.Exception.Runtime,
                "An unexpected error occurred.",
                List.of(exception.getMessage()),
                request.getRequestURI());
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String type,
            String message,
            Object details,
            String path)
    {
        return ResponseEntity.status(status).body(
                new ApiErrorResponse(
                        status.value(),
                        status.getReasonPhrase(),
                        type,
                        message,
                        details,
                        path));
    }
}
