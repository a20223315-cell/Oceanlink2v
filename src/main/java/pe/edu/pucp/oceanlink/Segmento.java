package pe.edu.pucp.oceanlink;

public class Segmento {
    private long id;
    private String codigo;
    private long origenId;
    private long destinoId;
    private java.math.BigDecimal capacidad;
    private String condicion;
    private int estado;
    private String origenNombre;
    private String destinoNombre;
    public long getId(){return id;} public void setId(long v){id=v;}
    public String getCodigo(){return codigo;} public void setCodigo(String v){codigo=v;}
    public long getOrigenId(){return origenId;} public void setOrigenId(long v){origenId=v;}
    public long getDestinoId(){return destinoId;} public void setDestinoId(long v){destinoId=v;}
    public java.math.BigDecimal getCapacidad(){return capacidad;} public void setCapacidad(java.math.BigDecimal v){capacidad=v;}
    public String getCondicion(){return condicion;} public void setCondicion(String v){condicion=v;}
    public int getEstado(){return estado;} public void setEstado(int v){estado=v;}
    public String getOrigenNombre(){return origenNombre;} public void setOrigenNombre(String v){origenNombre=v;}
    public String getDestinoNombre(){return destinoNombre;} public void setDestinoNombre(String v){destinoNombre=v;}
}
