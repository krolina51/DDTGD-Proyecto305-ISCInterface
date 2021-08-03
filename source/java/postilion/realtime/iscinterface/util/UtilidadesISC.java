package postilion.realtime.iscinterface.util;

import java.util.HashMap;
import java.util.Map;

public class UtilidadesISC {
	
	private UtilidadesISC() {
		
	}
    
    private static final String REGEX_RESPUESTA      = "(\\w{4})(\\w{12})(\\w{10})(\\w{8})(\\w{4})(\\w{16})(\\w{8})(\\w{24})(\\w{22})(\\w+)";
    private static final String PLANTILLA_PARTE_FIJA = "codigo_transaccion=$4\nstatus_code=$5\nid_cajero=$6\nsecuencia_transaccion=$7\nsaldo_actual=$8\n";
    private static final String REGISTRO_VARIABLE    = "$10";
    
    private static final String DELIMITADOR              = "11C2601D60";
    private static final String REGEX_VARIABLE           = "(.*)COMISION:(.*)SALDO DISPONIBLE:(.*)PIGNORACIONES(.*):(.*)IDENTIFI:(.*)SECUENCIA(.*)FRML(.*)AVSEGURO(.*)";
    private static final String PLANTILLA_PARTE_VARIABLE = "comision=$2\nsaldo_disponible=$3\npignoraciones=$5\nidentificacion=$6\nsecuencia=$7\nfrml=$8\nav_seguro=$9\n";        
    
    public static String obtenerRespuesta(String strRespuestaISC) {
        
        HashMap<String, String> parteFijaHm     = new HashMap<>();
        HashMap<String, String> parteVariableHm = new HashMap<>();
        String mensajeRespuesta    = "";
        String parteVariableAscii  = "";
        String parteFijaEbcdic     = "";       
        String parteVariableEbcdic = ""; 
        String parteVariable       = "";
        
        try {
            parteFijaEbcdic = strRespuestaISC.replaceAll(REGEX_RESPUESTA, PLANTILLA_PARTE_FIJA);       
            parteFijaHm     = (HashMap<String, String>) UtilidadesMensajeria.stringToHashmap(parteFijaEbcdic);
            
            for (Map.Entry<String, String> i : parteFijaHm.entrySet()) {
                parteFijaHm.put(i.getKey().toString(), UtilidadesMensajeria.ebcdicToAscii(i.getValue().toString()));
            }
            
            parteVariableEbcdic = strRespuestaISC.replaceAll(REGEX_RESPUESTA, REGISTRO_VARIABLE);
            parteVariableAscii  = UtilidadesMensajeria.ebcdicToAscii(parteVariableEbcdic.replaceAll(DELIMITADOR, ""));
            parteVariable       = parteVariableAscii.replaceAll(REGEX_VARIABLE, PLANTILLA_PARTE_VARIABLE);  
            parteVariableHm     = (HashMap<String, String>) UtilidadesMensajeria.stringToHashmap(parteVariable);
    
            mensajeRespuesta = UtilidadesMensajeria.hashmapToString(parteFijaHm) + UtilidadesMensajeria.hashmapToString(parteVariableHm);
        } finally {
            parteFijaHm.clear();
            parteVariableHm.clear();       
        }

        return mensajeRespuesta;
    }
}
