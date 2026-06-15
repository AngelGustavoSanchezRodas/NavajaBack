package com.navaja.navajabackend.services;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ImageConversionServiceTest {

    private final ImageConversionService imageConversionService = new ImageConversionService();

    @Test
    void convertDebeRechazarFormatoNoSoportado() {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", new byte[] {1, 2, 3});

        assertThrows(IllegalArgumentException.class, () -> {
            imageConversionService.convert(file, "", false, null);
        });
    }

    @Test
    void convert_WithUnsupportedFormat_ShouldThrowException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", new byte[10]);

        assertThrows(IllegalArgumentException.class, () -> {
            imageConversionService.convert(file, "svg", false, null);
        });
    }
}
