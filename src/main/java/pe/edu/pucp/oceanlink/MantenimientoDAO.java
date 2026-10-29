package pe.edu.pucp.oceanlink;

import java.sql.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public class MantenimientoDAO {
    private static final String CONSULTA="SELECT t.*,ti.nombre tipo_nombre,COALESCE(CONCAT(e.codigo_estacion,' · ',e.nombre),s.codigo_segmento) elemento_nombre FROM mantenimiento t JOIN tipo_mantenimiento ti ON ti.id_tipo_mantenimiento=t.id_tipo_mantenimiento LEFT JOIN estacion e ON e.id_estacion=t.id_estacion LEFT JOIN segmento s ON s.id_segmento=t.id_segmento";
    private static final String FILTRO=" WHERE (t.codigo_mantenimiento REGEXP ? OR t.descripcion REGEXP ?) AND (?='' OR t.fase=?)";
    private Mantenimiento leer(ResultSet rs)throws SQLException{
        Mantenimiento e=new Mantenimiento();
        e.setId(rs.getLong("id_mantenimiento"));
        e.setCodigo(rs.getString("codigo_mantenimiento"));
        e.setTipoId(rs.getLong("id_tipo_mantenimiento"));
        e.setEstacionId(rs.getLong("id_estacion"));
        e.setSegmentoId(rs.getLong("id_segmento"));
        e.setFase(rs.getString("fase"));
        e.setInicio((rs.getTimestamp("inicio_programado")==null?null:rs.getTimestamp("inicio_programado").toLocalDateTime()));
        e.setFin((rs.getTimestamp("fin_programado")==null?null:rs.getTimestamp("fin_programado").toLocalDateTime()));
        e.setInicioReal((rs.getTimestamp("inicio_real")==null?null:rs.getTimestamp("inicio_real").toLocalDateTime()));
        e.setCierre((rs.getTimestamp("fecha_cierre")==null?null:rs.getTimestamp("fecha_cierre").toLocalDateTime()));
        e.setDescripcion(rs.getString("descripcion"));
        e.setResultado(rs.getString("resultado"));
        e.setCreadoPor(rs.getLong("creado_por"));
        e.setTipoNombre(rs.getString("tipo_nombre"));
        e.setElementoNombre(rs.getString("elemento_nombre"));
        return e;
    }

    private String ordenar(String orden,String direccion){String columna=switch(Objects.toString(orden,"codigo")){case "codigo"->"CAST(SUBSTRING_INDEX(t.codigo_mantenimiento,'-',-1) AS UNSIGNED)";case "descripcion"->"t.descripcion";case "estado"->"t.fase";default->"CAST(SUBSTRING_INDEX(t.codigo_mantenimiento,'-',-1) AS UNSIGNED)";};return columna+("desc".equals(direccion)?" DESC":" ASC")+", t.id_mantenimiento ASC";}
    private void filtros(PreparedStatement ps,String q,String estado)throws SQLException{ps.setString(1,Busqueda.codigo(q));ps.setString(2,Busqueda.texto(q));ps.setString(3,estado);ps.setString(4,estado);}
    public int contar(String q,String estado)throws SQLException{try(Connection c=Conexion.abrir();PreparedStatement ps=c.prepareStatement("SELECT COUNT(*) FROM mantenimiento t JOIN tipo_mantenimiento ti ON ti.id_tipo_mantenimiento=t.id_tipo_mantenimiento LEFT JOIN estacion e ON e.id_estacion=t.id_estacion LEFT JOIN segmento s ON s.id_segmento=t.id_segmento"+FILTRO)){filtros(ps,q,estado);try(ResultSet rs=ps.executeQuery()){rs.next();return rs.getInt(1);}}}
    public List<Mantenimiento> listar(String q,String estado,int pagina,int tamano)throws SQLException{return listar(q,estado,pagina,tamano,"codigo","asc");}
    public List<Mantenimiento> listar(String q,String estado,int pagina,int tamano,String orden,String direccion)throws SQLException{List<Mantenimiento> out=new ArrayList<>();try(Connection c=Conexion.abrir();PreparedStatement ps=c.prepareStatement(CONSULTA+FILTRO+" ORDER BY "+ordenar(orden,direccion)+" LIMIT ? OFFSET ?")){filtros(ps,q,estado);ps.setInt(5,tamano);ps.setLong(6,(long)(pagina-1)*tamano);try(ResultSet rs=ps.executeQuery()){while(rs.next())out.add(leer(rs));}}return out;}
    public Mantenimiento buscar(long id)throws SQLException{try(Connection c=Conexion.abrir()){return buscar(c,id);}}
    private Mantenimiento buscar(Connection c,long id)throws SQLException{try(PreparedStatement ps=c.prepareStatement(CONSULTA+" WHERE t.id_mantenimiento=?")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){return rs.next()?leer(rs):null;}}}
    private void bloquear(Connection c,long id)throws SQLException{try(PreparedStatement ps=c.prepareStatement("SELECT id_mantenimiento FROM mantenimiento WHERE id_mantenimiento=? FOR UPDATE")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("El registro ya no existe.");}}}

    public void guardar(Mantenimiento e,String actividad,long autor)throws SQLException{try(Connection c=Conexion.abrir()){c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);try{
        Mantenimiento anterior=null;if(e.getId()>0){bloquear(c,e.getId());anterior=buscar(c,e.getId());}
        if((e.getEstacionId()==0)==(e.getSegmentoId()==0))throw new IllegalArgumentException("Selecciona una estación o un segmento, solo uno.");
        List<String> fases=List.of("PROGRAMADO","EN_PROGRESO","FINALIZADO");String actual=anterior==null?"PROGRAMADO":anterior.getFase();int desde=fases.indexOf(actual),hasta=fases.indexOf(e.getFase());
        if(hasta<desde||hasta>desde+1||(anterior==null&&hasta!=0)||"FINALIZADO".equals(actual))throw new IllegalArgumentException("El mantenimiento debe pasar de Programado a En progreso y luego a Finalizado.");
        if(!e.getFin().isAfter(e.getInicio()))throw new IllegalArgumentException("El fin programado debe ser posterior al inicio.");
        if(anterior!=null&&!"PROGRAMADO".equals(actual)&&(e.getEstacionId()!=anterior.getEstacionId()||e.getSegmentoId()!=anterior.getSegmentoId()||!e.getInicio().equals(anterior.getInicio())||!e.getFin().equals(anterior.getFin())||e.getTipoId()!=anterior.getTipoId()))throw new IllegalArgumentException("La infraestructura y programación no cambian una vez iniciado el mantenimiento.");
        if(hasta>=1&&e.getInicioReal()==null)throw new IllegalArgumentException("Registra la fecha real de inicio.");
        if(hasta==2&&(e.getCierre()==null||e.getCierre().isBefore(e.getInicioReal())||e.getResultado().isBlank()))throw new IllegalArgumentException("Para finalizar se requiere resultado y cierre posterior o igual al inicio real.");
        if(hasta<2&&e.getCierre()!=null)throw new IllegalArgumentException("La fecha de cierre se registra al finalizar.");
        if(hasta==0&&e.getInicioReal()!=null)throw new IllegalArgumentException("El inicio real se registra al pasar a En progreso.");
        boolean nuevo=e.getId()==0;
        String sql=nuevo?"INSERT INTO mantenimiento(id_tipo_mantenimiento,id_estacion,id_segmento,fase,inicio_programado,fin_programado,inicio_real,fecha_cierre,descripcion,resultado,creado_por,codigo_mantenimiento) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)":"UPDATE mantenimiento SET id_tipo_mantenimiento=?,id_estacion=?,id_segmento=?,fase=?,inicio_programado=?,fin_programado=?,inicio_real=?,fecha_cierre=?,descripcion=?,resultado=? WHERE id_mantenimiento=?";
        try(PreparedStatement ps=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){ps.setLong(1,e.getTipoId());if(e.getEstacionId()==0)ps.setNull(2,Types.BIGINT);else ps.setLong(2,e.getEstacionId());if(e.getSegmentoId()==0)ps.setNull(3,Types.BIGINT);else ps.setLong(3,e.getSegmentoId());ps.setString(4,e.getFase());ps.setTimestamp(5,Timestamp.valueOf(e.getInicio()));ps.setTimestamp(6,Timestamp.valueOf(e.getFin()));ps.setTimestamp(7,e.getInicioReal()==null?null:Timestamp.valueOf(e.getInicioReal()));ps.setTimestamp(8,e.getCierre()==null?null:Timestamp.valueOf(e.getCierre()));ps.setString(9,e.getDescripcion());ps.setString(10,e.getResultado());ps.setLong(11,nuevo?autor:e.getId());if(nuevo)ps.setString(12,Codigos.temporal());ps.executeUpdate();if(nuevo)e.setId(Codigos.insertado(ps));}
        if(nuevo)Codigos.asignar(c,"mantenimiento","id_mantenimiento","codigo_mantenimiento",e.getId(),"MNT");
        if(nuevo||!actual.equals(e.getFase())||!actividad.isBlank()){
            String descripcion=hasta==2?e.getResultado():actividad.isBlank()?e.getDescripcion():actividad;
            try(PreparedStatement ps=c.prepareStatement("INSERT INTO actividad_mantenimiento(id_mantenimiento,tipo_actividad,descripcion,registrado_por) VALUES(?,?,?,?)")){ps.setLong(1,e.getId());ps.setString(2,nuevo?"Programación":!actual.equals(e.getFase())?e.getFase():"Actividad registrada");ps.setString(3,descripcion);ps.setLong(4,autor);ps.executeUpdate();}
        }c.commit();
    }catch(SQLException|RuntimeException ex){c.rollback();throw ex;}}}
    public void eliminar(long id)throws SQLException{try(Connection c=Conexion.abrir()){c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);try{bloquear(c,id);if(!"PROGRAMADO".equals(buscar(c,id).getFase()))throw new IllegalArgumentException("Solo se eliminan mantenimientos programados. Los iniciados conservan su historial.");for(String tabla:List.of("actividad_mantenimiento","mantenimiento")){try(PreparedStatement ps=c.prepareStatement("DELETE FROM "+tabla+" WHERE id_mantenimiento=?")){ps.setLong(1,id);ps.executeUpdate();}}c.commit();}catch(SQLException|RuntimeException ex){c.rollback();throw ex;}}}
    public List<String[]> actividades(long id)throws SQLException{return DetalleDAO.filas("SELECT tipo_actividad,DATE_FORMAT(fecha,'%d/%m/%Y %H:%i'),descripcion,u.nombre_completo FROM actividad_mantenimiento a JOIN usuario u ON u.id_usuario=a.registrado_por WHERE id_mantenimiento=? ORDER BY fecha,id_actividad",id);}
    public List<String[]> afectados(long estacion,long segmento)throws SQLException{return DetalleDAO.filas("SELECT DISTINCT s.codigo_servicio,cl.razon_social,s.capacidad_contratada_tbps,s.estado FROM servicio s JOIN servicio_segmento ss ON ss.id_servicio=s.id_servicio JOIN segmento seg ON seg.id_segmento=ss.id_segmento JOIN solicitud_capacidad r ON r.id_solicitud=s.id_solicitud JOIN cliente cl ON cl.id_cliente=r.id_cliente WHERE s.estado='ACTIVO' AND (seg.id_segmento=? OR seg.id_estacion_origen=? OR seg.id_estacion_destino=?) ORDER BY s.codigo_servicio",segmento,estacion,estacion);}

}
