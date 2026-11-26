package cl.acis.ordenes.dto; import cl.acis.ordenes.model.*; import jakarta.validation.constraints.*; import java.time.LocalDateTime;
public record OrdenRequest(@NotNull Long clienteId,Long equipoId,@NotBlank String tipoServicio,@NotNull Prioridad prioridad,@NotNull @FutureOrPresent LocalDateTime fechaProgramada,@NotBlank String descripcion,String comentarioCliente){}
