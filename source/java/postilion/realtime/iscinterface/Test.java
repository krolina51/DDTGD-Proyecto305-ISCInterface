package postilion.realtime.iscinterface;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import postilion.realtime.iscinterface.message.ISCResMessage;
import postilion.realtime.iscinterface.message.ISCReqMessage.Constants;
import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.iscinterface.util.Utils;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.message.bitmap.StructuredData;
import postilion.realtime.sdk.message.stream.XStreamBase;
import postilion.realtime.sdk.util.convert.Pack;
import postilion.realtime.sdk.util.convert.Transform;

public class Test {
	
	public static void main(String[] args) throws XStreamBase, UnsupportedEncodingException {
		
		
		String date = getStringDate();
		System.out.println(date);
		
		Pattern p = Pattern.compile("[A-Z]{2}(\\d{4})(.)[A-Z]{1}:(.)");
		//String o = "313233343536E5C9C2C1114040C1E3E6C9C1E3F6F540404040F0F8F9F7F0F0F8F1F0F1F2F5F74040404040404040F0F0F0F0F0F0F0F0119130F1F9F3F7F1F31140C3F111C3F0F0F0F0F0F0F0F0F0F4F5F0F0F0F0F0119131F1F0F1F2F5F7114040F0F0F0F0F1F1F8F1F5F811E5C6F4F5F7F6F0F2F0F0F0F0F0F6F6F9F0F611C17AF0F0F5F1F1F41191A1F1119160F0F2119161F0F4119162F0F0F0F0F0F9F1F5119181F011912FF0F0F1F8F7F9F5F011924AF0F0F0F0119125F011912DF0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0119199C1E3C84040C24BC1E5E54040D3C1C2D6D9C1E3D6D9C9D6F1F0F0F1F0F0C2D6C7D6E3C1C3C1C7C3D61197A5E5E211E4F2F011E4F3F0F0F0F0F0F0F0F0F8F0F2F4F3F0F0F911D140F0F9F1F511D138F0F0F0F011D139C111E5C7F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F01197A7F0F011A9B1D411E5F3F011D141D511E0E2F0F0F0F011913DF0F0F0F0F0F0F0F0F0F0F0F0F0F0F0";
		String o = "4B4B4B4B4B4B4B4BE5C9C2C14B4040C1E3E6C9C1E3F0F740404040F2F2F9F3F0F0F8F1F5F0F4F5F14040404040404040F0F0F0F0F0F0F0F04B914BF0F5F1F3F1F94B40C3F14BC3F0F0F0F0F0F0F0F0F0F2F0F0F0F0F0F04B914BF1F5F0F4F5F14B4040F0F0F0F0F1F1F8F1F5F84BE5C6F4F5F7F6F0F2F0F0F0F0E3E2F6F9F0F64BC17AF0F0F7F3F9F64B91A1F14B9160F0F24B9161F0F44B914BF0F0F0F0F3F9F1F54B9181F04B914BF0F0F4F5F1F4F7F34B924BF0F0F0F04B914BF04B914BF0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F04B9199C1E3C84040C24BC1E5E54040D3C1C2D6D9C1E3D6D9C9D6F1F0F0F1F0F0C2D6C7D6E3C1C3C1C7C3D64B97A5E5E24BE4F2F04BE4F3F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F04BD140F3F9F1F54BD14BF0F0F0F04BD14BC14BE5C7F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F04B97A7F0F04BA94BD44BE5F3F04BD14BD54BE0E2F0F0F0F04B914BF0F0F0F0F0F0F0F0F0F0F0F0F0F0F0";
		String s = Transform.fromEbcdicToAscii(Transform.fromHexToBin(o));
		Matcher m = p.matcher(s);
		int i = 0;
		while (m.find()) {
		    System.out.println(i+"::"+m.group(1));
		    i++;
		}
		
		
		String b = new String(o);
		//String c = convertControlChars(b);
		System.out.println("###-->" + Transform.fromEbcdicToAscii(Transform.fromHexToBin(b)));
		System.out.println("###-->" + Transform.fromHexToBin(Transform.fromEbcdicToAscii(b)));
		System.out.println("###-->" + Utils.ebcdicToAscii(b));
		System.out.println("###-->" + Utils.ebcdicToAscii(b));
		
		//String a = "008E3132333435361140401D60E5C9C2C1F1F0C1E3F0F240404040F2F1F4F0F0F0F0F0F0F0F0F0F0F0F0F0F00086404040404040404011C2601D60D4C5E2E2C1C7C5E240C6D6D940D9C5E3C9D9D640C1E3D440F0F2F9F9F0F4F5F2F7F811C2601D60C9D4F8F040E3E2F5F2F8F040C67A40C5E7C3C5C4C540D3C9D4C9E3C540E3D94BC4C9C1D9C9C1E26040C8D6E8";
		String a = "313233343536";
		
		System.out.println("Processing message from interchange! ISC");
		byte[] dataAscii = Transform.fromEbcdicDataToAsciiData(Transform.getData(a, Transform.Encoding.EBCDIC));
		System.out.println("###-->" + Transform.getString(dataAscii, Transform.Encoding.ASCII));
		
		ISCResMessage rspISCMsg = new ISCResMessage();
		rspISCMsg.fromMsg(dataAscii);
		System.out.println("[MSG][ISC] \n" + rspISCMsg.toString());
		
		String z = "450942hghghg";
		System.out.println(z.substring(0, 6));
		
		
		System.out.println(Utils.ebcdicToAscii(rspISCMsg.getField(ISCResMessage.Fields._VARIABLE_BODY)));
		
	}
	
    public static String Convert (String strToConvert,String in, String out){
        try {

         Charset charset_in = Charset.forName(out);
         Charset charset_out = Charset.forName(in);

         CharsetDecoder decoder = charset_out.newDecoder();

         CharsetEncoder encoder = charset_in.newEncoder();

         CharBuffer uCharBuffer = CharBuffer.wrap(strToConvert);

         ByteBuffer bbuf = encoder.encode(uCharBuffer);

         CharBuffer cbuf = decoder.decode(bbuf);

         String s = cbuf.toString();

         //System.out.println("Original String is: " + s);
         return s;

     } catch (CharacterCodingException e) {

         //System.out.println("Character Coding Error: " + e.getMessage());
         return "";

     }
        
    }
    
    private static String convertControlChars(String input) {
    	String output = "";
    	for(int i = 0; i < input.length(); ) {
    		
    		String hexByte = input.substring(i, i + 2);
    		switch (hexByte) {
			case "11":
				hexByte = "4B";
				break;
			default:
				hexByte = hexByte;
				break;
			}
    		output += hexByte;
    		i += 2;
    	}
    	System.out.println(output);
    	return output;
    }
    
	public static String getStringDate() {

		Calendar cal = Calendar.getInstance();
		StringBuilder date = new StringBuilder();

		date.append(String.valueOf(cal.get(Calendar.YEAR)).substring(2, 4));
		date.append(
				String.valueOf(cal.get(Calendar.MONTH)+1).length() > 1 ? String.valueOf(cal.get(Calendar.MONTH)+1)
						: "0" + String.valueOf(cal.get(Calendar.MONTH)+1));
		date.append(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)).length() > 1
				? String.valueOf(cal.get(Calendar.DAY_OF_MONTH))
				: "0" + String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
		date.append(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)).length() > 1
				? String.valueOf(cal.get(Calendar.HOUR_OF_DAY))
				: "0" + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
		date.append(String.valueOf(cal.get(Calendar.MINUTE)).length() > 1 ? String.valueOf(cal.get(Calendar.MINUTE))
				: "0" + String.valueOf(cal.get(Calendar.MINUTE)));
		date.append(String.valueOf(cal.get(Calendar.SECOND)).length() > 1 ? String.valueOf(cal.get(Calendar.SECOND))
				: "0" + String.valueOf(cal.get(Calendar.SECOND)));

		return date.toString();
	}
    
    public final static String EBCDIC_1 = "CP1047";
    public final static String EBCDIC_2 = "CP1140";
    public final static String EBCDIC_3 = "CP1141";
    public final static String EBCDIC_4 = "CP1143";
    public final static String ASCII = "ISO-8859-1";
}
