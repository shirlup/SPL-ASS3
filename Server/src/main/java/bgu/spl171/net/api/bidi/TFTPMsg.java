package bgu.spl171.net.api.bidi;

public abstract class TFTPMsg {
	private short opCode;

    public TFTPMsg(short optCode){
        
        this.opCode = optCode;
    }
    
    public short getOpCode(){
    	return opCode;
    }
}

