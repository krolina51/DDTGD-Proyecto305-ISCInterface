package postilion.realtime.iscinterface.util;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;

import postilion.realtime.iscinterface.web.model.Field;
import postilion.realtime.iscinterface.web.model.Homologation;
import postilion.realtime.iscinterface.web.model.TransactionSetting;
import postilion.realtime.iscinterface.web.model.WholeTransSetting;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.message.bitmap.StructuredData;
import postilion.realtime.sdk.message.bitmap.XFieldUnableToConstruct;
import postilion.realtime.sdk.util.XPostilion;
import postilion.realtime.sdk.util.convert.Pack;
import postilion.realtime.sdk.util.convert.Transform;

public class TransactionProcessorUtil {
	
	/**
	 * Mapea el archivo JSON a clases del modelo trabajdo
	 * @param jsonURL
	 * @return
	 */
	public static WholeTransSetting mapSettingsFromJsonFile (String jsonURL) {
		
		ObjectMapper mapper = new ObjectMapper();

		// convert JSON string to Book object
		WholeTransSetting wholeTransConfig = null;
		try {
			wholeTransConfig = mapper.readValue(Paths.get(jsonURL).toFile(),
					WholeTransSetting.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return wholeTransConfig;
	}
	
	/**
	 * Este metodo es el encargado de construir la llave del mansaje, dicha llava debe coincidir con la llave de
	 * alguna transacion configurada en el Json.
	 * 
	 * <tipo mensaje>_<processing code>_<canal>_<entidad adquiriente>_<entidad
	 * autorizadora>_<efectivo (0 tarjeta - 1 efectivo)>_<variation>
	 * 
	 * @return
	 * @throws XFieldUnableToConstruct
	 * @throws XPostilion
	 */
 	public static String constructMessageKey(Iso8583Post msg) throws XPostilion {

		String msgTran = "";
		
		StructuredData sd = msg.getStructuredData();
		sd.put("IS_COST_INQUIRY", "FALSE");
		
		msgTran = msg.getMessageType().concat("_").concat(msg.getProcessingCode().toString()).concat("_")
				.concat(msg.getStructuredData().get("B24_Field_41").substring(12, 13)).concat("_")
				.concat("0000").concat("_");

		if (msg.getProcessingCode().getTranType().equals("40")) {

			// ES PSP
			if (msg.getProcessingCode().getToAccount().equals("00")) {
				
				msgTran = msgTran.concat(msg.getStructuredData().get("B24_Field_103").substring(5, 9)).concat("_")
						.concat(msg.getField(Iso8583.Bit._022_POS_ENTRY_MODE) != null
								&& !msg.getField(Iso8583.Bit._022_POS_ENTRY_MODE).equals("010")
								&& !msg.getField(Iso8583.Bit._022_POS_ENTRY_MODE).equals("021") ? "0" : "1");

				String variation = msg.getStructuredData().get("B24_Field_103").substring(0, 1);
				msgTran = msgTran.concat("_").concat(variation);

				// Si es MIXTO o CREDITO
				if (variation.equals("0") || variation.equals("2")) {

					if (Utils.search4covenant(msg)) {
						
						if ((variation.equals("2")
								&& msg.getStructuredData().get("PRIM_COV_PAYMENT_TYPE").equals("0")))
							msgTran = "00";
						else if (msg.getStructuredData().get("PRIM_COV_ACCOUNT_NR").substring(0, 6).equals("940999"))
							msgTran = "05_1";

					} else
						msgTran = "05";

				} else if (variation.equals("1")) {
					
					sd.put("PRIM_COV_ABO", "2");
				}

			}
			// ES DEPOSITO
			else if ((msg.getProcessingCode().getFromAccount().equals("10")
					|| msg.getProcessingCode().getFromAccount().equals("20"))
					&& (msg.getProcessingCode().getToAccount().equals("14")
							|| msg.getProcessingCode().getToAccount().equals("24"))) {
				
				msgTran = msgTran.concat("0000").concat("_")
				.concat("0");
				
				
				String variation = msg.getStructuredData().get("B24_Field_103").substring(2, 3);
				msgTran = msgTran.concat("_").concat(variation);
			}
			
			// TRANSFER
			else if (msg.getStructuredData().get("B24_Field_41").substring(12, 13).equals("7")
					&& (msg.getProcessingCode().getFromAccount().equals("10")
							|| msg.getProcessingCode().getFromAccount().equals("20"))
					&& (msg.getProcessingCode().getToAccount().equals("10")
							|| msg.getProcessingCode().getToAccount().equals("20"))) {
				
				msgTran = msgTran.concat("0000").concat("_")
						.concat("0");

				String variation = msg.getStructuredData().get("B24_Field_103").substring(2, 3);
				msgTran = msgTran.concat("_").concat(variation);
				
			}
			
			// TRANSFER CNB
			else if (msg.getStructuredData().get("B24_Field_41").substring(12, 13).equals("2")
					&& (msg.getProcessingCode().getFromAccount().equals("10")
							|| msg.getProcessingCode().getFromAccount().equals("20"))
					&& (msg.getProcessingCode().getToAccount().equals("10")
							|| msg.getProcessingCode().getToAccount().equals("20"))) {
				
				// POR REVISAR
				if(msg.getStructuredData().get("B24_Field_41").substring(4, 8).equals("8354")
						|| msg.getStructuredData().get("B24_Field_41").substring(4, 8).equals("8206")
						|| msg.getStructuredData().get("B24_Field_41").substring(4, 8).equals("8110")
						|| msg.getStructuredData().get("B24_Field_41").substring(4, 8).equals("9631")
						|| msg.getStructuredData().get("B24_Field_41").substring(4, 8).equals("9632")) {
					msgTran = msgTran.concat("0000").concat("_")
							.concat("MASS");
				}
				else {
					msgTran = msgTran.concat("0000").concat("_")
							.concat("0");
				}
				
				if(msg.getStructuredData().get("B24_Field_103").substring(16, 17).equals("5")) {
					msgTran = msgTran.concat("C");
				}


				String variation = msg.getStructuredData().get("B24_Field_103").substring(2, 3);
				msgTran = msgTran.concat("_").concat(variation);
				
			}

		}
		
		else if (msg.getProcessingCode().getTranType().substring(0, 1).equals("3")) {
			
			msgTran = msg.getMessageType().concat("_");
			
			if(msg.getProcessingCode().getTranType().substring(1, 2).equals("2")) {		
				
				if(sd.get("B24_Field_3").equals("890000")) {
					
					String b24F126 = sd.get("B24_Field_126");
					
					Logger.logLine("OPERACION 32: " + b24F126, false);
					
					String realPCode = b24F126.substring(b24F126.indexOf("QT")).substring(8, 14);
					
					sd.put("REAL PCODE", realPCode);
					sd.put("IS_COST_INQUIRY", "TRUE");
					
					msgTran = msgTran.concat(realPCode);
					
				}
				
			}
			else {
				msgTran = msgTran.concat(msg.getProcessingCode().toString());
			}
			
			msgTran = msgTran.concat("_")
					.concat(msg.getStructuredData().get("B24_Field_41").substring(12, 13)).concat("_")
					.concat("0000").concat("_").concat("0000").concat("_")
					.concat("0");
			
		}

		// POBLIG
		else if (msg.getProcessingCode().getTranType().equals("50")) {
			
			msgTran = msgTran.concat("0000").concat("_")
					.concat("0");

			String variation = msg.getStructuredData().get("B24_Field_103").substring(2, 3);
			msgTran = msgTran.concat("_").concat(variation);
		}

		else {
			msgTran = msgTran.concat("0000").concat("_").concat("0");
		}
		
		msg.putStructuredData(sd);

		return msgTran;
	}

 	/**
 	 * Usa la llave de la transaccion para conseguir la configuracion de dicha transaccion
 	 * @param msgTranKey
 	 * @param wholeTransConfig
 	 * @return
 	 */
	public static TransactionSetting findTranSetting(String msgTranKey, WholeTransSetting wholeTransConfig) {

		TransactionSetting tranSetting = null;

		System.out.println("TRANKEY: " + msgTranKey);

		if (wholeTransConfig != null && wholeTransConfig.getAllTran() != null
				&& wholeTransConfig.getAllTran().length != 0) {

			for (int i = 0; i < wholeTransConfig.getAllTran().length; i++) {

				System.out.println("TRANKEY IN JSON: " + wholeTransConfig.getAllTran()[i].getTranKey());

				if (wholeTransConfig.getAllTran()[i].getTranKey().equals(msgTranKey)) {

					System.out.println("-- TRANKEY MATCHED --");
					System.out.println(wholeTransConfig.getAllTran()[i].getDescription());
					tranSetting = wholeTransConfig.getAllTran()[i];
					break;
				}
			}

		}

		return tranSetting;
	}

	/**
	 * Construye el mensaje usandon la configuracion de la transaccion
	 * @param trSetting
	 * @param inputMsg
	 * @param isRev
	 * @return
	 * @throws Exception
	 */
	public static String constructMsgString(TransactionSetting trSetting, Iso8583Post inputMsg, boolean isRev) throws Exception {

		StringBuilder msgStrBuilder = new StringBuilder();
		StructuredData msgSD = inputMsg.getStructuredData();

		Logger.logLine(msgSD.toString(), false);

//		if (trSetting.getPreOps().equals("1")) {
//			getCovenant(inputMsg);
//		}
//
//		else {
//			Logger.logLine("NO SEARCH FOR COVENANT");
//		}

		Logger.logLine("NUEVA IMPL - MSG TO PROCESS: \n" + inputMsg.toString(), false);

		for (int i = 0; i < trSetting.getFields().length; i++) {

			Field curField = trSetting.getFields()[i];

			Logger.logLine("CUR FIELD: " + i + " - " + curField.toString(), false);

			switch (curField.getFieldType()) {
			case "fixed":

				fixedField(curField, msgStrBuilder);

				break;
			case "copy":

				copyField(curField, msgStrBuilder, inputMsg, msgSD);

				break;
			case "homologate":

				homologateField(curField, msgStrBuilder, inputMsg, msgSD);

				break;
			case "method":
				
				// HE COMENTADO LAS LINEAS SIGUEINTES PORQUE SON METODOS QUE MANEJAN ALGUNOS CAMPOS EXCEPCIONALES
				// Y SON UNA DEUDA TECNICA, LA IDEA ES LLEVAR LAS VALIDACIONES HECHAS CON ESTOS CAMPOS DE UNA MANERA MAS
				// ACORDE CON EL MODELO DE CONFIGURACION DE CAMPOS QUE ESTAMOS MANEJANDO

//				if (curField.getTagPrefix().equals("119161"))
//					prepareAcqNetworkTag9161(curField, msgStrBuilder, inputMsg);
//				
//				else if (curField.getTagPrefix().equals("11D140"))
//					prepareTagD140(curField, msgStrBuilder, inputMsg);
//				
//				else if (curField.getTagPrefix().equals("tran-code")) 
//					prepareTranCodeRetiro(curField, msgStrBuilder, inputMsg);
//				
//				else if (curField.getTagPrefix().equals("11E0E2")) 
//					prepareTagE0E2(curField, msgStrBuilder, inputMsg);
//				
//				else if(curField.getTagPrefix().equals("state"))
//					setStateHeaderTag(msgStrBuilder, hour4Check, isNextDay, inputMsg.getStructuredData().get("B24_Field_17"), isRev);

				break;
			default:
				break;
			}

		}

		return msgStrBuilder.toString();

	}
		
	private static void fixedField(Field curField, StringBuilder msgStrBuilder) {

		String extractedVal = "";

		if (curField.isHeaderField()) {
			extractedVal = curField.getValueHex() != null ? curField.getValueHex()
					: UtilidadesMensajeria.asciiToEbcdic(curField.getValue()).toUpperCase();
			System.out.println(extractedVal + "--" + Transform.fromAsciiToEbcdic(extractedVal) + "--"
					+ UtilidadesMensajeria.asciiToEbcdic(extractedVal));
			msgStrBuilder.append(extractedVal);
		}

		else {
			extractedVal = curField.getTagPrefix()
					.concat(UtilidadesMensajeria.asciiToEbcdic(curField.getValue()).toUpperCase());
			System.out.println(extractedVal + "--" + Transform.fromAsciiToEbcdic(extractedVal) + "--"
					+ UtilidadesMensajeria.asciiToEbcdic(extractedVal));
			msgStrBuilder.append(extractedVal);
		}
	}

	private static void copyField(Field curField, StringBuilder msgStrBuilder, Iso8583Post inputMsg, StructuredData msgSD)
			throws XPostilion {

		String extractedVal = "";

		if (curField.getCopyFrom() == 1) {

			System.out.println("COPY ::" + inputMsg.getStructuredData().get(curField.getCopyTag()));

		}

		if (!curField.isHeaderField())
			msgStrBuilder.append(curField.getTagPrefix());

		if (curField.getCopyFrom() == 0)
			extractedVal = (curField.getCopyFinalIndex() == 0) ? inputMsg.getField(curField.getCopyTag())
					: inputMsg.getField(curField.getCopyTag()).substring(curField.getCopyInitialIndex(),
							curField.getCopyFinalIndex());
		else {
			if (msgSD.get(curField.getCopyTag()) != null) {
				extractedVal = (curField.getCopyFinalIndex() == 0) ? msgSD.get(curField.getCopyTag())
						: msgSD.get(curField.getCopyTag()).substring(curField.getCopyInitialIndex(),
								curField.getCopyFinalIndex());
			} else {
				extractedVal = curField.getConditionalVal();
			}

		}	

		System.out.println(extractedVal + "--" + Transform.fromAsciiToEbcdic(extractedVal) + "--"
				+ UtilidadesMensajeria.asciiToEbcdic(extractedVal));

		msgStrBuilder.append(UtilidadesMensajeria
				.asciiToEbcdic(Pack.resize(extractedVal, curField.getTagValueLength(), '0', false)).toUpperCase());
	}

	private static void homologateField(Field curField, StringBuilder msgStrBuilder, Iso8583Post inputMsg,
			StructuredData msgSD) throws XPostilion {

		String extractedVal = "";
		boolean homolMatch = false;

		if (!curField.isHeaderField())
			msgStrBuilder.append(curField.getTagPrefix());

		if (curField.getCopyFrom() == 0)
			extractedVal = (curField.getCopyFinalIndex() == 0) ? inputMsg.getField(curField.getCopyTag())
					: inputMsg.getField(curField.getCopyTag()).substring(curField.getCopyInitialIndex(),
							curField.getCopyFinalIndex());
		else
			extractedVal = (curField.getCopyFinalIndex() == 0) ? msgSD.get(curField.getCopyTag())
					: msgSD.get(curField.getCopyTag()).substring(curField.getCopyInitialIndex(),
							curField.getCopyFinalIndex());	

		System.out.println(extractedVal + "--" + Transform.fromAsciiToEbcdic(extractedVal) + "--"
				+ UtilidadesMensajeria.asciiToEbcdic(extractedVal));

		for (Homologation h : Arrays.asList(curField.getHomologations())) {

			if (h.getValue().equals(extractedVal)) {
				Logger.logLine("HOMOLOGACION MATCH-- homologation: " + h.getValue() + " ExtracVal" + extractedVal
						+ " convertion" + h.getConvertion(), false);
				extractedVal = h.getConvertion();
				homolMatch = true;
				Logger.logLine("VAL homologated: " + extractedVal, false);
				break;
			}

		}
		
		if(!homolMatch) {
			
			extractedVal = curField.getConditionalVal();
			
		}

		msgStrBuilder.append(UtilidadesMensajeria
				.asciiToEbcdic(Pack.resize(extractedVal, curField.getTagValueLength(), '0', false)).toUpperCase());
	}
}
