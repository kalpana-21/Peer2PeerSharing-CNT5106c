# Peer2PeerSharing-CNT5106c

## Group Name: Tharun Bhupathi's group

### Team Members:
+ Bhavana Aleti - 82433275
+ Kalpana Sathya Ponnada - 52461920
+ Tanuj Karuturi - 78056734
+ Tharun Bhupathi -83289089

## Execution Instructions:

1. Place all project files in a single folder on the CISE server.
2. Compile the project using the following command: make [We have a make file which will compile all the java files when the make command is given]
3. Run the StartRemotePeers class to initiate all peers and execute the entire program with the command : java StartRemotePeers
   Alternative way to start the peers is manually starting the program on all peer servers. The command to start manually would be as below respectively on six peer servers.
   java peerProcess 1001 
   java peerProcess 1002
   java peerProcess 1003 
   java peerProcess 1004
   java peerProcess 1005 
   java peerProcess 1006

## Source Files description:

* CommonConfigClass.java - This class takes the configuration parameters such as file name, chunk size, unchoking intervals, and the number of neighbors from the common config and stores them for future use.
* Constants.java - Contains constants and enums used in the project.
* LoggingClass.java - Utility Class to handle all the logs like tcp connection, neighbor list etc..
* FileHelper.java - utility class to parse and read contents of file.
* NeighborPeer.java - A peer class where each object represents a peer in a peer-to-peer network. Each object stores information about the peer such as its ID, host, port number, file possession status.
* PeerUtil.java - A utility file for all peer related things like creating folders folder all peers, dividing the file into chunks according to the specs in common config and then atlast combining the chucks into a file.
* peerProcess.java - The main java file where all the functionality is implemented. It handles the TCP handshake and establishes the socket connections with other peers and also the Bit Torrent protocols like choke, unchoke, have, inetrested etc..
* RemotePeerInfo.java - Used in StartRemotePeers
* StartRemotePeers.java - Remotely start all the peers.
