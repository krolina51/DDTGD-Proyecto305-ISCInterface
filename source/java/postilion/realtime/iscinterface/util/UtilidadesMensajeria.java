package postilion.realtime.iscinterface.util;

import java.math.BigInteger;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilidadesMensajeria {
	
	private UtilidadesMensajeria() {}
    
    private static final String EBCDIC_ENCODING  = "IBM-1047";
    private static final String HEX_CHARS        = "0123456789abcdef";    
    private static final String KEY_VALUES_REGEX = "(\\w*)=(.*)";

    public static String ebcdicToAscii(String strHexEbcdic) {
        String strAscii = "";
        
        try {
            byte[] ebcdicByteArray = new BigInteger(strHexEbcdic, 16).toByteArray();
            strAscii               = new String(ebcdicByteArray, EBCDIC_ENCODING);
        } catch (Exception e) {
            strAscii = "";
        }
        
        return strAscii;
    }
    
    public static String asciiToEbcdic(String strAscii) {
        String strEbcdicHex = "";
        
        try {
            byte[] ebcdicByteArray = strAscii.getBytes(EBCDIC_ENCODING);
            strEbcdicHex           = asHex(ebcdicByteArray);            
        } catch (Exception e) {
            strEbcdicHex = "";
        }        

        return strEbcdicHex;
    }
    
    public static String asHex(byte[] buf) {
        char[] hexCharsArray = HEX_CHARS.toCharArray();
        char[] chars         = new char[2 * buf.length];
        for (int i = 0; i < buf.length; ++i) {
            chars[2 * i]     = hexCharsArray[(buf[i] & 0xF0) >>> 4];
            chars[2 * i + 1] = hexCharsArray[buf[i] & 0x0F];
        }
        return new String(chars);
    }

    public static Map<String, String> stringToHashmap(String stringList) {
       Pattern pattern = Pattern.compile(KEY_VALUES_REGEX);
       Matcher matcher = pattern.matcher(stringList);
       HashMap<String, String> hm = new HashMap<>();
       while (matcher.find()) {
          String key = matcher.group(1);
          String value = matcher.group(2);
          hm.put(key, value.trim());
       }
       return hm;
    }
    
    public static String hashmapToString(Map<String, String> hm) {

       String result;
       String hmKey;
       String hmValue;
       StringBuilder message = new StringBuilder();

       for (Map.Entry<String, String> i : hm.entrySet()) {
          hmKey = i.getKey();
          try {
             hmValue = i.getValue();
          } catch (Exception ex) {
             hmValue = "";
          }
          message.append(hmKey + "=" + hmValue + "\n");
       }

       result = message.toString();

       return result;
    }    

}
