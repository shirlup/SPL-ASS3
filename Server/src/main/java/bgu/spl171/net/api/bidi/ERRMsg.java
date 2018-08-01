package bgu.spl171.net.api.bidi;
	
	public class ERRMsg extends TFTPMsg{ //ERROR
		String error;
		short errorCode;//(opCode, errorCode, error)
	    public ERRMsg(short optCode, short errorCode, String error){
	        super(optCode);
	        this.errorCode = errorCode;
	        this.error= error;
	    }
	    
	    public String getError(){
	    	return error;
	    }
	    public short getErrorCode(){
	    	return errorCode;
	    }

	}

