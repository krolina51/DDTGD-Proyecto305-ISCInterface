package postilion.realtime.iscinterface.web;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.api.server.ServerSessionListener;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.frames.DataFrame;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.FuturePromise;
import org.eclipse.jetty.util.Jetty;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.iscinterface.web.model.Message;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.util.convert.Transform;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

// Copied from the JavaDoc for org.eclipse.jetty.http2.client.HTTP2Client
public class Http2CustClient {
	
	String host = "localhost";
	int port = 8443;
	
	String curIscMsgString = null;
	
	HTTP2Client client = null;
	SslContextFactory sslContextFactory = null;
	
	public Http2CustClient() throws Exception{
        // Create and start HTTP2Client.
        this.client = new HTTP2Client();
        this.sslContextFactory = new SslContextFactory(true);
        client.addBean(sslContextFactory);
        client.start();
	}
	
	public Http2CustClient(String host, int port) throws Exception{
		
		this.host = host;
		this.port = port;
		
        // Create and start HTTP2Client.
        this.client = new HTTP2Client();
        this.sslContextFactory = new SslContextFactory(true);
        client.addBean(sslContextFactory);
        client.start();
	}
	
	public void closeClient() {
		
		if(!this.client.isStopped() || !this.client.isStopping()) {
			try {
				this.client.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

    public String sendIsoMsg2IscMsg(Iso8583Post isoMsg) throws Exception {
        long startTime = System.nanoTime();

        FuturePromise<Session> sessionPromise = new FuturePromise<>();
        client.connect(this.sslContextFactory, new InetSocketAddress(this.host, this.port), new ServerSessionListener.Adapter(), sessionPromise);

        // Obtain the client Session object.
        Session session = sessionPromise.get(5, TimeUnit.SECONDS);

        // Prepare the HTTP request headers.
        HttpFields requestFields = new HttpFields();
        requestFields.put("User-Agent", client.getClass().getName() + "/" + Jetty.VERSION);
        
        Message msg = new Message();
        msg.setMsgId(isoMsg.getStructuredData().get("REFERENCE_KEY"));
        msg.setDate(isoMsg.getField(Iso8583.Bit._012_TIME_LOCAL));
        msg.setIsoMsg(Transform.fromBinToHex(Transform.getString(isoMsg.toMsg())));
       
        // Prepare the HTTP request object.
        MetaData.Request request = new MetaData.Request("GET", new HttpURI("https://" + host + ":" + port + "/iso2isc?message="+msg.toStringXml()), HttpVersion.HTTP_2, requestFields);
       
        // Create the HTTP/2 HEADERS frame representing the HTTP request.
        HeadersFrame headersFrame = new HeadersFrame(request, null, true);

        // Prepare the listener to receive the HTTP response frames.
        Stream.Listener responseListener = new Stream.Listener.Adapter()
        {
            @Override
            public void onData(Stream stream, DataFrame frame, Callback callback)
            {
                byte[] bytes = new byte[frame.getData().remaining()];
                frame.getData().get(bytes);
                long duration = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime);
                curIscMsgString = new String(bytes);
                callback.succeeded();
            }
        };

        for(int i = 0 ; i < 100; i++) {
        	session.newStream(headersFrame, new FuturePromise<>(), responseListener);
        }

        //Thread.sleep(TimeUnit.SECONDS.toMillis(20));

        //client.stop();
        
        Logger.logLine("HTTP/2 CLIENT:"+curIscMsgString, false);
        return curIscMsgString;
    }
    
}
