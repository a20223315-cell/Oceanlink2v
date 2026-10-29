package pe.edu.pucp.oceanlink;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/RutasServlet")
public class RutasServlet extends HttpServlet {
    protected void doGet(HttpServletRequest r,HttpServletResponse s)throws IOException{
        s.setContentType("application/json;charset=UTF-8");
        try{var rutas=CapacidadDAO.rutas(Entrada.id(r,"origen"),Entrada.id(r,"destino"));StringBuilder out=new StringBuilder("[");for(var ruta:rutas.entrySet()){if(out.length()>1)out.append(',');out.append("{\"value\":\"").append(ruta.getKey()).append("\",\"label\":\"").append(ruta.getValue()).append("\"}");}s.getWriter().print(out.append(']'));}
        catch(Exception ex){s.setStatus(400);s.getWriter().print("[]");}
    }
}
