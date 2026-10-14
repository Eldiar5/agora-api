package agora.exceptions.handler;

import agora.exceptions.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import agora.dto.response.ErrorResponseDto;

import java.util.Arrays;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({UserNotFoundException.class, PostNotFoundException.class})
    public ResponseEntity<ErrorResponseDto> handleNotFoundException(RuntimeException ex) {
        // 2. Создай DTO и верни его
        ErrorResponseDto errorDto = new ErrorResponseDto(ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({AgoraIllegalArgumentException.class, UnknownUserTypeException.class})
    public ResponseEntity<ErrorResponseDto> handleBadRequestException(RuntimeException ex) {
        ErrorResponseDto errorDto = new ErrorResponseDto(ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponseDto> handleForbiddenException(SecurityException ex) {
        ErrorResponseDto errorDto = new ErrorResponseDto(ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName();
        String requiredType = "";

        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            requiredType = String.format(
                    " (допустимые значения: %s)",
                    Arrays.toString(ex.getRequiredType().getEnumConstants())
            );
        }

        String message = String.format("Некорретное значение для параметра '%s'%s.", paramName, requiredType);
        ErrorResponseDto errorDto = new ErrorResponseDto(message); // 3. Оберни свое кастомное сообщение
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleBadJson(HttpMessageNotReadableException ex) {
        String message = "Ошибка в формате JSON-запроса. Проверьте типы данных (например, 'userType' должен быть числом 0 или 1).";
        ErrorResponseDto errorDto = new ErrorResponseDto(message);
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleInternalServerError(Exception ex) {
        ex.printStackTrace(); // Не теряй стектрейс в логах!
        String message = "Внутренняя ошибка сервера: " + ex.getMessage();
        ErrorResponseDto errorDto = new ErrorResponseDto(message);
        return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(cv -> {
                    String path = cv.getPropertyPath().toString();
                    String paramName = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                    return paramName + ": " + cv.getMessage();
                })
                .collect(Collectors.joining(", "));

        ErrorResponseDto errorDto = new ErrorResponseDto(message);
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponseDto errorDto = new ErrorResponseDto(message);
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AgoraAuthException.class)
    public ResponseEntity<ErrorResponseDto> handleAgoraAuthException(AgoraAuthException ex) {
        ErrorResponseDto errorDto = new ErrorResponseDto(ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.UNAUTHORIZED);
    }


}
