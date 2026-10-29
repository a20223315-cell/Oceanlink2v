package pe.edu.pucp.oceanlink;

import java.sql.*;

public final class Telefono {
    private Telefono(){}
    private static final com.google.i18n.phonenumbers.PhoneNumberUtil UTIL=com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance();
    public static String region(String telefono,String fallback){
        String text=telefono==null?"":telefono.trim();
        if(!text.startsWith("+"))return fallback;
        return Paises.TODOS.stream().filter(p->text.startsWith(p.prefijo())).map(Paises.Pais::codigo).findFirst().orElse(fallback);
    }
    public static String prefijoRegion(String region){if(region==null||region.isBlank())return "";int code=UTIL.getCountryCodeForRegion(region);return code==0?"":"+"+code;}
    public static String normalizarRegion(String numero,String region){
        if(region==null||!java.util.Set.of("PE","CL","CO","PA").contains(region))throw new IllegalArgumentException("Seleccione el prefijo del celular.");
        int length=switch(region){case "PE","CL"->9;case "CO"->10;default->8;};
        String text=numero==null?"":numero.trim();
        String digits=text.replace(" ","");
        if(!text.matches("[0-9 ]+")||digits.length()!=length)throw new IllegalArgumentException("Ingrese "+length+" dígitos.");
        return prefijoRegion(region)+" "+digits;
    }

    public static String prefijo(String pais){
        return switch(pais==null?"":pais){case "Perú"->"+51";case "Chile"->"+56";case "Colombia"->"+57";case "Panamá"->"+507";default->prefijoRegion(Paises.codigo(pais));};
    }
    public static String prefijoPais(long id)throws SQLException{
        try(Connection c=Conexion.abrir();PreparedStatement ps=c.prepareStatement("SELECT nombre FROM pais WHERE id_pais=?")){
            ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("Selecciona un país válido.");return prefijo(rs.getString(1));}
        }
    }
    public static String local(String telefono,String prefijo){
        String texto=telefono==null?"":telefono.trim();
        return !prefijo.isEmpty()&&texto.startsWith(prefijo)?texto.substring(prefijo.length()).trim():texto;
    }
    public static String normalizar(String telefono,String prefijo){
        String numero=local(telefono,prefijo);
        if(numero.isEmpty())throw new IllegalArgumentException("El teléfono es obligatorio.");
        if(prefijo.isEmpty()||!numero.matches("[0-9 ()-]+"))throw new IllegalArgumentException("Ingresa solo el número local del país registrado.");
        try{if(!UTIL.isValidNumber(UTIL.parse(prefijo+numero,"ZZ")))throw new IllegalArgumentException("Ingrese un teléfono válido para el prefijo seleccionado.");}catch(com.google.i18n.phonenumbers.NumberParseException ex){throw new IllegalArgumentException("Ingrese un teléfono válido.");}
        return prefijo+" "+numero;
    }
}
