package bgu.spl171.net.api.bidi;

public class DELRQMsg extends TFTPMsg{//DELRQ
	private String fileName;
    public DELRQMsg(short optCode, String fileName){
        super(optCode);
        this.fileName = fileName;
    }
    
    public String getFileName(){
    	return fileName;
    }

}