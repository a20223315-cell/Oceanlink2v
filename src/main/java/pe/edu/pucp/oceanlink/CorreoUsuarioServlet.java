package pe.edu.pucp.oceanlink;
import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;import java.sql.*;
@WebServlet("/CorreoUsuarioServlet")
public class CorreoUsuarioServlet extends HttpServlet {
 protected void doGet(HttpServletRequest r,HttpServletResponse s)throws IOException {
        HttpSession sesion = r.getSession(false);
        s.setHeader("Cache-Control", "no-store");
        s.setHeader("Pragma", "no-cache");
        s.setDateHeader("Expires", 0);
        if (sesion == null || !(sesion.getAttribute("usuarioSesion") instanceof Usuario usuarioAutenticado) || usuarioAutenticado.getId() <= 0) {
            s.sendRedirect(r.getContextPath() + "/LoginServlet");
            return;
        }

  s.setContentType("text/plain;charset=UTF-8");s.setHeader("Cache-Control","no-store");
  try(Connection c=Conexion.abrir()){String nombres=Entrada.texto(r,"nombres",57,true),apellidos=Entrada.texto(r,"apellidos",100,true);Paises.validarNombre(nombres,"nombres");Paises.validarNombre(apellidos,"apellidos");s.getWriter().print(CorreoCorporativo.generar(c,nombres,apellidos));}
  catch(IllegalArgumentException e){s.setStatus(400);s.getWriter().print(e.getMessage());}
  catch(SQLException e){log("No se pudo consultar el correo disponible",e);s.setStatus(503);s.getWriter().print("No se pudo generar el correo. Inténtalo nuevamente.");}
 }
}
