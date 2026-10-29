package pe.edu.pucp.oceanlink;

import java.sql.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public class SolicitudDAO {
    private static final String CONSULTA="SELECT t.*,cl.razon_social cliente_nombre,o.nombre origen_nombre,d.nombre destino_nombre,es.nombre estado_nombre,(SELECT GROUP_CONCAT(ss.id_segmento ORDER BY ss.orden SEPARATOR ',') FROM solicitud_segmento ss WHERE ss.id_solicitud=t.id_solicitud) ruta FROM solicitud_capacidad t JOIN cliente cl ON cl.id_cliente=t.id_cliente JOIN estacion o ON o.id_estacion=t.id_estacion_origen JOIN estacion d ON d.id_estacion=t.id_estacion_destino JOIN estado_solicitud_capacidad es ON es.id_estado_solicitud=t.id_estado_solicitud";
    private static final String FILTRO=" WHERE (t.codigo_solicitud REGEXP ? OR cl.razon_social REGEXP ?) AND (?='' OR es.nombre=?)";
    private Solicitud leer(ResultSet rs)throws SQLException{
        Solicitud e=new Solicitud();
        e.setId(rs.getLong("id_solicitud"));
        e.setCodigo(rs.getString("codigo_solicitud"));
        e.setClienteId(rs.getLong("id_cliente"));
        e.setOrigenId(rs.getLong("id_estacion_origen"));
        e.setDestinoId(rs.getLong("id_estacion_destino"));
        e.setCapacidad(rs.getBigDecimal("capacidad_solicitada_tbps"));
        e.setEstado(rs.getString("estado_nombre"));
        e.setObservacion(rs.getString("observacion"));
        e.setCreadoPor(rs.getLong("creado_por"));
        e.setClienteNombre(rs.getString("cliente_nombre"));
        e.setOrigenNombre(rs.getString("origen_nombre"));
        e.setDestinoNombre(rs.getString("destino_nombre"));
        e.setRuta(rs.getString("ruta"));
        return e;
    }

    private String ordenar(String orden,String direccion){String columna=switch(Objects.toString(orden,"codigo")){case "codigo"->"CAST(SUBSTRING_INDEX(t.codigo_solicitud,'-',-1) AS UNSIGNED)";case "cliente"->"cl.razon_social";case "estado"->"es.nombre";default->"CAST(SUBSTRING_INDEX(t.codigo_solicitud,'-',-1) AS UNSIGNED)";};return columna+("desc".equals(direccion)?" DESC":" ASC")+", t.id_solicitud ASC";}
    private void filtros(PreparedStatement ps,String q,String estado)throws SQLException{ps.setString(1,Busqueda.codigo(q));ps.setString(2,Busqueda.texto(q));ps.setString(3,estado);ps.setString(4,estado);}
    public int contar(String q,String estado)throws SQLException{try(Connection c=Conexion.abrir();PreparedStatement ps=c.prepareStatement("SELECT COUNT(*) FROM solicitud_capacidad t JOIN cliente cl ON cl.id_cliente=t.id_cliente JOIN estacion o ON o.id_estacion=t.id_estacion_origen JOIN estacion d ON d.id_estacion=t.id_estacion_destino JOIN estado_solicitud_capacidad es ON es.id_estado_solicitud=t.id_estado_solicitud"+FILTRO)){filtros(ps,q,estado);try(ResultSet rs=ps.executeQuery()){rs.next();return rs.getInt(1);}}}
    public List<Solicitud> listar(String q,String estado,int pagina,int tamano)throws SQLException{return listar(q,estado,pagina,tamano,"codigo","asc");}
    public List<Solicitud> listar(String q,String estado,int pagina,int tamano,String orden,String direccion)throws SQLException{List<Solicitud> out=new ArrayList<>();try(Connection c=Conexion.abrir();PreparedStatement ps=c.prepareStatement(CONSULTA+FILTRO+" ORDER BY "+ordenar(orden,direccion)+" LIMIT ? OFFSET ?")){filtros(ps,q,estado);ps.setInt(5,tamano);ps.setLong(6,(long)(pagina-1)*tamano);try(ResultSet rs=ps.executeQuery()){while(rs.next())out.add(leer(rs));}}return out;}
    public Solicitud buscar(long id)throws SQLException{try(Connection c=Conexion.abrir()){return buscar(c,id);}}
    private Solicitud buscar(Connection c,long id)throws SQLException{try(PreparedStatement ps=c.prepareStatement(CONSULTA+" WHERE t.id_solicitud=?")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){return rs.next()?leer(rs):null;}}}
    private void bloquear(Connection c,long id)throws SQLException{try(PreparedStatement ps=c.prepareStatement("SELECT id_solicitud FROM solicitud_capacidad WHERE id_solicitud=? FOR UPDATE")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("El registro ya no existe.");}}}

    public void guardar(Solicitud e,List<Long> ruta)throws SQLException{
        try(Connection c=Conexion.abrir()){c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);try{
            Solicitud anterior=null;if(e.getId()>0){bloquear(c,e.getId());anterior=buscar(c,e.getId());}
            String actual=anterior==null?"REGISTRADA":anterior.getEstado();
            Map<String,String> siguientes=Map.of("REGISTRADA","EN_EVALUACION","EN_EVALUACION","APROBADA,RECHAZADA,PENDIENTE_CAPACIDAD","PENDIENTE_CAPACIDAD","EN_EVALUACION","APROBADA","PROVISIONADA","PROVISIONADA","ACTIVA");
            if(!actual.equals(e.getEstado())&&!Arrays.asList(siguientes.getOrDefault(actual,"").split(",")).contains(e.getEstado()))throw new IllegalArgumentException("La solicitud no admite ese cambio de estado.");
            if(anterior==null&&!"REGISTRADA".equals(e.getEstado()))throw new IllegalArgumentException("Las solicitudes se crean como Registrada.");
            if(Set.of("PROVISIONADA","ACTIVA","RECHAZADA").contains(actual)){
                List<Long> original=CapacidadDAO.ruta(c,"solicitud_segmento","id_solicitud",e.getId());
                if(e.getClienteId()!=anterior.getClienteId()||e.getOrigenId()!=anterior.getOrigenId()||e.getDestinoId()!=anterior.getDestinoId()||e.getCapacidad().compareTo(anterior.getCapacidad())!=0||!ruta.equals(original))throw new IllegalArgumentException("La solicitud ya fue provisionada o finalizada; sus datos de capacidad y ruta no pueden cambiar.");
            }
            if("ACTIVA".equals(actual)&&!e.getObservacion().equals(Objects.toString(anterior.getObservacion(),"")))throw new IllegalArgumentException("La solicitud activa es de solo consulta.");
            CapacidadDAO.bloquearEstaciones(c,e.getOrigenId(),e.getDestinoId());
            List<Long> bloqueos=new ArrayList<>(ruta);if(anterior!=null)bloqueos.addAll(CapacidadDAO.ruta(c,"solicitud_segmento","id_solicitud",e.getId()));CapacidadDAO.bloquearSegmentos(c,bloqueos);
            CapacidadDAO.validarRuta(c,e.getOrigenId(),e.getDestinoId(),ruta,e.getCapacidad(),e.getId(),0,Set.of("APROBADA","PROVISIONADA").contains(e.getEstado()));
            if(anterior==null||!Set.of("PROVISIONADA","ACTIVA","RECHAZADA").contains(actual)){
                try(PreparedStatement ps=c.prepareStatement("SELECT estado FROM cliente WHERE id_cliente=? FOR UPDATE")){ps.setLong(1,e.getClienteId());try(ResultSet rs=ps.executeQuery()){if(!rs.next()||rs.getInt(1)!=1)throw new IllegalArgumentException("Selecciona un cliente activo.");}}
            }
            boolean nuevo=e.getId()==0;
            if(nuevo){try(PreparedStatement ps=c.prepareStatement("INSERT INTO solicitud_capacidad(codigo_solicitud,id_cliente,id_estacion_origen,id_estacion_destino,id_estado_solicitud,capacidad_solicitada_tbps,observacion,creado_por) VALUES(?,?,?,?,(SELECT id_estado_solicitud FROM estado_solicitud_capacidad WHERE nombre='REGISTRADA'),?,?,?)",Statement.RETURN_GENERATED_KEYS)){ps.setString(1,Codigos.temporal());ps.setLong(2,e.getClienteId());ps.setLong(3,e.getOrigenId());ps.setLong(4,e.getDestinoId());ps.setBigDecimal(5,e.getCapacidad());ps.setString(6,e.getObservacion());ps.setLong(7,e.getCreadoPor());ps.executeUpdate();e.setId(Codigos.insertado(ps));}Codigos.asignar(c,"solicitud_capacidad","id_solicitud","codigo_solicitud",e.getId(),"REQ");}
            else if(!"ACTIVA".equals(e.getEstado())||"ACTIVA".equals(actual)){
                try(PreparedStatement ps=c.prepareStatement("UPDATE solicitud_capacidad SET id_cliente=?,id_estacion_origen=?,id_estacion_destino=?,capacidad_solicitada_tbps=?,observacion=?,id_estado_solicitud=(SELECT id_estado_solicitud FROM estado_solicitud_capacidad WHERE nombre=?),fecha_provision=IF(?='PROVISIONADA',COALESCE(fecha_provision,CURRENT_TIMESTAMP),fecha_provision) WHERE id_solicitud=?")){ps.setLong(1,e.getClienteId());ps.setLong(2,e.getOrigenId());ps.setLong(3,e.getDestinoId());ps.setBigDecimal(4,e.getCapacidad());ps.setString(5,e.getObservacion());ps.setString(6,e.getEstado());ps.setString(7,e.getEstado());ps.setLong(8,e.getId());ps.executeUpdate();}
            }
            if(!Set.of("PROVISIONADA","ACTIVA","RECHAZADA").contains(actual))CapacidadDAO.guardarRuta(c,"solicitud_segmento","id_solicitud",e.getId(),ruta,"PROVISIONADA".equals(e.getEstado()));
            if("ACTIVA".equals(e.getEstado())&&!"ACTIVA".equals(actual))CapacidadDAO.activar(c,e.getId());
            if("PROVISIONADA".equals(e.getEstado()))CapacidadDAO.registrarHistorial(c,ruta);c.commit();
        }catch(SQLException|RuntimeException ex){c.rollback();throw ex;}}
    }
    public void eliminar(long id)throws SQLException{try(Connection c=Conexion.abrir()){c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);try{bloquear(c,id);Solicitud e=buscar(c,id);if(!Set.of("REGISTRADA","RECHAZADA","PENDIENTE_CAPACIDAD").contains(e.getEstado()))throw new IllegalArgumentException("Solo se eliminan solicitudes registradas, rechazadas o pendientes por capacidad sin servicio asociado.");try(PreparedStatement ps=c.prepareStatement("DELETE FROM solicitud_segmento WHERE id_solicitud=?")){ps.setLong(1,id);ps.executeUpdate();}try(PreparedStatement ps=c.prepareStatement("DELETE FROM solicitud_capacidad WHERE id_solicitud=?")){ps.setLong(1,id);ps.executeUpdate();}c.commit();}catch(SQLException|RuntimeException ex){c.rollback();throw ex;}}}

}
