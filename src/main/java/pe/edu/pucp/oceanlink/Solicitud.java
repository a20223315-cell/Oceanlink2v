package pe.edu.pucp.oceanlink;

public class Solicitud {
    private long id;
    private String codigo;
    private long clienteId;
    private long origenId;
    private long destinoId;
    private java.math.BigDecimal capacidad;
    private String estado;
    private String observacion;
    private long creadoPor;
    private String clienteNombre;
    private String origenNombre;
    private String destinoNombre;
    private String ruta;
    public long getId(){return id;} public void setId(long v){id=v;}
    public String getCodigo(){return codigo;} public void setCodigo(String v){codigo=v;}
    public long getClienteId(){return clienteId;} public void setClienteId(long v){clienteId=v;}
    public long getOrigenId(){return origenId;} public void setOrigenId(long v){origenId=v;}
    public long getDestinoId(){return destinoId;} public void setDestinoId(long v){destinoId=v;}
    public java.math.BigDecimal getCapacidad(){return capacidad;} public void setCapacidad(java.math.BigDecimal v){capacidad=v;}
    public String getEstado(){return estado;} public void setEstado(String v){estado=v;}
    public String getObservacion(){return observacion;} public void setObservacion(String v){observacion=v;}
    public long getCreadoPor(){return creadoPor;} public void setCreadoPor(long v){creadoPor=v;}
    public String getClienteNombre(){return clienteNombre;} public void setClienteNombre(String v){clienteNombre=v;}
    public String getOrigenNombre(){return origenNombre;} public void setOrigenNombre(String v){origenNombre=v;}
    public String getDestinoNombre(){return destinoNombre;} public void setDestinoNombre(String v){destinoNombre=v;}
    public String getRuta(){return ruta;} public void setRuta(String v){ruta=v;}
}
