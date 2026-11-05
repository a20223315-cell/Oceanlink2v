package pe.edu.pucp.oceanlink;

import jakarta.servlet.http.*;
import java.sql.*;
import java.util.*;

public final class Pantallas {
    private Pantallas(){}
    public static void perfil(HttpServletRequest r){
        r.setAttribute("paisesTelefono",Paises.TODOS.stream().filter(p->!p.prefijo().isEmpty()).toList());
        HttpSession session = r.getSession(false);
        if (session == null || session.getAttribute("usuarioId") == null) return;
        String codigo = Objects.toString(session.getAttribute("rolCodigo"), "");
        String area = switch (codigo) {
            case "ADMIN" -> "admin";
            case "CAPACITY" -> "cp";
            case "NETWORK" -> "no";
            case "MAINTENANCE" -> "mc";
            case "SUPERVISOR" -> "sup";
            default -> "";
        };
        r.setAttribute("menu", area);
        r.setAttribute("rol", session.getAttribute("rolNombre"));
        r.setAttribute("perfil", new Usuario());
        Usuario perfil = new Usuario();
        perfil.setId((Long) session.getAttribute("usuarioId"));
        perfil.setNombre(Objects.toString(session.getAttribute("usuarioNombre"), ""));
        perfil.setCorreo(Objects.toString(session.getAttribute("usuarioCorreo"), ""));
        r.setAttribute("perfil", perfil);
    }
    public static String preparar(HttpServletRequest r,String vista,List<String[]> capacidad)throws SQLException{
        HttpSession session = r.getSession(false);
        String role = session == null ? "" : Objects.toString(session.getAttribute("rolCodigo"), "");
        String area = switch (role) { case "ADMIN" -> "admin"; case "CAPACITY" -> "cp"; case "NETWORK" -> "no"; case "MAINTENANCE" -> "mc"; case "SUPERVISOR" -> "sup"; default -> ""; };
        r.setAttribute("menu", area);
        r.setAttribute("rol", session == null ? "" : session.getAttribute("rolNombre"));
        ResumenPantallas.cargar(r,vista,capacidad);
        boolean sup="SUPERVISOR".equals(role);
        if(sup){r.setAttribute("menu","sup");r.setAttribute("rol","Supervisor");}
        if(vista.equals("general")&&!sup){
            r.setAttribute("metricas",DetalleDAO.filas("SELECT 'Usuarios activos',COUNT(*) FROM usuario WHERE estado=1 UNION ALL SELECT 'Estaciones registradas',COUNT(*) FROM estacion UNION ALL SELECT 'Segmentos operativos',COUNT(*) FROM segmento WHERE estado=1 AND condicion_operativa='OPERATIVO' UNION ALL SELECT 'Incidencias activas',COUNT(*) FROM incidencia WHERE fase NOT IN ('CERRADA','RESTAURADA')"));
            r.setAttribute("infraestructura",DetalleDAO.filas("SELECT 'Segmento',codigo_segmento,condicion_operativa,DATE_FORMAT(fecha_actualizacion,'%d/%m/%Y') FROM segmento ORDER BY id_segmento"));return "panelAdmin";
        }
        if(vista.equals("general")){
            int periodo=Set.of("7","30","90").contains(Objects.toString(r.getParameter("periodo"),""))?Integer.parseInt(r.getParameter("periodo")):30;
            long segmento=Entrada.opcional(r,"segmento");r.setAttribute("periodo",periodo);r.setAttribute("alcances",DetalleDAO.filas("SELECT id_segmento,codigo_segmento FROM segmento WHERE estado=1 ORDER BY id_segmento"));
            List<String[]> priority=new ArrayList<>();double average=0;int count=0;
            List<String[]> ids=DetalleDAO.filas("SELECT id_segmento,codigo_segmento FROM segmento WHERE estado=1 ORDER BY id_segmento");
            for(String[] row:capacidad){if(segmento>0&&ids.stream().noneMatch(a->a[0].equals(Long.toString(segmento))&&a[1].equals(row[0])))continue;double p=Double.parseDouble(row[7]);average+=p;count++;priority.add(new String[]{row[0],row[1]+" - "+row[2],String.format(Locale.ROOT,"%.1f",p),p>95?"critical":p>=80?"high":"",p>95?"danger":p>=80?"warning":"ok",p>95?"Crítica":p>=80?"Alta":"Normal"});}
            priority.sort((a,b)->Double.compare(Double.parseDouble(b[2]),Double.parseDouble(a[2])));r.setAttribute("prioridad",priority.stream().limit(3).toList());
            String incidentWhere=" FROM incidencia i WHERE i.fase NOT IN ('CERRADA','RESTAURADA') AND i.fecha_deteccion>=DATE_SUB(CURRENT_TIMESTAMP,INTERVAL ? DAY) AND (?=0 OR EXISTS(SELECT 1 FROM incidencia_segmento x WHERE x.id_incidencia=i.id_incidencia AND x.id_segmento=?))";
            List<String[]> alerts=DetalleDAO.filas("SELECT i.id_incidencia,i.codigo_incidencia,i.severidad,i.fase"+incidentWhere+" ORDER BY i.fecha_actualizacion DESC",periodo,segmento,segmento);r.setAttribute("alertas",alerts);
            List<String[]> affected=DetalleDAO.filas("SELECT COUNT(DISTINCT s.id_servicio) FROM servicio s JOIN servicio_segmento ss ON ss.id_servicio=s.id_servicio JOIN incidencia_segmento ins ON ins.id_segmento=ss.id_segmento JOIN incidencia i ON i.id_incidencia=ins.id_incidencia WHERE s.estado='ACTIVO' AND i.fase NOT IN ('CERRADA','RESTAURADA') AND i.fecha_deteccion>=DATE_SUB(CURRENT_TIMESTAMP,INTERVAL ? DAY) AND (?=0 OR ss.id_segmento=?)",periodo,segmento,segmento);
            List<String[]> maint=DetalleDAO.filas("SELECT COUNT(*) FROM mantenimiento m WHERE m.fase<>'FINALIZADO' AND m.inicio_programado>=DATE_SUB(CURRENT_TIMESTAMP,INTERVAL ? DAY) AND (?=0 OR m.id_segmento=? OR EXISTS(SELECT 1 FROM segmento s WHERE s.id_segmento=? AND m.id_estacion IN (s.id_estacion_origen,s.id_estacion_destino)))",periodo,segmento,segmento,segmento);
            r.setAttribute("kpis",List.of(String.format(Locale.ROOT,"%.1f%%",count==0?0:average/count),Integer.toString(alerts.size()),affected.get(0)[0],maint.get(0)[0]));r.setAttribute("titulo","Panel de indicadores");return "panelSupervisor";
        }
        List<List<String[]>> tablas=new ArrayList<>();String target;
        if(vista.equals("capacidad")||vista.equals("red")){
            List<String[]> ids=DetalleDAO.filas("SELECT id_segmento,codigo_segmento FROM segmento WHERE estado=1 ORDER BY id_segmento");List<String[]> rows=new ArrayList<>();
            for(String[] c:capacidad){String id=ids.stream().filter(x->x[1].equals(c[0])).findFirst().orElseThrow()[0];String link="LINK:ConsultaDetalleServlet?id="+id+"&area="+(sup?"sup":vista.equals("red")?"no":"cp");rows.add(sup||vista.equals("red")?new String[]{c[0],c[1]+" - "+c[2],c[7],c[8],link}:new String[]{c[0],c[3],c[4],c[5],c[6],c[7],link});}
            tablas.add(rows);target=sup?"sup_network_capacity":vista.equals("red")?"no_network":"cp_capacity";
        }else if(vista.equals("incidencias")){
            tablas.add(DetalleDAO.filas("SELECT i.codigo_incidencia,COALESCE((SELECT GROUP_CONCAT(s.codigo_segmento SEPARATOR ', ') FROM incidencia_segmento x JOIN segmento s ON s.id_segmento=x.id_segmento WHERE x.id_incidencia=i.id_incidencia),''),i.severidad,i.fase,(SELECT COUNT(DISTINCT ss.id_servicio) FROM incidencia_segmento x JOIN servicio_segmento ss ON ss.id_segmento=x.id_segmento JOIN servicio v ON v.id_servicio=ss.id_servicio WHERE x.id_incidencia=i.id_incidencia AND v.estado='ACTIVO'),DATE_FORMAT(i.fecha_actualizacion,'%d/%m/%Y %H:%i') FROM incidencia i WHERE i.fase NOT IN ('CERRADA','RESTAURADA') ORDER BY i.id_incidencia"));target="sup_incidents";
        }else if(vista.equals("afectados")){
            tablas.add(DetalleDAO.filas("SELECT DISTINCT i.codigo_incidencia,seg.codigo_segmento,s.codigo_servicio,cl.razon_social,s.capacidad_contratada_tbps,i.severidad FROM incidencia i JOIN incidencia_segmento ins ON ins.id_incidencia=i.id_incidencia JOIN segmento seg ON seg.id_segmento=ins.id_segmento JOIN servicio_segmento ss ON ss.id_segmento=seg.id_segmento JOIN servicio s ON s.id_servicio=ss.id_servicio JOIN solicitud_capacidad sol ON sol.id_solicitud=s.id_solicitud JOIN cliente cl ON cl.id_cliente=sol.id_cliente WHERE i.fase NOT IN ('CERRADA','RESTAURADA') AND s.estado='ACTIVO' ORDER BY i.codigo_incidencia"));target="sup_affected_services";
        }else if(vista.equals("mantenimientos")){
            tablas.add(DetalleDAO.filas("SELECT m.codigo_mantenimiento,t.nombre,COALESCE(e.codigo_estacion,s.codigo_segmento),CONCAT(DATE_FORMAT(m.inicio_programado,'%d/%m/%Y %H:%i'),' - ',DATE_FORMAT(m.fin_programado,'%d/%m/%Y %H:%i')),(SELECT COUNT(DISTINCT ss.id_servicio) FROM servicio_segmento ss JOIN servicio v ON v.id_servicio=ss.id_servicio JOIN segmento z ON z.id_segmento=ss.id_segmento WHERE v.estado='ACTIVO' AND (ss.id_segmento=m.id_segmento OR m.id_estacion IN(z.id_estacion_origen,z.id_estacion_destino))),m.fase FROM mantenimiento m JOIN tipo_mantenimiento t ON t.id_tipo_mantenimiento=m.id_tipo_mantenimiento LEFT JOIN estacion e ON e.id_estacion=m.id_estacion LEFT JOIN segmento s ON s.id_segmento=m.id_segmento WHERE m.fase<>'FINALIZADO' ORDER BY m.inicio_programado"));tablas.add(DetalleDAO.filas("SELECT m.codigo_mantenimiento,COALESCE(e.codigo_estacion,s.codigo_segmento),DATE_FORMAT(m.fecha_cierre,'%d/%m/%Y'),m.resultado FROM mantenimiento m LEFT JOIN estacion e ON e.id_estacion=m.id_estacion LEFT JOIN segmento s ON s.id_segmento=m.id_segmento WHERE m.fase='FINALIZADO' ORDER BY m.fecha_cierre DESC LIMIT 10"));target="sup_maintenance";
        }else if(vista.equals("historicos")){
            tablas.add(DetalleDAO.filas("SELECT i.codigo_incidencia,COALESCE((SELECT GROUP_CONCAT(s.codigo_segmento) FROM incidencia_segmento x JOIN segmento s ON s.id_segmento=x.id_segmento WHERE x.id_incidencia=i.id_incidencia),''),i.severidad,DATE_FORMAT(i.fecha_cierre,'%d/%m/%Y %H:%i'),i.resultado,CONCAT('LINK:IncidenciaServlet?action=detalle&id=',i.id_incidencia) FROM incidencia i WHERE i.fase='CERRADA' ORDER BY i.fecha_cierre DESC"));
            tablas.add(DetalleDAO.filas("SELECT m.codigo_mantenimiento,t.nombre,COALESCE(e.codigo_estacion,s.codigo_segmento),DATE_FORMAT(m.fecha_cierre,'%d/%m/%Y %H:%i'),m.resultado,CONCAT('LINK:MantenimientoServlet?action=detalle&id=',m.id_mantenimiento) FROM mantenimiento m JOIN tipo_mantenimiento t ON t.id_tipo_mantenimiento=m.id_tipo_mantenimiento LEFT JOIN estacion e ON e.id_estacion=m.id_estacion LEFT JOIN segmento s ON s.id_segmento=m.id_segmento WHERE m.fase='FINALIZADO' ORDER BY m.fecha_cierre DESC"));target="sup_history";
        }else if(vista.equals("reportes")){
            tablas.add(DetalleDAO.filas("SELECT r.codigo_reporte,COALESCE(s.codigo_segmento,'Toda la red'),CONCAT(DATE_FORMAT(r.fecha_inicio,'%d/%m/%Y'),' - ',DATE_FORMAT(r.fecha_fin,'%d/%m/%Y')),u.nombre_completo,DATE_FORMAT(r.generado_en,'%d/%m/%Y %H:%i'),CONCAT('LINK:ReporteServlet?id=',r.id_reporte) FROM reporte_generado r JOIN usuario u ON u.id_usuario=r.generado_por LEFT JOIN segmento s ON s.id_segmento=r.id_segmento ORDER BY r.generado_en DESC"));target="sup_reports";
        }else return "consulta";
        Map<Object,String> etiquetas=Etiquetas.todas();for(List<String[]> table:tablas)for(String[] row:table)for(int i=0;i<row.length;i++)if(etiquetas.containsKey(row[i]))row[i]=etiquetas.get(row[i]);
        r.setAttribute("tablas",tablas);return target;
    }
}
