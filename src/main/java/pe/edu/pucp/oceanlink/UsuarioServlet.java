package pe.edu.pucp.oceanlink;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
@WebServlet("/UsuarioServlet")
public class UsuarioServlet extends HttpServlet {
    private final UsuarioDAO dao=new UsuarioDAO();
    private void preparar(HttpServletRequest r){r.setAttribute("titulo","Usuarios");r.setAttribute("modulo","Usuario");r.setAttribute("menu","admin");r.setAttribute("rol","Administrador");r.setAttribute("etiquetas",Etiquetas.todas());r.setAttribute("estados",List.of("1","0","pendiente"));}
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
                String rolFiltro=Entrada.texto(r,"rolFiltro",20,false);r.setAttribute("rolFiltro",rolFiltro);r.setAttribute("roles",new CatalogoDAO().roles());
                String q=Entrada.texto(r,"q",160,false),estado=Entrada.texto(r,"estado",40,false);int tamano=Entrada.tamano(r),total=dao.contar(q,estado,rolFiltro),paginas=Math.max(1,(int)Math.ceil((double)total/tamano)),pagina=Math.min(Entrada.pagina(r),paginas);
                r.setAttribute("lista",dao.listar(q,estado,pagina,tamano,r.getParameter("orden"),r.getParameter("direccion"),rolFiltro));r.setAttribute("pagina",pagina);r.setAttribute("paginas",paginas);r.setAttribute("tamano",tamano);r.setAttribute("total",total);r.setAttribute("desde",total==0?0:(pagina-1)*tamano+1);r.setAttribute("hasta",Math.min(pagina*tamano,total));r.setAttribute("q",q);r.setAttribute("estadoFiltro",estado);
                r.getRequestDispatcher("/WEB-INF/views/usuarioLista.jsp").forward(r,s);break;
            case "formCrear":case "editar":case "detalle":
                Usuario e;if("formCrear".equals(accion)){e=new Usuario();e.setEstado(0);e.setFechaIngreso(java.time.LocalDateTime.now(java.time.ZoneId.of("America/Lima")).withSecond(0).withNano(0));}else{e=dao.buscar(Entrada.id(r,"id"));if(e==null){s.sendError(404);return;}}
                r.setAttribute("soloLectura","detalle".equals(accion));formulario(r,s,e);break;
            default:s.sendError(400);
        }
    }catch(IllegalArgumentException ex){s.sendError(400,ex.getMessage());}catch(SQLException ex){log("Error consultando Usuario",ex);r.setAttribute("error","No se pudo consultar la información. Revisa la conexión a la base de datos.");r.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(r,s);}}
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
            else{Usuario e=new Usuario();e.setId(id);if(id>0){Usuario anterior=dao.buscar(id);if(anterior==null){s.sendError(404);return;}e.setCodigo(anterior.getCodigo());}
                String apellidos=Entrada.texto(r,"apellidos",100,true),nombres=Entrada.texto(r,"nombres",57,true);Paises.validarNombre(apellidos,"apellidos");Paises.validarNombre(nombres,"nombres");if(apellidos.contains(",")||nombres.contains(","))throw new IllegalArgumentException("Escribe los apellidos y nombres en sus campos, sin comas.");e.setNombre(apellidos+", "+nombres);e.setCorreo(id==0?null:dao.buscar(id).getCorreo());e.setTelefono(Entrada.texto(r,"telefono",30,false));e.setRolId(Entrada.id(r,"rolId"));e.setPaisId(Entrada.id(r,"paisId"));e.setEstado(id==0?0:dao.buscar(id).getEstado());if(id==0){java.time.LocalDate hoy=java.time.LocalDate.now(java.time.ZoneId.of("America/Lima"));String fecha=Entrada.texto(r,"fechaIngreso",10,false);java.time.LocalDate ingreso;try{ingreso=fecha.isEmpty()?hoy:java.time.LocalDate.parse(fecha);}catch(java.time.format.DateTimeParseException ex){throw new IllegalArgumentException("Selecciona una fecha de ingreso válida.");}if(ingreso.isAfter(hoy))throw new IllegalArgumentException("La fecha de ingreso no puede ser posterior a hoy.");e.setFechaIngreso(ingreso.atStartOfDay());}else e.setFechaIngreso(dao.buscar(id).getFechaIngreso());
                e.setTelefonoRegion(Entrada.texto(r,"telefonoRegion",2,true));e.setTelefono(Telefono.normalizarRegion(e.getTelefono(),e.getTelefonoRegion()));
                String contrasenaTemporal = "";
                if (id == 0) {
                    contrasenaTemporal = Entrada.texto(r, "contrasenaTemporal", 100, true);
                    if (contrasenaTemporal.length() < 8)
                        throw new IllegalArgumentException("La contraseña temporal debe tener al menos 8 caracteres.");
                    e.setEstado(1);
                }
                dao.guardar(e, contrasenaTemporal);
            }
            s.sendRedirect(r.getContextPath()+"/UsuarioServlet?ok=1");
        }catch(SQLException|IllegalArgumentException ex){
            String mensaje=ex instanceof IllegalArgumentException?ex.getMessage():((SQLException)ex).getErrorCode()==1062?"Ya existe un registro con ese correo, documento o código.":((SQLException)ex).getErrorCode()==1451?"No se puede eliminar: existen registros relacionados.":((SQLException)ex).getErrorCode()==1452?"Selecciona valores existentes en los campos relacionados.":"No se pudo guardar la operación. Revisa los datos e inténtalo nuevamente.";
            if(ex instanceof SQLException)log("Error guardando Usuario",ex);
            r.setAttribute("error",mensaje);
            try{if("borrar".equals(accion)){r.setAttribute("lista",dao.listar("","",1,10));r.setAttribute("pagina",1);r.setAttribute("paginas",Math.max(1,(int)Math.ceil(dao.contar("","")/10.0)));r.setAttribute("tamano",10);r.setAttribute("total",dao.contar("",""));r.setAttribute("desde",dao.contar("","")==0?0:1);r.setAttribute("hasta",Math.min(10,dao.contar("","")));r.setAttribute("q","");r.setAttribute("estadoFiltro","");r.getRequestDispatcher("/WEB-INF/views/usuarioLista.jsp").forward(r,s);}
                else{Usuario e=id==0?new Usuario():dao.buscar(id);if(e==null){s.sendError(404);return;}formulario(r,s,e);}
            }catch(SQLException fatal){throw new ServletException(fatal);}
        }
    }
    private void formulario(HttpServletRequest r,HttpServletResponse s,Usuario e)throws SQLException,ServletException,IOException{r.setAttribute("paisesTelefono",Paises.TODOS.stream().filter(p->!p.prefijo().isEmpty()).toList());r.setAttribute("hoy",java.time.LocalDate.now(java.time.ZoneId.of("America/Lima")));if(e.getFechaIngreso()==null)e.setFechaIngreso(java.time.LocalDate.now(java.time.ZoneId.of("America/Lima")).atStartOfDay());r.setAttribute("fechaIngresoTexto",e.getFechaIngreso().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));r.setAttribute("registro",e);new CatalogoDAO().cargar(r);r.getRequestDispatcher(Boolean.TRUE.equals(r.getAttribute("soloLectura"))?"/WEB-INF/views/usuarioPerfil.jsp":"/WEB-INF/views/usuarioFormulario.jsp").forward(r,s);}
}
