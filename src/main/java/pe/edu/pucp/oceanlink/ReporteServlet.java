package pe.edu.pucp.oceanlink;
import jakarta.servlet.*;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;
import java.sql.*;import java.io.IOException;import java.util.*;import java.time.*;
@WebServlet("/ReporteServlet")
public class ReporteServlet extends HttpServlet{
 protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        HttpSession sesion = r.getSession(false);
        s.setHeader("Cache-Control", "no-store");
        s.setHeader("Pragma", "no-cache");
        s.setDateHeader("Expires", 0);
        if (sesion == null || !(sesion.getAttribute("usuarioSesion") instanceof Usuario usuarioAutenticado) || usuarioAutenticado.getId() <= 0) {
            s.sendRedirect(r.getContextPath() + "/LoginServlet");
            return;
        }
        mostrar(r,s,false);}
 protected void doPost(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        HttpSession sesion = r.getSession(false);
        s.setHeader("Cache-Control", "no-store");
        s.setHeader("Pragma", "no-cache");
        s.setDateHeader("Expires", 0);
        if (sesion == null || !(sesion.getAttribute("usuarioSesion") instanceof Usuario usuarioAutenticado) || usuarioAutenticado.getId() <= 0) {
            s.sendRedirect(r.getContextPath() + "/LoginServlet");
            return;
        }
        mostrar(r,s,true);}
 private void mostrar(HttpServletRequest r,HttpServletResponse s,boolean guardar)throws ServletException,IOException{
  r.setAttribute("menu","sup");r.setAttribute("rol","Supervisor");r.setAttribute("titulo","Reporte operativo");r.setAttribute("etiquetas",Etiquetas.todas());Pantallas.perfil(r);
  try{
   String tipo=Objects.toString(r.getParameter("tipo"),"OPERACION_GENERAL"),desde=Objects.toString(r.getParameter("desde"),LocalDate.now().minusDays(30).toString()),hasta=Objects.toString(r.getParameter("hasta"),LocalDate.now().toString());long segmento=Entrada.opcional(r,"segmento");
   if(r.getParameter("id")!=null){List<String[]> data=DetalleDAO.filas("SELECT tipo,DATE_FORMAT(fecha_inicio,'%Y-%m-%d'),DATE_FORMAT(fecha_fin,'%Y-%m-%d'),COALESCE(id_segmento,0) FROM reporte_generado WHERE id_reporte=?",Entrada.id(r,"id"));if(data.isEmpty()){s.sendError(404);return;}tipo=data.get(0)[0];desde=data.get(0)[1];hasta=data.get(0)[2];segmento=Long.parseLong(data.get(0)[3]);}
   LocalDate start=LocalDate.parse(desde),end=LocalDate.parse(hasta);if(end.isBefore(start))throw new IllegalArgumentException();
   String sql;List<String> columnas;
   switch(tipo){
    case "INCIDENCIAS":columnas=List.of("Código","Severidad","Estado","Detección","Resultado");sql="SELECT i.codigo_incidencia,i.severidad,i.fase,i.fecha_deteccion,COALESCE(i.resultado,'') FROM incidencia i WHERE DATE(i.fecha_deteccion) BETWEEN ? AND ? AND (?=0 OR EXISTS(SELECT 1 FROM incidencia_segmento x WHERE x.id_incidencia=i.id_incidencia AND x.id_segmento=?))";break;
    case "MANTENIMIENTOS":columnas=List.of("Código","Estado","Inicio programado","Descripción","Resultado");sql="SELECT m.codigo_mantenimiento,m.fase,m.inicio_programado,m.descripcion,COALESCE(m.resultado,'') FROM mantenimiento m WHERE DATE(m.inicio_programado) BETWEEN ? AND ? AND (?=0 OR m.id_segmento=?)";break;
    case "SERVICIOS_CLIENTES":columnas=List.of("Servicio","Cliente","Capacidad (Tbps)","Estado");sql="SELECT v.codigo_servicio,c.razon_social,v.capacidad_contratada_tbps,v.estado FROM servicio v JOIN solicitud_capacidad sol ON sol.id_solicitud=v.id_solicitud JOIN cliente c ON c.id_cliente=sol.id_cliente WHERE DATE(COALESCE(v.fecha_activacion,sol.fecha_solicitud)) BETWEEN ? AND ? AND (?=0 OR EXISTS(SELECT 1 FROM servicio_segmento x WHERE x.id_servicio=v.id_servicio AND x.id_segmento=?))";break;
    case "OPERACION_GENERAL":columnas=List.of("Área","Código","Estado","Fecha");sql="WITH criterios AS (SELECT CAST(? AS DATE) desde,CAST(? AS DATE) hasta,? segmento,? alcance) SELECT 'Incidencia',i.codigo_incidencia,i.fase,DATE_FORMAT(i.fecha_deteccion,'%d/%m/%Y %H:%i') FROM incidencia i CROSS JOIN criterios c WHERE DATE(i.fecha_deteccion) BETWEEN c.desde AND c.hasta AND (c.segmento=0 OR EXISTS(SELECT 1 FROM incidencia_segmento x WHERE x.id_incidencia=i.id_incidencia AND x.id_segmento=c.segmento)) UNION ALL SELECT 'Mantenimiento',m.codigo_mantenimiento,m.fase,DATE_FORMAT(m.inicio_programado,'%d/%m/%Y %H:%i') FROM mantenimiento m CROSS JOIN criterios c WHERE DATE(m.inicio_programado) BETWEEN c.desde AND c.hasta AND (c.segmento=0 OR m.id_segmento=c.segmento) UNION ALL SELECT 'Servicio',v.codigo_servicio,v.estado,DATE_FORMAT(v.fecha_activacion,'%d/%m/%Y %H:%i') FROM servicio v CROSS JOIN criterios c WHERE DATE(v.fecha_activacion) BETWEEN c.desde AND c.hasta AND (c.segmento=0 OR EXISTS(SELECT 1 FROM servicio_segmento x WHERE x.id_servicio=v.id_servicio AND x.id_segmento=c.segmento))";break;
    case "CAPACIDAD_UTILIZACION":case "INDICADORES_HISTORICOS":columnas=List.of("Segmento","Fecha","Total (Tbps)","Utilizada (Tbps)","Reservada (Tbps)");sql="SELECT s.codigo_segmento,h.fecha,h.capacidad_total_tbps,h.capacidad_utilizada_tbps,h.capacidad_reservada_tbps FROM historial_capacidad_segmento h JOIN segmento s ON s.id_segmento=h.id_segmento WHERE DATE(h.fecha) BETWEEN ? AND ? AND (?=0 OR h.id_segmento=?) ORDER BY h.fecha";break;
    default:throw new IllegalArgumentException();
   }
   List<String[]> rows=new ArrayList<>();try(Connection c=Conexion.abrir();PreparedStatement ps=c.prepareStatement(sql)){ps.setString(1,desde);ps.setString(2,hasta);ps.setLong(3,segmento);ps.setLong(4,segmento);try(ResultSet rs=ps.executeQuery()){while(rs.next()){String[] row=new String[columnas.size()];for(int i=0;i<row.length;i++)row[i]=rs.getString(i+1);rows.add(row);}}}
   if(guardar){if(!(r.getAttribute("perfil") instanceof Usuario usuario)){s.sendError(400);return;}try(Connection c=Conexion.abrir()){ReporteContenido.preparar(c);c.setAutoCommit(false);try(PreparedStatement ps=c.prepareStatement("INSERT INTO reporte_generado(codigo_reporte,tipo,id_segmento,fecha_inicio,fecha_fin,generado_por) VALUES(?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){ps.setString(1,"REP-"+UUID.randomUUID().toString().substring(0,12));ps.setString(2,tipo);if(segmento==0)ps.setNull(3,Types.BIGINT);else ps.setLong(3,segmento);ps.setString(4,desde+" 00:00:00");ps.setString(5,hasta+" 23:59:59");ps.setLong(6,usuario.getId());ps.executeUpdate();try(ResultSet key=ps.getGeneratedKeys()){key.next();ReporteContenido.guardar(c,key.getLong(1),columnas,rows);c.commit();s.sendRedirect(r.getContextPath()+"/ReporteServlet?id="+key.getLong(1));return;}}}}
   r.setAttribute("columnas",columnas);r.setAttribute("filas",rows);r.setAttribute("tipoReporte",tipo);r.setAttribute("desdeReporte",desde);r.setAttribute("hastaReporte",hasta);r.setAttribute("segmentoReporte",segmento);if(r.getParameter("id")!=null&&!ReporteContenido.cargar(r,Entrada.id(r,"id")))r.setAttribute("reporteAnterior",true);r.getRequestDispatcher("/WEB-INF/views/reporte.jsp").forward(r,s);
  }catch(SQLException|IllegalArgumentException ex){log("Error en reporte",ex);s.sendError(400,"Revisa el periodo y los criterios del reporte.");}
 }
}
