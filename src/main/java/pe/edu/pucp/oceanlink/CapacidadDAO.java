package pe.edu.pucp.oceanlink;

import java.sql.*;
import java.util.*;
import java.math.BigDecimal;
public final class CapacidadDAO extends DaoBase {
    private CapacidadDAO(){}
    public static void bloquearEstaciones(Connection c,long origen,long destino)throws SQLException{
        for(long id:new TreeSet<>(List.of(origen,destino))){try(PreparedStatement ps=c.prepareStatement("SELECT estado FROM estacion WHERE id_estacion=? FOR UPDATE")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){if(!rs.next()||rs.getInt(1)!=1)throw new IllegalArgumentException("Selecciona estaciones activas.");}}}
    }
    public static void bloquearSegmentos(Connection c,List<Long> ids)throws SQLException{
        for(long id:new TreeSet<>(ids)){try(PreparedStatement ps=c.prepareStatement("SELECT id_segmento FROM segmento WHERE id_segmento=? FOR UPDATE")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("Uno de los segmentos ya no existe.");}}}
    }
    public static BigDecimal ocupada(Connection c,long segmento,long excluirSolicitud,long excluirServicio)throws SQLException{
        String sql="SELECT COALESCE((SELECT SUM(s.capacidad_contratada_tbps) FROM servicio s JOIN servicio_segmento ss ON ss.id_servicio=s.id_servicio WHERE ss.id_segmento=? AND s.estado='ACTIVO' AND s.id_servicio<>?),0)+COALESCE((SELECT SUM(r.capacidad_solicitada_tbps) FROM solicitud_capacidad r JOIN solicitud_segmento ss ON ss.id_solicitud=r.id_solicitud JOIN estado_solicitud_capacidad es ON es.id_estado_solicitud=r.id_estado_solicitud WHERE ss.id_segmento=? AND es.nombre='PROVISIONADA' AND r.id_solicitud<>?),0)";
        try(PreparedStatement ps=c.prepareStatement(sql)){ps.setLong(1,segmento);ps.setLong(2,excluirServicio);ps.setLong(3,segmento);ps.setLong(4,excluirSolicitud);try(ResultSet rs=ps.executeQuery()){rs.next();return rs.getBigDecimal(1);}}
    }
    public static List<Long> ruta(Connection c,String tabla,String propietario,long id)throws SQLException{
        List<Long> out=new ArrayList<>();try(PreparedStatement ps=c.prepareStatement("SELECT id_segmento FROM "+tabla+" WHERE "+propietario+"=? ORDER BY orden")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){while(rs.next())out.add(rs.getLong(1));}}return out;
    }
    public static void validarRuta(Connection c,long origen,long destino,List<Long> ruta,BigDecimal requerida,long excluirSolicitud,long excluirServicio,boolean comprobarCapacidad)throws SQLException{
        if(requerida==null||requerida.signum()<=0)throw new IllegalArgumentException("La capacidad solicitada para la reserva debe ser mayor que 0.");
        if(origen==destino||ruta.isEmpty())throw new IllegalArgumentException("Selecciona dos estaciones distintas y una ruta.");
        long actual=origen;Set<Long> visitadas=new HashSet<>();visitadas.add(actual);
        for(long id:ruta){try(PreparedStatement ps=c.prepareStatement("SELECT id_estacion_origen,id_estacion_destino,capacidad_total_tbps,estado,condicion_operativa FROM segmento WHERE id_segmento=?")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){
            if(!rs.next()||rs.getInt("estado")!=1)throw new IllegalArgumentException("La ruta contiene un segmento inactivo.");
            long a=rs.getLong(1),b=rs.getLong(2);long siguiente=a==actual?b:b==actual?a:0;
            if(siguiente==0||!visitadas.add(siguiente))throw new IllegalArgumentException("La ruta no es continua o repite estaciones.");
            if(comprobarCapacidad){if("FUERA_SERVICIO".equals(rs.getString(5)))throw new IllegalArgumentException("La ruta contiene un segmento fuera de servicio.");
                BigDecimal disponible=rs.getBigDecimal(3).subtract(ocupada(c,id,excluirSolicitud,excluirServicio));if(disponible.compareTo(requerida)<0)throw new IllegalArgumentException("No se puede asignar o reservar "+requerida.stripTrailingZeros().toPlainString()+" Tbps: el segmento "+id+" solo dispone de "+disponible.stripTrailingZeros().toPlainString()+" Tbps.");}
            actual=siguiente;
        }}}
        if(actual!=destino)throw new IllegalArgumentException("La ruta no llega a la estación de destino.");
    }
    public static void guardarRuta(Connection c,String tabla,String propietario,long id,List<Long> ruta,boolean reservar)throws SQLException{
        try(PreparedStatement ps=c.prepareStatement("DELETE FROM "+tabla+" WHERE "+propietario+"=?")){ps.setLong(1,id);ps.executeUpdate();}
        String sql="INSERT INTO "+tabla+" ("+propietario+",id_segmento,orden"+(tabla.equals("solicitud_segmento")?",fecha_reserva":"")+") VALUES (?,?,?"+(tabla.equals("solicitud_segmento")?",?":"")+")";
        try(PreparedStatement ps=c.prepareStatement(sql)){int orden=1;for(long seg:ruta){ps.setLong(1,id);ps.setLong(2,seg);ps.setInt(3,orden++);if(tabla.equals("solicitud_segmento"))ps.setTimestamp(4,reservar?new Timestamp(System.currentTimeMillis()):null);ps.addBatch();}ps.executeBatch();}
    }
    public static long activar(Connection c,long solicitud)throws SQLException{
        long origen,destino;BigDecimal cantidad;
        try(PreparedStatement ps=c.prepareStatement("SELECT r.id_estacion_origen,r.id_estacion_destino,r.capacidad_solicitada_tbps,es.nombre FROM solicitud_capacidad r JOIN estado_solicitud_capacidad es ON es.id_estado_solicitud=r.id_estado_solicitud WHERE r.id_solicitud=? FOR UPDATE")){ps.setLong(1,solicitud);try(ResultSet rs=ps.executeQuery()){if(!rs.next()||!"PROVISIONADA".equals(rs.getString(4)))throw new IllegalArgumentException("Solo se puede activar una solicitud provisionada con reserva.");origen=rs.getLong(1);destino=rs.getLong(2);cantidad=rs.getBigDecimal(3);}}
        List<Long> ruta=ruta(c,"solicitud_segmento","id_solicitud",solicitud);bloquearSegmentos(c,ruta);
        try(PreparedStatement ps=c.prepareStatement("SELECT COUNT(*) FROM solicitud_segmento WHERE id_solicitud=? AND fecha_reserva IS NULL")){ps.setLong(1,solicitud);try(ResultSet rs=ps.executeQuery()){rs.next();if(rs.getInt(1)>0)throw new IllegalArgumentException("La solicitud no tiene una reserva completa.");}}
        validarRuta(c,origen,destino,ruta,cantidad,solicitud,0,true);
        long servicio=0;
        try(PreparedStatement ps=c.prepareStatement("SELECT id_servicio,estado FROM servicio WHERE id_solicitud=? FOR UPDATE")){ps.setLong(1,solicitud);try(ResultSet rs=ps.executeQuery()){if(rs.next()){if(!"PENDIENTE".equals(rs.getString(2)))throw new IllegalArgumentException("La solicitud ya tiene un servicio activo o cancelado.");servicio=rs.getLong(1);}}}
        if(servicio==0){try(PreparedStatement ps=c.prepareStatement("INSERT INTO servicio(codigo_servicio,id_solicitud,capacidad_contratada_tbps,estado,fecha_activacion) VALUES(?,?,?,'ACTIVO',CURRENT_TIMESTAMP)",Statement.RETURN_GENERATED_KEYS)){ps.setString(1,Codigos.temporal());ps.setLong(2,solicitud);ps.setBigDecimal(3,cantidad);ps.executeUpdate();servicio=Codigos.insertado(ps);}Codigos.asignar(c,"servicio","id_servicio","codigo_servicio",servicio,"SRV");}
        else{try(PreparedStatement ps=c.prepareStatement("UPDATE servicio SET estado='ACTIVO',capacidad_contratada_tbps=?,fecha_activacion=CURRENT_TIMESTAMP WHERE id_servicio=?")){ps.setBigDecimal(1,cantidad);ps.setLong(2,servicio);ps.executeUpdate();}}guardarRuta(c,"servicio_segmento","id_servicio",servicio,ruta,false);
        try(PreparedStatement ps=c.prepareStatement("UPDATE solicitud_capacidad SET id_estado_solicitud=(SELECT id_estado_solicitud FROM estado_solicitud_capacidad WHERE nombre='ACTIVA') WHERE id_solicitud=?")){ps.setLong(1,solicitud);ps.executeUpdate();}registrarHistorial(c,ruta);return servicio;
    }
    public static void registrarHistorial(Connection c,List<Long> segmentos)throws SQLException{
        for(long id:new TreeSet<>(segmentos)){
            BigDecimal total,usada;
            try(PreparedStatement ps=c.prepareStatement("SELECT capacidad_total_tbps FROM segmento WHERE id_segmento=?")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){if(!rs.next())continue;total=rs.getBigDecimal(1);}}
            try(PreparedStatement ps=c.prepareStatement("SELECT COALESCE(SUM(s.capacidad_contratada_tbps),0) FROM servicio s JOIN servicio_segmento ss ON ss.id_servicio=s.id_servicio WHERE ss.id_segmento=? AND s.estado='ACTIVO'")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){rs.next();usada=rs.getBigDecimal(1);}}
            BigDecimal reservada=ocupada(c,id,0,0).subtract(usada);
            try(PreparedStatement ps=c.prepareStatement("INSERT INTO historial_capacidad_segmento(id_segmento,fecha,capacidad_total_tbps,capacidad_utilizada_tbps,capacidad_reservada_tbps) VALUES(?,CURRENT_TIMESTAMP,?,?,?)")){ps.setLong(1,id);ps.setBigDecimal(2,total);ps.setBigDecimal(3,usada);ps.setBigDecimal(4,reservada);ps.executeUpdate();}
        }
    }
    public static Map<String,String> rutas(long origen,long destino)throws SQLException{
        Map<Long,List<long[]>> grafo=new HashMap<>();Map<Long,String> codigos=new HashMap<>();
        try(Connection c=obtenerConexion();PreparedStatement ps=c.prepareStatement("SELECT s.id_segmento,s.codigo_segmento,s.id_estacion_origen,s.id_estacion_destino FROM segmento s JOIN estacion a ON a.id_estacion=s.id_estacion_origen JOIN estacion b ON b.id_estacion=s.id_estacion_destino WHERE s.estado=1 AND a.estado=1 AND b.estado=1 ORDER BY s.id_segmento");ResultSet rs=ps.executeQuery()){while(rs.next()){long id=rs.getLong(1),a=rs.getLong(3),b=rs.getLong(4);codigos.put(id,rs.getString(2));grafo.computeIfAbsent(a,k->new ArrayList<>()).add(new long[]{b,id});grafo.computeIfAbsent(b,k->new ArrayList<>()).add(new long[]{a,id});}}
        Map<String,String> out=new LinkedHashMap<>();if(origen!=destino)caminar(origen,destino,grafo,codigos,new HashSet<>(Set.of(origen)),new ArrayList<>(),out);return out;
    }
    private static void caminar(long actual,long destino,Map<Long,List<long[]>> grafo,Map<Long,String> codigos,Set<Long> vistos,List<Long> ruta,Map<String,String> out){
        if(actual==destino){out.put(Entrada.unir(ruta),String.join(" → ",ruta.stream().map(codigos::get).toList()));return;}
        for(long[] paso:grafo.getOrDefault(actual,List.of()))if(!vistos.contains(paso[0])){vistos.add(paso[0]);ruta.add(paso[1]);caminar(paso[0],destino,grafo,codigos,vistos,ruta,out);ruta.remove(ruta.size()-1);vistos.remove(paso[0]);}
    }
}
