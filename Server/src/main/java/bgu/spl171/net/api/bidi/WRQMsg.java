package bgu.spl171.net.api.bidi;

public class WRQMsg extends TFTPMsg{ //WRQ
	
	private String fileName;
	
   public WRQMsg(short optCode,String fileName){
       super(optCode);
       this.fileName = fileName;
   }
   
   public String getFileName(){
   	return fileName;
   }

}
