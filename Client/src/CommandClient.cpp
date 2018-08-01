
#include <boost/thread.hpp>
#include <ConnectionHandler.h>

class CommandClient {
private:
    int _id;
    ConnectionHandler *handler;
    bool disc;
public:

    CommandClient(ConnectionHandler *handler) : handler(handler),disc(false) {}

    void readLine() {
        while (!(disc)) {
            const short bufsize = 1024;
            char buf[bufsize];
            std::cin.getline(buf, bufsize);
            std::string line(buf);
                if(line == "DISC") disc = true;
                handler->encode(line);
        }
    }
};

