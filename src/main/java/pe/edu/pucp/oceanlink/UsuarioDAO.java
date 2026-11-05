package pe.edu.pucp.oceanlink;

import java.sql.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public class UsuarioDAO extends DaoBase {
    private static final String CONSULTA="SELECT t.*,ro.nombre rol_nombre,pa.nombre pais_nombre FROM usuario t JOIN rol ro ON ro.id_rol=t.id_rol JOIN pais pa ON pa.id_pais=t.id_pais";
    private static final String FILTRO=" WHERE (t.codigo_usuario REGEXP ? OR CONCAT(t.nombre_completo,' ',t.correo) REGEXP ?) AND (?='' OR (CASE WHEN t.estado=0 AND t.fecha_primera_activacion IS NULL THEN 'pendiente' WHEN t.estado=0 THEN '0' ELSE '1' END)=?) AND (?='' OR CAST(t.id_rol AS CHAR)=?)";
    private Usuario leer(ResultSet rs)throws SQLException{
        Usuario e=new Usuario();
        e.setId(rs.getLong("id_usuario"));
        e.setCodigo(rs.getString("codigo_usuario"));
        e.setNombre(rs.getString("nombre_completo"));
        e.setCorreo(rs.getString("correo"));
        e.setTelefono(rs.getString("telefono"));e.setTelefonoRegion(rs.getString("telefono_region"));
        e.setRolId(rs.getLong("id_rol"));
        e.setPaisId(rs.getLong("id_pais"));
        e.setEstado(rs.getInt("estado"));e.setPrimeraActivacion(rs.getTimestamp("fecha_primera_activacion")!=null);
        e.setFechaIngreso((rs.getTimestamp("fecha_ingreso")==null?null:rs.getTimestamp("fecha_ingreso").toLocalDateTime()));
        e.setRolNombre(rs.getString("rol_nombre"));
        e.setPaisNombre(rs.getString("pais_nombre"));
        e.setUltimoAcceso(rs.getTimestamp("ultimo_acceso")==null?null:rs.getTimestamp("ultimo_acceso").toLocalDateTime());
        return e;
    }

    private String ordenar(String orden,String direccion){String columna=switch((orden==null || orden.isBlank()?"nombre":orden)){case "codigo"->"CAST(SUBSTRING_INDEX(t.codigo_usuario,'-',-1) AS UNSIGNED)";case "nombre"->"CASE WHEN LOCATE(',',t.nombre_completo)>0 THEN t.nombre_completo WHEN LENGTH(t.nombre_completo)-LENGTH(REPLACE(t.nombre_completo,' ',''))=1 THEN CONCAT(SUBSTRING(t.nombre_completo,LOCATE(' ',t.nombre_completo)+1),', ',SUBSTRING_INDEX(t.nombre_completo,' ',1)) ELSE t.nombre_completo END";case "correo"->"t.correo";case "rol"->"ro.nombre";case "estado"->"t.estado";default->"CAST(SUBSTRING_INDEX(t.codigo_usuario,'-',-1) AS UNSIGNED)";};return columna+("desc".equals(direccion)?" DESC":" ASC")+", t.nombre_completo ASC, t.id_usuario ASC";}
    private void filtros(PreparedStatement ps,String q,String estado,String rol)throws SQLException{ps.setString(1,Busqueda.codigo(q));ps.setString(2,Busqueda.texto(q));ps.setString(3,estado);ps.setString(4,estado);ps.setString(5,rol);ps.setString(6,rol);}
    public int contar(String q,String estado)throws SQLException{return contar(q,estado,"");}
    public int contar(String q,String estado,String rol)throws SQLException{try(Connection c=obtenerConexion();PreparedStatement ps=c.prepareStatement("SELECT COUNT(*) FROM usuario t JOIN rol ro ON ro.id_rol=t.id_rol JOIN pais pa ON pa.id_pais=t.id_pais"+FILTRO)){filtros(ps,q,estado,rol);try(ResultSet rs=ps.executeQuery()){rs.next();return rs.getInt(1);}}}
    public List<Usuario> listar(String q,String estado,int pagina,int tamano)throws SQLException{return listar(q,estado,pagina,tamano,"codigo","asc","");}
    public List<Usuario> listar(String q,String estado,int pagina,int tamano,String orden,String direccion,String rol)throws SQLException{List<Usuario> out=new ArrayList<>();try(Connection c=obtenerConexion();PreparedStatement ps=c.prepareStatement(CONSULTA+FILTRO+" ORDER BY "+ordenar(orden,direccion)+" LIMIT ? OFFSET ?")){filtros(ps,q,estado,rol);ps.setInt(7,tamano);ps.setLong(8,(long)(pagina-1)*tamano);try(ResultSet rs=ps.executeQuery()){while(rs.next())out.add(leer(rs));}}return out;}
    public Usuario buscar(long id)throws SQLException{try(Connection c=obtenerConexion()){return buscar(c,id);}}
    private Usuario buscar(Connection c,long id)throws SQLException{try(PreparedStatement ps=c.prepareStatement(CONSULTA+" WHERE t.id_usuario=?")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){return rs.next()?leer(rs):null;}}}
    private void bloquear(Connection c,long id)throws SQLException{try(PreparedStatement ps=c.prepareStatement("SELECT id_usuario FROM usuario WHERE id_usuario=? FOR UPDATE")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("El registro ya no existe.");}}}

    public void guardar(Usuario e, String contrasenaTemporal)throws SQLException{
        Paises.validarNombre(e.getNombres(),"nombres");Paises.validarNombre(e.getApellidos(),"apellidos");
        if(e.getTelefono()==null||e.getTelefono().isBlank())throw new IllegalArgumentException("El teléfono es obligatorio.");
        try(Connection c=obtenerConexion()){boolean correoBloqueado=false;c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);try{
            try(PreparedStatement pais=c.prepareStatement("SELECT codigo_pais FROM pais WHERE id_pais=?")){pais.setLong(1,e.getPaisId());try(ResultSet r=pais.executeQuery()){if(!r.next()||!Set.of("PE","CL","CO","PA").contains(r.getString(1)))throw new IllegalArgumentException("Seleccione Perú, Chile, Colombia o Panamá.");}}
            long id=e.getId(); boolean nuevo=id==0; if(!nuevo)bloquear(c,e.getId());
            if(nuevo && (contrasenaTemporal==null || contrasenaTemporal.length()<8))
                throw new IllegalArgumentException("La contraseña temporal debe tener al menos 8 caracteres.");
            if(nuevo){
                try(Statement lock=c.createStatement();ResultSet result=lock.executeQuery("SELECT GET_LOCK('oceanlink.usuario.correo',10)")){if(!result.next()||result.getInt(1)!=1)throw new IllegalArgumentException("Hay otro registro en proceso. Inténtalo nuevamente.");correoBloqueado=true;}
                e.setCorreo(CorreoCorporativo.generar(c,e.getNombres(),e.getApellidos()));
            }else{e.setCorreo(buscar(c,e.getId()).getCorreo());}
            if(!e.getCorreo().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))throw new IllegalArgumentException("Ingresa un correo válido.");
            if(!nuevo && e.getEstado()==0){try(PreparedStatement ps=c.prepareStatement("UPDATE usuario SET estado=0 WHERE id_usuario=?")){ps.setLong(1,id);ps.executeUpdate();}}
            if(e.getId()==0){

                try(PreparedStatement ps=c.prepareStatement("INSERT INTO usuario (codigo_usuario,nombre_completo,correo,telefono,id_rol,id_pais,estado,fecha_ingreso,hash_contrasena,fecha_primera_activacion) VALUES (?,?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){ps.setString(1,Codigos.temporal());ps.setString(2,e.getNombre());ps.setString(3,e.getCorreo());ps.setString(4,e.getTelefono());ps.setLong(5,e.getRolId());ps.setLong(6,e.getPaisId());ps.setInt(7,1);ps.setTimestamp(8,(e.getFechaIngreso()==null?null:Timestamp.valueOf(e.getFechaIngreso())));ps.setString(9,PasswordHash.sha256(contrasenaTemporal));ps.setTimestamp(10,Timestamp.valueOf(LocalDateTime.now()));ps.executeUpdate();e.setId(Codigos.insertado(ps));}
            }else{try(PreparedStatement ps=c.prepareStatement("UPDATE usuario SET fecha_primera_activacion=COALESCE(fecha_primera_activacion,IF(?=1,CURRENT_TIMESTAMP,NULL)) WHERE id_usuario=?")){ps.setInt(1,e.getEstado());ps.setLong(2,e.getId());ps.executeUpdate();}
                try(PreparedStatement ps=c.prepareStatement("UPDATE usuario SET codigo_usuario=?,nombre_completo=?,correo=?,telefono=?,id_rol=?,id_pais=?,estado=?,fecha_ingreso=? WHERE id_usuario=?")){ps.setString(1,e.getCodigo());ps.setString(2,e.getNombre());ps.setString(3,e.getCorreo());ps.setString(4,e.getTelefono());ps.setLong(5,e.getRolId());ps.setLong(6,e.getPaisId());ps.setInt(7,e.getEstado());ps.setTimestamp(8,(e.getFechaIngreso()==null?null:Timestamp.valueOf(e.getFechaIngreso())));ps.setLong(9,e.getId());ps.executeUpdate();}
            }
            if(nuevo)e.setCodigo(Codigos.asignar(c,"usuario","id_usuario","codigo_usuario",e.getId(),"USR"));
            try(PreparedStatement region=c.prepareStatement("UPDATE usuario SET telefono_region=? WHERE id_usuario=?")){region.setString(1,e.getTelefonoRegion());region.setLong(2,e.getId());region.executeUpdate();}
            c.commit();
        }catch(SQLException|RuntimeException ex){c.rollback();throw ex;}finally{if(correoBloqueado){try(Statement lock=c.createStatement()){lock.execute("SELECT RELEASE_LOCK('oceanlink.usuario.correo')");}}}}
    }
    public void guardar(Usuario e)throws SQLException{
        guardar(e, null);
    }

    public void eliminar(long id)throws SQLException{try(Connection c=obtenerConexion()){c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);try{bloquear(c,id);try(PreparedStatement ps=c.prepareStatement("UPDATE usuario SET estado=0 WHERE id_usuario=?")){ps.setLong(1,id);ps.executeUpdate();}c.commit();}catch(SQLException|RuntimeException e){c.rollback();throw e;}}}

    public Usuario autenticar(String identificador, String contrasena) throws SQLException {
        if (identificador == null || identificador.isBlank() || contrasena == null || contrasena.isEmpty()) return null;
        String sql = "SELECT u.id_usuario, u.codigo_usuario, u.nombre_completo, u.correo, u.id_rol, r.codigo_rol, r.nombre AS rol_nombre, u.estado, u.hash_contrasena " +
                     "FROM usuario u JOIN rol r ON r.id_rol = u.id_rol " +
                     "WHERE (LOWER(u.correo) = LOWER(?) OR LOWER(u.codigo_usuario) = LOWER(?)) AND u.estado = 1 LIMIT 1";
        try (Connection c = obtenerConexion(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, identificador.trim());
            ps.setString(2, identificador.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                if (!PasswordHash.coincide(contrasena, rs.getString(9))) return null;
                Usuario usuario = new Usuario();
                usuario.setId(rs.getLong(1));
                usuario.setCodigo(rs.getString(2));
                usuario.setNombre(rs.getString(3));
                usuario.setCorreo(rs.getString(4));
                usuario.setRolId(rs.getLong(5));
                usuario.setRolCodigo(rs.getString(6));
                usuario.setRolNombre(rs.getString(7));
                usuario.setEstado(rs.getInt(8));
                try (PreparedStatement update = c.prepareStatement("UPDATE usuario SET ultimo_acceso = CURRENT_TIMESTAMP WHERE id_usuario = ?")) {
                    update.setLong(1, usuario.getId());
                    update.executeUpdate();
                }
                return usuario;
            }
        }
    }

}
