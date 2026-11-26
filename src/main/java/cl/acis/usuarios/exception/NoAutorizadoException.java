package cl.acis.usuarios.exception;

public class NoAutorizadoException extends RuntimeException {
    public NoAutorizadoException(String m) {
        super(m);
    }
}
