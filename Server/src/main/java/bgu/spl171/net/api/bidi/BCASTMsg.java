package bgu.spl171.net.api.bidi;

public class BCASTMsg extends TFTPMsg{
	private boolean deleteOrAdd;
	private String fileName;
   public BCASTMsg(short optCode, boolean deleteOrAdd, String fileName){
       super(optCode); //opCode, singleByte, fileName)
       this.deleteOrAdd =deleteOrAdd;
       this.fileName = fileName;
   }
   
   public String getFileName(){
   	return fileName;
   }
   
   public boolean getDeleteOrAdd(){
   	return deleteOrAdd;
   }

}
