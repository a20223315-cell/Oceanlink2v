package pe.edu.pucp.oceanlink;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
@WebServlet("/ServicioServlet")
public class ServicioServlet extends HttpServlet {
    private final ServicioDAO dao=new ServicioDAO();
    private void preparar(HttpServletRequest r){r.setAttribute("titulo","Servicios y rutas");r.setAttribute("modulo","Servicio");r.setAttribute("menu","cp");r.setAttribute("rol","Planificador de capacidad");r.setAttribute("etiquetas",Etiquetas.todas());r.setAttribute("estados",List.of("ACTIVO","PENDIENTE","CANCELADO"));}
    protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        HttpSession sesion = r.getSession(false);
        s.setHeader("Cache-Control", "no-store");
        s.setHeader("Pragma", "no-cache");
        s.setDateHeader("Expires", 0);
        if (sesion == null || !(sesion.getAttribute("usuarioSesion") instanceof Usuario usuarioAutenticado) || usuarioAutenticado.getId() <= 0) {
            s.sendRedirect(r.getContextPath() + "/LoginServlet");
            return;
        }
        preparar(r);String accion=Objects.toString(r.getParameter("action"),"lista");try{
        switch(accion){
            case "lista":
                String q=Entrada.texto(r,"q",160,false),estado=Entrada.texto(r,"estado",40,false);int tamano=Entrada.tamano(r),total=dao.contar(q,estado),paginas=Math.max(1,(int)Math.ceil((double)total/tamano)),pagina=Math.min(Entrada.pagina(r),paginas);
                r.setAttribute("lista",dao.listar(q,estado,pagina,tamano,r.getParameter("orden"),r.getParameter("direccion")));r.setAttribute("pagina",pagina);r.setAttribute("paginas",paginas);r.setAttribute("tamano",tamano);r.setAttribute("total",total);r.setAttribute("desde",total==0?0:(pagina-1)*tamano+1);r.setAttribute("hasta",Math.min(pagina*tamano,total));r.setAttribute("q",q);r.setAttribute("estadoFiltro",estado);
                r.getRequestDispatcher("/WEB-INF/views/servicioLista.jsp").forward(r,s);break;
            case "formCrear":case "editar":case "detalle":
                Servicio e;if("formCrear".equals(accion)){e=new Servicio();}else{e=dao.buscar(Entrada.id(r,"id"));if(e==null){s.sendError(404);return;}}
                r.setAttribute("soloLectura","detalle".equals(accion));formulario(r,s,e);break;
            default:s.sendError(400);
        }
    }catch(IllegalArgumentException ex){s.sendError(400,ex.getMessage());}catch(SQLException ex){log("Error consultando Servicio",ex);r.setAttribute("error","No se pudo consultar la información. Revisa la conexión a la base de datos.");r.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(r,s);}}
    protected void doPost(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        HttpSession sesion = r.getSession(false);
        s.setHeader("Cache-Control", "no-store");
        s.setHeader("Pragma", "no-cache");
        s.setDateHeader("Expires", 0);
        if (sesion == null || !(sesion.getAttribute("usuarioSesion") instanceof Usuario usuarioAutenticado) || usuarioAutenticado.getId() <= 0) {
            s.sendRedirect(r.getContextPath() + "/LoginServlet");
            return;
        }
        r.setCharacterEncoding("UTF-8");preparar(r);String accion=r.getParameter("action");
        if(!Set.of("crear","actualizar","borrar").contains(Objects.toString(accion,""))){s.sendError(400);return;}
        long id=0;
        try{
            if(!"crear".equals(accion))id=Entrada.id(r,"id");
            if("borrar".equals(accion))dao.eliminar(id);
            else{Servicio e=new Servicio();e.setId(id);if(id>0){Servicio anterior=dao.buscar(id);if(anterior==null){s.sendError(404);return;}e.setCodigo(anterior.getCodigo());}
                if(e.getId()==0)e.setSolicitudId(Entrada.id(r,"solicitudId"));
                dao.guardar(e,e.getId()==0?java.util.List.of():Entrada.ruta(r));
            }
            s.sendRedirect(r.getContextPath()+"/ServicioServlet?ok=1"+("1".equals(r.getParameter("returnDetail"))?"&action=detalle&id="+id:""));
        }catch(SQLException|IllegalArgumentException ex){
            String mensaje=ex instanceof IllegalArgumentException?ex.getMessage():((SQLException)ex).getErrorCode()==1062?"Ya existe un registro con ese correo, documento o código.":((SQLException)ex).getErrorCode()==1451?"No se puede eliminar: existen registros relacionados.":((SQLException)ex).getErrorCode()==1452?"Selecciona valores existentes en los campos relacionados.":"No se pudo guardar la operación. Revisa los datos e inténtalo nuevamente.";
            if(ex instanceof SQLException)log("Error guardando Servicio",ex);
            r.setAttribute("error",mensaje);
            try{if("borrar".equals(accion)){r.setAttribute("lista",dao.listar("","",1,10));r.setAttribute("pagina",1);r.setAttribute("paginas",Math.max(1,(int)Math.ceil(dao.contar("","")/10.0)));r.setAttribute("tamano",10);r.setAttribute("total",dao.contar("",""));r.setAttribute("desde",dao.contar("","")==0?0:1);r.setAttribute("hasta",Math.min(10,dao.contar("","")));r.setAttribute("q","");r.setAttribute("estadoFiltro","");r.getRequestDispatcher("/WEB-INF/views/servicioLista.jsp").forward(r,s);}
                else{Servicio e=id==0?new Servicio():dao.buscar(id);if(e==null){s.sendError(404);return;}formulario(r,s,e);}
            }catch(SQLException fatal){throw new ServletException(fatal);}
        }
    }
    private void formulario(HttpServletRequest r,HttpServletResponse s,Servicio e)throws SQLException,ServletException,IOException{if(e.getId()>0&&!"ACTIVO".equals(e.getEstado()))r.setAttribute("soloLectura",true);r.setAttribute("registro",e);if(e.getId()>0)r.setAttribute("detalleRuta",dao.detalleRuta(e.getId()));new CatalogoDAO().cargar(r);long origen=e.getOrigenId(),destino=e.getDestinoId();
        try{if(r.getParameter("origenId")!=null){origen=Entrada.id(r,"origenId");destino=Entrada.id(r,"destinoId");}}catch(IllegalArgumentException ignored){}
        var rutas=CapacidadDAO.rutas(origen,destino);if(e.getRuta()!=null&&!rutas.containsKey(e.getRuta()))rutas.put(e.getRuta(),"Ruta registrada");r.setAttribute("rutas",rutas);
r.getRequestDispatcher("/WEB-INF/views/servicioFormulario.jsp").forward(r,s);}
}
