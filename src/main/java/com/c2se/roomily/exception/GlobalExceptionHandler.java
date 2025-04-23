package com.c2se.roomily.exception;

import com.c2se.roomily.payload.response.ErrorDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
//    private ResponseEntity<ErrorDetails> createErrorResponse(Exception exception, WebRequest webRequest, HttpStatus status) {
//        ErrorDetails errorDetails = new ErrorDetails(
//                new Date(),
//                exception.getMessage(),
//                webRequest.getDescription(false)
//        );
//        return new ResponseEntity<>(errorDetails, status);
//    }
//
//    @ExceptionHandler(BusinessException.class)
//    public ResponseEntity<ErrorDetails> handleBusinessException(BusinessException exception,
//                                                                WebRequest webRequest) {
//        return createErrorResponse(exception, webRequest, exception.getHttpStatus());
//    }
//
//    @ExceptionHandler(APIException.class)
//    public ResponseEntity<ErrorDetails> handleAPIException(APIException exception,
//                                                           WebRequest webRequest) {
//        return createErrorResponse(exception, webRequest, HttpStatus.BAD_REQUEST);
//    }
//
//    @Override
//    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
//                                                                  HttpHeaders headers,
//                                                                  HttpStatusCode status,
//                                                                  WebRequest request) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach(error -> {
//            String fieldName = ((FieldError) error).getField();
//            String message = error.getDefaultMessage();
//            errors.put(fieldName, message);
//        });
//        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(HttpServerErrorException.InternalServerError.class)
//    public ResponseEntity<ObjectNode> handleInternalServerError(HttpServerErrorException.InternalServerError exception,
//                                                                WebRequest webRequest) {
//        // Log the error details
//        System.err.println("Internal Server Error: " + exception.getMessage());
//        ObjectMapper objectMapper = new ObjectMapper();
//        ObjectNode response = objectMapper.createObjectNode();
//        response.put("error", 1);
//        response.put("message", "Internal Server Error");
//        response.put("details", exception.getMessage());
//        response.put("timestamp", new Date().toString());
//        response.put("path", webRequest.getDescription(false));
//        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorDetails> handleGlobalException(Exception exception,
//                                                              WebRequest webRequest){
//        ErrorDetails errorDetails = new ErrorDetails(new Date(), exception.getMessage(), webRequest.getDescription(false));
//        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
}
