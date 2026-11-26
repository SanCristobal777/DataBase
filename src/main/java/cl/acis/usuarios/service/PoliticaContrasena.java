package cl.acis.usuarios.service;
import cl.acis.usuarios.exception.ReglaNegocioException; import java.util.regex.Pattern; import org.springframework.stereotype.Component;
@Component public class PoliticaContrasena { private static final Pattern P=Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"); public void validar(String p){if(p==null||!P.matcher(p).matches()) throw new ReglaNegocioException("La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula y un número");} }
