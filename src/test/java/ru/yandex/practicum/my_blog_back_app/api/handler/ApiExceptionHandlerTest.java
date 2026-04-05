package ru.yandex.practicum.my_blog_back_app.api.handler;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.ErrorResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApiExceptionHandler Unit Tests")
class ApiExceptionHandlerTest {

    @InjectMocks
    private ApiExceptionHandler exceptionHandler;

    private static final String ERROR_MESSAGE = "Тест ошибки";

    @Nested
    @DisplayName("Тесты для IllegalArgumentException")
    class IllegalArgumentExceptionTests {

        @Test
        @DisplayName("Должен обработать IllegalArgumentException и вернуть BAD_REQUEST с сообщением")
        void shouldHandleIllegalArgumentExceptionAndReturnBadRequest() {
            IllegalArgumentException exception = new IllegalArgumentException(ERROR_MESSAGE);

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("Должен обработать IllegalArgumentException с пустым сообщением")
        void shouldHandleIllegalArgumentExceptionWithEmptyMessage() {
            IllegalArgumentException exception = new IllegalArgumentException();

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("Должен обработать IllegalArgumentException с null сообщением")
        void shouldHandleIllegalArgumentExceptionWithNullMessage() {
            IllegalArgumentException exception = new IllegalArgumentException((String) null);

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
        }

    }

    @Nested
    @DisplayName("Тесты для InvalidImageException")
    class InvalidImageExceptionTests {

        @Test
        @DisplayName("Должен обработать InvalidImageException и вернуть BAD_REQUEST с сообщением")
        void shouldHandleInvalidImageExceptionAndReturnBadRequest() {
            InvalidImageException exception = new InvalidImageException(ERROR_MESSAGE);

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidImage(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("Должен обработать InvalidImageException с сообщением о неверном формате")
        void shouldHandleInvalidImageExceptionWithInvalidFormatMessage() {
            String invalidFormatMessage = "Неподдерживаемый формат изображения. Используйте JPEG, PNG или GIF";
            InvalidImageException exception = new InvalidImageException(invalidFormatMessage);

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidImage(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("Должен обработать InvalidImageException с сообщением о превышении размера")
        void shouldHandleInvalidImageExceptionWithSizeExceededMessage() {
            String sizeMessage = "Размер изображения превышает допустимые 5MB";
            InvalidImageException exception = new InvalidImageException(sizeMessage);

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidImage(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Тесты для RuntimeException")
    class RuntimeExceptionTests {

        @Test
        @DisplayName("Должен обработать RuntimeException и вернуть INTERNAL_SERVER_ERROR с общим сообщением")
        void shouldHandleRuntimeExceptionAndReturnInternalServerError() {
            new RuntimeException("Some internal error");

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleInternalError();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("Должен скрыть детали внутренней ошибки от клиента")
        void shouldHideInternalErrorDetailsFromClient() {

            new RuntimeException("Database connection failed with sensitive details");

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleInternalError();

            Assertions.assertNotNull(response.getBody());
            assertThat(response.getBody().getMessage()).isEqualTo("Произошла непредвиденная ошибка");
            assertThat(response.getBody().getMessage()).doesNotContain("Database");
            assertThat(response.getBody().getMessage()).doesNotContain("sensitive");
        }

        @Test
        @DisplayName("Должен обработать NullPointerException как RuntimeException")
        void shouldHandleNullPointerExceptionAsRuntimeException() {

            new NullPointerException();

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleInternalError();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Nested
    @DisplayName("Тесты обработки MethodArgumentNotValidException")
    class MethodArgumentNotValidExceptionTests {

        @Mock
        private MethodArgumentNotValidException exception;

        @Mock
        private BindingResult bindingResult;

        @BeforeEach
        void setUp() {
            when(exception.getBindingResult()).thenReturn(bindingResult);
        }

        @Test
        @DisplayName("Должен обработать одну ошибку валидации")
        void shouldHandleSingleValidationError() {
            String errorMessage = "Название поста не может быть пустым";
            ObjectError error = new ObjectError("Test", errorMessage);
            when(bindingResult.getAllErrors()).thenReturn(List.of(error));

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("Должен обработать несколько ошибок валидации и объединить их через запятую")
        void shouldHandleMultipleValidationErrorsAndJoinWithComma() {
            String errorMessage1 = "Название поста не может быть пустым";
            String errorMessage2 = "Содержание поста не может быть пустым";

            ObjectError error1 = new ObjectError("Test", errorMessage1);
            ObjectError error2 = new ObjectError("Test_2", errorMessage2);

            when(bindingResult.getAllErrors()).thenReturn(List.of(error1, error2));

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo(
                    "Название поста не может быть пустым, Содержание поста не может быть пустым"
            );
        }

        @Test
        @DisplayName("Должен обработать FieldError корректно")
        void shouldHandleFieldErrorCorrectly() {
            FieldError fieldError = new FieldError("post", "title", "Title is required");
            when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("Title is required");
        }

        @Test
        @DisplayName("Должен обработать пустой список ошибок валидации")
        void shouldHandleEmptyValidationErrors() {
            when(bindingResult.getAllErrors()).thenReturn(List.of());

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEmpty();
        }

        @Test
        @DisplayName("Должен обработать ошибки валидации с null сообщениями")
        void shouldHandleValidationErrorsWithNullMessages() {
            ObjectError error1 = new ObjectError("Test", null);
            ObjectError error2 = new ObjectError("Test", "Valid message");
            ObjectError error3 = new ObjectError("Test", null);

            when(bindingResult.getAllErrors()).thenReturn(List.of(error1, error2, error3));

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("null, Valid message, null");
        }
    }

}