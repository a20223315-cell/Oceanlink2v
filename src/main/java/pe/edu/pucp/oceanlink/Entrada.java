package pe.edu.pucp.oceanlink;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
public final class Entrada {
    private Entrada(){}
    public static String etiqueta(String n){return switch(n){
        case "capacidad"->"La capacidad";case "nombre"->"El nombre";case "apellidos"->"Los apellidos";case "nombres"->"Los nombres";
        case "correo"->"El correo electrónico";case "telefono"->"El teléfono";case "origenId"->"la estación de origen";case "destinoId"->"la estación de destino";
        case "clienteId"->"el cliente";case "paisId"->"el país";case "rolId"->"el rol";case "creadoPor"->"el responsable";
        case "ruta"->"La ruta";case "descripcion"->"La descripción";case "observacion"->"La observación";case "resultado"->"El resultado";
        case "contacto"->"El contacto";case "documento"->"El documento";case "tipoDocumento"->"El tipo de documento";case "ubicacion"->"La ubicación";
        case "tipoId"->"el tipo de mantenimiento";case "segmentoId"->"el segmento";case "estacionId"->"la estación";case "inicioReal"->"La fecha de inicio real";case "actividad"->"La actividad";case "estado"->"el estado";case "fase"->"la fase";case "severidad"->"la severidad";case "condicion"->"la condición operativa";case "inicio"->"La fecha de inicio";case "fin"->"La fecha de fin";case "deteccion"->"La fecha de detección";case "restauracion"->"La fecha de restauración";case "cierre"->"La fecha de cierre";
        default->n;};}
    public static String texto(HttpServletRequest r,String n,int max,boolean obligatorio){
        String v=Objects.toString(r.getParameter(n),"").trim();
        if(obligatorio&&v.isBlank())throw new IllegalArgumentException("Completa el campo: "+etiqueta(n)+".");if(v.length()>max)throw new IllegalArgumentException(etiqueta(n)+" no puede superar "+max+" caracteres.");return v;
    }
    public static long id(HttpServletRequest r,String n){try{long v=Long.parseLong(r.getParameter(n));if(v>0)return v;}catch(Exception ignored){}throw new IllegalArgumentException("Selecciona un valor válido para "+etiqueta(n)+".");}
    public static long opcional(HttpServletRequest r,String n){return texto(r,n,20,false).isBlank()?0:id(r,n);}
    public static int estado(HttpServletRequest r){String v=texto(r,"estado",1,true);if(!Set.of("0","1").contains(v))throw new IllegalArgumentException("Estado inválido.");return Integer.parseInt(v);}
    public static BigDecimal decimal(HttpServletRequest r,String n,int enteros,int escala){return decimal(r,n,enteros,escala,"La capacidad");}
    public static BigDecimal decimal(HttpServletRequest r,String n,int enteros,int escala,String campo){
        String texto=Objects.toString(r.getParameter(n),"").trim();
        if(texto.isEmpty())throw new IllegalArgumentException(campo+" es obligatoria.");
        BigDecimal v;try{v=new BigDecimal(texto);}catch(NumberFormatException ex){throw new IllegalArgumentException(campo+" debe ser un número válido.");}
        if(v.signum()<=0)throw new IllegalArgumentException(campo+" debe ser mayor que 0.");
        if(Math.max(0,v.scale())>escala)throw new IllegalArgumentException(campo+" admite hasta "+escala+" decimales.");
        if((long)v.precision()-v.scale()>enteros)throw new IllegalArgumentException(campo+" no puede superar "+enteros+" dígitos enteros.");
        return v;
    }

    public static String opcion(HttpServletRequest r,String n,String... opciones){String v=texto(r,n,60,true);if(!Arrays.asList(opciones).contains(v))throw new IllegalArgumentException("Selecciona una opción válida para "+etiqueta(n)+".");return v;}
    public static LocalDateTime fecha(HttpServletRequest r,String n,boolean obligatorio){String v=texto(r,n,30,obligatorio);if(v.isEmpty())return null;try{return LocalDateTime.parse(v);}catch(DateTimeParseException ex){throw new IllegalArgumentException(etiqueta(n)+" debe tener una fecha y hora válidas.");}}
    public static List<Long> ids(HttpServletRequest r,String n){String[] values=r.getParameterValues(n);if(values==null||values.length==0)throw new IllegalArgumentException("Selecciona al menos un segmento.");List<Long> out=new ArrayList<>();for(String s:values){try{long id=Long.parseLong(s);if(id<=0||out.contains(id))throw new NumberFormatException();out.add(id);}catch(NumberFormatException e){throw new IllegalArgumentException("Segmentos inválidos.");}}return out;}
    public static List<Long> ruta(HttpServletRequest r){String v=texto(r,"ruta",2000,true);List<Long> out=new ArrayList<>();for(String s:v.split(",")){try{long id=Long.parseLong(s);if(id<=0||out.contains(id))throw new NumberFormatException();out.add(id);}catch(NumberFormatException e){throw new IllegalArgumentException("Selecciona una ruta válida.");}}return out;}
    public static String unir(List<Long> ids){return String.join(",",ids.stream().map(String::valueOf).toList());}
    public static int pagina(HttpServletRequest r){try{return Math.max(1,Integer.parseInt(r.getParameter("pagina")));}catch(Exception e){return 1;}}
    public static int tamano(HttpServletRequest r){String v=r.getParameter("tamano");return "25".equals(v)?25:"50".equals(v)?50:10;}
}
