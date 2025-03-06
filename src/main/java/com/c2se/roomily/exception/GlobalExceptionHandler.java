package com.c2se.roomily.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

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

//    @ExceptionHandler(BusinessException.class)
//    public ResponseEntity<ErrorDetails> handleBusinessException(BusinessException exception,
//                                                                WebRequest webRequest) {
//        return createErrorResponse(exception, webRequest, exception.getHttpStatus());
//    }

//    @ExceptionHandler(APIException.class)
//    public ResponseEntity<ErrorDetails> handleAPIException(APIException exception,
//                                                           WebRequest webRequest) {
//        return createErrorResponse(exception, webRequest, HttpStatus.BAD_REQUEST);
//    }

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
    //    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorDetails> handleGlobalException(Exception exception,
//                                                              WebRequest webRequest){
//        ErrorDetails errorDetails = new ErrorDetails(new Date(), exception.getMessage(), webRequest.getDescription(false));
//        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
}
