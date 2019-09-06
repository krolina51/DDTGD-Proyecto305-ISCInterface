package postilion.realtime.base24framework.adapter;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

import postilion.realtime.base24framework.bitmap.Iso8583B24;
import postilion.realtime.base24framework.stream.Header;
import postilion.realtime.sdk.message.Validator;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.util.XPostilion;
import postilion.testharness.entity.message.adapter.Adapter;
import postilion.testharness.entity.message.adapter.XUnableToCreateAdapter;
import postilion.testharness.entity.message.adapter.XUnableToPackMessage;
import postilion.testharness.migration.env.ExecutionException;

public class Iso8583B24Adapter extends Adapter
{
  private Iso8583B24 msg;

  protected Iso8583B24Adapter() throws XUnableToCreateAdapter
  {
	  try
	  {
		  String custom_header = null;
		  //custom_header = "123456AAAA";
		  msg = new Iso8583B24(custom_header);
	  }
	  catch (Exception e)
	  {
		  throw new XUnableToCreateAdapter(getClass(), e);
	  }
  }

  protected Iso8583B24Adapter(Iso8583B24 msg)
  {
	  this.msg = msg;
  }

  public Iso8583B24Adapter(String msg_type)
  	throws XUnableToCreateAdapter
  {
	  this();
	  int msg_type_int = getMessageTypeAsInt(msg_type);
	  this.msg.putMsgType(msg_type_int);
  }

  protected Iso8583B24 getMessage()
  {
	  return this.msg;
  }

  public void putField(String field_id, String field_value)
  	throws Throwable
  {
	  if (field_id.startsWith("HDR"))
	  {
		  Header hdr = this.msg.getHeader();
		  String header_field = field_id.substring(field_id.indexOf(".") + 1);
    	
		  if (header_field.equals(Header.Field.ISO_LITERAL))
		  {
			  hdr.putField(Header.Field.ISO_LITERAL, "ISO");
		  }
		  else if (header_field.equals(Header.Field.ORIGINATOR_CODE))
		  {
			  hdr.putField(Header.Field.ORIGINATOR_CODE, field_value);
		  }
		  else if (header_field.equals(Header.Field.PRODUCT_INDICATOR))
		  {
			  hdr.putField(Header.Field.PRODUCT_INDICATOR, field_value);
		  }
		  else if (header_field.equals(Header.Field.RELEASE_NUMBER))
		  {
			  hdr.putField(Header.Field.RELEASE_NUMBER, field_value);
		  }
		  else if (header_field.equals(Header.Field.RESPONDER_CODE))
		  {	
			  hdr.putField(Header.Field.RESPONDER_CODE, field_value);
		  }
		  else if (header_field.equals(Header.Field.STATUS))
		  {
			  hdr.putField(Header.Field.STATUS, field_value);
		  }
    	
		  this.msg.putHeader(hdr);
	  }
	  else
	  {
		  int field_number = getFieldNumber(field_id);
		  this.msg.putField(field_number, field_value);
	  }
  }

  public String getField(String field_id)
    throws Throwable
  {
    String field;
    if (field_id.startsWith("HDR."))
    {
    	String header_field = field_id.substring(field_id.indexOf(".") + 1);
    	Header hdr = this.msg.getHeader();
    	field = hdr.getField(header_field);
    }
    else
    {
    	int field_number = getFieldNumber(field_id);
    	String field_value = this.msg.getField(field_number);
     
    	if ((field_value != null) && (field_value.length() > 0))
    	{
    		field = field_value;
    	}
    	else
    	{
    		field = null;
    	}
    }

    return field;
  }

  public void fromMessage(byte[] msg_data)
    throws XPostilion
  {
    getMessage().fromMsg(msg_data);
  }

  public byte[] toMessage()
    throws XPostilion
  {
    return getMessage().toMsg();
  }

  public String getMessageType()
  {
    return Iso8583.MsgType.toString(getMessage().getMsgType());
  }

  public Enumeration enumerateSetFields()
  {
    return getMessage().enumerateSetFields();
  }

  public String toString()
  {
    return getMessage().toString();
  }

  public Iterator setFields()
    throws Throwable
  {
    LinkedList list_of_fields = new LinkedList();

    Enumeration set_header_fields = this.msg.getHeader().enumerateSetFields();

    while (set_header_fields.hasMoreElements())
    {
      String set_header_field = (String)set_header_fields.nextElement();
      list_of_fields.add("HDR." + set_header_field);
    }

    Enumeration set_fields = this.msg.enumerateSetFields();

    while (set_fields.hasMoreElements())
    {
      String set_field = (String)set_fields.nextElement();
      list_of_fields.add(set_field);
    }

    return list_of_fields.listIterator();
  }

  private int getFieldNumber(String field_id)
    throws XUnableToPackMessage
  {
	  int field_num;
    try
    {
      field_num = Integer.parseInt(field_id);
    }
    catch (NumberFormatException nfe)
    {
      
      throw new XUnableToPackMessage(
        this, "Invalid field name: " + field_id);
    }
    
    return field_num;
  }

  protected static int getMessageTypeAsInt(String msg_type)
  {
    if (Validator.isValidN(msg_type))
    {
      return Integer.parseInt(msg_type, 16);
    }

    throw new ExecutionException("Invalid message type: " + msg_type);
  }
}