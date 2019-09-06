package postilion.realtime.base24framework.adapter;

import postilion.realtime.sdk.util.XPostilion;
import postilion.testharness.entity.message.adapter.Adapter;
import postilion.testharness.entity.message.adapter.AdapterFactory;
import postilion.testharness.entity.message.adapter.XUnableToCreateAdapter;

public class Iso8583B24AdapterFactory
  implements AdapterFactory
{
  public Adapter createAdapter(String msg_type)
    throws XUnableToCreateAdapter
  {
    return new Iso8583B24Adapter(msg_type);
  }

  public Adapter createAdapter(byte[] msg_data)
    throws XUnableToCreateAdapter
  {
	Iso8583B24Adapter adapter = new Iso8583B24Adapter();
	try
	{
	  adapter.fromMessage(msg_data);
	}
	catch (XPostilion xp)
	{
	  throw new XUnableToCreateAdapter(getClass(), xp);
	}
	return adapter;
  }
}
