package postilion.realtime.iscinterface.auxiliar;


import java.io.PrintWriter;
import java.io.StringWriter;

import postilion.realtime.iscinterface.message.ISCReqInMsg;
import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.iscinterface.web.model.TransactionSetting;
import postilion.realtime.sdk.crypto.CryptoCfgManager;
import postilion.realtime.sdk.crypto.CryptoManager;
import postilion.realtime.sdk.crypto.DesKwp;
import postilion.realtime.sdk.crypto.XCrypto;
import postilion.realtime.sdk.crypto.XPinTranslationFailure;
import postilion.realtime.sdk.eventrecorder.EventRecorder;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.message.bitmap.StructuredData;
import postilion.realtime.sdk.util.XPostilion;
import postilion.realtime.sdk.util.convert.Pack;
import postilion.realtime.sdk.util.convert.Transform;

public class RetiroAux {
	
	public Iso8583Post processMsg (Iso8583Post out, ISCReqInMsg in, TransactionSetting tSetting, String cons, boolean enableMonitor) throws XPostilion {
		
		Logger.logLine("Reflected:\n" + in.toString(), enableMonitor);
		String newPin = "FFFFFFFFFFFFFFFF";
		String encPinBlock = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(622, 654)));
		String pan = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(468, 500)));
		Logger.logLine("encPinBlock:" + encPinBlock, enableMonitor);
		Logger.logLine("pan:" + pan, enableMonitor);
		try {
			CryptoCfgManager crypcfgman = CryptoManager.getStaticConfiguration();
			DesKwp kwp = crypcfgman.getKwp("ATH_KPE");
			Logger.logLine("kwp:" + kwp.getName(), enableMonitor);
			newPin = kwp.translatePin(encPinBlock, kwp, pan);
			Logger.logLine("newPin:" + newPin, enableMonitor);
		} catch (XCrypto e) {
			Logger.logLine("KWP ERROR: " + e.toString(), enableMonitor);
			EventRecorder.recordEvent(new Exception(e.toString()));
		}
		
		StructuredData sd = null;
		
		if(out.getStructuredData() != null) {
			sd = out.getStructuredData();	
		} else {
			sd = new StructuredData();
		}
		
		
		//TRACK2 Field 35
		out.putField(Iso8583.Bit._035_TRACK_2_DATA, Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(468, 542))), 37, '0', false));
		
		//CAMPO 37 Retrieval Reference Number
		out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, "0901".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(30, 38))))
				.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_SEQUENCE_NR, ISCReqInMsg.POS_end_SEQUENCE_NR)))));
		
		out.putField(Iso8583.Bit._052_PIN_DATA, newPin);
		//CAMPO 102 DEBIT ACCOUNT
		out.putField(Iso8583.Bit._102_ACCOUNT_ID_1, Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_DEBIT_ACC_NR, ISCReqInMsg.POS_end_DEBIT_ACC_NR))), 18, '0', false));
		
		//127.2 SWITCHKEY
		out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, "0200".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(248, 268)))).concat("0"+cons.substring(2, 5)));		
		
		sd.put("B24_Field_48", "000000000000               ");
		sd.put("B24_Field_15", Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(250, 258))));
		sd.put("B24_Field_41", "0001".concat(cons.substring(12, 16)).concat("00005   "));	
		sd.put("B24_Field_104", Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(96, 128))), 18, '0', false));
		sd.put("B24_Field_126", constructField126(in.getTotalHexString().substring(660, 1494)));
//		sd.put("B24_Field_126", "& 0000500342! QT00032 0110000000000000000000000000000 "
//				+ "! B200158 7FF90000808080048800B95259759DCF36970000070000000000000000003800001F17017022041901D0D87DAF000706011203A0B80100000000000000000000000000000000000000000000000000"
//				+ "! B300080 CF00SmartPOS60D8C8000000000000001100020204000007A0000000031010000000000000000000"
//				+ "! B400020 05151000000000000000");
		out.putStructuredData(sd);		
		
		return out;
	}
	
	public String constructField126(String inToken) {
		StringBuilder sb = new StringBuilder();
		sb.append(constructTokenQT(inToken));
		sb.append(constructTokenB2(inToken));
		sb.append(constructTokenB3(inToken));
		sb.append(constructTokenB4(inToken));
		return sb.toString();
	}
	
	public String constructTokenQT(String inToken) {
		return "& 0000500342! QT00032 0110000000000000000000000000000 ";
	}
	
	public String constructTokenB2(String inToken) {
		StringBuilder b2 = new StringBuilder();
		b2.append("! B200158 7FF90000");
		b2.append(Transform.fromEbcdicToAscii(Transform.fromHexToBin(inToken.substring(630, 634))));
		b2.append(Transform.fromEbcdicToAscii(Transform.fromHexToBin(inToken.substring(194, 214))));
		b2.append(Transform.fromEbcdicToAscii(Transform.fromHexToBin(inToken.substring(426, 458))));
		b2.append(Transform.fromEbcdicToAscii(Transform.fromHexToBin(inToken.substring(100, 124))));
		b2.append(Transform.fromEbcdicToAscii(Transform.fromHexToBin(inToken.substring(138, 162))));
		b2.append(Transform.fromEbcdicToAscii(Transform.fromHexToBin(inToken.substring(586, 594))));
		b2.append(Transform.fromEbcdicToAscii(Transform.fromHexToBin(inToken.substring(608, 616))));
		b2.append(Transform.fromEbcdicToAscii(Transform.fromHexToBin(inToken.substring(176, 184))));
		b2.append(Transform.fromEbcdicToAscii(Transform.fromHexToBin(inToken.substring(228, 236))));
		b2.append(Transform.fromEbcdicToAscii(Transform.fromHexToBin(inToken.substring(246, 258))));
		b2.append(Transform.fromEbcdicToAscii(Transform.fromHexToBin(inToken.substring(268, 272))));
		b2.append(Transform.fromEbcdicToAscii(Transform.fromHexToBin(inToken.substring(286, 302))));
		b2.append("00").append(Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(inToken.substring(716, 748))), 66, '0', true));
		
		return b2.toString();
	}
	
	public String constructTokenB3(String inToken) {
		StringBuilder b3 = new StringBuilder();
		b3.append("! B300080 CF00");
		b3.append(Transform.fromHexToBin(Transform.fromEbcdicToAscii(Transform.fromHexToBin(inToken.substring(674, 706)))));
		b3.append(Transform.fromEbcdicToAscii(Transform.fromHexToBin(inToken.substring(762, 774))));
		b3.append("00000000000000").append(Transform.fromEbcdicToAscii(Transform.fromHexToBin(inToken.substring(316, 320))));
		b3.append(Transform.fromEbcdicToAscii(Transform.fromHexToBin(inToken.substring(824, 832))));
		b3.append(Transform.fromEbcdicToAscii(Transform.fromHexToBin(inToken.substring(648, 660))));
		b3.append(Transform.fromEbcdicToAscii(Transform.fromHexToBin(inToken.substring(544, 576))));
		b3.append("000000000000000000");
		return b3.toString();
	}
	
	public String constructTokenB4(String inToken) {
		StringBuilder b4 = new StringBuilder();
		b4.append("! B400020 ");
		b4.append(Transform.fromEbcdicToAscii(Transform.fromHexToBin(inToken.substring(806, 810))));
		b4.append("151");
		b4.append(Transform.fromEbcdicToAscii(Transform.fromHexToBin(inToken.substring(788, 792))));
		b4.append("0000000000000");
		return b4.toString();
	}

}
