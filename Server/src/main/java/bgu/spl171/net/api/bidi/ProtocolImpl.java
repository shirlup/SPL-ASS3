package bgu.spl171.net.api.bidi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ProtocolImpl<T> implements BidiMessagingProtocol<TFTPMsg>{

	private static ConcurrentHashMap<Integer, String> mapOfOnlineClients = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String,Integer> filesInRead = new ConcurrentHashMap<>();	
	private boolean shouldTerminate;
	private int connectionId;
	private  ConnectionsImpl<TFTPMsg> connections;
	private FileInputStream input;
	private FileOutputStream output;
	private File path;
	private short RWOpCode;
	private byte [] toReturn ;
	


	@Override
	/**
	 *this function starts the protocol
	 */
	public void start(int connectionId, Connections<TFTPMsg> connections) {
		this.connectionId = connectionId;
		this.connections = (ConnectionsImpl<TFTPMsg>) connections;
		this.shouldTerminate = false;
		this.path = null;
		this.RWOpCode = -1;
		this.toReturn = null;
	}
	public boolean getShouldTerminate(){
		return shouldTerminate;
	}

	@Override
	/**
	 * this function gets TFTPMsg after it was decoded and processes the request
	 */
	public void process(TFTPMsg message) {
		switch(message.getOpCode()){

		case(1): //RRQ | Opcode | Filename | 0 | (2 bytes string 1 byte) 
			if (!(isAlreadyOnline(connectionId))) userNotLoggedInErr(); // user not logged in
			else{	
				RWOpCode = message.getOpCode(); //update field
				String fileName = (((RRQMsg)message).getFileName());
				if(!(filesInRead.containsKey(fileName))) {
					filesInRead.put(fileName, 1);
				}
				else{ 
					int value = filesInRead.get(fileName);
					filesInRead.replace(fileName, value+1);	//increase counter of number of clients reading current file	
				}
				connections.send(connectionId, readFromFile(fileName,(short)0));
			}
		break;

		case(2)://WRQ | Opcode | Filename | 0 | (2 bytes string 1 byte)

		if (!(isAlreadyOnline(connectionId))) userNotLoggedInErr(); // user not logged in
		else {
			RWOpCode = message.getOpCode(); //updste fields
			String fileName1 = (((WRQMsg)message).getFileName());
			//now we need to check if server already has the file in one of 2 directories
			this.path = new File (System.getProperty("user.dir") + File.separator + "Files" + File.separator + fileName1); 
			
			if(path.exists()){ //Files directory
				TFTPMsg msg = new ERRMsg((short)5,(short) 5, "File already exists – File name exists on WRQ.");
				connections.send(connectionId, msg);
			}
			else { //Temp Directory
				path = new File(System.getProperty("user.dir") + File.separator + "Temp" + File.separator+ fileName1); 
				if(path.exists()){
					TFTPMsg msg = new ERRMsg((short)5,(short) 5, "File already exists – File name exists on WRQ.");
					connections.send(connectionId, msg);
				}
				else { //file do not exist ! should be written to Temp directory
					try { 
						path.createNewFile();
						output = new FileOutputStream(path);//creating the new file (empty) with the required name 
					} catch (IOException e) {}

					TFTPMsg msg = new ACKMsg((short) 4, (short) 0); //ACK
					connections.send(connectionId, msg);
				}
			}
		}
		break;

		case(3): //DATA : Opcode | Packet Size | Block # | Data (2 bytes 2 bytes 2 bytes n bytes)
		if (!(isAlreadyOnline(connectionId))) userNotLoggedInErr(); // user not logged in
		else {	
			if (RWOpCode != 2){ //the user did not asked to write before sending DATA Packet
				TFTPMsg msg = new ERRMsg((short)5,(short) 2, "Access violation – File cannot be written, read or deleted.");
				connections.send(connectionId, msg);
			
			}
			else{ //the user got permission to write 
				TFTPMsg msg = writeToFile(((DATAMsg)message).getData(),((DATAMsg)message).getBlockNum(),((DATAMsg)message).getPacketSize());
				connections.send(connectionId, msg);
			}
		}
		break;

		case (4): //ACK | Opcode | Block # | (2 bytes 2 bytes) // client sends ACK only in RRQ
			
			//  if client sends ACK it means that he is waiting for more DATA Packets (RRQ or DIRQ)
			if (!(isAlreadyOnline(connectionId))) userNotLoggedInErr(); // user not logged in
			else { //if the last request was RRQ
				if(RWOpCode == 1){
					if(path.length()-(((ACKMsg)message).getBlockNum()*512) > 0 )
						connections.send(connectionId, readFromFile(path.getName(),((ACKMsg)message).getBlockNum()));
				}
				else if(RWOpCode == 6){ //if last request was DIRQ
					if(toReturn.length-(((ACKMsg)message).getBlockNum()*512) > 0 ){ // if there are more bytes to read
						int size = toReturn.length-(((ACKMsg)message).getBlockNum()*512);
						if (size>512){ 
							byte [] toReturn1 = new byte[512];
							for (int j =0 ; j<512 ; j++){
								toReturn1[j]= toReturn[j+(((ACKMsg)message).getBlockNum()*512)];
							}			
						} 
						else{
							byte [] toReturn1 = new byte[size]; //create array of bytes
							for (int j =0 ; j<size ; j++){
								toReturn1[j]= toReturn[j+(((ACKMsg)message).getBlockNum()*512)];
							}
						}
					}
				}

				else connections.send(connectionId, new ERRMsg((short) 5, (short) 2, "Access violation"));
			}
		break;
		 
		//error msg is sent in 2 cases - server to server OR client to server 
		case (5): //ERROR | Opcode | ErrorCode | ErrMsg | 0 |
			if (!(isAlreadyOnline(connectionId))) userNotLoggedInErr(); // user not logged in
			else {		
				short errorCode = ((ERRMsg)message).getErrorCode();
				if (errorCode == 4) {
					connections.send(connectionId, message);
				}
				else {
					if (output!=null){
						try {
							Files.delete(path.toPath());
							output.close();
						} catch (IOException e) {}
						output= null;
					}
				}
			}
		break;

		case(6): //DIRQ
			if (!(isAlreadyOnline(connectionId))) userNotLoggedInErr(); // user not logged in
			else {	
				RWOpCode = message.getOpCode(); //update fields
				ConcurrentLinkedDeque<Byte> bytesOfNames = new ConcurrentLinkedDeque<Byte>();
				path = new File (System.getProperty("user.dir") + File.separator + "Files");
				File[] list =path.listFiles();
				for (File file : list) { //get string name
					byte[] filebytes = file.getName().getBytes();
					for(int i = 0; i<filebytes.length; i++){ 
						bytesOfNames.addLast(filebytes[i]); //put the bytes one by one in list
					}
					bytesOfNames.addLast((byte)'\0');
				}	
				int size = bytesOfNames.size();
				if (size>512){ 
					toReturn = new byte[512];
					for (int j =0 ; j<512 ; j++){
						toReturn[j]= bytesOfNames.getFirst();
					}			
				}
				else{
					toReturn = new byte[size]; //create array of bytes
					for (int j =0 ; j<size ; j++){
						toReturn[j]= bytesOfNames.pollFirst();
					}
				}
				TFTPMsg msg2Return= new DATAMsg((short)3, (short)toReturn.length, (short)1, toReturn);
				connections.send(connectionId,msg2Return);
			}
		break;
		case(7): //LOGRQ | Opcode | Username | 0 (2 bytes string 1 byte)
			String userName = ((LOGRQMsg)message).getUserName();
			if(nameAlreadyExist(userName) == true || isAlreadyOnline(connectionId)){  //userName already exists
				TFTPMsg msg = new ERRMsg((short)5,(short)7 , "User already logged in");
				connections.send(connectionId, msg);
				
			}
			else { //userName is valid
				mapOfOnlineClients.put(connectionId,userName);
				TFTPMsg msg = new ACKMsg((short)4,(short)0);
				connections.send(connectionId, msg);
					
			}

		break;	

		case(8): //DELRQ | Opcode | Filename | 0 | (2 bytes string 1 byte)
			if (!(isAlreadyOnline(connectionId))) userNotLoggedInErr(); // user not logged in
			else {
				String fileName = ((DELRQMsg) message).getFileName();
				path = new File(System.getProperty("user.dir") + File.separator + "Files"+ File.separator + fileName);
				if(filesInRead.containsKey(fileName)) { //if the file is being read by other clients the DELRQ is not allowed
					TFTPMsg msg = new ERRMsg((short)5,(short)2 , "Access violation");
					connections.send(connectionId, msg);	
				}
				
				else if (path.exists()){//if the file exists
					
					try {
						Files.delete(path.toPath());
					} catch (IOException e) { }
					broadcast(new BCASTMsg((short)9,false,path.getName()));

				}
				else{  
					
					TFTPMsg msg = new ERRMsg((short) 5, (short) 2, "Access violation");
					(connections).send(connectionId, msg);	          	
				}
			}
		break;

		case(9):  //BCAST | Opcode | Deleted/Added | Filename | 0 | (2 bytes 1 byte string 1 byte) //
			//ILLEGAL - only server-client!!!!
			TFTPMsg msg = new ERRMsg((short)5,(short)0,"Not defined, see error message (if any).");
		connections.send(connectionId, msg);

		break;	

		case(10)://DISC
			if (isAlreadyOnline(connectionId) == false) { //the user is not logged in ! - illegal action
				userNotLoggedInErr();
			}
			else{
				TFTPMsg msg1 = new ACKMsg((short)4,(short) 0); //ACK
				connections.send(connectionId, msg1);

				try {
					if(output != null) output.flush(); 
					if(input != null )input.reset();
				} catch (IOException e) {}

				
				mapOfOnlineClients.remove(connectionId); //remove the client from online client's map
				connections.disconnect(connectionId); //this action closes the socket

			}

		break;

		default :
			if (!(isAlreadyOnline(connectionId))) userNotLoggedInErr(); // user not logged in
			else {
				TFTPMsg msg2 = new ERRMsg((short)5,(short)4,"Illegal TFTP operation – Unknown Opcode.");
				connections.send(connectionId, msg2);
				}
		}//end switch
	}	


	@Override
	public boolean shouldTerminate() {

		return false;
	}
	
	/**
	 * this function reads data from file (to be send to client)
	 * @param fileName
	 * @param acceptedBlockNum
	 * @return TFTPMag
	 */
	private TFTPMsg readFromFile(String fileName, short acceptedBlockNum){
		//check if the file currently being read by other clients 
	
		TFTPMsg dataMsg = null;
		
		int alreadyRead = acceptedBlockNum * 512; //indicates how many bytes was read;
		byte[] bytes;
		
		//Sending the first block of the file
		if ((path == null) || fileName != path.getName()) {  //checks if the open file is the requested file
			path = new File(System.getProperty("user.dir") + File.separator + "Files" +File.separator + fileName);
			if (!path.exists()) { 
				return new ERRMsg((short)5,(short)1,"File not found");
			}
		}
		if(path.exists()){ //Read other file
			try(FileInputStream stream = new FileInputStream(path)){
			if (path.length() - alreadyRead <= 512) { //if the file size is smaller or equal to 512
				bytes = new byte[(int) path.length() - alreadyRead];
				stream.read(bytes);
				int value = filesInRead.get(fileName);
			
				if(value>1){
					filesInRead.replace(fileName, value-1);	//if you are the last person reading the file
					
				}
				else {
					filesInRead.remove(fileName); //remove file name from map
				}
			
			
				dataMsg = new DATAMsg((short) 3, (short) bytes.length, (short) (acceptedBlockNum + 1), bytes); //blockNum increased by 1
			} 
			else { //the file size is bigger than 512
				bytes = new byte[512];
					stream.skip((long) (alreadyRead)); //skips the number of bytes that was already read
					stream.read(bytes);
				dataMsg = new DATAMsg((short) 3, (short) bytes.length, (short) (acceptedBlockNum + 1), bytes);//DATA
				
			}
			} catch (FileNotFoundException e) {}
				
			 catch (IOException e) {}
		}
		else
			return new ERRMsg((short)5,(short)1,"File not found"); //if the requested file do not exist

		return dataMsg;
	}
	/**
	 * this function accepts DATA bytes and creates a new file in Files directory
	 * @param data
	 * @param blockNum
	 * @param packetSize
	 * @return TFTPMag
	 */
	private TFTPMsg writeToFile(byte[] data, short blockNum , short packetSize){
		try{
			output.write(data); //writes data to outputStream
			output.flush();
		}
		catch (IOException e){}

		if(packetSize < 512) { //means it is the last packet that was send !! move to Files Directory from Temp
		
			File pathToMoveTo = new File(System.getProperty("user.dir") + File.separator + "Files" + File.separator + path.getName());
			
			try {
				
		
				Files.copy(path.toPath(), pathToMoveTo.toPath()); //copy the file to Files directory
				broadcast(new BCASTMsg((short)9,true,path.getName())); //BCAST all online clients
				
				output.close(); //close outputStream after use
			} catch (IOException e) {}
			path.delete(); //delete the file from Temp directory	
		}
		return new ACKMsg((short)4,blockNum); //ACK
	}
	/**
	 * this method broadcasts message to all online clients
	 * @param data
	 */
	private void broadcast(BCASTMsg data) {
		for(Integer id: mapOfOnlineClients.keySet()){
			connections.send(id , data);
		}

	}
	/**
	 * this function sends error to the client via connections
	 */
	private void userNotLoggedInErr (){
		//the user is not logged in ! - illegal action
		TFTPMsg msg = new ERRMsg((short)5,(short) 6, "User not logged in – Any opcode received before Login completes.");
		connections.send(connectionId, msg);
	}

	/**
	 * this function check if a client is already online, hence if exists in map containing all online clients
	 * @param id
	 * @return
	 */
	public boolean isAlreadyOnline(int id){
		boolean isOnline  = (mapOfOnlineClients.containsKey(id));
		return isOnline;
	}

/**
 * this function checks if the user name that was chosen by client is available
 * @param name
 * @return
 */
	public boolean nameAlreadyExist(String name){
		return (mapOfOnlineClients.containsValue(name));
	}


}//end ProtocolImpl






