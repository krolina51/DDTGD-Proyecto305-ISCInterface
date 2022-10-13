package postilion.realtime.iscinterface.auxiliar;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import postilion.realtime.genericinterface.eventrecorder.events.TryCatchException;
import postilion.realtime.iscinterface.message.ISCReqInMsg;
import postilion.realtime.iscinterface.message.ISCReqInMsg.Fields;
import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.iscinterface.util.Utils;
import postilion.realtime.iscinterface.web.model.TransactionSetting;
import postilion.realtime.sdk.crypto.CryptoCfgManager;
import postilion.realtime.sdk.crypto.CryptoManager;
import postilion.realtime.sdk.crypto.DesKwp;
import postilion.realtime.sdk.crypto.XCrypto;
import postilion.realtime.sdk.crypto.XPinTranslationFailure;
import postilion.realtime.sdk.env.calendar.BusinessCalendar;
import postilion.realtime.sdk.eventrecorder.EventRecorder;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.message.bitmap.StructuredData;
import postilion.realtime.sdk.util.XPostilion;
import postilion.realtime.sdk.util.convert.Pack;
import postilion.realtime.sdk.util.convert.Transform;

public class RetiroAux {
	
	public static int pos9f27,pos95,pos9f26,pos9f02,pos9f03,pos82,pos9f36,pos9f1a,pos5f2a,pos9a,pos9c,pos9f37,pos9f10;
	public static int pos9f1e,pos9f33,pos9f35,pos9f09,pos9f34,pos84;
	public static int pos9f39,pos5f34;
	
	public Iso8583Post processMsg (Iso8583Post out, ISCReqInMsg in, TransactionSetting tSetting, String cons, boolean enableMonitor) throws XPostilion {
		
		try {
			
			BusinessCalendar objectBusinessCalendar = new BusinessCalendar("DefaultBusinessCalendar");
			Date businessCalendarDate = null;
			String settlementDate = null;
			
			String decoded = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString()));
			
			String parts[] = decoded.split("\\+");
			
			for (int i=0; i<parts.length; i++) {
				if(parts[i].startsWith("9F27"))
					pos9f27 = i;
				if(parts[i].startsWith("95"))
					pos95 = i;
				if(parts[i].startsWith("9F26"))
					pos9f26 = i;
				if(parts[i].startsWith("9F02"))
					pos9f02 = i;
				if(parts[i].startsWith("9F03"))
					pos9f03 = i;
				if(parts[i].startsWith("82"))
					pos82 = i;
				if(parts[i].startsWith("9F36"))
					pos9f36 = i;
				if(parts[i].startsWith("9F1A"))
					pos9f1a = i;
				if(parts[i].startsWith("5F2A"))
					pos5f2a = i;
				if(parts[i].startsWith("9A"))
					pos9a = i;
				if(parts[i].startsWith("9C"))
					pos9c = i;
				if(parts[i].startsWith("9F37"))
					pos9f37 = i;
				if(parts[i].startsWith("9F10"))
					pos9f10 = i;
				if(parts[i].startsWith("9F1E"))
					pos9f1e = i;
				if(parts[i].startsWith("9F33"))
					pos9f33 = i;
				if(parts[i].startsWith("9F35"))
					pos9f35 = i;
				if(parts[i].startsWith("9F09"))
					pos9f09 = i;
				if(parts[i].startsWith("9F34"))
					pos9f34 = i;
				if(parts[i].startsWith("84"))
					pos84 = i;
				if(parts[i].startsWith("9F39"))
					pos9f39 = i;
				if(parts[i].startsWith("5F34"))
					pos5f34 = i;
			}
			
			Logger.logLine("Reflected:\n" + in.toString(), enableMonitor);
			String newPin = "FFFFFFFFFFFFFFFF";
			String encPinBlock = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(622, 654)));
			String pan = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(468, 500)));
			Logger.logLine("encPinBlock:" + encPinBlock, enableMonitor);
			Logger.logLine("pan:" + pan, enableMonitor);
//			try {
//				CryptoCfgManager crypcfgman = CryptoManager.getStaticConfiguration();
//				DesKwp kwp = crypcfgman.getKwp("ATH_KPE");
//				Logger.logLine("kwp:" + kwp.getName(), enableMonitor);
//				newPin = kwp.translatePin(encPinBlock, kwp, pan);
//				Logger.logLine("newPin:" + newPin, enableMonitor);
//			} catch (XCrypto e) {
//				Logger.logLine("KWP ERROR: " + e.toString(), enableMonitor);
//				EventRecorder.recordEvent(new Exception(e.toString()));
//			}
			
			StructuredData sd = null;
			
			if(out.getStructuredData() != null) {
				sd = out.getStructuredData();	
			} else {
				sd = new StructuredData();
			}
			
			if(in.getTotalHexString().substring(46,52).matches("^((F0F4F0)|(F0F5F0)|(F0F6F0)|(F0F7F0))")) {
				businessCalendarDate = objectBusinessCalendar.getNextBusinessDate();
				settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
			}else {
				businessCalendarDate = objectBusinessCalendar.getCurrentBusinessDate();
				settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
			}
			out.putField(Iso8583.Bit._015_DATE_SETTLE, settlementDate);
			
			Logger.logLine("sd:" + sd, enableMonitor);
			//TRACK2 Field 35
			Logger.logLine("seteando campo 35:"+in.getTotalHexString().substring(468, 542), enableMonitor);
			out.putField(Iso8583.Bit._035_TRACK_2_DATA, Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(468, 542))), 37, '0', false));
			
			//CAMPO 37 Retrieval Reference Number
			Logger.logLine("seteando campo 37:"+ in.getTotalHexString().substring(30, 46), enableMonitor);
			out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, "0901".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(30, 38))))
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_SEQUENCE_NR, ISCReqInMsg.POS_end_SEQUENCE_NR)))));
			
			//CAMPO 102 DEBIT ACCOUNT
			Logger.logLine("seteando campo 102: 1"+ in.getTotalHexString(), enableMonitor);
			Logger.logLine("seteando campo 102: 2"+ in.getTotalHexString().substring(ISCReqInMsg.POS_ini_DEBIT_ACC_NR, ISCReqInMsg.POS_end_DEBIT_ACC_NR), enableMonitor);
			out.putField(Iso8583.Bit._102_ACCOUNT_ID_1, Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_DEBIT_ACC_NR, ISCReqInMsg.POS_end_DEBIT_ACC_NR))), 18, '0', false));
			
			//127.2 SWITCHKEY
			Logger.logLine("seteando campo 127.2:"+ in.getTotalHexString().substring(248,268), enableMonitor);
			out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, "0200".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(248, 268)))).concat("0"+cons.substring(2, 5)));		
			
			
			//127.22 TAG B24_Field_17
			sd.put("B24_Field_17", settlementDate);
			sd.put("B24_Field_48", "000000000000               ");
			Logger.logLine("seteando campo 15:"+ in.getTotalHexString().substring(250, 258), enableMonitor);
			sd.put("B24_Field_15", Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(250, 258))));
			sd.put("B24_Field_41", "0001".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(30, 38)))).concat("00003   "));	
			sd.put("B24_Field_52", encPinBlock);	
			Logger.logLine("seteando campo 104:"+ in.getTotalHexString().substring(96, 128), enableMonitor);
			//sd.put("B24_Field_104", Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(96, 128))), 18, '0', false));
			sd.put("B24_Field_104", Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(136,138))).concat("0")
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(96, 128)))));
			Logger.logLine("seteando campo 126:"+ in.getTotalHexString().substring(660, 1494), enableMonitor);
			sd.put("B24_Field_126", constructField126(parts));
			
			
//			sd.put("B24_Field_126", "& 0000500342! QT00032 0110000000000000000000000000000 "
//					+ "! B200158 7FF90000808080048800B95259759DCF36970000070000000000000000003800001F17017022041901D0D87DAF000706011203A0B80100000000000000000000000000000000000000000000000000"
//					+ "! B300080 CF00SmartPOS60D8C8000000000000001100020204000007A0000000031010000000000000000000"
//					+ "! B400020 05151000000000000000");
			out.putStructuredData(sd);		
			
			return out;
		}catch(Exception e) {
			e.printStackTrace();
			EventRecorder.recordEvent(
					new Exception("ERROR processMsg: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					"Method :[" + "processMsg" + "]\n" + "processMsg: " + "\n",
					Utils.getStringMessageException(e) }));
		}
		return out;
		
		
	}
	
	public static String constructField126(String[] tokens) {
		StringBuilder sb = new StringBuilder();
		sb.append(constructTokenQT(tokens));
		sb.append(constructTokenB2(tokens));
		sb.append(constructTokenB3(tokens));
		sb.append(constructTokenB4(tokens));
		return sb.toString();
	}
	
	public static String constructTokenQT(String[] tokens) {
		return "& 0000500342! QT00032 0110000000000000000000000000000 ";
	}
	
	public static String constructTokenB2(String[] tokens) {
		StringBuilder b2 = new StringBuilder();
		try {
			b2.append("! B200158 7FF90000");
			b2.append(tokens[pos9f27].substring(6,8)); //9f27
			b2.append(tokens[pos95].substring(4,14)); //95
			b2.append(tokens[pos9f26].substring(6,22)); //9f26
			b2.append(tokens[pos9f02].substring(6,18)); //9f02
			b2.append(tokens[pos9f03].substring(6,18)); //9f03
			b2.append(tokens[pos82].substring(4,8)); //82
			b2.append(tokens[pos9f36].substring(6,10)); //9f36
			b2.append(tokens[pos9f1a].substring(7,10)); //9f1a
			b2.append(tokens[pos5f2a].substring(7,10)); //5f2a
			b2.append(tokens[pos9a].substring(4,10)); //9a
			b2.append(tokens[pos9c].substring(4,6)); //9c
			b2.append(tokens[pos9f37].substring(6,14)); //9f37
			b2.append("00").append(Pack.resize(tokens[pos9f10].substring(4,20), 66, '0', true)); //9f10
		}catch(Exception e) {
			e.printStackTrace();
			EventRecorder.recordEvent(
					new Exception("ERROR processMsg: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					"Method :[" + "processMsg" + "]\n" + "processMsg: " + "\n",
					Utils.getStringMessageException(e) }));
		}
		
		return b2.toString();
	}
	
	public static String constructTokenB3(String[] tokens) {
		StringBuilder b3 = new StringBuilder();
		
		try {
			b3.append("! B300080 CF00");
			b3.append(Transform.fromHexToBin(tokens[pos9f1e].substring(6,22))); //9f1e
			b3.append(tokens[pos9f33].substring(6,12)); //9f33
			b3.append("00000000000000").append(tokens[pos9f35].substring(6,8)); //9f35
			b3.append(tokens[pos9f09].substring(6,10)); //9f09
			b3.append(tokens[pos9f34].substring(6,12)); //9f34
			b3.append("00").append(tokens[pos84].substring(2,18)); //84-4f
			b3.append("000000000000000000");
		}catch(Exception e) {
			e.printStackTrace();
			EventRecorder.recordEvent(
					new Exception("ERROR processMsg: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					"Method :[" + "processMsg" + "]\n" + "processMsg: " + "\n",
					Utils.getStringMessageException(e) }));
		}
		
		return b3.toString();
	}
	
	public static String constructTokenB4(String[] tokens) {
		StringBuilder b4 = new StringBuilder();
		
		try {
			b4.append("! B400020 ");
			b4.append(tokens[pos9f39].substring(6,8)); //9f39
			b4.append("151");
			b4.append(tokens[pos5f34].substring(6,8)); //5f34
			b4.append("0000000000000");
		}catch(Exception e) {
			e.printStackTrace();
			EventRecorder.recordEvent(
					new Exception("ERROR processMsg: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					"Method :[" + "processMsg" + "]\n" + "processMsg: " + "\n",
					Utils.getStringMessageException(e) }));
		}
		
		return b4.toString();
	}

}
