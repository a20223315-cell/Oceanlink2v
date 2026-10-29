package pe.edu.pucp.oceanlink;

import java.sql.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public class IncidenciaDAO {
    private static final String CONSULTA="SELECT t.*,(SELECT GROUP_CONCAT(seg.codigo_segmento SEPARATOR ' · ') FROM incidencia_segmento x JOIN segmento seg ON seg.id_segmento=x.id_segmento WHERE x.id_incidencia=t.id_incidencia) segmentos,(SELECT GROUP_CONCAT(ss.id_segmento ORDER BY ss.id_segmento SEPARATOR ',') FROM incidencia_segmento ss WHERE ss.id_incidencia=t.id_incidencia) ruta FROM incidencia t";
    private static final String FILTRO=" WHERE (t.codigo_incidencia REGEXP ? OR t.descripcion REGEXP ?) AND (?='' OR t.fase=?)";
    private Incidencia leer(ResultSet rs)throws SQLException{
        Incidencia e=new Incidencia();e.setSegmentos(rs.getString("segmentos"));
        e.setId(rs.getLong("id_incidencia"));
        e.setCodigo(rs.getString("codigo_incidencia"));
        e.setSeveridad(rs.getString("severidad"));
        e.setFase(rs.getString("fase"));
        e.setDeteccion((rs.getTimestamp("fecha_deteccion")==null?null:rs.getTimestamp("fecha_deteccion").toLocalDateTime()));
        e.setRestauracion((rs.getTimestamp("fecha_restauracion")==null?null:rs.getTimestamp("fecha_restauracion").toLocalDateTime()));
        e.setCierre((rs.getTimestamp("fecha_cierre")==null?null:rs.getTimestamp("fecha_cierre").toLocalDateTime()));
        e.setDescripcion(rs.getString("descripcion"));
        e.setResultado(rs.getString("resultado"));
        e.setCreadoPor(rs.getLong("creado_por"));
        e.setRuta(rs.getString("ruta"));
        return e;
    }

    private String ordenar(String orden,String direccion){String columna=switch(Objects.toString(orden,"codigo")){case "codigo"->"CAST(SUBSTRING_INDEX(t.codigo_incidencia,'-',-1) AS UNSIGNED)";case "descripcion"->"t.descripcion";case "severidad"->"t.severidad";case "estado"->"t.fase";default->"CAST(SUBSTRING_INDEX(t.codigo_incidencia,'-',-1) AS UNSIGNED)";};return columna+("desc".equals(direccion)?" DESC":" ASC")+", t.id_incidencia ASC";}
    private void filtros(PreparedStatement ps,String q,String estado)throws SQLException{ps.setString(1,Busqueda.codigo(q));ps.setString(2,Busqueda.texto(q));ps.setString(3,estado);ps.setString(4,estado);}
    public int contar(String q,String estado)throws SQLException{try(Connection c=Conexion.abrir();PreparedStatement ps=c.prepareStatement("SELECT COUNT(*) FROM incidencia t"+FILTRO)){filtros(ps,q,estado);try(ResultSet rs=ps.executeQuery()){rs.next();return rs.getInt(1);}}}
    public List<Incidencia> listar(String q,String estado,int pagina,int tamano)throws SQLException{return listar(q,estado,pagina,tamano,"codigo","asc");}
    public List<Incidencia> listar(String q,String estado,int pagina,int tamano,String orden,String direccion)throws SQLException{List<Incidencia> out=new ArrayList<>();try(Connection c=Conexion.abrir();PreparedStatement ps=c.prepareStatement(CONSULTA+FILTRO+" ORDER BY "+ordenar(orden,direccion)+" LIMIT ? OFFSET ?")){filtros(ps,q,estado);ps.setInt(5,tamano);ps.setLong(6,(long)(pagina-1)*tamano);try(ResultSet rs=ps.executeQuery()){while(rs.next())out.add(leer(rs));}}return out;}
    public Incidencia buscar(long id)throws SQLException{try(Connection c=Conexion.abrir()){return buscar(c,id);}}
    private Incidencia buscar(Connection c,long id)throws SQLException{try(PreparedStatement ps=c.prepareStatement(CONSULTA+" WHERE t.id_incidencia=?")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){return rs.next()?leer(rs):null;}}}
    private void bloquear(Connection c,long id)throws SQLException{try(PreparedStatement ps=c.prepareStatement("SELECT id_incidencia FROM incidencia WHERE id_incidencia=? FOR UPDATE")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("El registro ya no existe.");}}}

    public void guardar(Incidencia e,List<Long> segmentos,String observacion,long autor)throws SQLException{try(Connection c=Conexion.abrir()){c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);try{
        Incidencia anterior=null;if(e.getId()>0){bloquear(c,e.getId());anterior=buscar(c,e.getId());}
        List<String> fases=List.of("DETECTADA","EN_ANALISIS","REPARACION_PROGRAMADA","EN_REPARACION","RESTAURADA","CERRADA");
        String actual=anterior==null?"DETECTADA":anterior.getFase();int desde=fases.indexOf(actual),hasta=fases.indexOf(e.getFase());
        if(hasta<desde||hasta>desde+1||(anterior==null&&hasta!=0)||"CERRADA".equals(actual))throw new IllegalArgumentException("La incidencia debe respetar el flujo de atención y las cerradas son de solo consulta.");
        if(anterior!=null&&!actual.equals(e.getFase())&&observacion.isBlank())throw new IllegalArgumentException("Registra una observación para el cambio de estado.");
        if(hasta>=4&&(e.getRestauracion()==null||e.getRestauracion().isBefore(e.getDeteccion())))throw new IllegalArgumentException("Registra una restauración posterior o igual a la detección.");
        if(e.getRestauracion()!=null&&e.getRestauracion().isBefore(e.getDeteccion()))throw new IllegalArgumentException("La restauración no puede ser anterior a la detección.");
        if(hasta==5&&(e.getCierre()==null||e.getCierre().isBefore(e.getRestauracion())||e.getResultado().isBlank()))throw new IllegalArgumentException("Para cerrar se requiere resultado y fecha posterior o igual a la restauración.");
        if(hasta<4&&(e.getRestauracion()!=null||e.getCierre()!=null))throw new IllegalArgumentException("Registra la restauración al alcanzar esa fase.");
        if(hasta<5&&e.getCierre()!=null)throw new IllegalArgumentException("La fecha de cierre corresponde a la fase Cerrada.");
        if(anterior!=null&&(!Objects.equals(e.getDeteccion(),anterior.getDeteccion())))throw new IllegalArgumentException("La fecha de detección se conserva para el historial.");
        boolean nuevo=e.getId()==0;
        String sql=nuevo?"INSERT INTO incidencia(severidad,fase,fecha_deteccion,fecha_restauracion,fecha_cierre,descripcion,resultado,creado_por,codigo_incidencia) VALUES(?,?,?,?,?,?,?,?,?)":"UPDATE incidencia SET severidad=?,fase=?,fecha_deteccion=?,fecha_restauracion=?,fecha_cierre=?,descripcion=?,resultado=? WHERE id_incidencia=?";
        try(PreparedStatement ps=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){ps.setString(1,e.getSeveridad());ps.setString(2,e.getFase());ps.setTimestamp(3,Timestamp.valueOf(e.getDeteccion()));ps.setTimestamp(4,e.getRestauracion()==null?null:Timestamp.valueOf(e.getRestauracion()));ps.setTimestamp(5,e.getCierre()==null?null:Timestamp.valueOf(e.getCierre()));ps.setString(6,e.getDescripcion());ps.setString(7,e.getResultado());ps.setLong(8,nuevo?autor:e.getId());if(nuevo)ps.setString(9,Codigos.temporal());ps.executeUpdate();if(nuevo)e.setId(Codigos.insertado(ps));}
        if(nuevo)Codigos.asignar(c,"incidencia","id_incidencia","codigo_incidencia",e.getId(),"INC");
        try(PreparedStatement ps=c.prepareStatement("DELETE FROM incidencia_segmento WHERE id_incidencia=?")){ps.setLong(1,e.getId());ps.executeUpdate();}
        try(PreparedStatement ps=c.prepareStatement("INSERT INTO incidencia_segmento(id_incidencia,id_segmento) VALUES(?,?)")){for(long s:segmentos){ps.setLong(1,e.getId());ps.setLong(2,s);ps.addBatch();}ps.executeBatch();}
        if(nuevo||!observacion.isBlank()){try(PreparedStatement ps=c.prepareStatement("INSERT INTO observacion_incidencia(id_incidencia,fase,observacion,registrado_por) VALUES(?,?,?,?)")){ps.setLong(1,e.getId());ps.setString(2,e.getFase());ps.setString(3,nuevo&&observacion.isBlank()?e.getDescripcion():observacion);ps.setLong(4,autor);ps.executeUpdate();}}
        c.commit();
    }catch(SQLException|RuntimeException ex){c.rollback();throw ex;}}}
    public void eliminar(long id)throws SQLException{try(Connection c=Conexion.abrir()){c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);try{bloquear(c,id);if(!"DETECTADA".equals(buscar(c,id).getFase()))throw new IllegalArgumentException("Solo se eliminan incidencias en fase Detectada. Las atendidas conservan su historial.");for(String tabla:List.of("observacion_incidencia","incidencia_segmento","incidencia")){try(PreparedStatement ps=c.prepareStatement("DELETE FROM "+tabla+" WHERE id_incidencia=?")){ps.setLong(1,id);ps.executeUpdate();}}c.commit();}catch(SQLException|RuntimeException ex){c.rollback();throw ex;}}}
    public List<String[]> observaciones(long id)throws SQLException{return DetalleDAO.filas("SELECT fase,DATE_FORMAT(fecha,'%d/%m/%Y %H:%i'),observacion,u.nombre_completo FROM observacion_incidencia o JOIN usuario u ON u.id_usuario=o.registrado_por WHERE id_incidencia=? ORDER BY fecha,id_observacion",id);}
    public List<String[]> afectados(long id)throws SQLException{return DetalleDAO.filas("SELECT DISTINCT s.codigo_servicio,cl.razon_social,s.capacidad_contratada_tbps,s.estado FROM incidencia_segmento i JOIN servicio_segmento ss ON ss.id_segmento=i.id_segmento JOIN servicio s ON s.id_servicio=ss.id_servicio JOIN solicitud_capacidad r ON r.id_solicitud=s.id_solicitud JOIN cliente cl ON cl.id_cliente=r.id_cliente WHERE i.id_incidencia=? AND s.estado='ACTIVO' ORDER BY s.codigo_servicio",id);}

}
