package bgu.spl171.net.impl.TFTPreactor;

import java.util.function.Supplier;
import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.api.MessageEncoderDecoderImpl;
import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.api.bidi.ConnectionsImpl;
import bgu.spl171.net.api.bidi.ProtocolImpl;
import bgu.spl171.net.api.bidi.TFTPMsg;
import bgu.spl171.net.srv.Reactor;
import bgu.spl171.net.srv.Server;

public class ReactorMain {

	public static void main(String[] args) {
		   int port=Integer.parseInt(args[0]);
	        Server.reactor(
	        		Runtime.getRuntime().availableProcessors(),
	        		port,
	        		()-> new ProtocolImpl<>()
					 ,
	        		MessageEncoderDecoderImpl::new
	        	).serve();	
		
	}

}

