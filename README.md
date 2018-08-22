# TFTP-SERVER
Implemention of an extended TFTP (Trivial File Transfer Protocol) server and client. 
The implementation of the server will be based on the Reactor and TPC.
This version will require a user to perform a passwordless server login as well as enable the
server to communicate broadcast messages to all users and support for directory listings. 

The commands are defined by an opcode that describes
the incoming command. For each command, a different length of data needs to be read
according to it’s specifications. 

## opcode operation
```
 1 Read request (RRQ)
 2 Write request (WRQ)
 3 Data (DATA)
 4 Acknowledgment (ACK)
 5 Error (ERROR)
 6 Directory listing request (DIRQ)
 7 Login request (LOGRQ)
 8 Delete request (DELRQ)
 9 Broadcast file added/deleted (BCAST)
 10 Disconnect (DISC)
 ```
 
 ## Testing run commands

Reactor server:
```
mvn exec:java -Dexec.mainClass=”bgu.spl171.net.impl.TFTPreactor.ReactorMain” -
Dexec.args=”<port>”
```

Thread per client server:
```
mvn exec:java -Dexec.mainClass=”bgu.spl171.net.impl.TFTPtpc.TPCMain” -
Dexec.args=”<port>”
```
