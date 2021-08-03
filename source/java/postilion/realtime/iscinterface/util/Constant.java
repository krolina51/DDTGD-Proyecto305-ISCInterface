package postilion.realtime.iscinterface.util;

public class Constant {

	private Constant() {
	    throw new IllegalStateException();
	  }

	public static class AllowTrans {
		
		private AllowTrans() {
		    throw new IllegalStateException();
		  }
		
		public static final String TRAN_RETIRO_AHORRO = "0200_011000_1";
		public static final String TRAN_RETIRO_CORRIENTE = "0200_012000_1";
		public static final String TRAN_RETIRO_CB = "0200_501043_1";
		public static final String TRAN_COMPRA_AHORRO = "0200_001000_1";
		public static final String TRAN_COMPRA_CORRIENTE = "0200_002000_1";
		public static final String TRAN_COSULTA_COSTO = "0200_320000_1";
		
	}
	
	public static class B24Fields {
		
		private B24Fields() {
		    throw new IllegalStateException();
		  }
		
		public static final String B24_F_4 = "B24_Field_4";
		public static final String B24_F_40 = "B24_Field_40";
		public static final String B24_F_41 = "B24_Field_41";
		public static final String B24_F_44 = "B24_Field_44";
		public static final String B24_F_48 = "B24_Field_48";
		public static final String B24_F_54 = "B24_Field_54";
		public static final String B24_F_62 = "B24_Field_62";
		public static final String B24_F_63 = "B24_Field_63";
		public static final String B24_F_102 = "B24_Field_102";
		public static final String B24_F_103 = "B24_Field_103";
		public static final String B24_F_126 = "B24_Field_126";
		public static final String B24_TOKEN_CONTROL = "01";
		public static final String B24_SOURCE = "ATH";
		
	}
	
	public static class TagNames {
		
		private TagNames() {
		    throw new IllegalStateException();
		  }
		
		public static final String COMISION = "COMISION";
		public static final String SALDO_DISPONIBLE = "SALDO_DISPONIBLE";
		public static final String SALDO_TOTAL = "SALDO_TOTAL";
		public static final String SALDOS = "SALDOS";
		public static final String AV_SEGURO = "AV_SEGURO";
		public static final String I2_RSP_TIME = "I2_RSP_TIME";
		public static final String I2_REQ_TIME = "I2_REQ_TIME";
		public static final String TRANSACTION_AMOUNT = "TRANSACTION_AMOUNT";
		public static final String DONATION_AMOUNT = "DONATION_AMOUNT";
		public static final String SECURE_AMOUNT = "SECURE_AMOUNT";
		public static final String SETTER_AMOUNT = "SETTER_AMOUNT";
		public static final String TOKEN_CONTROL_B24 = "_TOKEN_CONTROL_B24";
		public static final String SOURCE = "SOURCE";
		public static final String SEQ_TERMINAL = "SEQ_TERMINAL";
		public static final String RETRIEVED_ACCOUNT = "RETRIEVED_ACCOUNT";
		public static final String REFERENCE_KEY = "REFERENCE_KEY";
		public static final String ORIGINAL_MSG = "ORIGINAL_MSG";
		public static final String CUSTOMER_ID = "CUSTOMER_ID";
		public static final String REVERSE_AUTH_MODE = "REVERSE_AUTH_MODE";
		public static final String P_CODE = "P_CODE";
		public static final String PA_FORZA = "PA_FORZA";
		
	}
	
	public static class InfoMsg {
		
		private InfoMsg() {
		    throw new IllegalStateException();
		  }
		
		public static final String MSG_NOT_REGISTRED = "Tipo de mensaje no permitido, la transacción de bebe se agregada [ISCInterface][mapISCFields2ISOFields]";
	}
	
	public static class Misce {
		
		private Misce() {
		    throw new IllegalStateException();
		  }
		
		public static final char CHAR_ONE_SPACE = ' ';
		public static final char CHAR_ONE_ZERO = '0';
		public static final String STR_ERROR = "ERROR";
		public static final String STR_PIPE = "|";
		public static final String STR_EMPTY = "";
		public static final String STR_ONE_SPACE = " ";
		public static final String STR_POINT = ".";
		public static final String STR_COMA = ",";
		public static final String STR_ONE_ZERO = "0";
		public static final String STR_TWO_ZEROS = "00";
		public static final String STR_THREE_ZEROS = "000";
		public static final String STR_FOUR_ZEROS = "0000";
		public static final String STR_SIX_ZEROS = "000000";
		public static final String STR_EIGHT_ZEROS = "00000000";
		public static final String STR_TWELVE_ZEROS = "000000000000";
		public static final String STR_SIXTEEN_ZEROS = "0000000000000000";
		public static final String STR_TWENTYFOUR_ZEROS = "000000000000000000000000";
		public static final String STR_THIRTY_ZEROS = "000000000000000000000000000000";
		public static final String STR_ONE_ONE = "1";
		public static final String F_820_INSTITUTION_ID = "00000111111";
		public static final String F_820_NETWORK_INFO_CODE = "301";
		public static final String ATTF_ID = "1";
		public static final String ATTC_ID = "2";
		public static final String ATTD_ID = "3";
		public static final String ATTF = "ATTF";
		public static final String ATTC = "ATTC";
		public static final String ATTD = "ATTD";
		public static final String ATPG = "ATPG";
		public static final String ATPA = "ATPA";
	}
}
