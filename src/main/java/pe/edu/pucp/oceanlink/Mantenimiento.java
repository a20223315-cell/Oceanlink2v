package pe.edu.pucp.oceanlink;

public class Mantenimiento {
    private long id;
    private String codigo;
    private long tipoId;
    private long estacionId;
    private long segmentoId;
    private String fase;
    private java.time.LocalDateTime inicio;
    private java.time.LocalDateTime fin;
    private java.time.LocalDateTime inicioReal;
    private java.time.LocalDateTime cierre;
    private String descripcion;
    private String resultado;
    private long creadoPor;
    private String tipoNombre;
    private String elementoNombre;
    public long getId(){return id;} public void setId(long v){id=v;}
    public String getCodigo(){return codigo;} public void setCodigo(String v){codigo=v;}
    public long getTipoId(){return tipoId;} public void setTipoId(long v){tipoId=v;}
    public long getEstacionId(){return estacionId;} public void setEstacionId(long v){estacionId=v;}
    public long getSegmentoId(){return segmentoId;} public void setSegmentoId(long v){segmentoId=v;}
    public String getFase(){return fase;} public void setFase(String v){fase=v;}
    public java.time.LocalDateTime getInicio(){return inicio;} public void setInicio(java.time.LocalDateTime v){inicio=v;}
    public java.time.LocalDateTime getFin(){return fin;} public void setFin(java.time.LocalDateTime v){fin=v;}
    public java.time.LocalDateTime getInicioReal(){return inicioReal;} public void setInicioReal(java.time.LocalDateTime v){inicioReal=v;}
    public java.time.LocalDateTime getCierre(){return cierre;} public void setCierre(java.time.LocalDateTime v){cierre=v;}
    public String getDescripcion(){return descripcion;} public void setDescripcion(String v){descripcion=v;}
    public String getResultado(){return resultado;} public void setResultado(String v){resultado=v;}
    public long getCreadoPor(){return creadoPor;} public void setCreadoPor(long v){creadoPor=v;}
    public String getTipoNombre(){return tipoNombre;} public void setTipoNombre(String v){tipoNombre=v;}
    public String getElementoNombre(){return elementoNombre;} public void setElementoNombre(String v){elementoNombre=v;}
    public String getInicioTexto(){return inicio==null?"":inicio.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));}
}
