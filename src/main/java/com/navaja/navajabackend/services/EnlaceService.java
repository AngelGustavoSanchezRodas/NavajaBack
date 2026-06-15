package com.navaja.navajabackend.services;

import com.navaja.navajabackend.dto.CrearEnlaceRequest;
import com.navaja.navajabackend.dto.EnlaceResponse;
import com.navaja.navajabackend.exceptions.AliasEnUsoException;
import com.navaja.navajabackend.exceptions.EnlaceNoEncontradoException;
import com.navaja.navajabackend.models.Enlace;
import com.navaja.navajabackend.models.TipoEnlace;
import com.navaja.navajabackend.models.Usuario;
import com.navaja.navajabackend.repositories.EnlaceRepository;
import com.navaja.navajabackend.repositories.ClicRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import com.navaja.navajabackend.dto.ActualizarEnlaceRequest;
import java.util.Map;
import com.navaja.navajabackend.security.UrlSecurityValidator;

@Service
public class EnlaceService {

    private static final int MAX_SHORTCODE_ATTEMPTS = 10;

    private final EnlaceRepository enlaceRepository;
    private final ShortcodeGenerator shortcodeGenerator;
    private final QuotaService quotaService;
    private final ClicRepository clicRepository;
    private final CacheManager cacheManager;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final EnlaceMapper enlaceMapper;
    private final UrlSecurityValidator urlSecurityValidator;

    public EnlaceService(
            EnlaceRepository enlaceRepository,
            ShortcodeGenerator shortcodeGenerator,
            QuotaService quotaService,
            ClicRepository clicRepository,
            CacheManager cacheManager,
            AuthenticatedUserResolver authenticatedUserResolver,
            EnlaceMapper enlaceMapper,
            UrlSecurityValidator urlSecurityValidator
    ) {
        this.enlaceRepository = enlaceRepository;
        this.shortcodeGenerator = shortcodeGenerator;
        this.quotaService = quotaService;
        this.clicRepository = clicRepository;
        this.cacheManager = cacheManager;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.enlaceMapper = enlaceMapper;
        this.urlSecurityValidator = urlSecurityValidator;
    }

    @Transactional
    public EnlaceResponse crearEnlace(CrearEnlaceRequest request) {
        Usuario usuario = authenticatedUserResolver.resolveOrNull();
        String aliasPersonalizado = request.alias();
        String tipoHerramienta = normalizarTipoHerramienta(request.tipoHerramienta());
        TipoEnlace tipoEnlace = request.tipo() != null ? request.tipo() : TipoEnlace.STANDARD;

        if (request.urlOriginal() == null || request.urlOriginal().isBlank()) {
            throw new IllegalArgumentException("La URL original es obligatoria para este tipo de enlace");
        }

        OffsetDateTime fechaExpiracion = null;
        if (usuario != null) {
            quotaService.verificarLimite(usuario, aliasPersonalizado);
        } else {
            fechaExpiracion = OffsetDateTime.now().plusHours(24);
        }

        if (tipoEnlace == TipoEnlace.SIGNATURE) {
            Object templateObj = request.metadata() == null ? null : request.metadata().get("templateId");
            String templateId = templateObj == null ? null : String.valueOf(templateObj);
            String usuarioIdStr = usuario == null ? null : String.valueOf(usuario.getId());
            quotaService.validarCreacionFirma(usuarioIdStr, templateId);
        } else if (tipoEnlace == TipoEnlace.STANDARD) {
            String usuarioIdStr = usuario == null ? null : String.valueOf(usuario.getId());
            quotaService.validarCreacionAcortador(usuarioIdStr, request.alias());
        } else if (tipoEnlace == TipoEnlace.QR) {
            String usuarioIdStr = usuario == null ? null : String.valueOf(usuario.getId());
            quotaService.validarCreacionQr(usuarioIdStr, request.alias());
        }

        String codigoCorto = resolverCodigoCorto(request);

        Enlace enlace = new Enlace();
        enlace.setCodigoCorto(codigoCorto);

        enlace.setUrlOriginal(request.urlOriginal());

        enlace.setEsDinamico(Boolean.TRUE.equals(request.esDinamico()));
        enlace.setUsuario(usuario);
        enlace.setTipoHerramienta(tipoHerramienta);
        enlace.setFechaExpiracion(fechaExpiracion);
        enlace.setTipo(tipoEnlace);
        
        Map<String, Object> mapMeta = enlaceMapper.toMetadata(request.metadata());
        if (tipoEnlace == TipoEnlace.SIGNATURE && mapMeta != null && mapMeta.get("imageUrl") != null) {
            urlSecurityValidator.validateImageUrl(String.valueOf(mapMeta.get("imageUrl")));
        } else if (request.urlOriginal() != null && !request.urlOriginal().isBlank()) {
            urlSecurityValidator.validateSafeUrl(request.urlOriginal());
        }

        enlace.setMetadata(mapMeta);

        Enlace saved = guardarEnlace(enlace);

        return enlaceMapper.toResponse(saved);
    }

    @Transactional
    @CachePut(value = "enlaces", key = "#result.codigoCorto", unless = "#result == null")
    public Enlace guardarEnlace(Enlace enlace) {
        return enlaceRepository.save(enlace);
    }

    @Transactional
    @CacheEvict(value = "enlaces", key = "#result.codigoCorto")
    public EnlaceResponse actualizarEnlace(Long enlaceId, ActualizarEnlaceRequest request) {
        Usuario usuario = authenticatedUserResolver.resolveOrNull();
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        Enlace enlace = enlaceRepository.findByIdAndUsuarioId(enlaceId, usuario.getId())
                .orElseThrow(EnlaceNoEncontradoException::new);

        if (request.urlOriginal() != null && !request.urlOriginal().isBlank()) {
            urlSecurityValidator.validateSafeUrl(request.urlOriginal());
            enlace.setUrlOriginal(request.urlOriginal());
        }

        if (request.esDinamico() != null) {
            enlace.setEsDinamico(request.esDinamico());
        }

        if (request.metadata() != null) {
            Map<String, Object> newMeta = enlaceMapper.toMetadata(request.metadata());
            if (enlace.getTipo() == TipoEnlace.SIGNATURE && newMeta.get("imageUrl") != null) {
                urlSecurityValidator.validateImageUrl(String.valueOf(newMeta.get("imageUrl")));
            }
            enlace.setMetadata(newMeta);
        }

        Enlace saved = guardarEnlace(enlace);
        return enlaceMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EnlaceResponse> listarEnlaces(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El usuarioId es obligatorio para listar enlaces");
        }
        List<Enlace> enlaces = enlaceRepository.findAllByUsuarioIdOrderByFechaCreacionDesc(usuarioId);

        return enlaces.stream().map(enlaceMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EnlaceResponse> listarEnlacesPorTipo(Long usuarioId, TipoEnlace tipo) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El usuarioId es obligatorio para listar enlaces");
        }
        List<Enlace> enlaces = enlaceRepository.findAllByUsuarioIdAndTipoOrderByFechaCreacionDesc(usuarioId, tipo);

        return enlaces.stream().map(enlaceMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "enlaces", key = "#codigoCorto", unless = "#result == null")
    public Enlace obtenerEnlacePorCodigoCorto(String codigoCorto) {
        Enlace enlace = enlaceRepository.findByCodigoCorto(codigoCorto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shortcode no encontrado"));

        if (estaExpirado(enlace)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shortcode no encontrado");
        }

        return enlace;
    }

    @org.springframework.cache.annotation.Cacheable(value = "enlaces", key = "#alias", unless = "#result == null")
    public String obtenerUrlOriginalPorAlias(String alias) {
        Enlace enlace = obtenerEnlacePorCodigoCorto(alias);

        if (estaExpirado(enlace)) {
            eliminarEnlace(enlace);
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Shortcode expirado");
        }

        return enlace.getUrlOriginal();
    }

    @Transactional
    @CacheEvict(value = "enlaces", key = "#enlace.codigoCorto")
    public void eliminarEnlace(Enlace enlace) {
        enlaceRepository.delete(enlace);
    }

    @Transactional
    public void eliminarEnlacePropietario(Long enlaceId, Long usuarioId) {
        Enlace enlace = enlaceRepository.findByIdAndUsuarioId(enlaceId, usuarioId)
                .orElseThrow(EnlaceNoEncontradoException::new);

        clicRepository.deleteByEnlaceId(enlace.getId());
        enlaceRepository.delete(enlace);

        if (cacheManager != null) {
            org.springframework.cache.Cache cache = cacheManager.getCache("enlaces");
            if (cache != null) {
                cache.evict(enlace.getCodigoCorto());
            }
        }
    }

    private String generateUniqueShortcode() {
        for (int attempt = 0; attempt < MAX_SHORTCODE_ATTEMPTS; attempt++) {
            String shortcode = shortcodeGenerator.generate();
            if (!enlaceRepository.existsByCodigoCorto(shortcode)) {
                return shortcode;
            }
        }

        throw new IllegalStateException("No fue posible generar un shortcode único");
    }

    private boolean estaExpirado(Enlace enlace) {
        return enlace.getFechaExpiracion() != null && enlace.getFechaExpiracion().isBefore(OffsetDateTime.now());
    }

    private String normalizarTipoHerramienta(String tipoHerramienta) {
        return StringUtils.hasText(tipoHerramienta) ? tipoHerramienta.trim().toUpperCase() : "QR";
    }

    private String resolverCodigoCorto(CrearEnlaceRequest request) {
        if (!StringUtils.hasText(request.alias())) {
            return generateUniqueShortcode();
        }

        String alias = request.alias().trim();
        if (enlaceRepository.existsByCodigoCorto(alias)) {
            throw new AliasEnUsoException("El alias ya esta en uso");
        }

        return alias;
    }
}
