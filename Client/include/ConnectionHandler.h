#ifndef CONNECTION_HANDLER__
#define CONNECTION_HANDLER__
#include <string>
#include <iostream>
#include <boost/asio.hpp>
#include <sstream>
#include <vector>
#include <cstring>
#include <fstream>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>


using boost::asio::ip::tcp;
using namespace std;
class ConnectionHandler {
private:
	const std::string host_;
	const short port_;
	boost::asio::io_service io_service_;   // Provides core I/O functionality
	tcp::socket socket_;
    short opCode;
    bool askToDisconnect;
    bool disconnectComplete;
    bool ifRRQ;
    string lastFileName;
    std::fstream fileToReadFrom;
    std::fstream fileToWrite;

 
public:
    ConnectionHandler(std::string host, short port);

    virtual ~ConnectionHandler();
 
    // Connect to the remote machine
    bool connect();
 
    // Read a fixed number of bytes from the server - blocking.
    // Returns false in case the connection is closed before bytesToRead bytes can be read.
    bool getBytes(char bytes[], unsigned int bytesToRead);
 
	// Send a fixed number of bytes from the client - blocking.
    // Returns false in case the connection is closed before all the data is sent.
    bool sendBytes(const char bytes[], int bytesToWrite);
	
    // Read an ascii line from the server
    // Returns false in case connection closed before a newline can be read.
    bool getLine(std::string& line);
	
	// Send an ascii line from the server
    // Returns false in case connection closed before all the data is sent.
    bool sendLine(std::string& line);
 
    // Get Ascii data from the server until the delimiter character
    // Returns false in case connection closed before null can be read.
    bool getFrameAscii(std::string& frame, char delimiter);

    // Send a message to the remote host.
    // Returns false in case connection is closed before all the data is sent.
    bool sendFrameAscii(const std::string& frame, char delimiter);
	
    // Close down the connection properly.
    void close();

    //decodes a data packet from the server
    //returns true if the decoding sccu
    bool decode();

    //Encodes the user input into data packets
    char* encode(string msg);

    bool getDisconnectComplete();
private:

    //Split the string
    void split(const std::string &s, char delim, std::vector<std::string> &elems);

    //This method calls another function to split a string and put it in a data structure
    std::vector<std::string> split(const std::string &s, char delim);

    //convert an array of chars and return the short value of it
    short bytesToShort(char* bytesArr);

    //gets an empty array and a short number and fills the array with it
    void shortToBytes(short num, char* bytesArr);

    //checks if the file exists
    bool fileExists(const string &filename);

}; //class ConnectionHandler
 
#endif