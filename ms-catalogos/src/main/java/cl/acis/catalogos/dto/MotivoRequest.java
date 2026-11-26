package cl.acis.catalogos.dto; import cl.acis.catalogos.model.TipoMotivo; import jakarta.validation.constraints.*; public record MotivoRequest(@NotNull TipoMotivo tipo,@NotBlank String texto){}
