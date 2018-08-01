package bgu.spl171.net.api.bidi;

public class RRQMsg extends TFTPMsg{ //RRQ
   
private String fileName;
	
   public RRQMsg(short opCode,String fileName){
       super(opCode);
       this.fileName = fileName;
   }
   
   public String getFileName(){
   	return fileName;
   }
   
}
