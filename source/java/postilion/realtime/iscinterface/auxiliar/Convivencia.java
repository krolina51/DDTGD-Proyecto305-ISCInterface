package postilion.realtime.iscinterface.auxiliar;


import java.util.Base64;

import postilion.realtime.iscinterface.message.ISCReqInMsg;
import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.iscinterface.util.Utils;
import postilion.realtime.iscinterface.web.model.TransactionSetting;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.message.bitmap.StructuredData;
import postilion.realtime.sdk.util.DateTime;
import postilion.realtime.sdk.util.XPostilion;
import postilion.realtime.sdk.util.convert.Pack;
import postilion.realtime.sdk.util.convert.Transform;

public class Convivencia {
	
	private static int counter = 0;
	
	public Iso8583Post processMsg (Iso8583Post out, ISCReqInMsg in, TransactionSetting tSetting, String cons, boolean enableMonitor) throws XPostilion {
		
		Logger.logLine("Reflected:\n" + in.toString(), enableMonitor);
		
		StructuredData sd = null;
		
		if(out.getStructuredData() != null) {
			sd = out.getStructuredData();
		}
		else {
			sd = new StructuredData();
		}
		
		
		//CAMPO 7 TRANSMISSION DATE N TIME
		out.putField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME, new DateTime(5).get("MMddHHmmss"));	
		
		//CAMPO 12 TIME LOCAL
		out.putField(Iso8583.Bit._012_TIME_LOCAL, new DateTime().get("HHmmss"));
		
		
		//CAMPO 13 TRANSMISSION DATE N TIME
		out.putField(Iso8583.Bit._013_DATE_LOCAL, new DateTime().get("MMdd"));
		
		//CAMPO 15 TRANSMISSION DATE N TIME
		out.putField(Iso8583.Bit._015_DATE_SETTLE, new DateTime().get("MMdd"));
		
		//TRACK2 Field 35
		out.putField(Iso8583.Bit._035_TRACK_2_DATA, "5454541234567890D29122211388313500000");
		
		//CAMPO 37 Retrieval Reference Number     DEBE SER CAMBIADO SE DEJO ASI PARA REALIZAR LAS PRUEBAS INICIALES
		out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, "0901".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(30, 38))))
				.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(38, 44)))).concat(cons.substring(4, 5)));
		
		//127.2 SWITCHKEY
		out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, "0200".concat(Utils.getStringDate(Utils.MMDDYYhhmmss).substring(0, 4))
				.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(52, 64))))
				.concat("0"+cons.substring(2, 5)));
		

		sd.put("ISCREQ_Message", in.getTotalHexString());
		sd.put("ISCREQ_MessageConv", Base64.getEncoder().encodeToString(in.getTotalHexString().getBytes()));
		sd.put("IN_MSG", in.getTotalHexString());
		out.putStructuredData(sd);
		
		return out;
	}
	

}
