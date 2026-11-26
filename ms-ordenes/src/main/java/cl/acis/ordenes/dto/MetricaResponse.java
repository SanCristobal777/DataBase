package cl.acis.ordenes.dto; import java.util.Map; public record MetricaResponse(Map<String,Long> porEstado,long fueraDeSla,double tiempoPromedioHoras){}
