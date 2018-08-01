package bgu.spl171.net.api.bidi;

public class LOGRQMsg extends TFTPMsg{ //LOGRQ
	String userName;
    public LOGRQMsg(short optCode,String userName){
        super(optCode);
        this.userName = userName;
    }
    
    public String getUserName(){
    	return userName;
    }

}