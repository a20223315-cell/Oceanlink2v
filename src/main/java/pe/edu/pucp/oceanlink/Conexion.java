package pe.edu.pucp.oceanlink;

import java.sql.*;
public class Conexion {
    public static Connection abrir() throws SQLException {
        String url=System.getenv().getOrDefault("DB_URL","jdbc:mysql://localhost:3306/oceanlink?useUnicode=true&characterEncoding=UTF-8");
        try{Class.forName("com.mysql.cj.jdbc.Driver");}catch(ClassNotFoundException e){throw new SQLException("No se encontró el controlador MySQL",e);}
        return DriverManager.getConnection(url,System.getenv().getOrDefault("DB_USER","root"),System.getenv().getOrDefault("DB_PASSWORD",""));
    }
}
