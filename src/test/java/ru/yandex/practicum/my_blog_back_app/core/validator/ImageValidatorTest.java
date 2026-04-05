package ru.yandex.practicum.my_blog_back_app.core.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.my_blog_back_app.api.handler.InvalidImageException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты валидатора изображений")
class ImageValidatorTest {

    @InjectMocks
    private ImageValidator imageValidator;

    @Mock
    private MultipartFile image;

    @Test
    @DisplayName("Должен выбросить исключение когда изображение равно null")
    void validateWithNullImage() {
        assertThatThrownBy(() -> imageValidator.validate(null))
                .isInstanceOf(InvalidImageException.class)
                .hasMessage("Image file is empty");
    }

    @Test
    @DisplayName("Должен выбросить исключение когда изображение пустое")
    void validateWithEmptyImage() {
        when(image.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> imageValidator.validate(image))
                .isInstanceOf(InvalidImageException.class)
                .hasMessage("Image file is empty");
    }

    @Test
    @DisplayName("Должен выбросить исключение когда размер изображения больше 5 МБ")
    void validateWithSizeExceedsLimit() {
        long sizeMoreThan5MB = 6 * 1024 * 1024;
        when(image.isEmpty()).thenReturn(false);
        when(image.getSize()).thenReturn(sizeMoreThan5MB);

        assertThatThrownBy(() -> imageValidator.validate(image))
                .isInstanceOf(InvalidImageException.class)
                .hasMessage("Image size exceeds 5MB");
    }

    @Test
    @DisplayName("Не должен выбросить исключение когда размер изображения меньше 5 МБ")
    void validateWithSizeBelowLimit() {
        long sizeLessThan5MB = 3 * 1024 * 1024;
        when(image.isEmpty()).thenReturn(false);
        when(image.getSize()).thenReturn(sizeLessThan5MB);

        assertThatCode(() -> imageValidator.validate(image))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Не должен выбросить исключение когда размер изображения ровно 5 МБ")
    void validateWithSizeExactlyLimit() {
        long sizeExactly5MB = 5 * 1024 * 1024;
        when(image.isEmpty()).thenReturn(false);
        when(image.getSize()).thenReturn(sizeExactly5MB);

        assertThatCode(() -> imageValidator.validate(image))
                .doesNotThrowAnyException();
    }
}