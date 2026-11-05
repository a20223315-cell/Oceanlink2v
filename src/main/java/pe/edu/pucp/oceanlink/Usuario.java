package pe.edu.pucp.oceanlink;

public class Usuario {
    private boolean primeraActivacion;
    public void setPrimeraActivacion(boolean v){primeraActivacion=v;}
    public boolean isPendiente(){return estado==0 && !primeraActivacion;}
    public String getEstadoTexto(){return estado==1?"Activo":isPendiente()?"Pendiente de activación":"Inactivo";}
    public String getEstadoClase(){return estado==1?"ok":isPendiente()?"warning":"danger";}
    public String getApellidos(){return nombre==null?"":nombre.contains(",")?nombre.split(",",2)[0].trim():nombre.trim().split(" +").length==2?nombre.substring(nombre.indexOf(' ')+1):"";}
    public String getNombres(){return nombre==null?"":nombre.contains(",")?nombre.split(",",2)[1].trim():nombre.trim().split(" +").length==2?nombre.split(" ",2)[0]:nombre;}
    public String getNombreOrdenado(){return getApellidos().isEmpty()?getNombres():getApellidos()+", "+getNombres();}
    public String getPrefijo(){return Telefono.prefijoRegion(getTelefonoRegion());}
    public String getTelefonoLocal(){return Telefono.local(telefono,getPrefijo());}

    private java.time.LocalDateTime ultimoAcceso;
    public void setUltimoAcceso(java.time.LocalDateTime v){ultimoAcceso=v;}
    public String getUltimoAccesoTexto(){return ultimoAcceso==null?"Sin acceso registrado":ultimoAcceso.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));}
    public String getFechaIngresoTexto(){return fechaIngreso==null?"Sin registro":fechaIngreso.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));}
    private String telefonoRegion;
    public String getTelefonoRegion(){return telefonoRegion==null||telefonoRegion.isBlank()?Telefono.region(telefono,""):telefonoRegion;}
    public void setTelefonoRegion(String v){telefonoRegion=v;}
    public String getPaisCodigo(){return Paises.codigo(paisNombre);}
    private long id;
    private String codigo;
    private String nombre;
    private String correo;
    private String telefono;
    private long rolId;
    private long paisId;
    private int estado;
    private java.time.LocalDateTime fechaIngreso;
    private String rolNombre;
    private String rolCodigo;
    private String paisNombre;
    public long getId(){return id;} public void setId(long v){id=v;}
    public String getCodigo(){return codigo;} public void setCodigo(String v){codigo=v;}
    public String getNombre(){return nombre;} public void setNombre(String v){nombre=v;}
    public String getCorreo(){return correo;} public void setCorreo(String v){correo=v;}
    public String getTelefono(){return telefono;} public void setTelefono(String v){telefono=v;}
    public long getRolId(){return rolId;} public void setRolId(long v){rolId=v;}
    public long getPaisId(){return paisId;} public void setPaisId(long v){paisId=v;}
    public int getEstado(){return estado;} public void setEstado(int v){estado=v;}
    public java.time.LocalDateTime getFechaIngreso(){return fechaIngreso;} public void setFechaIngreso(java.time.LocalDateTime v){fechaIngreso=v;}
    public String getRolNombre(){return rolNombre;} public void setRolNombre(String v){rolNombre=v;}
    public String getRolCodigo(){return rolCodigo;} public void setRolCodigo(String v){rolCodigo=v;}
    public String getPaisNombre(){return paisNombre;} public void setPaisNombre(String v){paisNombre=v;}
}
