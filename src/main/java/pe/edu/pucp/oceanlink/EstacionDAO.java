package pe.edu.pucp.oceanlink;

import java.sql.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public class EstacionDAO extends DaoBase {
    private static final String CONSULTA="SELECT t.*,pa.codigo_pais,pa.nombre pais_nombre FROM estacion t JOIN pais pa ON pa.id_pais=t.id_pais";
    private static final String FILTRO=" WHERE (t.codigo_estacion REGEXP ? OR t.nombre REGEXP ?) AND (?='' OR CAST(t.estado AS CHAR)=? OR t.condicion_operativa=?) AND (?=0 OR t.id_pais=?)";
    private Estacion leer(ResultSet rs)throws SQLException{
        Estacion e=new Estacion();
        e.setId(rs.getLong("id_estacion"));
        e.setCodigo(rs.getString("codigo_estacion"));
        e.setNombre(rs.getString("nombre"));
        e.setUbicacion(rs.getString("ubicacion"));
        e.setPaisId(rs.getLong("id_pais"));
        e.setCapacidad(rs.getBigDecimal("capacidad_energia_kw"));
        e.setCondicion(rs.getString("condicion_operativa"));
        e.setEstado(rs.getInt("estado"));
        Pais p=new Pais(e.getPaisId());p.setCodigo(rs.getString("codigo_pais"));p.setNombre(rs.getString("pais_nombre"));e.setPais(p);
        return e;
    }

    private String ordenar(String orden,String direccion){String columna=switch(Objects.toString(orden,"codigo")){case "codigo"->"CAST(SUBSTRING_INDEX(t.codigo_estacion,'-',-1) AS UNSIGNED)";case "nombre"->"t.nombre";case "pais"->"pa.nombre";case "condicion"->"t.condicion_operativa";case "estado"->"t.estado";default->"CAST(SUBSTRING_INDEX(t.codigo_estacion,'-',-1) AS UNSIGNED)";};return columna+("desc".equals(direccion)?" DESC":" ASC")+", t.id_estacion ASC";}
    private void filtros(PreparedStatement ps, String q, String estado, long paisId) throws SQLException {
        ps.setString(1, Busqueda.codigo(q));
        ps.setString(2, Busqueda.texto(q));
        ps.setString(3, estado);
        ps.setString(4, estado);
        ps.setString(5, estado);
        ps.setLong(6, paisId);
        ps.setLong(7, paisId);
    }

    public int contar(String q, String estado) throws SQLException {
        return contar(q, estado, 0);
    }

    public int contar(String q, String estado, long paisId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM estacion t JOIN pais pa ON pa.id_pais=t.id_pais" + FILTRO;
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            filtros(pstmt, q, estado, paisId);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public List<Estacion> listar(String q, String estado, int pagina, int tamano) throws SQLException {
        return listar(q, estado, 0, pagina, tamano, "codigo", "asc");
    }

    public List<Estacion> listar(String q, String estado, int pagina, int tamano, String orden, String direccion) throws SQLException {
        return listar(q, estado, 0, pagina, tamano, orden, direccion);
    }

    public List<Estacion> listar(String q, String estado, long paisId, int pagina, int tamano, String orden, String direccion) throws SQLException {
        ArrayList<Estacion> listaEstaciones = new ArrayList<>();
        String sql = CONSULTA + FILTRO + " ORDER BY " + ordenar(orden, direccion) + " LIMIT ? OFFSET ?";
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            filtros(pstmt, q, estado, paisId);
            pstmt.setInt(8, tamano);
            pstmt.setLong(9, (long) (pagina - 1) * tamano);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    listaEstaciones.add(leer(rs));
                }
            }
        }
        return listaEstaciones;
    }
    public Estacion buscar(long id)throws SQLException{try(Connection c=obtenerConexion()){return buscar(c,id);}}
    private Estacion buscar(Connection c,long id)throws SQLException{try(PreparedStatement ps=c.prepareStatement(CONSULTA+" WHERE t.id_estacion=?")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){return rs.next()?leer(rs):null;}}}
    private void bloquear(Connection c,long id)throws SQLException{try(PreparedStatement ps=c.prepareStatement("SELECT id_estacion FROM estacion WHERE id_estacion=? FOR UPDATE")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("El registro ya no existe.");}}}

    public void guardar(Estacion e)throws SQLException{
        try(Connection c=obtenerConexion()){c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);try{
            long id=e.getId(); boolean nuevo=id==0; if(!nuevo)bloquear(c,e.getId());
            String codigoPais;try(PreparedStatement ps=c.prepareStatement("SELECT codigo_pais FROM pais WHERE id_pais=?")){ps.setLong(1,e.getPaisId());try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("País inválido.");codigoPais=rs.getString(1);}}
            if(!nuevo && e.getEstado()==0){try(PreparedStatement ps=c.prepareStatement("SELECT 1 FROM segmento WHERE estado=1 AND (id_estacion_origen=? OR id_estacion_destino=?) LIMIT 1")){ps.setLong(1,id);ps.setLong(2,id);try(ResultSet rs=ps.executeQuery()){if(rs.next())throw new IllegalArgumentException("Desactiva primero los segmentos asociados a esta estación.");}}try(PreparedStatement ps=c.prepareStatement("UPDATE estacion SET estado=0 WHERE id_estacion=?")){ps.setLong(1,id);ps.executeUpdate();}}
            if(e.getId()==0){

                try(PreparedStatement ps=c.prepareStatement("INSERT INTO estacion (codigo_estacion,nombre,ubicacion,id_pais,capacidad_energia_kw,condicion_operativa,estado) VALUES (?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){ps.setString(1,Codigos.temporal());ps.setString(2,e.getNombre());ps.setString(3,e.getUbicacion());ps.setLong(4,e.getPaisId());ps.setBigDecimal(5,e.getCapacidad());ps.setString(6,e.getCondicion());ps.setInt(7,e.getEstado());ps.executeUpdate();e.setId(Codigos.insertado(ps));}
            }else{
                try(PreparedStatement ps=c.prepareStatement("UPDATE estacion SET codigo_estacion=?,nombre=?,ubicacion=?,id_pais=?,capacidad_energia_kw=?,condicion_operativa=?,estado=? WHERE id_estacion=?")){ps.setString(1,e.getCodigo());ps.setString(2,e.getNombre());ps.setString(3,e.getUbicacion());ps.setLong(4,e.getPaisId());ps.setBigDecimal(5,e.getCapacidad());ps.setString(6,e.getCondicion());ps.setInt(7,e.getEstado());ps.setLong(8,e.getId());ps.executeUpdate();}
            }
            e.setCodigo(Codigos.asignar(c,"estacion","id_estacion","codigo_estacion",e.getId(),codigoPais));
            c.commit();
        }catch(SQLException|RuntimeException ex){c.rollback();throw ex;}}
    }
    public void eliminar(long id)throws SQLException{try(Connection c=obtenerConexion()){c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);try{bloquear(c,id);try(PreparedStatement ps=c.prepareStatement("SELECT 1 FROM segmento WHERE estado=1 AND (id_estacion_origen=? OR id_estacion_destino=?) LIMIT 1")){ps.setLong(1,id);ps.setLong(2,id);try(ResultSet rs=ps.executeQuery()){if(rs.next())throw new IllegalArgumentException("Desactiva primero los segmentos asociados a esta estación.");}}try(PreparedStatement ps=c.prepareStatement("UPDATE estacion SET estado=0 WHERE id_estacion=?")){ps.setLong(1,id);ps.executeUpdate();}c.commit();}catch(SQLException|RuntimeException e){c.rollback();throw e;}}}

}
