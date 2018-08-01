
import java.nio.charset.StandardCharsets;

import bgu.spl171.net.api.MessageEncoderDecoderImpl;
import bgu.spl171.net.api.bidi.RRQMsg;
import bgu.spl171.net.api.bidi.DISCMsg;
import bgu.spl171.net.api.bidi.TFTPMsg;
import bgu.spl171.net.api.bidi.DATAMsg;
import bgu.spl171.net.api.bidi.WRQMsg;
import bgu.spl171.net.api.bidi.ACKMsg;
import bgu.spl171.net.api.bidi.ERRMsg;
import bgu.spl171.net.api.bidi.DIRQMsg;
import bgu.spl171.net.api.bidi.LOGRQMsg;
import bgu.spl171.net.api.bidi.DELRQMsg;
import bgu.spl171.net.api.bidi.BCASTMsg;

 
public class EncDecTest {
	public static void main (String args[]){
	
	MessageEncoderDecoderImpl encdec = new MessageEncoderDecoderImpl();

 /* * * * * * * * * * * * * * * * * * * * * * * * * * * 
                TESTING THE ENCODER DECODER
                /* * * * * * * * * * * * * * * * * * * * * * * * * * * */

 // Instructions: 1. Change the names I used for your names.
 //               2. Import the thing you need
 //               3. Remove the "//" from the packet you want to test, and run it.
 //                  You can activate decode and then encode in order to see that you receive the same output as you started.
 //               *. Some of the tests are not relevant - You need to encode just: data, ack, bcast, and error. 
 //testRRQDecode(encdec); // 1
  // testWRQDecode(encdec); // 2
    testDataDecode(encdec); // 3
	//testDataEncode(encdec); // 3
	//testACKDecode(encdec); // 4
//	testACKEncode(encdec); // 4
//	testErrorDecode(encdec); // 5
//	testErrorEncode(encdec); // 5
	//testDIRQDecode(encdec); // 6
 //	testLOGRQDecode(encdec); // 7
// 	testDELRQDecode(encdec); // 8
 //	testBCastDecode(encdec); // 9
//	testBCastEncode(encdec); // 9
	//testDISCDecode(encdec); // 10
// testBIGDataDecode(encdec);
 //testBIGDataEncode(encdec);
   
	}
//
	public static void testBIGDataDecode (MessageEncoderDecoderImpl encdec){
		byte[] bigData = new byte[567];
		for (int i=0; i<bigData.length;i++)
			bigData[i]=(byte)i;
		System.out.println("FLAG Data");
		byte[] b = {0,3,2,55,1,5}; //
		byte[] append = new byte[b.length+bigData.length];
		for (int i=0; i<b.length; i++)
			append[i]=b[i];
		for (int i=b.length; i<append.length; i++)
			append[i]=bigData[i-b.length];
		System.out.println("FLAG Append");
		// bytesToShort({2,55})=(short)567 - The packetSize, bytesToShort({1,5})=(short)261 - The blockNum
		TFTPMsg res=null;
		System.out.println("Before decoding, the Arr is");
		printArr(append);
		for (int i=0; i<append.length; i++)
			res=encdec.decodeNextByte(append[i]);
		short opcode=((DATAMsg)res).getOpCode();
		short packetSize=((DATAMsg)res).getPacketSize();
		short blockNum=((DATAMsg)res).getBlockNum();
		byte[] dataBytes=((DATAMsg)res).getData();
		System.out.println("After decoding the arr, we've got a packet!");
		System.out.println("The opcode is " + opcode + " The packetSize is " + packetSize +"  and the blockNum is " + blockNum);
		System.out.println("The data is ");
		printArr(dataBytes);
	}

	public static void testBIGDataEncode (MessageEncoderDecoderImpl encdec){
		byte[] bigData = new byte[567];
		for (int i=0; i<bigData.length;i++)
			bigData[i]=(byte)i;
			System.out.println("FLAG Data");
		TFTPMsg packet = new DATAMsg(((short)3), ((short)255), ((short)261), bigData);
		byte[] res = encdec.encode(packet);
		System.out.println("Encoding the packet " + packet.getOpCode() + " is the Opcode "+ ((DATAMsg)packet).getPacketSize() + " is the packetSize " +((DATAMsg) packet).getBlockNum() + " is the Block Num " );
		System.out.println("The data arr is " );
		printArr(bigData);
		System.out.print("Output: ");

		printArr(res);
		System.out.println("The output should be... long");
	}


	public static void testDataDecode (MessageEncoderDecoderImpl encdec){
		byte[] b = {0,3,0,5,1,5,1,2,3,4,5}; // 0,5 is the packetSize(5), 1,5 is the blockNum(261)
		// bytesToShort({0,5})=(short)5, bytesToShort({1,5})=(short)261
		TFTPMsg res=null;
		System.out.println("Before decoding, the Arr is");
		printArr(b);
		for (int i=0; i<b.length; i++)
			res=encdec.decodeNextByte(b[i]);
		short opcode=((DATAMsg)res).getOpCode();
		short packetSize=((DATAMsg)res).getPacketSize();
		short blockNum=((DATAMsg)res).getBlockNum();
		byte[] dataBytes=((DATAMsg)res).getData();
		System.out.println("After decoding the arr, we've got a packet!");
		System.out.println("The opcode is " + opcode + " The packetSize is " + packetSize +"  and the blockNum is " + blockNum);
		System.out.println("The data is ");
		printArr(dataBytes);
	}
	
	public static void testDataEncode (MessageEncoderDecoderImpl encdec){
		byte[] b = {1,2,3,4,5};
		TFTPMsg packet = new DATAMsg(((short)3), ((short)5), ((short)261), b);
		byte[] res = encdec.encode(packet);
		System.out.println("Encoding the packet " + packet.getOpCode() + " is the Opcode "+ ((DATAMsg)packet).getPacketSize() + " is the packetSize " + ((DATAMsg)packet).getBlockNum() + " is the Block Num " );
		System.out.println("The data arr is " );
		printArr(b);
		System.out.print("Output: ");
	
		printArr(res); // Should be {0,3,0,5,1,5,1,2,3,4,5}
		System.out.println("The output should be {0,3,0,5,1,5,1,2,3,4,5}");
	}
	

	public static void testDISCDecode (MessageEncoderDecoderImpl encdec){
		byte[] b = {0,10}; 
		TFTPMsg res=null;
		System.out.println("Before decoding, the Arr is");
		printArr(b);
		for (int i=0; i<b.length; i++)
			res=encdec.decodeNextByte(b[i]);
		short opcode=((TFTPMsg)res).getOpCode();
		System.out.println("After decoding the arr, we've got a packet!");
		System.out.println("The opcode is " + opcode);
	}
	
	public static void testDISCEncode (MessageEncoderDecoderImpl encdec){
		TFTPMsg packet = new DISCMsg((short)10);
		byte[] res = encdec.encode(packet);
		System.out.println("Encoding the packet " + packet.getOpCode() + " is the Opcode");
		System.out.print("Output: ");

		printArr(res); // Should be {0,10}
		System.out.println("The output should be {0,10}");
	}

	public static void testBCastDecode (MessageEncoderDecoderImpl encdec){
		byte[] b = {0,9,1,66,67,97,115,116,83,116,114,0}; 
		// popString({66,67,97,115,116,83,116,114})=(String)"BCastStr"
		TFTPMsg res=null;
		System.out.println("Before decoding, the Arr is");
		printArr(b);
		for (int i=0; i<b.length; i++)
			res=encdec.decodeNextByte(b[i]);
		short opcode=((TFTPMsg)res).getOpCode();
		boolean deleted_or_added=((BCASTMsg)res).getDeleteOrAdd();
		String Filename=((BCASTMsg)res).getFileName();
		System.out.println("After decoding the arr, we've got a packet!");
		System.out.println("The opcode is " + opcode + " the deleted_or_added is " + deleted_or_added +"  and the Filename is " + Filename);
	}
	
	public static void testBCastEncode (MessageEncoderDecoderImpl encdec){
		TFTPMsg packet = new BCASTMsg (((short)9), true, "BCastStr");
		byte[] res = encdec.encode(packet);
		System.out.println("Encoding the packet " + packet.getOpCode() + " is the Opcode " + ((BCASTMsg)packet).getDeleteOrAdd() + " is the deleted_or_added code " + ((BCASTMsg)packet).getFileName());
		System.out.print("Output: ");

		printArr(res); // Should be {0,9,1,66,67,97,115,116,83,116,114,0}
		System.out.println("The output should be {0,9,1,66,67,97,115,116,83,116,114,0}");
	}

	public static void testDELRQDecode (MessageEncoderDecoderImpl encdec){
		byte[] b = {0,8,68,97,110,97,0};
		TFTPMsg res=null;
		System.out.println("Before decoding, the Arr is");
		printArr(b);
		for (int i=0; i<b.length; i++)
			res=encdec.decodeNextByte(b[i]);
		short opcode= ((TFTPMsg)res).getOpCode();
		String fileName=((DELRQMsg)res).getFileName();
		System.out.println("After decoding the arr, we've got a packet!");
		System.out.println("The opcode is " + opcode +" and the fileName is " + fileName);
	}
	
	public static void testDELRQEncode (MessageEncoderDecoderImpl encdec){
		TFTPMsg packet = new DELRQMsg((short) 8, "Dana");
		byte[] res = encdec.encode(packet);
		System.out.println("Encoding the packet " + packet.getOpCode() + " Opcode " + ((DELRQMsg)packet).getFileName());
		System.out.print("Output: ");

		printArr(res); // Should be {0,8,68,97,110,97,0}
		System.out.println("The output should be {0,8,68,97,110,97,0}");
	}
	
	
	public static void testLOGRQDecode (MessageEncoderDecoderImpl encdec){
		byte[] b = {0,7,68,97,110,97,0};
		TFTPMsg res=null;
		System.out.println("Before decoding, the Arr is");
		printArr(b);
		for (int i=0; i<b.length; i++)
			res=encdec.decodeNextByte(b[i]);
		short opcode=((TFTPMsg)res).getOpCode();
		String userName=((LOGRQMsg)res).getUserName();
		System.out.println("After decoding the arr, we've got a packet!");
		System.out.println("The opcode is " + opcode +" and the userName is " + userName);
	}
	
	public static void testLOGRQEncode (MessageEncoderDecoderImpl encdec){
		TFTPMsg packet = new DELRQMsg((short) 7, "Dana");
		byte[] res = encdec.encode(packet);
		System.out.println("Encoding the packet " + packet.getOpCode() + " Opcode " + ((LOGRQMsg)packet).getUserName());
		System.out.print("Output: ");

		printArr(res); // Should be {0,7,68,97,110,97,0}
		System.out.println("The output should be {0,7,68,97,110,97,0}");
	}
	
	
	public static void testDIRQDecode (MessageEncoderDecoderImpl encdec){
		byte[] b = {0,6}; 
		TFTPMsg res=null;
		System.out.println("Before decoding, the Arr is");
		printArr(b);
		for (int i=0; i<b.length; i++)
			res=encdec.decodeNextByte(b[i]);
		short opcode=((TFTPMsg)res).getOpCode();
		System.out.println("After decoding the arr, we've got a packet!");
		System.out.println("The opcode is " + opcode);
	}
	
	public static void testDIRQEncode (MessageEncoderDecoderImpl encdec){
		TFTPMsg packet = new DIRQMsg((short)6);
		byte[] res = encdec.encode(packet);
		System.out.println("Encoding the packet " + packet.getOpCode() + " is the Opcode");
		System.out.print("Output: ");

		printArr(res); // Should be {0,6}
		System.out.println("The output should be {0,6}");
	}
	
 	
	public static void testErrorDecode (MessageEncoderDecoderImpl encdec){
		byte[] b = {0,5,14,20 ,69,114,114,111,114,32,75,97,112,97,114,97 ,0}; 
		// bytesToShort({14,20})=(short)3604, and popString({69,114,114,111,114,32,75,97,112,97,114,97})=(String)"Error Kapara"
		TFTPMsg res=null;
		System.out.println("Before decoding, the Arr is");
		printArr(b);
		for (int i=0; i<b.length; i++)
			res=encdec.decodeNextByte(b[i]);
		short opcode=((TFTPMsg)res).getOpCode();
		short errorCode=((ERRMsg)res).getErrorCode();
		String errorMsg=((ERRMsg)res).getError();
		System.out.println("After decoding the arr, we've got a packet!");
		System.out.println("The opcode is " + opcode + " The Error code is " + errorCode +"  and the error messege is " + errorMsg);
	}
	
	public static void testErrorEncode (MessageEncoderDecoderImpl encdec){
		TFTPMsg packet = new ERRMsg((short)5, (short)3604, "Error Kapara");
		byte[] res = encdec.encode(packet);
		System.out.println("Encoding the packet " + packet.getOpCode() + " is the Opcode " + ((ERRMsg)packet).getErrorCode() + " is the error code " + ((ERRMsg)packet).getError());
		System.out.print("Output: ");
	
		printArr(res); // Should be {0,5,14,20 ,69,114 ,114,111,114,32,75,97,112,97,114,97 ,0}
		System.out.println("The output should be {0,5,14,20,69,114,114,111,114,32,75,97,112,97,114,97,0}");
	}
	
	public static void testRRQDecode (MessageEncoderDecoderImpl encdec){
		byte[] b = {0,1,68,97,110,97,0};
		TFTPMsg res=null;
		System.out.println("Before decoding, the Arr is");
		printArr(b);
		for (int i=0; i<b.length; i++)
			res=encdec.decodeNextByte(b[i]);
		short opcode=((TFTPMsg)res).getOpCode();
		String fileName=((RRQMsg)res).getFileName();
		System.out.println("After decoding the arr, we've got a packet!");
		System.out.println("The opcode is " + opcode +" and the fileName is " + fileName);
	}
	
	public static void testRRQEncode (MessageEncoderDecoderImpl encdec){
		TFTPMsg packet = new RRQMsg((short) 1, "Dana");
		byte[] res = encdec.encode(packet);
		System.out.println("Encoding the packet " + packet.getOpCode() + " Opcode " + ((RRQMsg) packet).getFileName());
		System.out.print("Output: ");

		printArr(res); // Should be {0,1,68,97,110,97,0}
		System.out.println("The output should be {0,1,68,97,110,97,0}");
	}
	
	public static void testWRQDecode (MessageEncoderDecoderImpl encdec){
		byte[] b = {0,2,68,97,110,97,0};
		TFTPMsg res=null;
		System.out.println("Before decoding, the Arr is");
		printArr(b);
		for (int i=0; i<b.length; i++)
			res=encdec.decodeNextByte(b[i]);
		short opcode=((WRQMsg)res).getOpCode();
		String fileName=((WRQMsg)res).getFileName();
		System.out.println("After decoding the arr, we've got a packet!");
		System.out.println("The opcode is " + opcode +" and the fileName is " + fileName);
	}
	
	public static void testWRQEncode (MessageEncoderDecoderImpl encdec){
		TFTPMsg packet = new WRQMsg((short) 2, "Dana");
		byte[] res = encdec.encode(packet);
		System.out.println("Encoding the packet " + packet.getOpCode() + " Opcode " + ((WRQMsg)packet).getFileName());
		System.out.print("Output: ");

		printArr(res); // Should be {0,2,68,97,110,97,0}
		System.out.println("The output should be {0,2,68,97,110,97,0}");
	}
	
	public static void testACKDecode (MessageEncoderDecoderImpl encdec){
		byte[] b = {0,4,14,20}; // bytesToShort({14,20})=(short)3604
		TFTPMsg res=null;
		System.out.println("Before decoding, the Arr is");
		printArr(b);
		for (int i=0; i<b.length; i++)
			res=encdec.decodeNextByte(b[i]);
		short opcode=((ACKMsg)res).getOpCode();
		short blockNum=((ACKMsg)res).getBlockNum();
		System.out.println("After decoding the arr, we've got a packet!");
		System.out.println("The opcode is " + opcode +" and the blockNum is " + blockNum);
	}
	
	public static void testACKEncode (MessageEncoderDecoderImpl encdec){
		TFTPMsg packet = new ACKMsg((short) 4, ((short)3604)); // bytesToShort({14,20})=(short)3604
		byte[] res = encdec.encode(packet);
		System.out.println("Encoding the packet " + packet.getOpCode() + " Opcode " + ((ACKMsg)packet).getBlockNum());
		System.out.print("Output: ");

		printArr(res); // Should be {0,2,68,97,110,97,0}
		System.out.println("The output should be {0,4,14,20}");
	}
	
	
	public static void printArr(byte[] b){
	//	System.out.print("Output: ");
		for (int i=0; i<b.length; i++)
			System.out.print(b[i]+"-");
		System.out.println();
	}


    public static short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }
    
    public static byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }
    
    public static byte[] popTwoFirstBytes(byte[] dataToDecode){
		byte[] newDataToDecode= new byte[dataToDecode.length-2];
		for(int i=0; i<newDataToDecode.length;i++)
			newDataToDecode[i]=dataToDecode[i+2];
		return newDataToDecode;
    }
    
    
    public static String popString(byte[] bytes) {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
    	int len=bytes.length;
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
        return result;
    }
    
}