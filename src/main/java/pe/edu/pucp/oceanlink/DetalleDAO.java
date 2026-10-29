package pe.edu.pucp.oceanlink;

import java.sql.*;
import java.util.*;
public final class DetalleDAO {
    private DetalleDAO(){}
    public static List<String[]> filas(String sql,long... ids)throws SQLException{
        List<String[]> out=new ArrayList<>();try(Connection c=Conexion.abrir();PreparedStatement ps=c.prepareStatement(sql)){for(int i=0;i<ids.length;i++)ps.setLong(i+1,ids[i]);try(ResultSet rs=ps.executeQuery()){int n=rs.getMetaData().getColumnCount();while(rs.next()){String[] row=new String[n];for(int i=0;i<n;i++)row[i]=rs.getString(i+1);out.add(row);}}}return out;
    }
}
