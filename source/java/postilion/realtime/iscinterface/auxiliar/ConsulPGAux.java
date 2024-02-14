package postilion.realtime.iscinterface.auxiliar;


import java.text.SimpleDateFormat;
import java.util.Date;

import postilion.realtime.genericinterface.eventrecorder.events.TryCatchException;
import postilion.realtime.iscinterface.message.ISCReqInMsg;
import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.iscinterface.util.Utils;
import postilion.realtime.iscinterface.web.model.TransactionSetting;
import postilion.realtime.sdk.env.calendar.BusinessCalendar;
import postilion.realtime.sdk.eventrecorder.EventRecorder;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.message.bitmap.StructuredData;
import postilion.realtime.sdk.util.DateTime;
import postilion.realtime.sdk.util.XPostilion;
import postilion.realtime.sdk.util.convert.Pack;
import postilion.realtime.sdk.util.convert.Transform;

public class ConsulPGAux {
	
	
	public Iso8583Post processMsg (Iso8583Post out, ISCReqInMsg in, TransactionSetting tSetting, String cons, boolean enableMonitor, boolean isNextDay) throws XPostilion {
		
		
		try {
			
			BusinessCalendar objectBusinessCalendar = new BusinessCalendar("DefaultBusinessCalendar");
			Date businessCalendarDate = null;
			String settlementDate = null;
			
			Logger.logLine("Reflected:\n" + in.toString(), enableMonitor);
			
			StructuredData sd = null;
			
			if(out.getStructuredData() != null) {
				sd = out.getStructuredData();	
			} else {
				sd = new StructuredData();
			}
			
			
			if(Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._10_H_NEXTDAY_IND)).equals("1")) {
				businessCalendarDate = objectBusinessCalendar.getNextBusinessDate();
				settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
			}else {
				businessCalendarDate = objectBusinessCalendar.getCurrentBusinessDate();
				settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
			}

			String cuenta = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(146,178)));
			String factura = Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(146,206))).replace(" ", ""), 30, '0', false);
			String datosAdicionales1 = Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(296,356))).replace(" ", ""), 30, '0', false);
			String datosAdicionales2 = Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(356,416))).replace(" ", ""), 30, '0', false);
			String datosAdicionales3 = Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(416,456))).replace(" ", ""), 20, '0', false);
			String datosAdicionales4 = Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(456,496))).replace(" ", ""), 20, '0', false);
			String datosAdicionales5 = Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(496,536))).replace(" ", ""), 20, '0', false);
			
			String p62 = factura.concat(datosAdicionales1)
					.concat(datosAdicionales2)
					.concat(datosAdicionales3)
					.concat(datosAdicionales4)
					.concat(datosAdicionales5);
			
			
			
			
			//CAMPO 7 TRANSMISSION DATE N TIME
			out.putField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME, new DateTime(5).get("MMddHHmmss"));
			
			//Field 13
			out.putField(Iso8583.Bit._013_DATE_LOCAL, new DateTime().get("MMdd"));
			
			out.putField(Iso8583.Bit._015_DATE_SETTLE, settlementDate);
			
			//TRACK2 Field 35
			out.putField(Iso8583.Bit._035_TRACK_2_DATA, "008801".concat(cuenta.substring(3).concat("D49120000000100000")));
			
			//CAMPO 37 Retrieval Reference Number
			out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, "09013300".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(38, 46)))));
			
			//TRACK2 Field 43
			out.putField(Iso8583.Bit._043_CARD_ACCEPTOR_NAME_LOC, "BOG  ".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(30, 38))))
					.concat(" OFICINA BBTA             BOCCO"));
			
			//Field 102
			out.putField(Iso8583.Bit._102_ACCOUNT_ID_1, Pack.resize(cuenta, 18, '0', false));
			
			//Field 103
			out.putField(Iso8583.Bit._103_ACCOUNT_ID_2, "0000000000".concat(Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(858))), 18, '0', false)));
		
			
			//127.2 SWITCHKEY
			out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, "0200".concat(new DateTime().get("MMddHHmmss")).concat("0"+cons.substring(2, 5)));

			//127.22 TAG B24_Field_17
			sd.put("B24_Field_17", settlementDate);
			//127.22 TAG B24_Field_35
			sd.put("B24_Field_35", "008801".concat(cuenta.substring(3).concat("D49120000000100000")));
			//127.22 TAG B24_Field_41
			sd.put("B24_Field_41", "0001829700003   ");
			//127.22 TAG B24_Field_48
			sd.put("B24_Field_48", "3                       30000000000000000000");
			//127.22 TAG B24_Field_60
			sd.put("B24_Field_60", "0901BBOG+000");
			//127.22 TAG B24_Field_62
			sd.put("B24_Field_62", p62);
			sd.put("IN_MSG", in.getTotalHexString());
			
			out.putStructuredData(sd);	
			
		}catch(Exception e) {
			e.toString();
			EventRecorder.recordEvent(
					new Exception("ERROR processMsg: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					"Method :[" + "processMsg" + "]\n" + "processMsg: " + "\n",
					Utils.getStringMessageException(e) }));
		}
		
		
		
		return out;
	}
	

}
