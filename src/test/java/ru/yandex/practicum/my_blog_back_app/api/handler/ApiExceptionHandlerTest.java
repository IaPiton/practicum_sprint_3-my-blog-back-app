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
            assertThat(response.getBody().getError()).isEqualTo("BAD_REQUEST");
            assertThat(response.getBody().getMessage()).isEqualTo(ERROR_MESSAGE);
        }

        @Test
        @DisplayName("Должен обработать IllegalArgumentException с пустым сообщением")
        void shouldHandleIllegalArgumentExceptionWithEmptyMessage() {
            IllegalArgumentException exception = new IllegalArgumentException();

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("BAD_REQUEST");
            assertThat(response.getBody().getMessage()).isNull();
        }

        @Test
        @DisplayName("Должен обработать IllegalArgumentException с null сообщением")
        void shouldHandleIllegalArgumentExceptionWithNullMessage() {
            IllegalArgumentException exception = new IllegalArgumentException((String) null);

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("BAD_REQUEST");
            assertThat(response.getBody().getMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("Тесты для EntityNotFoundException")
    class EntityNotFoundExceptionTests {

        @Test
        @DisplayName("Должен обработать EntityNotFoundException и вернуть BAD_REQUEST с сообщением о ненайденной записи")
        void shouldHandleEntityNotFoundExceptionAndReturnBadRequest() {
            ResponseEntity<ErrorResponse> response = exceptionHandler.EntityNotFound();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("BAD_REQUEST");
            assertThat(response.getBody().getMessage()).isEqualTo("Запись не найднен");
        }

        @Test
        @DisplayName("Должен возвращать одинаковый ответ при любом EntityNotFoundException")
        void shouldReturnSameResponseForAnyEntityNotFoundException() {
            ResponseEntity<ErrorResponse> response1 = exceptionHandler.EntityNotFound();
            ResponseEntity<ErrorResponse> response2 = exceptionHandler.EntityNotFound();

            assertThat(response1.getBody().getError()).isEqualTo(response2.getBody().getError());
            assertThat(response1.getBody().getMessage()).isEqualTo(response2.getBody().getMessage());
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
            assertThat(response.getBody().getError()).isEqualTo("BAD_REQUEST");
            assertThat(response.getBody().getMessage()).isEqualTo(ERROR_MESSAGE);
        }

        @Test
        @DisplayName("Должен обработать InvalidImageException с сообщением о неверном формате")
        void shouldHandleInvalidImageExceptionWithInvalidFormatMessage() {
            String invalidFormatMessage = "Неподдерживаемый формат изображения. Используйте JPEG, PNG или GIF";
            InvalidImageException exception = new InvalidImageException(invalidFormatMessage);

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidImage(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("BAD_REQUEST");
            assertThat(response.getBody().getMessage()).isEqualTo(invalidFormatMessage);
        }

        @Test
        @DisplayName("Должен обработать InvalidImageException с сообщением о превышении размера")
        void shouldHandleInvalidImageExceptionWithSizeExceededMessage() {
            String sizeMessage = "Размер изображения превышает допустимые 5MB";
            InvalidImageException exception = new InvalidImageException(sizeMessage);

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidImage(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("BAD_REQUEST");
            assertThat(response.getBody().getMessage()).isEqualTo(sizeMessage);
        }

        @Test
        @DisplayName("Должен обработать InvalidImageException с пустым сообщением")
        void shouldHandleInvalidImageExceptionWithEmptyMessage() {
            InvalidImageException exception = new InvalidImageException("");

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidImage(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("BAD_REQUEST");
            assertThat(response.getBody().getMessage()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Тесты для RuntimeException")
    class RuntimeExceptionTests {

        @Test
        @DisplayName("Должен обработать RuntimeException и вернуть INTERNAL_SERVER_ERROR с общим сообщением")
        void shouldHandleRuntimeExceptionAndReturnInternalServerError() {
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleInternalError();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("INTERNAL_SERVER");
            assertThat(response.getBody().getMessage()).isEqualTo("Произошла непредвиденная ошибка");
        }

        @Test
        @DisplayName("Должен скрыть детали внутренней ошибки от клиента")
        void shouldHideInternalErrorDetailsFromClient() {
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleInternalError();

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("INTERNAL_SERVER");
            assertThat(response.getBody().getMessage()).isEqualTo("Произошла непредвиденная ошибка");
            assertThat(response.getBody().getMessage()).doesNotContain("Database");
            assertThat(response.getBody().getMessage()).doesNotContain("sensitive");
            assertThat(response.getBody().getMessage()).doesNotContain("connection");
        }

        @Test
        @DisplayName("Должен возвращать одинаковый ответ для любых RuntimeException")
        void shouldReturnSameResponseForAnyRuntimeException() {
            ResponseEntity<ErrorResponse> response1 = exceptionHandler.handleInternalError();
            ResponseEntity<ErrorResponse> response2 = exceptionHandler.handleInternalError();

            assertThat(response1.getBody().getError()).isEqualTo(response2.getBody().getError());
            assertThat(response1.getBody().getMessage()).isEqualTo(response2.getBody().getMessage());
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
            assertThat(response.getBody().getError()).isEqualTo("BAD_REQUEST");
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
            assertThat(response.getBody().getError()).isEqualTo("BAD_REQUEST");
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
            assertThat(response.getBody().getError()).isEqualTo("BAD_REQUEST");
            assertThat(response.getBody().getMessage()).isEqualTo("Title is required");
        }

        @Test
        @DisplayName("Должен обработать пустой список ошибок валидации")
        void shouldHandleEmptyValidationErrors() {
            when(bindingResult.getAllErrors()).thenReturn(List.of());

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("BAD_REQUEST");
            assertThat(response.getBody().getMessage()).isEmpty();
        }

        @Test
        @DisplayName("Должен обработать ошибки валидации с null сообщениями")
        void shouldHandleValidationErrorsWithNullMessages() {
            ObjectError error1 = new ObjectError("Test",  null);
            ObjectError error2 = new ObjectError("Test", "Valid message");
            ObjectError error3 = new ObjectError("Test",  null);

            when(bindingResult.getAllErrors()).thenReturn(List.of(error1, error2, error3));

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("BAD_REQUEST");
            assertThat(response.getBody().getMessage()).isEqualTo("null, Valid message, null");
        }

        @Test
        @DisplayName("Должен обработать большое количество ошибок валидации")
        void shouldHandleManyValidationErrors() {
            List<ObjectError> errors = List.of(
                    new ObjectError("Test", "Ошибка 1"),
                    new ObjectError("Test", "Ошибка 2"),
                    new ObjectError("Test", "Ошибка 3"),
                    new ObjectError("Test", "Ошибка 4"),
                    new ObjectError("Test", "Ошибка 5")
            );
            when(bindingResult.getAllErrors()).thenReturn(errors);

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("BAD_REQUEST");
            assertThat(response.getBody().getMessage()).contains("Ошибка 1");
            assertThat(response.getBody().getMessage()).contains("Ошибка 5");
        }
    }

    @Nested
    @DisplayName("Тесты обработки общих исключений")
    class GeneralExceptionTests {

        @Test
        @DisplayName("Должен обработать необработанное исключение как INTERNAL_SERVER_ERROR")
        void shouldHandleUnhandledExceptionAsInternalServerError() {
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleInternalError();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("INTERNAL_SERVER");
            assertThat(response.getBody().getMessage()).isEqualTo("Произошла непредвиденная ошибка");
        }

        @Test
        @DisplayName("Должен возвращать корректную структуру ErrorResponse для всех исключений")
        void shouldReturnCorrectErrorResponseStructureForAllExceptions() {
            IllegalArgumentException illegalArgEx = new IllegalArgumentException("Test");
            ResponseEntity<ErrorResponse> response1 = exceptionHandler.handleBadRequest(illegalArgEx);

            assertThat(response1.getBody()).isInstanceOf(ErrorResponse.class);
            assertThat(response1.getBody().getError()).isNotBlank();
            assertThat(response1.getBody().getMessage()).isEqualTo("Test");

            ResponseEntity<ErrorResponse> response2 = exceptionHandler.EntityNotFound();

            assertThat(response2.getBody()).isInstanceOf(ErrorResponse.class);
            assertThat(response2.getBody().getError()).isEqualTo("BAD_REQUEST");
            assertThat(response2.getBody().getMessage()).isEqualTo("Запись не найднен");

            ResponseEntity<ErrorResponse> response3 = exceptionHandler.handleInternalError();

            assertThat(response3.getBody()).isInstanceOf(ErrorResponse.class);
            assertThat(response3.getBody().getError()).isEqualTo("INTERNAL_SERVER");
            assertThat(response3.getBody().getMessage()).isEqualTo("Произошла непредвиденная ошибка");
        }

        @Test
        @DisplayName("Должен корректно обрабатывать все HTTP статусы")
        void shouldHandleAllHttpStatusesCorrectly() {
            ResponseEntity<ErrorResponse> response1 = exceptionHandler.handleBadRequest(new IllegalArgumentException());
            assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            ResponseEntity<ErrorResponse> response2 = exceptionHandler.EntityNotFound();
            assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            ResponseEntity<ErrorResponse> response3 = exceptionHandler.handleInvalidImage(new InvalidImageException(""));
            assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            MethodArgumentNotValidException mockException = mock(MethodArgumentNotValidException.class);
            when(mockException.getBindingResult()).thenReturn(mock(BindingResult.class));
            when(mockException.getBindingResult().getAllErrors()).thenReturn(List.of());
            ResponseEntity<ErrorResponse> response4 = exceptionHandler.handleBadRequest(mockException);
            assertThat(response4.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            ResponseEntity<ErrorResponse> response5 = exceptionHandler.handleInternalError();
            assertThat(response5.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("Должен правильно устанавливать поле error в ErrorResponse")
        void shouldCorrectlySetErrorFieldInErrorResponse() {
            ResponseEntity<ErrorResponse> badRequestResponse = exceptionHandler.handleBadRequest(new IllegalArgumentException("test"));
            assertThat(badRequestResponse.getBody().getError()).isEqualTo("BAD_REQUEST");

            ResponseEntity<ErrorResponse> internalErrorResponse = exceptionHandler.handleInternalError();
            assertThat(internalErrorResponse.getBody().getError()).isEqualTo("INTERNAL_SERVER");
        }

        @Test
        @DisplayName("Должен правильно устанавливать поле message в ErrorResponse")
        void shouldCorrectlySetMessageFieldInErrorResponse() {
            String testMessage = "Тестовое сообщение об ошибке";

            ResponseEntity<ErrorResponse> response1 = exceptionHandler.handleBadRequest(new IllegalArgumentException(testMessage));
            assertThat(response1.getBody().getMessage()).isEqualTo(testMessage);

            ResponseEntity<ErrorResponse> response2 = exceptionHandler.EntityNotFound();
            assertThat(response2.getBody().getMessage()).isEqualTo("Запись не найднен");

            ResponseEntity<ErrorResponse> response3 = exceptionHandler.handleInvalidImage(new InvalidImageException(testMessage));
            assertThat(response3.getBody().getMessage()).isEqualTo(testMessage);

            ResponseEntity<ErrorResponse> response4 = exceptionHandler.handleInternalError();
            assertThat(response4.getBody().getMessage()).isEqualTo("Произошла непредвиденная ошибка");
        }
    }
}