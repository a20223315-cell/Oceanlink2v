package pe.edu.pucp.oceanlink;

public class Pais {
    private long id; private String codigo; private String nombre;
    public Pais() {} public Pais(long id) { this.id=id; }
    public long getId(){return id;} public void setId(long v){id=v;}
    public String getCodigo(){return codigo;} public void setCodigo(String v){codigo=v;}
    public String getNombre(){return nombre;} public void setNombre(String v){nombre=v;}
}
