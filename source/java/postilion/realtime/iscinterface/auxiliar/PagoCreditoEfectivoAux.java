package postilion.realtime.iscinterface.auxiliar;

import java.text.SimpleDateFormat;
import java.util.Date;

import postilion.realtime.genericinterface.eventrecorder.events.TryCatchException;
import postilion.realtime.genericinterface.translate.bitmap.Base24Ath;
import postilion.realtime.iscinterface.ISCInterfaceCB;
import postilion.realtime.iscinterface.database.DBHandler;
import postilion.realtime.iscinterface.message.ISCReqInMsg;
import postilion.realtime.iscinterface.message.ISCReqInMsg.Fields;
import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.iscinterface.util.Utils;
import postilion.realtime.iscinterface.web.model.TransactionSetting;
import postilion.realtime.sdk.env.calendar.BusinessCalendar;
import postilion.realtime.sdk.eventrecorder.EventRecorder;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.message.bitmap.StructuredData;
import postilion.realtime.sdk.message.bitmap.XFieldUnableToConstruct;
import postilion.realtime.sdk.util.DateTime;
import postilion.realtime.sdk.util.XPostilion;
import postilion.realtime.sdk.util.convert.Pack;
import postilion.realtime.sdk.util.convert.Transform;

public class PagoCreditoEfectivoAux {

	// PAGO DE OBLIGACIONES
	public static final String PCODE_PAGO_OBLIGACIONES_CREDITO_HIPOTECARIO_AHORROS = "501000";
	public static final String PCODE_PAGO_OBLIGACIONES_CREDITO_HIPOTECARIO_CORRIENTE = "502000";
	public static final String PCODE_PAGO_OBLIGACIONES_TARJETA_CREDITO_AHORROS = "501030";
	public static final String PCODE_PAGO_OBLIGACIONES_TARJETA_CREDITO_CORRIENTE = "502030";
	public static final String PCODE_PAGO_OBLIGACIONES_CREDITOROTATIVO_CREDISERVICES_DINEROEXTRA_AHORROS = "501040";
	public static final String PCODE_PAGO_OBLIGACIONES_CREDITOROTATIVO_CREDISERVICES_DINEROEXTRA_CORRIENTE = "502040";
	public static final String PCODE_PAGO_OBLIGACIONES_OTROS_CREDITOS_AHORROS = "501041";
	public static final String PCODE_PAGO_OBLIGACIONES_OTROS_CREDITOS_CORRIENTE = "502041";
	public static final String PCODE_PAGO_OBLIGACIONES_VEHICULOS_AHORROS = "501042";
	public static final String PCODE_PAGO_OBLIGACIONES_VEHICULOS_CORRIENTE = "502042";

	public static final String PCODE_PAGO_OBLIGACIONES_HIPOTECARIO_EFECTIVO = "500100";
	public static final String PCODE_PAGO_OBLIGACIONES_HIPOTECARIO_CHEQUE = "500200";

	public static final String PCODE_PAGO_OBLIGACIONES_TC_EFECTIVO = "500130";
	public static final String PCODE_PAGO_OBLIGACIONES_TC_CHEQUE = "500230";

	public static final String PCODE_PAGO_OBLIGACIONES_ROTATIVO_EFECTIVO = "500140";
	public static final String PCODE_PAGO_OBLIGACIONES_ROTATIVO_CHEQUE = "500240";

	public static final String PCODE_PAGO_OBLIGACIONES_OTROS_EFECTIVO = "500141";
	public static final String PCODE_PAGO_OBLIGACIONES_OTROS_CHEQUE = "500241";
	public static final String PCODE_PAGO_OBLIGACIONES_PAGO_MOTOS_Y_VEHICULOS_EFECTIVO = "500142";
	public static final String PCODE_PAGO_OBLIGACIONES_PAGO_MOTOS_Y_VEHICULOS_CHEQUE = "500242";

	// ACA SE HACE CON LA HOJA GUIA_ISC
	public static final int COD_OFICINA_INI = 18;
	public static final int COD_OFICINA_FIN = 18 + 4;
	public static final int NUM_SEQ_TX_INI = 22;
	public static final int NUM_SEQ_TX_FIN = 22 + 4;
	public static final int BYTES_ESTADO_INI = 26;
	public static final int BYTES_ESTADO_FIN = 26 + 3;
	public static final int HORA_INI = 29;
	public static final int HORA_FIN = 29 + 6;
	public static final int NUMERO_IDENTIFICACION_INI = 51;
	public static final int NUMERO_IDENTIFICACION_FIN = 51 + 16;
	public static final int TIPO_IDENTIFICACION_INI = 71;
	public static final int TIPO_IDENTIFICACION_FIN = 71 + 1;
	public static final int CODIGO_ENTIDAD_QUE_AUTORIZA_EL_CREDITO_INI = 72;
	public static final int CODIGO_ENTIDAD_QUE_AUTORIZA_EL_CREDITO_FIN = 72 + 4;
	public static final int TIPO_CUENTA_ACREDITAR_INI = 76;
	public static final int TIPO_CUENTA_ACREDITAR_FIN = 76 + 1;
	public static final int NUMERO_CUENTA_ACREDITAR_INI = 77;
	public static final int NUMERO_CUENTA_ACREDITAR_FIN = 77 + 16;
	public static final int TIPO_CUENTA_DEBITADA_INI = 93;
	public static final int TIPO_CUENTA_DEBITADA_FIN = 93 + 1;
	public static final int NUMERO_CUENTA_DEBITADA_INI = 94;
	public static final int NUMERO_CUENTA_DEBITADA_FIN = 94 + 16;
	public static final int VALOR_TRANSFERENCIA_INI = 110;
	public static final int VALOR_TRANSFERENCIA_FIN = 110 + 14;
	public static final int SECUENCIA_TS_INI = 124;
	public static final int SECUENCIA_TS_FIN = 124 + 20;
	public static final int MODALIDAD_DE_PAGO_INI = 144;
	public static final int MODALIDAD_DE_PAGO_FIN = 144 + 1;
	public static final int NATURALEZA_TRANSACCION_INI = 145;
	public static final int NATURALEZA_TRANSACCION_FIN = 145 + 1;
	public static final int CLASE_PAGO_INI = 146;
	public static final int CLASE_PAGO_FIN = 146 + 1;
	public static final int FECHA_APLICACION_DEL_PAGO_INI = 147;
	public static final int FECHA_APLICACION_DEL_PAGO_FIN = 147 + 8;
	public static final int NUM_SEQ_TX_ACTUAL_INI = 206;
	public static final int NUM_SEQ_TX_ACTUAL_FIN = 206 + 4;
	public static final int NUM_SEQ_TX_ORIG_A_REVERSAR_INI = 210;
	public static final int NUM_SEQ_TX_ORIG_A_REVERSAR_FIN = 210 + 4;
	public static final int NOMB_OFI_ADQUIRIENTE_INI = 219;
	public static final int NOMB_OFI_ADQUIRIENTE_FIN = 219 + 18;
	public static final int CODIGO_DANE_INI = 214;
	public static final int CODIGO_DANE_FIN = 214 + 5;
	public static final int COD_OFICINA_ADQUIRIENTE_INI = 181;
	public static final int COD_OFICINA_ADQUIRIENTE_FIN = 181 + 4;
	public static final int NUMERO_CEDULA_PERSONA_QUE_PAGA_CON_CHEQUE_INI = 834;
	public static final int NUMERO_CEDULA_PERSONA_QUE_PAGA_CON_CHEQUE_FIN = 834 + 11;
	public static final int CANTIDAD_DE_CHEQUE_INI = 832;
	public static final int CANTIDAD_DE_CHEQUE_FIN = 832 + 2;
	public static final String INITIAL_SPACE = "   ";

	public Iso8583Post processMsg(Iso8583Post out, ISCReqInMsg in, TransactionSetting tSetting, String cons,
			boolean enableMonitor, boolean isNextDay) throws XPostilion {

		String tramaCompletaAscii = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString()));
		tramaCompletaAscii = INITIAL_SPACE.concat(tramaCompletaAscii);

		try {

			BusinessCalendar objectBusinessCalendar = new BusinessCalendar("DefaultBusinessCalendar");
			Date businessCalendarDate = null;
			String settlementDate = null;
			String tranType = null;

			Logger.logLine("Reflected:\n" + in.toString(), enableMonitor);

			StructuredData sd = null;
			StructuredData sdOriginal = new StructuredData();
			if (out.getStructuredData() != null) {
				sd = out.getStructuredData();
			} else {
				sd = new StructuredData();
			}

			if(Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._10_H_NEXTDAY_IND)).equals("1")
					|| isNextDay) {
				businessCalendarDate = objectBusinessCalendar.getNextBusinessDate();
				settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
			}else {
				businessCalendarDate = objectBusinessCalendar.getCurrentBusinessDate();
				settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
			}

			String keyReverse = "";

			String p37 = "0901".concat(tramaCompletaAscii.substring(COD_OFICINA_ADQUIRIENTE_INI, COD_OFICINA_ADQUIRIENTE_FIN))
					.concat(tramaCompletaAscii.substring(NUM_SEQ_TX_ACTUAL_INI, NUM_SEQ_TX_ACTUAL_FIN));
			String p12 = new DateTime().get("HHmmss");
			String p13 = new DateTime().get("MMdd");

			String key = "0200".concat(p37).concat(p13).concat(p12).concat("00").concat(settlementDate);
			String key420 = "0420".concat(p37).concat(p13).concat(p12).concat("00").concat(settlementDate);
			String keyAnulacion = "0200".concat(p37).concat(p13).concat(p12).concat("00").concat(settlementDate);
			String seqNr = tramaCompletaAscii.substring(NUM_SEQ_TX_ACTUAL_INI, NUM_SEQ_TX_ACTUAL_FIN);
			String seqNrReverse = tramaCompletaAscii.substring(NUM_SEQ_TX_ORIG_A_REVERSAR_INI, NUM_SEQ_TX_ORIG_A_REVERSAR_FIN);

			String p41 = "0001".concat(tramaCompletaAscii.substring(COD_OFICINA_ADQUIRIENTE_INI, COD_OFICINA_ADQUIRIENTE_FIN))
					.concat("00003   ");
			String bin = "008801";
			String binExtract = "008801";
			String p43 = tramaCompletaAscii.substring(COD_OFICINA_ADQUIRIENTE_INI, COD_OFICINA_ADQUIRIENTE_FIN)
					.concat(tramaCompletaAscii.substring(NOMB_OFI_ADQUIRIENTE_INI, NOMB_OFI_ADQUIRIENTE_FIN))
					.concat(tramaCompletaAscii.substring(CODIGO_DANE_INI, CODIGO_DANE_FIN)).concat("             ");
			String codEntidadAut = tramaCompletaAscii.substring(CODIGO_ENTIDAD_QUE_AUTORIZA_EL_CREDITO_INI,
					CODIGO_ENTIDAD_QUE_AUTORIZA_EL_CREDITO_FIN);
			if (codEntidadAut.equals("0002")) {
				bin = "007702";
				binExtract = "007702";
			} else if (codEntidadAut.equals("0023")) {
				bin = "007723";
				binExtract = "007723";
			} else if (codEntidadAut.startsWith("0052")) {
				bin = "000052";
				binExtract = "007752";
			} else if (codEntidadAut.startsWith("0001")) {
				bin = "007701";
				binExtract = "007701";
			}

			String tipoCuentaDebitar = "";
			if (tramaCompletaAscii.substring(MODALIDAD_DE_PAGO_INI, MODALIDAD_DE_PAGO_FIN).equals("1")) {
				tipoCuentaDebitar = "01";
				sd.put("Indicador_Efectivo_Cheque", "1");
			} else if (tramaCompletaAscii.substring(MODALIDAD_DE_PAGO_INI, MODALIDAD_DE_PAGO_FIN).equals("2")) {
				tipoCuentaDebitar = "02";
				sd.put("Indicador_Efectivo_Cheque", "2");
				sd.put("Numero_Cheques", tramaCompletaAscii.substring(CANTIDAD_DE_CHEQUE_INI, CANTIDAD_DE_CHEQUE_FIN));
			}

			String naturaleza = tramaCompletaAscii.substring(NATURALEZA_TRANSACCION_INI, NATURALEZA_TRANSACCION_FIN);
			String ValiTipoCuentaAcreditar = tramaCompletaAscii.substring(TIPO_CUENTA_ACREDITAR_INI,
					TIPO_CUENTA_ACREDITAR_FIN);
			String tipoCuentaCreditar = "";
			if (ValiTipoCuentaAcreditar.equals("3")) {
				switch (naturaleza) {
				case "1":
					tipoCuentaCreditar = "00";
					break;
				case "2":
					tipoCuentaCreditar = "40";
					break;
				case "3":
					tipoCuentaCreditar = "41";
					break;
				case "4":
					tipoCuentaCreditar = "42";
					break;
				case "5":
					tipoCuentaCreditar = "30";
					break;

				default:
					tipoCuentaCreditar = "00";
					break;
				}
			} else {
				tipoCuentaCreditar = "00";
			}
			String tipoPago = "";
			if (tipoCuentaCreditar.equals("30") && (codEntidadAut.substring(1, 2).equals("0"))) {
				tipoPago = "0";
			} else if (tipoCuentaCreditar.equals("30") && (codEntidadAut.substring(1, 2).equals("1"))) {
				tipoPago = "1";
			} else {
				switch (tramaCompletaAscii.substring(CLASE_PAGO_INI, CLASE_PAGO_FIN)) {
				case "2":
					tipoPago = "2";
					break;
				case "3":
					tipoPago = "3";
					break;
				case "4":
					tipoPago = "4";
					break;
				case "5":
					tipoPago = "5";
					break;

				default:
					tipoPago = "1";
					break;
				}
			}

			tranType = "50";

			String cuentaCreditar = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString()
					.substring(ISCReqInMsg.POS_ini_CREDIT_ACC_NR, ISCReqInMsg.POS_end_CREDIT_ACC_NR)));

			// CAMPO 37 Retrieval Reference Number
			out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, p37);

			// PROCESAMIENTO DE REVERSO
			if (Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("080")
					|| Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("020")) {

				sdOriginal = DBHandler.getKeyOriginalTxBySeqNr(seqNrReverse);
				Logger.logLine("sdOriginal:\n" + sdOriginal, enableMonitor);
				keyReverse = sdOriginal.get("KeyOriginalTx");
				if (keyReverse == null) {
					keyReverse = "0000000000";
					sd.put("REV_DECLINED", "TRUE");
				} else {
					out.putField(Iso8583.Bit._090_ORIGINAL_DATA_ELEMENTS, Pack.resize(keyReverse, 42, '0', true));

					out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, key420);
					// out.putPrivField(Iso8583Post.PrivBit._011_ORIGINAL_KEY, keyReverse);
					sd.put("B24_Field_95", "000000000000000000000000000000000000000000");
					sd.put("KEY_REVERSE", keyReverse);
					sd.put("B24_Field_90", keyReverse + "0000000000");
					// sd.put("B24_Field_37", keyReverse.substring(4,16));

					if (Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("020")) {
						tranType = "20";
						sd.put("ANULACION", "TRUE");
						sd.put("B24_Field_15", settlementDate);
						sd.put("B24_Field_38", sdOriginal.get("Autorizacion_Original"));
						sd.put("KeyOriginalTx", keyReverse);
						sd.put("B24_Field_54", "000".concat(sdOriginal.get("Monto_Original"))
								.concat("000000000000000000").concat(sdOriginal.get("Monto_Original")));
						out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, keyAnulacion);
					}
				}

				// PROCESAMIENTO TX FINANCIERA
			} else {
				Logger.logLine("msg in TransferAux:\n" + in.getTotalHexString(), enableMonitor);
				String p125 = tramaCompletaAscii.substring(CANTIDAD_DE_CHEQUE_INI, CANTIDAD_DE_CHEQUE_FIN)
						.concat("                              ")
						.concat(tramaCompletaAscii.substring(NUMERO_CEDULA_PERSONA_QUE_PAGA_CON_CHEQUE_INI,
								NUMERO_CEDULA_PERSONA_QUE_PAGA_CON_CHEQUE_FIN));

				// 127.22 TAG B24_Field_125
				sd.put("B24_Field_125", p125);

				out.putField(Iso8583Post.Bit._059_ECHO_DATA,
						tramaCompletaAscii.substring(NUM_SEQ_TX_ACTUAL_INI, NUM_SEQ_TX_ACTUAL_FIN));

				// 127.2 SWITCHKEY
				out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, key);
			}
			// CAMPO 3 CODIGO DE PROCESO
			out.putField(Iso8583.Bit._003_PROCESSING_CODE,
					tranType.concat(tipoCuentaDebitar).concat(tipoCuentaCreditar));

//			//CAMPO 7 TRANSMISSION DATE N TIME
			out.putField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME, new DateTime(5).get("MMddHHmmss"));

			out.putField(Iso8583.Bit._013_DATE_LOCAL, p13);

			out.putField(Iso8583.Bit._015_DATE_SETTLE, settlementDate);

			// TRACK2 Field 35
			out.putField(Iso8583.Bit._035_TRACK_2_DATA, "0088010000000000000=9912000");

			// TRACK2 Field 43
			out.putField(Iso8583.Bit._043_CARD_ACCEPTOR_NAME_LOC, p43);

			// CAMPO 103 CREDIT ACCOUNT
			out.putField(Iso8583.Bit._103_ACCOUNT_ID_2, "011"
					.concat(tramaCompletaAscii.substring(CODIGO_ENTIDAD_QUE_AUTORIZA_EL_CREDITO_INI,
							CODIGO_ENTIDAD_QUE_AUTORIZA_EL_CREDITO_FIN))
					.concat("0")
					.concat(tramaCompletaAscii.substring(NUMERO_CUENTA_ACREDITAR_INI, NUMERO_CUENTA_ACREDITAR_FIN)));

//ojo cuadro verde
			// 127.22 TAG B24_Field_17
			sd.put("B24_Field_17", settlementDate);
			// 127.22 TAG B24_Field_35
			sd.put("B24_Field_35", bin.concat(Pack.resize(cuentaCreditar, 18, '0', false)).concat("=            "));
			// 127.22 TAG B24_Field_41
			sd.put("B24_Field_41", p41);

			if (!Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("020")) {
				////////// TAGS EXTRACT

				sd.put("VIEW_ROUTER", "V2");

				sd.put("Codigo_FI_Origen", "1019");
				sd.put("Nombre_FI_Origen", "CIC");
				sd.put("Identificacion_Canal", "OF");
				sd.put("TRANSACTION_TYPE_CBN", "CREDITO");
				sd.put("Canal", "01");
				sd.put("Dispositivo", "D");
				if (tipoCuentaCreditar.equals("30")) {
					sd.put("Codigo_Transaccion_Producto", "02");
					sd.put("Codigo_Transaccion", "01");
					sd.put("Tipo_de_Cuenta_Debitada", "CRE");
				} else {
					sd.put("Codigo_Transaccion_Producto", "06");
					sd.put("Codigo_Transaccion", "01");
				}
				tagTTypePOblig(out, sd);
				sd.put("Nombre_Transaccion", "DEPOSI");
				sd.put("CLIENT_CARD_NR_1", binExtract.concat("0000000000000"));
				sd.put("CLIENT_CARD_NR_1_REV", "007701".concat("0000000000000"));
				sd.put("PRIM_ACCOUNT_NR", Pack.resize(cuentaCreditar, 18, '0', false));
				sd.put("Codigo_de_Red", "1019");
				sd.put("Identificacion_Canal", "OF");
				sd.put("Codigo_Establecimiento", "          ");
				sd.put("SEC_ACCOUNT_TYPE", "   ");
				sd.put("PAN_Tarjeta", binExtract.concat("0000000000   "));
				sd.put("PAN_Tarjeta_REV", "007701".concat("0000000000   "));
				sd.put("Indicador_AVAL", "1");
				sd.put("Vencimiento", "0000");
				sd.put("SECUENCIA", tramaCompletaAscii.substring(SECUENCIA_TS_INI, SECUENCIA_TS_FIN));
				sd.put("Ofi_Adqui",
						tramaCompletaAscii.substring(COD_OFICINA_ADQUIRIENTE_INI, COD_OFICINA_ADQUIRIENTE_FIN));
				sd.put("Numero_Terminal",
						tramaCompletaAscii.substring(COD_OFICINA_ADQUIRIENTE_INI, COD_OFICINA_ADQUIRIENTE_FIN));
				sd.put("Clase_Pago", "2");
				sd.put("Ent_Adq", "0001");
				sd.put("Ofi_Adqui_REV", "0000");
				sd.put("Dispositivo", "0");
				sd.put("Indicador_Autorizacion_REV", "0");
				sd.put("Indicador_Efectivo_Cheque_REV", "0");
				sd.put("Canal", "01");
				sd.put("service_restriction_code", "000");
				sd.put("pos_entry_mode", "000");
				sd.put("Entidad", "0000");
				sd.put("Identificador_Terminal", "0");
				sd.put("Numero_Cedula", tramaCompletaAscii.substring(NUMERO_CEDULA_PERSONA_QUE_PAGA_CON_CHEQUE_INI,
						NUMERO_CEDULA_PERSONA_QUE_PAGA_CON_CHEQUE_FIN));

				///////// FIN TAGS EXTRACT
			}
			sd.put("IN_MSG", in.getTotalHexString());

			out.putStructuredData(sd);

		} catch (Exception e) {
			e.toString();
			EventRecorder.recordEvent(new Exception("ERROR processMsg: " + e.toString()));
			EventRecorder.recordEvent(new Exception("ERROR getmessage: " + e.getMessage()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					"Method :[" + "processMsg" + "]\n" + "processMsg: " + "\n", Utils.getStringMessageException(e) }));
		}

		return out;
	}

	public static void tagTTypePOblig(Iso8583Post out, StructuredData sd) throws XFieldUnableToConstruct {

		switch (out.getProcessingCode().toString()) {
		case PCODE_PAGO_OBLIGACIONES_CREDITO_HIPOTECARIO_AHORROS:
		case PCODE_PAGO_OBLIGACIONES_CREDITO_HIPOTECARIO_CORRIENTE:
			sd.put("Mod_Credito", "1");
			sd.put("Mod_CreditoX1", "1");
			break;

		case PCODE_PAGO_OBLIGACIONES_TARJETA_CREDITO_AHORROS:
		case PCODE_PAGO_OBLIGACIONES_TARJETA_CREDITO_CORRIENTE:
			sd.put("Mod_Credito", "5");
			sd.put("Mod_CreditoX1", "5");
			break;

		case PCODE_PAGO_OBLIGACIONES_CREDITOROTATIVO_CREDISERVICES_DINEROEXTRA_AHORROS:
		case PCODE_PAGO_OBLIGACIONES_CREDITOROTATIVO_CREDISERVICES_DINEROEXTRA_CORRIENTE:
			sd.put("Mod_Credito", "2");
			sd.put("Mod_CreditoX1", "2");
			break;

		case PCODE_PAGO_OBLIGACIONES_OTROS_CREDITOS_AHORROS:
		case PCODE_PAGO_OBLIGACIONES_OTROS_CREDITOS_CORRIENTE:
			sd.put("Mod_Credito", "3");
			sd.put("Mod_CreditoX1", "3");
			break;

		case PCODE_PAGO_OBLIGACIONES_HIPOTECARIO_EFECTIVO:
			sd.put("Mod_Credito", "1");
			sd.put("Mod_CreditoX1", "1");
			break;

		case PCODE_PAGO_OBLIGACIONES_HIPOTECARIO_CHEQUE:

			sd.put("Mod_Credito", "1");
			sd.put("Mod_CreditoX1", "1");
			break;

		case PCODE_PAGO_OBLIGACIONES_TC_EFECTIVO:
			sd.put("Mod_Credito", "5");
			sd.put("Mod_CreditoX1", "5");
			break;

		case PCODE_PAGO_OBLIGACIONES_TC_CHEQUE:
			sd.put("Mod_Credito", "5");
			sd.put("Mod_CreditoX1", "5");
			break;

		case PCODE_PAGO_OBLIGACIONES_ROTATIVO_EFECTIVO:
			sd.put("Mod_Credito", "2");
			sd.put("Mod_CreditoX1", "2");
			break;

		case PCODE_PAGO_OBLIGACIONES_ROTATIVO_CHEQUE:
			sd.put("Mod_Credito", "2");
			sd.put("Mod_CreditoX1", "2");
			break;

		case PCODE_PAGO_OBLIGACIONES_OTROS_EFECTIVO:
			sd.put("Mod_Credito", "3");
			sd.put("Mod_CreditoX1", "3");
			break;

		case PCODE_PAGO_OBLIGACIONES_OTROS_CHEQUE:
			sd.put("Mod_Credito", "3");
			sd.put("Mod_CreditoX1", "3");
			break;

		case PCODE_PAGO_OBLIGACIONES_VEHICULOS_AHORROS:
		case PCODE_PAGO_OBLIGACIONES_VEHICULOS_CORRIENTE:
			sd.put("Mod_Credito", "4");
			sd.put("Mod_CreditoX1", "4");
			break;

		}
	}

}
