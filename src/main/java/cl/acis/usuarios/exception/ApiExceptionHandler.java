package cl.acis.usuarios.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {
    record Error(LocalDateTime timestamp, int status, String error, String mensaje) {
    }

    private ResponseEntity<Error> e(HttpStatus s, String m) {
        return ResponseEntity.status(s).body(new Error(LocalDateTime.now(), s.value(), s.getReasonPhrase(), m));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Error> valid(MethodArgumentNotValidException x) {
        return e(HttpStatus.BAD_REQUEST, x.getBindingResult().getFieldErrors().stream().map(a -> a.getDefaultMessage())
                .collect(Collectors.joining("; ")));
    }

    @ExceptionHandler(NoAutorizadoException.class)
    ResponseEntity<Error> no(NoAutorizadoException x) {
        return e(HttpStatus.UNAUTHORIZED, x.getMessage());
    }

    @ExceptionHandler(ProhibidoException.class)
    ResponseEntity<Error> pro(ProhibidoException x) {
        return e(HttpStatus.FORBIDDEN, x.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    ResponseEntity<Error> not(RecursoNoEncontradoException x) {
        return e(HttpStatus.NOT_FOUND, x.getMessage());
    }

    @ExceptionHandler(ConflictoException.class)
    ResponseEntity<Error> con(ConflictoException x) {
        return e(HttpStatus.CONFLICT, x.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    ResponseEntity<Error> reg(ReglaNegocioException x) {
        return e(HttpStatus.BAD_REQUEST, x.getMessage());
    }
}
