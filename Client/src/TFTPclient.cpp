#include "CommandClient.cpp"
#include <stdlib.h>
#include <boost/thread.hpp>


/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);
    
    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
    CommandClient keyBoard(&connectionHandler);
    boost::thread thread(boost::bind(&CommandClient::readLine,&keyBoard));
	//From here we will see the rest of the ehco client implementation:
    while (!(connectionHandler.getDisconnectComplete())) {
        connectionHandler.decode();
    }

    thread.join();
    return 0;
}
