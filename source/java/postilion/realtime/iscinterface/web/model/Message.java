package postilion.realtime.iscinterface.web.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "message")
public class Message implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String msgId;
	private String date;
	private String isoMsg;
		
	public Message() {
		super();
		// TODO Auto-generated constructor stub
	}

	@XmlElement(name = "msgId")
	public String getMsgId() {
		return msgId;
	}


	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	@XmlElement(name = "date")
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	@XmlElement(name = "isoMsg")
	public String getIsoMsg() {
		return isoMsg;
	}

	public void setIsoMsg(String isoMsg) {
		this.isoMsg = isoMsg;
	}
	
	public String toStringXml() {
		StringBuilder sb =  new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<message>");
		sb.append("<msgId>").append(this.msgId).append("</msgId>");
		sb.append("<date>").append(this.date).append("</date>");
		sb.append("<isoMsg>").append(this.isoMsg).append("</isoMsg>");
		sb.append("</message>");
		return sb.toString();
	}
	
}
