package postilion.realtime.iscinterface.database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.sdk.jdbc.JdbcManager;
import postilion.realtime.sdk.message.bitmap.StructuredData;
import postilion.realtime.sdk.util.XPostilion;	

/**
 * This class search for information in the realtime database and loads the result
 * in memory
 */
public class DBHandler
{
	static final String seq_nr = "NIL";
	/*-------------------------------------------------------------------------*/
	
	/**
	 * This method uploads to memory the configurations of the messages 
	 * to send to TM for a source interface.
	 * @param id_node
	 *    The "id_node" associated with this application.
	 * @return
	 *	Hastable with the configuration 	 
	 */
	public static String getCalculateConsecutive(String atmId, String termId) throws Exception
	{

		String consecutive = null;
		Connection cn = null;
		CallableStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			
			cn = JdbcManager.getDefaultConnection();
			stmt = cn.prepareCall ("{call Get_Consecutivo(?, ?, ?)}");
			stmt.setString(1, atmId);
			stmt.setString(2, termId);
			stmt.registerOutParameter(3, java.sql.Types.VARCHAR);
			stmt.execute();
			consecutive = stmt.getString(3);
			Logger.logLine("####>"+consecutive);
			JdbcManager.commit(cn, stmt, rs);			
		}
		
		catch (Exception e)
		{			
			Logger.logLine("####>"+e.getMessage());
			throw new XPostilion();
		}
		finally
		{
			JdbcManager.cleanup(cn, stmt, rs);
		}
		
		return consecutive;	
	}


	public static String getAccountInfo(int issuer_nr, String pan, String type_account) throws SQLException {
		
		String consecutive = null;
		String account_id = "";
		Connection cn = null;
		CallableStatement stmt = null;
		ResultSet rs = null;

		try {
			
			cn = JdbcManager.getConnection("postcard");

			//cn = DriverManager.getConnection("jdbc:sqlserver://Aquiles40d:50815;databaseName=postcard",
			//		"usr_postilion", "P0sti1I0n_desa#5412");

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

			e.printStackTrace();

			return null;
		}
		finally {
			JdbcManager.cleanup(cn, stmt, rs);
		}

	}
	
	public static String getHistoricalConsecutive(String retrivalRef) throws Exception
	{

		StructuredData sd = new StructuredData();
		Connection cn = null;
		CallableStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			
			cn = JdbcManager.getDefaultConnection();
			stmt = cn.prepareCall ("{call cust_get_structured_data_by_ref_nr(?, ?)}");
			stmt.setString(1, retrivalRef);
			stmt.registerOutParameter(2, java.sql.Types.VARCHAR);
			stmt.execute();
			sd.fromMsgString(stmt.getString(2));
			Logger.logLine("##Retrived original SD##>"+retrivalRef+" :: "+sd);
			JdbcManager.commit(cn, stmt, rs);			
		}
		
		catch (Exception e)
		{			
			Logger.logLine("##ERROR RETRIVING##>"+e.getStackTrace());
			throw new XPostilion();
		}
		finally
		{
			JdbcManager.cleanup(cn, stmt, rs);
		}
		
		Logger.logLine("##SD Key INFO##>"+sd.get("REFERENCE_KEY"));
		
		return sd.get("REFERENCE_KEY");	
	}

}
