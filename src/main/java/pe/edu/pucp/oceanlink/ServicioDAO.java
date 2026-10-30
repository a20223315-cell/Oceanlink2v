package pe.edu.pucp.oceanlink;

import java.sql.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public class ServicioDAO extends DaoBase {
    private static final String CONSULTA="SELECT t.*,(SELECT nombre FROM estacion WHERE id_estacion=r.id_estacion_origen) origen_nombre,(SELECT nombre FROM estacion WHERE id_estacion=r.id_estacion_destino) destino_nombre,cl.razon_social cliente_nombre,r.codigo_solicitud solicitud_codigo,r.id_estacion_origen,r.id_estacion_destino,(SELECT GROUP_CONCAT(ss.id_segmento ORDER BY ss.orden SEPARATOR ',') FROM servicio_segmento ss WHERE ss.id_servicio=t.id_servicio) ruta FROM servicio t JOIN solicitud_capacidad r ON r.id_solicitud=t.id_solicitud JOIN cliente cl ON cl.id_cliente=r.id_cliente";
    private static final String FILTRO=" WHERE (t.codigo_servicio REGEXP ? OR cl.razon_social REGEXP ?) AND (?='' OR t.estado=?)";
    private Servicio leer(ResultSet rs)throws SQLException{
        Servicio e=new Servicio();
        e.setId(rs.getLong("id_servicio"));
        e.setCodigo(rs.getString("codigo_servicio"));
        e.setSolicitudId(rs.getLong("id_solicitud"));
        e.setCapacidad(rs.getBigDecimal("capacidad_contratada_tbps"));
        e.setEstado(rs.getString("estado"));
        e.setClienteNombre(rs.getString("cliente_nombre"));
        e.setSolicitudCodigo(rs.getString("solicitud_codigo"));
        e.setOrigenId(rs.getLong("id_estacion_origen"));
        e.setDestinoId(rs.getLong("id_estacion_destino"));
        e.setRuta(rs.getString("ruta"));
        e.setOrigenNombre(rs.getString("origen_nombre"));e.setDestinoNombre(rs.getString("destino_nombre"));e.setFechaActivacion(rs.getTimestamp("fecha_activacion")==null?null:rs.getTimestamp("fecha_activacion").toLocalDateTime());e.setFechaCancelacion(rs.getTimestamp("fecha_cancelacion")==null?null:rs.getTimestamp("fecha_cancelacion").toLocalDateTime());
        return e;
    }

    private String ordenar(String orden,String direccion){String columna=switch(Objects.toString(orden,"codigo")){case "codigo"->"CAST(SUBSTRING_INDEX(t.codigo_servicio,'-',-1) AS UNSIGNED)";case "cliente"->"cl.razon_social";case "capacidad"->"t.capacidad_contratada_tbps";case "estado"->"t.estado";default->"CAST(SUBSTRING_INDEX(t.codigo_servicio,'-',-1) AS UNSIGNED)";};return columna+("desc".equals(direccion)?" DESC":" ASC")+", t.id_servicio ASC";}
    private void filtros(PreparedStatement ps,String q,String estado)throws SQLException{ps.setString(1,Busqueda.codigo(q));ps.setString(2,Busqueda.texto(q));ps.setString(3,estado);ps.setString(4,estado);}
    public int contar(String q,String estado)throws SQLException{try(Connection c=obtenerConexion();PreparedStatement ps=c.prepareStatement("SELECT COUNT(*) FROM servicio t JOIN solicitud_capacidad r ON r.id_solicitud=t.id_solicitud JOIN cliente cl ON cl.id_cliente=r.id_cliente"+FILTRO)){filtros(ps,q,estado);try(ResultSet rs=ps.executeQuery()){rs.next();return rs.getInt(1);}}}
    public List<Servicio> listar(String q,String estado,int pagina,int tamano)throws SQLException{return listar(q,estado,pagina,tamano,"codigo","asc");}
    public List<Servicio> listar(String q,String estado,int pagina,int tamano,String orden,String direccion)throws SQLException{List<Servicio> out=new ArrayList<>();try(Connection c=obtenerConexion();PreparedStatement ps=c.prepareStatement(CONSULTA+FILTRO+" ORDER BY "+ordenar(orden,direccion)+" LIMIT ? OFFSET ?")){filtros(ps,q,estado);ps.setInt(5,tamano);ps.setLong(6,(long)(pagina-1)*tamano);try(ResultSet rs=ps.executeQuery()){while(rs.next())out.add(leer(rs));}}return out;}
    public Servicio buscar(long id)throws SQLException{try(Connection c=obtenerConexion()){return buscar(c,id);}}
    private Servicio buscar(Connection c,long id)throws SQLException{try(PreparedStatement ps=c.prepareStatement(CONSULTA+" WHERE t.id_servicio=?")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){return rs.next()?leer(rs):null;}}}
    private void bloquear(Connection c,long id)throws SQLException{try(PreparedStatement ps=c.prepareStatement("SELECT id_servicio FROM servicio WHERE id_servicio=? FOR UPDATE")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("El registro ya no existe.");}}}

    public void guardar(Servicio e,List<Long> ruta)throws SQLException{try(Connection c=obtenerConexion()){c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);try{
        if(e.getId()==0){e.setId(CapacidadDAO.activar(c,e.getSolicitudId()));}
        else{bloquear(c,e.getId());Servicio anterior=buscar(c,e.getId());if(!"ACTIVO".equals(anterior.getEstado()))throw new IllegalArgumentException("Solo se modifica la ruta de un servicio activo.");
            List<Long> bloqueos=new ArrayList<>(ruta);bloqueos.addAll(CapacidadDAO.ruta(c,"servicio_segmento","id_servicio",e.getId()));CapacidadDAO.bloquearSegmentos(c,bloqueos);
            CapacidadDAO.validarRuta(c,anterior.getOrigenId(),anterior.getDestinoId(),ruta,anterior.getCapacidad(),0,e.getId(),true);
            CapacidadDAO.guardarRuta(c,"servicio_segmento","id_servicio",e.getId(),ruta,false);CapacidadDAO.registrarHistorial(c,bloqueos);
        }c.commit();
    }catch(SQLException|RuntimeException ex){c.rollback();throw ex;}}}
    public void eliminar(long id)throws SQLException{try(Connection c=obtenerConexion()){c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);try{bloquear(c,id);Servicio e=buscar(c,id);if(!"ACTIVO".equals(e.getEstado()))throw new IllegalArgumentException("Solo se cancelan servicios activos. Los pendientes se gestionan desde la solicitud.");CapacidadDAO.bloquearSegmentos(c,CapacidadDAO.ruta(c,"servicio_segmento","id_servicio",id));try(PreparedStatement ps=c.prepareStatement("UPDATE servicio SET estado='CANCELADO',fecha_cancelacion=CURRENT_TIMESTAMP WHERE id_servicio=?")){ps.setLong(1,id);ps.executeUpdate();}CapacidadDAO.registrarHistorial(c,CapacidadDAO.ruta(c,"servicio_segmento","id_servicio",id));c.commit();}catch(SQLException|RuntimeException ex){c.rollback();throw ex;}}}

    public List<String[]> detalleRuta(long id)throws SQLException{return DetalleDAO.filas("SELECT ss.orden,seg.codigo_segmento,a.nombre,b.nombre,s.capacidad_contratada_tbps,seg.condicion_operativa FROM servicio_segmento ss JOIN servicio s ON s.id_servicio=ss.id_servicio JOIN segmento seg ON seg.id_segmento=ss.id_segmento JOIN estacion a ON a.id_estacion=seg.id_estacion_origen JOIN estacion b ON b.id_estacion=seg.id_estacion_destino WHERE ss.id_servicio=? ORDER BY ss.orden",id);}
}
