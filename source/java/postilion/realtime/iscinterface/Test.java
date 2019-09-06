package postilion.realtime.iscinterface;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
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
		
		
		Pattern p = Pattern.compile("\\w{2}(\\d{4})(.)[A-Z]{1}:(.)");
		String o = "3132333435361140401D60E5C9C2C1F4F0C1E3F6F540404040F0F7F3F8F0F0F0F0F0F0F0F0F0F0F0F0F0013E404040404040404011C2601D60D4C5E2E2C1C7C5E240C6D6D940D9C5E3C9D9D640C1E3D440F0F0F0F0F0F0F0F0F1F1F8F1F8F211C2601D60E2E3F8F040E2E3F0F3F0F940E27A40C9D5C1C3E3C9E5C14B40D9C5D8E4C9C5D9C540E2E4D7C5D9E5C9E2D6D940404040404040404040404B11C2601D60E2E3F8F040E3E2F1F4F1F740C97A40F0F0F0F0F0F7F0F2F6F2F4F0F0F040F0F0F0F0F0F0F0F0F0F0F0F0F0F040F0F0F0F0F0F7F0F2F6F2F4F0F0F011C2601D60D4C5E2E2C1C7C5E240C6D6D940C4C240C5D4C5D940C5C340F0F0F0F0F0F0F0F0F1F1F8F1F8F211C2601D60E2E3F8F040E3E2F0F4F9F840E27A40C1D3C5D9E3C1406040C3E4C5D5E3C140C9D5C1C3E3C9E5C1404040404040404040404040404040404B";
		String s = Transform.fromEbcdicToAscii(Transform.fromHexToBin(o));
		Matcher m = p.matcher(s);
		int i = 0;
		while (m.find()) {
		    System.out.println(i+"::"+m.group(1));
		    i++;
		}
		
		
		String b = new String("313233343536E5C9C2C1114040C1E3E6C9C1E3F6F540404040F0F5F6F9F0F0F8F1F4F4F4F4F24040404040404040F0F0F0F0F0F0F0F0119130F1F9F3F5F2F91140C3F011C3F0F0F0F0F0F0F0F0F0F3F0F0F0F0F0F0119131F1F4F4F4F4F2114040F0F0F0F6F1F9F1F8F9F411E5C6F4F6F4F6F2F0F0F1F1F5F0F0F1F6F8F511C17AF0F0F5F9F1F91191A1F1119160F0F2119161F0F4119162F0F0F0F0F0F9F1F5119181F011912FF0F0F3F2F0F2F8F811924AF0F0F0F0119125F011912DF0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0119199C1E3C84040C24BC1E5E54040D3C1C2D6D9C1E3D6D9C9D6F1F0F0F1F0F0C2D6C7D6E3C1C3C1C7C3D61197A5E5E211E4F2F011E4F3F0F0F0F0F0F0F2F0F1F3F5F8F9F1F4F711D140F0F9F1F511D138F0F0F0F011D139C111E5C7F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F01197A7F0F011A9B1D411E5F3F011D141D511E0E2F0F0F0F011913DF0F0F0F0F0F0F0F0F0F0F0F0F0F0F0404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040F0F5F3F3F1C1E3E6C9F0F011C2601D6040404040C6D9D4D3404011C2601D6040404040C1E5E2C5C7E4D9D640F0D4D3404011C2601D6040404040C1E5E2C5C7E4D9D640F0404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
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
    
    public final static String EBCDIC_1 = "CP1047";
    public final static String EBCDIC_2 = "CP1140";
    public final static String EBCDIC_3 = "CP1141";
    public final static String EBCDIC_4 = "CP1143";
    public final static String ASCII = "ISO-8859-1";
}
