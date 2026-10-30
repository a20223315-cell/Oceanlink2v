package pe.edu.pucp.oceanlink;

import java.sql.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public class SegmentoDAO extends DaoBase {
    private static final String CONSULTA="SELECT t.*,o.nombre origen_nombre,d.nombre destino_nombre FROM segmento t JOIN estacion o ON o.id_estacion=t.id_estacion_origen JOIN estacion d ON d.id_estacion=t.id_estacion_destino";
    private static final String FILTRO=" WHERE (t.codigo_segmento REGEXP ? OR CONCAT(o.nombre,' ',d.nombre) REGEXP ?) AND (?='' OR CAST(t.estado AS CHAR)=? OR t.condicion_operativa=?)";
    private Segmento leer(ResultSet rs)throws SQLException{
        Segmento e=new Segmento();
        e.setId(rs.getLong("id_segmento"));
        e.setCodigo(rs.getString("codigo_segmento"));
        e.setOrigenId(rs.getLong("id_estacion_origen"));
        e.setDestinoId(rs.getLong("id_estacion_destino"));
        e.setCapacidad(rs.getBigDecimal("capacidad_total_tbps"));
        e.setCondicion(rs.getString("condicion_operativa"));
        e.setEstado(rs.getInt("estado"));
        e.setOrigenNombre(rs.getString("origen_nombre"));
        e.setDestinoNombre(rs.getString("destino_nombre"));
        return e;
    }

    private String ordenar(String orden,String direccion){String columna=switch(Objects.toString(orden,"codigo")){case "codigo"->"CAST(SUBSTRING_INDEX(t.codigo_segmento,'-',-1) AS UNSIGNED)";case "origen"->"o.nombre";case "destino"->"d.nombre";case "capacidad"->"t.capacidad_total_tbps";case "condicion"->"t.condicion_operativa";default->"CAST(SUBSTRING_INDEX(t.codigo_segmento,'-',-1) AS UNSIGNED)";};return columna+("desc".equals(direccion)?" DESC":" ASC")+", t.id_segmento ASC";}
    private void filtros(PreparedStatement ps,String q,String estado)throws SQLException{ps.setString(1,Busqueda.codigo(q));ps.setString(2,Busqueda.texto(q));ps.setString(3,estado);ps.setString(4,estado);ps.setString(5,estado);}
    public int contar(String q,String estado)throws SQLException{try(Connection c=obtenerConexion();PreparedStatement ps=c.prepareStatement("SELECT COUNT(*) FROM segmento t JOIN estacion o ON o.id_estacion=t.id_estacion_origen JOIN estacion d ON d.id_estacion=t.id_estacion_destino"+FILTRO)){filtros(ps,q,estado);try(ResultSet rs=ps.executeQuery()){rs.next();return rs.getInt(1);}}}
    public List<Segmento> listar(String q,String estado,int pagina,int tamano)throws SQLException{return listar(q,estado,pagina,tamano,"codigo","asc");}
    public List<Segmento> listar(String q,String estado,int pagina,int tamano,String orden,String direccion)throws SQLException{List<Segmento> out=new ArrayList<>();try(Connection c=obtenerConexion();PreparedStatement ps=c.prepareStatement(CONSULTA+FILTRO+" ORDER BY "+ordenar(orden,direccion)+" LIMIT ? OFFSET ?")){filtros(ps,q,estado);ps.setInt(6,tamano);ps.setLong(7,(long)(pagina-1)*tamano);try(ResultSet rs=ps.executeQuery()){while(rs.next())out.add(leer(rs));}}return out;}
    public Segmento buscar(long id)throws SQLException{try(Connection c=obtenerConexion()){return buscar(c,id);}}
    private Segmento buscar(Connection c,long id)throws SQLException{try(PreparedStatement ps=c.prepareStatement(CONSULTA+" WHERE t.id_segmento=?")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){return rs.next()?leer(rs):null;}}}
    private void bloquear(Connection c,long id)throws SQLException{try(PreparedStatement ps=c.prepareStatement("SELECT id_segmento FROM segmento WHERE id_segmento=? FOR UPDATE")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("El registro ya no existe.");}}}

    public void guardar(Segmento e)throws SQLException{
        try(Connection c=obtenerConexion()){c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);try{
            long id=e.getId(); boolean nuevo=id==0; if(!nuevo)bloquear(c,e.getId());
            if(e.getOrigenId()==e.getDestinoId())throw new IllegalArgumentException("Origen y destino deben ser distintos.");
            CapacidadDAO.bloquearEstaciones(c,e.getOrigenId(),e.getDestinoId());
            if(e.getId()>0){Segmento anterior=buscar(c,e.getId());if(anterior.getOrigenId()!=e.getOrigenId()||anterior.getDestinoId()!=e.getDestinoId()){try(PreparedStatement ps=c.prepareStatement("SELECT 1 FROM solicitud_segmento WHERE id_segmento=? UNION ALL SELECT 1 FROM servicio_segmento WHERE id_segmento=? LIMIT 1")){ps.setLong(1,e.getId());ps.setLong(2,e.getId());try(ResultSet rs=ps.executeQuery()){if(rs.next())throw new IllegalArgumentException("No se pueden cambiar extremos de un segmento con rutas registradas.");}}}BigDecimal ocupada=CapacidadDAO.ocupada(c,e.getId(),0,0);if(e.getCapacidad().compareTo(ocupada)<0)throw new IllegalArgumentException("La capacidad total no puede ser menor que la utilizada más la reservada.");if(e.getEstado()==0&&ocupada.signum()>0)throw new IllegalArgumentException("El segmento tiene capacidad asignada o reservada.");}
            if(!nuevo && e.getEstado()==0){if(CapacidadDAO.ocupada(c,id,0,0).signum()>0)throw new IllegalArgumentException("El segmento tiene servicios activos o capacidad reservada.");try(PreparedStatement ps=c.prepareStatement("UPDATE segmento SET estado=0 WHERE id_segmento=?")){ps.setLong(1,id);ps.executeUpdate();}}
            if(e.getId()==0){

                try(PreparedStatement ps=c.prepareStatement("INSERT INTO segmento (codigo_segmento,id_estacion_origen,id_estacion_destino,capacidad_total_tbps,condicion_operativa,estado) VALUES (?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){ps.setString(1,Codigos.temporal());ps.setLong(2,e.getOrigenId());ps.setLong(3,e.getDestinoId());ps.setBigDecimal(4,e.getCapacidad());ps.setString(5,e.getCondicion());ps.setInt(6,e.getEstado());ps.executeUpdate();e.setId(Codigos.insertado(ps));}
            }else{
                try(PreparedStatement ps=c.prepareStatement("UPDATE segmento SET codigo_segmento=?,id_estacion_origen=?,id_estacion_destino=?,capacidad_total_tbps=?,condicion_operativa=?,estado=? WHERE id_segmento=?")){ps.setString(1,e.getCodigo());ps.setLong(2,e.getOrigenId());ps.setLong(3,e.getDestinoId());ps.setBigDecimal(4,e.getCapacidad());ps.setString(5,e.getCondicion());ps.setInt(6,e.getEstado());ps.setLong(7,e.getId());ps.executeUpdate();}
            }
            if(nuevo)e.setCodigo(Codigos.asignar(c,"segmento","id_segmento","codigo_segmento",e.getId(),"SEG"));
            CapacidadDAO.registrarHistorial(c,List.of(e.getId()));c.commit();
        }catch(SQLException|RuntimeException ex){c.rollback();throw ex;}}
    }
    public void eliminar(long id)throws SQLException{try(Connection c=obtenerConexion()){c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);try{bloquear(c,id);if(CapacidadDAO.ocupada(c,id,0,0).signum()>0)throw new IllegalArgumentException("El segmento tiene servicios activos o capacidad reservada.");try(PreparedStatement ps=c.prepareStatement("UPDATE segmento SET estado=0 WHERE id_segmento=?")){ps.setLong(1,id);ps.executeUpdate();}c.commit();}catch(SQLException|RuntimeException e){c.rollback();throw e;}}}

}
