package postilion.realtime.iscinterface;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import com.fasterxml.jackson.databind.ObjectMapper;

import monitor.core.dto.MonitorSnapShot;
import monitor.core.dto.Observable;
import postilion.realtime.genericinterface.date.SettlementDate;
import postilion.realtime.iscinterface.database.DBHandler;
import postilion.realtime.iscinterface.message.ISCReqInMsg;
import postilion.realtime.iscinterface.message.ISCReqMessage;
import postilion.realtime.iscinterface.message.ISCResMessage;
import postilion.realtime.iscinterface.message.KeepAliveMessage;
import postilion.realtime.iscinterface.processors.ISCAssembler;
import postilion.realtime.iscinterface.util.Client;
import postilion.realtime.iscinterface.util.Constant;
import postilion.realtime.iscinterface.util.FlowDirection;
import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.iscinterface.util.MsgMappedResult;
import postilion.realtime.iscinterface.util.TagCreationResult;
import postilion.realtime.iscinterface.util.UtilidadesMensajeria;
import postilion.realtime.iscinterface.util.Utils;
import postilion.realtime.iscinterface.web.HttpCryptoServ;
import postilion.realtime.iscinterface.web.WebClient;
import postilion.realtime.iscinterface.web.model.Field;
import postilion.realtime.iscinterface.web.model.Homologation;
import postilion.realtime.iscinterface.web.model.TransactionSetting;
import postilion.realtime.iscinterface.web.model.WholeTransSetting;
import postilion.realtime.library.common.InitialLoadFilter;
import postilion.realtime.library.common.model.ResponseCode;
import postilion.realtime.sdk.crypto.CryptoCfgManager;
import postilion.realtime.sdk.crypto.CryptoManager;
import postilion.realtime.sdk.crypto.DesKwp;
import postilion.realtime.sdk.crypto.XCrypto;
import postilion.realtime.sdk.env.calendar.BusinessCalendar;
import postilion.realtime.sdk.eventrecorder.EventRecorder;
import postilion.realtime.sdk.eventrecorder.events.NodeConnected;
import postilion.realtime.sdk.message.IMessage;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.message.bitmap.StructuredData;
import postilion.realtime.sdk.message.bitmap.XFieldUnableToConstruct;
import postilion.realtime.sdk.message.bitmap.Iso8583.SettlementCode;
import postilion.realtime.sdk.message.xml.XMLMessage2;
import postilion.realtime.sdk.node.AInterchangeDriver8583;
import postilion.realtime.sdk.node.AInterchangeDriverEnvironment;
import postilion.realtime.sdk.node.Action;
import postilion.realtime.sdk.node.XNodeParameterValueInvalid;
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
public class ISCInterfaceCB extends AInterchangeDriver8583 {

	WebClient wc;

	long startTime = 0;

	long endTime = 0;

	private AInterchangeDriverEnvironment thisInter;

	Timer timer = new Timer();
	TimerTask tTask = null;

	/**
	 * Contine las configuraciones para hacer buscar los codigos equiavalentes de
	 * ISO8583 a B24
	 */
	private static HashMap<String, ResponseCode> v1CodesIscToIso = new HashMap<>();
	
	private static HashMap<String, ResponseCode> v2CodesIscToIso = new HashMap<>();

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

	// Parametro que indica en modo en que se procesaran los MonitorSnapShop para
	// monitoreo de la aplicacion
	private int monitorLogMode = 0;

	// Parametro que indica la URL del servicio web POST al cual reportar 200/210
	private String postURL = "";

	// habilita monitor plus http ws
	private boolean enableMonitor = false;

	// ignore http call
	private boolean ignoreHttp2 = true;

	// Mapa donde se alojara toda la configuracion desde la BBDD se apoyo
	public static Map<String, String> allConfig = new HashMap<>();

	// Arreglo donde se alojaran los user parameters de la interchange
	public static String[] userParams = null;

	// Rango en el que se debe enviar el flag NextDay y eveluar error para apagar el
	// flag
	private String hour4Check = null;

	public boolean isNextDay = false;

	public static HashMap<String, String> convenios = new HashMap<>();

	public WholeTransSetting wholeTransConfig = new WholeTransSetting();

	public WholeTransSetting wholeTransConfigIn = new WholeTransSetting();

	public Properties ndPropertyFile = new Properties();

	public TransactionSetting strTranSetting;

	private String ignoreBatch = null;

	boolean localCovValidation = true;

	String termConsecutiveSection = "1";

	private String jsonURL = "";

	private String jsonURLInput = "";

	private String nextDayFileURL = "";

	private String interName = "";

	private String monitorIP = "";

	private String monitorPort = "";

	// CRYPTO

	private String kwpName = "ATH_KPE";

	private DesKwp kwpParam1 = null;

	private String kvpParam2 = "";

	@Override
	/***************************************************************************************
	 * Implementación de metodo del SDK, sirve para inicializar
	 ***************************************************************************************/
	public void init(AInterchangeDriverEnvironment interchange) throws XPostilion {

		this.interName = interchange.getName();
		thisInter = interchange;
		this.wc = WebClient.getWebClient();

		String[] parameterArray = getParameters(interchange);

		try {

			// 120000 true false false false 0 http://10.89.0.168:47000 false 16:00

			this.transStore = new TimedHashtable(this.internalMaxTimeLimit);
			this.isIsoPassThrough = false; // recibe ISO y entrega ISo
			// this.onlyDummyReq = Boolean.valueOf(parameterArray[1]); // solo enviar ISC
			// dummy msg
			this.monitorLogMode = Integer.parseInt("0");
			this.postURL = parameterArray[1];
			this.enableMonitor = Boolean.valueOf(parameterArray[2]);
			this.hour4Check = parameterArray[3];
			this.ignoreBatch = parameterArray[4];
//			this.localCovValidation = Boolean.parseBoolean(parameterArray[5]);
			this.termConsecutiveSection = parameterArray[6];
			this.jsonURL = parameterArray[7].concat("\\MessagesSetting.json");
			this.jsonURLInput = parameterArray[7].concat("\\MessagesSettingIn.json");
			this.nextDayFileURL = parameterArray[7].concat("\\NextDay.properties");
			this.monitorIP = parameterArray[8];
			this.monitorPort = parameterArray[9];

			Logger.logLine("Init with params - Tran Storage Limit:" + this.transStore + " Enable Logging:"
					+ " Post URL:" + this.postURL + " Enabling Monitor Plus:" + this.enableMonitor
					+ " Nextday hour check:" + this.hour4Check, this.enableMonitor);

//			try {
//				// create object mapper instance
//				ObjectMapper mapper = new ObjectMapper();
//
//				// convert JSON string to wholeTransConfig object
//				wholeTransConfig = mapper.readValue(Paths.get(this.jsonURL).toFile(), WholeTransSetting.class);
//
//				// print book
//				Logger.logLine("JSON TRANS COFIG:" + wholeTransConfig.toString(), this.enableMonitor);
//
//			} catch (Exception ex) {
//				StringWriter outError = new StringWriter();
//				ex.printStackTrace(new PrintWriter(outError));
//				Logger.logLine("ERROR JSON TRANS COFIG: " + outError.toString(), this.enableMonitor);
//				EventRecorder.recordEvent(new Exception(outError.toString()));
//			}

			wholeTransConfig = Utils.retriveJsonConfig(this.jsonURL, true);

			wholeTransConfigIn = Utils.retriveJsonConfig(this.jsonURLInput, true);

			// Lectura de archivo
			InputStream inp = new FileInputStream(this.nextDayFileURL);

			ndPropertyFile.load(inp);

			if (ndPropertyFile.getProperty(NEXTDAY) != null) {

				this.isNextDay = Boolean.valueOf(ndPropertyFile.getProperty(NEXTDAY));

			} else {

				this.isNextDay = false;
				updateNextdayPersistence(NEXTDAY, "false");

			}

			v1CodesIscToIso = postilion.realtime.library.common.db.DBHandler.getResponseCodes(true, "0", "1");
			v2CodesIscToIso = postilion.realtime.library.common.db.DBHandler.getResponseCodes(true, "0", "2");

			if (v1CodesIscToIso.size() > 0) {
				for (Map.Entry<String, ResponseCode> e : v1CodesIscToIso.entrySet()) {
					Logger.logLine(e.getKey() + " :: " + e.getValue().getKeyIsc() + " :: " + e.getValue().getKeyIso()
							+ " :: " + e.getValue().getDescriptionIsc(), this.enableMonitor);
				}
			} else {
				Logger.logLine(" :: ERROR :: NO RSP CODES ::", this.enableMonitor);
			}

			Logger.logLine("Config Length :: " + v1CodesIscToIso.size(), this.enableMonitor);
			
			if (v2CodesIscToIso.size() > 0) {
				for (Map.Entry<String, ResponseCode> e : v2CodesIscToIso.entrySet()) {
					Logger.logLine(e.getKey() + " :: " + e.getValue().getKeyIsc() + " :: " + e.getValue().getKeyIso()
							+ " :: " + e.getValue().getDescriptionIsc(), this.enableMonitor);
				}
			} else {
				Logger.logLine(" :: ERROR :: NO RSP CODES ::", this.enableMonitor);
			}

			Logger.logLine("Config Length :: " + v2CodesIscToIso.size(), this.enableMonitor);

			List<String> strCovenats = Arrays.asList(this.wc.retriveAllCovenatData().split(","));

			Logger.logLine("COVENANT SIZE" + strCovenats.size(), this.enableMonitor);

			for (String e : strCovenats) {
				String k = e.split(":")[0].replace("\\", "").replace("\"", "");
				String v = e.split(":")[1].replace("\\", "").replace("\"", "");
				Logger.logLine("COV " + k + " -- " + v, this.enableMonitor);
				ISCInterfaceCB.convenios.put(k, v);
				Logger.logLine("MAP " + ISCInterfaceCB.convenios.get(k), this.enableMonitor);
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
		
		
		Utils.convertFields2Hash(msg);
		
		IMessage msg2Remote = null;
		Iso8583Post msg2TM = null;

		Logger.logLine("****processTranReqFromTranmgr****\n" + new String(msg.toMsg()), this.enableMonitor);

		Iso8583Post test2 = new Iso8583Post();
		String t2 = Transform.fromBinToHex(Transform.getString(msg.toMsg()));
		Logger.logLine("ISO:" + t2, this.enableMonitor);
		Logger.logLine("ISO:" + Transform.getString(msg.toMsg()), this.enableMonitor);
		Logger.logLine("ISO:" + Utils.getBitMap(msg), this.enableMonitor);
		test2.fromMsg(Transform.fromHexToBin(t2));
		
		// MONITOREO	
		Utils.postMsgInMonitor(this.monitorIP, 
				this.monitorPort, msg, msg2Remote, 
				this.interName, Transform.fromBinToHex(Transform.getString(msg.toMsg())), null);

		// NUEVA IMPLEMENTACION
		String msgKey = "";
		try {

			if (msg.getProcessingCode().getTranType().substring(0, 2).equals("32")) {

				msgKey = setSDandMsgkeyForCostconsult(msg);

			} else {
				StructuredData sd = msg.getStructuredData();
				sd.put(IS_COST_INQUIRY, FALSE);
				msg.putStructuredData(sd);
				msgKey = constructMessageKey(msg);
			}

			Logger.logLine(
					"AAAA NUEVA IMPLEMENTACION ::  <msg type>_<p code>_<canal>_<acq entity>_<aut entity>_<efectivo (0 tarjeta - 1 efectivo)>_<variation> ",
					this.enableMonitor);
			Logger.logLine("NUEVA IMPLEMENTACION :: TRAN KEY: " + msgKey, this.enableMonitor);

			if (msgKey != null && msgKey != "") {

				if (!msgKey.substring(0, 2).equals("00")) {

					strTranSetting = findTranSetting(msgKey);
					if (strTranSetting == null) {
						msgKey = "06_1";
					}

				}

			} else {
	
				// MONITOREO	
				Utils.postMsgInMonitor(this.monitorIP, 
						this.monitorPort, msg2TM, msg2Remote, 
						this.interName, Transform.fromBinToHex(Transform.getString(msg.toMsg())), "ERRISOZ4");

				Logger.logLine("ERROR CREANDO MSG KEY ", this.enableMonitor);
			}

		} catch (XPostilion e) {

			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter(outError));
			EventRecorder.recordEvent(new Exception("NUEVA IMPLEMENTACION: " + outError.toString()));

		}
		// NUEVA IMPLEMENTACION
		
		Logger.logLine("NUEVA IMPLEMENTACION:" + strTranSetting, this.enableMonitor);

		if (!this.ignoreHttp2) {
			try {
				wc.createPOSTRequest2("http://localhost:8080/entry-point/iso2isc",
						Transform.fromBinToHex(new String(msg.getBinaryData())));
			} catch (Exception e) {
				Logger.logLine("Error WS " + "http://localhost:8080/entry-point/iso2isc" + " " + e.getMessage(),
						this.enableMonitor);
			}
		}

		// Se determina el canal el mismo viene en la posición 13 del Tag "B24_Field_41"
		String canal = Utils.getTranChannel(msg.getStructuredData().get("B24_Field_41"));

		// Se determina el tipo de transacción "AAAA_BBBBBB_C"
		// AAAA-Tipo de Msg ; BBBBBB-Codigo proceso ; C-canal
		String tranType = Utils.getTranType(msg, canal);

		// Se invoca al metodo getTransactionConsecutive a fin de obtener el consecutivo
		// para la transaación
		String cons = getTransactionConsecutive(msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR).substring(5, 9), "00",
				 this.termConsecutiveSection);

		Logger.logLine("CONSE:" + cons, this.enableMonitor);

		switch (msgKey) {
		case "05":
			
			msg2TM = createErrorRspMsg(msg, "COVENENT_NOT_FOUND", "06");
			// MONITOREO	
			Utils.postMsgInMonitor(this.monitorIP, 
					this.monitorPort, msg2TM, msg2Remote, 
					this.interName, Transform.fromBinToHex(Transform.getString(msg.toMsg())), "DECISOZ5");

			break;

		case "05_1":
			
			msg2TM = createErrorRspMsg(msg, "NOT_ON_US_COVENANT", "06");
			// MONITOREO	
			Utils.postMsgInMonitor(this.monitorIP, 
					this.monitorPort, msg2TM, msg2Remote, 
					this.interName, Transform.fromBinToHex(Transform.getString(msg.toMsg())), "DECISOZ6");

			break;

		case "06_1":
			
			msg2TM = createErrorRspMsg(msg, "TRANSACCION NO CONFIGURADA", "06");
			// MONITOREO	
			Utils.postMsgInMonitor(this.monitorIP, 
					this.monitorPort, msg2TM, msg2Remote, 
					this.interName, Transform.fromBinToHex(Transform.getString(msg.toMsg())), "DECISOZ7");

			break;

		case "00":
			Logger.logLine("BATCH APP SD:" + msg.getStructuredData().toString(), this.enableMonitor);
			msg2TM = createErrorRspMsg(msg, "BATCH_APPROVED", "00");
			msg2TM.putField(Iso8583.Bit._038_AUTH_ID_RSP,
					cons.split(",")[0].trim().substring(2).concat(cons.split(",")[1].trim().substring(1)));
			break;

		case "00_1":
			Logger.logLine("BATCH CCOSTO:" + msg.getStructuredData().toString(), this.enableMonitor);
			msg2TM = createErrorRspMsg(msg, "BATCH_APPROVED_CCOSTO_OBLIGACION", "00");
			msg2TM.putField(Iso8583.Bit._038_AUTH_ID_RSP,
					cons.split(",")[0].trim().substring(2).concat(cons.split(",")[1].trim().substring(1)));
			break;

		default:

			// verificación del numero consecutivo
			if (cons == null || cons.trim().equals("")) {

				String errorMsg = "Error recuperando el consecutivo para la transaccion: "
						+ msg.getField(Iso8583Post.Bit._037_RETRIEVAL_REF_NR);
				msg2TM = (Iso8583Post) createErrorRspMsg(msg, errorMsg, "06");
				EventRecorder.recordEvent(new Exception(errorMsg));
				Logger.logLine("ERROR:" + errorMsg, this.enableMonitor);

			} else {

				mapISOFieldsInISOMessage(msg);
				addingAdditionalStructuredDataFields(msg, cons, tranType);

				Logger.logLine("SECUENCIA DE TRAN REQ: " + msg.getStructuredData().get(Constant.TagNames.SEQ_TERMINAL),
						this.enableMonitor);
				Logger.logLine("TRAN_TYPE----->" + tranType, this.enableMonitor);

				Logger.logLine("NUEVA IMPLEMENTACION :: TRAN SETTING: " + strTranSetting.getTranKey(),
						this.enableMonitor);

				if (strTranSetting.getPreOps() != null && strTranSetting.getPreOps().equals("1")) {

					//

				}

				MsgMappedResult resFromMapping = constructMsgString(strTranSetting, msg, false);

				if (resFromMapping.isContainsError()) {

					Logger.logLine("RESULT AAA" + resFromMapping.getErrors().get(0).toString(), this.enableMonitor);

					for (TagCreationResult t : resFromMapping.getErrors()) {
						if (t.getTagError() != null) {
							msg2TM = createErrorRspMsg(msg, resFromMapping.getErrors().get(0).getTagError(), "06");
							msg2Remote = null;
							break;
						}
					}


				} else {

					Logger.logLine("NUEVA IMPLEMENTACION :: STR MESSAGE : " + resFromMapping.getOutputMsg(),
							this.enableMonitor);
					if (resFromMapping.getOutputMsg().length() < 20) {
						
						msg2TM = createErrorRspMsg(msg, "CRYPTO ERROR", "06");
						msg2Remote = null;
						
					} else {
						ISCReqMessage req = new ISCReqMessage();
						req.fromHexStr(resFromMapping.getOutputMsg());
						msg2Remote = req;

						StructuredData sd = msg.getStructuredData();
						sd.put("NEXTDAY_STATE_FLAG",
								Transform.fromEbcdicToAscii(req.getField(ISCReqMessage.Fields._09_H_STATE)));
						msg.putStructuredData(sd);

						this.transStore.put(cons.split(Constant.Misce.STR_COMA)[0].trim()
								.concat(cons.split(Constant.Misce.STR_COMA)[1].trim()), msg);

						// }
						// NUEVA IMPLEMENTACION

						Logger.logLine("[MSG][OutFromTM] \n" + msg.toString(), this.enableMonitor);
						// Logger.logLine("[MSG][OutFromTM SD] \n" + msg.getStructuredData());

						Logger.logLine(
								"========================================\n========================================\n========================================\n",
								this.enableMonitor);

					}

				}

			}

			break;
		}

		// MONITOREO	
		Utils.postMsgInMonitor(this.monitorIP, 
				this.monitorPort, msg2TM, msg2Remote, 
				this.interName, msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR), null);

		return new Action(msg2TM, msg2Remote, null, null);

	}

	/**
	 * Implementación de metodo del SDK, toma msj de respuesta remoto para
	 * procesarlo y envia un 0210 al TM
	 */
	@Override
	public Action processTranReqRspFromInterchange(AInterchangeDriverEnvironment interchange, Iso8583 msg)
			throws XPostilion {

		Logger.logLine("processTranReqRspFromInterchange", this.enableMonitor);
		Logger.logLine(((Iso8583Post) msg).getStructuredData().toString(), this.enableMonitor);

		this.startTime = System.currentTimeMillis();
		Iso8583Post msg2TM = null;

		Action act = new Action();

		if (msg != null) {

			msg2TM = (Iso8583Post) msg;

			ResponseCode rspCode = Utils.set38And39Fields(msg2TM, v1CodesIscToIso, v2CodesIscToIso);

			StructuredData sd = putAdditionalStructuredDataRspFields(msg2TM, rspCode);

			sd.put("I2_RSP_TIME", String.valueOf(System.currentTimeMillis() - this.startTime));

			if (sd.get(Constant.B24Fields.B24_F_102) != null && !sd.get(Constant.B24Fields.B24_F_102).equals("")) {
				msg2TM.putField(Iso8583.Bit._102_ACCOUNT_ID_1, sd.get(Constant.B24Fields.B24_F_102));
			}
			if (sd.get(Constant.B24Fields.B24_F_103) != null && !sd.get(Constant.B24Fields.B24_F_103).equals("")) {
				msg2TM.putField(Iso8583.Bit._103_ACCOUNT_ID_2, sd.get(Constant.B24Fields.B24_F_103));
			}

			msg2TM.putStructuredData(sd);
			act.putMsgToTranmgr(msg2TM);
		} else {
			act = new Action();
		}

		Logger.logLine("[MSG][OutToTM] \n" + (null != msg2TM ? msg2TM.toString() : ""), this.enableMonitor);
//		Logger.logLine("[MSG][OutToTM SD] \n" + msg2TM.getStructuredData());
		Logger.logLine(
				"================================\n================================\n================================\n",
				this.enableMonitor);

		// Consuming to POST 210 msg request
//		if (this.enabledMonitorPlus) {
//			postMsg2Monitor(msg2TM);
//		}
//
//		monitorMsg210.close();
		return act;
	}

	@Override
	public Action processTranAdvFromTranmgr(AInterchangeDriverEnvironment interchange, Iso8583Post msg)
			throws Exception {

		Logger.logLine("****processTranAdvFromTranmgr****\n" + new String(msg.toMsg()), this.enableMonitor);
		
		Iso8583Post msgClone = (Iso8583Post) msg.clone();
		msgClone.setMessageType(Iso8583.MsgTypeStr._0200_TRAN_REQ);

		IMessage msg2Remote = null;
		Iso8583Post msg2TM = null;

		if (msgClone.getField(Iso8583.Bit._039_RSP_CODE).equals("00") && null != msg.getStructuredData().get("TRANSACTION_TO_HOST") &&
				msg.getStructuredData().get("TRANSACTION_TO_HOST").equals("MANDATORY")) {
			//		Iso8583Post test2 = new Iso8583Post();
			String t2 = Transform.fromBinToHex(Transform.getString(msgClone.toMsg()));
			//		test2.fromMsg(Transform.fromHexToBin(t2));
			Logger.logLine("ISO:" + t2, this.enableMonitor);
			// MONITOREO	
			Utils.postMsgInMonitor(this.monitorIP, this.monitorPort, msgClone, null, this.interName,
					Transform.fromBinToHex(Transform.getString(msgClone.toMsg())), null);
			boolean echoMsg = msgClone.getStructuredData() != null
					? msgClone.getStructuredData().get("ECHO_TEST_MSG") != null ? true : false
					: false;
			// NUEVA IMPLEMENTACION
			String msgKey = "";
			try {

				StructuredData sd = msgClone.getStructuredData();
				sd.put("IS_COST_INQUIRY", "FALSE");
				msgClone.putStructuredData(sd);

				msgKey = constructMessageKey(msgClone);

				Logger.logLine(
						"AAAA NUEVA IMPLEMENTACION ::  <msg type>_<p code>_<canal>_<acq entity>_<aut entity>_<efectivo (0 tarjeta - 1 efectivo)>_<variation> ",
						this.enableMonitor);
				Logger.logLine("NUEVA IMPLEMENTACION :: TRAN KEY: " + msgKey, this.enableMonitor);

				if (msgKey != null && msgKey != "") {

					if (!msgKey.substring(0, 2).equals("00")) {

						strTranSetting = findTranSetting(msgKey);
						if (strTranSetting == null) {
							msgKey = "06_1";
						}

					}

				} else {
					EventRecorder.recordEvent(new Exception("ERROR CREANDO MSG KEY "));

					// MONITOREO
					try {
						Client mon = new Client(this.monitorIP, this.monitorPort);
						if (msgClone != null)
							mon.sendData(Client.getMsgKeyValue(msgClone.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR),
									"ERRISOZ4" + Transform.fromBinToHex(Transform.getString(msgClone.toMsg())), "ISO",
									this.interName));
					} catch (Exception e) {
						Logger.logLine("ERROR CLIENTE MONITOR", this.enableMonitor);
					}

					Logger.logLine("ERROR CREANDO MSG KEY ", this.enableMonitor);
				}

			} catch (XPostilion e) {

				StringWriter outError = new StringWriter();
				e.printStackTrace(new PrintWriter(outError));
				EventRecorder.recordEvent(new Exception("NUEVA IMPLEMENTACION: " + outError.toString()));

			}
			// NUEVA IMPLEMENTACION
			Logger.logLine("NUEVA IMPLEMENTACION:" + strTranSetting, this.enableMonitor);
			this.startTime = System.currentTimeMillis();
			// Se determina el canal el mismo viene en la posición 13 del Tag "B24_Field_41"
			String canal = Utils.getTranChannel(msgClone.getStructuredData().get("B24_Field_41"));
			// Se determina el tipo de transacción "AAAA_BBBBBB_C"
			// AAAA-Tipo de Msg ; BBBBBB-Codigo proceso ; C-canal
			String tranType = Utils.getTranType(msgClone, canal);
			// Se invoca al metodo getTransactionConsecutive a fin de obtener el consecutivo
			// para la transaación
			String cons = getTransactionConsecutive(msgClone.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR).substring(5, 9),
					"00", this.termConsecutiveSection);
			Logger.logLine("CONSE:" + cons, this.enableMonitor);
			msg2Remote = null;
			msg2TM = null;
			// verificación del numero consecutivo
			if (cons == null || cons.trim().equals("")) {

				String errorMsg = "Error recuperando el consecutivo para la transaccion: "
						+ msgClone.getField(Iso8583Post.Bit._037_RETRIEVAL_REF_NR);
				msg2TM = (Iso8583Post) createErrorRspMsg(msgClone, errorMsg, "06");
				EventRecorder.recordEvent(new Exception(errorMsg));
				Logger.logLine("ERROR:" + errorMsg, this.enableMonitor);

			} else {

				mapISOFieldsInISOMessage(msgClone);
				addingAdditionalStructuredDataFields(msgClone, cons, tranType);

				Logger.logLine("SECUENCIA DE TRAN REQ: " + msgClone.getStructuredData().get(Constant.TagNames.SEQ_TERMINAL),
						this.enableMonitor);
				Logger.logLine("TRAN_TYPE----->" + tranType, this.enableMonitor);

				Logger.logLine("NUEVA IMPLEMENTACION :: TRAN SETTING: " + strTranSetting.getTranKey(),
						this.enableMonitor);

				MsgMappedResult resFromMapping = constructMsgString(strTranSetting, msgClone, false);

				if (resFromMapping.isContainsError()) {

					Logger.logLine("RESULT AAA" + resFromMapping.getErrors().get(0).toString(), this.enableMonitor);

					for (TagCreationResult t : resFromMapping.getErrors()) {
						if (t.getTagError() != null) {
							msg2TM = createErrorRspMsg(msgClone, resFromMapping.getErrors().get(0).getTagError(), "06");
							msg2Remote = null;
							break;
						}
					}

				} else {

					Logger.logLine("NUEVA IMPLEMENTACION :: STR MESSAGE : " + resFromMapping.getOutputMsg(),
							this.enableMonitor);
					if (resFromMapping.getOutputMsg().length() < 20) {
						msg2TM = createErrorRspMsg(msgClone, "CRYPTO ERROR", "06");
						msg2Remote = null;
					} else {
						ISCReqMessage req = new ISCReqMessage();
						req.fromHexStr(resFromMapping.getOutputMsg());
						msg2Remote = req;

						StructuredData sd = msgClone.getStructuredData();
						sd.put("NEXTDAY_STATE_FLAG",
								Transform.fromEbcdicToAscii(req.getField(ISCReqMessage.Fields._09_H_STATE)));
						msgClone.putStructuredData(sd);

						this.transStore.put(cons.split(Constant.Misce.STR_COMA)[0].trim()
								.concat(cons.split(Constant.Misce.STR_COMA)[1].trim()), msgClone);

						// }
						// NUEVA IMPLEMENTACION

						Logger.logLine("[MSG][OutFromTM] \n" + msgClone.toString(), this.enableMonitor);
						// Logger.logLine("[MSG][OutFromTM SD] \n" + msgClone.getStructuredData());

						Logger.logLine(
								"========================================\n========================================\n========================================\n",
								this.enableMonitor);

					}

				}

			}
			// MONITOREO	
			Utils.postMsgInMonitor(this.monitorIP, this.monitorPort, msg2TM, msg2Remote, this.interName,
					msgClone.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR), null);
		}
		else {
			msg2TM = (Iso8583Post) msgClone.clone();
			msg2TM.setMessageType(Iso8583.MsgTypeStr._0230_TRAN_ADV_RSP);
			msg2TM.putField(Iso8583.Bit._039_RSP_CODE, "00");
		}
		
		return new Action(msg2TM, msg2Remote, null, null);
	}
	
	@Override
	public Action processTranAdvRspFromInterchange(AInterchangeDriverEnvironment interchange, Iso8583 msg)
			throws Exception {

		Logger.logLine("***processTranAdvRspFromInterchange**\n" + msg.toString(), this.enableMonitor);
		
		return new Action((Iso8583Post) msg, null, null, null);
	}

	@Override
	public Action processNwrkMngAdvFromInterchange(AInterchangeDriverEnvironment interchange, Iso8583 msg) {

		Iso8583Post networkAdv = (Iso8583Post) msg;
		Action action = new Action();
		// action.putMsgToTranmgr(networkAdv);

		return action;
	}

//	/**
//	 * Procesar reversos desde TM
//	 */
//	@Override
//	public Action processAcquirerRevAdvFromTranmgr(AInterchangeDriverEnvironment interchange, Iso8583Post msg)
//			throws Exception {
//
//		Logger.logLine("**PROCESANDO REVERSO**");
//		Logger.logLine("ES REPETICIÓN: " + msg.getMessageType());
//		Logger.logLine(msg.toString());
//
//		monitorRevAdv = new MonitorSnapShot(msg.toString(), thisInter.getName(), monitorLogMode);
//		monitorRevAdv.getObservables().put("processAcquirerRevAdvFromTranmgr", new Observable(
//				"metodo para procesar reversos desde TM", "processAcquirerRevAdvFromTranmgr", msg.toString()));
//
//		Action act = new Action();
//
//		// Se determina el tipo de transacción "AAAA_BBBBBB_C"
//		// AAAA-Tipo de Msg ; BBBBBB-Codigo proceso ; C-canal
//		String tranType = Utils.getTranType(msg, Utils.getTranChannel(msg.getStructuredData().get("B24_Field_41")));
//
//		if (msg.isFieldSet(Iso8583.Bit._038_AUTH_ID_RSP)) {
//			// Se invoca al metodo getTransactionConsecutive a fin de obtener el consecutivo
//			// para la transaación
//			String cons = getTransactionConsecutive(msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR).substring(5, 9),
//					msg.getField(Iso8583.Bit._038_AUTH_ID_RSP).substring(0, 2), monitorRevAdv);
//
//			Logger.logLine(
//					DBHandler.getHistoricalConsecutive(msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR), monitorRevAdv));
//
//			// verificación del numero consecutivo
//			if (cons == null || cons.trim().equals("")) {
//				String errorMsg = "Error recuperando el consecutivo para la transaccion: "
//						+ msg.getField(Iso8583Post.Bit._037_RETRIEVAL_REF_NR);
//				act.putMsgToTranmgr((Iso8583Post) createErrorRspMsg(msg, errorMsg));
//				EventRecorder.recordEvent(new Exception(errorMsg));
//				Logger.logLine("ERROR:" + errorMsg);
//
//			} else {
//
//				mapISOFieldsInISOMessage(msg);
//				addingAdditionalStructuredDataFields(msg, cons, tranType);
//				Logger.logLine(
//						"SECUENCIA DE TRAN ADV: " + msg.getStructuredData().get(Constant.TagNames.SEQ_TERMINAL));
//
//				if (msg.getMessageType().equals("0420")) {
//					this.transStore.put(cons.split(",")[0].trim().concat(cons.split(",")[1].trim()), msg);
//
//					ISCReqMessage reverso = ISCAssembler.createISCMessage(msg);
//					act.putMsgToRemote(reverso);
//				} else {
//					Logger.logLine("0421 para aprobación");
//					msg.setMessageType(Iso8583.MsgTypeStr._0430_ACQUIRER_REV_ADV_RSP);
//					msg.putField(Iso8583.Bit._038_AUTH_ID_RSP,
//							msg.getStructuredData().get(Constant.TagNames.SEQ_TERMINAL).split(",")[0].trim()
//									.substring(3)
//									.concat(msg.getStructuredData().get(Constant.TagNames.SEQ_TERMINAL).split(",")[1]
//											.trim()));
//					msg.putField(Iso8583.Bit._039_RSP_CODE, "00");
//					StructuredData sd = msg.getStructuredData();
//					sd.put(Constant.TagNames.REVERSE_AUTH_MODE, "2");
//					msg.putStructuredData(sd);
//					act.putMsgToTranmgr(msg);
//				}
//			}
//		}
//
//		else {
//			Iso8583Post reverseRspAuto = (Iso8583Post) msg.clone();
//			reverseRspAuto.setMessageType(Iso8583.MsgTypeStr._0430_ACQUIRER_REV_ADV_RSP);
//			reverseRspAuto.putField(Iso8583.Bit._039_RSP_CODE, "00");
//			act.putMsgToTranmgr(reverseRspAuto);
//		}
//
//		monitorRevAdv.getObservables().get("processAcquirerRevAdvFromTranmgr").close();
//
//		return act;
//	}

	/**
	 * Procesar reversos desde TM
	 */
	@Override
	public Action processAcquirerRevAdvFromTranmgr(AInterchangeDriverEnvironment interchange, Iso8583Post msg)
			throws Exception {

		Logger.logLine("**PROCESANDO REVERSO**", this.enableMonitor);
		Logger.logLine("ES REPETICION?: " + msg.getMessageType() + "\n" + msg.toString(), this.enableMonitor);

		// MONITOREO
		try {
			Client mon = new Client(this.monitorIP, this.monitorPort);
			if (msg != null)
				mon.sendData(Client.getMsgKeyValue(msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR), msg.toString(),
						"ISO", this.interName));
		} catch (Exception e) {
			Logger.logLine("ERROR CLIENTE MONITOR", this.enableMonitor);
		}

		boolean echoMsg = msg.getStructuredData() != null
				? msg.getStructuredData().get("ECHO_TEST_MSG") != null ? true : false
				: false;

		IMessage msg2Remote;
		Iso8583Post msg2TM;

		// NUEVA IMPLEMENTACION
		String msgKey = "";

		try {

			StructuredData sd = msg.getStructuredData();
			sd.put("IS_COST_INQUIRY", "FALSE");
			msg.putStructuredData(sd);

			msgKey = constructMessageKey(msg);

			Logger.logLine(
					"AAAA NUEVA IMPLEMENTACION ::  <msg type>_<p code>_<canal>_<acq entity>_<aut entity>_<efectivo (0 tarjeta - 1 efectivo)>_<variation> ",
					this.enableMonitor);
			Logger.logLine("NUEVA IMPLEMENTACION :: TRAN KEY: " + msgKey, this.enableMonitor);

			if (msgKey != null && msgKey != "") {

				if (!msgKey.substring(0, 2).equals("00")) {

					strTranSetting = findTranSetting(msgKey);
					if (strTranSetting == null) {
						msgKey = "06_1";
					}

				}

			} else {
				EventRecorder.recordEvent(new Exception("ERROR CREANDO MSG KEY "));

				try {
					Client mon = new Client(this.monitorIP, this.monitorPort);
					if (msg != null)
						mon.sendData(Client.getMsgKeyValue(msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR),
								"ERRISOZ4" + Transform.fromBinToHex(Transform.getString(msg.toMsg())), "ISO",
								this.interName));
				} catch (Exception e) {
					Logger.logLine("ERROR CLIENTE MONITOR", this.enableMonitor);
				}

				Logger.logLine("ERROR CREANDO MSG KEY ", this.enableMonitor);
			}

		} catch (XPostilion e) {

			msgKey = "";
			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter(outError));
			EventRecorder.recordEvent(new Exception("NUEVA IMPLEMENTACION: " + outError.toString()));

		}
		// NUEVA IMPLEMENTACION

		msg2Remote = null;
		msg2TM = null;
		switch (msgKey) {
		case "05":
			msg2TM = createErrorRspMsg(msg, "COVENENT_NOT_FOUND", "06");

			// MONITOREO
			try {
				Client mon = new Client(this.monitorIP, this.monitorPort);
				if (msg != null)
					mon.sendData(Client.getMsgKeyValue(msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR),
							"DECISOZ5" + Transform.fromBinToHex(Transform.getString(msg.toMsg())), "ISO",
							this.interName));
			} catch (Exception e) {
				Logger.logLine("ERROR CLIENTE MONITOR", this.enableMonitor);
			}

			break;

		case "05_1":
			msg2TM = createErrorRspMsg(msg, "NOT_ON_US_COVENANT", "06");

			// MONITOREO
			try {
				Client mon = new Client(this.monitorIP, this.monitorPort);
				if (msg != null)
					mon.sendData(Client.getMsgKeyValue(msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR),
							"DECISOZ6" + Transform.fromBinToHex(Transform.getString(msg.toMsg())), "ISO",
							this.interName));
			} catch (Exception e) {
				Logger.logLine("ERROR CLIENTE MONITOR", this.enableMonitor);
			}

			break;

		case "06_1":
			msg2TM = createErrorRspMsg(msg, "TRANSACCION NO CONFIGURADA", "06");

			// MONITOREO
			try {
				Client mon = new Client(this.monitorIP, this.monitorPort);
				if (msg != null)
					mon.sendData(Client.getMsgKeyValue(msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR),
							"DECISOZ7" + Transform.fromBinToHex(Transform.getString(msg.toMsg())), "ISO",
							this.interName));
			} catch (Exception e) {
				Logger.logLine("ERROR CLIENTE MONITOR", this.enableMonitor);
			}

			break;

		case "00":
			msg2TM = createErrorRspMsg(msg, "BATCH_APPROVED", "00");
			break;

		default:

			// Se determina el tipo de transacción "AAAA_BBBBBB_C"
			// AAAA-Tipo de Msg ; BBBBBB-Codigo proceso ; C-canal
			String tranType = Utils.getTranType(msg,
					Utils.getTranChannel(msg.getField(Iso8583.Bit._041_CARD_ACCEPTOR_TERM_ID)));

			if (msg.isFieldSet(Iso8583.Bit._038_AUTH_ID_RSP)) {
				Logger.logLine("Campo 38 setted " + msg.getField(Iso8583.Bit._038_AUTH_ID_RSP), this.enableMonitor);

				StructuredData histSD = DBHandler.getHistoricalConsecutive(msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR),
						msg.getProcessingCode().toString(), this.enableMonitor);

				String histCons = histSD.get("REFERENCE_KEY");
				
				StructuredData sdIn = msg.getStructuredData();
				
				if(null != histSD.get("ATCG_ID_TYPE"))
					sdIn.put("ATCG_ID_TYPE", histSD.get("ATCG_ID_TYPE"));
				
				if(null != histSD.get("ATCG_ID_NR"))
					sdIn.put("ATCG_ID_NR", histSD.get("ATCG_ID_NR"));
				
				if(null != histSD.get("ATCG_GIRO_NR"))
					sdIn.put("ATCG_GIRO_NR", histSD.get("ATCG_GIRO_NR"));
				
				if(null != histSD.get("ATCG_GIRO_AMOUNT"))
					sdIn.put("ATCG_GIRO_AMOUNT", histSD.get("ATCG_GIRO_AMOUNT"));
				
				if(null != histSD.get("ATCG_ACCOUNT_TYPE"))
					sdIn.put("ATCG_ACCOUNT_TYPE", histSD.get("ATCG_ACCOUNT_TYPE"));
				
				if(null != histSD.get("ATCG_ACCOUNT_NR"))
					sdIn.put("ATCG_ACCOUNT_NR", histSD.get("ATCG_ACCOUNT_NR"));
				
				if(null != histSD.get("ATCG_FLAG"))
					sdIn.put("ATCG_FLAG", histSD.get("ATCG_FLAG"));
				
				if(null != histSD.get("ATCG_GIRO_KEY"))
					sdIn.put("ATCG_GIRO_KEY", histSD.get("ATCG_GIRO_KEY"));

				sdIn.put("HIST_CONS", histCons);

				msg.putStructuredData(sdIn);

				Logger.logLine("HIST CONS:" + histCons, this.enableMonitor);

				if (histCons != null && !histCons.equals("")) {

					int originalterm = Integer.parseInt(histCons.substring(15, 18));
					// Se invoca al metodo getTransactionConsecutive a fin de obtener el consecutivo
					// para la transaación
					String cons = getTransactionConsecutive(
							msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR).substring(5, 9),
							String.valueOf(originalterm), this.termConsecutiveSection);
					// verificación del numero consecutivo
					if (cons == null || cons.trim().equals("")) {
						String errorMsg = "Error recuperando el consecutivo para la transaccion: "
								+ msg.getField(Iso8583Post.Bit._037_RETRIEVAL_REF_NR);
						// act.putMsgToTranmgr((Iso8583Post) createErrorRspMsg(msg, errorMsg, "06"));
						msg2TM = (Iso8583Post) createErrorRspMsg(msg, errorMsg, "06");
						EventRecorder.recordEvent(new Exception(errorMsg));
						Logger.logLine("ERROR:" + errorMsg, this.enableMonitor);

					} else {

						mapISOFieldsInISOMessage(msg);
						addingAdditionalStructuredDataFields(msg, cons, tranType);
						Logger.logLine(
								"SECUENCIA DE TRAN ADV: " + msg.getStructuredData().get(Constant.TagNames.SEQ_TERMINAL),
								this.enableMonitor);

						// NUEVA IMPLEMENTACION
						if (strTranSetting == null) {

							if (msg.getMessageType().equals("0420")) {
								this.transStore.put(cons.split(",")[0].trim().concat(cons.split(",")[1].trim()), msg);

								// ISCReqMessage reverso = (ISCReqMessage) processReverse(msg);
								msg2Remote = (ISCReqMessage) ISCAssembler.createISCMessage(msg, this.hour4Check,
										this.isNextDay, this.localCovValidation, this);
								// act.putMsgToRemote(reverso);

								// Agrega msg a map de mensajes usando como key la llave alojada en "tranKey"
								StructuredData sd = msg.getStructuredData();
								sd.put(Constant.TagNames.I2_REQ_TIME,
										String.valueOf(System.currentTimeMillis() - this.startTime));

								Logger.logLine("VAR BODY:"
										+ ((ISCReqMessage) msg2Remote).getField(ISCReqMessage.Fields._VARIABLE_BODY),
										this.enableMonitor);

								if (((ISCReqMessage) msg2Remote).getField(ISCReqMessage.Fields._VARIABLE_BODY)
										.equals("BATCH_APPROVED")) {
									msg2Remote = null;
									msg2TM = createErrorRspMsg(msg, "BATCH_APPROVED", "00");
								} else if (((ISCReqMessage) msg2Remote).getField(ISCReqMessage.Fields._VARIABLE_BODY)
										.equals("NOT_ON_US_COVENANT")) {
									msg2Remote = null;
									msg2TM = createErrorRspMsg(msg, "NOT_ON_US_COVENANT", "06");
								} else if (((ISCReqMessage) msg2Remote).getField(ISCReqMessage.Fields._VARIABLE_BODY)
										.equals("COVENENT_NOT_FOUND")) {
									msg2Remote = null;
									msg2TM = createErrorRspMsg(msg, "COVENENT_NOT_FOUND", "06");
								} else if (((ISCReqMessage) msg2Remote).getField(ISCReqMessage.Fields._VARIABLE_BODY)
										.equals("TRAN_NOT_ALLOWED")) {
									msg2Remote = null;
									msg2TM = createErrorRspMsg(msg, "TRAN_NOT_ALLOWED", "06");
								} else if (((ISCReqMessage) msg2Remote).getField(ISCReqMessage.Fields._VARIABLE_BODY)
										.equals("SHARE_COVENANT")) {
									msg2Remote = null;
									msg2TM = createErrorRspMsg(msg, "SHARE_COVENANT", "06");
								} else {
									sd.put("ISC420Message", Base64.getEncoder().encodeToString(Transform
											.fromBinToHex(((ISCReqMessage) msg2Remote).getTotalString()).getBytes()));
								}

							} else {
								Logger.logLine("0421 para aprobación", this.enableMonitor);
								msg.setMessageType(Iso8583.MsgTypeStr._0430_ACQUIRER_REV_ADV_RSP);
								msg.putField(Iso8583.Bit._038_AUTH_ID_RSP,
										msg.getStructuredData().get(Constant.TagNames.SEQ_TERMINAL).split(",")[0].trim()
												.substring(3).concat(msg.getStructuredData()
														.get(Constant.TagNames.SEQ_TERMINAL).split(",")[1].trim()));
								msg.putField(Iso8583.Bit._039_RSP_CODE, "00");
								StructuredData sd = msg.getStructuredData();
								sd.put(Constant.TagNames.REVERSE_AUTH_MODE, "2");
								msg.putStructuredData(sd);
								msg2TM = msg;
							}

						}

						// NUEVA IMPLEMENTACION
						else {

							Logger.logLine("NUEVA IMPLEMENTACION :: TRAN SETTING: " + strTranSetting.getTranKey(),
									this.enableMonitor);

							if (strTranSetting.getPreOps() != null && strTranSetting.getPreOps().equals("1")) {

								//

							}

							MsgMappedResult resFromMapping = constructMsgString(strTranSetting, msg, true);

							if (resFromMapping.getErrors().size() <= 0) {
								Logger.logLine("NUEVA IMPLEMENTACION :: STR MESSAGE : " + resFromMapping.getOutputMsg(),
										this.enableMonitor);
								ISCReqMessage req = new ISCReqMessage();
								req.fromHexStr(resFromMapping.getOutputMsg());
								msg2Remote = req;
								StructuredData sd = msg.getStructuredData();
								sd.put("NEXTDAY_STATE_FLAG",
										Transform.fromEbcdicToAscii(req.getField(ISCReqMessage.Fields._09_H_STATE)));
								msg.putStructuredData(sd);
								this.transStore.put(cons.split(Constant.Misce.STR_COMA)[0].trim()
										.concat(cons.split(Constant.Misce.STR_COMA)[1].trim()), msg);
							} else {

								msg2TM = createErrorRspMsg(msg, resFromMapping.getErrors().get(0).getTagError(), "06");
								msg2Remote = null;
							}

						}
						// NUEVA IMPLEMENTACION

					}
				}

				else {
					Iso8583Post reverseRspAuto = (Iso8583Post) msg.clone();
					reverseRspAuto.setMessageType(Iso8583.MsgTypeStr._0430_ACQUIRER_REV_ADV_RSP);
					reverseRspAuto.putField(Iso8583.Bit._039_RSP_CODE, "25");
					StructuredData sd = new StructuredData();
					sd.put("REVERSE_MODE", "ORIGINAL TRAN NOT FOUND");
					reverseRspAuto.putStructuredData(sd);
					msg2TM = reverseRspAuto;
				}
			}

			else {
				Iso8583Post reverseRspAuto = (Iso8583Post) msg.clone();
				reverseRspAuto.setMessageType(Iso8583.MsgTypeStr._0430_ACQUIRER_REV_ADV_RSP);
				reverseRspAuto.putField(Iso8583.Bit._039_RSP_CODE, "00");
				StructuredData sd = new StructuredData();
				sd.put("REVERSE_MODE", "AUTO");
				reverseRspAuto.putStructuredData(sd);
				msg2TM = reverseRspAuto;
			}

			break;
		}

		// MONITOREO
		try {
			Client mon = new Client(this.monitorIP, this.monitorPort);
			if (msg2TM != null)
				mon.sendData(Client.getMsgKeyValue(msg2TM.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR),
						msg2TM.toString(), "ISO", this.interName));
			if (msg2Remote != null)
				mon.sendData(Client.getMsgKeyValue(
						((ISCReqMessage) msg2Remote).getField(ISCReqMessage.Fields._08_H_TRAN_SEQ_NR),
						msg2Remote.toString(), "ISC", this.interName));

		} catch (Exception e) {
			Logger.logLine("ERROR CLIENTE MONITOR", this.enableMonitor);
		}

		return new Action(msg2TM, msg2Remote, null, null);
	}

	@Override
	public Action processAcquirerRevAdvRspFromInterchange(AInterchangeDriverEnvironment interchange, Iso8583 msg)
			throws Exception {

		Action act = new Action();

		if (msg.isFieldSet(Iso8583.Bit._039_RSP_CODE) && msg.isFieldSet(Iso8583.Bit._038_AUTH_ID_RSP)) {

			Iso8583Post inMsg = (Iso8583Post) msg;
			Logger.logLine("processAcquirerRevReqRspFromInterchange\n" + inMsg, this.enableMonitor);

			Iso8583Post msg2TM = null;

			if (msg != null) {

				msg2TM = (Iso8583Post) msg;

				StructuredData sd = msg2TM.getStructuredData();

				ResponseCode responseCode;
				if (msg2TM.getStructuredData().get("ERROR") != null) {
					
					responseCode = InitialLoadFilter.getFilterCodeISCToIso(msg2TM.getStructuredData().get("ERROR"),
							v1CodesIscToIso);
					msg2TM.putField(Iso8583.Bit._038_AUTH_ID_RSP, "000000");

					Logger.logLine("mensaje 430 no aprobado upstream", this.enableMonitor);
					msg2TM.putField(Iso8583.Bit._039_RSP_CODE, "00");
					sd.put("REVERSE_AUTH_MODE", "1");

				} else {
					responseCode = InitialLoadFilter.getFilterCodeISCToIso("0000", v1CodesIscToIso);
					msg2TM.putField(Iso8583.Bit._038_AUTH_ID_RSP,
							sd.get("SEQ_TERMINAL") != null ? sd.get("SEQ_TERMINAL").split(",")[0].trim().substring(3)
									.concat(sd.get("SEQ_TERMINAL").split(",")[1].trim()) : "000000");
					sd.put("REVERSE_AUTH_MODE", "0");
				}

				Logger.logLine("RESPOSE CODE KEY>>>" + responseCode.getKeyIsc(), this.enableMonitor);
				Logger.logLine("RESPOSE CODE DESCRIP>>>" + responseCode.getDescriptionIsc(), this.enableMonitor);

				msg2TM.putField(Iso8583.Bit._039_RSP_CODE, responseCode.getKeyIso());

				act.putMsgToTranmgr(inMsg);

				Logger.logLine("[MSG][OutToTM] \n" + msg2TM.toString(), this.enableMonitor);
//				Logger.logLine("[MSG][OutToTM SD] \n" + msg2TM.getStructuredData());
				Logger.logLine(
						"================================\n================================\n================================\n",
						this.enableMonitor);

			}

		}
		return act;
	}

	@Override
	public Action processAcquirerFileUpdateAdvFromTranmgr(AInterchangeDriverEnvironment interchange, Iso8583Post msg)
			throws Exception {

		Logger.logLine("**Procesando 320 from TM**", this.enableMonitor);
		msg.setMessageType(Iso8583.MsgTypeStr._0330_ACQUIRER_FILE_UPDATE_ADV_RSP);
		msg.putField(Iso8583.Bit._039_RSP_CODE, "00");

		return new Action(msg, null, null, null);
	}

	@Override
	public Action processAcquirerFileUpdateAdvFromInterchange(AInterchangeDriverEnvironment interchange, Iso8583 msg)
			throws Exception {
		Logger.logLine("**Procesando 320 from Inter**", this.enableMonitor);
		return new Action(null, null, null, null);
	}

	@Override
	public Action processAdminReqRspFromInterchange(AInterchangeDriverEnvironment interchange, Iso8583 msg) {
		Logger.logLine("**Procesando 610 from Inter**", this.enableMonitor);
		return new Action((Iso8583Post) msg, null, null, null);
	}

	@Override
	public Action processAdminAdvFromTranmgr(AInterchangeDriverEnvironment interchange, Iso8583Post msg)
              throws Exception {
		
		Iso8583Post msg630 = (Iso8583Post) msg.clone();
		msg630.setMessageType(Iso8583.MsgTypeStr._0630_ADMIN_ADV_RSP);
		msg630.putField(Iso8583.Bit._039_RSP_CODE, "00");
		
		return new Action(msg630, null, null, null);
	}
	
	@Override
	public Action processSetCommand(AInterchangeDriverEnvironment interchange, String param, String value)
			throws Exception {

		Logger.logLine("[processSetCommand][param]" + param + "[value]" + value, this.enableMonitor);

		switch (param) {
		case "NEXTDAY":
			if (value.equals("ON") || value.equals("true")) {
				this.isNextDay = true;
				updateNextdayPersistence(NEXTDAY, "true");
//				Logger.logLine("[processSetCommand][NEXTDAY]:" + value, this.enableMonitor);

			} else if (value.equals("OFF") || value.equals("false")) {
				this.isNextDay = false;
				updateNextdayPersistence(NEXTDAY, "false");
//				Logger.logLine("[processSetCommand][NEXTDAY]:" + value, this.enableMonitor);

			}
			break;
		default:
			if (value.equals("ON") || value.equals("true")) {
				this.isNextDay = true;
				updateNextdayPersistence(NEXTDAY, "true");
//				Logger.logLine("[processSetCommand][NEXTDAY]:" + value, this.enableMonitor);

			} else if (value.equals("OFF") || value.equals("false")) {
				this.isNextDay = false;
				updateNextdayPersistence(NEXTDAY, "false");
//				Logger.logLine("[processSetCommand][NEXT_DAY]:" + value, this.enableMonitor);

			}
			break;
		}

		return new Action();
	}

	@Override
	public String processGetCommand(AInterchangeDriverEnvironment interchange, String param) throws Exception {
		String out = "";
		if (param.equals("NEXTDAY")) {
			out = String.valueOf(this.isNextDay);
		}
		return out;
	}

	private Iso8583Post dummyApprobedGoodAndServices(Iso8583Post inMsg) throws XPostilion {
		Iso8583Post approbedEcho = inMsg;
		approbedEcho.setMessageType(Iso8583.MsgTypeStr._0210_TRAN_REQ_RSP);
		approbedEcho.putField(Iso8583.Bit._039_RSP_CODE, Iso8583.RspCode._00_SUCCESSFUL);
		approbedEcho.putField(Iso8583.Bit._038_AUTH_ID_RSP, Utils.getStringDate(Utils.YYMMDDhhmmss).substring(6));
		return approbedEcho;
	}

	@Override
	public Action processMsgFromRemote(AInterchangeDriverEnvironment interchange, IMessage msg) throws Exception {

		Logger.logLine("**processMsgFromRemote**\n", this.enableMonitor);

		Iso8583Post rspISOMsg = null;

		if (msg.getClass().getName().equals(ISCResMessage.class.getName())) {

			String body = UtilidadesMensajeria
					.ebcdicToAscii(((ISCResMessage) msg).getField(ISCResMessage.Fields._VARIABLE_BODY))
					.replace("\0", "");

			String term = UtilidadesMensajeria
					.ebcdicToAscii(((ISCResMessage) msg).getField(ISCResMessage.Fields._06_H_ATM_ID)).replace("\0", "");

			String tranSeq = UtilidadesMensajeria
					.ebcdicToAscii(((ISCResMessage) msg).getField(ISCResMessage.Fields._07_H_TRAN_SEQ_NR))
					.replace("\0", "");

			Logger.logLine("BODY: " + body + "\nTERM: " + term + "\nSEQ: " + tranSeq, this.enableMonitor);

			if (body.contains("TRANS ABNORMAL END-CAL")) {

//				rspISOMsg = new Iso8583Post();
//				
//				rspISOMsg.setMessageType(Iso8583.MsgTypeStr._0210_TRAN_REQ_RSP);

				// MONITOREO
				try {
					Client mon = new Client(this.monitorIP, this.monitorPort);
					if (msg != null)
						mon.sendData(Client.getMsgKeyValue("000000000000",
								"REVISCZ8" + Transform.fromBinToHex(Transform.getString(msg.toMsg())), "ISC",
								this.interName));
				} catch (Exception e) {
					Logger.logLine("ERROR CLIENTE MONITOR", this.enableMonitor);
				}

				String outError = "TRANS ABNORMAL END-CAL -- La data recibida fue: ".concat(msg.toMsg().toString());
				Logger.logLine("TRANS ABNORMAL END-CAL " + outError.toString(), this.enableMonitor);
				EventRecorder.recordEvent(new Exception(outError.toString()));

			}

			else if (tranSeq.contains("9999") && term.contains("AT000")) {
				rspISOMsg = (Iso8583Post) this.transStore.get("AT0009999");

				rspISOMsg.setMessageType(Iso8583.MsgTypeStr._0610_ADMIN_REQ_RSP);
				rspISOMsg.putField(Iso8583.Bit._038_AUTH_ID_RSP, "000000");
				rspISOMsg.putField(Iso8583.Bit._039_RSP_CODE, "00");

				Logger.logLine("OOO:\n" + rspISOMsg, this.enableMonitor);

//				rspISOMsg = null;
			}

			else if (!tranSeq.contains("9999")) {

				Logger.logLine("PROCESSING ISCResMessage FROM HOST", this.enableMonitor);
				ISCResMessage rspISCMsg = (ISCResMessage) msg;
				rspISOMsg = mapISCFields2ISOFields(rspISCMsg);

				
				Logger.logLine("PRE VALIDATION PHASES \n" + rspISOMsg.getStructuredData().toString(), this.enableMonitor);
				if (rspISOMsg.getStructuredData().get("PHASE") != null
						&& rspISOMsg.getStructuredData().get("PHASE").equals("P1")
						&& rspISCMsg.getField(ISCResMessage.Fields._05_H_STATE).equals("F0F0")) {

					// PHASE 2
					Logger.logLine("PHASE 2", true);
					Iso8583Post p2Msg = (Iso8583Post) rspISOMsg.clone();
					p2Msg.setMessageType(Iso8583.MsgTypeStr._0200_TRAN_REQ);

					StructuredData s = p2Msg.getStructuredData();
					s.put("PHASE", "P2");
					p2Msg.putStructuredData(s);

					return processSecondMsgPhase(p2Msg);

				}

				else {

					StructuredData sd = rspISOMsg.getStructuredData();
					sd.put("ISC210Message", Base64.getEncoder().encodeToString(rspISCMsg.toMsg()));
					rspISOMsg.putStructuredData(sd);

					switch (rspISOMsg.getMsgType()) {
					case Iso8583.MsgType._0210_TRAN_REQ_RSP:
//						monitorMsg210 = new MonitorSnapShot(rspISOMsg.toString(), thisInter.getName(), monitorLogMode);
						break;
					case Iso8583.MsgType._0230_TRAN_ADV_RSP:
//						monitorMsg230 = new MonitorSnapShot(rspISOMsg.toString(), thisInter.getName(), monitorLogMode);
						break;
					default:
//						monitorMsg210 = new MonitorSnapShot(rspISOMsg.toString(), thisInter.getName(), monitorLogMode);
						break;
					}

				}

			}

		} else if (msg.getClass().getName().equals(KeepAliveMessage.class.getName())) {
			Logger.logLine("PROCESSING KeepAliveMessage FROM HOST", this.enableMonitor);
			KeepAliveMessage rspISCMsg = (KeepAliveMessage) msg;
			rspISOMsg = createNetworkAdv(rspISCMsg);
		}

		// SI EL MSG ENTRANTE ES UN REQUEST
		else if (msg.getClass().getName().equals(ISCReqInMsg.class.getName())) {

			Logger.logLine("ISCMsg:\n" + msg.toString(), this.enableMonitor);
			
			String cons = Utils.getTransactionConsecutive("AT", "00", "1");

			rspISOMsg = (Iso8583Post) Utils.processReqISCMsg(this.wholeTransConfigIn, (ISCReqInMsg)msg, FlowDirection.ISC2ISO , cons);
			Logger.logLine("REQ MAPPED:\n" + rspISOMsg.toString(), this.enableMonitor);
//			Iso8583Post reqISOMsg = Utils.fromISCReqToISOReq(reqISCMsg);

			return new Action(rspISOMsg, null, null, null);

		}

		// MONITOREO
		try {
			Client mon = new Client(this.monitorIP, this.monitorPort);
			if (rspISOMsg != null)
				mon.sendData(Client.getMsgKeyValue(rspISOMsg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR),
						rspISOMsg.toString(), "ISO", this.interName));

		} catch (Exception e) {
			Logger.logLine("ERROR CLIENTE MONITOR", this.enableMonitor);
		}

		return super.processMsgFromRemote(interchange, rspISOMsg);

	}

	@Override
	public Action processTranReqFromInterchange(AInterchangeDriverEnvironment interchange, Iso8583 msg)
			throws Exception {

		Logger.logLine("FROM INTERCHANGE:" + msg.toString(), true);

		return super.processAuthReqFromInterchange(interchange, msg);
	}

	@Override
	public IMessage newMsg(byte[] data) throws Exception {

		Logger.logLine(
				"**Recibiendo nuevo mensaje de respuesta**\n"
						+ Utils.asciiToEbcdic(Transform.getString(data, Transform.Encoding.EBCDIC)).toUpperCase(),
				this.enableMonitor);

		Iso8583Post rspISOMsg = null;

		if (this.isIsoPassThrough) {
			Logger.logLine("Processing input frame", this.enableMonitor);
			rspISOMsg = new Iso8583Post();
			rspISOMsg.fromMsg(data);

			Logger.logLine("[MSG][ISO] \n" + rspISOMsg.toString(), this.enableMonitor);
			return rspISOMsg;
		} else {

			Logger.logLine("Processing message from interchange! ISC", this.enableMonitor);
			// Solo se intenta procesar el mensaje si la trama tiene una longitud superior a
			// 60

			Logger.logLine("Trama de longitud correcta", this.enableMonitor);
			String trama = Utils.asciiToEbcdic(Transform.getString(data, Transform.Encoding.EBCDIC)).toUpperCase();

			Logger.logLine("DATA RECIBIDA:\n" + trama, this.enableMonitor);

			if (trama.substring(0, 2).equals("00")) {
				trama = trama.substring(2);
			}

			if (trama.length() > 60) {

				Logger.logLine("ISCReqInMsg:" + Transform.fromEbcdicToAscii(Transform.fromHexToBin(trama)),
						this.enableMonitor);

				if (!Transform.fromEbcdicToAscii(Transform.fromHexToBin(trama)).contains("ABNORMAL END-CAL")
						&& Transform.fromEbcdicToAscii(Transform.fromHexToBin(trama.substring(0, 8))).equals("SRLN")) {

					Logger.logLine("REQ RECIBIDA:\n" + trama, this.enableMonitor);

					ISCReqInMsg iscInputReq = new ISCReqInMsg();

					iscInputReq.fromHexStr(trama);

					return iscInputReq;

				} else {

					ISCResMessage msgISCMsg = new ISCResMessage();
					msgISCMsg.fromMsg(trama);
					return msgISCMsg;

				}

			} else {
				KeepAliveMessage networkISCMsg = new KeepAliveMessage();
				networkISCMsg.fromMsg(trama);
//				rspISOMsg = createNetworkAdv(networkISCMsg);
				return networkISCMsg;
			}

//			return rspISOMsg;

		}

	}

	public Iso8583Post createErrorRspMsg(Iso8583Post inMsg, String errorMsg, String rspCode) throws XPostilion {

		StructuredData sd = inMsg.getStructuredData();

		switch (inMsg.getMessageType()) {
		case Iso8583.MsgTypeStr._0200_TRAN_REQ:
			inMsg.setMessageType(Iso8583.MsgTypeStr._0210_TRAN_REQ_RSP);

			if (errorMsg.equals("BATCH_APPROVED_CCOSTO_OBLIGACION")) {
				sd.put("COMISION", "00000000000000");
				Utils.putB24Field126IntoStructuredData(sd);
			}

			break;
		case Iso8583.MsgTypeStr._0420_ACQUIRER_REV_ADV:

			inMsg.setMessageType(Iso8583.MsgTypeStr._0430_ACQUIRER_REV_ADV_RSP);

			break;
		default:
			break;
		}

		inMsg.putField(Iso8583.Bit._039_RSP_CODE, rspCode);
		sd.put("NOTE", errorMsg);
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
	private String getTransactionConsecutive(String termPrefix, String term, String termConsSection) {
		String output = null;

		// To-DO consultar consecutivo
		output = DBHandler.getCalculateConsecutive("AT", term, termConsSection, this.enableMonitor);

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

		Logger.logLine("Mapeando campos de ISO a ISO", this.enableMonitor);

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

		Logger.logLine("***Mapeando ISC a ISO***", this.enableMonitor);
		Map<String, String> bodyFields = new HashMap<>();
		List<String> errors = new ArrayList<>();

		String state = msgFromInter.getField(ISCResMessage.Fields._05_H_STATE);

		// Switch para basado en el estado de la respuesta de la transacción (Byte de
		// estado response)
		Logger.logLine("TRASACTION RSP STATE:" + state, this.enableMonitor);

		String tranKey = Utils.ebcdicToAscii(msgFromInter.getField(ISCResMessage.Fields._06_H_ATM_ID)).trim()
				.concat(Utils.ebcdicToAscii(msgFromInter.getField(ISCResMessage.Fields._07_H_TRAN_SEQ_NR)).trim());

		Logger.logLine("***TRAN KEY***" + tranKey, this.enableMonitor);
		Logger.logLine("***MAPA Contiene TRAN KEY***" + this.transStore.containsKey(tranKey), this.enableMonitor);

		Iso8583Post originalMsgReq = (Iso8583Post) this.transStore.get(tranKey);

//		switch (state) {
//		case APPROVAL_STATE_HEX:
//			for (Map.Entry<String, String> e : OUTPUT_TEMPS.entrySet()) {
//				bodyFields.putAll(Utils.getBodyInnerFields(msgFromInter.getField(ISCReqMessage.Fields._VARIABLE_BODY),
//						e.getKey(), e.getValue(), DELIMITADOR));
//
//				if (!bodyFields.isEmpty())
//					break;
//			}
//			break;
		switch (state) {
		case APPROVAL_STATE_HEX:

			bodyFields.putAll(Utils.getBodyInnerFields(msgFromInter.getField(ISCReqMessage.Fields._VARIABLE_BODY),
					originalMsgReq.getStructuredData().get("ISC_TRAN_CODE"),
					originalMsgReq.getStructuredData().get("IS_COST_INQUIRY")));

			if (!bodyFields.isEmpty())
				break;

			break;
		default:
			errors.addAll(Utils.getErrorsFromResponse(REGEX_ERROR, Transform.fromEbcdicToAscii(
					Transform.fromHexToBin(msgFromInter.getField(ISCReqMessage.Fields._VARIABLE_BODY)))));
			bodyFields.put("ERROR", (errors.isEmpty() || errors.size() == 0) ? "10002" : errors.get(0));

			// MONITOREO
			try {
				Client mon = new Client(this.monitorIP, this.monitorPort);

				mon.sendData(Client.getMsgKeyValue("000000000000",
						"DECISC" + bodyFields.get("ERROR")
								+ Transform.fromBinToHex(Transform.getString(msgFromInter.toMsg())),
						"ISC", this.interName));

			} catch (Exception e) {
				Logger.logLine("ERROR CLIENTE MONITOR", this.enableMonitor);
			}

			break;
		}

		if (originalMsgReq.getStructuredData().get("ISC_TRAN_CODE") == null
				|| !originalMsgReq.getStructuredData().get("ISC_TRAN_CODE").equals("AT2I"))
			bodyFields.put(Constant.TagNames.SALDO_DISPONIBLE,
					Utils.ebcdicToAscii(msgFromInter.getField(ISCResMessage.Fields._08_H_CUR_BALANCE)));

		Logger.logLine("TAGs RECUPERADOS DE RSP:" + bodyFields.size(), this.enableMonitor);
		for (Map.Entry<String, String> e : bodyFields.entrySet()) {
			Logger.logLine(":: " + e.getKey() + ":: " + e.getValue(), this.enableMonitor);
		}

		if (originalMsgReq != null) {
			this.transStore.remove(tranKey);

			int msgType = originalMsgReq.getMsgType();

			Logger.logLine("***ORIGINAL MSG***\n" + originalMsgReq, this.enableMonitor);
			
			if(null != originalMsgReq.getStructuredData().get("STANDING") && 
					originalMsgReq.getStructuredData().get("STANDING").equals("S")) {
				
				originalMsgReq.setMessageType(Iso8583.MsgTypeStr._0230_TRAN_ADV_RSP);
				
			}
			else {
				switch (msgType) {
				case Iso8583.MsgType._0200_TRAN_REQ:
					originalMsgReq.setMessageType(Iso8583.MsgTypeStr._0210_TRAN_REQ_RSP);
					break;
				case Iso8583.MsgType._0220_TRAN_ADV:
					originalMsgReq.setMessageType(Iso8583.MsgTypeStr._0230_TRAN_ADV_RSP);
					break;
				case Iso8583.MsgType._0420_ACQUIRER_REV_ADV:
					originalMsgReq.setMessageType(Iso8583.MsgTypeStr._0430_ACQUIRER_REV_ADV_RSP);
					break;
				case Iso8583.MsgType._0421_ACQUIRER_REV_ADV_REP:
					originalMsgReq.setMessageType(Iso8583.MsgTypeStr._0430_ACQUIRER_REV_ADV_RSP);
					break;
				}
			}

			// REPARAR

			StructuredData origSD = originalMsgReq.getStructuredData();
			for (Map.Entry<String, String> entry : bodyFields.entrySet()) {
				
				Logger.logLine("NNN:"+entry.getKey().toUpperCase() + " :: " + entry.getValue(), this.enableMonitor);
				
				if (entry.getValue() != null && !entry.getValue().equals(" ") && !entry.getValue().equals("")) {

					String tagRspValOrig = entry.getValue();
					String tagRspValEdit = entry.getValue().replaceAll("[^A-Za-z0-9]", "").replaceAll("\u0000", "")
							.replaceAll("\u001c", "").replaceAll("\n", "").replaceAll("\t", "").replaceAll("\n", "")
							.replaceAll("\u0001", "").replaceAll("\u0011", "").replaceAll("\u002F", "")
							.replaceAll("\u0003", "").replaceAll("\u00E6", "").replaceAll("\u00C6", "")
							.replaceAll("\u000C", "");

					if (entry.getKey().toUpperCase().contains("MOVIMIEN")
							|| entry.getKey().toUpperCase().contains("NOMBRE")) {

						Logger.logLine(entry.getKey().toUpperCase() + " :: " + tagRspValOrig, this.enableMonitor);

						origSD.put(entry.getKey().toUpperCase(), tagRspValOrig);

					} else {

						Logger.logLine(entry.getKey().toUpperCase() + " :: " + tagRspValEdit, this.enableMonitor);

						origSD.put(entry.getKey().toUpperCase(), tagRspValEdit);

					}

				}
			}

			origSD.put("ADITIONAL_ERRORS", Utils.list2String(errors, CHAR_PIPE));
			originalMsgReq.putStructuredData(origSD);

		}

		return originalMsgReq;

	}

	private Iso8583Post createNetworkAdv(KeepAliveMessage msgFromInter) throws XPostilion {
		Iso8583Post networkAdv = new Iso8583Post();

		String date = Utils.getStringDate(Utils.YYMMDDhhmmss);

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

	private Iso8583Post addingAdditionalStructuredDataFields(Iso8583Post inMsg, String consecutive, String tranType)
			throws XPostilion {

		Logger.logLine("addingAdditionalStructuredDataFields ::" + inMsg.toString(), this.enableMonitor);
		StructuredData sd = inMsg.getStructuredData();

		Logger.logLine("ACOUNT FROM ISO MSG FROM TM:" + inMsg.getField(Iso8583.Bit._102_ACCOUNT_ID_1),
				this.enableMonitor);

		sd.put("SYS_TIME", Utils.getStringDate(Utils.MMDDYYhhmmss).substring(0, 6));
		sd.put("SYS_TIME_DDMMYY", Utils.getStringDate(Utils.DDMMYYhhmmss).substring(0, 6));
		sd.put("SYS_TIME_YYYYMMDD", Utils.getStringDate(Utils.YYYYMMDDhhmmss).substring(0, 8));

//		sd.put("SYS_TIME", inMsg.getStructuredData().get("B24_Field_17").concat(Utils.getStringDate().substring(4, 6)));

		// Campo 40
		sd.put(Constant.B24Fields.B24_F_40, "000");

		// Campo privado 41
		if (sd.get(Constant.B24Fields.B24_F_41) == null)
			sd.put(Constant.B24Fields.B24_F_41, "0054232100000   "); // PRUEBA
		else
			sd.put(Constant.B24Fields.B24_F_41, sd.get(Constant.B24Fields.B24_F_41));

		// Campo privado 48
		if (sd.get(Constant.B24Fields.B24_F_48) == null)
			sd.put(Constant.B24Fields.B24_F_48, "00520000000030000000000000000000000000000000"); // PRUEBA
		else {
			sd.put(Constant.B24Fields.B24_F_48, sd.get(Constant.B24Fields.B24_F_48));

			switch (tranType) {
			case TRAN_COMPRA_AHORRO:
			case TRAN_COMPRA_CORRIENTE:
				putSplitedB54FieldIntoStructuredData(sd, inMsg);
				break;

			default:
				break;
			}

			putSplitedB54FieldIntoStructuredData(sd, inMsg);

		}

		// Campo privado TRANSACTION_TO_HOST
		if (sd.get("TRANSACTION_TO_HOST") != null && sd.get("TRANSACTION_TO_HOST").equals("MANDATORY")) {

			sd.put("STANDING", "S");

		} else {

			sd.put("STANDING", "N");

		}

		// Campo privado 126
		if (sd.get(Constant.B24Fields.B24_F_126) == null)
			sd.put(Constant.B24Fields.B24_F_126, "& 0000200054! QT00032 0110000000000000000000000000000"); // PRUEBA

		// Campo privado control base 24
		sd.put("_TOKEN_CONTROL_B24", "01");

		// Campo privado source
		sd.put(Constant.TagNames.SOURCE, "ATH");

		// Campo privado para secuencia de la transacción
		sd.put(Constant.TagNames.SEQ_TERMINAL, consecutive);
		
		if(sd.get("GOOD_AND_SERVICES_QR") != null) {
			sd.put("GAS_VIRTUAL_IND", "=");
		}
		else {
			sd.put("GAS_VIRTUAL_IND", "0");
		}

		// Campo privado para secuencia de la transacción
		sd.put(Constant.TagNames.RETRIEVED_ACCOUNT, inMsg.getField(Iso8583.Bit._102_ACCOUNT_ID_1));

		sd.put(Constant.TagNames.REFERENCE_KEY, inMsg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR).concat("|")
				.concat(consecutive.split(",")[0].trim().concat(consecutive.split(",")[1].trim())));

		if (bines.contains(inMsg.getField(Iso8583.Bit._035_TRACK_2_DATA).substring(0, 6))) {
			sd.put("ON_US_BIN", "TRUE");
		} else {
			sd.put("ON_US_BIN", "FALSE");
		}

		inMsg.putStructuredData(sd);

		// String originalMsgB64 =
		// Base64.getEncoder().encodeToString(inMsg.toString().getBytes());
		// sd.put("ORIGINAL_MSG", originalMsgB64);

		inMsg.putStructuredData(sd);

		return inMsg;
	}

	private StructuredData putAdditionalStructuredDataRspFields(Iso8583Post msg, ResponseCode rspCode)
			throws XPostilion {

		StructuredData sd = msg.getStructuredData();
		String tTipe = Utils.getTranType(msg, Utils.getTranChannel(msg.getStructuredData().get("B24_Field_41")));

		String realPCode = "";

		Logger.logLine("TRANTYPE:.:" + tTipe, this.enableMonitor);
		switch (tTipe) {
		case RSP_TRAN_RETIRO_AHORRO:
		case RSP_TRAN_RETIRO_CORRIENTE:
			Utils.putB24Field126IntoStructuredData(sd);
			Utils.putB24Field63IntoStructuredData(sd, rspCode);
			Utils.putB24Field40IntoStructuredData(sd);
			Utils.putB24Field44IntoStructuredData(sd);
			Utils.putB24Field48IntoStructuredData(sd, false);
			break;
		case RSP_TRAN_RETIRO_401410_OTP:
		case RSP_TRAN_RETIRO_401420_OTP:
		case RSP_TRAN_RETIRO_402410_OTP:
		case RSP_TRAN_RETIRO_402420_OTP:
			Utils.putB24Field126IntoStructuredData(sd);
			Utils.putB24Field103IntoStructuredData(sd);
			Utils.putB24Field63IntoStructuredData(sd, rspCode);
			Utils.putB24Field40IntoStructuredData(sd);
			Utils.putB24Field44IntoStructuredData(sd);
			Utils.putB24Field48IntoStructuredData(sd, false);
			break;
		case RSP_TRAN_COMPRA_CREDIBANCO_AHORRO:
		case RSP_TRAN_COMPRA_CREDIBANCO_CORRIENTE:
		case RSP_TRAN_ANULACION_CREDIBANCO_AHORRO:
		case RSP_TRAN_ANULACION_CREDIBANCO_CORRIENTE:
		case RSP_TRAN_RETIRO_CREDIBANCO_AHORRO:
		case RSP_TRAN_RETIRO_CREDIBANCO_CORRIENTE:
			Utils.putB24Field126IntoStructuredData(sd);
			Utils.putB24Field102IntoStructuredDataCUSTOM(sd);
			Utils.putB24Field44IntoStructuredData(sd);
			Utils.putB24Field40IntoStructuredData(sd);
			Utils.putB24Field48IntoStructuredData(sd, false);
			break;
		case RSP_TRAN_PSP_AHORRO:
		case RSP_TRAN_PSP_CORRIENTE:
		case RSP_TRAN_TRANSFER_AHO_AHO:
		case RSP_TRAN_TRANSFER_AHO_COR:
		case RSP_TRAN_TRANSFER_COR_AHO:
		case RSP_TRAN_TRANSFER_COR_COR:
		case RSP_TRAN_DEPOSITO_AH_AH:
		case RSP_TRAN_DEPOSITO_AH_CO:
		case RSP_TRAN_DEPOSITO_CO_AH:
		case RSP_TRAN_DEPOSITO_CO_CO:
		case RSP_TRAN_POB_1040:
		case RSP_TRAN_POB_1041:
		case RSP_TRAN_POB_2040:
		case RSP_TRAN_POB_2041:
			Utils.putB24Field126IntoStructuredData(sd);
			Utils.putB24Field63IntoStructuredData(sd, rspCode);
			Utils.putB24Field40IntoStructuredData(sd);
			Utils.putB24Field44IntoStructuredData(sd);
//			Utils.putB24Field48IntoStructuredData(sd, false);
			break;
		case RSP_TRAN_DEPOSITO_MUL_AH:
		case RSP_TRAN_DEPOSITO_MUL_CO:
		case RSP_PAGO_CREDITO_MULT:
			Utils.putSpecialCharsOnStructuredData(sd);
			Utils.putB24Field126IntoStructuredData(sd);
			Utils.putB24Field62IntoStructuredData(sd);
			Utils.putB24Field63IntoStructuredData(sd, rspCode);
			Utils.putB24Field40IntoStructuredData(sd);
			Utils.putB24Field44IntoStructuredData(sd);
//			Utils.putB24Field48IntoStructuredData(sd, false);
			break;
		case RSP_TRAN_RETIRO_CNB_AHORRO:
		case RSP_TRAN_RETIRO_CNB_CORRIENTE:
		case RSP_TRAN_GIRO_AHORRO:
		case RSP_TRAN_GIRO_CORRIENTE:
			Utils.putB24Field126IntoStructuredData(sd);
			Utils.putB24Field63IntoStructuredData(sd, rspCode);
			Utils.putB24Field40IntoStructuredData(sd);
			Utils.putB24Field44IntoStructuredData(sd);

			if(msg.getField(Iso8583.Bit._039_RSP_CODE).equals("00")) {
				Utils.putB24Field102IntoStructuredData(sd, tTipe);
			}

			Utils.putB24Field103IntoStructuredData(sd);
			break;

		case RSP_TRAN_COSULTA_COSTO:
		case RSP_TRAN_COSULTA_COSTO_AH:
		case RSP_TRAN_COSULTA_COSTO_CO:
			Utils.putB24Field126IntoStructuredData(sd);
			Utils.putB24Field63IntoStructuredData(sd, rspCode);

			realPCode = sd.get("B24_Field_126").substring(sd.get("B24_Field_126").indexOf("QT")).substring(8, 14);
			if (realPCode.equals("011000") || realPCode.equals("012000")) {
				Utils.putB24Field48IntoStructuredData(sd, true);
			}

			Utils.putB24Field44IntoStructuredData(sd);
			sd.remove(Constant.B24Fields.B24_F_40);
//			sd.remove(Constant.B24Fields.B24_F_54);
			break;
		case RSP_TRAN_COSULTA_ULT5_AH:
		case RSP_TRAN_COSULTA_ULT5_CO:
			Utils.putB24Field126IntoStructuredData(sd);
			Utils.putB24Field63IntoStructuredData(sd, rspCode);
			Utils.fixLast5Movements(sd);

			realPCode = sd.get("B24_Field_126").substring(sd.get("B24_Field_126").indexOf("QT")).substring(8, 14);
			if (realPCode.equals("011000") || realPCode.equals("012000")) {
				Utils.putB24Field48IntoStructuredData(sd, true);
			}

			Utils.putB24Field44IntoStructuredData(sd);
			sd.remove(Constant.B24Fields.B24_F_40);
//			sd.remove(Constant.B24Fields.B24_F_54);
			break;
		case RSP_TRAN_COSULTA_SALDO_CB_AH:
		case RSP_TRAN_COSULTA_SALDO_CB_CO:
		case RSP_TRAN_PAGO_CREDITO_HIPO:
			Utils.putB24Field126IntoStructuredData(sd);
			Utils.putB24Field63IntoStructuredData(sd, rspCode);
//			Utils.putB24Field48IntoStructuredData(sd, false);
			Utils.putB24Field44IntoStructuredData(sd);
			sd.remove(Constant.B24Fields.B24_F_40);
//			sd.remove(Constant.B24Fields.B24_F_54);
			break;
		case RSP_TRAN_COSULTA_SALDO_CREDIBANCO_AH:
		case RSP_TRAN_COSULTA_SALDO_CREDIBANCO_CO:
			Utils.putB24Field4IntoStructuredData(sd);
			Utils.putB24Field126IntoStructuredData(sd);
			Utils.putB24Field102IntoStructuredDataCUSTOM(sd);
			Utils.putB24Field63IntoStructuredData(sd, rspCode);
			Utils.putB24Field44IntoStructuredData(sd);
			sd.remove(Constant.B24Fields.B24_F_40);

			break;
		case RSP_TRAN_COSULTA_CUPO_ROT:
			Utils.putB24Field126IntoStructuredData(sd);
			Utils.putB24Field63IntoStructuredData(sd, rspCode);
//			Utils.putB24Field48IntoStructuredData(sd, false);
			Utils.putB24Field44IntoStructuredData(sd);
			sd.remove(Constant.B24Fields.B24_F_40);
//			sd.remove(Constant.B24Fields.B24_F_54);
			break;
		case RSP_TRAN_PAGO_TDC_AHORRO:
		case RSP_TRAN_PAGO_TDC_CORRIENTE:
		case RSP_PAGO_OBLIG:
			Utils.putB24Field126IntoStructuredData(sd);
			Utils.putB24Field63IntoStructuredData(sd, rspCode);
//			Utils.putB24Field48IntoStructuredData(sd, false);
			Utils.putB24Field44IntoStructuredData(sd);
			sd.remove(Constant.B24Fields.B24_F_40);
			sd.remove(Constant.B24Fields.B24_F_54);
			break;
//		case RSP_TRAN_PSP_AHORRO:

//			Logger.logLine("SABOR::0210_501000_1");
//			Utils.putB24Field126IntoStructuredData(sd);
//			Utils.putB24Field63IntoStructuredData(sd, rspCode);
//			Utils.putB24Field40IntoStructuredData(sd);
//			Utils.putB24Field44IntoStructuredData(sd);
//			Utils.putB24Field48IntoStructuredData(sd, false);
////			Utils.putB24Field102IntoStructuredData(sd);
////			Utils.putB24Field103IntoStructuredData(sd);
//			break;
		default:
			Logger.logLine("NOT RSP TAG MAPPED FOR THIS MSG", this.enableMonitor);
			break;
		}

		return sd;
	}

	private void postMsg2Monitor(Iso8583Post msg) {

		try {
			// Consumiendo web service POST
			Logger.logLine("TRY WS ", this.enableMonitor);
			Logger.logLine("TRY WS ", this.enableMonitor);

			XMLMessage2 msgXml = new XMLMessage2();
			msgXml.setField("/MonitorNotifyMsg/MsgTranDate", msg.getField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME));
			msgXml.setField("/MonitorNotifyMsg/MsgRetrivalNr", msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR));
			msgXml.setField("/MonitorNotifyMsg/MsgData", Base64.getEncoder().encodeToString(msg.toString().getBytes()));

			try {
				wc.createPOSTRequest(this.postURL, msgXml.toString());
			} catch (Exception e) {
				Logger.logLine("Error WS " + this.postURL + " " + e.getMessage(), this.enableMonitor);
			}
		} catch (Exception e) {
			Logger.logLine("Error consumiendo WS " + e.getMessage(), this.enableMonitor);
		}

	}

	/**
	 * Verifica y evalua los campos 48 y 54 en busca de los montos de transaccion,
	 * donacion y seguro
	 * 
	 * @param sd
	 * @param inMsg
	 * @throws XPostilion
	 */
	private void putSplitedB54FieldIntoStructuredData(StructuredData sd, Iso8583Post inMsg) throws XPostilion {

		// SE VERIFICA SI ES RETIRO Y RED AVAL
		if (inMsg.getProcessingCode().getTranType().equals("01")
				&& (inMsg.getProcessingCode().getFromAccount().equals("10")
						|| inMsg.getProcessingCode().getFromAccount().equals("20"))
				&& sd.get("B24_Field_41").substring(12, 13).equals("1")) {

			// SE VERIFICA SI COMPRA SEGURO
			if (sd.get(Constant.B24Fields.B24_F_48).substring(sd.get(Constant.B24Fields.B24_F_48).length() - 1)
					.equals("1")) {

				// SI COMPRA SEGURO. separa el campo 54 en montos (MONTO TRAN 0-12, MONTO DON
				// 12-24, MONTO SEC 24-36)
				splitField54(sd);

			}

			// NO COMPRA SEGURO
//				else if (sd.get(Constant.B24Fields.B24_F_48).substring(sd.get(Constant.B24Fields.B24_F_48).length() - 1)
//						.equals("2")) {
			else {

				// AJUSTA EL CAMPO 54 al formato (12 primeras posiciones CAMPO 4, 12 segundas a
				// "000000000000" y 12 terceras a "000000000000")
				sd.put(Constant.B24Fields.B24_F_54, inMsg.getField(Iso8583.Bit._004_AMOUNT_TRANSACTION)
						.concat(Constant.Misce.STR_TWENTYFOUR_ZEROS));

				sd.put(Constant.TagNames.TRANSACTION_AMOUNT, sd.get(Constant.B24Fields.B24_F_54).substring(0, 12));

				// SE VERIFICAN UNIDADES de MIL para determinar valor de donacion (EN CASO de
				// que exista, los valores permitidos son 1000, 2000 y 5000)
				int thousandUnits = Integer.parseInt(sd.get(Constant.TagNames.TRANSACTION_AMOUNT).substring(6, 7));
				if (thousandUnits == 1 || thousandUnits == 2 || thousandUnits == 5) {
					sd.put(Constant.TagNames.TRANSACTION_AMOUNT,
							sd.get(Constant.TagNames.TRANSACTION_AMOUNT).substring(0, 6).concat("000000"));
					sd.put(Constant.TagNames.DONATION_AMOUNT,
							Pack.resize(String.valueOf(thousandUnits * 1000).concat("00"), 12,
									Constant.Misce.CHAR_ONE_ZERO, false));
				}
				// EN CASO de que las unidades de mil sean un valor no permitido se fija a ceros
				else {
					sd.put(Constant.TagNames.DONATION_AMOUNT, Constant.Misce.STR_TWELVE_ZEROS);
				}

				// SE fija el valor de seguro a ceros
				sd.put(Constant.TagNames.SECURE_AMOUNT, Constant.Misce.STR_TWELVE_ZEROS);

				putSetterAmountTag(sd);

			}

		}

		// NO ES RETIRO o NO RED AVAL
		else {

			sd.put(Constant.TagNames.TRANSACTION_AMOUNT, inMsg.getField(Iso8583Post.Bit._004_AMOUNT_TRANSACTION));
			sd.put(Constant.TagNames.DONATION_AMOUNT, Constant.Misce.STR_TWELVE_ZEROS);
			sd.put(Constant.TagNames.SECURE_AMOUNT, Constant.Misce.STR_TWELVE_ZEROS);

			putSetterAmountTag(sd);

		}

		sd.put("PA_MODE_SELECTED",
				sd.get(Constant.B24Fields.B24_F_48).substring(sd.get(Constant.B24Fields.B24_F_48).length() - 1));

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
	
	private String setSDandMsgkeyForCostconsult(Iso8583Post msg) throws XPostilion, CloneNotSupportedException {
		
		StructuredData sd = msg.getStructuredData();
		String b24F126 = sd.get(B24_Field_126);
		String msgKey;

		Logger.logLine("OPERACION 32: " + b24F126, this.enableMonitor);

		String realPCode = b24F126.substring(b24F126.indexOf("QT")).substring(8, 14);

		if (((realPCode.substring(0, 1).equals("5") || realPCode.substring(0, 2).equals("27"))
				&& sd.get(B24_Field_103).substring(2, 3).equals("1"))) {

			msgKey = "00_1";

		} else if ((realPCode.substring(0, 2).equals("51")
				&& msg.getField(Iso8583.Bit._035_TRACK_2_DATA).substring(0, 6).equals(PSEUDO_BIN_777760)
				&& sd.get("TRANSACTION_TYPE_CBN").equals("CREDITO"))) {

			msgKey = "00_1";

		} else if ((realPCode.substring(0, 2).equals("40")
				&& msg.getField(Iso8583.Bit._035_TRACK_2_DATA).substring(0, 6).equals(PSEUDO_BIN_777760)
				&& msg.getField(Iso8583.Bit._022_POS_ENTRY_MODE).equals("021"))) {

			msgKey = "00_1";

		} else {

			if (realPCode.substring(0, 2).equals("27")) {

				if (realPCode.substring(4, 6).equals("10") || realPCode.substring(4, 6).equals("20")) {
					realPCode = "21".concat(realPCode.substring(2));
				} else {
					realPCode = "51".concat(realPCode.substring(2));
				}

			}
			sd.put("REAL PCODE", realPCode);
			sd.put(IS_COST_INQUIRY, TRUE);
			msg.putStructuredData(sd);
			Iso8583Post msgCloned = (Iso8583Post) msg.clone();
			msgCloned.putField(Iso8583.Bit._003_PROCESSING_CODE, realPCode);
			msgKey = constructMessageKey(msgCloned);
		}
		
		return msgKey;
	}

	// NUEVA IMPLEMENTACION

	/**
	 * 
	 * <tipo mensaje>_<processing code>_<canal>_<entidad adquiriente>_<entidad
	 * autorizadora>_<efectivo (0 tarjeta - 1 efectivo)>_<variation>
	 * 
	 * @return
	 * @throws XFieldUnableToConstruct
	 * @throws XPostilion
	 */
	private String constructMessageKey(Iso8583Post msg) throws XPostilion {

		Logger.logLine("ZXC: \n" + msg.toString(), this.enableMonitor);

		String msgTran = "";

		StructuredData sd = msg.getStructuredData();
//		sd.put("IS_COST_INQUIRY", "FALSE");

		msgTran = msg.getMessageType().concat("_").concat(msg.getProcessingCode().toString()).concat("_")
				.concat(msg.getStructuredData().get("B24_Field_41").substring(12, 13)).concat("_").concat("0000")
				.concat("_");

		if (msg.getProcessingCode().getTranType().equals("40")) {

			// ES PSP
			if (msg.getProcessingCode().getToAccount().equals("00")) {

//				msgTran = msgTran.concat(msg.getStructuredData().get("B24_Field_103").substring(5, 9)).concat("_")
//						.concat(msg.getField(Iso8583.Bit._022_POS_ENTRY_MODE) != null
//								&& !msg.getField(Iso8583.Bit._022_POS_ENTRY_MODE).equals("010")
//								&& !msg.getField(Iso8583.Bit._022_POS_ENTRY_MODE).equals("021") ? "0" : "1");

				msgTran = msgTran.concat("0000");
				String variation = "";

				if (msg.getField(Iso8583.Bit._035_TRACK_2_DATA).substring(0, 6).equals("777760")) {
					msgTran = msgTran.concat("_MULT");
					variation = "0";
					msgTran = msgTran.concat("_").concat(variation);
				} 
				else {
					variation = msg.getStructuredData().get("B24_Field_103").substring(0, 1);
					msgTran = msgTran.concat("_").concat(variation);
					
					//OBSERVACION
					
					// Si es MIXTO o CREDITO
					if (variation.equals("0") || variation.equals("2")) {

						Logger.logLine("PSP MIXTO o CREDITO:" + msgTran, this.enableMonitor);

						if (Utils.search4covenant(msg)) {

							sd = msg.getStructuredData();

							if ((variation.equals("2") && msg.getStructuredData().get("PRIM_COV_PAYMENT_TYPE").equals("0")))
								msgTran = "00";

							else if ((variation.equals("0")
									&& msg.getStructuredData().get("PRIM_COV_PAYMENT_TYPE").equals("0"))) {

								Logger.logLine("PSP MIXTO BATCH:" + msgTran, this.enableMonitor);

								variation = "1";
								msgTran = msgTran.substring(0, msgTran.length() - 1).concat(variation);
							}

//							else if (msg.getStructuredData().get("PRIM_COV_ACCOUNT_NR").substring(0, 6).equals("940999"))
//								msgTran = "05_1";

						} 
						else {
							msgTran = "05";
						}

					} 
					else if (variation.equals("1")) {

						sd.put("PRIM_COV_ABO", "2");
					}
					
					//OBSERVACION
					
				}

//				msgTran = msgTran.concat("_").concat(variation);

			}

			// RETIRO OTP CNB
			else if ((msg.getProcessingCode().getFromAccount().equals("14")
					|| msg.getProcessingCode().getFromAccount().equals("24"))
					&& (msg.getProcessingCode().getToAccount().equals("10")
							|| msg.getProcessingCode().getToAccount().equals("20"))) {

//				msgTran = msgTran.concat(msg.getStructuredData().get("B24_Field_103").substring(5, 9)).concat("_")
//						.concat(msg.getField(Iso8583.Bit._022_POS_ENTRY_MODE) != null
//								&& !msg.getField(Iso8583.Bit._022_POS_ENTRY_MODE).equals("010")
//								&& !msg.getField(Iso8583.Bit._022_POS_ENTRY_MODE).equals("021") ? "0" : "1");

				msgTran = msgTran.concat("0000").concat("_").concat("0");

				String variation = msg.getStructuredData().get("B24_Field_103").substring(2, 3);
				msgTran = msgTran.concat("_").concat(variation);
			}

			// ES DEPOSITO
			else if ((msg.getProcessingCode().getFromAccount().equals("10")
					|| msg.getProcessingCode().getFromAccount().equals("20"))
					&& (msg.getProcessingCode().getToAccount().equals("14")
							|| msg.getProcessingCode().getToAccount().equals("24"))) {

//				msgTran = msgTran.concat(msg.getStructuredData().get("B24_Field_103").substring(5, 9)).concat("_")
//						.concat(msg.getField(Iso8583.Bit._022_POS_ENTRY_MODE) != null
//								&& !msg.getField(Iso8583.Bit._022_POS_ENTRY_MODE).equals("010")
//								&& !msg.getField(Iso8583.Bit._022_POS_ENTRY_MODE).equals("021") ? "0" : "1");

				msgTran = msgTran.concat("0000").concat("_").concat("0");

				String variation = msg.getStructuredData().get("B24_Field_103").substring(2, 3);
				msgTran = msgTran.concat("_").concat(variation);
			}

			// TRANSFER
			else if ((msg.getStructuredData().get("B24_Field_41").substring(12, 13).equals("7")
					|| msg.getStructuredData().get("B24_Field_41").substring(12, 13).equals("1"))
					&& (msg.getProcessingCode().getFromAccount().equals("10")
							|| msg.getProcessingCode().getFromAccount().equals("20"))
					&& (msg.getProcessingCode().getToAccount().equals("10")
							|| msg.getProcessingCode().getToAccount().equals("20"))) {

				msgTran = msgTran.concat("0000").concat("_").concat("0");

				String variation = msg.getStructuredData().get("B24_Field_103").substring(2, 3);
				String cEntity = msg.getStructuredData().get("B24_Field_103").substring(3, 7);

				Logger.logLine(
						"ACH:" + variation.equals("2") + "-" + cEntity + "-" + sd.get("B24_Field_41").substring(12, 13),
						this.enableMonitor);

				if (variation.equals("2")
						&& (!cEntity.equals("0001") && !cEntity.equals("0002") && !cEntity.equals("0023")
								&& !cEntity.equals("0052"))
						&& msg.getStructuredData().get("B24_Field_41").substring(12, 13).equals("1")) {

					Logger.logLine("ACH", this.enableMonitor);

					variation = "ACH";

				}

				msgTran = msgTran.concat("_").concat(variation);

			}

			// TRANSFER CNB
			else if (msg.getStructuredData().get("B24_Field_41").substring(12, 13).equals("2")
					&& (msg.getProcessingCode().getFromAccount().equals("10")
							|| msg.getProcessingCode().getFromAccount().equals("20"))
					&& (msg.getProcessingCode().getToAccount().equals("10")
							|| msg.getProcessingCode().getToAccount().equals("20"))) {

				// POR REVISAR
				if (msg.getStructuredData().get("B24_Field_41").substring(4, 8).equals("8354")
						|| msg.getStructuredData().get("B24_Field_41").substring(4, 8).equals("8206")
						|| msg.getStructuredData().get("B24_Field_41").substring(4, 8).equals("8110")
						|| msg.getStructuredData().get("B24_Field_41").substring(4, 8).equals("9631")
						|| msg.getStructuredData().get("B24_Field_41").substring(4, 8).equals("9632")) {
					msgTran = msgTran.concat("0000").concat("_").concat("MASS");
					
					if (msg.getStructuredData().get("TRANSITORIA") != null && msg.getStructuredData().get("TRANSITORIA").equals("SI")){
						msgTran = "00";	
					}
		
				}
				else {
					msgTran = msgTran.concat("0000").concat("_").concat("0");
				}

				// PAGO DE CARTERA
//				if (msg.getStructuredData().get("B24_Field_103").substring(16, 17).equals("5")) {
//					msgTran = msgTran.concat("C");
//				}
				
				if (msg.getStructuredData().get("MAS_CARTERA") != null && msg.getStructuredData().get("MAS_CARTERA").equals("true")) {
					msgTran = msgTran.concat("C");
				}

				String variation = msg.getStructuredData().get("B24_Field_103").substring(2, 3);
				msgTran = msgTran.concat("_").concat(variation);

			}

			else {
				msgTran = msgTran.concat("0000").concat("_").concat("0");
			}

		}

		else if (msg.getProcessingCode().getTranType().substring(0, 1).equals("3")) {

			msgTran = msg.getMessageType().concat("_").concat(msg.getProcessingCode().toString());

			if (msg.getStructuredData().get("CHANNEL") != null && msg.getStructuredData().get("CHANNEL").equals("3")) {

				msgTran = msgTran.concat("_").concat(msg.getStructuredData().get("CHANNEL")).concat("_").concat("0000")
						.concat("_").concat("0000").concat("_").concat("0");

			} else {

				msgTran = msgTran.concat("_").concat(msg.getStructuredData().get("B24_Field_41").substring(12, 13))
						.concat("_").concat("0000").concat("_").concat("0000").concat("_").concat("0");

			}

		}

		// POBLIG
		else if (msg.getProcessingCode().getTranType().substring(0, 1).equals("5")) {

			if (msg.getStructuredData().get("CHANNEL") != null && msg.getStructuredData().get("CHANNEL").equals("3")) {

				msgTran = msg.getMessageType().concat("_").concat(msg.getProcessingCode().toString());

				msgTran = msgTran.concat("_").concat(msg.getStructuredData().get("CHANNEL")).concat("_").concat("0000")
						.concat("_").concat("0000").concat("_").concat("0");

			} else {

				msgTran = msgTran.concat("0000");

				if (msg.getField(Iso8583.Bit._035_TRACK_2_DATA).substring(0, 6).equals("777760")) {
					msgTran = msgTran.concat("_MULT");
//					String variation = msg.getStructuredData().get("B24_Field_103").substring(2, 3);
					String variation = "0";
					msgTran = msgTran.concat("_").concat(variation);

				} else {

					if ((msg.getProcessingCode().toString().equals("501030")
							|| msg.getProcessingCode().toString().equals("502030"))
							&& !msg.getStructuredData().get("B24_Field_41").substring(12, 13).equals("7")) {

//						String variation = msg.getStructuredData().get("B24_Field_103").substring(2, 3);
						String variation = "0";
						msgTran = msgTran.concat("_").concat(variation);

					} else {

						String variation = msg.getStructuredData().get("B24_Field_103").substring(2, 3);
//						String variation = "0";
						msgTran = msgTran.concat("_").concat(variation);

					}

				}

			}
		}

		else if (msg.getProcessingCode().getTranType().equals("00")) {

			msgTran = msg.getMessageType().concat("_").concat(msg.getProcessingCode().toString());

			if (msg.getStructuredData().get("CHANNEL") != null && msg.getStructuredData().get("CHANNEL").equals("3")) {

				msgTran = msgTran.concat("_").concat(msg.getStructuredData().get("CHANNEL")).concat("_").concat("0000")
						.concat("_").concat("0000").concat("_").concat("0");

			}

		}

		else if (msg.getProcessingCode().getTranType().equals("01")) {

			if (msg.getStructuredData().get("CHANNEL") != null && msg.getStructuredData().get("CHANNEL").equals("3")) {

				msgTran = msg.getMessageType().concat("_").concat(msg.getProcessingCode().toString());

				msgTran = msgTran.concat("_").concat(msg.getStructuredData().get("CHANNEL")).concat("_").concat("0000")
						.concat("_").concat("0000").concat("_").concat("0");

			} else {

				msgTran = msgTran.concat("0000").concat("_").concat("0");

				if (msg.getField(Iso8583.Bit._035_TRACK_2_DATA).substring(0, 6).equals("777790")) {

					if (sd.get("IS_COST_INQUIRY").equals("TRUE")) {
						msgTran = msgTran.concat("_").concat("GT_DUM");
					} else {

						if (sd.get("PHASE") != null && sd.get("PHASE").equals("P2")) {

							Logger.logLine("PHASE 2 KEY CREATION", this.enableMonitor);

							msgTran = "0200_011000_1_0000_0000_0_GT_P2";
							sd.put("PHASE", "P2");

						} else {
							
							if(msg.getMsgType() == 1056) {
								msgTran = "0420_011000_1_0000_0000_0_GT_P2";
								sd.put("PHASE", "P2");
							}
							else {
								msgTran = "0200_890000_1_0000_0000_0_GT_P1";
								sd.put("PHASE", "P1");
							}

						}

					}

				}

				else if (msg.getField(Iso8583.Bit._022_POS_ENTRY_MODE).substring(0, 2).equals("02")
						&& sd.get("B24_Field_41").substring(12, 13).equals("1")) {
					msgTran = msgTran.concat("_").concat("OTP");
				}

			}

		}

		else if (msg.getProcessingCode().getTranType().equals("21")) {

			msgTran = msgTran.concat("0000");

			String variation = msg.getStructuredData().get("B24_Field_103").substring(2, 3);
			msgTran = msgTran.concat("_").concat(variation);

		}

		else if (msg.getProcessingCode().getTranType().equals("20")) {

			msgTran = msg.getMessageType().concat("_").concat(msg.getProcessingCode().toString());

			if (msg.getField(Iso8583Post.Bit._022_POS_ENTRY_MODE).equals("010")) {

				msgTran = msgTran.concat("_").concat(msg.getStructuredData().get("CHANNEL")).concat("_").concat("0000")
						.concat("_").concat("0000").concat("_").concat("OTC");

			} else {

				msgTran = msgTran.concat("_").concat(msg.getStructuredData().get("CHANNEL")).concat("_").concat("0000")
						.concat("_").concat("0000").concat("_").concat("0");

			}

		}
		
		else if (msg.getProcessingCode().getTranType().equals("02")) {

			if (msg.getStructuredData().get("CHANNEL") != null && msg.getStructuredData().get("CHANNEL").equals("3")) {

				msgTran = msg.getMessageType().concat("_").concat(msg.getProcessingCode().toString());

				msgTran = msgTran.concat("_").concat(msg.getStructuredData().get("CHANNEL")).concat("_").concat("0000")
						.concat("_").concat("0000").concat("_").concat("0");

			} else {

				msgTran = msg.getMessageType().concat("_").concat(msg.getProcessingCode().toString());

				msgTran = msgTran.concat("_").concat(msg.getStructuredData().get("B24_Field_41").substring(12, 13)).concat("_").concat("0000")
						.concat("_").concat("0000").concat("_").concat("0");

			}

		}

		msg.putStructuredData(sd);

		return msgTran;
	}

	private TransactionSetting findTranSetting(String msgTranKey) {

		TransactionSetting tranSetting = null;

		Logger.logLine("TRANKEY: " + msgTranKey, this.enableMonitor);

		if (wholeTransConfig != null && wholeTransConfig.getAllTran() != null
				&& wholeTransConfig.getAllTran().length != 0) {

			for (int i = 0; i < wholeTransConfig.getAllTran().length; i++) {

				Logger.logLine("TRANKEY IN JSON: " + wholeTransConfig.getAllTran()[i].getTranKey(), this.enableMonitor);

				if (wholeTransConfig.getAllTran()[i].getTranKey().equals(msgTranKey)) {

					Logger.logLine("-- TRANKEY MATCHED --", this.enableMonitor);
					Logger.logLine(wholeTransConfig.getAllTran()[i].getDescription(), this.enableMonitor);
					tranSetting = wholeTransConfig.getAllTran()[i];
					break;
				}
			}
		}

		return tranSetting;
	}

	private MsgMappedResult constructMsgString(TransactionSetting trSetting, Iso8583Post inputMsg, boolean isRev)
			throws Exception {

		MsgMappedResult result = new MsgMappedResult();
		result.setInputMsg(inputMsg);
		result.setContainsError(false);

		StringBuilder msgStrBuilder = new StringBuilder();
		StructuredData msgSD = inputMsg.getStructuredData();

		Logger.logLine(msgSD.toString(), this.enableMonitor);

		if (trSetting.getPreOps().equals("1")) {
			if(!getCovenant(inputMsg)) {
				
				retrieveConvenants();
				
			}
	
		}

		else {
			Logger.logLine("NO SEARCH FOR COVENANT", this.enableMonitor);
		}

		Logger.logLine("NUEVA IMPL - MSG TO PROCESS: \n" + inputMsg.toString(), this.enableMonitor);

		for (int i = 0; i < trSetting.getFields().length; i++) {

			Field curField = trSetting.getFields()[i];

			Logger.logLine("CUR FIELD: " + i + " - " + curField.toString(), this.enableMonitor);

			switch (curField.getFieldType()) {
			case "fixed":

				if (curField.getDescription().equals("tran-code")) {
					StructuredData sd = inputMsg.getStructuredData();
					sd.put("ISC_TRAN_CODE", curField.getValue());
					inputMsg.putStructuredData(sd);
				}

				fixedField(curField, msgStrBuilder);

				break;
			case "copy":

				copyField(curField, msgStrBuilder, inputMsg, msgSD);

				break;
			case "homologate":

				homologateField(curField, msgStrBuilder, inputMsg, msgSD);

				break;
			case "method":

				if (curField.getTagPrefix().equals("119161"))
					prepareAcqNetworkTag9161(curField, msgStrBuilder, inputMsg);

				else if (curField.getTagPrefix().equals("11D140"))
					prepareTagD140(curField, msgStrBuilder, inputMsg);

				else if (curField.getTagPrefix().equals("tran-code"))
					prepareTranCodeRetiro(curField, msgStrBuilder, inputMsg);

				else if (curField.getTagPrefix().equals("11E0E2"))
					prepareTagE0E2(curField, msgStrBuilder, inputMsg);

				else if (curField.getTagPrefix().equals("state"))
					setStateHeaderTag(msgStrBuilder, hour4Check, isNextDay,
							inputMsg.getStructuredData().get("B24_Field_17"), isRev, inputMsg);

				else if (curField.getTagPrefix().equals("119405")) {
					TagCreationResult tagRes = extractTokenFromPinBlock(curField, msgStrBuilder, inputMsg);
					result.getErrors().add(tagRes);
					result.setContainsError(tagRes.getTagError() != null ? true : false);
				}

				else if (curField.getTagPrefix().equals("11912D"))
					prepareTag912D(curField, msgStrBuilder, inputMsg);

				break;
			default:
				break;
			}

		}

		result.setOutputMsg(msgStrBuilder.toString());

		return result;

	}

//	@Override
//	public Action processAdminReqFromTranmgr(AInterchangeDriverEnvironment interchange, Iso8583Post msg)
//			throws Exception {
//		Logger.logLine("600 recibido!!");
//		Iso8583Post rspNextDayMsgRsp = new Iso8583Post();
//		ISCReqMessage echoReqMsg = new ISCReqMessage();
//
//		if (msg.getStructuredData().get("NEXTDAY").equals("OFF")) {
//			this.isNextDay = false;
//			rspNextDayMsgRsp = (Iso8583Post) msg.clone();
//			rspNextDayMsgRsp.setMessageType(Iso8583.MsgTypeStr._0610_ADMIN_REQ_RSP);
//			rspNextDayMsgRsp.putField(Iso8583.Bit._039_RSP_CODE, "00");
//			echoReqMsg = null;
//			
//			Logger.logLine("610 RSP: " + rspNextDayMsgRsp.toString().concat("\n"));
//		}
//		else if(msg.getStructuredData().get("ECHO").equals("ON")) {
//			echoReqMsg = Utils.createEchoTestMsg();
//			
//			Logger.logLine("ECHO: " + echoReqMsg.toString().concat("\n"));
//		}
//
//		
//		
//		return new Action(rspNextDayMsgRsp, echoReqMsg, null, null);
//	}

//	@Override
//	public Action processAdminReqFromTranmgr(AInterchangeDriverEnvironment interchange, Iso8583Post msg)
//			throws Exception {
//		Logger.logLine("SABOR");
//		Iso8583Post rsp = new Iso8583Post();
//
//		if (msg.getStructuredData().get("NEXTDAY").equals("OFF")) {
//			this.isNextDay = false;
//			rsp = (Iso8583Post) msg.clone();
//			rsp.setMessageType(Iso8583.MsgTypeStr._0610_ADMIN_REQ_RSP);
//			rsp.putField(Iso8583.Bit._039_RSP_CODE, "00");
//		}
//
//		Logger.logLine("610 RSP: " + rsp.toString().concat("\n"));
//		return new Action(rsp, null, null, null);
//	}

	@Override
	public Action processAdminReqFromTranmgr(AInterchangeDriverEnvironment interchange, Iso8583Post msg)
			throws Exception {
//		Logger.logLine("600 recibido!!\n"+msg.toString());
		Logger.logLine("600 recibido!!\n", this.enableMonitor);

		Iso8583Post rspNextDayMsgRsp = new Iso8583Post();
		ISCReqMessage echoReqMsg = new ISCReqMessage();

		if (msg.getStructuredData().get("NEXTDAY") != null && msg.getStructuredData().get("NEXTDAY").equals("OFF")) {
			this.isNextDay = false;
//			Logger.logLine("610 RSP: " + rspNextDayMsgRsp.toString().concat("\n"));

			rspNextDayMsgRsp = (Iso8583Post) msg.clone();
			rspNextDayMsgRsp.setMessageType(Iso8583.MsgTypeStr._0610_ADMIN_REQ_RSP);
			rspNextDayMsgRsp.putField(Iso8583.Bit._039_RSP_CODE, "00");
			echoReqMsg = null;

		} else if (msg.getStructuredData().get("ECHO") != null && msg.getStructuredData().get("ECHO").equals("ON")) {
			Logger.logLine("ECHO", this.enableMonitor);
			echoReqMsg = Utils.createEchoTestMsg();
			rspNextDayMsgRsp = null;
			this.transStore.put("AT0009999", msg);
//			Logger.logLine("ECHO: " + echoReqMsg.toString().concat("\n"));
		}

		return new Action(rspNextDayMsgRsp, echoReqMsg, null, null);
	}

	private void fixedField(Field curField, StringBuilder msgStrBuilder) {

		String extractedVal = "";

		if (curField.isHeaderField()) {
			extractedVal = curField.getValueHex() != null ? curField.getValueHex()
					: UtilidadesMensajeria.asciiToEbcdic(curField.getValue()).toUpperCase();
			Logger.logLine(extractedVal + "--" + Transform.fromAsciiToEbcdic(extractedVal) + "--"
					+ UtilidadesMensajeria.asciiToEbcdic(extractedVal), this.enableMonitor);
			msgStrBuilder.append(extractedVal);
		}

		else {
			extractedVal = curField.getTagPrefix()
					.concat(UtilidadesMensajeria.asciiToEbcdic(curField.getValue()).toUpperCase());
			Logger.logLine(extractedVal + "--" + Transform.fromAsciiToEbcdic(extractedVal) + "--"
					+ UtilidadesMensajeria.asciiToEbcdic(extractedVal), this.enableMonitor);
			msgStrBuilder.append(extractedVal);
		}
	}

	private void copyField(Field curField, StringBuilder msgStrBuilder, Iso8583Post inputMsg, StructuredData msgSD)
			throws FileNotFoundException {

		String extractedVal = "";

		try {
			if (curField.getCopyFrom() == 1) {

				Logger.logLine("COPY ::" + inputMsg.getStructuredData().get(curField.getCopyTag()), this.enableMonitor);
				Logger.logLine("SD ::" + inputMsg.getStructuredData().get(curField.getCopyTag()), this.enableMonitor);

			}

			if (!curField.isHeaderField())
				msgStrBuilder.append(curField.getTagPrefix());

			if (curField.getCopyFrom() == 0)
				extractedVal = (curField.getCopyFinalIndex() == 0) ? inputMsg.getField(curField.getCopyTag())
						: inputMsg.getField(curField.getCopyTag()).substring(curField.getCopyInitialIndex(),
								curField.getCopyFinalIndex());
			else {
				if (inputMsg.getStructuredData().get(curField.getCopyTag()) != null) {
					extractedVal = (curField.getCopyFinalIndex() == 0)
							? inputMsg.getStructuredData().get(curField.getCopyTag())
							: inputMsg.getStructuredData().get(curField.getCopyTag())
									.substring(curField.getCopyInitialIndex(), curField.getCopyFinalIndex());
				} else {
					extractedVal = curField.getConditionalVal();
				}

			}
		} catch (Exception e) {
			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter(
					outError + "\n" + "ERROR COPYING FIELD: " + curField.getTagPrefix() + outError.toString()));
			Logger.logLine("ERROR COPYING FIELD: " + curField.getTagPrefix() + outError.toString(), this.enableMonitor);
			EventRecorder.recordEvent(
					new Exception("ERROR COPYING FIELD: " + curField.getTagPrefix() + outError.toString()));
		}

//			extractedVal = (curField.getCopyFinalIndex() == 0) ? msgSD.get(curField.getCopyTag()) :
//				msgSD.get(curField.getCopyTag()).substring(curField.getCopyInitialIndex(), curField.getCopyFinalIndex()) ;
//			extractedVal = msgSD.get(curField.getCopyTag()).substring(curField.getCopyInitialIndex(), curField.getCopyFinalIndex() == 0 ? msgSD.get(curField.getCopyTag()).length() - 1 : curField.getCopyFinalIndex()) ;	

		Logger.logLine(extractedVal + "--" + Transform.fromAsciiToEbcdic(extractedVal) + "--"
				+ UtilidadesMensajeria.asciiToEbcdic(extractedVal), this.enableMonitor);

		msgStrBuilder
				.append(UtilidadesMensajeria
						.asciiToEbcdic(Pack.resize(extractedVal, curField.getTagValueLength(),
								curField.getPadChar() == null ? '0' : curField.getPadChar().charAt(0), false))
						.toUpperCase());
	}

	private void homologateField(Field curField, StringBuilder msgStrBuilder, Iso8583Post inputMsg,
			StructuredData msgSD) {

		String extractedVal = "";
		boolean homolMatch = false;

		try {
			if (!curField.isHeaderField())
				msgStrBuilder.append(curField.getTagPrefix());

			if (curField.getCopyFrom() == 0)
				extractedVal = (curField.getCopyFinalIndex() == 0) ? inputMsg.getField(curField.getCopyTag())
						: inputMsg.getField(curField.getCopyTag()).substring(curField.getCopyInitialIndex(),
								curField.getCopyFinalIndex());
			else
				extractedVal = (curField.getCopyFinalIndex() == 0)
						? inputMsg.getStructuredData().get(curField.getCopyTag())
						: inputMsg.getStructuredData().get(curField.getCopyTag())
								.substring(curField.getCopyInitialIndex(), curField.getCopyFinalIndex());
//			extractedVal = msgSD.get(curField.getCopyTag()).substring(curField.getCopyInitialIndex(), curField.getCopyFinalIndex() == 0 ? msgSD.get(curField.getCopyTag()).length() - 1 : curField.getCopyFinalIndex()) ;	

		} catch (XPostilion e) {

			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter(outError));
			Logger.logLine("ERROR HOMOLOGATING FIELD: " + curField.getTagPrefix() + outError.toString(),
					this.enableMonitor);
			EventRecorder.recordEvent(
					new Exception("ERROR HOMOLOGATING FIELD: " + curField.getTagPrefix() + outError.toString()));

		}

		Logger.logLine(extractedVal + "--" + Transform.fromAsciiToEbcdic(extractedVal) + "--"
				+ UtilidadesMensajeria.asciiToEbcdic(extractedVal), this.enableMonitor);

		for (Homologation h : Arrays.asList(curField.getHomologations())) {

			if (h.getValue().equals(extractedVal)) {
				Logger.logLine("HOMOLOGACION MATCH-- homologation: " + h.getValue() + " ExtracVal" + extractedVal
						+ " convertion" + h.getConvertion(), this.enableMonitor);
				extractedVal = h.getConvertion();
				homolMatch = true;
				Logger.logLine("VAL homologated: " + extractedVal, this.enableMonitor);
				break;
			}

		}

		if (!homolMatch) {

			extractedVal = curField.getConditionalVal();

		}

		msgStrBuilder.append(UtilidadesMensajeria
				.asciiToEbcdic(Pack.resize(extractedVal, curField.getTagValueLength(), '0', false)).toUpperCase());
	}

	private boolean getCovenant(Iso8583Post msg) throws XPostilion {

		boolean foundedCovenant = false;
		StructuredData sd = msg.getStructuredData();

		String primCov = ISCInterfaceCB.convenios
				.get(msg.getStructuredData().get("B24_Field_103").substring(10)) != null
						? ISCInterfaceCB.convenios.get(msg.getStructuredData().get("B24_Field_103").substring(10))
						: "";

		Logger.logLine("CONVENIO PRIM BUSQUEDA - " + msg.getStructuredData().get("B24_Field_103").substring(10) + " "
				+ primCov, this.enableMonitor);

		if (!primCov.equals("") && primCov != null) {
			foundedCovenant = true;
			sd.put("PRIM_COV_ACCOUNT_TYPE", primCov.split("\\|")[0].equals("") ? "0" : primCov.split("\\|")[0]);
			sd.put("PRIM_COV_ACCOUNT_NR", primCov.split("\\|")[1].equals("") ? "0" : primCov.split("\\|")[1]);
			sd.put("PRIM_COV_SERVICE_NAME", primCov.split("\\|")[2].equals("") ? "0" : primCov.split("\\|")[2]);
			sd.put("PRIM_COV_SERVICE_TYPE", primCov.split("\\|")[3].equals("") ? "0" : primCov.split("\\|")[3]);
			sd.put("PRIM_COV_REPRO_INDICATOR", primCov.split("\\|")[4].equals("") ? "0" : primCov.split("\\|")[4]);
			sd.put("PRIM_COV_ABO", primCov.split("\\|")[5].equals("") ? "0" : primCov.split("\\|")[5]);
			sd.put("PRIM_COV_PAYMENT_TYPE", primCov.split("\\|")[6].equals("") ? "0" : primCov.split("\\|")[6]);
			
			if (primCov.split("\\|")[7].equals("S")) {
				sd.put("PRIM_COV_HAS_SUB", primCov.split("\\|")[7].equals("") ? "0" : primCov.split("\\|")[7]);
				sd.put("PRIM_COV_HAS_SUB_NR", primCov.split("\\|")[8].equals("") ? "0" : primCov.split("\\|")[8]);
				sd.put("SEC_COV_NR", primCov.split("\\|")[8].equals("") ? "0" : primCov.split("\\|")[8]);
			}

//			String secCov = msg.getStructuredData().get("B24_Field_104").substring(17) != null
//					? ISCInterfaceCB.convenios.get(msg.getStructuredData().get("B24_Field_103").substring(10)
//							.concat(msg.getStructuredData().get("B24_Field_104").substring(16)))
//					: "";
				
			String secCov = msg.getStructuredData().get("B24_Field_104").substring(17) != null
					? ISCInterfaceCB.convenios.get(sd.get("PRIM_COV_HAS_SUB_NR"))
					: "";
					
			Logger.logLine("SEC COVENANT:" + secCov, this.enableMonitor);

			if (secCov != "" && secCov != null) {
				sd.put("SEC_COV_ACCOUNT_TYPE", secCov.split("\\|")[0].equals("") ? "0" : secCov.split("\\|")[0]);
				sd.put("SEC_COV_ACCOUNT_NR", secCov.split("\\|")[1].equals("") ? "0" : secCov.split("\\|")[1]);
				sd.put("SEC_COV_SERVICE_NAME", secCov.split("\\|")[2].equals("") ? "0" : secCov.split("\\|")[2]);
				sd.put("SEC_COV_SERVICE_TYPE", secCov.split("\\|")[3].equals("") ? "0" : secCov.split("\\|")[3]);
				sd.put("SEC_COV_REPRO_INDICATOR", secCov.split("\\|")[4].equals("") ? "0" : secCov.split("\\|")[4]);
				sd.put("SEC_COV_ABO", primCov.split("\\|")[5].equals("") ? "0" : secCov.split("\\|")[5]);
				sd.put("SEC_COV_PAYMENT_TYPE", secCov.split("\\|")[6].equals("") ? "0" : secCov.split("\\|")[6]);
			}

			msg.putStructuredData(sd);

			Logger.logLine(
					"PRIM COVENANT " + msg.getStructuredData().get("B24_Field_103").substring(10) + ":" + primCov,
					this.enableMonitor);
		}

		return foundedCovenant;
	}

	private void prepareAcqNetworkTag9161(Field curField, StringBuilder msgStrBuilder, Iso8583Post msg)
			throws XPostilion {

		String acqNetwork = "";

		if (msg.getStructuredData().get("B24_Field_41").substring(0, 4).equals("0054")) {

			if (msg.getStructuredData().get("B24_Field_48").substring(0, 4).equals("0001")) {
				acqNetwork = ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2
						.concat(UtilidadesMensajeria.asciiToEbcdic("01"));
			} else if (msg.getStructuredData().get("B24_Field_48").substring(0, 4).equals("0002")) {
				acqNetwork = ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2
						.concat(UtilidadesMensajeria.asciiToEbcdic("09"));
			} else if (msg.getStructuredData().get("B24_Field_48").substring(0, 4).equals("0023")) {
				acqNetwork = ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2
						.concat(UtilidadesMensajeria.asciiToEbcdic("03"));
			} else if (msg.getStructuredData().get("B24_Field_48").substring(0, 4).equals("0052")) {
				acqNetwork = ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2
						.concat(UtilidadesMensajeria.asciiToEbcdic("04"));
			} else {
				acqNetwork = ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2
						.concat(UtilidadesMensajeria.asciiToEbcdic("99"));
			}

		} else {

			if (msg.getStructuredData().get("B24_Field_41").substring(4, 8).equals("1004")) {
				acqNetwork = ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2
						.concat(UtilidadesMensajeria.asciiToEbcdic("05"));
			} else if (msg.getStructuredData().get("B24_Field_41").substring(4, 8).equals("1005")) {
				acqNetwork = ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2
						.concat(UtilidadesMensajeria.asciiToEbcdic("07"));
			} else if (msg.getStructuredData().get("B24_Field_41").substring(4, 8).equals("1006")) {
				acqNetwork = ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2
						.concat(UtilidadesMensajeria.asciiToEbcdic("06"));
			} else {
				acqNetwork = ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2
						.concat(UtilidadesMensajeria.asciiToEbcdic("99"));
			}
		}

		Logger.logLine("cvb:" + acqNetwork, this.enableMonitor);

		msgStrBuilder.append(acqNetwork);
	}

	private void prepareTag912D(Field curField, StringBuilder msgStrBuilder, Iso8583Post msg) throws XPostilion {

		String tag912D = ISCReqMessage.Constants.TAG_11912D_AVAL_CREDIT_ACC_NR_20
				.concat(UtilidadesMensajeria.asciiToEbcdic(Pack.resize(msg.getStructuredData().get("CREDIT_ACCOUNT_NR"),
						curField.getTagValueLength(), ' ', false)));

		Logger.logLine("tag912D:" + tag912D, this.enableMonitor);

		msgStrBuilder.append(tag912D);
	}

	private void prepareTagD140(Field curField, StringBuilder msgStrBuilder, Iso8583Post msg) throws XPostilion {

		String tagD140 = "";

		if (msg.getStructuredData().get("B24_Field_41").substring(12, 13).equals("1")) {

			tagD140 = ISCReqMessage.Constants.TAG_11D140_ACQ_ENTITY_4.concat(
					UtilidadesMensajeria.asciiToEbcdic(msg.getStructuredData().get("B24_Field_41").substring(4, 8)));

		}

		else {

			tagD140 = ISCReqMessage.Constants.TAG_11D140_ACQ_ENTITY_4.concat(
					UtilidadesMensajeria.asciiToEbcdic(msg.getStructuredData().get("B24_Field_41").substring(0, 4)));

		}

		Logger.logLine("tagD140:" + tagD140, this.enableMonitor);

		msgStrBuilder.append(tagD140);
	}

	private void prepareTagE0E2(Field curField, StringBuilder msgStrBuilder, Iso8583Post msg) throws XPostilion {

		String tagE0E2 = "";

		if (msg.getStructuredData().get("B24_Field_41").substring(0, 4).equals("0001")) {

			if (msg.getStructuredData().get("ON_US_BIN").equals("TRUE")) {
				tagE0E2 = ISCReqMessage.Constants.TAG_11E0E2_TRAN_IDENTIFICATOR_4
						.concat(UtilidadesMensajeria.asciiToEbcdic("C003"));
			} else {
				tagE0E2 = ISCReqMessage.Constants.TAG_11E0E2_TRAN_IDENTIFICATOR_4
						.concat(UtilidadesMensajeria.asciiToEbcdic("C001"));
			}

		} else {

			if (msg.getStructuredData().get("ON_US_BIN").equals("TRUE")) {
				tagE0E2 = ISCReqMessage.Constants.TAG_11E0E2_TRAN_IDENTIFICATOR_4
						.concat(UtilidadesMensajeria.asciiToEbcdic("C002"));
			} else {
				tagE0E2 = ISCReqMessage.Constants.TAG_11E0E2_TRAN_IDENTIFICATOR_4
						.concat(UtilidadesMensajeria.asciiToEbcdic("C004"));
			}

		}

		Logger.logLine("tagE0E2:" + tagE0E2, this.enableMonitor);

		msgStrBuilder.append(tagE0E2);

	}

	private void prepareTranCodeRetiro(Field curField, StringBuilder msgStrBuilder, Iso8583Post msg) throws XPostilion {

		String TranCode = "";

		StructuredData sd = msg.getStructuredData();

		if (msg.getProcessingCode().getTranType().toString().equals("00")) {

			if (msg.getField(Iso8583.Bit._002_PAN).substring(0, 6).equals("450942")) {

				TranCode = UtilidadesMensajeria.asciiToEbcdic("POWV");
				sd.put("ISC_TRAN_CODE", "POWV");
				msg.putStructuredData(sd);

			}

			else {

				TranCode = UtilidadesMensajeria.asciiToEbcdic("POWI");
				sd.put("ISC_TRAN_CODE", "POWI");
				msg.putStructuredData(sd);

			}

		} else {

			if (msg.getField(Iso8583.Bit._002_PAN).substring(0, 6).equals("450942")) {

				TranCode = UtilidadesMensajeria.asciiToEbcdic("ATWV");
				sd.put("ISC_TRAN_CODE", "ATWV");
				msg.putStructuredData(sd);

			}

			else {

				TranCode = UtilidadesMensajeria.asciiToEbcdic("ATWI");
				sd.put("ISC_TRAN_CODE", "ATWI");
				msg.putStructuredData(sd);

			}

		}

		Logger.logLine("TranCode:" + TranCode, this.enableMonitor);

		msgStrBuilder.append(TranCode);
	}

	/**
	 * Metodo encargado de colocar el flag de estado para la mensajeria ISC
	 * 
	 * @param msgStrBuilder
	 * @param hour4Check
	 * @param isNextDay
	 * @param b24Field17
	 * @param isRev
	 * @throws Exception
	 */
	public void setStateHeaderTag(StringBuilder msgStrBuilder, String hour4Check, boolean isNextDay, String b24Field17,
			boolean isRev, Iso8583Post input) throws Exception {

		// Se verifica si el flag Nextday NO esta activo en la interface
		if (!isNextDay) {

			Logger.logLine("[requestISOFields2ISCFields][NextDay false]", this.enableMonitor);

			BusinessCalendar cal = null; // Calendario de Postilion "DefaultBusinessCalendar"
			Date b24Field17Date = null; // Fecha entrante en TAG "B24_Field_127"
			Date systDate = null; // Hora y Fecha de sistema
			Date calCurBDate = null; // Business day en el que se encuantra el calendario
			Date calNextBDate = null; // Proximo Business day en el que se encuantra el calendario

			cal = new BusinessCalendar("DefaultBusinessCalendar"); // Obtiene "DefaultBusinessCalendar"

			b24Field17Date = Utils
					.string2Date(Utils.getStringDate(Utils.YYMMDDhhmmss).substring(0, 2).concat(b24Field17), "yyMMdd");
			systDate = Utils.string2Date(Utils.getStringDate(Utils.YYMMDDhhmmss).substring(0, 6), "yyMMdd");
			calCurBDate = cal.getCurrentBusinessDate();
			calNextBDate = cal.getNextBusinessDate();

			Logger.logLine("[requestISOFields2ISCFields][Cur Business day]  \n[b24Field17Date] " + b24Field17Date
					+ "\n[systDate] " + systDate + "\n[calCurBDate] " + calCurBDate + "\n[calNextBDate] "
					+ calNextBDate, this.enableMonitor);

			if (cal.isHoliday(systDate) || !cal.isBusinessDay(systDate)) { // la fecha del sistema indica que es feriado
																			// o fin de semana

				// DIA NO LABORABLE o DIA FERIADO

				Logger.logLine("[requestISOFields2ISCFields][FERIADO o FINSEMANA]", this.enableMonitor);

				if (!isRev) {
					msgStrBuilder.append(UtilidadesMensajeria.asciiToEbcdic("008"));
				} else {
					msgStrBuilder.append(UtilidadesMensajeria.asciiToEbcdic("188"));
				}

			}

			else if (cal.isBusinessDay(systDate)) {

				// DIA LABORABLE

				Logger.logLine("[requestISOFields2ISCFields][DIA LABORABLE]", this.enableMonitor);

				if (Utils.checkThisHour(hour4Check, Utils.getStringDate(Utils.YYMMDDhhmmss).substring(6))) {

					// Hora EN rango

					Logger.logLine("[requestISOFields2ISCFields][HORA EN RANGO]", this.enableMonitor);

					if (!isRev) {
						msgStrBuilder.append(UtilidadesMensajeria.asciiToEbcdic("040"));
					} else {
						msgStrBuilder.append("F0FCF0");
					}

					this.isNextDay = true;
					updateNextdayPersistence(NEXTDAY, "true");

				} else {
					// Hora FUERA rango

					Logger.logLine("[requestISOFields2ISCFields][HORA FUERA RANGO]", this.enableMonitor);

					if (b24Field17Date.compareTo(calCurBDate) > 0) { // la fecha de la transaccion es futura a la fecha
																		// current en el calendario

						// FECHA FUTURA

						Logger.logLine("[requestISOFields2ISCFields][B24_Field_17 FUTURO]", this.enableMonitor);

						if (!isRev) {
							msgStrBuilder.append(UtilidadesMensajeria.asciiToEbcdic("040"));
						} else {
							msgStrBuilder.append("F0FCF0");
						}
					} else {

						if (!isRev) {
							msgStrBuilder.append(UtilidadesMensajeria.asciiToEbcdic("008"));
						} else {
							msgStrBuilder.append(UtilidadesMensajeria.asciiToEbcdic("188"));
						}

					}

				}

			}

		}
		// Si el flag Nextday SI esta activo en la interface
		else {

			Logger.logLine("[requestISOFields2ISCFields][NextDay true]", this.enableMonitor);

			if (!isRev) {
				msgStrBuilder.append(UtilidadesMensajeria.asciiToEbcdic("040"));
			} else {
				msgStrBuilder.append("F0FCF0");
			}

		}
		
		try {
			SettlementDate sd = new SettlementDate((Iso8583)input, hour4Check, isNextDay);
			Logger.logLine("CURR  NEXTDAY IMPL::"+msgStrBuilder.substring(msgStrBuilder.length() - 6), this.enableMonitor);
			Logger.logLine("CARDO NEXTDAY IMPL::"+sd.getFlagNextDate(), this.enableMonitor);
		}
		catch(Exception e) {
			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter(outError));
			Logger.logLine("NUEVO NEXTDAY ERROR: " +  outError.toString(),
					this.enableMonitor);
			EventRecorder.recordEvent(
					new Exception("NUEVO NEXTDAY ERROR: " +  outError.toString()));
		}

	}

	private void splitField54(StructuredData sd) {

		sd.put(Constant.TagNames.TRANSACTION_AMOUNT, sd.get(Constant.B24Fields.B24_F_54).substring(0, 12));
		sd.put(Constant.TagNames.DONATION_AMOUNT, sd.get(Constant.B24Fields.B24_F_54).substring(12, 24));
		sd.put(Constant.TagNames.SECURE_AMOUNT, sd.get(Constant.B24Fields.B24_F_54).substring(24, 36));

		putSetterAmountTag(sd);

	}

	private void putSetterAmountTag(StructuredData sd) {

		sd.put(Constant.TagNames.SETTER_AMOUNT,
				sd.get(Constant.TagNames.TRANSACTION_AMOUNT).equals(Constant.Misce.STR_TWELVE_ZEROS)
						? Constant.Misce.STR_ONE_ZERO
						: Constant.Misce.STR_ONE_ONE);
		sd.put(Constant.TagNames.SETTER_AMOUNT,
				sd.get(Constant.TagNames.DONATION_AMOUNT).equals("000000000000")
						? sd.get(Constant.TagNames.SETTER_AMOUNT).concat(Constant.Misce.STR_ONE_ZERO)
						: sd.get(Constant.TagNames.SETTER_AMOUNT).concat(Constant.Misce.STR_ONE_ONE));
		sd.put(Constant.TagNames.SETTER_AMOUNT,
				sd.get(Constant.TagNames.SECURE_AMOUNT).equals("000000000000")
						? sd.get(Constant.TagNames.SETTER_AMOUNT).concat(Constant.Misce.STR_ONE_ZERO)
						: sd.get(Constant.TagNames.SETTER_AMOUNT).concat(Constant.Misce.STR_ONE_ONE));

	}

	private void prepareTag9405(Field curField, StringBuilder msgStrBuilder, Iso8583Post msg) throws XPostilion {

		String tag9405 = "";

		tag9405 = "119405".concat(UtilidadesMensajeria.asciiToEbcdic("85958408"));

		Logger.logLine("tag9405:" + tag9405, this.enableMonitor);

		msgStrBuilder.append(tag9405);
	}

	private void updateNextdayPersistence(String property2Update, String value2update) {

		try (OutputStream output = new FileOutputStream(this.nextDayFileURL)) {
			Properties prop = new Properties(); // set the properties value
//			prop.setProperty(property2Update, value2update);
			prop.put(property2Update, value2update);
			prop.store(output, null);
			Logger.logLine("UPDATE PROPS:\n" + prop.toString(), this.enableMonitor);
		}

		catch (IOException io) {
			StringWriter outError = new StringWriter();
			io.printStackTrace(new PrintWriter(outError));
			Logger.logLine("ERROR IO: " + outError.toString(), this.enableMonitor);
			EventRecorder.recordEvent(new Exception(outError.toString()));
		}

	}

	public Action processSecondMsgPhase(Iso8583Post msg) throws XFieldUnableToConstruct, XPostilion, Exception {

		Logger.logLine("****processTranReqSecondPhase****\n" + new String(msg.toMsg()), this.enableMonitor);

//				Iso8583Post test2 = new Iso8583Post();
		String t2 = Transform.fromBinToHex(Transform.getString(msg.toMsg()));
//				test2.fromMsg(Transform.fromHexToBin(t2));
		Logger.logLine("ISO:" + t2, this.enableMonitor);

		// MONITOREO
		try {
			Client mon = new Client(this.monitorIP, this.monitorPort);
			if (msg != null)
				mon.sendData(Client.getMsgKeyValue(msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR),
						Transform.fromBinToHex(Transform.getString(msg.toMsg())), "ISO", this.interName));
		} catch (Exception e) {
			Logger.logLine("ERROR CLIENTE MONITOR", this.enableMonitor);
		}

		boolean echoMsg = msg.getStructuredData() != null
				? msg.getStructuredData().get("ECHO_TEST_MSG") != null ? true : false
				: false;

		IMessage msg2Remote;
		Iso8583Post msg2TM;

		// NUEVA IMPLEMENTACION
		String msgKey = "";
		try {

			if (msg.getProcessingCode().getTranType().substring(0, 2).equals("32")) {

				StructuredData sd = msg.getStructuredData();

				String b24F126 = sd.get("B24_Field_126");

				Logger.logLine("OPERACION 32: " + b24F126, this.enableMonitor);

				String realPCode = b24F126.substring(b24F126.indexOf("QT")).substring(8, 14);

				if (((realPCode.substring(0, 1).equals("5") || realPCode.substring(0, 2).equals("27"))
						&& sd.get("B24_Field_103").substring(2, 3).equals("1"))) {

					msgKey = "00_1";

				} else if ((realPCode.substring(0, 2).equals("51")
						&& msg.getField(Iso8583.Bit._035_TRACK_2_DATA).substring(0, 6).equals("777760")
						&& sd.get("TRANSACTION_TYPE_CBN").equals("CREDITO"))) {

					msgKey = "00_1";

				} else if ((realPCode.substring(0, 2).equals("40")
						&& msg.getField(Iso8583.Bit._035_TRACK_2_DATA).substring(0, 6).equals("777760")
						&& msg.getField(Iso8583.Bit._022_POS_ENTRY_MODE).equals("021"))) {

					msgKey = "00_1";

				} else {

					if (realPCode.substring(0, 2).equals("27")) {

						if (realPCode.substring(4, 6).equals("10") || realPCode.substring(4, 6).equals("20")) {
							realPCode = "21".concat(realPCode.substring(2));
						} else {
							realPCode = "51".concat(realPCode.substring(2));
						}

					}
					sd.put("REAL PCODE", realPCode);
					sd.put("IS_COST_INQUIRY", "TRUE");
					msg.putStructuredData(sd);
					Iso8583Post msgCloned = (Iso8583Post) msg.clone();
					msgCloned.putField(Iso8583.Bit._003_PROCESSING_CODE, realPCode);
					msgKey = constructMessageKey(msgCloned);

				}

			} else {

				StructuredData sd = msg.getStructuredData();
				sd.put("IS_COST_INQUIRY", "FALSE");
				msg.putStructuredData(sd);

				msgKey = constructMessageKey(msg);
			}

			Logger.logLine(
					"AAAA NUEVA IMPLEMENTACION ::  <msg type>_<p code>_<canal>_<acq entity>_<aut entity>_<efectivo (0 tarjeta - 1 efectivo)>_<variation> ",
					this.enableMonitor);
			Logger.logLine("NUEVA IMPLEMENTACION :: TRAN KEY: " + msgKey, this.enableMonitor);

			if (msgKey != null && msgKey != "") {

				if (!msgKey.substring(0, 2).equals("00")) {

					strTranSetting = findTranSetting(msgKey);
					if (strTranSetting == null) {
						msgKey = "06_1";
					}

				}

			} else {
				EventRecorder.recordEvent(new Exception("ERROR CREANDO MSG KEY "));

				// MONITOREO
				try {
					Client mon = new Client(this.monitorIP, this.monitorPort);
					if (msg != null)
						mon.sendData(Client.getMsgKeyValue(msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR),
								"ERRISOZ4" + Transform.fromBinToHex(Transform.getString(msg.toMsg())), "ISO",
								this.interName));
				} catch (Exception e) {
					Logger.logLine("ERROR CLIENTE MONITOR", this.enableMonitor);
				}

				Logger.logLine("ERROR CREANDO MSG KEY ", this.enableMonitor);
			}

		} catch (XPostilion e) {

			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter(outError));
			EventRecorder.recordEvent(new Exception("NUEVA IMPLEMENTACION: " + outError.toString()));

		}
		// NUEVA IMPLEMENTACION

		Logger.logLine("NUEVA IMPLEMENTACION:" + strTranSetting, this.enableMonitor);

		if (!this.ignoreHttp2) {
			try {
				wc.createPOSTRequest2("http://localhost:8080/entry-point/iso2isc",
						Transform.fromBinToHex(new String(msg.getBinaryData())));
			} catch (Exception e) {
				Logger.logLine("Error WS " + "http://localhost:8080/entry-point/iso2isc" + " " + e.getMessage(),
						this.enableMonitor);
			}
		}

		// Se determina el canal el mismo viene en la posición 13 del Tag "B24_Field_41"
		String canal = Utils.getTranChannel(msg.getStructuredData().get("B24_Field_41"));

		// Se determina el tipo de transacción "AAAA_BBBBBB_C"
		// AAAA-Tipo de Msg ; BBBBBB-Codigo proceso ; C-canal
		String tranType = Utils.getTranType(msg, canal);

		// Se invoca al metodo getTransactionConsecutive a fin de obtener el consecutivo
		// para la transaación
		String cons = getTransactionConsecutive(msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR).substring(5, 9), "00",
				this.termConsecutiveSection);

		Logger.logLine("CONSE:" + cons, this.enableMonitor);

		msg2Remote = null;
		msg2TM = null;
		switch (msgKey) {
		case "05":
			msg2TM = createErrorRspMsg(msg, "COVENENT_NOT_FOUND", "06");

			// MONITOREO
			try {
				Client mon = new Client(this.monitorIP, this.monitorPort);
				if (msg != null)
					mon.sendData(Client.getMsgKeyValue(msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR),
							"DECISOZ5" + Transform.fromBinToHex(Transform.getString(msg.toMsg())), "ISO",
							this.interName));
			} catch (Exception e) {
				Logger.logLine("ERROR CLIENTE MONITOR", this.enableMonitor);
			}

			break;

		case "05_1":
			msg2TM = createErrorRspMsg(msg, "NOT_ON_US_COVENANT", "06");

			// MONITOREO
			try {
				Client mon = new Client(this.monitorIP, this.monitorPort);
				if (msg != null)
					mon.sendData(Client.getMsgKeyValue(msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR),
							"DECISOZ6" + Transform.fromBinToHex(Transform.getString(msg.toMsg())), "ISO",
							this.interName));
			} catch (Exception e) {
				Logger.logLine("ERROR CLIENTE MONITOR", this.enableMonitor);
			}

			break;

		case "06_1":
			msg2TM = createErrorRspMsg(msg, "TRANSACCION NO CONFIGURADA", "06");

			// MONITOREO
			try {
				Client mon = new Client(this.monitorIP, this.monitorPort);
				if (msg != null)
					mon.sendData(Client.getMsgKeyValue(msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR),
							"DECISOZ7" + Transform.fromBinToHex(Transform.getString(msg.toMsg())), "ISO",
							this.interName));
			} catch (Exception e) {
				Logger.logLine("ERROR CLIENTE MONITOR", this.enableMonitor);
			}

			break;

		case "00":
			Logger.logLine("BATCH APP SD:" + msg.getStructuredData().toString(), this.enableMonitor);
			msg2TM = createErrorRspMsg(msg, "BATCH_APPROVED", "00");
			msg2TM.putField(Iso8583.Bit._038_AUTH_ID_RSP,
					cons.split(",")[0].trim().substring(2).concat(cons.split(",")[1].trim().substring(1)));
			break;

		case "00_1":
			Logger.logLine("BATCH CCOSTO:" + msg.getStructuredData().toString(), this.enableMonitor);
			msg2TM = createErrorRspMsg(msg, "BATCH_APPROVED_CCOSTO_OBLIGACION", "00");
			msg2TM.putField(Iso8583.Bit._038_AUTH_ID_RSP,
					cons.split(",")[0].trim().substring(2).concat(cons.split(",")[1].trim().substring(1)));
			break;

		default:

			// verificación del numero consecutivo
			if (cons == null || cons.trim().equals("")) {

				String errorMsg = "Error recuperando el consecutivo para la transaccion: "
						+ msg.getField(Iso8583Post.Bit._037_RETRIEVAL_REF_NR);
				msg2TM = (Iso8583Post) createErrorRspMsg(msg, errorMsg, "06");
				EventRecorder.recordEvent(new Exception(errorMsg));
				Logger.logLine("ERROR:" + errorMsg, this.enableMonitor);

			} else {

				mapISOFieldsInISOMessage(msg);
				addingAdditionalStructuredDataFields(msg, cons, tranType);

				Logger.logLine("SECUENCIA DE TRAN REQ: " + msg.getStructuredData().get(Constant.TagNames.SEQ_TERMINAL),
						this.enableMonitor);
				Logger.logLine("TRAN_TYPE----->" + tranType, this.enableMonitor);
				Logger.logLine("NUEVA IMPLEMENTACION :: TRAN SETTING: " + strTranSetting.getTranKey(),
						this.enableMonitor);

				if (strTranSetting.getPreOps() != null && strTranSetting.getPreOps().equals("1")) {

					//

				}

				MsgMappedResult resFromMapping = constructMsgString(strTranSetting, msg, false);

				if (resFromMapping.isContainsError()) {

					Logger.logLine("RESULT AAA" + resFromMapping.getErrors().get(0).toString(), this.enableMonitor);

					for (TagCreationResult t : resFromMapping.getErrors()) {
						if (t.getTagError() != null) {
							msg2TM = createErrorRspMsg(msg, resFromMapping.getErrors().get(0).getTagError(), "06");
							msg2Remote = null;
							break;
						}
					}

					// PARA BORRAR
//					ISCReqMessage req = new ISCReqMessage();
//					req.fromHexStr(
//							"313233343536E5C9C2C1114040C1E3C3C7C1E3F6F8F8404040F0F1F3F4f0f4f0F1F6F5F6F3F34040404040404040F0F0F0F0F0F0F0F0119130F0F5F1F0F2F111C3F0F0F0F0F0F0F0F0F0F1F0F0F0F0F0F011E5C6F7F7F7F7F9F0F0F0F7F9F7F2F0F9F0F111912FF0F0F0F0F0F1F3F411E4F2C3119405F0F8F5F6F6F6F6F311E4F3F0F0F0F7F9F7F2F0F9F0F1119249F9F9F6F1F3");
//					msg2Remote = req;
//
//					StructuredData sd = msg.getStructuredData();
//					sd.put("NEXTDAY_STATE_FLAG",
//							Transform.fromEbcdicToAscii(req.getField(ISCReqMessage.Fields._09_H_STATE)));
//					msg.putStructuredData(sd);
//
//					this.transStore.put(cons.split(Constant.Misce.STR_COMA)[0].trim()
//							.concat(cons.split(Constant.Misce.STR_COMA)[1].trim()), msg);
					// PARA BORRAR
				} else {

					Logger.logLine("NUEVA IMPLEMENTACION :: STR MESSAGE : " + resFromMapping.getOutputMsg(),
							this.enableMonitor);
					if (resFromMapping.getOutputMsg().length() < 20) {
						msg2TM = createErrorRspMsg(msg, "CRYPTO ERROR", "06");
						msg2Remote = null;
					} else {
						ISCReqMessage req = new ISCReqMessage();
						req.fromHexStr(resFromMapping.getOutputMsg());
						msg2Remote = req;

						StructuredData sd = msg.getStructuredData();
						sd.put("NEXTDAY_STATE_FLAG",
								Transform.fromEbcdicToAscii(req.getField(ISCReqMessage.Fields._09_H_STATE)));
						msg.putStructuredData(sd);

						this.transStore.put(cons.split(Constant.Misce.STR_COMA)[0].trim()
								.concat(cons.split(Constant.Misce.STR_COMA)[1].trim()), msg);

						// }
						// NUEVA IMPLEMENTACION

						Logger.logLine("[MSG][OutFromTM] \n" + msg.toString(), this.enableMonitor);
						// Logger.logLine("[MSG][OutFromTM SD] \n" + msg.getStructuredData());

						Logger.logLine(
								"========================================\n========================================\n========================================\n",
								this.enableMonitor);

					}

				}

			}

			break;
		}

		// MONITOREO
		try {
			Client mon = new Client(this.monitorIP, this.monitorPort);
			if (msg2TM != null)
				mon.sendData(Client.getMsgKeyValue(msg2TM.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR),
						msg2TM.toString(), "ISO", this.interName));
			if (msg2Remote != null)
				mon.sendData(Client.getMsgKeyValue((msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR)),
						msg2Remote.toString(), "ISC", this.interName));

		} catch (Exception e) {
			Logger.logLine("ERROR CLIENTE MONITOR", this.enableMonitor);
		}

		return new Action(msg2TM, msg2Remote, null, null);

	}

	@SuppressWarnings("unused")
	private TagCreationResult extractTokenFromPinBlock(Field curField, StringBuilder msgStrBuilder, Iso8583Post msg)
			throws XPostilion {

		TagCreationResult res = new TagCreationResult(curField.getTagPrefix(), null, null);

		String token = "";

		String encodParam0 = "BASE64";

		try {
			CryptoCfgManager crypcfgman = CryptoManager.getStaticConfiguration();
			this.kwpParam1 = crypcfgman.getKwp(this.kwpName);
		} catch (XCrypto e) {
			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter(outError));
			Logger.logLine("KWP ERROR: " + outError.toString(), this.enableMonitor);
			EventRecorder.recordEvent(new Exception(outError.toString()));
		}

		String param1 = Base64.getEncoder().encodeToString(this.kwpParam1.getValueUnderKsk().getBytes());

		this.kvpParam2 = "MVBVTkUwMDAsRUJDOEJDNjM0MEM2RkUyRjYxMTU2M0Y0MjY4MDdEMjM0OUI5QjdCNDU4NDNCMDk2LDg4Q0NEOTk5MDNFMjE2QTY";

		Logger.logLine("PIN DATA: " + msg.getField(Iso8583.Bit._052_PIN_DATA), this.enableMonitor);
		Logger.logLine("PIN DATA to HEX: " + Transform.fromBinToHex(msg.getField(Iso8583.Bit._052_PIN_DATA)),
				this.enableMonitor);
		Logger.logLine(
				"PIN DATA to HEX to B64: " + Base64.getEncoder()
						.encodeToString(Transform.fromBinToHex(msg.getField(Iso8583.Bit._052_PIN_DATA)).getBytes()),
				this.enableMonitor);

		String param3 = Base64.getEncoder()
				.encodeToString(Transform.fromBinToHex(msg.getField(Iso8583.Bit._052_PIN_DATA)).getBytes());

		String param4 = Base64.getEncoder().encodeToString(msg.getTrack2Data().getPan().getBytes());

		String hexSeedParam5 = "MEIxQTJDM0Q0RjVFNjc4OTk4NzZGNEU1RDNDMkIwQTEwQjFBMkMzRDRGNUU2Nzg5";

		Logger.logLine("KWP: " + Base64.getEncoder().encodeToString(this.kwpParam1.getValueUnderKsk().getBytes()),
				this.enableMonitor);

		String endPoint = "https://10.89.0.169:8081/entry-point/getPIN?encoding=%s&workingKey1=%s&workingKey2=%s&pinBlock=%s&pan=%s&seeds=%s";
		String[] params = new String[6];
		params[0] = encodParam0;
		params[1] = param1;
		params[2] = this.kvpParam2;
		params[3] = param3;
		params[4] = param4;
		params[5] = hexSeedParam5;

		token = HttpCryptoServ.httpConnection(endPoint, params);
		token = new String(Base64.getMimeDecoder().decode(token));

		Logger.logLine("TOKEN: " + token, this.enableMonitor);

		msgStrBuilder.append(curField.getTagPrefix().concat(UtilidadesMensajeria
				.asciiToEbcdic(Pack.resize(token, curField.getTagValueLength(), '0', false)).toUpperCase()));

		if (token.equals("") || token.equals(" ")) {

			res.setTagError("CRYPTO ERROR : NULL ");
		} else if (token.equals("E: Sanity - 080000")) {

			res.setTagError("CRYPTO ERROR : E: Sanity - 080000");
		} else if (token.equals("E: Sanity - N")) {

			res.setTagError("CRYPTO ERROR : E: Sanity - N");
		}

		return res;

	}
	
	private void retrieveConvenants() throws IOException {
		
		List<String> strCovenats = Arrays.asList(this.wc.retriveAllCovenatData().split(","));

		Logger.logLine("COVENANT SIZE" + strCovenats.size(), this.enableMonitor);

		for (String e : strCovenats) {
			String k = e.split(":")[0].replace("\\", "").replace("\"", "");
			String v = e.split(":")[1].replace("\\", "").replace("\"", "");
			Logger.logLine("COV " + k + " -- " + v, this.enableMonitor);
			ISCInterfaceCB.convenios.put(k, v);
			Logger.logLine("MAP " + ISCInterfaceCB.convenios.get(k), this.enableMonitor);
		}
		
	}

	private static final String STR_008 = "008";
	private static final String NEXTDAY = "NEXTDAY";

	private static final List<String> bines = new ArrayList<>();

	static {

		bines.add("406238");
		bines.add("401096");
		bines.add("403722");
		bines.add("403799");
		bines.add("407665");
		bines.add("414529");
		bines.add("417947");
		bines.add("417948");
		bines.add("417949");
		bines.add("419328");
		bines.add("419329");
		bines.add("421544");
		bines.add("423383");
		bines.add("426045");
		bines.add("430274");
		bines.add("433460");
		bines.add("446872");
		bines.add("450668");
		bines.add("450886");
		bines.add("450942");
		bines.add("450952");
		bines.add("454062");
		bines.add("454200");
		bines.add("457320");
		bines.add("457321");
		bines.add("457537");
		bines.add("457542");
		bines.add("457563");
		bines.add("457564");
		bines.add("457602");
		bines.add("457603");
		bines.add("457604");
		bines.add("457605");
		bines.add("459504");
		bines.add("459505");
		bines.add("459918");
		bines.add("459919");
		bines.add("464620");
		bines.add("465770");
		bines.add("466090");
		bines.add("470435");
		bines.add("474630");
		bines.add("477363");
		bines.add("477364");
		bines.add("485935");
		bines.add("486412");
		bines.add("486514");
		bines.add("491511");
		bines.add("491602");
		bines.add("491603");
		bines.add("491614");
		bines.add("491615");
		bines.add("491616");
		bines.add("491617");
		bines.add("491626");
		bines.add("491627");
		bines.add("491628");
		bines.add("493110");
		bines.add("493111");
		bines.add("496083");
		bines.add("499812");
		bines.add("512069");
		bines.add("512804");
		bines.add("512827");
		bines.add("515864");
		bines.add("520137");
		bines.add("520354");
		bines.add("522104");
		bines.add("523433");
		bines.add("529198");
		bines.add("531088");
		bines.add("531122");
		bines.add("531126");
		bines.add("539612");
		bines.add("540080");
		bines.add("543862");
		bines.add("548494");
		bines.add("548940");
		bines.add("552221");
		bines.add("552865");
		bines.add("553661");

	}
	
	public static final String TRAN_RETIRO_AHORRO = "0200_011000_1";
	public static final String TRAN_RETIRO_CORRIENTE = "0200_012000_1";
	public static final String TRAN_COMPRA_AHORRO = "0200_001000_1";
	public static final String TRAN_COMPRA_CORRIENTE = "0200_002000_1";
	public static final String TRAN_COSULTA_COSTO = "0200_320000_1";
	public static final String RSP_TRAN_RETIRO_AHORRO = "0210_011000_1";
	public static final String RSP_TRAN_RETIRO_CORRIENTE = "0210_012000_1";
	public static final String RSP_TRAN_GIRO_AHORRO = "0210_011000_GIRO";
	public static final String RSP_TRAN_GIRO_CORRIENTE = "0210_012000_GIRO";
	public static final String RSP_TRAN_RETIRO_CREDIBANCO_AHORRO = "0210_011000_3";
	public static final String RSP_TRAN_RETIRO_CREDIBANCO_CORRIENTE = "0210_012000_3";
	public static final String RSP_TRAN_RETIRO_CNB_AHORRO = "0210_501043_1";
	public static final String RSP_TRAN_RETIRO_CNB_CORRIENTE = "0210_502043_1";
	public static final String RSP_TRAN_COMPRA_AHORRO = "0210_001000_1";
	public static final String RSP_TRAN_COMPRA_CORRIENTE = "0210_002000_1";
	public static final String RSP_TRAN_COMPRA_CREDIBANCO_AHORRO = "0210_001000_3";
	public static final String RSP_TRAN_COMPRA_CREDIBANCO_CORRIENTE = "0210_002000_3";
	public static final String RSP_TRAN_ANULACION_CREDIBANCO_AHORRO = "0210_201000_3";
	public static final String RSP_TRAN_ANULACION_CREDIBANCO_CORRIENTE = "0210_202000_3";
	public static final String RSP_TRAN_COSULTA_COSTO = "0210_320000_1";
	public static final String RSP_TRAN_COSULTA_ULT5_AH = "0210_381000_1";
	public static final String RSP_TRAN_COSULTA_ULT5_CO = "0210_382000_1";
	public static final String RSP_TRAN_COSULTA_COSTO_AH = "0210_321000_1";
	public static final String RSP_TRAN_COSULTA_COSTO_CO = "0210_322000_1";
	public static final String RSP_TRAN_COSULTA_SALDO_CB_AH = "0210_311000_1";
	public static final String RSP_TRAN_COSULTA_SALDO_CB_CO = "0210_312000_1";
	public static final String RSP_TRAN_COSULTA_SALDO_CREDIBANCO_AH = "0210_311000_3";
	public static final String RSP_TRAN_COSULTA_SALDO_CREDIBANCO_CO = "0210_312000_3";
	public static final String RSP_TRAN_COSULTA_CUPO_ROT = "0210_314000_1";
	public static final String RSP_TRAN_PAGO_TDC_AHORRO = "0210_501030_1";
	public static final String RSP_TRAN_PAGO_CREDITO_HIPO = "0210_501000_1";
	public static final String RSP_TRAN_PAGO_TDC_CORRIENTE = "0210_502030_1";
	public static final String RSP_TRAN_POB_1040 = "0210_501040_1";
	public static final String RSP_TRAN_POB_2040 = "0210_502040_1";
	public static final String RSP_TRAN_POB_1041 = "0210_501041_1";
	public static final String RSP_TRAN_POB_2041 = "0210_502041_1";
	public static final String RSP_TRAN_POB_0000 = "0210_500000_1";
	public static final String RSP_TRAN_PSP_AHORRO = "0210_401000_1";
	public static final String RSP_TRAN_PSP_CORRIENTE = "0210_402000_1";
	public static final String RSP_TRAN_DEPOSITO_AH_AH = "0210_401014_1";
	public static final String RSP_TRAN_DEPOSITO_AH_CO = "0210_401024_1";
	public static final String RSP_TRAN_DEPOSITO_CO_AH = "0210_402014_1";
	public static final String RSP_TRAN_DEPOSITO_CO_CO = "0210_402024_1";
	public static final String RSP_TRAN_DEPOSITO_MUL_AH = "0210_210110_1";
	public static final String RSP_TRAN_DEPOSITO_MUL_CO = "0210_210120_1";
	public static final String RSP_PAGO_CREDITO_MULT = "0210_510141_1";
	public static final String APPROVAL_STATE_HEX = "F0F0";
	public static final String APPROVED_SECURE = "1";
	public static final String NON_APPROVED_SECURE = "2";
	public static final String RSP_TRAN_TRANSFER_AHO_AHO = "0210_401010_1";
	public static final String RSP_TRAN_TRANSFER_COR_AHO = "0210_402010_1";
	public static final String RSP_TRAN_TRANSFER_AHO_COR = "0210_401020_1";
	public static final String RSP_TRAN_TRANSFER_COR_COR = "0210_402020_1";
	public static final String RSP_TRAN_RETIRO_401420_OTP = "0210_401420_1";
	public static final String RSP_TRAN_RETIRO_402420_OTP = "0210_402420_1";
	public static final String RSP_TRAN_RETIRO_401410_OTP = "0210_401410_1";
	public static final String RSP_TRAN_RETIRO_402410_OTP = "0210_402410_1";
	public static final String RSP_PAGO_OBLIG = "0210_500000_1";
	public static final char CHAR_PIPE = '|';

	private static final String REGEX_ERROR = "[A-Z]{2}(\\d{4})(.)[A-Z]{1}:(.)";

	private static final String DELIMITADOR = "11C2601D60";

	private static final Map<String, String> OUTPUT_TEMPS = new LinkedHashMap<>();

	static {

		OUTPUT_TEMPS.put(
				"(.*)COMISION:(.*)SALDO DISPONIBLE:(.*)PIGNORACIONES(.*):(.*)IDENTIFI:(.*)SECUENCIA(.*)FRML(.*)AVSEGURO(.*)MONTOPA(.*)TASAPA(.*)PLAZOPA(.*)CUOTAPA(.*)FORZAPA(.*)",
				"comision=$2\nsaldo_disponible=$3\npignoraciones=$5\nidentificacion=$6\nsecuencia=$7\nfrml=$8\nav_seguro=$9\npa_monto=$10\npa_tasa=$11\npa_plazo=$12\npa_cuota=$13\npa_forza=$14");

		OUTPUT_TEMPS.put(
				"(.*)COMISION:(.*)SALDO DISPONIBLE:(.*)PIGNORACIONES(.*):(.*)IDENTIFI:(.*)SECUENCIA(.*)FRML(.*)AVSEGURO(.*)MONTOPA(.*)TASAPA(.*)PLAZOPA(.*)CUOTAPA(.*)FORZAPA",
				"comision=$2\nsaldo_disponible=$3\npignoraciones=$5\nidentificacion=$6\nsecuencia=$7\nfrml=$8\nav_seguro=$9\npa_monto=$10\npa_tasa=$11\npa_plazo=$12\npa_cuota=$13\npa_forza=99");

		OUTPUT_TEMPS.put(
				"(.*)COMISION:(.*)nombre(.*)saldo(.*)IDENTIFI:(.*)SECUENCIA(.*)FRML(.*)AVSEGURO(.*)MONTOPA(.*)TASAPA(.*)PLAZOPA(.*)CUOTAPA(.*)FORZAPA(.*)",
				"comision=$2\nnombre=$3\nsaldo_total=$4\nidentificacion=$5\nsecuencia=$6\nfrml=$7\nav_seguro=$8\npa_monto=$9\npa_tasa=$10\npa_plazo=$11\npa_cuota=$12\npa_forza=$13");

		OUTPUT_TEMPS.put(
				"(.*)COMISION:(.*)nombre(.*)saldo(.*)IDENTIFI:(.*)SECUENCIA(.*)FRML(.*)AVSEGURO(.*)MONTOPA(.*)TASAPA(.*)PLAZOPA(.*)CUOTAPA(.*)FORZAPA",
				"comision=$2\nnombre=$3\nsaldo_total=$4\nidentificacion=$5\nsecuencia=$6\nfrml=$7\nav_seguro=$8\npa_monto=$9\npa_tasa=$10\npa_plazo=$11\npa_cuota=$12\npa_forza=99");

		OUTPUT_TEMPS.put(
				"(.*)COMISION:(.*)SALDO DISPONIBLE:(.*)PIGNORACIONES(.*):(.*)IDENTIFI:(.*)SECUENCIA(.*)FRML(.*)AVSEGURO(.*)",
				"comision=$2\nsaldo_disponible=$3\npignoraciones=$5\nidentificacion=$6\nsecuencia=$7\nfrml=$8\nav_seguro=$9\n");

		OUTPUT_TEMPS.put("(.*)COMISION:(.*)nombre(.*)saldo(.*)IDENTIFI:(.*)SECUENCIA(.*)FRML(.*)AVSEGURO(.*)",
				"comision=$2\nnombre=$3\nsaldo_total=$4\nidentificacion=$5\nsecuencia=$6\nfrml=$7\nav_seguro=$8\n");

		OUTPUT_TEMPS.put("(.*)COMISION:(.*)nombre(.*)saldo(.*)IDENTIFI:(.*)SECUENCIA(.*)",
				"comision=$2\nnombre=$3\nsaldo_total=$4\nidentificacion=$5\nsecuencia=$6\n");

		OUTPUT_TEMPS.put(
				"(.*)COMISION:(.*)SALDO DISPONIBLE:(.*)PIGNORACIONES(.*)IDENTIFI:(.*)SECUENCIA(.*)INDNAL:(.*)TIPTRANS:(.*)NOMBRE2:(.*)",
				"comision=$2\nsaldo_disponible=$3\npignoraciones=$4\nidentificacion=$5\nsecuencia=$6\ntipo_transaccion=$7tipo_transporte=$8nombre2=$9\n");

		OUTPUT_TEMPS.put("(.*)COMISION:(.*)SALDO DISPONIBLE:(.*)PIGNORACIONES(.*)IDENTIFI:(.*)SECUENCIA(.*)TIPODOC(.*)",
				"comision=$2\nsaldo_disponible=$3\npignoraciones=$4\nidentificacion=$5\nsecuencia=$6\ntipo_doc=$7\n");

		OUTPUT_TEMPS.put("(.*)COMISION:(.*)SALDO DISPONIBLE:(.*)PIGNORACIONES(.*)IDENTIFI:(.*)SECUENCIA(.*)",
				"comision=$2\nsaldo_disponible=$3\npignoraciones=$4\nidentificacion=$5\nsecuencia=$6\n");

		OUTPUT_TEMPS.put(
				"(.*)CCCON01(.*)COMISION:(.*)SECUENCIA(.*)MOVIMIEN1(.*)MOVIMIEN2(.*)MOVIMIEN3(.*)MOVIMIEN4(.*)MOVIMIEN5(.*)",
				"saldos=$2\ncomision=$3\nsecuencia=$4\nmovimien_1=$5\nmovimien_2=$6\nmovimien_3=$7\nmovimien_4=$8\nmovimien_5=$9\n");

		OUTPUT_TEMPS.put(
				"(.*)CCCON01(.*)COMISION:(.*)SECUENCIA(.*)MOVIMIEN1(.*)MOVIMIEN2(.*)MOVIMIEN3(.*)MOVIMIEN4(.*)",
				"saldos=$2\ncomision=$3\nsecuencia=$4\nmovimien_1=$5\nmovimien_2=$6\nmovimien_3=$7\nmovimien_4=$8\n");

		OUTPUT_TEMPS.put("(.*)CCCON01(.*)COMISION:(.*)SECUENCIA(.*)MOVIMIEN1(.*)MOVIMIEN2(.*)MOVIMIEN3(.*)",
				"saldos=$2\ncomision=$3\nsecuencia=$4\nmovimien_1=$5\nmovimien_2=$6\nmovimien_3=$7\n");

		OUTPUT_TEMPS.put("(.*)CCCON01(.*)COMISION:(.*)SECUENCIA(.*)MOVIMIEN1(.*)MOVIMIEN2(.*)",
				"saldos=$2\ncomision=$3\nsecuencia=$4\nmovimien_1=$5\nmovimien_2=$6\n");

		OUTPUT_TEMPS.put("(.*)CCCON01(.*)COMISION:(.*)SECUENCIA(.*)MOVIMIEN1(.*)",
				"saldos=$2\ncomision=$3\nsecuencia=$4\nmovimien_1=$5\n");

		OUTPUT_TEMPS.put("(.*)CCCON01(.*)COMISION:(.*)SECUENCIA(.*)TIPODOC(.*)",
				"saldos=$2\ncomision=$3\nsecuencia=$4\ntipo_doc=$5\n");

		OUTPUT_TEMPS.put("(.*)CCCON01(.*)COMISION:(.*)SECUENCIA(.*)", "saldos=$2\ncomision=$3\nsecuencia=$4\n");

	}

	private static final String PROC_TRAN_REQ_FROM_TM = "processTranReqFromTranmgr";
	private static final String PROC_NET_ADV_FROM_INTER = "processNwrkMngAdvFromInterchange";
	private static final String PROC_ACQ_REV_FROM_TM = "processAcquirerRevAdvFromTranmgr";
	private static final String PROC_ACQ_REV_FROM_INTER = "processAcquirerRevAdvRspFromInterchange";
	private static final String PROC_ACQ_FILE_ADV_FROM_TM = "processAcquirerFileUpdateAdvFromTranmgr";
	
	private static final String B24_Field_126 = "B24_Field_126";
	private static final String B24_Field_103 = "B24_Field_103";
	private static final String PSEUDO_BIN_777760 = "777760";
	private static final String IS_COST_INQUIRY = "IS_COST_INQUIRY";
	private static final String TRUE = "TRUE";
	private static final String FALSE = "FALSE";
}