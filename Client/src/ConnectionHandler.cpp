#include <ConnectionHandler.h>
#include <boost/asio.hpp>
//#include <rpcndr.h>


using boost::asio::ip::tcp;
using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;

ConnectionHandler::ConnectionHandler(string host, short port): host_(host), port_(port), io_service_(),socket_(io_service_),askToDisconnect(false),ifRRQ(false),disconnectComplete(
        false){}

ConnectionHandler::~ConnectionHandler(){
    close();
}
bool ConnectionHandler::connect() {
    std::cout << "Starting connect to " 
        << host_ << ":" << port_ << std::endl;
    try {
		tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
		boost::system::error_code error;
		socket_.connect(endpoint, error);
		if (error)
			throw boost::system::system_error(error);
    }
    catch (std::exception& e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp ) {
			tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);			
        }
		if(error)
			throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {
			tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
		if(error)
			throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool ConnectionHandler::getLine(std::string& line) {
    return getFrameAscii(line, '\n');
}

bool ConnectionHandler::sendLine(std::string& line) {
    return sendFrameAscii(line, '\n');
}
 
bool ConnectionHandler::getFrameAscii(std::string& frame, char delimiter) {
    char ch;
    // Stop when we encounter the null character. 
    // Notice that the null character is not appended to the frame string.
    try {
		do{
			getBytes(&ch, 1);
            frame.append(1, ch);
        }while (delimiter != ch);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool ConnectionHandler::sendFrameAscii(const std::string& frame, char delimiter) {
	bool result=sendBytes(frame.c_str(),frame.length());
	if(!result) return false;
	return sendBytes(&delimiter,1);
}
 
// Close down the connection properly.
void ConnectionHandler::close() {
    try {
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}
/*
 * This method decodes an array of bytes from the server and handles each case
 */
bool ConnectionHandler::decode()  {
    char op[2];
    getBytes(op,2);
    if(op[1] == 3) { //Data Packet
        char packetSize[2];
        char blockNumber[2];
        if (!getBytes(packetSize, 2)) return false;
        if (!getBytes(blockNumber, 2)) return false;
        short shortBN = bytesToShort(blockNumber);
        short sizeOfPacket = bytesToShort(packetSize);
        if((shortBN == 1) && (ifRRQ)){ fileToWrite.open(lastFileName, ios::out); } //means the first block sent after RRQ
        char data[sizeOfPacket];
        if (!getBytes(data, sizeOfPacket)) return false;
        if (fileToWrite.is_open()) { // RRQ - get data to an open file
            fileToWrite.write(data, sizeOfPacket);
            if (sizeOfPacket < 512) {
                cout << "RRQ " << lastFileName << " complete" << endl;
                fileToWrite.close();
                ifRRQ = false;
            }
        }   else { //DIRQ - print to client's screen
            for(int i = 0 ; i < sizeOfPacket ; i++ ) {
                if(data[i] == '\0') cout << endl;
                else cout << data[i];
           }
        }

        //create ACK to return - 4 bytes
        char sendToServer[4];
        sendToServer[0] = 0;
        sendToServer[1] = 4;
        sendToServer[2] = blockNumber[0];
        sendToServer[3] = blockNumber[1];
        sendBytes(sendToServer, 4);
    }
    else if(op[1] == 4) { // ACK
        char blockNumber[2];
        if (!getBytes(blockNumber, 2)) { return false; }
        short prevShortBN = bytesToShort(blockNumber);
        short shortBN = prevShortBN + 1;
        shortToBytes(shortBN, blockNumber);
        cout << "ACK " << prevShortBN << endl;
        if (fileToReadFrom.is_open()) { //means the client did a write request and the file exists in his computer
            char packetSize[2];
            char c;
            vector<char> data;
            int i = 0;
            while (i < 512 && fileToReadFrom.get(c)) {
                data.push_back(c);
                i++;
            }
            char sendToServer[6+data.size()];
            sendToServer[0] = 0;
            sendToServer[1] = 3;
            short shortPS = (short)i;
            shortToBytes(shortPS, packetSize);
            sendToServer[2] = packetSize[0];
            sendToServer[3] = packetSize[1];
            sendToServer[4] = blockNumber[0];
            sendToServer[5] = blockNumber[1];
            for (i = 6; i < data.size()+6 ; i++) sendToServer[i] = data[i - 6];
            sendBytes(sendToServer, 6 + data.size());
            if(shortPS < 512) {
                fileToReadFrom.close();
                cout << "WRQ " << lastFileName << " complete" << endl;
            }
        } else if (askToDisconnect) {//the client wants to disconnect
           disconnectComplete = true;
        } else return true;
    }
    else if(op[1] == 5) { //Error message from the server
        char errorCode[2];
        getBytes(errorCode, 2);
        short errCo = bytesToShort(errorCode);
        cout << "ERROR " << errCo << endl;
        string errorMessage;
        return getFrameAscii(errorMessage, '\0');
    }
    else if(op[1]== 9) {//BROADCAST
        char deletedOrAdded[1];
        if (!getBytes(deletedOrAdded, 1)) { return false; }
        int delAdd;
        if(deletedOrAdded[0] == true) delAdd = 1;
        else delAdd = 0;
        string fileNameToBe = "";
        getFrameAscii(fileNameToBe, '\0');
        if (delAdd == 0) cout << "BCAST " << "deleted " << fileNameToBe << endl;
        else cout << "BCAST" << " added " << fileNameToBe << endl;
    }

}
    /*
    * This method recives a string from the keyboard and handles the client requests
    */
    char *ConnectionHandler::encode(string msg) {
        vector<std::string> splitting = ConnectionHandler::split(msg, ' ');//the vector represents the keyboard request
        vector<char> toReturn; //the char container for the msg that should be returned
        if(splitting.empty()){
            cout << endl;
            return NULL;
        }
        string toMatch = splitting.at(0);
        if (toMatch == "RRQ") {
            if (splitting.size() != 2) { //not the correct packet
                cout << "INVALID_INPUT" << endl;
                return NULL;
            }
            toReturn.push_back(0);
            toReturn.push_back(1);
            string fileName = splitting.at(1).c_str();
            if ((fileExists(fileName))){
                cout << "FILE NAME ALREADY EXIST" << endl;
                return NULL;
            } else{
                lastFileName = fileName;
                ifRRQ = true;
            }
            for (unsigned int i = 0; i < fileName.length(); i++) {
                if (fileName.at(i) == '\0') {
                    cout << "INVALID_INPUT" << endl;
                    return NULL;
                } else {

                    toReturn.push_back(fileName.at(i));
                }
            }
            toReturn.push_back('\0');

        } else if (toMatch == "WRQ") {
            if (splitting.size() != 2) { //not the correct packet
                cout << "INVALID_INPUT" << endl;
                return NULL;
            }
            toReturn.push_back(0);
            toReturn.push_back(2);
            string fileName = splitting.at(1).c_str();
            if ((fileExists(fileName))) {
                fileToReadFrom.open(fileName, ios::in);
                lastFileName = fileName;
            }
            else {
                cout << "CANNOT SEND FILE : NOT EXISTING" << endl;
                return NULL;
            }

            for (unsigned int i = 0; i < fileName.length(); i++) {
                if (fileName.at(i) == '\0') {
                    cout << "INVALID_INPUT" << endl;
                    return NULL;
                }
                toReturn.push_back(fileName.at(i));
            }

            toReturn.push_back('\0');

        } else if (toMatch == "DIRQ") {
            if (splitting.size() != 1) { //not the correct packet
                cout << "INVALID_INPUT" << endl;
                return NULL;
            } else {
                toReturn.push_back(0);
                toReturn.push_back(6);
                ifRRQ = false;
            }

        } else if (toMatch == "DELRQ") {
            if (splitting.size() != 2) { //not the correct packet
                cout << "INVALID_INPUT" << endl;
                return NULL;
            }
            toReturn.push_back(0);
            toReturn.push_back(8);
            string fileName = splitting.at(1).c_str();
            for (unsigned int i = 0; i < fileName.length(); i++)
                toReturn.push_back(fileName.at(i));
            toReturn.push_back('\0');
        } else if (toMatch == "LOGRQ") {
            if (splitting.size() != 2) { //not the correct packet
                cout << "INVALID_INPUT" << endl;
                return NULL;
            }
            toReturn.push_back(0);
            toReturn.push_back(7);
            string userName = splitting.at(1).c_str();
            for (unsigned int i = 0; i < userName.length(); i++)
                toReturn.push_back(userName.at(i));
            toReturn.push_back('\0');
        } else if(toMatch == "DISC"){
            if (splitting.size() != 1) { //not the correct packet
                cout << "INVALID_INPUT" << endl;
                return NULL;
            }
            toReturn.push_back(0);
            toReturn.push_back(10);
            askToDisconnect = true;
        }

        else {
            cout << endl;
            return NULL;
        }

        char bytes[toReturn.size()];
        for (unsigned int i = 0; i < toReturn.size(); i++) {
            bytes[i] = toReturn[i];
        }
        sendBytes(bytes,toReturn.size());
    }

    short ConnectionHandler::bytesToShort(char* bytesArr) {
        short result = (short)((bytesArr[0] & 0xff) << 8);
        result += (short)(bytesArr[1] & 0xff);
        return result;
    }
    void ConnectionHandler::split(const std::string &s, char delim, std::vector<std::string> &elems) {
        std::stringstream ss;
        ss.str(s);
        std::string item;
        while (std::getline(ss, item, delim)) {
            elems.push_back(item);
        }
    }
    vector<string> ConnectionHandler::split(const std::string &s, char delim) {
        std::vector<std::string> elems;
        split(s, delim, elems);
        return elems;
    }

    void ConnectionHandler::shortToBytes(short num, char* bytesArr) {
        bytesArr[0] = ((num >> 8) & 0xFF);
        bytesArr[1] = (num & 0xFF);
    }

    bool ConnectionHandler::fileExists(const string &filename) {
    struct stat buf;
    if (stat(filename.c_str(), &buf) != -1) {
        return true;
    }
    return false;

}

bool ConnectionHandler:: getDisconnectComplete(){
    return disconnectComplete;
}