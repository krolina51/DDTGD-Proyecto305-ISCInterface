package postilion.realtime.iscinterface.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import postilion.realtime.iscinterface.auxiliar.TransferAux;
import postilion.realtime.iscinterface.message.ISCReqInMsg;
import postilion.realtime.iscinterface.message.ISCReqMessage;
import postilion.realtime.iscinterface.web.model.Field;
import postilion.realtime.iscinterface.web.model.TransactionSetting;
import postilion.realtime.iscinterface.web.model.WholeTransSetting;
import postilion.realtime.sdk.message.IMessage;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.message.bitmap.StructuredData;
import postilion.realtime.sdk.util.XPostilion;
import postilion.realtime.sdk.util.convert.Transform;

public class UtilsModel2 extends Utils{
	
	public static IMessage processReqISOMsg(WholeTransSetting transMsgsConfig, ISCReqInMsg iscInReq, FlowDirection dir, String cons) throws XPostilion, FileNotFoundException {
		
		Iso8583Post mappedIso = new Iso8583Post();
		mappedIso.setMessageType(Iso8583.MsgTypeStr._0200_TRAN_REQ);
		
		//CONSTRUCCION DE LA LLAVE DEL MSG
		String msgKey = constructMessageKeyISO2ISC(iscInReq, mappedIso);

		//RECUPERACION DE LA CONFIGURACION JSON PARA LA LLAVE
		TransactionSetting tSettings = findTranSetting(transMsgsConfig, msgKey, true);
		
		//VERIFICAR SI LA TRANSACCION TIENE CLASE AUXILIAR
		if (tSettings != null && tSettings.getAuxiliarClass() != null) {
			
			verifyForAuxClass(mappedIso, iscInReq, tSettings, cons);
		}

		
		try {
			mappedIso = constructMsgISC(tSettings, iscInReq, mappedIso);
		}
		catch(Exception e) {
			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter("ERROR COPYING FIELD: " +  outError.toString()));
			Logger.logLine("ERROR COPYING FIELD: " + outError.toString(), true);
		}

		Logger.logLine("OUTPUT:" + mappedIso.toString(), true);
		return mappedIso;
	}
	
	
	private static String constructMessageKeyISO2ISC(ISCReqInMsg msg, Iso8583Post output) {

		Logger.logLine("Utils 3353: \n" + msg.toString(), true);

		String msgKey = "";

		Logger.logLine("TRAN CODE: " + Transform.fromEbcdicToAscii(msg.getField(ISCReqInMsg.Fields._02_H_TRAN_CODE)), true);
		Logger.logLine("AUTRA CODE: " + Transform.fromEbcdicToAscii(msg.getField(ISCReqInMsg.Fields._04_H_AUTRA_CODE)), true);		
		
		if (Transform.fromEbcdicToAscii(msg.getField(ISCReqInMsg.Fields._02_H_TRAN_CODE)).equals("SRLN")) {

			switch (Transform.fromEbcdicToAscii(msg.getField(ISCReqInMsg.Fields._04_H_AUTRA_CODE))) {
			case "8520":

				msgKey = msgKey.concat("_").concat("8520");

				break;
			case "8550":
				
//				msgKey = get8850ExtentedKey(msg);

				break;

			default:

				msgKey = "06";

				break;
			}

		}

		return msgKey;
	}
	

	private static void verifyForAuxClass(ISCReqMessage out, Iso8583Post in, TransactionSetting tSettings, String cons) {
		
		Logger.logLine("postilion.realtime.iscinterface.auxiliar." + tSettings.getAuxiliarClass(), true);
		Logger.logLine(TransferAux.class.getCanonicalName(), true);

		try {
			
//			Class<?> classRequest = Class.forName("postilion.realtime.iscinterface.auxliar.".concat(tSettings.getAuxiliarClass()));	
			Class<?> classRequest = Class.forName("postilion.realtime.iscinterface.auxiliar." + tSettings.getAuxiliarClass());
            Class<?>[] argtypes = { Iso8583Post.class, ISCReqInMsg.class, TransactionSetting.class, String.class };

            Constructor<?> constructor = classRequest.getConstructor();
            Object obj = constructor.newInstance();
            
            Method methodExec = classRequest.getMethod("processMsg", argtypes);
            Object[] args = {out, in,  tSettings, cons};
            
            out = (ISCReqMessage)methodExec.invoke(obj, args);
            
            Logger.logLine(out.toString(), true);
			
			
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			
			Logger.logLine("ERROR REFLECTION: " + e.getLocalizedMessage(), true);
		}        
			
	}
	
	private static Iso8583Post constructMsgISC(TransactionSetting trSetting, ISCReqInMsg inputMsg, Iso8583Post mappedMsg)
			throws XPostilion, FileNotFoundException {
		
		Logger.logLine("constructMsgISO 3251:" + trSetting.getFields().length, true);
		Logger.logLine("constructMsgISO 3252:" + inputMsg.getTotalHexString(), true);

		
		StructuredData sd = null;
		
		if (mappedMsg.getStructuredData() != null) {
			sd = mappedMsg.getStructuredData();
		}
		else {
			sd = new StructuredData();
		}

		for (int i = 0; i < trSetting.getFields().length; i++) {

			Field cf = trSetting.getFields()[i];
			
			Logger.logLine("Field: " + cf.getDescription(), true);

//			mappedMsg.putField(Byte.parseByte(cf.getCopyTag()),
//					inputMsg.getField(cf.getCopyTag()).substring(cf.getCopyInitialIndex(), cf.getCopyFinalIndex()));
			
			switch (cf.getFieldType()) {
			
			case "fixed":
				
//				fixedField(cf, mappedMsg, sd);
				
				break;
				
			case "copy":
				
//				copyField(cf, mappedMsg, inputMsg, sd);
				
				break;
				
//			case "homologate":
//				
//				break;

			default:
				break;
			}
			

		}

		mappedMsg.putStructuredData(sd);

		return mappedMsg;
	}

}
