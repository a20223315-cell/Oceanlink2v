package pe.edu.pucp.oceanlink;
import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;import java.sql.*;
@WebServlet("/EstadoUsuarioServlet")
public class EstadoUsuarioServlet extends HttpServlet{
 protected void doPost(HttpServletRequest r,HttpServletResponse s)throws IOException{
  boolean ajax="XMLHttpRequest".equals(r.getHeader("X-Requested-With"));s.setContentType("text/plain;charset=UTF-8");
  try{long id=Entrada.id(r,"id");int estado=Entrada.estado(r);Usuario usuario=new UsuarioDAO().buscar(id);if(usuario==null){s.setStatus(404);s.getWriter().print("La cuenta ya no existe.");return;}try(Connection c=Conexion.abrir();PreparedStatement ps=c.prepareStatement("UPDATE usuario SET estado=?,fecha_primera_activacion=IF(?=1,COALESCE(fecha_primera_activacion,CURRENT_TIMESTAMP),fecha_primera_activacion) WHERE id_usuario=?")){ps.setInt(1,estado);ps.setInt(2,estado);ps.setLong(3,id);if(ps.executeUpdate()==0){s.sendError(404);return;}}if(ajax)s.getWriter().print("La cuenta de "+usuario.getNombreOrdenado()+" se "+(estado==1?"activó":"desactivó")+" correctamente.");else s.sendRedirect(r.getContextPath()+"/UsuarioServlet?ok=1");}catch(SQLException|IllegalArgumentException e){if(ajax){s.setStatus(400);s.getWriter().print("No se pudo cambiar el estado de la cuenta. Inténtalo nuevamente.");}else s.sendError(400,"No se pudo cambiar el estado de la cuenta.");}
 }
}
