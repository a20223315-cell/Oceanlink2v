package pe.edu.pucp.oceanlink;

public class Opcion {
    private final long id; private final String nombre;
    public Opcion(long id,String nombre){this.id=id;this.nombre=nombre;}
    public long getId(){return id;} public String getNombre(){return nombre;}
}
