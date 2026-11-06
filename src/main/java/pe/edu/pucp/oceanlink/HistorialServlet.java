package pe.edu.pucp.oceanlink;
import jakarta.servlet.*;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;import java.sql.*;
@WebServlet("/HistorialServlet")
public class HistorialServlet extends HttpServlet{
 protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        HttpSession sesion = r.getSession(false);
        s.setHeader("Cache-Control", "no-store");
        s.setHeader("Pragma", "no-cache");
        s.setDateHeader("Expires", 0);
        if (sesion == null || !(sesion.getAttribute("usuarioSesion") instanceof Usuario usuarioAutenticado) || usuarioAutenticado.getId() <= 0) {
            s.sendRedirect(r.getContextPath() + "/LoginServlet");
            return;
        }

  boolean incidencia=!"mantenimientos".equals(r.getParameter("tipo"));r.setAttribute("menu",incidencia?"no":"mc");r.setAttribute("rol",incidencia?"Operador de red":"Coordinador de mantenimiento");r.setAttribute("titulo",incidencia?"Historial de incidencias":"Historial de mantenimientos");r.setAttribute("etiquetas",Etiquetas.todas());
  try{r.setAttribute("filas",DetalleDAO.filas(incidencia?"SELECT i.codigo_incidencia,COALESCE((SELECT GROUP_CONCAT(s.codigo_segmento SEPARATOR ' · ') FROM incidencia_segmento x JOIN segmento s ON s.id_segmento=x.id_segmento WHERE x.id_incidencia=i.id_incidencia),''),i.severidad,DATE_FORMAT(i.fecha_cierre,'%d/%m/%Y %H:%i'),i.resultado,CONCAT('LINK:IncidenciaServlet?action=detalle&id=',i.id_incidencia) FROM incidencia i WHERE i.fase='CERRADA' ORDER BY i.fecha_cierre DESC":"SELECT m.codigo_mantenimiento,COALESCE(e.codigo_estacion,s.codigo_segmento),t.nombre,DATE_FORMAT(m.fecha_cierre,'%d/%m/%Y %H:%i'),m.resultado,CONCAT('LINK:MantenimientoServlet?action=detalle&id=',m.id_mantenimiento) FROM mantenimiento m JOIN tipo_mantenimiento t ON t.id_tipo_mantenimiento=m.id_tipo_mantenimiento LEFT JOIN estacion e ON e.id_estacion=m.id_estacion LEFT JOIN segmento s ON s.id_segmento=m.id_segmento WHERE m.fase='FINALIZADO' ORDER BY m.fecha_cierre DESC"));r.getRequestDispatcher("/WEB-INF/views/"+(incidencia?"no_incident_history":"mc_history")+".jsp").forward(r,s);}catch(SQLException ex){log("Error en historial",ex);s.sendError(500,"No se pudo consultar el historial.");}
 }
}
