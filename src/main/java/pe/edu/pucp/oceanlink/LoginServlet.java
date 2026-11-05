package pe.edu.pucp.oceanlink;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private static final Map<String, String> DESTINOS = Map.of(
        "ADMIN", "ConsultaServlet?vista=general",
        "CAPACITY", "ConsultaServlet?vista=capacidad",
        "NETWORK", "ConsultaServlet?vista=red",
        "MAINTENANCE", "MantenimientoServlet",
        "SUPERVISOR", "ConsultaServlet?vista=general"
    );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("usuarioSesion") != null) {
            Usuario usuario = (Usuario) session.getAttribute("usuarioSesion");
            String destino = DESTINOS.getOrDefault(usuario.getRolCodigo(), "login.jsp");
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/" + destino));
            return;
        }

        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        String identificador = request.getParameter("usuario");
        String contrasena = request.getParameter("contrasena");

        try {
            Usuario usuario = usuarioDAO.autenticar(identificador, contrasena);

            if (usuario == null) {
                HttpSession session = request.getSession(true);
                session.setAttribute("mensajeLogin", "Usuario o contraseña incorrectos, o cuenta inactiva.");
                response.sendRedirect(request.getContextPath() + "/LoginServlet");
                return;
            }

            HttpSession anterior = request.getSession(false);
            if (anterior != null) {
                anterior.invalidate();
            }

            HttpSession session = request.getSession(true);
            session.setMaxInactiveInterval(30 * 60);
            session.setAttribute("usuarioSesion", usuario);
            session.setAttribute("usuarioId", usuario.getId());
            session.setAttribute("usuarioCodigo", usuario.getCodigo());
            session.setAttribute("usuarioNombre", usuario.getNombre());
            session.setAttribute("usuarioCorreo", usuario.getCorreo());
            session.setAttribute("rolId", usuario.getRolId());
            session.setAttribute("rolCodigo", usuario.getRolCodigo());
            session.setAttribute("rolNombre", usuario.getRolNombre());

            String destino = DESTINOS.getOrDefault(usuario.getRolCodigo(), "login.jsp");
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/" + destino));
        } catch (SQLException ex) {
            getServletContext().log("No se pudo validar el inicio de sesión", ex);
            HttpSession session = request.getSession(true);
            session.setAttribute("mensajeLogin", "No se pudo validar el acceso. Inténtalo nuevamente.");
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
        }
    }
}
