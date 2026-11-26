package cl.acis.usuarios.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(name = "uk_usuario_correo", columnNames = "correo"))
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String correo;
    @Column(nullable = false)
    private String nombre;
    @Column(nullable = false)
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;
    @Column(nullable = false)
    private boolean activo = true;
    @Column(nullable = false)
    private boolean debeCambiarContrasena = false;
    private LocalDateTime aceptoTerminosEn;
    private String versionTerminos;
    @Column(nullable = false)
    private int intentosFallidos = 0;
    private LocalDateTime bloqueadoHasta;
    @Column(nullable = false, updatable = false)
    private LocalDateTime creadoEn;
    @Column(nullable = false)
    private LocalDateTime modificadoEn;

    public Usuario() {
    }

    @PrePersist
    void crear() {
        creadoEn = LocalDateTime.now();
        modificadoEn = creadoEn;
    }

    @PreUpdate
    void actualizar() {
        modificadoEn = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long v) {
        id = v;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String v) {
        correo = v;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String v) {
        nombre = v;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String v) {
        passwordHash = v;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol v) {
        rol = v;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean v) {
        activo = v;
    }

    public boolean isDebeCambiarContrasena() {
        return debeCambiarContrasena;
    }

    public void setDebeCambiarContrasena(boolean v) {
        debeCambiarContrasena = v;
    }

    public LocalDateTime getAceptoTerminosEn() {
        return aceptoTerminosEn;
    }

    public void setAceptoTerminosEn(LocalDateTime v) {
        aceptoTerminosEn = v;
    }

    public String getVersionTerminos() {
        return versionTerminos;
    }

    public void setVersionTerminos(String v) {
        versionTerminos = v;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(int v) {
        intentosFallidos = v;
    }

    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public void setBloqueadoHasta(LocalDateTime v) {
        bloqueadoHasta = v;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public LocalDateTime getModificadoEn() {
        return modificadoEn;
    }
}
