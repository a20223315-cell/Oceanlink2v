package pe.edu.pucp.oceanlink;

public class Servicio {
    private long id;
    private String codigo;
    private long solicitudId;
    private java.math.BigDecimal capacidad;
    private String estado;
    private String clienteNombre;
    private String solicitudCodigo;
    private long origenId;
    private long destinoId;
    private String ruta;
    public long getId(){return id;} public void setId(long v){id=v;}
    public String getCodigo(){return codigo;} public void setCodigo(String v){codigo=v;}
    public long getSolicitudId(){return solicitudId;} public void setSolicitudId(long v){solicitudId=v;}
    public java.math.BigDecimal getCapacidad(){return capacidad;} public void setCapacidad(java.math.BigDecimal v){capacidad=v;}
    public String getEstado(){return estado;} public void setEstado(String v){estado=v;}
    public String getClienteNombre(){return clienteNombre;} public void setClienteNombre(String v){clienteNombre=v;}
    public String getSolicitudCodigo(){return solicitudCodigo;} public void setSolicitudCodigo(String v){solicitudCodigo=v;}
    public long getOrigenId(){return origenId;} public void setOrigenId(long v){origenId=v;}
    public long getDestinoId(){return destinoId;} public void setDestinoId(long v){destinoId=v;}
    public String getRuta(){return ruta;} public void setRuta(String v){ruta=v;}
    private String origenNombre; public String getOrigenNombre(){return origenNombre;} public void setOrigenNombre(String v){origenNombre=v;}
    private String destinoNombre; public String getDestinoNombre(){return destinoNombre;} public void setDestinoNombre(String v){destinoNombre=v;}
    private java.time.LocalDateTime fechaActivacion; public java.time.LocalDateTime getFechaActivacion(){return fechaActivacion;} public void setFechaActivacion(java.time.LocalDateTime v){fechaActivacion=v;} public String getFechaActivacionTexto(){return fechaActivacion==null?"Sin registrar":fechaActivacion.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));}
    private java.time.LocalDateTime fechaCancelacion; public java.time.LocalDateTime getFechaCancelacion(){return fechaCancelacion;} public void setFechaCancelacion(java.time.LocalDateTime v){fechaCancelacion=v;} public String getFechaCancelacionTexto(){return fechaCancelacion==null?"Sin registrar":fechaCancelacion.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));}
}
