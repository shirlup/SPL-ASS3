package bgu.spl171.net.api.bidi;

public class DATAMsg extends TFTPMsg{ //DATA
	private short packetSize; 
	private short blockNum;
	private byte [] data; 
   
	public DATAMsg(short optCode,short packetSize, short blockNum ,byte[] data){
       super(optCode);
       this.packetSize = packetSize;
       this.blockNum = blockNum;
       this.data = data;     
   }
	public byte [] getData() {
		return data;
	}
	public short getPacketSize() {
		return packetSize;
	}
	public short getBlockNum() {
		return blockNum;
	}
}
