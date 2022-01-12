package postilion.realtime.iscinterface.auxiliar;


import postilion.realtime.iscinterface.message.ISCReqInMsg;
import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.iscinterface.util.Utils;
import postilion.realtime.iscinterface.web.model.TransactionSetting;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.message.bitmap.StructuredData;
import postilion.realtime.sdk.util.XPostilion;
import postilion.realtime.sdk.util.convert.Pack;
import postilion.realtime.sdk.util.convert.Transform;

public class AvanceBancaMovil {
	
	private static int counter = 0;
	
	public Iso8583Post processMsg (Iso8583Post out, ISCReqInMsg in, TransactionSetting tSetting, String cons) throws XPostilion {
		
		Logger.logLine("Reflected:\n" + in.toString(), true);
		
		StructuredData sd = null;
		
		if(out.getStructuredData() != null) {
			sd = out.getStructuredData();
		}
		else {
			sd = new StructuredData();
		}
		
		
//		//CAMPO 7 TRANSMISSION DATE N TIME
		out.putField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME, Utils.getStringDate(Utils.MMDDYYhhmmss).substring(0, 4).concat(
				Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(52, 64)))));	
		
		
		//CAMPO 13 TRANSMISSION DATE N TIME
		out.putField(Iso8583.Bit._013_DATE_LOCAL, Utils.getStringDate(Utils.MMDDYYhhmmss).substring(0, 4));
		sd.put("B24_Field_13", Utils.getStringDate(Utils.MMDDYYhhmmss).substring(0, 4));
		
		//TRACK2 Field 35
		out.putField(Iso8583.Bit._035_TRACK_2_DATA, "0099010000000000000=9912000");
		
		//CAMPO 37 Retrieval Reference Number     DEBE SER CAMBIADO SE DEJO ASI PARA REALIZAR LAS PRUEBAS INICIALES
		out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, "0901".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(30, 38))))
				.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(38, 44)))).concat(cons.substring(4, 5)));
		
		//CAMPO 102 DEBIT ACCOUNT
		out.putField(Iso8583.Bit._102_ACCOUNT_ID_1, "00010".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(182, 214)))));
		
		//CAMPO 103 CREDIT ACCOUNT
		out.putField(Iso8583.Bit._103_ACCOUNT_ID_2, "0110052".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(148, 180)))));
		
		
		Logger.logLine("127.02:\n" + "0"+cons.substring(2, 5), true);
		Logger.logLine("127.02:\n" + Utils.getStringDate(Utils.MMDDYYhhmmss).substring(0, 4), true);
		Logger.logLine("127.02:\n" + Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(52, 64))), true);
		
		//127.2 SWITCHKEY
		out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, "0200".concat(Utils.getStringDate(Utils.MMDDYYhhmmss).substring(0, 4))
				.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(52, 64))))
				.concat("0"+cons.substring(2, 5)));
		
		//127.22 TAG B24_Field_7
		sd.put("B24_Field_7", Utils.getStringDate(Utils.MMDDYYhhmmss).substring(0, 4).concat(
				Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(52, 64)))));
			
		//127.22 TAG B24_Field_18
		sd.put("B24_Field_18", "6010");

		//127.22 TAG B24_Field_41
		sd.put("B24_Field_41", "0001".concat(cons.substring(12, 16)).concat("00005   "));	
		
		//127.22 TAG B24_Field_48
		sd.put("B24_Field_48", "1                  00000000");		
		
		//127.22 TAG B24_Field_61
		sd.put("B24_Field_61", "0001000000000000000");
		
		//127.22 TAG B24_Field_63
		sd.put("B24_Field_63", "& 0000600278! 0400020  00000000000Y     Y0! BM00036 Q101013000      00000000000000000000! C000026                   2  0    ! C400012 1 25100  600! Q400122 D0000000000000000000000000000000000000000000000000000000000000000000000000000000000000017000121100000000000000000000000000");
		
		//127.22 TAG B24_Field_121
		sd.put("B24_Field_121", "00000000000000000000");
		
		//127.22 TAG B24_Field_124
		sd.put("B24_Field_124", "000000000");
		
		//127.22 TAG B24_Field_125
		sd.put("B24_Field_125", "000000000000");
		
		//127.22 TAG B24_Field_126
		sd.put("B24_Field_126", "00000000000000000000000000000000000000");
		
		//127.22 TAG B24_Field_35
		sd.put("B24_Field_35", "009901".concat(Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(182, 214))), 18, '0', false)).concat("D991200000001"));
		
		//127.22 TAG B24_Field_17
		sd.put("B24_Field_17", Utils.getStringDate(Utils.MMDDYYhhmmss).substring(0, 4));	

		out.putStructuredData(sd);
		
		return out;
	}
	

}
