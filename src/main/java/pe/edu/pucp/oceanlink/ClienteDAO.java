package pe.edu.pucp.oceanlink;

import java.sql.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public class ClienteDAO {
    private static final String CONSULTA="SELECT t.*,(SELECT COUNT(*) FROM servicio v JOIN solicitud_capacidad sol ON sol.id_solicitud=v.id_solicitud WHERE sol.id_cliente=t.id_cliente AND v.estado='ACTIVO') servicios FROM cliente t";
    private static final String FILTRO=" WHERE (t.codigo_cliente REGEXP ? OR t.razon_social REGEXP ?) AND (?='' OR CAST(t.estado AS CHAR)=?)";
    private Cliente leer(ResultSet rs)throws SQLException{
        Cliente e=new Cliente();e.setServicios(rs.getInt("servicios"));
        e.setId(rs.getLong("id_cliente"));
        e.setCodigo(rs.getString("codigo_cliente"));
        e.setNombre(rs.getString("razon_social"));
        e.setTipoDocumento(rs.getString("tipo_identificacion"));
        e.setDocumento(rs.getString("identificacion_fiscal"));
        e.setContacto(rs.getString("nombre_contacto"));
        e.setCorreo(rs.getString("correo_contacto"));
        e.setTelefono(rs.getString("telefono_contacto"));
        e.setEstado(rs.getInt("estado"));
        return e;
    }

    private String ordenar(String orden,String direccion){String columna=switch(Objects.toString(orden,"codigo")){case "codigo"->"CAST(SUBSTRING_INDEX(t.codigo_cliente,'-',-1) AS UNSIGNED)";case "nombre"->"t.razon_social";case "correo"->"t.correo";case "estado"->"t.estado";default->"CAST(SUBSTRING_INDEX(t.codigo_cliente,'-',-1) AS UNSIGNED)";};return columna+("desc".equals(direccion)?" DESC":" ASC")+", t.id_cliente ASC";}
    private void filtros(PreparedStatement ps,String q,String estado)throws SQLException{ps.setString(1,Busqueda.codigo(q));ps.setString(2,Busqueda.texto(q));ps.setString(3,estado);ps.setString(4,estado);}
    public int contar(String q,String estado)throws SQLException{try(Connection c=Conexion.abrir();PreparedStatement ps=c.prepareStatement("SELECT COUNT(*) FROM cliente t"+FILTRO)){filtros(ps,q,estado);try(ResultSet rs=ps.executeQuery()){rs.next();return rs.getInt(1);}}}
    public List<Cliente> listar(String q,String estado,int pagina,int tamano)throws SQLException{return listar(q,estado,pagina,tamano,"codigo","asc");}
    public List<Cliente> listar(String q,String estado,int pagina,int tamano,String orden,String direccion)throws SQLException{List<Cliente> out=new ArrayList<>();try(Connection c=Conexion.abrir();PreparedStatement ps=c.prepareStatement(CONSULTA+FILTRO+" ORDER BY "+ordenar(orden,direccion)+" LIMIT ? OFFSET ?")){filtros(ps,q,estado);ps.setInt(5,tamano);ps.setLong(6,(long)(pagina-1)*tamano);try(ResultSet rs=ps.executeQuery()){while(rs.next())out.add(leer(rs));}}return out;}
    public Cliente buscar(long id)throws SQLException{try(Connection c=Conexion.abrir()){return buscar(c,id);}}
    private Cliente buscar(Connection c,long id)throws SQLException{try(PreparedStatement ps=c.prepareStatement(CONSULTA+" WHERE t.id_cliente=?")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){return rs.next()?leer(rs):null;}}}
    private void bloquear(Connection c,long id)throws SQLException{try(PreparedStatement ps=c.prepareStatement("SELECT id_cliente FROM cliente WHERE id_cliente=? FOR UPDATE")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("El registro ya no existe.");}}}

    public void guardar(Cliente e)throws SQLException{
        if(e.getTelefono()==null||e.getTelefono().isBlank())throw new IllegalArgumentException("El teléfono es obligatorio.");
        try(Connection c=Conexion.abrir()){c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);try{
            long id=e.getId(); boolean nuevo=id==0; if(!nuevo)bloquear(c,e.getId());
            String telefono=java.util.Objects.toString(e.getTelefono(),"").trim();
            if(!telefono.isEmpty()){
                String digitos=telefono.replaceAll("[^0-9]","");
                if(!telefono.matches("[+]?[0-9 ()-]+")||digitos.length()<7||digitos.length()>15)throw new IllegalArgumentException("Ingresa un teléfono de contacto válido, de 7 a 15 dígitos, con indicativo si corresponde.");
            }
            if(e.getNombre().isBlank()||e.getContacto().isBlank()||e.getDocumento().isBlank())throw new IllegalArgumentException("Completa la razón social, el documento y el contacto.");

            if(!e.getCorreo().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))throw new IllegalArgumentException("Ingresa un correo de contacto válido.");
            if(!nuevo && e.getEstado()==0){try(PreparedStatement ps=c.prepareStatement("SELECT 1 FROM solicitud_capacidad r JOIN estado_solicitud_capacidad es ON es.id_estado_solicitud=r.id_estado_solicitud WHERE r.id_cliente=? AND es.nombre NOT IN ('RECHAZADA','ACTIVA') UNION ALL SELECT 1 FROM servicio s JOIN solicitud_capacidad r ON r.id_solicitud=s.id_solicitud WHERE r.id_cliente=? AND s.estado='ACTIVO' LIMIT 1")){ps.setLong(1,id);ps.setLong(2,id);try(ResultSet rs=ps.executeQuery()){if(rs.next())throw new IllegalArgumentException("El cliente tiene solicitudes pendientes o servicios activos.");}}try(PreparedStatement ps=c.prepareStatement("UPDATE cliente SET estado=0 WHERE id_cliente=?")){ps.setLong(1,id);ps.executeUpdate();}}
            if(e.getId()==0){

                try(PreparedStatement ps=c.prepareStatement("INSERT INTO cliente (codigo_cliente,razon_social,tipo_identificacion,identificacion_fiscal,nombre_contacto,correo_contacto,telefono_contacto,estado) VALUES (?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){ps.setString(1,Codigos.temporal());ps.setString(2,e.getNombre());ps.setString(3,e.getTipoDocumento());ps.setString(4,e.getDocumento());ps.setString(5,e.getContacto());ps.setString(6,e.getCorreo());ps.setString(7,e.getTelefono());ps.setInt(8,e.getEstado());ps.executeUpdate();e.setId(Codigos.insertado(ps));}
            }else{
                try(PreparedStatement ps=c.prepareStatement("UPDATE cliente SET codigo_cliente=?,razon_social=?,tipo_identificacion=?,identificacion_fiscal=?,nombre_contacto=?,correo_contacto=?,telefono_contacto=?,estado=? WHERE id_cliente=?")){ps.setString(1,e.getCodigo());ps.setString(2,e.getNombre());ps.setString(3,e.getTipoDocumento());ps.setString(4,e.getDocumento());ps.setString(5,e.getContacto());ps.setString(6,e.getCorreo());ps.setString(7,e.getTelefono());ps.setInt(8,e.getEstado());ps.setLong(9,e.getId());ps.executeUpdate();}
            }
            if(nuevo)e.setCodigo(Codigos.asignar(c,"cliente","id_cliente","codigo_cliente",e.getId(),"CLI"));
            c.commit();
        }catch(SQLException|RuntimeException ex){c.rollback();throw ex;}}
    }
    public void eliminar(long id)throws SQLException{try(Connection c=Conexion.abrir()){c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);try{bloquear(c,id);try(PreparedStatement ps=c.prepareStatement("SELECT 1 FROM solicitud_capacidad r JOIN estado_solicitud_capacidad es ON es.id_estado_solicitud=r.id_estado_solicitud WHERE r.id_cliente=? AND es.nombre NOT IN ('RECHAZADA','ACTIVA') UNION ALL SELECT 1 FROM servicio s JOIN solicitud_capacidad r ON r.id_solicitud=s.id_solicitud WHERE r.id_cliente=? AND s.estado='ACTIVO' LIMIT 1")){ps.setLong(1,id);ps.setLong(2,id);try(ResultSet rs=ps.executeQuery()){if(rs.next())throw new IllegalArgumentException("El cliente tiene solicitudes pendientes o servicios activos.");}}try(PreparedStatement ps=c.prepareStatement("UPDATE cliente SET estado=0 WHERE id_cliente=?")){ps.setLong(1,id);ps.executeUpdate();}c.commit();}catch(SQLException|RuntimeException e){c.rollback();throw e;}}}

}
