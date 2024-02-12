package postilion.realtime.iscinterface.auxiliar;


import java.text.SimpleDateFormat;
import java.util.Date;

import postilion.realtime.genericinterface.eventrecorder.events.TryCatchException;
import postilion.realtime.iscinterface.ISCInterfaceCB;
import postilion.realtime.iscinterface.database.DBHandler;
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

public class TransferAuxCel {
	
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
	public static final int COD_OFICINA_ADQUIRIENTE_INI = 181;
	public static final int COD_OFICINA_ADQUIRIENTE_FIN = 181 + 4;
	public static final int NUMERO_CEDULA_PERSONA_QUE_PAGA_CON_CHEQUE_INI = 834;
	public static final int NUMERO_CEDULA_PERSONA_QUE_PAGA_CON_CHEQUE_FIN = 834 + 11;
	public static final int NUMERO_FACTURA_INI = 845; 
	public static final int NUMERO_FACTURA_FIN = 845 + 24; 
	public static final int NUMERO_CEL_INI = 894; 
	public static final int NUMERO_CEL_FIN = 894 + 13;
	public static final int POS_INICIAL_CELULAR = 1782; 
	
	public static final String TERMINAL_BANCA_MOVIL= "8592";
	public static final String TERMINAL_BANCA_VIRTUAL= "8593";
	public static final int LON_CELULAR_HEXA= 48;	
	public static final int I_LONGITUD_CON_CELULAR = 1830; 	
	public static final String INITIAL_SPACE = "   ";
		
	public Iso8583Post processMsg (Iso8583Post out, ISCReqInMsg in, TransactionSetting tSetting, String cons, boolean enableMonitor, boolean isNextDay) throws XPostilion {

		String tramaCompletaAscii = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString()));		
		tramaCompletaAscii = INITIAL_SPACE.concat(tramaCompletaAscii);
		try {			
			
			BusinessCalendar objectBusinessCalendar = new BusinessCalendar("DefaultBusinessCalendar");
			Date businessCalendarDate = null;
			String settlementDate = null;
					
			Logger.logLine("Reflected:\n" + in.toString(), enableMonitor);

			StructuredData sd = null;	
			StructuredData sdOriginal = new StructuredData();
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
			
			
		    String keyReverse = "";
			String celular = "";
			String tranType = "";	
			String bin = "008801";
			String secuenciaTS = tramaCompletaAscii.substring(SECUENCIA_TS_INI,SECUENCIA_TS_FIN);
			String seqNr = tramaCompletaAscii.substring(NUM_SEQ_TX_ACTUAL_INI,NUM_SEQ_TX_ACTUAL_FIN);
			String seqNrReverse = tramaCompletaAscii.substring(NUM_SEQ_TX_ORIG_A_REVERSAR_INI,NUM_SEQ_TX_ACTUAL_FIN);					
			String codOficina = tramaCompletaAscii.substring(COD_OFICINA_INI,COD_OFICINA_FIN);	
			String cuentaDebitar = tramaCompletaAscii.substring(NUMERO_CUENTA_DEBITADA_INI,NUMERO_CUENTA_DEBITADA_FIN);
			String p125 = "";
			String p12 = new DateTime().get("HHmmss");
			String p13 = new DateTime().get("MMdd");
			String p37 = "0901".concat(tramaCompletaAscii.substring(COD_OFICINA_INI,COD_OFICINA_FIN))
							   .concat(tramaCompletaAscii.substring(NUM_SEQ_TX_INI,NUM_SEQ_TX_FIN));			
			String key = "0200".concat(p37).concat(p13).concat(p12).concat("00").concat(settlementDate);
			String canalp41 = (codOficina.equals("8592")) ? "00006   " : (codOficina.equals("8593")) ? "00002   " : "";
			String canalp35 = (codOficina.equals(TERMINAL_BANCA_MOVIL)) ? "=291200000001" : (codOficina.equals(TERMINAL_BANCA_VIRTUAL)) ? "=091200000001" : "";
			String p41 =  "00018592" + canalp41;				
			
			String tipoCuentaCreditar = tramaCompletaAscii.substring(TIPO_CUENTA_ACREDITAR_INI, TIPO_CUENTA_ACREDITAR_FIN).equals("0") 
								 ? "10" 
								 : tramaCompletaAscii.substring(TIPO_CUENTA_ACREDITAR_INI, TIPO_CUENTA_ACREDITAR_FIN).equals("1") 
								 ? "20" : "";
			String tipoCuentaDebitar = tramaCompletaAscii.substring(TIPO_CUENTA_DEBITADA_INI, TIPO_CUENTA_DEBITADA_FIN).equals("0") 
								? "10" 
								: tramaCompletaAscii.substring(TIPO_CUENTA_DEBITADA_INI, TIPO_CUENTA_DEBITADA_FIN).equals("1") 
								? "20" : "";	

			tranType = "40";
			
			Logger.logLine("msg in TransferAux:\n" + in.getTotalHexString(), enableMonitor);	
			// Si en la transacccion no esta presente el celular, tratandose de una cel2cel con destino ATH desde BBOG //1784, 1808 // 907 * 2 = 1814, lo vimos en depuracion.
			celular =  in.getTotalHexString().length() >= I_LONGITUD_CON_CELULAR 
								   ? Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(POS_INICIAL_CELULAR, POS_INICIAL_CELULAR + I_LONGITUD_CON_CELULAR)))
								   :"3333333333";
			//FIELD 125
			p125 =Pack.resize(tramaCompletaAscii.substring(NUMERO_FACTURA_INI, NUMERO_FACTURA_FIN)
							.concat(Pack.resize("",  24, ' ', true) )
							.concat("1")
							.concat((Pack.resize(celular,  24, ' ', true))), 150, ' ', true);
			
			// PROCESAMIENTO DE REVERSO
			if(Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("080")
				|| Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("020")) {
				
				sdOriginal = DBHandler.getKeyOriginalTxBySeqNr(seqNrReverse);
				keyReverse = sdOriginal.get("KeyOriginalTx");
				
				if(keyReverse == null) {
					keyReverse = "0000000000";
					sd.put("REV_DECLINED", "TRUE");
				}
				
				out.putField(Iso8583.Bit._090_ORIGINAL_DATA_ELEMENTS, Pack.resize(keyReverse, 42, '0', true));	
				//Valdiar porque las posiciones del hexa no coinciden con la documentaci�n LMM
				out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, "0420".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(248, 268)))).concat("0"+cons.substring(2, 5)));
				out.putPrivField(Iso8583Post.PrivBit._011_ORIGINAL_KEY, keyReverse);
				
				if (Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("020"))
					tranType = "20";	
			//FIN PROCESAMIENTO REVERSO
			} else {
				//PROCESAMIENTO TX FINANCIERA
				out.putField(Iso8583Post.Bit._059_ECHO_DATA,tramaCompletaAscii.substring(NUM_SEQ_TX_ACTUAL_INI, NUM_SEQ_TX_ACTUAL_FIN));			
				//127.2 SWITCHKEY
				out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, key);		
				ISCInterfaceCB.cacheKeyReverseMap.put(seqNr,key);
			}
			
			//FIELD 3 PROCESSING CODE
			out.putField(Iso8583.Bit._003_PROCESSING_CODE, tranType.concat(tipoCuentaDebitar).concat(tipoCuentaCreditar));			
			//FIELD 7 TRANSMISSION DATE N TIME
			out.putField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME, new DateTime(5).get("MMddHHmmss"));
			//FIELD 13 DATE LOCAL
			out.putField(Iso8583.Bit._013_DATE_LOCAL, p13);
			//FIELD 15 DATE SETTLE
			out.putField(Iso8583.Bit._015_DATE_SETTLE, settlementDate);			
			//FIELD 35 TRACK2
			out.putField(Iso8583.Bit._035_TRACK_2_DATA, "0088010000000000000=2912000");
			//FIELD 37 Retrieval Reference Number			
			out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, p37);			
			

			//FIELD 102 DEBIT ACCOUNT
			out.putField(Iso8583.Bit._102_ACCOUNT_ID_1, "00010".concat(tramaCompletaAscii.substring(NUMERO_CUENTA_DEBITADA_INI,NUMERO_CUENTA_DEBITADA_FIN)));
			//FIELD 103 CREDIT ACCOUNT
			out.putField(Iso8583.Bit._103_ACCOUNT_ID_2, "011"
					.concat(tramaCompletaAscii.substring(CODIGO_ENTIDAD_QUE_AUTORIZA_EL_CREDITO_INI,CODIGO_ENTIDAD_QUE_AUTORIZA_EL_CREDITO_FIN)).concat("0")
					.concat(tramaCompletaAscii.substring(NUMERO_CUENTA_ACREDITAR_INI,NUMERO_CUENTA_ACREDITAR_FIN)));
			
			////////// INFORMACI�N QUE DEBE VIAJAR EN EL 127.22 PARA ARMAR MENSAJERIA B24
			
			//127.22 TAG B24_Field_17
			sd.put("B24_Field_17", settlementDate);
			//127.22 TAG B24_Field_35
			sd.put("B24_Field_35", bin.concat(Pack.resize(cuentaDebitar, 18, '0', false)).concat(canalp35));	
			//127.22 TAG B24_Field_41
			sd.put("B24_Field_41", p41);
			//127.22 TAG B24_Field_125
			sd.put("B24_Field_125", p125);
			sd.put("B24_Field_125_numerocelular", celular);
			
			////////// TAGS EXTRACT 
			
			sd.put("VIEW_ROUTER", "V2");			
			sd.put("TRANSACTION_INPUT", "TRANSFERENCIA_CEL2CEL");
			sd.put("TRANSACTION_CNB_TYPE", "CEL2CEL_TRANSFERENCIAS");			
			sd.put("Codigo_FI_Origen", "1019");
			sd.put("Nombre_FI_Origen", "CIC");		
			sd.put("Canal", "01");
			sd.put("Dispositivo", "D");			
			sd.put("FI_DEBITO",(out.isFieldSet(Iso8583.Bit._102_ACCOUNT_ID_1)) ? out.getField(Iso8583.Bit._102_ACCOUNT_ID_1).substring(0, 4) : "0000");
			if (out.isFieldSet(Iso8583.Bit._103_ACCOUNT_ID_2)) {
				sd.put("FI_CREDITO", out.getField(Iso8583.Bit._103_ACCOUNT_ID_2).substring(3, 7));
				sd.put("Clase_Pago", out.getField(Iso8583.Bit._103_ACCOUNT_ID_2).substring(0, 1));
			}
			sd.put("Ent_Adq",p41.substring(0, 4));			
			sd.put("Codigo_Transaccion_Producto",
					(out.getProcessingCode().getFromAccount().equals("10")) ? "05" : "04");
			sd.put("Tipo_de_Cuenta_Debitada",
					(out.getProcessingCode().getFromAccount().equals("10")) ? "AHO" : "CTE");
			sd.put("Codigo_Transaccion", "23");
			sd.put("Nombre_Transaccion", "TRANSD");
			sd.put("CLIENT_CARD_NR_1", bin.concat("0000000000000"));
			sd.put("PRIM_ACCOUNT_NR", Pack.resize(cuentaDebitar, 18, '0', false));
			sd.put("Codigo_de_Red", "1019");
			sd.put("Numero_Terminal", "8592"); //Terminal incorrecta 8201 se actualiza 8592
			sd.put("Identificacion_Canal",(codOficina.equals(TERMINAL_BANCA_VIRTUAL)?"PB":"IT"));
			sd.put("Codigo_Establecimiento", "          ");
			sd.put("SEC_ACCOUNT_NR",
					(out.isFieldSet(Iso8583.Bit._103_ACCOUNT_ID_2))
							? out.getField(Iso8583.Bit._103_ACCOUNT_ID_2).substring(7).trim()
							: "00000000000000000");
			sd.put("SEC_ACCOUNT_TYPE", out.getProcessingCode().getToAccount());
			sd.put("PAN_Tarjeta", bin.concat(Pack.resize(cuentaDebitar.substring(3), 13, '0', false)));
			sd.put("SECUENCIA", secuenciaTS);
			sd.put("Tarjeta_Amparada", bin.concat(Pack.resize(cuentaDebitar.substring(3), 13, '0', false)));
			sd.put("Indicador_AVAL", "1");
			sd.put("Vencimiento", "9912");
			sd.put("Ent_Adq", "0001");
			sd.put("Dispositivo", "0");
			sd.put("Canal", "01");
			sd.put("service_restriction_code", "000");
			sd.put("pos_entry_mode", "000");
			sd.put("Entidad", "0000");
			sd.put("Identificador_Terminal", "0");
			sd.put("Numero_Cedula", tramaCompletaAscii.substring(NUMERO_CEDULA_PERSONA_QUE_PAGA_CON_CHEQUE_INI,NUMERO_CEDULA_PERSONA_QUE_PAGA_CON_CHEQUE_FIN));
			sd.put("Transaccion_Unica", "C201");
			sd.put("Numero_Factura", tramaCompletaAscii.substring(NUMERO_FACTURA_INI,NUMERO_FACTURA_FIN));
			sd.put("NUMERO_CELULAR_CEL2CEL", tramaCompletaAscii.substring(NUMERO_CEL_INI,NUMERO_CEL_FIN));
			sd.put("IN_MSG", in.getTotalHexString());
			///////// FIN TAGS EXTRACT			
			out.putStructuredData(sd);			
		}catch(Exception e) {
			e.toString();
			EventRecorder.recordEvent(
					new Exception("ERROR processMsg: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					"Method :[" + "processMsg" + "]\n" + "processMsg: " + "\n",Utils.getStringMessageException(e) }));
		}			
		return out;
	}
	
	public static final String secuencialesAlearios(int iCuantos)
	{
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i< iCuantos; i++ ) {
			sb.append( "" + (int)( Math.random() * 10 ) );
		}
		return sb.toString();
	}
}
