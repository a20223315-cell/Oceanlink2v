package pe.edu.pucp.oceanlink;
import jakarta.servlet.http.*;import java.sql.*;import java.util.*;import java.math.*;
public final class SolicitudPresentacion{
 public static void cargar(HttpServletRequest r,Solicitud e)throws SQLException{
  r.setAttribute("pasosSolicitud",List.of("REGISTRADA","EN_EVALUACION","APROBADA","PROVISIONADA","ACTIVA"));List<String[]> rows=new ArrayList<>();BigDecimal limite=null;boolean operativa=true;
  if(e.getRuta()!=null&&!e.getRuta().isBlank())try(Connection c=Conexion.abrir()){int orden=0;for(String parte:e.getRuta().split(",")){long id=Long.parseLong(parte);try(PreparedStatement ps=c.prepareStatement("SELECT s.codigo_segmento,a.nombre,b.nombre,s.capacidad_total_tbps,s.condicion_operativa FROM segmento s JOIN estacion a ON a.id_estacion=s.id_estacion_origen JOIN estacion b ON b.id_estacion=s.id_estacion_destino WHERE s.id_segmento=?")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){if(rs.next()){BigDecimal disponible=rs.getBigDecimal(4).subtract(CapacidadDAO.ocupada(c,id,e.getId(),0));limite=limite==null?disponible:limite.min(disponible);operativa&=!"FUERA_SERVICIO".equals(rs.getString(5));rows.add(new String[]{Integer.toString(++orden),rs.getString(1),rs.getString(2)+" - "+rs.getString(3),disponible.stripTrailingZeros().toPlainString(),Etiquetas.todas().get(rs.getString(5))});}}}}}
  r.setAttribute("rutaEvaluada",rows);r.setAttribute("viabilidadOk",limite!=null&&operativa&&e.getCapacidad()!=null&&limite.compareTo(e.getCapacidad())>=0);r.setAttribute("limiteRuta",limite==null?"Sin ruta":limite.stripTrailingZeros().toPlainString()+" Tbps");
 }
}
