package bgu.spl171.net.api.bidi;


public class ACKMsg extends TFTPMsg{ //ACK
	
	private short blockNum;
   
	public ACKMsg(short optCode, short blockNum){
       super(optCode);
       this.blockNum = blockNum;
   }
	public short getBlockNum() {
		return blockNum;
	}

}