package pe.edu.pucp.oceanlink;

public class Estacion {
    private long id;
    private String codigo;
    private String nombre;
    private String ubicacion;
    private java.math.BigDecimal capacidad;
    private String condicion;
    private int estado;
    public long getId(){return id;} public void setId(long v){id=v;}
    public String getCodigo(){return codigo;} public void setCodigo(String v){codigo=v;}
    public String getNombre(){return nombre;} public void setNombre(String v){nombre=v;}
    public String getUbicacion(){return ubicacion;} public void setUbicacion(String v){ubicacion=v;}
    public long getPaisId(){return pais.getId();} public void setPaisId(long v){pais.setId(v);}
    public java.math.BigDecimal getCapacidad(){return capacidad;} public void setCapacidad(java.math.BigDecimal v){capacidad=v;}
    public String getCondicion(){return condicion;} public void setCondicion(String v){condicion=v;}
    public int getEstado(){return estado;} public void setEstado(int v){estado=v;}
    private Pais pais=new Pais();
    public Pais getPais(){return pais;} public void setPais(Pais v){pais=v;}
}
