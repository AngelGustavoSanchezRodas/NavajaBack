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

        assertThrows(IllegalArgumentException.class, () -> imageConversionService.convert(file, "svg"));
    }

    @Test
    void convertDebeRechazarFormatoVacio() {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", new byte[] {1, 2, 3});

        assertThrows(IllegalArgumentException.class, () -> imageConversionService.convert(file, ""));
    }
}
