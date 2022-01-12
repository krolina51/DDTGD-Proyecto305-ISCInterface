package postilion.realtime.iscinterface.util;

import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MsgContract {
	    
	    private String msgKey;
	    private HashMap<Integer, String> msgData;
	    private String msgTime;
	    
	    public MsgContract() {
	        super();
	        msgData = new HashMap<Integer, String>();
	    } 
	             
	    public String getMsgKey() {
			return msgKey;
		}

		public void setMsgKey(String msgKey) {
			this.msgKey = msgKey;
		}

		public HashMap<Integer, String> getMsgData() {
			return msgData;
		}

		public void setMsgData(HashMap<Integer, String> msgData) {
			this.msgData = msgData;
		}

		public String getMsgTime() {
			return msgTime;
		}

		public void setMsgTime(String msgTime) {
			this.msgTime = msgTime;
		}

		public String toJsonStr() {
	        
	        return new Gson().toJson(this);
	        
	    }
	    
	    public JsonObject toJson() {
	        
	        JsonParser parser = new JsonParser();
	        return (JsonObject)parser.parse(new Gson().toJson(this));
	        
	    }
}
