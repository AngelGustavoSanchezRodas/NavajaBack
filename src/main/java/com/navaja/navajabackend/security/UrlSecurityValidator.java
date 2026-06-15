package com.navaja.navajabackend.security;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

@Service
public class UrlSecurityValidator {

    private final RestTemplate restTemplate;

    public UrlSecurityValidator() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);
        this.restTemplate = new RestTemplate(factory);
    }

    public void validateSafeUrl(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            throw new IllegalArgumentException("La URL no puede estar vacía");
        }

        try {
            URI uri = new URI(urlString);
            if (!uri.isAbsolute() || uri.getHost() == null) {
                throw new IllegalArgumentException("La URL debe ser absoluta y válida");
            }

            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException("La URL debe usar el esquema http o https");
            }

            InetAddress address = InetAddress.getByName(uri.getHost());
            if (address.isAnyLocalAddress() || address.isLoopbackAddress() ||
                address.isLinkLocalAddress() || address.isSiteLocalAddress()) {
                throw new SecurityException("Dirección no permitida: posible ataque SSRF bloqueado.");
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("La URL enviada no tiene un formato válido", e);
        } catch (java.net.UnknownHostException e) {
            throw new IllegalArgumentException("El host de la URL no se pudo resolver", e);
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al validar la URL", e);
        }
    }

    public void validateImageUrl(String urlString) {
        validateSafeUrl(urlString);
        try {
            ResponseEntity<Void> response = restTemplate.exchange(urlString, HttpMethod.HEAD, null, Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                String contentType = response.getHeaders().getFirst("Content-Type");
                if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                    throw new IllegalArgumentException("La URL no apunta a una imagen válida (Content-Type: " + contentType + ")");
                }
            } else {
                throw new IllegalArgumentException("No se pudo acceder a la imagen, código de estado: " + response.getStatusCode().value());
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("No fue posible validar la imagen en la URL proporcionada: " + e.getMessage());
        }
    }
}
