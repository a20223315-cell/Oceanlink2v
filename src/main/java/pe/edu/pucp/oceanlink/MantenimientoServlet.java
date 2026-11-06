package pe.edu.pucp.oceanlink;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
@WebServlet("/MantenimientoServlet")
public class MantenimientoServlet extends HttpServlet {
    private final MantenimientoDAO dao=new MantenimientoDAO();
    private void preparar(HttpServletRequest r){r.setAttribute("titulo","Gestión de mantenimiento");r.setAttribute("modulo","Mantenimiento");r.setAttribute("menu","mc");r.setAttribute("rol","Coordinador de mantenimiento");r.setAttribute("etiquetas",Etiquetas.todas());r.setAttribute("estados",List.of("PROGRAMADO","EN_PROGRESO","FINALIZADO"));}
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
                r.getRequestDispatcher("/WEB-INF/views/mantenimientoLista.jsp").forward(r,s);break;
            case "formCrear":case "editar":case "detalle":
                Mantenimiento e;if("formCrear".equals(accion)){e=new Mantenimiento();e.setFase("PROGRAMADO");}else{e=dao.buscar(Entrada.id(r,"id"));if(e==null){s.sendError(404);return;}}
                r.setAttribute("soloLectura","detalle".equals(accion));formulario(r,s,e);break;
            default:s.sendError(400);
        }
    }catch(IllegalArgumentException ex){s.sendError(400,ex.getMessage());}catch(SQLException ex){log("Error consultando Mantenimiento",ex);r.setAttribute("error","No se pudo consultar la información. Revisa la conexión a la base de datos.");r.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(r,s);}}
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
            else{Mantenimiento e=new Mantenimiento();e.setId(id);if(id>0){Mantenimiento anterior=dao.buscar(id);if(anterior==null){s.sendError(404);return;}e.setCodigo(anterior.getCodigo());}
                e.setTipoId(Entrada.id(r,"tipoId"));e.setEstacionId(Entrada.opcional(r,"estacionId"));e.setSegmentoId(Entrada.opcional(r,"segmentoId"));e.setFase(Entrada.opcion(r,"fase","PROGRAMADO","EN_PROGRESO","FINALIZADO"));e.setInicio(Entrada.fecha(r,"inicio",true));e.setFin(Entrada.fecha(r,"fin",true));e.setInicioReal(Entrada.fecha(r,"inicioReal",false));e.setCierre(Entrada.fecha(r,"cierre",false));e.setDescripcion(Entrada.texto(r,"descripcion",2000,true));e.setResultado(Entrada.texto(r,"resultado",2000,false));e.setCreadoPor(Entrada.id(r,"creadoPor"));
                dao.guardar(e,Entrada.texto(r,"actividad",2000,false),Entrada.id(r,"creadoPor"));
            }
            s.sendRedirect(r.getContextPath()+"/MantenimientoServlet?ok=1");
        }catch(SQLException|IllegalArgumentException ex){
            String mensaje=ex instanceof IllegalArgumentException?ex.getMessage():((SQLException)ex).getErrorCode()==1062?"Ya existe un registro con ese correo, documento o código.":((SQLException)ex).getErrorCode()==1451?"No se puede eliminar: existen registros relacionados.":((SQLException)ex).getErrorCode()==1452?"Selecciona valores existentes en los campos relacionados.":"No se pudo guardar la operación. Revisa los datos e inténtalo nuevamente.";
            if(ex instanceof SQLException)log("Error guardando Mantenimiento",ex);
            r.setAttribute("error",mensaje);
            try{if("borrar".equals(accion)){r.setAttribute("lista",dao.listar("","",1,10));r.setAttribute("pagina",1);r.setAttribute("paginas",Math.max(1,(int)Math.ceil(dao.contar("","")/10.0)));r.setAttribute("tamano",10);r.setAttribute("total",dao.contar("",""));r.setAttribute("desde",dao.contar("","")==0?0:1);r.setAttribute("hasta",Math.min(10,dao.contar("","")));r.setAttribute("q","");r.setAttribute("estadoFiltro","");r.getRequestDispatcher("/WEB-INF/views/mantenimientoLista.jsp").forward(r,s);}
                else{Mantenimiento e=id==0?new Mantenimiento():dao.buscar(id);if(e==null){s.sendError(404);return;}formulario(r,s,e);}
            }catch(SQLException fatal){throw new ServletException(fatal);}
        }
    }
    private void formulario(HttpServletRequest r,HttpServletResponse s,Mantenimiento e)throws SQLException,ServletException,IOException{String faseActual=e.getFase(); if(faseActual==null)faseActual="PROGRAMADO";r.setAttribute("permitidos",e.getId()==0?List.of("PROGRAMADO") : switch(faseActual){case "PROGRAMADO" -> List.of("PROGRAMADO","EN_PROGRESO");case "EN_PROGRESO" -> List.of("EN_PROGRESO","FINALIZADO");case "FINALIZADO" -> List.of("FINALIZADO");default -> List.of(faseActual);});if(Set.of("FINALIZADO").contains(faseActual))r.setAttribute("soloLectura",true);r.setAttribute("registro",e);new CatalogoDAO().cargar(r);if(e.getId()>0)r.setAttribute("bitacora",dao.actividades(e.getId()));long estacion=e.getEstacionId(),segmento=e.getSegmentoId();try{if(r.getParameter("estacionId")!=null){estacion=Entrada.opcional(r,"estacionId");segmento=Entrada.opcional(r,"segmentoId");}}catch(IllegalArgumentException ignored){}r.setAttribute("afectados",dao.afectados(estacion,segmento));r.getRequestDispatcher("/WEB-INF/views/mantenimientoFormulario.jsp").forward(r,s);}
}
