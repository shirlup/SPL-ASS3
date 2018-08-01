package bgu.spl171.net.impl.TFTPtpc;

import bgu.spl171.net.api.MessageEncoderDecoderImpl;
import bgu.spl171.net.api.bidi.ProtocolImpl;
import bgu.spl171.net.api.bidi.TFTPMsg;
import bgu.spl171.net.srv.Server;

public class TPCMain {

	public static void main(String[] args) {
		int port=Integer.parseInt(args[0]);
		Server.threadPerClient(port, 
    		   ()-> new ProtocolImpl<TFTPMsg>(){
    		   } , 
    		   MessageEncoderDecoderImpl::new).serve();
		}
}

