package pe.edu.pucp.oceanlink;

import java.sql.*;
import java.util.UUID;
public final class Codigos {
    private Codigos(){}
    public static String temporal(){return UUID.randomUUID().toString().replace("-","").substring(0,16);}
    public static String asignar(Connection c,String tabla,String idCol,String codigoCol,long id,String prefijo)throws SQLException{
        String codigo=prefijo+"-"+String.format(java.util.Locale.ROOT,"%04d",id);
        try(PreparedStatement ps=c.prepareStatement("UPDATE "+tabla+" SET "+codigoCol+"=? WHERE "+idCol+"=?")){ps.setString(1,codigo);ps.setLong(2,id);ps.executeUpdate();}return codigo;
    }
    public static long insertado(PreparedStatement ps)throws SQLException{try(ResultSet rs=ps.getGeneratedKeys()){if(rs.next())return rs.getLong(1);throw new SQLException("No se obtuvo el identificador generado.");}}
}
