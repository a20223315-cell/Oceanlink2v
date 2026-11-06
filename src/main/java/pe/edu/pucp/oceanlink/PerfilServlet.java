package pe.edu.pucp.oceanlink;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
@WebServlet("/PerfilServlet")
public class PerfilServlet extends HttpServlet{
 protected void doPost(HttpServletRequest r,HttpServletResponse s)throws IOException{
        HttpSession sesion = r.getSession(false);
        s.setHeader("Cache-Control", "no-store");
        s.setHeader("Pragma", "no-cache");
        s.setDateHeader("Expires", 0);
        if (sesion == null || !(sesion.getAttribute("usuarioSesion") instanceof Usuario usuarioAutenticado) || usuarioAutenticado.getId() <= 0) {
            s.sendRedirect(r.getContextPath() + "/LoginServlet");
            return;
        }

  r.setCharacterEncoding("UTF-8");s.setContentType("text/plain;charset=UTF-8");
  try {
   String telefono = Entrada.texto(r,"telefono",30,true);
   String region = Entrada.texto(r,"telefonoRegion",2,true);
   telefono = Telefono.normalizarRegion(telefono,region);
   if (!new UsuarioDAO().actualizarCelular(usuarioAutenticado.getId(), telefono, region)) {
    s.setStatus(403);
    s.getWriter().print("Tu cuenta no está disponible para actualizar el perfil.");
    return;
   }
   usuarioAutenticado.setTelefono(telefono);
   usuarioAutenticado.setTelefonoRegion(region);
   s.getWriter().print("Celular actualizado.");
  } catch (IllegalArgumentException ex) {
   s.setStatus(400);
   s.getWriter().print(ex.getMessage());
  } catch (SQLException ex) {
   getServletContext().log("No se pudo guardar el celular del perfil", ex);
   s.setStatus(500);
   s.getWriter().print("No se pudo guardar el celular. Inténtalo nuevamente.");
  }
 }
}
