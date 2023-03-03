package postilion.realtime.iscinterface.database;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import monitor.core.dto.MonitorSnapShot;
import monitor.core.dto.Observable;
import monitor.core.dto.SnapShotSeverity;
import postilion.realtime.genericinterface.eventrecorder.events.SQLExceptionEvent;
import postilion.realtime.genericinterface.eventrecorder.events.TryCatchException;
import postilion.realtime.iscinterface.crypto.PinPad;
import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.iscinterface.util.Utils;
import postilion.realtime.sdk.eventrecorder.EventRecorder;
import postilion.realtime.sdk.ipc.SecurityManager;
import postilion.realtime.sdk.jdbc.JdbcManager;
import postilion.realtime.sdk.message.bitmap.StructuredData;
import postilion.realtime.sdk.util.DateTime;
import postilion.realtime.sdk.util.XPostilion;

/**
 * This class search for information in the realtime database and loads the
 * result in memory
 */
public class DBHandler {
	static final String seq_nr = "NIL";
	/*-------------------------------------------------------------------------*/

	/**
	 * This method uploads to memory the configurations of the messages to send to
	 * TM for a source interface.
	 * 
	 * @param id_node The "id_node" associated with this application.
	 * @return Hastable with the configuration
	 */
	public static String getCalculateConsecutive(String atmId, String termId, String consSection,
			boolean enableLog) {

		String consecutive = null;
		Connection cn = null;
		CallableStatement stmt = null;
		ResultSet rs = null;

		try {
			Logger.logLine("####>" + atmId + "####>" + termId + "####>" + consSection, false);
			cn = JdbcManager.getDefaultConnection();
			stmt = cn.prepareCall("{call Get_Consecutive(?, ?, ?, ?)}");
//			stmt = cn.prepareCall ("{call Get_Consecutivo(?, ?, ?)}");
			stmt.setString(1, atmId);
			stmt.setString(2, termId);
			stmt.setString(3, consSection);
			stmt.registerOutParameter(4, java.sql.Types.VARCHAR);
//			stmt.registerOutParameter(3, java.sql.Types.VARCHAR);
			stmt.execute();
//			consecutive = stmt.getString(3);
			consecutive = stmt.getString(4);
			Logger.logLine("####>" + consecutive, false);
			JdbcManager.commit(cn, stmt, rs);
		}

		catch (Exception e) {
			EventRecorder.recordEvent(e);
		} finally {
			try {
				JdbcManager.cleanup(cn, stmt, rs);
			} catch (SQLException e) {
				EventRecorder.recordEvent(e);
			}
		}

		return consecutive;
	}
	
//	/**
//	 * This method uploads to memory the configurations of the messages to send to
//	 * TM for a source interface.
//	 * 
//	 * @param id_node The "id_node" associated with this application.
//	 * @return Hastable with the configuration
//	 */
//	public static String getCalculateConsecutive(String atmId, String termId, String consSection,
//			boolean enableLog) {
//
//		String consecutive = null;
//		Connection cn = null;
//		CallableStatement stmt = null;
//		ResultSet rs = null;
//
//		try {
//			Logger.logLine("####>" + atmId + "####>" + termId + "####>" + consSection, enableLog);
//			cn = JdbcManager.getDefaultConnection();
//			stmt = cn.prepareCall("{call Get_Consecutive(?, ?, ?, ?)}");
////			stmt = cn.prepareCall ("{call Get_Consecutivo(?, ?, ?)}");
//			stmt.setString(1, atmId);
//			stmt.setString(2, termId);
//			stmt.setString(3, consSection);
//			stmt.registerOutParameter(4, java.sql.Types.VARCHAR);
////			stmt.registerOutParameter(3, java.sql.Types.VARCHAR);
//			stmt.execute();
////			consecutive = stmt.getString(3);
//			consecutive = stmt.getString(4);
//			Logger.logLine("####>" + consecutive, enableLog);
//			JdbcManager.commit(cn, stmt, rs);
//		}
//
//		catch (Exception e) {
//			EventRecorder.recordEvent(e);
////			shot.getObservables().get("getCalculateConsecutive")
////					.setDescription("error durante llamado a SP Get_Consecutivo");
////			shot.getObservables().get("getCalculateConsecutive").setSeverity(SnapShotSeverity.CRITICAL);
//		} finally {
//			try {
//				JdbcManager.cleanup(cn, stmt, rs);
//			} catch (SQLException e) {
//				EventRecorder.recordEvent(e);
////				shot.getObservables().get("getCalculateConsecutive").setDescription(e.getMessage());
////				shot.getObservables().get("getCalculateConsecutive").setSeverity(SnapShotSeverity.CRITICAL);
//			}
//		}
//
////		shot.getObservables().get("getCalculateConsecutive").close();
//		return consecutive;
//	}

	public static String getAccountInfo(int issuer_nr, String pan, String type_account, MonitorSnapShot shot)
			throws SQLException {

		shot.getObservables().put("getAccountInfo", new Observable(
				"metodo que consume sp, para obtener información de la una cuenta", "getAccountInfo", null));

		String consecutive = null;
		String account_id = "";
		Connection cn = null;
		CallableStatement stmt = null;
		ResultSet rs = null;

		try {

			cn = JdbcManager.getConnection("postcard");

			// cn =
			// DriverManager.getConnection("jdbc:sqlserver://Aquiles40d:50815;databaseName=postcard",
			// "usr_postilion", "P0sti1I0n_desa#5412");

			stmt = cn.prepareCall("{call GET_customer_id_default_account_type_NAME(?,?,?,?,?)}");
			stmt.setInt(1, issuer_nr);
			stmt.setString(2, pan);
			stmt.registerOutParameter(3, java.sql.Types.VARCHAR);
			stmt.registerOutParameter(4, java.sql.Types.VARCHAR);
			stmt.registerOutParameter(5, java.sql.Types.VARCHAR);
			stmt.execute();
			String cust_id = stmt.getString(3);
			String default_account = stmt.getString(4);
			String name = stmt.getString(5);

			stmt = cn.prepareCall("{call cm_load_card_accounts(?,?,?)}");
			stmt.setInt(1, issuer_nr);
			stmt.setString(2, pan);
			stmt.setString(3, seq_nr);
			rs = stmt.executeQuery();

			while (rs.next()) {
				String t_account = rs.getString("account_type");

				if (t_account.equals("" + type_account)) {

					account_id = rs.getString("account_id");

					return account_id + " " + t_account + " " + cust_id + " " + name;

				}

				else if (t_account.equals(default_account)) {
					account_id = rs.getString("account_id");

				}

			}
			return account_id + " " + default_account + " " + cust_id + " " + name;

		} catch (SQLException e) {

			EventRecorder.recordEvent(e);
			shot.getObservables().get("getAccountInfo").setDescription(e.getMessage());
			shot.getObservables().get("getAccountInfo").setSeverity(SnapShotSeverity.CRITICAL);
			return null;
		} finally {
			JdbcManager.cleanup(cn, stmt, rs);
			shot.getObservables().get("getAccountInfo").close();
		}

	}

	public static String getCardInfo(String issuerId, String accountNr, String accountType) {

		String encryptedAccount = null;
		String decryptedPan = null;
		SecurityManager sm = null;
		try {
			sm = new SecurityManager();
			encryptedAccount = sm.encrypt(accountNr);

		} catch (Exception e) {

			EventRecorder.recordEvent(e);

		}

//		shot.getObservables().put("getCalculateConsecutive", new Observable("metodo que consume sp, para obtener consecutivo", "getCalculateConsecutive", null));
		Connection cn = null;
		CallableStatement stmt = null;
		ResultSet rs = null;

		try {
//			Logger.logLine("####>"+atmId+"####>"+termId+"####>"+consSection, enableLog);
			cn = JdbcManager.getDefaultConnection();
			stmt = cn.prepareCall("{call pc_get_account_aditional_data(?, ?, ?)}");
//			stmt = cn.prepareCall ("{call Get_Consecutivo(?, ?, ?)}");
			stmt.setString(1, issuerId);
			stmt.setString(2, encryptedAccount);
			stmt.setString(3, accountType);
//			stmt.registerOutParameter(4, java.sql.Types.VARCHAR);
//			stmt.registerOutParameter(3, java.sql.Types.VARCHAR);
			rs = stmt.executeQuery();
			rs.next();

			if (rs.getString(1) != null) {

				String customer_id = rs.getString(1);// customer_id
				String extended_fields = rs.getString(2) == null ? "" : rs.getString(2);// extended_fields
				String pan_encrypted = rs.getString(3) == null ? "" : rs.getString(3);// pan_encrypted
				String c1_first_name = rs.getString(4) == null ? "" : rs.getString(4);// c1_first_name

				decryptedPan = sm.decryptPan(pan_encrypted);
			}
		}

		catch (Exception e) {

			EventRecorder.recordEvent(e);

		} finally {
			try {
				JdbcManager.cleanup(cn, stmt, rs);
			} catch (SQLException e) {
				EventRecorder.recordEvent(e);
			}
		}

		if (sm != null)

		{
			sm.dispose();
		}

//		shot.getObservables().get("getCalculateConsecutive").close();
		return decryptedPan;

	}

	public static StructuredData getHistoricalConsecutive(String retrivalRef, String processingCode, boolean enableLog) {

		Logger.logLine("## getHistoricalConsecutive ##>" + retrivalRef + " processingCode ", enableLog);

		StructuredData sd = new StructuredData();
		Connection cn = null;
		CallableStatement stmt = null;
		ResultSet rs = null;

		try {

			cn = JdbcManager.getDefaultConnection();
			stmt = cn.prepareCall("{call cust_get_structured_data_by_ref_nr(?, ?, ?, ?, ?)}");
			stmt.setString(1, retrivalRef);
			stmt.setString(2, processingCode.substring(0, 2));
			stmt.setString(3, processingCode.substring(2, 4));
			stmt.setString(4, processingCode.substring(4, 6));
			stmt.registerOutParameter(5, java.sql.Types.VARCHAR);
			stmt.execute();
			sd.fromMsgString(stmt.getString(5));
			Logger.logLine("##Retrived original SD##>" + retrivalRef + " :: " + sd, enableLog);
			JdbcManager.commit(cn, stmt, rs);
		}

		catch (Exception e) {

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			Logger.logLine("##ERROR RETRIVING##>" + sw.toString(), false);
		} finally {
			try {
				JdbcManager.cleanup(cn, stmt, rs);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		Logger.logLine("##SD Key INFO##>" + sd.get("REFERENCE_KEY"), false);

		return sd;
	}
	
	
	public static StructuredData getHistoricalConsecutiveByTranNr(String tranNr, boolean enableLog) {

		Logger.logLine("## getHistoricalConsecutive ##>" + tranNr + " tranNr ", enableLog);

		StructuredData sd = new StructuredData();
		Connection cn = null;
		CallableStatement stmt = null;
		ResultSet rs = null;

		try {

			cn = JdbcManager.getDefaultConnection();
			stmt = cn.prepareCall("{call cust_get_structured_data_by_tran_nr(?, ?)}");
			stmt.setString(1, tranNr);
			stmt.registerOutParameter(2, java.sql.Types.VARCHAR);
			stmt.execute();
			sd.fromMsgString(stmt.getString(2));
			Logger.logLine("##Retrived original SD##>" + tranNr + " :: " + sd, enableLog);
			JdbcManager.commit(cn, stmt, rs);
		}

		catch (Exception e) {

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			Logger.logLine("##ERROR RETRIVING##>" + sw.toString(), false);
		} finally {
			try {
				JdbcManager.cleanup(cn, stmt, rs);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		Logger.logLine("##SD Key INFO##>" + sd.get("REFERENCE_KEY"), false);

		return sd;
	}
	
	
	public static String getKeyOriginalTxBySeqNr(String SeqNr) {

		String keyOriginal = null;
		Connection cn = null;
		CallableStatement stmt = null;
		ResultSet rs = null;

		try {

			cn = JdbcManager.getDefaultConnection();
			stmt = cn.prepareCall("{call cust_get_srcnode_key_by_src_echodata(?, ?)}");
			stmt.setString(1, SeqNr);
			stmt.registerOutParameter(2, java.sql.Types.VARCHAR);
			stmt.execute();
			keyOriginal = stmt.getString(2);
			JdbcManager.commit(cn, stmt, rs);
		}

		catch (Exception e) {

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			Logger.logLine("##ERROR RETRIVING##>" + sw.toString(), false);
		} finally {
			try {
				JdbcManager.cleanup(cn, stmt, rs);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}


		return keyOriginal;
	}
	
	/*
	 * Obtiene de la base de datos los identificadores de institución y sus
	 * respectivos nombres
	 *
	 * @return hashMap con la información consultada
	 */
	public static ConcurrentHashMap<String, Object> loadPinPadKeys() {
		ConcurrentHashMap<String, Object> pinpadsData = new ConcurrentHashMap<>();
		PinPad pinpad = null;
		Statement st = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = JdbcManager.getDefaultConnection();
			String query = "SELECT * FROM cust_pinpad_data WITH (NOLOCK)";
			st = con.createStatement();
			rs = st.executeQuery(query);
			while (rs.next()) {
				pinpad = new PinPad();
				pinpad.setCodOficina(rs.getString("codigo_oficina"));
				pinpad.setSerial(rs.getString("serial"));
				pinpad.setTerminal1(rs.getString("terminal1"));
				pinpad.setTerminal2(rs.getString("terminal2"));
				pinpad.setFechaInicializacion( rs.getTimestamp("fecha_inicializacion"));
				pinpad.setKey_ini(rs.getString("key_ini"));
				pinpad.setKey_ini_snd(rs.getString("key_snd"));
				pinpad.setFechaIntercambio(rs.getTimestamp("fecha_intercambio"));
				pinpad.setKey_exc(rs.getString("key_exc"));
				pinpad.setKey_exc_snd(rs.getString("key_exc_snd"));
				pinpadsData.put(rs.getString("codigo_oficina")+rs.getString("serial"), pinpad);
			}
			JdbcManager.commit(con, st, rs);
			return pinpadsData;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				JdbcManager.cleanup(con, st, rs);
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
		return pinpadsData;
	}

	/**
	 * Activa codigos de respuesta qeu se reciben y no estaban activos en base de
	 * datos
	 * 
	 * @param code    a consultar
	 * @param process 1
	 * 
	 * @return update true si activo false de lo contrario
	 */
	public static void updateInsertPinPadDataInit(PinPad pinpad) {
		Statement st = null;
		ResultSet rs = null;
		Connection con = null;
		int rows = 0;
		try {
			con = JdbcManager.getDefaultConnection();
			st = con.createStatement();
			rs = st.executeQuery(String.format(Queries.SELECT_EXIST_INIT_PINPAD_WITH_CODOFI_SERIAL, 
					pinpad.getCodOficina(), pinpad.getSerial()));
			Logger.logLine("CONSULTA PREVIA " + st.toString(), true);
			Logger.logLine("CONSULTA PREVIA " + rows, true);
			if (rs.next()) {
//				st.executeUpdate(String.format(Queries.UPDATE_KEY_INIT_PINPAD, 
//						pinpad.getKey_ini(), pinpad.getKey_ini_snd(), pinpad.getFechaInicializacion(), pinpad.getCodOficina(), pinpad.getSerial()));
				PreparedStatement ps = con.prepareStatement(Queries.UPDATE_KEY_INIT_PINPAD);
				ps.setString(1, pinpad.getKey_ini());
				ps.setString(2, pinpad.getKey_ini_snd());
				ps.setTimestamp(3, pinpad.getFechaInicializacion());
				ps.setTimestamp(4, pinpad.getFecha_modificacion());
				ps.setString(5, pinpad.getUsuario_modificacion());
				ps.setString(6, pinpad.getCodOficina());
				ps.setString(7, pinpad.getSerial());
				Logger.logLine("CONSULTA PREVIA " + ps.toString(), true);
				ps.executeUpdate();
			}else {
				PreparedStatement ps1 = con.prepareStatement(Queries.INSERT_KEY_INIT_PINPAD);
//				st.executeUpdate(String.format(Queries.INSERT_KEY_INIT_PINPAD, 
//						pinpad.getCodOficina(), pinpad.getSerial(), pinpad.getTerminal1(), pinpad.getFechaInicializacion(), pinpad.getKey_ini(), pinpad.getKey_ini_snd()));
//				Logger.logLine("INSERT " + st.toString(), true);
				ps1.setString(1, pinpad.getCodOficina());
				ps1.setString(2, pinpad.getSerial());
				ps1.setString(3, pinpad.getTerminal1());
				ps1.setTimestamp(4, pinpad.getFechaInicializacion());
				ps1.setString(5, pinpad.getKey_ini());
				ps1.setString(6, pinpad.getKey_ini_snd());
				ps1.setTimestamp(7, pinpad.getFecha_creacion());
				ps1.setString(8, pinpad.getUsuario_creacion());
				Logger.logLine("CONSULTA PREVIA " + ps1.toString(), true);
				ps1.executeUpdate();
			}
				
			
			JdbcManager.commit(con, st, rs);
		} catch (SQLException e) {
			pinpad.setError(true);
			EventRecorder.recordEvent(
					new Exception("SQL: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					Utils.getStringMessageException(e) }));
		} catch (Exception e) {
			pinpad.setError(true);
			EventRecorder.recordEvent(
					new Exception("SQL Ex: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					Utils.getStringMessageException(e) }));
		} finally {
			try {
				JdbcManager.cleanup(con, st, rs);
			} catch (SQLException e) {
				EventRecorder.recordEvent(
						new Exception("SQL clean: " + e.toString()));
				EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
						Utils.getStringMessageException(e) }));
			}

		}
	}
	
	/**
	 * Activa codigos de respuesta qeu se reciben y no estaban activos en base de
	 * datos
	 * 
	 * @param code    a consultar
	 * @param process 1
	 * 
	 * @return update true si activo false de lo contrario
	 */
	public static void updateInsertPinPadDataExchange(PinPad pinpad) {
		Statement st = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = JdbcManager.getDefaultConnection();
			PreparedStatement ps = con.prepareStatement(Queries.UPDATE_KEY_EXC_PINPAD);
			ps.setString(1, pinpad.getKey_exc());
			ps.setString(2, pinpad.getKey_exc_snd());
			ps.setTimestamp(3, pinpad.getFechaIntercambio());
			ps.setTimestamp(4, pinpad.getFecha_modificacion());
			ps.setString(5, pinpad.getUsuario_modificacion());
			ps.setString(6, pinpad.getCodOficina());
			ps.setString(7, pinpad.getSerial());
			ps.executeUpdate();
				
			
			JdbcManager.commit(con, ps);
		} catch (SQLException e) {
			pinpad.setError(true);
			EventRecorder.recordEvent(
					new Exception("SQL: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					Utils.getStringMessageException(e) }));
		} catch (Exception e) {
			pinpad.setError(true);
			EventRecorder.recordEvent(
					new Exception("SQL Ex: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					Utils.getStringMessageException(e) }));
		} finally {
			try {
				JdbcManager.cleanup(con, st, rs);
			} catch (SQLException e) {
				EventRecorder.recordEvent(
						new Exception("SQL clean: " + e.toString()));
				EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
						Utils.getStringMessageException(e) }));
			}

		}
	}
	
	/**
	 * Define sentencias a ejecutar en base de datos.
	 * 
	 * @author Cristian Cardozo
	 *
	 */
	public static class Queries {
		public static final String SELECT_EXIST_INIT_PINPAD_WITH_CODOFI_SERIAL = "SELECT key_ini, key_snd FROM cust_pinpad_data WITH (NOLOCK) WHERE codigo_oficina = '%s' and serial='%s'";
		public static final String UPDATE_KEY_INIT_PINPAD = "UPDATE cust_pinpad_data SET key_ini = ?, key_snd = ?, fecha_inicializacion = ?, fecha_modificacion = ?, usuario_modificacion = ? WHERE codigo_oficina=? and serial=?";
		public static final String INSERT_KEY_INIT_PINPAD = "INSERT INTO cust_pinpad_data(codigo_oficina,serial,terminal1,fecha_inicializacion,key_ini,key_snd,fecha_creacion,usuario_creacion) VALUES(?,?,?,?,?,?,?,?)";
		public static final String UPDATE_KEY_EXC_PINPAD = "UPDATE cust_pinpad_data SET key_exc = ?, key_exc_snd = ?, fecha_intercambio = ?, fecha_modificacion = ?, usuario_modificacion = ? WHERE codigo_oficina=? and serial=?";
	}
	

}
