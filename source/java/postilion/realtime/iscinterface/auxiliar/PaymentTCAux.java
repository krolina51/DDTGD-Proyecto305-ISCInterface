package postilion.realtime.iscinterface.auxiliar;

import java.text.SimpleDateFormat;
import java.util.Date;

import postilion.realtime.genericinterface.eventrecorder.events.TryCatchException;
import postilion.realtime.iscinterface.ISCInterfaceCB;
import postilion.realtime.iscinterface.database.DBHandler;
import postilion.realtime.iscinterface.message.ISCReqInMsg;
import postilion.realtime.iscinterface.util.Client;
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

public class PaymentTCAux {
	public static final int TERMINAL_INI = 14;
	public static final int TERMINAL_FIN = 14 + 4;
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
	public static final int MODALIDAD_PAGO_INI = 144; //0-CargoCuenta, 1-Efectivo, 2-Cheque
	public static final int MODALIDAD_PAGO_FIN = 144 + 1;
	public static final int NATURALEZA_PAGO_INI = 145; //5-TC
	public static final int NATURALEZA_PAGO_FIN = 145 + 1;
	public static final int CLASE_PAGO_INI = 146; //2-Normal
	public static final int CLASE_PAGO_FIN = 146 + 1;
	public static final int DIA_FECHA_APLICACION_DEL_PAGO_INI = 147;
	public static final int DIA_FECHA_APLICACION_DEL_PAGO_FIN = 147 + 2;
	public static final int MES_FECHA_APLICACION_DEL_PAGO_INI = 149;
	public static final int MES_FECHA_APLICACION_DEL_PAGO_FIN = 149 + 2;
	public static final int IDENTIFICADOR_PINPAD_INI = 175;
	public static final int IDENTIFICADOR_PINPAD_FIN = 175 + 2;
	public static final int COD_OFICINA_ADQUIRIENTE_INI = 181;
	public static final int COD_OFICINA_ADQUIRIENTE_FIN = 181 + 4;
	public static final int NUM_SEQ_TX_ACTUAL_INI = 206;
	public static final int NUM_SEQ_TX_ACTUAL_FIN = 206 + 4;
	public static final int NUM_SEQ_TX_ORIG_A_REVERSAR_INI = 210;
	public static final int NUM_SEQ_TX_ORIG_A_REVERSAR_FIN = 210 + 4;
	public static final int CODIGO_DANE_OFICINA_ADQUIRIENTE_INI = 214;
    public static final int CODIGO_DANE_OFICINA_ADQUIRIENTE_FIN = 214 + 5;
	public static final int NOMBRE_OFICINA_ADQUIRIENTE_INI = 219;
	public static final int NOMBRE_OFICINA_ADQUIRIENTE_FIN = 219 + 18;
	public static final int CANTIDAD_CHEQUES_INI = 832;
	public static final int CANTIDAD_CHEQUES_FIN = 832 + 2;
	
	public static final String INITIAL_SPACE = "   ";
	

	public Iso8583Post processMsg(Iso8583Post out, ISCReqInMsg in, TransactionSetting tSetting, String cons,boolean enableMonitor, boolean isNextDay) throws XPostilion {
		
		String tramaCompletaAscii = INITIAL_SPACE + Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString()));

		try {
			
			BusinessCalendar objectBusinessCalendar = new BusinessCalendar("DefaultBusinessCalendar");
			Date businessCalendarDate = null;
			String settlementDate = null;

			Logger.logLine("Reflected:\n" + in.toString(), enableMonitor);

			StructuredData sd = null;
			StructuredData sdOriginal = new StructuredData();

			if (out.getStructuredData() != null) {
				sd = out.getStructuredData();
			} else {
				sd = new StructuredData();
			}
		
			sd.put("TXINNEXTDAY", isNextDay ? "TRUE": "FALSE");
			if(in.getTotalHexString().substring(46,52).matches("^((F0F4F0)|(F0F5F0)|(F0F6F0)|(F0F7F0))")
					|| Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._10_H_NEXTDAY_IND)).equals("1")) {
				
				if(isNextDay) {
					businessCalendarDate = objectBusinessCalendar.getCurrentBusinessDate();
					settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
				}else {
					businessCalendarDate = objectBusinessCalendar.getNextBusinessDate();
					settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
				}
				
			}else {
				businessCalendarDate = objectBusinessCalendar.getCurrentBusinessDate();
				settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
			}
					
			String p43 = "";
			String tranType = null;
			String fromAccount = null;
			String toAccount = null;
			String p12 = new DateTime().get("HHmmss");
			String p13 = new DateTime().get("MMdd");
			String codigoOficina = tramaCompletaAscii.substring(COD_OFICINA_INI,COD_OFICINA_FIN);
			String p37 = "0901".concat(codigoOficina).concat(tramaCompletaAscii.substring(NUM_SEQ_TX_INI,NUM_SEQ_TX_FIN));
			String key = "0200".concat(p37).concat(p13).concat(p12).concat("00").concat(settlementDate);	
			String seqNr = tramaCompletaAscii.substring(NUM_SEQ_TX_ACTUAL_INI, NUM_SEQ_TX_ACTUAL_FIN);
			String seqNrReverse = tramaCompletaAscii.substring(NUM_SEQ_TX_ORIG_A_REVERSAR_INI, NUM_SEQ_TX_ORIG_A_REVERSAR_FIN);
			String keyReverse = null;
			String terminal = tramaCompletaAscii.substring(TERMINAL_INI, TERMINAL_FIN);
			String cuentaCreditar = tramaCompletaAscii.substring(NUMERO_CUENTA_ACREDITAR_INI, NUMERO_CUENTA_ACREDITAR_FIN);
			String cuentaDebitar = tramaCompletaAscii.substring(NUMERO_CUENTA_DEBITADA_INI, NUMERO_CUENTA_DEBITADA_FIN);
			String identificadorPinpad = tramaCompletaAscii.substring(IDENTIFICADOR_PINPAD_INI, IDENTIFICADOR_PINPAD_FIN);  
			String identificacionCanal = "  ";
			String modalidadPago = tramaCompletaAscii.substring(MODALIDAD_PAGO_INI, MODALIDAD_PAGO_FIN);
			String naturalezaPago = tramaCompletaAscii.substring(NATURALEZA_PAGO_INI,NATURALEZA_PAGO_FIN);
			String clasePagoCredito = tramaCompletaAscii.substring(CLASE_PAGO_INI,CLASE_PAGO_FIN);
			String secuenciaTS = tramaCompletaAscii.substring(SECUENCIA_TS_INI,SECUENCIA_TS_FIN);
			String entidadAutorizadora = tramaCompletaAscii.substring(CODIGO_ENTIDAD_QUE_AUTORIZA_EL_CREDITO_INI, CODIGO_ENTIDAD_QUE_AUTORIZA_EL_CREDITO_FIN);
			String p41 = entidadAutorizadora.concat(tramaCompletaAscii.substring(COD_OFICINA_ADQUIRIENTE_INI, COD_OFICINA_ADQUIRIENTE_FIN)).concat("00003   ");
			String codigoOfiAdqui = tramaCompletaAscii.substring(COD_OFICINA_ADQUIRIENTE_INI, COD_OFICINA_ADQUIRIENTE_FIN);
			String codigoDaneOfiAdqui = tramaCompletaAscii.substring(CODIGO_DANE_OFICINA_ADQUIRIENTE_INI, CODIGO_DANE_OFICINA_ADQUIRIENTE_FIN);
			String nombreOficinaAdqui = tramaCompletaAscii.substring(NOMBRE_OFICINA_ADQUIRIENTE_INI, NOMBRE_OFICINA_ADQUIRIENTE_FIN);
			String tipoCuentaDebito = tramaCompletaAscii.substring(TIPO_CUENTA_DEBITADA_INI, TIPO_CUENTA_DEBITADA_FIN);
			String FI_Tarjeta = "";
			String cantidadCheques = tramaCompletaAscii.substring(CANTIDAD_CHEQUES_INI, CANTIDAD_CHEQUES_FIN);
			
			String binTarjeta = "000000000000000000";
			String p100 = "";
			String p125 = "                                                                                          ";
			// NATURALEZA DE LA TRANSACCION ES TARJETA DE CREDITO
			toAccount = "30";
			
			//Armamos Case para determinar Nombre de Terminal segun Origen de la transaccion (Primer digito de codigo de Oficina)
            String p43_pos_6_a_22 = "     OFICINA BBOG";	
			
        	//Validacion Titularidad, trae informacion nombre e identificacion del tarjetahabiente solo para tarjeta Bco Bta
			Client udpClientValidation = new Client(ISCInterfaceCB.ipServerValidation, ISCInterfaceCB.portServerValidation);				
			String msgFromValidationTC = udpClientValidation.sendMsgForValidationTitular(cuentaCreditar, enableMonitor);


			switch (codigoOficina.substring(0,1)) {
			case "3":
				p43_pos_6_a_22 = "  FIDUCOMERCIO   ";
				break;
			case "5":
				p43_pos_6_a_22 = "     INTN BBS BTA";
				break;
			case "6":
				p43_pos_6_a_22 = "    BCAMOVIL  BB ";
				break;
			case "7":
				p43_pos_6_a_22 = "  890AGILIZADORBB";
				break;
			case "8":
				p43_pos_6_a_22 = "     INTERNET BTA";
				break;
			case "9":
				p43_pos_6_a_22 = "     IVR BOGOTA  ";
				break;
			default:
				p43_pos_6_a_22 = "     OFICINA BBOG";
				break;
			}
			
			
			
			String p22 = "010";
			if (codigoOficina.substring(0, 1).equals("4") &&
			    identificadorPinpad.equals("PP"))
			{
				p22 = "051";
			}
			
			// Inicio Logica para tarjetas BBOG (FirstData)
			if (entidadAutorizadora.equals("0001")) {	
				// Armado P41 para Firsdata
				p100 = "100"; //Enruta a Firstdata
				switch (codigoOficina.substring(0, 1)) {
				case "2":
					p41 = codigoOficina.substring(1, 2).equals("5") && p22.equals("051") 
					?  "0001000000008   "	// canal redes externas
					: codigoOficina.substring(1, 2).equals("1")
					?  "0001930000002   "	// canal CNB Moviles
					:  "0000000000000   ";	
					binTarjeta = "002501000000000000";					
				break;
				case "4":	// canal Oficinas
					p41 = "0001".concat(codigoOfiAdqui).concat("00003   ");
					identificacionCanal = "OF";
					binTarjeta = "007701000000000000";			
					if (modalidadPago.equals("0")) //Cargo Cuenta		
						p43 = codigoOfiAdqui.concat(p43_pos_6_a_22).concat(codigoDaneOfiAdqui).concat("              ");
					else //Efectivo o Cheque
						p43 = codigoOfiAdqui.concat(nombreOficinaAdqui.substring(0,17)).concat(codigoDaneOfiAdqui).concat("              ");
					break;
				case "5":	// canal BBS
					p41 = "0001820500002   ";
					identificacionCanal = "BS";
					binTarjeta = "005501000000000000";
					break;
				case "6":	// canal WAP
					p41 = "0001829800002   ";
					identificacionCanal = "WT";
					binTarjeta = "006501000000000000";
					break;
				case "7":	// canal Agilizador
					p41 = "0001829900002   ";
					identificacionCanal = "AG";
					binTarjeta = "004701000000000000";
					break;
				case "8":		
					p43 = "BOG  ".concat(p43_pos_6_a_22).concat("             BOCCO");
					identificacionCanal = "IT";
					if(codigoOficina.equals("8332")){	// canal PPA
						p41 = "0001833200002   ";
					}
					else if(codigoOficina.equals("8300") ||  // Canal Corporate 
						   codigoOficina.equals("8600")){	 // canal ICBS
							binTarjeta = cuentaCreditar;
							FI_Tarjeta = cuentaCreditar.substring(0,6);
							identificacionCanal = "85";
						}
						else {
							p41 = "0001820100002   ";	// canal internet
							binTarjeta = "008801000000000000";
							}
					
					break;
				case "9":	// canal IVR
					p41 = "0001920100005   ";
					identificacionCanal = "IV";
					binTarjeta = "009901000000000000";
					p43 = "BOG  ".concat(p43_pos_6_a_22).concat("             BOCCO");
					break;
				default: 
					p41 = "0001829700003   ";
					break;
				}
			

			
				tranType = "20";
				fromAccount = "00";
				
				//Logica Tokens First Data Inicio -----------------------------------------
				//Token 04
				String token_04 = "! 0400020  00000000000Y     Y0";
				String token_BM = "";
				//Token BM
				switch (modalidadPago)
				{
					case "0": //Cargo cuenta
						if (tipoCuentaDebito.equals("1"))
							token_BM = "! BM00036 Q401502030      00000000000000000000";
						else
							token_BM = "! BM00036 Q401501030      00000000000000000000";							
					break;
					case "1": //Efectivo
						token_BM = "! BM00036 Q406500130      00000000000000000000";					
					break;
					case "2": //Cheque
						token_BM = "! BM00036 Q406500230      00000000000000000000";
					break;
				}		
				
				//Token C0
				String token_C0 = "! C000026                   2";
				String franquicia = " ";
				if(cuentaCreditar.substring(0,1).equals("5")){
					franquicia = "M";
				}
				String fld_present = "0";
				if(identificadorPinpad.equals("PP")){ //Transacción presente
					fld_present = "1"; 
				}
				
				token_C0 = token_C0.concat(franquicia).concat(" ").concat(fld_present).concat("    ");
				//Token C4
				String crdhldr_present_ind = "0";
				String crd_present_ind = "0";
				if(codigoOficina.substring(0,1).equals("4") || //Oficina
				   codigoOficina.substring(0,1).equals("6") || //Aval Net
				   codigoOficina.substring(0,1).equals("8") || //Internet
				   codigoOficina.substring(0,1).equals("9")){  //IVR
					crdhldr_present_ind = "5";
					crd_present_ind = "1";
				}
				String token_C4 = "! C400012 1 2".concat(crdhldr_present_ind).concat(crd_present_ind).concat("00  600");
				//Token Q4
				String moneda = "170"; //Moneda Pesos
				if(codigoOficina.substring(0,1).equals("4") && codigoOficina.substring(1,2).equals("1"))
					 moneda = "840"; //Moneda Dolares
				String token_Q4 = "! Q400122 ".concat("000000000000000000000000000000000000000000000000000000000000000000000000000000000000000").concat(moneda).concat("00").concat(settlementDate).concat("00000000000000000000000000");
					
				//Logica Tokens First Data Fin -----------------------------------------
	
				// 127.22 TAG B24_Field_18
				sd.put("B24_Field_18", "6010");
				// 127.22 TAG B24_Field_35
				sd.put("B24_Field_35", cuentaCreditar.concat("=99120000000000000000"));
				// 127.22 TAG B24_Field_48
				sd.put("B24_Field_48", "1                  00000000");
				// 127.22 TAG B24_Field_60
				sd.put("B24_Field_60", "P801P80100000000");
				// 127.22 TAG B24_Field_61
				sd.put("B24_Field_61", "0001000000000000000");
				// 127.22 TAG B24_Field_63
				//sd.put("B24_Field_63",
				//		"& 0000600278! 0400020  00000000000Y     Y0! BM00036     500130      00000000000000000000! C000026                   2  0    ! C400012 1 25100  600! Q400122 23000000000000000000000000000000000000000000000000000000000000000000000000000000000000017000041500000000000000000000000000");
				sd.put("B24_Field_63",
					"& 0000600278".concat(token_04).concat(token_BM).concat(token_C0).concat(token_C4).concat(token_Q4));  
				// 127.22 TAG B24_Field_100
				sd.put("B24_Field_100", "10000000001");
				// 127.22 TAG B24_Field_121
				sd.put("B24_Field_121", "00000000000000000000");
				// 127.22 TAG B24_Field_124
				sd.put("B24_Field_124", "000000000");
				// 127.22 TAG B24_Field_125
				sd.put("B24_Field_125", "000000000000");
				// 127.22 TAG B24_Field_126
				sd.put("B24_Field_126", "00000000000000000000000000000000000000");
				// 127.22 TAG FI_Tarjeta
				sd.put("FI_Tarjeta", FI_Tarjeta);
				
				// 127.22 TAG ERROR
				if (msgFromValidationTC.equals("NO"))
				   sd.put("ERROR", "TARJETA NO EXISTE");
				
				
			//FIN LOGICA FIRSDATA	

			} else {	// Lógica para tarjetas AVAL (ATH)
				String bin = "008801";
				p100 = "70"; // Enrutamiento a ATH conexion internet
				switch (codigoOficina.substring(0, 1)) {
					case "2":						
						p41 = codigoOficina.substring(1,2).equals("5") && p22.equals("051") 
							? "0001000000008   "  // Canal redes externas
							: "0001930000002   "; // Canal CNB Moviles
						p43 = "BOG  ".concat(p43_pos_6_a_22).concat("             BOCCO");	
						binTarjeta = "002501000000000000";
					break;
				case "3":
					p41 = "0001829600002   ";
					p43 = "BOG  ".concat(p43_pos_6_a_22).concat("             BOCCO");
					break;
				case "4":	// canal Oficinas					
					p41 = "0001".concat(codigoOfiAdqui).concat("00003   ");
					p100 = "50"; // Enrutamiento a ATH conexion oficinas
					if (modalidadPago.equals("0")) //Cargo Cuenta		
						p43 = codigoOfiAdqui.concat(p43_pos_6_a_22).concat(codigoDaneOfiAdqui).concat("              ");
					else //Efectivo o Cheque
						p43 = codigoOfiAdqui.concat(nombreOficinaAdqui.substring(0,17)).concat(codigoDaneOfiAdqui).concat("              ");
									
					//Definir bin para canal oficina
					if (entidadAutorizadora.equals("0052")){ //Bco AvVillas
						bin = "000052";
						binTarjeta = "007752000000000000";
					}
					else if (entidadAutorizadora.equals("0002")){ //Bco Popular
						bin = "007702";
						binTarjeta = "007702000000000000";
					}
					else if (entidadAutorizadora.equals("0023")){ //Bco Occidente
						bin = "007723";
						binTarjeta = "007723000000000000";						
					}
					
					identificacionCanal = "OF";
					break;
				case "5":
					if (codigoOficina.substring(1,2).equals("3")) {
						p41 = "0001000000008   "; // canal Corporate
						p43_pos_6_a_22 = "     INTN COR BTA";
					}
					else if (codigoOficina.substring(1,2).equals("6")) {
						p41 = "0001930000002   "; // canal Business
						p43_pos_6_a_22 = "     INTN BSE BTA";
					}
					else {
						p41 = "0001820500002   "; // canal BBS
						p43_pos_6_a_22 = "     INTN BBS BTA";
					}		
					p43 = "BOG  ".concat(p43_pos_6_a_22).concat("             BOCCO");	
					binTarjeta = "005501000000000000";
					identificacionCanal = "BS";
					break;
				case "6":	// canal WAP
					p41 = "0001829800002   ";
					p43 = "BOG  ".concat(p43_pos_6_a_22).concat("             BOCCO");
					binTarjeta = "006501000000000000";
					identificacionCanal = "WT";
					break;
				case "7":	// canal Agilizador
					p41 = "0001829900002   ";
					p43 = "BOG  ".concat(p43_pos_6_a_22).concat("             BOCCO");
					binTarjeta = "004701000000000000";
					identificacionCanal = "AG";
					break;
				case "8":
					p41 = "0001820100002   ";	// canal internet	
					p43 = "BOG  ".concat(p43_pos_6_a_22).concat("             BOCCO");
					binTarjeta = "008801000000000000";
					identificacionCanal = "IT";
					break;
				case "9":	// canal IVR
					p41 = "0001920100005   ";
					p43 = "BOG  ".concat(p43_pos_6_a_22).concat("             BOCCO");
					bin = "009901";
					binTarjeta = "009901000000000000";
					identificacionCanal = "IV";
					break;
				default: 
					p41 = "0001829700003   ";
					p100 = "70";
					p43 = "BOG  ".concat(p43_pos_6_a_22).concat("             BOCCO");
					break;
				}
				
				tranType = "50";
				fromAccount = "";
			
				if (tramaCompletaAscii.substring(TIPO_CUENTA_DEBITADA_INI, TIPO_CUENTA_DEBITADA_FIN).equals("0")) {
					fromAccount = "10";	// cuenta de ahorros
					sd.put("Codigo_Transaccion_Producto", "05");
					if (modalidadPago.equals("1") || modalidadPago.equals("2")) //Si es Efectivo o Cheque
					sd.put("Tipo_de_Cuenta_Debitada", "CRE");
					else
					sd.put("Tipo_de_Cuenta_Debitada", "AHO");
				} else if(tramaCompletaAscii.substring(TIPO_CUENTA_DEBITADA_INI, TIPO_CUENTA_DEBITADA_FIN).equals("1")){
					fromAccount = "20";	// cuenta corriente
					sd.put("Codigo_Transaccion_Producto", "04");
				sd.put("Tipo_de_Cuenta_Debitada", "CTE");
				}
				
				//} else if(modalidadPago.equals("1")){
				//	fromAccount = "01";	// efectivo
				//} else if(modalidadPago.equals("2")){
				//	fromAccount = "02";	// cheque
				//}

				if (modalidadPago.equals("1")) //Si es Efectivo
					fromAccount = "01";
				else if (modalidadPago.equals("2")) //Si es Cheque
					fromAccount = "02";
				

				// TRACK2 Field 98
				out.putField(Iso8583.Bit._098_PAYEE, "0054150070650000000000000");

				// CAMPO 102 DEBIT ACCOUNT
				out.putField(Iso8583.Bit._102_ACCOUNT_ID_1,"00010".concat(cuentaDebitar));
				if (codigoOficina.substring(0, 1).equals("4"))
				out.putField(Iso8583.Bit._102_ACCOUNT_ID_1,"00000".concat(cuentaDebitar));	


				// CAMPO 103 CREDIT ACCOUNT
				out.putField(Iso8583.Bit._103_ACCOUNT_ID_2, "011".concat(entidadAutorizadora).concat("0").concat(cuentaCreditar));
				if (codigoOficina.equals("5600")) // Si es canal ICBS
				out.putField(Iso8583.Bit._103_ACCOUNT_ID_2, "611".concat(entidadAutorizadora).concat("0").concat(cuentaCreditar));
				
				// 127.22 TAG B24_Field_35
				sd.put("B24_Field_35", bin.concat(Pack.resize(cuentaDebitar, 18, '0', false)).concat("D            "));
				if (codigoOficina.equals("5600")) // Si es canal ICBS
				sd.put("B24_Field_35", bin.concat(Pack.resize(cuentaDebitar, 18, '0', false)).concat("D1           "));
				if (codigoOficina.substring(0,1).equals("4")) // Si es canal Oficina
				sd.put("B24_Field_35", bin.concat(Pack.resize(cuentaCreditar, 18, '0', false)).concat("D            "));
				
				// 127.22 TAG B24_Field_52
				sd.put("B24_Field_52", "FFFFFFFFFFFFFFFF");
				// 127.22 TAG B24_Field_60
				sd.put("B24_Field_60", "0901BBOG+000");
				// 127.22 TAG B24_Field_104
				sd.put("B24_Field_104", "000000000000000000");		
				// 127.22 TAG B24_Field_125
				if (codigoOficina.substring(0,1).equals("4")) //Si es oficina
					p125 = cantidadCheques.concat ("                              00000000000");
				
				
				if (!codigoOficina.equals("5600") && !codigoOficina.equals("9000") ) // Diferente de ICBS y IVR
				sd.put("B24_Field_125", p125);
			
			}

			// PROCESAMIENTO DE REVERSO
			if (Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("080") //Reverso
					|| Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("020")) { //Anulacion

				String key420 = "0420".concat(p37).concat(p13).concat(p12).concat("00").concat(settlementDate);
				String keyAnulacion = "0200".concat(p37).concat(p13).concat(p12).concat("00").concat(settlementDate);
				
				Logger.logLine("sdOriginal:\n" + sdOriginal, enableMonitor);
				keyReverse = sdOriginal.get("KeyOriginalTx");
				if(keyReverse == null) {
					keyReverse = "0000000000";
					sd.put("REV_DECLINED", "TRUE");
				} else {
					out.putField(Iso8583.Bit._090_ORIGINAL_DATA_ELEMENTS, Pack.resize(keyReverse, 42, '0', true));
					
					out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, key420);
					//out.putPrivField(Iso8583Post.PrivBit._011_ORIGINAL_KEY, keyReverse);
					sd.put("B24_Field_95", "000000000000000000000000000000000000000000");
					sd.put("KEY_REVERSE", keyReverse);
					sd.put("B24_Field_90", keyReverse+"0000000000");
					//sd.put("B24_Field_37", keyReverse.substring(4,16));
					
					if (Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("020")) {
						tranType = "20";
						sd.put("ANULACION", "TRUE");
						sd.put("B24_Field_15", settlementDate);
						sd.put("B24_Field_38", sdOriginal.get("Autorizacion_Original"));
						sd.put("KeyOriginalTx", keyReverse);
						sd.put("B24_Field_54", "000".concat(sdOriginal.get("Monto_Original"))
								.concat("000000000000000000")
								.concat(sdOriginal.get("Monto_Original")));
						out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, keyAnulacion);
					}
				}


				// PROCESAMIENTO TX FINANCIERA
			} else {
				out.putField(Iso8583Post.Bit._059_ECHO_DATA, Transform
						.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(406, 414))));
				// 127.2 SWITCHKEY
				out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, key);
				ISCInterfaceCB.cacheKeyReverseMap.put(seqNr, key);
			}

			// Field 3
			out.putField(Iso8583.Bit._003_PROCESSING_CODE, tranType.concat(fromAccount).concat(toAccount));

//			//CAMPO 7 TRANSMISSION DATE N TIME
			out.putField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME, new DateTime(5).get("MMddHHmmss"));

			// CAMPO 37 Retrieval Reference Number
			out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, p37);

			// CAMPO 12 TIME LOCAL
			out.putField(Iso8583.Bit._012_TIME_LOCAL, p12);

			// CAMPO 13 TRANSMISSION DATE N TIME
			out.putField(Iso8583.Bit._013_DATE_LOCAL, p13);

			out.putField(Iso8583.Bit._015_DATE_SETTLE, settlementDate);

			// TRACK2 Field 22
			out.putField(Iso8583.Bit._022_POS_ENTRY_MODE, p22);

			// TRACK2 Field 35
			out.putField(Iso8583.Bit._035_TRACK_2_DATA, "0088010000000000000=9912000");
			
			// TRACK2 Field 43
			out.putField(Iso8583.Bit._043_CARD_ACCEPTOR_NAME_LOC, p43);
			
			// CAMPOO 100 ROUTING 
			out.putField(Iso8583.Bit._100_RECEIVING_INST_ID_CODE, p100);
			
			// 127.22 TAG B24_Field_17
			sd.put("B24_Field_17", settlementDate);
			// 127.22 TAG B24_Field_41
			sd.put("B24_Field_41", p41);
			// 127.22 TAG B24_Field_125
			//sd.put("B24_Field_125", p125);
			//127.22 TAG MSG_HEXA
			sd.put("IN_MSG", in.getTotalHexString());
			//127.22 TAG Entidad Autorizadora
			sd.put("ENT_AUT", entidadAutorizadora);
			//127.22 TAG Entidad Autorizadora
			sd.put("P100", p100);
			
			////////// TAGS EXTRACT 
			
			sd.put("VIEW_ROUTER", "V2");
			sd.put("Codigo_FI_Origen", "1019");
			sd.put("Nombre_FI_Origen", "CIC");		
			
			
			sd.put("Ind_4xmil", "1");
			sd.put("Tarjeta_Amparada", cuentaCreditar);
			
			if (entidadAutorizadora.equals("0001"))  //Si es Banco de Bogota
			{	
				sd.put("Codigo_Transaccion_Producto", "02");
				sd.put("Tipo_de_Cuenta_Debitada", "CRE");
				sd.put("BIN_Cuenta", cuentaCreditar.substring(0,6));
				sd.put("PRIM_ACCOUNT_NR", Pack.resize(cuentaCreditar.substring(6,16), 18, '0', false));
				sd.put("SEC_ACCOUNT_NR_PAGOTC",Pack.resize(cuentaDebitar, 18, '0', false));
				sd.put("PAN_Tarjeta", Pack.resize(cuentaCreditar, 19, ' ', true));	
				if (modalidadPago.equals("0")) //Si es Cargo cuenta
				{	
				sd.put("Codigo_Transaccion", "29");
				sd.put("Nombre_Transaccion", "ONESID");
				sd.put("FI_CREDITO", "0001");
				sd.put("FI_DEBITO", "0001");
					if (codigoOficina.substring(0, 1).equals("4") ) //Canal Oficina
					sd.put("Ind_4xmil", "0");
				}
				else //Efectivo o Cheque
				{	
				sd.put("Codigo_Transaccion", "01");
				sd.put("Nombre_Transaccion", "DEPOSI");
				sd.put("FI_CREDITO", "0000");
				sd.put("FI_DEBITO", "0000");
				sd.put("Ind_4xmil", "1");
				sd.put("SEC_ACCOUNT_TYPE", "   ");
				sd.put("Ofi_Adqui", codigoOfiAdqui);
				}
			}
			else  //Si es Aval
			{
				sd.put("Codigo_Transaccion", "29");
				sd.put("Nombre_Transaccion", "ONESID");
				sd.put("BIN_Cuenta", "000000");
				sd.put("PRIM_ACCOUNT_NR", cuentaDebitar);
				sd.put("FI_DEBITO", "0001");
				sd.put("FI_CREDITO", entidadAutorizadora);
				sd.put("SEC_ACCOUNT_NR_PAGOTC", cuentaCreditar);
				sd.put("SEC_ACCOUNT_TYPE", "CRE");
				sd.put("PAN_Tarjeta", binTarjeta.substring(0,16).concat("   "));
				sd.put("Vencimiento", "0000");
				sd.put("Tarjeta_Amparada", "0000000000000000");
				sd.put("Numero_Cheques", cantidadCheques);
				if (codigoOficina.substring(0, 1).equals("4")) //Canal Oficina
					sd.put("Codigo_Transaccion_Producto", "02");
					if (modalidadPago.equals("0")) //Si es Cargo cuenta
					{		
					sd.put("Codigo_Transaccion", "29");
					sd.put("Nombre_Transaccion", "ONESID");
					}
					else //Efectivo o Cheque
					{
					sd.put("Codigo_Transaccion", "01");
					sd.put("Nombre_Transaccion", "DEPOSI");
					sd.put("PRIM_ACCOUNT_NR", cuentaCreditar);
					sd.put("FI_CREDITO", "0000"); 
					sd.put("SEC_ACCOUNT_NR_PAGOTC", "000000000000000000");
					sd.put("FI_DEBITO", "0000");
					sd.put("SEC_ACCOUNT_TYPE", "   ");
					sd.put("Ofi_Adqui", codigoOfiAdqui);
					}
			}			
			
			
			sd.put("CLIENT_CARD_NR_1", binTarjeta);
			sd.put("Codigo_de_Red", "1019");
			
			if (codigoOficina.substring(0, 1).equals("4")){ //Canal Oficina
				sd.put("Numero_Terminal", codigoOfiAdqui);
				sd.put("Nombre_Establecimiento_QR", codigoOfiAdqui.concat(nombreOficinaAdqui));
			}
			else {  //Otros canales 
				 sd.put("Numero_Terminal",codigoOficina);
				 sd.put("Nombre_Establecimiento_QR", "                    ");
			}
		
			sd.put("Identificacion_Canal", identificacionCanal);
			sd.put("Codigo_Establecimiento", "          ");
			
			if (!msgFromValidationTC.equals("NO") && entidadAutorizadora.equals("0001"))
			{
			sd.put("CUSTOMER_NAME",Pack.resize(msgFromValidationTC.substring(4,25), 28,' ', true));	
			sd.put("ID_CLIENT",Pack.resize(msgFromValidationTC.substring(25,33), 13, '0', false));
			}
			else
			{
			sd.put("CUSTOMER_NAME", "                            ");	
			sd.put("ID_CLIENT", "0000000000000");
			}	
			
			sd.put("TITULAR_TC",msgFromValidationTC);
			sd.put("TRANSACTION_INPUT", "PAGO_TC_CANALVIRTUAL");
			
			sd.put("Indicador_AVAL", "1");	
			sd.put("SECUENCIA_REQ", secuenciaTS);
			
			sd.put("Mod_Credito", naturalezaPago);
			sd.put("Clase_Pago", clasePagoCredito);
			sd.put("Ent_Adq", "0001");
			sd.put("Dispositivo", "0");
			sd.put("Canal", "01");
			sd.put("pos_entry_mode", "000");
			sd.put("service_restriction_code", "000");
			sd.put("Entidad", "0000");
			sd.put("Identificador_Terminal", "0");
			sd.put("Indicador_Efectivo_Cheque", modalidadPago);
							
			out.putStructuredData(sd);

		} catch (Exception e) {
			e.toString();
			EventRecorder.recordEvent(new Exception("ERROR processMsg: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					"Method :[" + "processMsg" + "]\n" + "processMsg: " + "\n", Utils.getStringMessageException(e) }));
		}

		return out;
	}

}
