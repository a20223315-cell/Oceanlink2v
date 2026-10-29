package pe.edu.pucp.oceanlink;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
@WebServlet("/SegmentoServlet")
public class SegmentoServlet extends HttpServlet {
    private final SegmentoDAO dao=new SegmentoDAO();
    private void preparar(HttpServletRequest r){r.setAttribute("titulo","Segmentos submarinos");r.setAttribute("modulo","Segmento");r.setAttribute("menu","admin");r.setAttribute("rol","Administrador");r.setAttribute("etiquetas",Etiquetas.todas());r.setAttribute("estados",List.of("OPERATIVO","DEGRADADO","EN_MANTENIMIENTO","FUERA_SERVICIO"));}
    protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{preparar(r);String accion=Objects.toString(r.getParameter("action"),"lista");try{
        switch(accion){
            case "lista":
                String q=Entrada.texto(r,"q",160,false),estado=Entrada.texto(r,"estado",40,false);int tamano=Entrada.tamano(r),total=dao.contar(q,estado),paginas=Math.max(1,(int)Math.ceil((double)total/tamano)),pagina=Math.min(Entrada.pagina(r),paginas);
                r.setAttribute("lista",dao.listar(q,estado,pagina,tamano,r.getParameter("orden"),r.getParameter("direccion")));r.setAttribute("pagina",pagina);r.setAttribute("paginas",paginas);r.setAttribute("tamano",tamano);r.setAttribute("total",total);r.setAttribute("desde",total==0?0:(pagina-1)*tamano+1);r.setAttribute("hasta",Math.min(pagina*tamano,total));r.setAttribute("q",q);r.setAttribute("estadoFiltro",estado);
                r.getRequestDispatcher("/WEB-INF/views/segmentoLista.jsp").forward(r,s);break;
            case "formCrear":case "editar":case "detalle":
                Segmento e;if("formCrear".equals(accion)){e=new Segmento();e.setEstado(1);e.setCondicion("OPERATIVO");}else{e=dao.buscar(Entrada.id(r,"id"));if(e==null){s.sendError(404);return;}}
                r.setAttribute("soloLectura","detalle".equals(accion));formulario(r,s,e);break;
            default:s.sendError(400);
        }
    }catch(IllegalArgumentException ex){s.sendError(400,ex.getMessage());}catch(SQLException ex){log("Error consultando Segmento",ex);r.setAttribute("error","No se pudo consultar la información. Revisa la conexión a la base de datos.");r.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(r,s);}}
    protected void doPost(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{r.setCharacterEncoding("UTF-8");preparar(r);String accion=r.getParameter("action");
        if(!Set.of("crear","actualizar","borrar").contains(Objects.toString(accion,""))){s.sendError(400);return;}
        long id=0;
        try{
            if(!"crear".equals(accion))id=Entrada.id(r,"id");
            if("borrar".equals(accion))dao.eliminar(id);
            else{Segmento e=new Segmento();e.setId(id);if(id>0){Segmento anterior=dao.buscar(id);if(anterior==null){s.sendError(404);return;}e.setCodigo(anterior.getCodigo());}
                e.setOrigenId(Entrada.id(r,"origenId"));e.setDestinoId(Entrada.id(r,"destinoId"));e.setCapacidad(Entrada.decimal(r,"capacidad",10,4,"La capacidad total del segmento"));e.setCondicion(Entrada.opcion(r,"condicion","OPERATIVO","DEGRADADO","EN_MANTENIMIENTO","FUERA_SERVICIO"));e.setEstado(Entrada.estado(r));
                dao.guardar(e);
            }
            s.sendRedirect(r.getContextPath()+"/SegmentoServlet?ok=1");
        }catch(SQLException|IllegalArgumentException ex){
            String mensaje=ex instanceof IllegalArgumentException?ex.getMessage():((SQLException)ex).getErrorCode()==1062?"Ya existe un registro con ese correo, documento o código.":((SQLException)ex).getErrorCode()==1451?"No se puede eliminar: existen registros relacionados.":((SQLException)ex).getErrorCode()==1452?"Selecciona valores existentes en los campos relacionados.":"No se pudo guardar la operación. Revisa los datos e inténtalo nuevamente.";
            if(ex instanceof SQLException)log("Error guardando Segmento",ex);
            r.setAttribute("error",mensaje);
            try{if("borrar".equals(accion)){r.setAttribute("lista",dao.listar("","",1,10));r.setAttribute("pagina",1);r.setAttribute("paginas",Math.max(1,(int)Math.ceil(dao.contar("","")/10.0)));r.setAttribute("tamano",10);r.setAttribute("total",dao.contar("",""));r.setAttribute("desde",dao.contar("","")==0?0:1);r.setAttribute("hasta",Math.min(10,dao.contar("","")));r.setAttribute("q","");r.setAttribute("estadoFiltro","");r.getRequestDispatcher("/WEB-INF/views/segmentoLista.jsp").forward(r,s);}
                else{Segmento e=id==0?new Segmento():dao.buscar(id);if(e==null){s.sendError(404);return;}formulario(r,s,e);}
            }catch(SQLException fatal){throw new ServletException(fatal);}
        }
    }
    private void formulario(HttpServletRequest r,HttpServletResponse s,Segmento e)throws SQLException,ServletException,IOException{r.setAttribute("registro",e);new CatalogoDAO().cargar(r);r.getRequestDispatcher("/WEB-INF/views/segmentoFormulario.jsp").forward(r,s);}
}
