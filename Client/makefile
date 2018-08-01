CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lboost_locale -lboost_thread
OFILES:=bin/ConnectionHandler.o bin/TFTPclient.o bin/CommandClient.o

all: TFTPclient
	g++ -o bin/TFTPclient $(OFILES) $(LDFLAGS)

TFTPclient: $(OFILES)

bin/CommandClient.o: src/CommandClient.cpp
	g++ $(CFLAGS) -o bin/CommandClient.o src/CommandClient.cpp
	
bin/TFTPclient.o: src/TFTPclient.cpp
	g++ $(CFLAGS) -o bin/TFTPclient.o src/TFTPclient.cpp
	
bin/ConnectionHandler.o: src/ConnectionHandler.cpp
	g++ $(CFLAGS) -o bin/ConnectionHandler.o src/ConnectionHandler.cpp
	

.PHONY: clean
clean:
	rm -f bin/*