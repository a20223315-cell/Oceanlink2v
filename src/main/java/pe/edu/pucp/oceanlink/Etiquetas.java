package pe.edu.pucp.oceanlink;

import java.util.*;
public final class Etiquetas {
    private Etiquetas(){}
    public static Map<Object,String> todas(){Map<Object,String> out=new LinkedHashMap<>();out.put(0,"Inactivo");out.put(1,"Activo");
        out.put("0","Inactivo");
        out.put("1","Activo");
        out.put("OPERATIVA","Operativa");
        out.put("OPERATIVO","Operativo");
        out.put("DEGRADADO","Degradado");
        out.put("EN_MANTENIMIENTO","En mantenimiento");
        out.put("FUERA_SERVICIO","Fuera de servicio");
        out.put("REGISTRADA","Registrada");
        out.put("EN_EVALUACION","En evaluación");
        out.put("APROBADA","Aprobada");
        out.put("PROVISIONADA","Provisionada");
        out.put("ACTIVA","Activa");
        out.put("RECHAZADA","Rechazada");
        out.put("PENDIENTE_CAPACIDAD","Pendiente por capacidad");
        out.put("ACTIVO","Activo");
        out.put("PENDIENTE","Pendiente");
        out.put("CANCELADO","Cancelado");
        out.put("DETECTADA","Detectada");
        out.put("EN_ANALISIS","En análisis");
        out.put("REPARACION_PROGRAMADA","Reparación programada");
        out.put("EN_REPARACION","En reparación");
        out.put("RESTAURADA","Restaurada");
        out.put("CERRADA","Cerrada");
        out.put("PROGRAMADO","Programado");
        out.put("EN_PROGRESO","En progreso");
        out.put("FINALIZADO","Finalizado");
        out.put("BAJA","Baja");
        out.put("MEDIA","Media");
        out.put("ALTA","Alta");
        out.put("CRITICA","Crítica");
        out.put("PREVENTIVO","Preventivo");
        out.put("CORRECTIVO","Correctivo");
        return out;
    }
}
