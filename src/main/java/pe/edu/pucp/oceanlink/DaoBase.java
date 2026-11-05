package pe.edu.pucp.oceanlink;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public abstract class DaoBase {
    protected static Connection obtenerConexion() throws SQLException {
        String url = System.getenv().getOrDefault(
                "DB_URL",
                "jdbc:mysql://localhost:3306/oceanlink?useUnicode=true&characterEncoding=UTF-8"
        );
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el controlador MySQL", e);
        }
        String usuario = System.getenv().getOrDefault("DB_USER", "root");
        String contrasena = System.getenv().getOrDefault("DB_PASSWORD", "");
        return DriverManager.getConnection(url, usuario, contrasena);
    }
}
