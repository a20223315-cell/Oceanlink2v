package pe.edu.pucp.oceanlink;
import java.util.*;
public final class Paises {
 public record Pais(String codigo,String nombre,String prefijo){}
 public static final List<Pais> TODOS=List.of(
  new Pais("CL","Chile","+56"),
  new Pais("CO","Colombia","+57"),
  new Pais("PA","Panamá","+507"),
  new Pais("PE","Perú","+51")
 );
 public static String codigo(String nombre){return TODOS.stream().filter(p->p.nombre().equals(nombre)).map(Pais::codigo).findFirst().orElse("");}
 public static void validarNombre(String valor,String campo){if(valor==null||!valor.matches("[\\p{L}\\p{M}]+(?:[ '\u2019-][\\p{L}\\p{M}]+)*"))throw new IllegalArgumentException("Ingrese "+campo+" sin números ni símbolos inválidos.");}
}
