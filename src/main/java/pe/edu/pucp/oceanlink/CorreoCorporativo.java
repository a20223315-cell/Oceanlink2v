package pe.edu.pucp.oceanlink;
import java.sql.*;
import java.text.Normalizer;
import java.util.*;
public final class CorreoCorporativo {
 private CorreoCorporativo(){}
 private static String limpiar(String v){return Normalizer.normalize(v,Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]","");}
 public static String[] partes(String nombres,String apellidos){
  String nombre=limpiar(Objects.toString(nombres,"").trim().split("\\s+")[0]);
  String[] palabras=Objects.toString(apellidos,"").trim().split("\\s+");StringBuilder apellido=new StringBuilder();
  for(String palabra:palabras){String parte=limpiar(palabra);apellido.append(parte);if(!Set.of("de","del","la","las","los").contains(parte))break;}
  if(nombre.isEmpty()||apellido.isEmpty())throw new IllegalArgumentException("Completa nombres y apellidos con letras para generar el correo corporativo.");
  return new String[]{nombre,apellido.toString()};
 }
 private static String limitar(String base,String sufijo){return base.substring(0,Math.min(base.length(),64-sufijo.length()))+sufijo+"@oceanlink.pe";}
 private static boolean existe(Connection c,String correo)throws SQLException{try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM usuario WHERE correo=? LIMIT 1")){p.setString(1,correo);try(ResultSet r=p.executeQuery()){return r.next();}}}
 public static String generar(Connection c,String nombres,String apellidos)throws SQLException{
  String[] p=partes(nombres,apellidos);String base=p[0]+p[1];Set<String> vistos=new HashSet<>();
  for(int n=1;n<=p[0].length();n++){String candidato=limitar(p[0].substring(0,n)+p[1],"");if(vistos.add(candidato)&&!existe(c,candidato))return candidato;}
  for(int n=2;n<1000000;n++){String candidato=limitar(base,String.valueOf(n));if(!existe(c,candidato))return candidato;}
  throw new IllegalArgumentException("No se pudo generar un correo disponible. Contacta al administrador.");
 }
}
