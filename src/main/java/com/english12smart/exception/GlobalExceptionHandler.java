package com.english12smart.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import lombok.extern.slf4j.Slf4j;
import com.english12smart.dto.ApiResponseDTO;

/**
 * ========== GLOBAL EXCEPTION HANDLER ==========
 * Xử lý các exceptions toàn ứng dụng
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        /**
         * Handle ApplicationException
         */
        @ExceptionHandler(ApplicationException.class)
        public ResponseEntity<ApiResponseDTO<String>> handleApplicationException(
                        ApplicationException ex,
                        WebRequest request) {
                log.error("Application exception: {} - {}", ex.getErrorCode(), ex.getMessage());

                ApiResponseDTO<String> response = new ApiResponseDTO<>(
                                ex.getStatusCode(),
                                ex.getMessage(),
                                null);
                response.setError(ex.getErrorCode());
                response.setPath(request.getDescription(false).replace("uri=", ""));
                response.setTimestamp(System.currentTimeMillis());

                return new ResponseEntity<>(response, HttpStatus.valueOf(ex.getStatusCode()));
        }

        /**
         * Handle ResourceNotFoundException
         */
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiResponseDTO<String>> handleResourceNotFoundException(
                        ResourceNotFoundException ex,
                        WebRequest request) {
                log.warn("Resource not found: {}", ex.getMessage());

                ApiResponseDTO<String> response = new ApiResponseDTO<>(
                                404,
                                ex.getMessage(),
                                null);
                response.setError("NOT_FOUND");
                response.setPath(request.getDescription(false).replace("uri=", ""));
                response.setTimestamp(System.currentTimeMillis());

                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        /**
         * Handle ValidationException
         */
        @ExceptionHandler(ValidationException.class)
        public ResponseEntity<ApiResponseDTO<String>> handleValidationException(
                        ValidationException ex,
                        WebRequest request) {
                log.warn("Validation error: {}", ex.getMessage());

                ApiResponseDTO<String> response = new ApiResponseDTO<>(
                                400,
                                ex.getMessage(),
                                null);
                response.setError("VALIDATION_ERROR");
                response.setPath(request.getDescription(false).replace("uri=", ""));
                response.setTimestamp(System.currentTimeMillis());

                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        /**
         * Handle AuthenticationException
         */
        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiResponseDTO<String>> handleAuthenticationException(
                        AuthenticationException ex,
                        WebRequest request) {
                log.warn("Authentication failed: {}", ex.getMessage());

                ApiResponseDTO<String> response = new ApiResponseDTO<>(
                                401,
                                ex.getMessage(),
                                null);
                response.setError("AUTHENTICATION_FAILED");
                response.setPath(request.getDescription(false).replace("uri=", ""));
                response.setTimestamp(System.currentTimeMillis());

                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        /**
         * Handle AuthorizationException
         */
        @ExceptionHandler(AuthorizationException.class)
        public ResponseEntity<ApiResponseDTO<String>> handleAuthorizationException(
                        AuthorizationException ex,
                        WebRequest request) {
                log.warn("Authorization failed: {}", ex.getMessage());

                ApiResponseDTO<String> response = new ApiResponseDTO<>(
                                403,
                                ex.getMessage(),
                                null);
                response.setError("AUTHORIZATION_FAILED");
                response.setPath(request.getDescription(false).replace("uri=", ""));
                response.setTimestamp(System.currentTimeMillis());

                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        /**
         * Handle BadRequestException
         */
        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ApiResponseDTO<String>> handleBadRequestException(
                        BadRequestException ex,
                        WebRequest request) {
                log.warn("Bad request: {}", ex.getMessage());

                ApiResponseDTO<String> response = new ApiResponseDTO<>(
                                400,
                                ex.getMessage(),
                                null);
                response.setError("BAD_REQUEST");
                response.setPath(request.getDescription(false).replace("uri=", ""));
                response.setTimestamp(System.currentTimeMillis());

                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        /**
         * Handle DuplicateResourceException
         */
        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ApiResponseDTO<String>> handleDuplicateResourceException(
                        DuplicateResourceException ex,
                        WebRequest request) {
                log.warn("Duplicate resource: {}", ex.getMessage());

                ApiResponseDTO<String> response = new ApiResponseDTO<>(
                                409,
                                ex.getMessage(),
                                null);
                response.setError("DUPLICATE_RESOURCE");
                response.setPath(request.getDescription(false).replace("uri=", ""));
                response.setTimestamp(System.currentTimeMillis());

                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }

        /**
         * Handle TokenExpiredException
         */
        @ExceptionHandler(TokenExpiredException.class)
        public ResponseEntity<ApiResponseDTO<String>> handleTokenExpiredException(
                        TokenExpiredException ex,
                        WebRequest request) {
                log.warn("Token expired: {}", ex.getMessage());

                ApiResponseDTO<String> response = new ApiResponseDTO<>(
                                401,
                                ex.getMessage(),
                                null);
                response.setError("TOKEN_EXPIRED");
                response.setPath(request.getDescription(false).replace("uri=", ""));
                response.setTimestamp(System.currentTimeMillis());

                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        /**
         * Handle Spring Validation Errors (MethodArgumentNotValidException)
         * Triggered by @Valid annotation on request body
         */
        @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponseDTO<Object>> handleMethodArgumentNotValid(
                        org.springframework.web.bind.MethodArgumentNotValidException ex,
                        WebRequest request) {
                log.warn("Validation failed: {}", ex.getMessage());

                // Collect all validation errors
                java.util.Map<String, String> errors = new java.util.HashMap<>();
                ex.getBindingResult().getFieldErrors()
                                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

                ApiResponseDTO<Object> response = new ApiResponseDTO<>(
                                400,
                                "Dữ liệu không hợp lệ",
                                errors);
                response.setError("VALIDATION_ERROR");
                response.setPath(request.getDescription(false).replace("uri=", ""));
                response.setTimestamp(System.currentTimeMillis());

                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        /**
         * Handle all other exceptions
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponseDTO<String>> handleGlobalException(
                        Exception ex,
                        WebRequest request) {
                log.error("Unexpected exception: ", ex);

                ApiResponseDTO<String> response = new ApiResponseDTO<>(
                                500,
                                "Lỗi máy chủ nội bộ. Vui lòng thử lại sau.",
                                null);
                response.setError(ex.getClass().getSimpleName());
                response.setPath(request.getDescription(false).replace("uri=", ""));
                response.setTimestamp(System.currentTimeMillis());

                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}