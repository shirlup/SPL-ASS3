package bgu.spl171.net.srv;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.api.MessagingProtocol;
import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.ConnectionsImpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
//thread per client
public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private ConnectionsImpl<T> connections;

    
    //constructor
    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
    }

    @Override
    /**
     * this function runs the connection handler in use
     */
    public void run() {
    	protocol.start(connections.getId(this), connections); //starting the protocol of current connection handler
        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            while (connected && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                   protocol.process(nextMessage); //protocol handles the message
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    /**
     * this function closes the socket
     */
    public void close() throws IOException {
        connected = false;
        if(in != null ) in.reset();
        if(out != null ) out.flush();
        sock.close();
        
    }

	@Override
	/**
	 * this function sends msg constructed by server to client
	 */
	public void send(T msg) {
		try {
			out.write(encdec.encode(msg));
			out.flush();
		} catch (IOException e) {}
		
}
	/**
	 * this function sets connections to current connection handler
	 * @param connections
	 */
	public void setConnections (ConnectionsImpl<T> connections ){
		this.connections = connections;
	}
}
