package postilion.realtime.iscinterface.auxiliar;


import postilion.realtime.iscinterface.message.ISCReqInMsg;
import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.iscinterface.web.model.TransactionSetting;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.message.bitmap.StructuredData;
import postilion.realtime.sdk.util.XPostilion;
import postilion.realtime.sdk.util.convert.Pack;
import postilion.realtime.sdk.util.convert.Transform;

public class TransferAux {
	
	private static int counter = 0;
	
	public Iso8583Post processMsg (Iso8583Post out, ISCReqInMsg in, TransactionSetting tSetting, String cons) throws XPostilion {
		
		Logger.logLine("Reflected:\n" + in.toString(), true);
		
		
//		//CAMPO 7 TRANSMISSION DATE N TIME
//		out.putField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME, in.getTotalHexString().substring(beginIndex, endIndex));
		
		
		//TRACK2 Field 35
		out.putField(Iso8583.Bit._035_TRACK_2_DATA, "0099010000000000000=9912000");
		
		//CAMPO 37 Retrieval Reference Number
		out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, "0901".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(30, 38))))
				.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_SEQUENCE_NR, ISCReqInMsg.POS_end_SEQUENCE_NR)))));
		
		//CAMPO 102 DEBIT ACCOUNT
		out.putField(Iso8583.Bit._102_ACCOUNT_ID_1, "00010".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_DEBIT_ACC_NR, ISCReqInMsg.POS_end_DEBIT_ACC_NR)))));
		
		//CAMPO 103 CREDIT ACCOUNT
		out.putField(Iso8583.Bit._103_ACCOUNT_ID_2, "0110052".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_CREDIT_ACC_NR, ISCReqInMsg.POS_end_CREDIT_ACC_NR)))));
		
		//127.2 SWITCHKEY
		out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, "0200".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(248, 268)))).concat("0"+cons.substring(2, 5)));

		//127.22 TAG B24_Field_41
		if(out.getStructuredData() != null) {
			StructuredData sd = out.getStructuredData();
			sd.put("B24_Field_41", "0001".concat(cons.substring(12, 16)).concat("00005   "));	
			out.putStructuredData(sd);
		}
		else {
			StructuredData sd = new StructuredData();
			sd.put("B24_Field_41", "0001".concat(cons.substring(12, 16)).concat("00005   "));	
			out.putStructuredData(sd);
		}
		
		
		//127.22 TAG B24_Field_35
		if(out.getStructuredData() != null) {
			StructuredData sd = out.getStructuredData();
			sd.put("B24_Field_35", "009901".concat(Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(182, 214))), 18, '0', false)).concat("D991200000001"));	
			out.putStructuredData(sd);
		}
		else {
			StructuredData sd = new StructuredData();
			sd.put("B24_Field_35", "009901".concat(Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(182, 214))), 18, '0', false)).concat("D991200000001"));	
			out.putStructuredData(sd);
		}
		
		return out;
	}
	

}
