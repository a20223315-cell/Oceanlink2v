package pe.edu.pucp.oceanlink;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
@WebServlet("/PerfilServlet")
public class PerfilServlet extends HttpServlet{
 protected void doPost(HttpServletRequest r,HttpServletResponse s)throws IOException{
  r.setCharacterEncoding("UTF-8");s.setContentType("text/plain;charset=UTF-8");
  if(r.getSession(false)==null||!(r.getSession().getAttribute("perfilDemoId") instanceof Long id)){s.sendError(400);return;}
  try{String telefono=Entrada.texto(r,"telefono",30,false);String region=Entrada.texto(r,"telefonoRegion",2,true);telefono=Telefono.normalizarRegion(telefono,region);
   try(Connection c=Conexion.abrir();PreparedStatement ps=c.prepareStatement("UPDATE usuario SET telefono=?,telefono_region=? WHERE id_usuario=?")){ps.setString(1,telefono);ps.setString(2,region);ps.setLong(3,id);ps.executeUpdate();}s.getWriter().print("Celular actualizado.");
  }catch(SQLException|IllegalArgumentException ex){s.setStatus(400);s.getWriter().print(ex instanceof IllegalArgumentException?ex.getMessage():"No se pudo guardar el celular. Inténtalo nuevamente.");}
 }
}
