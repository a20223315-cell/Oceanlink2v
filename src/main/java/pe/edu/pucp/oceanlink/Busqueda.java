package pe.edu.pucp.oceanlink;
public final class Busqueda{
 private Busqueda(){}
 private static String literal(String q){if(q.isBlank())return ".*";StringBuilder out=new StringBuilder();for(char c:q.toCharArray()){if("\\.^$|?*+()[]{}".indexOf(c)>=0)out.append('\\');out.append(c);}return out.toString();}
 public static String codigo(String q){return q.matches("[0-9]+")?"-0*"+q.replaceFirst("^0+(?!$)","")+"$":literal(q);}
 public static String texto(String q){return q.matches("[0-9]+")?"a^":literal(q);}
}
