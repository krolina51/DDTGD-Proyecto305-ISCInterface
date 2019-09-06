package postilion.realtime.iscinterface;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import postilion.realtime.commonclass.FilterSettings;
import postilion.realtime.commonclass.model.ResponseCode;
import postilion.realtime.iscinterface.database.DBHandler;
import postilion.realtime.iscinterface.message.ISCReqMessage;
import postilion.realtime.iscinterface.message.ISCResMessage;
import postilion.realtime.iscinterface.message.KeepAliveMessage;
import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.iscinterface.util.Utils;
import postilion.realtime.sdk.eventrecorder.EventRecorder;
import postilion.realtime.sdk.eventrecorder.events.NodeConnected;
import postilion.realtime.sdk.ipc.SecurityManager;
import postilion.realtime.sdk.message.IMessage;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.message.bitmap.Iso8583Rev93;
import postilion.realtime.sdk.message.bitmap.ProcessingCode;
import postilion.realtime.sdk.message.bitmap.StructuredData;
import postilion.realtime.sdk.message.bitmap.XFieldUnableToConstruct;
import postilion.realtime.sdk.message.stream.StreamMessage;
import postilion.realtime.sdk.node.AInterchangeDriver8583;
import postilion.realtime.sdk.node.AInterchangeDriverEnvironment;
import postilion.realtime.sdk.node.Action;
import postilion.realtime.sdk.util.TimedHashtable;
import postilion.realtime.sdk.util.XPostilion;
import postilion.realtime.sdk.util.convert.Pack;
import postilion.realtime.sdk.util.convert.Transform;

/**********************************************************************
 * Class: ISCInterface This is the Interface's main class This class is
 * reponsible for message interchanging with ISC Authorizator interface.
 * 
 * @author Javier Flores / Cristian Cardozo / Albert Medina
 *
 ***********************************************************************
 */
public class ISCInterface extends AInterchangeDriver8583 {

	long startTime = 0;

	long endTime = 0;

	/**
	 * Contine las configuraciones para hacer buscar los codigos equiavalentes de
	 * ISO8583 a B24
	 */
	private static HashMap<String, ResponseCode> allCodesIscToIso = new HashMap<String, ResponseCode>();

	// Variable que almacena durante un tiempo estipulado los mensajes
	private TimedHashtable transStore = null;

	// Variable que dictamina el tiempo en el tiempo de los msg en "tranRequest"
	private Long internalMaxTimeLimit = 3600000L;

	// Variable que dictamina si la interchange debe funcionar como PassThrough
	// "true" Iso a Iso, "false" Iso a ISC
	private boolean isIsoPassThrough = false;

	// Parametro que indica si la interchange solo enviara respuestas ISC Dummy
	private boolean onlyDummyReq = false;

	// Parametro que indica si la interchange debe consultar el consecutivo
	private boolean dummyConsecutive = false;

	// Mapa donde se alojara toda la configuracion desde la BBDD se apoyo
	public static Map<String, String> allConfig = new HashMap<>();

	// Arreglo donde se alojaran los user parameters de la interchange
	public static String[] userParams = null;

	/***************************************************************************************
	 * Implementación de metodo del SDK, sirve para inicializar
	 ***************************************************************************************/
	public void init(AInterchangeDriverEnvironment interchange) throws XPostilion, Exception {

		String[] parameterArray = getParameters(interchange);

		try {

			this.transStore = new TimedHashtable(this.internalMaxTimeLimit);
			this.isIsoPassThrough = Boolean.valueOf(parameterArray[1]); // recibe ISO y entrega ISo
			this.onlyDummyReq = Boolean.valueOf(parameterArray[2]); // solo enviar ISC dummy msg
			this.dummyConsecutive = Boolean.valueOf(parameterArray[3]); // solo recibir ISC dummy msg

			allCodesIscToIso = postilion.realtime.commonclass.handler.DBHandler.getResponseCodes(true);

			Logger.logLine("INIT");
			if (allCodesIscToIso.size() > 0) {
				for (Map.Entry<String, ResponseCode> e : allCodesIscToIso.entrySet()) {
					Logger.logLine(e.getKey() + "::" + e.getValue().getKeyIso());
				}
			}

		} catch (Exception e) {
			EventRecorder.recordEvent(e);
		}

	}

	/***************************************************************************************
	 * Implementación de metodo del SDK, sirve para procesar un mensaje 0200 desde
	 * TM
	 ***************************************************************************************/
	public Action processTranReqFromTranmgr(AInterchangeDriverEnvironment interchange, Iso8583Post msg)
			throws XFieldUnableToConstruct, XPostilion, Exception {

		// Logger.logLine("MSG desde TM\n"+msg.toString());
		this.startTime = System.currentTimeMillis();

		IMessage msg2Remote = null;
		Iso8583Post msg2TM = null;

		Action act = new Action();

		// Se usa el metodo getDBSettings para traer la configuracion de base de datos
		// para la transaccion
		HashMap<String, String> config = getDBSettings(
				msg.getMessageType().concat("_").concat(msg.getProcessingCode().toString()));

		// Se determina el canal el mismo viene en la posición 13 del Tag "B24_Field_41"
		String canal = (msg.isPrivFieldSet(Iso8583Post.PrivBit._022_STRUCT_DATA)
				&& msg.getStructuredData().get("B24_Field_41") != null)
						? canal = msg.getStructuredData().get("B24_Field_41").substring(12, 13)
						: null;

		// Se determina el tipo de transacción "AAAA_BBBBBB_C"
		// AAAA-Tipo de Msg ; BBBBBB-Codigo proceso ; C-canal
		StringBuilder tranTypeBuilder = new StringBuilder();
		tranTypeBuilder.append(msg.getMessageType()).append("_").append(msg.getProcessingCode().toString()).append("_")
				.append(canal);

		// Se invoca al metodo getTransactionConsecutive a fin de obtener el consecutivo
		// para la transaación
		String cons = this.dummyConsecutive == false
				? getTransactionConsecutive(msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR).substring(5, 9))
				: null;

		// verificación del numero consecutivo
		if (cons == null || cons.trim().equals("")) {

			String errorMsg = "Error recuperando el consecutivo para la transaccion: "
					+ msg.getField(Iso8583Post.Bit._037_RETRIEVAL_REF_NR);
			act.putMsgToTranmgr((Iso8583Post) createErrorRspMsg(msg, errorMsg));
			EventRecorder.recordEvent(new Exception(errorMsg));
			Logger.logLine("ERROR:" + errorMsg);
			
		} else {

			msg = mapISOFieldsInISOMessage(msg);
			msg = addingAdditionalStructuredDataFields(msg, cons);

			if (!this.isIsoPassThrough) {
				switch (tranTypeBuilder.toString()) {
				case _TRAN_RETIRO_AHORRO:
					msg2Remote = processWithdrawal(msg);
					act = new Action(null, msg2Remote, null, null);
					break;
				case _TRAN_RETIRO_CORRIENTE:
					msg2Remote = processWithdrawal(msg);
					act = new Action(null, msg2Remote, null, null);
					break;
				case _TRAN_COMPRA_AHORRO:
					msg2TM = dummyApprobedGoodAndServices(msg);
					act = new Action(msg2TM, null, null, null);
					break;
				case _TRAN_COMPRA_CORRIENTE:
					msg2TM = dummyApprobedGoodAndServices(msg);
					act = new Action(msg2TM, null, null, null);
					break;
				default:
					Logger.logLine("Tipo de Transaccion no coincide");
					EventRecorder.recordEvent(new Exception("Tipo de Transaccion no coincide ["
							+ tranTypeBuilder.toString() + "][" + _TRAN_RETIRO_AHORRO + "]"));
				}
			} 
			else {
				msg2Remote = msg;
			}
		}

		// Agrega msg a map de mensajes usando como key la llave alojada en "tranKey"
		StructuredData sd = msg.getStructuredData();
		sd.put("I2_REQ_TIME", String.valueOf(System.currentTimeMillis() - this.startTime));
		msg.putStructuredData(sd);
		this.transStore.put(cons, msg);

		Logger.logLine("[MSG][OutFromTM] \n" + msg.toString());
		Logger.logLine("[MSG][OutFromTM SD] \n" + msg.getStructuredData());
		// Logger.logLine("[MSG][Out2Remote] \n"+msg2Remote.toString());

		Logger.logLine(
				"========================================\n========================================\n========================================\n");

		return act;
	}

	/**
	 * Implementación de metodo del SDK, toma msj de respuesta remoto para
	 * procesarlo y envia un 0210 al TM
	 */
	@Override
	public Action processTranReqRspFromInterchange(AInterchangeDriverEnvironment interchange, Iso8583 msg)
			throws XPostilion {

		Logger.logLine("processTranReqRspFromInterchange");
		this.startTime = System.currentTimeMillis();
		Iso8583Post msg2TM = null;

		Action act = new Action();

		if (msg != null) {
			msg2TM = new Iso8583Post();
			msg2TM = (Iso8583Post) msg;

			StructuredData sd = msg2TM.getStructuredData();

			if (!msg2TM.isFieldSet(Iso8583.Bit._038_AUTH_ID_RSP))
				msg2TM.putField(Iso8583.Bit._038_AUTH_ID_RSP, sd.get("SEQ_TERMINAL").substring(2));

			ResponseCode responseCode = FilterSettings.getFilterCodeISCToIso(
					msg2TM.getStructuredData().get("ERROR") == null ? "0000" : msg2TM.getStructuredData().get("ERROR"),
					allCodesIscToIso);
			msg2TM.putField(Iso8583.Bit._039_RSP_CODE, responseCode.getKeyIso());

			sd.put("B24_Field_126",
					sd.get("COMISION") != null
							? sd.get("B24_Field_126").substring(0, sd.get("B24_Field_126").length() - 14)
									.concat(Pack.resize(sd.get("COMISION").replace(".", ""), 12, '0', false))
									.concat(sd.get("B24_Field_126").substring(sd.get("B24_Field_126").length() - 2))
							: sd.get("B24_Field_126"));
			sd.put("B24_Field_63",
					responseCode.getKeyIsc().concat(Pack.resize(responseCode.getDescriptionIsc(), 44, ' ', true)));

			sd.put("B24_Field_40", "000");
			if (sd.get("SALDO_DISPONIBLE") != null) {
				sd.put("B24_Field_44", "2000000000000".concat(
						Pack.resize(sd.get("SALDO_DISPONIBLE").replace(",", "").replace(".", ""), 12, '0', false)));
			} else {
				sd.put("B24_Field_44", "2000000000000000000000000");
			}

			sd.put("I2_RSP_TIME", String.valueOf(System.currentTimeMillis() - this.startTime));

			msg2TM.putStructuredData(sd);
			act.putMsgToTranmgr(msg2TM);
		} else {
			act = new Action();
		}

		Logger.logLine("[MSG][OutToTM] \n" + msg2TM.toString());
		Logger.logLine("[MSG][OutToTM SD] \n" + msg2TM.getStructuredData());
		Logger.logLine(
				"================================\n================================\n================================\n");
		return act;
	}

	@Override
	public Action processNwrkMngAdvFromInterchange(AInterchangeDriverEnvironment interchange, Iso8583 msg)
			throws Exception {

		Iso8583Post networkAdv = (Iso8583Post) msg;
		Action action = new Action();
		action.putMsgToTranmgr(networkAdv);

		return action;
	}

	/**
	 * Procesar reversos desde TM
	 */
	@Override
	public Action processAcquirerRevAdvFromTranmgr(AInterchangeDriverEnvironment interchange, Iso8583Post msg)
			throws Exception {
		Action action = new Action();

		Logger.logLine("Procesando reverso | F37:".concat(msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR))
				.concat(" F38:").concat(msg.getField(Iso8583.Bit._038_AUTH_ID_RSP)));

		this.transStore.put("AT".concat(msg.getField(Iso8583.Bit._038_AUTH_ID_RSP)), msg);

		ISCReqMessage reverso = (ISCReqMessage) processReverse(msg);
		action.putMsgToRemote(reverso);

		return action;
	}

	@Override
	public Action processAcquirerRevAdvRspFromInterchange(AInterchangeDriverEnvironment interchange, Iso8583 msg)
			throws Exception {
		Iso8583Post inMsg = (Iso8583Post) msg;
		Logger.logLine("processAcquirerRevReqRspFromInterchange\n" + inMsg);
		Action action = new Action();
		action.putMsgToTranmgr(inMsg);

		return action;
	}

	public IMessage processWithdrawal(Iso8583Post msgIn) throws XPostilion, CloneNotSupportedException {

		Logger.logLine("**Procesando retiro**");

		IMessage msgToRemote = null;

		if (this.onlyDummyReq) {
			Logger.logLine("**DUMMY REQUEST HABILIDATA**");
			msgToRemote = Utils.constructTestStreamReqMsg(msgIn.getStructuredData().get("SEQ_TERMINAL"));
		} else {
			Logger.logLine("**DUMMY REQUEST DESHABILITADA**");
			msgToRemote = requestISOFields2ISCFields(msgIn);
		}

		return msgToRemote;
	}

	public IMessage processReverse(Iso8583Post msgIn) throws XPostilion {
		Logger.logLine("**Procesando reverso**");

		IMessage msgToRemote = null;
		msgToRemote = reverseISOFields2ISCFields(msgIn);
		return msgToRemote;
	}
	
	private Iso8583Post dummyApprobedGoodAndServices(Iso8583Post inMsg) throws XPostilion{
		Iso8583Post approbedEcho = inMsg;
		approbedEcho.setMessageType(Iso8583.MsgTypeStr._0210_TRAN_REQ_RSP);
		approbedEcho.putField(Iso8583.Bit._039_RSP_CODE, Iso8583.RspCode._00_SUCCESSFUL);
		approbedEcho.putField(Iso8583.Bit._038_AUTH_ID_RSP, Utils.getStringDate().substring(6));
		return approbedEcho;
	}

	@Override
	public IMessage newMsg(byte[] data) throws Exception {

		Logger.logLine("**Recibiendo nuevo mensaje de respuesta**");

		// Logger.logLine("**Data super\n**"+super.newMsg(data) == null? "-->" :
		// super.newMsg(data).toString());

		Iso8583Post rspISOMsg = null;

		if (this.isIsoPassThrough) {
			Logger.logLine("Processing input frame");
			rspISOMsg = new Iso8583Post();
			rspISOMsg.fromMsg(data);

			Logger.logLine("[MSG][ISO] \n" + rspISOMsg.toString());
			return rspISOMsg;
		} else {

			Logger.logLine("Processing message from interchange! ISC");
			// Solo se intenta procesar el mensaje si la trama tiene una longitud superior a
			// 60

			Logger.logLine("Trama de longitud correcta");
			String trama = Utils.asciiToEbcdic(Transform.getString(data, Transform.Encoding.EBCDIC)).toUpperCase();

			Logger.logLine("DATA RECIBIDA:\n" + trama);

			if (trama.length() > 60) {
				ISCResMessage msgISCMsg = new ISCResMessage();
				msgISCMsg.fromMsg(trama);
				rspISOMsg = mapISCFields2ISOFields(msgISCMsg);
			} else {
				KeepAliveMessage msgISCMsg = new KeepAliveMessage();
				msgISCMsg.fromMsg(trama);
				rspISOMsg = createNetworkAdv(msgISCMsg);
			}

			// Logger.logLine("[MSG][ISCmapped] \n" + rspISOMsg.toString());
			// Logger.logLine("SD:" + rspISOMsg.getStructuredData());
			return rspISOMsg;

			// mapISCFields2ISOFields(rspISCMsg);

		}

		// return rspISOMsg;
		// return super.newMsg(data);

	}

	public Iso8583 createErrorRspMsg(Iso8583Post inMsg, String errorMsg) throws XPostilion {

		inMsg.setMessageType(Iso8583.MsgTypeStr._0210_TRAN_REQ_RSP);
		inMsg.putField(Iso8583.Bit._039_RSP_CODE, Iso8583.RspCode._06_ERROR);
		StructuredData sd = inMsg.isPrivFieldSet(Iso8583Post.PrivBit._022_STRUCT_DATA) ? inMsg.getStructuredData()
				: new StructuredData();
		sd.put("ERROR", errorMsg);
		inMsg.putStructuredData(sd);

		return inMsg;
	}

	/***************************************************************************************
	 * Metodo auxiliar de uso interno que permite recuperar los parametros de
	 * usuario configurados para la interchange
	 * 
	 * @param interchange
	 * @return
	 ***************************************************************************************/
	private String[] getParameters(AInterchangeDriverEnvironment interchange) {

		String[] interchangeParams = Pack.splitParams(interchange.getUserParameter());
		return interchangeParams;
	}

	/**************************************************************************************
	 * Metodo para consultar consecutivo para la transacción
	 * 
	 * @param atmId
	 * @return
	 **************************************************************************************/
	private String getTransactionConsecutive(String termPrefix) {
		String output = null;

		// To-DO consultar consecutivo
		try {
			String[] spResult = DBHandler.getCalculateConsecutive("AT").split(",");
			output = spResult[0].trim().concat(spResult[1].trim());

		} catch (Exception e) {
			Logger.logLine(e.getMessage());
		}

		return output;
	}

	/**************************************************************************************
	 * Metodo para ajustar campos desde el Iso8583Post con data de prueba en caso de
	 * ser requerido
	 * 
	 * @param msgFromTm
	 * @return
	 * @throws XPostilion
	 * @throws CloneNotSupportedException
	 **************************************************************************************/
	private Iso8583Post mapISOFieldsInISOMessage(Iso8583Post msgFromTm) throws XPostilion, CloneNotSupportedException {

		Logger.logLine("Mapeando campos de ISO a ISO");

		msgFromTm.setMessageType(Iso8583.MsgTypeStr._0200_TRAN_REQ);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._002_PAN, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._003_PROCESSING_CODE, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._004_AMOUNT_TRANSACTION, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._007_TRANSMISSION_DATE_TIME, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._011_SYSTEMS_TRACE_AUDIT_NR, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._012_TIME_LOCAL, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._013_DATE_LOCAL, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._014_DATE_EXPIRATION, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._015_DATE_SETTLE, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._022_POS_ENTRY_MODE, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._025_POS_CONDITION_CODE, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._026_POS_PIN_CAPTURE_CODE, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._028_AMOUNT_TRAN_FEE, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._030_AMOUNT_TRAN_PROC_FEE, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._032_ACQUIRING_INST_ID_CODE, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._035_TRACK_2_DATA, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._037_RETRIEVAL_REF_NR, msgFromTm);
		msgFromTm.putField(Iso8583Post.Bit._040_SERVICE_RESTRICTION_CODE, "000");
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._041_CARD_ACCEPTOR_TERM_ID, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._042_CARD_ACCEPTOR_ID_CODE, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._043_CARD_ACCEPTOR_NAME_LOC, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._049_CURRENCY_CODE_TRAN, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._056_MSG_REASON_CODE, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._102_ACCOUNT_ID_1, msgFromTm);
		msgFromTm.copyFieldFrom(Iso8583Post.Bit._123_POS_DATA_CODE, msgFromTm);
		msgFromTm.copyPrivFieldFrom(Iso8583Post.PrivBit._002_SWITCH_KEY, msgFromTm);
		msgFromTm.copyPrivFieldFrom(Iso8583Post.PrivBit._003_ROUTING_INFO, msgFromTm);
		msgFromTm.copyPrivFieldFrom(Iso8583Post.PrivBit._020_AUTHORIZER_DATE_SETTLEMENT, msgFromTm);

		return msgFromTm;

	}

	private Iso8583Post mapISCFields2ISOFields(ISCResMessage msgFromInter) throws XPostilion {
		Logger.logLine("***Mapeando ISC a ISO***");
		Logger.logLine("***Mj a ser mapeado***\n" + msgFromInter.toString());
		Map<String, String> bodyFields = new HashMap<String, String>();
		List<String> errors = new ArrayList<>();
		
		String state = msgFromInter.getField(ISCResMessage.Fields._05_H_STATE);
		
		//Switch para basado en el estado de la respuesta de la transacción (Byte de estado response)
		switch (state) {
		case _APPROVAL_STATE_HEX:		
			bodyFields.putAll(Utils.getBodyInnerFields(msgFromInter.getField(ISCReqMessage.Fields._VARIABLE_BODY),
					REGEX_VARIABLE, OUTPUT_TEMPLATE, DELIMITADOR));
			if (bodyFields.size() < 1) {
				bodyFields.putAll(Utils.getBodyInnerFields(msgFromInter.getField(ISCReqMessage.Fields._VARIABLE_BODY),
						REGEX_VARIABLE_2, OUTPUT_TEMPLATE_2, DELIMITADOR));
			}	
			break;
		default:
			errors.addAll(Utils.getErrorsFromResponse(REGEX_ERROR, Transform.fromEbcdicToAscii(Transform.fromHexToBin(msgFromInter.getField(ISCReqMessage.Fields._VARIABLE_BODY)))));
			bodyFields.put("ERROR", (errors.isEmpty() || errors.size() == 0)? "10002": errors.get(0));	
			break;
		}

		String tranKey = Utils.ebcdicToAscii(msgFromInter.getField(ISCResMessage.Fields._06_H_ATM_ID)).trim()
				.concat(Utils.ebcdicToAscii(msgFromInter.getField(ISCResMessage.Fields._07_H_TRAN_SEQ_NR)).trim());

		Logger.logLine("***TRAN KEY***" + tranKey);
		Logger.logLine("***MAPA Contiene TRAN KEY***" + this.transStore.containsKey(tranKey));
		Logger.logLine("***MAPA Contiene***" + this.transStore.toString());

		Iso8583Post originalMsgReq = (Iso8583Post) this.transStore.get(tranKey);
		if (originalMsgReq != null) {
			this.transStore.remove(tranKey);

			int msgType = originalMsgReq.getMsgType();

			Logger.logLine("***ORIGINAL MSG***" + msgType);

			switch (msgType) {
			case Iso8583.MsgType._0200_TRAN_REQ:
				originalMsgReq.setMessageType(Iso8583.MsgTypeStr._0210_TRAN_REQ_RSP);
				break;
			case Iso8583.MsgType._0420_ACQUIRER_REV_ADV:
				originalMsgReq.setMessageType(Iso8583.MsgTypeStr._0430_ACQUIRER_REV_ADV_RSP);
				break;
			case Iso8583.MsgType._0421_ACQUIRER_REV_ADV_REP:
				originalMsgReq.setMessageType(Iso8583.MsgTypeStr._0430_ACQUIRER_REV_ADV_RSP);
				break;
			}

			StructuredData origSD = originalMsgReq.getStructuredData();

			for (Map.Entry<String, String> entry : bodyFields.entrySet()) {
				if (entry.getValue() != null && !entry.getValue().equals(" ") && !entry.getValue().equals("")) {
					Logger.logLine(entry.getKey().toUpperCase() + " :: "
							+ entry.getValue().replaceAll("\u0000", "").replaceAll("\u001c", "").replaceAll("\n", "")
									.replaceAll("\t", "").replaceAll("\n", "").replaceAll("\u0001", "")
									.replaceAll("\u0011", "").replaceAll("\u002F", ""));
					origSD.put(entry.getKey().toUpperCase(),
							entry.getValue().replaceAll("\u0000", "").replaceAll("\u001c", "").replaceAll("\n", "")
									.replaceAll("\t", "").replaceAll("\n", "").replaceAll("\u0001", "")
									.replaceAll("\u0011", "").replaceAll("\u002F", ""));
				}
			}

			origSD.put("ADITIONAL_ERRORS", Utils.list2String(errors, _CHAR_PIPE));
			originalMsgReq.putStructuredData(origSD);

		}

		return originalMsgReq;

	}

	private Iso8583Post createNetworkAdv(KeepAliveMessage msgFromInter) throws XPostilion {
		Iso8583Post networkAdv = new Iso8583Post();

		String date = Utils.getStringDate();

		networkAdv.setMessageType(Iso8583.MsgTypeStr._0820_NWRK_MNG_ADV);
		networkAdv.putField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME, date.substring(2));
		networkAdv.putField(Iso8583.Bit._008_AMOUNT_CARDHOLDER_BILL_FEE, String.valueOf(System.currentTimeMillis())
				.substring(String.valueOf(System.currentTimeMillis()).length() - 6));
		networkAdv.putField(Iso8583.Bit._012_TIME_LOCAL, date.substring(6));
		networkAdv.putField(Iso8583.Bit._013_DATE_LOCAL, date.substring(2, 6));
		networkAdv.putField(Iso8583.Bit._070_NETWORK_MNG_INFO_CODE, "301");
		networkAdv.putField(Iso8583.Bit._100_RECEIVING_INST_ID_CODE, "11111111111");
		networkAdv.putField(Iso8583Post.PrivBit._002_SWITCH_KEY, date);

		return networkAdv;
	}

	/**************************************************************************************
	 * Construye StreamMessage de prueba con la estructura ISCReqMessage el mismo
	 * podrá ser enviado a la entidad remota para efectos de prueba
	 * 
	 * @return
	 * @throws XPostilion
	 **************************************************************************************/
	private ISCReqMessage requestISOFields2ISCFields(Iso8583Post inputMsg) throws XPostilion {
		Logger.logLine("Mapeando campos de ISO a ISC :: RETIRO");

		ISCReqMessage output = new ISCReqMessage();
		output.setConstantHeaderFields();

		output.putField(ISCReqMessage.Fields._05_H_TRAN_CODE,
				inputMsg.getField(Iso8583Post.Bit._002_PAN).substring(0, 6).equals("450942")
						? Transform.fromAsciiToEbcdic("ATWV")
						: Transform.fromAsciiToEbcdic("ATWI"));

		output.putField(ISCReqMessage.Fields._06_H_ATM_ID,
				Transform.fromAsciiToEbcdic(inputMsg.getStructuredData().get("SEQ_TERMINAL").substring(0, 4)));

		output.putField(ISCReqMessage.Fields._08_H_TRAN_SEQ_NR,
				Transform.fromAsciiToEbcdic(inputMsg.getStructuredData().get("SEQ_TERMINAL").substring(4, 8)));

		output.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("008"));

		output.putField(ISCReqMessage.Fields._10_H_TIME,
				Transform.fromAsciiToEbcdic(inputMsg.getField(Iso8583Post.Bit._012_TIME_LOCAL)));

		output.putField(ISCReqMessage.Fields._VARIABLE_BODY, Utils.prepareVariableReqBody(inputMsg, 0));

		Logger.logLine("DATA ISC REQUEST MSG ISC:\n" + Transform.fromBinToHex(output.getTotalString())
				+ "\n===========================\n===========================\n");

		return output;
	}

	/**************************************************************************************
	 * Construye StreamMessage de prueba con la estructura ISCReqMessage el mismo
	 * podrá ser enviado a la entidad remota para efectos de prueba
	 * 
	 * @return
	 * @throws XPostilion
	 **************************************************************************************/
	private ISCReqMessage reverseISOFields2ISCFields(Iso8583Post inputMsg) throws XPostilion {
		Logger.logLine("Mapeando campos de ISO a ISC :: REVERSO");

		ISCReqMessage output = new ISCReqMessage();
		output.setConstantHeaderFields();

		output.putField(ISCReqMessage.Fields._05_H_TRAN_CODE,
				inputMsg.getField(Iso8583Post.Bit._002_PAN).substring(0, 6).equals("450942")
						? Transform.fromAsciiToEbcdic("ATWV")
						: Transform.fromAsciiToEbcdic("ATWI"));

		output.putField(ISCReqMessage.Fields._06_H_ATM_ID, Transform
				.fromAsciiToEbcdic("AT".concat(inputMsg.getField(Iso8583.Bit._038_AUTH_ID_RSP).substring(0, 2))));

		output.putField(ISCReqMessage.Fields._08_H_TRAN_SEQ_NR,
				Transform.fromAsciiToEbcdic(inputMsg.getField(Iso8583.Bit._038_AUTH_ID_RSP).substring(2)));

		output.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("188"));

		output.putField(ISCReqMessage.Fields._10_H_TIME,
				Transform.fromAsciiToEbcdic(inputMsg.getField(Iso8583Post.Bit._012_TIME_LOCAL)));

		output.putField(ISCReqMessage.Fields._VARIABLE_BODY, Utils.prepareVariableReqBody(inputMsg, 1));

		Logger.logLine("DATA ISC REVERSE MSG ISC:\n" + Transform.fromBinToHex(output.getTotalString())
				+ "\n===========================\n===========================\n");

		return output;
	}

	private Iso8583Post addingAdditionalStructuredDataFields(Iso8583Post inMsg, String consecutive) throws XPostilion {

		Logger.logLine("--->48::" + inMsg.toString() + "\nSD:" + inMsg.getStructuredData());
		StructuredData sd = inMsg.getStructuredData();

		String account_info = "";

		try {
			account_info = DBHandler.getAccountInfo(1, "4573210000096242", inMsg.getProcessingCode().getFromAccount());
			Logger.logLine("ACCOUNT INFO:" + account_info);
		} catch (SQLException e) {
			EventRecorder.recordEvent(e);
			e.printStackTrace();
		}

		// Campo 40
		sd.put("B24_Field_40", "000");

		// Campo privado 41
		if (sd.get("B24_Field_41") == null)
			sd.put("B24_Field_41", "0054232100000   "); // PRUEBA
		else
			sd.put("B24_Field_41", sd.get("B24_Field_41"));

		// Campo privado 48
		if (sd.get("B24_Field_48") == null)
			sd.put("B24_Field_48", "00520000000030000000000000000000000000000000"); // PRUEBA
		else {
			sd.put("B24_Field_48", sd.get("B24_Field_48"));

			if (sd.get("B24_Field_48").substring(sd.get("B24_Field_48").length() - 1).equals("1")) {

				sd.put("TRANSACTION_AMOUNT", sd.get("B24_Field_54").substring(0, 12));
				sd.put("DONATION_AMOUNT", sd.get("B24_Field_54").substring(12, 24));
				sd.put("SECURE_AMOUNT", sd.get("B24_Field_54").substring(24, 36));

				sd.put("SETTER_AMOUNT", Integer.valueOf(sd.get("TRANSACTION_AMOUNT")) == 0 ? "0" : "1");
				sd.put("SETTER_AMOUNT",
						Integer.valueOf(sd.get("DONATION_AMOUNT")) == 0 ? sd.get("SETTER_AMOUNT").concat("0")
								: sd.get("SETTER_AMOUNT").concat("1"));
				sd.put("SETTER_AMOUNT",
						Integer.valueOf(sd.get("SECURE_AMOUNT")) == 0 ? sd.get("SETTER_AMOUNT").concat("0")
								: sd.get("SETTER_AMOUNT").concat("1"));
			} else {
				sd.put("B24_Field_54",
						inMsg.getField(Iso8583.Bit._004_AMOUNT_TRANSACTION).concat("000000000000000000000000"));
				sd.put("TRANSACTION_AMOUNT", sd.get("B24_Field_54").substring(0, 12));

				int thousandUnits = Integer.parseInt(sd.get("TRANSACTION_AMOUNT").substring(6, 7));

				if (thousandUnits == 1 || thousandUnits == 2 || thousandUnits == 5) {
					sd.put("TRANSACTION_AMOUNT", sd.get("TRANSACTION_AMOUNT").substring(0, 6).concat("000000"));
					sd.put("DONATION_AMOUNT",
							Pack.resize(String.valueOf(thousandUnits * 1000).concat("00"), 12, '0', false));
				} else {
					sd.put("DONATION_AMOUNT", sd.get("B24_Field_54").substring(12, 24));
				}

				sd.put("SECURE_AMOUNT", sd.get("B24_Field_54").substring(24, 36));

				sd.put("SETTER_AMOUNT", Integer.valueOf(sd.get("TRANSACTION_AMOUNT")) == 0 ? "0" : "1");
				sd.put("SETTER_AMOUNT",
						Integer.valueOf(sd.get("DONATION_AMOUNT")) == 0 ? sd.get("SETTER_AMOUNT").concat("0")
								: sd.get("SETTER_AMOUNT").concat("1"));
				sd.put("SETTER_AMOUNT",
						Integer.valueOf(sd.get("SECURE_AMOUNT")) == 0 ? sd.get("SETTER_AMOUNT").concat("0")
								: sd.get("SETTER_AMOUNT").concat("1"));
			}

		}

		// Campo privado 126
		if (sd.get("B24_Field_126") == null)
			sd.put("B24_Field_126", "& 0000200054! QT00032 0110000000000000000000000000000"); // PRUEBA

		// Campo privado control base 24
		sd.put("_TOKEN_CONTROL_B24", "01");

		// Campo privado source
		sd.put("SOURCE", "ATH");

		// Campo privado para secuencia de la transacción
		sd.put("SEQ_TERMINAL", consecutive);

		// Campo privado para secuencia de la transacción
		sd.put("RETRIEVED_ACCOUNT", inMsg.getField(Iso8583.Bit._102_ACCOUNT_ID_1));

		inMsg.putStructuredData(sd);

		String originalMsgB64 = Base64.getEncoder().encodeToString(inMsg.toString().getBytes());
		sd.put("ORIGINAL_MSG", originalMsgB64);

		inMsg.putStructuredData(sd);

		return inMsg;
	}

	/**************************************************************************************
	 * Metodo que será usado para la verificación de las tarjetas/cuentas en
	 * PostCard
	 * 
	 * @param cardNr
	 * @param accountType
	 * @param accountNr
	 * @throws Exception
	 **************************************************************************************/
	private void checkCardAccountsPostcard(String cardNr) throws Exception {

		// TO-DO debe utilizar los mecanismos para ir a postcard y validar el tipo de
		// cuenta correct
		SecurityManager sm = new SecurityManager();
		String panRef = sm.getPanReference(cardNr);
		String cons = getTransactionConsecutive(panRef);

	}

	/**************************************************************************************
	 * Metodo que sirve para recuperar la configuración asociada a la transacción
	 * 
	 * @return
	 **************************************************************************************/
	private HashMap<String, String> getDBSettings(String configKey) {

		Map<String, String> config = new HashMap<>();
		return (HashMap<String, String>) config;

	}

	private String getOriginalConsecutive(String origSwitchKey) {
		return origSwitchKey;
	}

	public Action processOpenCommand(AInterchangeDriverEnvironment interchange) throws Exception {
		return new Action(null, null, Action.Comms.CONNECT, null);
	}

	@Override
	public Action processResyncCommand(AInterchangeDriverEnvironment interchange) throws Exception {
		this.init(interchange);
		return super.processResyncCommand(interchange);
	}

	public Action processInterchangeConnected() {
		new NodeConnected(new Object[] { "Nodo Conectado" });

		return new Action();
	}

	////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// SECCION DE PRUEBA Y
	//////////////////////////////////////////////////////////////////////////////////////// TEST////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////

	/**************************************************************************************
	 * Metodo "main" solo para efectos de pruebas DEBE SER ELIMINADO de la version
	 * final
	 * 
	 * @param args
	 * @throws XPostilion
	 **************************************************************************************/
	public static void main(String[] args) throws XPostilion {

		/* ISCReqMessage resStream = constructTestStreamReqMsg(); */
		// Action action = new Action();

		// Iso8583Post postMsg = constructTestIsoPostReqMsg();
		// ISCReqMessage iscStreamMsg = mapFieldsFromReq(postMsg);
		//
		// System.out.println(postMsg.toString());
		// System.out.println("============================================================");
		// System.out.println("============================================================");
		// System.out.println(constructTestStreamReqMsg().getTotalString());
	}

	/**************************************************************************************
	 * Construye un mensaje Iso8583Post usando la estructura proporcionada por el
	 * SDK el mismo podrá ser usado para emular el mensaje recibido desde TM
	 * 
	 * @return
	 * @throws XPostilion
	 **************************************************************************************/
	private static Iso8583Post constructTestIsoPostReqMsg() throws XPostilion {
		Iso8583Post output = new Iso8583Post();

		String date = Utils.getStringDate();

		output.putMsgType(Iso8583.MsgType._0210_TRAN_REQ_RSP);
		output.putProcessingCode(new ProcessingCode("011000"));
		output.putField(Iso8583.Bit._004_AMOUNT_TRANSACTION, "000000010000");
		output.putField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME, date.substring(2));
		output.putField(Iso8583.Bit._011_SYSTEMS_TRACE_AUDIT_NR, date.substring(6));
		output.putField(Iso8583.Bit._012_TIME_LOCAL, date.substring(6));
		output.putField(Iso8583.Bit._013_DATE_LOCAL, date.substring(2, 6));
		output.putField(Iso8583.Bit._015_DATE_SETTLE, date.substring(2, 6));
		output.putField(Iso8583.Bit._017_DATE_CAPTURE, date.substring(2, 6));
		output.putField(Iso8583.Bit._022_POS_ENTRY_MODE, "051");
		output.putField(Iso8583.Bit._032_ACQUIRING_INST_ID_CODE, "10000000054");
		output.putField(Iso8583.Bit._035_TRACK_2_DATA, "4509420110016287=15122211264674400000");
		output.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, "00" + date.substring(2));
		output.putField(Iso8583.Bit._041_CARD_ACCEPTOR_TERM_ID, "00543113");
		output.putField(Iso8583.Bit._042_CARD_ACCEPTOR_ID_CODE, "               ");
		output.putField(Iso8583.Bit._043_CARD_ACCEPTOR_NAME_LOC, "ATH  B.AVV  LABORA 16  100100BOGOT CUNCO");
		output.putField(Iso8583.Bit._048_ADDITIONAL_DATA, "300000000000000000000000300");
		output.putField(Iso8583.Bit._049_CURRENCY_CODE_TRAN, "170");
		output.putField(Iso8583.Bit._052_PIN_DATA, "C5692DEE");
		output.putField(Iso8583.Bit._054_ADDITIONAL_AMOUNTS, "1002170D000000010000");
		output.putField(Iso8583.Bit._102_ACCOUNT_ID_1, "000000000000000000");
		output.putField(Iso8583.Bit._104_TRAN_DESCRIPTION, "000000000000000000");

		StructuredData sd = new StructuredData();

		sd.put("B24_Field_126",
				"126 & 0000200054! QT00032 0110000000000000000000000000000126 & 0000200054! QT00032 0110000054180030020000000002000 ");

		sd.put("B24_Field_41", "0054311300000   ");

		output.putStructuredData(sd);

		return output;
	}

	public static final String _TRAN_RETIRO_AHORRO = "0200_011000_1";
	public static final String _TRAN_RETIRO_CORRIENTE = "0200_012000_1";
	public static final String _TRAN_COMPRA_AHORRO = "0200_001000_1";
	public static final String _TRAN_COMPRA_CORRIENTE = "0200_002000_1";
	public static final String _APPROVAL_STATE_HEX = "F0F0";
	public static final char _CHAR_PIPE = '|';

	private static final String REGEX_ERROR = "\\w{2}(\\d{4})(.)[A-Z]{1}:(.)";
	private static final String ERROR_TEMPLATE = "error=$1";

	private static final String DELIMITADOR = "11C2601D60";
	private static final String REGEX_VARIABLE = "(.*)COMISION:(.*)SALDO DISPONIBLE:(.*)PIGNORACIONES(.*):(.*)IDENTIFI:(.*)SECUENCIA(.*)FRML(.*)AVSEGURO(.*)";
	private static final String OUTPUT_TEMPLATE = "comision=$2\nsaldo_disponible=$3\npignoraciones=$5\nidentificacion=$6\nsecuencia=$7\nfrml=$8\nav_seguro=$9\n";
	private static final String REGEX_VARIABLE_2 = "(.*)COMISION:(.*)nombre(.*)saldo(.*)IDENTIFI:(.*)SECUENCIA(.*)FRML(.*)AVSEGURO(.*)";
	private static final String OUTPUT_TEMPLATE_2 = "comision=$2\nnombre=$3\nsaldo_disponible=$4\nidentificacion=$5\nsecuencia=$6\nfrml=$7\nav_seguro=$8\n";
}