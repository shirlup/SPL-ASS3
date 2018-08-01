package bgu.spl171.net.api;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;

import javax.annotation.Generated;

import bgu.spl171.net.api.bidi.*;

public class MessageEncoderDecoderImpl<T> implements MessageEncoderDecoder<TFTPMsg> {

	short opCode= -1;
	short packetSize = -1;
	short blockNum = -1;
	private byte[] bytes = new byte[1 << 10]; //start with 1k
	private int len = 0;
	private final ByteBuffer lengthBuffer = ByteBuffer.allocate(2);
	private short errorCode =-1;
	private Boolean broadCast = null;
	
	@Override
	/**
	 * this function decodes byte after byte and constructs a TFTPMsg (to be send to process by protocol)
	 */
	public TFTPMsg decodeNextByte(byte nextByte) {
		if(opCode == -1){ 
			 lengthBuffer.put(nextByte);
	            if (!lengthBuffer.hasRemaining()) { //we read 2 bytes and therefore can take the opCode
	            	lengthBuffer.flip();
	            	opCode = lengthBuffer.getShort();
	            	lengthBuffer.clear();
	                //2 optional packets size 2 bytes:
	            	if (opCode == 6) { //DIRQ
	                	TFTPMsg msg2Return = new DIRQMsg(opCode); 
						this.resetFields();
						return msg2Return;
	                }
	                else if(opCode == 10) {//DISC
	                	TFTPMsg msg2Return = new DISCMsg(opCode); 
						this.resetFields();
						return msg2Return;
	                }	
	                lengthBuffer.clear();
	            }	
		}
		
		else { //(opCode != -1) we have the opCode  
			switch(opCode){
				case(1): //RRQ | Opcode | Filename | 0 | (2 bytes string 1 byte)
					if(nextByte != '\0'){
						pushByte(nextByte);
					}
					else { //we got to the end 
						String fileName = fromBytesToString();
						TFTPMsg msg2Return = new RRQMsg(opCode,fileName);
						this.resetFields();
						return msg2Return; 
					}
					break;
				case(2): //WRQ | Opcode | Filename | 0 | (2 bytes string 1 byte)
					if(nextByte != '\0'){
						pushByte(nextByte);
					}
					else { 
						String fileName = fromBytesToString();
						TFTPMsg msg2Return = new WRQMsg(opCode,fileName);
						this.resetFields();
						return msg2Return; 	 
					}
					break;
				case(3): //DATA : Opcode | Packet Size | Block # | Data (2 bytes 2 bytes 2 bytes n bytes)
					if(packetSize == -1){ 
						 lengthBuffer.put(nextByte);
				            if (!lengthBuffer.hasRemaining()) { //we read 2 bytes and therefore can take the packetSize
				                lengthBuffer.flip();
				                packetSize = lengthBuffer.getShort();
				                lengthBuffer.clear();
				                bytes = new byte [packetSize];
				                
				            }
				     }
				    else{
				           if(blockNum == -1){ 
				        	   lengthBuffer.put(nextByte);
						       		if (!lengthBuffer.hasRemaining()) { //we read 2 bytes and therefore can take the blockNumber
						               lengthBuffer.flip();
						               blockNum = lengthBuffer.getShort();
						               lengthBuffer.clear();
						               if(packetSize==0){ //means message doesn't have content
						            	   TFTPMsg msg2Return = new DATAMsg(opCode,packetSize,blockNum,bytes);
						            	   this.resetFields();
						            	   return msg2Return; 
						            	   	   
						               }
						            }
				           }
				           else{ 
				        	   if(len == packetSize-1){ //we are in the last byte to read 
				        		   pushByte(nextByte); //push the last byte to array
				        		   TFTPMsg msg2Return = new DATAMsg(opCode,packetSize,blockNum,bytes);
				            	   this.resetFields();
				            	   return msg2Return; 
				        		   
				        	   }
				        	   else //continue reading
				        	   pushByte(nextByte);
				           }
				    	}
				break;
				
				case (4): //ACK | Opcode | Block # | (2 bytes 2 bytes)
                    if (blockNum == -1) {
                        lengthBuffer.put(nextByte);
                        if (!lengthBuffer.hasRemaining()) { //we read 2 bytes and therefore can take the blockNumber
                        	lengthBuffer.flip();
                        	blockNum = lengthBuffer.getShort();
                        	lengthBuffer.clear();
                        	TFTPMsg msg2Return = new ACKMsg(opCode,blockNum);
                        	this.resetFields();
                        	return msg2Return; 
                        } 
                    }
                    	    
                break;
                
				case (5): //ERROR | Opcode | ErrorCode | ErrMsg | 0 |
                	if (errorCode == -1) {
					lengthBuffer.put(nextByte);
						if (!lengthBuffer.hasRemaining()) { //we read 2 bytes and therefore can take the errorCode
						lengthBuffer.flip();
						errorCode = lengthBuffer.getShort();
						lengthBuffer.clear();
					}
				}
                else {
					if (nextByte != '\0')
						pushByte(nextByte);
					else {
						String error = fromBytesToString();
						TFTPMsg msg2Return = new ERRMsg(opCode, errorCode, error);
		            	this.resetFields();
		            	return msg2Return; 	
					}
                 }
                break;
                case(7): //LOGRQ | Opcode | Username | 0 (2 bytes string 1 byte)
                	if(nextByte != '\0'){
						pushByte(nextByte);
					}
					else { 
						String userName = fromBytesToString();
						TFTPMsg msg2Return = new LOGRQMsg(opCode,userName);
		            	this.resetFields();
		            	return msg2Return; 	
					}
					break;
				case(8): //DELRQ | Opcode | Filename | 0 | (2 bytes string 1 byte)
					if(nextByte != '\0'){
						pushByte(nextByte);
					}
					else { 
						String fileName = fromBytesToString();
						TFTPMsg msg2Return = new DELRQMsg(opCode,fileName);
		            	this.resetFields();
		            	return msg2Return; 	 
					}
					break;
				case(9): //BCAST | Opcode | Deleted/Added | Filename | 0 | (2 bytes 1 byte string 1 byte)
					if (broadCast == null) {
						if (nextByte == 0)
							broadCast = false; // 0 means a file been deleted
						else
							broadCast = true; // 1 means a file been added
					} 
					else {
						if (nextByte != '\0')
							pushByte(nextByte);
						else {
							String fileName = new String(bytes, StandardCharsets.UTF_8);
							TFTPMsg msg2Return = new BCASTMsg(opCode, broadCast, fileName);;
			            	this.resetFields();
			            	return msg2Return; 	
						}
					}
					break;
				default : //if opcode is unknown 
					
					TFTPMsg msg2Return = new ERRMsg(opCode,(short)4,"unknown operation code!!!");
					return msg2Return; //message is sent to protocol
               	}
		}
		return null;
	}
	/**
	 * this function converts from bytes to string
	 * @return String
	 */
	   private String fromBytesToString() {
		   return new String(bytes,0, len, StandardCharsets.UTF_8);
		
	}
	/**
	 * this function pushes the nextByte that is being decoded to a bytes array
	 * @param nextByte
	 */
	private void pushByte(byte nextByte) {
	        if (len >= bytes.length) {
	            bytes = Arrays.copyOf(bytes, len * 2);
	        }

	        bytes[len++] = nextByte;
	    }

	@Override
	/**
	 * this function translates (encodes) TFTPMsg constructed process in protocol to array of bytes
	 * the array will be send to client as a response to request
	 */
	public byte[] encode(TFTPMsg message) { // convert msg to bytes
		LinkedList <Byte> listOfBytes = new LinkedList<Byte>();

		switch (message.getOpCode()){
			case (1):  //RRQ | Opcode | Filename | 0 | (2 bytes string 1 byte)
				convertShort(message.getOpCode(), listOfBytes);
				convertString(((RRQMsg)message).getFileName(), listOfBytes);
				listOfBytes.addLast((byte)'\0');
			break;
			
			case (2): //WRQ | Opcode | Filename | 0 | (2 bytes string 1 byte)
				convertShort(message.getOpCode(), listOfBytes);
				convertString(((WRQMsg)message).getFileName(), listOfBytes);
				listOfBytes.addLast((byte)'\0');	
			break;
			
			case (3): //DATA : Opcode | Packet Size | Block # | Data (2 bytes 2 bytes 2 bytes n bytes)
				convertShort(message.getOpCode(), listOfBytes);
				convertShort(((DATAMsg)message).getPacketSize(), listOfBytes);
				convertShort(((DATAMsg)message).getBlockNum(), listOfBytes);
				byte[] data = ((DATAMsg)message).getData();
				for (int i = 0 ; i < data.length ; i++) { //copy array of bytes to list
					listOfBytes.add(data[i]);
				}
			break;
			
			case (4): //ACK | Opcode | Block # | (2 bytes 2 bytes)
                convertShort(message.getOpCode(),listOfBytes);
                convertShort(((ACKMsg)message).getBlockNum(),listOfBytes);
                //bytes = returnByteArr(listOfBytes);
                break;
            
			case (5): //ERROR | Opcode | ErrorCode | ErrMsg | 0 |
                convertShort(message.getOpCode(),listOfBytes);
                convertShort(((ERRMsg)message).getErrorCode(),listOfBytes);
                convertString(((ERRMsg)message).getError(),listOfBytes);
                listOfBytes.addLast((byte)'\0'); //adds '\0' in the end
                //bytes = returnByteArr(listOfBytes);
                break;
			
			case (6): //DIRQ |opcode| (2 bytes)
				convertShort(message.getOpCode(), listOfBytes);	
			break;
		
			case (7): //LOGRQ | Opcode | userName | 0 (2 bytes string 1 byte)
				convertShort(message.getOpCode(), listOfBytes);
				convertString(((LOGRQMsg)message).getUserName(), listOfBytes);
				listOfBytes.addLast((byte)'\0');  //adds '\0' in the end
			break;
		       case 8: //DELRQ | Opcode | Filename | 0 | (2 bytes string 1 byte)
	                convertShort(message.getOpCode(),listOfBytes);
	                convertString(((DELRQMsg)message).getFileName(),listOfBytes);
	                listOfBytes.addLast((byte)'\0');  //adds '\0' in the end
	              //  bytes = returnByteArr(listOfBytes);
	                break;
	            case 9: //BCAST | Opcode | Deleted/Added | Filename | 0 | (2 bytes 1 byte string 1 byte)
	                convertShort(message.getOpCode(),listOfBytes);
	               listOfBytes.addLast((byte)(((BCASTMsg)message).getDeleteOrAdd()?1:0));
	                convertString(((BCASTMsg)message).getFileName(),listOfBytes);
	                listOfBytes.addLast((byte)'\0');  //adds '\0' in the end
	                //bytes = returnByteArr(listOfBytes);
	                break;
			case(10): //DISC |Opcode|
				convertShort(message.getOpCode(), listOfBytes);
			break;
			

		}
		this.resetFields(); //reset the fields before end of use in encDec
		return returnByteArr(listOfBytes);
		
	}
    /**
     * this function converts shorts to bytes
     * @param toConvert
     * @param toAddTo
     */
	private void convertShort(short toConvert,LinkedList<Byte> toAddTo){ 
		
		toAddTo.addLast((byte)((toConvert >> 8) & 0xFF));
		toAddTo.addLast((byte)(toConvert & 0xFF));	
		
	}
	
	/**
	 * this function converts string to bytes
	 * @param msg
	 * @param toAddTo
	 */
	private void convertString(String msg,LinkedList<Byte> toAddTo){ 
		byte[] toAddList = msg.getBytes();
		
		for(int i = 0 ; i < toAddList.length ; i++ )
			toAddTo.addLast(toAddList[i]);
	}
	
	/**
	 * this function returns array of bytes
	 * @param toConvert
	 * @return
	 */
    private byte[] returnByteArr(LinkedList<Byte>toConvert){ //converts the listOfBytes to byte array.
		byte[] toReturn = new byte[toConvert.size()];
		for(int i = 0 ; i < toConvert.size() ; i++ )
			toReturn[i] = (byte)toConvert.get(i);
		return toReturn;
    }
	/**
	 * this function resets all the fields 
	 */
    private void resetFields(){
    	this.opCode= -1;
    	this.packetSize = -1;
    	this.blockNum = -1;
    	this.bytes = new byte[1 << 10]; //start with 1k
    	this.len = 0;
    	this.errorCode=-1;
    	this.broadCast = null;
    	
    }
}






