package postilion.realtime.iscinterface.database;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import monitor.core.dto.MonitorSnapShot;
import monitor.core.dto.Observable;
import monitor.core.dto.SnapShotSeverity;
import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.sdk.eventrecorder.EventRecorder;
import postilion.realtime.sdk.ipc.SecurityManager;
import postilion.realtime.sdk.jdbc.JdbcManager;
import postilion.realtime.sdk.message.bitmap.StructuredData;
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
			Logger.logLine("####>" + atmId + "####>" + termId + "####>" + consSection, enableLog);
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
			Logger.logLine("####>" + consecutive, enableLog);
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

		Logger.logLine("## getHistoricalConsecutive ##>" + retrivalRef, enableLog);

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
			Logger.logLine("##ERROR RETRIVING##>" + sw.toString(), enableLog);
		} finally {
			try {
				JdbcManager.cleanup(cn, stmt, rs);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		Logger.logLine("##SD Key INFO##>" + sd.get("REFERENCE_KEY"), enableLog);

		return sd;
	}

}
