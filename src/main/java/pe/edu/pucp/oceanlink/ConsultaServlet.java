package pe.edu.pucp.oceanlink;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.sql.*;
import java.io.IOException;
import java.util.*;
@WebServlet("/ConsultaServlet")
public class ConsultaServlet extends HttpServlet {
    protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        HttpSession sesion = r.getSession(false);
        s.setHeader("Cache-Control", "no-store");
        s.setHeader("Pragma", "no-cache");
        s.setDateHeader("Expires", 0);
        if (sesion == null || !(sesion.getAttribute("usuarioSesion") instanceof Usuario usuarioAutenticado) || usuarioAutenticado.getId() <= 0) {
            s.sendRedirect(r.getContextPath() + "/LoginServlet");
            return;
        }

        String vista=Objects.toString(r.getParameter("vista"),"general");
        r.setAttribute("titulo",switch(vista){case "capacidad"->"Capacidad por segmento";case "red"->"Estado de la red";case "historialCapacidad"->"Historial de utilización";case "afectados"->"Servicios y clientes afectados";default->"Panel general";});
        String menu="sup".equals(r.getParameter("menu"))?"sup":vista.equals("red")?"no":vista.equals("general")?"admin":"cp";
        r.setAttribute("modulo","Consulta");r.setAttribute("vista",vista);r.setAttribute("menu",menu);r.setAttribute("rol",switch(menu){case "cp"->"Planificador de capacidad";case "no"->"Operador de red";case "sup"->"Supervisor";default->"Administrador";});r.setAttribute("etiquetas",Etiquetas.todas());
        try{
            List<String[]> filas;
            if(vista.equals("historialCapacidad")){
                r.setAttribute("columnas",List.of("Segmento","Fecha","Total (Tbps)","Utilizada (Tbps)","Reservada (Tbps)","Disponible (Tbps)"));
                filas=DetalleDAO.filas("SELECT s.codigo_segmento,DATE_FORMAT(h.fecha,'%d/%m/%Y %H:%i'),h.capacidad_total_tbps,h.capacidad_utilizada_tbps,h.capacidad_reservada_tbps,h.capacidad_total_tbps-h.capacidad_utilizada_tbps-h.capacidad_reservada_tbps FROM historial_capacidad_segmento h JOIN segmento s ON s.id_segmento=h.id_segmento ORDER BY h.fecha DESC,h.id_historial DESC");
            }else if(vista.equals("afectados")){
                r.setAttribute("columnas",List.of("Incidencia","Segmento","Servicio","Cliente","Severidad"));
                filas=DetalleDAO.filas("SELECT DISTINCT i.codigo_incidencia,seg.codigo_segmento,s.codigo_servicio,cl.razon_social,i.severidad FROM incidencia i JOIN incidencia_segmento ins ON ins.id_incidencia=i.id_incidencia JOIN segmento seg ON seg.id_segmento=ins.id_segmento JOIN servicio_segmento ss ON ss.id_segmento=seg.id_segmento JOIN servicio s ON s.id_servicio=ss.id_servicio JOIN solicitud_capacidad r ON r.id_solicitud=s.id_solicitud JOIN cliente cl ON cl.id_cliente=r.id_cliente WHERE i.fase NOT IN ('CERRADA','RESTAURADA') AND s.estado='ACTIVO' ORDER BY i.codigo_incidencia,seg.codigo_segmento,s.codigo_servicio");
            }else{
                r.setAttribute("columnas",List.of("Segmento","Origen","Destino","Total (Tbps)","Utilizada (Tbps)","Reservada (Tbps)","Disponible (Tbps)","Utilización (%)","Condición","Alerta"));
                String sql="SELECT s.id_segmento,s.codigo_segmento,a.nombre,b.nombre,s.capacidad_total_tbps,s.condicion_operativa FROM segmento s JOIN estacion a ON a.id_estacion=s.id_estacion_origen JOIN estacion b ON b.id_estacion=s.id_estacion_destino WHERE s.estado=1 ORDER BY s.id_segmento";
                filas=new ArrayList<>();try(Connection c=Conexion.abrir();PreparedStatement ps=c.prepareStatement(sql);ResultSet rs=ps.executeQuery()){
                    while(rs.next()){long id=rs.getLong(1);java.math.BigDecimal total=rs.getBigDecimal(5),ocupada=CapacidadDAO.ocupada(c,id,0,0),utilizada;
                        try(PreparedStatement q=c.prepareStatement("SELECT COALESCE(SUM(s.capacidad_contratada_tbps),0) FROM servicio s JOIN servicio_segmento ss ON ss.id_servicio=s.id_servicio WHERE ss.id_segmento=? AND s.estado='ACTIVO'")){q.setLong(1,id);try(ResultSet z=q.executeQuery()){z.next();utilizada=z.getBigDecimal(1);}}
                        java.math.BigDecimal porcentaje=utilizada.multiply(java.math.BigDecimal.valueOf(100)).divide(total,4,java.math.RoundingMode.HALF_UP);String alerta=porcentaje.compareTo(java.math.BigDecimal.valueOf(80))<0?"Normal":porcentaje.compareTo(java.math.BigDecimal.valueOf(95))<=0?"Alta utilización":"Utilización crítica";
                        filas.add(new String[]{rs.getString(2),rs.getString(3),rs.getString(4),total.toPlainString(),utilizada.toPlainString(),ocupada.subtract(utilizada).toPlainString(),total.subtract(ocupada).toPlainString(),porcentaje.setScale(2,java.math.RoundingMode.HALF_UP).toPlainString(),Etiquetas.todas().get(rs.getString(6)),alerta});
                    }
                }
            }
            if(vista.equals("general"))r.setAttribute("indicadores",DetalleDAO.filas("SELECT 'Estaciones activas',COUNT(*) FROM estacion WHERE estado=1 UNION ALL SELECT 'Segmentos activos',COUNT(*) FROM segmento WHERE estado=1 UNION ALL SELECT 'Incidencias en atención',COUNT(*) FROM incidencia WHERE fase NOT IN ('CERRADA','RESTAURADA') UNION ALL SELECT 'Servicios activos',COUNT(*) FROM servicio WHERE estado='ACTIVO' UNION ALL SELECT 'Mantenimientos programados',COUNT(*) FROM mantenimiento WHERE fase='PROGRAMADO'"));
            r.setAttribute("filas",filas);String plantilla=Pantallas.preparar(r,vista,filas);r.getRequestDispatcher("/WEB-INF/views/"+plantilla+".jsp").forward(r,s);
        }catch(SQLException ex){log("Error de consulta",ex);r.setAttribute("error","No se pudo consultar la base de datos.");r.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(r,s);}
    }
}
