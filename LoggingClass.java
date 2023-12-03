import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
/**
 * Class for generating logs in  P2P file sharing system.
 */
public class LoggingClass {
	File file = null;
	BufferedWriter logger = null;

	// Constructor for the Logs class
	public LoggingClass(File file) {
		// Using a try block to handle potential IOExceptions
		try {
			// Assigning the provided file to the class's file field
			this.file = file;

			// Initializing 'logger' as a BufferedWriter
			// FileWriter is set to append mode (true), so new data will be added
			// to the end of the file rather than overwriting it
			logger = new BufferedWriter(new FileWriter(file.getAbsolutePath(), true));
		}
		// Catch block to handle IOExceptions
		catch(IOException ex){
			// Printing the stack trace of the exception to the standard error stream
			// This helps in diagnosing the reason for the exception
			ex.printStackTrace();
		}
	}


	/**
	 * Logs the establishment of a TCP connection from one peer to another.
	 * This method constructs a log message including the current timestamp
	 * and the IDs of both the initiating and receiving peers.
	 *
	 * @param peerId1 The ID of the peer that initiates the TCP connection.
	 * @param peerId2 The ID of the peer to which the connection is made.
	 */
	public synchronized void logForTcpConnectionTo(int peerId1, int peerId2){

		// StringBuilder to efficiently construct the log message
		StringBuilder logRecord = new StringBuilder();

		// Generating a timestamp in a specific format
		String timeStamp = new SimpleDateFormat("y-M-d 'at' h:m:s a z").format(Calendar.getInstance().getTime());
		// Appending the timestamp and a message indicating the TCP connection
		logRecord.append("["+timeStamp + "]: Peer [" + peerId1 + "] makes a connection to Peer [" + peerId2 + "].");

		try {
			// Calling logHelper method to handle the actual logging
			logHelper(logRecord);
		} catch (Exception e) {
			// Printing the stack trace of any exceptions for debugging purposes
			e.printStackTrace();
		}
	}

	/**
	 * Logs the event of a TCP connection being established from one peer to another.
	 * This method constructs a log entry indicating that the specified peer (peerId1)
	 * has received an incoming TCP connection from another peer (peerId2).
	 *
	 * @param peerId1 The ID of the peer that received the incoming connection.
	 * @param peerId2 The ID of the peer from which the connection originated.
	 */
	public synchronized void logForTcpConnectionFrom(int peerId1, int peerId2){
		StringBuilder logRecord = new StringBuilder();
		String timeStamp = new SimpleDateFormat("y-M-d 'at' h:m:s a z").format(Calendar.getInstance().getTime());
		logRecord.append("["+timeStamp + "]: Peer [" + peerId1 + "] is connected from Peer [" + peerId2 + "].");
		try {
			// Calling logHelper method to perform the actual logging
			logHelper(logRecord);
		} catch (Exception e) {
			// Printing the stack trace of any exceptions for debugging
			e.printStackTrace();
		}
	}


	/**
	 * Logs the event of a change in the preferred neighbors for a specific peer.
	 * This method constructs a log message that includes the current timestamp,
	 * the ID of the peer, and the updated list of preferred neighbor IDs.
	 *
	 * @param peerId       The ID of the peer whose preferred neighbors have changed.
	 * @param peerIdsList  An array of peer IDs that are now considered as preferred neighbors.
	 */
	public synchronized void logForChangeOfPreferredNeighbors(int peerId, int[] peerIdsList){
		// StringBuilder for efficient string concatenation
		StringBuilder logRecord = new StringBuilder();

		// Generating a timestamp in a specified format
		String timeStamp = new SimpleDateFormat("y-M-d 'at' h:m:s a z").format(Calendar.getInstance().getTime());
		// Starting the log record with the timestamp and peer ID
		logRecord.append("["+timeStamp + "]: Peer [" + peerId + "] has the preferred neighbors [");

		// StringBuilder to concatenate the peer IDs
		StringBuilder s = new StringBuilder();
		// Iterate over the peer IDs and append them to the StringBuilder
		for (int i : peerIdsList) {
			s.append(Integer.toString(i)).append(",");
		}
		// Remove the trailing comma and append the result to the log record
		String result = s.deleteCharAt(s.length() - 1).toString();
		logRecord.append(result);
		logRecord.append("].");

		try {
			// Call the logHelper method to handle the actual logging
			logHelper(logRecord);
		} catch (Exception e) {
			// Print the stack trace of any exceptions for debugging purposes
			e.printStackTrace();
		}
	}


	/**
	 * Logs the event of selecting an optimistically unchoked neighbor for a peer.
	 * This method constructs a log message that includes the current timestamp,
	 * the ID of the peer making the selection, and the ID of the peer chosen as
	 * the optimistically unchoked neighbor.
	 *
	 * @param peerId1 The ID of the peer making the selection.
	 * @param peerId2 The ID of the peer selected as the optimistically unchoked neighbor.
	 */
	public synchronized void logForChangeOfOptimisticallyUnchokedNeighbor(int peerId1, int peerId2){

		// StringBuilder for efficient string concatenation
		StringBuilder logRecord = new StringBuilder();

		// Generating a timestamp in a specified format
		String timeStamp = new SimpleDateFormat("y-M-d 'at' h:m:s a z").format(Calendar.getInstance().getTime());
		// Appending the timestamp and a message indicating the optimistically unchoked neighbor
		logRecord.append("["+timeStamp + "]: Peer [" + peerId1 + "] has the optimistically unchoked neighbor [" + peerId2 + "].");

		try {
			// Call the logHelper method to handle the actual logging
			logHelper(logRecord);
		}
		catch (Exception e) {
			// Print the stack trace of any exceptions for debugging purposes
			e.printStackTrace();
		}
	}


	/**
	 * Logs the event of a peer being unchoked by another peer.
	 * This method constructs a log message with the current timestamp,
	 * and the IDs of both the unchoked peer and the peer performing the unchoking.
	 *
	 * @param peerId1 The ID of the peer that is unchoked.
	 * @param peerId2 The ID of the peer that performs the unchoking.
	 */
	public synchronized void logForUnchoking(int peerId1, int peerId2){

		// StringBuilder for efficient string concatenation
		StringBuilder logRecord = new StringBuilder();

		// Generating a timestamp in a specified format
		String timeStamp = new SimpleDateFormat("y-M-d 'at' h:m:s a z").format(Calendar.getInstance().getTime());
		// Appending the timestamp and a message indicating the unchoking event
		logRecord.append("["+timeStamp + "]: Peer [" + peerId1 + "] is unchoked by [" + peerId2 + "].");

		try {
			// Call the logHelper method to handle the actual logging
			logHelper(logRecord);
		}
		catch (Exception e) {
			// Print the stack trace of any exceptions for debugging purposes
			e.printStackTrace();
		}
	}


	/**
	 * Logs the event of a peer being choked by another peer.
	 * This method constructs a log message that includes the current timestamp,
	 * and the IDs of both the choked peer and the peer performing the choking.
	 *
	 * @param peerId1 The ID of the peer that is choked.
	 * @param peerId2 The ID of the peer that performs the choking.
	 */
	public synchronized void logForChoking(int peerId1, int peerId2){

		// StringBuilder for efficient string concatenation
		StringBuilder logRecord = new StringBuilder();

		// Generating a timestamp in a specified format
		String timeStamp = new SimpleDateFormat("y-M-d 'at' h:m:s a z").format(Calendar.getInstance().getTime());
		// Appending the timestamp and a message indicating the choking event
		logRecord.append("["+timeStamp + "]: Peer [" + peerId1  +"] is choked by ["+ peerId2 +"].");

		try {
			// Call the logHelper method to handle the actual logging
			logHelper(logRecord);
		}
		catch (Exception e) {
			// Print the stack trace of any exceptions for debugging purposes
			e.printStackTrace();
		}
	}



	/**
	 * Logs the event of a peer sending a specific piece of data to another peer.
	 * This method constructs a log message that includes the current timestamp,
	 * the IDs of the sending and receiving peers, and the index of the piece
	 * being sent.
	 *
	 * @param peerId1 The ID of the peer sending the piece.
	 * @param peerId2 The ID of the peer receiving the piece.
	 * @param idx     The index of the piece being sent.
	 */
	public synchronized void logForSendPieceMessage(int peerId1, int peerId2, int idx){
		// StringBuilder for efficient string concatenation
		StringBuilder logRecord = new StringBuilder();

		// Generating a timestamp in a specified format
		String timeStamp = new SimpleDateFormat("y-M-d 'at' h:m:s a z").format(Calendar.getInstance().getTime());
		// Appending the timestamp and a message indicating the sending of the piece
		logRecord.append("["+timeStamp + "]: Peer [" + peerId1 +"] sent the 'piece' " + idx + " to Peer [" + peerId2+ "].");

		try {
			// Call the logHelper method to handle the actual logging
			logHelper(logRecord);
		}
		catch (Exception e) {
			// Print the stack trace of any exceptions for debugging purposes
			e.printStackTrace();
		}
	}


	/**
	 * Logs the event of a peer receiving a 'have' message from another peer.
	 * This method constructs a log message that includes the current timestamp,
	 * the IDs of both the receiving and sending peers, and the index of the piece
	 * being communicated in the 'have' message.
	 *
	 * @param peerId1 The ID of the peer receiving the 'have' message.
	 * @param peerId2 The ID of the peer sending the 'have' message.
	 * @param idx     The index of the piece that is the subject of the 'have' message.
	 */
	public synchronized void logForReceivingHaveMessage(int peerId1, int peerId2, int idx){
		// StringBuilder for efficient string concatenation
		StringBuilder logRecord = new StringBuilder();

		// Generating a timestamp in a specified format
		String timeStamp = new SimpleDateFormat("y-M-d 'at' h:m:s a z").format(Calendar.getInstance().getTime());
		// Appending the timestamp and a message indicating the receipt of the 'have' message
		logRecord.append("["+timeStamp + "]: Peer [" + peerId1 +"] received 'have' message from [" + peerId2+ "] for the piece: " + idx + ".");

		try {
			// Call the logHelper method to handle the actual logging
			logHelper(logRecord);
		}
		catch (Exception e) {
			// Print the stack trace of any exceptions for debugging purposes
			e.printStackTrace();
		}
	}


	/**
	 * Logs the event of a peer receiving an 'interested' message from another peer.
	 * This method constructs a log message that includes the current timestamp,
	 * and the IDs of both the receiving and sending peers. The message indicates
	 * that the sending peer is interested in the data held by the receiving peer.
	 *
	 * @param peerId1 The ID of the peer receiving the 'interested' message.
	 * @param peerId2 The ID of the peer sending the 'interested' message.
	 */
	public synchronized void logForReceivingInterestedMessage(int peerId1, int peerId2){

		// StringBuilder for efficient string concatenation
		StringBuilder logRecord = new StringBuilder();

		// Generating a timestamp in a specified format
		String timeStamp = new SimpleDateFormat("y-M-d 'at' h:m:s a z").format(Calendar.getInstance().getTime());
		// Appending the timestamp and a message indicating the receipt of the 'interested' message
		logRecord.append("["+timeStamp + "]: Peer [" + peerId1 + "] received the 'interested' message from [" + peerId2 + "].");

		try {
			// Call the logHelper method to handle the actual logging
			logHelper(logRecord);
		}
		catch (Exception e) {
			// Print the stack trace of any exceptions for debugging purposes
			e.printStackTrace();
		}

	}


	/**
	 * Logs the event of a peer receiving a 'not interested' message from another peer.
	 * This method constructs a log message that includes the current timestamp,
	 * and the IDs of both the receiving and sending peers. The message indicates
	 * that the sending peer is not interested in the data held by the receiving peer.
	 *
	 * @param peerId1 The ID of the peer receiving the 'not interested' message.
	 * @param peerId2 The ID of the peer sending the 'not interested' message.
	 */
	public synchronized void logForReceivingNotInterestedMessage(int peerId1, int peerId2){

		// StringBuilder for efficient string concatenation
		StringBuilder logRecord = new StringBuilder();

		// Generating a timestamp in a specified format
		String timeStamp = new SimpleDateFormat("y-M-d 'at' h:m:s a z").format(Calendar.getInstance().getTime());
		// Appending the timestamp and a message indicating the receipt of the 'not interested' message
		logRecord.append("["+timeStamp + "]: Peer [" + peerId1 + "] received the 'not interested' message from [" + peerId2 + "].");

		try {
			// Call the logHelper method to handle the actual logging
			logHelper(logRecord);
		}
		catch (Exception e) {
			// Print the stack trace of any exceptions for debugging purposes
			e.printStackTrace();
		}

	}


	/**
	 * Logs the event of a peer receiving a 'request' message from another peer.
	 * This method constructs a log message that includes the current timestamp,
	 * the IDs of both the receiving and sending peers, and the index of the piece
	 * being requested. The message indicates that the sending peer is requesting
	 * a specific piece of data from the receiving peer.
	 *
	 * @param peerId1 The ID of the peer receiving the 'request' message.
	 * @param peerId2 The ID of the peer sending the 'request' message.
	 * @param idx     The index of the piece that is the subject of the 'request' message.
	 */
	public synchronized void logForReceivingRequestMessage(int peerId1, int peerId2, int idx){

		// StringBuilder for efficient string concatenation
		StringBuilder logRecord = new StringBuilder();

		// Generating a timestamp in a specified format
		String timeStamp = new SimpleDateFormat("y-M-d 'at' h:m:s a z").format(Calendar.getInstance().getTime());
		// Appending the timestamp and a message indicating the receipt of the 'request' message
		logRecord.append("["+timeStamp + "]: Peer [" + peerId1 + "] received the 'request' message from [" + peerId2 + "] for the piece " + idx + " .");

		try {
			// Call the logHelper method to handle the actual logging
			logHelper(logRecord);
		}
		catch (Exception e) {
			// Print the stack trace of any exceptions for debugging purposes
			e.printStackTrace();
		}
	}



	/**
	 * Logs the event of a peer downloading a specific piece of data from another peer.
	 * This method constructs a log message that includes the current timestamp, the IDs
	 * of both the downloading and providing peers, the index of the piece being downloaded,
	 * and the total number of pieces the downloading peer now has.
	 *
	 * @param peerId1   The ID of the peer downloading the piece.
	 * @param peerId2   The ID of the peer from which the piece is downloaded.
	 * @param idx       The index of the piece being downloaded.
	 * @param no_pieces The total number of pieces the downloading peer now possesses.
	 */
	public synchronized void logForDownloadingAPiece(int peerId1, int peerId2, int idx, int no_pieces){

		// StringBuilder for efficient string concatenation
		StringBuilder logRecord = new StringBuilder();

		// Generating a timestamp in a specified format
		String timeStamp = new SimpleDateFormat("y-M-d 'at' h:m:s a z").format(Calendar.getInstance().getTime());
		// Appending the timestamp and a message indicating the downloading of the piece
		logRecord.append("["+timeStamp + "]: Peer [" + peerId1 + "] has downloaded the piece " + idx + " from [" + peerId2 + "]. " + "Now the number of pieces it has is : " + no_pieces + ".");

		try {
			// Call the logHelper method to handle the actual logging
			logHelper(logRecord);
		}
		catch (Exception e) {
			// Print the stack trace of any exceptions for debugging purposes
			e.printStackTrace();
		}
	}



	/**
	 * Logs the event of a peer completing the download of the entire file.
	 * This method constructs a log message that includes the current timestamp
	 * and the ID of the peer that has finished downloading the file.
	 *
	 * @param peerId The ID of the peer that has completed the file download.
	 */
	public synchronized void logforCompletionOfDownload(int peerId){

		// StringBuilder for efficient string concatenation
		StringBuilder logRecord = new StringBuilder();

		// Generating a timestamp in a specified format
		String timeStamp = new SimpleDateFormat("y-M-d 'at' h:m:s a z").format(Calendar.getInstance().getTime());
		// Appending the timestamp and a message indicating the completion of the file download
		logRecord.append("["+timeStamp + "]: Peer [" + peerId + "] has downloaded the complete file.");

		try {
			// Call the logHelper method to handle the actual logging
			logHelper(logRecord);
		}
		catch (Exception e) {
			// Print the stack trace of any exceptions for debugging purposes
			e.printStackTrace();
		}
	}


	/**
	 * Logs the event marking the completion of the download process by all peers in the network.
	 * This method constructs a log message that includes the current timestamp and a notification
	 * that all peers have finished downloading, indicating that the service will be stopped.
	 */
	public synchronized void logForCompletionOfProcess(){
		// StringBuilder for efficient string concatenation
		StringBuilder logRecord = new StringBuilder();

		// Generating a timestamp in a specified format
		String timeStamp = new SimpleDateFormat("y-M-d 'at' h:m:s a z").format(Calendar.getInstance().getTime());
		// Appending the timestamp and a message indicating the completion of the process by all peers
		logRecord.append("["+timeStamp + "]: All peers have finished downloading. So stopping the service");

		try {
			// Call the logHelper method to handle the actual logging
			logHelper(logRecord);
		}
		catch (Exception e) {
			// Print the stack trace of any exceptions for debugging purposes
			e.printStackTrace();
		}
	}
	/**
	 * Logs the details of the Common.cfg configuration file after it has been read.
	 * This includes various configuration parameters such as number of neighbors,
	 * unchoking intervals, file name, file size, and chunk size.
	 *
	 * @param id   The unique identifier of the peer that reads the configuration file.
	 * @param cfg  The ConfigFile object containing the configuration details.
	 */
	public synchronized void readCommonCfgFile(int id, CommonConfigClass cfg) {
		// StringBuilder to construct the log message
		StringBuilder logRecord = new StringBuilder();

		// Getting the current timestamp in a specific format
		String timeStamp = new SimpleDateFormat("y-M-d 'at' h:m:s a z").format(Calendar.getInstance().getTime());
		// Appending the timestamp and details from the configuration file to the log record
		logRecord.append("["+timeStamp + "]: The Peer [" + id + "] scanned the Common.cfg file. \nPreferred Neighbours = " + cfg.getNumberOfNeighbors() + " UnchokingInterval = " + cfg.getUnchokingInterval() + " Optimistic UnchokingInterval = " + cfg.getOptimisticUnchokingInterval() + " File name= " + cfg.getFile() + " File size = " + cfg.getFileSize() + " Chunk size = " + cfg.getChunkSize());
		try {
			logHelper(logRecord);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void logHelper(StringBuilder logRecord) {
		try {
			// Writing the constructed log message to the logger
			logger.write(logRecord.toString());
			// Writing a new line to the logger
			logger.newLine();
			// Flushing the stream to ensure immediate writing
			logger.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}