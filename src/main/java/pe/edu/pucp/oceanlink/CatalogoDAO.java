package pe.edu.pucp.oceanlink;

import java.sql.*;
import java.util.*;
public class CatalogoDAO {
    private List<Opcion> opciones(String sql)throws SQLException{List<Opcion> out=new ArrayList<>();try(Connection c=Conexion.abrir();PreparedStatement ps=c.prepareStatement(sql);ResultSet rs=ps.executeQuery()){while(rs.next())out.add(new Opcion(rs.getLong(1),rs.getString(2)));}return out;}
    public List<Opcion> paises()throws SQLException{return opciones("SELECT id_pais,CONCAT(codigo_pais,' · ',nombre) FROM pais WHERE codigo_pais IN ('PE','CL','CO','PA') ORDER BY nombre");}
    public List<Opcion> roles()throws SQLException{return opciones("SELECT id_rol,nombre FROM rol ORDER BY id_rol");}
    public List<Opcion> estaciones()throws SQLException{return opciones("SELECT id_estacion,CONCAT(codigo_estacion,' · ',nombre,IF(estado=0,' (Inactiva)','')) FROM estacion ORDER BY nombre");}
    public List<Opcion> segmentos()throws SQLException{return opciones("SELECT s.id_segmento,CONCAT(s.codigo_segmento,' · ',o.nombre,' — ',d.nombre,IF(s.estado=0,' (Inactivo)','')) FROM segmento s JOIN estacion o ON o.id_estacion=s.id_estacion_origen JOIN estacion d ON d.id_estacion=s.id_estacion_destino ORDER BY s.id_segmento");}
    public List<Opcion> clientes()throws SQLException{return opciones("SELECT id_cliente,CONCAT(codigo_cliente,' · ',razon_social,IF(estado=0,' (Inactivo)','')) FROM cliente ORDER BY razon_social");}
    public List<Opcion> usuarios()throws SQLException{return opciones("SELECT id_usuario,CONCAT(nombre_completo,IF(estado=0,' (Inactivo)','')) FROM usuario ORDER BY nombre_completo");}
    public List<Opcion> tipos()throws SQLException{return opciones("SELECT id_tipo_mantenimiento,nombre FROM tipo_mantenimiento ORDER BY id_tipo_mantenimiento");}
    public List<Opcion> solicitudes()throws SQLException{return opciones("SELECT r.id_solicitud,CONCAT(r.codigo_solicitud,' · ',cl.razon_social,' · ',r.capacidad_solicitada_tbps,' Tbps') FROM solicitud_capacidad r JOIN estado_solicitud_capacidad e ON e.id_estado_solicitud=r.id_estado_solicitud JOIN cliente cl ON cl.id_cliente=r.id_cliente WHERE e.nombre='PROVISIONADA' AND NOT EXISTS(SELECT 1 FROM servicio s WHERE s.id_solicitud=r.id_solicitud AND s.estado<>'PENDIENTE') ORDER BY r.id_solicitud");}
    public void cargar(jakarta.servlet.http.HttpServletRequest r)throws SQLException{r.setAttribute("paises",paises());r.setAttribute("roles",roles());r.setAttribute("estaciones",estaciones());r.setAttribute("segmentos",segmentos());r.setAttribute("clientes",clientes());r.setAttribute("usuarios",usuarios());r.setAttribute("tipos",tipos());r.setAttribute("solicitudes",solicitudes());}
}
