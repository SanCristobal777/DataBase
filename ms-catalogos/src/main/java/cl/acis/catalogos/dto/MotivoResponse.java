package cl.acis.catalogos.dto; import cl.acis.catalogos.model.TipoMotivo; public record MotivoResponse(Long id,TipoMotivo tipo,String texto,boolean activo){}
