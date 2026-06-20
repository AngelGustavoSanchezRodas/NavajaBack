# 🤖 Reglas de Oro para Asistentes de IA (AI Guidelines)

Este documento define las normas estrictas y reglas de oro que **cualquier asistente de Inteligencia Artificial** (o desarrollador) debe seguir al interactuar, refactorizar o agregar código en este proyecto (`NavajaBack`).

Antes de proponer o escribir cualquier línea de código, **debes leer y aplicar** los siguientes principios.

---

## 1. 🥇 Filosofía y Calidad de Código
*   **Cero "Parches" (No Quick Fixes):** Se prohíbe proponer soluciones temporales, parches rápidos o "hacks". Cada solución debe ser **profesional, robusta y definitiva**.
*   **Cero Deuda Técnica:** No dejes código comentado, variables sin usar, o implementaciones "para arreglar después" (TODOs sin justificación). El código entregado debe estar listo para producción.
*   **Buenas Prácticas (SOLID & Clean Code):** Aplica principios SOLID. Escribe métodos cortos, con una única responsabilidad. Si un método o clase se vuelve muy grande, divídelo lógicamente.

## 2. 🏛️ Arquitectura del Proyecto
El proyecto está construido en **Java 21** usando **Spring Boot 3+** y sigue una **Arquitectura de N-Capas** bien definida:
*   `controllers`: Endpoints REST únicamente. Aquí solo se recibe la petición, se validan los DTOs y se delega al servicio. No debe haber lógica de negocio aquí.
*   `services`: Contiene toda la lógica de negocio de la aplicación.
*   `repositories`: Interfaces que extienden de Spring Data JPA (ej. `JpaRepository`) para la persistencia en base de datos (PostgreSQL).
*   `models`: Entidades de dominio (Anotadas con `@Entity`, `@Table`, etc.).
*   `dto`: Objetos de Transferencia de Datos. **Deben ser implementados usando Java `record`**, no clases convencionales.
*   `security`: Configuraciones de JWT, filtros y Spring Security.
*   `exceptions`: Manejo global de errores (ej. usando `@ControllerAdvice` como en `ManejadorExcepcionesGlobal.java`).
*   `config`: Configuraciones de beans, CORS, Swagger, etc.

## 3. ✍️ Convenciones de Sintaxis y Estilo
*   **Nomenclatura CamelCase:** Usa estricto `camelCase` para variables, propiedades y métodos (ej. `iniciarSesion`, `registroRequest`).
*   **PascalCase:** Para nombres de Clases e Interfaces (ej. `AuthController`, `ManejadorExcepcionesGlobal`).
*   **UPPER_SNAKE_CASE:** Para variables constantes (`static final`).
*   **Idioma:** El código mantiene una mezcla aceptada donde las Clases técnicas o de capa pueden estar en inglés (`AuthController`), pero los métodos y variables de dominio a menudo están en español (`iniciarSesion`, `contrasena`). Sé **consistente** con el archivo que estés editando. No mezcles idiomas en el mismo bloque si se puede evitar, y prefiere seguir la convención ya establecida en la clase.

## 4. ⚙️ Reglas Técnicas Específicas
*   **Inyección de Dependencias:** Utiliza **siempre inyección por constructor**. Está estrictamente prohibido usar la anotación `@Autowired` en los campos (Field Injection).
*   **Sin Lombok:** Este proyecto **no utiliza Lombok**. Para los DTOs usa `records`. Para las Entidades, genera los Getters, Setters y Constructores de forma manual con la sintaxis de Java estándar.
*   **Validaciones (Jakarta Validation):** Usa validaciones en la capa de controladores con anotaciones como `@Valid`, `@NotBlank`, `@Email`, etc., ubicadas en los DTOs.
*   **Seguridad:** El proyecto usa `jjwt` (JWT) y Spring Security. Toda nueva ruta protegida debe integrarse adecuadamente a la configuración de seguridad existente.
*   **Respuestas HTTP:** Utiliza `ResponseEntity` de forma consistente para devolver los status codes correctos (`200 OK`, `201 Created`, `400 Bad Request`, `404 Not Found`, etc.).

## 5. 🛠️ Stack Tecnológico Reconocido
Considera que estas librerías están en el proyecto. Utilízalas en lugar de reinventar la rueda o agregar nuevas dependencias innecesariamente:
*   Base de Datos: **PostgreSQL**
*   Seguridad: **Spring Security + JWT (jjwt)**
*   Rate Limiting: **Bucket4j**
*   Manejo de Imágenes/QR: **Zxing, Thumbnailator, TwelveMonkeys (WebP/TIFF)**
*   Parsing HTML: **Jsoup**
*   Documentación de API: **Springdoc OpenAPI (Swagger)**

## ⚠️ Instrucción Final para la IA
Cuando se te asigne una tarea, responde brevemente confirmando que has leído este archivo de reglas (`AI_RULES.md`) y asegura que todo código generado cumple estrictamente con las normativas descritas. **Piensa paso a paso** antes de implementar cambios estructurales.
