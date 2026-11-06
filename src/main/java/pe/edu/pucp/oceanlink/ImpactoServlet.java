package pe.edu.pucp.oceanlink;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
@WebServlet("/ImpactoServlet")
public class ImpactoServlet extends HttpServlet {
    private String json(String v){return "\""+Objects.toString(v,"").replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t")+"\"";}
    protected void doGet(HttpServletRequest r,HttpServletResponse s)throws IOException{
        HttpSession sesion = r.getSession(false);
        s.setHeader("Cache-Control", "no-store");
        s.setHeader("Pragma", "no-cache");
        s.setDateHeader("Expires", 0);
        if (sesion == null || !(sesion.getAttribute("usuarioSesion") instanceof Usuario usuarioAutenticado) || usuarioAutenticado.getId() <= 0) {
            s.sendRedirect(r.getContextPath() + "/LoginServlet");
            return;
        }

        s.setContentType("application/json;charset=UTF-8");
        try{long estacion=Entrada.opcional(r,"estacionId"),segmento=Entrada.opcional(r,"segmentoId");java.util.List<String[]> filas;
            if(r.getParameter("codigo")!=null){boolean incidencia="incidencia".equals(r.getParameter("tipo"));String sql=incidencia?"SELECT id_incidencia,0,0 FROM incidencia WHERE codigo_incidencia=?":"SELECT id_mantenimiento,COALESCE(id_estacion,0),COALESCE(id_segmento,0) FROM mantenimiento WHERE codigo_mantenimiento=?";try(var c=Conexion.abrir();var ps=c.prepareStatement(sql)){ps.setString(1,r.getParameter("codigo"));try(var rs=ps.executeQuery()){if(!rs.next()){s.sendError(404);return;}filas=incidencia?new IncidenciaDAO().afectados(rs.getLong(1)):new MantenimientoDAO().afectados(rs.getLong(2),rs.getLong(3));}}}else filas=new MantenimientoDAO().afectados(estacion,segmento);StringBuilder out=new StringBuilder("[");for(String[] fila:filas){if(out.length()>1)out.append(',');out.append('[');for(int i=0;i<fila.length;i++){if(i>0)out.append(',');out.append(json(fila[i]));}out.append(']');}s.getWriter().print(out.append(']'));}catch(Exception ex){s.setStatus(400);s.getWriter().print("[]");}
    }
}
