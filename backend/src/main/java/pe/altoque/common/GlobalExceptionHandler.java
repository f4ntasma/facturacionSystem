package pe.altoque.common;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record ErrorResponse(int status, String mensaje, Map<String, String> campos, Instant fecha) {}

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus status, String mensaje, Map<String, String> campos) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), mensaje, campos, Instant.now()));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> api(ApiException e) {
        return respuesta(e.getStatus(), e.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
        String primero = campos.values().stream().findFirst().orElse("Revisa los datos, pe.");
        return respuesta(HttpStatus.BAD_REQUEST, primero, campos);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> restriccion(ConstraintViolationException e) {
        return respuesta(HttpStatus.BAD_REQUEST, e.getMessage(), null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> ilegible(HttpMessageNotReadableException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "No pudimos leer los datos enviados.", null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> archivoGrande(MaxUploadSizeExceededException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "El archivo pesa demasiado (máximo 5 MB).", null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> credenciales(Exception e) {
        return respuesta(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos.", null);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> deshabilitado(Exception e) {
        return respuesta(HttpStatus.UNAUTHORIZED, "Tu usuario está desactivado. Habla con el dueño del negocio.", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> denegado(AccessDeniedException e) {
        return respuesta(HttpStatus.FORBIDDEN, "No tienes permiso para esto.", null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException e) {
        log.warn("Violación de integridad: {}", e.getMostSpecificCause().getMessage());
        return respuesta(HttpStatus.CONFLICT, "Ese registro ya existe o está en uso.", null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> sinRecurso(NoResourceFoundException e) {
        return respuesta(HttpStatus.NOT_FOUND, "Ruta no encontrada.", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> general(Exception e) {
        log.error("Error no controlado", e);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Algo salió mal de nuestro lado. Inténtalo otra vez.", null);
    }
}
