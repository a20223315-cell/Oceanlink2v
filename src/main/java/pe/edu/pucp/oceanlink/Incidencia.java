package pe.edu.pucp.oceanlink;

public class Incidencia {
    private String segmentos; public String getSegmentos(){return segmentos;} public void setSegmentos(String v){segmentos=v;}
    private long id;
    private String codigo;
    private String severidad;
    private String fase;
    private java.time.LocalDateTime deteccion;
    private java.time.LocalDateTime restauracion;
    private java.time.LocalDateTime cierre;
    private String descripcion;
    private String resultado;
    private long creadoPor;
    private String ruta;
    public long getId(){return id;} public void setId(long v){id=v;}
    public String getCodigo(){return codigo;} public void setCodigo(String v){codigo=v;}
    public String getSeveridad(){return severidad;} public void setSeveridad(String v){severidad=v;}
    public String getFase(){return fase;} public void setFase(String v){fase=v;}
    public java.time.LocalDateTime getDeteccion(){return deteccion;} public void setDeteccion(java.time.LocalDateTime v){deteccion=v;}
    public java.time.LocalDateTime getRestauracion(){return restauracion;} public void setRestauracion(java.time.LocalDateTime v){restauracion=v;}
    public java.time.LocalDateTime getCierre(){return cierre;} public void setCierre(java.time.LocalDateTime v){cierre=v;}
    public String getDescripcion(){return descripcion;} public void setDescripcion(String v){descripcion=v;}
    public String getResultado(){return resultado;} public void setResultado(String v){resultado=v;}
    public long getCreadoPor(){return creadoPor;} public void setCreadoPor(long v){creadoPor=v;}
    public String getRuta(){return ruta;} public void setRuta(String v){ruta=v;}
    public String getDeteccionTexto(){return deteccion==null?"":deteccion.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));}
}
