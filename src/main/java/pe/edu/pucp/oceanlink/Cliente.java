package pe.edu.pucp.oceanlink;

public class Cliente {
    private int servicios; public int getServicios(){return servicios;} public void setServicios(int v){servicios=v;}
    private long id;
    private String codigo;
    private String nombre;
    private String tipoDocumento;
    private String documento;
    private String contacto;
    private String correo;
    private String telefono;
    private int estado;
    public long getId(){return id;} public void setId(long v){id=v;}
    public String getCodigo(){return codigo;} public void setCodigo(String v){codigo=v;}
    public String getNombre(){return nombre;} public void setNombre(String v){nombre=v;}
    public String getTipoDocumento(){return tipoDocumento;} public void setTipoDocumento(String v){tipoDocumento=v;}
    public String getDocumento(){return documento;} public void setDocumento(String v){documento=v;}
    public String getContacto(){return contacto;} public void setContacto(String v){contacto=v;}
    public String getCorreo(){return correo;} public void setCorreo(String v){correo=v;}
    public String getTelefono(){return telefono;} public void setTelefono(String v){telefono=v;}
    public int getEstado(){return estado;} public void setEstado(int v){estado=v;}
}
